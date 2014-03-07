package be.cytomine.test.http

import be.cytomine.processing.Job
import be.cytomine.processing.JobTemplate
import be.cytomine.processing.JobTemplateAnnotation
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage ImageInstance to Cytomine with HTTP request during functional test
 */
class JobTemplateAnnotationAPI extends DomainAPI {

    static def list(Long idTemplate, Long idAnnotation, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobtemplateannotation.json?" + (idTemplate? "&template=$idTemplate":"") + (idAnnotation? "&annotation=$idAnnotation":"")
        return doGET(URL, username, password)
    }

    static def show(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobtemplateannotation/${id}.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobtemplateannotation.json"
        def result = doPOST(URL,json,username,password)
        result.job = Job.get(JSON.parse(result.data)?.job?.id)
        result.data = JobTemplateAnnotation.get(JSON.parse(result.data)?.jobtemplateannotation?.id)
        return result
    }

    static def delete(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobtemplateannotation/${id}.json"
        return doDELETE(URL,username,password)
    }

}
