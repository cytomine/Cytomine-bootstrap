package be.cytomine.test.http

import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody

import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class is a root class for all xxxAPI class. These class allow to manage (get/create/update/delete/...) each domain instance easily durint test.
 * It encapsulate all HTTP request to have clean test
 *
 */
class DomainAPI {

    private static final log = LogFactory.getLog(this)

    /**
     * Check if json list contains number id
     * @param id Number
     * @param list JSON list
     * @return True if id is in list, otherwise, false
     */
    static boolean containsInJSONList(Long id, def list) {
        println "Search $id in ${list}"
        if(list instanceof String) {
            list = JSON.parse(list)
        }
        list = list.collection
        if (list == []) return false
        boolean find = false
        list.each { item ->
            Long idItem = item.id
            println "idItem=$idItem id=$id equals=${(idItem+"").equals(id+"")}"
            if ((idItem+"").equals(id+"")) {find = true}
        }
        return find
    }

    static boolean containsStringInJSONList(String key, def list) {
        println "Search $key in ${list}"
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
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println "undo = " + response
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
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println "redo = " + response
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
        println "undo = " + response
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
        println "redo = " + response
        client.disconnect();
        return [data: response, code: code]
    }


    static def doGET(String URL,String username,String password) {
        log.info("GET:"+URL)
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
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







}
