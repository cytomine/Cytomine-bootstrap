
var ProjectController = Backbone.Controller.extend({

   routes: {
      "project"     :   "project"
   },

   initView : function(callback) {
      var self = this;

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

                  if (_.isFunction(callback)) callback.call();
               }});
         }});
   },

   project : function() {
      var self = this;
      if (!this.view) {
         this.initView(function(){self.view.container.show(self.view, "#warehouse > .sidebar", "project");window.app.view.showComponent(window.app.view.components.warehouse);});
      }
      self.view.container.show(self.view, "#warehouse > .sidebar", "project");
      window.app.view.showComponent(window.app.view.components.warehouse);
   }
});