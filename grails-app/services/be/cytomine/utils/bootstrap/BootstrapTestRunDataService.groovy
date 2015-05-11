package be.cytomine.utils.bootstrap

import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.security.*
import be.cytomine.test.Infos
import groovy.sql.Sql

/**
 * This part of the code is run when testrun env is called.
 * Usefull for client testing.
 *
 * Do not remove this class :-)
 */
class BootstrapTestRunDataService {

    def bootstrapUtilsService
    def  bootstrapTestDataService
    def dataSource

    static USER_PUB_KEY = "61a338c5-20b5-43f7-8578-f0c9b16da9de"
    static USER_PRIV_KEY = "e5cc752e-f193-4ae4-b582-2c98447676b1"

    static long ID_USER1 = 101l

    static long ID_ROLE_USER = 201l
    static long ID_ROLE_ADMIN = 202l

    static long ID_ONTOLOGY = 301l

    static long ID_PROJECT = 401l

    static long ID_GROUP1 = 501l
    static long ID_GROUP2 = 502l

    static long ID_USER_GROUP11 = 601l
    static long ID_USER_GROUP12 = 602l
    static long ID_USER_GROUP21 = 603l

    def initData() {

        bootstrapTestDataService.recreateTableFromNotDomainClass()

        //ALTER SEQUENCE serial RESTART WITH 100;

        SecRole role1 = new SecRole(authority: "ROLE_USER")
        role1.setId(ID_ROLE_USER)
        role1.save(flush: true,failOnError: true)
        SecRole role2 = new SecRole(authority: "ROLE_ADMIN")
        role2.setId(ID_ROLE_ADMIN)
        role2.save(flush: true,failOnError: true)


        User user = new User(username: "lrollus",firstname:"Loïc",lastname: "Rollus",email: "lrollus@ulg.ac.be",color: "#FF0000",password: "toto",enabled: true)
        user.generateKeys()
        user.publicKey = USER_PUB_KEY
        user.privateKey = USER_PRIV_KEY
        user.setId(ID_USER1)
        user.save(flush:true,failOnError: true)
        user.publicKey = USER_PUB_KEY
        user.privateKey = USER_PRIV_KEY
        user.save(flush:true,failOnError: true)

        SecUserSecRole secUserSecRole1 = new SecUserSecRole(secUser: user,secRole: role1)
        secUserSecRole1.save(flush:true,failOnError: true)
        SecUserSecRole secUserSecRole2 = new SecUserSecRole(secUser: user,secRole: role2)
        secUserSecRole2.save(flush:true,failOnError: true)


        Ontology ontology = new Ontology(name:"ontology", user:user)
        ontology.setId(ID_ONTOLOGY)
        ontology.save(flush:true,failOnError: true)

        Project project = new Project(name:"project",ontology:ontology)
        project.setId(ID_PROJECT)
        project.save(flush:true,failOnError: true)


        Infos.addUserRight(user,project)
        Infos.addUserRight(user,ontology)


        Group group1 =new Group(name:"LBTD")
        group1.setId(ID_GROUP1)
        group1.save(flush:true)
        Group group2 =new Group(name:"ANAPATH")
        group2.setId(ID_GROUP1)
        group2.save(flush:true)

        UserGroup ug1 = new UserGroup(user:user,group: group1)
        ug1.setId(ID_USER_GROUP11)
        ug1.save(flush:true,failOnError: true)

        UserGroup ug2 = new UserGroup(user:user,group: group2)
        ug2.setId(ID_USER_GROUP12)
        ug2.save(flush:true,failOnError: true)

       new Sql(dataSource).execute("ALTER SEQUENCE hibernate_sequence RESTART WITH 100000;")


    }

}
