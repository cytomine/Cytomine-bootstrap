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

/**
 * Created by lrollus on 7/22/14.
 */
abstract class AnnotationSearch extends EngineSearch {

    public abstract String getClassName()

    public abstract String getTermTable()

    public abstract String getTable()

    public abstract String getLinkTerm()

    public String createRequestOnAttributes(List<String> words) {
        return """
            SELECT annotation.id as id,'${getClassName()}' as type ${getMatchingValue("term.name")} ${
            getName("CAST(annotation.id as VARCHAR)")
        }
            FROM ${getTable()} as annotation, image_instance ii,term as term, ${getTermTable()} as at, acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE true
            ${getRestrictedIdForm("annotation.id")}
            ${getLinkTerm()}
            ${idProject && !idProject.isEmpty() ? "AND annotation.project_id IN (${idProject.join(",")})" : ""}
            AND aoi.object_id_identity = annotation.project_id
            AND sid.sid = '${currentUser.username}'
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words, "term.name")}
            AND ii.id = annotation.image_id AND ii.deleted IS NULL
        """
    }

    public String createRequestOnProperty(List<String> words) {
        return """
            SELECT property.domain_ident as id, property.domain_class_name as type ${
            getMatchingValue("property.key || ': ' || property.value")
        } ${getName("CAST(property.domain_ident as VARCHAR)")}
            FROM property property, image_instance ii,${getTable()} as annotation,acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE property.domain_class_name like '${getClassName()}'
            ${getRestrictedIdForm("domain_ident")}
            ${idProject && !idProject.isEmpty() ? "AND annotation.project_id IN (${idProject.join(",")})" : ""}
            AND annotation.id = domain_ident
            AND aoi.object_id_identity = annotation.project_id
            AND sid.sid = '${currentUser.username}'
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words, "property.value")}
            AND ii.id = annotation.image_id AND ii.deleted IS NULL
        """
    }

    public String createRequestOnDescription(List<String> words) {
        println "${getTable()}.createRequestOnDescription"
        return """
            SELECT description.domain_ident as id, description.domain_class_name as type ${
            getMatchingValue("description.data")
        } ${getName("CAST(description.domain_ident as VARCHAR)")}
            FROM description description,image_instance ii, ${getTable()} as annotation, acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE description.domain_class_name like '${getClassName()}'
            ${getRestrictedIdForm("domain_ident")}
            ${idProject && !idProject.isEmpty() ? "AND annotation.project_id IN (${idProject.join(",")})" : ""}
            AND annotation.id = domain_ident
            AND aoi.object_id_identity = annotation.project_id
            AND sid.sid = '${currentUser.username}'
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words, "description.data")}
            AND ii.id = annotation.image_id AND ii.deleted IS NULL
        """
    }

}
