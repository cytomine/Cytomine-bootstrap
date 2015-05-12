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

import be.cytomine.image.AbstractImage

/**
 * Created by lrollus on 7/22/14.
 */
class AbstractImageSearch extends EngineSearch {

    public String createRequestOnAttributes(List<String> words) {
        if (idProject) return "" //if inside a project, no need to search in abstract image (just image instance)
        return """
            SELECT ai.id as id,'${AbstractImage.class.name}' as type ${getMatchingValue("ai.original_filename")} ${
            getName("ai.original_filename")
        }
            FROM abstract_image as ai, storage_abstract_image sai, acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE ai.id = sai.abstract_image_id
            ${getRestrictedIdForm("ai.id")}
            AND aoi.object_id_identity = sai.storage_id
            AND sid.sid = '${currentUser.username}'
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words, "ai.original_filename")}
            AND ai.deleted IS NULL
        """
    }

    public String createRequestOnProperty(List<String> words) {
        if (idProject) return "" //if inside a project, no need to search in abstract image (just image instance)
        return """
            SELECT property.domain_ident as id, property.domain_class_name as type ${
            getMatchingValue("property.key || ': ' || property.value")
        } ${getName("ai.original_filename")}
            FROM property property, storage_abstract_image sai, abstract_image ai, acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE property.domain_class_name like '${AbstractImage.class.name}'
            ${getRestrictedIdForm("property.domain_ident")}
            AND property.domain_ident = ai.id
            AND ai.id = sai.abstract_image_id
            AND aoi.object_id_identity = sai.storage_id
            AND sid.sid = '${currentUser.username}'
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words, "property.value")}
            AND ai.deleted IS NULL
        """
    }

    public String createRequestOnDescription(List<String> words) {
        println "PROJECT.createRequestOnDescription"
        if (idProject) return "" //if inside a project, no need to search in abstract image (just image instance)
        return """
            SELECT description.domain_ident as id, description.domain_class_name as type ${
            getMatchingValue("description.data")
        } ${getName("ai.original_filename")}
            FROM description description, storage_abstract_image sai,abstract_image ai,acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE description.domain_class_name like '${AbstractImage.class.name}'
            ${getRestrictedIdForm("description.domain_ident")}
            AND description.domain_ident = ai.id
            AND ai.id = sai.abstract_image_id
            AND aoi.object_id_identity = sai.storage_id
            AND sid.sid = '${currentUser.username}'
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words, "description.data")}
            AND ai.deleted IS NULL
        """
    }
}
