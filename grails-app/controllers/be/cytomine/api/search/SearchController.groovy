package be.cytomine.api.search

import be.cytomine.Exception.InvalidRequestException
import be.cytomine.api.RestController
import be.cytomine.utils.SearchFilter
import be.cytomine.utils.SearchOperator
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.annotation.ApiResponseObject
import org.jsondoc.core.pojo.ApiParamType

/**
 * Created with IntelliJ IDEA.
 * User: pierre
 * Date: 15/04/13
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
@Api(name = "search services", description = "Methods for searching domain")
class SearchController extends RestController {

    def searchService

    @ApiMethodLight(description="Search for domain with a keywords list. The search will into properties value and description domain.")
    @ApiParams(params=[
        @ApiParam(name="keywords", type="list", paramType = ApiParamType.QUERY, description = "(Optional) List of keywords. If null, take all domain."),
        @ApiParam(name="operator", type="string", paramType = ApiParamType.QUERY, description = "(Optional) Operator between keyword (OR or AND). If null, take OR."),
        @ApiParam(name="filter", type="string", paramType = ApiParamType.QUERY, description = "(Optional) Domain type (PROJECT, IMAGE, ANNOTATION or ALL). If null, get all domain type"),
        @ApiParam(name="projects", type="string", paramType = ApiParamType.QUERY, description = "(Optional) Look only on domain from the project list id. If null, look into all project (available for the current user)"),
    ])
    @ApiResponseObject(objectIdentifier = "[search]")
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
        all.sort{-it.id}
        responseSuccess(all)
    }
}
