# Aentic Exam Framework - Security and Monitoring Guide

## Table of Contents
1. [Security Overview](#security-overview)
2. [Authentication & Authorization](#authentication--authorization)
3. [Data Protection](#data-protection)
4. [API Security](#api-security)
5. [Infrastructure Security](#infrastructure-security)
6. [Monitoring Overview](#monitoring-overview)
7. [Application Monitoring](#application-monitoring)
8. [Infrastructure Monitoring](#infrastructure-monitoring)
9. [Security Monitoring](#security-monitoring)
10. [Alerting](#alerting)
11. [Compliance](#compliance)

## Security Overview

### Security Principles

The Aentic Exam Framework follows these security principles:

1. **Defense in Depth**: Multiple layers of security controls
2. **Least Privilege**: Users and services have minimal required permissions
3. **Zero Trust**: Verify everything, trust nothing
4. **Encryption**: Data encrypted at rest and in transit
5. **Audit Trail**: Comprehensive logging and monitoring
6. **Secure by Default**: Security features enabled by default

### Threat Model

| Threat Category | Description | Mitigation |
|-----------------|-------------|-------------|
| Unauthorized Access | Access without proper authentication | JWT/OAuth2, MFA |
| Data Breach | Unauthorized data access | Encryption, access controls |
| Injection Attacks | SQL injection, XSS, CSRF | Input validation, parameterized queries |
| DoS Attacks | Denial of service attacks | Rate limiting, load balancing |
| Data Tampering | Unauthorized data modification | Integrity checks, audit logs |

## Authentication & Authorization

### JWT Implementation

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/students/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/topics/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}
```

### JWT Service

```java
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public String getUsernameFromToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret)
            .parseClaimsJws(token).getBody().getSubject();
    }
}
```

### Role-Based Access Control

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    private RoleType roleType;
}

public enum RoleType {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_INSTRUCTOR
}
```

## Data Protection

### Encryption Configuration

```java
@Configuration
public class EncryptionConfig {
    
    @Bean
    public AESUtil aesUtil() {
        return new AESUtil();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}

@Component
public class AESUtil {
    
    private static final String SECRET_KEY = "your-secret-key-here";
    private static final String ALGORITHM = "AES";
    
    public String encrypt(String data) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    public String decrypt(String encryptedData) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedData = cipher.doFinal(decodedData);
            return new String(decryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
```

### Sensitive Data Handling

```java
@Entity
@Table(name = "students")
public class Student {
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    @Convert(converter = PhoneEncryptConverter.class)
    private String phone;
    
    @Column(columnDefinition = "TEXT")
    @Convert(converter = InterestEncryptConverter.class)
    private String interests;
}

@Converter
public class PhoneEncryptConverter implements AttributeConverter<String, String> {
    
    @Autowired
    private AESUtil aesUtil;
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute != null ? aesUtil.encrypt(attribute) : null;
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData != null ? aesUtil.decrypt(dbData) : null;
    }
}
```

## API Security

### Rate Limiting

```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}

@Component
public class RateLimitFilter implements Filter {
    
    private final Map<String, Map<String, Integer>> rateLimitMap = new ConcurrentHashMap<>();
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIP = getClientIP(httpRequest);
        String endpoint = httpRequest.getRequestURI();
        
        if (!isAllowed(clientIP, endpoint)) {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isAllowed(String clientIP, String endpoint) {
        Map<String, Integer> endpointCounts = rateLimitMap.computeIfAbsent(clientIP, k -> new HashMap<>());
        int currentCount = endpointCounts.getOrDefault(endpoint, 0);
        
        if (currentCount >= getRateLimit(endpoint)) {
            return false;
        }
        
        endpointCounts.put(endpoint, currentCount + 1);
        return true;
    }
    
    private int getRateLimit(String endpoint) {
        if (endpoint.startsWith("/api/auth/")) return 10;
        if (endpoint.startsWith("/api/questions/generate")) return 5;
        return 100;
    }
}
```

### Input Validation

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "Input validation failed",
            errors
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParseException(HttpMessageNotReadableException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_JSON",
            "Invalid JSON format",
            Collections.singletonList("Request body contains invalid JSON")
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
}

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String code;
    private String message;
    private List<String> details;
    private LocalDateTime timestamp = LocalDateTime.now();
}
```

## Infrastructure Security

### Docker Security

```dockerfile
# Use non-root user
FROM openjdk:17-jre-slim

# Create non-root user
RUN groupadd -r examapp && useradd -r -g examapp examapp

# Set proper permissions
WORKDIR /app
COPY --chown=examapp:examapp target/exam-framework-1.0.0.jar app.jar

# Switch to non-root user
USER examapp

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Kubernetes Security

```yaml
apiVersion: v1
kind: PodSecurityPolicy
metadata:
  name: exam-framework-psp
spec:
  privileged: false
  allowPrivilegeEscalation: false
  requiredDropCapabilities:
    - ALL
  volumes:
    - 'configMap'
    - 'emptyDir'
    - 'projected'
    - 'secret'
    - 'downwardAPI'
    - 'persistentVolumeClaim'
  runAsUser:
    rule: 'MustRunAsNonRoot'
  seLinux:
    rule: 'RunAsAny'
  fsGroup:
    rule: 'RunAsAny'
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: exam-framework-psp-user
rules:
- apiGroups: ['policy']
  resources: ['podsecuritypolicies']
  verbs: ['use']
  resourceNames:
  - exam-framework-psp
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: exam-framework-psp-binding
roleRef:
  kind: Role
  name: exam-framework-psp-user
  apiGroup: rbac.authorization.k8s.io
subjects:
- kind: ServiceAccount
  name: exam-framework
  namespace: exam-framework
```

### Network Policies

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: exam-framework-netpol
  namespace: exam-framework
spec:
  podSelector:
    matchLabels:
      app: exam-framework
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: kube-system
    ports:
    - protocol: TCP
      port: 53
    - protocol: UDP
      port: 53
  - to: []
    ports:
    - protocol: TCP
      port: 443
      port: 80
```

## Monitoring Overview

### Monitoring Stack Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Monitoring Stack                         │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ Prometheus  │  │ Grafana     │  │ AlertManager│         │
│  │ Collection  │  │ Visualization│  │ Alerting    │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ Jaeger      │  │ ELK Stack   │  │ Sentry      │         │
│  │ Tracing     │  │ Logging     │  │ Error Track │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
```

## Application Monitoring

### Micrometer Configuration

```java
@Configuration
public class MetricsConfig {
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
    
    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }
    
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
            "application", "exam-framework",
            "version", "1.0.0"
        );
    }
}

@RestController
public class StudentController {
    
    private final MeterRegistry meterRegistry;
    private final Counter studentRegistrationCounter;
    
    public StudentController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.studentRegistrationCounter = Counter.builder("student.registrations")
            .description("Number of student registrations")
            .register(meterRegistry);
    }
    
    @PostMapping("/students/register")
    @Timed(name = "student.registration.time", description = "Time taken to register student")
    @Counted(value = "student.registration.count", description = "Number of student registrations")
    public ResponseEntity<Student> registerStudent(@Valid @RequestBody StudentRegistrationDto dto) {
        studentRegistrationCounter.increment();
        // Registration logic
        return ResponseEntity.ok(student);
    }
}
```

### Custom Metrics

```java
@Component
public class CustomMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Gauge activeExamsGauge;
    private final Counter aiQuestionsGeneratedCounter;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.activeExamsGauge = Gauge.builder("exams.active")
            .description("Number of active exams")
            .register(meterRegistry, this, CustomMetrics::getActiveExamsCount);
        
        this.aiQuestionsGeneratedCounter = Counter.builder("ai.questions.generated")
            .description("Number of AI-generated questions")
            .register(meterRegistry);
    }
    
    private double getActiveExamsCount() {
        // Logic to count active exams
        return examService.getActiveExamsCount();
    }
    
    public void incrementAIQuestionsGenerated() {
        aiQuestionsGeneratedCounter.increment();
    }
}
```

### Health Checks

```java
@Component
public class CustomHealthIndicators {
    
    @Bean
    public HealthIndicator openAIHealthIndicator() {
        return () -> {
            try {
                // Test OpenAI API connectivity
                openAIService.testConnection();
                return Health.up()
                    .withDetail("status", "OpenAI API is accessible")
                    .build();
            } catch (Exception e) {
                return Health.down()
                    .withDetail("status", "OpenAI API is not accessible")
                    .withDetail("error", e.getMessage())
                    .build();
            }
        };
    }
    
    @Bean
    public HealthIndicator databaseHealthIndicator() {
        return () -> {
            try {
                // Test database connectivity
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                return Health.up()
                    .withDetail("status", "Database is accessible")
                    .build();
            } catch (Exception e) {
                return Health.down()
                    .withDetail("status", "Database is not accessible")
                    .withDetail("error", e.getMessage())
                    .build();
            }
        };
    }
}
```

## Infrastructure Monitoring

### Prometheus Configuration

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "/etc/prometheus/rules/*.yml"

scrape_configs:
  - job_name: 'exam-framework'
    static_configs:
      - targets: ['exam-framework-service:80']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
    
  - job_name: 'postgres'
    static_configs:
      - targets: ['postgres-exporter:9187']
    
  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']
    
  - job_name: 'nginx'
    static_configs:
      - targets: ['nginx-exporter:9113']

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093
```

### Grafana Dashboard JSON

```json
{
  "dashboard": {
    "title": "Exam Framework Monitoring",
    "panels": [
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_requests_total[5m])",
            "legendFormat": "{{method}} {{uri}}"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 0}
      },
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))",
            "legendFormat": "95th percentile"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 0}
      },
      {
        "title": "Active Exams",
        "type": "stat",
        "targets": [
          {
            "expr": "exams.active"
          }
        ],
        "gridPos": {"h": 4, "w": 6, "x": 0, "y": 8}
      },
      {
        "title": "AI Questions Generated",
        "type": "stat",
        "targets": [
          {
            "expr": "rate(ai_questions_generated_total[5m])"
          }
        ],
        "gridPos": {"h": 4, "w": 6, "x": 6, "y": 8}
      }
    ]
  }
}
```

## Security Monitoring

### Security Event Logging

```java
@Component
public class SecurityAuditLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityAuditLogger.class);
    
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        String ip = getClientIP();
        
        logger.info("Authentication success: user={}, ip={}", username, ip);
        
        // Store in audit database
        auditService.logSecurityEvent("AUTH_SUCCESS", username, ip, null);
    }
    
    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String ip = getClientIP();
        String reason = event.getException().getMessage();
        
        logger.warn("Authentication failure: user={}, ip={}, reason={}", username, ip, reason);
        
        // Store in audit database
        auditService.logSecurityEvent("AUTH_FAILURE", username, ip, reason);
    }
    
    @EventListener
    public void handleAuthorizationFailure(AccessDeniedEvent event) {
        String username = event.getAuthentication().getName();
        String ip = getClientIP();
        String resource = ((Authentication) event.getSource()).getName();
        
        logger.warn("Authorization failure: user={}, ip={}, resource={}", username, ip, resource);
        
        // Store in audit database
        auditService.logSecurityEvent("ACCESS_DENIED", username, ip, resource);
    }
}
```

### Intrusion Detection

```java
@Component
public class IntrusionDetectionService {
    
    private final Map<String, List<LocalDateTime>> failedAttempts = new ConcurrentHashMap<>();
    private final int MAX_FAILED_ATTEMPTS = 5;
    private final int LOCKOUT_DURATION_MINUTES = 15;
    
    @EventListener
    public void handleFailedAuthentication(AuthenticationFailureEvent event) {
        String ip = getClientIP();
        LocalDateTime now = LocalDateTime.now();
        
        failedAttempts.computeIfAbsent(ip, k -> new ArrayList<>()).add(now);
        
        // Clean old attempts
        failedAttempts.get(ip).removeIf(time -> time.isBefore(now.minusMinutes(LOCKOUT_DURATION_MINUTES)));
        
        if (failedAttempts.get(ip).size() >= MAX_FAILED_ATTEMPTS) {
            logger.warn("Potential brute force attack detected from IP: {}", ip);
            // Block IP or notify security team
            securityService.blockIP(ip, Duration.ofMinutes(LOCKOUT_DURATION_MINUTES));
        }
    }
    
    public boolean isIPBlocked(String ip) {
        List<LocalDateTime> attempts = failedAttempts.get(ip);
        if (attempts == null) return false;
        
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(LOCKOUT_DURATION_MINUTES);
        return attempts.stream().anyMatch(time -> time.isAfter(cutoff)) && 
               attempts.size() >= MAX_FAILED_ATTEMPTS;
    }
}
```

## Alerting

### Prometheus Alert Rules

```yaml
groups:
- name: exam-framework-alerts
  rules:
  - alert: HighErrorRate
    expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "High error rate detected"
      description: "Error rate is {{ $value }} errors per second"
      
  - alert: HighResponseTime
    expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) > 2
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High response time detected"
      description: "95th percentile response time is {{ $value }} seconds"
      
  - alert: DatabaseConnectionsHigh
    expr: hikaricp_connections_active / hikaricp_connections_max > 0.8
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "Database connection pool usage high"
      description: "{{ $value | humanizePercentage }} of database connections in use"
      
  - alert: OpenAIAPIFailure
    expr: up{job="openai-api"} == 0
    for: 2m
    labels:
      severity: critical
    annotations:
      summary: "OpenAI API is down"
      description: "OpenAI API endpoint is not responding"
      
  - alert: SecurityEvent
    expr: increase(security_events_total{type="AUTH_FAILURE"}[5m]) > 10
    for: 1m
    labels:
      severity: warning
    annotations:
      summary: "Multiple authentication failures detected"
      description: "{{ $value }} authentication failures in the last 5 minutes"
```

### AlertManager Configuration

```yaml
global:
  smtp_smarthost: 'localhost:587'
  smtp_from: 'alerts@exam-framework.com'

route:
  group_by: ['alertname']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'web.hook'
  routes:
  - match:
      severity: critical
    receiver: 'critical-alerts'
  - match:
      severity: warning
    receiver: 'warning-alerts'

receivers:
- name: 'web.hook'
  webhook_configs:
  - url: 'http://localhost:8080/webhook/alerts'

- name: 'critical-alerts'
  email_configs:
  - to: 'admin@exam-framework.com'
    subject: '[CRITICAL] Exam Framework Alert'
    body: |
      {{ range .Alerts }}
      Alert: {{ .Annotations.summary }}
      Description: {{ .Annotations.description }}
      {{ end }}
  slack_configs:
  - api_url: 'https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK'
    channel: '#alerts'
    title: 'Critical Alert: {{ .GroupLabels.alertname }}'

- name: 'warning-alerts'
  email_configs:
  - to: 'team@exam-framework.com'
    subject: '[WARNING] Exam Framework Alert'
    body: |
      {{ range .Alerts }}
      Alert: {{ .Annotations.summary }}
      Description: {{ .Annotations.description }}
      {{ end }}
```

## Compliance

### GDPR Compliance

```java
@Component
public class GDPRComplianceService {
    
    @EventListener
    public void handleStudentRegistration(StudentRegisteredEvent event) {
        // Log consent for data processing
        consentService.logDataProcessingConsent(
            event.getStudent().getId(),
            "student_registration",
            LocalDateTime.now()
        );
    }
    
    public void deleteStudentData(Long studentId) {
        // Right to be forgotten
        studentService.deleteStudent(studentId);
        auditService.logDataDeletion(studentId, LocalDateTime.now());
    }
    
    public byte[] exportStudentData(Long studentId) {
        // Right to data portability
        Student student = studentService.getStudentById(studentId)
            .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        
        return dataExportService.exportStudentData(student);
    }
}
```

### Audit Trail

```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String action;
    
    @Column(nullable = false)
    private String entityType;
    
    @Column(nullable = false)
    private Long entityId;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Column(nullable = false)
    private String userAgent;
}

@Service
public class AuditService {
    
    @EventListener
    public void handleEntityEvent(EntityEvent event) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(event.getAction());
        auditLog.setEntityType(event.getEntityType());
        auditLog.setEntityId(event.getEntityId());
        auditLog.setUsername(event.getUsername());
        auditLog.setIpAddress(getClientIP());
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setDetails(event.getDetails());
        auditLog.setUserAgent(getUserAgent());
        
        auditLogRepository.save(auditLog);
    }
}
```

This comprehensive security and monitoring guide provides all the necessary components to secure and monitor the Aentic Exam Framework in production environments.
