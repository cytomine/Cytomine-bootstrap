
var ApplicationController = Backbone.Controller.extend({

       models : {},
       controllers : {},
       view : null,
       status : {},

       routes: {
          ""          :   "initialRoute",
          "explorer"  :   "explorer",
          "upload"    :   "upload",
          "admin"     :   "admin",
          "warehouse" :   "warehouse"
       },

       startup : function () {
          var self = this;

          var loadingView = new LoadingDialogView();
          //loadingView.render();
          //init collections
          self.models.images = new ImageCollection({project:undefined});
          self.models.imagesinstance = new ImageInstanceCollection({project:undefined});
          self.models.slides = new SlideCollection({project:undefined});
          self.models.users = new UserCollection({project:undefined});
          self.models.terms = new TermCollection({project:undefined});
          self.models.ontologies = new OntologyCollection();
          self.models.projects = new ProjectCollection({user:undefined});
          self.models.annotations = new AnnotationCollection({});

          //fetch models
          var modelsToPreload = [self.models.users];
          if (_.size(modelsToPreload) == 0) {
             self.modelFetched(0, 0, loadingView);
          } else {
             loadingView.initProgressBar();
             var nbModelFetched = 0;
             _.each(modelsToPreload, function(model){
                model.fetch({
                       success :  function(model, response) {
                          self.modelFetched(++nbModelFetched, _.size(modelsToPreload), loadingView);
                       }
                    });
             });
          }
       },

       modelFetched : function (cpt, expected, loadingView) {
          var step = 100 / expected;
          var value = cpt * step;
          //loadingView.progress(value);
          if (cpt == expected) {
             //loadingView.close();
             this.view.render(this.start);
          }
       },
       start : function () {
          window.app.controllers.image        = new ImageController();
          window.app.controllers.project      = new ProjectController();
          window.app.controllers.dashboard    = new DashboardController();
          window.app.controllers.browse       = new ExplorerController();
          window.app.controllers.ontology     = new OntologyController();
          window.app.controllers.upload       = new UploadController();
          window.app.controllers.command      = new CommandController();
          window.app.controllers.annotation   = new AnnotationController();
          Backbone.history.start();
       },
       initialize : function () {
          var self = this;
          self.view = new ApplicationView({
                 el: $('#app')
              });

          //init controllers
          self.controllers.auth         = new AuthController();

          require(["text!application/templates/ServerDownDialog.tpl.html"], function (serverDownTpl) {
             var serverDown = function(status) {
                $("#app").fadeOut('slow');
                var dialog = new ConfirmDialogView({
                       el:'#dialogs',
                       template : serverDownTpl,
                       dialogAttr : {
                          dialogID : "#server-down"
                       }
                    }).render();
             }

             var successcallback =  function (data) {
                self.status.version = data.version;
                self.status.user = {
                   id : data.user,
                   authenticated : data.authenticated
                }
                if (data.authenticated) {
                   self.startup();
                } else {
                   self.controllers.auth.login();
                }
             }

             var pingURL = 'server/ping';
             $.ajax({
                    url: pingURL,
                    type: 'GET',
                    success : successcallback
                 });


             self.status = new Status(pingURL, serverDown,
                 function () { //TO DO: HANDLE WHEN USER IS DISCONNECTED BY SERVER
                 }, 10000);

          });




       },

       explorer: function() {
          this.view.showComponent(this.view.components.explorer);
       },

       upload: function() {
          this.view.showComponent(this.view.components.upload);
       },

       admin: function() {
          this.view.showComponent(this.view.components.admin);
       },

       warehouse : function () {
          this.view.showComponent(this.view.components.warehouse);
       },

       initialRoute: function() {
          this.controllers.project.project();
       }



    });