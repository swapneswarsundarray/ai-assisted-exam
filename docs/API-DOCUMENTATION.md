# Aentic Exam Framework - API Documentation

## Table of Contents
1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Base URL](#base-url)
4. [Common Response Formats](#common-response-formats)
5. [Student APIs](#student-apis)
6. [Topic APIs](#topic-apis)
7. [Question APIs](#question-apis)
8. [Exam APIs](#exam-apis)
9. [Report APIs](#report-apis)
10. [Export APIs](#export-apis)
11. [Error Handling](#error-handling)
12. [Rate Limiting](#rate-limiting)
13. [SDK Examples](#sdk-examples)

## Overview

The Aentic Exam Framework provides a comprehensive RESTful API for managing students, topics, questions, exams, and reports. All APIs follow REST conventions and return JSON responses.

### Key Features
- **RESTful Design**: Standard HTTP methods and status codes
- **JSON Responses**: Consistent response format
- **Error Handling**: Structured error responses
- **Pagination**: For list endpoints
- **Validation**: Input validation with detailed error messages
- **AI Integration**: OpenAI-powered question generation and evaluation

## Authentication

Currently, the API uses basic authentication. In production, implement JWT or OAuth2 for better security.

### Basic Authentication
```http
Authorization: Basic base64(username:password)
```

### Example
```bash
curl -u admin:password https://api.exam-framework.com/students/api/1
```

## Base URL

```
Development: http://localhost:8080
Production: https://api.exam-framework.com
```

## Common Response Formats

### Success Response
```json
{
  "success": true,
  "data": {
    // Response data
  },
  "message": "Operation completed successfully",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input data",
    "details": [
      {
        "field": "email",
        "message": "Invalid email format"
      }
    ]
  },
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### Paginated Response
```json
{
  "success": true,
  "data": {
    "content": [
      // Array of items
    ],
    "page": {
      "number": 0,
      "size": 20,
      "totalElements": 100,
      "totalPages": 5,
      "first": true,
      "last": false
    }
  },
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## Student APIs

### Register Student

**POST** `/students/register`

Register a new student with comprehensive profile information.

#### Request Body
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "country": "United States",
  "educationLevel": "Bachelor's Degree",
  "preferredLanguage": "English",
  "interests": "Programming, Mathematics, AI",
  "profession": "Software Developer",
  "age": 25,
  "experienceYears": 3
}
```

#### Response
```json
{
  "success": true,
  "data": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "country": "United States",
    "educationLevel": "Bachelor's Degree",
    "preferredLanguage": "English",
    "interests": "Programming, Mathematics, AI",
    "profession": "Software Developer",
    "age": 25,
    "experienceYears": 3,
    "registrationDate": "2024-01-01T12:00:00Z"
  },
  "message": "Student registered successfully"
}
```

#### Example
```bash
curl -X POST http://localhost:8080/students/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "country": "United States",
    "educationLevel": "Bachelor'\''s Degree",
    "preferredLanguage": "English",
    "interests": "Programming, Mathematics, AI",
    "profession": "Software Developer",
    "age": 25,
    "experienceYears": 3
  }'
```

### Get Student by ID

**GET** `/students/api/{id}`

Retrieve student information by ID.

#### Parameters
- `id` (path): Student ID

#### Response
```json
{
  "success": true,
  "data": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "country": "United States",
    "educationLevel": "Bachelor's Degree",
    "preferredLanguage": "English",
    "interests": "Programming, Mathematics, AI",
    "profession": "Software Developer",
    "age": 25,
    "experienceYears": 3,
    "registrationDate": "2024-01-01T12:00:00Z"
  }
}
```

#### Example
```bash
curl http://localhost:8080/students/api/1
```

### Get Student by Email

**GET** `/students/api/email/{email}`

Retrieve student information by email.

#### Parameters
- `email` (path): Student email

#### Example
```bash
curl http://localhost:8080/students/api/email/john.doe@example.com
```

### Update Student

**PUT** `/students/api/{id}`

Update student information.

#### Parameters
- `id` (path): Student ID

#### Request Body
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "email": "john.smith@example.com",
  "phone": "+1234567890",
  "country": "United States",
  "educationLevel": "Master's Degree",
  "preferredLanguage": "English",
  "interests": "Programming, AI, Machine Learning",
  "profession": "Senior Software Developer",
  "age": 26,
  "experienceYears": 4
}
```

#### Example
```bash
curl -X PUT http://localhost:8080/students/api/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Smith",
    "email": "john.smith@example.com",
    "phone": "+1234567890",
    "country": "United States",
    "educationLevel": "Master'\''s Degree",
    "preferredLanguage": "English",
    "interests": "Programming, AI, Machine Learning",
    "profession": "Senior Software Developer",
    "age": 26,
    "experienceYears": 4
  }'
```

### Delete Student

**DELETE** `/students/api/{id}`

Delete a student by ID.

#### Parameters
- `id` (path): Student ID

#### Example
```bash
curl -X DELETE http://localhost:8080/students/api/1
```

### Get All Students

**GET** `/students/api/all`

Get all students with pagination.

#### Query Parameters
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort field (default: id)
- `direction` (optional): Sort direction (default: asc)

#### Example
```bash
curl "http://localhost:8080/students/api/all?page=0&size=10&sort=firstName&direction=asc"
```

## Topic APIs

### Get All Topics

**GET** `/topics/api/all`

Get all available topics.

#### Response
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Java Programming",
      "description": "Comprehensive Java programming concepts",
      "difficultyLevel": "INTERMEDIATE",
      "timeLimitMinutes": 60,
      "questionCount": 20,
      "createdAt": "2024-01-01T12:00:00Z"
    },
    {
      "id": 2,
      "name": "Spring Framework",
      "description": "Spring Boot and Spring MVC",
      "difficultyLevel": "ADVANCED",
      "timeLimitMinutes": 90,
      "questionCount": 25,
      "createdAt": "2024-01-01T12:00:00Z"
    }
  ]
}
```

#### Example
```bash
curl http://localhost:8080/topics/api/all
```

### Get Topic by ID

**GET** `/topics/api/{id}`

Get topic details by ID.

#### Parameters
- `id` (path): Topic ID

#### Example
```bash
curl http://localhost:8080/topics/api/1
```

### Get Topics by Difficulty

**GET** `/topics/api/difficulty/{level}`

Get topics filtered by difficulty level.

#### Parameters
- `level` (path): Difficulty level (BEGINNER, INTERMEDIATE, ADVANCED)

#### Example
```bash
curl http://localhost:8080/topics/api/difficulty/INTERMEDIATE
```

### Create Topic

**POST** `/topics/api`

Create a new topic (admin only).

#### Request Body
```json
{
  "name": "Python Programming",
  "description": "Python programming fundamentals",
  "difficultyLevel": "BEGINNER",
  "timeLimitMinutes": 45,
  "questionCount": 15
}
```

#### Example
```bash
curl -X POST http://localhost:8080/topics/api \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Python Programming",
    "description": "Python programming fundamentals",
    "difficultyLevel": "BEGINNER",
    "timeLimitMinutes": 45,
    "questionCount": 15
  }'
```

## Question APIs

### Get Questions by Topic

**GET** `/questions/api/topic/{topicId}`

Get all questions for a specific topic.

#### Parameters
- `topicId` (path): Topic ID

#### Response
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "questionText": "What is the purpose of the Spring Boot framework?",
      "questionType": "MULTIPLE_CHOICE",
      "options": "A: Simplify Java development, B: Database management, C: UI development, D: Network programming",
      "correctAnswer": "A",
      "explanation": "Spring Boot simplifies Java development by providing auto-configuration and starter dependencies.",
      "difficultyLevel": "INTERMEDIATE",
      "points": 1,
      "topic": {
        "id": 2,
        "name": "Spring Framework"
      },
      "createdAt": "2024-01-01T12:00:00Z"
    }
  ]
}
```

#### Example
```bash
curl http://localhost:8080/questions/api/topic/2
```

### Get Random Questions

**GET** `/questions/api/random/{topicId}/{limit}`

Get random questions for a topic.

#### Parameters
- `topicId` (path): Topic ID
- `limit` (path): Number of questions

#### Example
```bash
curl http://localhost:8080/questions/api/random/2/10
```

### Generate AI Questions

**POST** `/questions/api/generate`

Generate AI-powered questions for a topic.

#### Request Body
```json
{
  "topicId": 2,
  "count": 5,
  "difficultyLevel": "INTERMEDIATE",
  "studentId": 1
}
```

#### Response
```json
{
  "success": true,
  "data": {
    "generatedQuestions": 5,
    "questions": [
      {
        "id": 101,
        "questionText": "How does Spring Boot's auto-configuration work?",
        "questionType": "MULTIPLE_CHOICE",
        "options": "A: Uses @EnableAutoConfiguration, B: Scans classpath automatically, C: Both A and B, D: Neither A nor B",
        "correctAnswer": "C",
        "explanation": "Spring Boot's auto-configuration uses both @EnableAutoConfiguration and classpath scanning.",
        "difficultyLevel": "INTERMEDIATE",
        "points": 1
      }
    ]
  },
  "message": "AI questions generated successfully"
}
```

#### Example
```bash
curl -X POST http://localhost:8080/questions/api/generate \
  -H "Content-Type: application/json" \
  -d '{
    "topicId": 2,
    "count": 5,
    "difficultyLevel": "INTERMEDIATE",
    "studentId": 1
  }'
```

### Create Question

**POST** `/questions/api`

Create a new question (admin only).

#### Request Body
```json
{
  "questionText": "What is dependency injection?",
  "questionType": "MULTIPLE_CHOICE",
  "options": "A: Design pattern, B: Database concept, C: UI pattern, D: Network protocol",
  "correctAnswer": "A",
  "explanation": "Dependency injection is a design pattern that implements IoC.",
  "difficultyLevel": "INTERMEDIATE",
  "points": 1,
  "topicId": 2
}
```

#### Example
```bash
curl -X POST http://localhost:8080/questions/api \
  -H "Content-Type: application/json" \
  -d '{
    "questionText": "What is dependency injection?",
    "questionType": "MULTIPLE_CHOICE",
    "options": "A: Design pattern, B: Database concept, C: UI pattern, D: Network protocol",
    "correctAnswer": "A",
    "explanation": "Dependency injection is a design pattern that implements IoC.",
    "difficultyLevel": "INTERMEDIATE",
    "points": 1,
    "topicId": 2
  }'
```

## Exam APIs

### Start Exam

**POST** `/exams/start`

Start a new exam for a student.

#### Request Body
```json
{
  "studentId": 1,
  "topicId": 2,
  "difficultyLevel": "INTERMEDIATE"
}
```

#### Response
```json
{
  "success": true,
  "data": {
    "id": 1,
    "examCode": "EXAM-2024-001",
    "student": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe"
    },
    "topic": {
      "id": 2,
      "name": "Spring Framework"
    },
    "startTime": "2024-01-01T12:00:00Z",
    "endTime": null,
    "timeLimitMinutes": 90,
    "questions": [
      {
        "id": 1,
        "questionText": "What is Spring Boot?",
        "options": "A: Framework, B: Library, C: Tool, D: IDE",
        "questionType": "MULTIPLE_CHOICE"
      }
    ]
  },
  "message": "Exam started successfully"
}
```

#### Example
```bash
curl -X POST http://localhost:8080/exams/start \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "topicId": 2,
    "difficultyLevel": "INTERMEDIATE"
  }'
```

### Submit Exam

**POST** `/exams/submit`

Submit exam answers.

#### Request Body
```json
{
  "examId": 1,
  "answers": [
    {
      "questionId": 1,
      "answerText": "A",
      "timeTakenSeconds": 30
    },
    {
      "questionId": 2,
      "answerText": "B",
      "timeTakenSeconds": 45
    }
  ]
}
```

#### Response
```json
{
  "success": true,
  "data": {
    "id": 1,
    "examCode": "EXAM-2024-001",
    "totalScore": 8,
    "maxScore": 10,
    "percentage": 80.0,
    "grade": "B",
    "startTime": "2024-01-01T12:00:00Z",
    "endTime": "2024-01-01T13:30:00Z",
    "correctAnswers": 8,
    "wrongAnswers": 2
  },
  "message": "Exam submitted successfully"
}
```

#### Example
```bash
curl -X POST http://localhost:8080/exams/submit \
  -H "Content-Type: application/json" \
  -d '{
    "examId": 1,
    "answers": [
      {
        "questionId": 1,
        "answerText": "A",
        "timeTakenSeconds": 30
      },
      {
        "questionId": 2,
        "answerText": "B",
        "timeTakenSeconds": 45
      }
    ]
  }'
```

### Get Exam by ID

**GET** `/exams/api/{id}`

Get exam details by ID.

#### Parameters
- `id` (path): Exam ID

#### Example
```bash
curl http://localhost:8080/exams/api/1
```

### Get Exam by Code

**GET** `/exams/api/code/{examCode}`

Get exam details by exam code.

#### Parameters
- `examCode` (path): Exam code

#### Example
```bash
curl http://localhost:8080/exams/api/code/EXAM-2024-001
```

### Get Exams by Student

**GET** `/exams/api/student/{studentId}`

Get all exams for a student.

#### Parameters
- `studentId` (path): Student ID

#### Example
```bash
curl http://localhost:8080/exams/api/student/1
```

### Get Exam Questions

**GET** `/exams/{examId}/questions`

Get questions for a specific exam.

#### Parameters
- `examId` (path): Exam ID

#### Example
```bash
curl http://localhost:8080/exams/1/questions
```

## Report APIs

### Get Report by ID

**GET** `/reports/api/{id}`

Get report details by ID.

#### Parameters
- `id` (path): Report ID

#### Response
```json
{
  "success": true,
  "data": {
    "id": 1,
    "reportCode": "RPT-2024-001",
    "totalQuestions": 10,
    "correctAnswers": 8,
    "wrongAnswers": 2,
    "totalTimeMinutes": 90,
    "averageTimePerQuestion": 9.0,
    "strengthAreas": "Strong understanding of core concepts",
    "improvementAreas": "Need more practice on advanced topics",
    "detailedAnalysis": "Overall performance is good with room for improvement",
    "generatedAt": "2024-01-01T13:35:00Z",
    "exam": {
      "id": 1,
      "examCode": "EXAM-2024-001",
      "percentage": 80.0,
      "grade": "B"
    }
  }
}
```

#### Example
```bash
curl http://localhost:8080/reports/api/1
```

### Get Report by Code

**GET** `/reports/api/code/{reportCode}`

Get report details by report code.

#### Parameters
- `reportCode` (path): Report code

#### Example
```bash
curl http://localhost:8080/reports/api/code/RPT-2024-001
```

### Get Report by Exam ID

**GET** `/reports/api/exam/{examId}`

Get report for a specific exam.

#### Parameters
- `examId` (path): Exam ID

#### Example
```bash
curl http://localhost:8080/reports/api/exam/1
```

## Export APIs

### Export PDF Report

**GET** `/reports/export/pdf/{reportCode}`

Export report as PDF.

#### Parameters
- `reportCode` (path): Report code

#### Response
Returns PDF file with appropriate headers:
```
Content-Type: application/pdf
Content-Disposition: attachment; filename="report-RPT-2024-001.pdf"
```

#### Example
```bash
curl -o report.pdf http://localhost:8080/reports/export/pdf/RPT-2024-001
```

### Export JSON Report

**GET** `/reports/export/json/{reportCode}`

Export report as JSON.

#### Parameters
- `reportCode` (path): Report code

#### Response
Returns JSON file with appropriate headers:
```
Content-Type: application/json
Content-Disposition: attachment; filename="report-RPT-2024-001.json"
```

#### Example
```bash
curl -o report.json http://localhost:8080/reports/export/json/RPT-2024-001
```

### Export Student Results

**GET** `/reports/export/student/{studentId}/json`

Export all reports for a student as JSON.

#### Parameters
- `studentId` (path): Student ID

#### Example
```bash
curl -o student-reports.json http://localhost:8080/reports/export/student/1/json
```

## Error Handling

### HTTP Status Codes

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict |
| 422 | Unprocessable Entity |
| 500 | Internal Server Error |

### Error Response Format

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "email",
        "message": "Invalid email format"
      },
      {
        "field": "age",
        "message": "Age must be between 10 and 100"
      }
    ]
  },
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### Common Error Codes

| Error Code | Description |
|------------|-------------|
| VALIDATION_ERROR | Input validation failed |
| RESOURCE_NOT_FOUND | Resource not found |
| DUPLICATE_RESOURCE | Resource already exists |
| UNAUTHORIZED | Authentication required |
| FORBIDDEN | Access denied |
| INTERNAL_ERROR | Server error |
| EXTERNAL_SERVICE_ERROR | External API error |
| RATE_LIMIT_EXCEEDED | Too many requests |

## Rate Limiting

### Rate Limits

| Endpoint | Limit | Window |
|----------|-------|--------|
| All endpoints | 100 requests | 1 minute |
| Question generation | 10 requests | 1 minute |
| Report generation | 5 requests | 1 minute |
| Export endpoints | 20 requests | 1 minute |

### Rate Limit Headers

```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1640995200
```

### Rate Limit Exceeded Response

```json
{
  "success": false,
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Rate limit exceeded. Try again later.",
    "details": {
      "limit": 100,
      "window": "1 minute",
      "retryAfter": 30
    }
  },
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## SDK Examples

### JavaScript/Node.js

```javascript
class ExamFrameworkAPI {
  constructor(baseURL, username, password) {
    this.baseURL = baseURL;
    this.auth = 'Basic ' + Buffer.from(username + ':' + password).toString('base64');
  }

  async request(method, endpoint, data = null) {
    const response = await fetch(`${this.baseURL}${endpoint}`, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': this.auth
      },
      body: data ? JSON.stringify(data) : null
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.error?.message || 'Request failed');
    }

    return response.json();
  }

  async registerStudent(studentData) {
    return this.request('POST', '/students/register', studentData);
  }

  async getStudent(id) {
    return this.request('GET', `/students/api/${id}`);
  }

  async startExam(studentId, topicId, difficultyLevel) {
    return this.request('POST', '/exams/start', {
      studentId,
      topicId,
      difficultyLevel
    });
  }

  async submitExam(examId, answers) {
    return this.request('POST', '/exams/submit', {
      examId,
      answers
    });
  }

  async getReport(reportCode) {
    return this.request('GET', `/reports/api/code/${reportCode}`);
  }
}

// Usage
const api = new ExamFrameworkAPI('http://localhost:8080', 'admin', 'password');

// Register student
api.registerStudent({
  firstName: 'John',
  lastName: 'Doe',
  email: 'john.doe@example.com',
  phone: '+1234567890',
  educationLevel: 'Bachelor\'s Degree'
}).then(result => {
  console.log('Student registered:', result.data);
}).catch(error => {
  console.error('Error:', error.message);
});
```

### Python

```python
import requests
import base64
from typing import Dict, Any, Optional

class ExamFrameworkAPI:
    def __init__(self, base_url: str, username: str, password: str):
        self.base_url = base_url
        self.auth = base64.b64encode(f"{username}:{password}".encode()).decode()
        self.headers = {
            'Content-Type': 'application/json',
            'Authorization': f'Basic {self.auth}'
        }

    def _request(self, method: str, endpoint: str, data: Optional[Dict] = None) -> Dict[str, Any]:
        url = f"{self.base_url}{endpoint}"
        response = requests.request(method, url, json=data, headers=self.headers)
        
        if not response.ok:
            error = response.json()
            raise Exception(error.get('error', {}).get('message', 'Request failed'))
        
        return response.json()

    def register_student(self, student_data: Dict) -> Dict[str, Any]:
        return self._request('POST', '/students/register', student_data)

    def get_student(self, student_id: int) -> Dict[str, Any]:
        return self._request('GET', f'/students/api/{student_id}')

    def start_exam(self, student_id: int, topic_id: int, difficulty_level: str) -> Dict[str, Any]:
        return self._request('POST', '/exams/start', {
            'studentId': student_id,
            'topicId': topic_id,
            'difficultyLevel': difficulty_level
        })

    def submit_exam(self, exam_id: int, answers: list) -> Dict[str, Any]:
        return self._request('POST', '/exams/submit', {
            'examId': exam_id,
            'answers': answers
        })

    def get_report(self, report_code: str) -> Dict[str, Any]:
        return self._request('GET', f'/reports/api/code/{report_code}')

# Usage
api = ExamFrameworkAPI('http://localhost:8080', 'admin', 'password')

# Register student
try:
    result = api.register_student({
        'firstName': 'John',
        'lastName': 'Doe',
        'email': 'john.doe@example.com',
        'phone': '+1234567890',
        'educationLevel': 'Bachelor\'s Degree'
    })
    print('Student registered:', result['data'])
except Exception as e:
    print(f'Error: {e}')
```

### Java

```java
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.Base64Utils;
import java.util.*;

public class ExamFrameworkAPI {
    private final String baseURL;
    private final RestTemplate restTemplate;
    private final HttpHeaders headers;

    public ExamFrameworkAPI(String baseURL, String username, String password) {
        this.baseURL = baseURL;
        this.restTemplate = new RestTemplate();
        this.headers = new HttpHeaders();
        
        String auth = username + ":" + password;
        String encodedAuth = Base64Utils.encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.set("Content-Type", "application/json");
    }

    public Map<String, Object> registerStudent(Map<String, Object> studentData) {
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(studentData, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseURL + "/students/register", request, Map.class);
        return response.getBody();
    }

    public Map<String, Object> getStudent(Long id) {
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            baseURL + "/students/api/" + id, HttpMethod.GET, request, Map.class);
        return response.getBody();
    }

    public Map<String, Object> startExam(Long studentId, Long topicId, String difficultyLevel) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("studentId", studentId);
        requestData.put("topicId", topicId);
        requestData.put("difficultyLevel", difficultyLevel);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestData, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseURL + "/exams/start", request, Map.class);
        return response.getBody();
    }

    public Map<String, Object> submitExam(Long examId, List<Map<String, Object>> answers) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("examId", examId);
        requestData.put("answers", answers);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestData, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseURL + "/exams/submit", request, Map.class);
        return response.getBody();
    }

    public Map<String, Object> getReport(String reportCode) {
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            baseURL + "/reports/api/code/" + reportCode, HttpMethod.GET, request, Map.class);
        return response.getBody();
    }
}

// Usage
ExamFrameworkAPI api = new ExamFrameworkAPI("http://localhost:8080", "admin", "password");

// Register student
Map<String, Object> studentData = new HashMap<>();
studentData.put("firstName", "John");
studentData.put("lastName", "Doe");
studentData.put("email", "john.doe@example.com");
studentData.put("phone", "+1234567890");
studentData.put("educationLevel", "Bachelor's Degree");

try {
    Map<String, Object> result = api.registerStudent(studentData);
    System.out.println("Student registered: " + result.get("data"));
} catch (Exception e) {
    System.err.println("Error: " + e.getMessage());
}
```

This comprehensive API documentation provides all the necessary information to integrate with the Aentic Exam Framework, including detailed examples in multiple programming languages.
