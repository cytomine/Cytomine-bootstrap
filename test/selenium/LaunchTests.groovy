import grails.plugins.selenium.*

@Mixin(SeleniumAware)
class HelloWorldTests extends GroovyTestCase{

    void testCytomineTitle() {
        selenium.open("/");
        log.info "selenium.title="+selenium.title
        selenium.title
        assertTrue(selenium.title.equals("Cytomine"));
    }

    void testCytomineLogPanel() {
        selenium.open("/");
        selenium.waitForTextPresent("Sign in to Cytomine");
    }
}
