# Aentic Exam Framework - Architecture Documentation

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture Components](#architecture-components)
3. [Technology Stack](#technology-stack)
4. [System Architecture](#system-architecture)
5. [Data Flow](#data-flow)
6. [Security Architecture](#security-architecture)
7. [Scalability Architecture](#scalability-architecture)
8. [Deployment Architecture](#deployment-architecture)

## System Overview

The Aentic Exam Framework is a comprehensive AI-powered examination system built on Spring Boot that provides personalized question generation, real-time exam management, and intelligent evaluation capabilities.

### Key Features
- **AI-Powered Question Generation**: OpenAI integration for personalized questions
- **Real-time Exam Management**: Timer-based examinations with automatic submission
- **Intelligent Evaluation**: AI-driven answer evaluation and detailed reporting
- **Multi-format Export**: PDF and JSON report generation
- **Responsive Web Interface**: Modern Bootstrap-based UI
- **Student Profiling**: Comprehensive student profiles for personalized learning

## Architecture Components

### 1. Presentation Layer
```
┌─────────────────────────────────────────────────────────────┐
│                    Web Interface Layer                       │
├─────────────────────────────────────────────────────────────┤
│  • Thymeleaf Templates (HTML/Thymeleaf)                     │
│  • Bootstrap CSS Framework                                  │
│  • JavaScript (Client-side validation, timers)              │
│  • REST API Endpoints (JSON responses)                      │
└─────────────────────────────────────────────────────────────┘
```

### 2. Application Layer
```
┌─────────────────────────────────────────────────────────────┐
│                   Application Layer                          │
├─────────────────────────────────────────────────────────────┤
│  • Controllers (Student, Topic, Exam, Report Controllers)   │
│  • Service Layer (Business Logic)                           │
│  • DTOs (Data Transfer Objects)                             │
│  • Validation Framework                                      │
│  • OpenAI Integration Service                               │
└─────────────────────────────────────────────────────────────┘
```

### 3. Business Logic Layer
```
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                             │
├─────────────────────────────────────────────────────────────┤
│  • StudentService (Registration, Profile Management)         │
│  • TopicService (Topic Management)                           │
│  • QuestionService (AI Question Generation)                   │
│  • ExamService (Exam Management, Timer)                      │
│  • ReportService (AI Evaluation, Analysis)                   │
│  • ExportService (PDF/JSON Generation)                       │
│  • OpenAIService (AI Integration)                            │
└─────────────────────────────────────────────────────────────┘
```

### 4. Data Access Layer
```
┌─────────────────────────────────────────────────────────────┐
│                    Data Access Layer                         │
├─────────────────────────────────────────────────────────────┤
│  • Spring Data JPA Repositories                              │
│  • Entity Classes (Student, Topic, Question, etc.)           │
│  • Database Configuration                                    │
│  • Transaction Management                                   │
└─────────────────────────────────────────────────────────────┘
```

### 5. Database Layer
```
┌─────────────────────────────────────────────────────────────┐
│                     Database Layer                           │
├─────────────────────────────────────────────────────────────┤
│  • H2 Database (Development/Testing)                        │
│  • PostgreSQL/MySQL (Production)                            │
│  • Connection Pooling (HikariCP)                            │
│  • Database Migrations (Flyway)                             │
└─────────────────────────────────────────────────────────────┘
```

## Technology Stack

### Core Framework
- **Spring Boot 3.2.0** - Main application framework
- **Spring Data JPA** - Database access and ORM
- **Spring Security** - Authentication and authorization
- **Spring MVC** - Web framework and REST APIs

### Database & Persistence
- **H2 Database** - In-memory database for development
- **PostgreSQL/MySQL** - Production database options
- **HikariCP** - Connection pooling
- **Flyway** - Database migrations

### Frontend Technologies
- **Thymeleaf** - Server-side templating engine
- **Bootstrap 5.1.3** - CSS framework for responsive UI
- **JavaScript** - Client-side functionality
- **HTML5/CSS3** - Modern web standards

### AI & External Services
- **OpenAI API** - AI question generation and evaluation
- **GPT-3.5-turbo** - Language model for question generation
- **HTTP Client** - External API communication

### Report Generation
- **iText 7** - PDF generation library
- **Jackson** - JSON processing
- **Apache Commons** - Utility libraries

### Development & Build Tools
- **Maven 3.6+** - Build and dependency management
- **Java 17** - Programming language
- **JUnit 5** - Unit testing
- **Mockito** - Mock testing framework

## System Architecture

### High-Level Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                    Load Balancer                            │
│                    (Nginx/HAProxy)                          │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────────┐
│                  Application Server                         │
│                (Spring Boot + Tomcat)                       │
├─────────────────────┬───────────────────────────────────────┤
│  Web Layer          │  Service Layer                        │
│  ├─ Controllers     │  ├─ Business Logic                    │
│  ├─ REST APIs       │  ├─ OpenAI Integration                │
│  └─ Thymeleaf       │  └─ Transaction Management           │
├─────────────────────┴───────────────────────────────────────┤
│                    Data Access Layer                        │
│  ├─ Spring Data JPA │  ├─ Repositories                      │
│  ├─ Entity Classes  │  └─ Database Operations              │
└─────────────────────┴───────────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────────┐
│                    Database Cluster                         │
│              (PostgreSQL/MySQL with Replication)            │
└─────────────────────────────────────────────────────────────┘
```

### Microservices Architecture (Optional for Large Scale)
```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway                              │
│                 (Spring Cloud Gateway)                       │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────┼───────────────────────────────────────┐
│  Student Service    │  Exam Service                         │
│  (Registration,     │  (Question Generation,                │
│   Profile Mgmt)     │   Timer, Submission)                  │
└─────────────────────┼───────────────────────────────────────┘
                      │
┌─────────────────────┼───────────────────────────────────────┐
│  Question Service   │  Report Service                       │
│  (AI Generation,    │  (Evaluation, Analysis,               │
│   Management)       │   Export)                             │
└─────────────────────┼───────────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────────┐
│                    Shared Services                         │
│  ├─ OpenAI Service  │  ├─ Notification Service              │
│  ├─ Export Service  │  └─ Logging Service                   │
└─────────────────────────────────────────────────────────────┘
```

## Data Flow

### Student Registration Flow
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Browser   │───▶│ Controller  │───▶│   Service   │───▶│  Database   │
│   (Form)    │    │   (POST)    │    │   Layer     │    │   (H2)     │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │                   │
       │◀──────────────────│◀──────────────────│◀──────────────────│
       │   (Success)       │   (Student)       │   (Entity)        │
       │                   │                   │                   │
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Redirect  │◀───│   View      │◀───│   DTO       │◀───│   Result    │
│   (Topics)  │    │   (Thymeleaf)│   │   Mapping   │    │   (Saved)   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

### AI Question Generation Flow
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Exam      │───▶│ Question    │───▶│  OpenAI     │───▶│  OpenAI     │
│   Service   │    │   Service   │    │   Service   │    │    API      │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │                   │
       │                   │                   │◀──────────────────│
       │                   │                   │   (Generated)     │
       │                   │◀──────────────────│                   │
       │◀──────────────────│   (Parsed)        │                   │
       │   (Questions)      │                   │                   │
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Database  │◀───│   Entity    │◀───│   Question  │◀───│   Response  │
│   (Save)    │    │   Creation  │    │   Parsing   │    │   (JSON)    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

### Report Generation Flow
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Exam      │───▶│   Report    │───▶│  OpenAI     │───▶│  OpenAI     │
│  Complete   │    │   Service   │    │   Service   │    │    API      │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │                   │
       │                   │                   │◀──────────────────│
       │                   │                   │   (Analysis)      │
       │                   │◀──────────────────│                   │
       │◀──────────────────│   (Report)        │                   │
       │   (Results)       │                   │                   │
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Export    │◀───│   Report    │◀───│   Entity    │◀───│   Response  │
│   Service   │    │   Entity    │    │   Creation  │    │   (JSON)    │
│ (PDF/JSON)  │    │   (Save)    │    │             │    │             │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

## Security Architecture

### Authentication & Authorization
```
┌─────────────────────────────────────────────────────────────┐
│                    Security Layer                           │
├─────────────────────────────────────────────────────────────┤
│  • Spring Security Configuration                            │
│  • JWT Token Management (Optional)                          │
│  • Role-Based Access Control (RBAC)                         │
│  • Session Management                                        │
│  • CORS Configuration                                        │
│  • Input Validation & Sanitization                          │
└─────────────────────────────────────────────────────────────┘
```

### Data Protection
- **Encryption**: Sensitive data encryption at rest
- **HTTPS**: TLS/SSL for all communications
- **Input Validation**: Protection against injection attacks
- **CSRF Protection**: Cross-site request forgery prevention
- **XSS Protection**: Cross-site scripting prevention

### API Security
- **Rate Limiting**: Prevent API abuse
- **API Key Management**: Secure OpenAI API key handling
- **Request Validation**: Input sanitization and validation
- **Error Handling**: Secure error responses

## Scalability Architecture

### Horizontal Scaling
```
┌─────────────────────────────────────────────────────────────┐
│                    Load Balancer                            │
│                 (Round Robin / Least Connections)           │
└─────────────────────┬───────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
┌───────▼──────┐ ┌────▼────┐ ┌──────▼──────┐
│  Instance 1  │ │Instance 2│ │ Instance 3  │
│ (Spring Boot)│ │(Spring  │ │ (Spring Boot)│
│              │ │ Boot)   │ │              │
└──────────────┘ └─────────┘ └──────────────┘
        │             │             │
        └─────────────┼─────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                    Database Cluster                         │
│              (Master-Slave Replication)                     │
└─────────────────────────────────────────────────────────────┘
```

### Caching Strategy
```
┌─────────────────────────────────────────────────────────────┐
│                    Caching Layer                            │
├─────────────────────────────────────────────────────────────┤
│  • Redis/Memcached (Session Store)                          │
│  • Application Level Caching (Questions, Topics)            │
│  • Database Query Caching                                   │
│  • CDN for Static Assets                                    │
└─────────────────────────────────────────────────────────────┘
```

### Database Scaling
- **Read Replicas**: Separate read and write databases
- **Connection Pooling**: Optimize database connections
- **Indexing Strategy**: Proper database indexing
- **Partitioning**: Data partitioning for large datasets

## Deployment Architecture

### Container-Based Deployment
```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Container                         │
│  ├─ Spring Boot Application (JAR)                           │
│  ├─ Embedded Tomcat Server                                  │
│  ├─ Application Properties                                  │
│  └─ Environment Variables                                    │
└─────────────────────────────────────────────────────────────┘
```

### Kubernetes Deployment
```
┌─────────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                       │
├─────────────────────────────────────────────────────────────┤
│  • Pods (Application Instances)                              │
│  • Services (Load Balancing)                                 │
│  • ConfigMaps (Configuration)                                │
│  • Secrets (API Keys, Passwords)                            │
│  • Persistent Volumes (Database Storage)                     │
│  • Ingress (External Access)                                 │
└─────────────────────────────────────────────────────────────┘
```

### Production Environment
```
┌─────────────────────────────────────────────────────────────┐
│                    Production Stack                          │
├─────────────────────────────────────────────────────────────┤
│  • Load Balancer (Nginx/HAProxy)                             │
│  • Application Servers (Multiple Instances)                 │
│  • Database Cluster (PostgreSQL)                             │
│  • Redis Cluster (Caching)                                   │
│  • File Storage (AWS S3/MinIO)                               │
│  • Monitoring (Prometheus/Grafana)                            │
│  • Logging (ELK Stack)                                       │
└─────────────────────────────────────────────────────────────┘
```

## Performance Considerations

### Database Optimization
- **Connection Pooling**: HikariCP configuration
- **Query Optimization**: Efficient JPA queries
- **Indexing Strategy**: Proper database indexes
- **Batch Processing**: Bulk operations for large datasets

### Application Performance
- **Caching**: Redis for frequently accessed data
- **Async Processing**: Non-blocking operations
- **Resource Optimization**: Memory and CPU usage
- **Lazy Loading**: JPA lazy loading strategies

### Network Optimization
- **CDN**: Static asset delivery
- **Compression**: GZIP compression
- **HTTP/2**: Protocol optimization
- **Keep-Alive**: Connection reuse

## Monitoring & Observability

### Application Monitoring
```
┌─────────────────────────────────────────────────────────────┐
│                    Monitoring Stack                         │
├─────────────────────────────────────────────────────────────┤
│  • Metrics Collection (Micrometer)                          │
│  • Health Checks (Spring Boot Actuator)                     │
│  • Performance Monitoring (APM)                              │
│  • Error Tracking (Sentry)                                  │
│  • Log Aggregation (ELK Stack)                               │
└─────────────────────────────────────────────────────────────┘
```

### Key Metrics
- **Response Time**: API and page load times
- **Throughput**: Requests per second
- **Error Rate**: Application error percentage
- **Resource Usage**: CPU, memory, disk usage
- **Database Performance**: Query times, connection pool stats

This architecture provides a solid foundation for the Aentic Exam Framework, supporting scalability, security, and maintainability while delivering a comprehensive AI-powered examination experience.
