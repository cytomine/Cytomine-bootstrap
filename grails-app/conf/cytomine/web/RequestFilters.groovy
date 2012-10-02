package cytomine.web
import be.cytomine.security.SecUser

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 19/08/11
 * Time: 9:49
 * To change this template use File | Settings | File Templates.
 */

class RequestFilters {

    def springSecurityService

    def filters = {
        //all(uri:'/api/**') {
        all(uri:'/**') {
            before = {
                if(actionName.equals("crop")) return
                if(actionName.equals("ping")) return
                if(actionName.equals("listOnlineFriendsWithPosition")) return
                if(actionName.equals("listOnlineFriendsWithPosition")) return
                if(controllerName.equals("restUserPosition") && actionName.equals("add")) return
                request.currentTime = System.currentTimeMillis()
                String userInfo = ""
                try { userInfo = springSecurityService.principal.id} catch(Exception e) { userInfo = springSecurityService.principal}
                log.info controllerName+"."+actionName + ": user:" + userInfo
            }
            after = {}
            afterView = {
                if(actionName.equals("crop")) return
                if(actionName.equals("ping")) return
                if(actionName.equals("listOnlineFriendsWithPosition")) return
                if(actionName.equals("listOnlineUsersByImage")) return
                if(controllerName.equals("restUserPosition") && actionName.equals("add")) return
                log.info controllerName+"."+actionName + " Request took ${System.currentTimeMillis()-request.currentTime}ms"
            }
        }
    }
}
