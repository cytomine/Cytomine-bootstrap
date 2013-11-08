package be.cytomine.utils

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.sql.AnnotationListing
import groovy.sql.Sql

/**
 * @author lrollus
 *
 * This service simplify request parameters extraction in controller
 * E.g. thanks to "/api/annotation.json?users=1,5 => it will retrieve user object with 1 and 5
 */
class ParamsService {

    def imageInstanceService
    def termService
    def secUserService
    def dataSource


    /**
     * Retrieve all images id from paramsImages request string (format images=x,y,z or x_y_z)
     * Just get images from project
     */
    public List<Long> getParamsTermList(String paramsTerms, Project project) {
       if(paramsTerms != null && !paramsTerms.equals("null")) {
           if (!paramsTerms.equals(""))
                    return termService.getAllTermId(project).intersect(paramsTerms.split(paramsTerms.contains("_")?"_":",").collect{ Long.parseLong(it)})
           else return []
       } else {
           termService.getAllTermId(project)
       }
    }


    public List<String> getPropertyGroupToShow(params) {
        def propertiesToPrint = []

        //map group properties and the url params name
        def assoc = [showBasic:'basic',showMeta:'meta',showWKT:'wkt',showGIS:'gis',showTerm:'term',showImage:'image',showAlgo:'algo', showUser: 'user']

        //show if ask
        assoc.each { show, group ->
            if(params.getBoolean(show)) {
                propertiesToPrint << group
            }
        }

        //if no specific show asked show default prop
        if(params.getBoolean('showDefault') || propertiesToPrint.isEmpty()) {
            AnnotationListing.availableColumnDefault.each {
                propertiesToPrint << it
            }
            propertiesToPrint.unique()
        }

        //hide if asked
        assoc.each { show, group ->
            if(params.getBoolean(show.replace('show','hide'))) {
                propertiesToPrint = propertiesToPrint - group
            }
        }

        if(propertiesToPrint.isEmpty()) {
            throw new ObjectNotFoundException("You must ask at least one properties group for request.")
        }

        propertiesToPrint
    }

}
