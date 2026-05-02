# Aentic Exam Framework - Deployment and Scaling Guide

## Table of Contents
1. [Deployment Strategies](#deployment-strategies)
2. [Traditional Server Deployment](#traditional-server-deployment)
3. [Container Deployment](#container-deployment)
4. [Kubernetes Deployment](#kubernetes-deployment)
5. [Cloud Deployment](#cloud-deployment)
6. [Scaling Strategies](#scaling-strategies)
7. [Load Balancing](#load-balancing)
8. [Database Scaling](#database-scaling)
9. [Performance Optimization](#performance-optimization)
10. [Monitoring and Alerting](#monitoring-and-alerting)
11. [Disaster Recovery](#disaster-recovery)
12. [CI/CD Pipeline](#cicd-pipeline)

## Deployment Strategies

### Overview

The Aentic Exam Framework supports multiple deployment strategies:

1. **Traditional Server Deployment** - Direct JAR deployment on servers
2. **Container Deployment** - Docker containers with orchestration
3. **Kubernetes Deployment** - Full cloud-native deployment
4. **Cloud Platform Deployment** - Managed services (AWS, Azure, GCP)

### Choosing the Right Strategy

| Strategy | Best For | Complexity | Scalability | Cost |
|-----------|-----------|------------|--------------|------|
| Traditional | Small teams, simple setup | Low | Limited | Low |
| Container | Medium teams, moderate scale | Medium | Good | Medium |
| Kubernetes | Large teams, high scale | High | Excellent | High |
| Cloud Platform | Rapid deployment, managed services | Low-Medium | Excellent | Medium-High |

## Traditional Server Deployment

### Prerequisites

- **Server Requirements**: 4GB RAM, 2 CPU cores minimum
- **Operating System**: Ubuntu 20.04+, CentOS 8+, or RHEL 8+
- **Java**: OpenJDK 17 or Oracle JDK 17
- **Database**: PostgreSQL 13+ or MySQL 8.0+
- **Web Server**: Nginx or Apache (optional)

### Step-by-Step Deployment

#### 1. Server Preparation

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Java 17
sudo apt install openjdk-17-jdk -y

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib -y

# Install Nginx
sudo apt install nginx -y

# Create application user
sudo useradd -m -s /bin/bash examapp
sudo usermod -aG sudo examapp
```

#### 2. Database Setup

```bash
# Switch to postgres user
sudo -u postgres psql

# Create database and user
CREATE DATABASE exam_framework_prod;
CREATE USER exam_app WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE exam_framework_prod TO exam_app;
\q

# Configure PostgreSQL
sudo nano /etc/postgresql/13/main/postgresql.conf
# Set: listen_addresses = 'localhost'

sudo nano /etc/postgresql/13/main/pg_hba.conf
# Add: local   exam_framework_prod   exam_app   md5

sudo systemctl restart postgresql
```

#### 3. Application Deployment

```bash
# Create application directory
sudo mkdir -p /opt/exam-framework
sudo chown examapp:examapp /opt/exam-framework

# Switch to application user
sudo su - examapp

# Download and extract application
cd /opt/exam-framework
wget https://your-repo/exam-framework-1.0.0.jar
chmod +x exam-framework-1.0.0.jar

# Create environment file
cat > .env << EOF
DATABASE_URL=jdbc:postgresql://localhost:5432/exam_framework_prod
DATABASE_USERNAME=exam_app
DATABASE_PASSWORD=secure_password
OPENAI_API_KEY=your-openai-api-key
SPRING_PROFILES_ACTIVE=prod
EOF
```

#### 4. Systemd Service Configuration

```bash
# Create systemd service file
sudo nano /etc/systemd/system/exam-framework.service
```

```ini
[Unit]
Description=Exam Framework Application
After=network.target postgresql.service

[Service]
Type=simple
User=examapp
Group=examapp
WorkingDirectory=/opt/exam-framework
Environment=JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
EnvironmentFile=/opt/exam-framework/.env
ExecStart=/usr/lib/jvm/java-17-openjdk-amd64/bin/java -jar exam-framework-1.0.0.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

```bash
# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable exam-framework
sudo systemctl start exam-framework
sudo systemctl status exam-framework
```

#### 5. Nginx Configuration

```bash
# Create Nginx configuration
sudo nano /etc/nginx/sites-available/exam-framework
```

```nginx
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;

    ssl_certificate /etc/ssl/certs/your-domain.crt;
    ssl_certificate_key /etc/ssl/private/your-domain.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
    }

    location /actuator/health {
        proxy_pass http://localhost:8080/actuator/health;
        access_log off;
    }
}
```

```bash
# Enable site and restart Nginx
sudo ln -s /etc/nginx/sites-available/exam-framework /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

## Container Deployment

### Dockerfile Optimization

```dockerfile
# Multi-stage build for optimized image
FROM openjdk:17-jdk-slim as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build application
RUN ./mvnw clean package -DskipTests

# Runtime image
FROM openjdk:17-jre-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r examapp && useradd -r -g examapp examapp

# Set working directory
WORKDIR /app

# Copy application
COPY --from=builder /app/target/exam-framework-1.0.0.jar app.jar

# Create log directory
RUN mkdir -p /var/log/exam-framework && \
    chown -R examapp:examapp /var/log/exam-framework

# Switch to non-root user
USER examapp

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Start application
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

### Docker Compose Configuration

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
      - DATABASE_PASSWORD=secure_password
      - REDIS_HOST=redis
      - OPENAI_API_KEY=${OPENAI_API_KEY}
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    volumes:
      - ./logs:/var/log/exam-framework
      - ./reports:/app/reports
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  postgres:
    image: postgres:13
    environment:
      - POSTGRES_DB=exam_framework_prod
      - POSTGRES_USER=exam_app
      - POSTGRES_PASSWORD=secure_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U exam_app -d exam_framework_prod"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:6-alpine
    command: redis-server --requirepass redis_password --appendonly yes
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/nginx/ssl:ro
      - ./logs/nginx:/var/log/nginx
    depends_on:
      - app
    restart: unless-stopped

volumes:
  postgres_data:
    driver: local
  redis_data:
    driver: local
```

### Deployment Commands

```bash
# Build and start containers
docker-compose up -d --build

# Check status
docker-compose ps

# View logs
docker-compose logs -f app

# Scale application
docker-compose up -d --scale app=3

# Update application
docker-compose pull
docker-compose up -d
```

## Kubernetes Deployment

### Namespace and RBAC

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: exam-framework
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: exam-framework
  name: exam-framework-role
rules:
- apiGroups: [""]
  resources: ["pods", "services", "configmaps", "secrets"]
  verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  namespace: exam-framework
  name: exam-framework-rolebinding
subjects:
- kind: ServiceAccount
  name: exam-framework
  namespace: exam-framework
roleRef:
  kind: Role
  name: exam-framework-role
  apiGroup: rbac.authorization.k8s.io
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: exam-framework
  namespace: exam-framework
```

### Deployment Configuration

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: exam-framework
  namespace: exam-framework
  labels:
    app: exam-framework
    version: v1.0.0
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: exam-framework
  template:
    metadata:
      labels:
        app: exam-framework
        version: v1.0.0
    spec:
      serviceAccountName: exam-framework
      containers:
      - name: exam-framework
        image: exam-framework:1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8080
          name: http
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
        - name: REDIS_HOST
          value: "redis-service"
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
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        volumeMounts:
        - name: logs
          mountPath: /var/log/exam-framework
        - name: reports
          mountPath: /app/reports
      volumes:
      - name: logs
        persistentVolumeClaim:
          claimName: exam-framework-logs
      - name: reports
        persistentVolumeClaim:
          claimName: exam-framework-reports
      terminationGracePeriodSeconds: 30
```

### Service Configuration

```yaml
apiVersion: v1
kind: Service
metadata:
  name: exam-framework-service
  namespace: exam-framework
  labels:
    app: exam-framework
spec:
  type: ClusterIP
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
    name: http
  selector:
    app: exam-framework
---
apiVersion: v1
kind: Service
metadata:
  name: exam-framework-headless
  namespace: exam-framework
  labels:
    app: exam-framework
spec:
  type: ClusterIP
  clusterIP: None
  ports:
  - port: 8080
    targetPort: 8080
    protocol: TCP
    name: http
  selector:
    app: exam-framework
```

### Ingress Configuration

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: exam-framework-ingress
  namespace: exam-framework
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
    nginx.ingress.kubernetes.io/proxy-body-size: "10m"
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/rate-limit-window: "1m"
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

### Horizontal Pod Autoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: exam-framework-hpa
  namespace: exam-framework
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: exam-framework
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
```

## Cloud Deployment

### AWS Deployment

#### 1. ECS (Elastic Container Service)

```json
{
  "family": "exam-framework",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "executionRoleArn": "arn:aws:iam::account:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::account:role/ecsTaskRole",
  "containerDefinitions": [
    {
      "name": "exam-framework",
      "image": "your-account.dkr.ecr.region.amazonaws.com/exam-framework:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        }
      ],
      "secrets": [
        {
          "name": "DATABASE_URL",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:exam-framework/db-url"
        },
        {
          "name": "OPENAI_API_KEY",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:exam-framework/openai-key"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/exam-framework",
          "awslogs-region": "us-west-2",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

#### 2. CloudFormation Template

```yaml
AWSTemplateFormatVersion: '2010-09-09'
Description: 'Exam Framework Infrastructure'

Parameters:
  Environment:
    Type: String
    Default: prod
    AllowedValues: [dev, staging, prod]
  
  DatabasePassword:
    Type: String
    NoEcho: true
    Description: 'Database password'

Resources:
  # VPC Configuration
  VPC:
    Type: AWS::EC2::VPC
    Properties:
      CidrBlock: 10.0.0.0/16
      EnableDnsHostnames: true
      EnableDnsSupport: true
      Tags:
        - Key: Name
          Value: !Sub '${Environment}-exam-framework-vpc'

  # Public Subnets
  PublicSubnet1:
    Type: AWS::EC2::Subnet
    Properties:
      VpcId: !Ref VPC
      CidrBlock: 10.0.1.0/24
      AvailabilityZone: !Select [0, !GetAZs '']
      MapPublicIpOnLaunch: true
      Tags:
        - Key: Name
          Value: !Sub '${Environment}-public-subnet-1'

  PublicSubnet2:
    Type: AWS::EC2::Subnet
    Properties:
      VpcId: !Ref VPC
      CidrBlock: 10.0.2.0/24
      AvailabilityZone: !Select [1, !GetAZs '']
      MapPublicIpOnLaunch: true
      Tags:
        - Key: Name
          Value: !Sub '${Environment}-public-subnet-2'

  # RDS Database
  DatabaseSubnetGroup:
    Type: AWS::RDS::DBSubnetGroup
    Properties:
      DBSubnetGroupDescription: Subnet group for RDS database
      SubnetIds:
        - !Ref PublicSubnet1
        - !Ref PublicSubnet2
      Tags:
        - Key: Name
          Value: !Sub '${Environment}-db-subnet-group'

  Database:
    Type: AWS::RDS::DBInstance
    Properties:
      DBInstanceIdentifier: !Sub '${Environment}-exam-framework-db'
      DBInstanceClass: db.t3.medium
      Engine: postgres
      EngineVersion: '13.7'
      AllocatedStorage: 100
      StorageType: gp2
      DBName: exam_framework_prod
      MasterUsername: exam_app
      MasterUserPassword: !Ref DatabasePassword
      DBSubnetGroupName: !Ref DatabaseSubnetGroup
      VPCSecurityGroups:
        - !Ref DatabaseSecurityGroup
      BackupRetentionPeriod: 7
      MultiAZ: false
      StorageEncrypted: true
      Tags:
        - Key: Name
          Value: !Sub '${Environment}-exam-framework-db'

  # ECS Cluster
  ECSCluster:
    Type: AWS::ECS::Cluster
    Properties:
      ClusterName: !Sub '${Environment}-exam-framework'
      CapacityProviders:
        - FARGATE
        - FARGATE_SPOT
      DefaultCapacityProviderStrategy:
        - CapacityProvider: FARGATE
          Weight: 1

  # Application Load Balancer
  LoadBalancer:
    Type: AWS::ElasticLoadBalancingV2::LoadBalancer
    Properties:
      Name: !Sub '${Environment}-exam-framework-alb'
      Scheme: internet-facing
      Type: application
      Subnets:
        - !Ref PublicSubnet1
        - !Ref PublicSubnet2
      SecurityGroups:
        - !Ref LoadBalancerSecurityGroup
      Tags:
        - Key: Name
          Value: !Sub '${Environment}-exam-framework-alb'

Outputs:
  LoadBalancerDNS:
    Description: 'Load Balancer DNS Name'
    Value: !GetAtt LoadBalancer.DNSName
    Export:
      Name: !Sub '${Environment}-LoadBalancerDNS'
```

### Azure Deployment

#### ARM Template for Azure Container Instances

```json
{
  "$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#",
  "contentVersion": "1.0.0.0",
  "parameters": {
    "containerGroupName": {
      "type": "string",
      "defaultValue": "exam-framework-aci",
      "metadata": {
        "description": "Container group name"
      }
    },
    "openaiApiKey": {
      "type": "secureString",
      "metadata": {
        "description": "OpenAI API Key"
      }
    }
  },
  "resources": [
    {
      "type": "Microsoft.ContainerInstance/containerGroups",
      "apiVersion": "2021-09-01",
      "name": "[parameters('containerGroupName')]",
      "location": "[resourceGroup().location]",
      "properties": {
        "containers": [
          {
            "name": "exam-framework",
            "properties": {
              "image": "your-registry/exam-framework:latest",
              "resources": {
                "requests": {
                  "cpu": 1.0,
                  "memoryInGb": 2.0
                }
              },
              "ports": [
                {
                  "port": 8080,
                  "protocol": "TCP"
                }
              ],
              "environmentVariables": [
                {
                  "name": "SPRING_PROFILES_ACTIVE",
                  "value": "prod"
                },
                {
                  "name": "OPENAI_API_KEY",
                  "secureValue": "[parameters('openaiApiKey')]"
                }
              ],
              "livenessProbe": {
                "exec": {
                  "command": [
                    "curl",
                    "-f",
                    "http://localhost:8080/actuator/health"
                  ]
                },
                "initialDelaySeconds": 60,
                "periodSeconds": 30
              },
              "readinessProbe": {
                "exec": {
                  "command": [
                    "curl",
                    "-f",
                    "http://localhost:8080/actuator/health"
                  ]
                },
                "initialDelaySeconds": 30,
                "periodSeconds": 10
              }
            }
          }
        ],
        "ipAddress": {
          "type": "Public",
          "ports": [
            {
              "port": 80,
              "protocol": "TCP"
            }
          ]
        },
        "restartPolicy": "Always",
        "osType": "Linux"
      }
    }
  ]
}
```

## Scaling Strategies

### Vertical Scaling

#### Resource Optimization

```yaml
# Increase resources for higher load
apiVersion: v1
kind: Deployment
metadata:
  name: exam-framework
spec:
  template:
    spec:
      containers:
      - name: exam-framework
        resources:
          requests:
            memory: "2Gi"    # Increased from 1Gi
            cpu: "1000m"     # Increased from 500m
          limits:
            memory: "4Gi"    # Increased from 2Gi
            cpu: "2000m"     # Increased from 1000m
```

#### JVM Tuning for Vertical Scaling

```bash
# Production JVM settings for larger instances
JAVA_OPTS="-Xms4g -Xmx8g \
           -XX:+UseG1GC \
           -XX:MaxGCPauseMillis=200 \
           -XX:+UseStringDeduplication \
           -XX:+OptimizeStringConcat \
           -XX:+UseCompressedOops \
           -XX:+UseCompressedClassPointers \
           -XX:G1HeapRegionSize=16m \
           -XX:+UseLargePages \
           -Djava.security.egd=file:/dev/./urandom"
```

### Horizontal Scaling

#### Auto-scaling Configuration

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: exam-framework-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: exam-framework
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
```

#### Cluster Autoscaler

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cluster-autoscaler
  namespace: kube-system
spec:
  template:
    spec:
      containers:
      - image: k8s.gcr.io/autoscaling/cluster-autoscaler:v1.21.0
        name: cluster-autoscaler
        resources:
          limits:
            cpu: 100m
            memory: 300Mi
          requests:
            cpu: 100m
            memory: 300Mi
        command:
        - ./cluster-autoscaler
        - --v=4
        - --stderrthreshold=info
        - --cloud-provider=aws
        - --skip-nodes-with-local-storage=false
        - --expander=least-waste
        - --node-group-auto-discovery=asg:tag=k8s.io/cluster-autoscaler/enabled,k8s.io/cluster-autoscaler/exam-framework
```

## Load Balancing

### Nginx Load Balancer Configuration

```nginx
upstream exam_framework_backend {
    least_conn;
    server app1:8080 max_fails=3 fail_timeout=30s;
    server app2:8080 max_fails=3 fail_timeout=30s;
    server app3:8080 max_fails=3 fail_timeout=30s;
    keepalive 32;
}

server {
    listen 80;
    server_name exam-framework.yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name exam-framework.yourdomain.com;

    ssl_certificate /etc/ssl/certs/exam-framework.crt;
    ssl_certificate_key /etc/ssl/private/exam-framework.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512;
    ssl_prefer_server_ciphers off;

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    limit_req_zone $binary_remote_addr zone=login:10m rate=5r/m;

    location / {
        proxy_pass http://exam_framework_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;
    }

    location /api/ {
        limit_req zone=api burst=20 nodelay;
        proxy_pass http://exam_framework_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /login {
        limit_req zone=login burst=5 nodelay;
        proxy_pass http://exam_framework_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Health check endpoint
    location /actuator/health {
        proxy_pass http://exam_framework_backend;
        access_log off;
        proxy_connect_timeout 5s;
        proxy_send_timeout 5s;
        proxy_read_timeout 5s;
    }
}
```

### HAProxy Configuration

```haproxy
global
    daemon
    maxconn 4096
    log stdout local0

defaults
    mode http
    timeout connect 5000ms
    timeout client 50000ms
    timeout server 50000ms
    option httplog
    option dontlognull
    retries 3

frontend exam_framework_frontend
    bind *:80
    bind *:443 ssl crt /etc/ssl/certs/exam-framework.pem
    redirect scheme https if !{ ssl_fc }
    default_backend exam_framework_backend

backend exam_framework_backend
    balance roundrobin
    option httpchk GET /actuator/health
    server app1 app1:8080 check
    server app2 app2:8080 check
    server app3 app3:8080 check

backend exam_framework_api
    balance leastconn
    option httpchk GET /actuator/health
    server api1 api1:8080 check
    server api2 api2:8080 check
    server api3 api3:8080 check
```

## Database Scaling

### Read Replicas Configuration

```sql
-- Master configuration
-- postgresql.conf
wal_level = replica
max_wal_senders = 3
wal_keep_segments = 64
archive_mode = on
archive_command = 'cp %p /var/lib/postgresql/archive/%f'

-- pg_hba.conf
host replication replicator 10.0.1.0/24  md5
```

```yaml
# Kubernetes StatefulSet for PostgreSQL
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgresql-master
spec:
  serviceName: postgresql-master
  replicas: 1
  selector:
    matchLabels:
      app: postgresql
      role: master
  template:
    metadata:
      labels:
        app: postgresql
        role: master
    spec:
      containers:
      - name: postgresql
        image: postgres:13
        env:
        - name: POSTGRES_DB
          value: exam_framework_prod
        - name: POSTGRES_USER
          value: exam_app
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgresql-secrets
              key: password
        - name: POSTGRES_REPLICATION_USER
          value: replicator
        - name: POSTGRES_REPLICATION_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgresql-secrets
              key: replication-password
        ports:
        - containerPort: 5432
        volumeMounts:
        - name: postgresql-storage
          mountPath: /var/lib/postgresql/data
        - name: postgresql-config
          mountPath: /etc/postgresql/postgresql.conf
          subPath: postgresql.conf
  volumeClaimTemplates:
  - metadata:
      name: postgresql-storage
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 100Gi
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgresql-replica
spec:
  serviceName: postgresql-replica
  replicas: 2
  selector:
    matchLabels:
      app: postgresql
      role: replica
  template:
    metadata:
      labels:
        app: postgresql
        role: replica
    spec:
      containers:
      - name: postgresql
        image: postgres:13
        env:
        - name: PGUSER
          value: postgres
        - name: POSTGRES_MASTER_SERVICE
          value: postgresql-master
        - name: POSTGRES_REPLICATION_USER
          value: replicator
        - name: POSTGRES_REPLICATION_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgresql-secrets
              key: replication-password
        ports:
        - containerPort: 5432
        volumeMounts:
        - name: postgresql-storage
          mountPath: /var/lib/postgresql/data
  volumeClaimTemplates:
  - metadata:
      name: postgresql-storage
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 100Gi
```

### Connection Pooling

```properties
# Advanced HikariCP configuration for high load
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.validation-timeout=3000
spring.datasource.hikari.leak-detection-threshold=60000
spring.datasource.hikari.pool-name=ExamFrameworkHikariPool

# Read/Write splitting configuration
spring.datasource.primary.jdbc-url=jdbc:postgresql://master:5432/exam_framework_prod
spring.datasource.primary.username=exam_app
spring.datasource.primary.password=${DATABASE_PASSWORD}
spring.datasource.primary.hikari.maximum-pool-size=30

spring.datasource.replica.jdbc-url=jdbc:postgresql://replica:5432/exam_framework_prod
spring.datasource.replica.username=exam_app
spring.datasource.replica.password=${DATABASE_PASSWORD}
spring.datasource.replica.hikari.maximum-pool-size=20
```

## Performance Optimization

### Application Performance

```java
@Configuration
public class PerformanceConfig {
    
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("exam-framework-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
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

### Database Optimization

```sql
-- Create indexes for better performance
CREATE INDEX CONCURRENTLY idx_student_email ON students(email);
CREATE INDEX CONCURRENTLY idx_exam_student_id ON exams(student_id);
CREATE INDEX CONCURRENTLY idx_exam_topic_id ON exams(topic_id);
CREATE INDEX CONCURRENTLY idx_answer_exam_id ON answers(exam_id);
CREATE INDEX CONCURRENTLY idx_answer_question_id ON answers(question_id);
CREATE INDEX CONCURRENTLY idx_question_topic_id ON questions(topic_id);

-- Partition large tables
CREATE TABLE answers_2024 PARTITION OF answers
FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');

-- Optimize queries with materialized views
CREATE MATERIALIZED VIEW exam_stats AS
SELECT 
    e.id,
    e.student_id,
    e.topic_id,
    COUNT(a.id) as total_questions,
    COUNT(CASE WHEN a.is_correct = true THEN 1 END) as correct_answers,
    e.total_score,
    e.max_score
FROM exams e
LEFT JOIN answers a ON e.id = a.exam_id
GROUP BY e.id, e.student_id, e.topic_id, e.total_score, e.max_score;

-- Refresh materialized view periodically
CREATE OR REPLACE FUNCTION refresh_exam_stats()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY exam_stats;
END;
$$ LANGUAGE plpgsql;
```

## Monitoring and Alerting

### Prometheus Configuration

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
data:
  prometheus.yml: |
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
    
    alerting:
      alertmanagers:
        - static_configs:
            - targets:
              - alertmanager:9093
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-rules
data:
  exam-framework.yml: |
    groups:
    - name: exam-framework
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
        expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) > 1
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
```

### Grafana Dashboard

```json
{
  "dashboard": {
    "title": "Exam Framework Dashboard",
    "panels": [
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_requests_total[5m])",
            "legendFormat": "{{method}} {{uri}}"
          }
        ]
      },
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))",
            "legendFormat": "95th percentile"
          },
          {
            "expr": "histogram_quantile(0.50, rate(http_request_duration_seconds_bucket[5m]))",
            "legendFormat": "50th percentile"
          }
        ]
      },
      {
        "title": "Database Connections",
        "type": "graph",
        "targets": [
          {
            "expr": "hikaricp_connections_active",
            "legendFormat": "Active"
          },
          {
            "expr": "hikaricp_connections_idle",
            "legendFormat": "Idle"
          }
        ]
      },
      {
        "title": "OpenAI API Calls",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(openai_api_calls_total[5m])",
            "legendFormat": "API calls/sec"
          }
        ]
      }
    ]
  }
}
```

## Disaster Recovery

### Backup Strategy

```bash
#!/bin/bash
# backup.sh - Database backup script

BACKUP_DIR="/backups/exam-framework"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="exam_framework_prod"

# Create backup directory
mkdir -p $BACKUP_DIR

# Database backup
pg_dump -h localhost -U exam_app -d $DB_NAME | gzip > $BACKUP_DIR/db_backup_$DATE.sql.gz

# File backup
tar -czf $BACKUP_DIR/files_backup_$DATE.tar.gz /var/log/exam-framework /app/reports

# Upload to cloud storage (AWS S3)
aws s3 cp $BACKUP_DIR/db_backup_$DATE.sql.gz s3://exam-framework-backups/database/
aws s3 cp $BACKUP_DIR/files_backup_$DATE.tar.gz s3://exam-framework-backups/files/

# Clean old backups (keep 30 days)
find $BACKUP_DIR -name "*.gz" -mtime +30 -delete

echo "Backup completed: $DATE"
```

### Recovery Procedures

```bash
#!/bin/bash
# restore.sh - Database restore script

BACKUP_FILE=$1
DB_NAME="exam_framework_prod"

if [ -z "$BACKUP_FILE" ]; then
    echo "Usage: $0 <backup_file>"
    exit 1
fi

# Download from S3 if needed
if [[ $BACKUP_FILE == s3://* ]]; then
    aws s3 cp $BACKUP_FILE /tmp/restore.sql.gz
    BACKUP_FILE="/tmp/restore.sql.gz"
fi

# Stop application
systemctl stop exam-framework

# Restore database
gunzip -c $BACKUP_FILE | psql -h localhost -U exam_app -d $DB_NAME

# Start application
systemctl start exam-framework

echo "Restore completed"
```

### High Availability Setup

```yaml
# Multi-region deployment
apiVersion: v1
kind: ConfigMap
metadata:
  name: geo-replication-config
data:
  replication.yml: |
    primary:
      region: us-west-2
      endpoint: primary.exam-framework.com
    replicas:
      - region: us-east-1
        endpoint: replica1.exam-framework.com
      - region: eu-west-1
        endpoint: replica2.exam-framework.com
    
    failover:
      automatic: true
      health_check_interval: 30
      failover_timeout: 60
```

## CI/CD Pipeline

### GitHub Actions Workflow

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Run tests
      run: ./mvnw test
    
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests
        path: target/surefire-reports/*.xml
        reporter: java-junit

  build:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Log in to Container Registry
      uses: docker/login-action@v2
      with:
        registry: ${{ env.REGISTRY }}
        username: ${{ github.actor }}
        password: ${{ secrets.GITHUB_TOKEN }}
    
    - name: Extract metadata
      id: meta
      uses: docker/metadata-action@v4
      with:
        images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
    
    - name: Build and push Docker image
      uses: docker/build-push-action@v4
      with:
        context: .
        push: true
        tags: ${{ steps.meta.outputs.tags }}
        labels: ${{ steps.meta.outputs.labels }}

  deploy-staging:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    environment: staging
    steps:
    - uses: actions/checkout@v3
    
    - name: Deploy to staging
      run: |
        echo "Deploying to staging environment"
        # Add deployment commands here
    
    - name: Run integration tests
      run: |
        echo "Running integration tests"
        # Add integration test commands here

  deploy-production:
    needs: deploy-staging
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    environment: production
    steps:
    - uses: actions/checkout@v3
    
    - name: Deploy to production
      run: |
        echo "Deploying to production environment"
        # Add production deployment commands here
    
    - name: Health check
      run: |
        echo "Performing health check"
        curl -f https://exam-framework.yourdomain.com/actuator/health
```

This comprehensive deployment and scaling guide provides everything needed to deploy the Aentic Exam Framework in production environments with proper scaling, monitoring, and disaster recovery capabilities.
