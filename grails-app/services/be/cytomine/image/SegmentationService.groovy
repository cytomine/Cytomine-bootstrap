package be.cytomine.image

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.LineString
import ij.ImagePlus
import ij.process.ImageProcessor
import ij.process.PolygonFiller


import java.awt.Color
import java.awt.Rectangle
import java.awt.image.BufferedImage

/**
 * TODO: refactoring + doc + test
 */
class SegmentationService {

    static transactional = false


    public BufferedImage colorizeWindow(AbstractImage image, BufferedImage window, Collection<Geometry> geometryCollection, int x, int y, double x_ratio, double y_ratio) {

        for (geometry in geometryCollection) {

            if (geometry instanceof com.vividsolutions.jts.geom.MultiPolygon) {
                com.vividsolutions.jts.geom.MultiPolygon multiPolygon = (com.vividsolutions.jts.geom.MultiPolygon) geometry;
                for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                    window = colorizeWindow(image, window, multiPolygon.getGeometryN(i), x, y, x_ratio, y_ratio)
                }
            } else {
                window = colorizeWindow(image, window, geometry, x, y, x_ratio, y_ratio)
            }
        }

        return window
    }

    public BufferedImage colorizeWindow(AbstractImage image, BufferedImage window,  com.vividsolutions.jts.geom.Geometry geometry, int x, int y, double x_ratio, double y_ratio) {

        if (geometry instanceof com.vividsolutions.jts.geom.Polygon) {
            com.vividsolutions.jts.geom.Polygon polygon = (com.vividsolutions.jts.geom.Polygon) geometry;
            window = colorizeWindow(image, window, polygon, x, y, x_ratio, y_ratio)
        }

        return window
    }

    public BufferedImage colorizeWindow(AbstractImage image, BufferedImage window, com.vividsolutions.jts.geom.Polygon polygon, int x, int y, double x_ratio, double y_ratio) {

        window = colorizeWindow(image, window, polygon.getExteriorRing(), Color.WHITE, x, y, x_ratio, y_ratio)
        for (def j = 0; j < polygon.getNumInteriorRing(); j++) {
            window = colorizeWindow(image, window, polygon.getInteriorRingN(j), Color.BLACK, x, y, x_ratio, y_ratio)
        }

        return window
    }

    public BufferedImage colorizeWindow(AbstractImage image, BufferedImage window, LineString lineString, Color color, int x, int y, double x_ratio, double y_ratio) {
        ImagePlus imagePlus = new ImagePlus("", window)
        ImageProcessor ip = imagePlus.getProcessor()
        ip.setColor(color)
        //int[] pixels = (int[]) ip.getPixels()

        Collection<Coordinate> coordinates = lineString.getCoordinates()
        int[] _x = new int[coordinates.size()]
        int[] _y = new int[coordinates.size()]
        coordinates.eachWithIndex { coordinate, i ->
            if(i%100==0) {
                //println "*** $i/${coordinates.size()} coordinates"
            }
            int xLocal = Math.min((coordinate.x - x) * x_ratio, window.getWidth());
            xLocal = Math.max(0, xLocal)
            int yLocal = Math.min((image.getHeight() - coordinate.y - y) * y_ratio, window.getHeight());
            yLocal = Math.max(0, yLocal)
            _x[i] = xLocal
            _y[i] = yLocal
        }
        PolygonFiller polygonFiller = new PolygonFiller()
        polygonFiller.setPolygon(_x, _y, coordinates.size())
        polygonFiller.fill(ip, new Rectangle(window.getWidth(), window.getHeight()))

        //ip.setPixels(pixels)
        ip.getBufferedImage()
    }
}
