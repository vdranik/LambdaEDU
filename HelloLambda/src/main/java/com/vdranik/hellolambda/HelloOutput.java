package com.vdranik.hellolambda;

public class HelloOutput {

  private String message;
  private String functionName;
  private int memoryLimit;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getFunctionName() {
    return functionName;
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  public int getMemoryLimit() {
    return memoryLimit;
  }

  public void setMemoryLimit(int memoryLimit) {
    this.memoryLimit = memoryLimit;
  }
}
