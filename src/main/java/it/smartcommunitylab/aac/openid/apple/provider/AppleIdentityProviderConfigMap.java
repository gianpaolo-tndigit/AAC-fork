package it.smartcommunitylab.aac.openid.apple.provider;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.core.model.ConfigurableProperties;
import it.smartcommunitylab.aac.openid.provider.OIDCIdentityProviderConfigMap;

@Valid
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppleIdentityProviderConfigMap implements ConfigurableProperties, Serializable {
    private static final long serialVersionUID = SystemKeys.AAC_APPLE_SERIAL_VERSION;

    private static ObjectMapper mapper = new ObjectMapper();
    private final static TypeReference<HashMap<String, Serializable>> typeRef = new TypeReference<HashMap<String, Serializable>>() {
    };

    private String clientId;
    private String teamId;

    private String keyId;
    private String privateKey;

    private Boolean askNameScope;
    private Boolean askEmailScope;

    public AppleIdentityProviderConfigMap() {

    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public Boolean getAskNameScope() {
        return askNameScope;
    }

    public void setAskNameScope(Boolean askNameScope) {
        this.askNameScope = askNameScope;
    }

    public Boolean getAskEmailScope() {
        return askEmailScope;
    }

    public void setAskEmailScope(Boolean askEmailScope) {
        this.askEmailScope = askEmailScope;
    }

    @Override
    @JsonIgnore
    public Map<String, Serializable> getConfiguration() {
        // use mapper
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        return mapper.convertValue(this, typeRef);
    }

    @Override
    @JsonIgnore
    public void setConfiguration(Map<String, Serializable> props) {
        // use mapper
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        AppleIdentityProviderConfigMap map = mapper.convertValue(props, AppleIdentityProviderConfigMap.class);

        this.clientId = map.getClientId();
        this.teamId = map.getTeamId();
        this.privateKey = map.getPrivateKey();
        this.keyId = map.getKeyId();

        this.askEmailScope = map.getAskEmailScope();
        this.askNameScope = map.getAskNameScope();

    }

    @JsonIgnore
    public static JsonSchema getConfigurationSchema() throws JsonMappingException {
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
        return schemaGen.generateSchema(OIDCIdentityProviderConfigMap.class);
    }
}
