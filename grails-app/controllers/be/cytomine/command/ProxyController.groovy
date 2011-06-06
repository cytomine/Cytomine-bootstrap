package be.cytomine.command

import ij.ImagePlus
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import ij.plugin.filter.Binary
import imagej.DynamicThreshold
import ij.process.ImageConverter
import imagej.Multi_OtsuThreshold

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 1/06/11
 * Time: 13:44
 */
class ProxyController {

    def binary = {
        def url = params.url
        def out = new ByteArrayOutputStream()
        out << new URL(url).openStream()
        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        ImagePlus ip = new ImagePlus(url,bufferedImage)
        ImageConverter ic = new ImageConverter(ip)
        ic.convertToGray8()
        DynamicThreshold dt = new DynamicThreshold()
        dt.setup(url, ip)
        dt.run(ip.getProcessor())
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(dt.getImMeanMaxMin().getBufferedImage(), "jpg", baos);
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

    def otsu = {
        def url = params.url
        def out = new ByteArrayOutputStream()
        out << new URL(url).openStream()
        println "streamed"
        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        ImagePlus ip = new ImagePlus(url,bufferedImage)
        ImageConverter ic = new ImageConverter(ip)
        ic.convertToGray8()
        Multi_OtsuThreshold dt = new Multi_OtsuThreshold()
        dt.setup(url, ip)
        dt.run(ip.getProcessor())
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(dt.getResult().getBufferedImage(), "jpg", baos);
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
}
