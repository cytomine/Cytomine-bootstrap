package cytomine.web

import be.cytomine.image.AbstractImage
import be.cytomine.image.server.ImageProperty
import be.cytomine.image.server.ImageServer
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.server.resolvers.Resolver

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 19/07/11
 * Time: 15:19
 */
class ImagePropertiesService {

    def clear(AbstractImage image) {
        image.imageProperties.each {
            it.delete()
        }
    }

    def populate(AbstractImage image) {
        def imageServer = ImageServer.findByName("IIP-Openslide2");
        Resolver resolver = Resolver.getResolver(imageServer.className)
        def propertiesURL = resolver.getPropertiesURL(imageServer.getBaseUrl(), imageServer.getStorage().getBasePath() + image.getPath())
        println image.getFilename() + " : " + propertiesURL
        URL url = new URL(propertiesURL)
        URLConnection conn = url.openConnection();
        conn.setReadTimeout(1500)
        try {
            conn.getInputStream().eachLine { line ->
                if (line.isEmpty()) return;

                def args = line.split(":")
                if (args.length != 2) return
                //if (args[0].contains("Error/")) return
                def property = new ImageProperty(key: args[0], value: args[1], image: image)
                property.save();
                image.addToImageProperties(property)
            }
            image.save()
        } catch (java.net.SocketTimeoutException e) {
            println "Timeout reached to image : " + image.getFilename()
        } catch (IOException e) {
            println "IO Exception:" + e.toString()
        }

    }

    def extractUseful(AbstractImage image) {
        switch (image.getMime().extension) {
            case "mrxs":
                extractUsefulMrxs(image)
                break
            case "vms":
                extractUsefulVms(image)
                break
            case "tif":
                extractUsefulTif(image)
                break;
            case "tiff":
                extractUsefulTif(image)
                break;
            case "svs":
                extractUsefulTif(image)
                break;
        }
    }

    private def extractUsefulTif(AbstractImage image) {
        println "extract properties from tiff : " + image.getFilename()
        def storages = StorageAbstractImage.findAllByAbstractImage(image).collect { it.storage }
        if (storages.size() == 0) return
        def imageServer = ImageServer.findByStorage(storages.first())
        Resolver resolver = Resolver.getResolver(imageServer.className)
        def metadaURL = resolver.getMetaDataURL(imageServer.getBaseUrl(), imageServer.getStorage().getBasePath() + image.getPath())
        def url = new URL(metadaURL)
        print metadaURL
        //def dimensions = null

        URLConnection conn = url.openConnection();
        conn.setReadTimeout(1500)
        try {
            conn.getInputStream().eachLine { line ->
                def args = line.split(":")
                if (args[0].equals("Max-size")) {
                    def sizes = args[1].split(" ")
                    //dimensions = [width : Integer.parseInt(sizes[0]), height : Integer.parseInt(sizes[1])]
                    image.setWidth(Integer.parseInt(sizes[0]))
                    image.setHeight(Integer.parseInt(sizes[1]))
                }
            }
        } catch (java.net.SocketTimeoutException e) {
            println "Timeout reached to image : " + image.getFilename()
        } catch (IOException e) {
            println "IO Exception:" + e.toString()
        }
        image.setMagnification(10)
        image.setResolution(0.65)

    }

    private def extractUsefulMrxs(AbstractImage image) {
        println "extract properties from mrxs : " + image.getFilename()
        //Magnificiation
        def magnificationProperty = ImageProperty.findByImageAndKey(image, "mirax.GENERAL.OBJECTIVE_MAGNIFICATION")
        if (magnificationProperty) image.setMagnification(Integer.parseInt(magnificationProperty.getValue()))
        else println "magnificationProperty is null"
        //Width
        def widthProperty = ImageProperty.findByImageAndKey(image, "openslide.layer[0].width")
        if (widthProperty) image.setWidth(Integer.parseInt(widthProperty.getValue()))
        else println "widthProperty is null"
        //Height
        def heightProperty = ImageProperty.findByImageAndKey(image, "openslide.layer[0].height")
        if (heightProperty) image.setHeight(Integer.parseInt(heightProperty.getValue()))
        else println "heightProperty is null"
        //Resolution
        def resolutionProperty = ImageProperty.findByImageAndKey(image, "mirax.LAYER_0_LEVEL_0_SECTION.MICROMETER_PER_PIXEL_X")
        if (resolutionProperty) image.setResolution(Float.parseFloat(resolutionProperty.getValue()))
        else println "resolutionProperty is null"
    }

    private def extractUsefulVms(AbstractImage image) {
        println "extract properties from vms : " + image.getFilename()
        //Magnification : hamamatsu.SourceLens
        def magnificationProperty = ImageProperty.findByImageAndKey(image, "hamamatsu.SourceLens")
        if (magnificationProperty) {
            def value = Float.parseFloat(magnificationProperty.getValue().replace(",", "."))
            image.setMagnification(value.toInteger())
        }
        //Width openslide.layer[0].width
        def widthProperty = ImageProperty.findByImageAndKey(image, "openslide.layer[0].width")
        if (widthProperty) image.setWidth(Integer.parseInt(widthProperty.getValue()))
        //Height openslide.layer[0].height
        def heightProperty = ImageProperty.findByImageAndKey(image, "openslide.layer[0].height")
        if (heightProperty) image.setHeight(Integer.parseInt(heightProperty.getValue()))
        //Resolution : hamamatsu.PhysicalWidth / openslide.layer[0].width / 1000
        def physicalWidthProperty = ImageProperty.findByImageAndKey(image, "hamamatsu.PhysicalWidth")
        if (physicalWidthProperty && widthProperty) {
            def resolutionProperty = Float.parseFloat(physicalWidthProperty.getValue()) / Float.parseFloat(widthProperty.getValue()) / 1000
            image.setResolution(resolutionProperty)
        }
    }

}
