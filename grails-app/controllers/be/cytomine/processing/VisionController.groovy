package be.cytomine.processing

import ij.ImagePlus
import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import ij.process.ImageConverter
import be.cytomine.processing.image.filters.Multi_OtsuThreshold

import be.cytomine.processing.image.filters.Colour_Deconvolution
import be.cytomine.processing.image.filters.DynamicThreshold
import be.cytomine.processing.image.filters.Auto_Threshold
import ij.plugin.ContrastEnhancer
import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 1/06/11
 * Time: 13:44
 */
class VisionController {

    def process = {
        def split = request.queryString.split("url=http://")
        String imageURL = ConfigurationHolder.config.grails.serverURL + "/images/notavailable.jpg"
        if (split.size() > 0) {
             imageURL = "http://" + split[1]
        }

        /*println "URL " + params.url
        println "METHOD " + params.method
        println "contrast " + params.contrast
        println "brightness " + params.brightness*/

        try {
            /* Create Buffered Image  From URL */
            BufferedImage bufferedImage = getImageFromURL(imageURL)

            /* Process the BufferedImage */
            if (params.method == "eosin") {
                ImagePlus ip = new ImagePlus(imageURL,bufferedImage)
                Colour_Deconvolution dt = new Colour_Deconvolution()
                dt.setup(params.url, ip)
                dt.run(ip.getProcessor())
                bufferedImage = dt.getResult(1).getBufferedImage()
            }

            if (params.method == "haematoxylin") {
                ImagePlus ip = new ImagePlus(imageURL,bufferedImage)
                Colour_Deconvolution dt = new Colour_Deconvolution()
                dt.setup(params.url, ip)
                dt.run(ip.getProcessor())
                bufferedImage = dt.getResult(0).getBufferedImage()
            }

            if (params.method == "binary") {
                ImagePlus ip = new ImagePlus(imageURL,bufferedImage)
                ImageConverter ic = new ImageConverter(ip)
                ic.convertToGray8()
                ip.getProcessor().autoThreshold()
                bufferedImage = ip.getBufferedImage()
            }

            if (params.method == "gray") {
                ImagePlus ip = new ImagePlus(imageURL,bufferedImage)
                ImageConverter ic = new ImageConverter(ip)
                ic.convertToGray8()
                bufferedImage = ip.getBufferedImage()
            }

            if (params.method == "otsu") {
                ImagePlus ip = new ImagePlus(imageURL,bufferedImage)
                ImageConverter ic = new ImageConverter(ip)
                ic.convertToGray8()
                Multi_OtsuThreshold dt = new Multi_OtsuThreshold()
                dt.setup(imageURL, ip)
                dt.run(ip.getProcessor())
                bufferedImage = dt.getResult().getBufferedImage()
            }
            /* Apply filters */
            if (params.method=="huang" ) {
                bufferedImage = dynBinary(imageURL, bufferedImage, "Huang")
            }
            if( params.method=="intermodes" ) {
                bufferedImage = dynBinary(imageURL, bufferedImage, "Intermodes")
            }
            if( params.method=="isodata" ) {
                bufferedImage = dynBinary(imageURL, bufferedImage, "IsoData")
            }
            if( params.method=="li" ) {
                bufferedImage = dynBinary(imageURL, bufferedImage, "Li")
            }
            if( params.method=="maxentropy" ) {
                bufferedImage = dynBinary(imageURL, bufferedImage, "MaxEntropy")
            }
            if( params.method=="mean" ) {
                bufferedImage = dynBinary(imageURL, bufferedImage, "Mean")
            }
            if( params.method=="minerror" ) {
                bufferedImage = dynBinary(imageURL, bufferedImage, "MinError(I)")
            }
            if( params.method=="minimum" ) {
                bufferedImage = dynBinary(imageURL, bufferedImage, "Minimum")
            }
            if( params.method=="moments" ) {
                bufferedImage = dynBinary(imageURL, bufferedImage, "Moments")
            }
            if( params.method=="percentile" ) {
                bufferedImage = dynBinary(imageURL, bufferedImage, "percentile")
            }
            if( params.method=="renyientropy" ) {
                bufferedImage = dynBinary(imageURL, bufferedImage, "RenyiEntropy")
            }
            if( params.method=="shanbhag" ) {
                bufferedImage = dynBinary(imageURL, bufferedImage, "Shanbhag")
            }
            if( params.method=="triangle" ) {
                bufferedImage = dynBinary(imageURL, bufferedImage, "Triangle")
            }
            if( params.method=="yen") {
                bufferedImage = dynBinary(imageURL, bufferedImage, "Yen")
            }


            /* Apply filters */
            if (Boolean.parseBoolean(params.enhance)) {
                ImagePlus ip = new ImagePlus(imageURL,bufferedImage)
                new ContrastEnhancer().stretchHistogram(ip, 0.5)
                bufferedImage = ip.getBufferedImage()
            }

            if (Boolean.parseBoolean(params.invert)) {
                ImagePlus ip = new ImagePlus(imageURL,bufferedImage)
                ip.getProcessor().invert()
                bufferedImage = ip.getBufferedImage()
            }

            if (params.brightness != null && params.contrast != null) {
                double brightness = Double.parseDouble(params.brightness)
                double contrast = Double.parseDouble(params.contrast)
                ImagePlus ip = new ImagePlus(imageURL,bufferedImage)
                double defaultMin = ip.getDisplayRangeMin()
                double defaultMax = ip.getDisplayRangeMax()
                double max = ip.getDisplayRangeMax()
                double min = ip.getDisplayRangeMin()
                double range = defaultMax-defaultMin
                int fullRange = 256

                //BRIGHTNESS
                def center = defaultMin + (defaultMax-defaultMin)*((range-brightness)/range);
                double width = max-min;

                min = center - width/2.0;
                max = center + width/2.0;

                //CONTRAST
                center = min + (max-min)/2.0;
                double mid  = fullRange / 2
                double slope
                if (contrast <= mid) {
                    slope = brightness/mid
                } else {
                    slope = mid / (fullRange-contrast)
                }
                if (slope > 0.0) {
                    min = center-(0.5*range)/slope
                    max = center+(0.5*range)/slope;
                }

                //println("MIN/MAX : " + Math.round(min) + "/" + Math.round(max))
                ip.getProcessor().setMinAndMax(Math.round(min),Math.round(max))
                bufferedImage = ip.getBufferedImage()
            }

            /* Write response from BufferedImage */
            responseImage(bufferedImage)

        } catch (Exception e) {
            BufferedImage bufferedImage = getImageFromURL(ConfigurationHolder.config.grails.serverURL + "/images/notavailable.jpg")
            responseImage(bufferedImage)
        }
    }

    BufferedImage getImageFromURL(String url)  {
        def out = new ByteArrayOutputStream()
        out << new URL(url).openStream()
        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
        return ImageIO.read(inputStream);
    }

    def responseImage(BufferedImage bufferedImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", baos);
        byte[] bytesOut = baos.toByteArray();
        response.contentLength = baos.size();
        response.setHeader("Connection","Keep-Alive")
        response.setHeader("Accept-Ranges","bytes")
        response.setHeader("Content-Type", "image/jpeg")
        withFormat {
            jpg {
                if (request.method == 'HEAD') {
                    render(text: "", contentType: "image/jpeg");
                }
                else {
                    response.contentType = "image/jpeg";
                    response.getOutputStream() << bytesOut
                    response.getOutputStream().flush()
                }
            }
        }
    }

    def dynBinary (String url, BufferedImage bufferedImage, String method) {
        ImagePlus ip = new ImagePlus(url,bufferedImage)
        ImageConverter ic = new ImageConverter(ip)
        ic.convertToGray8()
        def at = new Auto_Threshold()
        Object[] result = at.exec(ip, method, false, false, true, false, false, false)
        ImagePlus ipThresholded = (ImagePlus) result[1]
        return ipThresholded.getBufferedImage()
    }
}
