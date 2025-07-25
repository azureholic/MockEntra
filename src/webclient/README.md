# Demo API Web Client

This is a Spring Boot web application that demonstrates calling the Demo API with and without OAuth2 tokens. The web client can acquire tokens from either Keycloak or Entra ID and test the API validation endpoint.

## Features

- **Two-button interface**: 
  - Call API without token (should return 401 Unauthorized)
  - Call API with token (acquires token first, then calls API)
- **Provider-agnostic**: Works with Keycloak, Entra ID, or other OAuth2 providers
- **Server-side token acquisition**: All token requests are made server-side
- **Real-time feedback**: Shows token acquisition and API call results

## Running the Web Client

### Prerequisites
1. Demo API must be running on `http://localhost:8081`
2. OAuth2 provider (Keycloak or Entra ID) must be configured and running

### With Keycloak
1. Start Keycloak and the Demo API first
2. Start the web client with Keycloak profile:
   ```bash
   cd webclient
   mvn spring-boot:run -Dspring-boot.run.profiles=keycloak
   ```

### With Entra ID
1. Configure your Entra ID application registration
2. Update `application-entraid.properties` with your tenant ID and client credentials
3. Start the web client with Entra ID profile:
   ```bash
   cd webclient
   mvn spring-boot:run -Dspring-boot.run.profiles=entraid
   ```

### Access the Web Interface
Open your browser and navigate to: `http://localhost:8082`

## Web Interface

The web interface provides:

### Buttons
1. **🚫 Call API with No Token** (Red button)
   - Calls the API without any authentication
   - Should return "401 Unauthorized - No token provided"

2. **🔑 Call API with Token** (Green button)
   - First acquires a token from the configured OAuth2 provider
   - Then calls the API with the acquired token
   - Should return "Valid Token" if successful

### Results Display
- **Token Information**: Shows the first 50 characters of the acquired token
- **API Response**: Shows the actual response from the Demo API
- **Status Indicators**: ✅ SUCCESS, ❌ UNAUTHORIZED, ❌ ERROR

## Configuration

### Keycloak Configuration (application-keycloak.properties)
```properties
oauth2.provider=keycloak
oauth2.keycloak.token-endpoint=http://localhost:8080/realms/local-development/protocol/openid_connect/token
oauth2.keycloak.client-id=demo-client
oauth2.keycloak.client-secret=demo-client-secret
oauth2.keycloak.username=demo-user
oauth2.keycloak.password=demo-password
oauth2.keycloak.scope=api-access profile email
```

### Entra ID Configuration (application-entraid.properties)
```properties
oauth2.provider=entraid
oauth2.entraid.token-endpoint=https://login.microsoftonline.com/{tenant-id}/oauth2/v2.0/token
oauth2.entraid.client-id={client-id}
oauth2.entraid.client-secret={client-secret}
oauth2.entraid.scope=https://graph.microsoft.com/.default
```

## Architecture

### Components
- **WebController**: Handles web requests and form submissions
- **TokenService**: Acquires OAuth2 tokens from different providers
- **ApiService**: Makes HTTP calls to the Demo API
- **Configuration Classes**: OAuth2ClientProperties, ApiProperties

### Flow
1. User clicks "Call API with Token"
2. TokenService acquires token from OAuth2 provider (Keycloak/Entra ID)
3. ApiService calls Demo API with the acquired token
4. Results are displayed in the web interface

### Error Handling
- Token acquisition failures are caught and displayed
- API call errors (401, 403, etc.) are handled gracefully
- Network errors are caught and shown to the user

## Testing Scenarios

### Expected Results

#### Call API with No Token
- **Result**: "401 Unauthorized - No token provided"
- **Status**: ❌ UNAUTHORIZED

#### Call API with Valid Token
- **Result**: "Valid Token"
- **Status**: ✅ SUCCESS

#### Call API with Invalid Token
- **Result**: "401 Unauthorized - Invalid or expired token"
- **Status**: ❌ UNAUTHORIZED

### Troubleshooting

#### Token Acquisition Fails
- Check that OAuth2 provider is running and accessible
- Verify client credentials in configuration
- Check network connectivity to token endpoint

#### API Call Fails
- Ensure Demo API is running on http://localhost:8081
- Verify the API endpoint is accessible
- Check API logs for authentication issues

#### Web Client Won't Start
- Check that port 8082 is available
- Verify Spring Boot dependencies are correctly configured
- Check application logs for startup errors

## Development

### Adding New OAuth2 Providers
1. Add provider configuration to `OAuth2ClientProperties`
2. Implement token acquisition logic in `TokenService`
3. Create new application-{provider}.properties file
4. Update documentation

### Customizing the UI
- Modify `templates/index.html` for layout changes
- Update CSS styles within the HTML file
- Add new buttons or forms as needed

## Security Notes

- Client secrets are stored in configuration files (use environment variables in production)
- Tokens are handled server-side and not exposed to the browser
- All HTTP calls use WebClient with proper error handling
- CORS is not needed since all calls are server-side
