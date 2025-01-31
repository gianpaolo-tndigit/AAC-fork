package it.smartcommunitylab.aac.openid.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.core.provider.ProviderConfigRepository;
import it.smartcommunitylab.aac.oauth.model.PromptMode;
import it.smartcommunitylab.aac.openid.provider.OIDCIdentityProviderConfig;

public class ExtendedAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final ProviderConfigRepository<OIDCIdentityProviderConfig> registrationRepository;
    private final OAuth2AuthorizationRequestResolver resolver;

    public ExtendedAuthorizationRequestResolver(OAuth2AuthorizationRequestResolver resolver,
            ProviderConfigRepository<OIDCIdentityProviderConfig> registrationRepository) {
        this.resolver = resolver;
        this.registrationRepository = registrationRepository;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return enhance(resolver.resolve(request));

    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return enhance(resolver.resolve(request, clientRegistrationId));

    }

    public OAuth2AuthorizationRequest enhance(OAuth2AuthorizationRequest authRequest) {
        if (authRequest == null) {
            return null;
        }

        String providerId = authRequest.getAttribute(OAuth2ParameterNames.REGISTRATION_ID);
        if (!StringUtils.hasText(providerId)) {
            return null;
        }

        OIDCIdentityProviderConfig provider = registrationRepository.findByProviderId(providerId);

        // fetch paramers from resolved request
        Map<String, Object> attributes = new HashMap<>(authRequest.getAttributes());
        Map<String, Object> additionalParameters = new HashMap<>(authRequest.getAdditionalParameters());

        if (provider.getConfigMap().getPromptMode() != null) {
            addPromptParameters(provider.getConfigMap().getPromptMode(), additionalParameters);

        }

        // get a builder and reset parameters
        return OAuth2AuthorizationRequest.from(authRequest)
                .attributes(attributes)
                .additionalParameters(additionalParameters)
                .build();

    }

    private void addPromptParameters(Set<PromptMode> promptMode, Map<String, Object> additionalParameters) {

        String prompt = StringUtils.collectionToDelimitedString(promptMode, " ");
        if (StringUtils.hasText(prompt)) {
            additionalParameters.put("prompt", prompt);
        }

    }

}