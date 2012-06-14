package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON

class Annotation extends CytomineDomain implements Serializable {

    String name
    Geometry location
    ImageInstance image
    Double zoomLevel
    String channels
    SecUser user
    Double similarity
    Double geometryCompression
    Project project
    Annotation parent
    long countComments = 0L

    static belongsTo = [ImageInstance, Project]
    static hasMany = [annotationTerm: AnnotationTerm]

    static transients = ["boundaries", "similarity"]

    static constraints = {
        name(blank: true)
        location(nullable: false)
        zoomLevel(nullable: true)
        geometryCompression(nullable: true)
        channels(nullable: true)
        user(nullable: false)
        project(nullable:true)
        parent(nullable: true)
    }

    static mapping = {
        id generator: "assigned"
        columns {
            location type: org.hibernatespatial.GeometryUserType
        }
        annotationTerm fetch: 'join'
    }


    String toString() {return "Annotation " + id}

    /**
     * If name is empty, fill it by "Annotation $id"
     */
    public beforeInsert() {
        super.beforeInsert()
        name = name && !name.trim().equals("") ? name : "Annotation " + id
        project = image.project
    }
    /**
     * Get all terms map with the annotation
     * @return list of terms
     */
    def terms() {
        return annotationTerm.collect {
            it.term
        }
    }

    def getFilename() {
          return this.image?.baseImage?.getFilename()
      }

    def termsId() {
        if (user.algo()) {
            return AlgoAnnotationTerm.findAllByAnnotation(this).collect{it.term?.id}.unique()
        } else {
            return annotationTerm.collect{it.term?.id}.unique()
        }

    }

    def usersIdByTerm() {
        def results = []
        annotationTerm.each { annotationTerm ->
            def map = [:]
            map.id = annotationTerm.id
            map.term = annotationTerm.term?.id
            map.user = [annotationTerm.user?.id]
            def item = results.find { it.term == annotationTerm.term?.id }
            if (!item) results << map
            else item.user.add(annotationTerm.user.id)
        }
        results
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
        /*def metadata = JSON.parse(new URL(image.getMetadataURL()).text)
     int zoom = Integer.parseInt(metadata.levels)*/
        Coordinate[] coordinates = location.getEnvelope().getCoordinates()
        int topLeftX = coordinates[3].x
        int topLeftY = coordinates[3].y
        //int topLeftY = Integer.parseInt(metadata.height) - coordinates[3].y
        int width = coordinates[1].x - coordinates[0].x
        int height = coordinates[3].y - coordinates[0].y

        //log.debug "topLeftX :" + topLeftX + " topLeftY :" + topLeftY + " width :" + width + " height :" + height
        return [topLeftX: topLeftX, topLeftY: topLeftY, width: width, height: height]
    }

    def toCropURL() {
        def boundaries = getBoundaries()
        return image.baseImage.getCropURL(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height)
    }

    def toCropURLWithMaxWithOrHeight(int dimension) {
        def boundaries = getBoundaries()
        return image.baseImage.getCropURLWithMaxWithOrHeight(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height, dimension)
    }

    def toCropURL(int zoom) {
        def boundaries = getBoundaries()
        return image.baseImage.getCropURL(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height)
    }

    def computeArea() {
        if (this.image.baseImage.resolution == null) return Math.round(this.getArea())// + " pixels²"
        else return Math.round(this.getArea() * this.image.baseImage.resolution)// + " µm²"
    }

    def computePerimeter() {
        if (this.image.baseImage.resolution == null) return Math.round(this.getPerimeter())// + " pixels"
        else return Math.round(this.getPerimeter() * this.image.baseImage.resolution)// + " µm"
    }

    static Annotation createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    /**
     * Create a new Annotation with jsonAnnotation attributes
     * So, jsonAnnotation must have jsonAnnotation.location, jsonAnnotation.name, ...
     * @param jsonAnnotation JSON
     * @return Annotation
     */
    static Annotation createFromData(jsonAnnotation) {
        def annotation = new Annotation()
        getFromData(annotation, jsonAnnotation)
    }

    /**
     * Fill annotation with data attributes
     * So, jsonAnnotation must have jsonAnnotation.location, jsonAnnotation.name, ...
     * @param annotation Annotation Source
     * @param jsonAnnotation JSON
     * @return annotation with json attributes
     */
    static Annotation getFromData(annotation, jsonAnnotation) {
        try {
            annotation.name = jsonAnnotation.name
            annotation.zoomLevel = (!jsonAnnotation.zoomLevel.toString().equals("null")) ? ((String) jsonAnnotation.zoomLevel).toDouble() : -1
            annotation.geometryCompression = (!jsonAnnotation.geometryCompression.toString().equals("null")) ? ((String) jsonAnnotation.geometryCompression).toDouble() : 0
            annotation.channels = jsonAnnotation.channels

            annotation.created = (!jsonAnnotation.created.toString().equals("null")) ? new Date(Long.parseLong(jsonAnnotation.created)) : null
            annotation.updated = (!jsonAnnotation.updated.toString().equals("null")) ? new Date(Long.parseLong(jsonAnnotation.updated)) : null

            //location
            annotation.location = new WKTReader().read(jsonAnnotation.location)
            if (annotation.location.getNumPoints() < 1) throw new WrongArgumentException("Geometry is empty:" + annotation.location.getNumPoints() + " points")
            if (annotation.location.getNumPoints() < 3) throw new WrongArgumentException("Geometry is not a polygon :" + annotation.location.getNumPoints() + " points")
            if (!annotation.location) throw new WrongArgumentException("Geo is null: 0 points")
            //image
            annotation.image = ImageInstance.get(jsonAnnotation.image);
            if (!annotation.image) throw new WrongArgumentException("Image $jsonAnnotation.image not found!")
            //project
            annotation.project = Project.get(jsonAnnotation.project);
            if (!annotation.project) throw new WrongArgumentException("Project $jsonAnnotation.project not found!")
            //user
            annotation.user = SecUser.get(jsonAnnotation.user);
            if (!annotation.project) throw new WrongArgumentException("SecUser $jsonAnnotation.user not found!")
            //parent
            if (!jsonAnnotation.parent.toString().equals("null")) {
                annotation.parent = Annotation.get(jsonAnnotation.parent)
                if(!annotation.parent) throw new WrongArgumentException("Annotation parent ${jsonAnnotation.parent} not found!")
            }

        } catch (com.vividsolutions.jts.io.ParseException ex) {
            throw new WrongArgumentException(ex.toString())
        }
        return annotation;
    }

    def getCallBack() {
        return [annotationID: this.id, imageID: this.image.id]

    }

    static void registerMarshaller(String cytomineBaseUrl) {
        println "Register custom JSON renderer for " + Annotation.class
        JSON.registerObjectMarshaller(Annotation) { annotation ->
            def returnArray = [:]
            ImageInstance imageinstance = annotation.image
            returnArray['class'] = annotation.class
            returnArray['id'] = annotation.id
            returnArray['name'] = annotation.name != "" ? annotation.name : "Annotation " + annotation.id
            returnArray['location'] = annotation.location.toString()
            returnArray['image'] = annotation.image?.id
            returnArray['zoomLevel'] = annotation.zoomLevel
            returnArray['geometryCompression'] = annotation.geometryCompression
            returnArray['channels'] = annotation.channels
            returnArray['project'] = annotation.project.id
            returnArray['container'] = annotation.project.id
            returnArray['parent'] = annotation.parent?.id
            returnArray['user'] = annotation.user?.id
            returnArray['nbComments'] = annotation.countComments
            returnArray['area'] = annotation.computeArea()
            returnArray['perimeter'] = annotation.computePerimeter()
            returnArray['centroid'] = annotation.getCentroid()
            returnArray['created'] = annotation.created ? annotation.created.time.toString() : null
            returnArray['updated'] = annotation.updated ? annotation.updated.time.toString() : null
            returnArray['term'] = annotation.termsId()
            returnArray['userByTerm'] = annotation.usersIdByTerm()
            try {if (annotation?.similarity) returnArray['similarity'] = annotation.similarity} catch (Exception e) {}
            returnArray['cropURL'] = UrlApi.getAnnotationCropWithAnnotationId(cytomineBaseUrl,annotation.id)
            returnArray['smallCropURL'] = UrlApi.getAnnotationCropWithAnnotationIdWithMaxWithOrHeight(cytomineBaseUrl,annotation.id, 512)
            returnArray['url'] = UrlApi.getAnnotationCropWithAnnotationId(cytomineBaseUrl,annotation.id)
            returnArray['imageURL'] = UrlApi.getAnnotationURL(cytomineBaseUrl,imageinstance.project?.id, imageinstance.id, annotation.id)
            return returnArray
        }
    }

}
