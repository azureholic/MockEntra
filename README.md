# MockEntra - OAuth2/JWT Demo Application

## Objective

MockEntra is a demonstration application that showcases how to build OAuth2/JWT-based authentication systems that can work with multiple identity providers. The project demonstrates:

- **Provider-agnostic JWT validation** - A Spring Boot API that can validate JWTs from different OAuth2 providers
- **OAuth2 Client Credentials flow** - A web client that obtains access tokens and calls protected APIs
- **Seamless provider switching** - Easy configuration changes to switch between Keycloak and Azure Entra ID (formerly Azure AD)

The application consists of three main components:
1. **Demo API** (`demo-api`) - A protected REST API that validates JWT tokens and extracts claims
2. **Web Client** (`webclient`) - A Spring Boot application that uses OAuth2 Client Credentials flow to obtain tokens and call the API
3. **Keycloak** (optional) - A local identity provider for development and testing

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Web Client    │    │    Demo API      │    │  Identity       │
│   (Port 8082)   │    │   (Port 8081)    │    │  Provider       │
│                 │    │                  │    │  (Keycloak/     │
│ ┌─────────────┐ │    │ ┌──────────────┐ │    │   Entra ID)     │
│ │OAuth2 Client│ │    │ │JWT Validator │ │    │                 │
│ │Credentials  │ │────┼▶│              │ │    │                 │
│ │Flow         │ │    │ │              │ │    │                 │
│ └─────────────┘ │    │ └──────────────┘ │    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                        │                       │
         └────────────────────────┼───────────────────────┘
                                  │
                         ┌────────▼────────┐
                         │   JWT Token     │
                         │   Exchange      │
                         └─────────────────┘
```

## How It Works

### 1. Token Acquisition
The web client uses OAuth2 Client Credentials flow to obtain an access token from the configured identity provider.

### 2. API Call
The web client includes the access token in the Authorization header when calling the Demo API.

### 3. JWT Validation
The Demo API validates the JWT token by:
- Verifying the signature using the provider's public keys (JWK Set)
- Checking the issuer and audience claims
- Extracting and displaying token information (issuer, audience, scope, roles)

### 4. Response
The API returns detailed information about the validated token, including extracted claims and roles.

## Quick Start

### Using Keycloak (Default)

1. **Start the services**:
   ```bash
   docker-compose up --build
   ```

2. **Wait for services to be healthy** (approximately 1-2 minutes)

3. **Test the application**:
   - Open your browser to http://localhost:8082
   - Click "Call Protected API" to see the token validation in action

### Using Azure Entra ID

1. **Create your `.env` file** (see Environment Configuration section below)

2. **Start only the application services** (skip Keycloak):
   ```bash
   docker-compose up --build demo-api webclient
   ```

3. **Test the application**:
   - Open your browser to http://localhost:8082
   - Click "Call Protected API"

## Environment Configuration

The application uses environment variables to configure OAuth2 providers. Create a `.env` file in the project root with the appropriate configuration:

### Keycloak Configuration (Default)

```properties
# Generic OAuth2 Configuration for Keycloak (Client Credentials Flow)
TOKEN_ENDPOINT=http://keycloak:8080/realms/local-development/protocol/openid-connect/token
CLIENT_ID=demo-client
CLIENT_SECRET=demo-client-secret
SCOPE=api-access

# Generic JWT Validation Configuration for Keycloak
VALID_TOKEN_ISSUER=http://localhost:8080/realms/local-development
VALID_AUDIENCE=demo-api
JWK_SET_URI=http://keycloak:8080/realms/local-development/protocol/openid-connect/certs
```

### Azure Entra ID Configuration

```properties
# Generic OAuth2 Configuration for Azure Entra ID (Client Credentials Flow)
TOKEN_ENDPOINT=https://login.microsoftonline.com/{YOUR_TENANT_ID}/oauth2/v2.0/token
CLIENT_ID={YOUR_CLIENT_ID}
CLIENT_SECRET={YOUR_CLIENT_SECRET}
SCOPE=api://{YOUR_API_CLIENT_ID}/.default

# Generic JWT Validation Configuration for Azure Entra ID
VALID_TOKEN_ISSUER=https://login.microsoftonline.com/{YOUR_TENANT_ID}/v2.0
VALID_AUDIENCE={YOUR_API_CLIENT_ID}
JWK_SET_URI=https://login.microsoftonline.com/{YOUR_TENANT_ID}/discovery/v2.0/keys
```

**Placeholder Replacements for Azure Entra ID:**
- `{YOUR_TENANT_ID}` - Your Azure AD tenant ID (GUID format)
- `{YOUR_CLIENT_ID}` - The Application (client) ID of your client application
- `{YOUR_CLIENT_SECRET}` - The client secret for your client application
- `{YOUR_API_CLIENT_ID}` - The Application (client) ID of your API application

## Switching from Keycloak to Azure Entra ID

### Step 1: Azure Entra ID Setup

1. **Register the API application**:
   - Go to Azure Portal → Azure Active Directory → App registrations
   - Create a new registration for your API
   - Note the Application (client) ID - this becomes `{YOUR_API_CLIENT_ID}`
   - Under "Expose an API", add a scope (e.g., `api.access`)

2. **Register the client application**:
   - Create another app registration for the client
   - Note the Application (client) ID - this becomes `{YOUR_CLIENT_ID}`
   - Under "Certificates & secrets", create a new client secret - this becomes `{YOUR_CLIENT_SECRET}`
   - Under "API permissions", add permission to your API application

3. **Get your tenant information**:
   - In Azure AD overview, note your Tenant ID - this becomes `{YOUR_TENANT_ID}`

### Step 2: Update Configuration

1. **Create/update your `.env` file** with Azure Entra ID configuration (see example above)

2. **Restart the application services**:
   ```bash
   # Stop any running services
   docker-compose down
   
   # Start only the app services (no Keycloak needed)
   docker-compose up --build demo-api webclient
   ```

### Step 3: Test the Integration

1. Open http://localhost:8082
2. Click "Call Protected API"
3. Verify that the token information shows Azure AD as the issuer

## Development

### Building the Application

```bash
# Build the API module
mvn clean package -DskipTests

# Or use the VS Code task
# Ctrl+Shift+P → "Tasks: Run Task" → "Maven Build webapi"
```

### Running Individual Components

```bash
# Run the API locally (requires environment variables)
cd src/webapi
mvn spring-boot:run

# Run the web client locally (requires environment variables)
cd src/webclient
mvn spring-boot:run
```

### Project Structure

```
├── docker-compose.yml              # Container orchestration
├── Dockerfile                      # Multi-stage build for API
├── pom.xml                         # Parent Maven configuration
├── .env                           # Environment variables (create this)
├── realm-config/
│   └── local-development.json     # Keycloak realm configuration
├── src/
│   ├── webapi/                    # Protected REST API
│   │   ├── pom.xml
│   │   └── main/java/com/example/demoapi/
│   │       ├── DemoApiApplication.java
│   │       ├── config/            # Security and JWT configuration
│   │       └── controller/        # API endpoints
│   └── webclient/                 # OAuth2 client application
│       ├── pom.xml
│       ├── Dockerfile
│       └── src/main/java/com/example/webclient/
│           ├── WebClientApplication.java
│           ├── config/            # OAuth2 client configuration
│           ├── controller/        # Web controllers
│           └── service/           # API and token services
```

## Key Features

### Provider-Agnostic Design
- Uses generic JWT validation that works with multiple OAuth2 providers
- Configurable issuer, audience, and JWK Set URI
- Supports different token claim structures (Keycloak vs Azure AD)

### Security Best Practices
- JWT signature validation using provider's public keys
- Proper audience and issuer validation
- Support for various role claim formats
- CORS configuration for web clients

### Development-Friendly
- Docker Compose for easy local development
- Detailed logging for debugging
- Health checks and monitoring endpoints
- Hot reload support for development

## Troubleshooting

### Common Issues

1. **Services not starting**: Wait for Keycloak to be fully ready (health check passes)
2. **Token validation fails**: Verify issuer URLs match between client and API configuration
3. **Connection refused**: Ensure all services are running and healthy
4. **Azure AD integration issues**: Double-check tenant ID, client IDs, and secret values

### Logs

View service logs:
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f demo-api
docker-compose logs -f webclient
docker-compose logs -f keycloak
```

## License

This project is provided as-is for demonstration and educational purposes.
