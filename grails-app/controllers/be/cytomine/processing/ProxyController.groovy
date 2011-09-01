package be.cytomine.processing

import ij.ImagePlus
import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import ij.process.ImageConverter
import be.cytomine.processing.image.filters.Multi_OtsuThreshold

import be.cytomine.processing.image.filters.Colour_Deconvolution
import be.cytomine.processing.image.filters.DynamicThreshold
import be.cytomine.processing.image.filters.Auto_Threshold

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 1/06/11
 * Time: 13:44
 */
class ProxyController {

    def haematoxylin = {
        def split = request.queryString.split("http://")
        String url = "http://" + split[1]
        def out = new ByteArrayOutputStream()
        out << new URL(url).openStream()

        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
        BufferedImage bufferedImage;

        bufferedImage = ImageIO.read(inputStream);
        ImagePlus ip = new ImagePlus(url,bufferedImage)
        ImageConverter ic = new ImageConverter(ip)
        Colour_Deconvolution dt = new Colour_Deconvolution()
        dt.setup(url, ip)
        dt.run(ip.getProcessor())
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(dt.getResult(0).getBufferedImage(), "jpg", baos);
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
                    out.flush()
                    response.getOutputStream().flush()
                }
            }
        }
    }

    def eosin = {
        def split = request.queryString.split("http://")
        String url = "http://" + split[1]
        def out = new ByteArrayOutputStream()
        out << new URL(url).openStream()

        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
        BufferedImage bufferedImage;

        bufferedImage = ImageIO.read(inputStream);
        ImagePlus ip = new ImagePlus(url,bufferedImage)
        ImageConverter ic = new ImageConverter(ip)
        Colour_Deconvolution dt = new Colour_Deconvolution()
        dt.setup(url, ip)
        dt.run(ip.getProcessor())
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(dt.getResult(1).getBufferedImage(), "jpg", baos);
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
                    out.flush()
                    response.getOutputStream().flush()
                }
            }
        }
    }

    def binary = {
        def split = request.queryString.split("http://")
        def url = "http://" + split[1]
        def out = new ByteArrayOutputStream()
        out << new URL(url).openStream()
        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        ImagePlus ip = new ImagePlus(url,bufferedImage)
        ImageConverter ic = new ImageConverter(ip)
        ic.convertToGray8()
        ip.getProcessor().autoThreshold()

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

    def gray = {
        def split = request.queryString.split("http://")
        def url = "http://" + split[1]
        def out = new ByteArrayOutputStream()
        out << new URL(url).openStream()
        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        ImagePlus ip = new ImagePlus(url,bufferedImage)
        ImageConverter ic = new ImageConverter(ip)
        ic.convertToGray8()
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

    def otsu = {

        def split = request.queryString.split("http://")
        String url = "http://" + split[1]
        def out = new ByteArrayOutputStream()
        out << new URL(url).openStream()

        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
        BufferedImage bufferedImage;

        bufferedImage = ImageIO.read(inputStream);
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
                    out.flush()
                    response.getOutputStream().flush()
                }
            }
        }
    }


    private def parseImageUrl(queryString) {
        def split = queryString.split("http://")
        return "http://" + split[1]
    }

    def huang = {
        return dynbinary(parseImageUrl(request.queryString), "Huang")
    }

    def intermodes = {
        return dynbinary(parseImageUrl(request.queryString), "Intermodes")
    }

    def isodata = {
        return dynbinary(parseImageUrl(request.queryString), "IsoData")
    }

    def li = {
        return dynbinary(parseImageUrl(request.queryString), "Li")
    }

    def maxentropy = {
        return dynbinary(parseImageUrl(request.queryString), "MaxEntropy")
    }

    def mean = {
        return dynbinary(parseImageUrl(request.queryString), "Mean")
    }

    def minerror = {
        return dynbinary(parseImageUrl(request.queryString), "MinError(I)")
    }

    def minimum = {
        return dynbinary(parseImageUrl(request.queryString), "Minimum")
    }

    def moments = {
        return dynbinary(parseImageUrl(request.queryString), "Moments")
    }

    def percentile = {
        return dynbinary(parseImageUrl(request.queryString), "percentile")
    }

    def renyientropy = {
        return dynbinary(parseImageUrl(request.queryString), "RenyiEntropy")
    }

    def shanbhag = {
        return dynbinary(parseImageUrl(request.queryString), "Shanbhag")
    }

    def triangle = {
        return dynbinary(parseImageUrl(request.queryString), "Triangle")
    }

    def yen = {
        return dynbinary(parseImageUrl(request.queryString), "Yen")
    }

    def dynbinary (url, method) {
        def out = new ByteArrayOutputStream()
        out << new URL(url).openStream()
        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        ImagePlus ip = new ImagePlus(url,bufferedImage)
        ImageConverter ic = new ImageConverter(ip)
        ic.convertToGray8()
        //ip.getProcessor().autoThreshold()
        def at = new Auto_Threshold()
        Object[] result = at.exec(ip, method, false, false, true, false, false, false)
        ImagePlus ip_thresholded = (ImagePlus) result[1]

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(ip_thresholded.getBufferedImage(), "jpg", baos);
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
