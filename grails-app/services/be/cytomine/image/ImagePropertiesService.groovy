package be.cytomine.image

import be.cytomine.image.server.ImageProperty
import be.cytomine.image.server.ImageServerStorage
import be.cytomine.image.server.Storage
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.server.resolvers.Resolver
import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 19/07/11
 * Time: 15:19
 * TODOSTEVBEN: doc + clean + refactor
 */
class ImagePropertiesService implements Serializable{

    def clear(AbstractImage image) {
        ImageProperty.findAllByImage(image)?.each {
            it.delete()
        }
    }

    def populate(AbstractImage image) {
        Collection<ImageServerStorage> imageServerStorages = image.getImageServersStorage()
        if (imageServerStorages == null || imageServerStorages.size() == 0) return
        def index = (Integer) Math.round(Math.random() * (imageServerStorages.size() - 1)) //select an url randomly
        def imageServerStorage = imageServerStorages.get(index)
        Resolver resolver = Resolver.getResolver(imageServerStorage.imageServer.className)
        Storage storage = StorageAbstractImage.findAllByAbstractImage(image).first().storage
        def propertiesURL = resolver.getPropertiesURL(imageServerStorage.imageServer.getBaseUrl(), [storage.getBasePath(),image.getPath()].join(File.separator))
        log.info image.getFilename() + " : " + propertiesURL
        DefaultHttpClient httpClient = new DefaultHttpClient()
        URI _url = new URI(propertiesURL)
        HttpGet httpGet = new HttpGet(_url)
        def response = httpClient.execute(httpGet)
        HttpEntity entityResponse = response.getEntity()
        try {
            entityResponse.getContent().eachLine { line ->
                if (line.isEmpty()) return;

                def args = line.split(":")
                if (args.length != 2) return
                //if (args[0].contains("Error/")) return
                def property = new ImageProperty(key: args[0], value: args[1], image: image)
                log.info("new property, $args[0] => $args[1]")
                property.save();
            }
            image.save(flush:true, failOnError: true)
        } catch (java.net.SocketTimeoutException e) {
            log.info "Timeout reached to image : " + image.getFilename()
        } catch (IOException e) {
            log.info "IO Exception:" + e.toString()
        }

    }

    def extractUseful(AbstractImage image) {
        log.info "image=$image"
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
                extractUsefulSVS(image)
                break;
            case "jp2":
                extractUsefulTif(image)
                break;
            case "scn":
                extractUsefulSCN(image)
                break;
            case "ndpi":
                extractUsefulTif(image)
                break;
            case "bif":
                extractUsefulBif(image)
                break;
            default:
                extractUsefulNDPI(image)

        }
        image.save(flush:true, failOnError: true)
    }

    private def extractUsefulSCN(AbstractImage image) {
        log.info "extract properties from mrxs : " + image.getFilename()
        //Magnificiation
        def magnificationProperty = ImageProperty.findByImageAndKey(image, "leica.objective")
        if (magnificationProperty) image.setMagnification(Integer.parseInt(magnificationProperty.getValue()))
        else log.info "magnificationProperty is null"
        //Width
        def widthProperty = ImageProperty.findByImageAndKey(image, "openslide.level[0].width")
        if (widthProperty) image.setWidth(Integer.parseInt(widthProperty.getValue()))
        else log.info "widthProperty is null"
        //Height
        def heightProperty = ImageProperty.findByImageAndKey(image, "openslide.level[0].height")
        if (heightProperty) image.setHeight(Integer.parseInt(heightProperty.getValue()))
        else log.info "heightProperty is null"
        //Resolution
        def resolutionProperty = ImageProperty.findByImageAndKey(image, "openslide.mpp-x")
        if (resolutionProperty) image.setResolution(Float.parseFloat(resolutionProperty.getValue()))
        else log.info "resolutionProperty is null"
    }

    private def extractUsefulTif(AbstractImage image) {
        Collection<ImageServerStorage> imageServerStorages = image.getImageServersStorage()
        if (imageServerStorages == null || imageServerStorages.size() == 0) return
        def index = (Integer) Math.round(Math.random() * (imageServerStorages.size() - 1)) //select an url randomly
        def imageServerStorage = imageServerStorages.get(index)
        log.info "imageServer="+imageServerStorage
         log.info "imageServerclassName="+imageServerStorage.imageServer.className
        Resolver resolver = Resolver.getResolver(imageServerStorage.imageServer.className)
        log.info "resolver="+resolver
        log.info "image="+image
        Storage storage = StorageAbstractImage.findAllByAbstractImage(image).first().storage
        log.info "storage="+storage
        def metadaURL = resolver.getMetaDataURL(imageServerStorage.imageServer.getBaseUrl(), [storage.getBasePath(),image.getPath()].join(File.separator))
        log.info "metadataURL="+metadaURL
        DefaultHttpClient httpClient = new DefaultHttpClient()
        URI _url = new URI(metadaURL)
        HttpGet httpGet = new HttpGet(_url)
        def response = httpClient.execute(httpGet)
        HttpEntity entityResponse = response.getEntity()
        print metadaURL
        //def dimensions = null


        /*URLConnection conn = url.openConnection();
        conn.setReadTimeout(5000)
        conn.setConnectTimeout(5000)*/
        try {
            entityResponse.getContent().eachLine { line ->
                def args = line.split(":")
                if (args[0].equals("Max-size")) {
                    def sizes = args[1].split(" ")
                    //dimensions = [width : Integer.parseInt(sizes[0]), height : Integer.parseInt(sizes[1])]
                    image.setWidth(Integer.parseInt(sizes[0]))
                    image.setHeight(Integer.parseInt(sizes[1]))
                }
            }
        } catch (java.net.SocketTimeoutException e) {
            log.info "Timeout reached to image : " + image.getFilename()
        } catch (IOException e) {
            log.info "IO Exception:" + e.toString()
        }
        image.setMagnification(40)
        image.setResolution(0.65)

    }

    private def extractUsefulMrxs(AbstractImage image) {
        log.info "extract properties from mrxs : " + image.getFilename()
        //Magnificiation
        def magnificationProperty = ImageProperty.findByImageAndKey(image, "mirax.GENERAL.OBJECTIVE_MAGNIFICATION")
        if (magnificationProperty) image.setMagnification(Integer.parseInt(magnificationProperty.getValue()))
        else log.info "magnificationProperty is null"
        //Width
        def widthProperty = ImageProperty.findByImageAndKey(image, "openslide.level[0].width")
        if (widthProperty) image.setWidth(Integer.parseInt(widthProperty.getValue()))
        else log.info "widthProperty is null"
        //Height
        def heightProperty = ImageProperty.findByImageAndKey(image, "openslide.level[0].height")
        if (heightProperty) image.setHeight(Integer.parseInt(heightProperty.getValue()))
        else log.info "heightProperty is null"
        //Resolution
        def resolutionProperty = ImageProperty.findByImageAndKey(image, "mirax.LAYER_0_LEVEL_0_SECTION.MICROMETER_PER_PIXEL_X")
        if (resolutionProperty) image.setResolution(Float.parseFloat(resolutionProperty.getValue()))
        else log.info "resolutionProperty is null"
    }

    private def extractUsefulVms(AbstractImage image) {
        log.info "extract properties from vms : " + image.getFilename()
        //Magnification : hamamatsu.SourceLens
        def magnificationProperty = ImageProperty.findByImageAndKey(image, "hamamatsu.SourceLens")
        if (magnificationProperty) {
            def value = Float.parseFloat(magnificationProperty.getValue().replace(",", "."))
            image.setMagnification(value.toInteger())
        }
        //Width openslide.level[0].width
        def widthProperty = ImageProperty.findByImageAndKey(image, "openslide.level[0].width")
        if (widthProperty) image.setWidth(Integer.parseInt(widthProperty.getValue()))
        //Height openslide.level[0].height
        def heightProperty = ImageProperty.findByImageAndKey(image, "openslide.level[0].height")
        if (heightProperty) image.setHeight(Integer.parseInt(heightProperty.getValue()))
        //Resolution : hamamatsu.PhysicalWidth / openslide.level[0].width / 1000
        def physicalWidthProperty = ImageProperty.findByImageAndKey(image, "hamamatsu.PhysicalWidth")
        if (physicalWidthProperty && widthProperty) {
            def resolutionProperty = Float.parseFloat(physicalWidthProperty.getValue()) / Float.parseFloat(widthProperty.getValue()) / 1000
            image.setResolution(resolutionProperty)
        }
    }

    private def extractUsefulSVS(AbstractImage image) {
        log.info "extract properties from svs : " + image.getFilename()
        def magnificationProperty = ImageProperty.findByImageAndKey(image, "aperio.AppMag")
        log.info "magnificationProperty="+magnificationProperty
        if (magnificationProperty) {
            def value = Float.parseFloat(magnificationProperty.getValue().replace(",", "."))
            image.setMagnification(value.toInteger())
        }
        //Width openslide.level[0].width
        def widthProperty = ImageProperty.findByImageAndKey(image, "openslide.level[0].width")
        if (widthProperty) image.setWidth(Integer.parseInt(widthProperty.getValue()))
        log.info "widthProperty="+widthProperty
        //Height openslide.level[0].height
        def heightProperty = ImageProperty.findByImageAndKey(image, "openslide.level[0].height")
        if (heightProperty) image.setHeight(Integer.parseInt(heightProperty.getValue()))
        log.info "heightProperty="+heightProperty
        //openslide.mpp-x
        def resolutionProperty = ImageProperty.findByImageAndKey(image, "aperio.MPP")
        if (resolutionProperty) image.setResolution(Float.parseFloat(resolutionProperty.getValue()))
        log.info "resolutionProperty="+resolutionProperty

    }

    private def extractUsefulBif(AbstractImage image) {
        log.info "extract properties from bif : " + image.getFilename()
        def magnificationProperty = ImageProperty.findByImageAndKey(image, "ventana.Magnification")
        log.info "magnificationProperty="+magnificationProperty
        if (magnificationProperty) {
            def value = Float.parseFloat(magnificationProperty.getValue().replace(",", "."))
            image.setMagnification(value.toInteger())
        }
        //Width openslide.level[0].width
        def widthProperty = ImageProperty.findByImageAndKey(image, "openslide.level[0].width")
        if (widthProperty) image.setWidth(Integer.parseInt(widthProperty.getValue()))
        log.info "widthProperty="+widthProperty
        //Height openslide.level[0].height
        def heightProperty = ImageProperty.findByImageAndKey(image, "openslide.level[0].height")
        if (heightProperty) image.setHeight(Integer.parseInt(heightProperty.getValue()))
        log.info "heightProperty="+heightProperty
        //ventana.Magnification
        def resolutionProperty = ImageProperty.findByImageAndKey(image, "ventana.ScanRes")
        if (resolutionProperty) image.setResolution(Float.parseFloat(resolutionProperty.getValue()))
        log.info "resolutionProperty="+resolutionProperty

    }

    private def extractUsefulNDPI(AbstractImage image) {
        log.info "extract properties from svs : " + image.getFilename()
        def magnificationProperty = ImageProperty.findByImageAndKey(image, "hamamatsu.SourceLens")
        log.info "magnificationProperty="+magnificationProperty
        if (magnificationProperty) {
            def value = Float.parseFloat(magnificationProperty.getValue().replace(",", "."))
            image.setMagnification(value.toInteger())
        }
        //Width openslide.level[0].width
        def widthProperty = ImageProperty.findByImageAndKey(image, "openslide.level[0].width")
        if (widthProperty) image.setWidth(Integer.parseInt(widthProperty.getValue()))
        log.info "widthProperty="+widthProperty
        //Height openslide.level[0].height
        def heightProperty = ImageProperty.findByImageAndKey(image, "openslide.level[0].height")
        if (heightProperty) image.setHeight(Integer.parseInt(heightProperty.getValue()))
        log.info "heightProperty="+heightProperty
        //openslide.mpp-x
        def resolutionProperty = ImageProperty.findByImageAndKey(image, "openslide.mpp-x")
        if (resolutionProperty) image.setResolution(Float.parseFloat(resolutionProperty.getValue()))
        log.info "resolutionProperty="+resolutionProperty

    }

}
