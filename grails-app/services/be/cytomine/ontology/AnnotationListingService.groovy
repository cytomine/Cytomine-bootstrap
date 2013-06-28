package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.SecurityACL
import be.cytomine.api.UrlApi
import be.cytomine.command.*
import be.cytomine.image.ImageInstance
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.UserJob
import be.cytomine.sql.AnnotationListing
import be.cytomine.sql.UserAnnotationListing
import be.cytomine.utils.GeometryUtils
import be.cytomine.utils.GisUtils
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTWriter
import groovy.sql.Sql

import static org.springframework.security.acls.domain.BasePermission.READ

class AnnotationListingService extends ModelService {

    static transactional = true

    def cytomineService
    def transactionService
    def dataSource
    def kmeansGeometryService


    def listGeneric(AnnotationListing al) {
        println "p=${al.kmeansValue}"
        println "kmeans=${al.kmeans}"
        println "al.vontainer=${al.container()}"
        SecurityACL.check(al.container(),READ)
        if(al.kmeans && !al.kmeansValue) {
            if(!al.image || !al.bbox) {
                throw new WrongArgumentException("If you want to use kmeans, you must provide image (=${al.image}, bbox (=${al.bbox})")
            }
            def rule = kmeansGeometryService.mustBeReduce(al.image,al.user,al.bbox)
            al.kmeansValue = rule
            println "s=${al.kmeansValue}"
        } else {
            //no kmeans
            al.kmeansValue = kmeansGeometryService.FULL
        }
        executeRequest(al)
    }

    def executeRequest(AnnotationListing al) {


        println "x=${al.kmeansValue}"
        if(al.kmeansValue==kmeansGeometryService.FULL) {






























            selectGenericAnnotation(al)
        } else if(al.kmeansValue==kmeansGeometryService.KMEANSFULL) {
            kmeansGeometryService.doKeamsFullRequest(al.getAnnotationsRequest())
        } else {
            kmeansGeometryService.doKeamsSoftRequest(al.getAnnotationsRequest())
        }

    }

    /**
     * Execute request and format result into a list of map
     */
      def selectGenericAnnotation(AnnotationListing al) {

          def data = []
          long lastAnnotationId = -1
          long lastTermId = -1
          boolean first = true;

          def realColumn = []
         def request = al.getAnnotationsRequest()
          println  request
          println dataSource
          boolean termAsked = false

          new Sql(dataSource).eachRow(request) {
              /**
               * If an annotation has n multiple term, it will be on "n" lines.
               * For the first line for this annotation (it.id!=lastAnnotationId), add the annotation data,
               * For the other lines, we add term data to the last annotation
               */
              if (it.id != lastAnnotationId) {
                  if(first) {
                      al.getAllPropertiesName().each { columnName ->
                            if(columnExist(it,columnName)) {
                                realColumn << columnName
                            }
                      }
                      first = false
                  }


                  def item = [:]
                  item['class'] = al.getDomainClass()

                  realColumn.each { columnName ->
                      item[columnName]=it[columnName]
                  }



                  if(al.columnToPrint.contains('term')) {
                      termAsked = true
                      item['term'] = (it.term ? [it.term] : [])
                      item['userByTerm'] = (it.term ? [[id: it.annotationTerms, term: it.term, user: [it.userTerm]]] : [])
                  }

                  if(al.columnToPrint.contains('image')) {
                      item['originalfilename'] = (it.originalfilename ? it.originalfilename : null)
                  }

                  if(al.columnToPrint.contains('gis')) {
                      item['perimeterUnit'] = (it.perimeterUnit ? GisUtils.retrieveUnit(it.perimeterUnit) : null)
                      item['areaUnit'] = (it.areaUnit ? GisUtils.retrieveUnit(it.areaUnit) : null)
                  }

                  if(al.columnToPrint.contains('meta')) {
                      if(al.getClass().name.contains("UserAnnotation")) {
                         item['cropURL'] = UrlApi.getUserAnnotationCropWithAnnotationId(it.id)
                         item['smallCropURL'] = UrlApi.getUserAnnotationCropWithAnnotationIdWithMaxWithOrHeight(it.id, 256)
                         item['url'] = UrlApi.getUserAnnotationCropWithAnnotationId(it.id)
                         item['imageURL'] = UrlApi.getAnnotationURL(it.project, it.image, it.id)
                      } else if(al.getClass().name.contains("AlgoAnnotation")) {
                          item['cropURL'] = UrlApi.getAlgoAnnotationCropWithAnnotationId(it.id)
                          item['smallCropURL'] = UrlApi.getAlgoAnnotationCropWithAnnotationIdWithMaxWithOrHeight(it.id, 256)
                          item['url'] = UrlApi.getAlgoAnnotationCropWithAnnotationId(it.id)
                          item['imageURL'] = UrlApi.getAnnotationURL(it.project, it.image, it.id)
                      }else if(al.getClass().name.contains("ReviewedAnnotation")) {
                            item['cropURL'] = UrlApi.getReviewedAnnotationCropWithAnnotationId(it.id)
                            item['smallCropURL'] = UrlApi.getReviewedAnnotationCropWithAnnotationIdWithMaxWithOrHeight(it.id, 256)
                            item['url'] = UrlApi.getReviewedAnnotationCropWithAnnotationId(it.id)
                            item['imageURL'] = UrlApi.getAnnotationURL(it.project, it.image, it.id)
                      }
                  }
                  data << item
              } else {
                  if (it.term) {
                      data.last().term.add(it.term)
                      data.last().term.unique()
                      if (it.term == lastTermId) {
                          data.last().userByTerm.last().user.add(it.userTerm)
                          data.last().userByTerm.last().user.unique()
                      } else {
                          data.last().userByTerm.add([id: it.annotationTerms, term: it.term, user: [it.userTerm]])
                      }
                  }
              }
              if (termAsked) {
                  lastTermId = it.term
                 lastAnnotationId = it.id
              }

          }
          data
      }



}
