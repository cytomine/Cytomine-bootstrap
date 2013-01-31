package be.cytomine.utils

import be.cytomine.project.Project
import be.cytomine.security.SecUser
import groovy.sql.Sql

/**
 * @author lrollus
 *
 * This service simplify request parameters extraction in controller
 * E.g. thanks to "/api/annotation.json?users=1,5 => it will retrieve user object with 1 and 5
 */
class ParamsService {

    def imageInstanceService
    def termService
    def secUserService
    def dataSource

    /**
     * Retrieve all user id from paramsUsers request string (format users=x,y,z or x_y_z)
     * Just get user from project
     */
    public List<Long> getParamsUserList(String paramsUsers, Project project) {
       if(paramsUsers != null && !paramsUsers.equals("null")) {
           if (!paramsUsers.equals(""))
               return secUserService.getAllowedUserIdList(project).intersect(paramsUsers.split(paramsUsers.contains("_")?"_":",").collect{ Long.parseLong(it)})
           else return []
       } else {
           secUserService.getAllowedUserIdList(project)
       }
    }

    /**
     * Retrieve all user and userjob id from paramsUsers request string (format users=x,y,z or x_y_z)
     * Just get user and user job  from project
     */
    public List<Long> getParamsSecUserList(String paramsUsers, Project project) {
       if(paramsUsers != null && !paramsUsers.equals("null")) {
           if (!paramsUsers.equals(""))
               return getUserIdList(paramsUsers.split(paramsUsers.contains("_")?"_":",").collect{ Long.parseLong(it)})
           else return []
       } else {
           secUserService.getAllowedUserIdList(project)
       }
    }

    /**
     * Retrieve all images id from paramsImages request string (format images=x,y,z or x_y_z)
     * Just get images from project
     */
    public List<Long> getParamsImageInstanceList(String paramsImages, Project project) {
       if(paramsImages != null && !paramsImages.equals("null")) {
           if (!paramsImages.equals(""))
                    return imageInstanceService.getAllImageId(project).intersect(paramsImages.split(paramsImages.contains("_")?"_":",").collect{ Long.parseLong(it)})
           else return []
       } else {
           imageInstanceService.getAllImageId(project)
       }
    }

    /**
     * Retrieve all images id from paramsImages request string (format images=x,y,z or x_y_z)
     * Just get images from project
     */
    public List<Long> getParamsTermList(String paramsTerms, Project project) {
       if(paramsTerms != null && !paramsTerms.equals("null")) {
           if (!paramsTerms.equals(""))
                    return termService.getAllTermId(project).intersect(paramsTerms.split(paramsTerms.contains("_")?"_":",").collect{ Long.parseLong(it)})
           else return []
       } else {
           termService.getAllTermId(project)
       }
    }

    /**
     * Retrieve all user and userjob object from paramsUsers request string (format users=x,y,z or x_y_z)
     * Just get user and user job  from project
     */
    public List<SecUser> getParamsSecUserDomainList(String paramsUsers, Project project) {
        List<SecUser> userList = []
        if (paramsUsers != null && paramsUsers != "null" && paramsUsers != "") {
            userList = secUserService.list(project, paramsUsers.split("_").collect{ Long.parseLong(it)})
        }
        return userList
    }

    private List<Long> getUserIdList(List<Long> users) {
        String request = "SELECT DISTINCT sec_user.id \n" +
                " FROM sec_user \n" +
                " WHERE id IN ("+users.join(",")+")"
        def data = []
        new Sql(dataSource).eachRow(request) {
            data << it[0]
        }
        return data
    }
}
