package be.cytomine.security

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

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObjectFields

/**
 * Cytomine user.
 * Its the parent class for "user" (human) and "user job" (algo).
 */
//@RestApiObject(name = "user")
class SecUser extends CytomineDomain implements Serializable {

    @RestApiObjectField(description = "The username of the user")
    String username

    @RestApiObjectField(description = "The user password", presentInResponse = false)
    String password
    String newPassword = null

    @RestApiObjectField(description = "The user public key", mandatory = false, defaultValue = "A generated key")
    String publicKey

    @RestApiObjectField(description = "The user private key", mandatory = false, defaultValue = "A generated key")
    String privateKey

    @RestApiObjectField(description = "If true, account is enabled", useForCreation = false,presentInResponse = false)
    boolean enabled

    @RestApiObjectField(description = "If true, account is expired", useForCreation = false,presentInResponse = false)
    boolean accountExpired

    @RestApiObjectField(description = "If true, account is locked",useForCreation = false,presentInResponse = false)
    boolean accountLocked

    @RestApiObjectField(description = "If true, password is expired",useForCreation = false,presentInResponse = false)
    boolean passwordExpired


    @RestApiObjectFields(params=[
        @RestApiObjectField(apiFieldName = "algo", description = "If true, user is a userjob",allowedType = "boolean",useForCreation = false)
    ])
    static transients = ["newPassword", "currentTransaction", "nextTransaction"]

    static constraints = {
        username blank: false, unique: true
        password blank: false
        newPassword(nullable : true, blank : false)
        publicKey (nullable : true, blank : false)
        privateKey (nullable : true, blank : false)
        id unique: true
    }

    static mapping = {
        password column: '`password`'
        id(generator: 'assigned', unique: true)
        sort "id"
        cache true
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['username'] = domain?.username
        returnArray['algo'] = domain?.algo()
        returnArray
    }

    def beforeInsert() {
        super.beforeInsert()
        encodePassword()
        generateKeys()
    }

    def beforeUpdate() {
        super.beforeUpdate()
        if (newPassword) {
            password = newPassword
            passwordExpired = false
            encodePassword()
        }
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        SecUser.withNewSession {
            SecUser user = SecUser.findByUsername(username)
            if(user && (user.id!=id)) {
                throw new AlreadyExistException("User "+username + " already exist!")
            }
        }
    }

    /**
     * Get user roles
     */
//    Set<SecRole> getAuthorities() {
//        SecUserSecRole.findAllBySecUser(this).collect { it.secRole } as Set
//    }

    /**
     * Check if user is a cytomine admin
     * Rem: a project admin is not a cytomine admin
     */

//    boolean isAdminAuth() {
//        return (SecUserSecRole.get(id,SecRole.findByAuthority("ROLE_ADMIN").id) != null)
//    }
//
//    boolean isUserAuth() {
//        return (SecUserSecRole.get(id,SecRole.findByAuthority("ROLE_USER").id) != null)
//    }
//
//    boolean isGuestAuth() {
//        return (SecUserSecRole.get(id,SecRole.findByAuthority("ROLE_GUEST").id) != null)
//    }

    /**
     * Username of the human user back to this user
     * If User => humanUsername is username
     * If Algo => humanUsername is user that launch algo username
     */
    String humanUsername() {
        return username
    }

    /**
     * Generate public/privateKey for user authentification
     */
    def generateKeys() {
        String privateKey = UUID.randomUUID().toString()
        String publicKey = UUID.randomUUID().toString()
        this.setPrivateKey(privateKey)
        this.setPublicKey(publicKey)
    }

    /**
     * Check if user is an algo (otherwise its an human)
     */
    boolean algo() {
        return false
    }

    String toString() {
        return username
    }

    protected void encodePassword() {
        log.info "encodePassword for user="+username
        if(password.size()<4) throw new WrongArgumentException("Your password must have at least 4 characters!")
        if (password == "") password = ".+7dWl_=]@8%,<&"
        password = springSecurityService.encodePassword(password)
    }

    /**
     * Return domain user (annotation user, image user...)
     * By default, a domain has no user.
     * You need to override userDomainCreator() in domain class
     * @return Domain user
     */
    public SecUser userDomainCreator() {
        return this;
    }
}
