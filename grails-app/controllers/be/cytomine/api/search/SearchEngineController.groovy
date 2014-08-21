package be.cytomine.api.search

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.WrongArgumentException
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

    @RestApiMethod(description="Search for words and filters in Cytomine resources. This only retrieve id/class of matching domains.", listing = true)
    @RestApiResponseObject(objectIdentifier = "[search_engine_step1]")
    @RestApiParams(params=[
        @RestApiParam(name="expr", type="List<string>", paramType = RestApiParamType.QUERY, description = "List of words to search (AND search). Max 5 words and each words must have at least 3 characters."),
        @RestApiParam(name="projects", type="List<Long>", paramType = RestApiParamType.QUERY, description = "(Optional) Search only on domain from these projects"),
        @RestApiParam(name="domain", type="string", paramType = RestApiParamType.QUERY, description = "(Optional) Search only a king of domain groups (only project, only image,...)"),
        @RestApiParam(name="types", type="List<string>", paramType = RestApiParamType.QUERY, description = "(Optional) Search only on some attributes (only on domain itself, only on properties,...)"),
           ])
    def search() {
        def paramsValue = extractParams(params)
        def finalList = searchEngineService.search(paramsValue.types, paramsValue.domains, paramsValue.words, "id", "desc", paramsValue.projects, "AND")
        responseSuccess(finalList)
    }

    @RestApiMethod(description="Search for words and filters in Cytomine resources. This provides more data for a subset of results", listing = true)
    @RestApiResponseObject(objectIdentifier = "[search_engine_step2]")
        @RestApiParams(params=[
        @RestApiParam(name="ids", type="List<Long>", paramType = RestApiParamType.QUERY, description = "Search only on these domain ids"),
        @RestApiParam(name="expr", type="List<string>", paramType = RestApiParamType.QUERY, description = "List of words to search (AND search). Max 5 words and each words must have at least 3 characters."),
        @RestApiParam(name="projects", type="List<Long>", paramType = RestApiParamType.QUERY, description = "(Optional) Search only on domain from these projects"),
        @RestApiParam(name="domain", type="string", paramType = RestApiParamType.QUERY, description = "(Optional) Search only a king of domain groups (only project, only image,...)"),
        @RestApiParam(name="types", type="List<string>", paramType = RestApiParamType.QUERY, description = "(Optional) Search only on some attributes (only on domain itself, only on properties,...)"),
    ])
    def result() {
        def paramsValue = extractParams(params)
        def finalList = searchEngineService.results(paramsValue.types, paramsValue.domains, paramsValue.words, paramsValue.projects, paramsValue.ids)
        responseSuccess(finalList)
    }


    @RestApiMethod(description="Get a preview image for a domain. For image = thumb, for annotation = crop, for project = the last image...", listing = true)
    @RestApiResponseObject(objectIdentifier = "[search_engine_step2]")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="List<Long>", paramType = RestApiParamType.QUERY, description = "Search only on these domain ids"),
    @RestApiParam(name="className", type="List<string>", paramType = RestApiParamType.QUERY, description = "List of words to search (AND search). Max 5 words and each words must have at least 3 characters."),
    @RestApiParam(name="maxSize", type="int", paramType = RestApiParamType.QUERY, description = "(Optional) Max size of the image (default: 256)")])
    public String redirectToImageURL() {
        //http://localhost:8080/searchEngine/buildGotoLink?className=be.cytomine.project.Project&id=57
        //http://localhost:8080/searchEngine/redirectToImageURL?className=be.cytomine.image.ImageInstance&id=14697772&max=64
        String className = params.get('className')
        Long id = params.long('id')
        Long max = params.long("maxSize", 256l)
        String url = null
        if (className == ImageInstance.class.name) {
            url = UrlApi.getThumbImage(ImageInstance.read(id).baseImage.id, max)
        } else if (className == AbstractImage.class.name) {
            url = UrlApi.getThumbImage(AbstractImage.read(id).id, max)
        } else if (className == Project.class.name) {
            List<ImageInstance> images = imageInstanceService.list(Project.read(id))
            images = images.sort { it.id }
            if (!images.isEmpty()) {
                url = UrlApi.getThumbImage(images.last().baseImage.id, max)
            }
        } else if (className == UserAnnotation.class.name || className == AlgoAnnotation.class.name || className == ReviewedAnnotation.class.name) {
            url = UrlApi.getAnnotationCropWithAnnotationId(id, max)
        }

        if (!url) {
            url = "/images/cytomine.jpg"
        }
        redirect(url: url)
    }


    private def extractParams(params) {
        def words = []
        if (params.get("expr") != null && params.get("expr") != "") {
            words = params.get("expr").split(",").toList()
        }
        def ids = []
        if (params.get("ids") != null && params.get("ids") != "") {
            ids = params.get("ids").split(",").collect { Long.parseLong(it) }
        }
        def projects = null
        if (params.get("projects") != null && params.get("projects") != "") {
            projects = params.get("projects").split(",").collect { Long.parseLong(it) }
        }

        def allDomain = ["project", "annotation", "image"]
        if (params.get("domain") != null && params.get("domain") != "") {
            allDomain = [params.get("domain")]
        }

        def allType = ["domain", "property", "description"]
        if (params.get("types") != null && params.get("types") != "") {
            allType = [params.get("types")]
        }
        return [words: words, ids: ids, domains: allDomain, types: allType, projects: projects]
    }
}
