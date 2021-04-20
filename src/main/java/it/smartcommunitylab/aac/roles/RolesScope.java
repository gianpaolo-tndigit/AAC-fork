package it.smartcommunitylab.aac.roles;

import it.smartcommunitylab.aac.Config;
import it.smartcommunitylab.aac.model.ScopeType;
import it.smartcommunitylab.aac.profiles.model.ProfileClaimsSet;
import it.smartcommunitylab.aac.scope.Scope;

public class RolesScope extends Scope {

    @Override
    public String getResourceId() {
        return "aac.roles";
    }

    @Override
    public ScopeType getType() {
        return ScopeType.USER;
    }

    @Override
    public String getScope() {
        return Config.SCOPE_ROLE;
    }

    // TODO replace with keys for i18n
    @Override
    public String getName() {
        return "Read user's roles";
    }

    @Override
    public String getDescription() {
        return "Roles and authorities of the current platform user. Read access only.";
    }

}
