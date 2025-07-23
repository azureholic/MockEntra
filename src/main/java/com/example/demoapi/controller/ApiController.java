package com.example.demoapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> tokenInfo = new LinkedHashMap<>();
        
        // Extract Issuer
        String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "N/A";
        tokenInfo.put("Issuer", issuer);
        
        // Extract Audience
        List<String> audience = jwt.getAudience();
        tokenInfo.put("Audience", audience != null && !audience.isEmpty() ? audience : Arrays.asList("N/A"));
        
        // Extract Scope
        String scope = jwt.getClaimAsString("scope");
        if (scope == null) {
            // Try 'scp' claim for Azure AD tokens
            scope = jwt.getClaimAsString("scp");
        }
        tokenInfo.put("Scope", scope != null ? scope : "N/A");
        
        // Extract Roles
        List<String> roles = new ArrayList<>();
        
        // Try different role claim paths
        // 1. Direct 'roles' claim
        List<String> directRoles = jwt.getClaimAsStringList("roles");
        if (directRoles != null && !directRoles.isEmpty()) {
            roles.addAll(directRoles);
        }
        
        // 2. Keycloak realm_access.roles
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            Object realmRoles = realmAccess.get("roles");
            if (realmRoles instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> realmRolesList = (List<String>) realmRoles;
                roles.addAll(realmRolesList);
            }
        }
        
        // 3. Keycloak resource_access.[client].roles
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> clientAccess = (Map<String, Object>) entry.getValue();
                    if (clientAccess.containsKey("roles")) {
                        Object clientRoles = clientAccess.get("roles");
                        if (clientRoles instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> clientRolesList = (List<String>) clientRoles;
                            roles.addAll(clientRolesList);
                        }
                    }
                }
            }
        }
        
        // 4. Azure AD 'wids' (Well-known IDs) or 'roles' claim
        List<String> wids = jwt.getClaimAsStringList("wids");
        if (wids != null && !wids.isEmpty()) {
            roles.addAll(wids);
        }
        
        tokenInfo.put("Roles", roles.isEmpty() ? Arrays.asList("N/A") : roles);
        
        return ResponseEntity.ok(tokenInfo);
    }
}
