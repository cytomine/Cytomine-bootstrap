package be.cytomine.utils

import be.cytomine.project.Project
import be.cytomine.security.SecUser

/**
 * @author lrollus
 */
class ParamsService {

    def imageInstanceService
    def securityService
    def userService

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
               return securityService.getUserIdList(paramsUsers.split(paramsUsers.contains("_")?"_":",").collect{ Long.parseLong(it)})
           else return []
       } else {
           securityService.getAllowedUserIdList(project)
       }
    }

    public List<Long> getParamsImageInstanceList(String paramsImages, Project project) {
       if(paramsImages != null && !paramsImages.equals("null")) {
           if (!paramsImages.equals(""))
                    return imageInstanceService.getAllImageId(project).intersect(paramsImages.split(paramsImages.contains("_")?"_":",").collect{ Long.parseLong(it)})
           else return []
       } else {
           imageInstanceService.getAllImageId(project)
       }
    }

    public List<SecUser> getParamsSecUserDomainList(String paramsUsers, Project project) {
        List<SecUser> userList = []
        if (paramsUsers != null && paramsUsers != "null" && paramsUsers != "") {
            userList = userService.list(project, paramsUsers.split("_").collect{ Long.parseLong(it)})
        }
        return userList
    }
}
