# Agentic Exam Framework

A comprehensive AI-powered examination framework built with Spring Boot, featuring student registration, AI question generation, real-time exams, and intelligent evaluation with detailed reporting.

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.

## Features

- **Student Registration**: Complete registration system with validation
- **Topic Selection**: Multiple exam topics with different difficulty levels
- **AI Question Generation**: OpenAI-powered question generation for dynamic content
- **Timer Functionality**: Real-time exam timer with automatic submission
- **Answer Submission**: Interactive exam interface with multiple choice questions
- **AI Evaluation**: Intelligent answer evaluation using OpenAI
- **Detailed Reports**: Comprehensive performance analysis and recommendations
- **In-memory H2 Database**: Embedded database for easy deployment
- **Export Options**: PDF and JSON report export functionality
- **Responsive Web Interface**: Modern UI built with Bootstrap and Thymeleaf

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- OpenAI API Key (for AI features)

## Setup Instructions

### 1. Clone or Download the Project

```bash
git clone <repository-url>
cd aentic_exam_framework
```

### 2. Configure OpenAI API Key

**Option 1: Environment Variable (Recommended)**
```bash
export OPENAI_API_KEY=your-openai-api-key-here
```

**Option 2: Application Properties**
Edit `src/main/resources/application.properties` and replace:
```properties
openai.api.key=${OPENAI_API_KEY:your-openai-api-key-here}
```
with your actual OpenAI API key:
```properties
openai.api.key=sk-your-actual-openai-api-key
```

### 3. Build and Run the Application

**Option 1: Run as JAR**
```bash
mvn clean package
java -jar target/exam-framework-1.0.0.jar
```

**Option 2: Run with Maven**
```bash
mvn spring-boot:run
```

### 4. Access the Application

Open your web browser and navigate to:
```
http://localhost:8080
```

## Usage Guide

### 1. Student Registration
- Visit `http://localhost:8080/students/register`
- Fill in the registration form with valid details
- Upon successful registration, you'll be redirected to select a topic

### 2. Select Exam Topic
- Choose from available topics (Java, Spring, Database, Web Development, etc.)
- Each topic has different difficulty levels and time limits
- Click "Start Exam" to begin

### 3. Take Exam
- Answer multiple-choice questions within the time limit
- Timer displays remaining time in the top-right corner
- Submit answers when complete or wait for automatic submission

### 4. View Results
- Immediate score display with percentage and grade
- Detailed performance analysis
- Download reports as PDF or JSON

### 5. Access Reports
- View detailed reports with AI-powered insights
- Export reports in multiple formats
- Track performance over multiple exams

## API Endpoints

### Student Management
- `GET /students/api/{id}` - Get student by ID
- `GET /students/api/email/{email}` - Get student by email
- `POST /students/register` - Register new student
- `PUT /students/api/{id}` - Update student details
- `DELETE /students/api/{id}` - Delete student

### Topic Management
- `GET /topics/api/{id}` - Get topic by ID
- `GET /topics/api/all` - Get all topics
- `GET /topics/api/difficulty/{level}` - Get topics by difficulty
- `POST /topics/api` - Create new topic
- `PUT /topics/api/{id}` - Update topic
- `DELETE /topics/api/{id}` - Delete topic

### Exam Management
- `GET /exams/api/{id}` - Get exam by ID
- `GET /exams/api/code/{examCode}` - Get exam by code
- `GET /exams/api/student/{studentId}` - Get exams by student
- `POST /exams/submit` - Submit exam answers
- `GET /exams/{examId}/questions` - Get exam questions

### Report Management
- `GET /reports/api/{id}` - Get report by ID
- `GET /reports/api/code/{reportCode}` - Get report by code
- `GET /reports/export/pdf/{reportCode}` - Export PDF report
- `GET /reports/export/json/{reportCode}` - Export JSON report

## Database Access

The application uses an in-memory H2 database. You can access the H2 console at:
```
http://localhost:8080/h2-console
```

**Connection Details:**
- JDBC URL: `jdbc:h2:mem:examdb`
- Username: `sa`
- Password: `password`

## Configuration

### Application Properties
Key configuration options in `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:h2:mem:examdb
spring.datasource.username=sa
spring.datasource.password=password

# OpenAI Configuration
openai.api.key=${OPENAI_API_KEY:your-openai-api-key-here}

# Logging Configuration
logging.level.com.aentic.exam=DEBUG
```

## Sample Data

The application automatically loads sample data including:
- 6 exam topics (Java, Spring, Database, Web Development, Data Structures, System Design)
- 25+ sample questions across different difficulty levels
- Pre-configured exam durations and question counts

## Building the JAR

To create a runnable JAR file:

```bash
mvn clean package
```

The JAR file will be created at:
```
target/exam-framework-1.0.0.jar
```

Run the JAR:
```bash
java -jar target/exam-framework-1.0.0.jar
```

## AI Features

### Question Generation
- Uses OpenAI GPT-3.5-turbo for intelligent question creation
- Generates questions based on topic and difficulty level
- Includes multiple choice options, correct answers, and explanations
- Falls back to basic generation if OpenAI is unavailable

### Report Analysis
- AI-powered performance analysis
- Identifies strengths and improvement areas
- Provides personalized recommendations
- Generates detailed performance summaries

## Troubleshooting

### Common Issues

1. **OpenAI API Key Error**
   - Ensure your API key is valid and has sufficient credits
   - Check that the environment variable is set correctly
   - Verify the API key in application.properties

2. **Database Connection Issues**
   - The H2 database is in-memory and resets on restart
   - Access the H2 console to verify database state

3. **Port Conflicts**
   - Change server port in application.properties if 8080 is in use
   - `server.port=8081`

4. **Build Issues**
   - Ensure Java 17+ is installed and configured
   - Run `mvn clean` before `mvn package`

### Logs

Check application logs for detailed error information:
```bash
tail -f logs/application.log
```

## Support

For issues and questions:
1. Check the troubleshooting section above
2. Review application logs for error details
3. Verify OpenAI API key and connectivity
4. Ensure all prerequisites are met

## License

This project is for educational and demonstration purposes.
