package be.cytomine.image

import be.cytomine.SecurityACL
import be.cytomine.api.UrlApi
import be.cytomine.command.*
import be.cytomine.image.multidim.ImageSequence
import be.cytomine.ontology.*
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.social.UserPosition
import be.cytomine.utils.Description
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import groovy.sql.Sql
import org.hibernate.FetchMode

import static org.springframework.security.acls.domain.BasePermission.READ

/**
 * TODO:: refactor + doc!!!!!!!
 */
class NestedImageInstanceService extends ModelService {

    static transactional = true

     def cytomineService
     def transactionService
     def userAnnotationService
     def algoAnnotationService
     def dataSource
     def reviewedAnnotationService
     def imageSequenceService
     def propertyService
     def annotationIndexService

     def currentDomain() {
         return NestedImageInstance
     }

     def read(def id) {
         def image = NestedImageInstance.read(id)
         if(image) {
             SecurityACL.check(image.container(),READ)
         }
         image
     }


     def list(ImageInstance image) {
         SecurityACL.check(image.container(),READ)

         def images = NestedImageInstance.createCriteria().list {
             createAlias("baseImage", "i")
             eq("parent", image)
             order("i.created", "desc")
             fetchMode 'baseImage', FetchMode.JOIN
         }
         return images
     }

     /**
      * Add the new domain with JSON data
      * @param json New domain data
      * @return Response structure (created domain data,..)
      */
     def add(def json) {
         SecurityACL.check(json.project,Project,READ)
         SecurityACL.checkReadOnly(json.project,Project)
         SecUser currentUser = cytomineService.getCurrentUser()
         json.user = currentUser.id
         synchronized (this.getClass()) {
             Command c = new AddCommand(user: currentUser)
             executeCommand(c,null,json)
         }
     }

     /**
      * Update this domain with new data from json
      * @param domain Domain to update
      * @param jsonNewData New domain datas
      * @return  Response structure (new domain data, old domain data..)
      */
     def update(NestedImageInstance domain, def jsonNewData) {
         SecurityACL.check(domain.container(),READ)
         SecurityACL.check(jsonNewData.project,Project,READ)
         SecurityACL.checkReadOnly(domain.container())
         SecurityACL.checkReadOnly(jsonNewData.project,Project)
         SecUser currentUser = cytomineService.getCurrentUser()
         Command c = new EditCommand(user: currentUser)
         executeCommand(c,domain,jsonNewData)
     }

     /**
      * Delete this domain
      * @param domain Domain to delete
      * @param transaction Transaction link with this command
      * @param task Task for this command
      * @param printMessage Flag if client will print or not confirm message
      * @return Response structure (code, old domain,..)
      */
     def delete(NestedImageInstance domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
         SecurityACL.check(domain.container(),READ)
         SecurityACL.checkReadOnly(domain.container())
         SecUser currentUser = cytomineService.getCurrentUser()
         Command c = new DeleteCommand(user: currentUser,transaction:transaction)
         return executeCommand(c,domain,null)
     }

     def getStringParamsI18n(def domain) {
         return [domain.id, domain.baseImage?.filename, domain.project.name]
     }
}
