package it.smartcommunitylab.aac.core.provider;

import java.util.Collection;

import it.smartcommunitylab.aac.common.NoSuchUserException;
import it.smartcommunitylab.aac.common.RegistrationException;
import it.smartcommunitylab.aac.core.model.UserAccount;
import it.smartcommunitylab.aac.core.model.UserAttributes;
import it.smartcommunitylab.aac.core.model.UserIdentity;

/*
 * An identity provider which persists some data about users
 */

public interface IdentityService<I extends UserIdentity, U extends UserAccount>
        extends IdentityProvider<I> {

    /*
     * Services
     */

    public AccountService<U> getAccountService();

    public UserCredentialsService getCredentialsService();

    /*
     * Manage identities from this provider
     * 
     * userId is globally addressable
     */

    public I registerIdentity(
            String userId, UserAccount account,
            Collection<UserAttributes> attributes) throws NoSuchUserException, RegistrationException;

    public I updateIdentity(
            String identityId, UserAccount account,
            Collection<UserAttributes> attributes) throws NoSuchUserException, RegistrationException;

    /*
     * Registration
     */

    public String getRegistrationUrl();

}
