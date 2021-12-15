package it.smartcommunitylab.aac.core.provider;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

import it.smartcommunitylab.aac.core.base.AbstractProviderConfig;
import it.smartcommunitylab.aac.core.model.ConfigurableProperties;
import it.smartcommunitylab.aac.core.model.ConfigurableProvider;

/*
 * Expose provider configuration outside modules
 */
public interface ConfigurationProvider<T extends ConfigurableProvider, C extends AbstractProviderConfig, P extends ConfigurableProperties> {

    public String getAuthority();

    public String getType();

    /*
     * Translate a configurableProvider with valid props to a valid provider config,
     * with default values set
     */
    public C getConfig(T cp);

    public C getConfig(T cp, boolean mergeDefault);

    /*
     * Expose and translate to valid configMap
     */

    public P getDefaultConfigMap();

    public P getConfigMap(Map<String, Serializable> map);

    /*
     * Export the configuration schema for configMap
     */

    public JsonSchema getSchema();

}