package be.cytomine

import be.cytomine.middleware.MessageBrokerServer
import be.cytomine.processing.Job
import be.cytomine.processing.JobParameter
import be.cytomine.processing.Software
import be.cytomine.security.UserJob
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.JobAPI
import be.cytomine.test.http.JobParameterAPI
import be.cytomine.test.http.SoftwareAPI
import com.rabbitmq.client.Channel
import com.rabbitmq.client.GetResponse
import grails.converters.JSON
import grails.util.Holders

/**
 * Created by julien 
 * Date : 30/03/15
 * Time : 10:10
 */
class SoftwareRabbitQueueTests {

    def amqpQueueService = Holders.getGrailsApplication().getMainContext().getBean("amqpQueueService")
    def rabbitConnectionService = Holders.getGrailsApplication().getMainContext().getBean("rabbitConnectionService")

    void testAddSoftwareRabbitQueue() {
        def softwareToAdd = BasicInstanceBuilder.getSoftwareNotExistForRabbit(false)
        softwareToAdd.executeCommand = "groovy -cp algo/computeAnnotationStats/Cytomine-Java-Client.jar:algo/computeAnnotationStats/jts-1.13.jar algo/computeAnnotationStats/computeAnnotationStats.groovy"
        softwareToAdd.service = "createRabbitJobService"

        def result = SoftwareAPI.create(softwareToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        softwareToAdd = result.data as Software

        String queueName = amqpQueueService.queuePrefixSoftware + (String) softwareToAdd.name.capitalize()

        assert amqpQueueService.checkAmqpQueueDomainExists(queueName)

        MessageBrokerServer mbs = MessageBrokerServer.findByName("MessageBrokerServer")
        assert amqpQueueService.checkRabbitQueueExists(queueName, mbs)

        Job job = BasicInstanceBuilder.getJobNotExist(true, softwareToAdd)
        UserJob userJob = BasicInstanceBuilder.getUserJobNotExist(job, true)

        result = JobAPI.execute(job.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert result.code == 200


        Channel channel = rabbitConnectionService.getRabbitChannel(queueName, mbs)
        GetResponse getResponse = channel.basicGet(queueName, true)

        String message = new String(getResponse.body)
        assert message.length() > 0
        println "Message read : " + (new String(getResponse.body))
    }
}
