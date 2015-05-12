package be.cytomine.search

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

import be.cytomine.Exception.ObjectNotFoundException
import grails.util.Holders
import org.springframework.ldap.NameNotFoundException

import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.NamingException
import javax.naming.directory.*

/**
 * Created by hoyoux on 02.03.15.
 */
class LdapSearchService {

    enum Conjunction {
        OR, AND
    };
    private DirContext ctx;
    private Hashtable env = new Hashtable();
    private boolean ldapDisabled = Holders.getGrailsApplication().config.grails.plugin.springsecurity.ldap.active.toString()=="false"


            {
        // initialization of DirContext
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL,
                Holders.getGrailsApplication().config.grails.plugin.springsecurity.ldap.context.server +"/"+Holders.getGrailsApplication().config.grails.plugin.springsecurity.ldap.search.base);
        env.put(Context.SECURITY_PRINCIPAL,
                Holders.getGrailsApplication().config.grails.plugin.springsecurity.ldap.context.managerDn);
        env.put(Context.SECURITY_CREDENTIALS,
                Holders.getGrailsApplication().config.grails.plugin.springsecurity.ldap.context.managerPassword);
    }

    public Map<String, String> searchByUid(String uid, String... keys){
        Map<String, String> result = searchByUids([uid], keys).get(uid)
        if (result == null) throw new ObjectNotFoundException();
        return result
    }

    public Map<String, Map<String, String>> searchByUids(List<String> uids, String... keys){

        try {
            ctx = new InitialDirContext(env);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        Map<String, Map<String, String>> result = new HashMap<>();

        if (ldapDisabled) throw new ObjectNotFoundException();

        NamingEnumeration results = null;
        try {
            SearchControls controls =
                    new SearchControls();
            controls.setSearchScope(
                    SearchControls.SUBTREE_SCOPE);
            def requests = uids.collect {'uid=' + it}

            String request =  generateSearchArgument(Conjunction.OR, (String [])requests.toArray())

            results = ctx.search(
                    "", request, controls);

            while (results.hasMore()) {
                SearchResult searchResult =
                        (SearchResult) results.next();
                Attributes attributes =
                        searchResult.getAttributes();
                String uid = attributes.get("uid").get()
                Map<String, String> attrib = new HashMap<>();
                for(String key : keys) {
                    Attribute attr = attributes.get(key);
                    def values= [];
                    NamingEnumeration enumeration = attr.getAll()
                    while (enumeration.hasMore()) {
                        values << enumeration.next().toString()
                    }
                    attrib.put(key, (String[]) values.toArray());
                }
                result.put(uid, attrib);
            }

            return result;
        } catch (NameNotFoundException e) {
            // The base context was not found.
            // Just clean up and exit.
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
        }
    }

    public Map<String, String[]> searchByCn(String cn, String... keys){

        try {
            ctx = new InitialDirContext(env);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        Map<String, String[]> result = new HashMap<>();

        if (ldapDisabled) throw new ObjectNotFoundException();

        NamingEnumeration results = null;
        try {
            SearchControls controls =
                    new SearchControls();
            controls.setSearchScope(
                    SearchControls.SUBTREE_SCOPE);

            String request =  "(cn="+cn+")"

            results = ctx.search(
                    "", request, controls);


            while (results.hasMore()) {
                SearchResult searchResult =
                        (SearchResult) results.next();
                Attributes attributes =
                        searchResult.getAttributes();
                for(String key : keys) {
                    Attribute attr = attributes.get(key);
                    def values= [];
                    NamingEnumeration enumeration = attr.getAll()
                    while (enumeration.hasMore()) {
                        values << enumeration.next().toString()
                    }
                    result.put(key, (String[]) values.toArray());
                }
            }
            return result;
        } catch (NameNotFoundException e) {
            // The base context was not found.
            // Just clean up and exit.
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
        }
    }

    private String generateSearchArgument(Conjunction c, String... arguments) {
        if(arguments.length == 1) return '('+arguments[0]+')';
        String result = arguments.join(')(');
        result = '(' + result + ')' + ')'
        result = (c == Conjunction.OR ? '(|' : '(&') + result
        return result
    }
}
