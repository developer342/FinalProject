package com.example.FinalServer.common.exception;

public class InvalidRefreshTokenException extends RuntimeException{

  public InvalidRefreshTokenException(String message){super(message);}
}
