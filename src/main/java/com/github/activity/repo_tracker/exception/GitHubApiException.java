package com.github.activity.repo_tracker.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GitHubApiException extends RuntimeException {

    private final HttpStatus status;

    public GitHubApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public GitHubApiException(String message) {
        this(message, HttpStatus.BAD_GATEWAY);
    }
}