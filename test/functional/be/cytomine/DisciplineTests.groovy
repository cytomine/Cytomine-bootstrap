package be.cytomine

import be.cytomine.project.Discipline
import be.cytomine.test.BasicInstanceBuilder
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
class DisciplineTests  {

  void testListDisciplineWithCredential() {
      def result = DisciplineAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
  }

  void testListDisciplineWithoutCredential() {
      def result = DisciplineAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
      assert 401 == result.code
  }

  void testShowDisciplineWithCredential() {
      def result = DisciplineAPI.show(BasicInstanceBuilder.getDiscipline().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }

  void testAddDisciplineCorrect() {
      def disciplineToAdd = BasicInstanceBuilder.getDisciplineNotExist()
      def result = DisciplineAPI.create(disciplineToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      int idDiscipline = result.data.id

      result = DisciplineAPI.show(idDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code

      result = DisciplineAPI.undo()
      assert 200 == result.code

      result = DisciplineAPI.show(idDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == result.code

      result = DisciplineAPI.redo()
      assert 200 == result.code

      result = DisciplineAPI.show(idDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
  }

  void testAddDisciplineAlreadyExist() {
      def disciplineToAdd = BasicInstanceBuilder.getDiscipline()
      def result = DisciplineAPI.create(disciplineToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 409 == result.code
  }

  void testUpdateDisciplineCorrect() {
      Discipline disciplineToAdd = BasicInstanceBuilder.getDiscipline()

      def data = UpdateData.createUpdateSet(disciplineToAdd)
      def result = DisciplineAPI.update(data.oldData.id, data.newData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
      int idDiscipline = json.discipline.id

      def showResult = DisciplineAPI.show(idDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      json = JSON.parse(showResult.data)
      BasicInstanceBuilder.compare(data.mapNew, json)

      showResult = DisciplineAPI.undo()
      assert 200 == result.code
      showResult = DisciplineAPI.show(idDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      BasicInstanceBuilder.compare(data.mapOld, JSON.parse(showResult.data))

      showResult = DisciplineAPI.redo()
      assert 200 == result.code
      showResult = DisciplineAPI.show(idDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      BasicInstanceBuilder.compare(data.mapNew, JSON.parse(showResult.data))
  }

  void testUpdateDisciplineNotExist() {
      Discipline disciplineWithOldName = BasicInstanceBuilder.getDiscipline()
      Discipline disciplineWithNewName = BasicInstanceBuilder.getDisciplineNotExist()
      disciplineWithNewName.save(flush: true)
      Discipline disciplineToEdit = Discipline.get(disciplineWithNewName.id)
      def jsonDiscipline = disciplineToEdit.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonDiscipline)
      jsonUpdate.name = disciplineWithOldName.name
      jsonUpdate.id = -99
      jsonDiscipline = jsonUpdate.encodeAsJSON()
      def result = DisciplineAPI.update(-99, jsonDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == result.code
  }

  void testUpdateDisciplineWithNameAlreadyExist() {
      Discipline disciplineWithOldName = BasicInstanceBuilder.getDiscipline()
      Discipline disciplineWithNewName = BasicInstanceBuilder.getDisciplineNotExist()
      disciplineWithNewName.save(flush: true)
      Discipline disciplineToEdit = Discipline.get(disciplineWithNewName.id)
      def jsonDiscipline = disciplineToEdit.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonDiscipline)
      jsonUpdate.name = disciplineWithOldName.name
      jsonDiscipline = jsonUpdate.encodeAsJSON()
      def result = DisciplineAPI.update(disciplineToEdit.id, jsonDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 409 == result.code
  }
    
    void testEditDisciplineWithBadName() {
        Discipline disciplineToAdd = BasicInstanceBuilder.getDiscipline()
        Discipline disciplineToEdit = Discipline.get(disciplineToAdd.id)
        def jsonDiscipline = disciplineToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonDiscipline)
        jsonUpdate.name = null
        jsonDiscipline = jsonUpdate.encodeAsJSON()
        def result = DisciplineAPI.update(disciplineToAdd.id, jsonDiscipline, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

  void testDeleteDiscipline() {
      def disciplineToDelete = BasicInstanceBuilder.getDisciplineNotExist()
      assert disciplineToDelete.save(flush: true)!= null
      def id = disciplineToDelete.id
      def result = DisciplineAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code

      def showResult = DisciplineAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == showResult.code

      result = DisciplineAPI.undo()
      assert 200 == result.code

      result = DisciplineAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code

      result = DisciplineAPI.redo()
      assert 200 == result.code

      result = DisciplineAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == result.code
  }

  void testDeleteDisciplineNotExist() {
      def result = DisciplineAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == result.code
  }

  void testDeleteDisciplineWithProject() {
      def project = BasicInstanceBuilder.getProject()
      def disciplineToDelete = project.discipline
      def result = DisciplineAPI.delete(disciplineToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 400 == result.code
  }

}
