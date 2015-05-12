package be.cytomine.search.engine

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

import be.cytomine.project.Project

/**
 * Created by lrollus on 7/22/14.
 */
class ProjectSearch extends EngineSearch {

    public String createRequestOnAttributes(List<String> words) {
        //if(idProject) return "" //if inside a project, no need to search in the project table
        return """
            SELECT project.id as id,'${Project.class.name}' as type ${getMatchingValue("name")} ${getName("name")}
            FROM project as project, acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE aoi.object_id_identity = project.id
            ${getRestrictedIdForm("project.id")}
            ${idProject && !idProject.isEmpty() ? "AND project.id IN (${idProject.join(",")})" : ""}
            AND sid.sid = '${currentUser.username}'
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words, "name")}
            AND project.deleted IS NULL
        """
    }

    public String createRequestOnProperty(List<String> words) {
        //if(idProject) return "" //if inside a project, no need to search in the project table
        return """
            SELECT property.domain_ident as id, property.domain_class_name as type ${
            getMatchingValue("property.key || ': ' || property.value")
        } ${getName("name")}
            FROM property property, project project, acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE property.domain_class_name like '${Project.class.name}'
            ${getRestrictedIdForm("domain_ident")}
            AND aoi.object_id_identity = domain_ident
            AND sid.sid = '${currentUser.username}'
            ${idProject && !idProject.isEmpty() ? "AND property.domain_ident IN (${idProject.join(",")})" : ""}
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words, "property.value")}
            AND project.id = property.domain_ident AND project.deleted IS NULL
        """
    }

    public String createRequestOnDescription(List<String> words) {
        println "PROJECT.createRequestOnDescription"
        //if(idProject) return "" //if inside a project, no need to search in the project table
        return """
            SELECT description.domain_ident as id, description.domain_class_name as type ${
            getMatchingValue("description.data")
        } ${getName("name")}
            FROM description description, project project, acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE description.domain_class_name like '${Project.class.name}'
            ${getRestrictedIdForm("domain_ident")}
            AND aoi.object_id_identity = domain_ident
            AND sid.sid = '${currentUser.username}'
            ${idProject && !idProject.isEmpty() ? "AND description.domain_ident IN (${idProject.join(",")})" : ""}
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words, "description.data")}
            AND project.id = description.domain_ident AND project.deleted IS NULL
        """
    }
}
