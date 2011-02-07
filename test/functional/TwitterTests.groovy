
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 4/02/11
 * Time: 14:18
 * To change this template use File | Settings | File Templates.
 */
class TwitterTests extends functionaltestplugin.FunctionalTestCase {
  void testSearch() {
    /*println "***********************************testSearch"
    get('http://localhost:8080/cytomine-web/')
    println "***********************************search"
    //click "Recherche Google"
    println "***********************************assertStatus"
    assertStatus 200*/


    /*post("http://localhost:8080/cytomine-web/j_spring_security_check")  {
      body {
        "j_username=stevben&j_password=password"
      }
    }
    assertStatus 200 */

    println "*********************************** GET ***********************************"

    get('http://localhost:8080/cytomine-web/login/auth')
    /* {
    headers['Cookie'] = cookies
  }  */

    //assertStatus 200

    form("test") {
      j_username = "stevben"
      j_password = "password"
      click "submit_login"
    }

    assertStatus 200

    println "cookies.size()=" + cookies.size()

    /*assertContentContains "search"
    println "searchForm"
    form('searchForm') {
      q = "#grails"
      click "Search"
    }
    println "assertStatus"
    assertStatus 200
    assertContentContains "#grails"  */
  }

}
