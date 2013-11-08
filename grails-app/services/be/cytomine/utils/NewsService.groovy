package be.cytomine.utils
/**
 * Created with IntelliJ IDEA.
 * User: pierre
 * Date: 15/04/13
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */

class NewsService extends ModelService {

    def list() {
        return News.list()
    }
}
