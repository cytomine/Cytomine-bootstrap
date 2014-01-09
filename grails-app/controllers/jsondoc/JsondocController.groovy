package jsondoc

import grails.converters.JSON

class JsondocController {

    def index() {
    }

    def api() {
        render(ApiRegistry.jsondoc as JSON)
    }
}
