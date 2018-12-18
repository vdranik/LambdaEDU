package com.vdranik.hellolambdacaller;

import com.google.gson.Gson;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaAsyncClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.vdranik.hellolambda.HelloInput;
import com.vdranik.hellolambda.HelloOutput;

public class HelloLambdaCaller {

  public static void main(String[] args) {
    String name = "Vova";
    String region = "eu-central-1";

    if(args.length > 0){
      name = args[0];
    }

    if(args.length > 1){
      region = args[1];
    }

    Gson gson = new Gson();
    AWSLambdaAsyncClient awsLambdaAsyncClient = new AWSLambdaAsyncClient(
        new ProfileCredentialsProvider("hellolambda"))
          .withRegion(Regions.fromName(region));

    HelloInput in = new HelloInput();
    in.setName(name);
    InvokeRequest request = new InvokeRequest().withFunctionName("HelloLambda").withPayload(gson.toJson(in));
    InvokeResult result = awsLambdaAsyncClient.invoke(request);
    String s = java.nio.charset.StandardCharsets.UTF_8.decode(result.getPayload()).toString();
    HelloOutput out = gson.fromJson(s, HelloOutput.class);
    System.out.println("Message: " + out.getMessage());
    System.out.println("FunctionName: " + out.getFunctionName());
    System.out.println("Memory: " + out.getMemoryLimit());
  }
}
