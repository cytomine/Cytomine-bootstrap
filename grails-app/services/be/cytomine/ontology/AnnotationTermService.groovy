package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.utils.ModelService
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
import be.cytomine.security.SecUser
import be.cytomine.security.User
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize

class AnnotationTermService extends ModelService {

    static transactional = true
    def cytomineService
    def transactionService
    def commandService
    def responseService
    def modelService

    boolean saveOnUndoRedoStack = true

    @PreAuthorize("#userAnnotation.hasPermission(#userAnnotation.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(UserAnnotation userAnnotation) {
        userAnnotation.annotationTerm
    }

    @PreAuthorize("#userAnnotation.hasPermission(#userAnnotation.project,'READ') or hasRole('ROLE_ADMIN')")
    def listNotUser(UserAnnotation userAnnotation, User user) {
        AnnotationTerm.findAllByUserAnnotationAndUserNotEqual(userAnnotation, user)
    }

    @PreAuthorize("#annotation.hasPermission(#annotation.project,'READ') or hasRole('ROLE_ADMIN')")
    def read(AnnotationDomain annotation, Term term, SecUser user) {
        if (user) {
            AnnotationTerm.findWhere('userAnnotation.id': annotation.id, 'term': term, 'user': user)
        } else {
            AnnotationTerm.findWhere('userAnnotation.id': annotation.id, 'term': term)
        }
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkProjectAccess() or hasRole('ROLE_ADMIN')")
    def add(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecUser creator = SecUser.read(json.user)
        if (!creator)
            json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkCurrentUserCreator(principal.id) or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return deleteAnnotationTerm(json.userannotation, json.term, json.user, currentUser, true, null)
    }


    def addAnnotationTerm(def idUserAnnotation, def idTerm, def idExpectedTerm, def idUser, SecUser currentUser, Transaction transaction) {
        def json = JSON.parse("{userannotation: $idUserAnnotation, term: $idTerm, expectedTerm: $idExpectedTerm, user: $idUser}")
        return executeCommand(new AddCommand(user: currentUser, transaction: transaction), json)
    }

    /**
     * Add annotation-term for an annotation and delete all annotation-term that where already map with this annotation by this user
     */
    def addWithDeletingOldTerm(def idAnnotation, def idterm) {
        SecUser currentUser = cytomineService.getCurrentUser()
        UserAnnotation annotation = UserAnnotation.read(idAnnotation)
        if (!annotation) throw new ObjectNotFoundException("Annotation $idAnnotation not found")
        //Start transaction
        Transaction transaction = transactionService.start()

        //Delete all annotation term
        deleteAnnotationTermFromUser(annotation, currentUser, currentUser, transaction)

        //Add annotation term
        def result = addAnnotationTerm(idAnnotation, idterm, null, currentUser.id, currentUser, transaction)

        //Stop transaction
        transactionService.stop()

        return result
    }

    /**
     * Delete annotation-term
     * This method should delete all domain linked with annotation-term
     */
    def deleteAnnotationTerm(def idAnnotation, def idTerm, def idUser, User currentUser, boolean printMessage, Transaction transaction) {
        def json = JSON.parse("{userannotation: $idAnnotation, term: $idTerm, user: $idUser}")
        def result = executeCommand(new DeleteCommand(user: currentUser, transaction: transaction), json)
        return result
    }

    /**
     * Delete all term linked by user for this annotation
     */
    def deleteAnnotationTermFromUser(UserAnnotation annotation, User user, User currentUser, Transaction transaction) {
        //Delete all annotation term
        def annotationTerm = AnnotationTerm.findAllByUserAnnotationAndUser(annotation, user)
        log.info "Delete old annotationTerm= " + annotationTerm.size()
        annotationTerm.each { annotterm ->
            log.info "unlink annotterm:" + annotterm.id
            deleteAnnotationTerm(annotterm.userAnnotation.id, annotterm.term.id, annotterm.user.id, currentUser, false, transaction)
        }
    }

    /**
     * Delete all term linked with this annotation
     */
    def deleteAnnotationTermFromAllUser(UserAnnotation annotation, User currentUser, Transaction transaction) {
        //Delete all annotation term
        def annotationTerms = AnnotationTerm.findAllByUserAnnotation(annotation)
        log.info "Delete old annotationTerm= " + annotationTerms.size()
        annotationTerms.each { annotationTerm ->
            log.info "unlink annotterm:" + annotationTerm.id
            deleteAnnotationTerm(annotationTerm.userAnnotation.id, annotationTerm.term.id, annotationTerm.user.id, currentUser, false, transaction)
        }
    }

    /**
     * Delete all term linked by user for this term
     */
    def deleteAnnotationTermFromAllUser(Term term, User currentUser, Transaction transaction) {
        //Delete all annotation term
        def annotationTerm = AnnotationTerm.findAllByTerm(term)
        log.info "Delete old annotationTerm= " + annotationTerm.size()
        annotationTerm.each { annotterm ->
            log.info "unlink annotterm:" + annotterm.id
            deleteAnnotationTerm(annotterm.userAnnotation.id, annotterm.term.id, annotterm.user.id, currentUser, false, transaction)
        }
    }

    /**
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    def create(JSONObject json, boolean printMessage) {
        create(AnnotationTerm.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(AnnotationTerm domain, boolean printMessage) {
        //Build response message
        log.debug "domain=" + domain + " responseService=" + responseService
        //Save new object
        //modelService.saveDomain(domain)
        domain = AnnotationTerm.link(domain.userAnnotation, domain.term, domain.user)

        def response = responseService.createResponseMessage(domain, [domain.id, domain.userAnnotation.id, domain.term.name, domain.user?.username], printMessage, "Add", domain.getCallBack())

        return response
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(def json, boolean printMessage) {
        destroy(AnnotationTerm.createFromData(json), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(AnnotationTerm domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.userAnnotation.id, domain.term.name, domain.user?.username], printMessage, "Delete", domain.getCallBack())
        //Delete new object
        AnnotationTerm.unlink(domain.userAnnotation, domain.term, domain.user)
        //deleteDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    AnnotationTerm createFromJSON(def json) {
        return AnnotationTerm.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(def json) {
        UserAnnotation annotation = UserAnnotation.get(json.userannotation)
        Term term = Term.get(json.term)
        User user = User.get(json.user)
        AnnotationTerm relation = AnnotationTerm.findWhere(userAnnotation: annotation, 'term': term, 'user': user)
        if (!relation) {
            throw new ObjectNotFoundException("Annotation term not found ($annotation,$term,$user)")
        }
        return relation
    }
}
