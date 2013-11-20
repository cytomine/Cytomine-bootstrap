package be.cytomine.test.http

import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage ImageInstance to Cytomine with HTTP request during functional test
 */
class ImageInstanceAPI extends DomainAPI {

    static def listByProject(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/imageinstance.json"
        return doGET(URL, username, password)
    }

    static def listByUser(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/$id/imageinstance/light.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, int offset, int max, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/imageinstance.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, Long inf, Long sup,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/imageinstance.json?inf=$inf&sup=$sup"
        return doGET(URL, username, password)
    }

    static def listByProjectTree(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/imageinstance.json?tree=true"
        return doGET(URL, username, password)
    }

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def listLastOpened(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/lastopened.json"
        return doGET(URL, username, password)
    }



    static def create(String jsonImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance.json"
        def result = doPOST(URL,jsonImageInstance,username,password)
        result.data = ImageInstance.get(JSON.parse(result.data)?.imageinstance?.id)
        return result
    }

    static def update(def id, def jsonImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + id + ".json"
        return doPUT(URL,jsonImageInstance,username,password)
    }

    static def delete(ImageInstance image, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + image.id + ".json"
        return doDELETE(URL,username,password)
    }

    static def next(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + id + "/next.json"
        return doGET(URL, username, password)
    }

    static def previous(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + id + "/previous.json"
        return doGET(URL, username, password)
    }

    static def sameImageData(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + id + "/sameimagedata.json"
        return doGET(URL, username, password)
    }

    static def copyImageData(Long id, def layers,def idTask,String username, String password) {
        copyImageData(id,false,layers,idTask,username,password)
    }

    static def copyImageData(Long id, boolean giveMe,def layers,def idTask,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + id + "/copyimagedata.json?layers="+layers.collect{it.image.id+"_"+it.user.id}.join(",")  + (idTask? "&task=$idTask" : "")  + (giveMe? "&giveMe=$giveMe" : "")
        return doPOST(URL,"", username, password)
    }


//    "/api/imageinstance/$id/sameimagedata"(controller :"restImageInstance") {
//        action = [GET:"retrieveSameImageOtherProject"]
//    }
//    "/api/imageinstance/$id/copyimagedata"(controller :"restImageInstance") {
//        action = [POST:"copyAnnotationFromSameAbstractImage"]
//    }




    static ImageInstance buildBasicImage(String username, String password) {
        //Create project with user 1
        def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(), username, password)
        assert 200==result.code
        Project project = result.data
        //Add image with user 1
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist()
        image.project = project
        result = ImageInstanceAPI.create(image.encodeAsJSON(), username, password)
        assert 200==result.code
        image = result.data
        return image
    }

}
