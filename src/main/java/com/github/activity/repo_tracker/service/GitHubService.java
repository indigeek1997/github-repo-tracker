package com.github.activity.repo_tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.activity.repo_tracker.exception.GitHubApiException;
import com.github.activity.repo_tracker.model.CommitInfo;
import com.github.activity.repo_tracker.model.RepoInfo;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class GitHubService {

    private final HttpClient client;
    private final ObjectMapper mapper;

    private static final String GITHUB_BASE_URI = "https://api.github.com/";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String REPOS_PER_PAGE = "1";
    private static final String COMMITS_PER_PAGE = "20";

    @Setter
    @Value("${github.token}")
    private String gitHubToken;

    public GitHubService(HttpClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public List<RepoInfo> fetchUserRepoData(String username) {
        List<RepoInfo> repoInfos = new ArrayList<>();

        int page = 1;
        while (true) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GITHUB_BASE_URI + "users/" + username + "/repos?per_page=" + REPOS_PER_PAGE + "&page=" + page))
                        .header(AUTHORIZATION, BEARER + gitHubToken)
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new GitHubApiException("Failed to fetch repos for user: " + username +
                            ", Status: " + response.statusCode());
                }

                String responseBody = response.body();
                JsonNode root = mapper.readTree(responseBody);

                if (!root.isArray() || root.isEmpty()) break;

                for (JsonNode repoNode : root) {
                    JsonNode nameNode = repoNode.get("name");
                    String repoName = Objects.nonNull(nameNode) ? nameNode.asText() : "unknown-repo";

                    List<CommitInfo> commitInfos = fetchRecentCommits(username, repoName, client, mapper);
                    repoInfos.add(new RepoInfo(repoName, commitInfos));
                }

                page++;

            } catch (Exception e) {
                throw new GitHubApiException("Error fetching repositories for user: " + username + ". Reason: " + e.getMessage());
            }
        }

        return repoInfos;
    }

    private List<CommitInfo> fetchRecentCommits(String username, String repoName, HttpClient client, ObjectMapper mapper) {
        List<CommitInfo> commitInfos = new ArrayList<>();

        try {
            HttpRequest commitRequest = HttpRequest.newBuilder()
                    .uri(URI.create(GITHUB_BASE_URI + "repos/" + username + "/" + repoName + "/commits?per_page=" + COMMITS_PER_PAGE))
                    .header(AUTHORIZATION, BEARER + gitHubToken)
                    .build();

            HttpResponse<String> commitResponse = client.send(commitRequest, HttpResponse.BodyHandlers.ofString());

            if (commitResponse.statusCode() == 409) {
                // Repo is empty, skipping it gracefully
                return List.of(); // Return empty commit list
            }else if (commitResponse.statusCode() != 200) {
                throw new GitHubApiException("Failed to fetch commits for repo: " + repoName +
                        ", Status: " + commitResponse.statusCode());
            }

            String commitBody = commitResponse.body();
            JsonNode commitRoot = mapper.readTree(commitBody);

            for (JsonNode commitNode : commitRoot) {
                JsonNode commit = commitNode.get("commit");
                if (commit != null) {
                    JsonNode commitAuthor = commit.get("author");

                    String message = Objects.nonNull(commit.get("message")) ?
                            commit.get("message").asText() : "No message";
                    String author = (Objects.nonNull(commitAuthor) && Objects.nonNull(commitAuthor.get("name"))) ?
                            commitAuthor.get("name").asText() : "Unknown";
                    String timestamp = (Objects.nonNull(commitAuthor) && Objects.nonNull(commitAuthor.get("date"))) ?
                            commitAuthor.get("date").asText() : "Unknown";

                    commitInfos.add(new CommitInfo(message, author, timestamp));
                }
            }

        } catch (Exception ex) {
            throw new GitHubApiException("Error fetching commits for repo: " + repoName + ". Reason: " + ex.getMessage());
        }

        return commitInfos;
    }

}