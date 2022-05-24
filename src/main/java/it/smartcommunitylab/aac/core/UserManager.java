package it.smartcommunitylab.aac.core;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.Config;
import it.smartcommunitylab.aac.attributes.mapper.ExactAttributesMapper;
import it.smartcommunitylab.aac.attributes.service.AttributeService;
import it.smartcommunitylab.aac.audit.store.AuditEventStore;
import it.smartcommunitylab.aac.common.MissingDataException;
import it.smartcommunitylab.aac.common.NoSuchAttributeSetException;
import it.smartcommunitylab.aac.common.NoSuchAuthorityException;
import it.smartcommunitylab.aac.common.NoSuchClientException;
import it.smartcommunitylab.aac.common.NoSuchProviderException;
import it.smartcommunitylab.aac.common.NoSuchRealmException;
import it.smartcommunitylab.aac.common.NoSuchScopeException;
import it.smartcommunitylab.aac.common.NoSuchUserException;
import it.smartcommunitylab.aac.common.RegistrationException;
import it.smartcommunitylab.aac.core.model.AttributeSet;
import it.smartcommunitylab.aac.core.model.UserAccount;
import it.smartcommunitylab.aac.core.model.UserAttributes;
import it.smartcommunitylab.aac.core.persistence.ClientEntity;
import it.smartcommunitylab.aac.core.persistence.UserEntity;
import it.smartcommunitylab.aac.core.service.ClientEntityService;
import it.smartcommunitylab.aac.core.service.RealmService;
import it.smartcommunitylab.aac.core.service.UserService;
import it.smartcommunitylab.aac.dto.ConnectedAppProfile;
import it.smartcommunitylab.aac.internal.InternalAccountServiceAuthority;
import it.smartcommunitylab.aac.internal.persistence.InternalUserAccount;
import it.smartcommunitylab.aac.internal.provider.InternalAccountService;
import it.smartcommunitylab.aac.model.Group;
import it.smartcommunitylab.aac.model.Realm;
import it.smartcommunitylab.aac.model.RealmRole;
import it.smartcommunitylab.aac.model.User;
import it.smartcommunitylab.aac.oauth.store.ExtTokenStore;
import it.smartcommunitylab.aac.oauth.store.SearchableApprovalStore;
import it.smartcommunitylab.aac.scope.Scope;
import it.smartcommunitylab.aac.scope.ScopeRegistry;

/*
 * Manager for users
 * 
 * should handle all the operations on accounts, by relying on authority managers.
 * Operates on store, so not persisted identities won't be available
 * 
 * Additionally handles operations on the currently logged user, accessed via securityAccessor.
 * 
 * Exposed methods should include realm, to identify the invocation: 
 * users are a representation of a subject as visible from a realm.
 */

@Service
@PreAuthorize("hasAuthority('" + Config.R_ADMIN + "')"
        + " or hasAuthority(#realm+':" + Config.R_ADMIN + "')")
public class UserManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;

//    @Autowired
//    private UserEntityService userEntityService;

    @Autowired
    private SearchableApprovalStore approvalStore;

    @Autowired
    private ClientEntityService clientService;

    @Autowired
    private ScopeRegistry scopeRegistry;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private RealmService realmService;

//    @Autowired
//    private InternalUserManager internalUserManager;

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private InternalAccountServiceAuthority accountServiceAuthority;
//
//    @Autowired
//    private IdentityProviderService identityProviderService;
//
//    @Autowired
//    private IdentityServiceService identityServiceService;
//
//    @Autowired
//    private IdentityServiceAuthorityService identityServiceAuthorityService;
//
//    @Autowired
//    private AttributeProviderService attributeProviderService;

    @Autowired
    private ExtTokenStore tokenStore;

    @Autowired
    private AuditEventStore auditStore;

    /*
     * Manage users
     */

    /*
     * User describes user in terms of identities as attributes
     */
//    // source realm view, complete
//    public User getUser(String subjectId) throws NoSuchUserException {
//        return userService.getUser(subjectId);
//    }

    // per-realm view, partial and translated
    @Transactional(readOnly = true)
    public User getUser(String realm, String userId) throws NoSuchUserException, NoSuchRealmException {
        logger.debug("get user {} for realm {}", StringUtils.trimAllWhitespace(userId),
                StringUtils.trimAllWhitespace(realm));

        Realm r = realmService.getRealm(realm);
        // TODO evaluate if every user is globally accessible via translation or if we
        // require a pre-registration
        return userService.getUser(userId, r.getSlug());
    }

    // per realm view, lists both owned and proxied
    @Transactional(readOnly = true)
    public List<User> listUsers(String realm) throws NoSuchRealmException {
        logger.debug("list users for realm {}", StringUtils.trimAllWhitespace(realm));

        Realm r = realmService.getRealm(realm);
        return userService.listUsers(r.getSlug());
    }

    @Transactional(readOnly = true)
    public long countUsers(String realm) throws NoSuchRealmException {
        logger.debug("count users for realm {}", StringUtils.trimAllWhitespace(realm));

        Realm r = realmService.getRealm(realm);
        return userService.countUsers(r.getSlug());
    }

    @Transactional(readOnly = true)
    public Page<User> searchUsers(String realm, String keywords, Pageable pageRequest) throws NoSuchRealmException {
        logger.debug("search users for realm {} with keywords {}", StringUtils.trimAllWhitespace(realm),
                StringUtils.trimAllWhitespace(realm));
        String query = StringUtils.trimAllWhitespace(keywords);
        Realm r = realmService.getRealm(realm);
        return userService.searchUsers(r.getSlug(), query, pageRequest);
    }

    @Transactional(readOnly = true)
    public Page<User> searchUsersWithSpec(String realm, Specification<UserEntity> spec, Pageable pageRequest)
            throws NoSuchRealmException {
        logger.debug("search users for realm {} with spec {}", StringUtils.trimAllWhitespace(realm),
                String.valueOf(spec));
        Realm r = realmService.getRealm(realm);
        return userService.searchUsersWithSpec(r.getSlug(), spec, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<User> findUsersByUsername(String realm, String username) throws NoSuchRealmException {
        logger.debug("search users for realm {} with username {}", StringUtils.trimAllWhitespace(realm),
                StringUtils.trimAllWhitespace(username));
        Realm r = realmService.getRealm(realm);
        return userService.findUsersByUsername(r.getSlug(), username);
    }

    /*
     * Authorities
     */

    @Transactional(readOnly = true)
    public Collection<GrantedAuthority> getAuthorities(String realm, String userId)
            throws NoSuchUserException, NoSuchRealmException {
        logger.debug("get authorities for user {} in realm {}", StringUtils.trimAllWhitespace(userId),
                StringUtils.trimAllWhitespace(realm));

        Realm r = realmService.getRealm(realm);
        return userService.getUserAuthorities(userId, r.getSlug());
    }

    @Transactional(readOnly = false)
    public Collection<GrantedAuthority> setAuthorities(String realm, String userId, Collection<String> authorities)
            throws NoSuchUserException, NoSuchRealmException {
        logger.debug("update authorities for user {} in realm {}", StringUtils.trimAllWhitespace(userId),
                StringUtils.trimAllWhitespace(realm));
        if (logger.isTraceEnabled()) {
            logger.trace("authorities: {}", String.valueOf(authorities));
        }

        Realm r = realmService.getRealm(realm);
        return userService.setUserAuthorities(userId, r.getSlug(), authorities);
    }

    @Transactional(readOnly = false)
    public void removeUser(String realm, String userId) throws NoSuchUserException, NoSuchRealmException {
        logger.debug("remove user {} from realm {}", StringUtils.trimAllWhitespace(userId),
                StringUtils.trimAllWhitespace(realm));

        Realm r = realmService.getRealm(realm);

        // get user source realm
        String source = userService.getUserRealm(userId);
        if (source.equals(r.getSlug())) {
            // full delete
            deleteUser(userId);
        } else {
            // let userService handle account, registrations etc
            userService.removeUser(userId, r.getSlug());
        }
    }

    private void deleteUser(String userId) throws NoSuchUserException {
        logger.debug("delete user {}", StringUtils.trimAllWhitespace(userId));

        User user = userService.findUser(userId);
        if (user == null) {
            throw new NoSuchUserException();
        }

        // full delete, need to remove all associated content
        // kill sessions
        sessionManager.destroyUserSessions(userId);

        // approvals
        try {
            Collection<Approval> userApprovals = approvalStore.findUserApprovals(userId);
            approvalStore.revokeApprovals(userApprovals);
            Collection<Approval> clientApprovals = approvalStore.findClientApprovals(userId);
            approvalStore.revokeApprovals(clientApprovals);
        } catch (Exception e) {
        }

        // TODO tokens
        // TODO proxy for different realms?

        // let userService handle account, registrations etc
        userService.deleteUser(userId);
    }

    public User inviteUser(String realm, String provider, String emailAddress)
            throws NoSuchRealmException, NoSuchProviderException, RegistrationException, NoSuchAuthorityException {
        logger.debug("invite user {} to realm {}", StringUtils.trimAllWhitespace(emailAddress),
                StringUtils.trimAllWhitespace(realm));

        Realm r = realmService.getRealm(realm);

        if (!StringUtils.hasText(emailAddress)) {
            throw new MissingDataException("email");
        }

        InternalAccountService ids = null;
        if (StringUtils.hasText(provider)) {
            ids = accountServiceAuthority.getProvider(provider);
        } else {
            // fetch default internal provider if unspecified
            // TODO refactor
            ids = accountServiceAuthority.getProvidersByRealm(realm).stream().findFirst().orElse(null);
        }

        if (ids == null) {
            throw new NoSuchProviderException();
        }

        provider = ids.getProvider();

        // build only base identity
        InternalUserAccount reg = new InternalUserAccount();
        reg.setUsername(emailAddress);
        reg.setEmail(emailAddress);
        reg.setRealm(realm);

        try {
            // create internal account
            UserAccount account = userService.createUserAccount(null, provider, reg);
            String userId = account.getUserId();

            // force verification
            account = userService.verifyUserAccount(userId, provider, account.getId());

            return userService.getUser(userId, r.getSlug());
        } catch (NoSuchUserException e) {
            logger.error(e.getMessage(), e);
            throw new RegistrationException();
        }
    }

    @Transactional(readOnly = false)
    public User blockUser(String realm, String userId) throws NoSuchUserException, NoSuchRealmException {
        logger.debug("block user {} from realm {}", StringUtils.trimAllWhitespace(userId),
                StringUtils.trimAllWhitespace(realm));

        // check user source realm
        Realm r = realmService.getRealm(realm);
        String source = userService.getUserRealm(userId);
        if (!source.equals(r.getSlug())) {
            throw new IllegalArgumentException("realm-mismatch");
        }

        // block user
        return userService.blockUser(userId);
    }

    @Transactional(readOnly = false)
    public User activateUser(String realm, String userId) throws NoSuchUserException, NoSuchRealmException {
        logger.debug("activate user {} from realm {}", StringUtils.trimAllWhitespace(userId),
                StringUtils.trimAllWhitespace(realm));

        // check user source realm
        Realm r = realmService.getRealm(realm);
        String source = userService.getUserRealm(userId);
        if (!source.equals(r.getSlug())) {
            throw new IllegalArgumentException("realm-mismatch");
        }

        // activate user
        return userService.activateUser(userId);
    }

    @Transactional(readOnly = false)
    public User inactivateUser(String realm, String userId) throws NoSuchUserException, NoSuchRealmException {
        logger.debug("inactivate user {} from realm {}", StringUtils.trimAllWhitespace(userId),
                StringUtils.trimAllWhitespace(realm));

        // check user source realm
        Realm r = realmService.getRealm(realm);
        String source = userService.getUserRealm(userId);
        if (!source.equals(r.getSlug())) {
            throw new IllegalArgumentException("realm-mismatch");
        }

        // inactivate user
        return userService.inactivateUser(userId);
    }

    /*
     * User accounts
     */
    @Transactional(readOnly = false)
    public UserAccount createUserAccount(String realm, String userId, String providerId, UserAccount reg)
            throws NoSuchRealmException, NoSuchUserException, NoSuchProviderException, RegistrationException,
            NoSuchAuthorityException {
        logger.debug("create user {} identity from realm {}", StringUtils.trimAllWhitespace(userId),
                StringUtils.trimAllWhitespace(realm));

        // check user source realm
        Realm r = realmService.getRealm(realm);
        String source = userService.getUserRealm(userId);
        if (!source.equals(r.getSlug())) {
            throw new IllegalArgumentException("realm-mismatch");
        }

        return userService.createUserAccount(userId, providerId, reg);
    }

    @Transactional(readOnly = false)
    public UserAccount updateUserAccount(String realm, String userId, String providerId, String identityId,
            UserAccount reg)
            throws NoSuchRealmException, NoSuchUserException, NoSuchProviderException, RegistrationException,
            NoSuchAuthorityException {
        logger.debug("update user {} identity {} from realm {}", StringUtils.trimAllWhitespace(userId),
                StringUtils.trimAllWhitespace(identityId), StringUtils.trimAllWhitespace(realm));

        // check user source realm
        Realm r = realmService.getRealm(realm);
        String source = userService.getUserRealm(userId);
        if (!source.equals(r.getSlug())) {
            throw new IllegalArgumentException("realm-mismatch");
        }

        return userService.updateUserAccount(userId, providerId, identityId, reg);
    }

    @Transactional(readOnly = false)
    public void deleteUserAccount(String realm, String userId, String providerId, String identityId)
            throws NoSuchRealmException, NoSuchUserException, NoSuchProviderException, RegistrationException,
            NoSuchAuthorityException {
        logger.debug("delete user {} identity {} from realm {}", StringUtils.trimAllWhitespace(userId),
                StringUtils.trimAllWhitespace(identityId), StringUtils.trimAllWhitespace(realm));

        // check user source realm
        Realm r = realmService.getRealm(realm);
        String source = userService.getUserRealm(userId);
        if (!source.equals(r.getSlug())) {
            throw new IllegalArgumentException("realm-mismatch");
        }

        userService.deleteUserAccount(userId, providerId, identityId);
    }

    @Transactional(readOnly = false)
    public UserAccount verifyUserAccount(String realm, String userId, String providerId, String identityId)
            throws NoSuchRealmException, NoSuchUserException, NoSuchProviderException, RegistrationException,
            NoSuchAuthorityException {
        logger.debug("verify user {} identity {} from realm {}", StringUtils.trimAllWhitespace(userId),
                StringUtils.trimAllWhitespace(identityId), StringUtils.trimAllWhitespace(realm));

        // check user source realm
        Realm r = realmService.getRealm(realm);
        String source = userService.getUserRealm(userId);
        if (!source.equals(r.getSlug())) {
            throw new IllegalArgumentException("realm-mismatch");
        }

        return userService.verifyUserAccount(userId, providerId, identityId);
    }

    @Transactional(readOnly = false)
    public UserAccount confirmUserAccount(String realm, String userId, String providerId, String identityId)
            throws NoSuchRealmException, NoSuchUserException, NoSuchProviderException, RegistrationException,
            NoSuchAuthorityException {
        logger.debug("confirm user {} identity {} from realm {}", StringUtils.trimAllWhitespace(userId),
                StringUtils.trimAllWhitespace(identityId), StringUtils.trimAllWhitespace(realm));

        // check user source realm
        Realm r = realmService.getRealm(realm);
        String source = userService.getUserRealm(userId);
        if (!source.equals(r.getSlug())) {
            throw new IllegalArgumentException("realm-mismatch");
        }

        return userService.confirmUserAccount(userId, providerId, identityId);
    }

    @Transactional(readOnly = false)
    public UserAccount unconfirmUserAccount(String realm, String userId, String providerId, String identityId)
            throws NoSuchRealmException, NoSuchUserException, NoSuchProviderException, RegistrationException,
            NoSuchAuthorityException {
        logger.debug("unconfirm user {} identity {} from realm {}", StringUtils.trimAllWhitespace(userId),
                StringUtils.trimAllWhitespace(identityId), StringUtils.trimAllWhitespace(realm));

        // check user source realm
        Realm r = realmService.getRealm(realm);
        String source = userService.getUserRealm(userId);
        if (!source.equals(r.getSlug())) {
            throw new IllegalArgumentException("realm-mismatch");
        }

        return userService.unconfirmUserAccount(userId, providerId, identityId);
    }

    @Transactional(readOnly = false)
    public UserAccount lockUserAccount(String realm, String userId, String providerId, String identityId)
            throws NoSuchUserException, NoSuchRealmException, NoSuchProviderException, NoSuchAuthorityException {
        logger.debug("lock user {} identity {} from realm {}", StringUtils.trimAllWhitespace(userId),
                StringUtils.trimAllWhitespace(identityId), StringUtils.trimAllWhitespace(realm));

        // check user source realm
        Realm r = realmService.getRealm(realm);
        String source = userService.getUserRealm(userId);
        if (!source.equals(r.getSlug())) {
            throw new IllegalArgumentException("realm-mismatch");
        }

        return userService.lockUserAccount(userId, providerId, identityId);
    }

    @Transactional(readOnly = false)
    public UserAccount unlockUserAccount(String realm, String userId, String providerId, String identityId)
            throws NoSuchUserException, NoSuchRealmException, NoSuchProviderException, NoSuchAuthorityException {
        logger.debug("unlock user {} identity {} from realm {}", StringUtils.trimAllWhitespace(userId),
                StringUtils.trimAllWhitespace(identityId), StringUtils.trimAllWhitespace(realm));

        // check user source realm
        Realm r = realmService.getRealm(realm);
        String source = userService.getUserRealm(userId);
        if (!source.equals(r.getSlug())) {
            throw new IllegalArgumentException("realm-mismatch");
        }

        return userService.unlockUserAccount(userId, providerId, identityId);
    }

//    /*
//     * registration
//     */
//    public User registerUser(String providerId, String username, Collection<Map.Entry<String, String>> attributes) {
//        providerManager.getIdentityProvider(providerId);
//    }
//
//    public UserIdentity registerUserIdentity(String providerId, String username,
//            Collection<Map.Entry<String, String>> attributes) {
//
//    }
//
//    /*
//     * credentials
//     */
//    public UserCredentials getUserCredentials(String subjectId, String realm, String userId) {
//
//    }
//
//    public UserCredentials resetUserCredentials();
//
//    public String getResetUserCredentialsLink();
//
//    public UserCredentials setUserCredentials();

    /*
     * 2FA/MFA
     * 
     * TODO add dedicated TOTP/HTOP credentials provider
     */

    /*
     * Connected apps: subjects
     */
    @Transactional(readOnly = false)
    public Collection<ConnectedAppProfile> getConnectedApps(String realm, String subjectId) {

        // we return only clients which belong to the given realm
        List<ConnectedAppProfile> apps = getConnectedApps(subjectId).stream()
                .filter(a -> a.getRealm().equals(realm))
                .collect(Collectors.toList());

        return apps;
    }

    @Transactional(readOnly = false)
    public void deleteConnectedApp(String realm, String subjectId, String clientId)
            throws NoSuchUserException, NoSuchClientException {

        // get registrations, we need to match realm to client
        ConnectedAppProfile app = getConnectedApp(subjectId, clientId);
        if (app != null && app.getRealm().equals(realm)) {
            // valid registration in approval store, remove
            deleteConnectedApp(subjectId, clientId);
        }
    }

    /*
     * Connected apps: service
     * 
     * TODO move to dedicated service!
     */
    private ConnectedAppProfile getConnectedApp(String subjectId, String clientId)
            throws NoSuchUserException, NoSuchClientException {

        ClientEntity client = clientService.getClient(clientId);
        Collection<Approval> approvals = approvalStore.getApprovals(subjectId, clientId);
        if (approvals.isEmpty()) {
            return null;
        }

        List<Scope> scopes = new ArrayList<>();

        for (Approval appr : approvals) {
            try {

                Scope scope = scopeRegistry.getScope(appr.getScope());
                scopes.add(scope);

            } catch (NoSuchScopeException e) {
                // scope does not exists
                // we should remove the approval
                approvalStore.revokeApprovals(Collections.singleton(appr));
            }
        }

        ConnectedAppProfile app = new ConnectedAppProfile(subjectId, clientId, client.getRealm(), client.getName(),
                scopes);

        return app;
    }

    private List<ConnectedAppProfile> getConnectedApps(String subjectId) {

        Collection<Approval> approvals = approvalStore.findUserApprovals(subjectId);
        Map<ClientEntity, List<Scope>> map = new HashMap<>();

        for (Approval appr : approvals) {
            try {
                String clientId = appr.getClientId();
                ClientEntity client = clientService.getClient(clientId);
                Scope scope = scopeRegistry.getScope(appr.getScope());

                if (!map.containsKey(client)) {
                    map.put(client, new ArrayList<>());
                }

                map.get(client).add(scope);

            } catch (NoSuchClientException | NoSuchScopeException e) {
                // client was removed or scope does not exists
                // we should remove the approval
                approvalStore.revokeApprovals(Collections.singleton(appr));
            }
        }

        List<ConnectedAppProfile> apps = map.entrySet().stream()
                .map(e -> new ConnectedAppProfile(subjectId, e.getKey().getClientId(), e.getKey().getRealm(),
                        e.getKey().getName(), e.getValue()))
                .collect(Collectors.toList());

        return apps;
    }

    private void deleteConnectedApp(String subjectId, String clientId) {

        // TODO revoke tokens

//        Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientIdAndUserName(clientId, user.toString());
//        for (OAuth2AccessToken token : tokens) {
//            if (token.getRefreshToken() != null) {
//                // remove refresh token
//                OAuth2RefreshToken refreshToken = token.getRefreshToken();
//                tokenStore.removeRefreshToken(refreshToken);
//            }
//
//            // remove access token
//            tokenStore.removeAccessToken(token);
//        }

        // remove approvals
        Collection<Approval> approvals = approvalStore.getApprovals(subjectId, clientId);
        if (!approvals.isEmpty()) {
            approvalStore.revokeApprovals(approvals);
        }

    }

    public Collection<Approval> getApprovals(String realm, String subjectId) throws NoSuchUserException {
        User user = userService.findUser(subjectId);
        if (user == null) {
            throw new NoSuchUserException();
        }

        Collection<Approval> approvals = approvalStore.findClientApprovals(subjectId);
        return approvals;
    }

    public Collection<OAuth2AccessToken> getAccessTokens(String realm, String subjectId) throws NoSuchUserException {
        User user = userService.findUser(subjectId);
        if (user == null) {
            throw new NoSuchUserException();
        }

        Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByUserName(subjectId);
        return tokens;
    }

    public Collection<AuditEvent> getAudit(String realm, String subjectId, Date after, Date before)
            throws NoSuchUserException {
        User user = userService.findUser(subjectId);
        if (user == null) {
            throw new NoSuchUserException();
        }

        Instant now = Instant.now();
        Instant a = after == null ? now.minus(5, ChronoUnit.DAYS) : after.toInstant();
        Instant b = before == null ? now : before.toInstant();

        return auditStore.findByPrincipal(subjectId, a, b, null);
    }

//	/**
//	 * @param subjectId
//	 * @return
//	 * @throws NoSuchUserException 
//	 * @throws NoSuchRealmException 
//	 */
//	public Collection<SpaceRole> getMyRoles() throws NoSuchUserException, NoSuchRealmException {
//        UserDetails user = authHelper.getUserDetails();
//        if (user == null) {
//        	throw new NoSuchUserException();
//        }
//        Collection<SpaceRole> roles = new HashSet<>(userService.getUserRoles(user.getRealm(), user.getSubjectId()));
//        if (user.isSystemAdmin()) {
//        	roles.add(new SpaceRole(null, null, Config.R_PROVIDER));
//        }
//        return roles;
//	}

//	/**
//	 * @param context
//	 * @param q
//	 * @param pageRequest
//	 * @return
//	 */
//	public Page<SpaceRoles> getContextRoles(String context, String space, String q, Pageable pageRequest) {
//		return userService.getContextRoles(context, space, q, pageRequest);
//	}
//
//	/**
//	 * @param subject
//	 * @param context
//	 * @param space
//	 * @param roles
//	 * @return
//	 */
//	public SpaceRoles saveContextRoles(String subject, String context, String space, List<String> roles) {
//		return userService.saveContextRoles(subject, context, space, roles);
//	}

    public Collection<UserAttributes> getUserAttributes(String realm, String subjectId)
            throws NoSuchRealmException, NoSuchUserException {

        Realm r = realmService.getRealm(realm);
        return userService.getUserAttributes(subjectId, r.getSlug());
    }

    public UserAttributes getUserAttributes(String realm, String subjectId,
            String provider, String identifier)
            throws NoSuchUserException, NoSuchProviderException, NoSuchRealmException, NoSuchAttributeSetException,
            NoSuchAuthorityException {

        Realm r = realmService.getRealm(realm);

        // get attributeSet
        AttributeSet as = attributeService.getAttributeSet(identifier);

        return userService.getUserAttributes(subjectId, r.getSlug(), provider, as.getIdentifier());

    }

    public UserAttributes setUserAttributes(String realm, String subjectId,
            String provider, String identifier,
            Map<String, Serializable> attributes)
            throws NoSuchUserException, NoSuchProviderException, NoSuchRealmException, NoSuchAttributeSetException {

        Realm r = realmService.getRealm(realm);

        // get attributeSet
        AttributeSet as = attributeService.getAttributeSet(identifier);

        // build a mapper to extract from values
        ExactAttributesMapper mapper = new ExactAttributesMapper(as);
        AttributeSet set = mapper.mapAttributes(attributes);
        if (set.getAttributes() == null || set.getAttributes().isEmpty()) {
            throw new IllegalArgumentException("empty or invalid attribute set");
        }

        return userService.setUserAttributes(subjectId, r.getSlug(), provider, set);
    }

    public void removeUserAttributes(String realm, String subjectId,
            String provider, String identifier)
            throws NoSuchUserException, NoSuchProviderException, NoSuchRealmException, NoSuchAttributeSetException,
            NoSuchAuthorityException {
        userService.removeUserAttributes(subjectId, realm, provider, identifier);
    }

//    /*
//     * User identity/attribute providers
//     * 
//     * TODO evaluate returning actual providers in place of configurable models
//     */
//    public Collection<ConfigurableIdentityProvider> getUserIdentityProviders(String realm, String subjectId)
//            throws NoSuchRealmException, NoSuchUserException {
//
//        Realm r = realmService.getRealm(realm);
//        // TODO filter per user
//        return identityProviderService.listProviders(r.getSlug()).stream()
//                .map(cp -> {
//                    // clear config and reserved info
//                    cp.setEvents(null);
//                    cp.setPersistence(null);
//                    cp.setSchema(null);
//                    cp.setConfiguration(null);
//                    cp.setHookFunctions(null);
//
//                    return cp;
//                }).collect(Collectors.toList());
//
//    }
//
//    public Collection<ConfigurableAttributeProvider> getUserAttributeProviders(String realm, String subjectId)
//            throws NoSuchRealmException, NoSuchUserException {
//
//        Realm r = realmService.getRealm(realm);
//        // TODO filter per user
//        return attributeProviderService.listProviders(r.getSlug()).stream()
//                .map(cp -> {
//                    // clear config and reserved info
//                    cp.setEvents(null);
//                    cp.setPersistence(null);
//                    cp.setSchema(null);
//                    cp.setConfiguration(null);
//                    cp.setAttributeSets(null);
//                    return cp;
//                }).collect(Collectors.toList());
//
//    }

    /*
     * Roles
     */

    public Collection<RealmRole> getUserRealmRoles(String realm, String subjectId)
            throws NoSuchRealmException, NoSuchUserException {
        return userService.fetchUserRealmRoles(subjectId, realm);
    }

    /*
     * Groups
     */
    public Collection<Group> getUserGroups(String realm, String subjectId)
            throws NoSuchRealmException, NoSuchUserException {
        return userService.fetchUserGroups(subjectId, realm);
    }

}
