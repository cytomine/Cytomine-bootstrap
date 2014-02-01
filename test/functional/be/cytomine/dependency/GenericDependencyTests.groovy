package be.cytomine.dependency

import be.cytomine.DependencyController

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class GenericDependencyTests {

    void testMissingDeleteMethodDependency() {
        def controller = new DependencyController()
        controller.checkDependance()
    }


}
