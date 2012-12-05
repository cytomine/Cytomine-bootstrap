package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.security.UserJob
import org.apache.log4j.Logger
import grails.converters.JSON
import be.cytomine.security.User
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.image.ImageInstance
import be.cytomine.api.UrlApi
import be.cytomine.security.SecUser

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

     static ReviewedAnnotation createFromDataWithId(json) {
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
     static ReviewedAnnotation createFromData(jsonAnnotation) {
         def annotation = new ReviewedAnnotation()
         getFromData(annotation, jsonAnnotation)
     }



     /**
      * Fill annotation with data attributes
      * So, jsonAnnotation must have jsonAnnotation.location, jsonAnnotation.name, ...
      * @param annotation Annotation Source
      * @param jsonAnnotation JSON
      * @return annotation with json attributes
      */
     static ReviewedAnnotation getFromData(ReviewedAnnotation annotation, def jsonAnnotation) {
         try {
             println jsonAnnotation
             //TODO:: refactore this to share common code (userannotation, algoannotation,...)
             annotation.geometryCompression = (!jsonAnnotation.geometryCompression.toString().equals("null")) ? ((String) jsonAnnotation.geometryCompression).toDouble() : 0
             annotation.created = (!jsonAnnotation.created.toString().equals("null")) ? new Date(Long.parseLong(jsonAnnotation.created)) : null
             annotation.updated = (!jsonAnnotation.updated.toString().equals("null")) ? new Date(Long.parseLong(jsonAnnotation.updated)) : null

             //location
             annotation.location = new WKTReader().read(jsonAnnotation.location)
             if (annotation.location.getNumPoints() < 1) throw new WrongArgumentException("Geometry is empty:" + annotation.location.getNumPoints() + " points")
             //if (annotation.location.getNumPoints() < 3) throw new WrongArgumentException("Geometry is not a polygon :" + annotation.location.getNumPoints() + " points")
             if (!annotation.location) throw new WrongArgumentException("Geo is null: 0 points")
             //image
             try {
                 annotation.image = ImageInstance.read(Long.parseLong(jsonAnnotation.image.toString()));
                 if (!annotation.image) throw new Exception()
             }catch(Exception e) {throw new WrongArgumentException("Image $jsonAnnotation.image not found!:"+e)}

             //project
             try {
                 annotation.project = be.cytomine.project.Project.read(Long.parseLong(jsonAnnotation.project.toString()));
                 if (!annotation.project) throw new Exception()
             }catch(Exception e) {throw new WrongArgumentException("Project $jsonAnnotation.project not found!:"+e)}

             //user
             try {
                 annotation.user = SecUser.read(Long.parseLong(jsonAnnotation.user.toString()));
                 if (!annotation.user) throw new Exception()
             }catch(Exception e) {throw new WrongArgumentException("User $jsonAnnotation.user not found!:"+e)}

             //review user
             try {
                 annotation.reviewUser = SecUser.read(Long.parseLong(jsonAnnotation.reviewUser.toString()));
                 if (!annotation.reviewUser) throw new Exception()
             }catch(Exception e) {throw new WrongArgumentException("User $jsonAnnotation.reviewUser not found!:"+e)}

             //annotation parent
             String annotationParentId = jsonAnnotation.parentIdent?.toString()
             if(annotationParentId==null || annotationParentId.equals("") || annotationParentId.equals("null"))
                 annotationParentId = jsonAnnotation.annotation?.toString()
             def annotationParent = UserAnnotation.read(annotationParentId)
             if(!annotationParent){
                 annotationParent = AlgoAnnotation.read(annotationParentId)
             }
             if (annotationParent == null) throw new WrongArgumentException("Annotation was not found with id:" + annotationParent)
             annotation.parentClassName = annotationParent.class.getName()
             annotation.parentIdent = annotationParent.id

             //status
             try {
                 annotation.status = Long.parseLong(jsonAnnotation.status.toString())
             }catch(Exception e) {throw new WrongArgumentException("Status $jsonAnnotation.status invalid!:"+e)}

             //term
             println "jsonAnnotation.term="+jsonAnnotation.term
             if(annotation.term) annotation.term.clear()
             if (jsonAnnotation.term == null || jsonAnnotation.term.equals("null")) throw new WrongArgumentException("Term list was not found")
             jsonAnnotation.term.each {
                 annotation.addToTerm(Term.read(it))
             }
             println "annotation.term="+annotation.term
             //if (!annotation.term || annotation.term.isEmpty()) throw new WrongArgumentException("Term list cannot be empty: json.term = "+jsonAnnotation.term+" annotation.term="+annotation.term)


         } catch (com.vividsolutions.jts.io.ParseException ex) {
             throw new WrongArgumentException(ex.toString())
         }
         return annotation;
     }

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
