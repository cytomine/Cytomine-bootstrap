package be.cytomine

import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ProjectAPI
import be.cytomine.test.http.TermAPI
import be.cytomine.test.http.UserAPI
import be.cytomine.utils.JSONUtils
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import grails.util.Holders
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 10/02/11
 * Time: 9:31
 * To change this template use File | Settings | File Templates.
 */
class CustomUITests {

    void testGlobalUI() {

        def global = Holders.getGrailsApplication().config.cytomine.customUI.global;
        println "***********"
        println global
        println "***********"

        global = [
                dashboard: ["ALL"],
                project: ["ALL"],
                ontology: ["ROLE_USER","ROLE_ADMIN"],
                storage : ["ROLE_USER","ROLE_ADMIN"],
                activity : ["ROLE_USER","ROLE_ADMIN"],
                feedback : ["ROLE_USER","ROLE_ADMIN"],
                explore : ["ALL"],
                admin : ["ROLE_ADMIN"],
                help : ["ALL"]
        ]

        println "***********"
        println global
        println "***********"


        User guest = BasicInstanceBuilder.getGhest("testGlobalUIGuest","password")
        User user = BasicInstanceBuilder.getUser("testGlobalUIUser","password")
        User admin = BasicInstanceBuilder.getSuperAdmin("testGlobalUIAdmin","password")

        def json
        def result
        result = UserAPI.retrieveCustomUIGlobal(guest.username,"password")
        assert 200==result.code
        json = JSON.parse(result.data)
        assert true == json.dashboard
        assert true == json.project
        assert false == json.ontology
        assert false == json.storage
        assert false == json.activity
        assert false == json.feedback
        assert true == json.explore
        assert false == json.admin
        assert true == json.help

        result = UserAPI.retrieveCustomUIGlobal(user.username,"password")
        assert 200==result.code
        json = JSON.parse(result.data)
        assert true == json.dashboard
        assert true == json.project
        assert true == json.ontology
        assert true == json.storage
        assert true == json.activity
        assert true == json.feedback
        assert true == json.explore
        assert false == json.admin
        assert true == json.help

        result = UserAPI.retrieveCustomUIGlobal(admin.username,"password")
        assert 200==result.code
        json = JSON.parse(result.data)
        assert true == json.dashboard
        assert true == json.project
        assert true == json.ontology
        assert true == json.storage
        assert true == json.activity
        assert true == json.feedback
        assert true == json.explore
        assert true == json.admin
        assert true == json.help
    }


    void testCustomUIProject() {

        Project project = BasicInstanceBuilder.getProjectNotExist(true)

        def result = UserAPI.retrieveCustomUIProject(project.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200==result.code
        def json = JSON.parse(result.data)
        assert true == json["project-annotations-tab"]["ADMIN_PROJECT"]
        assert false == json["project-jobs-tab"]["GUEST_PROJECT"]
        assert false == json["project-configuration-tab"]["GUEST_PROJECT"]
        assert false == json["project-configuration-tab"]["USER_PROJECT"]

        json["project-annotations-tab"]["ADMIN_PROJECT"] = false
        json["project-jobs-tab"]["GUEST_PROJECT"] = true
        json["project-configuration-tab"]["GUEST_PROJECT"] = true
        json["project-configuration-tab"]["USER_PROJECT"] = true


        result = UserAPI.setCustomUIProject(project.id,json.toString(),Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200==result.code
        println result.data
        def value = JSON.parse(result.data)
        assert false == value["project-annotations-tab"]["ADMIN_PROJECT"]
        assert true == value["project-jobs-tab"]["GUEST_PROJECT"]
        assert true == value["project-configuration-tab"]["GUEST_PROJECT"]
        assert true == value["project-configuration-tab"]["USER_PROJECT"]


        result = UserAPI.retrieveCustomUIProject(project.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200==result.code
        json = JSON.parse(result.data)
        assert false == json["project-annotations-tab"]["ADMIN_PROJECT"]
        assert true == json["project-jobs-tab"]["GUEST_PROJECT"]
        assert true == json["project-configuration-tab"]["GUEST_PROJECT"]
        assert true == json["project-configuration-tab"]["USER_PROJECT"]

        json["project-configuration-tab"]["GUEST_PROJECT"] = false

        result = UserAPI.setCustomUIProject(project.id,json.toString(),Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200==result.code
        println result.data
        value = JSON.parse(result.data)
        assert false == value["project-annotations-tab"]["ADMIN_PROJECT"]
        assert true == value["project-jobs-tab"]["GUEST_PROJECT"]
        assert false == value["project-configuration-tab"]["GUEST_PROJECT"]
        assert true == value["project-configuration-tab"]["USER_PROJECT"]

        result = UserAPI.retrieveCustomUIProject(project.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200==result.code
        json = JSON.parse(result.data)
        assert false == json["project-annotations-tab"]["ADMIN_PROJECT"]
        assert true == json["project-jobs-tab"]["GUEST_PROJECT"]
        assert false == json["project-configuration-tab"]["GUEST_PROJECT"]
        assert true == json["project-configuration-tab"]["USER_PROJECT"]
    }


    void testCustomUIProjectFlag() {

        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        User projectGuest = BasicInstanceBuilder.getGhest("testCustomUIProjectFlagGuest","password")
        User projectUser = BasicInstanceBuilder.getUser("testCustomUIProjectFlagUser","password")
        User projectAdmin = BasicInstanceBuilder.getSuperAdmin("testCustomUIProjectFlagAdmin","password")

        ProjectAPI.addUserProject(project.id,projectGuest.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        ProjectAPI.addUserProject(project.id,projectUser.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        ProjectAPI.addAdminProject(project.id,projectAdmin.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)


        Holders.getGrailsApplication().config.cytomine.customUI.project = [
                "project-annotations-tab":["ADMIN_PROJECT":true,"USER_PROJECT":true,"GUEST_PROJECT":true],
                "project-properties-tab":["ADMIN_PROJECT":false,"USER_PROJECT":true,"GUEST_PROJECT":true],
                "project-jobs-tab":["ADMIN_PROJECT":true,"USER_PROJECT":true,"GUEST_PROJECT":false],
                "project-configuration-tab":["ADMIN_PROJECT":true,"USER_PROJECT":false,"GUEST_PROJECT":false]
        ]

        //no config by now, check if default config is ok
        def json
        def result
        result = UserAPI.retrieveCustomUIProjectFlag(project.id,projectAdmin.username,"password")
        assert 200==result.code
        json = JSON.parse(result.data)
        assert true == json["project-annotations-tab"]
        assert false == json["project-properties-tab"]
        assert true == json["project-jobs-tab"]
        assert true == json["project-configuration-tab"]

        result = UserAPI.retrieveCustomUIProjectFlag(project.id,projectUser.username,"password")
        assert 200==result.code
        json = JSON.parse(result.data)
        assert true == json["project-annotations-tab"]
        assert true == json["project-properties-tab"]
        assert true == json["project-jobs-tab"]
        assert false == json["project-configuration-tab"]

        result = UserAPI.retrieveCustomUIProjectFlag(project.id,projectGuest.username,"password")
        assert 200==result.code
        json = JSON.parse(result.data)
        assert true == json["project-annotations-tab"]
        assert true == json["project-properties-tab"]
        assert false == json["project-jobs-tab"]
        assert false == json["project-configuration-tab"]




        //we set a new config. we hide property tab for everyone and show the job tab for everyone
        def jsonConfig = ["project-annotations-tab":["ADMIN_PROJECT":true,"USER_PROJECT":true,"GUEST_PROJECT":true],
                "project-properties-tab":["ADMIN_PROJECT":false,"USER_PROJECT":false,"GUEST_PROJECT":false],
                "project-jobs-tab":["ADMIN_PROJECT":true,"USER_PROJECT":true,"GUEST_PROJECT":true],
                "project-configuration-tab":["ADMIN_PROJECT":true,"USER_PROJECT":false,"GUEST_PROJECT":false]
        ]

        result = UserAPI.setCustomUIProject(project.id,(jsonConfig as JSON).t,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200==result.code

        result = UserAPI.retrieveCustomUIProjectFlag(project.id,projectAdmin.username,"password")
        assert 200==result.code
        json = JSON.parse(result.data)
        assert true == json["project-annotations-tab"]
        assert false == json["project-properties-tab"]
        assert true == json["project-jobs-tab"]
        assert true == json["project-configuration-tab"]

        result = UserAPI.retrieveCustomUIProjectFlag(project.id,projectUser.username,"password")
        assert 200==result.code
        json = JSON.parse(result.data)
        assert true == json["project-annotations-tab"]
        assert false == json["project-properties-tab"]
        assert true == json["project-jobs-tab"]
        assert false == json["project-configuration-tab"]

        result = UserAPI.retrieveCustomUIProjectFlag(project.id,projectGuest.username,"password")
        assert 200==result.code
        json = JSON.parse(result.data)
        assert true == json["project-annotations-tab"]
        assert false == json["project-properties-tab"]
        assert true == json["project-jobs-tab"]
        assert false == json["project-configuration-tab"]


    }


    //{
//    "project-annotations-tab": true,
//    "project-configuration-tab": true,
//    "project-jobs-tab": false,
//    "project-properties-tab": true
//}
//


   // void


//cytomine.customUI.project = [
//    "project-annotations-tab":,
//    "project-properties-tab":["ADMIN_PROJECT":true,"USER_PROJECT":true,"GUEST_PROJECT":true],
//    "project-jobs-tab":["ADMIN_PROJECT":true,"USER_PROJECT":true,"GUEST_PROJECT":false],
//    "project-configuration-tab":["ADMIN_PROJECT":true,"USER_PROJECT":false,"GUEST_PROJECT":false]
//    ]

//    "project-annotations-tab":{"ADMIN_PROJECT":true,"USER_PROJECT":true,"ROLE_USER":true,"ROLE_GUEST":true},"project-properties-tab":{"ADMIN_PROJECT":true,"USER_PROJECT":true,"ROLE_USER":true,"ROLE_GUEST":true},"project-jobs-tab":{"ADMIN_PROJECT":true,"USER_PROJECT":true,"ROLE_USER":true,"ROLE_GUEST":true},"project-configuration-tab":{"ADMIN_PROJECT":true,"USER_PROJECT":true,"ROLE_USER":true,"ROLE_GUEST":true}}
}
