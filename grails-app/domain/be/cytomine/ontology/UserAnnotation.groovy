package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.security.User
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.apache.log4j.Logger

class UserAnnotation extends AnnotationDomain implements Serializable {

    User user
    Integer countReviewedAnnotations = 0

    static hasMany = [ annotationTerm: AnnotationTerm ]

    static constraints = {
    }

    static mapping = {
          id generator: "assigned"
          columns {
              location type: org.hibernatespatial.GeometryUserType
          }
          annotationTerm fetch: 'join'
         wktLocation(type: 'text')
      }

    boolean hasReviewedAnnotation() {
        return countReviewedAnnotations>0
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
        if (user.algo()) {
            return AlgoAnnotationTerm.findAllByAnnotationIdent(this.id).collect{it.term?.id}.unique()
        } else {
            return annotationTerm.collect{it.term?.id}.unique()
        }

    }

    List<Term> termsForReview() {
        terms().unique()
    }

    boolean isAlgoAnnotation() {
        return false
    }

    boolean isReviewedAnnotation() {
        return false
    }

    def getCropUrl(String cytomineUrl) {
        UrlApi.getUserAnnotationCropWithAnnotationId(cytomineUrl,id)
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

    def beforeInsert() {
        super.beforeInsert()
    }

    def beforeUpdate() {
        super.beforeUpdate()
    }

    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static UserAnnotation createFromDataWithId(def json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    /**
     * Thanks to the json, create a new domain of this class
     * If json.id is set, the method ignore id
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static UserAnnotation createFromData(def json) {
        def annotation = new UserAnnotation()
        insertDataIntoDomain(annotation, json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static UserAnnotation insertDataIntoDomain(def domain, json) {
        try {
            domain.geometryCompression = (!json.geometryCompression.toString().equals("null")) ? ((String) json.geometryCompression).toDouble() : 0
            domain.created = (!json.created.toString().equals("null")) ? new Date(Long.parseLong(json.created)) : null
            domain.updated = (!json.updated.toString().equals("null")) ? new Date(Long.parseLong(json.updated)) : null

            //location
            domain.location = new WKTReader().read(json.location)
            if (domain.location.getNumPoints() < 1) throw new WrongArgumentException("Geometry is empty:" + domain.location.getNumPoints() + " points")
            //if (domain.location.getNumPoints() < 3) throw new WrongArgumentException("Geometry is not a polygon :" + domain.location.getNumPoints() + " points")
            if (!domain.location) throw new WrongArgumentException("Geo is null: 0 points")
            //image
            domain.image = ImageInstance.get(json.image);
            if (!domain.image) throw new WrongArgumentException("Image $json.image not found!")
            //project
            domain.project = be.cytomine.project.Project.get(json.project);
            if (!domain.project) throw new WrongArgumentException("Project $json.project not found!")
            //user
            domain.user = User.get(json.user);
            if (!domain.user) throw new WrongArgumentException("User $json.user not found!")

        } catch (com.vividsolutions.jts.io.ParseException ex) {
            throw new WrongArgumentException(ex.toString())
        }
        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + UserAnnotation.class)
        JSON.registerObjectMarshaller(UserAnnotation) { annotation ->
            def returnArray = [:]
            ImageInstance imageinstance = annotation.image
            returnArray['class'] = annotation.class
            returnArray['id'] = annotation.id
            returnArray['location'] = annotation.location.toString()
            returnArray['image'] = annotation.image?.id

            returnArray['geometryCompression'] = annotation.geometryCompression

            returnArray['project'] = annotation.project.id
            returnArray['container'] = annotation.project.id
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
            try {if (annotation?.rate) returnArray['rate'] = annotation.rate} catch (Exception e) {}
            try {if (annotation?.rate) returnArray['idTerm'] = annotation.idTerm} catch (Exception e) {}
            try {if (annotation?.rate) returnArray['idExpectedTerm'] = annotation.idExpectedTerm} catch (Exception e) {}

            returnArray['cropURL'] = UrlApi.getUserAnnotationCropWithAnnotationId(cytomineBaseUrl,annotation.id)
            returnArray['smallCropURL'] = UrlApi.getUserAnnotationCropWithAnnotationIdWithMaxWithOrHeight(cytomineBaseUrl,annotation.id, 256)
            returnArray['url'] = UrlApi.getUserAnnotationCropWithAnnotationId(cytomineBaseUrl,annotation.id)
            returnArray['imageURL'] = UrlApi.getAnnotationURL(cytomineBaseUrl,imageinstance.project?.id, imageinstance.id, annotation.id)

            returnArray['reviewed'] = annotation.hasReviewedAnnotation()

            return returnArray
        }
    }
}
