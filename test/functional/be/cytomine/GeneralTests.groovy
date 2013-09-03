package be.cytomine

import be.cytomine.command.Command
import be.cytomine.command.CommandHistory
import be.cytomine.command.RedoStackItem
import be.cytomine.command.UndoStackItem
import be.cytomine.security.User
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import be.cytomine.test.http.UserAnnotationAPI
import be.cytomine.utils.News
import be.cytomine.utils.database.ArchiveCommandService
import grails.converters.JSON
import grails.util.Environment
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.ontology.UserAnnotation

import java.nio.file.DirectoryNotEmptyException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class GeneralTests  {

    def aclService
    def aclUtilService
    def objectIdentityRetrievalStrategy
    def sessionFactory
    def springSecurityService

    void testUIViewPortToXMLConversion() {
        try{
            ViewPortToBuildXML.process()
        } catch(Exception e) {
            log.error e
            fail()
        }
    }

    void testCommandMaxSizeOK() {
        log.info("create image")
        String jsonImage = "{\"text\" : \"*************************************************************************"
        String textAdded = "***************************************************************************************"
        jsonImage = jsonImage + "\"}"

        log.info("post with data size:" + jsonImage.size())
        String URL = Infos.CYTOMINEURL + "api/image.json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.post(jsonImage)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assert code != 413
        def json = JSON.parse(response)
    }

    void testLastAction() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()

        def result = UserAnnotationAPI.create(annotationToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        int idAnnotation = result.data.id

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = UserAnnotationAPI.undo()
        assert 200 == result.code

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = UserAnnotationAPI.redo()
        assert 200 == result.code

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        /*
        * Get the last 3 commands: it must be "REDO ADD ANNOTATION", "UNDO ADD ANNOTATION" and "ADD ANNOTATION"
        */
        Long idProject = annotationToAdd.image.project.id
        Integer max = 3
        HttpClient client = new HttpClient();
        String url = Infos.CYTOMINEURL + "api/project/" + idProject + "/last/" + max + ".json"
        client.connect(url, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        assert 200 == code
        def json = JSON.parse(response)
        assert json.collection instanceof JSONArray

    }

    void testLastActionProjectNotExist() {
        HttpClient client = new HttpClient();
        String url = Infos.CYTOMINEURL + "api/project/-99/last/10.json"
        client.connect(url, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        int code = client.getResponseCode()
        client.disconnect();
        assert 404 == code
    }

    void testMultipleAuthConnexion() {
        BasicInstanceBuilder.getUserAnnotation()
        UserAnnotation annotation = UserAnnotation.list().first()

        log.info "show userannotation " + annotation.id
        String URL = Infos.CYTOMINEURL + "api/userannotation/" + annotation.id + ".json"
        HttpClient client1 = new HttpClient();
        client1.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client1.get()
        int code = client1.getResponseCode()
        String response = client1.getResponseData()
        assert code == 200

        HttpClient client2 = new HttpClient();
        client2.connect(URL, Infos.ANOTHERLOGIN, Infos.ANOTHERPASSWORD);
        client2.get()
        code = client2.getResponseCode()
        assert code == 200
        client1.disconnect()

        client1.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client1.get()
        code = client1.getResponseCode()
        assert code == 200

        client2.disconnect();
        client2.connect(URL, Infos.ANOTHERLOGIN, Infos.ANOTHERPASSWORD);
        client2.get()
        code = client2.getResponseCode()
        assert code == 200

        client1.disconnect();
        client2.disconnect();
    }

    void testPing() {
        HttpClient client = new HttpClient();
        String url = Infos.CYTOMINEURL + "server/ping.json"
        client.connect(url, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        def json = '{"project": "' + BasicInstanceBuilder.getProject().id + '"}'
        client.post(json)
        int code = client.getResponseCode()
        client.disconnect();
        assert 200 == code
    }


//    void testArchiveCommand() {
//
//        CommandHistory.list().each {it.delete()}
//        Command.list().each {
//            UndoStackItem.findAllByCommand(it).each {it.delete()}
//            RedoStackItem.findAllByCommand(it).each {it.delete()}
//            it.delete()
//        }
//
//        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
//        def result = UserAnnotationAPI.create(annotationToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        result = UserAnnotationAPI.create(annotationToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        result = UserAnnotationAPI.create(annotationToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
//
//        assert Command.list().size()==3
//        def histories = CommandHistory.list()
//        assert histories.size()==3
//
//        histories[0].created = new SimpleDateFormat("yyyy-MM-dd").parse("2012-12-05")
//        histories[0].command.created = new SimpleDateFormat("yyyy-MM-dd").parse("2012-12-05")
//        histories[1].created = new SimpleDateFormat("yyyy-MM-dd").parse("2013-04-12")
//        histories[1].command.created = new SimpleDateFormat("yyyy-MM-dd").parse("2013-04-12")
//        histories[2].created = new SimpleDateFormat("yyyy-MM-dd").parse("2013-04-12")
//        histories[2].command.created = new SimpleDateFormat("yyyy-MM-dd").parse("2013-04-12")
//
//        histories.each {
//            BasicInstanceBuilder.saveDomain(it)
//            BasicInstanceBuilder.saveDomain(it.command)
//        }
//
//        def ids = CommandHistory.list().collect{it.command.id+""}
//
//
//        FileUtils.deleteDirectory(new File("oldcommand/${Environment.getCurrent()}"));
//
//        assert !new File("oldcommand/${Environment.getCurrent()}").exists()
//
//        ArchiveCommandService archive = new ArchiveCommandService()
//        archive.archiveOldCommand()
//
//        assert new File("oldcommand/${Environment.getCurrent()}").exists()
//         def today = new Date()
//        def firstFile = new File("oldcommand/${Environment.getCurrent()}/${today.year}-${today.month+1}-${today.date}.log")
//
//        assert firstFile.exists()
//
//        def content1 = firstFile.text.split("\n")
//
//        assert content1.size()==3
//
//        assert ids.contains(content1[0].split(";")[0])
//        assert ids.contains(content1[1].split(";")[0])
//        assert ids.contains(content1[2].split(";")[0])
//    }


     void testNewsListing() {
         def data = [
                 [date:'12/08/2013', text:'A project has now a user and admin list. A project admin is able to edit annotations from other user. Furthermore, a project admin is not affected by the "private layer" options. Only project creator and project admin can raise a user as project admin.'],
                 [date:'27/07/2013',text:'Project can be locked. If a project is locked, you can delete all job data with no reviewed annotation.'],
                 [date:'14/06/2013',text:'Project, Image and Annotation can now have a description.'],
                 [date:'27/05/2013',text:'Review view is now available in project. This helps meet specific needs especially for Cytology review.'],
                 [date: '08/05/2013',text:'You can now use keyboard shortcuts to perform some actions. Look at the "Help" section on the top of this windows.']

         ]

         data.each {

             News news = new News(added:new SimpleDateFormat("dd/MM/yyyy").parse(it.date),text:it.text, user: User.list().first())
             assert news.validate()
             println news.errors
             assert news.save(flush:true)
         }


         HttpClient client1 = new HttpClient();
         String URL = Infos.CYTOMINEURL + "api/news.json"
         client1.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
         client1.get()
         int code = client1.getResponseCode()
         String response = client1.getResponseData()
         assert code == 200
         client1.disconnect()
         def json = JSON.parse(response).collection
         assert json.size()==5


     }




}
