package be.cytomine.utils

import be.cytomine.CytomineDomain
import grails.converters.JSON

import org.apache.commons.logging.LogFactory
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

