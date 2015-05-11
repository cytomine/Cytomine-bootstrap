package be.cytomine.utils

import be.cytomine.api.UrlApi
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import groovy.sql.Sql
import org.hibernate.FetchMode

class DataTablesService {

//&iColumns=14&mDataProp_0=id&mDataProp_1=baseImage.macroURL&mDataProp_2=baseImage.originalFilename&mDataProp_3=baseImage.width&mDataProp_4=baseImage.height&mDataProp_5=baseImage.magnification&mDataProp_6=baseImage.resolution&mDataProp_7=countImageAnnotations&mDataProp_8=countImageJobAnnotations&mDataProp_9=countImageReviewedAnnotations&mDataProp_10=baseImage.mime.extension&mDataProp_11=created&mDataProp_12=reviewStart&mDataProp_13=updated&sSearch=&bRegex=false&sSearch_0=&bRegex_0=false&bSearchable_0=false&sSearch_1=&bRegex_1=false&bSearchable_1=false&sSearch_2=&bRegex_2=false&bSearchable_2=true&sSearch_3=&bRegex_3=false&bSearchable_3=false&sSearch_4=&bRegex_4=false&bSearchable_4=false&sSearch_5=&bRegex_5=false&bSearchable_5=false&sSearch_6=&bRegex_6=false&bSearchable_6=false&sSearch_7=&bRegex_7=false&bSearchable_7=false&sSearch_8=&bRegex_8=false&bSearchable_8=false&sSearch_9=&bRegex_9=false&bSearchable_9=false&sSearch_10=&bRegex_10=false&bSearchable_10=true&sSearch_11=&bRegex_11=false&bSearchable_11=false&sSearch_12=&bRegex_12=false&bSearchable_12=false&sSearch_13=&bRegex_13=false&bSearchable_13=false&iSortCol_0=5&sSortDir_0=asc&iSortingCols=1&bSortable_0=true&bSortable_1=true&bSortable_2=true&bSortable_3=true&bSortable_4=true&bSortable_5=true&bSortable_6=true&bSortable_7=true&bSortable_8=true&bSortable_9=true&bSortable_10=true&bSortable_11=true&bSortable_12=true&bSortable_13=true&datatables=true&_=1392363731906
    //TODO: document this methods + params

    def dataSource
    def cytomineService
    def securityACLService
    def currentRoleServiceProxy

    def process(params, domain, restrictions, returnFields, project) {
        params.max = params.iDisplayLength ? params.iDisplayLength as int : 10;
        params.offset = params.iDisplayStart ? params.iDisplayStart as int : 0;
        def dataToRender = [:]

        String abstractImageAlias = "ai"
        String _search = params.sSearch ? "%"+params.sSearch+"%" : "%"

        //&iSortCol_0=7&sSortDir_0=asc
        //iSortCol_0=0&sSortDir_0=asc

        //assert ["hi","hey","hello"] == ["hello","hi","hey"].sort { a, b -> a.length() <=> b.length() }


        def col = params.get("iSortCol_0");
        def sort = params.get("sSortDir_0")
        def sortProperty = "mDataProp_"+col

        if(domain==ImageInstance) {
            List<ImageInstance> images = ImageInstance.createCriteria().list() {
                createAlias("baseImage", abstractImageAlias)
                eq("project", project)
                isNull("parent")
                isNull("deleted")
                fetchMode 'baseImage', FetchMode.JOIN
                ilike(abstractImageAlias + ".originalFilename", _search)
            }

            def property = params.get(sortProperty)

            if(property) {
                images.sort {
                    //id, name,....


                    def data = null

                    if(property.equals("numberOfAnnotations")) {
                        data = it.countImageAnnotations
                    } else if(property.equals("numberOfJobAnnotations")) {
                        data = it.countImageJobAnnotations
                    }else if(property.equals("numberOfReviewedAnnotations")) {
                        data = it.countImageReviewedAnnotations
                    }else if(property.equals("originalFilename")) {
                        data = it.baseImage.originalFilename
                    }else {
                        data = it."$property"
                    }

                    return data
                }

                //if desc order, inverse
                if(sort.equals("desc")) {
                    images = images.reverse()
                }
            }


            return images
        } else if(domain==AbstractImage) {


            //FIRST OF UNION: take all image in project
            //SECOND OF UNION: take all image NOT IN this project

            String request ="""
                    SELECT DISTINCT ai.id, ai.original_filename, ai.created as created, true
                    FROM abstract_image ai LEFT OUTER JOIN image_instance ii ON ii.base_image_id = ai.id ${getAclTable()}
                    WHERE project_id = ${project.id}
                    AND ii.deleted IS NULL
                    AND ${(_search? "ai.original_filename ilike '%${_search}%'" : "")}
                    ${getAclWhere()}
                    UNION
                    SELECT DISTINCT ai.id, ai.original_filename, ai.created as created, false
                    FROM abstract_image ai ${getAclTable()}
                    WHERE ai.id NOT IN (SELECT ai.id
                                     FROM abstract_image ai LEFT OUTER JOIN image_instance ii ON ii.base_image_id = ai.id
                                     WHERE project_id = ${project.id}
                                     AND ii.deleted IS NULL)
                    AND ${(_search? "ai.original_filename ilike '%${_search}%'" : "")}
                     ${getAclWhere()}
                    ORDER BY created desc
                """

                println request
//
//
//                    "SELECT ai.id, ai.original_filename, ai.created as created, true\n" +
//                    "FROM abstract_image ai LEFT OUTER JOIN image_instance ii ON ii.base_image_id = ai.id\n" +
//                    "WHERE project_id = ${project.id}\n" +
//                    "AND ii.deleted IS NULL\n" +
//                    (_search? "AND ai.original_filename ilike '%${_search}%'" : "") +
//                    "UNION\n" +
//                    "SELECT ai.id, ai.original_filename, ai.created as created, false\n" +
//                    "FROM abstract_image ai\n" +
//                    "WHERE id NOT IN (SELECT ai.id\n" +
//                    "                 FROM abstract_image ai LEFT OUTER JOIN image_instance ii ON ii.base_image_id = ai.id\n" +
//                    "                 WHERE project_id = ${project.id}\n" +
//                    "                 AND ii.deleted IS NULL) " +
//                    (_search? "AND ai.original_filename ilike '%${_search}%'" : "") +
//                    " ORDER BY created desc"


            def data = []
            def sql = new Sql(dataSource)
            sql.eachRow(request) {
                def img = [:]
                img.id=it[0]
                img.originalFilename=it[1]
                img.created=it[2]
                img.macroURL = UrlApi.getAbstractImageThumbURL(img.id)
                img.inProject = it[3]
                data << img
            }
            try {
                sql.close()
            }catch (Exception e) {}

            data.sort {
                //id, name,....
                def property = params.get(sortProperty)
                return it."$property"
            }

            //if desc order, inverse
            if(sort.equals("desc")) {
                println "reverse"
                data = data.reverse()
            }

            return data
        }

    }

    private String getAclTable() {
        if(currentRoleServiceProxy.isAdminByNow(cytomineService.currentUser)) {
            return ""
        } else {
            return ", storage_abstract_image, acl_object_identity, acl_entry, acl_sid"
        }
    }

    private String getAclWhere() {
        if(currentRoleServiceProxy.isAdminByNow(cytomineService.currentUser)) {
            return ""
        } else {
            return """
                    AND storage_abstract_image.abstract_image_id = ai.id
                    AND acl_object_identity.object_id_identity = storage_abstract_image.storage_id
                    AND acl_entry.acl_object_identity = acl_object_identity.id
                    AND acl_entry.sid = acl_sid.id
                    AND acl_sid.sid like '${cytomineService.currentUser.username}'
            """
        }
    }
}
