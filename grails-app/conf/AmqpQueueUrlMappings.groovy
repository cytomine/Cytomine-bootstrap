/**
 * Created by julien 
 * Date : 25/02/15
 * Time : 15:01
 */

class AmqpQueueUrlMappings{
    static mappings = {
        "/api/amqp_queue.$format"(controller:"restAmqpQueue"){
            action = [GET: "list",POST:"add"]
        }
        "/api/amqp_queue/$id.$format"(controller:"restAmqpQueue"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/amqp_queue.$format?name=$name"(controller:"restAmqpQueue"){
            action = [GET:"listByNameILike"]
        }
        "/api/amqp_queue/name/$name.$format"(controller:"restAmqpQueue"){
            action = [GET:"show"]
        }
    }
}
