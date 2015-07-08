package be.cytomine.image.server

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

import be.cytomine.command.AddCommand
import be.cytomine.command.Command
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import grails.converters.JSON

import static org.springframework.security.acls.domain.BasePermission.WRITE
import static org.springframework.security.acls.domain.BasePermission.READ

class StorageAbstractImageService extends ModelService {

    def transactionService
    def securityACLService

    def currentDomain() {
        return StorageAbstractImage
    }

    def add(def json) {
        securityACLService.check(json.storage,Storage,WRITE)
        Command c = new AddCommand(user: cytomineService.getCurrentUser())
        executeCommand(c,null,json)
    }

    def delete(StorageAbstractImage sai, Transaction transaction = null, Task task = null) {
        //We don't delete domain, we juste change a flag
        // TODO : current container is storage but only admin can modify it.
        securityACLService.check(sai.container(),READ)

        def jsonNewData = JSON.parse(sai.encodeAsJSON())
        jsonNewData.deleted = new Date().time
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new EditCommand(user: currentUser)
        c.delete = true
        return executeCommand(c,sai,jsonNewData)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.abstractImage.filename, domain.storage.name]
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     * TODO: secure!
     */
    def retrieve(Map json) {
        StorageAbstractImage.read(json.id)
    }
}
