package be.cytomine.processing

import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.processing.image.filters.Auto_Threshold
import com.vividsolutions.jts.geom.Coordinate
import grails.converters.JSON
import ij.ImagePlus
import ij.plugin.filter.PlugInFilter
import ij.process.ImageConverter

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class ProcessingController extends RestController {

    def imageProcessingService
    def sessionFactory

    private static def ROI_SIZE = 750 //MAX ALLOWED SIZE IS MAX CVT IN IIP CONFIG FILE !

    def detect = {
        String method = params.method  != null ? params.method : "Otsu"

        //int scale = params.scale != null ? Integer.parseInt(params.scale) : 1
        int scale = 1
        int roiSize = ROI_SIZE * scale
        int middleROI = roiSize / 2
        def idImage = Integer.parseInt(params.image)
        AbstractImage image = ImageInstance.read(idImage).getBaseImage()
        int shiftY = image.getHeight() - Math.round(Double.parseDouble(params.y)) + middleROI
        int shiftX = Math.round(Double.parseDouble(params.x)) - middleROI
        def url = image.getCropURLWithMaxWithOrHeight((int) shiftX, (int) shiftY, roiSize, roiSize, (int) image.getWidth() / scale)
        log.info url
        //Image Processing
        ImagePlus ori = getThresholdedImage(url, method)
        /* Work in progress for handle scaling */
        double widthRatio = roiSize / ori.getWidth()
        double heightRatio = roiSize / ori.getHeight()
        int startPixelX = middleROI / widthRatio
        int startPixelY = middleROI / heightRatio
        /*log.info "ImageRatio "
        log.info "widthRatio = " + widthRatio
        log.info "heightRatio = " + heightRatio
        log.info "startPixelX = " + startPixelX
        log.info "startPixelY = " + startPixelY
        if (shiftX < 0) {
            log.info " shiftX < 0 : " + shiftX
            log.info " + " + (Math.abs(shiftX) / widthRatio)
            startPixelX = startPixelX + (Math.abs(shiftX) / widthRatio)
        }
        if (shiftY < 0) {
            log.info " shiftY < 0 : " + shiftX
            log.info " + " + (Math.abs(shiftY) / heightRatio)
            startPixelY = startPixelY - (Math.abs(shiftY) / heightRatio)
        }
        log.info "startPixelX = " + startPixelX
        log.info "startPixelY = " + startPixelY
        */

        int erodeDilateNumber = 3
        for (int i = 0; i < erodeDilateNumber; i++)
            ori.getProcessor().dilate()

        for (int i = 0; i < erodeDilateNumber; i++)
            ori.getProcessor().erode()

        PlugInFilter filler = new ij.plugin.filter.Binary()
        filler.setup("fill", ori)
        filler.run(ori.getProcessor())

        log.info "startPixelX $startPixelX"
        log.info "startPixelY $startPixelY"
        Coordinate[] coordinates = imageProcessingService.doWand(ori, startPixelX, startPixelY, 30, null)
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
        String method = params.method  != null ? params.method : "MaxEntropy"
        int middleROI = ProcessingController.ROI_SIZE / 2
        def idImage = Integer.parseInt(params.image)
        AbstractImage image = ImageInstance.read(idImage).getBaseImage()
        def y = image.getHeight() - Math.round(Double.parseDouble(params.y)) + middleROI
        def x = Math.round(Double.parseDouble(params.x)) - middleROI
        def url = image.getCropURL((int) x, (int) y, (int) ProcessingController.ROI_SIZE, (int) ProcessingController.ROI_SIZE)
        log.info url
        ImagePlus ip = getThresholdedImage(url, method)
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

    private def getThresholdedImage(String url, String method) {
        def out = new ByteArrayOutputStream()
        out << new URL(url).openStream()
        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
        BufferedImage bufferedImage;
        bufferedImage = ImageIO.read(inputStream);
        ImagePlus ip = new ImagePlus(url, bufferedImage)
        ImageConverter ic = new ImageConverter(ip)
        ic.convertToGray8()
        def at = new Auto_Threshold()
        Object[] result = at.exec(ip, method, false, false, true, false, false, false)
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
