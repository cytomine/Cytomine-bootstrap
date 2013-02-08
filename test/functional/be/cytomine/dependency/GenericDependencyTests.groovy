package be.cytomine.dependency

import be.cytomine.utils.Task

import be.cytomine.DependencyController

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class GenericDependencyTests {

    void testProjectDependency() {
        def controller = new DependencyController()
        controller.checkDependance()
    }


}
