package be.cytomine.ldap;

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
