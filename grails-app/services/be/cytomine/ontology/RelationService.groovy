package be.cytomine.ontology

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

import be.cytomine.command.Transaction
import be.cytomine.project.Project
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task

//import be.cytomine.Exception.CytomineMethodNotYetImplementedException
/**
 * No security restriction for this domain (only read)
 */
class RelationService extends ModelService {

    static transactional = true

    def list() {
        Relation.list()
    }

    def read(def id) {
        Relation.read(id)
    }

    def readByName(String name) {
        Relation.findByName(name)
    }

    def getRelationParent() {
        readByName(RelationTerm.names.PARENT)
    }

    def deleteDependentRelationTerm(Project project, Transaction transaction, Task task = null) {
        //throw new CytomineMethodNotYetImplementedException("");
    }
}
