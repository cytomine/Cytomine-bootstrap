package be.cytomine

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

import be.cytomine.api.RestController

class GrantRoleController extends RestController {

    def currentRoleServiceProxy
    def cytomineService

    def openAdminSession() {
        currentRoleServiceProxy.activeAdminSession(cytomineService.currentUser)
        responseSuccess(getCurrentRole())
    }

    def closeAdminSession() {
        currentRoleServiceProxy.closeAdminSession(cytomineService.currentUser)
        responseSuccess(getCurrentRole())
    }

    def infoAdminSession() {
        responseSuccess(getCurrentRole())
    }

    public def getCurrentRole() {
        def data = [:]
        def user = cytomineService.currentUser
        data['admin'] = currentRoleServiceProxy.isAdmin(user)
        data['user'] = !data['admin'] && currentRoleServiceProxy.isUser(user)
        data['guest'] = !data['admin'] && !data['user'] && currentRoleServiceProxy.isGuest(user)

        data['adminByNow'] = currentRoleServiceProxy.isAdminByNow(user)
        data['userByNow'] = currentRoleServiceProxy.isUserByNow(user)
        data['guestByNow'] = currentRoleServiceProxy.isGuestByNow(user)
        return data
    }
}
