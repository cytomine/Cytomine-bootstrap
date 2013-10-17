package be.cytomine.security

import be.cytomine.ontology.Ontology
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.OntologyAPI
import be.cytomine.test.http.ProjectAPI
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class SimpleAuthentificationTests {

   void testSimpleAuth() {



       def result = ProjectAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
       println result
       assert result.code==200
       result = ProjectAPI.list("johndoe", "test")
       println result
       assert result.code==200
       def user = BasicInstanceBuilder.getUser("usertoto","password")
       result = ProjectAPI.list("usertoto", "password")
       println result
       assert result.code==200
   }
}
