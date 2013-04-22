package be.cytomine.search

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import grails.util.Holders
import groovy.sql.Sql

/**
 * Created with IntelliJ IDEA.
 * User: pierre
 * Date: 15/04/13
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */

class SearchService extends ModelService {

    def dataSource
    def cytomineService

    //Security is made ​​in sql query thanks to currentUser
    //"getSecurityTable" and "getSecurityJoin" manage the security
    def list(List<String> keywords, String operator, String domain) {
        def data = []
        String request = ""
        String blocSelect = ""
        ArrayList<String> listTable = new ArrayList<String>()
        ArrayList<String> listType = new ArrayList<String>()

        SecUser currentUser = cytomineService.getCurrentUser()

        //Creating queries for project or annotation or image
        if (domain.equals("Project")) {
            listTable.add("project")
            listType.add(Project.class.getName())

            blocSelect = "SELECT DISTINCT pro.id, pro.name, '<domain>' " +
                    "FROM <table> as pro, property as p" + getSecurityTable(currentUser) +
                    "WHERE pro.id = p.domain_ident "

            //Add Security
            blocSelect += getSecurityJoin("pro.id", currentUser)
        } else if (domain.equals("Image")) {
            listTable.add("image_instance")
            listType.add(ImageInstance.class.getName())

            blocSelect = "SELECT DISTINCT ii.id, ai.filename, '<domain>', ai.id " +
                    "FROM abstract_image as ai, <table> as ii, property as p" + getSecurityTable(currentUser) +
                    "WHERE ai.id = ii.base_image_id "  +
                    "AND ii.id = p.domain_ident "

            //Add Security
            blocSelect += getSecurityJoin("ii.project_id", currentUser)
        } else if (domain.equals("Annotation")) {
            //There are three types of annotation
            listTable.add("user_annotation")
            listTable.add("algo_annotation")
            listTable.add("reviewed_annotation")
            listType.add(UserAnnotation.class.getName())
            listType.add(AlgoAnnotation.class.getName())
            listType.add(ReviewedAnnotation.class.getName())

            blocSelect = "SELECT DISTINCT a.id, a.created, '<domain>' " +
                    "FROM <table> as a, property as p" + getSecurityTable(currentUser) +
                    "WHERE a.id = p.domain_ident "

            //Add Security
            blocSelect += getSecurityJoin("a.project_id", currentUser)
        }

        //In case of the operator is OR
        if (operator.equals("OR")) {
            for (int a = 0; a < listTable.size(); a++) {
                //Replace in String: domain and table
                String blocTmp = blocSelect.replaceAll("<domain>", listType[a])
                String blocSelectFromWhere = blocTmp.replace("<table>", listTable[a])

                if (a != 0) { request += "UNION " }

                request +=  blocSelectFromWhere +
                        "AND (p.value ILIKE '%" + keywords[0] + "%'"

                //Loop for the keywords
                for (int i = 1; i < keywords.size() ; i++) {
                    request += " OR p.value ILIKE '%" + keywords[i] + "%'"
                }
                request += ") "
            }
            data = select(request)
        } else if (operator.equals("AND")) {
            for (int a = 0; a < listTable.size(); a++) {
                //Replace in String: domain and table
                String blocTmp = blocSelect.replaceAll("<domain>", listType[a])
                String blocSelectFromWhere = blocTmp.replace("<table>", listTable[a])

                if (a != 0) { request += "UNION " }

                request += "(" + blocSelectFromWhere +
                        "AND p.value ILIKE '%" + keywords[0] + "%' "

                //Loop for the keywords
                for (int i = 1; i < keywords.size() ; i++) {
                    request += "INTERSECT " +
                            blocSelectFromWhere +
                            "AND p.value ILIKE '%" + keywords[i] + "%' "
                }
                request += ") "
            }
            data = select(request)
        }
        data
    }

    //Return the table for the security
    private String getSecurityTable (SecUser currentUser) {
        String request = " "

        if (!currentUser.isAdmin()) {
            request = ", acl_object_identity as aoi, acl_sid as sid, acl_entry as ae "
        }
        return request
    }

    //Return jointure to check the security
    private String getSecurityJoin (String params, SecUser currentUser) {
        String request = ""

        if (!currentUser.isAdmin()) {
            request = "AND aoi.object_id_identity = " + params + " " +
                    "AND sid.sid = '${currentUser.humanUsername()}' " +
                    "AND ae.acl_object_identity = aoi.id " +
                    "AND ae.sid = sid.id "
        }
        return request
    }

    //Create the list with all results
    private def select(String request) {
        def data = []
        String domain
        println request

        new Sql(dataSource).eachRow(request) {
            def dataTmp = [:]
            //ID
            Long id = it[0]
            dataTmp.id = id

            //NAME
            String name = it[1]
            dataTmp.name = name

            //TYPE --> CLASS
            String type = it[2]
            dataTmp.type = type

            //URL FOR PROJECT/ANNOTATION/IMAGE
            if (type.equals(ImageInstance.class.getName())) {
                domain = "imageinstance"

                String idBaseImage = it[3]
                String image = "${serverUrl()}/api/image/$idBaseImage/thumb"
                dataTmp.image = image
            } else if (type.equals(Project.class.getName())) {
                domain = "project"
            } else if (type.equals(UserAnnotation.class.getName()) || type.equals(AlgoAnnotation.class.getName()) || type.equals(ReviewedAnnotation.class.getName())) {
                domain = "annotation"

                String cropImage = "${serverUrl()}/api/annotation/$id/crop.jpg"
                dataTmp.cropImage = cropImage
            }
            String url = "${serverUrl()}/api/$domain/$id"
            dataTmp.url = url

            data.add(dataTmp)
        }
        data
    }

    static def serverUrl() {
        Holders.getGrailsApplication().config.grails.serverURL
    }
}
