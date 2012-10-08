package be.cytomine

import be.cytomine.test.Infos
import grails.plugins.selenium.SeleniumAware

@Mixin(SeleniumAware)
public class AbstractWebCytomine extends GroovyTestCase{

    void logIn() {
        logIn(Infos.GOODLOGIN,Infos.GOODPASSWORD)
    }

    void logIn(String login, String password) {
        logOut()
        selenium.open("/");
        selenium.waitForElementPresent("id=j_username")
        selenium.type("id=j_username", login)
        selenium.waitForElementPresent("id=j_password")
        selenium.type("id=j_password", password)
        selenium.click("id=submit-login");
        selenium.waitForTextPresent("Filters");
    }

    void logOut() {
        selenium.open("/logout");
    }

    void click(String id) {
        selenium.waitForElementPresent(id)
        selenium.click(id);
    }

    void waitForTextPresent(String text) {
        selenium.waitForTextPresent("regexpi:" + text);
    }

    void waitForTextPresentCaseSensitive(String text) {
        selenium.waitForTextPresent(text);
    }

    void waitForNotTextPresent(String text) {
        selenium.waitForNotTextPresent("regexpi:"+text)
    }

}
