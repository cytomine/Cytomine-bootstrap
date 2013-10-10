package be.cytomine.api.image

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.image.multidim.ImageGroup
import be.cytomine.image.multidim.ImageSequence
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.processing.ProcessingServer
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import grails.converters.JSON
import sun.misc.BASE64Decoder

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage

/**
 * Controller for abstract image
 * An abstract image can be add in n projects
 */
class RestImageController extends RestController {

    def imagePropertiesService
    def abstractImageService
    def cytomineService
    def projectService
    def segmentationService

    /**
     * List all abstract image available on cytomine
     */
    def list = {
        SecUser user = cytomineService.getCurrentUser()
        if(params.rows!=null) {
            responseSuccess(abstractImageService.list(user, params.page, params.rows, params.sidx, params.sord, params.filename, params.createdstart, params.createdstop))
        } else {
            responseSuccess(abstractImageService.list(user))
        }
    }

    /**
     * List all abstract images for a project
     */
    def listByProject = {
        Project project = Project.read(params.id)
        if (project) {
            responseSuccess(abstractImageService.list(project))
        } else {
            responseNotFound("Image", "Project", params.id)
        }
    }

    /**
     * Get a single image
     */
    def show = {
        AbstractImage image = abstractImageService.read(params.long('id'))
        if (image) {
            responseSuccess(image)
        } else {
            responseNotFound("Image", params.id)
        }
    }

    /**
     * Add a new image
     * TODO:: how to manage security here?
     */
    def add = {
        add(abstractImageService, request.JSON)
    }

    /**
     * Update a new image
     * TODO:: how to manage security here?
     */
    def update = {
        update(abstractImageService, request.JSON)
    }

    /**
     * Delete a new image
     * TODO:: how to manage security here?
     */
    def delete = {
        delete(abstractImageService, JSON.parse("{id : $params.id}"),null)
    }

    /**
     * Get metadata URL for an images
     * If extract, populate data from metadata table into image object
     */
    def metadata = {
        def idImage = params.long('id')
        def extract = params.boolean('extract')
        if (extract) {
            AbstractImage image = abstractImageService.read(idImage)
            imagePropertiesService.extractUseful(image)
            image.save(flush : true)
        }
        def responseData = [:]
        responseData.metadata = abstractImageService.metadata(idImage)
        response(responseData)
    }

    /**
     * Extract image properties from file
     */
    def imageProperties = {
        responseSuccess(abstractImageService.imageProperties(params.long('id')))
    }

    /**
     * Get an image property
     */
    def imageProperty = {
        def imageProperty = abstractImageService.imageProperty(params.long('imageproperty'))
        if (imageProperty) {
            responseSuccess(imageProperty)
        } else {
            responseNotFound("ImageProperty", params.imageproperty)
        }
    }

    /**
     * Get image thumb URL
     */
    def thumb = {
        def url = abstractImageService.thumb(params.long('id'))
        responseImage(url)
    }

    /**
     * Get image preview URL
     */
    def preview = {
        responseImage(abstractImageService.preview(params.long('id')))
    }

    /**
     * Get annotation crop (image area that frame annotation)
     * This work for all kinds of annotations
     */
    def cropAnnotation = {
        try {
            def annotation = AnnotationDomain.getAnnotationDomain(params.id)
            def cropURL = getCropAnnotationURL(annotation,params)
            if(cropURL!=null) {
                if(!params.getBoolean('draw')) {
                    responseImage(cropURL)
                } else {
                    responseBufferedImage(createCropWithDraw(annotation,cropURL));
                }
            }
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }


    private BufferedImage createCropWithDraw(AnnotationDomain annotation,BufferedImage baseImage) {

        println "createCropWithDraw"
        //AbstractImage image, BufferedImage window, LineString lineString, Color color, int x, int y, double x_ratio, double y_ratio
        def boundaries = annotation.getBoundaries()
        double x_ratio = baseImage.getWidth() / boundaries.width
        double y_ratio = baseImage.getHeight() / boundaries.height

        println boundaries.width
        println x_ratio
        //int borderWidth = ((double)annotation.getArea()/(100000000d/50d))
        int borderWidth = ((double)boundaries.width/(15000/250d))*x_ratio


        println "borderWidth="+borderWidth

        //AbstractImage image, BufferedImage window, Collection<Geometry> geometryCollection, Color c, int borderWidth,int x, int y, double x_ratio, double y_ratio
        baseImage = segmentationService.drawPolygon(
                annotation.image.baseImage,
                baseImage,
                [annotation.location],
                Color.BLACK,
                borderWidth,
                boundaries.topLeftX,
                annotation.image.baseImage.getHeight() - boundaries.topLeftY,
                x_ratio,
                y_ratio
        )


      baseImage
    }


    /**
     * Get annotation crop (image area that frame annotation)
     * Force the size for crop annotation
     * This work for all kinds of annotations
     */
    def cropAnnotationMin = {
        try {
            params.max_size = "256"
            def annotation = AnnotationDomain.getAnnotationDomain(params.id)
            if(!params.getBoolean('draw')) {
                def cropURL = getCropAnnotationURL(annotation,params)
                responseImage(cropURL)
            } else {
                def value = params.max_size
                params.max_size=null
                def cropURL = getCropAnnotationURL(annotation,params)
                def image = createCropWithDraw(annotation,cropURL)
                if(value) {
                    println  Integer.parseInt(value)
                    image = scaleImage(image,Integer.parseInt(value),Integer.parseInt(value))
                }
                responseBufferedImage(image);
            }
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }


    /**
     * Get annotation user crop (image area that frame annotation)
     * (Use this service if you know the annotation type)
     */
    def cropUserAnnotation = {
        try {
            def annotation = UserAnnotation.read(params.id)

            if(!params.getBoolean('draw')) {
                def cropURL = getCropAnnotationURL(annotation,params)
                responseImage(cropURL)
            } else {
                def value = params.max_size
                params.max_size=null
                def cropURL = getCropAnnotationURL(annotation,params)
                println "Read image..."
                BufferedImage image = ImageIO.read(new URL(cropURL));
                println "Image read..."
                if(value && image.width>Integer.parseInt(value) && image.height>Integer.parseInt(value)) {
                    image = scaleImage(image,Integer.parseInt(value),Integer.parseInt(value))
                }
                image = createCropWithDraw(annotation,image)
                responseBufferedImage(image);
            }
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    /**
     * Get annotation algo crop (image area that frame annotation)
     * (Use this service if you know the annotation type)
     */
    def cropAlgoAnnotation = {
        try {
            def annotation = AlgoAnnotation.read(params.id)
            if(!params.getBoolean('draw')) {
                def cropURL = getCropAnnotationURL(annotation,params)
                responseImage(cropURL)
            } else {
                def value = params.max_size
                params.max_size=null
                def cropURL = getCropAnnotationURL(annotation,params)
                BufferedImage image = ImageIO.read(new URL(cropURL));
                if(value && image.width>Integer.parseInt(value) && image.height>Integer.parseInt(value)) {
                    image = scaleImage(image,Integer.parseInt(value),Integer.parseInt(value))
                }
                image = createCropWithDraw(annotation,image)
                responseBufferedImage(image);
            }
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    /**
     * Get annotation review crop (image area that frame annotation)
     * (Use this service if you know the annotation type)
     */
    def cropReviewedAnnotation = {
        try {
            def annotation = ReviewedAnnotation.read(params.id)
            if(!params.getBoolean('draw')) {
                def cropURL = getCropAnnotationURL(annotation,params)
                responseImage(cropURL)
            } else {
                def value = params.max_size
                params.max_size=null
                def cropURL = getCropAnnotationURL(annotation,params)
                BufferedImage image = ImageIO.read(new URL(cropURL));
                if(value && image.width>Integer.parseInt(value) && image.height>Integer.parseInt(value)) {
                    image = scaleImage(image,Integer.parseInt(value),Integer.parseInt(value))
                }
                image = createCropWithDraw(annotation,image)
                responseBufferedImage(image);
            }
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }


    private static BufferedImage scaleImage(BufferedImage img, Integer width, Integer height) {
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        if (imgWidth*height < imgHeight*width) {
            width = imgWidth*height/imgHeight;
        } else {
            height = imgHeight*width/imgWidth;
        }
        BufferedImage newImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImage.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.clearRect(0, 0, width, height);
            g.drawImage(img, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return newImage;
    }


    /**
     * Get crop annotation URL
     * @param annotation Annotation
     * @param params Params
     * @return Crop Annotation URL
     */
    private def getCropAnnotationURL(AnnotationDomain annotation, def params) {
        Integer zoom = 0
        Integer maxSize = -1
        if (params.max_size != null) {
            maxSize =  Integer.parseInt(params.max_size)
        }
        if (params.zoom != null) {
            zoom = Integer.parseInt(params.zoom)
        }

        if (annotation == null) {
            responseNotFound("Crop", "Annotation", params.id)
        } else if ((params.zoom != null) && (zoom < annotation.getImage().getBaseImage().getZoomLevels().min || zoom > annotation.getImage().getBaseImage().getZoomLevels().max)) {
            responseNotFound("Crop", "Zoom", zoom)
        } else {
            try {
                String cropURL
                if (maxSize != -1) {
                    cropURL = abstractImageService.cropWithMaxSize(annotation, maxSize)
                } else {
                    cropURL = abstractImageService.crop(annotation, zoom)
                }

                if (cropURL == null) {
                    //no crop available, add lambda image
                    cropURL = grailsApplication.config.grails.serverURL + "/images/cytomine.jpg"
                }
                return cropURL
            } catch (Exception e) {
                log.error("GetCrop:" + e)
                return null
            }
        }
    }

    public static BufferedImage base64ToImage(String base64ImageString) {

        BufferedImage image = null;
        byte[] imageByte;
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            imageByte = decoder.decodeBuffer(base64ImageString);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    def camera = {
        println "camera"
        println params.imgdata
        //:to do : save image in database ?
        //:+ send email
        String imageData = params.imgdata.replace(' ', '+')
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] imageByte = decoder.decodeBuffer(imageData);
        response.setContentType "application/octet-stream"
        response.setHeader "Content-disposition", "attachment; filename=capture.png"
        response.getOutputStream() << imageByte
        response.getOutputStream().flush()
    }


    /**
     * Get all image servers URL for an image
     */
    def imageServers = {

        try {
        def id = params.long('id')
        def merge = params.get('merge')

        if(merge) {
            def idImageInstance = params.long('imageinstance')
            ImageInstance image = ImageInstance.read(idImageInstance)

            log.info "Ai=$id Ii=$idImageInstance"

            ImageSequence sequence = imageSequenceService.get(image)

            if(!sequence) {
                throw new WrongArgumentException("ImageInstance $idImageInstance is not in a sequence!")
            }

            ImageGroup group = sequence.imageGroup

            log.info "sequence=$sequence group=$group"

            def images = imageSequenceService.list(group)

            log.info "all image for this group=$images"


            ProcessingServer server = ProcessingServer.first()

            def  params = []
            images.each { seq ->
                def imageinstance = seq.image
                def position = -1
                if(merge=="channel") position = seq.channel
                if(merge=="zstack") position = seq.zStack
                if(merge=="slice") position = seq.slice
                if(merge=="time") position = seq.time

                log.info "seq.image=${seq.image} position=${position}"

                def urls = abstractImageService.imageServers(imageinstance.baseImage.id).imageServersURLs

                log.info "urls=$urls"

                def param = "url$position="+ URLEncoder.encode(urls.first(),"UTF-8") +"&color$position="+ URLEncoder.encode(getColor(position),"UTF-8")
                log.info "param=$param"
                params << param

            }

            String url = server.url + "vision/merge?" + params.join("&") +"&zoomify="
            log.info "url=$url"

            //retrieve all image instance (same sequence)


            //get url for each image

            responseSuccess([imageServersURLs : [url]])
        } else {
            responseSuccess(abstractImageService.imageServers(id))
        }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    private static String getColor(int channel) {
        def rules = [0:"0,0,255",1:"0,255,0",2:"91,59,17",3:"255,0,0"]
        return rules.get(channel)
    }

    def imageSequenceService

    def mergeImage = {





    }
}



