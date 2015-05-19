package be.cytomine.api.ontology

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

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.security.SecUser
import be.cytomine.security.User
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller that handle link between an annotation and a term
 * This controller carry request for (user)annotationterm and algoannotationterm
 */
@RestApi(name = "annotation term service", description = "Methods for managing annotation term. Term added to an annotation by user or job.")
class RestAnnotationTermController extends RestController {

    def termService
    def annotationTermService
    def userAnnotationService
    def algoAnnotationService
    def algoAnnotationTermService
    def cytomineService
    def roiAnnotationService
    def reviewedAnnotationService

    def currentDomainName() {
        "annotation term or algo annotation term"
    }

    /**
     * List all term map with an annotation
     */
    @RestApiMethod(description="Get all annotationterm for an annotation", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="idannotation", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
        @RestApiParam(name="idUser", type="long", paramType = RestApiParamType.PATH,description = "(Optional) Only get term from this user id (may be a job)")
    ])
    def listTermByAnnotation() {

        if (params.idannotation == "undefined") {
            responseNotFound("Annotation Term", "Annotation", params.idannotation)
        }
        else {
            AnnotationDomain annotation = userAnnotationService.read(params.long('idannotation'))
            if (!annotation) {
                annotation = algoAnnotationService.read(params.long('idannotation'))
            }
            if (!annotation) {
                annotation = reviewedAnnotationService.read(params.long('idannotation'))
            }
            if (!annotation) {
                annotation = roiAnnotationService.read(params.long('idannotation'))
                responseSuccess([])
            }

            if (annotation && !params.idUser && annotation instanceof UserAnnotation) {
                responseSuccess(annotationTermService.list(annotation))
            } else if (annotation && !params.idUser && annotation instanceof AlgoAnnotation) {
                responseSuccess(algoAnnotationTermService.list(annotation))
            } else if (annotation && !params.idUser && annotation instanceof ReviewedAnnotation) {
                responseSuccess(reviewedAnnotationService.listTerms(annotation))
            } else if (annotation && params.idUser) {
                User user = User.read(params.long('idUser'))
                if (user) {
                    responseSuccess(termService.list(annotation, user))
                }
                else {
                    responseNotFound("Annotation Term", "User", params.idUser)
                }
            }
            else {
                responseNotFound("Annotation Term", "Annotation", params.idannotation)
            }
        }
    }

    /**
     * Get all term link with an annotation by all user except  params.idUser
     */
    @RestApiMethod(description="Get all annotationterm for an annotation except annotationterm from the user in param", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="idannotation", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
        @RestApiParam(name="idNotUser", type="long", paramType = RestApiParamType.PATH,description = "The user id")
    ])
    def listAnnotationTermByUserNot() {
        if (params.idannotation == "undefined") {
            responseNotFound("Annotation Term", "Annotation", params.idannotation)
        } else {
            UserAnnotation annotation = userAnnotationService.read(params.long('idannotation'))
            if (annotation != null && params.idNotUser) {
                User user = User.read(params.idNotUser)
                if (user) {
                    responseSuccess(annotationTermService.listNotUser(annotation, user))
                }else {
                    responseNotFound("Annotation Term", "User", params.idUser)
                }
            }
        }
    }

    @RestApiMethod(description="Get an annotation term")
    @RestApiParams(params=[
        @RestApiParam(name="idannotation", type="long", paramType = RestApiParamType.PATH, description = "The annotation id"),
        @RestApiParam(name="idterm", type="long", paramType = RestApiParamType.PATH, description = "The term id")
    ])
    def show() {
        AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(params.long('idannotation'))
        Term term = termService.read(params.long('idterm'))

        if (!annotation) {
            responseNotFound("Annotation", params.idannotation)
        }
        if (!term) {
            responseNotFound("Term", params.idterm)
        }
        else {
            if (params.idUser && SecUser.read(params.idUser)) {
                //user is set, get a specific annotation-term link from user
                if(!cytomineService.isUserAlgo()) {
                    def annoterm = annotationTermService.read(annotation, term, SecUser.read(params.idUser))
                    if (annoterm) responseSuccess(annoterm)
                    else responseNotFound("Annotation Term", "Term", "Annotation", "User", params.idterm, params.idannotation, params.idUser)
                } else {
                    def annoterm = algoAnnotationTermService.read(annotation, term, SecUser.read(params.idUser))
                    if (annoterm) responseSuccess(annoterm)
                    else responseNotFound("Algo Annotation Term", "Term", "Annotation", "User", params.idterm, params.idannotation, params.idUser)
                }
            } else {
                //user is not set, we will get the annotation-term from all user
                if(!cytomineService.isUserAlgo()) {
                    def annoterm = annotationTermService.read(annotation, term, null)
                    if (annoterm) responseSuccess(annoterm)
                    else responseNotFound("Annotation Term", "Term", "Annotation", params.idterm, params.idannotation)
                } else {
                     def annoterm = algoAnnotationTermService.read(annotation, term, null)
                    if (annoterm) responseSuccess(annoterm)
                    else responseNotFound("Algo Annotation Term", "Term", "Annotation", params.idterm, params.idannotation)
                }
            }
        }
    }

    @RestApiMethod(description="Add an annotation term")
    def add() {
        def json = request.JSON
        try {
            if(!cytomineService.isUserAlgo()) {
                if(!json.userannotation || !UserAnnotation.read(json.userannotation)) {
                    throw new WrongArgumentException("AnnotationTerm must have a valide userannotation:"+json.userannotation)
                }
                def result = annotationTermService.add(json)
                responseResult(result)
            } else {
                //TODO:: won't work if we add an annotation term to a algoannotation
                AnnotationDomain annotation = AlgoAnnotation.read(json.annotationIdent)
                if(!annotation) annotation = UserAnnotation.read(json.annotationIdent)
                if(!json.annotationIdent || !annotation) {
                    throw new WrongArgumentException("AlgoAnnotationTerm must have a valide annotation:"+json.annotationIdent)
                }
                def result = algoAnnotationTermService.add(json)
                responseResult(result)
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    @RestApiMethod(description="Delete an annotation term")
    @RestApiParams(params=[
        @RestApiParam(name="idannotation", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
        @RestApiParam(name="idterm", type="long", paramType = RestApiParamType.PATH,description = "The term id"),
    ])
    def delete() {
        if(cytomineService.isUserAlgo()) {
            throw new InvalidRequestException("A annotatation term from userJob cannot delete term")
        }
        def idUser = params.idUser!=null ? params.idUser : cytomineService.getCurrentUser().id
        def json = JSON.parse("{userannotation: $params.idannotation, term: $params.idterm, user: $idUser}")
        delete(annotationTermService, json,null)
    }

    /**
     * Add annotation-term for an annotation and delete all annotation-term that where already map with this annotation by this user
     */
    @RestApiMethod(description="Add an annotation term and delete all other term added to this annotation by this user")
    @RestApiParams(params=[
        @RestApiParam(name="idannotation", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
        @RestApiParam(name="idterm", type="long", paramType = RestApiParamType.PATH,description = "The term id"),
        @RestApiParam(name="clearForAll", type="boolean", paramType = RestApiParamType.QUERY,description = "Delete term for all users (no algo)"),
    ])
    def addWithDeletingOldTerm() {
        try {
            if(cytomineService.isUserAlgo()) {
                throw new InvalidRequestException("A userJob cannot delete user term from userannotation")
            }
            def result = annotationTermService.addWithDeletingOldTerm(params.idannotation, params.idterm, params.boolean('clearForAll'))
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        }
    }

}