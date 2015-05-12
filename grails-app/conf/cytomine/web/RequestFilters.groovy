package cytomine.web

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
                if(actionName.equals("listOnlineUsersByImage")) return
                if(controllerName.equals("restAnnotationIndex") && actionName.equals("listByImage")) return

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
                if(controllerName.equals("restAnnotationIndex") && actionName.equals("listByImage")) return

                log.info controllerName+"."+actionName + " Request took ${System.currentTimeMillis()-request.currentTime}ms"
            }
        }
    }
}
