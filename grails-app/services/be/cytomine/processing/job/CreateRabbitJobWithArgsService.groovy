package be.cytomine.processing.job

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

import be.cytomine.Exception.MiddlewareException
import be.cytomine.middleware.MessageBrokerServer
import be.cytomine.processing.Job
import be.cytomine.security.UserJob
import grails.converters.JSON
import grails.util.Holders
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * Created by julien 
 * Date : 24/04/15
 * Time : 16:15
 */
class CreateRabbitJobWithArgsService extends AbstractJobService{
    def jobParameterService
    def amqpQueueService

    def init(Job job, UserJob userJob) {
        jobParameterService.add(JSON.parse(createJobParameter("host",job,Holders.getGrailsApplication().config.grails.serverURL).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("publicKey",job,userJob.publicKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("privateKey",job,userJob.privateKey).encodeAsJSON()))
        //Execute Job
        log.info "Execute Job..."
    }


    def execute(Job job, UserJob userJob, boolean preview) {

        if(!job.software.executeCommand)
            throw new MiddlewareException("No command found for this job, cannot execute it")

        //check if the queue exists
        String queueName = amqpQueueService.queuePrefixSoftware + job.software.name.capitalize()
        MessageBrokerServer mbs = MessageBrokerServer.findByName("MessageBrokerServer")
        if(!amqpQueueService.checkRabbitQueueExists(queueName, mbs))
            throw new MiddlewareException("Amqp queue does not exist, cannot execute the job")

        String[] allArgs = getCommandJobWithArgs(job)

        String jsonArgs = getJSONArrayFromStringArray(allArgs)

        job.discard()
        printStartJobInfo(job,allArgs)


        println "Command tab : " + jsonArgs
        amqpQueueService.publishMessage(amqpQueueService.read(queueName), jsonArgs)

        //launchSoftware(allArgs,job)
        //printStopJobInfo(job,allArgs)
    }

    @Override
    Double computeRate(Job job) {
        return null
    }

    String getJSONArrayFromStringArray(String[] args) {
        JSONArray jsonArgs = new JSONArray(Arrays.asList(args))
        return jsonArgs.toString()
    }
}
