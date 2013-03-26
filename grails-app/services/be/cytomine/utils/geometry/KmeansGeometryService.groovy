package be.cytomine.utils.geometry

import be.cytomine.AnnotationDomain
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.*
import be.cytomine.security.SecUser
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import groovy.sql.Sql

class KmeansGeometryService {

    def dataSource

    public static int FULL = 3
    public static int KMEANSFULL = 2
    public static int KMEANSSOFT = 1

    public static int ANNOTATIONSIZE1 = 0
    public static int ANNOTATIONSIZE2 = 100
    public static int ANNOTATIONSIZE3 = 500
    public static int ANNOTATIONSIZE4 = 10000
    public static int ANNOTATIONSIZE5 = 100000

//    public static int ANNOTATIONSIZE1 = 0
//    public static int ANNOTATIONSIZE2 = 50
//    public static int ANNOTATIONSIZE3 = 100
//    public static int ANNOTATIONSIZE4 = 300
//    public static int ANNOTATIONSIZE5 = 400


    public static def rules = [
            100 : [((int)ANNOTATIONSIZE1): FULL, ((int)ANNOTATIONSIZE2): KMEANSFULL, ((int)ANNOTATIONSIZE3): KMEANSFULL, ((int)ANNOTATIONSIZE4): KMEANSFULL, ((int)ANNOTATIONSIZE5): KMEANSFULL],
            75 : [((int)ANNOTATIONSIZE1): FULL, ((int)ANNOTATIONSIZE2): FULL, ((int)ANNOTATIONSIZE3): FULL, ((int)ANNOTATIONSIZE4): KMEANSFULL, ((int)ANNOTATIONSIZE5): KMEANSFULL],
            50 : [((int)ANNOTATIONSIZE1): FULL, ((int)ANNOTATIONSIZE2): FULL, ((int)ANNOTATIONSIZE3): FULL, ((int)ANNOTATIONSIZE4): KMEANSFULL, ((int)ANNOTATIONSIZE5): KMEANSFULL],
            25 : [((int)ANNOTATIONSIZE1): FULL, ((int)ANNOTATIONSIZE2): FULL, ((int)ANNOTATIONSIZE3): FULL, ((int)ANNOTATIONSIZE4): FULL, ((int)ANNOTATIONSIZE5): KMEANSFULL],
            0 : [((int)ANNOTATIONSIZE1): FULL, ((int)ANNOTATIONSIZE2): FULL, ((int)ANNOTATIONSIZE3): FULL, ((int)ANNOTATIONSIZE4): FULL, ((int)ANNOTATIONSIZE5): FULL],
    ]




    public def doKeamsFullRequest(String request) {
        String requestKmeans = "SELECT kmeans, count(*), st_astext(ST_ConvexHull(ST_Collect(location))) \n" +
                "FROM (\n" + request +"\n" +") AS ksub\n" +
                "GROUP BY kmeans\n" +
                "ORDER BY kmeans;"
        return selectAnnotationLightKmeans(requestKmeans)
    }

    public def doKeamsSoftRequest(String request) {
        String requestKmeans = "SELECT kmeans, count(*), st_astext(ST_Centroid(ST_Collect(location))) \n" +
                "FROM (\n" + request +"\n" +") AS ksub\n" +
                "GROUP BY kmeans\n" +
                "ORDER BY kmeans;"
        return selectAnnotationLightKmeans(requestKmeans)
    }

    private def selectAnnotationLightKmeans(String request) {
        def data = []
        println request

        double max = 1

        new Sql(dataSource).eachRow(request) {

            long idK = it[0]
            long count = it[1]
            if(count>max) {
                max = count
            }
            String location = it[2]
            data << [id: idK, location: location, term:  [], count: count]
        }

        data.each {
            it.ratio = ((double)it.count/max)
        }

        data
    }

    public int mustBeReduce(ImageInstance image, SecUser user, Geometry bbox) {
        if (image.baseImage.width==null) {
            return  FULL
        }

        double imageWidth = image.baseImage.width
        double bboxWidth = bbox.getEnvelopeInternal().width

        double ratio = bboxWidth/imageWidth

        log.info "imageWidth=$imageWidth"
        log.info "bboxWidth=$bboxWidth"
        log.info "ratio=$ratio"

        int ratio25 = ((int)((ratio/25d)*100))*25

        log.info "ration25=$ratio25"

        def ruleLine = rules.get(Math.min(ratio25,100))

        log.info "ruleLine=$ruleLine"

        def rule = getRuleForNumberOfAnnotations((user.algo()? image.countImageJobAnnotations : image.countImageAnnotations), ruleLine)

        log.info "rule=$rule"

        return rule
    }

    public def getRuleForNumberOfAnnotations(def annotations, def ruleLine) {
        println "getRuleForNumberOfAnnotations=$annotations"
        if (annotations >= ANNOTATIONSIZE5) return ruleLine.get(ANNOTATIONSIZE5)
        println "5"
        println ruleLine
        println ruleLine.get(ANNOTATIONSIZE4)

        if (annotations >= ANNOTATIONSIZE4) return ruleLine.get(ANNOTATIONSIZE4)
        println "4"
        if (annotations >= ANNOTATIONSIZE3) return ruleLine.get(ANNOTATIONSIZE3)
        println "3"
        if (annotations >= ANNOTATIONSIZE2) return ruleLine.get(ANNOTATIONSIZE2)
        println "2"
        if (annotations >= ANNOTATIONSIZE1) return ruleLine.get(ANNOTATIONSIZE1)
        println "1"
    }

}
