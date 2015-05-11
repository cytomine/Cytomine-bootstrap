package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ImageServerAPI
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class ImageServerTests {

    void testImageServerListByMime() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        def result = ImageServerAPI.listImageServerByMime(imageInstance.baseImage.mime.mimeType,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection.size()>0
    }


    void testGetThumb512() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        def result = ImageServerAPI.thumb(imageInstance.baseImage.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD, 512)
        BufferedImage thumb = result.image
        BufferedImage expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/thumb512.png"))
        assert thumb
        assert expected
        assert thumb.width == expected.width
        assert thumb.height == expected.height
    }

    void testGetThumb256() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        def result = ImageServerAPI.thumb(imageInstance.baseImage.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BufferedImage thumb = result.image
        BufferedImage expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/thumb256.png"))
        assert thumb
        assert expected
        assert thumb.width == expected.width
        assert thumb.height == expected.height
    }

    void testGetImagePreview() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        def result = ImageServerAPI.preview(imageInstance.baseImage.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BufferedImage thumb = result.image
        BufferedImage expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/preview.png"))
        assert thumb
        assert expected
        assert thumb.width == expected.width
        assert thumb.height == expected.height
    }

    //api/imageinstance/1676/mask?x=7500&y=16500&w=2000&h=2000&term=28859

    //OLD TEST: NEW TESTS WILL COME WITH http://jira.cytomine.be/browse/CYTO-132
//    void testGetAnnotationMask() {
//        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
//
//        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(imageInstance.project,imageInstance, true)
//        annotation.location = new WKTReader().read("POLYGON ((9168 21200, 8080 21328, 7824 20592, 8112 19600, 9552 19504, 9936 20880, 9168 21200))")
//        BasicInstanceBuilder.saveDomain(annotation)
//        AnnotationTerm at = BasicInstanceBuilder.getAnnotationTermNotExist(annotation,true)
//
//        def result = ImageServerAPI.mask(imageInstance.id,7500,16500,2000,2000,null,null, [annotation.id], Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
//        BufferedImage thumb = result.image
//        BufferedImage expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/mask.png"))
//        assert thumb.width == expected.width
//        assert thumb.height == expected.height
//
//        result = ImageServerAPI.mask(imageInstance.id,7500,16500,2000,2000,at.term.id,null, [annotation.id], Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
//        thumb = result.image
//        assert thumb.width == expected.width
//        assert thumb.height == expected.height
//    }
//
//
//    void testGetReviewedAnnotationMask() {
//        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
//
//
//        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(imageInstance.project,imageInstance, true)
//        annotation.location = new WKTReader().read("POLYGON ((9168 21200, 8080 21328, 7824 20592, 8112 19600, 9552 19504, 9936 20880, 9168 21200))")
//        BasicInstanceBuilder.saveDomain(annotation)
//        AnnotationTerm at = BasicInstanceBuilder.getAnnotationTermNotExist(annotation,true)
//        ReviewedAnnotation reviewedAnnotation = BasicInstanceBuilder.createReviewAnnotation(annotation)
//        reviewedAnnotation.addToTerms(at.term)
//        BasicInstanceBuilder.saveDomain(reviewedAnnotation)
//
//        def result = ImageServerAPI.maskReviewed(imageInstance.id,7500,16500,2000,2000,reviewedAnnotation.terms.first().id,null,[reviewedAnnotation.id], Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
//        BufferedImage thumb = result.image
//        BufferedImage expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/mask.png"))
//        assert thumb.width == expected.width
//        assert thumb.height == expected.height
//
//    }


    void testGetUserAnnotationMask() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()

        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(imageInstance.project,imageInstance, true)
        annotation.location = new WKTReader().read("POLYGON ((9168 21200, 8080 21328, 7824 20592, 8112 19600, 9552 19504, 9936 20880, 9168 21200))")
        BasicInstanceBuilder.saveDomain(annotation)

        def result = ImageServerAPI.maskUserAnnotation(annotation.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BufferedImage thumb = result.image
        BufferedImage expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/maskAnnotation.png"))
        assert thumb.width == expected.width
        assert thumb.height == expected.height
    }


    void testGetUserAnnotationAlphaMask() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()

        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(imageInstance.project,imageInstance, true)
        annotation.location = new WKTReader().read("POLYGON ((9168 21200, 8080 21328, 7824 20592, 8112 19600, 9552 19504, 9936 20880, 9168 21200))")
        BasicInstanceBuilder.saveDomain(annotation)

        def result = ImageServerAPI.maskUserAnnotationAlpha(annotation.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BufferedImage thumb = result.image
        BufferedImage expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/alphamask.png"))
        assert thumb.width == expected.width
        assert thumb.height == expected.height
    }

    void testGetReviewedAnnotationAlphaMask() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(imageInstance.project,imageInstance, true)
        annotation.location = new WKTReader().read("POLYGON ((9168 21200, 8080 21328, 7824 20592, 8112 19600, 9552 19504, 9936 20880, 9168 21200))")
        BasicInstanceBuilder.saveDomain(annotation)
        ReviewedAnnotation reviewedAnnotation = BasicInstanceBuilder.createReviewAnnotation(annotation)

        BasicInstanceBuilder.saveDomain(reviewedAnnotation)

        def result = ImageServerAPI.maskReviewedAnnotationAlpha(reviewedAnnotation.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BufferedImage thumb = result.image
        BufferedImage expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/alphamask.png"))
        assert thumb.width == expected.width
        assert thumb.height == expected.height
    }


//    static def maskUserAnnotation(Long idAnnotation, Long idTerm, String username, String password) {
//        String URL = Infos.CYTOMINEURL + "api/userannotation/$idAnnotation/mask-$idTerm"
//        return downloadImage(URL,username,password)
//    }
//
//    static def maskUserAnnotationAlpha(Long idAnnotation, Long idTerm, String username, String password) {
//        String URL = Infos.CYTOMINEURL + "api/userannotation/$idAnnotation/alphamask-$idTerm"
//        return downloadImage(URL,username,password)
//    }
//
//    static def maskAlgoAnnotationAlpha(Long idAnnotation, Long idTerm, String username, String password) {
//        String URL = Infos.CYTOMINEURL + "api/algoannotation/$idAnnotation/alphamask-$idTerm"
//        return downloadImage(URL,username,password)
//    }
//
//    static def maskReviewedAnnotationAlpha(Long idAnnotation, Long idTerm, String username, String password) {
//        String URL = Infos.CYTOMINEURL + "api/reviewedannotation/$idAnnotation/alphamask-$idTerm"
//        return downloadImage(URL,username,password)
//    }






    void testGetWindow() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        def result = ImageServerAPI.window(imageInstance.id,20000,30000,300,300,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BufferedImage thumb = result.image
        BufferedImage expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/window-20000-30000-300-300.jpg"))
        assert thumb
        assert expected
        assert thumb.width == expected.width
        assert thumb.height == expected.height
    }

    void testGetWindowUrl() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        def result = ImageServerAPI.windowUrl(imageInstance.id,20000,30000,300,300,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert result.code == 200
    }

    void testGetCropGeometry() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        def result = ImageServerAPI.cropGeometry(imageInstance.id,"POLYGON ((9168 21200, 8080 21328, 7824 20592, 8112 19600, 9552 19504, 9936 20880, 9168 21200))",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BufferedImage thumb = result.image
        BufferedImage expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/cropgeometry.png"))
        assert thumb.width == expected.width
        assert thumb.height == expected.height
    }


    void testGetCropAnnotationWithoutDraw() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(imageInstance.project,imageInstance, true)
        annotation.location = new WKTReader().read("POLYGON ((9168 21200, 8080 21328, 7824 20592, 8112 19600, 9552 19504, 9936 20880, 9168 21200))")
        BasicInstanceBuilder.saveDomain(annotation)

        def result = ImageServerAPI.cropAnnotation(annotation.id,false,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BufferedImage thumb = result.image
        BufferedImage expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/crop.jpg"))
        assert thumb.width == expected.width
        assert thumb.height == expected.height

        result = ImageServerAPI.cropAnnotation(annotation.id,false,100,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        thumb = result.image
        assert thumb.width <= 100 || thumb.height <= 100

        result = ImageServerAPI.cropGeometryZoom(annotation.id,1,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        thumb = result.image
        expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/cropZoom1.jpg"))
        assert thumb.width == expected.width
        assert thumb.height == expected.height

    }


    void testGetCropAnnotationWithDraw() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(imageInstance.project,imageInstance, true)
        annotation.location = new WKTReader().read("POLYGON ((9168 21200, 8080 21328, 7824 20592, 8112 19600, 9552 19504, 9936 20880, 9168 21200))")
        BasicInstanceBuilder.saveDomain(annotation)

        def result = ImageServerAPI.cropAnnotation(annotation.id,true,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BufferedImage thumb = result.image
        BufferedImage expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/cropWithDraw.jpg"))
        assert thumb.width == expected.width
        assert thumb.height == expected.height

        result = ImageServerAPI.cropAnnotation(annotation.id,true,100,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        thumb = result.image
        assert thumb.width <= 100  || thumb.height <= 100

    }

    void testGetCropAnnotationMin() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(imageInstance.project,imageInstance, true)
        annotation.location = new WKTReader().read("POLYGON ((9168 21200, 8080 21328, 7824 20592, 8112 19600, 9552 19504, 9936 20880, 9168 21200))")
        BasicInstanceBuilder.saveDomain(annotation)
        def result = ImageServerAPI.cropAnnotationMin(annotation.id,false,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BufferedImage thumb = result.image
        BufferedImage expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/cropMin.jpg"))

        assert thumb.width-expected.width==0
        assert Math.abs(thumb.height-expected.height)<3

        result = ImageServerAPI.cropAnnotationMin(annotation.id,true,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        thumb = result.image
        assert thumb
    }




    void testGetCropUserAnnotationWithoutDraw() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(imageInstance.project,imageInstance, true)
        annotation.location = new WKTReader().read("POLYGON ((9168 21200, 8080 21328, 7824 20592, 8112 19600, 9552 19504, 9936 20880, 9168 21200))")
        BasicInstanceBuilder.saveDomain(annotation)

        def result = ImageServerAPI.cropUserAnnotation(annotation.id,false,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BufferedImage thumb = result.image
        BufferedImage expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/crop.jpg"))
        assert thumb.width == expected.width
        assert thumb.height == expected.height

        result = ImageServerAPI.cropUserAnnotation(annotation.id,false,100,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        thumb = result.image
        assert thumb.width <= 100  || thumb.height <= 100

        result = ImageServerAPI.cropUserAnnotation(annotation.id,1,false,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        thumb = result.image
        expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/cropZoom1.jpg"))
        assert thumb.width == expected.width
        assert thumb.height == expected.height

    }

    void testGetCropAlgoAnnotationWithoutDraw() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        AlgoAnnotation annotation = BasicInstanceBuilder.getAlgoAnnotationNotExist()
        annotation.project = imageInstance.project
        annotation.image = imageInstance
        annotation.location = new WKTReader().read("POLYGON ((9168 21200, 8080 21328, 7824 20592, 8112 19600, 9552 19504, 9936 20880, 9168 21200))")
        BasicInstanceBuilder.saveDomain(annotation)

        def result = ImageServerAPI.cropAlgoAnnotation(annotation.id,false,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BufferedImage thumb = result.image
        BufferedImage expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/crop.jpg"))
        assert thumb.width == expected.width
        assert thumb.height == expected.height

        result = ImageServerAPI.cropAlgoAnnotation(annotation.id,false,100,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        thumb = result.image
        assert thumb.width <= 100  || thumb.height <= 100

        result = ImageServerAPI.cropAlgoAnnotation(annotation.id,1,false,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        thumb = result.image
        expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/cropZoom1.jpg"))
        assert thumb.width == expected.width
        assert thumb.height == expected.height

    }

    void testGetCropReviewedAnnotationWithoutDraw() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(imageInstance.project,imageInstance, true)
        annotation.location = new WKTReader().read("POLYGON ((9168 21200, 8080 21328, 7824 20592, 8112 19600, 9552 19504, 9936 20880, 9168 21200))")
        BasicInstanceBuilder.saveDomain(annotation)
        ReviewedAnnotation reviewedAnnotation = BasicInstanceBuilder.createReviewAnnotation(annotation)

        def result = ImageServerAPI.cropReviewedAnnotation(reviewedAnnotation.id,false,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BufferedImage thumb = result.image
        BufferedImage expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/crop.jpg"))
        assert thumb.width == expected.width
        assert thumb.height == expected.height

        result = ImageServerAPI.cropReviewedAnnotation(reviewedAnnotation.id,false,100,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        thumb = result.image
        assert thumb.width <= 100  || thumb.height <= 100

        result = ImageServerAPI.cropReviewedAnnotation(reviewedAnnotation.id,1,false,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        thumb = result.image
        expected = ImageIO.read(new File("test/functional/be/cytomine/utils/images/cropZoom1.jpg"))
        assert thumb.width == expected.width
        assert thumb.height == expected.height

    }

    void testGetCropUserAnnotationWithDraw() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(imageInstance.project,imageInstance, true)
        annotation.location = new WKTReader().read("POLYGON ((9168 21200, 8080 21328, 7824 20592, 8112 19600, 9552 19504, 9936 20880, 9168 21200))")
        BasicInstanceBuilder.saveDomain(annotation)

        def result = ImageServerAPI.cropUserAnnotation(annotation.id,true,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BufferedImage thumb = result.image
        assert thumb

    }

    void testGetCropAlgoAnnotationWithDraw() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        AlgoAnnotation annotation = BasicInstanceBuilder.getAlgoAnnotationNotExist()
        annotation.project = imageInstance.project
        annotation.image = imageInstance
        annotation.location = new WKTReader().read("POLYGON ((9168 21200, 8080 21328, 7824 20592, 8112 19600, 9552 19504, 9936 20880, 9168 21200))")
        BasicInstanceBuilder.saveDomain(annotation)

        def result = ImageServerAPI.cropAlgoAnnotation(annotation.id,true,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BufferedImage thumb = result.image
        assert thumb

    }

    void testGetCropReviewedAnnotationWithDraw() {
        ImageInstance imageInstance = BasicInstanceBuilder.initImage()
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(imageInstance.project,imageInstance, true)
        annotation.location = new WKTReader().read("POLYGON ((9168 21200, 8080 21328, 7824 20592, 8112 19600, 9552 19504, 9936 20880, 9168 21200))")
        BasicInstanceBuilder.saveDomain(annotation)
        ReviewedAnnotation reviewedAnnotation = BasicInstanceBuilder.createReviewAnnotation(annotation)

        def result = ImageServerAPI.cropReviewedAnnotation(reviewedAnnotation.id,true,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BufferedImage thumb = result.image
        assert thumb

    }


    void testGetImageServers() {

        ImageInstance imageInstance = BasicInstanceBuilder.initImage()

        def result = ImageServerAPI.imageServers(imageInstance.baseImage.id,imageInstance.id,false,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert result.code == 200
        def json = JSON.parse(result.data)
        println json

        def imageSequence = BasicInstanceBuilder.getImageSequenceNotExist(true)
        imageSequence.image = imageInstance
        BasicInstanceBuilder.saveDomain(imageInstance)

        result = ImageServerAPI.imageServers(imageInstance.baseImage.id,imageInstance.id,true,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert result.code == 200
        json = JSON.parse(result.data)
        println json

    }

//
///////  void testGetMask() {
//
//    }
//
//    void testGetAlphaMaskUserAnnotation() {
//
//        //+test with draw
//
//    }
//
//    void testGetAlphaMaskAlgoAnnotation() {
//
//        //+test with draw
//
//    }
//
//    void testGetAlphaMaskReviewedAnnotation() {
//
//        //+test with draw
//
//    }
//
//    void testGetCropMask() {
//
//    }
//
//





}
