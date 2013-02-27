package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.SecurityCheck
import be.cytomine.command.*
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import grails.converters.JSON

class AnnotationPropertyService extends ModelService {

    static transactional = true
    def cytomineService
    def transactionService

    def currentDomain() {
        return AnnotationProperty;
    }

    def list() {
        AnnotationProperty.list()
    }

    def list(AnnotationDomain annotation) {
        AnnotationProperty.findAllByAnnotationIdent(annotation.id)
    }

    private List<String> listKeys(Project project, ImageInstance image) {
        //def keys = []
        //def listAllAnnotations = []

        //Requete SQL
        if (project) {

            return AnnotationProperty.executeQuery(
                    "SELECT ap.key " +
                    "FROM AnnotationProperty as ap, UserAnnotation as ua " +
                    "WHERE ap.annotationIdent = ua.id " +
                    (project? "AND ua.project.id = '"+ project.id + "' " : "") +
                    (image? "AND ua.image.id = '"+ image.id + "' " : "") +
                    "UNION " +
                    "SELECT ap1.key " +
                    "FROM AnnotationProperty as ap1, AlgoAnnotation as aa " +
                    "WHERE ap1.annotationIdent = aa.id " +
                    "AND aa.project.id = '"+ project.id + "' " +
                    "UNION " +
                    "SELECT ap2.key " +
                    "FROM AnnotationProperty as ap2, ReviewedAnnotation as ra " +
                    "WHERE ap2.annotationIdent = ra.id " +
                    "AND ra.project.id = '"+ project.id + "'")
        } else {
            return AnnotationProperty.executeQuery(
                    "SELECT ap.key " +
                    "FROM AnnotationProperty as ap, UserAnnotation as ua " +
                    "WHERE ap.annotationIdent = ua.id " +
                    "AND ua.image.id = '"+ image.id + "' " +
                    "UNION " +
                    "SELECT ap1.key " +
                    "FROM AnnotationProperty as ap1, AlgoAnnotation as aa " +
                    "WHERE ap1.annotationIdent = aa.id " +
                    "AND aa.image.id = '"+ image.id + "' " +
                    "UNION " +
                    "SELECT ap2.key " +
                    "FROM AnnotationProperty as ap2, ReviewedAnnotation as ra " +
                    "WHERE ap2.annotationIdent = ra.id " +
                    "AND ra.image.id = '"+ image.id + "' ")
        }

        //Chopper les AP en fct des argument & Ajout dans Keys
        /*if (project)
        {
            List<UserAnnotation> userAnnotations = UserAnnotation.findAllByProject(project)
            List<AlgoAnnotation> algoAnnotations = AlgoAnnotation.findAllByProject(project)
            List<ReviewedAnnotation> reviewedAnnotations = ReviewedAnnotation.findAllByProject(project)

            //merge in listAllAnnotations
            listAllAnnotations = userAnnotations + algoAnnotations + reviewedAnnotations
        } else
        {
            List<UserAnnotation> userAnnotations = UserAnnotation.findAllByImage(image)
            List<AlgoAnnotation> algoAnnotations = AlgoAnnotation.findAllByImage(image)
            List<ReviewedAnnotation> reviewedAnnotations = ReviewedAnnotation.findAllByImage(image)

            //merge in listAllAnnotations
            listAllAnnotations = userAnnotations + algoAnnotations + reviewedAnnotations
        }


        for (annotation in listAllAnnotations) {
            List<AnnotationProperty> annotationProperties = AnnotationProperty.findAllByAnnotationIdent(annotation.id)
            annotationProperties.each {
                keys.addAll(it.key)
            }

        }

        //Unique
        keys = keys.unique()

        keys*/
    }

    def read(def id) {
        def annotationProperty = AnnotationProperty.read(id)
        annotationProperty
    }

    def get(def id) {
        def annotationProperty = AnnotationProperty.get(id)
        annotationProperty
    }

    def read(AnnotationDomain annotation, String key) {
        AnnotationProperty.findByAnnotationIdentAndKey(annotation.id,key)
    }

    def add(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        Command command = new AddCommand(user: currentUser)
        return executeCommand(command, json)
    }

    def update(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        Command command = new EditCommand(user: currentUser)
        return executeCommand(command, json)
    }

    def delete (def json, SecurityCheck security, Task task = null) throws CytomineException {
        return delete(retrieve(json),transactionService.start(),true)
    }

    def delete(AnnotationProperty annotationProperty, Transaction transaction = null, boolean printMessage = true, Task task = null) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json =  JSON.parse("{id : ${annotationProperty.id}}")
        Command command = new DeleteCommand(user: currentUser, transaction:transaction)
        return executeCommand(command,json,task)
    }

    def getStringParamsI18n(def domain) {
        return [domain.key, domain.annotationIdent]
    }

}
