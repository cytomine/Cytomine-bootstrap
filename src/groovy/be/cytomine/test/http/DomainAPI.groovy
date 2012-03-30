package be.cytomine.test.http

import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import org.apache.commons.logging.LogFactory
/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * 
 */
class DomainAPI {

    private static final log = LogFactory.getLog(this)

   static boolean containsInJSONList(Long id, def list) {
       println "id="+id + " list="+list
       if(list==[]) return false
       boolean find = false
      list.each { item ->
          Long idItem=item.id
         if(idItem==id) {find=true}

      }
       return find
   }

    static def undo() {
        log.info("test undo")
        HttpClient client = new HttpClient()
        String  URL = Infos.CYTOMINEURL + Infos.UNDOURL
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def redo() {
        log.info("test undo")
        HttpClient client = new HttpClient()
        String  URL = Infos.CYTOMINEURL + Infos.REDOURL
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
