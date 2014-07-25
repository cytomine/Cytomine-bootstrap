package be.cytomine.api.search

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.InvalidRequestException
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.utils.SearchFilter
import be.cytomine.utils.SearchOperator
import org.restapidoc.annotation.*
import org.restapidoc.pojo.RestApiParamType

/**
 * A search engine (v2)
 */
@RestApi(name = "search services", description = "Methods for searching domain (v2)")
class SearchEngineController extends RestController {

    def searchEngineService

    def imageInstanceService

    //Faire REQ ID avec INTERSECT => OK

    //Supporter le ""

    //limiter a des mots d'au moins 3 lettres

    //limiter a max 3 mots


    def result() {
        def allType = ["domain","property","description"]
        def allDomain = ["project","annotation","image"]
        def words = params.get("expr").split(",").toList()
        def ids = params.get("ids").split(",").collect{Long.parseLong(it)}
        def finalList = searchEngineService.search2(allType,allDomain,words,null,ids)
        responseSuccess(finalList)
    }

    def search() {
        def allType = ["domain","property","description"]
        def allDomain = ["project","annotation","image"]
        def words = params.get("expr").split(",").toList()
        def finalList = searchEngineService.search(allType,allDomain,words,"id","desc",null,"AND")
        responseSuccess(finalList)
    }

    public String redirectToGoToURL() {
        //http://localhost:8080/searchEngine/buildGotoLink?className=be.cytomine.project.Project&id=57
        String className = params.get('className')
        Long id = params.long('id')
        String url = null
        if(className==Project.class.name) {
            url = UrlApi.getDashboardURL(57)
        } else if(className==ImageInstance.class.name) {
            url = UrlApi.getBrowseImageInstanceURL(ImageInstance.read(id).project.id,id)
        } else if(className==UserAnnotation.class.name || className==AlgoAnnotation.class.name || className==ReviewedAnnotation.class.name) {
            AnnotationDomain domain = AnnotationDomain.getAnnotationDomain(id)
            url = UrlApi.getAnnotationURL(domain.project.id,domain.image.id,domain.id)
        }
        redirect(url: url)

    }

    public String redirectToImageURL() {
        //http://localhost:8080/searchEngine/buildGotoLink?className=be.cytomine.project.Project&id=57
        //http://localhost:8080/searchEngine/redirectToImageURL?className=be.cytomine.image.ImageInstance&id=14697772&max=64
        String className = params.get('className')
        Long id = params.long('id')
        Long max = params.long("maxSize",256l)
        String url = null
        if(className==ImageInstance.class.name) {
            url = UrlApi.getThumbImage(ImageInstance.read(id).baseImage.id,max)
        } else if(className==AbstractImage.class.name) {
            url = UrlApi.getThumbImage(AbstractImage.read(id).id,max)
        }else if(className==Project.class.name) {
            List<ImageInstance> images = imageInstanceService.list(Project.read(id))
            images = images.sort {it.id}
            if(!images.isEmpty()) {
                url = UrlApi.getThumbImage(images.first().baseImage.id,max)
            }
        } else if(className==UserAnnotation.class.name || className==AlgoAnnotation.class.name || className==ReviewedAnnotation.class.name) {
            url = UrlApi.getAnnotationCropWithAnnotationId(id,max)
        }

        if(!url) {
           url = "images/cytomine.jpg"
        }
        redirect(url: url)
    }
}
