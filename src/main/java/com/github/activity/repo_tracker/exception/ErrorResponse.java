package com.github.activity.repo_tracker.exception;

public class ErrorResponse {
    private final String error;
    private final String message;
    private final int status;

    public ErrorResponse(String error, String message, int status) {
        this.error = error;
        this.message = message;
        this.status = status;
    }


    public String getError() {
        return error;
    }
    public String getMessage() {
        return message;
    }
    public int getStatus() {
        return status;
    }
}