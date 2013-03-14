package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityACL
import be.cytomine.command.AddCommand
import be.cytomine.command.Command
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import grails.converters.JSON

import static org.springframework.security.acls.domain.BasePermission.READ

class AnnotationTermService extends ModelService {

    static transactional = true
    def cytomineService
    def transactionService
    def commandService

    def currentDomain() {
        return AnnotationTerm
    }

    def list(UserAnnotation userAnnotation) {
        SecurityACL.check(userAnnotation.container(),READ)
        AnnotationTerm.findAllByUserAnnotation(userAnnotation)
    }

    def listNotUser(UserAnnotation userAnnotation, User user) {
        SecurityACL.check(userAnnotation.container(),READ)
        AnnotationTerm.findAllByUserAnnotationAndUserNotEqual(userAnnotation, user)
    }

    def read(AnnotationDomain annotation, Term term, SecUser user) {
        SecurityACL.check(annotation.container(),READ)
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
    def add(def json) {
        SecurityACL.check(json.userannotation,UserAnnotation,"container",READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        SecUser creator = SecUser.read(json.user)
        if (!creator)
            json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser),null,json)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(AnnotationTerm domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkIsCreator(domain,currentUser)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }


    def addAnnotationTerm(def idUserAnnotation, def idTerm, def idExpectedTerm, def idUser, SecUser currentUser, Transaction transaction) {
        def json = JSON.parse("{userannotation: $idUserAnnotation, term: $idTerm, expectedTerm: $idExpectedTerm, user: $idUser}")
        return executeCommand(new AddCommand(user: currentUser, transaction: transaction), null,json)
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
        def annotationTerm = AnnotationTerm.findAllByUserAnnotationAndUser(annotation, currentUser)
        log.info "Delete old annotationTerm= " + annotationTerm.size()
        annotationTerm.each { annotterm ->
            log.info "unlink annotterm:" + annotterm.id
            this.delete(annotterm,transaction,true)
        }
        //Add annotation term
        return addAnnotationTerm(idAnnotation, idterm, null, currentUser.id, currentUser, transaction)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.userAnnotation.id, domain.term.name, domain.user?.username]
    }

    /**
      * Retrieve domain thanks to a JSON object
      * @param json JSON with new domain info
      * @return domain retrieve thanks to json
      */
    def retrieve(Map json) {
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
