package be.cytomine.utils

import be.cytomine.CytomineDomain
import be.cytomine.image.AbstractImage
import be.cytomine.image.acquisition.Instrument
import be.cytomine.image.server.Storage
import be.cytomine.laboratory.Sample
import be.cytomine.ontology.AnnotationProperty
import be.cytomine.security.User
import be.cytomine.image.Mime
import grails.converters.JSON

import org.apache.commons.logging.LogFactory
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.security.UserJob
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.AnnotationDomain
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Discipline
import be.cytomine.security.Group
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.processing.Job
import be.cytomine.processing.JobData
import be.cytomine.processing.JobParameter
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.Term
import be.cytomine.processing.Software
import be.cytomine.processing.SoftwareParameter
import be.cytomine.ontology.AnnotationFilter
import be.cytomine.test.BasicInstanceBuilder

/**
 * User: lrollus
 * Date: 8/01/13
 * GIGA-ULg
 *
 */
class UpdateData {

    private static final log = LogFactory.getLog(this)


    static def createUpdateSet(CytomineDomain domain,def maps) {
         def mapOld = [:]
         def mapNew = [:]

        maps.each {
            String key = it.key
            mapOld[key] = extractValue(it.value[0])
            domain[key] = it.value[0]
        }

        BasicInstanceBuilder.saveDomain(domain)



        def json = JSON.parse(domain.encodeAsJSON())

        maps.each {
            String key = it.key
            mapNew[key] = extractValue(it.value[1])
            json[key] = extractValue(it.value[1])
        }

        println domain.encodeAsJSON()
        println domain.encodeAsJSON()

        println "mapOld="+mapOld
        println "mapNew="+mapNew

        return ['postData':json.toString(),'mapOld':mapOld,'mapNew':mapNew]


    }

    static extractValue(def value) {
        println "extractValue=$value"
        println "extractValue.class="+value.class
        println "extractValue.class.isInstance="+value.class.isInstance(CytomineDomain)
        if (value.class.toString().contains("be.cytomine")) {
            //if cytomine domain, get its id
            return value.id
        } else {
            return value
        }
    }
}

