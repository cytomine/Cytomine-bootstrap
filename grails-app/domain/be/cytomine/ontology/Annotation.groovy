package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.api.UrlApi
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.security.User
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project

class Annotation extends CytomineDomain implements Serializable {

    String name
    Geometry location
    ImageInstance image
    Double zoomLevel
    String channels
    User user
    Double similarity

    static belongsTo = [ImageInstance]
    static hasMany = [annotationTerm: AnnotationTerm]

    static transients = ["cropURL", "boundaries", "similarity"]

    static constraints = {
        name(blank: true)
        location(nullable: false)
        zoomLevel(nullable: true)
        channels(nullable: true)
        user(nullable: false)
        similarity(nullable: true)
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
    }
    /*public afterInsert() {
      println "Annotation.afterInsert"
        Project project = image.project;
        project.countAnnotations++
    }
    public def afterDelete()  {
      println "Annotation.afterDelete"
        Project project = image.project;
        project.countAnnotations--
    }*/
    /**
     * Get all terms map with the annotation
     * @return list of terms
     */
    def terms() {
        return annotationTerm.collect {
            it.term
        }
    }

    private def getTermsname() {
        return annotationTerm.collect {
            it.term.name
        }
    }

    def termsId() {
        return annotationTerm.collect {
            it.getIdTerm()
        }.unique()
    }

    def termsIdByUser() {
        Map<Long, List<Long>> usersAnnotation = [:]
        annotationTerm.each { annotationTerm ->
            if (usersAnnotation.containsKey(annotationTerm.user.id)) {
                //if user is already there, add term to the list
                List<Long> terms = usersAnnotation.get(annotationTerm.user.id)
                terms.add(annotationTerm.term.id)
                usersAnnotation.put(annotationTerm.user.id, terms)
            } else {
                //if user is not there create list with term id
                List<Long> terms = new ArrayList<Long>();
                terms.add(annotationTerm.term.id)
                usersAnnotation.put(annotationTerm.user.id, terms)
            }
        }
        usersAnnotation
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
        return image?.project
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

        log.debug "topLeftX :" + topLeftX + " topLeftY :" + topLeftY + " width :" + width + " height :" + height
        return [topLeftX: topLeftX, topLeftY: topLeftY, width: width, height: height]
    }

    def getCropURL() {
        def boundaries = getBoundaries()
        return image.baseImage.getCropURL(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height)
    }

    def crop = { domain, value ->

    }


    def getURLForCrop() {
        return UrlApi.getAnnotationCropWithAnnotationId(this.id)
    }

    def getURLForServerGoTo() {
        return UrlApi.getAnnotationURL(this.image.getIdProject(), this.image.id, this.id)
    }



    def getCropURL(int zoom) {
        def boundaries = getBoundaries()
        return image.baseImage.getCropURL(boundaries.topLeftX, boundaries.topLeftY, boundaries.width, boundaries.height)
    }

    def computeArea() {
        if (this.image.baseImage.resolution == null) return Math.round(this.getArea()) + " pixels²"
        else return Math.round(this.getArea() * this.image.baseImage.resolution) + " µm²"
    }

    def computePerimeter() {
        if (this.image.baseImage.resolution == null) return Math.round(this.getPerimeter()) + " pixels"
        else return Math.round(this.getPerimeter() * this.image.baseImage.resolution) + " µm"
    }

    static Annotation createFromDataWithId(json)  {
        def domain = createFromData(json)
        try{domain.id = json.id}catch(Exception e){}
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
        //annotation.location = DouglasPeuckerSimplifier.simplify(annotation.location,50)
        annotation.image = ImageInstance.get(jsonAnnotation.image);
        println "Annotation image = " + annotation.image + "($jsonAnnotation.image)"
        if (!annotation.image) throw new WrongArgumentException("Image $jsonAnnotation.image not found!")
        annotation.zoomLevel = (!jsonAnnotation.zoomLevel.toString().equals("null")) ? ((String) jsonAnnotation.zoomLevel).toDouble() : -1
        annotation.channels = jsonAnnotation.channels
        annotation.user = User.get(jsonAnnotation.user);
        annotation.created = (!jsonAnnotation.created.toString().equals("null")) ? new Date(Long.parseLong(jsonAnnotation.created)) : null
        annotation.updated = (!jsonAnnotation.updated.toString().equals("null")) ? new Date(Long.parseLong(jsonAnnotation.updated)) : null
        if (!annotation.location) throw new WrongArgumentException("Geo is null: 0 points")
        if (annotation.location.getNumPoints() < 1) throw new WrongArgumentException("Geo is empty:" + annotation.location.getNumPoints() + " points")
             } catch (com.vividsolutions.jts.io.ParseException ex) {
            throw new WrongArgumentException(ex.toString())
        }
        return annotation;
    }

    def getIdImage() {
        if (this.imageId) return this.imageId
        else return this.image?.id
    }

    def getCallBack() {
        return [annotationID: this.id, imageID: this.image.id]
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + Annotation.class
        JSON.registerObjectMarshaller(Annotation) {
            def returnArray = [:]

            ImageInstance imageinstance = it.image
            AbstractImage image = imageinstance?.baseImage
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['name'] = it.name != "" ? it.name : "Annotation " + it.id
            returnArray['location'] = it.location.toString()
            returnArray['image'] = it.getIdImage()
            returnArray['imageFilename'] = image?.filename
            returnArray['zoomLevel'] = it.zoomLevel
            returnArray['channels'] = it.channels
            returnArray['project'] = imageinstance?.getIdProject()
            if (it.userId) returnArray['user'] = it.userId
            else returnArray['user'] = it.user?.id

            returnArray['area'] = it.computeArea()
            returnArray['perimeter'] = it.computePerimeter()
            returnArray['centroid'] = it.getCentroid()

            returnArray['created'] = it.created ? it.created.time.toString() : null
            returnArray['updated'] = it.updated ? it.updated.time.toString() : null

            returnArray['term'] = it.termsId()

            returnArray['userByTerm'] = it.usersIdByTerm()

            //retrieval
            try {if (it?.similarity) returnArray['similarity'] = it.similarity} catch (Exception e) {}

            returnArray['cropURL'] = UrlApi.getAnnotationCropWithAnnotationId(it.id)
            returnArray['url'] = UrlApi.getAnnotationCropWithAnnotationId(it.id)
            returnArray['imageURL'] = UrlApi.getAnnotationURL(imageinstance.getIdProject(), imageinstance.id, it.id)
            return returnArray
        }
    }

}
