# Aentic Exam Framework - System Architecture Diagrams

## Table of Contents
1. [High-Level System Architecture](#high-level-system-architecture)
2. [Component Architecture](#component-architecture)
3. [Data Flow Diagrams](#data-flow-diagrams)
4. [Database Schema](#database-schema)
5. [Security Architecture](#security-architecture)
6. [Deployment Architecture](#deployment-architecture)
7. [Scalability Architecture](#scalability-architecture)

## High-Level System Architecture

```mermaid
graph TB
    subgraph "External Services"
        OpenAI[OpenAI API<br/>GPT-3.5-turbo]
    end
    
    subgraph "Client Layer"
        Browser[Web Browser<br/>Student Interface]
    end
    
    subgraph "Application Layer"
        LB[Load Balancer<br/>Nginx/HAProxy]
        
        subgraph "Spring Boot Application"
            Web[Web Layer<br/>Controllers & Views]
            Service[Service Layer<br/>Business Logic]
            Data[Data Layer<br/>Repositories]
        end
    end
    
    subgraph "Data Layer"
        DB[(Database<br/>PostgreSQL/H2)]
        Cache[(Cache<br/>Redis)]
    end
    
    subgraph "Storage"
        FileStorage[File Storage<br/>PDF/JSON Reports]
    end
    
    Browser --> LB
    LB --> Web
    Web --> Service
    Service --> Data
    Service --> OpenAI
    Data --> DB
    Service --> Cache
    Service --> FileStorage
    
    style OpenAI fill:#ff9999
    style Browser fill:#99ccff
    style LB fill:#ffcc99
    style Web fill:#99ff99
    style Service fill:#ccff99
    style Data fill:#ff99cc
    style DB fill:#ccccff
    style Cache fill:#ffcccc
    style FileStorage fill:#ccffcc
```

## Component Architecture

```mermaid
graph TB
    subgraph "Presentation Layer"
        UI[Web UI<br/>Thymeleaf + Bootstrap]
        API[REST API<br/>JSON Endpoints]
    end
    
    subgraph "Application Layer"
        StudentCtrl[Student Controller]
        TopicCtrl[Topic Controller]
        ExamCtrl[Exam Controller]
        ReportCtrl[Report Controller]
    end
    
    subgraph "Service Layer"
        StudentSvc[Student Service]
        TopicSvc[Topic Service]
        QuestionSvc[Question Service]
        ExamSvc[Exam Service]
        ReportSvc[Report Service]
        ExportSvc[Export Service]
        OpenAISvc[OpenAI Service]
    end
    
    subgraph "Data Access Layer"
        StudentRepo[Student Repository]
        TopicRepo[Topic Repository]
        QuestionRepo[Question Repository]
        ExamRepo[Exam Repository]
        ReportRepo[Report Repository]
    end
    
    subgraph "Database Layer"
        StudentDB[(Student Table)]
        TopicDB[(Topic Table)]
        QuestionDB[(Question Table)]
        ExamDB[(Exam Table)]
        ReportDB[(Report Table)]
    end
    
    UI --> StudentCtrl
    UI --> TopicCtrl
    UI --> ExamCtrl
    UI --> ReportCtrl
    API --> StudentCtrl
    API --> TopicCtrl
    API --> ExamCtrl
    API --> ReportCtrl
    
    StudentCtrl --> StudentSvc
    TopicCtrl --> TopicSvc
    ExamCtrl --> ExamSvc
    ReportCtrl --> ReportSvc
    
    QuestionSvc --> OpenAISvc
    ReportSvc --> OpenAISvc
    ExamSvc --> QuestionSvc
    ReportSvc --> ExportSvc
    
    StudentSvc --> StudentRepo
    TopicSvc --> TopicRepo
    QuestionSvc --> QuestionRepo
    ExamSvc --> ExamRepo
    ReportSvc --> ReportRepo
    
    StudentRepo --> StudentDB
    TopicRepo --> TopicDB
    QuestionRepo --> QuestionDB
    ExamRepo --> ExamDB
    ReportRepo --> ReportDB
```

## Data Flow Diagrams

### Student Registration Flow

```mermaid
sequenceDiagram
    participant Browser as Web Browser
    participant Controller as Student Controller
    participant Service as Student Service
    participant DB as Database
    participant OpenAI as OpenAI API
    
    Browser->>Controller: POST /students/register
    Controller->>Controller: Validate Input
    Controller->>Service: registerStudent(dto)
    Service->>DB: Check email exists
    DB-->>Service: Email status
    Service->>DB: Save student profile
    DB-->>Service: Student entity
    Service-->>Controller: Student object
    Controller-->>Browser: Redirect to topics
```

### AI Question Generation Flow

```mermaid
sequenceDiagram
    participant Exam as Exam Service
    participant Question as Question Service
    participant OpenAI as OpenAI Service
    participant API as OpenAI API
    participant DB as Database
    
    Exam->>Question: generateQuestions(topic, count, difficulty)
    Question->>DB: Get existing questions
    DB-->>Question: Question list
    Question->>OpenAI: generateQuestion(topic, difficulty, student)
    OpenAI->>API: Send request with student profile
    API-->>OpenAI: Generated question
    OpenAI-->>Question: Parsed question
    Question->>DB: Save question
    DB-->>Question: Saved question
    Question-->>Exam: Questions list
```

### Report Generation Flow

```mermaid
sequenceDiagram
    participant Exam as Exam Service
    participant Report as Report Service
    participant OpenAI as OpenAI Service
    participant API as OpenAI API
    participant Export as Export Service
    participant DB as Database
    
    Exam->>Report: generateReport(exam)
    Report->>DB: Get exam answers
    DB-->>Report: Answer list
    Report->>OpenAI: generateReportAnalysis(examData)
    OpenAI->>API: Send exam data
    API-->>OpenAI: Analysis results
    OpenAI-->>Report: Parsed analysis
    Report->>DB: Save report
    DB-->>Report: Report entity
    Report-->>Exam: Report object
    Exam->>Export: generatePDF(reportCode)
    Export-->>Exam: PDF bytes
```

## Database Schema

```mermaid
erDiagram
    STUDENT {
        bigint id PK
        varchar first_name
        varchar last_name
        varchar email UK
        varchar phone
        varchar country
        varchar education_level
        varchar preferred_language
        text interests
        varchar profession
        integer age
        integer experience_years
        timestamp registration_date
    }
    
    TOPIC {
        bigint id PK
        varchar name UK
        text description
        varchar difficulty_level
        integer time_limit_minutes
        integer question_count
        timestamp created_at
    }
    
    QUESTION {
        bigint id PK
        text question_text
        varchar question_type
        text options
        varchar correct_answer
        text explanation
        varchar difficulty_level
        integer points
        bigint topic_id FK
        timestamp created_at
    }
    
    EXAM {
        bigint id PK
        varchar exam_code UK
        timestamp start_time
        timestamp end_time
        integer total_score
        integer max_score
        decimal percentage
        varchar grade
        bigint student_id FK
        bigint topic_id FK
        timestamp created_at
    }
    
    ANSWER {
        bigint id PK
        text answer_text
        boolean is_correct
        integer time_taken_seconds
        bigint question_id FK
        bigint exam_id FK
        timestamp created_at
    }
    
    REPORT {
        bigint id PK
        varchar report_code UK
        integer total_questions
        integer correct_answers
        integer wrong_answers
        integer total_time_minutes
        decimal average_time_per_question
        text strength_areas
        text improvement_areas
        text detailed_analysis
        timestamp generated_at
        bigint exam_id FK
    }
    
    STUDENT ||--o{ EXAM : "takes"
    TOPIC ||--o{ EXAM : "belongs to"
    TOPIC ||--o{ QUESTION : "contains"
    EXAM ||--o{ ANSWER : "has"
    QUESTION ||--o{ ANSWER : "answered in"
    EXAM ||--|| REPORT : "generates"
```

## Security Architecture

```mermaid
graph TB
    subgraph "Security Layers"
        Firewall[Firewall<br/>Network Security]
        LB[Load Balancer<br/>SSL Termination]
        Auth[Authentication<br/>Spring Security]
        Authz[Authorization<br/>RBAC]
        Input[Input Validation<br/>Sanitization]
        Encrypt[Encryption<br/>Data at Rest]
    end
    
    subgraph "Application Components"
        Web[Web Layer]
        API[REST API]
        Service[Service Layer]
        DB[Database]
    end
    
    subgraph "External Security"
        OpenAI[OpenAI API<br/>API Key Management]
        HTTPS[HTTPS/TLS<br/>Communication Security]
    end
    
    Firewall --> LB
    LB --> Auth
    Auth --> Authz
    Authz --> Input
    Input --> Encrypt
    Encrypt --> Web
    Encrypt --> API
    Web --> Service
    API --> Service
    Service --> DB
    
    Service --> OpenAI
    HTTPS -.-> OpenAI
    
    style Firewall fill:#ff6666
    style LB fill:#ffaa66
    style Auth fill:#66ff66
    style Authz fill:#66ffaa
    style Input fill:#66aaff
    style Encrypt fill:#aa66ff
```

## Deployment Architecture

### Development Environment

```mermaid
graph TB
    subgraph "Development"
        Dev[Developer Machine<br/>Local JAR]
        H2[(H2 Database<br/>In-memory)]
    end
    
    Dev --> H2
    
    style Dev fill:#99ff99
    style H2 fill:#ff9999
```

### Production Environment

```mermaid
graph TB
    subgraph "Production Infrastructure"
        Internet[Internet]
        
        subgraph "Load Balancing"
            LB[Load Balancer<br/>Nginx/HAProxy]
            SSL[SSL Certificate]
        end
        
        subgraph "Application Servers"
            App1[App Server 1<br/>Spring Boot]
            App2[App Server 2<br/>Spring Boot]
            App3[App Server 3<br/>Spring Boot]
        end
        
        subgraph "Database Cluster"
            Master[(PostgreSQL<br/>Master)]
            Slave1[(PostgreSQL<br/>Slave 1)]
            Slave2[(PostgreSQL<br/>Slave 2)]
        end
        
        subgraph "Caching Layer"
            Redis[Redis Cluster<br/>Session & Cache]
        end
        
        subgraph "Storage"
            S3[Object Storage<br/>AWS S3/MinIO]
        end
        
        subgraph "Monitoring"
            Monitor[Monitoring Stack<br/>Prometheus/Grafana]
            Logs[Log Aggregation<br/>ELK Stack]
        end
    end
    
    Internet --> LB
    LB --> SSL
    SSL --> App1
    SSL --> App2
    SSL --> App3
    
    App1 --> Master
    App2 --> Master
    App3 --> Master
    Master --> Slave1
    Master --> Slave2
    
    App1 --> Redis
    App2 --> Redis
    App3 --> Redis
    
    App1 --> S3
    App2 --> S3
    App3 --> S3
    
    App1 --> Monitor
    App2 --> Monitor
    App3 --> Monitor
    App1 --> Logs
    App2 --> Logs
    App3 --> Logs
```

### Container-Based Deployment

```mermaid
graph TB
    subgraph "Docker Containers"
        App[Spring Boot App<br/>Docker Container]
        DB[PostgreSQL<br/>Docker Container]
        Redis[Redis<br/>Docker Container]
        Nginx[Nginx<br/>Docker Container]
    end
    
    subgraph "Kubernetes Cluster"
        K8s[Kubernetes<br/>Orchestration]
        
        subgraph "K8s Resources"
            Pod[Pods<br/>App Instances]
            Service[Services<br/>Load Balancing]
            Config[ConfigMaps<br/>Configuration]
            Secret[Secrets<br/>API Keys]
            PVC[Persistent Volumes<br/>Storage]
        end
    end
    
    subgraph "External Services"
        OpenAI[OpenAI API]
        Cloud[Cloud Provider<br/>AWS/Azure/GCP]
    end
    
    K8s --> Pod
    K8s --> Service
    K8s --> Config
    K8s --> Secret
    K8s --> PVC
    
    Pod --> App
    Pod --> DB
    Pod --> Redis
    Service --> Nginx
    
    App --> OpenAI
    PVC --> Cloud
```

## Scalability Architecture

### Horizontal Scaling

```mermaid
graph TB
    subgraph "Scaling Strategy"
        Users[Users<br/>Client Requests]
        
        subgraph "Load Distribution"
            LB[Load Balancer<br/>Round Robin]
            Health[Health Checks]
        end
        
        subgraph "Application Tier"
            App1[App Instance 1<br/>Spring Boot]
            App2[App Instance 2<br/>Spring Boot]
            App3[App Instance 3<br/>Spring Boot]
            Auto[Auto Scaling<br/>Based on Load]
        end
        
        subgraph "Data Tier"
            Master[(PostgreSQL<br/>Write Master)]
            Read1[(PostgreSQL<br/>Read Replica 1)]
            Read2[(PostgreSQL<br/>Read Replica 2)]
        end
        
        subgraph "Cache Tier"
            Redis[Redis Cluster<br/>Distributed Cache]
        end
    end
    
    Users --> LB
    LB --> Health
    LB --> App1
    LB --> App2
    LB --> App3
    Auto --> App1
    Auto --> App2
    Auto --> App3
    
    App1 --> Master
    App2 --> Read1
    App3 --> Read2
    
    App1 --> Redis
    App2 --> Redis
    App3 --> Redis
```

### Caching Strategy

```mermaid
graph TB
    subgraph "Multi-Level Caching"
        Browser[Browser Cache<br/>Static Assets]
        
        subgraph "Application Cache"
            L1[L1 Cache<br/>Application Memory]
            L2[L2 Cache<br/>Redis Cluster]
        end
        
        subgraph "Database Cache"
            Query[Query Cache<br/>PostgreSQL]
            Buffer[Buffer Pool<br/>Database Memory]
        end
        
        subgraph "CDN"
            CDN[Content Delivery Network<br/>Static Files]
        end
    end
    
    Browser --> CDN
    Browser --> L1
    L1 --> L2
    L2 --> Query
    Query --> Buffer
```

## Performance Monitoring

```mermaid
graph TB
    subgraph "Monitoring Stack"
        App[Spring Boot App<br/>Micrometer Metrics]
        
        subgraph "Collection"
            Prometheus[Prometheus<br/>Metrics Collection]
            Grafana[Grafana<br/>Visualization]
        end
        
        subgraph "Logging"
            Log4j[Log4j2<br/>Application Logs]
            ELK[ELK Stack<br/>Log Aggregation]
        end
        
        subgraph "Tracing"
            Jaeger[Jaeger<br/>Distributed Tracing]
            APM[APM Tool<br/>Performance Monitoring]
        end
        
        subgraph "Alerting"
            Alert[AlertManager<br/>Alert Management]
            Slack[Slack/Email<br/>Notifications]
        end
    end
    
    App --> Prometheus
    App --> Log4j
    App --> Jaeger
    
    Prometheus --> Grafana
    Log4j --> ELK
    Jaeger --> APM
    
    Prometheus --> Alert
    Alert --> Slack
```

These diagrams provide a comprehensive visual representation of the Aentic Exam Framework's architecture, covering all major components, data flows, and deployment scenarios. Each diagram is designed to help understand different aspects of the system at various levels of detail.
