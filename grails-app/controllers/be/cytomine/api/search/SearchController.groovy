package be.cytomine.api.search

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

import be.cytomine.Exception.InvalidRequestException
import be.cytomine.api.RestController
import be.cytomine.utils.SearchFilter
import be.cytomine.utils.SearchOperator
import org.restapidoc.annotation.*
import org.restapidoc.pojo.RestApiParamType

/**
 * Created with IntelliJ IDEA.
 * User: pierre
 * Date: 15/04/13
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
@RestApi(name = "search services", description = "Methods for searching domain")
class SearchController extends RestController {

    def searchService

    @RestApiMethod(description="Search for domain with a keywords list. The search will into properties value and description domain.")
    @RestApiParams(params=[
        @RestApiParam(name="keywords", type="list", paramType = RestApiParamType.QUERY, description = "(Optional) List of keywords. If null, take all domain."),
        @RestApiParam(name="operator", type="string", paramType = RestApiParamType.QUERY, description = "(Optional) Operator between keyword (OR or AND). If null, take OR."),
        @RestApiParam(name="filter", type="string", paramType = RestApiParamType.QUERY, description = "(Optional) Domain type (PROJECT, IMAGE, ANNOTATION or ALL). If null, get all domain type"),
        @RestApiParam(name="projects", type="string", paramType = RestApiParamType.QUERY, description = "(Optional) Look only on domain from the project list id. If null, look into all project (available for the current user)"),
    ])
    @RestApiResponseObject(objectIdentifier = "[search]")
    def listResponse() {
        List<String> listKeyword = []

        def keywords = params.get('keywords')
        String operator = params.get('operator')
        String filter = params.get('filter')
        def idsProjectStr = params.get('projects')

        def idsProject = null
        if(idsProjectStr) {
            idsProject = idsProjectStr.split(",")
        }

//        if (!keywords) {
//            responseError(new InvalidRequestException("Please specify some keywords"))
//        }

        if (operator) {
            operator = operator.toUpperCase()
            if (!SearchOperator.getPossibleValues().contains(operator)) {
                String possibleValues =  SearchOperator.getPossibleValues().join(",")
                responseError(new InvalidRequestException("Operator $operator does not exists. Possible value : $possibleValues"))
            }
        } else {
            operator = SearchOperator.OR
        }

        if (filter) {
            filter = filter.toUpperCase()
            if (!SearchFilter.getPossibleValues().contains(filter)) {
                String possibleValues =  SearchFilter.getPossibleValues().join(",")
                responseError(new InvalidRequestException("Filter $filter does not exists. Possible value : $possibleValues"))
            }
        } else {
            filter = SearchFilter.ALL
        }

        if(keywords) {
            listKeyword = keywords.split(",")
        }


        def all = []
        if (filter.equals(SearchFilter.PROJECT) || filter.equals(SearchFilter.ALL)) {
            all.addAll(searchService.list(listKeyword, operator, SearchFilter.PROJECT,idsProject))
        }

        if (filter.equals(SearchFilter.IMAGE) || filter.equals(SearchFilter.ALL)) {
            all.addAll(searchService.list(listKeyword, operator, SearchFilter.IMAGE,idsProject))
        }

        if (filter.equals(SearchFilter.ANNOTATION) || filter.equals(SearchFilter.ALL)) {
            all.addAll(searchService.list(listKeyword, operator, SearchFilter.ANNOTATION,idsProject))
        }
        if (filter.equals(SearchFilter.ABSTRACTIMAGE) || filter.equals(SearchFilter.ALL)) {
            all.addAll(searchService.list(listKeyword, operator, SearchFilter.ABSTRACTIMAGE,idsProject))
        }
        all.sort{-it.id}
        responseSuccess(all)
    }
}
