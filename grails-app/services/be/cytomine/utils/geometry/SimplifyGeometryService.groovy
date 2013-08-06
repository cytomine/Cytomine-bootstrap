package be.cytomine.utils.geometry

import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.Polygon
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier

/**
 * Simplify polygon
 * Usefull for annotation when user use FreeHand mode.
 * Freehand = polygon with too much points (difficult to edit, heavy in database,...)
 */
class SimplifyGeometryService {

    def transactional = false

    static transaction = false

    /**
     * Simplify form (limit point number)
     * Return simplify polygon and the rate used for simplification
     */
    def simplifyPolygon(String form, def minPoint = null, def maxPoint = null) {
        Geometry annotationFull = new WKTReader().read(form);
        println "annotationFull.getNumGeometries()=${annotationFull.getNumGeometries()}"
        println "annotationFull.getNumInteriorRing()=${annotationFull.getNumInteriorRing()}"
        int numOfGeometry = annotationFull.getNumGeometries()*annotationFull.getNumInteriorRing()
        numOfGeometry = Math.max(1,numOfGeometry)
        if(numOfGeometry>10) {
            numOfGeometry = numOfGeometry/2
        }

        println "numOfGeometry=$numOfGeometry"


//        log.info "minPoint=$minPoint maxPoint=$maxPoint"
        Geometry lastAnnotationFull = annotationFull
        double ratioMax = 1.3d
        double ratioMin = 1.7d
        /* Number of point (ex: 500 points) */
        double numberOfPoint = annotationFull.getNumPoints()
        /* Maximum number of point that we would have (500/5 (max 150)=max 100 points)*/
        double rateLimitMax = Math.max(numberOfPoint / ratioMax, numOfGeometry*200)
        if(maxPoint) {
            //overide if max/minpoint is in argument
            rateLimitMax = maxPoint
        }
        /* Minimum number of point that we would have (500/10 (min 10 max 100)=min 50 points)*/
        double rateLimitMin = Math.min(Math.max(numberOfPoint / ratioMin, 10), numOfGeometry*100)
        if(minPoint) {
            //overide if max/minpoint is in argument
            rateLimitMin = minPoint
        }
//        log.info "rateLimitMax=$rateLimitMax rateLimitMin=$rateLimitMin"

        /* Increase value for the increment (allow to converge faster) */
        float incrThreshold = 0.25f
        double increaseIncrThreshold = numberOfPoint / 100d
        float i = 0;
        /* Max number of loop (prevent infinite loop) */
        int maxLoop = 1000
        double rate = 0
//        println "numberOfPoint=$numberOfPoint"
        Boolean isPolygonAndNotValid = (annotationFull instanceof com.vividsolutions.jts.geom.Polygon && !((Polygon) annotationFull).isValid())
        while (numberOfPoint > rateLimitMax && maxLoop > 0) {
            println "simplify"
            rate = i
            if (isPolygonAndNotValid) {
                lastAnnotationFull = TopologyPreservingSimplifier.simplify(annotationFull, rate)
            } else {
                lastAnnotationFull = DouglasPeuckerSimplifier.simplify(annotationFull, rate)
            }
//            println "numberOfPoint=${lastAnnotationFull.getNumPoints()}"
            if (lastAnnotationFull.getNumPoints() < rateLimitMin) break;
            annotationFull = lastAnnotationFull
            i = i + (incrThreshold * increaseIncrThreshold); maxLoop--;
        }
        println "POINTS="+lastAnnotationFull.getNumPoints()
        return [geometry: annotationFull, rate: rate]
    }

    def simplifyPolygon(String form, double rate) {
        Geometry annotation = new WKTReader().read(form);
        Boolean isPolygonAndNotValid = (annotation instanceof com.vividsolutions.jts.geom.Polygon && !((Polygon) annotationFull).isValid())
        if (isPolygonAndNotValid) {
            //Preserving polygon shape but slower than DouglasPeuker
            annotation = TopologyPreservingSimplifier.simplify(annotation, rate)
        } else {
            annotation = DouglasPeuckerSimplifier.simplify(annotation, rate)
        }
        return [geometry: annotation, rate: rate]
    }

}
