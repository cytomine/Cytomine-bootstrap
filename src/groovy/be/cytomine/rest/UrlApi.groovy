package be.cytomine.rest

import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 1/03/11
 * Time: 13:33
 * To change this template use File | Settings | File Templates.
 */
class UrlApi {

  static def getImageURLWithProjectId(Long idProject) {
    return ConfigurationHolder.config.grails.serverURL + '/api/project/'+ idProject +'/image.json';
  }

  static def getTermsURLWithOntologyId(Long idOntology) {
    return ConfigurationHolder.config.grails.serverURL + '/api/ontology/'+ idOntology +'/term.json';
  }

  static def getTermsURLWithAnnotationId(Long idAnnotation) {
    return ConfigurationHolder.config.grails.serverURL + '/api/annotation/'+ idAnnotation +'/term.json';
  }

  static def getTermsURLWithImageId(Long idImage) {
    return ConfigurationHolder.config.grails.serverURL + '/api/image/'+ idImage +'/term.json';
  }

}
