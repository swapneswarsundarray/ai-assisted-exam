# Aentic Exam Framework - Production Configuration Guide

## Table of Contents
1. [Environment Setup](#environment-setup)
2. [Database Configuration](#database-configuration)
3. [Application Configuration](#application-configuration)
4. [Security Configuration](#security-configuration)
5. [Performance Configuration](#performance-configuration)
6. [Monitoring Configuration](#monitoring-configuration)
7. [Logging Configuration](#logging-configuration)
8. [External Services Configuration](#external-services-configuration)
9. [Environment Variables](#environment-variables)
10. [Docker Configuration](#docker-configuration)
11. [Kubernetes Configuration](#kubernetes-configuration)

## Environment Setup

### Prerequisites
- **Java 17+**: OpenJDK or Oracle JDK
- **Maven 3.6+**: Build tool
- **PostgreSQL 13+**: Production database
- **Redis 6+**: Caching and session storage
- **Nginx/HAProxy**: Load balancer (optional)

### System Requirements
- **Minimum**: 2 CPU cores, 4GB RAM, 20GB storage
- **Recommended**: 4 CPU cores, 8GB RAM, 50GB storage
- **High Load**: 8+ CPU cores, 16GB+ RAM, 100GB+ storage

## Database Configuration

### PostgreSQL Setup

#### 1. Database Creation
```sql
-- Create database
CREATE DATABASE exam_framework_prod;

-- Create user
CREATE USER exam_app WITH PASSWORD 'secure_password_here';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE exam_framework_prod TO exam_app;

-- Connect to database
\c exam_framework_prod;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO exam_app;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO exam_app;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO exam_app;
```

#### 2. Connection Configuration
```properties
# Production Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/exam_framework_prod
spring.datasource.username=exam_app
spring.datasource.password=secure_password_here
spring.datasource.driver-class-name=org.postgresql.Driver

# Connection Pooling (HikariCP)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.leak-detection-threshold=60000
```

#### 3. Database Migration (Flyway)
```properties
# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.validate-on-migrate=true
spring.flyway.out-of-order=false
spring.flyway.locations=classpath:db/migration
```

### Redis Configuration

#### 1. Redis Setup
```bash
# Install Redis (Ubuntu/Debian)
sudo apt-get update
sudo apt-get install redis-server

# Configure Redis
sudo nano /etc/redis/redis.conf
```

#### 2. Redis Configuration
```properties
# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=redis_password_here
spring.redis.timeout=2000ms
spring.redis.lettuce.pool.max-active=8
spring.redis.lettuce.pool.max-idle=8
spring.redis.lettuce.pool.min-idle=0
spring.redis.lettuce.pool.max-wait=-1ms
```

## Application Configuration

### Production Application Properties

Create `application-prod.properties`:

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/
server.tomcat.max-threads=200
server.tomcat.min-spare-threads=10
server.tomcat.connection-timeout=20000
server.tomcat.max-connections=8192

# Spring Boot Actuator
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.health.defaults.enabled=false
management.health.db.enabled=true
management.health.redis.enabled=true

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory

# Cache Configuration
spring.cache.type=redis
spring.cache.redis.time-to-live=600000
spring.cache.cache-names=students,topics,questions,reports

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.enabled=true

# OpenAI Configuration
openai.api.key=${OPENAI_API_KEY}
openai.api.url=https://api.openai.com/v1/chat/completions
openai.timeout=30000
openai.max-retries=3

# Security Configuration
spring.security.enabled=true
spring.security.require-ssl=true
spring.security.headers.frame-options=DENY
spring.security.headers.content-type-options=nosniff
spring.security.headers.xss-protection=1; mode=block

# Session Configuration
server.servlet.session.timeout=1800
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict

# CORS Configuration
spring.web.cors.allowed-origins=https://yourdomain.com
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.web.cors.allowed-headers=*
spring.web.cors.allow-credentials=true
```

### Profile-Specific Configuration

Create `application.yml` for better organization:

```yaml
spring:
  profiles:
    active: prod
  
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/exam_framework_prod}
    username: ${DATABASE_USERNAME:exam_app}
    password: ${DATABASE_PASSWORD:secure_password_here}
    hikari:
      maximum-pool-size: ${DB_POOL_SIZE:20}
      minimum-idle: ${DB_MIN_IDLE:5}
      
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: false
        jdbc:
          batch_size: 20
          
server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /
    session:
      timeout: 1800
      cookie:
        http-only: true
        secure: true
        same-site: strict
        
logging:
  level:
    com.aentic.exam: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/exam-framework/application.log
    max-size: 100MB
    max-history: 30
```

## Security Configuration

### SSL/TLS Configuration

#### 1. SSL Certificate Setup
```bash
# Generate self-signed certificate (for testing)
keytool -genkeypair -alias exam-framework -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 365

# Production: Use Let's Encrypt or commercial certificate
```

#### 2. SSL Configuration
```properties
# SSL Configuration
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=keystore_password
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=exam-framework
server.ssl.trust-store=classpath:truststore.p12
server.ssl.trust-store-password=truststore_password
```

### Security Headers Configuration

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .requiresChannel()
                .requestMatchers(r -> r.getRequestURI().startsWith("/")).requiresSecure()
                .and()
            .headers()
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true))
                .and()
            .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
            .authorizeHttpRequests()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .and()
            .exceptionHandling()
                .accessDeniedPage("/access-denied");
                
        return http.build();
    }
}
```

## Performance Configuration

### JVM Tuning

```bash
# Production JVM Options
JAVA_OPTS="-Xms2g -Xmx4g \
           -XX:+UseG1GC \
           -XX:MaxGCPauseMillis=200 \
           -XX:+UseStringDeduplication \
           -XX:+OptimizeStringConcat \
           -XX:+UseCompressedOops \
           -XX:+UseCompressedClassPointers \
           -Djava.security.egd=file:/dev/./urandom \
           -Dspring.profiles.active=prod"
```

### Connection Pooling Optimization

```properties
# Advanced HikariCP Configuration
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.validation-timeout=3000
spring.datasource.hikari.leak-detection-threshold=60000
spring.datasource.hikari.pool-name=ExamFrameworkHikariPool
```

### Caching Strategy

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues()
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
                
        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(config)
            .transactionAware()
            .build();
    }
}
```

## Monitoring Configuration

### Micrometer & Prometheus

```properties
# Metrics Configuration
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.metrics.enabled=true
management.endpoint.prometheus.enabled=true
management.metrics.export.prometheus.enabled=true
management.metrics.export.prometheus.step=60s

# Custom Metrics
management.metrics.tags.application=exam-framework
management.metrics.tags.environment=${spring.profiles.active}
```

### Health Checks

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // Custom health check logic
        return Health.up()
            .withDetail("database", "Available")
            .withDetail("openai", "Available")
            .withDetail("cache", "Available")
            .build();
    }
}
```

## Logging Configuration

### Logback Configuration (logback-spring.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>/var/log/exam-framework/application.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>/var/log/exam-framework/application.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                    <maxFileSize>100MB</maxFileSize>
                </timeBasedFileNamingAndTriggeringPolicy>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        
        <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>/var/log/exam-framework/error.log</file>
            <filter class="ch.qos.logback.classic.filter.LevelFilter">
                <level>ERROR</level>
                <onMatch>ACCEPT</onMatch>
                <onMismatch>DENY</onMismatch>
            </filter>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>/var/log/exam-framework/error.%d{yyyy-MM-dd}.log</fileNamePattern>
                <maxHistory>30</maxHistory>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        
        <root level="INFO">
            <appender-ref ref="FILE"/>
            <appender-ref ref="ERROR_FILE"/>
        </root>
        
        <logger name="com.aentic.exam" level="INFO"/>
        <logger name="org.springframework.security" level="WARN"/>
        <logger name="org.hibernate.SQL" level="WARN"/>
    </springProfile>
</configuration>
```

## External Services Configuration

### OpenAI API Configuration

```java
@Configuration
public class OpenAIConfig {
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    @Value("${openai.api.url}")
    private String apiUrl;
    
    @Value("${openai.timeout:30000}")
    private int timeout;
    
    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(apiKey, Duration.ofMillis(timeout));
    }
    
    @Bean
    public RestTemplate openAiRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Configure timeout
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        
        restTemplate.setRequestFactory(factory);
        
        // Add interceptors for API key
        restTemplate.setInterceptors(List.of(new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
                    ClientHttpRequestExecution execution) throws IOException {
                request.getHeaders().set("Authorization", "Bearer " + apiKey);
                return execution.execute(request, body);
            }
        }));
        
        return restTemplate;
    }
}
```

## Environment Variables

### Production Environment Variables

```bash
# Database Configuration
export DATABASE_URL=jdbc:postgresql://localhost:5432/exam_framework_prod
export DATABASE_USERNAME=exam_app
export DATABASE_PASSWORD=secure_password_here

# Redis Configuration
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=redis_password_here

# OpenAI Configuration
export OPENAI_API_KEY=sk-your-openai-api-key

# Application Configuration
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=prod

# Security Configuration
export SSL_KEYSTORE_PASSWORD=keystore_password
export SSL_TRUSTSTORE_PASSWORD=truststore_password

# Logging Configuration
export LOGGING_LEVEL=INFO
export LOG_FILE_PATH=/var/log/exam-framework/application.log

# Performance Configuration
export DB_POOL_SIZE=20
export DB_MIN_IDLE=5
export JVM_HEAP_SIZE=4g
```

### Environment File (.env)

```bash
# .env file for production
DATABASE_URL=jdbc:postgresql://localhost:5432/exam_framework_prod
DATABASE_USERNAME=exam_app
DATABASE_PASSWORD=secure_password_here
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis_password_here
OPENAI_API_KEY=sk-your-openai-api-key
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
SSL_KEYSTORE_PASSWORD=keystore_password
SSL_TRUSTSTORE_PASSWORD=truststore_password
LOGGING_LEVEL=INFO
LOG_FILE_PATH=/var/log/exam-framework/application.log
DB_POOL_SIZE=20
DB_MIN_IDLE=5
JVM_HEAP_SIZE=4g
```

## Docker Configuration

### Dockerfile

```dockerfile
# Multi-stage Dockerfile
FROM openjdk:17-jdk-slim as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jre-slim

# Install necessary packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create application user
RUN groupadd -r examapp && useradd -r -g examapp examapp

# Set working directory
WORKDIR /app

# Copy JAR file
COPY --from=builder /app/target/exam-framework-1.0.0.jar app.jar

# Create log directory
RUN mkdir -p /var/log/exam-framework && \
    chown -R examapp:examapp /var/log/exam-framework

# Switch to non-root user
USER examapp

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Start application
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

### Docker Compose

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_URL=jdbc:postgresql://postgres:5432/exam_framework_prod
      - DATABASE_USERNAME=exam_app
      - DATABASE_PASSWORD=secure_password_here
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - OPENAI_API_KEY=${OPENAI_API_KEY}
    depends_on:
      - postgres
      - redis
    volumes:
      - ./logs:/var/log/exam-framework
    restart: unless-stopped

  postgres:
    image: postgres:13
    environment:
      - POSTGRES_DB=exam_framework_prod
      - POSTGRES_USER=exam_app
      - POSTGRES_PASSWORD=secure_password_here
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    restart: unless-stopped

  redis:
    image: redis:6-alpine
    command: redis-server --requirepass redis_password_here
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - app
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:
```

## Kubernetes Configuration

### Deployment YAML

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: exam-framework
  labels:
    app: exam-framework
spec:
  replicas: 3
  selector:
    matchLabels:
      app: exam-framework
  template:
    metadata:
      labels:
        app: exam-framework
    spec:
      containers:
      - name: exam-framework
        image: exam-framework:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: exam-framework-secrets
              key: database-url
        - name: DATABASE_USERNAME
          valueFrom:
            secretKeyRef:
              name: exam-framework-secrets
              key: database-username
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: exam-framework-secrets
              key: database-password
        - name: OPENAI_API_KEY
          valueFrom:
            secretKeyRef:
              name: exam-framework-secrets
              key: openai-api-key
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        volumeMounts:
        - name: logs
          mountPath: /var/log/exam-framework
      volumes:
      - name: logs
        persistentVolumeClaim:
          claimName: exam-framework-logs
---
apiVersion: v1
kind: Service
metadata:
  name: exam-framework-service
spec:
  selector:
    app: exam-framework
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: exam-framework-ingress
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  tls:
  - hosts:
    - exam-framework.yourdomain.com
    secretName: exam-framework-tls
  rules:
  - host: exam-framework.yourdomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: exam-framework-service
            port:
              number: 80
```

### ConfigMap and Secrets

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: exam-framework-config
data:
  application.yml: |
    spring:
      profiles:
        active: prod
      redis:
        host: redis-service
        port: 6379
    server:
      port: 8080
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
---
apiVersion: v1
kind: Secret
metadata:
  name: exam-framework-secrets
type: Opaque
data:
  database-url: <base64-encoded-database-url>
  database-username: <base64-encoded-username>
  database-password: <base64-encoded-password>
  openai-api-key: <base64-encoded-openai-key>
```

## Production Deployment Checklist

### Pre-Deployment Checklist
- [ ] Database schema created and migrated
- [ ] SSL certificates configured
- [ ] Environment variables set
- [ ] Monitoring and logging configured
- [ ] Backup strategy implemented
- [ ] Security headers configured
- [ ] Performance tuning completed
- [ ] Load testing performed

### Post-Deployment Checklist
- [ ] Health checks passing
- [ ] Metrics collection working
- [ ] Log aggregation functioning
- [ ] SSL certificates valid
- [ ] Database connections stable
- [ ] Cache system operational
- [ ] External services accessible
- [ ] Backup processes running

This production configuration guide provides a comprehensive setup for deploying the Aentic Exam Framework in a production environment with proper security, performance, and monitoring configurations.
