package be.cytomine.utils

import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import grails.util.Holders
import groovy.sql.Sql

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
