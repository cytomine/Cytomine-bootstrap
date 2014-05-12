package be.cytomine.test.http

import be.cytomine.security.User
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONElement

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage User to Cytomine with HTTP request during functional test
 */
class UserAPI extends DomainAPI {

    static def showCurrent(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/current.json"
        return doGET(URL, username, password)
    }

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def show(String id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def keys(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/" + id + "/keys.json"
        return doGET(URL, username, password)
    }

    static def signature(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/signature.json"
        return doGET(URL, username, password)
    }

    static def showUserJob(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userJob/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        list(null,username,password)
    }

    static def list(String key,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user.json" + (key? "?publicKey=$key":"")
        return doGET(URL, username, password)
    }

    static def list(Long id,String domain,String type,String username, String password) {
        list(id,domain,type,false,username,password)
    }

    static def list(Long id,String domain,String type,def offline,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/${domain}/$id/${type}.json" + (offline? "?offline=true":"")
        return doGET(URL, username, password)
    }

    static def listFriends(Long id,def offline, Long idProject,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/$id/friends.json?offline=" + (offline? "true":"false") + (idProject? "&project=${idProject}":"")
        return doGET(URL, username, password)
    }

    static def listOnline(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "/api/project/$id/online/user.json"
        return doGET(URL, username, password)
    }

    static def listUserJob(Long id,Boolean tree, Long idImage,String username, String password) {
        String URL = Infos.CYTOMINEURL + "/api/project/$id/userjob.json?tree="+(tree?"true":false)+(idImage?"&image=$idImage":"")
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        JSONElement jsonWithPassword = JSON.parse(json)
        if(jsonWithPassword.password==null || jsonWithPassword.password.toString()=="null") {
            jsonWithPassword.password = "toto"
            jsonWithPassword.oldPassword = password
        }
        String URL = Infos.CYTOMINEURL + "api/user.json"
        def result = doPOST(URL,jsonWithPassword.toString(),username,password)
        result.data = User.get(JSON.parse(result.data)?.user?.id)
        return result
    }

    static def update(def id, def jsonUser, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/" + id + ".json"
        return doPUT(URL,jsonUser,username,password)
    }
    static def resetPassword(def id,def newPassword, String username, String password) {
        def queryString = ""
        if (newPassword) {
            queryString = "password=$newPassword&oldPassword=$password"
        }
        String URL = Infos.CYTOMINEURL + "api/user/$id/password.json?$queryString"
        return doPUT(URL,"",username,password)
    }


    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/" + id + ".json"
        return doDELETE(URL,username,password)
    }

    static def listLayers(Long idProject,String username, String password) {
        String URL = Infos.CYTOMINEURL + "/api/project/$idProject/userlayer.json"
        return doGET(URL, username, password)
    }

    static def switchUser(String usernameToSwitch,String username, String password) {
        String URL = Infos.CYTOMINEURL + "j_spring_security_switch_user"
        return doPOST(URL, 'j_username: '+usernameToSwitch,username, password)
    }
}
