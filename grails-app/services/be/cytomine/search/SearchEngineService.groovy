package be.cytomine.search

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.search.engine.AbstractImageSearch
import be.cytomine.search.engine.AlgoAnnotationSearch
import be.cytomine.search.engine.EngineSearch
import be.cytomine.search.engine.ImageInstanceSearch
import be.cytomine.search.engine.ProjectSearch
import be.cytomine.search.engine.ResultSearch
import be.cytomine.search.engine.ReviewedAnnotationSearch
import be.cytomine.search.engine.UserAnnotationSearch
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import groovy.sql.Sql
import static grails.async.Promises.*

/**
 * New version for searchEngine
 *
 */
class SearchEngineService extends ModelService {

    def dataSource
    def cytomineService
    def currentRoleServiceProxy

    static Map<String, String> className = [
            "project": [Project.class.name],
            "image": [ImageInstance.class.name, AbstractImage.class.name],
            "annotation": [UserAnnotation.class.name, AlgoAnnotation.class.name, ReviewedAnnotation.class.name]
    ]


    public
    def search2(List<String> attributes, List<String> domainType, List<String> words, List<Long> idProject, List<Long> ids) {
        List<ResultSearch> results = []
        checkConstraint(words)
        if (ids != null && ids.isEmpty()) {
            throw new WrongArgumentException("There is no result!")
        }

        String req = buildSearchRequest(attributes, domainType, words, idProject, true, ids)
        req = req + "\nORDER BY id desc"

        def sql = new Sql(dataSource)

        println "################################"
        println req
        println "################################"

        long lastDomainId = -1
        sql.eachRow(req) {
            Long id = it[0]
            String className = it[1]
            String value = it[2]
            String type = it[3] //property, description,...
            String name = it[4]
            if (lastDomainId != id) {
                results << [id: id, className: className, url: getGoToURL(id,className),name: name, matching: [[value: value, type: type]]]
            } else {
                results.last().matching.add([value: value, type: type])
            }
            lastDomainId = it.id
        }
        return results
    }

    private static String getGoToURL(Long id, String className) {
        String url = null
        if (className == Project.class.name) {
            url = UrlApi.getDashboardURL(id)
        } else if (className == ImageInstance.class.name) {
            url = UrlApi.getBrowseImageInstanceURL(ImageInstance.read(id).project.id, id)
        } else if (className == UserAnnotation.class.name || className == AlgoAnnotation.class.name || className == ReviewedAnnotation.class.name) {
            AnnotationDomain domain = AnnotationDomain.getAnnotationDomain(id)
            url = UrlApi.getAnnotationURL(domain.project.id, domain.image.id, domain.id)
        }
        return url
    }

    public def search(List<String> attributes, List<String> domainType, List<String> words, String order = "id", String sort = "desc", List<Long> idProject, String op) {
        List<ResultSearch> results = []
        checkConstraint(words)
        String req
        if (op.equals("OR")) {
            req = buildSearchRequest(attributes, domainType, words, idProject, false, null)
        } else {
            //AND
            if (words.isEmpty()) {
                throw new WrongArgumentException("Min 1 word!")
            }
            List<String> requestParts = []
            println "words=$words"
            words.each {
                println "doFirstRequest:${it}"
                requestParts << "(" + buildSearchRequest(attributes, domainType, [it], idProject, false, null) + ")"
            }
            println "requestParts=${requestParts.size()}"
            req = requestParts.join("\nINTERSECT\n")
        }
        req = req + "\nORDER BY $order $sort"

        def sql = new Sql(dataSource)

        println "################################"
        println req
        println "################################"

        sql.eachRow(req) {
            results << [id: it[0], className: it[1]]
        }
        return results
    }

    private void checkConstraint(List<String> words) {
        if (words.isEmpty()) {
            throw new WrongArgumentException("Min 1 word!")
        }
        println "words1=${words}"
        if (words.size() > 5) {
            throw new WrongArgumentException("Max 5 words!")
        }
        if (words.find { it.size() < 3 }) {
            throw new WrongArgumentException("Each words must have at least 3 characters!")
        }

        if (words.find { it.contains("*") || it.contains("%") || it.contains("_") }) {
            throw new WrongArgumentException("Character *, % or _ are not allowed!")
        }
    }

    public String buildSearchRequest(List<String> attributes, List<String> domainType, List<String> words, List<Long> idProject, boolean extractMatchingValue = false, List<Long> ids = null) {
        List<String> requestParts = []
        List<String> domains = domainType.collect { convertToClassName(it) }

        SecUser currentUser = cytomineService.currentUser

        List<EngineSearch> engines = []

        if (domainType.contains("project")) {
            engines << new ProjectSearch(currentUser: currentUser, idProject: idProject, restrictedIds: ids, extractValue: extractMatchingValue)
        }
        if (domainType.contains("image")) {
            engines << new ImageInstanceSearch(currentUser: currentUser, idProject: idProject, restrictedIds: ids, extractValue: extractMatchingValue)
            engines << new AbstractImageSearch(currentUser: currentUser, idProject: idProject, restrictedIds: ids, extractValue: extractMatchingValue)
        }
        if (domainType.contains("annotation")) {
            engines << new UserAnnotationSearch(currentUser: currentUser, idProject: idProject, restrictedIds: ids, extractValue: extractMatchingValue)
            engines << new AlgoAnnotationSearch(currentUser: currentUser, idProject: idProject, restrictedIds: ids, extractValue: extractMatchingValue)
            engines << new ReviewedAnnotationSearch(currentUser: currentUser, idProject: idProject, restrictedIds: ids, extractValue: extractMatchingValue)
        }

        engines.each { engine ->
            println "${engine.class.name} ${requestParts.size()}"
            if (attributes.contains("domain")) {
                requestParts << engine.createRequestOnAttributes(words)
            }
            if (attributes.contains("property")) {
                requestParts << engine.createRequestOnProperty(words)
            }
            if (attributes.contains("description")) {
                requestParts << engine.createRequestOnDescription(words)
            }
        }

        requestParts = requestParts.findAll { it != "" }

        println requestParts.size()

        String req = requestParts.join("\nUNION\n")

        return req
    }

    private List<String> convertToClassName(String domainName) {
        List<String> classNames = className.get(domainName)
        if (!classNames) {
            throw new WrongArgumentException("Class $domainName is not supported!")
        }
        return classNames
    }
}
