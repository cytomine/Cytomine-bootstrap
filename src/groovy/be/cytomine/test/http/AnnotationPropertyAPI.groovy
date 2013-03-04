package be.cytomine.test.http

import be.cytomine.ontology.AnnotationProperty
import be.cytomine.test.Infos
import grails.converters.JSON

class AnnotationPropertyAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotationproperty/${id}.json"
        return doGET(URL, username, password)
    }

    static def listByAnnotation(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/$id/annotationproperty.json"
        return doGET(URL, username, password)
    }

    static def listKeyWithProject(Long idProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotationproperty/key.json?idProject=$idProject"
        return doGET(URL, username, password)
    }
    static def listKeyWithImage(Long idImage, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotationproperty/key.json?idImage=$idImage"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotationproperty.json"
        def result = doPOST(URL,json,username,password)
        //{"
        // message":"Project 9,384,520 (TEST1502) added",
        // "project":{"ontologyName":"LBA","class":"be.cytomine.project.Project","numberOfSlides":-1,"ontology":5500,"privateLayer":false,"retrievalProjects":[],"id":9384520,"numberOfReviewedAnnotations":0,"numberOfJobAnnotations":0,"updated":null,"created":"1360930795928","disciplineName":"CYTOLOGY","name":"TEST1502","numberOfAnnotations":0,"retrievalAllOntology":true,"discipline":26480,"numberOfImages":0,"retrievalDisable":false},
        // "callback":{"projectID":"9384520","method":"be.cytomine.AddProjectCommand"},
        // "printMessage":true}
        println "result=$result"
        result.data = AnnotationProperty.get(JSON.parse(result.data)?.annotationproperty?.id)
        return result
    }

    static def update(def id, def jsonAnnotationProperty, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotationproperty/${id}.json"
        //{
        // "message":"Project 9,384,520  (TEST1503) edited",
        // "project":{"ontologyName":"LBA","class":"be.cytomine.project.Project","numberOfSlides":-1,"ontology":5500,"privateLayer":false,"retrievalProjects":[],"id":9384520,"numberOfReviewedAnnotations":0,"numberOfJobAnnotations":0,"updated":null,"created":"1360930795928","disciplineName":"CYTOLOGY","name":"TEST1503","numberOfAnnotations":0,"retrievalAllOntology":true,"discipline":26480,"numberOfImages":0,"retrievalDisable":false},
        // "callback":{"projectID":"9384520","method":"be.cytomine.EditProjectCommand"},
        // "printMessage":true}
        return doPUT(URL,jsonAnnotationProperty,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotationproperty/${id}.json"
        return doDELETE(URL,username,password)
    }

    static def listAnnotationCenterPosition(Long idUser, Long idImage, String bbox, String key, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/$idUser/imageinstance/$idImage/annotationposition.json?bbox=$bbox,&key=$key"
        return doGET(URL, username, password)
    }

}
