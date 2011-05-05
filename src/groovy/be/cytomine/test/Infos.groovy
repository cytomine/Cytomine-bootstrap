package be.cytomine.test

import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 10/02/11
 * Time: 9:34
 * To change this template use File | Settings | File Templates.
 */
class Infos {

  public static String CYTOMINEURL = ConfigurationHolder.config.grails.serverURL + "/"

  public static String GOODLOGIN = "lrollus"
  public static String GOODPASSWORD = 'password'

  public static String BADLOGIN = 'badlogin'
  public static String BADPASSWORD = 'badpassword'

  public static String UNDOURL = "command/undo"
  public static String REDOURL = "command/redo"

  public static String BEGINTRANSACT = "transaction/begin"
  public static String ENDTRANSACT = "transaction/end"


}
