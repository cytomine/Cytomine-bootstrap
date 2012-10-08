package be.cytomine

import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.Annotation
import be.cytomine.security.SecUser
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.LineString
import groovy.sql.Sql

class UnionTestController extends RestController {

    static int minIntersectLenght = 5

    def dataSource
    def annotationService
    def domainService



    def index() {
        ImageInstance image = ImageInstance.read(params.getLong('idImage'))
        SecUser user = SecUser.read(params.getLong('idUser'))
        unionAnnotations(image, user)
    }


    private def unionAnnotations(ImageInstance image, SecUser user) {
        long start = System.currentTimeMillis()
        //unionNaive(image,user)
        //unionPostgisHSQL(image)
        unionPostgisSQL(image, user)
        //unionNaiveBetter(image)
        long end = System.currentTimeMillis()
        println "#TIME#=" + (end - start)
    }

    private def unionPostgisSQL(ImageInstance image, SecUser user) {
        println "unionPostgisSQL"

        //all annotation must be valid to compute intersection
        List<Annotation> annotations = Annotation.findAllByImageAndUser(image, user)
        println "valide annotation..."
        annotations.each {
            if (!it.location.isValid()) {
                it.location = it.location.buffer(0)
                it.save(flush: true)
            }
        }

        //key = deleted annotation, value = annotation that take in the deleted annotation
        //If y is deleted and merge with x, we add an entry <y,x>. Further if y had intersection with z, we replace "y" (deleted) by "x" (has now intersection with z).
        HashMap<Long, Long> removedByUnion = new HashMap<Long, Long>(annotations.size())

        def sql = new Sql(dataSource)
        println "********************\n********************\n********************\n********************\n"
        sql.eachRow("SELECT length(ST_Intersection(annotation1.location, annotation2.location)) as length,annotation1.id as id1, annotation2.id as id2\n" +
                " FROM annotation annotation1, annotation annotation2\n" +
                " WHERE annotation1.image_id = $image.id\n" +
                " AND annotation2.image_id = $image.id\n" +
                " AND annotation2.created > annotation1.created\n" +
                " AND annotation1.user_id = ${user.id}\n" +
                " AND annotation2.user_id = ${user.id}\n" +
                " AND ST_length2d(ST_Intersection(annotation1.location, annotation2.location))>$minIntersectLenght"
        ) {

            long idBased = it[1]
            //check if annotation has be deleted (because merge), if true get the union annotation
            if (removedByUnion.containsKey(it[1]))
                idBased = removedByUnion.get(it[1])
            long idCompared = it[2]
            //check if annotation has be deleted (because merge), if true get the union annotation
            if (removedByUnion.containsKey(it[2]))
                idCompared = removedByUnion.get(it[2])

            Annotation based = Annotation.get(idBased)
            Annotation compared = Annotation.get(idCompared)

            if (based && compared && based.id != compared.id) {
                based.location = based.location.union(compared.location)
                removedByUnion.put(compared.id, based.id)
                //save new annotation with union location
                domainService.saveDomain(based)
                //remove old annotation with data
                AlgoAnnotationTerm.executeUpdate("delete AlgoAnnotationTerm aat where aat.annotation = :annotation", [annotation: compared])
                domainService.deleteDomain(compared)

            }


        }
    }

    private def unionNaive(ImageInstance image, SecUser user) {
        println "unionNaive"

        List<Annotation> annotations = Annotation.findAllByImageAndUser(image, user)
        HashMap<Long, Long> removedByUnion = new HashMap<Long, Long>(annotations.size())

        println "valide annotation..."
        annotations.each {
            if (!it.location.isValid()) {
                it.location = it.location.buffer(0)
                it.save(flush: true)
            }
        }

        int i = 0
        def sql = new Sql(dataSource)
        println "********************\n********************\n********************\n********************\n"
        def rows = sql.rows("SELECT annotation1.id as id1, annotation2.id as id2\n" +
                " FROM annotation annotation1, annotation annotation2\n" +
                " WHERE annotation1.image_id = $image.id\n" +
                " AND annotation2.image_id = $image.id\n" +
                " AND annotation2.created > annotation1.created\n" +
                " AND annotation1.user_id = ${user.id}\n" +
                " AND annotation2.user_id = ${user.id}\n")


        rows.each {
            if (i % 1000 == 0) {
                println "Union annotation: ${i}/${rows.size()}"
                cleanUpGorm()
            }

            long idBased = it[0]
            if (removedByUnion.containsKey(it[0]))
                idBased = removedByUnion.get(it[0])
            long idCompared = it[1]
            if (removedByUnion.containsKey(it[1]))
                idCompared = removedByUnion.get(it[1])


            Annotation compared = Annotation.read(idCompared)

            if (compared) {
                Annotation based = Annotation.read(idBased)
                if (based && based.id != compared.id) {

                    Geometry intersectU = based.location.intersection(compared.location)

                    LineString lineIntersect
                    if (intersectU.coordinates.length > 1)
                        lineIntersect = new GeometryFactory().createLineString(intersectU.coordinates)
                    if (lineIntersect && lineIntersect.length >= minIntersectLenght) {
                        Geometry union = based.location.union(compared.location)
                        based.location = union
                        removedByUnion.put(compared.id, based.id)
                        domainService.saveDomain(based)
                        AlgoAnnotationTerm.executeUpdate("delete AlgoAnnotationTerm aat where aat.annotation = :annotation", [annotation: compared])
                        domainService.deleteDomain(compared)
                    }

                }
            }
            i++
        }

    }

//    def index() {
//        ImageInstance image = ImageInstance.read(params.getLong('idImage'))
//        removeAnnotations(image)
//        addAnnotations(image)
//        unionAnnotations(image)
//    }
//    private def removeAnnotations(ImageInstance image) {
//        def annotations = Annotation.findAllByImage(image)
//        annotations.each {
//            println "##### DELETE ANNOTATION " + it.id
//            //annotationService.deleteAnnotation(it.id,cytomineService.getCurrentUser(),transaction)
//            it.delete()
//        }
//    }
//
//    private def addAnnotations(ImageInstance image) {
//        createAnnotation("testUL",image,"POLYGON ((10000 20000, 15000 20000, 15000 22500, 15000 25000, 10000 25000, 10000 20000))");
//        createAnnotation("testUR",image,"POLYGON ((15000 20000, 20000 20000, 20000 25000, 15000 25000, 15000 20000))");
//        createAnnotation("testDL",image,"POLYGON ((10000 25000, 15000 25000, 15000 30000, 10000 30000, 10000 25000))");
//        createAnnotation("testDR",image,"POLYGON ((15000 25000, 20000 25000, 20000 30000, 15000 30000, 15000 25000))");
//
//        createAnnotation("test2a",image,"POLYGON ((15000 15000, 20000 15000, 22500 13000, 15000 13000, 15000 15000))");
//        createAnnotation("test2b",image,"POLYGON ((21250 14000, 20000 18000, 25000 18000, 22500 13000, 21250 14000))");
//        createAnnotation("test2c",image,"POLYGON ((15000 13000, 14000 13000, 14000 12000, 15000 12000, 15000 13000))");
//    }

//    private def createAnnotation(String name,ImageInstance image, String location) {
//        Annotation annotation = new Annotation();
//        annotation.project = image.project
//        annotation.image = image
//        annotation.name = name
//        annotation.user = User.read(16)
//        annotation.location = new WKTReader().read(location)
//        if(!annotation.validate())
//            println "errors:"+annotation.errors
//        annotation.save(flush: true)
//    }


}
