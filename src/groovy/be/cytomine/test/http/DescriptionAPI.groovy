package be.cytomine.test.http

import be.cytomine.ontology.AnnotationFilter
import be.cytomine.test.Infos
import be.cytomine.utils.Description
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Discipline to Cytomine with HTTP request during functional test
 */
class DescriptionAPI extends DomainAPI {

    static def show(Long id, String className, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/domain/${className.replace(".","_")}/$id/description.json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/description.json"
        return doGET(URL, username, password)
    }

    static def create(Long id, String className,String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/domain/${className.replace(".","_")}/$id/description.json"
        def result = doPOST(URL, json,username, password)
        Long descr = JSON.parse(result.data)?.description?.id
        return [data: Description.get(descr), code: result.code]
    }

    static def update(Long id, String className, def json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/domain/${className.replace(".","_")}/$id/description.json"
        return doPUT(URL,json,username,password)
    }

    static def delete(Long id, String className, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/domain/${className.replace(".","_")}/$id/description.json"
        return doDELETE(URL,username,password)
    }
}
