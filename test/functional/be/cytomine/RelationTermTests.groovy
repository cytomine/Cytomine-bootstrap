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

import be.cytomine.ontology.Relation
import be.cytomine.ontology.RelationTerm
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.RelationTermAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 23/02/11
 * Time: 11:01
 * To change this template use File | Settings | File Templates.
 */
class RelationTermTests {

    void testShowRelationTerm() {
        RelationTerm relationTerm = BasicInstanceBuilder.getRelationTerm()
        def result = RelationTermAPI.show(relationTerm.relation.id,relationTerm.term1.id,relationTerm.term2.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject

        result = RelationTermAPI.show(-99,relationTerm.term1.id,relationTerm.term2.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
        result = RelationTermAPI.show(relationTerm.relation.id,-99,relationTerm.term2.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
        result = RelationTermAPI.show(relationTerm.relation.id,relationTerm.term1.id,-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testListRelation() {
        def result = RelationTermAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }
    
  void testListRelationTermByTerm1() {
      def result = RelationTermAPI.listByTerm(BasicInstanceBuilder.getTerm().id,1, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray

      result = RelationTermAPI.listByTerm(BasicInstanceBuilder.getTerm().id,3, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }

    void testListRelationTermByTerm2() {
        def result = RelationTermAPI.listByTerm(BasicInstanceBuilder.getTerm().id,2, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }


    void testListRelationTermByTerm() {
        def result = RelationTermAPI.listByTermAll(BasicInstanceBuilder.getTerm().id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = RelationTermAPI.listByTermAll(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }


  void testAddRelationTermCorrect() {
      def relationTermToAdd = BasicInstanceBuilder.getRelationTermNotExist()

      String jsonRelationTerm = relationTermToAdd.encodeAsJSON()
      def json = JSON.parse(jsonRelationTerm)
      json.relation = relationTermToAdd.relation.id
      json.term1 = relationTermToAdd.term1.id
      json.term2 = relationTermToAdd.term2.id
      int idRelation = relationTermToAdd.relation.id
      int idTerm1 = relationTermToAdd.term1.id
      int idTerm2 = relationTermToAdd.term2.id
      jsonRelationTerm = json.toString()

      def result = RelationTermAPI.create(jsonRelationTerm, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      //int idRelationTerm = result.data.id

      result = RelationTermAPI.show(idRelation,idTerm1,idTerm2, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      result = RelationTermAPI.undo()
      assert 200 == result.code

      result = RelationTermAPI.show(idRelation,idTerm1,idTerm2, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code

      result = RelationTermAPI.redo()
      assert 200 == result.code

      result = RelationTermAPI.show(idRelation,idTerm1,idTerm2, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

  }


   void testAddRelationTermCorrectDefaultRelationParent() {
        def relationTermToAdd = BasicInstanceBuilder.getRelationTermNotExist()

        String jsonRelationTerm = relationTermToAdd.encodeAsJSON()
        def json = JSON.parse(jsonRelationTerm)
        json.relation = null
        json.term1 = relationTermToAdd.term1.id
        json.term2 = relationTermToAdd.term2.id
        jsonRelationTerm = json.toString()

        def result = RelationTermAPI.create(jsonRelationTerm, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        //int idRelationTerm = result.data.id

    }

  void testAddRelationTermAlreadyExist() {
      def relationTermToAdd = BasicInstanceBuilder.getRelationTerm()

      String jsonRelationTerm = relationTermToAdd.encodeAsJSON()
      def json = JSON.parse(jsonRelationTerm)
      json.relation = relationTermToAdd.relation.id
      json.term1 = relationTermToAdd.term1.id
      json.term2 = relationTermToAdd.term2.id
      jsonRelationTerm = json.toString()

      def result = RelationTermAPI.create(jsonRelationTerm, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 409 == result.code
  }

  void testAddRelationTermWithRelationNotExist() {
    def relationTermToAdd = BasicInstanceBuilder.getRelationTerm()
    String jsonRelationTerm = relationTermToAdd.encodeAsJSON()
    def json = JSON.parse(jsonRelationTerm)
    json.relation = -99
    json.term1 = relationTermToAdd.term1.id
    json.term2 = relationTermToAdd.term2.id
    jsonRelationTerm = json.toString()

      def result = RelationTermAPI.create(jsonRelationTerm, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 400 == result.code
  }

  void testAddRelationTermWithTerm1NotExist() {
      def relationTermToAdd = BasicInstanceBuilder.getRelationTerm()
      String jsonRelationTerm = relationTermToAdd.encodeAsJSON()
      def json = JSON.parse(jsonRelationTerm)
      json.relation = relationTermToAdd.relation.id
      json.term1 = -99
      json.term2 = relationTermToAdd.term2.id
      jsonRelationTerm = json.toString()
  
        def result = RelationTermAPI.create(jsonRelationTerm, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
  }

  void testAddRelationTermWithTerm2NotExist() {
      def relationTermToAdd = BasicInstanceBuilder.getRelationTerm()
      String jsonRelationTerm = relationTermToAdd.encodeAsJSON()
      def json = JSON.parse(jsonRelationTerm)
      json.relation = relationTermToAdd.relation.id
      json.term1 = relationTermToAdd.term1.id
      json.term2 = -99
      jsonRelationTerm = json.toString()
  
        def result = RelationTermAPI.create(jsonRelationTerm, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
  }

  void testDeleteRelationTerm() {
      def relationtermToDelete = BasicInstanceBuilder.getRelationTermNotExist()
      assert relationtermToDelete.save(flush: true)  != null
      def id = relationtermToDelete.id
      int idRelation = relationtermToDelete.relation.id
      int idTerm1 = relationtermToDelete.term1.id
      int idTerm2 = relationtermToDelete.term2.id
      def result = RelationTermAPI.delete(relationtermToDelete.relation.id,relationtermToDelete.term1.id,relationtermToDelete.term2.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      def showResult = RelationTermAPI.show(idRelation,idTerm1,idTerm2, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == showResult.code

      result = RelationTermAPI.undo()
      assert 200 == result.code

      result = RelationTermAPI.show(idRelation,idTerm1,idTerm2, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      result = RelationTermAPI.redo()
      assert 200 == result.code

      result = RelationTermAPI.show(idRelation,idTerm1,idTerm2, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }

    void testDeleteRelationTermDefaultParent() {
        def relationtermToDelete = BasicInstanceBuilder.getRelationTermNotExist()
        relationtermToDelete.relation = Relation.findByName(RelationTerm.names.PARENT)
        assert relationtermToDelete.save(flush: true)  != null
        def result = RelationTermAPI.delete(null,relationtermToDelete.term1.id,relationtermToDelete.term2.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

    }

  void testDeleteRelationTermNotExist() {
      def result = RelationTermAPI.delete(-99,-99,-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }


}
