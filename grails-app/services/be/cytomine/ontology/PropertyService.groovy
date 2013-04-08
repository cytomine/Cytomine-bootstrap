package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.CytomineDomain
import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.JSONUtils
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import com.vividsolutions.jts.geom.Geometry
import groovy.sql.Sql

import static org.springframework.security.acls.domain.BasePermission.*

class PropertyService extends ModelService {

    static transactional = true
    def cytomineService
    def transactionService
    def dataSource

    def currentDomain() {
        return Property;
    }

    def list() {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        return Property.list()
    }

    def list(CytomineDomain cytomineDomain) {
        SecurityACL.check(cytomineDomain.container(),READ)
        Property.findAllByDomainIdent(cytomineDomain.id)
    }

    private List<String> listKeysForAnnotation(Project project, ImageInstance image) {
        if (project != null)
            SecurityACL.check(project,READ)
        else
            SecurityACL.check(image.container(),READ)

        String request = "SELECT DISTINCT p.key " +
                "FROM property as p, user_annotation as ua " +
                "WHERE p.domain_ident = ua.id " +
                (project? "AND ua.project_id = '"+ project.id + "' " : "") +
                (image? "AND ua.image_id = '"+ image.id + "' " : "") +
                "UNION " +
                "SELECT DISTINCT p1.key " +
                "FROM property as p1, algo_annotation as aa " +
                "WHERE p1.domain_ident = aa.id " +
                (project? "AND aa.project_id = '"+ project.id + "' " : "") +
                (image? "AND aa.image_id = '"+ image.id + "' " : "") +
                "UNION " +
                "SELECT DISTINCT p2.key " +
                "FROM property as p2, reviewed_annotation as ra " +
                "WHERE p2.domain_ident = ra.id " +
                (project? "AND ra.project_id = '"+ project.id + "' " : "") +
                (image? "AND ra.image_id = '"+ image.id + "' " : "")

        return selectListkey(request)
    }

    def listAnnotationCenterPosition(SecUser user, ImageInstance image, Geometry boundingbox, String key) {
        SecurityACL.check(image.container(),READ)
        String request = "SELECT DISTINCT ua.id, ST_CENTROID(ua.location), p.value " +
                "FROM user_annotation ua, property as p " +
                "WHERE p.domain_ident = ua.id " +
                "AND p.key = '"+ key + "' " +
                "AND ua.image_id = '"+ image.id +"' " +
                "AND ua.user_id = '"+ user.id +"' " +
                (boundingbox ? "AND ST_Intersects(ua.location,GeometryFromText('" + boundingbox.toString() + "',0)) " :"") +
                "UNION " +
                "SELECT DISTINCT aa.id, ST_CENTROID(aa.location), p.value " +
                "FROM algo_annotation aa, property as p " +
                "WHERE p.domain_ident = aa.id " +
                "AND p.key = '"+ key + "' " +
                "AND aa.image_id = '"+ image.id +"' " +
                "AND aa.user_id = '"+ user.id +"' " +
                (boundingbox ? "AND ST_Intersects(aa.location,GeometryFromText('" + boundingbox.toString() + "',0)) " :"")

        return selectsql(request)
    }

    def read(def id) {
        def property = Property.read(id)
        if (property) {
            SecurityACL.check(property.container(),READ)
        }
        property
    }

    def get(def id) {
        def property = Property.get(id)
        if (property) {
            SecurityACL.check(property.container(),READ)
        }
        property
    }

    def read(CytomineDomain domain, String key) {
        def cytomineDomain = Property.findByDomainIdentAndKey(domain.id,key)
        if (cytomineDomain) {
            SecurityACL.check(cytomineDomain.container(),READ)
        }
        cytomineDomain
    }

    def add(def json) {
        def domainClass = json.domainClassName
        CytomineDomain domain

        if(domainClass.contains("AnnotationDomain")) {
            domain = AnnotationDomain.getAnnotationDomain(json.domainIdent)
        } else {
            domain = Class.forName(domainClass, false, Thread.currentThread().contextClassLoader).read(JSONUtils.getJSONAttrLong(json,'domainIdent',0))
        }

        if (domain != null) {
            SecurityACL.check(domain.container(),READ)
        }

        SecUser currentUser = cytomineService.getCurrentUser()
        Command command = new AddCommand(user: currentUser)
        return executeCommand(command,null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(Property ap, def jsonNewData) {
        SecurityACL.check(ap.container(),READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        Command command = new EditCommand(user: currentUser)
        return executeCommand(command,ap,jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(Property domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.check(domain.container(),READ)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.key, domain.domainIdent]
    }

    private def selectListkey(String request) {
        def data = []
        new Sql(dataSource).eachRow(request) {
            String key = it[0]
            data << key
        }
        data
    }

    private def selectsql(String request) {
        def data = []
        new Sql(dataSource).eachRow(request) {

            long idAnnotation = it[0]
            String centre = it[1]
            String value = it[2]

            data << [idAnnotation: idAnnotation, centre: centre, value: value]
        }
        data
    }
}
