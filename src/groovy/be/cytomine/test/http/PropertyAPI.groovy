package be.cytomine.test.http

import be.cytomine.ontology.Property
import be.cytomine.test.Infos
import grails.converters.JSON

class PropertyAPI extends DomainAPI {

    //SHOW
    static def show(Long id, Long idDomain, String type, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/$type/$idDomain/property/${id}.json"
        return doGET(URL, username, password)
    }

    //LISTBY...
    static def listByDomain(Long id, String type, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/$type/$id/property.json"
        return doGET(URL, username, password)
    }

    //LISTKEYFORANNOTATION
    static def listKeyWithProject(Long idProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/property/key.json?idProject=$idProject"
        return doGET(URL, username, password)
    }
    static def listKeyWithImage(Long idImage, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/property/key.json?idImage=$idImage"
        return doGET(URL, username, password)
    }

    static def listKeyForImageInstanceWithProject(Long idProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/property/key.json?idProject=$idProject"
        return doGET(URL, username, password)
    }


    //ADD
    static def create(Long idDomain, String type, String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/$type/$idDomain/property.json"
        def result = doPOST(URL,json,username,password)
        println "result=$result"
        result.data = Property.get(JSON.parse(result.data)?.property?.id)
        return result
    }

    //UPDATE
    static def update(def id, Long idDomain, String type, def jsonAnnotationProperty, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/$type/$idDomain/property/${id}.json"
        return doPUT(URL,jsonAnnotationProperty,username,password)
    }

    //DELETE
    static def delete(def id, Long idDomain, String type, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/$type/$idDomain/property/${id}.json"
        return doDELETE(URL,username,password)
    }

    //LISTANNOTATIONPOSITION
    static def listAnnotationCenterPosition(Long idUser, Long idImage, String bbox, String key, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/$idUser/imageinstance/$idImage/annotationposition.json?bbox=$bbox,&key=$key"
        return doGET(URL, username, password)
    }
}
