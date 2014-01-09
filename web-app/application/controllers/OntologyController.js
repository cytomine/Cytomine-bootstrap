/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:05
 * To change this template use File | Settings | File Templates.
 */

var OntologyController = Backbone.Router.extend({
    routes: {
        "ontology": "ontology",
        "ontology/:idOntology": "ontology",
        "ontology/:idOntology/:idTerm": "ontology"
    },
    ontology: function () {
        this.ontology(0, 0, false);
    },
    ontology: function (idOntology) {
        this.ontology(idOntology, 0, false);
    },
    ontology: function (idOntology, idTerm) {
        this.ontology(idOntology, idTerm, false);
    },
    ontology: function (idOntology, idTerm, refresh) {
        var self = this;
        if (!self.view || refresh) {
            console.log("empty view");
            self.view = new OntologyView({
                el: $("#ontology"),
                container: window.app.view.components.ontology,
                idOntology: idOntology, //selected ontology
                idTerm: idTerm
            }).render();
            self.view.container.views.ontology = self.view;
            self.view.container.show(self.view, "#warehouse > .sidebar", "ontology");
            $("#warehouse-button").attr("href", "#ontology");
            window.app.view.showComponent(window.app.view.components.ontology);
        }
        else {
            self.view.container.show(self.view, "#warehouse > .sidebar", "ontology");
            window.app.view.showComponent(window.app.view.components.ontology);
            self.view.select(idOntology, idTerm);
        }
    }
});
