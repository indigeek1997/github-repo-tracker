package com.github.activity.repo_tracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.activity.repo_tracker.exception.GitHubApiException;
import com.github.activity.repo_tracker.model.CommitInfo;
import com.github.activity.repo_tracker.model.RepoInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GitHubServiceTest {

    private GitHubService gitHubService;
    private HttpClient mockHttpClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockHttpClient = mock(HttpClient.class);
        objectMapper = new ObjectMapper();

        gitHubService = new GitHubService(mockHttpClient, objectMapper);
        gitHubService.setGitHubToken("dummy_token");
    }

    @Test
    public void fetchUserRepoData_shouldReturnRepoInfoList() throws Exception {
        String username = "testUser";

        //  Page 1 response with 1 repo
        String repoJson = "[{\"name\": \"demo-repo\"}]";
        HttpResponse<String> repoResponse1 = mock(HttpResponse.class);
        when(repoResponse1.statusCode()).thenReturn(200);
        when(repoResponse1.body()).thenReturn(repoJson);

        //  Page 2 response empty (pagination break)
        HttpResponse<String> repoResponse2 = mock(HttpResponse.class);
        when(repoResponse2.statusCode()).thenReturn(200);
        when(repoResponse2.body()).thenReturn("[]");

        // Commit response for the repo
        String commitJson = "[{\"commit\": {\"message\": \"Init commit\", \"author\": {\"name\": \"Satyam\", \"date\": \"2025-07-26T10:00:00Z\"}}}]";
        HttpResponse<String> commitResponse = mock(HttpResponse.class);
        when(commitResponse.statusCode()).thenReturn(200);
        when(commitResponse.body()).thenReturn(commitJson);

        //  Configure sequential mock responses
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(repoResponse1)  // Page 1 repos
                .thenReturn(commitResponse) // commits for repo
                .thenReturn(repoResponse2); // Page 2 empty → breaks pagination

        //  Execute
        List<RepoInfo> repos = gitHubService.fetchUserRepoData(username);

        // Assert
        assertNotNull(repos);
        assertEquals(1, repos.size());

        RepoInfo repo = repos.get(0);
        assertEquals("demo-repo", repo.getRepoName());

        List<CommitInfo> commits = repo.getCommits();
        assertEquals(1, commits.size());
        assertEquals("Init commit", commits.get(0).getMessage());
        assertEquals("Satyam", commits.get(0).getAuthor());
        assertEquals("2025-07-26T10:00:00Z", commits.get(0).getTimestamp());
    }

    @Test
    void fetchUserRepoData_shouldThrowExceptionOnRepoFailure() throws Exception {
        HttpResponse<String> repoResponse = mock(HttpResponse.class);
        when(repoResponse.statusCode()).thenReturn(404);
        when(repoResponse.body()).thenReturn("Not Found");

        when(mockHttpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(repoResponse);

        GitHubApiException ex = assertThrows(GitHubApiException.class,
                () -> gitHubService.fetchUserRepoData("invaliduser"));

        assertTrue(ex.getMessage().contains("Failed to fetch repos"));
    }
}