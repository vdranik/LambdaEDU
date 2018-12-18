package com.vdranik.hellolambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class HelloHandler implements RequestHandler<HelloInput, HelloOutput> {

  public HelloOutput handleRequest(HelloInput helloInput, Context context) {
    HelloOutput helloOutput = new HelloOutput();
    helloOutput.setMessage("Hello " + helloInput.getName());
    helloOutput.setFunctionName(context.getFunctionName());
    helloOutput.setMemoryLimit(context.getMemoryLimitInMB());

    context.getLogger().log(helloInput.getName() + "said Hello");
    return helloOutput;
  }
}
