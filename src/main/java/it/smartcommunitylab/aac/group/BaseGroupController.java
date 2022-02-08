package it.smartcommunitylab.aac.group;

import java.util.Collection;
import java.util.Collections;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.smartcommunitylab.aac.Config;
import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.common.NoSuchRealmException;
import it.smartcommunitylab.aac.common.NoSuchGroupException;
import it.smartcommunitylab.aac.model.Group;

/*
 * Base controller for realm groups
 */
@Tag(name = "Groups", description = "Manage realm groups and group membership")
@PreAuthorize("hasAuthority(this.authority)")
public class BaseGroupController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected GroupManager groupManager;

    public String getAuthority() {
        return Config.R_USER;
    }

    /*
     * Realm groups
     */

    @GetMapping("/groups/{realm}")
    @Operation(summary = "list groups for realm")
    public Collection<Group> getGroups(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm)
            throws NoSuchRealmException {
        logger.debug("list groups for realm {}",
                StringUtils.trimAllWhitespace(realm));

        return groupManager.getGroups(realm);
    }

    @PostMapping("/groups/{realm}")
    @Operation(summary = "add a new group for realm")
    public Group createGroup(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @RequestBody @Valid @NotNull Group reg)
            throws NoSuchRealmException, NoSuchGroupException {
        logger.debug("add role to realm {}",
                StringUtils.trimAllWhitespace(realm));

        // unpack and build model
        String group = reg.getGroup();
        String parentGroup = reg.getParentGroup();
        String name = reg.getName();
        String description = reg.getDescription();

        Group g = new Group();
        g.setRealm(realm);
        g.setGroup(group);
        g.setParentGroup(parentGroup);
        g.setName(name);
        g.setDescription(description);
        g.setMembers(reg.getMembers());

        if (logger.isTraceEnabled()) {
            logger.trace("group bean: " + StringUtils.trimAllWhitespace(g.toString()));
        }

        g = groupManager.addGroup(realm, g);
        return g;
    }

    @GetMapping("/groups/{realm}/{groupId}")
    @Operation(summary = "fetch a specific group from realm")

    public Group getGroup(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String groupId)
            throws NoSuchRealmException, NoSuchGroupException {
        logger.debug("get group {} for realm {}",
                StringUtils.trimAllWhitespace(groupId), StringUtils.trimAllWhitespace(realm));

        Group g = groupManager.getGroup(realm, groupId, true);
        return g;
    }

    @PutMapping("/groups/{realm}/{groupId}")
    @Operation(summary = "update a specific group in the realm")
    public Group updateGroup(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String groupId,
            @RequestBody @Valid @NotNull Group reg)
            throws NoSuchRealmException, NoSuchGroupException {
        logger.debug("update group {} for realm {}",
                StringUtils.trimAllWhitespace(groupId), StringUtils.trimAllWhitespace(realm));

        Group g = groupManager.getGroup(realm, groupId, false);

        // unpack and build model
        String group = reg.getGroup();
        String parentGroup = reg.getParentGroup();
        String name = reg.getName();
        String description = reg.getDescription();

        g.setParentGroup(parentGroup);
        g.setName(name);
        g.setDescription(description);
        g.setMembers(reg.getMembers());

        if (logger.isTraceEnabled()) {
            logger.trace("group bean: " + StringUtils.trimAllWhitespace(g.toString()));
        }

        g = groupManager.updateGroup(realm, groupId, g);

        // enable group rename if requested
        if (!g.getGroup().equals(group)) {
            logger.debug("rename group {} for realm {}",
                    StringUtils.trimAllWhitespace(groupId), StringUtils.trimAllWhitespace(realm));

            g = groupManager.renameGroup(realm, groupId, group);
        }

        return g;
    }

    @DeleteMapping("/groups/{realm}/{groupId}")
    @Operation(summary = "remove a specific group from realm")
    public void removeGroup(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String groupId)
            throws NoSuchRealmException, NoSuchGroupException {
        logger.debug("delete group {} for realm {}",
                StringUtils.trimAllWhitespace(groupId), StringUtils.trimAllWhitespace(realm));

        groupManager.deleteGroup(realm, groupId);
    }

    /*
     * Group membership
     */

    @GetMapping("/groups/{realm}/{groupId}/members")
    @Operation(summary = "get members for a given group")
    public Collection<String> getGroupMembers(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String groupId)
            throws NoSuchRealmException, NoSuchGroupException {
        logger.debug("get group {} members for realm {}",
                StringUtils.trimAllWhitespace(groupId), StringUtils.trimAllWhitespace(realm));

        return groupManager.getGroupMembers(realm, groupId);
    }

    @PostMapping("/groups/{realm}/{groupId}/members")
    @Operation(summary = "add subjects as members for a given group")
    public Collection<String> addGroupMembers(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String groupId,
            @RequestBody @Valid @NotNull Collection<String> members)
            throws NoSuchRealmException, NoSuchGroupException {
        logger.debug("add group {} members for realm {}",
                StringUtils.trimAllWhitespace(groupId), StringUtils.trimAllWhitespace(realm));

        if (members != null && !members.isEmpty()) {
            return groupManager.addGroupMembers(realm, groupId, members);
        }

        return Collections.emptyList();
    }

    @PutMapping("/groups/{realm}/{groupId}/members")
    @Operation(summary = "set subjects as the members for a given group")
    public Collection<String> setGroupMembers(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String groupId,
            @RequestBody @Valid Collection<String> members)
            throws NoSuchRealmException, NoSuchGroupException {
        logger.debug("set group {} members for realm {}",
                StringUtils.trimAllWhitespace(groupId), StringUtils.trimAllWhitespace(realm));

        return groupManager.setGroupMembers(realm, groupId, members);
    }

    @DeleteMapping("/groups/{realm}/{groupId}/member/{subjectId}")
    @Operation(summary = "remove a specific subject from a given group")
    public void removeGroupMember(
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String realm,
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String groupId,
            @PathVariable @Valid @NotNull @Pattern(regexp = SystemKeys.SLUG_PATTERN) String subjectId)
            throws NoSuchRealmException, NoSuchGroupException {
        logger.debug("delete group {} members {} for realm {}",
                StringUtils.trimAllWhitespace(groupId), StringUtils.trimAllWhitespace(subjectId),
                StringUtils.trimAllWhitespace(realm));

        groupManager.removeGroupMember(realm, groupId, subjectId);
    }

}
