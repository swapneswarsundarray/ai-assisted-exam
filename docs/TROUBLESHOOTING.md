# Aentic Exam Framework - Troubleshooting and Maintenance Guide

## Table of Contents
1. [Common Issues](#common-issues)
2. [Database Issues](#database-issues)
3. [OpenAI API Issues](#openai-api-issues)
4. [Performance Issues](#performance-issues)
5. [Security Issues](#security-issues)
6. [Deployment Issues](#deployment-issues)
7. [Maintenance Procedures](#maintenance-procedures)
8. [Health Checks](#health-checks)
9. [Log Analysis](#log-analysis)
10. [Emergency Procedures](#emergency-procedures)

## Common Issues

### Application Won't Start

**Symptoms:**
- Application fails to start with error messages
- Port already in use errors
- Database connection failures

**Solutions:**

1. **Check Port Availability**
```bash
# Check if port 8080 is in use
netstat -tlnp | grep :8080
lsof -i :8080

# Kill process using the port
kill -9 <PID>

# Or change port in application.properties
server.port=8081
```

2. **Check Database Connection**
```bash
# Test database connectivity
psql -h localhost -U exam_app -d exam_framework_prod

# Check database logs
tail -f /var/log/postgresql/postgresql.log
```

3. **Verify Java Version**
```bash
java -version
# Should be Java 17 or higher
```

4. **Check Configuration**
```bash
# Verify application.properties
cat src/main/resources/application.properties

# Check environment variables
env | grep SPRING
env | grep DATABASE
```

### Registration Form Not Working

**Symptoms:**
- Form submission fails
- Validation errors
- 404 errors

**Solutions:**

1. **Check Form Validation**
```javascript
// Check browser console for JavaScript errors
// Verify form fields have correct names and types
```

2. **Verify Controller Mapping**
```java
// Ensure @PostMapping annotation is correct
@PostMapping("/students/register")
public ResponseEntity<Student> registerStudent(@Valid @RequestBody StudentRegistrationDto dto)
```

3. **Check Thymeleaf Template**
```html
<!-- Verify form action and field names -->
<form th:action="@{/students/register}" th:object="${student}" method="post">
    <input type="text" th:field="*{firstName}" required>
```

### Questions Not Generating

**Symptoms:**
- AI questions not being generated
- Fallback to dummy questions
- OpenAI API errors

**Solutions:**

1. **Check OpenAI API Key**
```bash
# Verify environment variable
echo $OPENAI_API_KEY

# Test API key manually
curl -H "Authorization: Bearer $OPENAI_API_KEY" \
     -H "Content-Type: application/json" \
     -d '{"model":"gpt-3.5-turbo","messages":[{"role":"user","content":"Hello"}]}' \
     https://api.openai.com/v1/chat/completions
```

2. **Check Network Connectivity**
```bash
# Test internet connectivity
ping api.openai.com

# Check firewall rules
sudo ufw status
```

3. **Review OpenAI Service Logs**
```bash
# Check application logs for OpenAI errors
tail -f logs/application.log | grep -i openai
```

## Database Issues

### Connection Pool Exhaustion

**Symptoms:**
- "Connection pool exhausted" errors
- Slow database responses
- Application hangs

**Solutions:**

1. **Increase Pool Size**
```properties
# application.properties
spring.datasource.hikari.maximum-pool-size=30
spring.datasource.hikari.minimum-idle=10
```

2. **Monitor Connection Usage**
```sql
-- Check active connections
SELECT count(*) FROM pg_stat_activity WHERE state = 'active';

-- Check long-running queries
SELECT query, pid, age, state FROM pg_stat_activity WHERE state != 'idle';
```

3. **Optimize Database Queries**
```java
// Add database indexes
CREATE INDEX CONCURRENTLY idx_student_email ON students(email);
CREATE INDEX CONCURRENTLY idx_exam_student_id ON exams(student_id);
```

### Database Migration Issues

**Symptoms:**
- Flyway migration failures
- Schema mismatch errors
- Data corruption

**Solutions:**

1. **Check Migration Status**
```bash
# Check Flyway schema history table
psql -U exam_app -d exam_framework_prod -c "SELECT * FROM flyway_schema_history ORDER BY installed_on;"
```

2. **Repair Failed Migration**
```sql
-- Mark problematic migration as resolved
UPDATE flyway_schema_history 
SET success = true, checksum = <new_checksum> 
WHERE version = <version>;
```

3. **Manual Schema Update**
```sql
-- Manually apply missing changes
ALTER TABLE students ADD COLUMN IF NOT EXISTS country VARCHAR(100);
ALTER TABLE students ADD COLUMN IF NOT EXISTS education_level VARCHAR(50);
```

### Database Performance Issues

**Symptoms:**
- Slow query responses
- High CPU usage
- Database locks

**Solutions:**

1. **Analyze Slow Queries**
```sql
-- Enable slow query logging
ALTER SYSTEM SET log_min_duration_statement = 1000;
SELECT pg_reload_conf();

-- Check slow queries
SELECT query, mean_time, calls, total_time 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;
```

2. **Update Statistics**
```sql
-- Update table statistics
ANALYZE students;
ANALYZE exams;
ANALYZE questions;
```

3. **Reindex Tables**
```sql
-- Reindex fragmented tables
REINDEX TABLE students;
REINDEX TABLE exams;
```

## OpenAI API Issues

### Rate Limiting

**Symptoms:**
- "Rate limit exceeded" errors
- 429 HTTP status codes
- Intermittent failures

**Solutions:**

1. **Implement Exponential Backoff**
```java
@Retryable(value = {OpenAiApiException.class}, 
           maxAttempts = 3, 
           backoff = @Backoff(delay = 1000, multiplier = 2))
public String generateQuestion(String topic, String difficulty, Student student) {
    // Implementation with retry logic
}
```

2. **Add Rate Limiting**
```java
@Component
public class OpenAIRateLimiter {
    private final RateLimiter rateLimiter = RateLimiter.create(10.0); // 10 requests per second
    
    public boolean tryRequest() {
        return rateLimiter.tryAcquire();
    }
}
```

3. **Monitor API Usage**
```bash
# Check OpenAI API usage
curl -H "Authorization: Bearer $OPENAI_API_KEY" \
     https://api.openai.com/v1/usage
```

### API Key Issues

**Symptoms:**
- "Invalid API key" errors
- Authentication failures
- Access denied errors

**Solutions:**

1. **Verify API Key Validity**
```bash
# Test API key
curl -H "Authorization: Bearer $OPENAI_API_KEY" \
     https://api.openai.com/v1/models
```

2. **Check Key Permissions**
```bash
# Ensure key has necessary permissions
# Check OpenAI dashboard for key restrictions
```

3. **Rotate API Key**
```bash
# Generate new API key from OpenAI dashboard
# Update environment variable
export OPENAI_API_KEY=new-api-key-here
```

### Model Availability

**Symptoms:**
- "Model not found" errors
- Service unavailable
- Model deprecation warnings

**Solutions:**

1. **Check Model Availability**
```bash
# List available models
curl -H "Authorization: Bearer $OPENAI_API_KEY" \
     https://api.openai.com/v1/models
```

2. **Update Model Configuration**
```java
// Update to use available model
ChatCompletionRequest request = ChatCompletionRequest.builder()
    .model("gpt-3.5-turbo")  // Use current model
    .messages(messages)
    .build();
```

## Performance Issues

### High Memory Usage

**Symptoms:**
- OutOfMemoryError
- High memory consumption
- Garbage collection issues

**Solutions:**

1. **Monitor Memory Usage**
```bash
# Check JVM memory usage
jstat -gc <pid>

# Generate heap dump
jmap -dump:format=b,file=heap.hprof <pid>
```

2. **Optimize JVM Settings**
```bash
# Production JVM settings
JAVA_OPTS="-Xms2g -Xmx4g \
           -XX:+UseG1GC \
           -XX:MaxGCPauseMillis=200 \
           -XX:+UseStringDeduplication"
```

3. **Profile Memory Usage**
```java
// Use VisualVM or YourKit to profile memory
// Look for memory leaks in collections
```

### Slow Response Times

**Symptoms:**
- High response times
- Timeout errors
- Poor user experience

**Solutions:**

1. **Enable Performance Monitoring**
```java
@Timed(name = "student.registration.time", description = "Time taken to register student")
public ResponseEntity<Student> registerStudent(@Valid @RequestBody StudentRegistrationDto dto) {
    // Implementation
}
```

2. **Optimize Database Queries**
```java
// Use JPA fetch optimization
@EntityGraph(attributePaths = {"topic", "student"})
Optional<Exam> findByIdWithDetails(Long id);
```

3. **Add Caching**
```java
@Cacheable(value = "students", key = "#id")
public Optional<Student> getStudentById(Long id) {
    return studentRepository.findById(id);
}
```

### High CPU Usage

**Symptoms:**
- High CPU consumption
- System overload
- Poor performance

**Solutions:**

1. **Profile CPU Usage**
```bash
# Check CPU usage
top -p <pid>

# Generate thread dump
jstack <pid> > thread_dump.txt
```

2. **Optimize Algorithms**
```java
// Use efficient algorithms
// Avoid nested loops
// Use streaming for large datasets
```

3. **Add Async Processing**
```java
@Async
public CompletableFuture<Void> processExamResults(Exam exam) {
    // Async processing
    return CompletableFuture.completedFuture(null);
}
```

## Security Issues

### Authentication Failures

**Symptoms:**
- Login failures
- Session issues
- Token problems

**Solutions:**

1. **Check Authentication Configuration**
```java
// Verify JWT configuration
@Bean
public JwtAuthenticationFilter jwtAuthenticationFilter() {
    return new JwtAuthenticationFilter();
}
```

2. **Validate Tokens**
```bash
# Decode JWT token
echo "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" | base64 -d
```

3. **Check Session Management**
```properties
# Session configuration
server.servlet.session.timeout=1800
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
```

### Authorization Issues

**Symptoms:**
- Access denied errors
- Permission problems
- Role issues

**Solutions:**

1. **Verify Role Configuration**
```java
// Check user roles
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Student> deleteStudent(Long id) {
    // Implementation
}
```

2. **Debug Security Context**
```java
// Log security context
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
log.info("User: {}, Authorities: {}", auth.getName(), auth.getAuthorities());
```

## Deployment Issues

### Container Issues

**Symptoms:**
- Container won't start
- Health check failures
- Resource constraints

**Solutions:**

1. **Check Container Logs**
```bash
# Check container logs
docker logs exam-framework-container

# Check container status
docker ps -a | grep exam-framework
```

2. **Verify Resource Limits**
```yaml
# Check docker-compose configuration
services:
  app:
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '1.0'
```

3. **Debug Health Checks**
```bash
# Test health endpoint manually
curl -f http://localhost:8080/actuator/health
```

### Kubernetes Issues

**Symptoms:**
- Pod won't start
- Service not accessible
- Resource constraints

**Solutions:**

1. **Check Pod Status**
```bash
# Get pod status
kubectl get pods -n exam-framework

# Describe pod issues
kubectl describe pod <pod-name> -n exam-framework
```

2. **Check Service Configuration**
```bash
# Check service endpoints
kubectl get endpoints -n exam-framework

# Test service connectivity
kubectl port-forward service/exam-framework-service 8080:80
```

3. **Verify Resource Requests**
```yaml
# Check resource configuration
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1000m"
```

## Maintenance Procedures

### Database Maintenance

1. **Regular Backups**
```bash
#!/bin/bash
# Daily backup script
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump -h localhost -U exam_app exam_framework_prod | gzip > backup_$DATE.sql.gz

# Upload to cloud storage
aws s3 cp backup_$DATE.sql.gz s3://exam-framework-backups/

# Clean old backups
find /backups -name "*.sql.gz" -mtime +30 -delete
```

2. **Database Optimization**
```sql
-- Weekly maintenance
VACUUM ANALYZE students;
VACUUM ANALYZE exams;
VACUUM ANALYZE questions;

-- Rebuild indexes
REINDEX DATABASE exam_framework_prod;
```

3. **Statistics Update**
```sql
-- Update table statistics
ANALYZE students;
ANALYZE exams;
ANALYZE questions;
ANALYZE answers;
ANALYZE reports;
```

### Application Maintenance

1. **Log Rotation**
```bash
# Configure log rotation
/var/log/exam-framework/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 examapp examapp
    postrotate
        systemctl reload exam-framework
    endscript
}
```

2. **Cache Cleanup**
```bash
# Clear Redis cache
redis-cli FLUSHDB

# Or clear specific keys
redis-cli DEL "students:*"
```

3. **Health Monitoring**
```bash
# Health check script
#!/bin/bash
HEALTH_URL="http://localhost:8080/actuator/health"
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_URL)

if [ $RESPONSE -ne 200 ]; then
    echo "Health check failed with status: $RESPONSE"
    # Send alert
    curl -X POST -H "Content-Type: application/json" \
         -d '{"text":"Exam Framework health check failed"}' \
         https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK
fi
```

## Health Checks

### Application Health

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check detailed health
curl http://localhost:8080/actuator/health/details
```

### Database Health

```bash
# Check database connectivity
pg_isready -h localhost -U exam_app -d exam_framework_prod

# Check database size
psql -U exam_app -d exam_framework_prod -c "SELECT pg_size_pretty(pg_database_size('exam_framework_prod'));"
```

### External Service Health

```bash
# Check OpenAI API
curl -H "Authorization: Bearer $OPENAI_API_KEY" \
     https://api.openai.com/v1/models

# Check Redis
redis-cli ping
```

## Log Analysis

### Application Logs

```bash
# Filter error logs
grep -i error /var/log/exam-framework/application.log

# Filter OpenAI errors
grep -i openai /var/log/exam-framework/application.log

# Filter security events
grep -i security /var/log/exam-framework/application.log
```

### Database Logs

```bash
# Check PostgreSQL logs
tail -f /var/log/postgresql/postgresql.log | grep ERROR

# Check slow queries
tail -f /var/log/postgresql/postgresql.log | grep "slow query"
```

### Access Logs

```bash
# Analyze Nginx access logs
awk '{print $1, $7, $9}' /var/log/nginx/access.log | sort | uniq -c | sort -nr

# Check for suspicious activity
awk '$9 >= 400 {print $1, $7, $9}' /var/log/nginx/access.log
```

## Emergency Procedures

### Application Down

1. **Immediate Actions**
```bash
# Check application status
systemctl status exam-framework

# Restart application
systemctl restart exam-framework

# Check logs
journalctl -u exam-framework -f
```

2. **Rollback Plan**
```bash
# Rollback to previous version
docker-compose down
docker-compose pull exam-framework:previous-version
docker-compose up -d
```

3. **Emergency Contact**
```bash
# Notify team
curl -X POST -H "Content-Type: application/json" \
     -d '{"text":"Exam Framework is DOWN - Immediate attention required"}' \
     https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK
```

### Database Corruption

1. **Immediate Actions**
```bash
# Stop application
systemctl stop exam-framework

# Backup current database
pg_dump exam_framework_prod > emergency_backup.sql
```

2. **Recovery Plan**
```bash
# Restore from backup
psql -U exam_app -d exam_framework_prod < backup_20240101_120000.sql

# Verify data integrity
psql -U exam_app -d exam_framework_prod -c "SELECT count(*) FROM students;"
```

3. **Data Validation**
```bash
# Check data integrity
psql -U exam_app -d exam_framework_prod -c "
SELECT 
    (SELECT count(*) FROM students) as students,
    (SELECT count(*) FROM exams) as exams,
    (SELECT count(*) FROM questions) as questions;
"
```

### Security Breach

1. **Immediate Actions**
```bash
# Block suspicious IPs
iptables -A INPUT -s <suspicious-ip> -j DROP

# Change passwords
openssl rand -base64 32  # Generate new password
```

2. **Investigation**
```bash
# Check access logs
grep <suspicious-ip> /var/log/nginx/access.log

# Check failed authentication attempts
grep "AUTH_FAILURE" /var/log/exam-framework/application.log
```

3. **Recovery**
```bash
# Reset all user passwords
# Force password change on next login
# Review and update security policies
```

This troubleshooting guide provides comprehensive solutions for common issues and emergency procedures to ensure smooth operation of the Aentic Exam Framework.
