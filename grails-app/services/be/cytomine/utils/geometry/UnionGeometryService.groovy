package be.cytomine.utils.geometry

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.security.SecUser
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryCollection
import com.vividsolutions.jts.geom.GeometryFactory
import groovy.sql.Sql

class UnionGeometryService {

    def dataSource

    def algoAnnotationService

    def simplifyGeometryService

    static transactional = false



    public void unionPicture(ImageInstance image, SecUser user, Term term, Integer areaWidth, Integer areaHeight,def bufferLength, def minIntersectLength) {
         //makeValidPolygon(image,user)

         //http://localhost:8080/api/algoannotation/union?idImage=8120370&idUser=11974001&idTerm=9444456&minIntersectionLength=10&bufferLength=0&area=2000

         def areas = computeArea(image,areaWidth,areaHeight)

        def unionedAnnotation = []

         areas.eachWithIndex { it, i ->
             log.info("******************** ${i}/${areas.size()}")
             boolean restart = true

             int max = 100
             while(restart && (max>0)) {
                 def result = unionArea(image,user,term,it,bufferLength,minIntersectLength)
                 restart = result.restart
                 unionedAnnotation.addAll(result.unionedAnnotation)
                 max--
             }
         }

        unionedAnnotation.unique()

        unionedAnnotation.each { idAnnotation ->
            try {
                def annotation = AnnotationDomain.getAnnotationDomain(idAnnotation)
                if(annotation && annotation.location.getNumPoints()>10000) {
                    def simplified = simplifyGeometryService.simplifyPolygon(annotation.location.toText())
                    annotation.location = simplified.geometry
                    algoAnnotationService.saveDomain(annotation)
                }
            }catch(ObjectNotFoundException e) {
            }
        }
     }
//
//     private makeValidPolygon(ImageInstance image, SecUser user) {
//         log.info "makeValidPolygon..."
//         List<AnnotationDomain> annotations
//         if (user.algo()) {
//             annotations = AlgoAnnotation.findAllByImageAndUser(image, user)
//         } else {
//             annotations = UserAnnotation.findAllByImageAndUser(image, user)
//
//         }
//         log.info "Check validation ${annotations.size()} annotations..."
//         annotations.eachWithIndex { it,i->
//             if(i%100==0) {
//                 log.info "validation ${((double)i/(double)annotations.size())*100}%"
//             }
//             if (!it.location.isValid()) {
//                 it.location = it.location.buffer(0)
//                 it.save(flush: true)
//             }
//             //UPDATE algo_annotation set location = ST_BUFFER(location,0) WHERE image_id = 8120370 and user_id = 11974001 AND st_isvalid(location)=false;
//         }
//     }

     private def computeArea(ImageInstance image, Integer maxW, Integer maxH) {
         log.info "computeArea..."
         Double width = image.baseImage.width
         Double height = image.baseImage.height

         log.info "width=$width"
         log.info "height=$height"

         log.info "maxW=$maxW"
         log.info "maxH=$maxH"

         Integer nbreAreaW =  Math.ceil(width/(double)maxW)
         Integer nbreAreaH = Math.ceil(height/(double)maxH)

         log.info "nbreAreaW=$nbreAreaW"
         log.info "height=$height"

         def areas = []
         for(int i=0;i<nbreAreaW;i++) {
             for(int j=0;j<nbreAreaH;j++) {

                 double bottomX = i*maxW
                 double bottomY = j*maxH
                 double topX = bottomX+maxW
                 double topY = bottomY+maxH

                 log.info bottomX + "x" + bottomY +" => " + topX + "x" + topY

                 Coordinate[] boundingBoxCoordinates = [new Coordinate(bottomX, bottomY), new Coordinate(bottomX, topY), new Coordinate(topX, topY), new Coordinate(topX, bottomY), new Coordinate(bottomX, bottomY)]
                 Geometry boundingbox = new GeometryFactory().createPolygon(new GeometryFactory().createLinearRing(boundingBoxCoordinates), null)
                 areas <<  boundingbox
             }
         }
         areas
     }



     private def unionArea(ImageInstance image, SecUser user, Term term, Geometry bbox, def bufferLength, def minIntersectLength) {
         log.info "unionArea... ${bbox.toString()}"
         List intersectAnnotation = intersectAnnotation(image,user,term,bbox,bufferLength,minIntersectLength)
         boolean mustBeRestart = false
         def unionedAnnotation = []

         intersectAnnotation.each {
             HashMap<Long, Long> removedByUnion = new HashMap<Long, Long>(1024)

                 long idBased = it[0]
                 //check if annotation has be deleted (because merge), if true get the union annotation
                 if (removedByUnion.containsKey(it[0]))
                     idBased = removedByUnion.get(it[0])

                 long idCompared = it[1]
                 //check if annotation has be deleted (because merge), if true get the union annotation
                 if (removedByUnion.containsKey(it[1]))
                     idCompared = removedByUnion.get(it[1])

             AnnotationDomain based
             AnnotationDomain compared

                 try {
                 based = AnnotationDomain.getAnnotationDomain(idBased)
                 compared = AnnotationDomain.getAnnotationDomain(idCompared)
                 } catch(Exception e) {

                 }

                 if (based && compared && based.id != compared.id) {
                     mustBeRestart = true

                     if(bufferLength) {
                         based.location = based.location.buffer(bufferLength)
                         compared.location = compared.location.buffer(bufferLength)
                     }


                     based.location = combineIntoOneGeometry([based.location,compared.location])

                     based.location = based.location.union()

                     if(bufferLength) {
                         based.location =  based.location.buffer(-bufferLength)
                     }

                     //based.location = simplifyGeometryService.simplifyPolygon(based.location.toText()).geometry
                     //based.location = based.location.union(compared.location)
                     removedByUnion.put(compared.id, based.id)

                     //save new annotation with union location
                     unionedAnnotation << based.id
                     if(based.algoAnnotation) {
                         algoAnnotationService.saveDomain(based)
                         //remove old annotation with data
                         AlgoAnnotationTerm.executeUpdate("delete AlgoAnnotationTerm aat where aat.annotationIdent = :annotation", [annotation: compared.id])
                         algoAnnotationService.removeDomain(compared)
                     } else {
                         algoAnnotationService.saveDomain(based)
                         //remove old annotation with data
                         AnnotationTerm.executeUpdate("delete AnnotationTerm aat where aat.userAnnotation.id = :annotation", [annotation: compared.id])
                         algoAnnotationService.removeDomain(compared)
                     }


                 }
         }
         return [restart: mustBeRestart, unionedAnnotation : unionedAnnotation]
     }

    static Geometry combineIntoOneGeometry( Collection<Geometry> geometryCollection ){
         GeometryFactory factory = new GeometryFactory();

         // note the following geometry collection may be invalid (say with overlapping polygons)
         GeometryCollection geometryCollection1 =
              (GeometryCollection) factory.buildGeometry( geometryCollection );

         return geometryCollection1.union();
     }

     private List intersectAnnotation(ImageInstance image, SecUser user, Term term, Geometry bbox, def bufferLength, def minIntersectLength) {
         String request

         if (user.algo()) {
             request = "SELECT annotation1.id as id1, annotation2.id as id2\n" +
                         " FROM algo_annotation annotation1, algo_annotation annotation2, algo_annotation_term at1, algo_annotation_term at2\n" +
                         " WHERE annotation1.image_id = $image.id\n" +
                         " AND annotation2.image_id = $image.id\n" +
                         " AND annotation2.created > annotation1.created\n" +
                         " AND annotation1.user_id = ${user.id}\n" +
                         " AND annotation2.user_id = ${user.id}\n" +
                         " AND annotation1.id = at1.annotation_ident\n" +
                         " AND annotation2.id = at2.annotation_ident\n" +
                         " AND at1.term_id = ${term.id}\n" +
                         " AND at2.term_id = ${term.id}\n" +
                         " AND ST_Intersects(annotation1.location,ST_GeometryFromText('" + bbox.toString() + "',0)) " +
                         " AND ST_Intersects(annotation2.location,ST_GeometryFromText('" + bbox.toString() + "',0)) "
         } else {
             request = "SELECT annotation1.id as id1, annotation2.id as id2\n" +
                         " FROM user_annotation annotation1, user_annotation annotation2, annotation_term at1, annotation_term at2\n" +
                         " WHERE annotation1.image_id = $image.id\n" +
                         " AND annotation2.image_id = $image.id\n" +
                         " AND annotation2.created > annotation1.created\n" +
                         " AND annotation1.user_id = ${user.id}\n" +
                         " AND annotation2.user_id = ${user.id}\n" +
                         " AND annotation1.id = at1.user_annotation_id\n" +
                         " AND annotation2.id = at2.user_annotation_id\n" +
                         " AND at1.term_id = ${term.id}\n" +
                         " AND at2.term_id = ${term.id}\n" +
                         " AND ST_Intersects(annotation1.location,ST_GeometryFromText('" + bbox.toString() + "',0)) \n" +
                         " AND ST_Intersects(annotation2.location,ST_GeometryFromText('" + bbox.toString() + "',0)) "
         }
         if(bufferLength!=null) {
             request = request + " AND ST_Perimeter(ST_Intersection(ST_Buffer(annotation1.location,$bufferLength), ST_Buffer(annotation2.location,$bufferLength)))>=$minIntersectLength\n"
         } else {
            request = request +  " AND ST_Perimeter(ST_Intersection(annotation1.location, annotation2.location))>=$minIntersectLength\n"
         }

         log.info request

         def sql = new Sql(dataSource)


         def data = []
         sql.eachRow(request) {
             data << [it[0],it[1]]
         }
         log.info "find intersect annotation... ${data.size()}"
         data
     }

    def printDebugInfo(ImageInstance image, SecUser user, Term term, Geometry bbox, def bufferLength, def minIntersectLength) {
        def request = "SELECT annotation1.id as id1, annotation2.id as id2, ST_Perimeter(ST_Intersection(ST_Buffer(annotation1.location,$bufferLength), ST_Buffer(annotation2.location,$bufferLength))), ST_AREA(annotation1.location),ST_AREA(annotation2.location)\n" +
                    " FROM user_annotation annotation1, user_annotation annotation2, annotation_term at1, annotation_term at2\n" +
                    " WHERE annotation1.image_id = $image.id\n" +
                    " AND annotation2.image_id = $image.id\n" +
                    " AND annotation2.created > annotation1.created\n" +
                    " AND annotation1.user_id = ${user.id}\n" +
                    " AND annotation2.user_id = ${user.id}\n" +
                    " AND annotation1.id = at1.user_annotation_id\n" +
                    " AND annotation2.id = at2.user_annotation_id\n" +
                    " AND at1.term_id = ${term.id}\n" +
                    " AND at2.term_id = ${term.id}\n" +
                    " AND ST_Intersects(annotation1.location,ST_GeometryFromText('" + bbox.toString() + "',0)) \n" +
                    " AND ST_Intersects(annotation2.location,ST_GeometryFromText('" + bbox.toString() + "',0)) "

        log.info request
        def sql = new Sql(dataSource)
        def data = []
        sql.eachRow(request) {
            log.info it[2] + "\t" + it[0] + "\t" + it[1]  + "\t" + it[3]  + "\t" + it[4]
        }


    }




}
