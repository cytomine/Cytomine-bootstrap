package be.cytomine.processing

import be.cytomine.AnnotationDomain
import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task

import static org.springframework.security.acls.domain.BasePermission.READ

class JobTemplateAnnotationService extends ModelService {

    static transactional = true

     def cytomineService
     def transactionService
     def userAnnotationService
     def algoAnnotationService
     def dataSource
     def reviewedAnnotationService
     def propertyService

     def currentDomain() {
         return JobTemplateAnnotation
     }

     def read(def id) {
         def domain = JobTemplateAnnotation.read(id)
         if(domain) {
             SecurityACL.check(domain.container(),READ)
         }
         domain
     }

     def list(JobTemplate jobTemplate, Long idAnnotation) {
         if(jobTemplate && idAnnotation) {
             SecurityACL.check(jobTemplate.container(),READ)
             return JobTemplateAnnotation.findAllByJobTemplateAndAnnotationIdent(jobTemplate,idAnnotation)
         } else if(idAnnotation){
             SecurityACL.check(AnnotationDomain.getAnnotationDomain(idAnnotation).container(),READ)
             return JobTemplateAnnotation.findAllByAnnotationIdent(idAnnotation)
         } else {
             SecurityACL.check(jobTemplate.container(),READ)
             return JobTemplateAnnotation.findAllByJobTemplate(jobTemplate)
         }
     }

     /**
      * Add the new domain with JSON data
      * @param json New domain data
      * @return Response structure (created domain data,..)
      */
     def add(def json) {
         AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(json.annotationIdent)
         SecurityACL.check(annotation.project,READ)
         SecUser currentUser = cytomineService.getCurrentUser()
         json.user = currentUser.id
         Command c = new AddCommand(user: currentUser)
         executeCommand(c,null,json)
     }

     /**
      * Delete this domain
      * @param domain Domain to delete
      * @param transaction Transaction link with this command
      * @param task Task for this command
      * @param printMessage Flag if client will print or not confirm message
      * @return Response structure (code, old domain,..)
      */
     def delete(JobTemplateAnnotation domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
         SecurityACL.check(domain.container(),READ)
         SecurityACL.checkReadOnly(domain.container())
         SecUser currentUser = cytomineService.getCurrentUser()
         Command c = new DeleteCommand(user: currentUser,transaction:transaction)
         return executeCommand(c,domain,null)
     }

     def getStringParamsI18n(def domain) {
         return [domain.jobTemplate.name]
     }
}
