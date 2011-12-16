package cytomine.web

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
          if(actionName.equals("crop")) return
          request.currentTime = System.currentTimeMillis()
          String strParam =""
          params.each{
              if(!it.key.equals('action') && !it.key.equals('controller')) {
              strParam = strParam +"<" + it.key +':'+ it.value +'>; '
              }
          }
          String strPost = ""
          try {strPost = request.JSON } catch(Exception e) {}
          String requestInfo = "| PARAM="+strParam + "| POST=" + strPost + " | "
          String userInfo = ""
          try { userInfo = springSecurityService.principal.id} catch(Exception e) { userInfo = springSecurityService.principal}

          log.info controllerName+"."+actionName + ": user:" + userInfo + " request=" + requestInfo
      }
      after = {}
      afterView = {
          if(actionName.equals("crop")) return
          log.info controllerName+"."+actionName + " Request took ${System.currentTimeMillis()-request.currentTime}ms"
      }
    }
  }
}
