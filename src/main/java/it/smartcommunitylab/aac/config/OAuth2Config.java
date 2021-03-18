package it.smartcommunitylab.aac.config;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.OAuth2RequestValidator;
import org.springframework.security.oauth2.provider.SecurityContextAccessor;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.approval.DefaultUserApprovalHandler;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.security.oauth2.provider.endpoint.RedirectResolver;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.support.DefaultSessionAttributeStore;
import org.springframework.web.bind.support.SessionAttributeStore;

import it.smartcommunitylab.aac.core.AuthenticationHelper;
import it.smartcommunitylab.aac.core.auth.DefaultSecurityContextAuthenticationHelper;
import it.smartcommunitylab.aac.core.service.ClientEntityService;
import it.smartcommunitylab.aac.oauth.AACOAuth2RequestValidator;
import it.smartcommunitylab.aac.oauth.AutoJdbcAuthorizationCodeServices;
import it.smartcommunitylab.aac.oauth.AutoJdbcTokenStore;
import it.smartcommunitylab.aac.oauth.ExtRedirectResolver;
import it.smartcommunitylab.aac.oauth.NonRemovingTokenServices;
import it.smartcommunitylab.aac.oauth.persistence.OAuth2ClientEntityRepository;
import it.smartcommunitylab.aac.oauth.service.OAuth2ClientDetailsService;
import it.smartcommunitylab.aac.oauth.service.OAuth2ClientUserDetailsService;
import it.smartcommunitylab.aac.oauth.token.AuthorizationCodeTokenGranter;
import it.smartcommunitylab.aac.oauth.token.ClientCredentialsTokenGranter;
import it.smartcommunitylab.aac.oauth.token.ImplicitTokenGranter;
import it.smartcommunitylab.aac.oauth.token.PKCEAwareTokenGranter;
import it.smartcommunitylab.aac.oauth.token.RefreshTokenGranter;
import it.smartcommunitylab.aac.oauth.token.ResourceOwnerPasswordTokenGranter;

@Configuration
public class OAuth2Config {

    @Value("${application.url}")
    private String applicationURL;

    @Value("${oauth2.jwt}")
    private boolean oauth2UseJwt;

    @Value("${oauth2.authcode.validity}")
    private int authCodeValidity;

    @Value("${oauth2.accesstoken.validity}")
    private int accessTokenValidity;

    @Value("${oauth2.refreshtoken.validity}")
    private int refreshTokenValidity;

    @Value("${oauth2.redirects.matchports}")
    private boolean redirectMatchPorts;

    @Value("${oauth2.redirects.matchsubdomains}")
    private boolean redirectMatchSubDomains;

    @Value("${oauth2.pkce.allowRefresh}")
    private boolean oauth2PKCEAllowRefresh;

    @Value("${oauth2.clientCredentials.allowRefresh}")
    private boolean oauth2ClientCredentialsAllowRefresh;

    @Value("${oauth2.resourceOwnerPassword.allowRefresh}")
    private boolean oauth2ResourceOwnerPasswordAllowRefresh;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AuthenticationManager authManager;

    @Bean
    public AuthenticationHelper authenticationHelper() {
        return new DefaultSecurityContextAuthenticationHelper();
    }

    @Bean
    public SecurityContextAccessor securityContextAccessor() {
        return new DefaultSecurityContextAuthenticationHelper();
    }

    @Bean
    public OAuth2RequestFactory getOAuth2RequestFactory(
            ClientDetailsService clientDetailsService,
            SecurityContextAccessor securityContextAccessor)
            throws PropertyVetoException {

//      AACOAuth2RequestFactory<UserManager> result = new AACOAuth2RequestFactory<>();
        DefaultOAuth2RequestFactory requestFactory = new DefaultOAuth2RequestFactory(clientDetailsService);
        requestFactory.setCheckUserScopes(false);
        requestFactory.setSecurityContextAccessor(securityContextAccessor);
        return requestFactory;
    }

    @Bean
    public OAuth2RequestValidator getOAuth2RequestValidator() {
        AACOAuth2RequestValidator requestValidator = new AACOAuth2RequestValidator();
        return requestValidator;
    }

    @Bean
    public RedirectResolver getRedirectResolver() {
        ExtRedirectResolver redirectResolver = new ExtRedirectResolver(applicationURL, redirectMatchPorts,
                redirectMatchSubDomains);
        return redirectResolver;
    }

    @Bean
    public AutoJdbcAuthorizationCodeServices getAuthorizationCodeServices() throws PropertyVetoException {
        return new AutoJdbcAuthorizationCodeServices(dataSource, authCodeValidity);
    }

    @Bean
    public ClientDetailsService getClientDetailsService(ClientEntityService clientService,
            OAuth2ClientEntityRepository clientRepository) throws PropertyVetoException {
        return new OAuth2ClientDetailsService(clientService, clientRepository);
    }

    // TODO remove class
    @Bean
    public OAuth2ClientUserDetailsService getClientUserDetailsService(OAuth2ClientEntityRepository clientRepository) {
        return new OAuth2ClientUserDetailsService(clientRepository);
    }

    @Bean
    public UserApprovalHandler getUserApprovalHandler() {
        // TODO replace our handler
        return new DefaultUserApprovalHandler();
    }

    @Bean
    public SessionAttributeStore getLocalSessionAttributeStore() {
        // store in httpSession
        return new DefaultSessionAttributeStore();
    }

    @Bean
    public TokenStore getJDBCTokenStore() throws PropertyVetoException {
        return new AutoJdbcTokenStore(dataSource);
    }

    @Bean
    public AuthorizationServerTokenServices getTokenServices(
            ClientDetailsService clientDetailsService,
            TokenStore tokenStore) throws PropertyVetoException {
        NonRemovingTokenServices tokenServices = new NonRemovingTokenServices();
        tokenServices.setAuthenticationManager(authManager);
        tokenServices.setClientDetailsService(clientDetailsService);
        tokenServices.setTokenStore(tokenStore);
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setReuseRefreshToken(true);
        tokenServices.setAccessTokenValiditySeconds(accessTokenValidity);
        tokenServices.setRefreshTokenValiditySeconds(refreshTokenValidity);
//        if (oauth2UseJwt) {
//            bean.setTokenEnhancer(new AACTokenEnhancer(tokenEnhancer, tokenConverter));
//        } else {
//            bean.setTokenEnhancer(new AACTokenEnhancer(tokenEnhancer));         
//        }

        return tokenServices;
    }

    @Bean
    public TokenGranter getTokenGranter(
            AuthorizationServerTokenServices tokenServices,
            ClientDetailsService clientDetailsService,
            AuthorizationCodeServices authorizationCodeServices,
            OAuth2RequestFactory oAuth2RequestFactory) {

        // build our own list of granters
        List<TokenGranter> granters = new ArrayList<TokenGranter>();
        // insert PKCE auth code granter as the first one to supersede basic authcode
        PKCEAwareTokenGranter pkceTokenGranter = new PKCEAwareTokenGranter(tokenServices,
                authorizationCodeServices,
                clientDetailsService, oAuth2RequestFactory);
        if (oauth2PKCEAllowRefresh) {
            pkceTokenGranter.setAllowRefresh(true);
        }
        granters.add(pkceTokenGranter);

        // auth code
        granters.add(new AuthorizationCodeTokenGranter(tokenServices,
                authorizationCodeServices, clientDetailsService,
                oAuth2RequestFactory));

        // refresh
        granters.add(new RefreshTokenGranter(tokenServices, clientDetailsService,
                oAuth2RequestFactory));

        // implicit
        granters.add(new ImplicitTokenGranter(tokenServices,
                clientDetailsService, oAuth2RequestFactory));

        // client credentials
        ClientCredentialsTokenGranter clientCredentialsTokenGranter = new ClientCredentialsTokenGranter(
                tokenServices,
                clientDetailsService, oAuth2RequestFactory);
        if (oauth2ClientCredentialsAllowRefresh) {
            clientCredentialsTokenGranter.setAllowRefresh(true);
        }
        granters.add(clientCredentialsTokenGranter);

        // resource owner password
        if (authManager != null) {
            ResourceOwnerPasswordTokenGranter passwordTokenGranter = new ResourceOwnerPasswordTokenGranter(
                    authManager, tokenServices,
                    clientDetailsService, oAuth2RequestFactory);
            if (!oauth2ResourceOwnerPasswordAllowRefresh) {
                passwordTokenGranter.setAllowRefresh(false);
            }
            granters.add(passwordTokenGranter);
        }

        return new CompositeTokenGranter(granters);
    }

    @Bean
    public AuthorizationEndpoint getAuthorizationEndpoint(
            ClientDetailsService clientDetailsService,
            AuthorizationCodeServices authorizationCodeServices,
            TokenGranter tokenGranter,
            RedirectResolver redirectResolver,
            UserApprovalHandler userApprovalHandler,
            SessionAttributeStore sessionAttributeStore,
            OAuth2RequestFactory oAuth2RequestFactory,
            OAuth2RequestValidator oauth2RequestValidator) {
        AuthorizationEndpoint authEndpoint = new AuthorizationEndpoint();
        authEndpoint.setClientDetailsService(clientDetailsService);
        authEndpoint.setAuthorizationCodeServices(authorizationCodeServices);
        authEndpoint.setTokenGranter(tokenGranter);
        authEndpoint.setRedirectResolver(redirectResolver);
        authEndpoint.setUserApprovalHandler(userApprovalHandler);
        authEndpoint.setSessionAttributeStore(sessionAttributeStore);
        authEndpoint.setOAuth2RequestFactory(oAuth2RequestFactory);
        authEndpoint.setOAuth2RequestValidator(oauth2RequestValidator);

        return authEndpoint;
    }

    @Bean
    public TokenEndpoint getTokenEndpoint(
            ClientDetailsService clientDetailsService,
            TokenGranter tokenGranter,
            RedirectResolver redirectResolver,
            UserApprovalHandler userApprovalHandler,
            SessionAttributeStore sessionAttributeStore,
            OAuth2RequestFactory oAuth2RequestFactory,
            OAuth2RequestValidator oauth2RequestValidator) {
        TokenEndpoint tokenEndpoint = new TokenEndpoint();
        tokenEndpoint.setClientDetailsService(clientDetailsService);
        tokenEndpoint.setTokenGranter(tokenGranter);
        tokenEndpoint.setOAuth2RequestFactory(oAuth2RequestFactory);
        tokenEndpoint.setOAuth2RequestValidator(oauth2RequestValidator);

        return tokenEndpoint;
    }

}
