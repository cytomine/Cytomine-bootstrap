package be.cytomine

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

import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AclAPI
import be.cytomine.test.http.OntologyAPI
import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class AclTests {



    void testGetACL() {
        User user = BasicInstanceBuilder.getUserNotExist(true)
        Project project = BasicInstanceBuilder.getProjectNotExist(true)

        def result = AclAPI.list(project.class.name, project.id, user.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert json.collection.isEmpty()

        assert 200 == AclAPI.create(project.class.name, project.id, user.id,"READ",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD).code
        assert 200 == AclAPI.create(project.class.name, project.id, user.id,"ADMINISTRATION",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD).code


        result = AclAPI.list(project.class.name, project.id, user.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert json.collection.size()==2
    }

    void testGetACLBadRequestWrongRequest() {
        def result = AclAPI.list("badname", null, null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testGetACLBadRequestObjectNotFound() {
        User user = BasicInstanceBuilder.getUserNotExist(true)
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        def result = AclAPI.list(project.class.name, 1, user.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void tesTAddACL() {
        User user = BasicInstanceBuilder.getUserNotExist(true)
        Project project = BasicInstanceBuilder.getProjectNotExist(true)

        assert (403 == ProjectAPI.show(project.id,user.username,"password").code)
        assert (403 == ProjectAPI.update(project.id,project.encodeAsJSON(),user.username,"password").code)
        assert (403 == OntologyAPI.show(project.ontology.id,user.username,"password").code)

        def result = AclAPI.create(project.class.name, project.id, user.id,"READ",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)

        assert (200 == ProjectAPI.show(project.id,user.username,"password").code)
        assert (403 == ProjectAPI.update(project.id,project.encodeAsJSON(),user.username,"password").code)
        assert (200 == OntologyAPI.show(project.ontology.id,user.username,"password").code)

        result = AclAPI.create(project.class.name, project.id, user.id,"ADMINISTRATION",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        assert (200 == ProjectAPI.show(project.id,user.username,"password").code)
        assert (200 == ProjectAPI.update(project.id,project.encodeAsJSON(),user.username,"password").code)

        assert 400 == AclAPI.create(project.class.name, project.id, user.id,"DELETE",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD).code
        assert 400 == AclAPI.create(project.class.name, project.id, user.id,"WRITE",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD).code

    }

    void tesTAddACLObjectNotFound() {
        User user = BasicInstanceBuilder.getUserNotExist(true)
        Project project = BasicInstanceBuilder.getProjectNotExist(true)

        def result = AclAPI.create(project.class.name, 1, user.id,"READ",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }


    void tesTAddACLDefault() {
        User user = BasicInstanceBuilder.getUserNotExist(true)
        Project project = BasicInstanceBuilder.getProjectNotExist(true)

        assert (403 == ProjectAPI.show(project.id,user.username,"password").code)
        assert (403 == ProjectAPI.update(project.id,project.encodeAsJSON(),user.username,"password").code)

        //give null
        def result = AclAPI.create(project.class.name, project.id, user.id,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)

        //check if null is READ
        assert (200 == ProjectAPI.show(project.id,user.username,"password").code)
        assert (403 == ProjectAPI.update(project.id,project.encodeAsJSON(),user.username,"password").code)

    }


    void testDeleteACL() {
        User user = BasicInstanceBuilder.getUserNotExist(true)
        Project project = BasicInstanceBuilder.getProjectNotExist(true)

        assert 200 == AclAPI.create(project.class.name, project.id, user.id,"READ",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD).code
        assert 200 == AclAPI.create(project.class.name, project.id, user.id,"ADMINISTRATION",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD).code

        assert (200 == ProjectAPI.show(project.id,user.username,"password").code)
        assert (200 == ProjectAPI.update(project.id,project.encodeAsJSON(),user.username,"password").code)

        def result = AclAPI.delete(project.class.name, project.id, user.id,"ADMINISTRATION",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)

        assert (200 == ProjectAPI.show(project.id,user.username,"password").code)
        assert (403 == ProjectAPI.update(project.id,project.encodeAsJSON(),user.username,"password").code)

        result = AclAPI.delete(project.class.name, project.id, user.id,"READ",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)

        assert (403 == ProjectAPI.show(project.id,user.username,"password").code)
        assert (403 == ProjectAPI.update(project.id,project.encodeAsJSON(),user.username,"password").code)

    }

    void testDeleteACLObjectNotFound() {
         User user = BasicInstanceBuilder.getUserNotExist(true)
         Project project = BasicInstanceBuilder.getProjectNotExist(true)

         def result = AclAPI.delete(project.class.name, 1, user.id,"ADMINISTRATION",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 404 == result.code

    }

    void testCriticalTestRestrictACL() {
        User user = BasicInstanceBuilder.getUserNotExist(true)
        Project project = BasicInstanceBuilder.getProjectNotExist(true)

        assert (403 == ProjectAPI.show(project.id,user.username,"password").code)
        assert (403 == ProjectAPI.update(project.id,project.encodeAsJSON(),user.username,"password").code)

        def result = AclAPI.create(project.class.name, project.id, user.id,"READ",user.username, "password")
        assert 403 == result.code

        assert (403 == ProjectAPI.show(project.id,user.username,"password").code)
        assert (403 == ProjectAPI.update(project.id,project.encodeAsJSON(),user.username,"password").code)

        assert 200 == AclAPI.create(project.class.name, project.id, user.id,"READ",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD).code
        assert 200 == AclAPI.create(project.class.name, project.id, user.id,"ADMINISTRATION",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD).code

        result = AclAPI.delete(project.class.name, project.id, user.id,"READ",user.username, "password")
        assert 403 == result.code

    }


    void testListAclAsAdmin() {
        User user = BasicInstanceBuilder.getUserNotExist(true)
        Project project = BasicInstanceBuilder.getProjectNotExist(true)



        def result = AclAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert result.code==200

        checkACLPresent(JSON.parse(result.data).collection,project,user,1,true)
        checkACLPresent(JSON.parse(result.data).collection,project,user,16,true)


        //add user to project
        assert AclAPI.create(project.class.name, project.id, user.id,"READ",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD).code ==200

        result = AclAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)

        checkACLPresent(JSON.parse(result.data).collection,project,user,1,false)

        assert AclAPI.create(project.class.name, project.id, user.id,"ADMINISTRATION",Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD).code ==200

        result = AclAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)

        checkACLPresent(JSON.parse(result.data).collection,project,user,16,false)
    }


    private static void checkACLPresent(def json, CytomineDomain domain, SecUser user, int mask, boolean notPresent = false) {

        println "Check for:"
        println "domainClassName = ${domain.class.name}"
        println "domainIdent = ${domain.id}"
        println "user = ${user.id}"
        println "mask = $mask"

        if(notPresent) {
            assert !json.find{
                it.domainClassName == domain.class.name && it.domainIdent == domain.id && it.mask == mask && it.idUser==user.id
            }
        } else {
            assert json.find{
                it.domainClassName == domain.class.name && it.domainIdent == domain.id && it.mask == mask && it.idUser==user.id
            }
        }

    }
}
