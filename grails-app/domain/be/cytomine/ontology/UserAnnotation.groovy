package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.security.User
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.apache.log4j.Logger
import be.cytomine.utils.JSONUtils
import be.cytomine.project.Project
import be.cytomine.security.UserJob
import be.cytomine.security.SecUser

/**
 * An annotation created by a user
 */
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


    def beforeInsert() {
        super.beforeInsert()
    }

    def beforeUpdate() {
        super.beforeUpdate()
    }

    /**
     * Check if annotation is reviewed
     * @return True if annotation is linked with at least one review annotation
     */
    boolean hasReviewedAnnotation() {
        return countReviewedAnnotations>0
    }

    /**
     * Get all terms map with the annotation
     * @return Terms list
     */
    def terms() {
        return annotationTerm.collect {
            it.term
        }
    }

    /**
     * Get all annotation terms id
     * @return Terms id list
     */
    def termsId() {
        if (user.algo()) {
            return AlgoAnnotationTerm.findAllByAnnotationIdent(this.id).collect{it.term?.id}.unique()
        } else {
            return annotationTerm.collect{it.term?.id}.unique()
        }

    }

    /**
     * Get all terms for automatic review
     * If review is done "for all" (without manual user control), we add these term to the new review annotation
     * @return
     */
    List<Term> termsForReview() {
        terms().unique()
    }

    /**
     * Check if its an algo annotation
     */
    boolean isAlgoAnnotation() {
        return false
    }

    /**
     * Check if its a review annotation
     */
    boolean isReviewedAnnotation() {
        return false
    }

    /**
     * Get CROP (annotation image area) URL for this annotation
     * @param cytomineUrl Cytomine base URL
     * @return Full CROP Url
     */
    def getCropUrl(String cytomineUrl) {
        UrlApi.getUserAnnotationCropWithAnnotationId(cytomineUrl,id)
    }

    /**
     * Get a list of each term link with annotation
     * For each term, add all users that add this term
     * [{id: x, term: y, user: [a,b,c]}, {...]
     */
    def usersIdByTerm() {
        def results = []
        annotationTerm.each { annotationTerm ->
            def map = [:]
            map.id = annotationTerm.id
            map.term = annotationTerm.term?.id
            map.user = [annotationTerm.user?.id]
            def item = results.find { it.term == annotationTerm.term?.id }
            if (!item) {
                results << map
            } else {
                item.user.add(annotationTerm.user.id)
            }
        }
        results
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
            domain.geometryCompression = JSONUtils.getJSONAttrDouble(json, 'geometryCompression', 0)
            domain.created = JSONUtils.getJSONAttrDate(json, 'created')
            domain.updated = JSONUtils.getJSONAttrDate(json, 'updated')
            domain.location = new WKTReader().read(json.location)
            domain.image = JSONUtils.getJSONAttrDomain(json, "image", new ImageInstance(), true)
            domain.project = JSONUtils.getJSONAttrDomain(json, "project", new Project(), true)
            domain.user = JSONUtils.getJSONAttrDomain(json, "user", new SecUser(), true)

            if (!domain.location) {
                throw new WrongArgumentException("Geo is null: 0 points")
            }
            if (domain.location.getNumPoints() < 1) {
                throw new WrongArgumentException("Geometry is empty:" + domain.location.getNumPoints() + " points")
            }
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
            returnArray['similarity'] = annotation.similarity
            returnArray['rate'] = annotation.rate
            returnArray['idTerm'] = annotation.idTerm
            returnArray['idExpectedTerm'] = annotation.idExpectedTerm
            returnArray['cropURL'] = UrlApi.getUserAnnotationCropWithAnnotationId(cytomineBaseUrl,annotation.id)
            returnArray['smallCropURL'] = UrlApi.getUserAnnotationCropWithAnnotationIdWithMaxWithOrHeight(cytomineBaseUrl,annotation.id, 256)
            returnArray['url'] = UrlApi.getUserAnnotationCropWithAnnotationId(cytomineBaseUrl,annotation.id)
            returnArray['imageURL'] = UrlApi.getAnnotationURL(cytomineBaseUrl,imageinstance.project?.id, imageinstance.id, annotation.id)
            returnArray['reviewed'] = annotation.hasReviewedAnnotation()
            return returnArray
        }
    }
}
