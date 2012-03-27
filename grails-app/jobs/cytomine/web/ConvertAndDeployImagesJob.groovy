package cytomine.web

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 27/03/12
 * Time: 14:58
 */
class ConvertAndDeployImagesJob {

    def imagePropertiesService

    static triggers = {
        simple name: 'convertAndDeployImagesJob', startDelay: 60000, repeatInterval: 1000*60
    }

    def execute() {


    }

}
