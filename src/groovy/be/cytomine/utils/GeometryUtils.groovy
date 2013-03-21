package be.cytomine.utils

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory

/**
 * User: lrollus
 * Date: 17/10/12
 * GIGA-ULg
 * Utility class to deals with file
 */
class GeometryUtils {

    public static Geometry createBoundingBox(String bbox) {
        String[] coordinates = bbox.split(",")
        double bottomX = Double.parseDouble(coordinates[0])
        double bottomY = Double.parseDouble(coordinates[1])
        double topX = Double.parseDouble(coordinates[2])
        double topY = Double.parseDouble(coordinates[3])
        Coordinate[] boundingBoxCoordinates = [new Coordinate(bottomX, bottomY), new Coordinate(bottomX, topY), new Coordinate(topX, topY), new Coordinate(topX, bottomY), new Coordinate(bottomX, bottomY)]
        Geometry boundingbox = new GeometryFactory().createPolygon(new GeometryFactory().createLinearRing(boundingBoxCoordinates), null)
        boundingbox
    }

    public static Geometry createLittleBoundingBox(String bbox) {
        String[] coordinates = bbox.split(",")
        double bottomX = Double.parseDouble(coordinates[0])
        double bottomY = Double.parseDouble(coordinates[1])
        double topX = Double.parseDouble(coordinates[2])
        double topY = Double.parseDouble(coordinates[3])

        println "bottomX=$bottomX"
        println "topX=$topX"
        println "bottomY=$bottomY"
        println "topY=$topY"

        double realBottomX = bottomX + ((topX-bottomX)/4)
        double realBottomY = bottomY + ((topY-bottomY)/4)
        double realTopX = topX - ((topX-bottomX)/4)
        double realTopY = topY - ((topY-bottomY)/4)


        println "realBottomX=$realBottomX"
        println "realTopX=$realTopX"
        println "realBottomY=$realBottomY"

        println "realTopY=$realTopY"



        Coordinate[] boundingBoxCoordinates = [new Coordinate(realBottomX, realBottomY), new Coordinate(realBottomX, realTopY), new Coordinate(realTopX, realTopY), new Coordinate(realTopX, realBottomY), new Coordinate(realBottomX, realBottomY)]
        Geometry boundingbox = new GeometryFactory().createPolygon(new GeometryFactory().createLinearRing(boundingBoxCoordinates), null)
        boundingbox
    }


}
