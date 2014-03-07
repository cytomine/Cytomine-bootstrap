package be.cytomine.job
/**
 * Refresh counter for project (annotations, images,...) and image (annotations)
 */
class RefreshCounterJob {

    def counterService

    static triggers = {
        simple name: 'generateMissingKeysJob', startDelay: 60000, repeatInterval: 60000*60
    }

    def execute() {
        counterService.refreshCounter()
    }
}
