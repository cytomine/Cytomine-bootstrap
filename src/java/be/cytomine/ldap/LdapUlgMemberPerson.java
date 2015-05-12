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
import org.springframework.security.ldap.userdetails.InetOrgPerson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hoyoux on 09.12.14.
 */
public class LdapUlgMemberPerson extends InetOrgPerson {

    private List<String> ulgCurriculum = new ArrayList<String>();
    private List<String> ulgMemberOf = new ArrayList<String>();

    public String[] getUlgCurriculum() {
        return ulgCurriculum.toArray(new String[ulgCurriculum.size()]);
    }
    public String[] getUlgMemberOf() {
        return ulgMemberOf.toArray(new String[ulgMemberOf.size()]);
    }

    @Override
    protected void populateContext(DirContextAdapter adapter) {
        super.populateContext(adapter);
        adapter.setAttributeValues("ulgCurriculum", getUlgCurriculum());
        adapter.setAttributeValues("ulgMemberOf", getUlgMemberOf());
    }

    public static class Essence extends InetOrgPerson.Essence {
        public Essence() {
            super();
        }

        public Essence(LdapUlgMemberPerson copyMe) {
            super(copyMe);
            setUlgCurriculum(copyMe.getUlgCurriculum());
            setUlgMemberOf(copyMe.getUlgMemberOf());
        }

        public Essence(org.springframework.ldap.core.DirContextOperations ctx) {
            super(ctx);
            setUlgCurriculum(ctx.getStringAttributes("ulgCurriculum"));
            setUlgMemberOf(ctx.getStringAttributes("ulgMemberOf"));
        }

        protected org.springframework.security.ldap.userdetails.LdapUserDetailsImpl createTarget() {
            return new LdapUlgMemberPerson();
        }

        public void setUlgCurriculum(String[] ulgCurriculum) {
            if(ulgCurriculum != null)
                ((LdapUlgMemberPerson) instance).ulgCurriculum = Arrays.asList(ulgCurriculum);
        }
        public void addUlgCurriculum(String value) {
            ((LdapUlgMemberPerson) instance).ulgCurriculum.add(value);
        }

        public void setUlgMemberOf(String[] ulgMemberOf) {
            if(ulgMemberOf != null)
                ((LdapUlgMemberPerson) instance).ulgMemberOf = Arrays.asList(ulgMemberOf);
        }
        public void addUlgMemberOf(String value) {
            ((LdapUlgMemberPerson) instance).ulgMemberOf.add(value);
        }
    }
}
