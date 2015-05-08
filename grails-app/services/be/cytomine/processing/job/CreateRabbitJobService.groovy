package be.cytomine.processing.job

import be.cytomine.Exception.MiddlewareException
import be.cytomine.middleware.MessageBrokerServer
import be.cytomine.processing.Job
import be.cytomine.processing.SoftwareParameter
import be.cytomine.security.UserJob
import grails.converters.JSON
import grails.util.Holders
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * Created by julien 
 * Date : 20/03/15
 * Time : 14:48
 */
class CreateRabbitJobService extends AbstractJobService{

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


        //get params defined for this job
        String[] jobParams = getParametersValues(job)

        //get the executed command value and all its hard-coded parameters
        String[] mainArgs = createArgsArray(job)
        String[] allArgs = new String[mainArgs.length+jobParams.length+2]

        int index = 0
        mainArgs.each {
            allArgs[index] = mainArgs[index]
            index++
        }
        //build software params
        allArgs[index++] = job.id
        allArgs[index++] = UserJob.findByJob(job).id

        jobParams.each {
            allArgs[index++] = it
        }

        String jsonArgs = getJSONArrayFromStringArray(allArgs)

        job.discard()
        printStartJobInfo(job,allArgs)

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
