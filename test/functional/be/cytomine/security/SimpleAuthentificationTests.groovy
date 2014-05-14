package be.cytomine.security

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ProjectAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class SimpleAuthentificationTests {

   void testSimpleAuth() {



       def result = ProjectAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
       println result
       assert result.code==200
       def user = BasicInstanceBuilder.getUser("usertoto","password")
       result = ProjectAPI.list("usertoto", "password")
       println result
       assert result.code==200
   }
}
