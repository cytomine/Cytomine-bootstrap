package be.cytomine.api.project

import grails.converters.*
import be.cytomine.project.Annotation
import be.cytomine.project.Scan
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.geom.Geometry

class RestAnnotationController {

  def index = { }



  def list = {

    List<Annotation> data = Annotation.list()
    HashMap jsonMap = getAnnotationsMap(data)


    withFormat {
      json { render jsonMap as JSON }
      xml { render jsonMap as XML}
    }

  }

  def show = {
    Annotation annotation = Annotation.get(params.idannotation)
    def data = []
    data.add(annotation)
    HashMap jsonMap = getAnnotationsMap(data)

    withFormat {
      json { render jsonMap as JSON }
      xml { render jsonMap as XML}
    }

  }

  def scanlist = {
    // List<Annotation> data = Annotation.findAllByScan(Integer.parseInt(params.idscan))

    Scan scan =  Scan.get(params.idscan)
    if((scan==null)) println "Scan is null"
    else println "Scan is not null"
    println "Search annotation from " + scan.filename
    def data = Annotation.findAllByScan(scan)

    HashMap jsonMap = getAnnotationsMap(data)

    withFormat {
      json { render jsonMap as JSON }
      xml { render jsonMap as XML}
    }

  }

  def add = {
    println params.annotation
    Scan scan =  Scan.get(params.idscan)
    if((scan==null)) println "Scan is null"
    else println "Scan is not null"

    Geometry geom = new WKTReader().read(params.annotation);
    Annotation annotation = new Annotation(name: "toto", location:geom, scan:scan)

    if(annotation.validate())
    {
      annotation.save(flush:true)
    }
    else
    {
      println("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
      scan.errors.each {
        err -> println err
      }
    }
  }



  /* Take a List of annotation(s) and return a Map of annotation with only some attribute.
  *  Avoid that the converter go into the geometry object.
  * */
  def getAnnotationsMap(annotationList) {
    if(annotationList==null || annotationList.size()==0)
    {
      new HashMap()
    }
    else
    {
      HashMap jsonMap = new HashMap()
      jsonMap.annotations = annotationList.collect {ann ->
        return [id: ann.id, name: ann.name, location: ann.location.toString(), scan: ann.scan.id]
      }
      jsonMap
    }
  }

}
