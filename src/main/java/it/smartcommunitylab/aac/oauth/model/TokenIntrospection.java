/*******************************************************************************
 * Copyright 2015 Fondazione Bruno Kessler
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/

package it.smartcommunitylab.aac.oauth.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * IETF RFC7662 Token Introspection model
 * 
 * @author raman
 *
 */
@JsonInclude(Include.NON_NULL)
public class TokenIntrospection {

    private static final Set<String> RESERVED_CLAIM_NAMES;

    static {
        Set<String> n = new HashSet<>();

        n.add("active");
        n.add("jti");
        n.add("scope");
        n.add("client_id");
        n.add("username");
        n.add("token_type");
        n.add("exp");
        n.add("iat");
        n.add("nbf");
        n.add("sub");
        n.add("iss");
        n.add("aud");
        n.add("azp");

        RESERVED_CLAIM_NAMES = Collections.unmodifiableSet(n);
    }

    private final boolean active;
    private String jti;

    private String scope;

    @JsonProperty("client_id")
    private String clientId;

    private String username;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("exp")
    private Integer expirationTime;

    @JsonProperty("iat")
    private Integer issuedAt;

    @JsonProperty("nbf")
    private Integer notBeforeTime;

    @JsonProperty("sub")
    private String subject;

    @JsonProperty("iss")
    private String issuer;

    @JsonProperty("aud")
    private String[] audience;

    @JsonProperty("azp")
    private String authorizedParty;

    @JsonUnwrapped
    Map<String, Serializable> claims;

    public TokenIntrospection(boolean active) {
        this.active = active;
        this.claims = new HashMap<>();
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Integer getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Integer expirationTime) {
        this.expirationTime = expirationTime;
    }

    public Integer getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Integer issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Integer getNotBeforeTime() {
        return notBeforeTime;
    }

    public void setNotBeforeTime(Integer notBeforeTime) {
        this.notBeforeTime = notBeforeTime;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String[] getAudience() {
        return audience;
    }

    public void setAudience(String[] audience) {
        this.audience = audience;
    }

    public String getAuthorizedParty() {
        return authorizedParty;
    }

    public void setAuthorizedParty(String authorizedParty) {
        this.authorizedParty = authorizedParty;
    }

    public boolean isActive() {
        return active;
    }

    public Map<String, Serializable> getClaims() {
        return claims;
    }

    public void setClaims(Map<String, Serializable> claims) {
        this.claims = claims.entrySet().stream()
                .filter(c -> !RESERVED_CLAIM_NAMES.contains(c.getKey()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }

    public void addClaim(String key, Serializable value) {
        if (RESERVED_CLAIM_NAMES.contains(key)) {
            throw new IllegalArgumentException("can't set a reserved claim");
        }
        claims.put(key, value);
    }
}
