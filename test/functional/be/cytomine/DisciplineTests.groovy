package be.cytomine

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.project.Discipline
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
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
class DisciplineTests  {

  void testListDisciplineWithCredential() {
      def result = DisciplineAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
  }

  void testListDisciplineWithoutCredential() {
      def result = DisciplineAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
      assert 401 == result.code
  }

  void testShowDisciplineWithCredential() {
      def result = DisciplineAPI.show(BasicInstanceBuilder.getDiscipline().id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }

  void testAddDisciplineCorrect() {
      def disciplineToAdd = BasicInstanceBuilder.getDisciplineNotExist()
      def result = DisciplineAPI.create(disciplineToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      int idDiscipline = result.data.id

      result = DisciplineAPI.show(idDiscipline, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      result = DisciplineAPI.undo()
      assert 200 == result.code

      result = DisciplineAPI.show(idDiscipline, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code

      result = DisciplineAPI.redo()
      assert 200 == result.code

      result = DisciplineAPI.show(idDiscipline, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
  }

  void testAddDisciplineAlreadyExist() {
      def disciplineToAdd = BasicInstanceBuilder.getDiscipline()
      def result = DisciplineAPI.create(disciplineToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 409 == result.code
  }

  void testUpdateDisciplineCorrect() {
      def discipline = BasicInstanceBuilder.getDiscipline()
      def data = UpdateData.createUpdateSet(discipline,[name: ["OLDNAME","NEWNAME"]])
      def result = DisciplineAPI.update(discipline.id, data.postData,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
      int idDiscipline = json.discipline.id

      def showResult = DisciplineAPI.show(idDiscipline, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      json = JSON.parse(showResult.data)
      BasicInstanceBuilder.compare(data.mapNew, json)

      showResult = DisciplineAPI.undo()
      assert 200 == result.code
      showResult = DisciplineAPI.show(idDiscipline, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      BasicInstanceBuilder.compare(data.mapOld, JSON.parse(showResult.data))

      showResult = DisciplineAPI.redo()
      assert 200 == result.code
      showResult = DisciplineAPI.show(idDiscipline, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
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
      jsonDiscipline = jsonUpdate.toString()
      def result = DisciplineAPI.update(-99, jsonDiscipline, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
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
      jsonDiscipline = jsonUpdate.toString()
      def result = DisciplineAPI.update(disciplineToEdit.id, jsonDiscipline, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 409 == result.code
  }
    
    void testEditDisciplineWithBadName() {
        Discipline disciplineToAdd = BasicInstanceBuilder.getDiscipline()
        Discipline disciplineToEdit = Discipline.get(disciplineToAdd.id)
        def jsonDiscipline = disciplineToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonDiscipline)
        jsonUpdate.name = null
        jsonDiscipline = jsonUpdate.toString()
        def result = DisciplineAPI.update(disciplineToAdd.id, jsonDiscipline, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 400 == result.code
    }

  void testDeleteDiscipline() {
      def disciplineToDelete = BasicInstanceBuilder.getDisciplineNotExist()
      assert disciplineToDelete.save(flush: true)!= null
      def id = disciplineToDelete.id
      def result = DisciplineAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      def showResult = DisciplineAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == showResult.code

      result = DisciplineAPI.undo()
      assert 200 == result.code

      result = DisciplineAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      result = DisciplineAPI.redo()
      assert 200 == result.code

      result = DisciplineAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }

  void testDeleteDisciplineNotExist() {
      def result = DisciplineAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }

  void testDeleteDisciplineWithProject() {
      def project = BasicInstanceBuilder.getProject()
      def disciplineToDelete = project.discipline
      def result = DisciplineAPI.delete(disciplineToDelete.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 400 == result.code
  }

}
