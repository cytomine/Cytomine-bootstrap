package be.cytomine.search

import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.SearchFilter
import be.cytomine.utils.SearchOperator
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
    def list(List<String> keywords, String operator, String filter, def idsProject) {
        def data = []
        String request = ""
        String blocSelect = ""
        ArrayList<String> listTable = new ArrayList<String>()
        ArrayList<String> listType = new ArrayList<String>()

        SecUser currentUser = cytomineService.getCurrentUser()

        //Creating queries for project or annotation or image
        if (filter.equals(SearchFilter.PROJECT)) {
            listTable.add("project")
            listType.add(Project.class.getName())

            blocSelect = "SELECT DISTINCT pro.id as id, extract(epoch from pro.created)*1000 as created, '<domain>' as class, pro.name as name, d.data as description, null as user, null as userfullname, pro.name as projectName, null as imageName, pro.id as project " +
                    "FROM <table> as pro LEFT OUTER JOIN description as d ON d.domain_ident = pro.id LEFT OUTER JOIN property as p ON pro.id = p.domain_ident" + getSecurityTable(currentUser) + "  " +
                    "WHERE true " +
                    (idsProject? "AND pro.id IN (${idsProject.join(",")}) " : "")
            //Add Security
            blocSelect += getSecurityJoin("pro.id", currentUser)
        } else if (filter.equals(SearchFilter.IMAGE)) {
            listTable.add("image_instance")
            listType.add(ImageInstance.class.getName())

            blocSelect = "SELECT DISTINCT ii.id as id, extract(epoch from ii.created)*1000 as created, '<domain>' as class, ai.original_filename as name, d.data as description, ii.user_id as user, su.lastname || ' ' || su.firstname as userfullname, pro.name as projectName, ai.filename as imageName, ai.id as baseImage, pro.id as project   " +
                    "FROM abstract_image as ai , <table> as ii LEFT OUTER JOIN description as d ON d.domain_ident = ii.id LEFT OUTER JOIN property as p ON ii.id = p.domain_ident" + getSecurityTable(currentUser) + ",  sec_user as su, project as pro " +
                    "WHERE ai.id = ii.base_image_id "  +
                    "AND ii.user_id = su.id " +
                    "AND ii.project_id = pro.id " +
                    (idsProject? "AND ii.project_id IN (${idsProject.join(",")}) " : "")

            //Add Security
            blocSelect += getSecurityJoin("ii.project_id", currentUser)
        } else if (filter.equals(SearchFilter.ANNOTATION)) {
            //There are three types of annotation
            listTable.add("user_annotation")
            listTable.add("algo_annotation")
            listTable.add("reviewed_annotation")
            listType.add(UserAnnotation.class.getName())
            listType.add(AlgoAnnotation.class.getName())
            listType.add(ReviewedAnnotation.class.getName())

            blocSelect = "SELECT DISTINCT a.id as id, extract(epoch from a.created)*1000 as created, '<domain>' as class, null as name, d.data as description, a.user_id as user, su.lastname || ' ' || su.firstname as userfullname, pro.name as projectName, ai.filename as imageName, ai.id as baseImage , pro.id as project, ii.id as image  " +
                    "FROM <table> as a LEFT OUTER JOIN description as d ON d.domain_ident = a.id LEFT OUTER JOIN property as p ON a.id = p.domain_ident" + getSecurityTable(currentUser) + ", sec_user as su, project as pro, image_instance as ii, abstract_image as ai "  +
                    "WHERE true " +
                    "AND su.id = a.user_id " +
                    "AND pro.id = a.project_id " +
                    "AND ii.base_image_id = ai.id " +
                    "AND ii.id = a.image_id " +
                    (idsProject? "AND a.project_id IN (${idsProject.join(",")}) " : "")

            //Add Security
            blocSelect += getSecurityJoin("a.project_id", currentUser)
        }

        //In case of the operator is OR
        if (operator.equals(SearchOperator.OR)) {
            for (int a = 0; a < listTable.size(); a++) {
                //Replace in String: domain and table
                String blocTmp = blocSelect.replaceAll("<domain>", listType[a])
                String blocSelectFromWhere = blocTmp.replace("<table>", listTable[a])

                if (a != 0) { request += "UNION " }

                request +=  blocSelectFromWhere
                if(!keywords.isEmpty()) {
                    request +=  "AND ( (p.value ILIKE '%" + keywords[0] + "%' OR d.data ILIKE '%" + keywords[0] + "%') "
                }

                        //"AND (p.value ILIKE '%" + keywords[0] + "%'"

                //Loop for the keywords
                for (int i = 1; i < keywords.size() ; i++) {
                    request += " OR (p.value ILIKE '%" + keywords[i] + "%' OR d.data ILIKE '%" + keywords[i]+ "%') "
                    //request += " OR p.value ILIKE '%" + keywords[i] + "%'"
                }
                if(!keywords.isEmpty()) {
                    request += ") "
                }
            }
            data = select(request)
        } else if (operator.equals(SearchOperator.AND)) {
            for (int a = 0; a < listTable.size(); a++) {
                //Replace in String: domain and table
                String blocTmp = blocSelect.replaceAll("<domain>", listType[a])
                String blocSelectFromWhere = blocTmp.replace("<table>", listTable[a])

                if (a != 0) { request += "UNION " }
//
//                request += "(" + blocSelectFromWhere +
//                        "AND p.value ILIKE '%" + keywords[0] + "%' "
//
//                //Loop for the keywords
//                for (int i = 1; i < keywords.size() ; i++) {
//                    request += "INTERSECT " +
//                            blocSelectFromWhere +
//                            "AND p.value ILIKE '%" + keywords[i] + "%' "
//                }
                request += "(" + blocSelectFromWhere
                if(!keywords.isEmpty()) {
                    request += "AND ( p.value ILIKE '%" + keywords[0] + "%' OR d.data ILIKE '%" + keywords[0] + "%') "
                }


                //Loop for the keywords
                for (int i = 1; i < keywords.size() ; i++) {
                    request += "INTERSECT " +
                            blocSelectFromWhere + " AND (p.value ILIKE '%" + keywords[i] + "%' OR d.data ILIKE '%" + keywords[i]+ "%') "
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

        def sql = new Sql(dataSource)
        sql.eachRow(request) {
            def dataTmp = [:]

            //SELECT DISTINCT pro.id as id, pro.created as created, '<domain>' as class, pro.name as name, d.data as description, null as user, null as userfullname, pro.name as projectName, null as imageName " +

            dataTmp.id = it.id
            dataTmp.created = it.created
            dataTmp.class = it.class
            dataTmp.name = it.name
            dataTmp.description = it.description
            dataTmp.user = it.user
            dataTmp.userfullname = it.userfullname
            dataTmp.projectName = it.projectName
            dataTmp.imageName = it.imageName

            if (it.class.equals(ImageInstance.class.getName())) {

               dataTmp.urlImage = UrlApi.getAbstractImageThumbURL(it.baseImage)
               dataTmp.urlGoTo =  UrlApi.getBrowseImageInstanceURL(it.project, it.id)
               dataTmp.urlApi = UrlApi.getApiURL("imageinstance",it.id)

            } else if (it.class.equals(Project.class.getName())) {

                dataTmp.urlImage = null
                dataTmp.urlGoTo =  UrlApi.getDashboardURL(it.project)
                dataTmp.urlApi = UrlApi.getApiURL("project",it.id)

            } else if (it.class.equals(UserAnnotation.class.getName()) || it.class.equals(AlgoAnnotation.class.getName()) || it.class.equals(ReviewedAnnotation.class.getName())) {

                dataTmp.urlImage = UrlApi.getAnnotationMinCropWithAnnotationId(it.id)
                dataTmp.urlGoTo =  UrlApi.getAnnotationURL(it.project, it.image, it.id)
                dataTmp.urlApi = UrlApi.getApiURL("annotation",it.id)

            }

            data.add(dataTmp)
        }
        try {
            sql.close()
        }catch (Exception e) {}
        data
    }

    static def serverUrl() {
        Holders.getGrailsApplication().config.grails.serverURL
    }



//   //Security is made ​​in sql query thanks to currentUser
//    //"getSecurityTable" and "getSecurityJoin" manage the security
//    def listDescription(List<String> keywords, String operator, String filter, def idsProject) {
//        def data = []
//        String request = ""
//        String blocSelect = ""
//        ArrayList<String> listTable = new ArrayList<String>()
//        ArrayList<String> listType = new ArrayList<String>()
//
//        SecUser currentUser = cytomineService.getCurrentUser()
//
//        //Creating queries for project or annotation or image
//        if (filter.equals(SearchFilter.PROJECT)) {
//            listTable.add("project")
//            listType.add(Project.class.getName())
//
//            blocSelect = "SELECT DISTINCT pro.id as id, extract(epoch from pro.created)*1000 as created, '<domain>' as class, pro.name as name, d.data as description, null as user, null as userfullname, pro.name as projectName, null as imageName, pro.id as project " +
//                    "FROM <table> as pro LEFT OUTER JOIN description as d ON d.domain_ident = pro.id" + getSecurityTable(currentUser) + "  " +
//                    "WHERE pro.id = p.domain_ident " +
//                    (idsProject? "AND pro.id IN (${idsProject.join(",")}) " : "")
//            //Add Security
//            blocSelect += getSecurityJoin("pro.id", currentUser)
//        } else if (filter.equals(SearchFilter.IMAGE)) {
//            listTable.add("image_instance")
//            listType.add(ImageInstance.class.getName())
//
//            blocSelect = "SELECT DISTINCT ii.id as id, extract(epoch from ii.created)*1000 as created, '<domain>' as class, ai.original_filename as name, d.data as description, ii.user_id as user, su.lastname || ' ' || su.firstname as userfullname, pro.name as projectName, ai.filename as imageName, ai.id as baseImage, pro.id as project   " +
//                    "FROM abstract_image as ai , <table> as ii LEFT OUTER JOIN description as d ON d.domain_ident = ii.id" + getSecurityTable(currentUser) + ",  sec_user as su, project as pro " +
//                    "WHERE ai.id = ii.base_image_id "  +
//                    "AND ii.id = p.domain_ident " +
//                    "AND ii.user_id = su.id " +
//                    "AND ii.project_id = pro.id " +
//                    (idsProject? "AND ii.project_id IN (${idsProject.join(",")}) " : "")
//
//            //Add Security
//            blocSelect += getSecurityJoin("ii.project_id", currentUser)
//        } else if (filter.equals(SearchFilter.ANNOTATION)) {
//            //There are three types of annotation
//            listTable.add("user_annotation")
//            listTable.add("algo_annotation")
//            listTable.add("reviewed_annotation")
//            listType.add(UserAnnotation.class.getName())
//            listType.add(AlgoAnnotation.class.getName())
//            listType.add(ReviewedAnnotation.class.getName())
//
//            blocSelect = "SELECT DISTINCT a.id as id, extract(epoch from a.created)*1000 as created, '<domain>' as class, null as name, d.data as description, a.user_id as user, su.lastname || ' ' || su.firstname as userfullname, pro.name as projectName, ai.filename as imageName, ai.id as baseImage , pro.id as project, ii.id as image  " +
//                    "FROM <table> as a LEFT OUTER JOIN description as d ON d.domain_ident = a.id" + getSecurityTable(currentUser) + ", sec_user as su, project as pro, image_instance as ii, abstract_image as ai "  +
//                    "WHERE a.id = p.domain_ident " +
//                    "AND su.id = a.user_id " +
//                    "AND pro.id = a.project_id " +
//                    "AND ii.base_image_id = ai.id " +
//                    "AND ii.id = a.image_id " +
//                    (idsProject? "AND a.project_id IN (${idsProject.join(",")}) " : "")
//
//            //Add Security
//            blocSelect += getSecurityJoin("a.project_id", currentUser)
//        }
//
//        //In case of the operator is OR
//        if (operator.equals(SearchOperator.OR)) {
//            for (int a = 0; a < listTable.size(); a++) {
//                //Replace in String: domain and table
//                String blocTmp = blocSelect.replaceAll("<domain>", listType[a])
//                String blocSelectFromWhere = blocTmp.replace("<table>", listTable[a])
//
//                if (a != 0) { request += "UNION " }
//
//                request +=  blocSelectFromWhere +
//                        "AND (d.data ILIKE '%" + keywords[0] + "%'"
//
//                //Loop for the keywords
//                for (int i = 1; i < keywords.size() ; i++) {
//                    request += " OR p.value ILIKE '%" + keywords[i] + "%'"
//                }
//                request += ") "
//            }
//            data = select(request)
//        } else if (operator.equals(SearchOperator.AND)) {
//            for (int a = 0; a < listTable.size(); a++) {
//                //Replace in String: domain and table
//                String blocTmp = blocSelect.replaceAll("<domain>", listType[a])
//                String blocSelectFromWhere = blocTmp.replace("<table>", listTable[a])
//
//                if (a != 0) { request += "UNION " }
//
//                request += "(" + blocSelectFromWhere +
//                        "AND p.value ILIKE '%" + keywords[0] + "%' "
//
//                //Loop for the keywords
//                for (int i = 1; i < keywords.size() ; i++) {
//                    request += "INTERSECT " +
//                            blocSelectFromWhere +
//                            "AND p.value ILIKE '%" + keywords[i] + "%' "
//                }
//                request += ") "
//            }
//            data = select(request)
//        }
//        data
//    }
}
