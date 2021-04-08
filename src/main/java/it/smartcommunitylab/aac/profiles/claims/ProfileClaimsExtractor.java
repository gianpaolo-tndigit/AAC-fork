package it.smartcommunitylab.aac.profiles.claims;

import java.util.Collection;

import it.smartcommunitylab.aac.claims.ScopeClaimsExtractor;
import it.smartcommunitylab.aac.claims.ClaimsSet;
import it.smartcommunitylab.aac.common.InvalidDefinitionException;
import it.smartcommunitylab.aac.common.SystemException;
import it.smartcommunitylab.aac.core.ClientDetails;
import it.smartcommunitylab.aac.core.UserDetails;
import it.smartcommunitylab.aac.model.User;
import it.smartcommunitylab.aac.profiles.model.AbstractProfile;
import it.smartcommunitylab.aac.profiles.model.ProfileClaimsSet;

public abstract class ProfileClaimsExtractor implements ScopeClaimsExtractor {

    @Override
    public String getResourceId() {
        return ProfileClaimsSet.RESOURCE_ID;
    }

    public abstract String getKey();

    @Override
    public ClaimsSet extractUserClaims(String scope, User user, ClientDetails client, Collection<String> scopes)
            throws InvalidDefinitionException, SystemException {

        AbstractProfile profile = buildUserProfile(user, scopes);

        // build a claimsSet
        ClaimsSet claimsSet = buildClaimsSet(scope, getKey(), profile, true);

        return claimsSet;

    }

    // subclasses need to provide the profile
    protected abstract AbstractProfile buildUserProfile(User user, Collection<String> scopes)
            throws InvalidDefinitionException;

    protected ClaimsSet buildClaimsSet(String scope, String key, AbstractProfile profile, boolean isUser) {
        ProfileClaimsSet claimsSet = new ProfileClaimsSet();
        claimsSet.setScope(scope);
        claimsSet.setKey(key);

        // by default profile claims are top level
        claimsSet.setNamespace(null);

        // set profile
        claimsSet.setUser(isUser);
        claimsSet.setProfile(profile);

        return claimsSet;
    }

    @Override
    public ClaimsSet extractClientClaims(String scope, ClientDetails client, Collection<String> scopes)
            throws InvalidDefinitionException, SystemException {
        // not supported now but subclasses can override
        return null;
    }

}