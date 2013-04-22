package be.cytomine.api.search

import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project

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
             operator = "OR"
        }
        if (!filter) {
            filter = "ALL"
        }

        if (keywords) {
            listKeyword = keywords.split(",")

            if (filter.equals("Project")) {
                responseSuccess(searchService.list(listKeyword, operator, filter))
            } else if (filter.equals("Image")) {
                responseSuccess(searchService.list(listKeyword, operator, filter))
            } else if (filter.equals("Annotation")) {
                responseSuccess(searchService.list(listKeyword, operator, filter))
            } else {
                // filter = ALL
                def all = []
                all.addAll(searchService.list(listKeyword, operator, "Project"))
                all.addAll(searchService.list(listKeyword, operator, "Image"))
                all.addAll(searchService.list(listKeyword, operator, "Annotation"))
                all.sort{-it.id}
                responseSuccess(all)
            }
        } else {
            responseNotFound("Search","Keywords", params.keywords)
        }


    }
}
