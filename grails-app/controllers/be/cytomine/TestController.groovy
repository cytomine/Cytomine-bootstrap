package be.cytomine

class TestController {

    def bootstrapTestDataService

    def index() {}


    def insert() {
        bootstrapTestDataService.initSoftwareAndJobTemplate(params.long('project'),params.long('term'))
    }
}
