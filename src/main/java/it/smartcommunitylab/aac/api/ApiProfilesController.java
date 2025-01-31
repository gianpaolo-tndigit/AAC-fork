package it.smartcommunitylab.aac.api;

import java.util.Collection;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.smartcommunitylab.aac.Config;
import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.common.InvalidDefinitionException;
import it.smartcommunitylab.aac.common.NoSuchRealmException;
import it.smartcommunitylab.aac.common.NoSuchUserException;
import it.smartcommunitylab.aac.profiles.ProfileManager;
import it.smartcommunitylab.aac.profiles.model.AbstractProfile;
import it.smartcommunitylab.aac.profiles.model.AccountProfile;
import it.smartcommunitylab.aac.profiles.model.BasicProfile;
import it.smartcommunitylab.aac.profiles.model.ProfileResponse;

@RestController
@Tag(name = "User profiles", description = "Access and consume user profiles")
@RequestMapping(value = "api", consumes = { MediaType.APPLICATION_JSON_VALUE,
        SystemKeys.MEDIA_TYPE_XYAML_VALUE }, produces = {
                MediaType.APPLICATION_JSON_VALUE, SystemKeys.MEDIA_TYPE_XYAML_VALUE })
public class ApiProfilesController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProfileManager profileManager;

    /*
     * Api operations
     */
    @GetMapping(value = "/profiles/{realm}/{identifier}/{subject}")
    @Operation(summary = "Get custom profiles of a user")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN')) and hasAuthority('SCOPE_profile.#identifier.all')")
    public @ResponseBody ProfileResponse getProfiles(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String identifier,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String subject)
            throws NoSuchRealmException, NoSuchUserException, InvalidDefinitionException {
        logger.debug("get profiles {} for subject {} from realm {}", StringUtils.trimAllWhitespace(identifier),
                StringUtils.trimAllWhitespace(subject), StringUtils.trimAllWhitespace(realm));

        Collection<AbstractProfile> profiles = profileManager.getProfiles(realm, subject, identifier);
        return new ProfileResponse(subject, profiles);
    }

    /*
     * Core profiles (legacy)
     */

    @Deprecated()
    @Operation(summary = "Get basic profile of a user")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN')) and hasAuthority('SCOPE_" + Config.SCOPE_BASIC_PROFILE_ALL
            + "')")
    @GetMapping(value = "/profiles/{realm}/basicprofile/{subject}")
    public @ResponseBody ProfileResponse getBasicProfile(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String subject)
            throws NoSuchRealmException, NoSuchUserException, InvalidDefinitionException {
        AbstractProfile profile = profileManager.getProfile(realm, subject, BasicProfile.IDENTIFIER);
        return new ProfileResponse(subject, profile);
    }

    @Deprecated()
    @Operation(summary = "Get openid profile of a user")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN')) and hasAuthority('SCOPE_" + Config.SCOPE_OPENID_PROFILE_ALL
            + "')")
    @GetMapping(value = "/profiles/{realm}/openidprofile/{subject}")
    public @ResponseBody ProfileResponse getOpenIdProfile(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String subject)
            throws NoSuchRealmException, NoSuchUserException, InvalidDefinitionException {
        AbstractProfile profile = profileManager.getProfile(realm, subject, BasicProfile.IDENTIFIER);
        return new ProfileResponse(subject, profile);
    }

    @Deprecated()
    @Operation(summary = "Get account profiles of a user")
    @PreAuthorize("(hasAuthority('" + Config.R_ADMIN
            + "') or hasAuthority(#realm+':ROLE_ADMIN')) and hasAuthority('SCOPE_" + Config.SCOPE_ACCOUNT_PROFILE_ALL
            + "')")
    @GetMapping(value = "/profiles/{realm}/accountprofile/{subject}")
    public @ResponseBody ProfileResponse getAccountProfiles(
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @Pattern(regexp = SystemKeys.SLUG_PATTERN) String subject)
            throws NoSuchRealmException, NoSuchUserException, InvalidDefinitionException {
        Collection<AbstractProfile> profiles = profileManager.getProfiles(realm, subject, AccountProfile.IDENTIFIER);
        return new ProfileResponse(subject, profiles);
    }

}
