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
import org.apache.log4j.Logger

/**
 *  A reviewed annotation is an user/algo-annotation validated by a user.
 *  When a user validate an user/algoannotation, we copy all data from the validated annotation to create the review annotation
 */
class ReviewedAnnotation extends AnnotationDomain implements Serializable {

    static hasMany = [ terms: Term ]

    /**
     * Annotation that has been reviewed (just keep a link)
     */
    String parentClassName
    Long parentIdent

    /**
     * Status for the reviewed (not yet use)
     * May be: 'validate','conflict',...
     */
    Integer status

    /**
     * User that create the annotation that has been reviewed
     */
    SecUser user

    /**
     * User that review annotation
     */
    SecUser reviewUser

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
         Logger.getLogger(this).info("Register custom JSON renderer for " + ReviewedAnnotation.class)
         JSON.registerObjectMarshaller(ReviewedAnnotation) { ReviewedAnnotation annotation ->
             def returnArray = [:]
             ImageInstance imageinstance = annotation.image
             returnArray['class'] = annotation.class
             returnArray['id'] = annotation.id
             returnArray['parentIdent'] = annotation.parentIdent
             returnArray['parentClassName'] = annotation.parentClassName
             returnArray['status'] = annotation.status
             returnArray['location'] = annotation.location.toString()
             returnArray['image'] = annotation.image?.id
             returnArray['geometryCompression'] = annotation.geometryCompression
             returnArray['project'] = annotation.project.id
             returnArray['container'] = annotation.project.id
             returnArray['user'] = annotation.user?.id
             returnArray['reviewUser'] = annotation.reviewUser?.id
             returnArray['area'] = annotation.area
             returnArray['perimeterUnit'] = annotation.retrievePerimeterUnit()
             returnArray['areaUnit'] = annotation.retrieveAreaUnit()
             returnArray['perimeter'] = annotation.perimeter
             returnArray['centroid'] = annotation.getCentroid()
             returnArray['created'] = annotation.created?.time?.toString()
             returnArray['updated'] = annotation.updated?.time?.toString()
             returnArray['terms'] = annotation.termsId()
             returnArray['term'] = returnArray['terms']
             returnArray['similarity'] = annotation.similarity
             returnArray['rate'] = annotation.rate
             returnArray['idTerm'] = annotation.idTerm
             returnArray['idExpectedTerm'] = annotation.idExpectedTerm
             returnArray['cropURL'] = UrlApi.getReviewedAnnotationCropWithAnnotationId(annotation.id)
             returnArray['smallCropURL'] = UrlApi.getReviewedAnnotationCropWithAnnotationIdWithMaxWithOrHeight(annotation.id, 256)
             returnArray['url'] = UrlApi.getReviewedAnnotationCropWithAnnotationId(annotation.id)
             returnArray['imageURL'] = UrlApi.getAnnotationURL(imageinstance.project?.id, imageinstance.id, annotation.id)
             returnArray['reviewed'] = true
             return returnArray
         }
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
