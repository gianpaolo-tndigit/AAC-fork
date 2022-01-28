package it.smartcommunitylab.aac.webauthn.model;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yubico.webauthn.AssertionRequest;

@Valid
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebAuthnLoginResponse {

    @JsonProperty("key")
    @NotNull
    private String key;

    @JsonProperty("assertionRequest")
    @JsonSerialize(using = AssertionRequestSerializer.class)
    @NotNull
    private AssertionRequest assertionrequest;

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public AssertionRequest getAssertionRequest() {
        return this.assertionrequest;
    }

    public void setAssertionRequest(AssertionRequest assertionrequest) {
        this.assertionrequest = assertionrequest;
    }

}