package be.cytomine.test.http

import be.cytomine.ontology.RelationTerm
import be.cytomine.security.User
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage RelationTerm to Cytomine with HTTP request during functional test
 */
class RelationTermAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)



    static def show(Long idRelation, Long idTerm1, Long idTerm2,String username, String password) {
        log.info "Show idRelation=$idRelation and idTerm1=$idTerm1 and idTerm2=$idTerm2"
        String URL = Infos.CYTOMINEURL + "api/relation/" + idRelation + "/term1/"+ idTerm1 +"/term2/"+idTerm2+".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def list(String username, String password) {
        log.info "List by userAnnotation idRelation"
        String URL = Infos.CYTOMINEURL+"api/relation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByRelation(Long idRelation,String username, String password) {
        log.info "List by userAnnotation idRelation=$idRelation"
        String URL = Infos.CYTOMINEURL+"api/relation/"+idRelation+"/term.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByTermAll(Long idTerm,String username, String password) {
        log.info "List by userAnnotation idRelation=$idTerm"
        String URL = Infos.CYTOMINEURL+"api/relation/term/"+idTerm+".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByTerm(Long idRelation,Long indexTerm,String username, String password) {
        log.info "List by userAnnotation idRelation=$idRelation"
        String URL = Infos.CYTOMINEURL+"api/relation/term/"+indexTerm+"/" + idRelation +  ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }



    static def create(RelationTerm relationtermToAdd, User user) {
       create(relationtermToAdd.encodeAsJSON(),user.username,user.password)
    }

    static def create(RelationTerm relationtermToAdd, String username, String password) {
        return create(relationtermToAdd.encodeAsJSON(), username, password)
    }

    static def create(String jsonRelationTerm, User user) {
        create(jsonRelationTerm,user.username,user.password)
    }

    static def create(String jsonRelationTerm, String username, String password) {
        log.info("create")
        create(jsonRelationTerm,username,password,false)
    }
    
    static def create(String jsonRelationTerm, String username, String password, boolean deleteOldTerm) {
        def json = JSON.parse(jsonRelationTerm);
        String URL = Infos.CYTOMINEURL+"api/relation/"+ json.relation +"/term.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(jsonRelationTerm)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        json = JSON.parse(response)
        int idRelationTerm
        try {idRelationTerm= json?.relationterm?.id } catch(Exception e) {log.error e}
        return [data: RelationTerm.get(idRelationTerm), code: code]
    }    

    static def delete(def idRelation, def idTerm1, def idTerm2, String username, String password) {
        log.info("delete")
        String URL = Infos.CYTOMINEURL+"api/relation/"+idRelation + "/term1/"+idTerm1+"/term2/"+idTerm2+".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
