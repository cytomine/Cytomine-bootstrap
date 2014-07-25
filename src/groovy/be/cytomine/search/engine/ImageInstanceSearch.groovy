package be.cytomine.search.engine

import be.cytomine.image.ImageInstance
import be.cytomine.project.Project

/**
 * Created by lrollus on 7/22/14.
 */
class ImageInstanceSearch extends EngineSearch {

    public String createRequestOnAttributes(List<String> words) {
        return """
            SELECT ii.id as id,'${ImageInstance.class.name}' as type ${getMatchingValue("ai.original_filename")} ${getName("ai.original_filename")}
            FROM image_instance ii, abstract_image ai, acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE ii.base_image_id = ai.id
            ${getRestrictedIdForm("ii.id")}
            AND aoi.object_id_identity = ii.project_id
            ${idProject? "AND ii.project_id = ${idProject}" : ""}
            AND sid.sid = '${currentUser.username}'
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words,"ai.original_filename")}
            AND ii.deleted IS NULL
        """
    }

    public String createRequestOnProperty(List<String> words) {
        if(idProject) return "" //if inside a project, no need to search in the project table
            return """
            SELECT property.domain_ident as id, property.domain_class_name as type ${getMatchingValue("property.key || ': ' || property.value")} ${getName("ai.original_filename")}
            FROM property property, image_instance ii, abstract_image ai, acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE property.domain_class_name like '${ImageInstance.class.name}'
            ${getRestrictedIdForm("property.domain_ident")}
            AND ii.base_image_id = ai.id
            AND property.domain_ident = ii.id
            AND aoi.object_id_identity = ii.project_id
            AND sid.sid = '${currentUser.username}'
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words,"property.value")}
            AND ii.deleted IS NULL
        """
    }

    public String createRequestOnDescription(List<String> words) {
        println "PROJECT.createRequestOnDescription"
        if(idProject) return "" //if inside a project, no need to search in the project table
        return """
            SELECT description.domain_ident as id, description.domain_class_name as type ${getMatchingValue("description.data")} ${getName("ai.original_filename")}
            FROM description description, image_instance ii, abstract_image ai,acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE description.domain_class_name like '${ImageInstance.class.name}'
            ${getRestrictedIdForm("description.domain_ident")}
            AND description.domain_ident = ii.id
            AND ii.base_image_id = ai.id
            AND aoi.object_id_identity = ii.project_id
            AND sid.sid = '${currentUser.username}'
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words,"description.data")}
            AND ii.deleted IS NULL
        """
    }
}
