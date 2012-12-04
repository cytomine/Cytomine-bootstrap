package be.cytomine

import com.vividsolutions.jts.geom.Geometry
import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser
import be.cytomine.project.Project
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.AlgoAnnotationTerm
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.Exception.WrongArgumentException
import org.apache.log4j.Logger
import grails.converters.JSON
import be.cytomine.api.UrlApi
import be.cytomine.ontology.Term

/**
 * User: lrollus
 * Date: 18/10/12
 * GIGA-ULg
 * 
 */
abstract class AnnotationDomain extends CytomineDomain implements Serializable {

    Geometry location
    ImageInstance image
    Project project
    Double geometryCompression
    long countComments = 0L

    /* Transients values for JSON/XML rendering */
    Double similarity
    Double rate
    Long idTerm
    Long idExpectedTerm


    static belongsTo = [ImageInstance, Project]

    static transients = ["boundaries", "similarity","rate", "idTerm", "idExpectedTerm"]

    static constraints = {
        location(nullable: false)
        geometryCompression(nullable: true)
        project(nullable:true)
    }

    static mapping = {
        tablePerHierarchy false
        id generator: "assigned"
        columns {
            location type: org.hibernatespatial.GeometryUserType
        }
    }

    abstract def terms()
    abstract def termsId()
    abstract boolean isAlgoAnnotation()
    abstract boolean isReviewedAnnotation()
    abstract List<Term> termsForReview()
    abstract def getCropUrl(String cytomineUrl)

    String toString() {return "Annotation " + id}

    /**
     * If name is empty, fill it by "Annotation $id"
     */
    public beforeInsert() {
        super.beforeInsert()
        project = image.project
    }



    def getFilename() {
          return this.image?.baseImage?.getFilename()
      }

    Project projectDomain() {
        return image?.project
    }

    def getArea() {
        //TODO: must be compute with zoom level
        return location.area
    }

    def getPerimeter() {
        //TODO: must be compute with zoom level
        return location.getLength()
    }

    def getCentroid() {
        if (location.area < 1) return null
        def centroid = location.getCentroid()
        def response = [:]
        response.x = centroid.x
        response.y = centroid.y
        return response
    }

   def getBoundaries() {
       //get num points
       if (location.getNumPoints()>3) {
           Coordinate[] coordinates = location.getEnvelope().getCoordinates()
           int topLeftX = coordinates[3].x
           int topLeftY = coordinates[3].y
           //int topLeftY = Integer.parseInt(metadata.height) - coordinates[3].y
           int width = coordinates[1].x - coordinates[0].x
           int height = coordinates[3].y - coordinates[0].y

           //log.debug "topLeftX :" + topLeftX + " topLeftY :" + topLeftY + " width :" + width + " height :" + height
           return [topLeftX: topLeftX, topLeftY: topLeftY, width: width, height: height]
       } else throw new be.cytomine.Exception.InvalidRequestException("Cannot make a crop for a POINT")

    }

    def toCropURL() {
        def boundaries = getBoundaries()
        return image.baseImage.getCropURL(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height)
    }

    def toCropURLWithMaxSize(int maxSize) {
        def boundaries = getBoundaries()
        return image.baseImage.getCropURLWithMaxWithOrHeight(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height, maxSize,maxSize)
    }

    def toCropURL(int zoom) {
        def boundaries = getBoundaries()
        return image.baseImage.getCropURL(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height, zoom)
    }

    def computeArea() {
        if (this.image.baseImage.resolution == null) return Math.round(this.getArea())// + " pixels²"
        else return Math.round(this.getArea() * this.image.baseImage.resolution)// + " µm²"
    }

    def computePerimeter() {
        if (this.image.baseImage.resolution == null) return Math.round(this.getPerimeter())// + " pixels"
        else return Math.round(this.getPerimeter() * this.image.baseImage.resolution)// + " µm"
    }

    def getCallBack() {
        return [annotationID: this.id, imageID: this.image.id]

    }

}
