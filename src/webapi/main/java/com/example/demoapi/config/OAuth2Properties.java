package com.example.demoapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Properties {
    
    private String issuerUri;
    private String jwkSetUri;
    private String audienceClaim = "aud";
    private String rolesClaim = "roles";
    private String scopeClaim = "scope";
    private String resourceAccessClaim = "resource_access";
    private String realmAccessClaim = "realm_access";
    private boolean cors;
    private int corsMaxAge;
    private String corsAllowedMethods;
    private String corsAllowedHeaders;
    
    // Getters and Setters
    public String getIssuerUri() {
        return issuerUri;
    }
    
    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }
    
    public String getJwkSetUri() {
        return jwkSetUri;
    }
    
    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
    }
    
    public String getAudienceClaim() {
        return audienceClaim;
    }
    
    public void setAudienceClaim(String audienceClaim) {
        this.audienceClaim = audienceClaim;
    }
    
    public String getRolesClaim() {
        return rolesClaim;
    }
    
    public void setRolesClaim(String rolesClaim) {
        this.rolesClaim = rolesClaim;
    }
    
    public String getScopeClaim() {
        return scopeClaim;
    }
    
    public void setScopeClaim(String scopeClaim) {
        this.scopeClaim = scopeClaim;
    }
    
    public String getResourceAccessClaim() {
        return resourceAccessClaim;
    }
    
    public void setResourceAccessClaim(String resourceAccessClaim) {
        this.resourceAccessClaim = resourceAccessClaim;
    }
    
    public String getRealmAccessClaim() {
        return realmAccessClaim;
    }
    
    public void setRealmAccessClaim(String realmAccessClaim) {
        this.realmAccessClaim = realmAccessClaim;
    }
    
    public boolean isCors() {
        return cors;
    }
    
    public void setCors(boolean cors) {
        this.cors = cors;
    }
    
    public int getCorsMaxAge() {
        return corsMaxAge;
    }
    
    public void setCorsMaxAge(int corsMaxAge) {
        this.corsMaxAge = corsMaxAge;
    }
    
    public String getCorsAllowedMethods() {
        return corsAllowedMethods;
    }
    
    public void setCorsAllowedMethods(String corsAllowedMethods) {
        this.corsAllowedMethods = corsAllowedMethods;
    }
    
    public String getCorsAllowedHeaders() {
        return corsAllowedHeaders;
    }
    
    public void setCorsAllowedHeaders(String corsAllowedHeaders) {
        this.corsAllowedHeaders = corsAllowedHeaders;
    }
}
