package be.cytomine.search.engine

import be.cytomine.project.Project

/**
 * Created by lrollus on 7/22/14.
 */
abstract class AnnotationSearch extends EngineSearch {

    public abstract String getClassName()
    public abstract String getTermTable()
    public abstract String getTable()
    public abstract String  getLinkTerm()

    public String createRequestOnAttributes(List<String> words) {
        return """
            SELECT annotation.id as id,'${getClassName()}' as type ${getMatchingValue("term.name")} ${getName("CAST(annotation.id as VARCHAR)")}
            FROM ${getTable()} as annotation, image_instance ii,term as term, ${getTermTable()} as at, acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE true
            ${getRestrictedIdForm("annotation.id")}
            ${getLinkTerm()}
            ${idProject? "AND annotation.project_id = ${idProject}" : ""}
            AND aoi.object_id_identity = annotation.project_id
            AND sid.sid = '${currentUser.username}'
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words,"term.name")}
            AND ii.id = annotation.image_id AND ii.deleted IS NULL
        """
    }

    public String createRequestOnProperty(List<String> words) {
            return """
            SELECT property.domain_ident as id, property.domain_class_name as type ${getMatchingValue("property.key || ': ' || property.value")} ${getName("CAST(property.domain_ident as VARCHAR)")}
            FROM property property, image_instance ii,${getTable()} as annotation,acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE property.domain_class_name like '${getClassName()}'
            ${getRestrictedIdForm("domain_ident")}
            ${idProject? "AND annotation.project_id = ${idProject}" : ""}
            AND annotation.id = domain_ident
            AND aoi.object_id_identity = annotation.project_id
            AND sid.sid = '${currentUser.username}'
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words,"property.value")}
            AND ii.id = annotation.image_id AND ii.deleted IS NULL
        """
    }

    public String createRequestOnDescription(List<String> words) {
        println "${getTable()}.createRequestOnDescription"
        return """
            SELECT description.domain_ident as id, description.domain_class_name as type ${getMatchingValue("description.data")} ${getName("CAST(description.domain_ident as VARCHAR)")}
            FROM description description,image_instance ii, ${getTable()} as annotation, acl_object_identity as aoi, acl_sid as sid, acl_entry as ae
            WHERE description.domain_class_name like '${getClassName()}'
            ${getRestrictedIdForm("domain_ident")}
            ${idProject? "AND annotation.project_id = ${idProject}" : ""}
            AND annotation.id = domain_ident
            AND aoi.object_id_identity = annotation.project_id
            AND sid.sid = '${currentUser.username}'
            AND ae.acl_object_identity = aoi.id
            AND ae.sid = sid.id
            AND ${formatCriteriaToWhere(words,"description.data")}
            AND ii.id = annotation.image_id AND ii.deleted IS NULL
        """
    }

}
