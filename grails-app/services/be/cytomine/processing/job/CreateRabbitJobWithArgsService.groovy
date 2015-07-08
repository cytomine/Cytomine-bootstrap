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
import be.cytomine.processing.JobParameter
import be.cytomine.processing.SoftwareParameter
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
    def softwareParameterService

    def init(Job job, UserJob userJob) {
        jobParameterService.add(JSON.parse(createJobParameter("host",job,Holders.getGrailsApplication().config.grails.serverURL).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("publicKey",job,userJob.publicKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("privateKey",job,userJob.privateKey).encodeAsJSON()))

        //get all parameters with set by server = true.
        def softwareParameters = softwareParameterService.list(job.software, true);

        // then set if these parameters exist
        if(softwareParameters.find {it.name == "cytomine_id_software"})
            jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_software",job,job.software.id.toString()).encodeAsJSON()))
        if(softwareParameters.find {it.name == "cytomine_id_project"})
            jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_project",job,job.project.id.toString()).encodeAsJSON()))

        if(softwareParameters.find {it.name == "pyxit_save_to"})
            jobParameterService.add(JSON.parse(createJobParameter("pyxit_save_to",job,"algo/models/"+job.software.name+"/"+job.id+".pkl").encodeAsJSON()))
        if(softwareParameters.find {it.name == "model_save_to_dir"})
            jobParameterService.add(JSON.parse(createJobParameter("model_save_to_dir",job,"algo/models/"+job.software.name+"/").encodeAsJSON()))
        if(softwareParameters.find {it.name == "model_name_to_save"})
            jobParameterService.add(JSON.parse(createJobParameter("model_name_to_save",job,""+job.id).encodeAsJSON()))

        SoftwareParameter modelJob = softwareParameterService.list(job.software).find {it.name == "model_id_job"};
        if(softwareParameters.find {it.name == "pyxit_load_from"}) {
            JobParameter jobParam = JobParameter.findByJobAndSoftwareParameter(job, modelJob)
            if (jobParam) {
                Job previousJob = Job.read(jobParam.value)
                jobParameterService.add(JSON.parse(createJobParameter("pyxit_load_from",job,"algo/models/"+previousJob.software.name+"/"+previousJob.id+".pkl").encodeAsJSON()))
            }
            // TODO throw error
            println "no jobParam."
        }
        SoftwareParameter modelsJob = softwareParameterService.list(job.software).find {it.name == "models_id_job"};
        if(softwareParameters.find {it.name == "cytomine_model_names_to_load"}) {
            JobParameter jobParam = JobParameter.findByJobAndSoftwareParameter(job, modelsJob)
            if (jobParam) {
                def ids = ((String) jobParam.value).split(",")
                def paths = []
                ids.each {
                    Job previousJob = Job.read(it)
                    paths << previousJob.software.name+"/"+previousJob.id
                }
                paths =  paths.join(",")
                jobParameterService.add(JSON.parse(createJobParameter("cytomine_model_names_to_load",job,paths).encodeAsJSON()))
            }
            // TODO throw error
            println "no jobParam."
        }


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
