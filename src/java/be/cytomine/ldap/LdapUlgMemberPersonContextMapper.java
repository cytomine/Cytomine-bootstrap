package be.cytomine.ldap;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;
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
