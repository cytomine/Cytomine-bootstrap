package be.cytomine.utils

import be.cytomine.project.Project
import be.cytomine.security.SecUser
import groovy.sql.Sql

/**
 * @author lrollus
 */
class ParamsService {

    def imageInstanceService
    def securityService
    def userService
    def dataSource

    //=> utility service
    public List<Long> getParamsUserList(String paramsUsers, Project project) {
       if(paramsUsers != null && !paramsUsers.equals("null")) {
           if (!paramsUsers.equals(""))
               return securityService.getAllowedUserIdList(project).intersect(paramsUsers.split(paramsUsers.contains("_")?"_":",").collect{ Long.parseLong(it)})
           else return []
       } else {
           securityService.getAllowedUserIdList(project)
       }
    }
    public List<Long> getParamsSecUserList(String paramsUsers, Project project) {
       if(paramsUsers != null && !paramsUsers.equals("null")) {
           if (!paramsUsers.equals(""))
               return getUserIdList(paramsUsers.split(paramsUsers.contains("_")?"_":",").collect{ Long.parseLong(it)})
           else return []
       } else {
           securityService.getAllowedUserIdList(project)
       }
    }

    public List<Long> getParamsImageInstanceList(String paramsImages, Project project) {
       if(paramsImages != null && !paramsImages.equals("null")) {
           if (!paramsImages.equals(""))
                    return getAllImageId(project).intersect(paramsImages.split(paramsImages.contains("_")?"_":",").collect{ Long.parseLong(it)})
           else return []
       } else {
           getAllImageId(project)
       }
    }

    public List<Long> getParamsTermList(String paramsTerms, Project project) {
       if(paramsTerms != null && !paramsTerms.equals("null")) {
           if (!paramsTerms.equals(""))
                    return getAllTermId(project).intersect(paramsTerms.split(paramsTerms.contains("_")?"_":",").collect{ Long.parseLong(it)})
           else return []
       } else {
           getAllTermId(project)
       }
    }

    public List<SecUser> getParamsSecUserDomainList(String paramsUsers, Project project) {
        List<SecUser> userList = []
        if (paramsUsers != null && paramsUsers != "null" && paramsUsers != "") {
            userList = userService.list(project, paramsUsers.split("_").collect{ Long.parseLong(it)})
        }
        return userList
    }


    public List<Long> getUserIdList(List<Long> users) {
        String request = "SELECT DISTINCT sec_user.id \n" +
                " FROM sec_user \n" +
                " WHERE id IN ("+users.join(",")+")"
        def data = []
        new Sql(dataSource).eachRow(request) {
            data << it[0]
        }
        return data
    }

    public List<Long> getAllImageId(Project project) {
        String request = "SELECT a.id FROM image_instance a WHERE project_id="+project.id
        def data = []
        new Sql(dataSource).eachRow(request) {
            data << it[0]
        }
        return data
    }

    public List<Long> getAllTermId(Project project) {
        String request = "SELECT t.id FROM term t WHERE t.ontology_id="+project.ontology.id
        def data = []
        new Sql(dataSource).eachRow(request) {
            data << it[0]
        }
        return data
    }

}
