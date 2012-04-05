/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class UserPositionUrlMappings {

    static mappings = {
        "/api/imageinstance/$id/position" (controller : "restUserPosition") {
             action = [POST:"add"]
         }
         "/api/imageinstance/$id/position/$user" (controller : "restUserPosition") {
             action = [GET:"lastPositionByUser"]
         }
         "/api/imageinstance/$id/online"(controller: "restUserPosition"){
             action = [GET:"listOnlineUsersByImage"]
         }
         "/api/project/$id/online"(controller:"restUserPosition") {
             action = [GET : "listLastUserPositionsByProject"]
         }

    }
}
