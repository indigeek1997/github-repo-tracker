# GitHub Repository Activity Tracker

A Spring Boot application that fetches the latest public repositories and recent commits for a given GitHub user using the GitHub API.

---

## 🚀 Features

- Accepts a GitHub username as input
- Fetches public repositories for that user
- For each repository, retrieves the latest 20 commits
- Outputs structured Java objects (POJOs) with:
    - Repository name
    - Commit message
    - Author
    - Timestamp
- Gracefully handles:
    - API pagination
    - Rate limits
    - Errors (e.g., invalid username or token)
- Clean exception responses in JSON format
- Includes unit tests with HTTP client mocking

---

## 🧪 Technologies Used

- Java 17+
- Spring Boot
- Jackson
- Java HttpClient (no external REST libraries)
- JUnit 5 + Mockito
- Lombok

---

## ⚙️ Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/<your-username>/github-repo-tracker.git
cd github-repo-tracker
```
---


### 2. Configure the GitHub Token

To run the application, simply replace the placeholder &lt;`PASTE_YOUR_PERSONAL_ACCESS_TOKEN`&gt;  in `src/main/resources/application.properties` file with your github personal access token:

```properties
github.token=<PASTE_YOUR_PERSONAL_ACCESS_TOKEN>
```
👉 Generate your **GitHub Personal Access Token (PAT)** by following this official guide:  
🔗 [Creating a personal access token on GitHub](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic)

### 🔐 Required Scopes for Token:

For this app, you can generate a classic token with:

- `repo` (for accessing your public repositories)
- `read:user` (to fetch basic user details)

---

### 3. Run the Application

Use the following command:

```bash
./mvnw spring-boot:run
```

> Alternatively, you can run the `RepoTrackerApplication` class from your IDE.

---

### 🌐 Usage

Once the application is running, you can access the endpoint using a browser or Postman.

### Endpoint:

```
GET http://localhost:8080/api/repos/{githubUsername}
```

Replace `{githubUsername}` with any valid GitHub username.

---

### ✅ Example Success Response (HTTP 200)

```json
[
    {
        "repoName": "github-repo-tracker",
        "commits": [
            {
                "message": "Initial GitHub Repo Tracker Spring Boot project setup",
                "author": "Satyam Kumar",
                "timestamp": "2025-07-26T07:14:07Z"
            }
        ]
    },
    {
        "repoName": "myAppSample",
        "commits": [
            {
                "message": "first commit",
                "author": "Satyam Kumar",
                "timestamp": "2023-08-14T19:20:56Z"
            },
            {
                "message": "added readme",
                "author": "Satyam",
                "timestamp": "2020-10-03T09:11:55Z"
            },
            {
                "message": "login form",
                "author": "Satyam",
                "timestamp": "2020-10-03T07:36:50Z"
            }
        ]
    },
    {
        "repoName": "sample",
        "commits": []
    }
]
```

---

## ❌ Example Error Responses

### Invalid Username (HTTP 404)
```json
{
  "error": "GitHub API Error",
  "message": "GitHub user not found: userNonExistent123",
  "status": 404
}
```

### Missing or Invalid Token (HTTP 401)
```json
{
  "error": "GitHub API Error",
  "message": "Unauthorized - Invalid or missing GitHub token",
  "status": 401
}
```
