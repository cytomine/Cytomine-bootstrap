package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import be.cytomine.security.SecUser

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

    def termsId() {
        return annotationTerm.collect{it.getIdTerm()}.unique()
    }

    def usersIdByTerm() {
        def results = []
        annotationTerm.each { annotationTerm ->
            def map = [:]
            map.id = annotationTerm.id
            map.term = annotationTerm.getIdTerm()
            map.user = [annotationTerm.getIdUser()]
            def item = results.find { it.term == annotationTerm.getIdTerm() }
            if (!item) results << map
            else item.user.add(annotationTerm.user.id)
        }
        results
    }

    def project() {
        return project
    }

    Project projectDomain() {
        return image?.project
    }

    String imageFileName() {
        return this.image?.baseImage?.getFilename()
    }

    private def getFilename() {
        return this.image?.baseImage?.getFilename()
    }

    private def getArea() {
        //TODO: must be compute with zoom level
        return location.area
    }

    private def getPerimeter() {
        //TODO: must be compute with zoom level
        return location.getLength()
    }

    private def getCentroid() {
        if (location.area < 1) return null
        def centroid = location.getCentroid()
        def response = [:]
        response.x = centroid.x
        response.y = centroid.y
        return response
    }

    private def getBoundaries() {
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

    def getCropURL() {
        def boundaries = getBoundaries()
        return image.baseImage.getCropURL(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height)
    }

    def getCropURLWithMaxWithOrHeight(int dimension) {
        def boundaries = getBoundaries()
        return image.baseImage.getCropURLWithMaxWithOrHeight(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height, dimension)
    }

    def getCropURL(int zoom) {
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
            annotation.location = new WKTReader().read(jsonAnnotation.location)
            if (annotation.location.getNumPoints() < 1) throw new WrongArgumentException("Geometry is empty:" + annotation.location.getNumPoints() + " points")
            if (annotation.location.getNumPoints() < 3) throw new WrongArgumentException("Geometry is not a polygon :" + annotation.location.getNumPoints() + " points")
            //annotation.location = DouglasPeuckerSimplifier.simplify(annotation.location,50)
            annotation.image = ImageInstance.get(jsonAnnotation.image);
            println "Annotation image = " + annotation.image + "($jsonAnnotation.image)"
            if (!annotation.image) throw new WrongArgumentException("Image $jsonAnnotation.image not found!")

            annotation.project = Project.get(jsonAnnotation.project);
            if (!annotation.project) throw new WrongArgumentException("Project $jsonAnnotation.project not found!")
            annotation.zoomLevel = (!jsonAnnotation.zoomLevel.toString().equals("null")) ? ((String) jsonAnnotation.zoomLevel).toDouble() : -1
            annotation.geometryCompression = (!jsonAnnotation.geometryCompression.toString().equals("null")) ? ((String) jsonAnnotation.geometryCompression).toDouble() : 0
            annotation.channels = jsonAnnotation.channels
            annotation.user = SecUser.get(jsonAnnotation.user);
            annotation.created = (!jsonAnnotation.created.toString().equals("null")) ? new Date(Long.parseLong(jsonAnnotation.created)) : null
            annotation.updated = (!jsonAnnotation.updated.toString().equals("null")) ? new Date(Long.parseLong(jsonAnnotation.updated)) : null
            if (!annotation.location) throw new WrongArgumentException("Geo is null: 0 points")

        } catch (com.vividsolutions.jts.io.ParseException ex) {
            throw new WrongArgumentException(ex.toString())
        }
        return annotation;
    }

    def getIdImage() {
//        if (this.imageId) return this.imageId
//        else return this.image?.id
        return this.image?.id
    }

    def getCallBack() {
        return [annotationID: this.id, imageID: this.image.id]

    }
    


    static void registerMarshaller(String cytomineBaseUrl) {
        println "Register custom JSON renderer for " + Annotation.class
        JSON.registerObjectMarshaller(Annotation) { annotation ->
            def returnArray = [:]

            Date start = new Date()
            ImageInstance imageinstance = annotation.image
            AbstractImage image = imageinstance?.baseImage
            returnArray['class'] = annotation.class
            returnArray['id'] = annotation.id
            returnArray['name'] = annotation.name != "" ? annotation.name : "Annotation " + annotation.id
            returnArray['location'] = annotation.location.toString()
            returnArray['image'] = annotation.getIdImage()
            returnArray['imageFilename'] = image?.filename

            returnArray['zoomLevel'] = annotation.zoomLevel
            returnArray['geometryCompression'] = annotation.geometryCompression
            returnArray['channels'] = annotation.channels
            returnArray['project'] = annotation.project.id
            returnArray['container'] = annotation.project.id

            if (annotation.userId) returnArray['user'] = annotation.userId
            else returnArray['user'] = annotation.user?.id
            returnArray['nbComments'] = annotation.countComments
            returnArray['area'] = annotation.computeArea()
            returnArray['perimeter'] = annotation.computePerimeter()
            returnArray['centroid'] = annotation.getCentroid()
            returnArray['created'] = annotation.created ? annotation.created.time.toString() : null
            returnArray['updated'] = annotation.updated ? annotation.updated.time.toString() : null
            returnArray['term'] = annotation.termsId()
            returnArray['userByTerm'] = annotation.usersIdByTerm()
            //retrieval
            try {if (annotation?.similarity) returnArray['similarity'] = annotation.similarity} catch (Exception e) {}
            returnArray['cropURL'] = annotation.getCropURL()
            //returnArray['smallCropURL'] = annotation.getCropURLWithMaxWithOrHeight(256)

            //println grailsApplication.config.grails.serverURL

            returnArray['url'] = UrlApi.getAnnotationCropWithAnnotationId(cytomineBaseUrl,annotation.id)
            returnArray['imageURL'] = UrlApi.getAnnotationURL(cytomineBaseUrl,imageinstance.getIdProject(), imageinstance.id, annotation.id)
            return returnArray
        }
    }

}
