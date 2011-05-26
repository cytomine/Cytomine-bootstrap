/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:05
 * To change this template use File | Settings | File Templates.
 */

var OntologyController = Backbone.Controller.extend({



       routes: {
          "ontology"            :   "ontology",
          "ontology/:idOntology"           :   "ontology"
       },

       ontology : function(idOntology) {
          var self = this;
          console.log("OntologyController:"+idOntology)
          if (!self.view) {
             console.log("Ontology controller");
             window.app.models.ontologies.fetch({
                    success : function (collection, response) {
                       self.view = new OntologyView({
                              model : collection,
                              el:$("#warehouse > .ontology"),
                              container : window.app.view.components.warehouse,
                              idOntology : idOntology //selected ontology
                           }).render();
                       self.view.container.views.ontology = self.view;

                       self.view.container.show(self.view, "#warehouse > .sidebar", "ontology");
                       window.app.view.showComponent(window.app.view.components.warehouse);
                    }
                 });



          }


       }


    });
