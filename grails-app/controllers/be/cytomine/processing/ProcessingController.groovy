package be.cytomine.processing

import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.processing.image.filters.Auto_Threshold
import com.vividsolutions.jts.algorithm.ConvexHull
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import grails.converters.JSON
import ij.ImagePlus
import ij.process.ImageConverter
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class ProcessingController extends RestController {

    def sessionFactory

    private static def ROI_SIZE = 250 //MAX CVT IN IIP CONFIG FILE !
    private static def BLACK = 0
    private static def WHITE = 255

    def detect = {
        int shift = ProcessingController.ROI_SIZE / 2
        def idImage = Integer.parseInt(params.image)
        AbstractImage image = ImageInstance.read(idImage).getBaseImage()
        def y = image.getHeight() - Math.round(Double.parseDouble(params.y)) + shift
        def x = Math.round(Double.parseDouble(params.x)) - shift
        def url = image.getCropURL((int) x, (int) y, (int) shift * 2, (int) shift * 2)
        println url
        ImagePlus ip = getImage(url)
        def coordinates = computeCoordinates(ip, shift, shift, x, y)
        Geometry geometry = new GeometryFactory().buildGeometry(new LinkedList<Coordinate>()) //EMPTY GEOMETRY
        if (coordinates != null) {
            ConvexHull convexHull = new ConvexHull(coordinates, new GeometryFactory())
            geometry = convexHull.getConvexHull()
            println "geometry" + geometry.geometryType
        }
        def result = [:]
        result.geometry = geometry.toString()
        render result as JSON
    }

    def show = {
        int shift = ProcessingController.ROI_SIZE / 2
        def idImage = Integer.parseInt(params.image)
        AbstractImage image = ImageInstance.read(idImage).getBaseImage()
        def y = image.getHeight() - Math.round(Double.parseDouble(params.y)) + shift
        def x = Math.round(Double.parseDouble(params.x)) - shift
        def url = image.getCropURL((int) x, (int) y, (int) shift * 2, (int) shift * 2)
        println url
        ImagePlus ip = getImage(url)
        //computeCoordinates(ip, shift,shift,x,y)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(ip.getBufferedImage(), "jpg", baos);
        byte[] bytesOut = baos.toByteArray();
        response.contentLength = baos.size();
        withFormat {
            jpg {
                if (request.method == 'HEAD') {
                    render(text: "", contentType: "image/jpeg");
                }
                else {
                    response.contentType = "image/jpeg"; response.getOutputStream() << bytesOut
                }
            }
        }
    }

    private def getImage(url) {
        def out = new ByteArrayOutputStream()
        out << new URL(url).openStream()
        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
        BufferedImage bufferedImage;
        bufferedImage = ImageIO.read(inputStream);
        ImagePlus ip = new ImagePlus(url, bufferedImage)
        ImageConverter ic = new ImageConverter(ip)
        ic.convertToGray8()
        //ip.getProcessor().autoThreshold()
        def at = new Auto_Threshold()
        Object[] result = at.exec(ip, "Triangle", false, false, true, false, false, false)
        ImagePlus ip_thresholded = (ImagePlus) result[1]
        ip_thresholded
    }


    private def getROI(idImage, x, y) {
        def shift = ProcessingController.ROI_SIZE / 2
        def points = []
        points.add([x: (x - shift), y: (y - shift)]) //topLeft
        points.add([x: (x + shift), y: (y - shift)]) //topRight
        points.add([x: (x + shift), y: (y + shift)]) //bottomRight
        points.add([x: (x - shift), y: (y + shift)]) //bottomLeft
        points
    }

    private def isInROI(ImagePlus ip, x, y) {
        return (x >= 0 && x < ip.getWidth() && y >= 0 && y < ip.getHeight())
    }

    private def computeCoordinates(ImagePlus ip, int x, int y, long topLeftX, long topLeftY) {
        int[] firstPixel = ip.getPixel(x, y)
        if (firstPixel[0] == WHITE) { //pixel is white, nothing to do
            return null
        }
        Stack<Coordinate> toVisit = new Stack<Coordinate>()
        List<Coordinate> visited = new LinkedList<Coordinate>()
        toVisit.push(new Coordinate(x, y))
        ip.getProcessor().putPixel(x, y, 255)
        assert (ip.getProcessor().getPixel((int) x, (int) y) == WHITE)
        while (!toVisit.empty()) {
            Coordinate point = toVisit.pop()
            visited.push(new Coordinate(topLeftX + point.x, topLeftY - point.y)) //compute the real coordinate, not relative to the crop

            int posX
            int posY

            int[] xShifts = [-1, 0, 1,
                    -1, 1,
                    -1, 0, 1]

            int[] yShifts = [-1, -1, -1,
                    0, 0,
                    1, 1, 1]

            assert (xShifts.size() == yShifts.size())
            for (int i = 0; i < xShifts.size(); i++) {
                posX = (int) point.x + xShifts[i]
                posY = (int) point.y + yShifts[i]
                if (isInROI(ip, posX, posY) && ip.getProcessor().getPixel(posX, posY) != WHITE) {
                    ip.getProcessor().putPixel(posX, posY, WHITE)
                    toVisit.push(new Coordinate(posX, posY))
                }
            }
        }
        /* Draw the detected region */
        /*for (int i = 0; i < ip.getWidth(); i++) {
                for (int j = 0; j < ip.getHeight(); j++)
                    ip.getProcessor().putPixel(i, j, 255)
        }
        visited.each { point ->
            ip.getProcessor().putPixel(point.x, point.y , 125)
        }*/
        //return to coordinates array
        Coordinate[] coordinates = new Coordinate[visited.size()]
        visited.toArray(coordinates)
        coordinates
    }

}
