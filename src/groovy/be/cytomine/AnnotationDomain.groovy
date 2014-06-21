package be.cytomine

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.processing.RoiAnnotation
import be.cytomine.project.Project
import be.cytomine.utils.GisUtils
import com.vividsolutions.jts.geom.Envelope
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import groovy.util.logging.Log
import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObjectFields
import org.restapidoc.annotation.RestApiObject

/**
 * User: lrollus
 * Date: 18/10/12
 * GIGA-ULg
 *
 * Annotation generic domain
 * Annotation can be:
 * -UserAnnotation => created by human user
 * -AlgoAnnotation => created by job
 * -ReviewedAnnotation => User or AlgoAnnotation validate by user
 */
@Log
@RestApiObject(name = "generic annotation")
abstract class AnnotationDomain extends CytomineDomain implements Serializable {


    /**
     * Annotation geometry object
     */
    @RestApiObjectField(description = "The WKT of the annotation form", allowedType = "string")
    Geometry location

    /**
     * Annotation image
     */
    @RestApiObjectField(description = "The image id of the annotation")
    ImageInstance image

    /**
     * Annotation project
     * Redundant with image.project, speedup
     */
    @RestApiObjectField(description = "The project id of the annotation")
    Project project

    /**
     * Compression threshold used for annotation simplification
     */
    @RestApiObjectField(description = "The geometry compression rate used to simplify the annotation (during creation)", mandatory = false)
    Double geometryCompression

    /**
     * Number of comments for annotation
     * Redundant to speed up
     */
    @RestApiObjectField(description = "The number of comments added by a user on this annotation", apiFieldName = "nbComments", useForCreation = false)
    long countComments = 0L

    /**
     * Annotation geometry WKT location
     * Redundant, better to use this than getting WKT from location properties
     */
    String wktLocation  //speedup listing

    /* Transients values for JSON/XML rendering */
    //TODO:: remove from here, use custom SQL request with these info
    @RestApiObjectField(description = "The similarity rate for this annotation compare to another annotation (from retrieval)", useForCreation = false)
    Double similarity

    @RestApiObjectField(description = "The reliability value estimated by the software for the mapping between annotation and term", useForCreation = false)
    Double rate

    @RestApiObjectField(description = "The id of the term map with this annotation by a the software", useForCreation = false)
    Long idTerm

    @RestApiObjectField(description = "The id of the real term (corresponding to the term add by a real user)", useForCreation = false)
    Long idExpectedTerm

    @RestApiObjectField(description = "The annotation form area", useForCreation = false)
    Double area

    @RestApiObjectField(description = "The annotation form perimeter", useForCreation = false)
    Double perimeter

    @RestApiObjectField(description = "The annotation unit used for area (pixels²=1,micron²=3)", useForCreation = false)
    Integer areaUnit

    @RestApiObjectField(description = "The annotation unit used for perimeter (pixels=0,mm=2,)", useForCreation = false)
    Integer perimeterUnit


    static belongsTo = [ImageInstance, Project]

    @RestApiObjectFields(params=[
        @RestApiObjectField(apiFieldName = "centroid", description = "X,Y coord of the annotation centroid",allowedType = "map(x,y)",useForCreation = false),
        @RestApiObjectField(apiFieldName = "term", description = "List of term id mapped with this annotation",allowedType = "list",useForCreation = true, mandatory=false),
    ])
    static transients = ["boundaries", "similarity","rate", "idTerm", "idExpectedTerm"]

    static constraints = {
        location(nullable: false)
        geometryCompression(nullable: true)
        project(nullable:true)
        wktLocation(nullable:false, empty:false)
        area(nullable:true)
        perimeter(nullable:true)
        areaUnit(nullable:true)
        perimeterUnit(nullable:true)
    }

    static mapping = {
        wktLocation(type: 'text')
        tablePerHierarchy false
        id generator: "assigned"
        columns {
            location type: org.hibernatespatial.GeometryUserType
        }
    }

    /**
     * If name is empty, fill it by "Annotation $id"
     */
    public beforeInsert() {
        super.beforeInsert()
        if(!project) {
            project = image.project
        }
        this.makeValid()
        wktLocation = location.toText()
    }

    def beforeUpdate() {
        super.beforeUpdate()
        this.makeValid()
        this.computeGIS()
        wktLocation = location.toText()
    }

    def beforeValidate() {
        if (!created) {
            created = new Date()
        }
        if (id == null) {
            id = sequenceService.generateID()
        }
        this.computeGIS()
        if(!wktLocation)
            wktLocation = location.toText()
    }

    /**
     * Get all terms map with the annotation
     * @return Terms list
     */
    abstract def terms()

    /**
     * Get all annotation terms id
     * @return Terms id list
     */
    abstract def termsId()

    /**
     * Check if its an algo annotation
     */
    abstract boolean isAlgoAnnotation()

    /**
     * Check if its a review annotation
     */
    abstract boolean isReviewedAnnotation()

    /**
     * Get all terms for automatic review
     * If review is done "for all" (without manual user control), we add these term to the new review annotation
     * @return
     */
    abstract List<Term> termsForReview()

    /**
     * Get CROP (annotation image area) URL for this annotation
     * @param cytomineUrl Cytomine base URL
     * @return Full CROP Url
     */
    abstract def getCropUrl()

    String toString() {return "Annotation " + id}

    def getFilename() {
          return this.image?.baseImage?.getFilename()
      }

    def retrieveAreaUnit() {
        GisUtils.retrieveUnit(areaUnit)
    }

    def retrievePerimeterUnit() {
        GisUtils.retrieveUnit(perimeterUnit)
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return project;
    }

    def computeGIS() {
        def image = this.image.baseImage

        //compute unit
        if (image.resolution == null) {
            perimeterUnit = GisUtils.PIXELv
            areaUnit = GisUtils.PIXELS2v
        } else {
            perimeterUnit = GisUtils.MMv
            areaUnit = GisUtils.MICRON2v
        }

        if (image.resolution == null) {
            area = Math.round(this.location.getArea())
            perimeter = Math.round(this.location.getLength())
        } else {
            area = Math.round(this.location.getArea() * image.resolution * image.resolution)
            perimeter = Math.round(this.location.getLength() * image.resolution / 1000)
        }

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
       int imageWidth = image.baseImage.getWidth()
       int imageHeight = image.baseImage.getHeight()
       if (location.getNumPoints()>3) {
         Envelope env = location.getEnvelopeInternal();
         Integer maxY = env.getMaxY();
         Integer minX = env.getMinX();
         Integer width = env.getWidth();
         Integer height = env.getHeight();
         return [topLeftX: minX, topLeftY: maxY, width: width, height: height, imageWidth: imageWidth, imageHeight : imageHeight]
       } else if (location.getNumPoints() == 1) {
           Envelope env = location.getEnvelopeInternal();
           Integer maxY = env.getMaxY()+50;
           Integer minX = env.getMinX()-50;
           Integer width = 100;
           Integer height = 100;
           return [topLeftX: minX, topLeftY: maxY, width: width, height: height, imageWidth: imageWidth, imageHeight : imageHeight]
       }
    }

    def toCropURL(params) {
        def boundaries = getBoundaries()
        if (params.zoom) boundaries.zoom = params.zoom
        if (params.max_size) boundaries.max_size = params.max_size
        if (params.draw) boundaries.max_size = params.max_size
        return UrlApi.getCropURL(image.baseImage.id, boundaries)
        //return image.baseImage.getCropURL(getBoundaries())
    }

    def getCallBack() {
        return [annotationID: this.id, imageID: this.image.id]

    }


    /**
     * Get user/algo/reviewed annotation with id
     * Check the correct type and return it
     * @param id Annotation id
     * @return Annotation
     */
    public static AnnotationDomain getAnnotationDomain(String id) {
        try {
            getAnnotationDomain(Long.parseLong(id))
        } catch(NumberFormatException e) {
            throw new ObjectNotFoundException("Annotation ${id} not found")
        }
    }

    /**
     * Get user/algo/reviewed annotation with id
     * Check the correct type and return it
     * @param id Annotation id
     * @return Annotation
     */
    public static AnnotationDomain getAnnotationDomain(long id) {
        AnnotationDomain basedAnnotation = UserAnnotation.read(id)
        if (!basedAnnotation) {
            basedAnnotation = AlgoAnnotation.read(id)
        }

        if (!basedAnnotation) {
            basedAnnotation = ReviewedAnnotation.read(id)
        }

        if (!basedAnnotation) {
            basedAnnotation = RoiAnnotation.read(id)
        }

        if (basedAnnotation) return basedAnnotation
        else throw new ObjectNotFoundException("Annotation ${id} not found")

    }


    public void makeValid() {
        Geometry geom = new WKTReader().read(this.location.toText())
        Geometry validGeom
        String type = geom.getGeometryType().toUpperCase()

        if (!geom.isValid()) {
            log.info "Geometry is not valid"
            //selfintersect,...
            validGeom = geom.buffer(0)
            this.location = validGeom
            this.wktLocation = validGeom.toText()
            geom = new WKTReader().read(this.location.toText())
            type = geom.getGeometryType().toUpperCase()
        }
        if (geom.isEmpty()) {
            log.info "Geometry is empty"
            //empty polygon,...
            throw new WrongArgumentException("${geom.toText()} is an empty geometry!")
        }

        //for geometrycollection, we may take first collection element
        if (type.equals("LINESTRING") || type.equals("MULTILINESTRING") || type.equals("GEOMETRYCOLLECTION")) {
            //geometry collection, take first elem
            throw new WrongArgumentException("${geom.getGeometryType()} is not a valid geometry type!")
        }


    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['location'] = domain?.location?.toString()
        returnArray['image'] = domain?.image?.id
        returnArray['geometryCompression'] = domain?.geometryCompression
        returnArray['project'] = domain?.project?.id
        returnArray['container'] = domain?.project?.id
        returnArray['user'] = domain?.user?.id
        returnArray['nbComments'] = domain?.countComments
        returnArray['area'] = domain?.area
        returnArray['perimeterUnit'] = domain?.retrievePerimeterUnit()
        returnArray['areaUnit'] = domain?.retrieveAreaUnit()
        returnArray['perimeter'] = domain?.perimeter
        returnArray['centroid'] = domain?.getCentroid()
        returnArray['term'] = domain?.termsId()
        returnArray['similarity'] = domain?.similarity
        returnArray['rate'] = domain?.rate
        returnArray['idTerm'] = domain?.idTerm
        returnArray['idExpectedTerm'] = domain?.idExpectedTerm
        return returnArray
    }


}
