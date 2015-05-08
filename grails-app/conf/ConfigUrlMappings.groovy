
class ConfigUrlMappings
{
    static mappings = {

        "/api/config.$format"(controller:"restConfig"){
            action = [GET:"list",POST: "add"]
        }
        "/api/config/key/$key.$format"(controller:"restConfig"){
            action = [GET:"show",PUT:"update"]
        }
        "/api/config/$id.$format"(controller:"restConfig"){
            action = [DELETE:"delete"]
        }
    }
}
