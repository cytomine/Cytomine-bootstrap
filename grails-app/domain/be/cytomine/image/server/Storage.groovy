package be.cytomine.image.server

import be.cytomine.CytomineDomain
import be.cytomine.security.SecUser
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * A storage is a remote file repository
 * It contains the network configuration, the credentials
 * and the remote path of the remote machine
 */
class Storage extends CytomineDomain {

    String name
    String basePath

    SecUser user //the creator, who got rights administration on the domain

    String ip
    String username
    String password
    String keyFile
    Integer port

    String newUsername
    String newPassword
    String newKeyFile


    static constraints = {
        name(unique: false)
        basePath(nullable: false, blank: false)
        username(nullable: true)
        password(nullable: true)
        keyFile(nullable : true)
        newUsername(nullable : true, blank : false)
        newPassword(nullable : true, blank : false)
        newKeyFile(nullable : true, blank : false)
    }

    static transients = ['newUsername', 'newPassword', 'newKeyFile']


    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static def getDataFromDomain(def storage) {
        def returnArray = CytomineDomain.getDataFromDomain(storage)
        returnArray['name'] = storage?.name
        returnArray['basePath'] = storage?.basePath
        returnArray['user'] = storage?.user?.id
        returnArray['ip'] = storage?.ip
        returnArray['port'] = storage?.port
        returnArray['username'] = storage?.username
        returnArray
    }

    def beforeUpdate() {
        super.beforeUpdate()
        if (newUsername) {
            username = newUsername
        }
        if (newPassword) {
            password = newPassword
        }
        if (newKeyFile) {
            keyFile = newKeyFile
        }
    }

    public boolean equals(Object o) {
        if (!o) {
            return false
        } else {
            try {
                return ((Storage) o).getId() == this.getId()
            } catch (Exception e) {
                return false
            }
        }

    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Storage insertDataIntoDomain(def json,def domain = new Storage()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.name = JSONUtils.getJSONAttrStr(json, 'name',true)
        domain.basePath = JSONUtils.getJSONAttrStr(json, 'basePath',true)
        domain.ip = JSONUtils.getJSONAttrStr(json, 'ip',true)
        domain.port = JSONUtils.getJSONAttrInteger(json, 'port', 22)
        domain.created = JSONUtils.getJSONAttrDate(json, 'created')
        domain.updated = JSONUtils.getJSONAttrDate(json, 'updated')
        domain.user = JSONUtils.getJSONAttrDomain(json, "user", new SecUser(), false)
        if (json.username && domain.username != null) {
            domain.newUsername = JSONUtils.getJSONAttrStr(json,'username') //storage is updated
        } else if (json.keyFile) {
            domain.username = JSONUtils.getJSONAttrStr(json,'username') //storage is created
        }
        if (json.password && domain.password != null) {
            domain.newPassword = JSONUtils.getJSONAttrStr(json,'password') //storage is updated
        } else if (json.password) {
            domain.password = JSONUtils.getJSONAttrStr(json,'password') //storage is created
        }
        if (json.keyFile && domain.keyFile != null) {
            domain.newKeyFile = JSONUtils.getJSONAttrStr(json,'keyFile') //storage is updated
        } else if (json.keyFile) {
            domain.password = JSONUtils.getJSONAttrStr(json,'keyFile') //storage is created
        }
        return domain
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return this;
    }




}
