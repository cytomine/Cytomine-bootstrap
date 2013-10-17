package be.cytomine.security

import be.cytomine.image.server.StorageService
import grails.util.Holders
import org.codehaus.groovy.grails.plugins.springsecurity.GormUserDetailsService
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.context.ApplicationContext
import org.springframework.dao.DataAccessException
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.ldap.userdetails.InetOrgPerson
import org.springframework.security.ldap.userdetails.LdapUserDetailsService

class CASLdapUserDetailsService extends GormUserDetailsService {

    /**
     * Some Spring Security classes (e.g. RoleHierarchyVoter) expect at least
     * one role, so we give a user with no granted roles this one which gets
     * past that restriction but doesn't grant anything.
     */
    static final List NO_ROLES = [new GrantedAuthorityImpl(SpringSecurityUtils.NO_ROLE)]

    LdapUserDetailsService ldapUserDetailsService


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        return loadUserByUsername(username, true)
    }


    @Override
    public UserDetails loadUserByUsername(String username, boolean loadRoles)
    throws UsernameNotFoundException, DataAccessException {

        SecUser user = SecUser.findByUsername(username)

        if (user == null) { //User does not exists in our database

            //fetch its informations through LDAP
            InetOrgPerson inetOrgPerson = (InetOrgPerson) ldapUserDetailsService.loadUserByUsername(username)

            User.withTransaction {

                //Can't get firstname with inetOrgPerson, but we've got the fullname and the lastname
                String firstname = inetOrgPerson.getCn()[0].replace(inetOrgPerson.getSn(), "")
                //remove whitespace at the beginning
                while (firstname.startsWith(" ")) {
                    firstname = firstname.substring(1)
                }

                // Create new user and save to the database
                user = new User()
                user.username = username
                user.lastname = inetOrgPerson.getSn()
                user.firstname = firstname
                user.email = inetOrgPerson.getMail()
                user.enabled = true
                user.password = "|0>%%Lyc>f(Zz!Q" //not used by the user
                user.generateKeys()
                if (user.validate()) {
                    user.save(flush: true)
                    user.refresh()

                    // Assign the default role of client
                    SecRole userRole = SecRole.findByAuthority("ROLE_USER")
                    SecUserSecRole secUsersecRole = new SecUserSecRole()
                    secUsersecRole.secUser = user
                    secUsersecRole.secRole = userRole
                    secUsersecRole.save(flush: true)

                } else {
                    println user.errors.each {
                        println it
                    }
                }
            }
        }

        //def authorities = user.getAuthorities().collect {new GrantedAuthorityImpl(it.authority)}

        return new GrailsUser(user.username, user.password, user.enabled, !user.accountExpired,
                !user.passwordExpired, !user.accountLocked,
                 NO_ROLES, user.id)
    }
}