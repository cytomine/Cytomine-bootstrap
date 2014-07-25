package be.cytomine.search.engine

import be.cytomine.project.Project
import be.cytomine.security.SecUser

/**
 * Created by lrollus on 7/22/14.
 */
class ProjectSearch extends EngineSearch {

    public String createRequestOnAttributes(List<String> words) {
        if(idProject) return "" //if inside a project, no need to search in the project table
        return """
            SELECT project.id as id,'${Project.class.name}' as type ${getMatchingValue("name")} ${getName("name")}
            FROM project as project, acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE aoi.object_id_identity = project.id
            ${getRestrictedIdForm("project.id")}
            AND sid.sid = '${currentUser.username}'
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words,"name")}
            AND project.deleted IS NULL
        """
    }

    public String createRequestOnProperty(List<String> words) {
        if(idProject) return "" //if inside a project, no need to search in the project table
            return """
            SELECT property.domain_ident as id, property.domain_class_name as type ${getMatchingValue("property.key || ': ' || property.value")} ${getName("name")}
            FROM property property, project project, acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE property.domain_class_name like '${Project.class.name}'
            ${getRestrictedIdForm("domain_ident")}
            AND aoi.object_id_identity = domain_ident
            AND sid.sid = '${currentUser.username}'
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words,"property.value")}
            AND project.id = property.domain_ident AND project.deleted IS NULL
        """
    }

    public String createRequestOnDescription(List<String> words) {
        println "PROJECT.createRequestOnDescription"
        if(idProject) return "" //if inside a project, no need to search in the project table
        return """
            SELECT description.domain_ident as id, description.domain_class_name as type ${getMatchingValue("description.data")} ${getName("name")}
            FROM description description, project project, acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE description.domain_class_name like '${Project.class.name}'
            ${getRestrictedIdForm("domain_ident")}
            AND aoi.object_id_identity = domain_ident
            AND sid.sid = '${currentUser.username}'
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words,"description.data")}
            AND project.id = description.domain_ident AND project.deleted IS NULL
        """
    }
}
