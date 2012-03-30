package be.cytomine.test.http

import be.cytomine.image.AbstractImage
import be.cytomine.image.Mime
import be.cytomine.image.acquisition.Scanner
import be.cytomine.project.Slide
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import be.cytomine.image.AbstractImageGroup

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 *
 */
class AbstractImageGroupAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def listByImage(Long id,String username, String password) {
        log.info("list AbstractImage")
        String URL = Infos.CYTOMINEURL + "api/image/$id/group.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByGroup(Long id,String username, String password) {
        log.info("list AbstractImage")
        String URL = Infos.CYTOMINEURL + "api/group/$id/image.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }


    static def show(Long idImage, Long idGroup, String username, String password) {
        log.info("show AbstractImage:" + idImage + " idGroup:"+idGroup)
        String URL = Infos.CYTOMINEURL + "api/image/" + idImage + "/group/" + idGroup + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }


    static def create(AbstractImageGroup AbstractImageGroup, User user) {
       create(AbstractImageGroup.encodeAsJSON(),user.username,user.password)
    }


    static def create(AbstractImageGroup AbstractImageGroupToAdd, String username, String password) {
        return create(AbstractImageGroupToAdd.abstractimage.id,AbstractImageGroupToAdd.group.id, AbstractImageGroupToAdd.encodeAsJSON(), username, password)
    }

    static def create(Long idImage, Long idGroup,String jsonAbstractImageGroup, String username, String password) {
        log.info("post AbstractImageGroup:" + jsonAbstractImageGroup.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/image/$idImage/group/$idGroup" + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(jsonAbstractImageGroup)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        def json = JSON.parse(response)
        Long idAbstractImageGroup = json?.abstractimagegroup?.id
        return [data: AbstractImageGroup.get(idAbstractImageGroup), code: code]
    }


    static def delete(Long idImage, Long idGroup,String username, String password) {
        log.info("delete AbstractImageGroup")
        String URL = Infos.CYTOMINEURL + "api/image/$idImage/group/$idGroup" + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
