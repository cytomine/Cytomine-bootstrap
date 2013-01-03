package be.cytomine.processing

import be.cytomine.api.RestController
import be.cytomine.processing.image.filters.Colour_Deconvolution
import ij.ImagePlus
import ij.plugin.ContrastEnhancer
import ij.process.ImageConverter

import java.awt.image.BufferedImage

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 1/06/11
 * Time: 13:44
 */
class VisionController extends RestController {

    def imageProcessingService

    def process = {
        def split = request.queryString.split("url=http://")
        String imageURL = grailsApplication.config.grails.serverURL + "/images/notavailable.jpg"
        if (split.size() > 0) {
            imageURL = "http://" + split[1]
        }

        /*log.info "URL " + params.url
        log.info "METHOD " + params.method
        log.info "contrast " + params.contrast
        log.info "brightness " + params.brightness*/

        try {
            /* Create Buffered Image  From URL */
            BufferedImage bufferedImage = getImageFromURL(imageURL)

            /* Process the BufferedImage */

            if (params.method == "r_rgb") {
                ImagePlus ip = new ImagePlus(imageURL, bufferedImage)
                Colour_Deconvolution dt = new Colour_Deconvolution()
                dt.setSelectedStain(14)
                dt.setup(params.url, ip)
                dt.run(ip.getProcessor())
                bufferedImage = dt.getResult(0).getBufferedImage()
            }

            else if (params.method == "g_rgb") {
                ImagePlus ip = new ImagePlus(imageURL, bufferedImage)
                Colour_Deconvolution dt = new Colour_Deconvolution()
                dt.setSelectedStain(14)
                dt.setup(params.url, ip)
                dt.run(ip.getProcessor())

                bufferedImage = dt.getResult(1).getBufferedImage()
            }

            else if (params.method == "b_rgb") {
                ImagePlus ip = new ImagePlus(imageURL, bufferedImage)
                Colour_Deconvolution dt = new Colour_Deconvolution()
                dt.setSelectedStain(14)
                dt.setup(params.url, ip)
                dt.run(ip.getProcessor())
                bufferedImage = dt.getResult(2).getBufferedImage()
            }

            else if (params.method == "c_cmy") {
                ImagePlus ip = new ImagePlus(imageURL, bufferedImage)
                Colour_Deconvolution dt = new Colour_Deconvolution()
                dt.setSelectedStain(15)
                dt.setup(params.url, ip)
                dt.run(ip.getProcessor())
                bufferedImage = dt.getResult(0).getBufferedImage()
            }
            else if (params.method == "m_cmy") {
                ImagePlus ip = new ImagePlus(imageURL, bufferedImage)
                Colour_Deconvolution dt = new Colour_Deconvolution()
                dt.setSelectedStain(15)
                dt.setup(params.url, ip)
                dt.run(ip.getProcessor())
                bufferedImage = dt.getResult(1).getBufferedImage()
            }

            else if (params.method == "y_cmy") {
                ImagePlus ip = new ImagePlus(imageURL, bufferedImage)
                Colour_Deconvolution dt = new Colour_Deconvolution()
                dt.setSelectedStain(15)
                dt.setup(params.url, ip)
                dt.run(ip.getProcessor())
                bufferedImage = dt.getResult(2).getBufferedImage()
            }

            else if (params.method == "he-eosin") {
                ImagePlus ip = new ImagePlus(imageURL, bufferedImage)
                Colour_Deconvolution dt = new Colour_Deconvolution()
                dt.setSelectedStain(1)
                dt.setup(params.url, ip)
                dt.run(ip.getProcessor())
                bufferedImage = dt.getResult(1).getBufferedImage()
            }

            else if (params.method == "he-haematoxylin") {
                ImagePlus ip = new ImagePlus(imageURL, bufferedImage)
                Colour_Deconvolution dt = new Colour_Deconvolution()
                dt.setSelectedStain(1)
                dt.setup(params.url, ip)
                dt.run(ip.getProcessor())
                bufferedImage = dt.getResult(0).getBufferedImage()
            }

            else if (params.method == "hdab-haematoxylin") {
                ImagePlus ip = new ImagePlus(imageURL, bufferedImage)
                Colour_Deconvolution dt = new Colour_Deconvolution()
                dt.setSelectedStain(3)
                dt.setup(params.url, ip)
                dt.run(ip.getProcessor())
                bufferedImage = dt.getResult(0).getBufferedImage()
            }

            if (params.method == "hdab-dab") {
                ImagePlus ip = new ImagePlus(imageURL, bufferedImage)
                Colour_Deconvolution dt = new Colour_Deconvolution()
                dt.setSelectedStain(3)
                dt.setup(params.url, ip)
                dt.run(ip.getProcessor())
                bufferedImage = dt.getResult(1).getBufferedImage()
            }

            else if (params.method == "binary") {
                ImagePlus ip = new ImagePlus(imageURL, bufferedImage)
                ImageConverter ic = new ImageConverter(ip)
                ic.convertToGray8()
                ip.getProcessor().autoThreshold()
                bufferedImage = ip.getBufferedImage()
            }

            else if (params.method == "gray") {
                ImagePlus ip = new ImagePlus(imageURL, bufferedImage)
                ImageConverter ic = new ImageConverter(ip)
                ic.convertToGray8()
                bufferedImage = ip.getBufferedImage()
            }

            else if (params.method == "otsu") {
                /*ImagePlus ip = new ImagePlus(imageURL, bufferedImage)
                ImageConverter ic = new ImageConverter(ip)
                ic.convertToGray8()
                Multi_OtsuThreshold dt = new Multi_OtsuThreshold()
                dt.setup(imageURL, ip)
                dt.run(ip.getProcessor())
                bufferedImage = dt.getResult().getBufferedImage()*/
                bufferedImage = imageProcessingService.dynBinary(imageURL, bufferedImage, "Otsu")
            }
            /* Apply filters */
            else if (params.method == "huang") {
                bufferedImage = imageProcessingService.dynBinary(imageURL, bufferedImage, "Huang")
            }
            else if (params.method == "intermodes") {
                bufferedImage = imageProcessingService.dynBinary(imageURL, bufferedImage, "Intermodes")
            }
            else if (params.method == "isodata") {
                bufferedImage = imageProcessingService.dynBinary(imageURL, bufferedImage, "IsoData")
            }
            else if (params.method == "li") {
                bufferedImage = imageProcessingService.dynBinary(imageURL, bufferedImage, "Li")
            }
            else if (params.method == "maxentropy") {
                bufferedImage = imageProcessingService.dynBinary(imageURL, bufferedImage, "MaxEntropy")
            }
            else if (params.method == "mean") {
                bufferedImage = imageProcessingService.dynBinary(imageURL, bufferedImage, "Mean")
            }
            else if (params.method == "minerror") {
                bufferedImage = imageProcessingService.dynBinary(imageURL, bufferedImage, "MinError(I)")
            }
            else if (params.method == "minimum") {
                bufferedImage = imageProcessingService.dynBinary(imageURL, bufferedImage, "Minimum")
            }
            else if (params.method == "moments") {
                bufferedImage = imageProcessingService.dynBinary(imageURL, bufferedImage, "Moments")
            }
            else if (params.method == "percentile") {
                bufferedImage = imageProcessingService.dynBinary(imageURL, bufferedImage, "percentile")
            }
            else if (params.method == "renyientropy") {
                bufferedImage = imageProcessingService.dynBinary(imageURL, bufferedImage, "RenyiEntropy")
            }
            else if (params.method == "shanbhag") {
                bufferedImage = imageProcessingService.dynBinary(imageURL, bufferedImage, "Shanbhag")
            }
            else if (params.method == "triangle") {
                bufferedImage = imageProcessingService.dynBinary(imageURL, bufferedImage, "Triangle")
            }
            else if (params.method == "yen") {
                bufferedImage = imageProcessingService.dynBinary(imageURL, bufferedImage, "Yen")
            }

            /* Apply filters */
            if (Boolean.parseBoolean(params.enhance)) {
                ImagePlus ip = new ImagePlus(imageURL, bufferedImage)
                new ContrastEnhancer().stretchHistogram(ip, 0.5)
                bufferedImage = ip.getBufferedImage()
            }

            if (Boolean.parseBoolean(params.invert)) {
                ImagePlus ip = new ImagePlus(imageURL, bufferedImage)
                ip.getProcessor().invert()
                bufferedImage = ip.getBufferedImage()
            }

            if (params.brightness != null && params.contrast != null) {
                double brightness = Double.parseDouble(params.brightness)
                double contrast = Double.parseDouble(params.contrast)
                ImagePlus ip = new ImagePlus(imageURL, bufferedImage)
                double defaultMin = ip.getDisplayRangeMin()
                double defaultMax = ip.getDisplayRangeMax()
                double max = ip.getDisplayRangeMax()
                double min = ip.getDisplayRangeMin()
                double range = defaultMax - defaultMin
                int fullRange = 256

                //BRIGHTNESS
                def center = defaultMin + (defaultMax - defaultMin) * ((range - brightness) / range);
                double width = max - min;

                min = center - width / 2.0;
                max = center + width / 2.0;

                //CONTRAST
                center = min + (max - min) / 2.0;
                double mid = fullRange / 2
                double slope
                if (contrast <= mid) {
                    slope = brightness / mid
                } else {
                    slope = mid / (fullRange - contrast)
                }
                if (slope > 0.0) {
                    min = center - (0.5 * range) / slope
                    max = center + (0.5 * range) / slope;
                }

                //log.info("MIN/MAX : " + Math.round(min) + "/" + Math.round(max))
                ip.getProcessor().setMinAndMax(Math.round(min), Math.round(max))
                bufferedImage = ip.getBufferedImage()
            }

            /* Write response from BufferedImage */
            responseBufferedImage(bufferedImage)

        } catch (Exception e) {
            BufferedImage bufferedImage = getImageFromURL(grailsApplication.config.grails.serverURL + "/images/notavailable.jpg")
            responseBufferedImage(bufferedImage)
        }
    }


}
