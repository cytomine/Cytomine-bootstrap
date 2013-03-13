package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.command.*
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import com.vividsolutions.jts.geom.Geometry
import groovy.sql.Sql

class AnnotationPropertyService extends ModelService {

    static transactional = true
    def cytomineService
    def transactionService
    def dataSource

    def currentDomain() {
        return AnnotationProperty;
    }

    def list() {
        AnnotationProperty.list()
    }

    def list(AnnotationDomain annotation) {
        AnnotationProperty.findAllByAnnotationIdent(annotation.id)
    }

    private List<String> listKeys(Project project, ImageInstance image) {

        String request = "SELECT DISTINCT ap.key " +
                "FROM annotation_property as ap, user_annotation as ua " +
                "WHERE ap.annotation_ident = ua.id " +
                (project? "AND ua.project_id = '"+ project.id + "' " : "") +
                (image? "AND ua.image_id = '"+ image.id + "' " : "") +
                "UNION " +
                "SELECT DISTINCT ap1.key " +
                "FROM annotation_property as ap1, algo_annotation as aa " +
                "WHERE ap1.annotation_ident = aa.id " +
                (project? "AND aa.project_id = '"+ project.id + "' " : "") +
                (image? "AND aa.image_id = '"+ image.id + "' " : "") +
                "UNION " +
                "SELECT DISTINCT ap2.key " +
                "FROM annotation_property as ap2, reviewed_annotation as ra " +
                "WHERE ap2.annotation_ident = ra.id " +
                (project? "AND ra.project_id = '"+ project.id + "' " : "") +
                (image? "AND ra.image_id = '"+ image.id + "' " : "")

        return selectListkey(request)
    }

    def listAnnotationCenterPosition(SecUser user, ImageInstance image, Geometry boundingbox, String key) {

        String request = "SELECT DISTINCT ua.id, ST_CENTROID(ua.location), ap.value " +
                "FROM user_annotation ua, annotation_property as ap " +
                "WHERE ap.annotation_ident = ua.id " +
                "AND ap.key = '"+ key + "' " +
                "AND ua.image_id = '"+ image.id +"' " +
                "AND ua.user_id = '"+ user.id +"' " +
                (boundingbox ? "AND ST_Intersects(ua.location,GeometryFromText('" + boundingbox.toString() + "',0)) " :"") +
                "UNION " +
                "SELECT DISTINCT aa.id, ST_CENTROID(aa.location), ap.value " +
                "FROM algo_annotation aa, annotation_property as ap " +
                "WHERE ap.annotation_ident = aa.id " +
                "AND ap.key = '"+ key + "' " +
                "AND aa.image_id = '"+ image.id +"' " +
                "AND aa.user_id = '"+ user.id +"' " +
                (boundingbox ? "AND ST_Intersects(aa.location,GeometryFromText('" + boundingbox.toString() + "',0)) " :"")

        return selectsql(request)
    }

    def read(def id) {
        def annotationProperty = AnnotationProperty.read(id)
        annotationProperty
    }

    def get(def id) {
        def annotationProperty = AnnotationProperty.get(id)
        annotationProperty
    }

    def read(AnnotationDomain annotation, String key) {
        AnnotationProperty.findByAnnotationIdentAndKey(annotation.id,key)
    }

    def add(def json) {
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
    def update(AnnotationProperty ap, def jsonNewData) {
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
    def delete(AnnotationProperty domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.key, domain.annotationIdent]
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
