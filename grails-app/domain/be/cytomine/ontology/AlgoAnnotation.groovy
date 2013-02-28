package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.UserJob
import be.cytomine.utils.JSONUtils
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * Annotation added by a job (software)
 * Extend AnnotationDomain that provide generic Annotation properties (location,...)
 */
class AlgoAnnotation extends AnnotationDomain implements Serializable {

    /**
     * Virtual user that create annotation
     */
    UserJob user

    /**
     * Number of reviewed annotation
     * Rem: With UI client, it can only be 0 or 1
     */
    Integer countReviewedAnnotations = 0

    static constraints = {
    }

    static mapping = {
        id generator: "assigned"
        columns {
            location type: org.hibernatespatial.GeometryUserType
        }
        wktLocation(type: 'text')
        sort "id"
    }

    def beforeInsert() {
        super.beforeInsert()
    }

    def beforeUpdate() {
        super.beforeUpdate()
    }

    /**
     * Get all terms map with the annotation
     * @return list of terms
     */
    def terms() {
        def criteria = AlgoAnnotationTerm.withCriteria() {
            eq('annotationIdent', id)
            projections {
                groupProperty("term")
            }
        }
        return criteria
    }

    /**
     * Get all terms id map with annotation
     * TODO: could be optim with single SQL request
     * @return list of terms id
     */
    def termsId() {
        terms().collect {it.id}
    }

    def getCropUrl(String cytomineUrl) {
        UrlApi.getAlgoAnnotationCropWithAnnotationId(cytomineUrl, id)
    }

    /**
     * Check if annotation is an algo annotation
     */
    boolean isAlgoAnnotation() {
        return true
    }

    /**
     * Check if annotation is a reviewed annotation
     * Rem: Even if this annotation is review, this is still algo annotation
     */
    boolean isReviewedAnnotation() {
        return false
    }

    /**
     * Get all terms to map with annotation if automatic review.
     * For AlgoAnnotation, we take AlgoAnnotationTerm created by this user
     * @return Term List
     */
    List<Term> termsForReview() {
        AlgoAnnotationTerm.findAllByAnnotationIdentAndUserJob(id, user).collect {it.term}.unique()
    }

    /**
     * Check if annotation has been reviewed
     * @return True if annotation has at least 1 reviewed annotation, otherwise false
     */
    boolean hasReviewedAnnotation() {
        return countReviewedAnnotations > 0
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static AlgoAnnotation insertDataIntoDomain(def json, def domain = new AlgoAnnotation()) {
        try {
            domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
            domain.geometryCompression = JSONUtils.getJSONAttrDouble(json, 'geometryCompression', 0)
            domain.created = JSONUtils.getJSONAttrDate(json, 'created')
            domain.updated = JSONUtils.getJSONAttrDate(json, 'updated')
            domain.location = new WKTReader().read(json.location)
            domain.image = JSONUtils.getJSONAttrDomain(json, "image", new ImageInstance(), true)
            domain.project = JSONUtils.getJSONAttrDomain(json, "project", new Project(), true)
            domain.user = JSONUtils.getJSONAttrDomain(json, "user", new UserJob(), true)

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
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + AlgoAnnotation.class)
        JSON.registerObjectMarshaller(AlgoAnnotation) { AlgoAnnotation annotation ->
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
            returnArray['created'] = annotation.created?.time?.toString()
            returnArray['updated'] = annotation.updated?.time?.toString()
            returnArray['term'] = annotation.termsId()
            returnArray['similarity'] = annotation.similarity
            returnArray['rate'] = annotation.rate
            returnArray['idTerm'] = annotation.idTerm
            returnArray['idExpectedTerm'] = annotation.idExpectedTerm
            returnArray['cropURL'] = UrlApi.getAlgoAnnotationCropWithAnnotationId(annotation.id)
            returnArray['smallCropURL'] = UrlApi.getAlgoAnnotationCropWithAnnotationIdWithMaxWithOrHeight(annotation.id, 256)
            returnArray['url'] = UrlApi.getAlgoAnnotationCropWithAnnotationId(annotation.id)
            returnArray['imageURL'] = UrlApi.getAnnotationURL(imageinstance.project?.id, imageinstance.id, annotation.id)
            returnArray['reviewed'] = annotation.hasReviewedAnnotation()
            return returnArray
        }
    }

    /**
     * Return domain user (annotation user, image user...)
     * By default, a domain has no user.
     * You need to override userDomainCreator() in domain class
     * @return Domain user
     */
    public SecUser userDomainCreator() {
        return user;
    }
}
