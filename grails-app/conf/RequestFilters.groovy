
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 19/08/11
 * Time: 9:49
 * To change this template use File | Settings | File Templates.
 */

class RequestFilters {
  def filters = {
    all(uri:'/api/**') {
      before = {request.currentTime = System.currentTimeMillis()}
      after = {}
      afterView = {
          log.info "Request took ${System.currentTimeMillis()-request.currentTime}ms"
      }
    }
  }
}
