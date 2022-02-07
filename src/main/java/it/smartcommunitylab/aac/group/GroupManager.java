/*******************************************************************************
 * Copyright 2015 Fondazione Bruno Kessler
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/

package it.smartcommunitylab.aac.group;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.Config;
import it.smartcommunitylab.aac.common.NoSuchGroupException;
import it.smartcommunitylab.aac.common.NoSuchRealmException;
import it.smartcommunitylab.aac.common.NoSuchSubjectException;
import it.smartcommunitylab.aac.core.AuthenticationHelper;
import it.smartcommunitylab.aac.core.service.SubjectService;
import it.smartcommunitylab.aac.group.persistence.GroupEntity;
import it.smartcommunitylab.aac.group.service.GroupService;
import it.smartcommunitylab.aac.model.Group;
import it.smartcommunitylab.aac.model.Subject;

/**
 * @author raman
 *
 */
@Service
@PreAuthorize("hasAuthority('" + Config.R_ADMIN + "')"
        + " or hasAuthority(#realm+':" + Config.R_ADMIN + "')")
public class GroupManager {
//    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private AuthenticationHelper authHelper;

    @Autowired
    private GroupService groupService;

//    @Autowired
//    private RealmService realmService;

    @Autowired
    private SubjectService subjectService;

    /*
     * Realm groups
     */
    @Transactional(readOnly = true)
    public Collection<Group> getRealmGroups(String realm)
            throws NoSuchRealmException {
        return groupService.listGroups(realm);

    }

    @Transactional(readOnly = true)
    public Group getRealmGroup(String realm, String groupId, boolean withMembers)
            throws NoSuchRealmException, NoSuchGroupException {
        Group g = groupService.getGroup(realm, groupId, withMembers);
        if (!realm.equals(g.getRealm())) {
            throw new IllegalArgumentException("realm mismatch");
        }

        return g;
    }

    public Group addRealmGroup(String realm, Group g) throws NoSuchRealmException {
        String group = g.getGroup();
        if (!StringUtils.hasText(group)) {
            throw new IllegalArgumentException("group can not be null or empty");
        }

        group = Jsoup.clean(group, Safelist.none());

        String groupId = g.getGroupId();
        String parentGroup = g.getParentGroup();
        String name = g.getName();
        String description = g.getDescription();

        if (StringUtils.hasText(name)) {
            name = Jsoup.clean(name, Safelist.none());
        }
        if (StringUtils.hasText(description)) {
            description = Jsoup.clean(description, Safelist.none());
        }

        return groupService.addGroup(groupId, realm, group, parentGroup, name, description);
    }

    public Group updateRealmGroup(String realm, String groupId, Group g)
            throws NoSuchRealmException, NoSuchGroupException {

        Group gl = groupService.getGroup(groupId);
        if (!realm.equals(gl.getRealm())) {
            throw new IllegalArgumentException("realm mismatch");
        }

        String group = g.getGroup();
        String parentGroup = g.getParentGroup();
        String name = g.getName();
        String description = g.getDescription();
        if (StringUtils.hasText(name)) {
            name = Jsoup.clean(name, Safelist.none());
        }
        if (StringUtils.hasText(description)) {
            description = Jsoup.clean(description, Safelist.none());
        }

        return groupService.updateGroup(groupId, realm, group, parentGroup, name, description);
    }

    public Group renameGroup(String realm, String groupId, String group)
            throws NoSuchRealmException, NoSuchGroupException {

        Group gl = groupService.getGroup(groupId);
        if (!realm.equals(gl.getRealm())) {
            throw new IllegalArgumentException("realm mismatch");
        }

        group = Jsoup.clean(group, Safelist.none());

        return groupService.renameGroup(groupId, realm, group);
    }

    public void deleteGroup(String realm, String groupId) throws NoSuchRealmException, NoSuchGroupException {
        Group gl = groupService.getGroup(groupId);
        if (gl != null) {
            if (!realm.equals(gl.getRealm())) {
                throw new IllegalArgumentException("realm mismatch");
            }

            groupService.deleteGroup(groupId);
        }
    }

    @Transactional(readOnly = true)
    public Page<Group> getRealmGroups(String realm, Pageable pageRequest) throws NoSuchRealmException {
        return groupService.listGroups(realm, pageRequest);
    }

    @Transactional(readOnly = true)
    public Collection<Group> getRealmGroupsByParent(String realm, String parentGroup) throws NoSuchRealmException {
        return groupService.listGroupsByParentGroup(realm, parentGroup);
    }

    @Transactional(readOnly = true)
    public Page<Group> searchGroupsWithSpec(String realm, Specification<GroupEntity> spec, PageRequest pageRequest)
            throws NoSuchRealmException {
        // TODO accept query spec for group not entity!
        return groupService.searchGroupsWithSpec(realm, spec, pageRequest, false);
    }

    /*
     * Group membership
     */
    public Collection<String> getGroupMembers(String realm, String group)
            throws NoSuchRealmException, NoSuchGroupException {
        return groupService.getGroupMembers(realm, group);
    }

    public Collection<String> addGroupMembers(String realm, String group, List<String> subjects)
            throws NoSuchRealmException, NoSuchGroupException {

        // TODO evaluate checking if subjects match the realm
        subjects.stream()
                .map(s -> groupService.addGroupMember(realm, group, s))
                .collect(Collectors.toList());

        return groupService.getGroupMembers(realm, group);
    }

    public Collection<String> setGroupMembers(String realm, String group, List<String> subjects)
            throws NoSuchRealmException, NoSuchGroupException {

        // TODO evaluate checking if subjects match the realm
        return groupService.setGroupMembers(realm, group, subjects);
    }

    public Collection<String> removeGroupMember(String realm, String group, String subject)
            throws NoSuchRealmException, NoSuchGroupException {

        // TODO evaluate checking if subjects match the realm
        groupService.removeGroupMember(realm, group, subject);

        return groupService.getGroupMembers(realm, group);
    }

    /*
     * Subject groups
     */
    public Collection<Group> curSubjectGroups(String realm) {
        Authentication auth = authHelper.getAuthentication();
        if (auth == null) {
            throw new InsufficientAuthenticationException("invalid or missing authentication");
        }

        String subjectId = auth.getName();
        return groupService.getSubjectGroups(subjectId, realm);
    }

    @Transactional(readOnly = true)
    public Collection<Group> getSubjectGroups(String subject, String realm)
            throws NoSuchSubjectException, NoSuchRealmException {
        return groupService.getSubjectGroups(subject, realm);
    }

    public Collection<Group> setSubjectGroups(String subject, String realm, Collection<Group> groups)
            throws NoSuchSubjectException, NoSuchRealmException, NoSuchGroupException {

        // check if subject exists
        Subject s = subjectService.getSubject(subject);

        // unpack
        List<String> toSet = groups.stream()
                .filter(r -> realm.equals(r.getRealm()) || r.getRealm() == null)
                .filter(r -> groupService.findGroup(realm, r.getGroup()) != null)
                .map(r -> r.getGroup()).collect(Collectors.toList());

        return groupService.setSubjectGroups(s.getSubjectId(), realm, toSet);

    }

    public void deleteSubjectFromGroups(String subject, String realm) throws NoSuchSubjectException {
        groupService.deleteSubjectFromGroups(subject, realm);
    }

}