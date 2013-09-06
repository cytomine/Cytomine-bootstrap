package be.cytomine.utils

import be.cytomine.api.RestController

class NewsController extends RestController {

    def newsService

    static scaffold = News


    def listNews = {
        responseSuccess(newsService.list())
    }
}
