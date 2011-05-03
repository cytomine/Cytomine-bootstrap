/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:05
 * To change this template use File | Settings | File Templates.
 */

var OntologyController = Backbone.Controller.extend({



    routes: {
        "ontology"            :   "ontology"
    },

    ontology : function() {
        if (!this.view) {

            this.view = new OntologyView({
                model : window.app.models.ontologies,
                el:$("#warehouse > .ontology"),
                container : window.app.view.components.warehouse
            }).render();

            this.view.container.views.ontology = this.view;
        }

        this.view.container.show(this.view, "#warehouse > .sidebar", "ontology");
        window.app.view.showComponent(window.app.view.components.warehouse);
    }


});
