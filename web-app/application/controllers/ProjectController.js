
var ProjectController = Backbone.Controller.extend({

       routes: {
          "project"     :   "project"
       },

       project : function() {
          var self = this;
          if (!this.view) {
             window.app.models.ontologies.fetch({
                    success : function (ontologies, response) {
                       window.app.models.projects.fetch({
                              success : function (collection, response) {
                                 self.view = new ProjectView({
                                        model : collection,
                                        el:$("#warehouse > .project"),
                                        container : window.app.view.components.warehouse
                                     }).render();

                                 self.view.container.views.project = self.view;
                                 self.view.container.show(self.view, "#warehouse > .sidebar", "project");
                                 window.app.view.showComponent(window.app.view.components.warehouse);
                              }});
                    }});
          }
          else {
             this.view.container.show(this.view, "#warehouse > .sidebar", "project");
             window.app.view.showComponent(window.app.view.components.warehouse);
          }
       }
    });