
var ExplorerController = Backbone.Controller.extend({

       tabs : null,

       routes: {
          "browse/:idProject/:idImage"   :   "browse",
          "browse/:idProject/:idImage/:idAnnotation"   :   "browse",
          "close"   :   "close"
       },

       initialize: function() {
       },

       initTabs : function() { //SHOULD BE OUTSIDE OF THIS CONTROLLER
          //create tabs if not exist
          if (this.tabs == null) {
             this.tabs = new ExplorerTabs({
                    el:$("#explorer > .browser"),
                    container : window.app.view.components.explorer
                 }).render();
          }
       },

       browse : function (idProject, idImage, idAnnotation) {
          var self = this;
          this.initTabs();

          if (window.app.status.currentProject == undefined) {//direct access -> create dashboard
             window.app.controllers.dashboard.dashboard(idProject);
          }

          var browseImageViewInitOptions = {};
          if (idAnnotation != undefined) {
             browseImageViewInitOptions.goToAnnotation = {value : idAnnotation};
          }

          self.tabs.addBrowseImageView(idImage, browseImageViewInitOptions);
          self.tabs.showTab(idImage);

          window.app.view.showComponent(self.tabs.container);
          self.showView();

       },
       closeAll : function () {
          if (this.tabs == null) return;

          this.tabs.closeAll();

          /*window.app.view.showComponent(this.tabs.container);*/
       },

       showView : function() {
          $("#explorer > .browser").show();
          $("#explorer > .noProject").hide();
          window.app.view.showComponent(window.app.view.components.explorer);
       }

    });