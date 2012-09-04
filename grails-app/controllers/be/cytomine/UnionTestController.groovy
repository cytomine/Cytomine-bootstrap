package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.command.Transaction
import be.cytomine.ontology.Annotation
import be.cytomine.security.SecUser
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.security.User
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.LineString
import com.vividsolutions.jts.geom.GeometryFactory
import groovy.sql.Sql
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.api.RestController
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier

class UnionTestController extends RestController {

    static int minIntersectLenght = 100

    def dataSource
    def annotationService
    def cytomineService
    def transactionService
    def domainService

//    def index() {
//        ImageInstance image = ImageInstance.read(params.getLong('idImage'))
//        removeAnnotations(image)
//        addAnnotations(image)
//        unionAnnotations(image)
//    }

    def index() {
        ImageInstance image = ImageInstance.read(params.getLong('idImage'))
        SecUser user =  SecUser.read(params.getLong('idUser'))
        unionAnnotations(image,user)
    }

    private def removeAnnotations(ImageInstance image) {
        def annotations = Annotation.findAllByImage(image)
        annotations.each {
            println "##### DELETE ANNOTATION " + it.id
            //annotationService.deleteAnnotation(it.id,cytomineService.getCurrentUser(),transaction)
            it.delete()
        }
    }

    private def addAnnotations(ImageInstance image) {
        createAnnotation("testUL",image,"POLYGON ((10000 20000, 15000 20000, 15000 22500, 15000 25000, 10000 25000, 10000 20000))");
        createAnnotation("testUR",image,"POLYGON ((15000 20000, 20000 20000, 20000 25000, 15000 25000, 15000 20000))");
        createAnnotation("testDL",image,"POLYGON ((10000 25000, 15000 25000, 15000 30000, 10000 30000, 10000 25000))");
        createAnnotation("testDR",image,"POLYGON ((15000 25000, 20000 25000, 20000 30000, 15000 30000, 15000 25000))");

        createAnnotation("test2a",image,"POLYGON ((15000 15000, 20000 15000, 22500 13000, 15000 13000, 15000 15000))");
        createAnnotation("test2b",image,"POLYGON ((21250 14000, 20000 18000, 25000 18000, 22500 13000, 21250 14000))");
        createAnnotation("test2c",image,"POLYGON ((15000 13000, 14000 13000, 14000 12000, 15000 12000, 15000 13000))");
    }

    private def createAnnotation(String name,ImageInstance image, String location) {
        Annotation annotation = new Annotation();
        annotation.project = image.project
        annotation.image = image
        annotation.name = name
        annotation.user = User.read(16)
        annotation.location = new WKTReader().read(location)
        if(!annotation.validate())
            println "errors:"+annotation.errors
        annotation.save(flush: true)
    }

    private def unionAnnotations(ImageInstance image, SecUser user) {
//          def data = [915145, 915175, 915275, 915301, 915345, 916710, 916697, 916606, 916489, 916892, 916853, 917203, 917100, 917022, 917385, 917372, 917346, 915631, 915644, 915670]
//            data.each {
//                Annotation annotation = Annotation.read(it)
//                Geometry location = annotation.location
//                println "**************************************"
//                println "isValid="+location.isValid()
//                println "isValid.buffer="+location.buffer(0).isValid()
//                println "isValid.topology="+new TopologyPreservingSimplifier(location).getResultGeometry().isValid()
//
//            }





        long start = System.currentTimeMillis()
        //unionNaive(image,user)
        //unionPostgisHSQL(image)
        unionPostgisSQL(image,user)
        //unionNaiveBetter(image)
        long end = System.currentTimeMillis()
        long time =  end-start
        println "#TIME#=" + time
//        unionPostgisHSQL(image)
//        unionPostgisSQL(image)

    }


    private def unionNaive(ImageInstance image,SecUser user) {
        println "unionNaive"

        List<Annotation> annotations = Annotation.findAllByImageAndUser(image,user)
        HashMap<Long,Long> removedByUnion = new HashMap<Long,Long>(annotations.size())

        println "valide annotation..."
        annotations.each {
            if(!it.location.isValid()) {
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
            if(i%1000==0) {
                println "XXXXXXXXXXXXXXXXXXXXX ${i}/${rows.size()}"
                cleanUpGorm()
            }
            //if(i>10000) return

            long idBased = it[0]
            if(removedByUnion.containsKey(it[0]))
                idBased = removedByUnion.get(it[0])
            long idCompared = it[1]
            if(removedByUnion.containsKey(it[1]))
                idCompared = removedByUnion.get(it[1])


            Annotation compared = Annotation.read(idCompared)

            if(compared) {
                Annotation based = Annotation.read(idBased)
                if(based && based.id!=compared.id) {

                        Geometry intersectU = based.location.intersection(compared.location)

                        LineString lineIntersect
                        if(intersectU.coordinates.length>1)
                            lineIntersect =  new GeometryFactory().createLineString(intersectU.coordinates)
                        if(lineIntersect && lineIntersect.length>=minIntersectLenght) {
                            //println "based.location="+based.location
                            //println "compared.location="+compared.location
                            Geometry union = based.location.union(compared.location)
                            //println "based.location.union(compared.location)="+union
                            based.location = union
                            removedByUnion.put(compared.id,based.id)
                            domainService.saveDomain(based)
                            //println "based.location.union(compared.location)="+Annotation.read(it[0]).location

                            AlgoAnnotationTerm.executeUpdate("delete AlgoAnnotationTerm aat where aat.annotation = :annotation", [annotation:compared])
                            domainService.deleteDomain(compared)
                        }

                }
            }
            i++
        }

    }




//    private def unionPostgisHSQL(ImageInstance image) {
//        println "unionPostgisHSQL"
//        def result = Annotation.executeQuery( "SELECT length(ST_Intersection(annotation1.location, annotation2.location)) as similarity,annotation1, annotation2\n" +
//                " FROM Annotation annotation1, Annotation annotation2\n" +
//                " WHERE annotation1.image.id = ${image.id}\n" +
//                " AND annotation2.image.id = ${image.id}\n" +
//                " AND annotation2.created > annotation1.created\n" +
//                " AND ST_IsValid(annotation1.location) \n" +
//                " AND ST_IsValid(annotation2.location) \n" +
//                " AND ST_length2d(ST_Intersection(annotation1.location, annotation2.location))>$minIntersectLenght"
//        )
//        println "result="+result
//
//        List<Long> removedByUnion = []
//        result.each {
//            Annotation based = it[1]
//            Annotation compared = it[2]
//            if(!removedByUnion.contains(compared.id)) {
//                based.location = based.location.union(compared.location)
//                removedByUnion << compared.id
//                based.save(flush: true)
//                compared.delete(flush: true)
//            }
//        }
//
//    }

    private def unionPostgisSQL(ImageInstance image,SecUser user) {
         println "unionPostgisSQL"

         List<Annotation> annotations = Annotation.findAllByImageAndUser(image,user)
         HashMap<Long,Long> removedByUnion = new HashMap<Long,Long>(annotations.size())

         println "valide annotation..."
         annotations.each {
             if(!it.location.isValid()) {
                 it.location = it.location.buffer(0)
                 it.save(flush: true)
             }
         }

          def sql = new Sql(dataSource)
          println "********************\n********************\n********************\n********************\n"
          sql.eachRow( "SELECT length(ST_Intersection(annotation1.location, annotation2.location)) as length,annotation1.id as id1, annotation2.id as id2\n" +
                  " FROM annotation annotation1, annotation annotation2\n" +
                  " WHERE annotation1.image_id = $image.id\n" +
                  " AND annotation2.image_id = $image.id\n" +
                  " AND annotation2.created > annotation1.created\n" +
                  " AND annotation1.user_id = ${user.id}\n" +
                  " AND annotation2.user_id = ${user.id}\n" +
                  " AND ST_length2d(ST_Intersection(annotation1.location, annotation2.location))>$minIntersectLenght"
          ) {
                 //println it
                 long idBased = it[1]
                 if(removedByUnion.containsKey(it[1]))
                     idBased = removedByUnion.get(it[1])
                 long idCompared = it[2]
                 if(removedByUnion.containsKey(it[2]))
                     idCompared = removedByUnion.get(it[2])

                  Annotation based = Annotation.get(idBased)
                  Annotation compared = Annotation.get(idCompared)
                  if(based && compared && based.id!=compared.id) {
                      based.location = based.location.union(compared.location)

                      removedByUnion.put(compared.id,based.id)

                     domainService.saveDomain(based)
                     //println "based.location.union(compared.location)="+Annotation.read(it[0]).location

                     AlgoAnnotationTerm.executeUpdate("delete AlgoAnnotationTerm aat where aat.annotation = :annotation", [annotation:compared])
                     domainService.deleteDomain(compared)

                 }


          }
     }
}
