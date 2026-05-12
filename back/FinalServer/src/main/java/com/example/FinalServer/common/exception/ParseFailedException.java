package com.example.FinalServer.common.exception;

/** OCR 파싱 실패 예외 */
public class ParseFailedException extends RuntimeException {
    public ParseFailedException(String message) {
        super(message);
    }

    public ParseFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
