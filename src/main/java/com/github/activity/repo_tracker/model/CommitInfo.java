package com.github.activity.repo_tracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitInfo {
    private String message;
    private String author;
    private String timestamp;
}
