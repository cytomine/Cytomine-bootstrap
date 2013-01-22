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

/**
 * TODOSTEVBEN: clean unused method + doc
 */
class ProcessingController extends RestController {

    def imageProcessingService
    def sessionFactory

    private static def ROI_SIZE = 750 //MAX ALLOWED SIZE IS MAX CVT IN IIP CONFIG FILE !

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


}
