package be.cytomine.test.http

import be.cytomine.image.UploadedFile
import be.cytomine.project.Discipline
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Discipline to Cytomine with HTTP request during functional test
 */
class UploadedFileAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/uploadedfile/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/uploadedfile.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/uploadedfile.json"
        def result = doPOST(URL, json,username, password)
        Long idUploadedFile = JSON.parse(result.data)?.uploadedfile?.id
        return [data: UploadedFile.get(idUploadedFile), code: result.code]
    }

    static def update(def id, def jsonUploadedFile, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/uploadedfile/" + id + ".json"
        return doPUT(URL,jsonUploadedFile,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/uploadedfile/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
