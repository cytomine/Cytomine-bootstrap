package be.cytomine.test.http

import be.cytomine.image.NestedImageInstance
import be.cytomine.processing.JobTemplate
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage ImageInstance to Cytomine with HTTP request during functional test
 */
class JobTemplateAPI extends DomainAPI {

    static def list(Long idProject, Long idSoftware, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/${idProject}/jobtemplate.json?" + (idSoftware? "&software=$idSoftware":"")
        return doGET(URL, username, password)
    }

    static def show(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobtemplate/${id}.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobtemplate.json"
        def result = doPOST(URL,json,username,password)
        result.data = JobTemplate.get(JSON.parse(result.data)?.jobtemplate?.id)
        return result
    }

    static def update(Long id, String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobtemplate/${id}.json"
        return doPUT(URL,json,username,password)
    }

    static def delete(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobtemplate/${id}.json"
        return doDELETE(URL,username,password)
    }

}
