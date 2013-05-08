package be.cytomine.api.search

import be.cytomine.Exception.InvalidRequestException
import be.cytomine.api.RestController
import be.cytomine.utils.SearchFilter
import be.cytomine.utils.SearchOperator

/**
 * Created with IntelliJ IDEA.
 * User: pierre
 * Date: 15/04/13
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */

class SearchController extends RestController {

    def searchService

    def listResponse = {
        List<String> listKeyword

        def keywords = params.get('keywords')
        String operator = params.get('operator')
        String filter = params.get('filter')

        if (!keywords) {
            responseError(new InvalidRequestException("Please specify some keywords"))
        }

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

        listKeyword = keywords.split(",")

        def all = []
        if (filter.equals(SearchFilter.PROJECT) || filter.equals(SearchFilter.ALL))
            all.addAll(searchService.list(listKeyword, operator, SearchFilter.PROJECT))
        if (filter.equals(SearchFilter.IMAGE) || filter.equals(SearchFilter.ALL))
            all.addAll(searchService.list(listKeyword, operator, SearchFilter.IMAGE))
        if (filter.equals(SearchFilter.ANNOTATION) || filter.equals(SearchFilter.ALL))
            all.addAll(searchService.list(listKeyword, operator, SearchFilter.ANNOTATION))

        all.sort{-it.id}
        responseSuccess(all)
    }
}
