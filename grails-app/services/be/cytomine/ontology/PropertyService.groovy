package be.cytomine.ontology

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.AnnotationDomain
import be.cytomine.CytomineDomain
import be.cytomine.command.*
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.JSONUtils
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import com.vividsolutions.jts.geom.Geometry
import groovy.sql.Sql

import static org.springframework.security.acls.domain.BasePermission.READ

class PropertyService extends ModelService {

    static transactional = true
    def cytomineService
    def transactionService
    def dataSource
    def securityACLService

    def currentDomain() {
        return Property;
    }

    def list() {
        securityACLService.checkAdmin(cytomineService.currentUser)
        return Property.list()
    }

    def list(CytomineDomain cytomineDomain) {
        if(!cytomineDomain.class.name.contains("AbstractImage")) {
            securityACLService.check(cytomineDomain.container(),READ)
        }
        Property.findAllByDomainIdent(cytomineDomain.id)
    }

    List<String> listKeysForAnnotation(Project project, ImageInstance image, Boolean withUser) {
        if (project != null)
            securityACLService.check(project,READ)
        else
            securityACLService.check(image.container(),READ)

        String request = "SELECT DISTINCT p.key " +
                (withUser? ", ua.user_id " : "") +
                "FROM property as p, user_annotation as ua " +
                "WHERE p.domain_ident = ua.id " +
                (project? "AND ua.project_id = '"+ project.id + "' " : "") +
                (image? "AND ua.image_id = '"+ image.id + "' " : "") +
                "UNION " +
                "SELECT DISTINCT p1.key " +
                (withUser? ", aa.user_id " : "") +
                "FROM property as p1, algo_annotation as aa " +
                "WHERE p1.domain_ident = aa.id " +
                (project? "AND aa.project_id = '"+ project.id + "' " : "") +
                (image? "AND aa.image_id = '"+ image.id + "' " : "") +
                "UNION " +
                "SELECT DISTINCT p2.key " +
                (withUser? ", ra.user_id " : "") +
                "FROM property as p2, reviewed_annotation as ra " +
                "WHERE p2.domain_ident = ra.id " +
                (project? "AND ra.project_id = '"+ project.id + "' " : "") +
                (image? "AND ra.image_id = '"+ image.id + "' " : "")

        return  (withUser ? selectListKeyWithUser(request) : selectListkey(request))
    }

     List<String> listKeysForImageInstance(Project project) {
        if (project != null)
            securityACLService.check(project,READ)

        String request = "SELECT DISTINCT p.key " +
                "FROM property as p, image_instance as ii " +
                "WHERE p.domain_ident = ii.id " +
                "AND ii.project_id = "+ project.id;

        return selectListkey(request)
    }

    def listAnnotationCenterPosition(SecUser user, ImageInstance image, Geometry boundingbox, String key) {
        securityACLService.check(image.container(),READ)
        String request = "SELECT DISTINCT ua.id, ST_X(ST_CENTROID(ua.location)) as x,ST_Y(ST_CENTROID(ua.location)) as y, p.value " +
                "FROM user_annotation ua, property as p " +
                "WHERE p.domain_ident = ua.id " +
                "AND p.key = '"+ key + "' " +
                "AND ua.image_id = '"+ image.id +"' " +
                "AND ua.user_id = '"+ user.id +"' " +
                (boundingbox ? "AND ST_Intersects(ua.location,ST_GeometryFromText('" + boundingbox.toString() + "',0)) " :"") +
                "UNION " +
                "SELECT DISTINCT aa.id, ST_X(ST_CENTROID(aa.location)) as x,ST_Y(ST_CENTROID(aa.location)) as y, p.value " +
                "FROM algo_annotation aa, property as p " +
                "WHERE p.domain_ident = aa.id " +
                "AND p.key = '"+ key + "' " +
                "AND aa.image_id = '"+ image.id +"' " +
                "AND aa.user_id = '"+ user.id +"' " +
                (boundingbox ? "AND ST_Intersects(aa.location,ST_GeometryFromText('" + boundingbox.toString() + "',0)) " :"")

        return selectsql(request)
    }

    def read(def id) {
        def property = Property.read(id)
        if (property && !property.domainClassName.contains("AbstractImage")) {
            securityACLService.check(property.container(),READ)
        }
        property
    }

    def read(CytomineDomain domain, String key) {
        def cytomineDomain = Property.findAllByDomainIdentAndKey(domain.id,key)
        if (cytomineDomain && !cytomineDomain.isEmpty() && !cytomineDomain.contains("AbstractImage")) {
            securityACLService.check(cytomineDomain.first().container(),READ)
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
        if (domain != null && !domain.class.name.contains("AbstractImage")) {
            securityACLService.check(domain.container(),READ)
            securityACLService.checkReadOnly(domain.container())
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
        if(!ap.domainClassName.contains("AbstractImage")) {
            securityACLService.check(ap.container(),READ)
            securityACLService.checkReadOnly(ap.container())
        }

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
        if(!domain.domainClassName.contains("AbstractImage")) {
            securityACLService.check(domain.container(),READ)
            securityACLService.checkReadOnly(domain.container())
        }
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.key, domain.domainClassName, domain.domainIdent]
    }

    private def selectListkey(String request) {
        def data = []
        def sql = new Sql(dataSource)
        sql.eachRow(request) {
            String key = it[0]
            data << key
        }
        try {
            sql.close()
        }catch (Exception e) {}
        data
    }

    private def selectListKeyWithUser(String request) {
        def data = []
        def sql = new Sql(dataSource)
        sql.eachRow(request) {
            String key = it[0]
            String user = it[1]
            data << [key : key, userId : user]
        }
        try {
            sql.close()
        }catch (Exception e) {}
        data
    }

    private def selectsql(String request) {
        def data = []
        def sql = new Sql(dataSource)
         sql.eachRow(request) {

            long idAnnotation = it[0]
            String value = it[3]

            data << [idAnnotation: idAnnotation, x: it[1],y: it[2], value: value]
        }
        try {
            sql.close()
        }catch (Exception e) {}
        data
    }
}
