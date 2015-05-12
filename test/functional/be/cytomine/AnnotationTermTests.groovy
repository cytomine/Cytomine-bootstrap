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

import be.cytomine.ontology.AnnotationTerm
import be.cytomine.security.User
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AnnotationTermAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 22/02/11
 * Time: 10:58
 * To change this template use File | Settings | File Templates.
 */
class AnnotationTermTests  {

  void testGetAnnotationTermWithCredential() {
    User currentUser = User.findByUsername(Infos.SUPERADMINLOGIN)
    def annotationTermToAdd = BasicInstanceBuilder.getAnnotationTerm()
    def result = AnnotationTermAPI.showAnnotationTerm(annotationTermToAdd.userAnnotation.id,annotationTermToAdd.term.id,currentUser.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
    assert 200 == result.code
    def json = JSON.parse(result.data)
    assert json instanceof JSONObject
  }

  void testListAnnotationTermByAnnotationWithCredential() {
    def result = AnnotationTermAPI.listAnnotationTermByAnnotation(BasicInstanceBuilder.getUserAnnotation().id,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
    assert 200 == result.code
    def json = JSON.parse(result.data)
    assert json.collection instanceof JSONArray
  }

    void testListAnnotationTermByAnnotationWithCredentialWithUser() {
      def result = AnnotationTermAPI.listAnnotationTermByAnnotation(BasicInstanceBuilder.getUserAnnotation().id,BasicInstanceBuilder.user1.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
    }

    void testListAnnotationTermByUserNotWithCredential() {
      def result = AnnotationTermAPI.listAnnotationTermByUserNot(BasicInstanceBuilder.getUserAnnotation().id,BasicInstanceBuilder.user1.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
    }

  void testAddAnnotationTermCorrect() {
     User currentUser = User.findByUsername(Infos.SUPERADMINLOGIN)
    def annotationTermToAdd = BasicInstanceBuilder.getAnnotationTermNotExist()
    annotationTermToAdd.discard()
    String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()
    def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
    assert 200 == result.code

    AnnotationTerm annotationTerm = result.data
    Long idAnnotation = annotationTerm.userAnnotation.id
    Long idTerm = annotationTerm.term.id
    log.info("check if object "+ annotationTerm.userAnnotation.id +"/"+ annotationTerm.term.id +"exist in DB")

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
    assert 200 == result.code

    result = AnnotationTermAPI.undo()
    assert 200 == result.code

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
    assert 404 == result.code

    result = AnnotationTermAPI.redo()
    assert 200 == result.code

    log.info("check if object "+ idAnnotation +"/"+ idTerm +" exist in DB")
    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
    assert 200 == result.code

  }

    void testAddAnnotationTermWithTermFromOtherOntology() {
         User currentUser = User.findByUsername(Infos.SUPERADMINLOGIN)
        def annotationTermToAdd = BasicInstanceBuilder.getAnnotationTermNotExist()
        annotationTermToAdd.discard()
        annotationTermToAdd.term = BasicInstanceBuilder.getTermNotExist(true)
        String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()
        def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 == result.code
    }

    void testAddAnnotationTermCorrectDeletingOldTerm() {
       User currentUser = User.findByUsername(Infos.SUPERADMINLOGIN)
      def annotationTermToAdd = BasicInstanceBuilder.getAnnotationTermNotExist()
      annotationTermToAdd.discard()
      String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()
      def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD,true)
      assert 200 == result.code

      AnnotationTerm annotationTerm = result.data
      Long idAnnotation = annotationTerm.userAnnotation.id
      Long idTerm = annotationTerm.term.id
      log.info("check if object "+ annotationTerm.userAnnotation.id +"/"+ annotationTerm.term.id +"exist in DB")

      result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      result = AnnotationTermAPI.undo()
      assert 200 == result.code

      result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 404 == result.code

      result = AnnotationTermAPI.redo()
      assert 200 == result.code

      log.info("check if object "+ idAnnotation +"/"+ idTerm +" exist in DB")
      result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

    }

  void testAddAnnotationTermAlreadyExist() {
    def annotationTermToAdd = BasicInstanceBuilder.getAnnotationTermNotExist()
    annotationTermToAdd.save(flush:true)
    //annotationTermToAdd is in database, we will try to add it twice
    String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()
    def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
    assert 409 == result.code
  }

  void testAddAnnotationTermWithAnnotationNotExist() {
    def annotationTermAdd = BasicInstanceBuilder.getAnnotationTermNotExist()
    String jsonAnnotationTerm = annotationTermAdd.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonAnnotationTerm)
    jsonUpdate.userannotation = -99
    jsonAnnotationTerm = jsonUpdate.toString()
    def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
    assert 400 == result.code
  }

  void testAddAnnotationTermWithTermNotExist() {
    def annotationTermAdd = BasicInstanceBuilder.getAnnotationTermNotExist()
    String jsonAnnotationTerm = annotationTermAdd.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonAnnotationTerm)
    jsonUpdate.term = -99
    jsonAnnotationTerm = jsonUpdate.toString()
    def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
    assert 400 == result.code
  }

  void testDeleteAnnotationTerm() {
    User currentUser = User.findByUsername(Infos.SUPERADMINLOGIN)

    def annotationTermToDelete = BasicInstanceBuilder.getAnnotationTerm()
    int idAnnotation = annotationTermToDelete.userAnnotation.id
    int idTerm = annotationTermToDelete.term.id
    int idUser = currentUser.id
    assert annotationTermToDelete.userAnnotation.project.ontology==annotationTermToDelete.term.ontology

    def result = AnnotationTermAPI.deleteAnnotationTerm(idAnnotation,idTerm,idUser,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
    assert 200 == result.code

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,idUser,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
    assert 404 == result.code

    result = AnnotationTermAPI.undo()
    assert 200 == result.code

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
    assert 200 == result.code

    result = AnnotationTermAPI.redo()
    assert 200 == result.code

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
    assert 404 == result.code

  }

  void testDeleteAnnotationTermNotExist() {
    def result = AnnotationTermAPI.deleteAnnotationTerm(-99,-99,-99,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
    assert 404 == result.code
  }
}
