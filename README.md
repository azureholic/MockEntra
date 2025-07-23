# Demo API with Generic OAuth2/OIDC Integration

This is a simple Spring Boot API application that validates JWT tokens from any OAuth2/OpenID Connect provider including Keycloak, Entra ID (Azure AD), Auth0, and others.

## Overview

The API provides a single protected endpoint:
- `GET /api/validate` - Returns "Valid Token" if the JWT is valid, otherwise returns 401 Unauthorized

## Configuration

All OAuth settings are injected through configuration properties and can be switched between providers by changing configuration files.

### Generic OAuth2 Configuration
```properties
# OAuth2 Resource Server (Standard Spring Security)
spring.security.oauth2.resourceserver.jwt.issuer-uri=${ISSUER_URI}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${JWK_SET_URI}

# Provider-agnostic OAuth2 settings
oauth2.issuer-uri=${ISSUER_URI}
oauth2.jwk-set-uri=${JWK_SET_URI}
oauth2.scope-claim=scope
```

### Supported OAuth2 Providers

#### 1. Keycloak
Use profile: `keycloak`
```bash
java -jar demo-api.jar --spring.profiles.active=keycloak
```

Configuration in `application-keycloak.properties`:
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/local-development
```

#### 2. Entra ID (Azure AD)
Use profile: `entraid`
```bash
java -jar demo-api.jar --spring.profiles.active=entraid
```

Configuration in `application-entraid.properties`:
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://login.microsoftonline.com/{tenant-id}/v2.0
```

## API Endpoint

### Protected Endpoint
- `GET /api/validate` - Validates JWT token and returns "Valid Token"
  - **Requires**: Valid JWT token in Authorization header
  - **Returns**: `200 OK` with "Valid Token" text
  - **On invalid/missing token**: `401 Unauthorized`

### Health Check (Public)
- `GET /actuator/health` - Health check (no authentication required)
- `GET /actuator/info` - Application info (no authentication required)

## Running the Application

### With Keycloak (Default)
1. Start Keycloak using Docker Compose:
   ```bash
   docker-compose up -d
   ```

2. Start the API with Keycloak profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=keycloak
   ```

### With Entra ID
1. Configure your Entra ID application registration
2. Update `application-entraid.properties` with your tenant ID
3. Start the API with Entra ID profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=entraid
   ```

## Testing the API

### 1. Get Access Token

#### Keycloak
```bash
curl -X POST http://localhost:8080/realms/local-development/protocol/openid_connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=demo-client" \
  -d "client_secret=demo-client-secret" \
  -d "username=demo-user" \
  -d "password=demo-password" \
  -d "scope=api-access"
```

#### Entra ID
```bash
curl -X POST https://login.microsoftonline.com/{tenant-id}/oauth2/v2.0/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id={client-id}" \
  -d "client_secret={client-secret}" \
  -d "scope=https://graph.microsoft.com/.default"
```

### 2. Test the Validation Endpoint

#### With Valid Token
```bash
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
     http://localhost:8081/api/validate
```
**Response**: `Valid Token`

#### Without Token
```bash
curl http://localhost:8081/api/validate
```
**Response**: `401 Unauthorized`

#### With Invalid Token
```bash
curl -H "Authorization: Bearer invalid_token" \
     http://localhost:8081/api/validate
```
**Response**: `401 Unauthorized`

### 3. Test Health Check (No Token Required)
```bash
curl http://localhost:8081/actuator/health
```

## Architecture

### Provider-Agnostic Design
- **Standard Spring Security OAuth2**: Uses only standard OAuth2 resource server features
- **JWT Validation**: Automatically validates tokens using JWK sets from the provider
- **Configurable**: Switch providers by changing configuration only

### Security Configuration
- JWT-based authentication using any OAuth2/OIDC provider
- Single protected endpoint requiring valid JWT
- CORS configuration for cross-origin requests
- Stateless session management

## Switching Between Providers

To switch from Keycloak to Entra ID (or any other provider):

1. **Change Profile**: Update `spring.profiles.active` to the desired provider
2. **No Code Changes Required**: The application automatically adapts
3. **Update Configuration**: Modify issuer URI and JWK set URI in the provider-specific properties file

### Example: Keycloak to Entra ID Migration
```bash
# From
mvn spring-boot:run -Dspring-boot.run.profiles=keycloak

# To
mvn spring-boot:run -Dspring-boot.run.profiles=entraid
```

## Adding New OAuth2 Providers

1. Create `application-{provider}.properties`
2. Set the issuer URI and JWK set URI for your provider
3. Start with the new profile: `--spring.profiles.active={provider}`

Example for Auth0:
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://{domain}/
oauth2.issuer-uri=https://{domain}/
```

## Troubleshooting

### Common Issues
1. **401 Unauthorized**: 
   - Check if token is valid and not expired
   - Verify issuer URI matches the token issuer
   - Ensure JWK set URI is accessible

2. **Token Validation Errors**:
   - Verify the provider's JWK endpoint is reachable
   - Check that the token audience matches expected values
   - Ensure clock synchronization between services

### Debug Logging
Enable debug logging:
```properties
logging.level.org.springframework.security=DEBUG
logging.level.com.example=DEBUG
```

## Monitoring

- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
