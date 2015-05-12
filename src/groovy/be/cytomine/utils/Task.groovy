package be.cytomine.utils

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
 * A task provide info about a command.
 * The main info is the progress status
 * THIS CLASS CANNOT BE A DOMAIN! Because it cannot works with hibernate transaction.
 */
class Task {

    Long id
    /**
     * Request progress between 0 and 100
     */
    int progress = 0

    /**
     * Project updated by the command task
     */
    Long projectIdent = -1

    /**
     * User that ask the task
     */
    Long userIdent

    boolean printInActivity = false


    def dataSource


    def sequenceService


    def getMap(taskService) {
        def map = [:]
        map.id = id
        map.progress = progress
        map.project = projectIdent
        map.user = userIdent
        map.printInActivity = printInActivity
        map.comments = taskService.getLastComments(this,5)
        return map
    }

}
