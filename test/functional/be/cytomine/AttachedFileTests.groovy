package be.cytomine

import be.cytomine.project.Discipline
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AttachedFileAPI
import be.cytomine.test.http.DisciplineAPI
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class AttachedFileTests {

  void testListAttachedFile() {
      def result = AttachedFileAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
  }

  void testShowAttachedFile() {
      def domain = BasicInstanceBuilder.getAttachedFileNotExist(true)
      println domain.domainClassName
      println domain.domainIdent

      Project.list().each{println it.id}

      def result = AttachedFileAPI.show(domain.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }

    void testShowAttachedFileNotExist() {
        def result = AttachedFileAPI.show(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


    void testListAttachedFileByDomain() {
        def domain = BasicInstanceBuilder.getAttachedFileNotExist(true)
        def result = AttachedFileAPI.listByDomain(domain.domainClassName,domain.domainIdent,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert !json.collection.isEmpty()
    }

    void testDownloadAttachedFile() {
        def result = AttachedFileAPI.download(BasicInstanceBuilder.getAttachedFileNotExist("test/functional/be/cytomine/utils/images/preview.png",true).id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

  void testAddAttachedFileCorrect() {
      def attachedFileToAdd = BasicInstanceBuilder.getAttachedFileNotExist(false)
      def result = AttachedFileAPI.upload(attachedFileToAdd.domainClassName,attachedFileToAdd.domainIdent,new File("test/functional/be/cytomine/utils/simpleFile.txt"),Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
  }


}
