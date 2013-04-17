package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.image.multidim.ImageGroup
import be.cytomine.image.multidim.ImageSequence
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ImageGroupAPI
import be.cytomine.test.http.ImageSequenceAPI
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 9:11
 * To change this template use File | Settings | File Templates.
 */
class ImageSequenceTests {
    
    void testGetImageSequence() {
        def result = ImageSequenceAPI.show(BasicInstanceBuilder.getImageSequence().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }    

    void testListImageSequenceByImageGroup() {

        def result = ImageSequenceAPI.list(BasicInstanceBuilder.getImageSequence().imageGroup.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert json.collection.size()>=1

        result = ImageSequenceAPI.list(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


    void testListByGroup() {
        def dataSet = BasicInstanceBuilder.getMultiDimensionalDataSet(["A","B","C"],["1","2","3"],["R","G","B"])
        def result = ImageSequenceAPI.list(dataSet.first().imageGroup.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert json.collection.size()==27


        result = ImageSequenceAPI.list(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


    void testGetByImage() {
        def dataSet = BasicInstanceBuilder.getMultiDimensionalDataSet(["A","B","C"],["1","2","3"],["R","G","B"])
        def result = ImageSequenceAPI.get(dataSet.first().image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject


        result = ImageSequenceAPI.list(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testPossibilitiesByImage() {
        def dataSet = BasicInstanceBuilder.getMultiDimensionalDataSet(["A","B","C"],["1","2","3"],["R","G","B"])
        def result = ImageSequenceAPI.getSequenceInfo(dataSet.last().image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        println json
        assert json instanceof JSONObject
        assert json.zStack.join(',').equals("0,1,2")
        assert json.time.join(',').equals("0,1,2")
        assert json.channel.join(',').equals("0,1,2")


        result = ImageSequenceAPI.list(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testGetSpecificIndex() {
        def dataSet = BasicInstanceBuilder.getMultiDimensionalDataSet(["A","B","C"],["1","2","3"],["R","G","B"])
        def group = dataSet.last().imageGroup
        def result = ImageSequenceAPI.get(group.id,0,0,0,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        ImageInstance image = ImageInstance.read(json.image)
        assert image.baseImage.filename.startsWith("A-1-R")

        result = ImageSequenceAPI.get(group.id,1,0,0,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(result.data)
        image = ImageInstance.read(json.image)
        assert image.baseImage.filename.startsWith("B-1-R")

        result = ImageSequenceAPI.get(group.id,1,2,2,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(result.data)
        image = ImageInstance.read(json.image)
        assert image.baseImage.filename.startsWith("B-3-B")


        result = ImageSequenceAPI.get(group.id,1,5,2,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testAddImageSequenceCorrect() {

        def result = ImageSequenceAPI.create(BasicInstanceBuilder.getImageSequenceNotExist(false).encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        ImageSequence image = result.data
        Long idImage = image.id

        result = ImageSequenceAPI.show(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ImageSequenceAPI.undo()
        assert 200 == result.code

        result = ImageSequenceAPI.show(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = ImageSequenceAPI.redo()
        assert 200 == result.code

        result = ImageSequenceAPI.show(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

    }

    void testEditImageSequence() {

        def image = BasicInstanceBuilder.getImageSequence()
        def data = UpdateData.createUpdateSet(image,[channel: [0,1],zStack: [0,10],time: [0,100]])

        def result = ImageSequenceAPI.update(image.id, data.postData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idImageSequence = json.imagesequence.id
        def showResult = ImageSequenceAPI.show(idImageSequence, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)

        showResult = ImageSequenceAPI.undo()
        assert 200==showResult.code

        showResult = ImageSequenceAPI.show(idImageSequence, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)

        BasicInstanceBuilder.compare(data.mapOld, json)

        showResult = ImageSequenceAPI.redo()
        assert 200==showResult.code

        showResult = ImageSequenceAPI.show(idImageSequence, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)
    }

    void testDeleteImageSequence() {
        def imageSequenceToDelete = BasicInstanceBuilder.getImageSequenceNotExist()
        assert imageSequenceToDelete.save(flush: true) != null
        def idImage = imageSequenceToDelete.id

        def result = ImageSequenceAPI.delete(imageSequenceToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def showResult = ImageSequenceAPI.show(imageSequenceToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == showResult.code

        result = ImageSequenceAPI.undo()
        assert 200 == result.code

        result = ImageSequenceAPI.show(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ImageSequenceAPI.redo()
        assert 200 == result.code

        result = ImageSequenceAPI.show(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testDeleteImageSequenceNoExist() {
        def imageSequenceToDelete = BasicInstanceBuilder.getImageSequenceNotExist()
        def result = ImageSequenceAPI.delete(imageSequenceToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


    void testAddFullWorkflow() {
        //create image group
        def result = ImageGroupAPI.create(BasicInstanceBuilder.getImageGroupNotExist(BasicInstanceBuilder.getProject(),false).encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        ImageGroup imageGroup = result.data

        //add 27 image instance / sequence
        [0,1,2].each { z ->
            [0].each{ t ->
                [0,1,2].each{ c->
                    def imageSeq = BasicInstanceBuilder.getImageSequenceNotExist(false)
                    def image =  BasicInstanceBuilder.getImageInstanceNotExist(imageGroup.project,true)
                    imageSeq.zStack = z
                    imageSeq.time = t
                    imageSeq.channel = c
                    imageSeq.imageGroup = imageGroup
                    imageSeq.image = image
                    result = ImageSequenceAPI.create(imageSeq.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
                    assert 200 == result.code
                }
            }
        }

        result = ImageSequenceAPI.list(imageGroup.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert json.collection.size()==9

        result = ImageSequenceAPI.getSequenceInfo(dataSet.last().image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        println json
        assert json instanceof JSONObject
        assert json.zStack.join(',').equals("0,1,2")
        assert json.time.join(',').equals("0")
        assert json.channel.join(',').equals("0,1,2")
    }
}
