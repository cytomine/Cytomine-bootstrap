package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.security.UserJob
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.image.ImageInstance
import grails.converters.JSON
import org.apache.log4j.Logger
import be.cytomine.api.UrlApi

class AlgoAnnotation extends AnnotationDomain implements Serializable {

    UserJob user
    Integer countReviewedAnnotations = 0

    static constraints = {
    }

    static mapping = {
          id generator: "assigned"
          columns {
              location type: org.hibernatespatial.GeometryUserType
          }
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
        return true
    }

    List<Term> termsForReview() {
        AlgoAnnotationTerm.findAllByAnnotationIdentAndUserJob(id,user).collect{it.term}.unique()
    }

    boolean hasReviewedAnnotation() {
        return countReviewedAnnotations>0
    }


    static AlgoAnnotation createFromDataWithId(json) {
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
    static AlgoAnnotation createFromData(jsonAnnotation) {
        def annotation = new AlgoAnnotation()
        getFromData(annotation, jsonAnnotation)
    }

    /**
     * Fill annotation with data attributes
     * So, jsonAnnotation must have jsonAnnotation.location, jsonAnnotation.name, ...
     * @param annotation Annotation Source
     * @param jsonAnnotation JSON
     * @return annotation with json attributes
     */
    static AlgoAnnotation getFromData(AlgoAnnotation annotation, jsonAnnotation) {
        try {
            annotation.geometryCompression = (!jsonAnnotation.geometryCompression.toString().equals("null")) ? ((String) jsonAnnotation.geometryCompression).toDouble() : 0
            annotation.created = (!jsonAnnotation.created.toString().equals("null")) ? new Date(Long.parseLong(jsonAnnotation.created)) : null
            annotation.updated = (!jsonAnnotation.updated.toString().equals("null")) ? new Date(Long.parseLong(jsonAnnotation.updated)) : null

            //location
            annotation.location = new WKTReader().read(jsonAnnotation.location)
            if (annotation.location.getNumPoints() < 1) throw new WrongArgumentException("Geometry is empty:" + annotation.location.getNumPoints() + " points")
            //if (annotation.location.getNumPoints() < 3) throw new WrongArgumentException("Geometry is not a polygon :" + annotation.location.getNumPoints() + " points")
            if (!annotation.location) throw new WrongArgumentException("Geo is null: 0 points")
            //image
            annotation.image = ImageInstance.get(jsonAnnotation.image);
            if (!annotation.image) throw new WrongArgumentException("Image $jsonAnnotation.image not found!")
            //project
            annotation.project = be.cytomine.project.Project.get(jsonAnnotation.project);
            if (!annotation.project) throw new WrongArgumentException("Project $jsonAnnotation.project not found!")
            //user
            annotation.user = UserJob.get(jsonAnnotation.user);
            if (!annotation.user) throw new WrongArgumentException("User $jsonAnnotation.user not found!")

        } catch (com.vividsolutions.jts.io.ParseException ex) {
            throw new WrongArgumentException(ex.toString())
        }
        return annotation;
    }




    static void registerMarshaller(String cytomineBaseUrl) {
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
            returnArray['created'] = annotation.created ? annotation.created.time.toString() : null
            returnArray['updated'] = annotation.updated ? annotation.updated.time.toString() : null
            returnArray['term'] = annotation.termsId()

            try {if (annotation?.similarity) returnArray['similarity'] = annotation.similarity} catch (Exception e) {}
            try {if (annotation?.rate) returnArray['rate'] = annotation.rate} catch (Exception e) {}
            try {if (annotation?.rate) returnArray['idTerm'] = annotation.idTerm} catch (Exception e) {}
            try {if (annotation?.rate) returnArray['idExpectedTerm'] = annotation.idExpectedTerm} catch (Exception e) {}

            returnArray['cropURL'] = UrlApi.getAlgoAnnotationCropWithAnnotationId(cytomineBaseUrl,annotation.id)
            returnArray['smallCropURL'] = UrlApi.getAlgoAnnotationCropWithAnnotationIdWithMaxWithOrHeight(cytomineBaseUrl,annotation.id, 256)
            returnArray['url'] = UrlApi.getAlgoAnnotationCropWithAnnotationId(cytomineBaseUrl,annotation.id)
            returnArray['imageURL'] = UrlApi.getAnnotationURL(cytomineBaseUrl,imageinstance.project?.id, imageinstance.id, annotation.id)

            returnArray['reviewed'] = annotation.hasReviewedAnnotation()

            return returnArray
        }
    }
}
