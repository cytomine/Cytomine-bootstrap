package be.cytomine.test.http

import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import org.apache.commons.logging.LogFactory
import be.cytomine.CytomineDomain

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
        if (list == []) return false
        boolean find = false
        list.each { item ->
            Long idItem = item.id
            if (idItem == id) {find = true}
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

    static def doPOST(String URL,def data,String username,String password) {
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
}
