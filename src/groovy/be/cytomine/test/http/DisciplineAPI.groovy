package be.cytomine.test.http

import be.cytomine.project.Discipline
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 *
 */
class DisciplineAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        log.info "show discipline $id"
        String URL = Infos.CYTOMINEURL + "api/discipline/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def list(String username, String password) {
        log.info "list discipline"
        String URL = Infos.CYTOMINEURL + "api/discipline.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def create(Discipline disciplineToAdd, User user) {
       create(disciplineToAdd.encodeAsJSON(),user.username,user.password)
    }


    static def create(Discipline disciplineToAdd, String username, String password) {
        return create(disciplineToAdd.encodeAsJSON(), username, password)
    }

    static def create(String json, User user) {
        create(json,user.username,user.password)
    }

    static def create(String json, String username, String password) {
        log.info "create discipline"
        String URL = Infos.CYTOMINEURL + "api/discipline.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(json)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        def jsonResponse = JSON.parse(response)
        Long idDiscipline = jsonResponse?.discipline?.id
        return [data: Discipline.get(idDiscipline), code: code]
    }

    static def update(Discipline discipline, String username, String password) {
        log.info "update discipline"
        String oldName = "NAME1"
         String newName = "NAME2"
         def mapNew = ["name":newName]
         def mapOld = ["name":oldName]
         /* Create a Name1 discipline */
         Discipline disciplineToAdd = BasicInstance.createOrGetBasicDiscipline()
         disciplineToAdd.name = oldName
         assert (disciplineToAdd.save(flush:true) != null)
         /* Encode a niew discipline Name2*/
         Discipline disciplineToEdit = Discipline.get(disciplineToAdd.id)
         def jsonDiscipline = disciplineToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonDiscipline)
         jsonUpdate.name = newName
         jsonDiscipline = jsonUpdate.encodeAsJSON()
        def data = update(discipline.id, jsonDiscipline, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def update(def id, def jsonDiscipline, String username, String password) {
        log.info "delete discipline"
        String URL = Infos.CYTOMINEURL + "api/discipline/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.put(jsonDiscipline)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        return [data: response, code: code]
    }

    static def delete(def id, String username, String password) {
        log.info "delete discipline"
        String URL = Infos.CYTOMINEURL + "api/discipline/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
