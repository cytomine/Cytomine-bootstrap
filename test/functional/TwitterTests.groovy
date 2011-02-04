
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 4/02/11
 * Time: 14:18
 * To change this template use File | Settings | File Templates.
 */
class TwitterTests extends functionaltestplugin.FunctionalTestCase {
  void testSearch() {
    println "***********************************testSearch"
    get('http://localhost:8080/cytomine-web/')
    println "***********************************search"
    //click "Recherche Google"
    println "***********************************assertStatus"
    assertStatus 200
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
