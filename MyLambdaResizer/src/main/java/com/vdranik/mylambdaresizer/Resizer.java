package com.vdranik.mylambdaresizer;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.apache.commons.io.FileUtils;
import org.imgscalr.Scalr;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.UUID;
import javax.imageio.ImageIO;

public class Resizer implements RequestHandler<ResizerInput, String> {

  private AmazonS3 s3Client;

  public String handleRequest(ResizerInput resizerInput, Context context) {

    String resizedUrl = createUrl(resizerInput, context);
    if(!alreadyExists(resizedUrl)){
      BufferedImage originalImage = readImage(resizerInput, context);
      if (originalImage != null){
        InputStream resizedImage = resizeImage(originalImage, resizerInput, context);
        if(resizedImage != null){
          if(!storeImageInS3(resizedImage, resizedUrl, context)){
            return "Failed to store image in S3";
          } else return resizedUrl;
        } else {
          return "Failed to resize Image";
        }
      } else {
        return "Failed to read Original Image";
      }
    }
    return resizedUrl;
  }

  private String createUrl(ResizerInput in, Context context){
    String resizedUrl = "";
    String publicUrl = System.getenv("publicurl");
    String fullHash = "" + Math.abs(in.getUrl().hashCode());
    String fileName = "";
    try {
      fileName = Paths.get(new URI(in.getUrl()).getPath()).getFileName().toString();
    } catch (URISyntaxException e) {
      context.getLogger().log("Unable to create url: " + in.getUrl() + " " + e.getMessage());
    }

    resizedUrl = publicUrl + fileName + "-" + fullHash + "-" + in.getWidth() + "-" + in.getHeight();
    return resizedUrl;
  }

  private BufferedImage readImage(ResizerInput in, Context context){
    try {
      return ImageIO.read(new URL(in.getUrl()).openStream());
    } catch (IOException e) {
      context.getLogger().log("Failed to read original url: " + in.getUrl() + " " + e.getMessage());
      return null;
    }
  }

  private InputStream resizeImage(BufferedImage bufferedImage, ResizerInput in, Context context){
    try {
      BufferedImage img = Scalr.resize(bufferedImage, Scalr.Method.BALANCED,
        Scalr.Mode.AUTOMATIC, in.getWidth(), in.getHeight(), Scalr.OP_ANTIALIAS);

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ImageIO.write(img, "gif", os);
      InputStream is = new ByteArrayInputStream(os.toByteArray());
      return is;
    } catch (IOException e) {
      context.getLogger().log("Image resizing failed : " + in.getUrl() + " " + e.getMessage());
      return null;
    }
  }

  private AmazonS3 getS3Client(){
    if(s3Client == null){
      s3Client = new AmazonS3Client();
    }
    return s3Client;
  }

  private String getS3Key(String resizedUrl){
    try {
      return Paths.get(new URI(resizedUrl).getPath()).getFileName().toString();
    } catch (URISyntaxException e) {
      return "";
    }
  }

  private Boolean storeImageInS3(InputStream is, String resizedUrl, Context context){
    String s3Key = getS3Key(resizedUrl);
    String bucketName = System.getenv("bucketname");
    File tempFile = null;
    try {
      tempFile = File.createTempFile(UUID.randomUUID().toString(), ".gif");
      FileUtils.copyInputStreamToFile(is, tempFile);
      PutObjectRequest request = new PutObjectRequest(bucketName, s3Key, tempFile).withCannedAcl(CannedAccessControlList.PublicRead);
      PutObjectResult result = getS3Client().putObject(request);
      context.getLogger().log("Stored in s3 " + bucketName + "/" +s3Key);
    } catch (IOException e) {
      context.getLogger().log("Error createing temp file : " + " " + e.getMessage());
    } finally {
        if(tempFile != null) {
          tempFile.delete();
        }

        return true;
    }
  }

  private Boolean alreadyExists(String resizedUrl){
    String bucketName = System.getenv("bucketname");
    try {
      getS3Client().getObject(bucketName, getS3Key(resizedUrl));
    } catch (AmazonServiceException e){
      return false;
    }
    return true;
  }
}
