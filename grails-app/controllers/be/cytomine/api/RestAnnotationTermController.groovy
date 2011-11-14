package be.cytomine.api

import grails.converters.*
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.command.annotationterm.AddAnnotationTermCommand
import be.cytomine.command.annotationterm.DeleteAnnotationTermCommand
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Term
import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.image.ImageInstance
import be.cytomine.command.TransactionController
import java.util.Map.Entry

class RestAnnotationTermController extends RestController {

  def springSecurityService

  def listTermByAnnotation = {

    log.info "listByAnnotation with idAnnotation=" + params.idannotation
    if(params.idannotation=="undefined") responseNotFound("Annotation Term","Annotation", params.idannotation)
    else
    {
        Annotation annotation =  Annotation.read(params.idannotation)
        if(annotation!=null && !params.idUser) responseSuccess(annotation.annotationTerm)
        else if(annotation!=null && params.idUser) {
            User user = User.read(params.idUser)
            if(user)  {
                responseSuccess(AnnotationTerm.findAllByUserAndAnnotation(user,annotation).collect{it.term.id})
            }
            else responseNotFound("Annotation Term","User", params.idUser)
        }
        else responseNotFound("Annotation Term","Annotation", params.idannotation)
    }

  }




//  def usersIdByTerm(Annotation annotation) {
//
//        Map<Term,List<User>> termsAnnotation = new HashMap<Term,List<User>>()
//
//        annotation.annotationTerm.each{ annotationTerm ->
//            if(termsAnnotation.containsKey(annotationTerm.term)) {
//                //if user is already there, add term to the list
//               List<User> users = termsAnnotation.get(annotationTerm.term)
//               users.add(annotationTerm.user)
//               termsAnnotation.put(annotationTerm.term,users)
//            } else {
//                //if user is not there create list with term id
//               List<User> users = new ArrayList<User>();
//               users.add(annotationTerm.user)
//               termsAnnotation.put(annotationTerm.term,users)
//            }
//        }
//        //if termsAnnotation is converte in marshalled  => ["idterm1": [user1, user2...],...] BAD FORMAT
//          def results = []
//            Iterator<Entry<Term, List<User>>> it = termsAnnotation.entrySet().iterator();
//            while (it.hasNext()) {
//                def subresult = [:]
//                Entry<Term, List<User>> pairs = it.next();
//
//                subresult.term = pairs.getKey()
//                subresult.user = pairs.getValue().collect{it.id}
//
//                results << subresult
//
//
//                it.remove(); // avoids a ConcurrentModificationException
//            }
//        results
//  }




  def listAnnotationTermByUser = {
    log.info "listByAnnotation with idAnnotation=" + params.idannotation + " idNotUser=" + params.idNotUser
    if(params.idannotation=="undefined") responseNotFound("Annotation Term","Annotation", params.idannotation)
    else
    {
        Annotation annotation =  Annotation.read(params.idannotation)
        if(annotation!=null && params.idNotUser) {
            User user = User.read(params.idNotUser)
            if(user)  {
                def annotationterms = AnnotationTerm.findAllByAnnotationAndUserNotEqual(annotation,user)
                responseSuccess(annotationterms)
            }
            else responseNotFound("Annotation Term","User", params.idUser)
        }
    }
  }


  def listTermByAnnotationAndOntology = {
    log.info "listTermByAnnotationAndOntology with idAnnotation=" + params.idannotation + " and idOntology=" + params.idontology
    if(params.idannotation=="undefined") responseNotFound("Annotation Term","Annotation", params.idannotation)
    else
    {
      Annotation annotation =  Annotation.read(params.idannotation)
      Ontology ontology = Ontology.read(params.idontology)
      if(annotation!=null && ontology!=null)
      {
        def termsOntology = []
        def terms = annotation.terms()

        terms.each { term ->
          if(term.ontology.id==ontology.id)
          {
            termsOntology << term
          }
        }
        responseSuccess(termsOntology)
      }
      else responseNotFound("Annotation Term","Annotation", params.idannotation)
    }

  }

  def listAnnotationByTerm = {
    log.info "listByTerm with idTerm=" +  params.idterm
    Term term = Term.read(params.idterm)

    if(term!=null) {
        responseSuccess(term.annotations())
    }
    else responseNotFound("Annotation Term","Term", params.idterm)
  }

  def listAnnotationByProjectAndTerm = {
    log.info "listByTerm with idTerm=" +  params.idterm
    Term term = Term.read(params.idterm)
    Project project = Project.read(params.idproject)
    List<User> userList = project.users()

      if(params.users) {
            String[] paramsIdUser = params.users.split("_")
            List<User> userListTemp = new ArrayList<User>()
            userList.each { user ->
                if(Arrays.asList(paramsIdUser).contains(user.id+"")) userListTemp.push(user);
            }
            userList = userListTemp;
        }
      log.info "List by idTerm " + term.id + " with user:"+ userList


    if(term==null) responseNotFound("Term", params.idterm)
    if(project==null) responseNotFound("Project", params.idproject)
    def annotationFromTermAndProject = []
    def annotationFromTerm = term.annotations()
    annotationFromTerm.each { annotation ->
      if(annotation.project()!=null && annotation.project().id == project.id && userList.contains(annotation.user))
        annotationFromTermAndProject << annotation
    }
    responseSuccess(annotationFromTermAndProject)
  }

  def listAnnotationByProjectAndImageInstance = {
      log.info "listByTerm with idTerm=" +  params.idterm + " and imageinstance=" +  params.idimageinstance
      Term term = Term.read(params.idterm)
      def annotations = []
      Annotation.findAllByImage(ImageInstance.read(params.idimageinstance)).each { annotation ->
        annotation.annotationTerm.each { annotationTerm->
            if (annotationTerm.getTerm() == term) annotations << annotation
        }
      }
      responseSuccess(annotations)
  }



    //idUser
  def show = {
    log.info "listByTerm with idTerm=" +  params.idterm + " idAnnotation=" + params.idannotation + " idUser="+params.idUser

    Annotation annotation = Annotation.read(params.idannotation)
    Term term = Term.read(params.idterm)

    if(params.idUser) {
        User user = User.read(params.idUser)
        if(annotation!=null && term!=null && user!=null && AnnotationTerm.findWhere('annotation':annotation,'term':term,'user':user)!=null)
            responseSuccess(AnnotationTerm.findWhere('annotation':annotation,'term':term,'user':user))
        else  responseNotFound("Annotation Term","Term","Annotation","User", params.idterm,  params.idannotation,params.idUser)
    } else {
        if(annotation!=null && term!=null && AnnotationTerm.findByAnnotationAndTerm(annotation,term)!=null)
            responseSuccess(AnnotationTerm.findByAnnotationAndTerm(annotation,term))
        else  responseNotFound("Annotation Term","Term","Annotation", params.idterm,  params.idannotation)
    }
  }


  def add = {
    log.info "Add"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username +" transaction:" +  currentUser.transactionInProgress + " request:" + request.JSON.toString()

    def json = JSON.parse(request.JSON.toString())
    json.user = currentUser.id

    Command addAnnotationTermCommand = new AddAnnotationTermCommand(postData : json.toString(),user: currentUser)
    def result = processCommand(addAnnotationTermCommand, currentUser)
    response(result)
  }

  //Add annotation-term for an annotation and delete all annotation-term that where already map with this annotation by this user
  def addWithDeletingOldTerm = {
    log.info "Add"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username +" transaction:" +  currentUser.transactionInProgress + " request:" + request.JSON.toString()

    Annotation annotation = Annotation.get(params.idannotation)
    if(annotation) {
        log.info "Start transaction"
        TransactionController transaction = new TransactionController();
        transaction.start()


        def annotationTerm = AnnotationTerm.findAllByAnnotationAndUser(annotation,currentUser)
        log.info "Delete old annotationTerm= " +annotationTerm.size()

        annotationTerm.each{ annotterm ->
            log.info "unlink annotterm:" +annotterm.id
            def postDataRT = ([term: annotterm.term.id,annotation: annotterm.annotation.id,user:annotterm.user.id]) as JSON
            Command deleteAnnotationTermCommand = new DeleteAnnotationTermCommand(postData :postDataRT.toString() ,user: currentUser,printMessage:false)
            def result = processCommand(deleteAnnotationTermCommand, currentUser)
        }

        log.info "Add new annotationTerm with Annotation="  + params.idannotation + " Term=" + params.idterm
        def postData = ([annotation : params.idannotation,term :params.idterm]) as JSON
        Command addAnnotationTermCommand = new AddAnnotationTermCommand(postData : postData.toString(),user: currentUser)
        def result = processCommand(addAnnotationTermCommand, currentUser)
        transaction.stop()
        response(result)

    }
    else responseNotFound("Annotation",params.id)

  }







  def delete =  {
    log.info "Delete"
    User currentUser = getCurrentUser(springSecurityService.principal.id)
    log.info "User:" + currentUser.username + " params.idannotation=" + params.idannotation
    def postData = ([annotation : params.idannotation,term :params.idterm, user:currentUser.id]) as JSON
    Command deleteAnnotationTermCommand = new DeleteAnnotationTermCommand(postData : postData.toString(),user: currentUser)
    def result = processCommand(deleteAnnotationTermCommand, currentUser)
    response(result)
  }
}