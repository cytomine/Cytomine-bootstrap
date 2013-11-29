/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class UserPositionUrlMappings {

    static mappings = {
        "/api/imageinstance/$id/position.$format" (controller : "restUserPosition") {
             action = [POST:"add"]
         }
         "/api/imageinstance/$id/position/$user.$format" (controller : "restUserPosition") {
             action = [GET:"lastPositionByUser"]
         }
         "/api/imageinstance/$id/online.$format"(controller: "restUserPosition"){
             action = [GET:"listOnlineUsersByImage"]
         }
         "/api/project/$id/online.$format"(controller:"restUserPosition") {
             action = [GET : "listLastUserPositionsByProject"]
         }

    }
}
