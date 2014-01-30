package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.JSONUtils
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject

/**
 * An annotation created by a user
 */
@ApiObject(name = "user annotation", description = "An annotation created by a user")
class UserAnnotation extends AnnotationDomain implements Serializable {

    @ApiObjectFieldLight(description = "User id that created this annotation")
    User user

    @ApiObjectFieldLight(description = "The number of reviewed annotations for this annotation", useForCreation = false)
    Integer countReviewedAnnotations = 0

    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "cropURL", description = "URL to get the annotation crop",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "smallCropURL", description = "URL to get a small annotation crop (<256px)",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "url", description = "URL to go to the annotation on the image",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "imageURL", description = "URL to go to the image",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "reviewed", description = "True if annotation has at least one review",allowedType = "boolean",useForCreation = false)
    ])
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
        if(this.version!=null) {
            AnnotationTerm.findAllByUserAnnotation(this).collect {it.term}
        } else {
            return []
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
            return terms().collect{it.id}.unique()
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
    def getCropUrl() {
        UrlApi.getUserAnnotationCropWithAnnotationId(id)
    }

    /**
     * Get a list of each term link with annotation
     * For each term, add all users that add this term
     * [{id: x, term: y, user: [a,b,c]}, {...]
     */
    def usersIdByTerm() {
        def results = []
        if(this.version!=null) {
            AnnotationTerm.findAllByUserAnnotation(this).each { annotationTerm ->
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
        }
        results
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static UserAnnotation insertDataIntoDomain(def json, def domain = new UserAnnotation()) {
        try {
            domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
            domain.geometryCompression = JSONUtils.getJSONAttrDouble(json, 'geometryCompression', 0)
            domain.created = JSONUtils.getJSONAttrDate(json, 'created')
            domain.updated = JSONUtils.getJSONAttrDate(json, 'updated')
            domain.location = new WKTReader().read(json.location)
            domain.image = JSONUtils.getJSONAttrDomain(json, "image", new ImageInstance(), true)
            //domain.imageId = Long.parseLong(json["image"].toString())
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
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + this.class)
        JSON.registerObjectMarshaller(UserAnnotation) { domain ->
            return getDataFromDomain(domain)
        }
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = AnnotationDomain.getDataFromDomain(domain)
        ImageInstance imageinstance = domain?.image
        returnArray['cropURL'] = UrlApi.getAlgoAnnotationCropWithAnnotationId(domain?.id)
        returnArray['smallCropURL'] = UrlApi.getAlgoAnnotationCropWithAnnotationIdWithMaxWithOrHeight(domain?.id, 256)
        returnArray['url'] = UrlApi.getAlgoAnnotationCropWithAnnotationId(domain?.id)
        returnArray['imageURL'] = UrlApi.getAnnotationURL(imageinstance?.project?.id, imageinstance?.id, domain?.id)
        returnArray['reviewed'] = domain?.hasReviewedAnnotation()
        return returnArray
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
