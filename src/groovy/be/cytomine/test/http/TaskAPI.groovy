package be.cytomine.test.http

import be.cytomine.test.Infos

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Annotation to Cytomine with HTTP request during functional test
 */
class TaskAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/task/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long idProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$idProject/task/comment.json"
        return doGET(URL, username, password)
    }

    static def create(Long idProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/task.json"
        def result = doPOST(URL,"{project:${idProject}}",username,password)
        return result
    }
}
