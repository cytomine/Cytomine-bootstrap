package be.cytomine.job
/**
 * Refresh counter for project (annotations, images,...) and image (annotations)
 */
class RefreshCounterJob {

    def counterService

    static triggers = {
        simple name: 'refreshCounterJob', startDelay: 3600000, repeatInterval: 3600000
    }

    def execute() {
        counterService.refreshCounter()
    }
}
