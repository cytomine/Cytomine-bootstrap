/**
 * Created by jconfetti on 04/02/15.
 */
class MessageBrokerServerUrlMappings{
    static mappings = {
        "/api/message_broker_server.$format"(controller:"restMessageBrokerServer"){
            action = [GET: "list",POST:"add"]
        }
        "/api/message_broker_server/$id.$format"(controller:"restMessageBrokerServer"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/message_broker_server.$format?name=$name"(controller:"restMessageBrokerServer"){
            action = [GET:"listByNameILike"]
        }
    }
}
