/*
 * Copyright 2023 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.smartcommunitylab.aac.core.provider;

import it.smartcommunitylab.aac.core.model.ConfigMap;
import it.smartcommunitylab.aac.core.model.ConfigurableProvider;
import it.smartcommunitylab.aac.core.model.Resource;
import java.util.Locale;

/*
 * Configurable ResourceProviders are providers backed by a persisted configuration,
 * in form of a ConfigurableProvider carrying a specific ConfigMap.
 * At runtime their config is expressed via a ProviderConfig with the same ConfigMap.
 */
public interface ConfigurableResourceProvider<
    R extends Resource, T extends ConfigurableProvider, M extends ConfigMap, C extends ProviderConfig<M>
>
    extends ResourceProvider<R> {
    public String getName();

    public String getTitle(Locale locale);

    public String getDescription(Locale locale);

    /*
     * Config
     */

    public C getConfig();
}
