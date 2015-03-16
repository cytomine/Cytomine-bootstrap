package be.cytomine.processing

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.image.server.RetrievalServer
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.test.HttpClient
import be.cytomine.utils.RetrievalHttpUtils
import be.cytomine.utils.ValueComparator
import grails.converters.JSON
import groovy.sql.Sql
import groovyx.gpars.Asynchronizer
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.ByteArrayBody
import org.apache.log4j.Logger
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.acls.model.NotFoundException

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import static org.springframework.security.acls.domain.BasePermission.READ

/**
 * Retrieval is a server that can provide similar pictures of a request picture
 * It can suggest term for an annotation thx to similar picture
 */
class ImageRetrievalService {

    static transactional = false

    public void indexImage(BufferedImage image,String id, String storage, Map<String,String> properties) {
        if(!RetrievalServer.list().isEmpty()) {
            RetrievalServer server = RetrievalServer.list().get(0)
            doRetrievalPOST(server.url+"/api/images","admin","admin",image,id,storage,properties)
        } else {
            log.info "No retrieval server found"
        }

    }



    public def doRetrievalPOST(String url, String username, String password, BufferedImage image,String id, String storage, Map<String,String> properties) {
        List<String> keys = []
        List<String> values = []
        properties.each {
            keys << it.key
            values << it.value
        }

        url = url+"?id=$id&storage=$storage&keys=${keys.join(";")}&values=${values.join(";")}"

        HttpClient client = new HttpClient()

        log.info "url=$url"
        log.info "username=$username password=$password"

        client.connect(url,username,password)

        MultipartEntity entity = createEntityFromImage(image)

        client.post(entity)

         String response = client.getResponseData()
         int code = client.getResponseCode()
         log.info "code=$code response=$response"
        return [code:code,response:response]

//        public static String getPostResponse(String URL, String resource, def jsonStr) {
//            log.info "getPostSearchResponse2"
//            HttpClient client = new HttpClient()
//            def url = URL.replace("/retrieval-web/api/resource.json",resource)
//            client.connect(url,'xxx','xxx')
//            client.post(jsonStr)
//            String response = client.getResponseData()
//            int code = client.getResponseCode()
//            log.info "code=$code response=$response"
//            return response
//        }
    }

    public MultipartEntity createEntityFromImage(BufferedImage image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();

        ////                   }
        MultipartEntity myEntity = new MultipartEntity();
        //myEntity.addPart("files[]", new ByteArrayBody(imageInByte, "file"));
        myEntity.addPart("file", new ByteArrayBody(imageInByte, "file"));
        return myEntity
    }
}
