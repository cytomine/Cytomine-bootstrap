package be.cytomine.api

import grails.converters.*
import be.cytomine.ontology.Annotation
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.annotation.AddAnnotationCommand
import be.cytomine.command.annotation.DeleteAnnotationCommand
import be.cytomine.command.annotation.EditAnnotationCommand

import be.cytomine.project.Project
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier
import com.vividsolutions.jts.io.WKTWriter
import be.cytomine.image.ImageInstance

import be.cytomine.command.TransactionController

import be.cytomine.ontology.AnnotationTerm
import be.cytomine.command.annotationterm.DeleteAnnotationTermCommand

import org.codehaus.groovy.grails.commons.ConfigurationHolder

import org.perf4j.LoggingStopWatch
import org.perf4j.StopWatch
import be.cytomine.image.server.RetrievalServer
import be.cytomine.ontology.Term

class RestAnnotationController extends RestController {

    def springSecurityService
    def exportService

    def list = {
        def data = Annotation.list()
        responseSuccess(data)
    }

    def listByImage = {
        log.info "List with id image:"+params.id
        ImageInstance image = ImageInstance.read(params.id)

        if(image!=null) responseSuccess(Annotation.findAllByImage(image))
        else responseNotFound("Image",params.id)
    }

    def listByUser = {
        log.info "List with id user:"+params.id
        User user = User.read(params.id)

        if(user!=null) responseSuccess(Annotation.findAllByUser(user))
        else responseNotFound("User",params.id)
    }

    def listByProject = {
        if (params.noTerm == "true") {
            Project project = Project.read(params.id)
            def terms = Term.findAllByOntology(project.getOntology())
            def annotationsWithTerms = AnnotationTerm.createCriteria().list {
                inList("term", terms)
                join("annotation")
                createAlias("annotation", "a")
                projections {
                    inList("a.image", project.imagesinstance())
                    groupProperty("annotation.id")
                }
            }
            def annotations = Annotation.createCriteria().list {
                inList("image", project.imagesinstance())
                not {
                    inList("id", annotationsWithTerms)
                }
            }
            if(project) responseSuccess(annotations)
            else responseNotFound("Project",params.id)
        } else {
            Project project = Project.read(params.id)
            def annotations = Annotation.findAllByImageInList(project.imagesinstance())
            if(project) responseSuccess(annotations)
            else responseNotFound("Project",params.id)
        }
    }

    def listByImageAndUser = {
        log.info "List with id image:"+params.idImage + " and id user:" + params.idUser
        def image = ImageInstance.read(params.idImage)
        def user = User.read(params.idUser)

        if(image && user) responseSuccess(Annotation.findAllByImageAndUser(image,user))
        else if(!user) responseNotFound("User",params.idUser)
        else if(!image) responseNotFound("Image",params.idImage)
    }

    def downloadDocumentByProject = {
        // Export service provided by Export plugin

        log.info "List with id project:"+params.id +" params.format="+params.format
        Project project = Project.read(params.id)
        if(project)
        {
            if(params?.format && params.format != "html"){
                def exporterIdentifier = params.format;
                if (exporterIdentifier == "xls") exporterIdentifier = "excel"
                response.contentType = ConfigurationHolder.config.grails.mime.types[params.format]
                response.setHeader("Content-disposition", "attachment; filename=annotations_project${project.id}.${params.format}")
                log.info "List annotation size="+ project.annotations().size()



                List fields = ["id","area","perimeter","centroid","image","filename","zoomLevel","user","created","updated","annotationTerm","URLForCrop","URLForServerGoTo",]
                Map labels = [
                        "id": "Id",
                        "area":"Area",
                        "perimeter" : "Perimeter",
                        "centroid" : "Centroid",
                        "image":"Image Id",
                        "filename":"Image Filename",
                        "zoomLevel":"Zoom Level",
                        "user":"User",
                        "created":"Created",
                        "updated":"Last update",
                        "annotationTerm":"Term list",
                        "URLForCrop":"View annotation picture",
                        "URLForServerGoTo":"View annotation on image"]

                /* Formatter closure in previous releases
                def upperCase = { value ->
                    return value.toUpperCase()
                }
                */

                // Formatter closure
                def wkt = { domain, value ->
                    return domain.location.toString()
                }
                def area = { domain, value ->
                    return domain.computeArea()
                }
                def perim = { domain, value ->
                    return domain.computePerimeter()
                }
                def centroid = { domain, value ->
                    return domain.getCentroid()
                }
                def imageId = { domain, value ->
                    return domain.image.id
                }
                def imageName = { domain, value ->
                    return domain.getFilename()
                }

                def user = { domain, value ->
                    return domain.user.username
                }

                def term = { domain, value ->
                    //return domain.termsName()
                    return domain.getTermsname()
                }

                def crop = { domain, value ->
                    return UrlApi.getAnnotationCropWithAnnotationId(domain.id)
                }

                def server = { domain, value ->
                    return UrlApi.getAnnotationURL(domain.image.getIdProject(),domain.image.id,domain.id)
                }
                Map formaters = [area : area,perimeter:perim, centroid:centroid,image:imageId,user:user,annotationTerm:term,URLForCrop:crop,URLForServerGoTo:server,filename:imageName]

                exportService.export(exporterIdentifier, response.outputStream,project.annotations(), fields, labels, formaters, ["csv.encoding":"UTF-8","separator":";"])
            }
            log.info "annotationInstanceList"
            [ annotationInstanceList: project.annotations() ]
        }
        else responseNotFound("Project",params.id)
    }

    def show = {
        log.info "Show with id:" + params.id
        Annotation annotation = Annotation.read(params.id)

        if(annotation!=null) responseSuccess(annotation)
        else responseNotFound("Annotation",params.id)
    }


    def simplify = {
        def toto = Project.findAllByIdInList([]);
        def simplify = [:]
        Geometry annotationFull = new WKTReader().read("POLYGON((10864 6624,10864 6656,10864 6752,10928 6816,10928 6848,10960 6880,10992 6912,10992 6944,10992 6976,10992 7008,11024 7040,11024 7072,11024 7136,11024 7168,11024 7232,11024 7296,11056 7328,11056 7392,11056 7424,11056 7456,11056 7488,11056 7552,11056 7584,11056 7616,11056 7680,11056 7712,11056 7744,11056 7808,11056 7840,11056 7872,11056 7904,11088 7968,11088 8032,11088 8064,11120 8096,11120 8160,11152 8192,11184 8256,11184 8320,11216 8384,11248 8416,11248 8448,11248 8480,11248 8512,11280 8576,11280 8640,11312 8704,11312 8768,11344 8768,11344 8832,11344 8864,11376 8896,11376 8928,11376 8960,11376 8992,11376 9024,11376 9056,11408 9056,11408 9120,11376 9184,11376 9216,11344 9248,11312 9280,11312 9312,11312 9344,11312 9376,11344 9472,11344 9504,11344 9536,11344 9568,11344 9600,11344 9632,11344 9664,11344 9696,11376 9728,11376 9760,11376 9792,11408 9824,11440 9856,11472 9888,11504 9920,11568 9952,11600 9952,11664 9984,11728 9984,11760 10048,11792 10048,11792 10080,11792 10112,11760 10144,11760 10176,11760 10208,11760 10240,11760 10272,11792 10272,11792 10304,11792 10336,11792 10368,11792 10464,11760 10528,11760 10592,11760 10624,11728 10656,11728 10688,11728 10720,11696 10752,11696 10784,11632 10912,11568 10976,11536 11008,11536 11040,11472 11104,11440 11136,11408 11168,11376 11200,11376 11232,11344 11296,11312 11360,11312 11392,11312 11424,11344 11456,11344 11488,11376 11552,11408 11584,11440 11616,11440 11648,11472 11680,11472 11712,11504 11712,11504 11744,11536 11744,11568 11744,11568 11776,11600 11808,11600 11840,11632 11872,11664 11904,11664 11936,11696 11936,11696 11968,11696 12000,11728 12000,11728 12064,11792 12128,11824 12160,11888 12224,12016 12288,12048 12352,12048 12384,12048 12416,12048 12448,12048 12480,12080 12512,12080 12544,12080 12576,12112 12608,12112 12672,12144 12736,12144 12768,12176 12832,12176 12896,12208 12928,12208 12960,12240 13024,12272 13056,12304 13088,12336 13120,12400 13184,12464 13248,12496 13280,12528 13312,12560 13344,12592 13376,12592 13408,12624 13440,12656 13472,12656 13504,12656 13536,12656 13568,12720 13632,12720 13696,12752 13728,12752 13792,12784 13824,12816 13856,12848 13920,12944 14016,13072 14112,13168 14176,13264 14272,13296 14272,13360 14336,13392 14400,13456 14464,13488 14496,13552 14560,13584 14592,13584 14624,13584 14656,13584 14752,13616 14816,13648 14912,13680 14976,13712 15040,13712 15104,13712 15168,13744 15232,13744 15296,13744 15328,13776 15360,13776 15424,13808 15456,13808 15520,13840 15584,13840 15648,13840 15680,13840 15744,13872 15808,13872 15840,13904 15904,13936 15968,13968 16000,13968 16064,14000 16128,14000 16160,14032 16224,14032 16256,14064 16320,14064 16352,14064 16384,14096 16448,14128 16544,14160 16608,14192 16640,14224 16672,14224 16704,14256 16736,14288 16768,14352 16800,14352 16832,14384 16864,14384 16896,14416 16928,14448 16992,14480 17024,14544 17088,14576 17120,14608 17120,14640 17120,14672 17120,14704 17152,14768 17184,14800 17184,14864 17184,14896 17216,14960 17216,14992 17216,15024 17216,15056 17216,15088 17216,15120 17216,15152 17216,15184 17216,15216 17248,15248 17248,15312 17248,15312 17280,15376 17312,15408 17344,15440 17376,15472 17376,15536 17376,15600 17376,15600 17408,15664 17440,15760 17440,15824 17472,15888 17472,15952 17472,15984 17472,16080 17472,16208 17472,16240 17472,16272 17472,16304 17472,16400 17472,16496 17440,16528 17440,16560 17440,16592 17408,16656 17408,16688 17408,16720 17408,16752 17408,16784 17408,16784 17376,16848 17344,16944 17280,17072 17248,17104 17216,17168 17184,17264 17120,17360 17088,17488 17024,17520 16992,17552 16960,17584 16896,17616 16896,17648 16832,17680 16832,17712 16800,17744 16736,17776 16672,17776 16608,17808 16576,17840 16512,17872 16480,17968 16320,18000 16256,18064 16192,18096 16128,18096 16096,18096 16064,18128 16064,18160 16064,18224 16000,18256 15936,18320 15936,18416 15840,18448 15808,18480 15776,18512 15744,18544 15744,18576 15680,18608 15648,18608 15616,18640 15584,18704 15552,18768 15488,18768 15456,18800 15392,18832 15360,18832 15296,18864 15232,18896 15200,18896 15136,18896 15072,18896 15040,18864 15040,18864 15008,18832 15008,18800 14976,18768 14944,18736 14944,18704 14912,18640 14880,18608 14880,18576 14880,18576 14848,18544 14848,18512 14816,18512 14784,18512 14752,18512 14688,18544 14688,18576 14624,18672 14592,18768 14528,18864 14496,18896 14496,18928 14496,18960 14496,18992 14496,18992 14464,18992 14496,19024 14496,19056 14528,19056 14560,19056 14592,19088 14592,19088 14560,19120 14560,19152 14496,19152 14432,19152 14400,19152 14368,19152 14304,19184 14144,19216 14080,19216 14048,19216 13984,19248 13920,19248 13888,19280 13824,19312 13792,19344 13760,19344 13696,19376 13632,19408 13600,19408 13472,19408 13440,19408 13344,19408 13312,19440 13248,19440 13152,19440 13056,19440 12992,19440 12864,19440 12768,19440 12704,19440 12640,19440 12608,19440 12544,19440 12480,19440 12448,19472 12384,19472 12320,19472 12256,19472 12192,19472 12128,19472 12064,19472 12000,19472 11936,19472 11904,19472 11872,19472 11840,19472 11808,19472 11776,19472 11712,19472 11648,19472 11616,19440 11584,19440 11520,19440 11488,19440 11456,19440 11392,19440 11360,19440 11296,19440 11232,19472 11200,19472 11136,19504 11072,19536 11008,19568 11008,19600 10944,19600 10880,19632 10848,19632 10784,19632 10752,19664 10752,19664 10720,19664 10656,19664 10592,19632 10528,19632 10464,19632 10368,19632 10336,19600 10272,19600 10208,19600 10144,19568 10080,19568 9984,19536 9920,19536 9856,19536 9792,19504 9728,19504 9696,19504 9664,19504 9632,19504 9600,19472 9536,19472 9472,19440 9376,19440 9280,19440 9216,19408 9184,19376 9088,19376 9056,19376 9024,19376 8928,19344 8896,19312 8864,19312 8832,19280 8768,19280 8736,19280 8704,19248 8672,19248 8640,19248 8608,19216 8512,19216 8480,19184 8416,19184 8384,19152 8288,19120 8224,19120 8160,19120 8128,19120 8032,19088 7968,19088 7904,19056 7808,19056 7712,19024 7648,19024 7616,19024 7552,19024 7520,19024 7456,19024 7424,18992 7392,18992 7360,18992 7328,18992 7264,18960 7232,18928 7168,18896 7136,18864 7072,18864 6976,18832 6944,18800 6880,18800 6848,18800 6816,18800 6784,18800 6752,18800 6720,18800 6688,18768 6624,18736 6592,18736 6528,18704 6496,18704 6400,18640 6304,18608 6208,18576 6176,18544 6112,18512 6080,18480 6016,18416 5952,18384 5888,18320 5760,18256 5664,18224 5632,18192 5536,18160 5472,18128 5440,18128 5376,18096 5312,18064 5280,18032 5216,17968 5152,17936 5056,17904 5056,17872 4992,17840 4928,17776 4864,17744 4832,17712 4768,17648 4672,17616 4576,17584 4448,17552 4416,17552 4320,17520 4256,17488 4160,17456 4128,17456 4096,17392 4000,17360 3936,17328 3776,17296 3712,17232 3648,17200 3552,17168 3520,17104 3456,17072 3392,17008 3360,17008 3296,16944 3232,16912 3136,16848 3072,16816 3008,16784 2912,16752 2848,16720 2752,16688 2656,16656 2592,16656 2528,16656 2496,16656 2464,16624 2432,16592 2368,16592 2304,16528 2240,16496 2208,16464 2144,16432 2080,16336 2016,16272 1984,16240 1920,16176 1856,16080 1856,16016 1856,15888 1760,15824 1728,15760 1728,15664 1696,15600 1696,15568 1696,15472 1696,15408 1696,15344 1696,15344 1728,15312 1728,15312 1760,15280 1760,15248 1760,15152 1824,15088 1856,15024 1856,14992 1888,14928 1920,14864 1984,14800 2080,14736 2144,14672 2208,14608 2304,14544 2368,14544 2432,14512 2464,14480 2464,14448 2464,14416 2496,14320 2496,14256 2496,14192 2496,14096 2496,14000 2464,13936 2464,13840 2432,13744 2432,13680 2432,13552 2432,13456 2400,13360 2400,13232 2400,13168 2400,13072 2368,12944 2336,12816 2336,12720 2304,12624 2240,12560 2208,12496 2208,12432 2144,12336 2112,12272 2112,12176 2048,11984 1952,11792 1856,11728 1824,11696 1792,11664 1760,11632 1760,11632 1728,11600 1696,11600 1664,11536 1632,11504 1568,11440 1472,11376 1408,11280 1344,11216 1248,11152 1184,11120 1152,11088 1088,10992 1056,10928 960,10864 928,10832 864,10768 768,10736 736,10672 704,10640 672,10608 640,10544 576,10448 544,10352 544,10256 544,10128 544,10064 544,9968 544,9904 544,9872 544,9776 608,9712 672,9584 832,9520 928,9456 992,9424 1056,9392 1088,9328 1184,9296 1248,9232 1312,9232 1376,9200 1408,9200 1472,9104 1568,9072 1632,9072 1696,9072 1728,9040 1792,9040 1888,9072 1984,9072 2080,9072 2144,9072 2272,9072 2400,9072 2528,9072 2656,9072 2752,9072 2848,9104 2976,9104 3040,9104 3200,9136 3328,9168 3360,9200 3488,9200 3584,9232 3680,9296 3744,9296 3808,9296 3872,9328 3968,9328 4096,9392 4192,9392 4288,9456 4352,9456 4448,9520 4576,9584 4672,9616 4736,9648 4832,9712 4928,9744 5056,9776 5120,9808 5152,9840 5216,9872 5248,9904 5280,9936 5312,10032 5376,10096 5408,10128 5472,10192 5536,10224 5632,10288 5664,10320 5728,10384 5824,10416 5888,10448 5952,10480 6016,10544 6080,10576 6144,10640 6176,10672 6272,10704 6272,10704 6304,10736 6304,10736 6336,10736 6368,10768 6400,10768 6432,10864 6624))");
        Geometry annotationCompress = (Geometry)annotationFull.clone();
        int lastSize = 0
        for(int i=0;i<1000;i=i+25)
        {
            annotationCompress = DouglasPeuckerSimplifier.simplify(annotationCompress,i)
            String wkt = new WKTWriter().write(annotationCompress)
            println "lastSize=" + lastSize  +" wkt.size()="  + wkt.size()
            if(wkt.size()!=lastSize) {
                println "add"
                simplify[i] =  wkt
                lastSize = wkt.size()
            }
        }
        responseSuccess(simplify)


    }

    def add = {
        log.info "Add"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def json = request.JSON
        println "json = " + json
        println "json.location = " + json.location
        try {
            String form = json.location;
            Geometry annotation = simplifyPolygon(form)
            json.location =  new WKTWriter().write(annotation)
        } catch(Exception e) {}
        log.info "User:" + currentUser.username + " transaction:" +  currentUser.transactionInProgress  + " request:" +json.toString()
        Command addAnnotationCommand = new AddAnnotationCommand(postData : json.toString(), user: currentUser)
        def result = processCommand(addAnnotationCommand, currentUser)
        log.info "Index annotation with id=" +result?.annotation?.id
        Long id = result?.annotation?.id

        try {if(id) indexRetrievalAnnotation(id) } catch(Exception e) { log.error "Cannot index in retrieval:"+e.toString()}

        response(result)
    }








    def delete = {
        log.info "Delete"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username +" transaction:" +  currentUser.transactionInProgress  + " params.id=" + params.id
        //TODO: delete annotation-term if annotation is deleted
        def postData = ([id : params.id]) as JSON

        log.info "Start transaction"
        TransactionController transaction = new TransactionController();
        transaction.start()

        Annotation annotation = Annotation.read(params.id)

        if(annotation) {
            def terms = annotation.terms()
            log.debug "annotation.terms.size=" + terms.size()
            terms.each { term ->

                def annotationTerm = AnnotationTerm.findAllByTermAndAnnotation(term,annotation)
                log.info "annotationTerm= " +annotationTerm.size()

                annotationTerm.each{ annotterm ->
                    log.info "unlink annotterm:" +annotterm.id
                    def postDataRT = ([term: annotterm.term.id,annotation: annotterm.annotation.id]) as JSON
                    Command deleteAnnotationTermCommand = new DeleteAnnotationTermCommand(postData :postDataRT.toString() ,user: currentUser,printMessage:false)
                    def result = processCommand(deleteAnnotationTermCommand, currentUser)
                }
            }
        }
        log.info "delete annotation"

        Command deleteAnnotationCommand = new DeleteAnnotationCommand(postData : postData.toString(), user: currentUser)
        def result = processCommand(deleteAnnotationCommand, currentUser)
        transaction.stop()
        Long id = result?.annotation?.id
        try {if(id) deleteRetrievalAnnotation(id) } catch(Exception e) { log.error "Cannot delete in retrieval:"+e.toString()}
        response(result)
    }

    private indexRetrievalAnnotation(Long id) {
        //index in retrieval (asynchronous)
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "annotation.id="+id + " stevben-server="+ retrieval
        if(id && retrieval) {
            log.info "index annotation " + id + " on  " +  retrieval.url
            RestRetrievalController.indexAnnotationSynchronous(Annotation.read(id))
        }

    }
    private deleteRetrievalAnnotation(Long id) {
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "annotation.id="+id + " retrieval-server="+ retrieval
        if(id && retrieval) {
            log.info "delete annotation " + id + " on  " +  retrieval.url
            RestRetrievalController.deleteAnnotationSynchronous(id)
        }
    }

    private updateRetrievalAnnotation(Long id) {
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "annotation.id="+id + " retrieval-server="+ retrieval
        if(id && retrieval) {
            log.info "update annotation " + id + " on  " +  retrieval.url
            RestRetrievalController.updateAnnotationSynchronous(id)
        }
    }

    def update = {
        log.info "Update"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
        Command editAnnotationCommand = new EditAnnotationCommand(postData : request.JSON.toString(), user: currentUser)
        def result = processCommand(editAnnotationCommand, currentUser)
        if(result.success) {
            Long id = result.annotation.id
            try {updateRetrievalAnnotation(id)} catch(Exception e) { log.error "Cannot update in retrieval:"+e.toString()}
        }
        response(result)
    }


    private Geometry simplifyPolygon(String form) {
        Geometry lastAnnotationFull
        Geometry annotationFull = new WKTReader().read(form);
        println "points=" + annotationFull.getNumPoints() + " " + annotationFull.getArea();
        println "annotationFull:"+annotationFull.getNumPoints() + " |" + new WKTWriter().write(annotationFull);
        StopWatch stopWatch = new LoggingStopWatch();
        /**
         * Must be improve:
         * -Number of point depends on: size of annotation, times during the draw, ...
         * Sometimes bad perf because incrThreshold is too small (but too big: risk to have bad compression => recover (break;) => too many points)
         */
        float i = 0;
        int max=500; //max loop (prevent infinite loop)
        float incrThreshold = 0.25f //increment threshold value
        if(annotationFull.getNumPoints()>500) {
            while(annotationFull.getNumPoints()>75 && max>0)
            {
                lastAnnotationFull = DouglasPeuckerSimplifier.simplify(annotationFull,i)
                println "annotationFull=" + i + " "+lastAnnotationFull.getNumPoints()
                if(lastAnnotationFull.getNumPoints()<50) break;
                annotationFull = lastAnnotationFull
                i=i+(incrThreshold*5); max--;
            }
        }else  if(annotationFull.getNumPoints()>250) {
            while(annotationFull.getNumPoints()>50 && max>0)
            {
                lastAnnotationFull = DouglasPeuckerSimplifier.simplify(annotationFull,i)
                println "annotationFull=" + i + " "+lastAnnotationFull.getNumPoints()
                if(lastAnnotationFull.getNumPoints()<35) break;
                annotationFull = lastAnnotationFull
                i=i+(incrThreshold*2); max--;
            }
        } else if(annotationFull.getNumPoints()>100) {
            while(annotationFull.getNumPoints()>10 && max>0)
            {
                lastAnnotationFull = DouglasPeuckerSimplifier.simplify(annotationFull,i)
                println "annotationFull=" + i + " "+lastAnnotationFull.getNumPoints()
                if(lastAnnotationFull.getNumPoints()<6) break;
                annotationFull = lastAnnotationFull
                i=i+(incrThreshold); max--;
            }
        }else {
            while(annotationFull.getNumPoints()>10 && max>0)
            {
                lastAnnotationFull = DouglasPeuckerSimplifier.simplify(annotationFull,i)
                println "annotationFull=" + i + " "+lastAnnotationFull.getNumPoints()
                if(lastAnnotationFull.getNumPoints()<6) break;
                annotationFull = lastAnnotationFull
                i=i+(incrThreshold); max--;
            }
        }
        stopWatch.stop("compress:");
        println "annotationFull good=" + i + " "+annotationFull.getNumPoints() + " |" + new WKTWriter().write(lastAnnotationFull);
        return lastAnnotationFull
    }

}
