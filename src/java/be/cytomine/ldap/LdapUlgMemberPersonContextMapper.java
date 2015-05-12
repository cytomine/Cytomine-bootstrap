package be.cytomine.ldap;

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * Created by hoyoux on 09.12.14.
 */
public class LdapUlgMemberPersonContextMapper implements UserDetailsContextMapper {

    @Override
    public UserDetails mapUserFromContext(DirContextOperations dirContextOperations, String s, Collection<? extends GrantedAuthority> grantedAuthorities) {
        LdapUlgMemberPerson.Essence p = new LdapUlgMemberPerson.Essence(dirContextOperations);

        p.setUsername(s);
        p.setAuthorities(grantedAuthorities);


        return p.createUserDetails();
    }

    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        Assert.isInstanceOf(LdapUlgMemberPerson.class, user, "UserDetails must be an LdapUlgMemberPerson instance");

        LdapUlgMemberPerson p = (LdapUlgMemberPerson) user;
        p.populateContext(ctx);
    }
}
