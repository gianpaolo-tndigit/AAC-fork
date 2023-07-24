package it.smartcommunitylab.aac.webauthn.provider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserVerificationRequirement;
import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.core.base.AbstractConfigMap;
import java.io.Serializable;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

@Valid
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebAuthnCredentialsServiceConfigMap extends AbstractConfigMap implements Serializable {

    private static final long serialVersionUID = SystemKeys.AAC_WEBAUTHN_SERIAL_VERSION;

    @Pattern(regexp = SystemKeys.SLUG_PATTERN)
    private String repositoryId;

    private Boolean allowUntrustedAttestation;
    private UserVerificationRequirement requireUserVerification;
    private ResidentKeyRequirement requireResidentKey;

    @Min(30)
    private Integer registrationTimeout;

    private Boolean requireAccountConfirmation;

    public WebAuthnCredentialsServiceConfigMap() {}

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public Boolean getAllowUntrustedAttestation() {
        return allowUntrustedAttestation;
    }

    public void setAllowUntrustedAttestation(Boolean allowUntrustedAttestation) {
        this.allowUntrustedAttestation = allowUntrustedAttestation;
    }

    protected UserVerificationRequirement getRequireUserVerification() {
        return requireUserVerification;
    }

    protected void setRequireUserVerification(UserVerificationRequirement requireUserVerification) {
        this.requireUserVerification = requireUserVerification;
    }

    protected ResidentKeyRequirement getRequireResidentKey() {
        return requireResidentKey;
    }

    protected void setRequireResidentKey(ResidentKeyRequirement requireResidentKey) {
        this.requireResidentKey = requireResidentKey;
    }

    public Integer getRegistrationTimeout() {
        return registrationTimeout;
    }

    public void setRegistrationTimeout(Integer registrationTimeout) {
        this.registrationTimeout = registrationTimeout;
    }

    public Boolean getRequireAccountConfirmation() {
        return requireAccountConfirmation;
    }

    public void setRequireAccountConfirmation(Boolean requireAccountConfirmation) {
        this.requireAccountConfirmation = requireAccountConfirmation;
    }

    @JsonIgnore
    public void setConfiguration(WebAuthnCredentialsServiceConfigMap map) {
        this.repositoryId = map.getRepositoryId();

        this.allowUntrustedAttestation = map.getAllowUntrustedAttestation();
        this.requireResidentKey = map.getRequireResidentKey();
        this.requireUserVerification = map.getRequireUserVerification();

        this.registrationTimeout = map.getRegistrationTimeout();
        this.requireAccountConfirmation = map.getRequireAccountConfirmation();
    }

    @Override
    @JsonIgnore
    public void setConfiguration(Map<String, Serializable> props) {
        // use mapper for local
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        WebAuthnCredentialsServiceConfigMap map = mapper.convertValue(props, WebAuthnCredentialsServiceConfigMap.class);

        setConfiguration(map);
    }

    @JsonIgnore
    public JsonSchema getSchema() throws JsonMappingException {
        return schemaGen.generateSchema(WebAuthnCredentialsServiceConfigMap.class);
    }
}
