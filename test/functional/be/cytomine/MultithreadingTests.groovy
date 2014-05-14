package be.cytomine

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AnnotationTermAPI
import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.test.http.UserAnnotationAPI
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class MultithreadingTests  {

    void testMultithreadAnnotationAdd() {

        int nbThread = 4
        Thread[] ts = new Thread[nbThread]

        for(int i=0;i<nbThread;i++) {
           ts[i]=new AnnotationAddConcurrent();
        }

        for(int i=0;i<nbThread;i++) {
           ts[i].start()
        }

        for(int i=0;i<nbThread;i++) {
           ts[i].join()
           assert 200==ts[i].code
        }
    }

    void testMultithreadImageInstanceAdd() {

        int nbThread = 4
        Thread[] ts = new Thread[nbThread]

        for(int i=0;i<nbThread;i++) {
           ts[i]=new ImageInstanceAddConcurrent();
        }
         for(int i=0;i<nbThread;i++) {
           ts[i].start()
        }
        for(int i=0;i<nbThread;i++) {
           ts[i].join()
            assert 200==ts[i].code
        }
    }

    void testMultithreadAnnotationTermAdd() {

        int nbThread = 4
        Thread[] ts = new Thread[nbThread]

        for(int i=0;i<nbThread;i++) {
           ts[i]=new AnnotationTermAddConcurrent();
        }
        for(int i=0;i<nbThread;i++) {
           ts[i].start()
        }

        for(int i=0;i<nbThread;i++) {
           ts[i].join()
            assert 200==ts[i].code
        }
    }
}



//: exempleConcurrent.java
class AnnotationAddConcurrent extends Thread {

    private static Log log = LogFactory.getLog(AnnotationAddConcurrent.class)

    public String json

    public Integer code = -1

    public AnnotationAddConcurrent() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        json = annotationToAdd.encodeAsJSON()
    }

    public void run() {
        log.info("start thread")
        log.info("create userannotation")
        def result = UserAnnotationAPI.create(json, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        log.info("check response")
        code = result.code
        log.info("end thread")
    }
}

class ImageInstanceAddConcurrent extends Thread {

    private static Log log = LogFactory.getLog(ImageInstanceAddConcurrent.class)

    public String json

    public Integer code = -1

    public ImageInstanceAddConcurrent() {
        def imageToAdd = BasicInstanceBuilder.getImageInstanceNotExist()
        json = imageToAdd.encodeAsJSON()
    }

    public void run() {
        log.info("start thread")
        log.info("create image instance")
        def result = ImageInstanceAPI.create(json, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        log.info("check response")
        code = result.code
        log.info("end thread")
    }
}

class AnnotationTermAddConcurrent extends Thread {

    private static Log log = LogFactory.getLog(AnnotationTermAddConcurrent.class)

    public String json

    public Integer code = -1

    public AnnotationTermAddConcurrent() {
        def annotationTermToAdd = BasicInstanceBuilder.getAnnotationTermNotExist()
        annotationTermToAdd.discard()
        json = annotationTermToAdd.encodeAsJSON()
    }

    public void run() {
        log.info("start thread")
        log.info("create image instance")
        def result = AnnotationTermAPI.createAnnotationTerm(json,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        log.info("check response")
        code = result.code
        log.info("end thread")
    }
}












