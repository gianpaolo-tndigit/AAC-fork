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

package it.smartcommunitylab.aac.openid.apple.model;

public class AppleOidcUserData {

    private String email;
    private AppleOidcUserDataName name;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AppleOidcUserDataName getName() {
        return name;
    }

    public void setName(AppleOidcUserDataName name) {
        this.name = name;
    }

    public String getFirstName() {
        if (name != null) {
            return name.firstName;
        }

        return null;
    }

    public String getLastName() {
        if (name != null) {
            return name.lastName;
        }

        return null;
    }
}

class AppleOidcUserDataName {

    public String firstName;
    public String lastName;
}
