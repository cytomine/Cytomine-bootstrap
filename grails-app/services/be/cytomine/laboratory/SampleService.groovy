package be.cytomine.laboratory

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

import be.cytomine.Exception.ConstraintException
import be.cytomine.command.*
import be.cytomine.image.AbstractImage
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task

class SampleService extends ModelService {

    static transactional = true

    def cytomineService
    def abstractImageService
    def transactionService
    def securityACLService

    def currentDomain() {
        return Sample
    }

    //@Secured(['ROLE_ADMIN'])
    def list() {
        securityACLService.checkAdmin(cytomineService.currentUser)
        Sample.list()
    }

    //TODO:: secure ACL (from abstract image)
    def list(User user) {
        def abstractImageAvailable = abstractImageService.list(user)
        if(abstractImageAvailable.isEmpty()) {
            return []
        } else {
            AbstractImage.createCriteria().list {
                inList("id", abstractImageAvailable.collect{it.id})
                projections {
                    groupProperty('sample')
                }
            }
        }
    }

    //TODO:: secure ACL (if abstract image from sample is avaialbale for user)
    def read(def id) {
        Sample.read(id)
    }

    //TODO:: secure ACL (who can add/update/delete a sample?)
    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkUser(currentUser)
        Command c = new AddCommand(user: currentUser)
        return executeCommand(c,null,json)
    }

    //TODO:: secure ACL (who can add/update/delete a sample?)
    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(Sample sample, def jsonNewData) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkUser(currentUser)
        return executeCommand(new EditCommand(user: currentUser),sample,jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(Sample domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkUser(currentUser)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }

    def deleteDependentAbstractImage(Sample sample, Transaction transaction, Task task = null) {
        AbstractImage.findAllBySample(sample).each {
            abstractImageService.delete(it,transaction,null,false)
        }
    }

    def deleteDependentSource(Sample sample, Transaction transaction, Task task = null) {
        //TODO: implement source cascade delete (first impl source command delete)
        if(Source.findAllBySample(sample)) {
            throw new ConstraintException("Sample has source. Cannot delete sample!")
        }
    }

}
