package be.cytomine

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 10/02/11
 * Time: 9:31
 * To change this template use File | Settings | File Templates.
 */
class AlgoAnnotationTermTests extends functionaltestplugin.FunctionalTestCase {

  void testAlgo() {

  }

//  void testGetAlgoAnnotationTermWithCredential() {
//    UserJob userJob = BasicInstance.createUserJob(User.findByUsername(Infos.GOODLOGIN))
//    def algoannotationTermToAdd = BasicInstance.createOrGetBasicAlgoAnnotationTerm(userJob)
//    log.info("get annotation:"+userJob.username+"|"+"password")
//    def result = AnnotationTermAPI.showAlgoAnnotationTerm(algoannotationTermToAdd.annotation.id,algoannotationTermToAdd.term.id,userJob.id,userJob.username,"password")
//    log.info("check response")
//    assertEquals(200,result.code)
//    def json = JSON.parse(result.data)
//    assert json instanceof JSONObject
//  }
//
//
//  void testAddAlgoAnnotationTermCorrect() {
//     UserJob userJob = BasicInstance.createUserJob(User.findByUsername(Infos.GOODLOGIN))
//     def algoannotationTermToAdd = BasicInstance.getBasicAlgoAnnotationTermNotExist(userJob)
//
//    log.info("create AnnotationTerm")
//    algoannotationTermToAdd.discard()
//    String jsonAnnotationTerm = algoannotationTermToAdd.encodeAsJSON()
//    def result = AnnotationTermAPI.createAlgoAnnotationTerm(jsonAnnotationTerm,userJob.username,"password")
//    log.info("check response")
//    assertEquals(200,result.code)
//    AlgoAnnotationTerm annotationTerm = result.data
//    Long idAnnotation = annotationTerm.annotation.id
//    Long idTerm = annotationTerm.term.id
//    log.info("check if object "+ annotationTerm.annotation.id +"/"+ annotationTerm.term.id +"exist in DB")
//
//    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,userJob.id,userJob.username,"password")
//    assertEquals(200,result.code)
//
//    log.info("test undo")
//    result = AnnotationTermAPI.undo()
//    assertEquals(200,result.code)
//
//    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,userJob.id,userJob.username,"password")
//    assertEquals(404,result.code)
//
//    log.info("test redo")
//    result = AnnotationTermAPI.redo()
//    assertEquals(200,result.code)
//
//    log.info("check if object "+ idAnnotation +"/"+ idTerm +" exist in DB")
//    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,userJob.id,userJob.username,"password")
//    assertEquals(200,result.code)
//
//  }

}
