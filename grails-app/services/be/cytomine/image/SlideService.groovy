package be.cytomine.image

import be.cytomine.ModelService
import be.cytomine.project.Slide
import be.cytomine.security.User
import grails.orm.PagedResultList

class SlideService extends ModelService {

    static transactional = true

    def list() {
        Slide.list()
    }

    def list(User user) {
        user.slides()
    }

    PagedResultList list(User user, def page, def limit, def sortedRow, def sord) {
        def data = [:]
        log.info "page=" + page + " limit=" + limit + " sortedRow=" + sortedRow + " sord=" + sord
        int pg = Integer.parseInt(page) - 1
        int max = Integer.parseInt(limit)
        int offset = pg * max
        PagedResultList results = user.slides(max, offset, sortedRow, sord)

        data.page = pg + ""
        data.records = results.totalCount
        data.total = Math.ceil(results.totalCount / max) + "" //[100/10 => 10 page]
        data.rows = results.list
        return data
    }

    def read(def id) {
        Slide.read(id)
    }

    def add(def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    def update(def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    def delete(def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
