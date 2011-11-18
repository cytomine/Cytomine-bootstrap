package be.cytomine.api

import grails.converters.*
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.command.annotationterm.AddAnnotationTermCommand
import be.cytomine.command.annotationterm.DeleteAnnotationTermCommand
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Term
import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.image.ImageInstance
import be.cytomine.command.TransactionController

class RestAnnotationTermController extends RestController {

    def springSecurityService

    def listTermByAnnotation = {
        if (params.idannotation == "undefined") responseNotFound("Annotation Term", "Annotation", params.idannotation)
        else {
            Annotation annotation = Annotation.read(params.idannotation)
            if (annotation && !params.idUser) responseSuccess(annotation.annotationTerm)
            else if (annotation && params.idUser) {
                User user = User.read(params.idUser)
                if (user) responseSuccess(AnnotationTerm.findAllByUserAndAnnotation(user, annotation).collect {it.term.id})
                else responseNotFound("Annotation Term", "User", params.idUser)
            }
            else responseNotFound("Annotation Term", "Annotation", params.idannotation)
        }

    }

    def listAnnotationTermByUser = {
        if (params.idannotation == "undefined") responseNotFound("Annotation Term", "Annotation", params.idannotation)
        else {
            Annotation annotation = Annotation.read(params.idannotation)
            if (annotation != null && params.idNotUser) {
                User user = User.read(params.idNotUser)
                if (user) responseSuccess(AnnotationTerm.findAllByAnnotationAndUserNotEqual(annotation, user))
                else responseNotFound("Annotation Term", "User", params.idUser)
            }
        }
    }

    def listTermByAnnotationAndOntology = {
        if (params.idannotation == "undefined") responseNotFound("Annotation Term", "Annotation", params.idannotation)
        else {
            Annotation annotation = Annotation.read(params.idannotation)
            Ontology ontology = Ontology.read(params.idontology)
            if (annotation && ontology) {
                def termsOntology = []
                def terms = annotation.terms()

                terms.each { term ->
                    if (term.ontology.id == ontology.id)
                        termsOntology << term
                }
                responseSuccess(termsOntology)
            }
            else responseNotFound("Annotation Term", "Annotation", params.idannotation)
        }

    }

    def listAnnotationByTerm = {
        Term term = Term.read(params.idterm)
        if (term != null) responseSuccess(term.annotations())
        else responseNotFound("Annotation Term", "Term", params.idterm)
    }

    def listAnnotationByProjectAndTerm = {
        Term term = Term.read(params.idterm)
        Project project = Project.read(params.idproject)
        List<User> userList = project.users()

        if (params.users) {
            String[] paramsIdUser = params.users.split("_")
            List<User> userListTemp = new ArrayList<User>()
            userList.each { user ->
                if (Arrays.asList(paramsIdUser).contains(user.id + "")) userListTemp.push(user);
            }
            userList = userListTemp;
        }
        log.info "List by idTerm " + term.id + " with user:" + userList


        if (term == null) responseNotFound("Term", params.idterm)
        if (project == null) responseNotFound("Project", params.idproject)
        def annotationFromTermAndProject = []
        def annotationFromTerm = term.annotations()
        annotationFromTerm.each { annotation ->
            if (annotation.project() != null && annotation.project().id == project.id && userList.contains(annotation.user))
                annotationFromTermAndProject << annotation
        }
        responseSuccess(annotationFromTermAndProject)
    }

    def listAnnotationByProjectAndImageInstance = {
        Term term = Term.read(params.idterm)
        def annotations = []
        Annotation.findAllByImage(ImageInstance.read(params.idimageinstance)).each { annotation ->
            annotation.annotationTerm.each { annotationTerm ->
                if (annotationTerm.getTerm() == term) annotations << annotation
            }
        }
        responseSuccess(annotations)
    }


    def show = {
        Annotation annotation = Annotation.read(params.idannotation)
        Term term = Term.read(params.idterm)

        if (params.idUser) {
            User user = User.read(params.idUser)
            if (annotation != null && term != null && user != null && AnnotationTerm.findWhere('annotation': annotation, 'term': term, 'user': user) != null)
                responseSuccess(AnnotationTerm.findWhere('annotation': annotation, 'term': term, 'user': user))
            else responseNotFound("Annotation Term", "Term", "Annotation", "User", params.idterm, params.idannotation, params.idUser)
        } else {
            if (annotation != null && term != null && AnnotationTerm.findByAnnotationAndTerm(annotation, term) != null)
                responseSuccess(AnnotationTerm.findByAnnotationAndTerm(annotation, term))
            else responseNotFound("Annotation Term", "Term", "Annotation", params.idterm, params.idannotation)
        }
    }


    def add = {
        def json = request.JSON
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        json.user = currentUser.id
        def result = processCommand(new AddAnnotationTermCommand(user: currentUser), json)
        response(result)
    }

    def addAnnotationTerm(def idAnnotation, def idTerm, def idUser, User currentUser) {
        def json = JSON.parse("{annotation: $idAnnotation, term: $idTerm, user: $idUser}")
        def result = processCommand(new AddAnnotationTermCommand(user: currentUser), json)
        return result
    }

    /**
     * Add annotation-term for an annotation and delete all annotation-term that where already map with this annotation by this user
     */
    def addWithDeletingOldTerm = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)

        Annotation annotation = Annotation.get(params.idannotation)
        if (annotation) {

            //Start transaction
            TransactionController transaction = new TransactionController();
            transaction.start()

            //Delete all annotation term
            deleteAnnotationTermFromUser(annotation, currentUser, currentUser)

            //Delete annotation term
            def result = deleteAnnotationTerm(params.idannotation, params.idterm, currentUser.id, currentUser)

            //Stop transaction
            transaction.stop()
            response(result)

        }
        else responseNotFound("Annotation", params.id)
    }

    def delete = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = deleteAnnotationTerm(params.idannotation, params.idterm, currentUser.id, currentUser)
        response(result)
    }

    /**
     * Delete an annotation term
     */
    def deleteAnnotationTerm(def idAnnotation, def idTerm, def idUser, User currentUser) {
        return deleteAnnotationTerm(idAnnotation,idTerm,idUser,currentUser,true)
    }
    def deleteAnnotationTerm(def idAnnotation, def idTerm, def idUser, User currentUser, boolean printMessage) {
        def json = JSON.parse("{annotation: $idAnnotation, term: $idTerm, user: $idUser}")
        def result = processCommand(new DeleteAnnotationTermCommand(user: currentUser,printMessage: printMessage), json)
        return result
    }

    /**
     * Delete all term map by user for annotation
     */
    def deleteAnnotationTermFromUser(Annotation annotation, User user, User currentUser) {
        //Delete all annotation term
        def annotationTerm = AnnotationTerm.findAllByAnnotationAndUser(annotation, user)
        log.info  "Delete old annotationTerm= " + annotationTerm.size()

        annotationTerm.each { annotterm ->
            log.info  "unlink annotterm:" + annotterm.id
            deleteAnnotationTerm(annotterm.annotation.id, annotterm.term.id, annotterm.user.id, currentUser,false)
        }
    }

    /**
     * Delete all term map for annotation
     */
    def deleteAnnotationTermFromAllUser(Annotation annotation, User currentUser) {
        //Delete all annotation term
        def annotationTerm = AnnotationTerm.findAllByAnnotation(annotation)
        log.info  "Delete old annotationTerm= " + annotationTerm.size()

        annotationTerm.each { annotterm ->
            log.info  "unlink annotterm:" + annotterm.id
            deleteAnnotationTerm(annotterm.annotation.id, annotterm.term.id, annotterm.user.id, currentUser,false)
        }
    }

    /**
     * Delete all term map by user for term
     */
    def deleteAnnotationTermFromAllUser(Term term, User currentUser) {
        //Delete all annotation term
        def annotationTerm = AnnotationTerm.findAllByTerm(term)
        log.info  "Delete old annotationTerm= " + annotationTerm.size()

        annotationTerm.each { annotterm ->
            log.info  "unlink annotterm:" + annotterm.id
            deleteAnnotationTerm(annotterm.annotation.id, annotterm.term.id, annotterm.user.id, currentUser,false)
        }
    }
}