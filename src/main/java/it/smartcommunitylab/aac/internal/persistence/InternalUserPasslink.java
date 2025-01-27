package it.smartcommunitylab.aac.internal.persistence;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.CredentialsContainer;

import it.smartcommunitylab.aac.SystemKeys;

@Entity
@Table(name = "internal_users_passlinks")
@EntityListeners(AuditingEntityListener.class)
public class InternalUserPasslink implements CredentialsContainer, Serializable {
    private static final long serialVersionUID = SystemKeys.AAC_CORE_SERIAL_VERSION;

    @Id
    @GeneratedValue
    private Long id;

    // account id
    @NotBlank
    @Column(name = "provider_id", length = 128)
    private String provider;

    @NotBlank
    @Column(name = "username", length = 128)
    private String username;

    @NotBlank
    private String passcode;

    private String status;

    @CreatedDate
    @Column(name = "created_date")
    private Date createDate;

    @Column(name = "expiration_date")
    private Date expirationDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public void eraseCredentials() {
        this.passcode = null;
    }

}
