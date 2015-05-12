package be.cytomine.test.http

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
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage AnnotationTerm to Cytomine with HTTP request during functional test
 */
class AnnotationTermAPI extends DomainAPI {

    static def listAnnotationTerm(Long idAnnotation,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/" + idAnnotation + "/term.json"
        return doGET(URL, username, password)
    }

    static def showAnnotationTerm(Long idAnnotation,Long idTerm, Long idUser,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/" + idAnnotation + "/term/"+ idTerm + (idUser? "/user/"+idUser : "")+".json"
        return doGET(URL, username, password)
    }

    static def listAnnotationTermByAnnotation(Long idAnnotation,String username, String password) {
        listAnnotationTermByAnnotation(idAnnotation,null,username,password)
    }

    static def listAnnotationTermByAnnotation(Long idAnnotation,Long idUser,String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation+"/term.json" + (idUser!=null? "?idUser=$idUser":"")
        return doGET(URL, username, password)
    }

    static def listAnnotationTermByUserNot(Long idAnnotation, Long idNotUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation+"/notuser/" + idNotUser + "/term.json"
        return doGET(URL, username, password)
    }

    static def createAnnotationTerm(String jsonAnnotationTerm, String username, String password) {
        createAnnotationTerm(jsonAnnotationTerm,username,password,false)
    }

    static def createAnnotationTerm(String jsonAnnotationTerm, String username, String password, boolean deleteOldTerm) {
        def json = JSON.parse(jsonAnnotationTerm);
        String URL
        if(deleteOldTerm)
            URL=Infos.CYTOMINEURL+"api/annotation/"+ json.userannotation +"/term/"+ json.term +"/clearBefore.json"
        else  URL=Infos.CYTOMINEURL+"api/annotation/"+ json.userannotation +"/term/"+ json.term +".json"
        def result = doPOST(URL,jsonAnnotationTerm,username,password)
        json = JSON.parse(result.data)
        def idAnnotationTerm = json?.annotationterm?.id
        return [data: AnnotationTerm.get(idAnnotationTerm), code: result.code]
    }

    static def deleteAnnotationTerm(def idAnnotation, def idTerm, def idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/" + idAnnotation + "/term/"+ idTerm +"/user/"+idUser+".json"
        return doDELETE(URL,username,password)
    }

}
