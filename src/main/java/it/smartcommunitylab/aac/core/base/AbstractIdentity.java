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

package it.smartcommunitylab.aac.core.base;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.smartcommunitylab.aac.core.model.UserIdentity;
import it.smartcommunitylab.aac.internal.model.InternalUserIdentity;
import it.smartcommunitylab.aac.openid.model.OIDCUserIdentity;
import it.smartcommunitylab.aac.saml.model.SamlUserIdentity;

/*
 * Abstract identity
 *
 * all implementations should derive from this
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
    {
        @Type(value = InternalUserIdentity.class, name = InternalUserIdentity.RESOURCE_TYPE),
        @Type(value = OIDCUserIdentity.class, name = OIDCUserIdentity.RESOURCE_TYPE),
        @Type(value = SamlUserIdentity.class, name = SamlUserIdentity.RESOURCE_TYPE),
    }
)
public abstract class AbstractIdentity extends AbstractBaseUserResource implements UserIdentity {

    private String uuid;
    private String userId;
    private String realm;

    protected AbstractIdentity(String authority, String provider) {
        super(authority, provider);
    }

    @Override
    public String getId() {
        // use uuid from persisted model
        return getUuid();
    }

    @Override
    public String getResourceId() {
        return getIdentityId();
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }
}
