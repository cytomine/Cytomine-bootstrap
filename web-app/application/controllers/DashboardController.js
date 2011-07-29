
var DashboardController = Backbone.Controller.extend({

   view : null,
   routes: {
      "tabs-images-:project"  : "images",
      "tabs-images-:project/:page"  : "imagespage",
      "tabs-thumbs-:project"  : "imagesthumbs",
      "tabs-imagesarray-:project"  : "imagesarray",
      "tabs-annotations-:project"  : "annotations",
      "tabs-dashboard-:project"  : "dashboard"
   },

   init : function (project, callback) {

      if (window.app.status.currentProject != undefined && window.app.status.currentProject != project) {

         this.destroyView();
         window.app.controllers.browse.closeAll();
         window.app.status.currentProject = undefined;

      }

      if (window.app.status.currentProject == undefined) {

         window.app.status.currentProject = project;
         window.app.controllers.browse.initTabs();
         if (this.view == null) this.createView(callback);
         this.showView();
      } else {
         callback.call();
         this.showView();
      }

   },
   images : function(project) {
      var self = this;
      var func = function() {
         self.view.refreshImages();
         var tabs = $("#explorer > .browser").children(".tabs");
         tabs.tabs("select", "#tabs-images-"+window.app.status.currentProject);
      }
      this.init(project, func);
   },
   imagespage : function(project,page) {
      var self = this;
      var func = function() {
         self.view.changeImagePage(page);
         self.view.showImages();
         var tabs = $("#explorer > .browser").children(".tabs");
         tabs.tabs("select", "#tabs-images-"+window.app.status.currentProject);
      }
      this.init(project, func);
   },
   imagesthumbs :  function(project) {
      var self = this;
      var func = function() {
         self.view.refreshImagesTabs();
         self.view.showImages();
         var tabs = $("#explorer > .browser").children(".tabs");
         tabs.tabs("select", "#tabs-images-"+window.app.status.currentProject);
      }
      this.init(project, func);
   },
   imagesarray : function(project) {
      var self = this;
      var func = function() {
         self.view.refreshImagesTabs();
         self.view.showImagesArray();
         var tabs = $("#explorer > .browser").children(".tabs");
         tabs.tabs("select", "#tabs-images-"+window.app.status.currentProject);
      }
      this.init(project, func);
   },
   annotations : function(project) {
      var self = this;
      var func = function() {
         self.view.refreshSelectedTerms();

         var tabs = $("#explorer > .browser").children(".tabs");
         tabs.tabs("select", "#tabs-annotations-"+window.app.status.currentProject);
      }
      this.init(project, func);
   },

   dashboard : function(project, callback) {
      var self = this;
      var func = function() {
         self.view.refresh();
         var tabs = $("#explorer > .browser").children(".tabs");
         tabs.tabs("select", "#tabs-dashboard-"+window.app.status.currentProject);
         if (callback != undefined) callback.call();
      }
      this.init(project, func);
   },

   createView : function (callback) {
      var tabs = $("#explorer > .browser").children(".tabs");
      var self = this;
      new ProjectModel({id : window.app.status.currentProject}).fetch({
         success : function(model, response) {
            window.app.status.currentProjectModel = model;
            self.view = new ProjectDashboardView({
               model : model,
               el: tabs,
               container : window.app.view.components.explorer
            }).render();
            callback.call();
         }
      });

   },

   destroyView : function() {
      this.view = null;
   },

   showView : function() {
      $("#explorer > .browser").show();
      $("#explorer > .noProject").hide();
      window.app.view.showComponent(window.app.view.components.explorer);
   }
});