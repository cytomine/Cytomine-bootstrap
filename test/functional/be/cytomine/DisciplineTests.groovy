package be.cytomine

import be.cytomine.project.Discipline
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.DisciplineAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.utils.UpdateData

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class DisciplineTests extends functionaltestplugin.FunctionalTestCase {

  void testListDisciplineWithCredential() {
      def result = DisciplineAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray
  }

  void testListDisciplineWithoutCredential() {
      def result = DisciplineAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
      assertEquals(401, result.code)
  }

  void testShowDisciplineWithCredential() {
      def result = DisciplineAPI.show(BasicInstance.createOrGetBasicDiscipline().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }

  void testAddDisciplineCorrect() {
      def disciplineToAdd = BasicInstance.getBasicDisciplineNotExist()
      def result = DisciplineAPI.create(disciplineToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      int idDiscipline = result.data.id

      result = DisciplineAPI.show(idDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)

      result = DisciplineAPI.undo()
      assertEquals(200, result.code)

      result = DisciplineAPI.show(idDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)

      result = DisciplineAPI.redo()
      assertEquals(200, result.code)

      result = DisciplineAPI.show(idDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
  }

  void testAddDisciplineAlreadyExist() {
      def disciplineToAdd = BasicInstance.createOrGetBasicDiscipline()
      def result = DisciplineAPI.create(disciplineToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(409, result.code)
  }

  void testUpdateDisciplineCorrect() {
      Discipline disciplineToAdd = BasicInstance.createOrGetBasicDiscipline()

      def data = UpdateData.createUpdateSet(disciplineToAdd)
      def result = DisciplineAPI.update(data.oldData.id, data.newData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
      int idDiscipline = json.discipline.id

      def showResult = DisciplineAPI.show(idDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      json = JSON.parse(showResult.data)
      BasicInstance.compareDiscipline(data.mapNew, json)

      showResult = DisciplineAPI.undo()
      assertEquals(200, result.code)
      showResult = DisciplineAPI.show(idDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      BasicInstance.compareDiscipline(data.mapOld, JSON.parse(showResult.data))

      showResult = DisciplineAPI.redo()
      assertEquals(200, result.code)
      showResult = DisciplineAPI.show(idDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      BasicInstance.compareDiscipline(data.mapNew, JSON.parse(showResult.data))
  }

  void testUpdateDisciplineNotExist() {
      Discipline disciplineWithOldName = BasicInstance.createOrGetBasicDiscipline()
      Discipline disciplineWithNewName = BasicInstance.getBasicDisciplineNotExist()
      disciplineWithNewName.save(flush: true)
      Discipline disciplineToEdit = Discipline.get(disciplineWithNewName.id)
      def jsonDiscipline = disciplineToEdit.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonDiscipline)
      jsonUpdate.name = disciplineWithOldName.name
      jsonUpdate.id = -99
      jsonDiscipline = jsonUpdate.encodeAsJSON()
      def result = DisciplineAPI.update(-99, jsonDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)
  }

  void testUpdateDisciplineWithNameAlreadyExist() {
      Discipline disciplineWithOldName = BasicInstance.createOrGetBasicDiscipline()
      Discipline disciplineWithNewName = BasicInstance.getBasicDisciplineNotExist()
      disciplineWithNewName.save(flush: true)
      Discipline disciplineToEdit = Discipline.get(disciplineWithNewName.id)
      def jsonDiscipline = disciplineToEdit.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonDiscipline)
      jsonUpdate.name = disciplineWithOldName.name
      jsonDiscipline = jsonUpdate.encodeAsJSON()
      def result = DisciplineAPI.update(disciplineToEdit.id, jsonDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(409, result.code)
  }
    
    void testEditDisciplineWithBadName() {
        Discipline disciplineToAdd = BasicInstance.createOrGetBasicDiscipline()
        Discipline disciplineToEdit = Discipline.get(disciplineToAdd.id)
        def jsonDiscipline = disciplineToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonDiscipline)
        jsonUpdate.name = null
        jsonDiscipline = jsonUpdate.encodeAsJSON()
        def result = DisciplineAPI.update(disciplineToAdd.id, jsonDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

  void testDeleteDiscipline() {
      def disciplineToDelete = BasicInstance.getBasicDisciplineNotExist()
      assert disciplineToDelete.save(flush: true)!= null
      def id = disciplineToDelete.id
      def result = DisciplineAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)

      def showResult = DisciplineAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, showResult.code)

      result = DisciplineAPI.undo()
      assertEquals(200, result.code)

      result = DisciplineAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)

      result = DisciplineAPI.redo()
      assertEquals(200, result.code)

      result = DisciplineAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)
  }

  void testDeleteDisciplineNotExist() {
      def result = DisciplineAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)
  }

  void testDeleteDisciplineWithProject() {
      def project = BasicInstance.createOrGetBasicProject()
      def disciplineToDelete = project.discipline
      def result = DisciplineAPI.delete(disciplineToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(400, result.code)
  }

}
