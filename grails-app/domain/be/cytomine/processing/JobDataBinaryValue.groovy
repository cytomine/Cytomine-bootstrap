package be.cytomine.processing

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

import be.cytomine.CytomineDomain

//* Its better to have a specific domain link than a simple 'byte[] value'.
//* Each time we load a job data

/**
 * A job data file stored in database
 * Used in JobData domain, it's better to do:
 * -JobDataBinaryValue value
 * Than
 * -byte[] value
 * With the first one, we only load the JobDataBinaryValue id
 * With the second one, evey time we load a JobData, we load the full byte[] automaticaly
 */
class JobDataBinaryValue extends CytomineDomain {

    /**
     * File data
     */
    byte[] data

    static belongsTo = [jobData: JobData]

    static constraints = {
        data(nullable: true)
    }
    static mapping = {
        id generator: "assigned"
        sort "id"
    }

    public beforeValidate() {
        super.beforeValidate()
    }
}
