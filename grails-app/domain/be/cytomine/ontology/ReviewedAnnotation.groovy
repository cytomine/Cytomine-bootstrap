package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.apache.log4j.Logger

class ReviewedAnnotation extends AnnotationDomain implements Serializable {

    static hasMany = [ term: Term ]

    String parentClassName
    Long parentIdent
    Integer status
    SecUser user
    SecUser reviewUser

    static constraints = {
    }

    static mapping = {
          id generator: "assigned"
          columns {
              location type: org.hibernatespatial.GeometryUserType
          }
        term fetch: 'join'
        wktLocation(type: 'text')

     }

    public String toString() {
         return "ReviewedAnnotation" + " " + parentClassName + ":" + parentIdent + " with term " + term + " from userjob " + user + " and  project " + project
     }

    public void putParentAnnotation(AnnotationDomain annotation) {
        parentClassName = annotation.class.getName()
        parentIdent = annotation.id
    }

    public AnnotationDomain retrieveParentAnnotation() {
        Class.forName(parentClassName, false, Thread.currentThread().contextClassLoader).read(parentIdent)
    }

    public static AnnotationDomain retrieveParentAnnotation(String id, String className) {
        Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
    }

    public static AnnotationDomain retrieveParentAnnotation(Long id, String className) {
        Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
    }

    def getCropUrl(String cytomineUrl) {
        UrlApi.getReviewedAnnotationCropWithAnnotationId(cytomineUrl,id)
    }

    def terms() {
        return term
    }

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

    List<Term> termsForReview() {
        term
    }


    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
     static ReviewedAnnotation createFromDataWithId(def json) {
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
     static ReviewedAnnotation createFromData(def json) {
         def annotation = new ReviewedAnnotation()
         insertDataIntoDomain(annotation, json)
     }


    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */     
     static ReviewedAnnotation insertDataIntoDomain(def domain, def json) {
         try {
             println json
             //TODO:: refactore this to share common code (userannotation, algoannotation,...)
             domain.geometryCompression = (!json.geometryCompression.toString().equals("null")) ? ((String) json.geometryCompression).toDouble() : 0
             domain.created = (!json.created.toString().equals("null")) ? new Date(Long.parseLong(json.created)) : null
             domain.updated = (!json.updated.toString().equals("null")) ? new Date(Long.parseLong(json.updated)) : null

             //location
             domain.location = new WKTReader().read(json.location)
             if (domain.location.getNumPoints() < 1) throw new WrongArgumentException("Geometry is empty:" + domain.location.getNumPoints() + " points")
             //if (annotation.location.getNumPoints() < 3) throw new WrongArgumentException("Geometry is not a polygon :" + annotation.location.getNumPoints() + " points")
             if (!domain.location) throw new WrongArgumentException("Geo is null: 0 points")
             //image
             try {
                 domain.image = ImageInstance.read(Long.parseLong(json.image.toString()));
                 if (!domain.image) throw new Exception()
             }catch(Exception e) {throw new WrongArgumentException("Image $json.image not found!:"+e)}

             //project
             try {
                 domain.project = be.cytomine.project.Project.read(Long.parseLong(json.project.toString()));
                 if (!domain.project) throw new Exception()
             }catch(Exception e) {throw new WrongArgumentException("Project $json.project not found!:"+e)}

             //user
             try {
                 domain.user = SecUser.read(Long.parseLong(json.user.toString()));
                 if (!domain.user) throw new Exception()
             }catch(Exception e) {throw new WrongArgumentException("User $json.user not found!:"+e)}

             //review user
             try {
                 domain.reviewUser = SecUser.read(Long.parseLong(json.reviewUser.toString()));
                 if (!domain.reviewUser) throw new Exception()
             }catch(Exception e) {throw new WrongArgumentException("User $json.reviewUser not found!:"+e)}

             //annotation parent
             String annotationParentId = json.parentIdent?.toString()
             if(annotationParentId==null || annotationParentId.equals("") || annotationParentId.equals("null"))
                 annotationParentId = json.annotation?.toString()
             def annotationParent = UserAnnotation.read(annotationParentId)
             if(!annotationParent){
                 annotationParent = AlgoAnnotation.read(annotationParentId)
             }
             if (annotationParent == null) throw new WrongArgumentException("Annotation was not found with id:" + annotationParent)
             domain.parentClassName = annotationParent.class.getName()
             domain.parentIdent = annotationParent.id

             //status
             try {
                 domain.status = Long.parseLong(json.status.toString())
             }catch(Exception e) {throw new WrongArgumentException("Status $json.status invalid!:"+e)}

             //term
             println "json.term="+json.term
             if(domain.term) domain.term.clear()
             if (json.term == null || json.term.equals("null")) throw new WrongArgumentException("Term list was not found")
             json.term.each {
                 domain.addToTerm(Term.read(it))
             }
             println "annotation.term="+domain.term
             //if (!annotation.term || annotation.term.isEmpty()) throw new WrongArgumentException("Term list cannot be empty: json.term = "+json.term+" annotation.term="+annotation.term)


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
             returnArray['area'] = annotation.computeArea()
             returnArray['perimeter'] = annotation.computePerimeter()
             returnArray['centroid'] = annotation.getCentroid()
             returnArray['created'] = annotation.created ? annotation.created.time.toString() : null
             returnArray['updated'] = annotation.updated ? annotation.updated.time.toString() : null
             //println "save json:"+annotation.termsId()
             returnArray['term'] = annotation.termsId()

             try {if (annotation?.similarity) returnArray['similarity'] = annotation.similarity} catch (Exception e) {}
             try {if (annotation?.rate) returnArray['rate'] = annotation.rate} catch (Exception e) {}
             try {if (annotation?.rate) returnArray['idTerm'] = annotation.idTerm} catch (Exception e) {}
             try {if (annotation?.rate) returnArray['idExpectedTerm'] = annotation.idExpectedTerm} catch (Exception e) {}

             returnArray['cropURL'] = UrlApi.getReviewedAnnotationCropWithAnnotationId(cytomineBaseUrl,annotation.id)
             returnArray['smallCropURL'] = UrlApi.getReviewedAnnotationCropWithAnnotationIdWithMaxWithOrHeight(cytomineBaseUrl,annotation.id, 256)
             returnArray['url'] = UrlApi.getReviewedAnnotationCropWithAnnotationId(cytomineBaseUrl,annotation.id)
             returnArray['imageURL'] = UrlApi.getAnnotationURL(cytomineBaseUrl,imageinstance.project?.id, imageinstance.id, annotation.id)

             returnArray['reviewed'] = true

             return returnArray
         }
     }

    def getObjectMap(String cytomineBaseUrl) {
        ReviewedAnnotation annotation = this
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
        returnArray['area'] = annotation.computeArea()
        returnArray['perimeter'] = annotation.computePerimeter()
        returnArray['centroid'] = annotation.getCentroid()
        returnArray['created'] = annotation.created ? annotation.created.time.toString() : null
        returnArray['updated'] = annotation.updated ? annotation.updated.time.toString() : null
        //println "save json:"+annotation.termsId()
        returnArray['term'] = annotation.termsId()

        try {if (annotation?.similarity) returnArray['similarity'] = annotation.similarity} catch (Exception e) {}
        try {if (annotation?.rate) returnArray['rate'] = annotation.rate} catch (Exception e) {}
        try {if (annotation?.rate) returnArray['idTerm'] = annotation.idTerm} catch (Exception e) {}
        try {if (annotation?.rate) returnArray['idExpectedTerm'] = annotation.idExpectedTerm} catch (Exception e) {}

        returnArray['cropURL'] = UrlApi.getReviewedAnnotationCropWithAnnotationId(cytomineBaseUrl,annotation.id)
        returnArray['smallCropURL'] = UrlApi.getReviewedAnnotationCropWithAnnotationIdWithMaxWithOrHeight(cytomineBaseUrl,annotation.id, 256)
        returnArray['url'] = UrlApi.getReviewedAnnotationCropWithAnnotationId(cytomineBaseUrl,annotation.id)
        returnArray['imageURL'] = UrlApi.getAnnotationURL(cytomineBaseUrl,imageinstance.project?.id, imageinstance.id, annotation.id)

        returnArray['reviewed'] = true

        return returnArray
    }




}
