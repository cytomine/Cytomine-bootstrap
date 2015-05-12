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

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.security.UserJob
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AlgoAnnotationAPI
import be.cytomine.utils.JSONUtils
import be.cytomine.utils.UpdateData
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class AlgoAnnotationTests  {

    void testGetAlgoAnnotationWithCredential() {
        def annotation = BasicInstanceBuilder.getAlgoAnnotation()
        def result = AlgoAnnotationAPI.show(annotation.id, Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    
    void testDownloadAlgoAnnotationDocument() {
        AlgoAnnotationTerm annotationTerm = BasicInstanceBuilder.getAlgoAnnotationTerm(true)
        def result = AlgoAnnotationAPI.downloadDocumentByProject(annotationTerm.retrieveAnnotationDomain().project.id,annotationTerm.retrieveAnnotationDomain().user.id,annotationTerm.term.id, annotationTerm.retrieveAnnotationDomain().image.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testAddAlgoAnnotationCorrect() {
        def annotationToAdd = BasicInstanceBuilder.getAlgoAnnotation()
        UserJob user = annotationToAdd.user
        try {Infos.addUserRight(user.user.username,annotationToAdd.project)} catch(Exception e) {println e}
        def result = AlgoAnnotationAPI.create(annotationToAdd.encodeAsJSON(),user.username, 'PasswordUserJob')
        assert 200 == result.code
        int idAnnotation = result.data.id

        result = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        assert 200 == result.code

        result = AlgoAnnotationAPI.undo(user.username, 'PasswordUserJob')
        assert 200 == result.code

        result = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        assert 404 == result.code

        result = AlgoAnnotationAPI.redo(user.username, 'PasswordUserJob')
        assert 200 == result.code

        result = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        assert 200 == result.code
    }

    void testAddAlgoAnnotationMultipleCorrect() {
        def annotationToAdd1 = BasicInstanceBuilder.getAlgoAnnotation()
        def annotationToAdd2 = BasicInstanceBuilder.getAlgoAnnotation()
        annotationToAdd2.image =  annotationToAdd1.image
        annotationToAdd2.project =  annotationToAdd1.project
        annotationToAdd2.save(flush: true)

        UserJob user1 = annotationToAdd1.user
        def annotations = []
        annotations << JSON.parse(annotationToAdd1.encodeAsJSON())
        annotations << JSON.parse(annotationToAdd2.encodeAsJSON())
        def result = AlgoAnnotationAPI.create(JSONUtils.toJSONString(annotations), user1.username, 'PasswordUserJob')
        assert 200 == result.code
    }

    void testAddAlgoAnnotationCorrectWithoutProject() {
        def annotationToAdd = BasicInstanceBuilder.getAlgoAnnotation()
        UserJob user = annotationToAdd.user

        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.project = null
        def result = AlgoAnnotationAPI.create(updateAnnotation.toString(), user.username, 'PasswordUserJob')
        assert 200 == result.code
    }

    void testAddAlgoAnnotationCorrectWithTerm() {
        def annotationToAdd = BasicInstanceBuilder.getAlgoAnnotation()
        UserJob user = annotationToAdd.user


        Long idTerm1 = BasicInstanceBuilder.getTerm().id
        Long idTerm2 = BasicInstanceBuilder.getAnotherBasicTerm().id

        def annotationWithTerm = JSON.parse((String)annotationToAdd.encodeAsJSON())
        annotationWithTerm.term = [idTerm1, idTerm2]

        def result = AlgoAnnotationAPI.create(JSONUtils.toJSONString(annotationWithTerm), user.username, 'PasswordUserJob')
        assert 200 == result.code
        int idAnnotation = result.data.id

        result = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        assert 200 == result.code

        result = AlgoAnnotationAPI.undo(user.username, 'PasswordUserJob')
        assert 200 == result.code

        result = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        assert 404 == result.code

        result = AlgoAnnotationAPI.redo(user.username, 'PasswordUserJob')
        assert 200 == result.code

        result = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        assert 200 == result.code
    }

    void testAddAlgoAnnotationWithoutProject() {
        def annotationToAdd = BasicInstanceBuilder.getAlgoAnnotation()
        UserJob user = annotationToAdd.user

        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.project = null

        def result = AlgoAnnotationAPI.create(updateAnnotation.toString(), user.username, 'PasswordUserJob')
        assert 200 == result.code
    }

    void testAddAlgoAnnotationBadGeom() {
        def annotationToAdd = BasicInstanceBuilder.getAlgoAnnotation()
        UserJob user = annotationToAdd.user

        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.location = 'POINT(BAD GEOMETRY)'

        Long idTerm1 = BasicInstanceBuilder.getTerm().id
        Long idTerm2 = BasicInstanceBuilder.getAnotherBasicTerm().id
        updateAnnotation.term = [idTerm1, idTerm2]

        def result = AlgoAnnotationAPI.create(updateAnnotation.toString(), user.username, 'PasswordUserJob')
        assert 400 == result.code
    }

    void testAddAlgoAnnotationBadGeomEmpty() {
        def annotationToAdd = BasicInstanceBuilder.getAlgoAnnotation()
        UserJob user = annotationToAdd.user

        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.location = 'POLYGON EMPTY'
        def result = AlgoAnnotationAPI.create(updateAnnotation.toString(), user.username, 'PasswordUserJob')
        assert 400 == result.code
    }

    void testAddAlgoAnnotationBadGeomNull() {
        def annotationToAdd = BasicInstanceBuilder.getAlgoAnnotation()
        UserJob user = annotationToAdd.user

        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.location = null
        def result = AlgoAnnotationAPI.create(updateAnnotation.toString(), user.username, 'PasswordUserJob')
        assert 400 == result.code
    }

    void testAddAlgoAnnotationImageNotExist() {
        def annotationToAdd = BasicInstanceBuilder.getAlgoAnnotation()
        UserJob user = annotationToAdd.user

        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.image = -99
        def result = AlgoAnnotationAPI.create(updateAnnotation.toString(), user.username, 'PasswordUserJob')
        assert 400 == result.code
    }

    void testEditAlgoAnnotation() {
        def aa = BasicInstanceBuilder.getAlgoAnnotation()
        def data = UpdateData.createUpdateSet(
                aa,
                [location: [new WKTReader().read("POLYGON ((2107 2160, 2047 2074, 1983 2168, 1983 2168, 2107 2160))"),new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168, 1983 2168))")]]
        )
        UserJob user = aa.user

        def result = AlgoAnnotationAPI.update(aa.id, data.postData,user.username, 'PasswordUserJob')
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idAnnotation = json.annotation.id

        def showResult = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)

        showResult = AlgoAnnotationAPI.undo(user.username, 'PasswordUserJob')
        assert 200 == result.code
        showResult = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        BasicInstanceBuilder.compare(data.mapOld, JSON.parse(showResult.data))

        showResult = AlgoAnnotationAPI.redo(user.username, 'PasswordUserJob')
        assert 200 == result.code
        showResult = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        BasicInstanceBuilder.compare(data.mapNew, JSON.parse(showResult.data))
    }

    void testEditAlgoAnnotationNotExist() {
        AlgoAnnotation annotationToAdd = BasicInstanceBuilder.getAlgoAnnotation()
        UserJob user = annotationToAdd.user

        AlgoAnnotation annotationToEdit = AlgoAnnotation.get(annotationToAdd.id)
        def jsonAnnotation = JSON.parse((String)annotationToEdit.encodeAsJSON())
        jsonAnnotation.id = "-99"
        def result = AlgoAnnotationAPI.update(annotationToAdd.id, jsonAnnotation.toString(), user.username,'PasswordUserJob')
        assert 404 == result.code
    }

    void testEditAlgoAnnotationWithBadGeometry() {
        AlgoAnnotation annotationToAdd = BasicInstanceBuilder.getAlgoAnnotation()
        UserJob user = annotationToAdd.user

        def jsonAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        jsonAnnotation.location = "POINT (BAD GEOMETRY)"
        def result = AlgoAnnotationAPI.update(annotationToAdd.id, jsonAnnotation.toString(), user.username, 'PasswordUserJob')
        assert 400 == result.code
    }

    void testDeleteAlgoAnnotation() {
        def annotationToDelete = BasicInstanceBuilder.getAlgoAnnotationNotExist()
        assert annotationToDelete.save(flush: true)  != null
        UserJob user = annotationToDelete.user

        def id = annotationToDelete.id
        def result = AlgoAnnotationAPI.delete(id, user.username, 'PasswordUserJob')
        assert 200 == result.code

        def showResult = AlgoAnnotationAPI.show(id, user.username,'PasswordUserJob')
        assert 404 == showResult.code

        result = AlgoAnnotationAPI.undo(user.username, 'PasswordUserJob')
        assert 200 == result.code

        result = AlgoAnnotationAPI.show(id, user.username,'PasswordUserJob')
        assert 200 == result.code

        result = AlgoAnnotationAPI.redo(user.username, 'PasswordUserJob')
        assert 200 == result.code

        result = AlgoAnnotationAPI.show(id, user.username,'PasswordUserJob')
        assert 404 == result.code
    }

    void testDeleteAlgoAnnotationNotExist() {
        def result = AlgoAnnotationAPI.delete(-99, Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testDeleteAlgoAnnotationWithData() {
        def annotTerm = BasicInstanceBuilder.getAlgoAnnotationTerm(true)
        UserJob user = annotTerm.retrieveAnnotationDomain().user

        def annotationToDelete = annotTerm.retrieveAnnotationDomain()
        def result = AlgoAnnotationAPI.delete(annotationToDelete.id,user.username,'PasswordUserJob')
        assert 200 == result.code
    }



    void testUnionAlgoAnnotationWithNotFound() {
        def a1 = BasicInstanceBuilder.getAlgoAnnotationTermNotExist()
        def result
        result = AlgoAnnotationAPI.union(-99,a1.retrieveAnnotationDomain().user.id,a1.term.id,10,20, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
        result = AlgoAnnotationAPI.union(a1.retrieveAnnotationDomain().image.id,-99,a1.term.id,10,20, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
        result = AlgoAnnotationAPI.union(a1.retrieveAnnotationDomain().image.id,a1.retrieveAnnotationDomain().user.id,-99,10,20, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testUnionAlgoAnnotationByProjectWithCredential() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist()
        image.save(flush: true)
        assert AlgoAnnotation.findAllByImage(image).size()==0

        def a1 = BasicInstanceBuilder.getAlgoAnnotationNotExist()

        a1.location = new WKTReader().read("POLYGON ((0 0, 0 5000, 10000 5000, 10000 0, 0 0))")
        a1.image = image
        a1.project = image.project
        assert a1.save(flush: true)  != null

        def a2 = BasicInstanceBuilder.getAlgoAnnotationNotExist()
        a2.location = new WKTReader().read("POLYGON ((0 5000, 10000 5000, 10000 10000, 0 10000, 0 5000))")
        a2.image = image
        a2.project = image.project
        assert a2.save(flush: true)  != null

        def at1 = BasicInstanceBuilder.getAlgoAnnotationTerm(a1.user.job,a1,a1.user)
        def at2 = BasicInstanceBuilder.getAlgoAnnotationTerm(a2.user.job,a2,a2.user)
        at2.term = at1.term
        at2.save(flush:true)

        assert AlgoAnnotation.findAllByImage(a1.image).size()==2

        def result = AlgoAnnotationAPI.union(a1.image.id,a1.user.id,a1.terms().first().id,10,20, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        assert AlgoAnnotation.findAllByImage(a1.image).size()==1
    }

    void testUnionAlgoAnnotationByProjectWithCredentialBufferNull() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist()
        image.save(flush: true)
        assert AlgoAnnotation.findAllByImage(image).size()==0

        def a1 = BasicInstanceBuilder.getAlgoAnnotationNotExist()

        a1.location = new WKTReader().read("POLYGON ((0 0, 0 5100, 10000 5100, 10000 0, 0 0))")
        a1.image = image
        a1.project = image.project
        assert a1.save(flush: true)  != null

        def a2 = BasicInstanceBuilder.getAlgoAnnotationNotExist()
        a2.location = new WKTReader().read("POLYGON ((0 5000, 10000 5000, 10000 10000, 0 10000, 0 5000))")
        a2.image = image
        a2.project = image.project
        assert a2.save(flush: true)  != null

        def at1 = BasicInstanceBuilder.getAlgoAnnotationTerm(a1.user.job,a1,a1.user)
        def at2 = BasicInstanceBuilder.getAlgoAnnotationTerm(a2.user.job,a2,a2.user)
        at2.term = at1.term
        at2.save(flush:true)

        assert AlgoAnnotation.findAllByImage(a1.image).size()==2

        def result = AlgoAnnotationAPI.union(a1.image.id,a1.user.id,a1.terms().first().id,10,null, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        assert AlgoAnnotation.findAllByImage(a1.image).size()==1
    }


    void testUnionAlgoAnnotationKeepGoodValue() {
         ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist()
         image.save(flush: true)
         assert AlgoAnnotation.findAllByImage(image).size()==0

         def a1 = BasicInstanceBuilder.getAlgoAnnotationNotExist()

         a1.location = new WKTReader().read("POLYGON ((0 0, 0 5100, 10000 5100, 10000 0, 0 0))")
         a1.image = image
         a1.project = image.project
         assert a1.save(flush: true)  != null

         def a2 = BasicInstanceBuilder.getAlgoAnnotationNotExist()
         a2.location = new WKTReader().read("POLYGON ((0 5000, 10000 5000, 10000 10000, 0 10000, 0 5000))")
         a2.image = image
         a2.project = image.project
         assert a2.save(flush: true)  != null

         def at1 = BasicInstanceBuilder.getAlgoAnnotationTerm(a1.user.job,a1,a1.user)
         def at2 = BasicInstanceBuilder.getAlgoAnnotationTerm(a2.user.job,a2,a2.user)
         at2.term = at1.term
         at2.save(flush:true)

         assert AlgoAnnotation.findAllByImage(a1.image).size()==2

         def result = AlgoAnnotationAPI.union(a1.image.id,a1.user.id,a1.terms().first().id,10,100, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code

         assert AlgoAnnotation.findAllByImage(a1.image).size()==1

         println  AlgoAnnotation.findAllByImage(a1.image).first().location

     }




    void testUnionAlgoAnnotationVeryBigAnnotationMustBeSimplified() {
         ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist()
         image.save(flush: true)
         assert AlgoAnnotation.findAllByImage(image).size()==0

         def a1 = BasicInstanceBuilder.getAlgoAnnotationNotExist()

         a1.location = new WKTReader().read("POLYGON ((14761 7489, 14765 7489, 14765 7495, 14761 7495, 14761 7489))")
         a1.image = image
         a1.project = image.project
         assert a1.save(flush: true)  != null

         def a2 = BasicInstanceBuilder.getAlgoAnnotationNotExist()
         a2.location = new WKTReader().read(new File('test/functional/be/cytomine/utils/very_big_annotation.txt').text)
         a2.image = image
         a2.project = image.project
         assert a2.save(flush: true)  != null

         def at1 = BasicInstanceBuilder.getAlgoAnnotationTerm(a1.user.job,a1,a1.user)
         def at2 = BasicInstanceBuilder.getAlgoAnnotationTerm(a2.user.job,a2,a2.user)
         at2.term = at1.term
         at2.save(flush:true)

         assert AlgoAnnotation.findAllByImage(a1.image).size()==2

         def result = AlgoAnnotationAPI.union(a1.image.id,a1.user.id,a1.terms().first().id,10,100, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code

         assert AlgoAnnotation.findAllByImage(a1.image).size()==1
         def annotationAlone = AlgoAnnotation.findAllByImage(a1.image).first()
         println  "NB POINTS END=" +annotationAlone.id + "=" + annotationAlone.location.getNumPoints()
         annotationAlone.refresh()
         println  "NB POINTS END=" +annotationAlone.id + "=" + annotationAlone.location.getNumPoints()

        println annotationAlone.location.toText()

     }

}
