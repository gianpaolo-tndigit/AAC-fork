package it.smartcommunitylab.aac.internal.auth;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.Config;
import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.core.auth.DefaultUserAuthenticatedPrincipal;
import it.smartcommunitylab.aac.core.auth.ExtendedAuthenticationProvider;
import it.smartcommunitylab.aac.core.auth.UserAuthenticatedPrincipal;
import it.smartcommunitylab.aac.crypto.InternalPasswordEncoder;
import it.smartcommunitylab.aac.internal.persistence.InternalUserAccount;
import it.smartcommunitylab.aac.internal.provider.InternalAccountService;
import it.smartcommunitylab.aac.internal.service.InternalUserAccountService;
import it.smartcommunitylab.aac.internal.service.InternalUserDetailsService;
import it.smartcommunitylab.aac.oauth.auth.OAuth2ClientPKCEAuthenticationToken;

public class ConfirmKeyAuthenticationProvider implements AuthenticationProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String realm;
    private final String providerId;

    private final InternalAccountService accountService;

    public ConfirmKeyAuthenticationProvider(String providerId,
            InternalAccountService accountService,
            String realm) {
        Assert.hasText(providerId, "provider can not be null or empty");
        Assert.notNull(accountService, "account service is mandatory");

        this.realm = realm;
        this.providerId = providerId;

        this.accountService = accountService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(ConfirmKeyAuthenticationToken.class, authentication,
                "Only ResetKeyAuthenticationToken is supported");

        ConfirmKeyAuthenticationToken authRequest = (ConfirmKeyAuthenticationToken) authentication;

        String username = authRequest.getUsername();
        String key = authRequest.getKey();

        if (!StringUtils.hasText(username) || !StringUtils.hasText(key)) {
            throw new BadCredentialsException("missing required parameters in request");
        }

        try {
            InternalUserAccount account = accountService.findAccountByUsername(username);
            if (account == null) {
                throw new BadCredentialsException("invalid request");
            }

            // check this account is ours
            if (!account.getRealm().equals(this.realm) || !account.getProvider().equals(this.providerId)) {
                throw new BadCredentialsException("invalid request");
            }

            // do confirm
            account = accountService.confirmAccount(key);
            if (!account.isConfirmed()) {
                throw new BadCredentialsException("invalid request");
            }

            // always grant user role
            // we really don't have any additional role on accounts, aac roles are set on
            // subject
            Set<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(Config.R_USER));

            // build a valid token
            ConfirmKeyAuthenticationToken auth = new ConfirmKeyAuthenticationToken(username, key, authorities);

            return auth;

        } catch (Exception e) {
            logger.error(e.getMessage());
            // don't leak
            throw new BadCredentialsException("invalid request");
        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (ConfirmKeyAuthenticationToken.class.isAssignableFrom(authentication));
    }

}