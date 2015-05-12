package be.cytomine.processing

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.AnnotationDomain
import be.cytomine.command.AddCommand
import be.cytomine.command.Command
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
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
     def jobService
     def jobParameterService
    def securityACLService

     def currentDomain() {
         return JobTemplateAnnotation
     }

     def read(def id) {
         def domain = JobTemplateAnnotation.read(id)
         if(domain) {
             securityACLService.check(domain.container(),READ)
         }
         domain
     }

     def list(JobTemplate jobTemplate, Long idAnnotation) {
         if(jobTemplate && idAnnotation) {
             securityACLService.check(jobTemplate.container(),READ)
             return JobTemplateAnnotation.findAllByJobTemplateAndAnnotationIdent(jobTemplate,idAnnotation)
         } else if(idAnnotation){
             securityACLService.check(AnnotationDomain.getAnnotationDomain(idAnnotation).container(),READ)
             return JobTemplateAnnotation.findAllByAnnotationIdent(idAnnotation)
         } else {
             securityACLService.check(jobTemplate.container(),READ)
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
         securityACLService.check(annotation.project,READ)
         SecUser currentUser = cytomineService.getCurrentUser()
         json.user = currentUser.id
         Command c = new AddCommand(user: currentUser)
         def result = executeCommand(c,null,json)
         return result

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
         securityACLService.check(domain.container(),READ)
         securityACLService.checkReadOnly(domain.container())
         SecUser currentUser = cytomineService.getCurrentUser()
         Command c = new DeleteCommand(user: currentUser,transaction:transaction)
         return executeCommand(c,domain,null)
     }

     def getStringParamsI18n(def domain) {
         return [domain.jobTemplate.name]
     }
}
