
class ConfigUrlMappings
{
    static mappings = {

        "/api/config.$format"(controller:"restConfig"){
            action = [GET:"list",POST: "add"]
        }
        "/api/config/$key.$format"(controller:"restConfig"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
    }
}
