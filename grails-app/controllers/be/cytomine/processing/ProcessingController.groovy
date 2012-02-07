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
import ij.process.ImageProcessor
import ij.IJ

class ProcessingController extends RestController {

    def imageProcessingService
    def sessionFactory

    private static def ROI_SIZE = 750 //MAX ALLOWED SIZE IS MAX CVT IN IIP CONFIG FILE !

    def detect = {
        int middleROI = ProcessingController.ROI_SIZE / 2
        def idImage = Integer.parseInt(params.image)
        AbstractImage image = ImageInstance.read(idImage).getBaseImage()
        int shiftY = image.getHeight() - Math.round(Double.parseDouble(params.y)) + middleROI
        int shiftX = Math.round(Double.parseDouble(params.x)) - middleROI
        def url = image.getCropURL((int) shiftX, (int) shiftY, ProcessingController.ROI_SIZE, ProcessingController.ROI_SIZE)
        ImagePlus ori = getImage(url)
        Coordinate[] coordinates = imageProcessingService.doWand(ori, middleROI, middleROI, 4, null)
        coordinates.each { coordinate ->
            coordinate.x = shiftX + coordinate.x
            coordinate.y = shiftY - coordinate.y
        }
        String polygon = imageProcessingService.getWKTPolygon(coordinates)
        def result = [:]
        result.geometry = polygon
        render result as JSON
    }

    def show = {
        int middleROI = ProcessingController.ROI_SIZE / 2
        def idImage = Integer.parseInt(params.image)
        AbstractImage image = ImageInstance.read(idImage).getBaseImage()
        def y = image.getHeight() - Math.round(Double.parseDouble(params.y)) + middleROI
        def x = Math.round(Double.parseDouble(params.x)) - middleROI
        def url = image.getCropURL((int) x, (int) y, (int) ProcessingController.ROI_SIZE, (int) ProcessingController.ROI_SIZE)
        println url
        ImagePlus ip = getImage(url)
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



}
