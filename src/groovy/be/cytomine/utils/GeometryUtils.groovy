package be.cytomine.utils

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.LinearRing

/**
 * User: lrollus
 * Date: 17/10/12
 * GIGA-ULg
 * Utility class to deals with file
 */
class GeometryUtils {

    public static Geometry createBoundingBox(String bbox) {
        String[] coordinates = bbox.split(",")
        double minX = Double.parseDouble(coordinates[0])
        double minY = Double.parseDouble(coordinates[1])
        double maxX = Double.parseDouble(coordinates[2])
        double maxY = Double.parseDouble(coordinates[3])
        return GeometryUtils.createBoundingBox(minX, maxX, minY , maxY)
    }

    public static Geometry createBoundingBox(double minX, double maxX, double minY, double maxY) {
        Coordinate[] roiPoints = new Coordinate[5]
        roiPoints[0] = new Coordinate(minX, minY)
        roiPoints[1] = new Coordinate(minX, maxY)
        roiPoints[2] = new Coordinate(maxX, maxY)
        roiPoints[3] = new Coordinate(maxX, minY)
        roiPoints[4] = roiPoints[0]
        //Build geometry
        LinearRing linearRing = new GeometryFactory().createLinearRing(roiPoints)
        return new GeometryFactory().createPolygon(linearRing)
    }
}
