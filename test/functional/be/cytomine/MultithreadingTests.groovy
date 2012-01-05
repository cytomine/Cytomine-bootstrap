package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Annotation
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.AnnotationAPI
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.test.http.AnnotationTermAPI
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class MultithreadingTests extends functionaltestplugin.FunctionalTestCase {

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
           assertEquals(200, ts[i].code)
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
           assertEquals(200, ts[i].code)
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
           assertEquals(200, ts[i].code)
        }
    }
}



//: exempleConcurrent.java
class AnnotationAddConcurrent extends Thread {

    private static Log log = LogFactory.getLog(AnnotationAddConcurrent.class)

    public String json

    public Integer code = -1

    public AnnotationAddConcurrent() {
        def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        json = annotationToAdd.encodeAsJSON()
    }

    public void run() {
        log.info("start thread")
        log.info("create annotation")
        def result = AnnotationAPI.createAnnotation(json, Infos.GOODLOGIN, Infos.GOODPASSWORD)
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
        def imageToAdd = BasicInstance.getBasicImageInstanceNotExist()
        json = imageToAdd.encodeAsJSON()
    }

    public void run() {
        log.info("start thread")
        log.info("create image instance")
        def result = ImageInstanceAPI.createImageInstance(json, Infos.GOODLOGIN, Infos.GOODPASSWORD)
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
        def annotationTermToAdd = BasicInstance.getBasicAnnotationTermNotExist("testAddAnnotationTermCorrect")
        annotationTermToAdd.discard()
        json = annotationTermToAdd.encodeAsJSON()
    }

    public void run() {
        log.info("start thread")
        log.info("create image instance")
        def result = AnnotationTermAPI.createAnnotationTerm(json,Infos.GOODLOGIN,Infos.GOODPASSWORD)
        log.info("check response")
        code = result.code
        log.info("end thread")
    }
}












