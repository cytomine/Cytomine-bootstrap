package be.cytomine.api.search

import be.cytomine.api.RestController
import be.cytomine.utils.SearchEnum.Filter
import be.cytomine.utils.SearchEnum.Operator

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
        def operator = params.get('operator')
        def filter = params.get('filter')

        if (!operator) {
             operator = Operator.OR
        }
        if (!filter) {
            filter = Filter.ALL
        }

        if (keywords) {
            listKeyword = keywords.split(",")

            if (operator.equals("OR")) {
                operator = Operator.OR
            } else if (operator.equals("AND")) {
                operator = Operator.AND
            }

            if (filter.equals(Filter.PROJECT)) {
                responseSuccess(searchService.list(listKeyword, operator, filter))
            } else if (filter.equals(Filter.IMAGE)) {
                responseSuccess(searchService.list(listKeyword, operator, filter))
            } else if (filter.equals(Filter.ANNOTATION)) {
                responseSuccess(searchService.list(listKeyword, operator, filter))
            } else {
                // filter = Filter.ALL
                def all = []
                all.addAll(searchService.list(listKeyword, operator, Filter.PROJECT))
                all.addAll(searchService.list(listKeyword, operator, Filter.IMAGE))
                all.addAll(searchService.list(listKeyword, operator, Filter.ANNOTATION))
                all.sort{-it.id}
                responseSuccess(all)
            }
        } else {
            responseNotFound("Search","Keywords", params.keywords)
        }


    }
}
