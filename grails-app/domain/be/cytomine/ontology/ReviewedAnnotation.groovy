package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.JSONUtils
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject
import org.jsondoc.core.annotation.ApiObjectField

/**
 *  A reviewed annotation is an user/algo-annotation validated by a user.
 *  When a user validate an user/algoannotation, we copy all data from the validated annotation to create the review annotation
 */
@ApiObject(name = "reviewed annotation", description = "A reviewed annotation is an user/algo-annotation validated by a user. When a user validate an user/algoannotation, we copy all data from the validated annotation to create the review annotation")
class ReviewedAnnotation extends AnnotationDomain implements Serializable {

//    returnArray['terms'] = domain?.termsId()
//    returnArray['term'] = returnArray['terms']


    /**
     * Annotation that has been reviewed (just keep a link)
     */
    @ApiObjectFieldLight(description = "Annotation id that has been reviewed")
    Long parentIdent

    @ApiObjectFieldLight(description = "Annotation type that has been reviewed (algo/user)")
    String parentClassName

    /**
     * Status for the reviewed (not yet use)
     * May be: 'validate','conflict',...
     */
    @ApiObjectFieldLight(description = "Status for the reviewed", mandatory = false)
    Integer status

    /**
     * User that create the annotation that has been reviewed
     */
    @ApiObjectFieldLight(description = "User that created the based annotation", useForCreation = false)
    SecUser user

    /**
     * User that review annotation
     */
    @ApiObjectFieldLight(description = "User that review the based annotation", useForCreation = true, mandatory = false, defaultValue = "current user")
    SecUser reviewUser

    static hasMany = [ terms: Term ]

    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "terms", description = "List of term id mapped with this annotation",allowedType = "list",useForCreation = true, mandatory=false),
        @ApiObjectFieldLight(apiFieldName = "cropURL", description = "URL to get the annotation crop",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "smallCropURL", description = "URL to get a small annotation crop (<256px)",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "url", description = "URL to go to the annotation on the image",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "imageURL", description = "URL to go to the image",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "reviewed", description = "Always true",allowedType = "boolean",useForCreation = false)
    ])
    static constraints = {
    }

    static mapping = {
          id generator: "assigned"
          columns {
              location type: org.hibernatespatial.GeometryUserType
          }
        terms fetch: 'join'
        wktLocation(type: 'text')
        sort "id"

     }

    public String toString() {
         return "ReviewedAnnotation" + " " + parentClassName + ":" + parentIdent + " with term " + terms + " from userjob " + user + " and  project " + project
    }

    /**
     * Set link to the annotation that has been reviewed
     * @param annotation Annotation that is reviewed
     */
    public void putParentAnnotation(AnnotationDomain annotation) {
        parentClassName = annotation.class.getName()
        parentIdent = annotation.id
    }

    /**
     * Get the annotation that has been reviewed
     * @return Annotation
     */
    public AnnotationDomain retrieveParentAnnotation() {
        Class.forName(parentClassName, false, Thread.currentThread().contextClassLoader).read(parentIdent)
    }

    def getCropUrl() {
        UrlApi.getReviewedAnnotationCropWithAnnotationId(id)
    }

    /**
     * Get all terms map with annotation
     * For reviewedAnnotation, we store term in hasMany, so return list
     * @return Terms lists
     */
    def terms() {
        return terms
    }

    /**
     * Get all terms id map with annotation
     * @return Terms id
     */
    def termsId() {
         terms().collect{it.id}
     }

     def beforeInsert() {
         super.beforeInsert()
     }

     def beforeUpdate() {
         super.beforeUpdate()
     }

    boolean isAlgoAnnotation() {
        return false
    }

    boolean isReviewedAnnotation() {
        return true
    }

    /**
     * Get terms list for automatic review
     * Not usefull for this domain, but we must implement this abstract method
     */
    List<Term> termsForReview() {
        terms
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */     
     static ReviewedAnnotation insertDataIntoDomain(def json,def domain=new ReviewedAnnotation()) {
         try {
             domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
             domain.geometryCompression = JSONUtils.getJSONAttrDouble(json, 'geometryCompression', 0)
             domain.created = JSONUtils.getJSONAttrDate(json, 'created')
             domain.updated = JSONUtils.getJSONAttrDate(json, 'updated')
             domain.location = new WKTReader().read(json.location)

             domain.image = JSONUtils.getJSONAttrDomain(json, "image", new ImageInstance(), true)
             domain.project = JSONUtils.getJSONAttrDomain(json, "project", new Project(), true)
             domain.user = JSONUtils.getJSONAttrDomain(json, "user", new SecUser(), true)
             domain.reviewUser = JSONUtils.getJSONAttrDomain(json, "reviewUser", new SecUser(), true)

             Long annotationParentId = JSONUtils.getJSONAttrLong(json, 'parentIdent', -1)
             if (annotationParentId == -1) {
                 annotationParentId = JSONUtils.getJSONAttrLong(json, 'annotation', -1)
             }
             try {
                 AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(annotationParentId)
                 domain.parentClassName = annotation.class.getName()
                 domain.parentIdent = annotation.id
             } catch(Exception e) {
                //parent is deleted...
              }

             domain.status = JSONUtils.getJSONAttrInteger(json, 'status', 0)


             if(domain.terms) {
                 //remove all review term
                 domain.terms.clear()
             }

             if (json.terms == null || json.terms.equals("null")) {
                 throw new WrongArgumentException("Term list was not found")
             }

             json.terms.each {
                 Term term = Term.read(it)
                 if(term.ontology!=domain.project.ontology) {
                     throw new WrongArgumentException("Term ${term} from ontology ${term.ontology} is not in ontology from the annotation project (${domain.project.ontology}")
                 }

                 domain.addToTerms(term)

             }

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
        JSON.registerObjectMarshaller(ReviewedAnnotation) { domain ->
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
        returnArray['parentIdent'] = domain?.parentIdent
        returnArray['parentClassName'] = domain?.parentClassName
        returnArray['status'] = domain?.status
        returnArray['reviewUser'] = domain?.reviewUser?.id
        returnArray['terms'] = domain?.termsId()
        returnArray['term'] = returnArray['terms']
        returnArray['cropURL'] = UrlApi.getReviewedAnnotationCropWithAnnotationId(domain?.id)
        returnArray['smallCropURL'] = UrlApi.getReviewedAnnotationCropWithAnnotationIdWithMaxWithOrHeight(domain?.id, 256)
        returnArray['url'] = UrlApi.getReviewedAnnotationCropWithAnnotationId(domain?.id)
        returnArray['imageURL'] = UrlApi.getAnnotationURL(imageinstance?.project?.id, imageinstance?.id, domain?.id)
        returnArray['reviewed'] = true
        return returnArray
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        ReviewedAnnotation.withNewSession {
                ReviewedAnnotation reviewed = ReviewedAnnotation.findByParentIdent(parentIdent)
                if(reviewed!=null && (reviewed.id!=id))  {
                    throw new AlreadyExistException("This annotation is already reviewed!")
                }
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
