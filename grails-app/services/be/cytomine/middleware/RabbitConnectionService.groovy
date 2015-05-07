package be.cytomine.middleware

import be.cytomine.Exception.MiddlewareException
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import grails.util.Holders
//import sun.plugin2.message.Message

/**
 * Created by julien 
 * Date : 17/03/15
 * Time : 10:08
 */
class RabbitConnectionService {
    def servletContext
    def amqpQueueService

    def setRabbitConnection(MessageBrokerServer mbsConnection) {
        ConnectionFactory factory = new ConnectionFactory()
        factory.setHost(mbsConnection.host)
        factory.setPort(mbsConnection.port)

        factory.setUsername(Holders.config.cytomine.middleware.rabbitmq.user);
        factory.setPassword(Holders.config.cytomine.middleware.rabbitmq.password);

        Connection connection

        try {
            connection = factory.newConnection()
        } catch(IOException e) {
            e.printStackTrace()
            throw new MiddlewareException("Connection to host : $mbsConnection.host could not be established. " + e.getMessage())
        }

        String rabbitConnection = "rabbitConnection." + mbsConnection.host
        servletContext[rabbitConnection] = connection
        return connection
    }


    Connection getRabbitConnection(MessageBrokerServer mbsConnection) {
        String rabbitConnection = "rabbitConnection." + mbsConnection.host
        Connection connection = (Connection)servletContext[rabbitConnection]

        if(connection == null) {
            connection = setRabbitConnection(mbsConnection)
        }

        return connection
    }


    def setRabbitChannel(String queueName, MessageBrokerServer mbs) {
        Channel channel

        try {
            Connection con = getRabbitConnection(mbs)
            channel = con.createChannel()
        } catch(IOException e) {
            throw new MiddlewareException("Channel not created! : " + e.getMessage())
        }

        // Setting up ack mode on this channel
        channel.confirmSelect()

        queueName = amqpQueueService.channelPrefixSoftware + queueName.capitalize()
        servletContext[queueName] = channel

        return channel
    }

    Channel getRabbitChannel(String queueName, MessageBrokerServer mbs) {
        String channelName = amqpQueueService.channelPrefixSoftware + queueName.capitalize()

        Channel channel = (Channel)servletContext[channelName]

        if(channel == null) {
            channel = setRabbitChannel(queueName, mbs)
        }

        return channel
    }

    def closeRabbitConnection(Connection connection) {
        connection.close()
    }

    def closeRabbitChannel(Channel channel) {
        channel.close()
    }

}
