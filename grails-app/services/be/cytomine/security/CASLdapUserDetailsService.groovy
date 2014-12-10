package be.cytomine.security

import be.cytomine.ldap.LdapUlgMemberPerson
import grails.plugin.springsecurity.userdetails.GormUserDetailsService
import grails.plugin.springsecurity.userdetails.GrailsUser
import groovy.sql.Sql
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.dao.DataAccessException
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.ldap.userdetails.InetOrgPerson
import org.springframework.security.ldap.userdetails.LdapUserDetailsService

class CASLdapUserDetailsService extends GormUserDetailsService {

    def dataSource

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
        boolean casDisabled = grailsApplication.config.grails.plugin.springsecurity.cas.active.toString()=="false"

        def authorities = []
        if(user) {
            def auth = SecUserSecRole.findAllBySecUser(user).collect{new GrantedAuthorityImpl(it.secRole.authority)}
            //by default, we remove the role_admin for the current session
            authorities.addAll(auth.findAll{it.authority!="ROLE_ADMIN"})
        }

        if(user==null && casDisabled)  {
            log.info "return null"
            return null
        }

        if (user == null) { //User does not exists in our database

            //fetch its informations through LDAP
            LdapUlgMemberPerson inetOrgPerson = (LdapUlgMemberPerson) ldapUserDetailsService.loadUserByUsername(username)
            if(inetOrgPerson==null) return null

            User.withTransaction {


                //Can't get firstname with inetOrgPerson, but we've got the fullname and the lastname
                String firstname = inetOrgPerson.getCn()[0].replace(inetOrgPerson.getSn(), "")
                //remove whitespace at the beginning
                while (firstname.startsWith(" ")) {
                    firstname = firstname.substring(1)
                }

                if(firstname==null || firstname.trim()==""){
                    firstname="#none"
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
                    authorities << new GrantedAuthorityImpl(userRole.authority)

                } else {
                    user.errors.each {
                        log.warn it
                    }
                }
            }
        }

        return new GrailsUser(user.username, user.password, user.enabled, !user.accountExpired,
                !user.passwordExpired, !user.accountLocked,
                authorities, user.id)
    }
}