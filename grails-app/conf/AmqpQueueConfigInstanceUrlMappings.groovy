/**
 * Created by julien 
 * Date : 04/03/15
 * Time : 16:22
 */

class AmqpQueueConfigInstanceUrlMappings{
    static mappings = {
        "/api/amqp_queue_config_instance.$format"(controller:"restAmqpQueueConfigInstance"){
            action = [GET:"list", POST:"add"]
        }
        "/api/amqp_queue_config_instance/$id.$format"(controller:"restAmqpQueueConfigInstance"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/amqp_queue/$id/amqp_queue_config_instance.$format"(controller:"restAmqpQueueConfigInstance"){
            action = [GET: "listByQueue"]
        }
    }
}
