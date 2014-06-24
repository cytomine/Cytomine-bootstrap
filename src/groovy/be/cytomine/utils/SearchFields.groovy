package be.cytomine.utils

/**
 * Created with IntelliJ IDEA.
 * User: pierre
 * Date: 24/04/13
 * Time: 14:35
 * To change this template use File | Settings | File Templates.
 */

class SearchOperator {
    public static final String OR = "OR"
    public static final String AND = "AND"

    public static String[] getPossibleValues() {
        [AND, OR]
    }
}

class SearchFilter {
    public static final String ALL = "ALL"
    public static final String PROJECT = "PROJECT"
    public static final String ANNOTATION = "ANNOTATION"
    public static final String IMAGE = "IMAGE"
    public static final String ABSTRACTIMAGE = "ABSTRACTIMAGE"

    public static String[] getPossibleValues() {
        [ALL, PROJECT,ANNOTATION,IMAGE,ABSTRACTIMAGE]
    }
}