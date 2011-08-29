package be.cytomine.processing

import grails.converters.JSON
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.api.RestController
import ij.ImagePlus
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import ij.process.ImageConverter
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.algorithm.ConvexHull
import com.vividsolutions.jts.geom.GeometryFactory

class ProcessingController extends RestController{

    def sessionFactory

    private static def ROI_SIZE = 1000 //MAX CVT IN IIP CONFIG FILE !
    private static def BLACK = 0
    private static def WHITE = 255

    def detect = {
        int shift = ProcessingController.ROI_SIZE / 2
        def idImage = Integer.parseInt(params.image)
        AbstractImage image = ImageInstance.read(idImage).getBaseImage()
        def y = image.getHeight() - Math.round(Double.parseDouble(params.y)) + shift
        def x = Math.round(Double.parseDouble(params.x)) - shift
        def url = image.getCropURL((int)x,(int)y,(int)shift*2,(int)shift*2)
        println url
        ImagePlus ip = getImage(url)
        def coordinates = computeCoordinates(ip, shift, shift, x, y)
        def convexHull = new ConvexHull(coordinates, new GeometryFactory())
        def geometry = convexHull.getConvexHull()
        println "geometry" + geometry.geometryType
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
        def url = image.getCropURL((int)x,(int)y,(int)shift*2,(int)shift*2)
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
        ImagePlus ip = new ImagePlus(url,bufferedImage)
        ImageConverter ic = new ImageConverter(ip)
        ic.convertToGray8()
        ip.getProcessor().autoThreshold()
        ip
    }

    private def getROI(idImage, x, y) {
        def shift = ProcessingController.ROI_SIZE / 2
        def points = []
        points.add([x : (x - shift), y  : (y - shift)]) //topLeft
        points.add([x : (x + shift), y  : (y - shift)]) //topRight
        points.add([x : (x + shift), y  : (y + shift)]) //bottomRight
        points.add([x : (x - shift), y  : (y + shift)]) //bottomLeft
        points
    }

    private def isInROI(ImagePlus ip, x, y) {
        return (x >= 0 && x < ip.getWidth() && y >= 0 && y < ip.getHeight())
    }

    private def computeCoordinates(ImagePlus ip, int x, int y, topLeftX, topLeftY) {
        println "computeCoordinates " + x + " " + y + " " +  ip.getWidth() + " " + ip.getHeight()
        int[] firstPixel = ip.getPixel(x,y)
        if (firstPixel[0] == WHITE) { //pixel is white, nothing to do
            return []
        }
        Stack<Coordinate> toVisit = new Stack<Coordinate>()
        List<Coordinate> visited = new LinkedList<Coordinate>()
        toVisit.push(new Coordinate(x,y))
        ip.getProcessor().putPixel(x,y,255)
        assert(ip.getProcessor().getPixel( (int) x, (int) y) == WHITE)
        def cpt = 0
        while (!toVisit.empty()) {

            if (cpt > ROI_SIZE*ROI_SIZE) return [] //error..
            Coordinate point = toVisit.pop()
            visited.push(new Coordinate(topLeftX + point.x, topLeftY - point.y)) //compute the real coordinate, not relative to the crop
            //if (ip.getProcessor().getPixel( (int) point.x, (int) point.y) == WHITE) continue
            int posX
            int posY
            if (cpt % 10000 == 0 || cpt == 0) {
                println "cpt " + cpt
                println "toVisit " + toVisit.size()
                println "visited " + visited.size()
                println "posX " + point.x
                println "posY " + point.y
                println "#################é"
            }
            cpt++
            /* 1 */
            posX = (int) point.x - 1
            posY = (int) point.y - 1
            if (isInROI(ip, posX, posY) && ip.getProcessor().getPixel(posX,posY) != WHITE) {
                ip.getProcessor().putPixel( posX, posY, WHITE)
                toVisit.push(new Coordinate(posX, posY))
            }
            /* 2 */
            posX = (int) point.x
            posY = (int) point.y - 1
            if (isInROI(ip, posX, posY) && ip.getProcessor().getPixel(posX,posY) != WHITE) {
                ip.getProcessor().putPixel( posX, posY, WHITE)
                toVisit.push(new Coordinate(posX, posY))
            }
            /* 3 */
            posX = (int) point.x + 1
            posY = (int) point.y - 1
            if (isInROI(ip, posX, posY) && ip.getProcessor().getPixel(posX,posY) != WHITE) {
                ip.getProcessor().putPixel( posX, posY, WHITE)
                toVisit.push(new Coordinate(posX, posY))
            }
            /* 4 */
            posX = (int) point.x - 1
            posY = (int) point.y
            if (isInROI(ip, posX, posY) && ip.getProcessor().getPixel(posX,posY) != WHITE) {
                ip.getProcessor().putPixel( posX, posY, WHITE)
                toVisit.push(new Coordinate(posX, posY))
            }
            /* 5 //Nothing to do, equal to start firstPixel
            /* 6 */
            posX = (int) point.x + 1
            posY = (int) point.y
            if (isInROI(ip, posX, posY) && ip.getProcessor().getPixel(posX,posY) != WHITE) {
                ip.getProcessor().putPixel( posX, posY, WHITE)
                toVisit.push(new Coordinate(posX, posY))
            }
            /* 7 */
            posX = (int) point.x - 1
            posY = (int) point.y + 1
            if (isInROI(ip, posX, posY) && ip.getProcessor().getPixel(posX,posY) != WHITE) {
                ip.getProcessor().putPixel( posX, posY, WHITE)
                toVisit.push(new Coordinate(posX, posY))
            }
            /* 8 */
            posX = (int) point.x
            posY = (int) point.y + 1
            if (isInROI(ip, posX, posY) && ip.getProcessor().getPixel(posX,posY) != WHITE) {
                ip.getProcessor().putPixel( posX, posY, WHITE)
                toVisit.push(new Coordinate(posX, posY))
            }
            /* 9 */
            posX = (int) point.x + 1
            posY = (int) point.y + 1
            if (isInROI(ip, posX, posY) && ip.getProcessor().getPixel(posX,posY) != WHITE) {
                ip.getProcessor().putPixel( posX, posY, WHITE)
                toVisit.push(new Coordinate(posX, posY))
            }
        }
        /* Draw the detected region */
        /*for (int i = 0; i < ip.getWidth(); i++) {
                for (int j = 0; j < ip.getHeight(); j++)
                    ip.getProcessor().putPixel(i, j, 255)
        }
        visited.each { point ->
            ip.getProcessor().putPixel(point.x, point.y , 0)
        }*/
        /* Simple ROI
        def xmin = ip.getWidth();
        def ymin = ip.getHeight();
        def xmax = 0;
        def ymax = 0;
        visited.each { point ->
            xmin = Math.min(xmin, point.x)
            ymin = Math.min(ymin, point.y)
            xmax = Math.max(xmax, point.x)
            ymax = Math.max(ymax, point.y)
        }
        points.add([x : xmin, y  : ymin]) //topLeft
        points.add([x : xmax, y  : ymin]) //topRight
        points.add([x : xmax, y  : ymax]) //bottomRight
        points.add([x : xmin, y  : ymax]) //bottomLeft
        points.add([x : xmin, y  : ymin]) //topLeft
        */
        /* Contours */
        /*Integer[] minx = new Integer[ip.getHeight()]
        for (int i = 0; i < ip.getHeight(); i++ ) {
            minx[i] = null
        }
        Integer[] maxx = new Integer[ip.getHeight()]
        for (int i = 0; i < ip.getHeight(); i++ ) {
            maxx[i] = null
        }
        visited.each { point ->
            minx[point.y] = minx[point.y] == null ? point.x : Math.min(minx[point.y], point.x)
            maxx[point.y] = maxx[point.y] == null ? point.x : Math.max(maxx[point.y], point.x)
        }
        for (int i = 0; i < ip.getHeight(); i++ ) {
            if (!minx[i]) continue;
            points.add([x : minx[i], y : i])
        }
        for (int i = ip.getHeight() - 1; i > 0; i-- ) {
            if (!maxx[i]) continue;
            points.add([x : maxx[i], y : i])
        }*/
        /* Convex HULL */
        Coordinate[] coordinates = new Coordinate[visited.size()]
        visited.toArray(coordinates)
        coordinates
    }

}
