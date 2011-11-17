package cytomine.web

import be.cytomine.api.RestController
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 19/08/11
 * Time: 9:49
 * To change this template use File | Settings | File Templates.
 */

class RequestFilters {

  def springSecurityService

  def filters = {
    all(uri:'/api/**') {
      before = {
          request.currentTime = System.currentTimeMillis()
          String strParam =""
          params.each{ strParam = strParam +"<" + it.key +':'+ it.value +'>; ' }
          String strPost = ""
          try {strPost = request.JSON } catch(Exception e) {}
          String requestInfo = "| PARAM="+strParam + "| POST=" + strPost + " | "
          log.info controllerName+"."+actionName + ": user:" + springSecurityService.principal.id + " request=" + requestInfo
      }
      after = {}
      afterView = {
          log.info controllerName+"."+actionName + " Request took ${System.currentTimeMillis()-request.currentTime}ms"
      }
    }
  }
}
