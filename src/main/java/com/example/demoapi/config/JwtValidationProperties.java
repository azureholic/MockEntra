package com.example.demoapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt.validation")
public class JwtValidationProperties {
    
    private String validTokenIssuer;
    private String validAudience;
    private String jwkSetUri;
    
    public String getValidTokenIssuer() {
        return validTokenIssuer;
    }
    
    public void setValidTokenIssuer(String validTokenIssuer) {
        this.validTokenIssuer = validTokenIssuer;
    }
    
    public String getValidAudience() {
        return validAudience;
    }
    
    public void setValidAudience(String validAudience) {
        this.validAudience = validAudience;
    }
    
    public String getJwkSetUri() {
        return jwkSetUri;
    }
    
    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
    }
}
