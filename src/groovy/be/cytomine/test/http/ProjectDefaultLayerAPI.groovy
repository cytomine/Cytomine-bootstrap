package be.cytomine.test.http

import be.cytomine.project.ProjectDefaultLayer
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * Created by hoyoux on 13.11.14.
 */
class ProjectDefaultLayerAPI extends DomainAPI {

    static def show(Long id, Long idProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/" + idProject + "/defaultlayer/"+id+".json"
        return doGET(URL, username, password)
    }

    static def list(Long idProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/" + idProject + "/defaultlayer.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/" + JSON.parse(json)["project"] + "/defaultlayer.json"
        def result = doPOST(URL,json,username,password)
        result.data = ProjectDefaultLayer.get(JSON.parse(result.data)?.projectdefaultlayer?.id)
        return result
    }

    static def update(def id, def json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/" + JSON.parse(json)["project"] + "/defaultlayer/"+id+".json"
        return doPUT(URL,json,username,password)
    }

    static def delete(def id, Long idProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/" + idProject + "/defaultlayer/"+id+".json"
        return doDELETE(URL,username,password)
    }
}
