package be.cytomine.test.http

import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import groovy.util.logging.Log
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.codehaus.groovy.grails.web.json.JSONObject

import java.awt.image.BufferedImage

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class is a root class for all xxxAPI class. These class allow to manage (get/create/update/delete/...) each domain instance easily durint test.
 * It encapsulate all HTTP request to have clean test
 *
 */
@Log
class DomainAPI {

    /**
     * Check if json list contains number id
     * @param id Number
     * @param list JSON list
     * @return True if id is in list, otherwise, false
     */
    static boolean containsInJSONList(Long id, def responselist) {
        log.info "Search $id in ${responselist}"
        if(responselist instanceof String) {
            responselist = JSON.parse(responselist)
        }


        def list = responselist.collection

        if (list == null)  {
            list = responselist.aaData
        }

        if (list == []) return false
        boolean find = false
        list.each { item ->
            Long idItem = item.id
            if ((idItem+"").equals(id+"")) {find = true}
        }
        return find
    }

    static boolean containsStringInJSONList(String key, def list) {
        log.info "Search $key in ${list}"
        list = list.collection
        if (list == []) return false
        boolean find = false
        list.each { item ->
            String strItem = item
            if (strItem.equals(key)) {find = true}
        }
        return find
    }


    /**
     * Make undo request to cytomine server
     */
    static def undo() {
        log.info("test undo")
        HttpClient client = new HttpClient()
        String URL = Infos.CYTOMINEURL + Infos.UNDOURL
        client.connect(URL, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    /**
     * Make redo request to cytomine server
     */
    static def redo() {
        log.info("test redo")
        HttpClient client = new HttpClient()
        String URL = Infos.CYTOMINEURL + Infos.REDOURL
        client.connect(URL, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }


    /**
     * Make undo request to cytomine server
     */
    static def undo(String username, String password) {
        log.info("test undo")
        HttpClient client = new HttpClient()
        String URL = Infos.CYTOMINEURL + Infos.UNDOURL
        client.connect(URL, username, password)
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    /**
     * Make redo request to cytomine server
     */
    static def redo(String username, String password) {
        log.info("test redo")
        HttpClient client = new HttpClient()
        String URL = Infos.CYTOMINEURL + Infos.REDOURL
        client.connect(URL, username, password)
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }


    static def doGET(String URL,String username,String password, HttpClient clientParam =null) {
        log.info("GET:"+URL)
        HttpClient client
        if(clientParam) {
            client = clientParam;
        } else {
            client = new HttpClient()
            log.info("Connect")
            client.connect(URL, username, password);
        }
        log.info("Get")
        client.printCookies();
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code,client:client]
    }

    static def doPOST(String URL,JSONObject json,String username,String password) {
        doPOST(URL,json.toString(),username,password)
    }

    static def doPOST(String URL,String data,String username,String password) {
        log.info("POST:"+URL)
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.post(data)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def doPUT(String URL,String data,String username,String password) {
        log.info("PUT:"+URL)
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.put(data)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def doPUT(String URL,byte[] data,String username,String password) {
        log.info("PUT:"+URL)
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.put(data)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def doDELETE(String URL,String username,String password) {
        log.info("DELETE:"+URL)
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }



    static def doPOSTUpload(String url,File file,String username,String password) throws Exception {

        MultipartEntity entity = new MultipartEntity();
        entity.addPart("files[]",new FileBody(file)) ;
        HttpClient client = new HttpClient();
        client.connect(url, username, password);
        client.post(entity)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }


    static def downloadImage(String URL,String username,String password) throws Exception {

        log.info("DOWNLOAD:"+URL)
        HttpClient client = new HttpClient();
        //BufferedImage image = client.readBufferedImageFromURLWithoutKey(URL, username, password)
        BufferedImage image = client.readBufferedImageFromURLWithRedirect(URL, username, password)
        return [image: image]
    }

    static def downloadFile(String URL,String username,String password) throws Exception {

        log.info("DOWNLOAD:"+URL)
        HttpClient client = new HttpClient();
        def data = client.readFileFromURLWithoutKey(URL, username, password)
        return [data: data]
    }





}
