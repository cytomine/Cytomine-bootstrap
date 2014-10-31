package be.cytomine.search

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.JSONUtils
import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField


/**
 * A SearchEngineFilter is a re-usable filter for the SearchEngine
 * It contains the words searched and the restrictions (domain types or attributes) with others parameters
 */
@RestApiObject(name = "searchEngineFilter", description = "A SearchEngineFilter is a re-usable filter for the SearchEngine. It contains the words searched and the restrictions (domain types or attributes) with others parameters")
class SearchEngineFilter extends CytomineDomain implements Serializable {

    @RestApiObjectField(description = "The name of the filter")
    String name

    @RestApiObjectField(description = "The author of the filter")
    User user

    @RestApiObjectField(description = "json with the param of the search")
    String filters

    static constraints = {
        name(blank: false, unique: true)
        //words(nullable: false, minSize: 1)
        user(nullable: false)
    }
    static mapping = {
        id(generator: 'assigned', unique: true)
        sort "id"
        cache true
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        SearchEngineFilter.withNewSession {
            if(name) {
                SearchEngineFilter filter = SearchEngineFilter.findByName(name)
                if(filter!=null && (filter.id!=id))  {
                    throw new AlreadyExistException("SearchEngineFilter " + filter.name + " already exist!")
                }
            }
        }
    }


    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {

        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['name'] = domain?.name
        returnArray['user'] = domain?.user?.id
        returnArray['filters'] = domain?.filters

        return returnArray
    }

    /* Marshaller Helper fro user field */
    private static Integer userID(SearchEngineFilter filter) {
        return filter.getUser()?.id
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static SearchEngineFilter insertDataIntoDomain(def json,def domain = new SearchEngineFilter()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.name = JSONUtils.getJSONAttrStr(json, 'name')
        domain.user = JSONUtils.getJSONAttrDomain(json, "user", new SecUser(), true)
        domain.filters = JSONUtils.getJSONAttrStr(json, 'filters')
        return domain;
    }


    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return this;
    }


}
