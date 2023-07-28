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

package it.smartcommunitylab.aac.saml.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.base.model.AbstractUserAccount;
import it.smartcommunitylab.aac.model.SubjectStatus;
import it.smartcommunitylab.aac.repository.HashMapSerializableConverter;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

@Entity
@IdClass(SamlUserAccountId.class)
@Table(name = "saml_users")
@EntityListeners(AuditingEntityListener.class)
public class SamlUserAccount extends AbstractUserAccount {

    private static final long serialVersionUID = SystemKeys.AAC_SAML_SERIAL_VERSION;
    public static final String RESOURCE_TYPE =
        SystemKeys.RESOURCE_ACCOUNT + SystemKeys.ID_SEPARATOR + SystemKeys.AUTHORITY_SAML;

    @Id
    @NotBlank
    @Column(name = "repository_id", length = 128)
    private String repositoryId;

    // subject identifier from external provider
    @Id
    @NotBlank
    @Column(name = "subject", length = 128)
    private String subjectId;

    // unique uuid (subject entity)
    @NotBlank
    @Column(unique = true, length = 128)
    private String uuid;

    // reference to user
    @NotNull
    @Column(name = "user_id", length = 128)
    private String userId;

    @NotBlank
    @Column(length = 128)
    private String realm;

    // login
    @Column(length = 32)
    private String status;

    // attributes
    @Column(name = "username", length = 128)
    private String username;

    @Column(name = "issuer")
    private String issuer;

    private String email;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    private String name;
    private String surname;

    @Column(length = 32)
    private String lang;

    // audit
    @CreatedDate
    @Column(name = "created_date")
    private Date createDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private Date modifiedDate;

    @Lob
    @Column(name = "attributes")
    @JsonIgnore
    @Convert(converter = HashMapSerializableConverter.class)
    private Map<String, Serializable> attributes;

    public SamlUserAccount() {
        super(SystemKeys.AUTHORITY_SAML, null);
    }

    public SamlUserAccount(String authority) {
        super(authority, null);
    }

    @Override
    public String getType() {
        return RESOURCE_TYPE;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public String getAccountId() {
        return subjectId;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getEmailAddress() {
        return email;
    }

    @Override
    public boolean isEmailVerified() {
        return (StringUtils.hasText(email) && emailVerified != null) ? emailVerified.booleanValue() : false;
    }

    @Override
    public boolean isLocked() {
        // only active users are *not* locked
        if (status == null || SubjectStatus.ACTIVE.getValue().equals(status)) {
            return false;
        }

        // every other condition locks login
        return true;
    }

    /*
     * fields
     */

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, Serializable> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Serializable> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return (
            "SamlUserAccount [repositoryId=" +
            repositoryId +
            ", subjectId=" +
            subjectId +
            ", uuid=" +
            uuid +
            ", userId=" +
            userId +
            ", realm=" +
            realm +
            ", status=" +
            status +
            ", username=" +
            username +
            ", issuer=" +
            issuer +
            ", email=" +
            email +
            ", emailVerified=" +
            emailVerified +
            ", name=" +
            name +
            ", surname=" +
            surname +
            ", lang=" +
            lang +
            ", createDate=" +
            createDate +
            ", modifiedDate=" +
            modifiedDate +
            "]"
        );
    }
}
