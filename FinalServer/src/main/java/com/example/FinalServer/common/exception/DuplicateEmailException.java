package com.example.FinalServer.common.exception;

public class DuplicateEmailException extends RuntimeException{

  public DuplicateEmailException(String message){ super(message);}
}
