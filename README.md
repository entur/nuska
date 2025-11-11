# Nuska

API to access raw NeTEx data from the Entur platform.

## Overview

Nuska is a Spring Boot REST API service that provides authenticated access to original NeTEx (Network Timetable Exchange) datasets uploaded by data providers. The service acts as a secure gateway to timetable data stored in blob storage, with OAuth2 authentication and role-based authorization.

## Features

- **Dataset Access**: Download NeTEx datasets by codespace and import key
- **Version Management**: List and retrieve specific versions of imported datasets
- **Latest Dataset**: Quick access to the most recent dataset for a codespace
- **OAuth2 Security**: JWT-based authentication with multi-issuer support
- **Role-Based Access**: Authorization using Entur's permission store
- **Cloud Storage**: Integration with Google Cloud Storage (GCS) and local file system
- **Monitoring**: Prometheus metrics via Spring Actuator
- **OpenAPI Documentation**: Auto-generated API documentation

## Technology Stack

- **Java 21** - Latest LTS version
- **Spring Boot 3** - Application framework with Jakarta EE
- **Spring Security** - OAuth2 resource server
- **Maven** - Build automation
- **OpenAPI 3.1** - API specification
- **Google Cloud Storage** - Primary blob storage
- **Docker** - Containerization with Liberica JRE 21
- **Helm** - Kubernetes deployment

## Architecture

### Core Components

- **NuskaController** (`NuskaController.java:24`) - REST API endpoints implementing the TimetableData API
- **NisabaBlobStoreService** (`NisabaBlobStoreService.java:36`) - Service layer for blob storage operations
- **NuskaBlobStoreRepository** - Repository abstractions for GCS and local storage
- **NuskaAuthorizationService** (`DefaultNuskaAuthorizationService.java:21`) - Authorization verification using Entur helpers
- **OAuth2Config** - Multi-issuer authentication configuration

### API Endpoints

1. `GET /timetable-data/datasets/{codespace}/latest` - Download latest dataset
2. `GET /timetable-data/datasets/{codespace}/version/{importKey}` - Download specific version
3. `GET /timetable-data/datasets/{codespace}/versions` - List available versions (max 10)
4. `GET /timetable-data/openapi.json` - OpenAPI specification

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Access to Google Cloud Storage (for production) or local file system (for development)
- OAuth2 credentials for authentication

## Building

```bash
# Build the project
mvn clean package

# Skip formatting checks
mvn clean package -Pprettier

# Run tests
mvn test
```

## Running Locally

```bash
# Using Maven
mvn spring-boot:run

# Using Java
java -jar target/nuska-0.0.1-SNAPSHOT.jar
```

## Configuration

Key configuration properties:

- `blobstore.gcs.nisaba.container.name` - GCS bucket name (default: `nisaba-exchange`)
- OAuth2 issuer configuration for authentication
- Spring Actuator endpoints for health and metrics

## Docker

```bash
# Build Docker image
docker build -t nuska:latest .

# Run container
docker run -p 8080:8080 nuska:latest
```

## Deployment

The service is deployed using Helm charts located in `helm/nuska/`:

```bash
# Deploy to Kubernetes
helm install nuska helm/nuska -f helm/nuska/env/values-<env>.yaml
```

## Security

- **Authentication**: OAuth2 JWT bearer tokens
- **Authorization**: Role-based access control via Entur permission store
- **Block Viewer Privileges**: Required to access datasets
- **Multi-Issuer Support**: Configurable JWT issuers for different environments

## Development

### Code Formatting

The project uses Prettier for Java code formatting:

```bash
# Format code
mvn prettier:write

# Check formatting
mvn prettier:check
```

### API Environments

- **Development**: `https://api.dev.entur.io/timetable/v1/timetable-data`
- **Staging**: `https://api.staging.entur.io/timetable/v1/timetable-data`
- **Production**: `https://api.entur.io/timetable/v1/timetable-data`

## Dependencies

Key dependencies:

- Entur RoR Helpers (storage-gcp-gcs, oauth2, permission-store-proxy)
- Spring Boot Starter Web & Actuator
- Spring Security OAuth2 Resource Server
- Micrometer Prometheus Registry
- Swagger Core Jakarta
- Logstash Logback Encoder

## License

Licensed under the EUPL (European Union Public License) Version 1.2.

