package it.smartcommunitylab.aac.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import it.smartcommunitylab.aac.SystemKeys;
import javax.validation.Valid;
import org.springframework.boot.context.properties.ConstructorBinding;

@Valid
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigurableCredentialsProvider extends ConfigurableProvider {

    private String repositoryId;

    public ConfigurableCredentialsProvider(String authority, String provider, String realm) {
        super(authority, provider, realm, SystemKeys.RESOURCE_CREDENTIALS);
    }

    /**
     * Private constructor for JPA and other serialization tools.
     *
     * We need to implement this to enable deserialization of resources via
     * reflection
     */
    @SuppressWarnings("unused")
    private ConfigurableCredentialsProvider() {
        this((String) null, (String) null, (String) null);
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }
}
