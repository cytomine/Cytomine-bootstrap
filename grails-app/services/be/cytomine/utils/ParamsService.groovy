package be.cytomine.utils

import be.cytomine.project.Project

/**
 * @author lrollus
 */
class ParamsService {

    def imageInstanceService
    def securityService

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
    public List<Long> getParamsImageInstanceList(String paramsImages, Project project) {
       if(paramsImages != null && !paramsImages.equals("null")) {
           if (!paramsImages.equals(""))
                    return imageInstanceService.getAllImageId(project).intersect(paramsImages.split(paramsImages.contains("_")?"_":",").collect{ Long.parseLong(it)})
           else return []
       } else {
           imageInstanceService.getAllImageId(project)
       }
    }
}
