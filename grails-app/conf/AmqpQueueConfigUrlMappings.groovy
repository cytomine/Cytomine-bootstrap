/**
 * Created by julien 
 * Date : 26/02/15
 * Time : 15:12
 */

class AmqpQueueConfigUrlMappings{
    static mappings = {
        "/api/amqp_queue_config.$format"(controller:"restAmqpQueueConfig"){
            action = [GET:"list", POST:"add"]
        }
        "/api/amqp_queue_config/$id.$format"(controller:"restAmqpQueueConfig"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/amqp_queue_config/name/$name.$format"(controller:"restAmqpQueueConfig"){
            action = [GET:"show"]
        }
    }
}
