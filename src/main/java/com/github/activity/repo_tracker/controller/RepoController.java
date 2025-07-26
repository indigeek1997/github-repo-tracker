package com.github.activity.repo_tracker.controller;

import com.github.activity.repo_tracker.model.RepoInfo;
import com.github.activity.repo_tracker.service.GitHubService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repos")
public class RepoController {

    private final GitHubService gitHubService;

    public RepoController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/{username}")
    public List<RepoInfo> getUserRepos(@PathVariable String username) {
        return gitHubService.fetchUserRepoData(username);
    }
}
