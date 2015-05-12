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

import be.cytomine.Exception.CytomineException
import be.cytomine.command.*
import be.cytomine.middleware.AmqpQueue
import be.cytomine.middleware.MessageBrokerServer
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import groovy.json.JsonBuilder
import org.springframework.security.acls.domain.BasePermission

import static org.springframework.security.acls.domain.BasePermission.*

class SoftwareService extends ModelService {

    static transactional = true

    boolean saveOnUndoRedoStack = false

    def cytomineService
    def transactionService
    def aclUtilService
    def softwareParameterService
    def jobService
    def softwareProjectService
    def securityACLService
    def amqpQueueService

    def currentDomain() {
        Software
    }

    Software read(def id) {
        //TODO: check authorization?
        Software.read(id)
    }

    def readMany(def ids) {
        //TODO: check authorization?
        Software.findAllByIdInList(ids)
    }

    def list() {
        securityACLService.checkGuest(cytomineService.currentUser)
        Software.list()
    }

    def list(Project project) {
        securityACLService.check(project.container(),READ)
        SoftwareProject.findAllByProject(project).collect {it.software}
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkGuest(currentUser)
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser),null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(Software software, def jsonNewData) {
        securityACLService.check(software.container(),WRITE)
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser),software, jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(Software domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        log.info "delete software"
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.check(domain.container(),DELETE)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }


    def afterAdd(def domain, def response) {
        aclUtilService.addPermission(domain, cytomineService.currentUser.username, BasePermission.ADMINISTRATION)

        // add 'defaults' software parameters
        SoftwareParameter softParam = new SoftwareParameter(software: domain as Software, name: "host", type: "String", required: true, index: 100, setByServer: true)
        softParam.save(failOnError: true)
        softParam = new SoftwareParameter(software: domain as Software, name: "publicKey", type: "String", required: true, index: 200, setByServer: true)
        softParam.save(failOnError: true)
        softParam = new SoftwareParameter(software: domain as Software, name: "privateKey", type: "String", required: true, index: 300, setByServer: true)
        softParam.save(failOnError: true)

        // add an AMQP queue with the name of the software (default parameters)
        String queueName = amqpQueueService.queuePrefixSoftware + ((domain as Software).name).capitalize()
        if(!amqpQueueService.checkAmqpQueueDomainExists(queueName)) {
            String exchangeName = amqpQueueService.exchangePrefixSoftware + ((domain as Software).name).capitalize()
            String brokerServerURL = (MessageBrokerServer.findByName("MessageBrokerServer")).host
            AmqpQueue aq = new AmqpQueue(name: queueName, host: brokerServerURL, exchange: exchangeName)
            aq.save(failOnError: true)

            // Creates the queue on the rabbit server
            amqpQueueService.createAmqpQueueDefault(aq)

            // Notify the queueCommunication that a software has been added
            def mapInfosQueue = [name: aq.name, host: aq.host, exchange: aq.exchange]
            JsonBuilder builder = new JsonBuilder()
            builder(mapInfosQueue)
            amqpQueueService.publishMessage(AmqpQueue.findByName("queueCommunication"), builder.toString())

        }
    }

    def afterDelete(def domain, def response) {

    }


    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }

    def deleteDependentSoftwareParameter(Software software, Transaction transaction, Task task = null) {
        log.info "deleteDependentSoftwareParameter ${SoftwareParameter.findAllBySoftware(software).size()}"
        SoftwareParameter.findAllBySoftware(software).each {
            softwareParameterService.delete(it,transaction,null, false)
        }
    }

    def deleteDependentJob(Software software, Transaction transaction, Task task = null) {
        Job.findAllBySoftware(software).each {
            jobService.delete(it,transaction,null, false)
        }
    }

    def deleteDependentSoftwareProject(Software software, Transaction transaction, Task task = null) {
        SoftwareProject.findAllBySoftware(software).each {
            softwareProjectService.delete(it,transaction,null, false)
        }
    }
}
