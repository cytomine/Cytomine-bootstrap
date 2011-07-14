
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
          //Start the history
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



    });var UploadController = Backbone.Controller.extend({
       initialized  : true,
       routes: {
       },
       upload : function() {
          if (this.initialized) return;
          /* init upload */

          this.initialized = true;
       }
    });

var AuthController = Backbone.Controller.extend({

    routes: {
    },

    login : function () {
        var loginView = new LoginDialogView({}).render();
    },
    logout : function () {
       var logoutView = new LogoutDialogView({}).render();
    },

    doLogin :  function () {
        var app = new ApplicationView(); //in order to use message function
        var data = $("#login-form").serialize(); //should be in LoginDIalogView
        $.ajax({
            url: 'j_spring_security_check',
            type: 'post',
            dataType : 'json',
            data : data,
            success : function(data){
                app.message("Welcome", "You are logged as " + data.fullname, "");
                $("#login-confirm").dialog("close"); //should be in LoginDIalogView
                window.app.status.user = {
                    authenticated : true,
                    id : data.id
                }
                window.app.startup();

            },
            error : function(data) {
                var resp = $.parseJSON(data.responseText);

                app.message("Error", resp.message, "error");
            }
        });
        return false;
    }
});
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
var DashboardController = Backbone.Controller.extend({

       view : null,
       routes: {
          "tabs-images-:project"  : "images",
          "tabs-imagestab-:project"  : "imagestab",
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
             self.view.selectTab(0);
          }
          this.init(project, func);


       },
       imagestab : function(project) {
          var self = this;
          var func = function() {
             self.view.refreshImagesTabs();
             var tabs = $("#explorer > .browser").children(".tabs");
             tabs.tabs("select", "#tabs-images-"+window.app.status.currentProject);
             self.view.selectTab(1);
          }
          this.init(project, func);
       },
       annotations : function(project) {
          var self = this;
          var func = function() {
             self.view.refreshSelectedTerms();
             self.view.selectTab(2);
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
var ImageController = Backbone.Controller.extend({



	routes: {
		"image"            :   "image",
		"image/p:page"     :   "image"
	},

	image : function(page) {
		if (!this.view) {
			this.view = new ImageView({
				page : page,
				model : window.app.models.images,
				el:$("#warehouse > .image"),
				container : window.app.view.components.warehouse
			}).render();

			this.view.container.views.image = this.view;
		}

		this.view.container.show(this.view, "#warehouse > .sidebar", "image");
        window.app.view.showComponent(window.app.view.components.warehouse);
	}


});
var ExplorerController = Backbone.Controller.extend({

       tabs : null,

       routes: {
          "tabs-image-:idProject-:idImage-:idAnnotation"   :   "browse",
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

          var createBrowseImageViewTab = function() {
             var browseImageViewInitOptions = {};
             if (idAnnotation != "") {
                browseImageViewInitOptions.goToAnnotation = {value : idAnnotation};
             }

             self.tabs.addBrowseImageView(idImage, browseImageViewInitOptions);
             /*self.tabs.showTab(idImage);*/
             var tabs = $("#explorer > .browser").children(".tabs");
             tabs.tabs("select", "#tabs-image-"+window.app.status.currentProject+"-"+idImage+"-");

             window.app.view.showComponent(self.tabs.container);
             self.showView();
          };

          if (window.app.status.currentProject == undefined) {//direct access -> create dashboard
             window.app.controllers.dashboard.dashboard(idProject, createBrowseImageViewTab);

             /*setTimeout(createBrowseImageViewTab, 0);*/
             return;
          }


          createBrowseImageViewTab();


       },
       closeAll : function () {
          if (this.tabs == null) return;

          this.tabs = null;
          $("#explorer > .browser").empty();
       },

       showView : function() {
          $("#explorer > .browser").show();
          $("#explorer > .noProject").hide();
          window.app.view.showComponent(window.app.view.components.explorer);
       }

    });/**
* Created by IntelliJ IDEA.
* User: lrollus
* Date: 7/04/11
* Time: 10:05
* To change this template use File | Settings | File Templates.
*/

var TermController = Backbone.Controller.extend({

	routes: {
		"term"            :   "term" ,
		"term/o:ontology"            :   "term"
	},

	term : function(ontology) {
		if (!this.view) {
			this.view = new TermView({
				model : window.app.models.terms,
				ontology : ontology,
				el:$("#explorer > .term"),
				container : window.app.view.components.explorer
			}).render();

			this.view.container.views.term = this.view;
		}

		this.view.container.show(this.view);
	}


});/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:05
 * To change this template use File | Settings | File Templates.
 */

var OntologyController = Backbone.Controller.extend({
       routes: {
          "ontology"                               :   "ontology",
          "ontology/:idOntology"                   :   "ontology",
          "ontology/:idOntology/:idTerm"           :   "ontology"
       },
       ontology : function() {
          this.ontology(0,0,false);
       },
       ontology : function(idOntology) {
          this.ontology(idOntology,0,false);
       },
       ontology : function(idOntology,idTerm) {
          this.ontology(idOntology,idTerm,false);
       },
       ontology : function(idOntology,idTerm,refresh) {
          var self = this;
          if (!self.view) {
             self.view = new OntologyView({
                    model : window.app.models.ontologies,
                    el:$("#warehouse > .ontology"),
                    container : window.app.view.components.warehouse,
                    idOntology : idOntology, //selected ontology
                    idTerm : idTerm
                 }).render();
             self.view.container.views.ontology = self.view;
             self.view.container.show(self.view, "#warehouse > .sidebar", "ontology");
             window.app.view.showComponent(window.app.view.components.warehouse);
             self.view.refresh(idOntology, idTerm);

          }
          else {
             self.view.container.show(self.view, "#warehouse > .sidebar", "ontology");
             window.app.view.showComponent(window.app.view.components.warehouse);

             if(refresh) {
                window.app.models.ontologies.fetch({
                       success : function (collection, response) {
                          self.view.select(idOntology)
                       }});
             }else {
                self.view.select(idOntology,idTerm);
             }



          }
       }
    });
var CommandController = Backbone.Controller.extend({
    undo : function() {
        var self = this;
        $.post('command/undo.json', {}, function(data) {
            
            
            _.each(data, function(undoElem){
                
                
                self.dispatch(undoElem.callback,undoElem.message,"Undo");
                
                if(undoElem.printMessage) {
                    
                    window.app.view.message("Undo", undoElem.message, "");
                }
            });

        }, "json");

    },

    redo : function () {
        var self = this;
        $.post('command/redo.json', {}, function(data) {
            
            
            _.each(data, function(redoElem){
                
                self.dispatch(redoElem.callback,redoElem.message, "Redo");
                if(redoElem.printMessage) window.app.view.message("Redo", redoElem.message, "");
            });
        }, "json");

    },

    dispatch : function(callback,message,operation) {
        

        if (!callback) return; //nothing to do
        
        /**
         * ANNOTATION
         */
        if (callback.method == "be.cytomine.AddAnnotationCommand") {

            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function(object) {
                
                return object.idImage == callback.imageID;
            });
            var image = tab.view;
            
            if (image == undefined) return; //tab is closed
            
            image.getUserLayer().annotationAdded(callback.annotationID);
            if(window.app.controllers.dashboard.view!=null)
                window.app.controllers.dashboard.view.refresh();
        } else if (callback.method == "be.cytomine.EditAnnotationCommand") {

            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function(object) {
                return object.idImage == callback.imageID;
            });
            var image = tab.view;
            if (image == undefined) return; //tab is closed
            image.getUserLayer().annotationUpdated(callback.annotationID);
            if(window.app.controllers.dashboard.view!=null)
                window.app.controllers.dashboard.view.refresh();
        } else if (callback.method == "be.cytomine.DeleteAnnotationCommand") {

            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function(object) {
                return object.idImage == callback.imageID;
            });
            var image = tab.view;
            
            
            if (image == undefined) return; //tab is closed
            
            image.getUserLayer().annotationRemoved(callback.annotationID);
              if(window.app.controllers.dashboard.view!=null)
                window.app.controllers.dashboard.view.refresh();
            /**
             * ANNOTATION TERM
             */
        } else if (callback.method == "be.cytomine.AddAnnotationTermCommand") {

            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function(object) {
                return object.idImage == callback.imageID;
            });
            var image = tab.view;
            if (image == undefined) return; //tab is closed
            image.getUserLayer().termAdded(callback.annotationID,callback.termID);
            if(window.app.controllers.dashboard.view!=null)
                window.app.controllers.dashboard.view.refresh();
        } else if (callback.method == "be.cytomine.DeleteAnnotationTermCommand") {

            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function(object) {
                return object.idImage == callback.imageID;
            });
            var image = tab.view;
            if (image == undefined) return; //tab is closed
            image.getUserLayer().termRemoved(callback.annotationID,callback.termID);
            if(window.app.controllers.dashboard.view!=null)
                window.app.controllers.dashboard.view.refresh();
        }

        /**
         * ONTOLOGY
         */
        else if (callback.method == "be.cytomine.AddOntologyCommand") {

            window.app.controllers.ontology.view.refresh(callback.ontologyID);
        } else if (callback.method == "be.cytomine.DeleteOntologyCommand") {

            window.app.controllers.ontology.view.refresh();
        } else if (callback.method == "be.cytomine.EditOntologyCommand") {

            window.app.controllers.ontology.view.refresh(callback.ontologyID);
        }
        /**
         * PROJECT
         */
        else if (callback.method == "be.cytomine.AddProjectCommand") {

            window.app.controllers.project.view.refresh();
        } else if (callback.method == "be.cytomine.DeleteProjectCommand") {

            window.app.controllers.project.view.refresh();
        } else if (callback.method == "be.cytomine.EditProjectCommand") {

            window.app.controllers.project.view.refresh();
        }
        /**
         * TERM
         */
        else if (callback.method == "be.cytomine.AddTermCommand") {

            window.app.controllers.ontology.view.refresh(callback.ontologyID);
        } else if (callback.method == "be.cytomine.DeleteTermCommand") {

            window.app.controllers.ontology.view.refresh(callback.ontologyID);
        } else if (callback.method == "be.cytomine.EditTermCommand") {

            window.app.controllers.ontology.view.refresh(callback.ontologyID);
        }

        else if (callback.method == "be.cytomine.AddImageInstanceCommand") {
            if(window.app.controllers.project.view!=null)
                window.app.controllers.project.view.refresh();
            if(window.app.controllers.dashboard.view!=null)
                window.app.controllers.dashboard.view.refresh();
        } else if (callback.method == "be.cytomine.DeleteImageInstanceCommand") {
            
            if(window.app.controllers.project.view!=null)
                window.app.controllers.project.view.refresh();
            if(window.app.controllers.dashboard.view!=null)
                window.app.controllers.dashboard.view.refresh();
        } else if (callback.method == "be.cytomine.EditImageInstanceCommand") {
            if(window.app.controllers.project.view!=null)
                window.app.controllers.project.view.refresh();
            if(window.app.controllers.dashboard.view!=null)
                window.app.controllers.dashboard.view.refresh();
        }

    }
});/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:05
 * To change this template use File | Settings | File Templates.
 */

var AnnotationController = Backbone.Controller.extend({

    routes: {
        "annotation"            :   "annotation",
        "annotation/:idAnnotation"           :   "annotation"
    },

    annotation : function(idAnnotation) {
        var self = this;
        
        if (!self.view) {
            
            window.app.models.images.fetch({
                success : function (collection, response) {


                    self.view = new AnnotationListView({
                        model : collection,
                        el:$("#warehouse > .annotation"),
                        container : window.app.view.components.warehouse,
                        idAnnotation : idAnnotation //selected annotation
                    }).render();
                    self.view.container.views.annotation = self.view;

                    self.view.container.show(self.view, "#warehouse > .sidebar", "annotation");
                    window.app.view.showComponent(window.app.view.components.warehouse);

                }
            });



        }


    }


});var ImageModel = Backbone.Model.extend({
	url : function() {
		var base = 'api/image';
		var format = '.json';
        if (this.isNew()) return base + format;
		return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
	}
});

// define our collection
var ImageCollection = Backbone.Collection.extend({
    model: ImageModel,
    url: function() {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/image.json";
        } else {
            return "api/currentuser/image.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
    }
});

var ImageInstanceModel = Backbone.Model.extend({
	url : function() {
        if(this.project == undefined && this.baseImage == undefined) {
            var base = 'api/imageinstance';
            var format = '.json';
            if (this.isNew()) return base + format;
            return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
        }
        else
        {
            return 'api/project/' + this.project +'/image/'+this.baseImage+'/imageinstance.json';
        }
	},
    initialize: function (options) {
        this.project = options.project;
        this.baseImage = options.baseImage;
    }
});

// define our collection
var ImageInstanceCollection = Backbone.Collection.extend({
    model: ImageModel,
    url: function() {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/imageinstance.json";
        } else {
            return "api/imageinstance.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
    }
});

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */
var TermModel = Backbone.Model.extend({

	url : function() {
		var base = 'api/term';
		var format = '.json';
        if (this.isNew()) return base + format;
		return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
	}
});

var AnnotationTermModel = Backbone.Model.extend({
	url : function() {
        if (this.term == null)
		    return 'api/annotation/' + this.annotation +'/term.json';
        else
            return 'api/annotation/' + this.annotation +'/term/'+this.term+'.json';
	},
    initialize: function (options) {
        this.annotation = options.annotation;
        this.term = options.term;
    }
});

var AnnotationTermCollection = Backbone.Collection.extend({
    model : TermModel,
	url : function() {
		return 'api/annotation/' + this.idAnnotation +'/term.json';
	},
    initialize: function (options) {
        this.idAnnotation = options.idAnnotation;

    }
});

var RelationTermModel = Backbone.Model.extend({
	url : function() {
        if (this.term == null)
		    return 'api/annotation/' + this.annotation +'/term.json';
        else
            return 'api/annotation/' + this.annotation +'/term/'+this.term+'.json';
	},
    initialize: function (options) {
        this.annotation = options.annotation;
        this.term = options.term;
    }
});

var RelationTermCollection = Backbone.Collection.extend({
    model : TermModel,
	url : function() {
		return 'api/annotation/' + this.idAnnotation +'/term.json';
	},
    initialize: function (options) {
        this.idAnnotation = options.idAnnotation;

    }
});



// define our collection
var TermCollection = Backbone.Collection.extend({
    model: TermModel,
    CLASS_NAME: "be.cytomine.ontology.Term",
	url : function() {
        if(this.idOntology==undefined && this.idAnnotation==undefined)
		    return 'api/term.json';
        else if(this.idOntology!=undefined && this.idAnnotation==undefined)
            return 'api/ontology/'+ this.idOntology + '/term.json';
        else if(this.idOntology!=undefined && this.idAnnotation!=undefined)
            return 'api/annotation/'+ this.idAnnotation + '/ontology/'+ this.idOntology + '/term.json';
	},
    initialize: function (options) {
        this.idOntology = options.idOntology;
        this.idAnnotation = options.idAnnotation;
        // something
    }
});
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 15:01
 * To change this template use File | Settings | File Templates.
 */
var OntologyModel = Backbone.Model.extend({
       url : function() {
          var base = 'api/ontology';
          var format = '.json';
          if (this.isNew()) return base + format;
          return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
       }
    });


// define our collection
var OntologyCollection = Backbone.Collection.extend({
       model: OntologyModel,
       CLASS_NAME: "be.cytomine.ontology.Ontology",
       url: 'api/currentuser/ontology.json',
       initialize: function () {
          // something
       },comparator : function(ontology) {
          return ontology.get("name");
       }
    });

var UserModel = Backbone.Model.extend({
    /*initialize: function(spec) {
        if (!spec || !spec.name || !spec.username) {
            throw "InvalidConstructArgs";
        }
    },

    validate: function(attrs) {
        if (attrs.name) {
            if (!_.isString(attrs.name) || attrs.name.length === 0) {
                return "Name must be a string with a length";
            }
        }
    },*/

	url : function() {
		var base = 'api/user';
		var format = '.json';
        if (this.isNew()) return base + format;
		return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
	},

   prettyName : function () {
      return this.get('firstname') + " " + this.get('lastname');
   }
});


// define our collection
var UserCollection = Backbone.Collection.extend({
    model: UserModel,

    url: function() {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/user.json";
        } else {
            return "api/user.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
    }
});

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/04/11
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
var ProjectModel = Backbone.Model.extend({

    url : function() {
        var base = 'api/project';
        var format = '.json';
        if (this.isNew()) return base + format;
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});

var ProjectUserModel = Backbone.Model.extend({
    url : function() {
        if (this.user == undefined) {
            return "api/project/" + this.project + "/user.json";
        }else {
            return "api/project/" + this.project + "/user/"+ this.user +".json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.user = options.user;
    }
});


var OntologyProjectModel = Backbone.Collection.extend({
       model: ProjectModel,
       url : function() {
          return "api/ontology/" + this.ontology + "/project.json";
       },
       initialize: function (options) {
          this.ontology = options.ontology;
       }
    });

// define our collection
var ProjectCollection = Backbone.Collection.extend({
    model: ProjectModel,

    url: function() {
        if (this.user != undefined) {
            return "api/user/" + this.user + "/project.json";
        }else if (this.ontology != undefined) {
            return "api/ontology/" + this.ontology + "/project.json";
        } else {
            return "api/currentuser/project.json";
        }
    },
    initialize: function (options) {
        this.user = options.user;
        this.ontology = options.ontology;
    },
    comparator : function (project) {
        return project.get("name");
    }
});
var AnnotationModel = Backbone.Model.extend({
       /*initialize: function(spec) {
        if (!spec || !spec.name || !spec.username) {
        throw "InvalidConstructArgs";
        }
        },

        validate: function(attrs) {
        if (attrs.name) {
        if (!_.isString(attrs.name) || attrs.name.length === 0) {
        return "Name must be a string with a length";
        }
        }
        },*/

       url : function() {
          var base = 'api/annotation';
          var format = '.json';
          if (this.isNew()) return base + format;
          return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
       }
    });


var AnnotationModel = Backbone.Model.extend({

       url : function() {
          var base = 'api/annotation';
          var format = '.json';
          if (this.isNew()) return base + format;
          return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
       }
    });


var AnnotationCropModel = Backbone.Model.extend({

       url : null,
       initialize: function (options) {
          this.url = options.url;
       }
    });

// define our collection
var AnnotationCollection = Backbone.Collection.extend({
       model: AnnotationModel,
       url: function() {
          if (this.user != undefined) {
             return "api/user/" + this.user + "/imageinstance/" + this.image + "/annotation.json";
          } else if (this.term != undefined && this.project !=undefined){
             return "api/term/" + this.term + "/project/" + this.project +"/annotation.json";
          } else if (this.project != undefined) {
             return "api/project/" + this.project + "/annotation.json";
          }  else if (this.image != undefined && this.term != undefined){
             return "api/term/"+this.term+"/imageinstance/" + this.image + "/annotation.json";
          }  else if (this.term != undefined){
             return "api/term/" + this.term + "/annotation.json";
          } else  if(this.image != undefined) {
             return "api/imageinstance/" + this.image + "/annotation.json";
          } else  {
             return "api/annotation.json";
          }
       },
       initialize: function (options) {
          this.image = options.image;
          this.user = options.user;
          this.project = options.project;
          this.term = options.term;
       }
    });

AnnotationCollection.comparator = function(annotation) {
   return annotation.get("created");
};

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */
var SlideModel = Backbone.Model.extend({

	url : function() {
		var base = 'api/slide';
		var format = '.json';
        if (this.isNew()) return base + format;
		return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
	}
});

var ProjectSlideModel = Backbone.Model.extend({
	url : function() {
        if (this.slide == null)
		    return 'api/project/' + this.project +'/slide.json';
        else
            return 'api/project/' + this.project +'/slide/'+this.slide+'.json';
	},
    initialize: function (options) {
        this.project = options.project;
        this.slide = options.slide;
    }
});

var ProjectSlideCollection = Backbone.Collection.extend({
    model : SlideModel,
	url : function() {
		return 'api/project/' + this.idProject +'/slide.json';
	},
    initialize: function (options) {
        this.idProject = options.idProject;

    }
});



// define our collection
var SlideCollection = Backbone.Collection.extend({
    model: SlideModel,
    CLASS_NAME: "be.cytomine.image.Slide",
	url : function() {
        if (this.page == null)
		    return 'api/currentuser/slide.json';
        else
            return 'api/currentuser/slide.json?page='+this.page+'&limit=';

        //Request URL:http://localhost:8080/cytomine-web/api/currentuser/image.json?_search=false&nd=1310463777413&rows=10&page=1&sidx=filename&sord=asc
	},
    initialize: function (options) {
        this.page = options.page;
        this.limit = options.limit;
        this.sidx = options.sidx;
        this.sord = options.sord;
    }
});
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 5/05/11
 * Time: 8:20
 * To change this template use File | Settings | File Templates.
 */
var BeginTransactionModel = Backbone.Model.extend({

	url : function() {
		var base = 'transaction/begin';
		var format = '.json';
		return base + format;
	}
});

var EndTransactionModel = Backbone.Model.extend({

	url : function() {
		var base = 'transaction/end';
		var format = '.json';
		return base + format;
	}
});/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */
var StatsModel = Backbone.Model.extend({

	url : function() {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/term/stat.json";
        } else if (this.term != undefined) {
            return "api/term/" + this.term + "/project/stat.json";
        } else {
            return "api/stat.json";
        }
	},
    initialize: function (options) {
        this.project = options.project;
        this.term = options.term;
    }
});

// define our collection
var StatsCollection = Backbone.Collection.extend({
    model: StatsModel,

    url: function() {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/term/stat.json";
        } else if (this.term != undefined) {
            return "api/term/" + this.term + "/project/stat.json";
        } else {
            return "api/stat.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.term = options.term;
    }
});/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 9/05/11
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/04/11
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
var CommandModel = Backbone.Model.extend({

	url : function() {
		var base = 'api/command';
		var format = '.json';
        if (this.isNew()) return base + format;
		return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
	}
});


// define our collection
var CommandCollection = Backbone.Collection.extend({
    model: CommandModel,

    url: function() {
        if (this.project != undefined) {
            return "api/project/" + this.project + "/last/" + this.max +".json";
        } else {
            return "api/command.json";
        }
    },
    initialize: function (options) {
        this.project = options.project;
        this.max = options.max;
    }
});

CommandCollection.comparator = function(command) {
  return command.get("created");
};var RelationTermModel = Backbone.Model.extend({
	url : function() {
        if(this.term != undefined)
            return  'api/relation/term/'+ this.term +'.json';
        else if (this.relation != undefined && this.term1!=undefined && this.term2!=undefined)
		    return 'api/relation/'+ this.relation +'/term1/' + this.term1 + "/term2/" + this.term2 + ".json";
        else if (this.relation == undefined && this.term1==undefined && this.term2==undefined)
		    return 'api/relation/parent/term.json';
        else if(this.term1==undefined && this.term2==undefined)
            return 'api/relation/' + this.relation +'/term.json';
        else
            return 'api/relation/parent/term1/' + this.term1 + "/term2/" + this.term2 + ".json";
	},
    initialize: function (options) {
        this.relation = options.relation;
        this.term = options.term;
        this.term1 = options.term1;
        this.term2 = options.term2;
    }
});

var RelationTermCollection = Backbone.Collection.extend({
    model : RelationTermModel,
	url : function() {
        if(this.term != undefined)
            return  'api/relation/term/'+ this.term +'.json';
        else
            return 'api/relation/' + this.relation +'/term.json';
	},
    initialize: function (options) {
        this.relation = options.relation;
        this.term = options.term;
    }
});var LoginDialogView = Backbone.View.extend({
       tagName : "div",
       initialize: function(options) {
       },
       doLayout: function(tpl) {
          var dialog = new ConfirmDialogView({
                 el:'#dialogs',
                 template : _.template(tpl, {version : window.app.status.version}),
                 dialogAttr : {
                    dialogID : "#login-confirm",
                    width : 350,
                    height : 350,
                    buttons: {
                       "Login": function() {
                          $('#login-form').submit();
                       }
                    },
                    close :function (event) {
                       /*window.location = "403";*/
                    }
                 }
              }).render();
          $("#progress").hide();
          $("#remember_me").button();
          $("#j_username").click(function() {
             $(this).select();
          });
          $("#j_password").click(function() {
             $(this).select();
          });
          $('#login-form').submit(window.app.controllers.auth.doLogin);
          $('#login-form').keydown(function(e){
             if (e.keyCode == 13) { //ENTER_KEY
                $('#login-form').submit();
                return false;
             }
          });
          return this;
       },
       render: function() {
          var self = this;
          require(["text!application/templates/auth/LoginDialog.tpl.html"], function(tpl) {
             self.doLayout(tpl);
          });
       },
       close : function() {
          $('#login-form').close();
       }
    });var LoadingDialogView = Backbone.View.extend({
       tagName : "div",
       initialize: function(options) {
       },
       doLayout: function(tpl) {
          var dialog = new ConfirmDialogView({
                 el:'#dialogs',
                 template : _.template(tpl, {}),
                 dialogAttr : {
                    dialogID : "#loading-dialog",
                    width : 475,
                    height : 375,
                    buttons: {

                    },
                    close :function (event) {

                    }
                 }
              }).render();

       },
       render: function() {
          var self = this;
          require(["text!application/templates/auth/LoadingDialog.tpl.html"], function(tpl) {
             self.doLayout(tpl);
          });
          return this;
       },
       initProgressBar : function() {
          $("#progress").show();
          $("#login-progressbar" ).progressbar({
                 value: 0
              });
       },
       progress : function(value) {
          $("#login-progressbar" ).progressbar({
                 value: value
              });
       },
       close : function() {
          $("#loading-dialog").dialog("close");
       }
    });var LogoutDialogView = Backbone.View.extend({
       tagName : "div",

       initialize: function(options) {
       },
       doLayout: function(tpl) {
          var dialog = new ConfirmDialogView({
                 el:'#dialogs',
                 template : _.template(tpl, {}),
                 dialogAttr : {
                    dialogID : "#logout-confirm",

                    buttons: {
                       "Confirm": function() {
                          window.location = "logout";
                       },
                       "Cancel": function() {
                          $(this).dialog("close");
                       }
                    },
                    close :function (event) {
                       $(this).remove();
                    }
                 }
              }).render();
          return this;
       },
       render: function() {
          var self = this;
          require(["text!application/templates/auth/LogoutDialog.tpl.html"], function(tpl) {
             self.doLayout(tpl);
          });
       }
    });var AnnotationThumbView = Backbone.View.extend({

       events: {

       },

       initialize: function(options) {
          this.id = "annotationthumb"+this.model.get('id');
          _.bindAll(this, 'render');
       },

       render: function() {
          var json = this.model.toJSON();
          json.project = window.app.status.currentProject;
          var self = this;
          require(["text!application/templates/dashboard/AnnotationThumb.tpl.html"], function(tpl) {
             $(self.el).html(_.template(tpl, json));
          });
          return this;
       }
    });
var AnnotationView = Backbone.View.extend({
       tagName : "div",

       initialize: function(options) {
          this.container = options.container;
          this.page = options.page;
          this.annotations = null; //array of annotations that are printed
          if (this.page == undefined) this.page = 0;
       },
       render: function() {
          var self = this;

          self.appendThumbs(self.page);

          /*$(window).scroll(function(){
             if  (($(window).scrollTop() + 100) >= $(document).height() - $(window).height()){
                self.appendThumbs(++self.page);
             }
          });*/

          return this;
       },
       appendThumbs : function(page) {
          var self = this;
          var cpt = 0;
          var nb_thumb_by_page = 2500;
          var inf = Math.abs(page) * nb_thumb_by_page;
          var sup = (Math.abs(page) + 1) * nb_thumb_by_page;

          self.annotations = new Array();

          self.model.each(function(annotation) {
             if ((cpt >= inf) && (cpt < sup)) {
                var thumb = new AnnotationThumbView({
                       model : annotation,
                       className : "thumb-wrap",
                       id : "annotationthumb"+annotation.get('id')
                    }).render();
                $(self.el).append(thumb.el);
             }
             cpt++;
             self.annotations.push(annotation.id);
          });
       },
       /**
        * Add the thumb annotation
        * @param annotation Annotation model
        */
       add : function(annotation) {
          
          var self = this;
          var thumb = new AnnotationThumbView({
                 model : annotation,
                 className : "thumb-wrap",
                 id : "thumb"+annotation.get('id')
              }).render();
          $(self.el).append(thumb.el);

       },
       /**
        * Remove thumb annotation with id
        * @param idAnnotation  Annotation id
        */
       remove : function (idAnnotation) {
          $("#thumb"+idAnnotation).remove();
       },
       /**
        * Refresh thumb with newAnnotations collection:
        * -Add annotations thumb from newAnnotations which are not already in the thumb set
        * -Remove annotations which are not in newAnnotations but well in the thumb set
        * @param newAnnotations newAnnotations collection
        */
       refresh : function(newAnnotations) {
          var self = this;

          var arrayDeletedAnnotations = self.annotations;
          newAnnotations.each(function(annotation) {
             //if annotation is not in table, add it
             if(_.indexOf(self.annotations, annotation.id)==-1){
                self.add(annotation);
                self.annotations.push(annotation.id);
             }
             /*
              * We remove each "new" annotation from  arrayDeletedAnnotations
              * At the end of the loop, element from arrayDeletedAnnotations must be deleted because they aren't
              * in the set of new annotations
              */
             //
             arrayDeletedAnnotations = _.without(arrayDeletedAnnotations,annotation.id);

          });

          arrayDeletedAnnotations.forEach(function(removeAnnotation) {
             self.remove(removeAnnotation);
             self.annotations = _.without(self.annotations,removeAnnotation);
          });

       }


    });
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 26/04/11
 * Time: 14:38
 * To change this template use File | Settings | File Templates.
 */
var ProjectDashboardView = Backbone.View.extend({
       tagName : "div",
       projectElem : "#projectdashboardinfo",  //div with project info
       tabsAnnotation : null,
       images : null,
       imagesView : null, //image view
       imagesTabsView : null,
       imagesThumbOrTab : null, //0=thumb, 1=tab
       annotationsViews : [], //array of annotation view
       maxCommandsView : 10,
       selectedTermTab : 0,
       rendered : false,
       initialize: function(options) {
          this.container = options.container;
          this.imagesThumbOrTab = options.imagesThumbOrTab;
          _.bindAll(this, 'render');
       },
       events: {
       },
       /**
        * Print all information for this project
        */
       render: function() {
          var self = this;
          require(["text!application/templates/dashboard/Dashboard.tpl.html"], function(tpl) {

             self.images = new Array();
             self.doLayout(tpl);
             self.rendered = true;
          });
          return this;
       },
       /**
        * Refresh all information for this project
        */
       refresh : function() {
          var self = this;
          if (!self.rendered) return;

          var projectModel = new ProjectModel({id : self.model.id});
          var projectCallback = function(model, response) {
             
             self.model = model;

             self.fetchProjectInfo();
             /*self.refreshImages();*/

             //refresh selected tab
             
             /*self.refreshAnnotations(self.selectedTermTab);*/

             //TODO: must be improve!
             new AnnotationCollection({project:self.model.id}).fetch({
                    success : function (collection, response) {
                       self.fetchCommands(collection);
                    }
                 });


             self.fetchStats();

          }

          projectModel.fetch({
                 success : function(model, response) {
                    projectCallback(model, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
                 }
              });

       },
       /**
        * Init annotation tabs
        */
       initTabs : function(){
          var self = this;
          /* Init dashboard */
          $("#projectcolmunChartPanel").panel({collapsible: true});
          $("#projectPieChartPanel").panel({collapsible: true});
          $("#projectInfoPanel").panel({collapsible: true});
          $("#projectLastCommandPanel").panel({collapsible: true});


          /* Init Annotations */
          require(["text!application/templates/dashboard/TermTab.tpl.html", "text!application/templates/dashboard/TermTabContent.tpl.html"], function(termTabTpl, termTabContentTpl) {
             new OntologyModel({id:self.model.get('ontology')}).fetch({
                    success : function(model, response) {
                       $(self.el).find('.tree').dynatree({
                              checkbox: true,
                              selectMode: 3,
                              expand : true,
                              onExpand : function() {},
                              children: model.toJSON(),
                              onSelect: function(select, node) {
                                 //if(!self.activeEvent) return;

                                 if (node.isSelected()) {

                                    self.refreshAnnotations(node.data.key);
                                    $("#tabsterm-panel-"+self.model.id+"-"+node.data.key).show();
                                 }
                                 else {
                                    $("#tabsterm-panel-"+self.model.id+"-"+node.data.key).hide();


                                 }

                              },
                              onDblClick: function(node, event) {
                                 //node.toggleSelect();
                              },

                              // The following options are only required, if we have more than one tree on one page:
                              initId: "treeData-annotations-"+self.model.get('ontology'),
                              cookieId: "dynatree-Cb-annotations-"+self.model.get('ontology'),
                              idPrefix: "dynatree-Cb-annotations-"+self.model.get('ontology')+"-"
                           });
                       //expand all nodes
                       $(self.el).find('.tree').dynatree("getRoot").visit(function(node){
                          node.expand(true);
                       });
                       $("#ontology-annotations-panel-"+self.model.id).panel();
                       $(self.el).find("#hideAllAnnotations").button();
                       $(self.el).find("#hideAllAnnotations").click(function(){
                          self.selectAnnotations(false);
                       });
                       $(self.el).find("#showAllAnnotations").button();
                       $(self.el).find("#showAllAnnotations").click(function(){
                          self.selectAnnotations(true);
                       });
                       $(self.el).find("#refreshAnnotations").button();
                       $(self.el).find("#refreshAnnotations").click(function(){
                          self.refreshSelectedTerms();
                       });


                    }
                 });
             new TermCollection({idOntology:self.model.get('ontology')}).fetch({
                    success : function (collection, response) {
                       collection.each(function(term) {
                          //add x term tab
                          $("#listtabannotation").prepend(_.template(termTabContentTpl, { project : self.model.id, id : term.get("id"), name : term.get("name")}));
                          $("#tabsterm-panel-"+self.model.id+"-"+term.get("id")).panel();
                          $("#tabsterm-panel-"+self.model.id+"-"+term.get("id")).hide();

                       });


                    }});
          });
       },
       /**
        * Add the the tab with term info
        * @param id  term id
        * @param name term name
        */
       addTermToTab : function(termTabTpl, termTabContentTpl, data) {
          //$("#ultabsannotation").append(_.template(termTabTpl, data));
          $("#listtabannotation").append(_.template(termTabContentTpl, data));

       },
       selectAnnotations : function (selected) {
          var self = this;
          new TermCollection({idOntology:self.model.get('ontology')}).fetch({
                 success : function (collection, response) {
                    collection.each(function(term) {
                       $(self.el).find('.tree').dynatree("getTree").selectKey(term.get("id"), selected);
                    });
                 }
              });
       },
       refreshSelectedTerms : function () {
          var self = this;
          var tree = $(this.el).find('.tree').dynatree("getRoot");
          tree.visit(function(node){
             if (!node.isSelected()) return;

             self.refreshAnnotations(node.data.key);
          });
       },
       /**
        * Refresh all annotation dor the given term
        * @param term annotation term to be refresh (all = 0)
        */
       refreshAnnotations : function(term) {
          this.printAnnotationThumb(term,"#tabsterm-"+this.model.id+"-"+term);
       },
       clearAnnotations : function (term) {
          var self = this;
          $("#tabsterm-"+self.model.id+"-"+term).empty();

       },
       /**
        * Print annotation for the given term
        * @param term term annotation term to be refresh (all = 0)
        * @param $elem  elem that will keep all annotations
        */
       printAnnotationThumb : function(term,$elem){
          var self = this;

          var idTerm = 0;
          if(term==0) {idTerm = undefined;}
          else idTerm = term

          new AnnotationCollection({project:self.model.id,term:idTerm}).fetch({
                 success : function (collection, response) {
                    
                    if (self.annotationsViews[idTerm] != null) { //only refresh
                       self.annotationsViews[idTerm].refresh(collection);
                       return;
                    }
                    self.annotationsViews[idTerm] = new AnnotationView({
                           page : undefined,
                           model : collection,
                           el:$($elem),
                           container : window.app.view.components.warehouse
                        }).render();
                    
                    //self.annotationsViews[term].refresh(collection);
                 }
              });
       },
       /**
        * Get and Print ALL images (use for the first time)
        */
       fetchImages : function() {
          
          var self = this;
          $("#tabs-images-listing-"+ self.model.get('id')).hide();
          new ImageInstanceCollection({project:self.model.get('id')}).fetch({
                 success : function (collection, response) {

                    

                    self.imagesView = new ImageView({
                           page : 0,
                           model : collection,
                           el:$("#projectImageThumb"+self.model.get('id')),
                           container : window.app.view.components.warehouse
                        }).render();
                    
                    self.imagesTabsView = new ImageTabsView({
                           page : 0,
                           model : collection,
                           el:$("#projectImageListing"+self.model.get('id')),
                           container : window.app.view.components.warehouse,
                           idProject : self.model.id
                        }).render();



                    $("#tabs-images-listing-"+ self.model.get('id')).tabs();
                    self.selectTab(self.imagesThumbOrTab);
                    $("#tabs-images-listing-"+ self.model.get('id')).show();
                 }});

       },
       /**
        * Get and Print only new images and remove delted images
        */
       refreshImages : function() {
          
          var self = this;
          if (self.imagesView == null || self.imagesTabsView == null) {
             self.fetchImages();
             return;
          }
          new ImageInstanceCollection({project:self.model.get('id')}).fetch({
                 success : function (collection, response) {
                    self.imagesView.refresh(collection);
                 }});
       },
       refreshImagesTabs : function() {
          
          var self = this;
          if(self.imagesTabsView==null) return; //imageView is not yet build
          new ImageInstanceCollection({project:self.model.get('id')}).fetch({
                 success : function (collection, response) {
                    self.imagesTabsView.refresh(collection);
                 }});
       },
       selectTab : function(index) {
          var self = this;
          
          $("#tabs-images-listing-"+ self.model.get('id')).tabs( "select" , index );
       },
       drawPieChart : function (collection, response) {
          $("#projectPieChart").empty();
          // Create and populate the data table.
          var data = new google.visualization.DataTable();
          data.addColumn('string', 'Term');
          data.addColumn('number', 'Number of annotations');
          data.addRows(_.size(collection));
          var i = 0;
          var colors = [];
          collection.each(function(stat) {
             colors.push(stat.get('color'));
             data.setValue(i,0, stat.get('key'));
             data.setValue(i,1, stat.get('value'));
             i++;
          });

          // Create and draw the visualization.
          new google.visualization.PieChart(document.getElementById('projectPieChart')).
              draw(data, {width: 500, height: 350,title:"", colors : colors});
       },
       drawColumnChart : function (collection, response) {
          $("#projectColumnChart").empty();
          var dataToShow = false;
          // Create and populate the data table.
          var data = new google.visualization.DataTable();

          data.addRows(_.size(collection));

          data.addColumn('string', 'Number');
          data.addColumn('number', 0);
          var colors = [];
          var j = 0;
          collection.each(function(stat) {
             colors.push(stat.get('color'));
             if (stat.get('value') > 0) dataToShow = true;
             data.setValue(j, 0, stat.get("key"));
             data.setValue(j, 1, stat.get("value"));
             j++;
          });

          // Create and draw the visualization.
          new google.visualization.ColumnChart(document.getElementById("projectColumnChart")).
              draw(data,
              {title:"",
                 width:500, height:350,
                 vAxis: {title: "Number of annotations"},
                 hAxis: {title: "Terms"}}
          );
          $("#projectColumnChart").show();

       },
       fetchStats : function () {
          var self = this;
          if (self.model.get('numberOfAnnotations') == 0) return;

          

          var statsCollection = new StatsCollection({project:self.model.get('id')});
          var statsCallback = function(collection, response) {
             //Check if there is something to display
             self.drawPieChart(collection, response);
             self.drawColumnChart(collection, response);
          }

          statsCollection.fetch({
                 success : function(model, response) {
                    statsCallback(model, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
                 }
              });

       },

       fetchProjectInfo : function () {
          var self = this;
          var json = self.model.toJSON();
          var idOntology = json.ontology;

          //Get ontology name
          //json.ontology = window.app.models.ontologies.get(idOntology).get('name');

          //Get created/updated date
          var dateCreated = new Date();
          dateCreated.setTime(json.created);
          json.created = dateCreated.toLocaleDateString() + " " + dateCreated.toLocaleTimeString();
          var dateUpdated = new Date();
          dateUpdated.setTime(json.updated);
          json.updated = dateUpdated.toLocaleDateString() + " " + dateUpdated.toLocaleTimeString();

          self.resetElem("#projectInfoName",json.name);
          self.resetElem("#projectInfoOntology",json.ontologyName);
          self.resetElem("#projectInfoNumberOfSlides",json.numberOfSlides);
          self.resetElem("#projectInfoNumberOfImages",json.numberOfImages);
          self.resetElem("#projectInfoNumberOfAnnotations",json.numberOfAnnotations);
          self.resetElem("#projectInfoCreated",json.created);
          self.resetElem("#projectInfoUpdated",json.updated);

          $("#projectInfoUserList").empty();

          require(["text!application/templates/dashboard/UserInfo.tpl.html"], function(tpl) {
             //Get users list
             var users = []
             _.each(self.model.get('users'), function (idUser) {
                users.push(window.app.models.users.get(idUser).prettyName());
             });
             $("#projectInfoUserList").html(users.join(", "));
          });



       },
       fetchCommands : function (annotations) {
          var self = this;
          require([
             "text!application/templates/dashboard/CommandAnnotation.tpl.html",
             "text!application/templates/dashboard/CommandAnnotationTerm.tpl.html",
            "text!application/templates/dashboard/CommandImageInstance.tpl.html"],
              function(commandAnnotationTpl, commandAnnotationTermTpl,commandImageInstanceTpl) {
                 var commandCollection = new CommandCollection({project:self.model.get('id'),max:self.maxCommandsView});

                 var commandCallback = function(collection, response) {
                    
                    $("#lastactionitem").empty();
                    collection.each(function(command) {
                       var json = command.toJSON()

                       
                       

                       var dateCreated = new Date();
                       dateCreated.setTime(json.created);
                       var dateStr = dateCreated.toLocaleDateString() + " " + dateCreated.toLocaleTimeString();

                       var jsonCommand = $.parseJSON(json.command.data);
                        //jsonCommand.cropURL
                       
                       
                       var action = ""

                       if(json.command.CLASSNAME=="be.cytomine.command.annotation.AddAnnotationCommand")
                       {
                          var cropStyle = "block";
                          var cropURL = jsonCommand.cropURL;

                          if (annotations.get(jsonCommand.id) == undefined) {
                             cropStyle = "none";
                             cropURL = "";
                          }

                          var action = _.template(commandAnnotationTpl, {idProject : self.model.id, idAnnotation : jsonCommand.id, idImage : jsonCommand.image,imageFilename : jsonCommand.imageFilename, icon:"add.png",text:json.prefixAction+ " " + json.command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});
                          $("#lastactionitem").append(action);
                       }
                       if(json.command.CLASSNAME=="be.cytomine.command.annotation.EditAnnotationCommand")
                       {
                          var cropStyle = "";
                          var cropURL = jsonCommand.newAnnotation.cropURL;
                          if (annotations.get(jsonCommand.newAnnotation.id) == undefined) {
                             cropStyle = "display : none;";
                             cropURL = "";
                          }

                          var action = _.template(commandAnnotationTpl, {idProject : self.model.id, idAnnotation : jsonCommand.newAnnotation.id, idImage : jsonCommand.newAnnotation.image,imageFilename : jsonCommand.newAnnotation.imageFilename,icon:"delete.gif",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});
                          $("#lastactionitem").append(action);
                       }
                       if(json.command.CLASSNAME=="be.cytomine.command.annotation.DeleteAnnotationCommand")
                       {
                          var cropStyle = "";
                          var cropURL = jsonCommand.cropURL;
                          if (annotations.get(jsonCommand.id) == undefined) {
                             cropStyle = "display : none;";
                             cropURL = "";
                          }
                          var action = _.template(commandAnnotationTpl, {idProject : self.model.id, idAnnotation : jsonCommand.id, idImage : jsonCommand.image,imageFilename : jsonCommand.imageFilename,icon:"delete.gif",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});
                          $("#lastactionitem").append(action);
                       }


                       if(json.command.CLASSNAME=="be.cytomine.command.annotationterm.AddAnnotationTermCommand")
                       {
                          
                          var action = _.template(commandAnnotationTermTpl, {icon:"ui-icon-plus",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,image:""});
                          $("#lastactionitem").append(action);

                       }
                       if(json.command.CLASSNAME=="be.cytomine.command.annotationterm.EditAnnotationTermCommand")
                       {
                          
                          var action = _.template(commandAnnotationTermTpl, {icon:"ui-icon-pencil",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,image:""});
                          $("#lastactionitem").append(action);

                       }
                       if(json.command.CLASSNAME=="be.cytomine.command.annotationterm.DeleteAnnotationTermCommand")
                       {
                          
                          var action = _.template(commandAnnotationTermTpl, {icon:"ui-icon-trash",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,image:""});
                          $("#lastactionitem").append(action);

                       }


                       if(json.command.CLASSNAME=="be.cytomine.command.imageinstance.AddImageInstanceCommand")
                       {
                          var cropStyle = "block";
                          var cropURL = jsonCommand.thumb;
                          
                           


                          var action = _.template(commandImageInstanceTpl, {idProject : self.model.id, idImage : jsonCommand.id, imageFilename : jsonCommand.filename, icon:"add.png",text:json.prefixAction+ " " + json.command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});
                          $("#lastactionitem").append(action);
                       }


                       if(json.command.CLASSNAME=="be.cytomine.command.imageinstance.DeleteImageInstanceCommand")
                       {
                          var cropStyle = "block";
                          var cropURL = jsonCommand.thumb;

                          var action = _.template(commandImageInstanceTpl, {idProject : self.model.id, idImage : jsonCommand.id, imageFilename : jsonCommand.filename,icon:"delete.gif",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});
                          $("#lastactionitem").append(action);
                       }




                    });
                 }

                 commandCollection.fetch({
                        success : function(model, response) {
                           commandCallback(model, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
                        }
                     });
              });


       },
       resetElem : function(elem,txt) {
          
          $(this.el).find(elem).empty();
          $(this.el).find(elem).append(txt);
       },
       doLayout : function(tpl) {
          
          var self = this;
          var html = _.template(tpl, self.model.toJSON());
          $(self.el).append(html);
          window.app.controllers.browse.tabs.addDashboard(self);
          self.initTabs();
       },
       initWidgets : function() {
          return; //do nothing actually. We have to keep positions in memory if we do that

          $( ".widgets" ).sortable({
                 connectWith: ".widgets"

              });

          $( ".widgets" ).disableSelection();
       }
    });/* Annotation Layer */


var AnnotationLayer = function (name, imageID, userID, color, ontologyTreeView, browseImageView, map) {
   
   var styleMap = new OpenLayers.StyleMap({
          "default" : OpenLayers.Util.applyDefaults({fillColor: color, fillOpacity: 0.5, strokeColor: "black", strokeWidth: 2},
              OpenLayers.Feature.Vector.style["default"]),
          "select" : OpenLayers.Util.applyDefaults({fillColor: "#25465D", fillOpacity: 0.5, strokeColor: "black", strokeWidth: 2},
              OpenLayers.Feature.Vector.style["default"])
       });
   this.ontologyTreeView = ontologyTreeView;
   this.name = name;
   this.map = map,
       this.imageID = imageID;
   this.userID = userID;
   this.vectorsLayer = new OpenLayers.Layer.Vector(this.name, {
          styleMap: styleMap,
          rendererOptions: {
             zIndexing: true
          }
       });

   this.features = [];
   this.controls = null;
   this.dialog = null;
   this.rotate = false;
   this.resize = false;
   this.drag = false;
   this.irregular = false;
   this.aspectRatio = false;
   this.browseImageView = browseImageView;
   this.map = browseImageView.map;
   this.popup = null;
   this.hoverControl = null;
   this.isOwner = null;
   this.deleteOnSelect = false; //true if select tool checked
   this.measureOnSelect = false;
   this.magicOnClick = false;
}

AnnotationLayer.prototype = {



   registerEvents: function (map) {

      var self = this;

      this.vectorsLayer.events.on({
             clickFeature : function (evt) {
                
             },
             onSelect : function (evt) {
                
             },
             featureselected: function (evt) {
                

                if (!self.measureOnSelect) {
                   self.ontologyTreeView.refresh(evt.feature.attributes.idAnnotation);
                   
                   if (self.deleteOnSelect == true) {
                      self.removeSelection();
                   }
                   self.showPopup(map, evt);
                }
                else self.showPopupMeasure(map, evt);

             },
             'featureunselected': function (evt) {
                
                if (self.measureOnSelect) self.vectorsLayer.removeFeatures(evt.feature);

                if (self.dialog != null) self.dialog.destroy();
                
                self.ontologyTreeView.clear();
                self.ontologyTreeView.clearAnnotation();
                self.clearPopup(map, evt);
                //alias.ontologyTreeView.refresh(null);
             },
             'featureadded': function (evt) {
                
                /* Check if feature must throw a listener when it is added
                 * true: annotation already in database (no new insert!)
                 * false: new annotation that just have been draw (need insert)
                 * */
                if (!self.measureOnSelect) {
                   if (evt.feature.attributes.listener != 'NO') {
                      
                      evt.feature.attributes.measure = 'YES';
                      self.addAnnotation(evt.feature);
                   }
                }
                else {
                   self.controls.select.unselectAll();
                   self.controls.select.select(evt.feature);
                }

             },
             'beforefeaturemodified': function (evt) {
                
             },
             'afterfeaturemodified': function (evt) {

                self.updateAnnotation(evt.feature);

             },
             'onDelete': function (feature) {
                
             }
          });
   },
   initControls: function (map, selectFeature) {
      /*if (isOwner) { */
      this.controls = {
         'freehand': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Path),
         'point': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Point),
         'line': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Path),
         'polygon': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Polygon),
         'regular': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.RegularPolygon, {
                handlerOptions: {
                   sides: 5
                }
             }),
         'modify': new OpenLayers.Control.ModifyFeature(this.vectorsLayer),
         'select': selectFeature
      }
      this.controls.freehand.freehand = true;

      /* else {
       
       this.controls = {
       'select': new OpenLayers.Control.SelectFeature(this.vectorsLayer)
       }
       }*/
      map.initTools(this.controls);
      map.initAutoAnnoteTools();
   },


   /*Load annotation from database on layer */
   loadAnnotations: function (browseImageView) {
      
      var self = this;
      new AnnotationCollection({user : this.userID, image : this.imageID, term: undefined}).fetch({
             success : function (collection, response) {
                collection.each(function(annotation) {
                   var feature = self.createFeatureFromAnnotation(annotation);
                   self.addFeature(feature);
                });
                browseImageView.layerLoadedCallback(self);
             }
          });
      browseImageView.addVectorLayer(this, this.userID);
   },
   addFeature: function (feature) {
      this.features[feature.attributes.idAnnotation] = feature;
      
      this.vectorsLayer.addFeatures(feature);
   },
   selectFeature: function (feature) {
      this.controls.select.unselectAll();
      this.controls.select.select(feature);
   },
   removeFeature: function (idAnnotation) {
      var feature = this.getFeature(idAnnotation);
      if (feature != null && feature.popup) {
         this.map.removePopup(feature.popup);
         feature.popup.destroy();
         feature.popup = null;
         this.popup = null;
      }
      this.vectorsLayer.removeFeatures(feature);
      this.ontologyTreeView.clearAnnotation();
      this.ontologyTreeView.clear();
      this.features[idAnnotation] = null;

   },
   getFeature : function(idAnnotation) {
      return this.features[idAnnotation];
   },
   removeSelection: function () {
      for (var i in this.vectorsLayer.selectedFeatures) {
         var feature = this.vectorsLayer.selectedFeatures[i];
         
         this.removeAnnotation(feature);
      }
   },
   clearPopup : function (map, evt) {
      var self = this;
      feature = evt.feature;
      if (feature.popup) {
         self.popup.feature = null;
         map.removePopup(feature.popup);
         feature.popup.destroy();
         feature.popup = null;
         self.popup = null;
      }
   },
   showPopup : function(map, evt) {
      var self = this;
      require([
         "text!application/templates/explorer/PopupAnnotation.tpl.html"
      ], function(tpl) {
         //
         if (evt.feature.popup != null) {
            return;
         }
         new AnnotationModel({id : evt.feature.attributes.idAnnotation}).fetch({
                success : function (model, response) {
                   var json = model.toJSON()
                   //username
                   json.username = window.app.models.users.get(json.user).prettyName();


                   //term
                   var terms = new Array();
                   _.each(json.term,function(term){
                      var tpl = _.template("<a href='#ontology/{{idOntology}}/{{idTerm}}'>{{termName}}</a>", {idOntology : term.ontology, idTerm : term.id, termName : term.name});
                      terms.push(tpl);

                   });
                   json.terms = terms.join(", ");

                   var content = _.template(tpl, json);
                   self.popup = new OpenLayers.Popup("",
                       new OpenLayers.LonLat(evt.feature.geometry.getBounds().right + 50, evt.feature.geometry.getBounds().bottom + 50),
                       new OpenLayers.Size(250, 100),
                       content,
                       false);
                   self.popup.setBackgroundColor("transparent");
                   self.popup.setBorder(0);
                   self.popup.padding = 0;

                   evt.feature.popup = self.popup;
                   self.popup.feature = evt.feature;
                   map.addPopup(self.popup);
                }
             });
      });

   },
   showPopupMeasure : function(map, evt) {
      var self = this;
      require([
         "text!application/templates/explorer/PopupMeasure.tpl.html"
      ], function(tpl) {
         if (evt.feature.popup != null) {
            return;
         }
         var content = _.template(tpl, {length:evt.feature.geometry.getLength()});
         self.popup = new OpenLayers.Popup("chicken",
             new OpenLayers.LonLat(evt.feature.geometry.getBounds().right + 50, evt.feature.geometry.getBounds().bottom + 50),
             new OpenLayers.Size(200, 60),
             content,
             false);
         self.popup.setBackgroundColor("transparent");
         self.popup.setBorder(0);
         self.popup.padding = 0;

         evt.feature.popup = self.popup;
         self.popup.feature = evt.feature;
         map.addPopup(self.popup);
      });


   },
   enableHightlight : function () {
      //this.hoverControl.activate();
   },
   disableHightlight : function () {
      //this.hoverControl.deactivate();
   },
   initHightlight : function (map) { //buggy :(
      /*this.hoverControl = new OpenLayers.Control.SelectFeature(this.vectorsLayer, {
       hover: true,
       highlightOnly: true,
       renderIntent: "temporary",
       eventListeners: {

       featurehighlighted: this.showPopup,
       featureunhighlighted: this.clearpopup
       }
       });


       map.addControl(this.hoverControl);
       //this.hoverControl.activate();   */
   },

   /*Add annotation in database*/
   addAnnotation: function (feature) {
      
      var format = new OpenLayers.Format.WKT();
      var geomwkt = format.write(feature);
      var alias = this;
      var annotation = new AnnotationModel({
             //"class": "be.cytomine.project.Annotation",
             name: "",
             location: geomwkt,
             image: this.imageID,
             parse: function(response) {
                
                window.app.view.message("Annotation", response.message, "");
                return response.annotation;
             }
          });

      
      new BeginTransactionModel({}).save({}, {
             success: function (model, response) {
                
                annotation.save(annotation.toJSON(), {
                       success: function (annotation, response) {


                          var annotationID = response.annotation.id;
                          var message = response.message;

                          var terms = alias.ontologyTreeView.getTermsChecked();

                          if (terms.length == 0) {
                             alias.addTermCallback(0, 0, feature, annotationID, message, undefined);
                          }

                          var counter = 0;
                          _.each(terms, function (idTerm) {
                             new AnnotationTermModel({
                                    term: idTerm,
                                    annotation: response.annotation.id
                                 }).save(null, {success : function (termModel, response) {
                                    alias.addTermCallback(terms.length, ++counter, feature, annotationID, message, idTerm);
                                 }});
                          });

                       },
                       error: function (model, response) {
                          var json = $.parseJSON(response.responseText);
                          window.app.view.message("Add annotation", "error:" + json.errors, "");
                       }
                    });

             },
             error: function (model, response) {
                
             }
          });

   },
   addTermCallback : function(total, counter, oldFeature, annotationID, message, idTerm) {
      if (counter < total) return;
      var self = this;
      new AnnotationModel({id:annotationID}).fetch({
             success : function (annotation, response) {
                self.vectorsLayer.removeFeatures([oldFeature]);
                var newFeature = self.createFeatureFromAnnotation(annotation);
                self.addFeature(newFeature);
                self.controls.select.unselectAll();
                self.controls.select.select(newFeature);
                window.app.view.message("Add annotation", message, "");
                new EndTransactionModel({}).save();
                self.browseImageView.refreshAnnotationTabs(idTerm);
                self.browseImageView.refreshAnnotationTabs(undefined);
             },
             error : function(model, response) {
                
                new EndTransactionModel({}).save();
             }
          });

   },
   createFeatureFromAnnotation :function (annotation) {
      var multipleTermColor = "#eeeeee";
      var format = new OpenLayers.Format.WKT();
      var point = format.read(annotation.get("location"));
      var geom = point.geometry;
      var feature = new OpenLayers.Feature.Vector(geom);
      feature.attributes = {
         idAnnotation: annotation.get("id"),
         measure : 'NO',
         listener: 'NO',
         importance: 10
      };
      if (_.size(annotation.get("term")) > 1) { //multiple terms
         feature.style =  {
            strokeColor :multipleTermColor,
            fillColor :  multipleTermColor,
            fillOpacity : 0.6
         }

      } else {
         _.each(annotation.get("term"), function(term){
            feature.style =  {
               strokeColor :term.color,
               fillColor :  term.color,
               fillOpacity : 0.6
            }
         });
      }

      return feature;
   },
   removeTermCallback : function(total, counter, feature, idAnnotation, idTerm) {
      
      if (counter < total) return;
      this.removeFeature(feature);
      this.controls.select.unselectAll();
      this.vectorsLayer.removeFeatures([feature]);
      var self = this;
      new AnnotationModel({id:feature.attributes.idAnnotation}).destroy({
             success: function (model, response) {
                
                
                window.app.view.message("Annotation", response.message, "");
                new EndTransactionModel({}).save();
                self.browseImageView.refreshAnnotationTabs(idTerm);
                self.browseImageView.refreshAnnotationTabs(undefined);

             },
             error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Annotation", json.errors, "");
             }
          });


   },
   removeAnnotation : function(feature) {
      var alias = this;
      var idAnnotation = feature.attributes.idAnnotation;
      
      var annotation = new AnnotationModel({id:idAnnotation});
      var counter = 0;
      new BeginTransactionModel({}).save({}, {
             success: function (model, response) {

                new AnnotationTermCollection({idAnnotation:idAnnotation}).fetch({success:function (collection, response) {
                       if (collection.size() == 0) {
                          alias.removeTermCallback(0, 0, feature, idAnnotation, undefined);
                          return;
                       }
                       collection.each(function(term) {
                          
                          

                          new AnnotationTermModel({annotation:idAnnotation,term:term.id}).destroy({success : function (model, response) {
                                 alias.removeTermCallback(collection.length, ++counter, feature, idAnnotation, term.id);
                              }});

                       });

                    }});

             },
             error: function (model, response) {
                
             }
          });

   },

   /*Modifiy annotation on database*/
   updateAnnotation: function (feature) {
      var format = new OpenLayers.Format.WKT();
      var geomwkt = format.write(feature);
      new AnnotationModel({id:feature.attributes.idAnnotation}).fetch({
             success : function(model, response) {
                model.set({location : geomwkt});
                model.save();  //TODO : callback success-error
             }
          });
   },
   /** Triggered when add new feature **/
   /*onFeatureAdded : function (evt) {
    
    // Check if feature must throw a listener when it is added
    // true: annotation already in database (no new insert!)
    // false: new annotation that just have been draw (need insert)
    //
    if(evt.feature.attributes.listener!='NO')
    {
    
    alias.addAnnotation(evt.feature);
    }
    },*/

   /** Triggered when update feature **/
   /* onFeatureUpdate : function (evt) {
    

    this.updateAnnotation(evt.feature);
    },*/
   toggleRotate: function () {
      this.resize = false;
      this.drag = false;
      this.rotate = true;
      this.updateControls();
      this.toggleControl("modify");
   },
   toggleResize: function () {
      this.resize = true;
      this.drag = false;
      this.rotate = false;
      this.updateControls();
      this.toggleControl("modify");
   },
   toggleDrag: function () {
      this.resize = false;
      this.drag = true;
      this.rotate = false;
      this.updateControls();
      this.toggleControl("modify");

   },
   toggleEdit: function () {
      this.resize = false;
      this.drag = false;
      this.rotate = false;
      this.updateControls();
      this.toggleControl("modify");

   },
   toggleIrregular: function () {
      
      this.irregular = !this.irregular;
      this.updateControls();
   },
   toggleAspectRatio: function () {
      this.aspectRatio = !this.aspectRatio;
      this.updateControls();
   },
   setSides: function (sides) {
      this.sides = sides;
      this.updateControls();
   },
   updateControls: function () {

      this.controls.modify.mode = OpenLayers.Control.ModifyFeature.RESHAPE;
      if (this.rotate) {
         this.controls.modify.mode |= OpenLayers.Control.ModifyFeature.ROTATE;
      }

      if (this.resize) {
         this.controls.modify.mode |= OpenLayers.Control.ModifyFeature.RESIZE;
         if (this.aspectRatio) {
            this.controls.modify.mode &= ~OpenLayers.Control.ModifyFeature.RESHAPE;
         }
      }
      if (this.drag) {
         this.controls.modify.mode |= OpenLayers.Control.ModifyFeature.DRAG;
      }
      if (this.rotate || this.drag) {
         this.controls.modify.mode &= ~OpenLayers.Control.ModifyFeature.RESHAPE;
      }
      this.controls.regular.handler.sides = this.sides;
      this.controls.regular.handler.irregular = this.irregular;
   },
   disableMeasureOnSelect : function() {
      var self = this;
      this.measureOnSelect = false;
      //browse measure on select
      for (var i in this.vectorsLayer.features) {
         var feature = this.vectorsLayer.features[i];
         
         
         if(feature.attributes.measure==undefined || feature.attributes.measure == 'YES') {
            self.vectorsLayer.removeFeatures(feature);
            if (feature.popup) {
               self.popup.feature = null;
               self.map.removePopup(feature.popup);
               feature.popup.destroy();
               feature.popup = null;
               self.popup = null;
            }

         }
      }
   },
   toggleControl: function (name) {
      //Simulate an OpenLayers.Control.EraseFeature tool by using SelectFeature with the flag 'deleteOnSelect'
      this.deleteOnSelect = false;
      this.disableMeasureOnSelect();
      this.magicOnClick = false;
      for (key in this.controls) {
         var control = this.controls[key];
         if (name == key) {
            control.activate();
            
            if (control == this.controls.modify) {
               for (var i in this.vectorsLayer.selectedFeatures) {
                  var feature = this.vectorsLayer.selectedFeatures[i];
                  control.selectFeature(feature);
               }
            }
         } else {
            control.deactivate();
            if (control == this.controls.modify) {
               for (var i in this.vectorsLayer.selectedFeatures) {
                  var feature = this.vectorsLayer.selectedFeatures[i];
                  control.unselectFeature(feature);
               }

            }
         }
      }

   },
   /* Callbacks undo/redo */
   annotationAdded: function (idAnnotation) {
      var self = this;
      
      var deleteOnSelectBackup = self.deleteOnSelect;
      self.deleteOnSelect = false;

      var annotation = new AnnotationModel({
             id: idAnnotation
          }).fetch({
             success: function (model) {
                var format = new OpenLayers.Format.WKT();
                var location = format.read(model.get('location'));
                var feature = new OpenLayers.Feature.Vector(location.geometry);
                feature.attributes = {
                   idAnnotation: model.get('id'),
                   listener: 'NO',
                   measure : 'NO',
                   importance: 10
                };
                self.addFeature(feature);
                self.selectFeature(feature);
                self.controls.select.activate();
                self.deleteOnSelect = deleteOnSelectBackup;
             }
          });

   },
   annotationRemoved: function (idAnnotation) {
      this.removeFeature(idAnnotation);
   },
   annotationUpdated: function (idAnnotation, idImage) {
      this.annotationRemoved(idAnnotation);
      this.annotationAdded(idAnnotation);
   },
   termAdded: function (idAnnotation, idTerm) {
      var self = this;
      
      this.ontologyTreeView.check(idTerm);
   },
   termRemoved: function (idAnnotation, idTerm) {
      
      this.ontologyTreeView.uncheck(idTerm);
   }
};var BrowseImageView = Backbone.View.extend({
       tagName: "div",
       /**
        * BrowseImageView constructor
        * Accept options used for initialization
        * @param options
        */
       initialize: function (options) {
          this.initCallback = options.initCallback;
          this.layers = [];
          this.layersLoaded = 0;
          this.baseLayers = [];
          this.annotationsPanel = null;
          this.map = null;
          this.currentAnnotation = null;
          _.bindAll(this, "initVectorLayers");
       },
       /**
        * Render the html into the DOM element associated to the view
        * @param tpl
        */
       doLayout: function (tpl) {
          var self = this;
          var templateData = this.model.toJSON();
          templateData.project = window.app.status.currentProject;
          var tpl = _.template(tpl, templateData);
          $(this.el).append(tpl);
          var tabs = $(this.el).children('.tabs');
          this.el.tabs("add", "#tabs-image-" + window.app.status.currentProject + "-" + this.model.get('id') + "-", this.model.get('filename'));
          this.el.css("display", "block");
          this.initToolbar();
          this.initMap();
          this.initOntology();
          this.initAnnotationsTabs();
          return this;
       },
       /**
        * Grab the layout and call ask for render
        */
       render : function() {
          var self = this;
          require([
             "text!application/templates/explorer/BrowseImage.tpl.html"
          ], function(tpl) {
             self.doLayout(tpl);
          });
          return this;
       },
       /**
        * Check init options and call appropriate methods
        */
       show : function(options) {
          var self = this;
          if (options.goToAnnotation != undefined) {
             _.each(this.layers, function(layer) {
                
                self.goToAnnotation(layer,  options.goToAnnotation.value);
             });
          }

       },
       refreshAnnotationTabs : function (idTerm) {
          this.annotationsPanel.refreshAnnotationTabs(idTerm);
       },
       /**
        * Move the OpenLayers view to the Annotation, at the
        * optimal zoom
        * @param layer The vector layer containing annotations
        * @param idAnnotation the annotation
        */
       goToAnnotation : function(layer, idAnnotation) {
          var self = this;
          var feature = layer.getFeature(idAnnotation);
          if (feature != undefined) {
             var bounds = feature.geometry.bounds;
             //Compute the ideal zoom to view the feature
             var featureWidth = bounds.right  - bounds.left;
             var featureHeight = bounds.top - bounds.bottom;
             var windowWidth = $(window).width();
             var windowHeight = $(window).height();
             var zoom = this.map.getNumZoomLevels()-1;
             var tmpWidth = featureWidth;
             var tmpHeight = featureHeight;
             while ((tmpWidth > windowWidth) || (tmpHeight > windowHeight)) {
                tmpWidth /= 2;
                tmpHeight /= 2;
                zoom--;
             }
             layer.controls.select.unselectAll();
             layer.controls.select.select(feature);
             self.currentAnnotation = idAnnotation;
             this.map.moveTo(new OpenLayers.LonLat(feature.geometry.getCentroid().x, feature.geometry.getCentroid().y), Math.max(0, zoom));
          }
       },
       getFeature : function (idAnnotation) {
          return this.userLayer.getFeature(idAnnotation);
       },
       removeFeature : function (idAnnotation) {
          return this.userLayer.removeFeature(idAnnotation);
       },
       /**
        * Callback used by AnnotationLayer at the end of theirs initializations
        * @param layer
        */
       layerLoadedCallback : function (layer) {
          var self = this;

          this.layersLoaded++;
          if (this.layersLoaded == _.size(window.app.models.users)) {
             //Init MultiSelected in LayerSwitcher
             $("#layerSwitcher"+this.model.get("id")).find("select").multiselect({
                    click: function(event, ui){
                       _.each(self.layers, function(layer){
                          if (layer.name != ui.value) return;
                          layer.vectorsLayer.setVisibility(ui.checked);
                       });
                    },
                    checkAll: function(){
                       _.each(self.layers, function(layer){
                          layer.vectorsLayer.setVisibility(true);
                       });
                    },
                    uncheckAll: function(){
                       _.each(self.layers, function(layer){
                          layer.vectorsLayer.setVisibility(false);
                       });
                    }
                 });

             //Init Controls on Layers
             var vectorLayers = _.map(this.layers, function(layer){ return layer.vectorsLayer;});
             var selectFeature = new OpenLayers.Control.SelectFeature(vectorLayers);
             _.each(this.layers, function(layer){
                layer.initControls(self, selectFeature);
                layer.registerEvents(self.map);
                if (layer.isOwner) {
                   self.userLayer = layer;
                   layer.vectorsLayer.setVisibility(true);
                   layer.toggleIrregular();
                   //Simulate click on None toolbar
                   var toolbar = $('#toolbar' + self.model.get('id'));
                   toolbar.find('input[id=none' + self.model.get('id') + ']').click();
                } else {
                   layer.controls.select.activate();
                   layer.vectorsLayer.setVisibility(false);
                }
             });
             if (_.isFunction(self.initCallback)) self.initCallback.call();
          }

       },
       /**
        * Return the AnnotationLayer of the logged user
        */
       getUserLayer: function () {
          return this.userLayer;
       },
       /**
        * Initialize the OpenLayers Map
        */
       initMap : function () {
          var self = this;
          var mime = this.model.get('mime');
          if (mime == "jp2") self.initDjatoka();
          if (mime == "vms" || mime == "mrxs" || mime == "tif" || mime == "tiff") self.initIIP();
       },
       /**
        * Add a base layer (image) on the Map
        * @param layer the layer to add
        */
       addBaseLayer : function(layer) {
          var self = this;
          this.map.addLayer(layer);
          this.baseLayers.push(layer);
          self.map.setBaseLayer(layer);
          var radioName = "layerSwitch-" + this.model.get("id");
          var layerID = "layerSwitch-" + this.model.get("id") + "-" + new Date().getTime(); //index of the layer in this.layers array
          var liLayer = _.template("<li><input type='radio' id='{{id}}' name='{{radioName}}' checked/><label style='font-weight:normal;color:#FFF' for='{{id}}'> {{name}}</label></li>", {id : layerID, radioName:radioName, name : layer.name.substr(0,15)});
          $("#layerSwitcher"+this.model.get("id")).find(".baseLayers").append(liLayer);
          $("#layerSwitcher"+this.model.get("id")).find(".baseLayers").find("#"+layerID);
          $("#layerSwitcher"+this.model.get("id")).find(".baseLayers").find("#"+layerID).click(function(){
             self.map.setBaseLayer(layer);
          });
       },
       /**
        * Add a vector layer on the Map
        * @param layer the layer to add
        * @param userID the id of the user associated to the layer
        */
       addVectorLayer : function(layer, userID) {
          this.map.addLayer(layer.vectorsLayer);
          this.layers.push(layer);

          var layerID = window.app.models.users.get(userID).prettyName();
          
          var color = window.app.models.users.get(userID).get('color');
          var layerOptionTpl;
          if (layer.isOwner) {
             layerOptionTpl = _.template("<option value='{{id}}' selected='selected'>{{name}}</option>", {id : layerID, name : layer.vectorsLayer.name, color : color});
          } else {
             layerOptionTpl = _.template("<option value='{{id}}'>{{name}}</option>", {id : layerID, name : layer.vectorsLayer.name, color : color});
          }

          $("#layerSwitcher"+this.model.get("id")).find(".annotationLayers").append(layerOptionTpl);


          /*  $("#layerSwitcher"+this.model.get("id")).find(".annotationLayers").find("#"+layerID).click(function(){
           var checked = $(this).attr("checked");
           layer.vectorsLayer.setVisibility(checked);
           });*/
       },
       /**
        * Create a draggable Panel containing Layers names
        * and tools
        */
       createLayerSwitcher : function() {
          new LayerSwitcherPanel({
                 browseImageView : this,
                 model : this.model,
                 el : this.el
              }).render();
       },
       /**
        * Create a draggable Panel containing a OverviewMap
        */
       createOverviewMap : function() {
          new OverviewMapPanel({
                 model : this.model,
                 el : this.el
              }).render();
       },
       /**
        * Init the Map if ImageServer is IIPImage
        */
       initIIP : function () {
          
          var self = this;
          var parseIIPMetadaResponse = function(response) {
             var metadata = null;
             var respArray = response.split("\n");
             _.each(respArray, function(it){
                var arg = it.split(":");
                if (arg[0] == "Max-size") {
                   var value = arg[1].split(" ");
                   var t_width  = value[0];
                   var t_height = value[1];
                   var nbZoom = 0;
                   while (t_width >= 256 || t_height >= 256) {
                      nbZoom++;
                      t_width = t_width / 2;
                      t_height = t_height / 2;
                   }
                   metadata = {width : value[0], height : value[1], nbZoom : nbZoom, overviewWidth : 200, overviewHeight : Math.round((200/value[0]*value[1]))};

                }

             });
             return metadata;
          }

          var initZoomifyLayer = function(metadata) {
             /* First we initialize the zoomify pyramid (to get number of tiers) */
             
             var baseURLs = self.model.get('imageServerBaseURL');
             
             
             var zoomify_url = []
             _.each(baseURLs, function(baseURL) {
                var url = baseURL + "/fcgi-bin/iipsrv.fcgi?zoomify=" + self.model.get('path') +"/";
                zoomify_url.push(url);
             });

             var baseLayer = new OpenLayers.Layer.Zoomify(
                 "Original",
                 zoomify_url,
                 new OpenLayers.Size( metadata.width, metadata.height )
                 , {transitionEffect: 'resize'}
             );
             var otsuURLS = _.map(zoomify_url, function (url){ return "http://localhost:8080/cytomine-web/proxy/otsu?url="+url;});
             var anotherLayer = new OpenLayers.Layer.Zoomify( "Otsu", otsuURLS,
                 new OpenLayers.Size( metadata.width, metadata.height ) );

             var layerSwitcher = self.createLayerSwitcher();

             //var numZoomLevels =  metadata.nbZoom;
             /* Map with raster coordinates (pixels) from Zoomify image */
             var options = {
                maxExtent: new OpenLayers.Bounds(0, 0, metadata.width, metadata.height),
                maxResolution: Math.pow(2,  metadata.nbZoom ),
                numZoomLevels:  metadata.nbZoom+1,
                units: 'pixels',
                controls: [
                   new OpenLayers.Control.TouchNavigation({
                          dragPanOptions: {
                             enableKinetic: true
                          }
                       }),
                   //new OpenLayers.Control.Navigation({zoomWheelEnabled : true, mouseWheelOptions: {interval: 1}, cumulative: false}),
                   new OpenLayers.Control.Navigation(),
                   new OpenLayers.Control.PanZoomBar(),
                   new OpenLayers.Control.MousePosition(),
                   new OpenLayers.Control.KeyboardDefaults()]
             };

             var overviewMap = new OpenLayers.Layer.Image(
                 "Overview"+self.model.get("id"),
                 self.model.get("thumb"),
                 new OpenLayers.Bounds(0, 0, metadata.width, metadata.height),
                 new OpenLayers.Size(metadata.overviewWidth, metadata.overviewHeight)
             );

             
             var overviewMapControl = new OpenLayers.Control.OverviewMap({
                    size: new OpenLayers.Size(metadata.overviewWidth, metadata.overviewHeight),
                    layers: [overviewMap],
                    div: document.getElementById('overviewMap' + self.model.get('id')),
                    minRatio: 1,
                    maxRatio: 1024,
                    mapOptions : {
                       maxExtent: new OpenLayers.Bounds(0, 0, metadata.width, metadata.height),
                       maximized: true
                    }
                 });

             self.map = new OpenLayers.Map("map" + self.model.get('id'), options);
             self.addBaseLayer(anotherLayer);
             self.addBaseLayer(baseLayer);
             self.createOverviewMap();
             self.map.zoomToMaxExtent();
             self.map.addControl(overviewMapControl);

          }

          $.ajax({
                 async: false,
                 processData : false,
                 dataType : 'text',
                 url: this.model.get('metadataUrl'),
                 success: function(response){
                    var metadata = parseIIPMetadaResponse(response);
                    initZoomifyLayer(metadata);
                 },
                 error: function(){
                    
                 }
              });
       },
       /**
        * Init the Map if ImageServer is Adore Djatoka
        */
       reloadAnnotation : function(idAnnotation) {
          var self = this;
          self.removeFeature(idAnnotation);
          new AnnotationModel({id:idAnnotation}).fetch({
                 success: function(annotation, response) {
                    var feature = self.userLayer.createFeatureFromAnnotation(annotation);
                    self.userLayer.addFeature(feature);
                    self.userLayer.selectFeature(feature);
                 }
              });
       },
       initDjatoka: function () {
          
          var self = this;
          var baseLayer = new OpenLayers.Layer.OpenURL(this.model.get('filename'), this.model.get('imageServerBaseURL'), {
                 transitionEffect: 'resize',
                 layername: 'basic',
                 format: 'image/jpeg',
                 rft_id: this.model.get('path'),
                 metadataUrl: this.model.get('metadataUrl')
              });


          var metadata = baseLayer.getImageMetadata();
          var resolutions = baseLayer.getResolutions();
          var maxExtent = new OpenLayers.Bounds(0, 0, metadata.width, metadata.height);
          var tileSize = baseLayer.getTileSize();
          var lon = metadata.width / 2;
          var lat = metadata.height / 2;
          var mapOptions = {
             maxExtent: maxExtent,
             maximized: true
          };



          var layerSwitcher = this.createLayerSwitcher();


          var options = {
             resolutions: resolutions,
             maxExtent: maxExtent,
             tileSize: tileSize,
             controls: [
                //new OpenLayers.Control.Navigation({zoomWheelEnabled : true, mouseWheelOptions: {interval: 1}, cumulative: false}),
                new OpenLayers.Control.Navigation(), new OpenLayers.Control.PanZoomBar(), new OpenLayers.Control.MousePosition(),
                new OpenLayers.Control.OverviewMap({
                       div: document.getElementById('overviewMap' + this.model.get('id')),
                       //size: new OpenLayers.Size(metadata.width / Math.pow(2, openURLLayer.getViewerLevel()), metadata.height / Math.pow(2,(openURLLayer.getViewerLevel()))),
                       size: new OpenLayers.Size(metadata.width / Math.pow(2, baseLayer.getViewerLevel()), metadata.height / Math.pow(2, (baseLayer.getViewerLevel()))),
                       minRatio: 1,
                       maxRatio: 1024,
                       mapOptions: mapOptions
                    }), new OpenLayers.Control.KeyboardDefaults()]
          };



          this.map = new OpenLayers.Map("map" + this.model.get('id'), options);
          
          this.addBaseLayer(baseLayer);
          this.map.setCenter(new OpenLayers.LonLat(lon, lat), 2);
          self.createOverviewMap();
       },
       initAutoAnnoteTools : function () {

          var self = this;

          var handleMapClick = function handleMapClick(evt) {

             if (!self.getUserLayer().magicOnClick) return;
             // don't select the point, select a square around the click-point
             var clickbuffer = 18; //how many pixels around should it look?
             var sw = self.map.getLonLatFromViewPortPx(new
                 OpenLayers.Pixel(evt.xy.x-clickbuffer , evt.xy.y+clickbuffer) );
             var ne = self.map.getLonLatFromViewPortPx(new
                 OpenLayers.Pixel(evt.xy.x+clickbuffer , evt.xy.y-clickbuffer) );

             // open a popup window, supplying the coords in GET
             var url =  sw.lon+','+sw.lat+','+ne.lon+','+ne.lat;
             alert(url);
          }
          this.map.events.register('click', this.map, handleMapClick);


       },
       /**
        * Init the toolbar
        */
       initToolbar: function () {
          var toolbar = $('#toolbar' + this.model.get('id'));
          var self = this;
          toolbar.find('input[name=select]').button({
                 //text : false,
                 // icons: {
                 // primary: "ui-icon-seek-start"
                 // }
              });
          toolbar.find('button[name=delete]').button({
                 text: false,
                 icons: {
                    primary: "ui-icon-trash"

                 }
              });

          toolbar.find('button[name=ruler]').button({
                 text: false,
                 icons: {
                    secondary: "ui-icon-arrow-2-ne-sw"

                 }
              });
          toolbar.find('input[id^=ruler]').button({
                 text: true,
                 icons: {
                    secondary: "ui-icon-arrow-2-ne-sw"

                 }
              });


          toolbar.find('input[name=rotate]').button();
          toolbar.find('input[name=resize]').button();
          toolbar.find('input[name=drag]').button();
          toolbar.find('input[name=irregular]').button();
          toolbar.find('span[class=nav-toolbar]').buttonset();
          toolbar.find('span[class=draw-toolbar]').buttonset();
          toolbar.find('span[class=edit-toolbar]').buttonset();
          toolbar.find('span[class=delete-toolbar]').buttonset();
          toolbar.find('span[class=ruler-toolbar]').buttonset();

          toolbar.find('input[id=none' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().controls.select.unselectAll();
             self.getUserLayer().toggleControl("none");
             self.getUserLayer().enableHightlight();
          });
          toolbar.find('input[id=select' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().toggleControl("select");
             self.getUserLayer().disableHightlight();
          });
          /*toolbar.find('input[id=freehand' + this.model.get('id') + ']').click(function () {
           self.getUserLayer().controls.select.unselectAll();
           self.getUserLayer().toggleControl("freehand");
           self.getUserLayer().disableHightlight();
           });*/
          toolbar.find('input[id=regular4' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().controls.select.unselectAll();
             self.getUserLayer().setSides(4);
             self.getUserLayer().toggleControl("regular");
             self.getUserLayer().disableHightlight();
          });
          toolbar.find('input[id=regular30' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().controls.select.unselectAll();
             self.getUserLayer().setSides(15);
             self.getUserLayer().toggleControl("regular");
             self.getUserLayer().disableHightlight();
          });
          toolbar.find('input[id=polygon' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().controls.select.unselectAll();
             self.getUserLayer().toggleControl("polygon");
             self.getUserLayer().disableHightlight();
          });
          toolbar.find('input[id=magic' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().controls.select.unselectAll();
             self.getUserLayer().toggleControl("select");
             self.getUserLayer().magicOnClick = true;
             self.getUserLayer().disableHightlight();
          });
          toolbar.find('input[id=modify' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().toggleEdit();
             self.getUserLayer().toggleControl("modify");
             self.getUserLayer().disableHightlight();
          });
          toolbar.find('input[id=delete' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().controls.select.unselectAll();
             self.getUserLayer().toggleControl("select");
             self.getUserLayer().deleteOnSelect = true;
             self.getUserLayer().disableHightlight();
          });
          toolbar.find('input[id=rotate' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().toggleRotate();
             self.getUserLayer().disableHightlight();
          });
          toolbar.find('input[id=resize' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().toggleResize();
             self.getUserLayer().disableHightlight();

          });
          toolbar.find('input[id=drag' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().toggleDrag();
             self.getUserLayer().disableHightlight();
          });
          toolbar.find('input[id=ruler' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().controls.select.unselectAll();
             self.getUserLayer().toggleControl("line");
             self.getUserLayer().measureOnSelect = true;
             self.getUserLayer().disableHightlight();
          });
          /*toolbar.find('input[id=irregular' + this.model.get('id') + ']').click(function () {
           self.getUserLayer().toggleIrregular();
           });
           toolbar.find('input[id=irregular' + this.model.get('id') + ']').hide();*/

       },
       /**
        * Collect data and call appropriate methods in order to add Vector Layers on the Map
        */
       initVectorLayers: function (ontologyTreeView) {
          var self = this;
          window.app.models.users.fetch({
                 success: function () {
                    window.app.models.users.each(function (user) {
                       var layerAnnotation = new AnnotationLayer(user.prettyName(), self.model.get('id'), user.get('id'), user.get('color'), ontologyTreeView, self, self.map );
                       layerAnnotation.isOwner = (user.get('id') == window.app.status.user.id);
                       layerAnnotation.loadAnnotations(self);
                    });
                 }
              });
       },

       initAnnotationsTabs : function(){
          this.annotationsPanel = new AnnotationsPanel({
                 el : this.el,
                 model : this.model
              }).render();

       },
       /**
        * Create a draggable Panel containing a tree which represents the Ontology
        * associated to the Image
        */
       initOntology: function () {
          var self = this;
          new OntologyPanel({
                 el : this.el,
                 model : this.model,
                 callback : self.initVectorLayers,
                 browseImageView : self
              }).render();

       },
       /**
        * Bind controls to the map
        * @param controls the controls we want to bind
        */
       initTools: function (controls) {
          for (var key in controls) {
             this.map.addControl(controls[key]);
          }
       }
    });




var DraggablePanelView = Backbone.View.extend({
       tagName : "div",

       initialize: function(options) {
          this.el = options.el;
          this.template = options.template;
          /*this.dialogAttr = options.dialogAttr;
           if (!options.dialogAttr.width) this.dialogAttr.width = 'auto';
           if (!options.dialogAttr.height) this.dialogAttr.height = 'auto';*/
       },
       render: function() {
          //
          var self = this;
          $(this.el).html(this.template);
          var width = $(this.el).width();
          var height = $(this.el).height();
          $(this.el).draggable({
                 cancel: '.slider',
                 start: function(event, ui) {
                    width = $(self.el).width();
                    height = $(self.el).height();
                 },
                 drag: function (event, ui) {
                    $(this).css("width", width);
                    $(this).css("height", height);
                 }
              });
          /*var dialog = $(this.dialogAttr.dialogID).dialog({
           open: function (e, ui) {
           $(this).closest('.ui-dialog').css(self.dialogAttr.css);
           //$(this).siblings('div.ui-dialog-titlebar').remove();
           },
           resizable: true,
           draggable : true,
           closeText: 'hide',
           width: this.dialogAttr.width,
           height: this.dialogAttr.height,
           closeOnEscape : false,
           modal: false
           //close : this.dialogAttr.close,
           //buttons: this.dialogAttr.buttons
           });*/
          return this;
       }



    });var ExplorerTabs = Backbone.View.extend({
       tagName : "div",

       /**
        * ExplorerTabs constructor
        * @param options
        */
       initialize: function(options) {
          this.tabs = [], //that we are browsing
          this.container = options.container
       },
       /**
        * Grab the layout and call ask for render
        */
       render : function() {
          var self = this;
          require(["text!application/templates/explorer/Tabs.tpl.html"], function(tpl) {
             self.doLayout(tpl);
          });
          return this;
       },
       /**
        * Render the html into the DOM element associated to the view
        * @param tpl
        */
       doLayout: function(tpl) {
          var self = this;
          $(this.el).html(_.template(tpl, {}));
          var tabs = $(this.el).children('.tabs');
          /*tabs.tabs().find( ".ui-tabs-nav" ).sortable({ axis: "x" });*/
          tabs.tabs({
                 add: function(event, ui) {
                    $("#"+ui.panel.id).parent().parent().css('height', "100%");
                    if (ui.panel.id != ("tabs-dashboard-"+window.app.status.currentProject)
                        && (ui.panel.id != "tabs-images-"+window.app.status.currentProject)
                        && ui.panel.id != ("tabs-annotations-"+window.app.status.currentProject)) {
                       tabs.find("ul").find("a[href=#"+ui.panel.id+"]").parent().append("<span class='ui-icon ui-icon-close'>Remove Tab</span>");
                       tabs.find("ul").find("a[href=#"+ui.panel.id+"]").parent().find("span.ui-icon-close" ).click(function() {
                          var index = $( "li", tabs ).index( $( this ).parent() );
                          self.removeTab(index);
                       });
                       
                       tabs.tabs('select', '#' + ui.panel.id);
                    }
                    $("#"+ui.panel.id).attr('style', 'width:100%;height:100%;;overflow:auto;');
                 },
                 show: function(event, ui){
                    $("#"+ui.panel.id).attr('style', 'width:100%;height:100%;overflow:auto;');
                    return true;
                 },
                 select: function(event, ui) {
                    location.href = ui.tab.href; //follow url
                 }
              });

          $("ul.tabs a").css('height', $("ul.tabs").height())
          return this;
       },
       /**
        *  Add a Tab containing a BrowseImageView instance
        *  @idImage : the id of the Image we want to display
        *  @options : some init options we want to pass the the BrowseImageView Instance
        */
       addBrowseImageView : function(idImage, options) {
          var self = this;
          var tab = this.getBrowseImageView(idImage);
          if (tab != null) {
             tab.view.show(options);
             return;
          }
          var tabs = $(self.el).children('.tabs');
          new ImageInstanceModel({id : idImage}).fetch({
                 success : function(model, response) {
                    var view = new BrowseImageView({
                           model : model,
                           initCallback : function(){view.show(options)},
                           el: tabs
                        }).render();
                    self.tabs.push({idImage : idImage,view : view});
                 }
              });
       },
       /**
        * Return the reference to a BrowseImageView instance
        * contained in a tab
        * @param idImage the ID of an Image contained in a BrowseImageView
        */
       getBrowseImageView : function(idImage) {
          var object  = _.detect(this.tabs, function(object) {
             
             return object.idImage == idImage;
          });
          return object != null ? object : null;
       },
       /**
        * Remove a Tab
        * @param index the identifier of the Tab
        */
       removeTab : function (index) {
          this.tabs.splice(index,1);
          var tabs = $(this.el).children('.tabs');
          tabs.tabs( "remove", index);
       },
       /**
        * Show a tab
        * @param index the identifier of the Tab
        */
       showTab : function(index) {
          var tabs = $("#explorer > .browser").children(".tabs");
          tabs.tabs("select", "#tabs-image-"+window.app.status.currentProject+"-"+index+"-");
          return;
       },
       /**
        * Return the number of opened tabs
        */
       size : function() {
          return _.size(this.tabs);
       },
       /**
        * Close all the Tabs
        */
       closeAll : function() {
          var tabs = $(this.el).children('.tabs');
          for (var i = tabs.tabs('length') - 1; i >= 0; i--) {
             tabs.tabs('remove', i);
          }
          this.tabs = [];
          tabs.tabs("destroy");
          $(this.el).hide();
          $(this.el).parent().find('.noProject').show();
       },
       /**
        * Add a ProjectDashBoardView instance in the first Tab
        * @param dashboard the ProjectDashBoardView instance
        */
       addDashboard : function(dashboard) {
          
          var tabs = $(this.el).children('.tabs');
          tabs.tabs("add", "#tabs-dashboard-"+window.app.status.currentProject, 'Dashboard');
             tabs.tabs("add", "#tabs-images-"+window.app.status.currentProject, 'Images');
          tabs.tabs("add", "#tabs-annotations-"+window.app.status.currentProject, 'Annotations');
          $("#explorer > .browser").show();
          $("#explorer > .noProject").hide();
          this.tabs.push({
                 idImage : 0,
                 view : dashboard
              });
          this.tabs.push({
                 idImage : 1,
                 view : dashboard
              });
          this.tabs.push({
                 idImage : 2,
                 view : dashboard
              });
       },
       /**
        * Ask to the dashboard view to refresh
        */
       getDashboard : function () {
          var dashboardTab = _.detect(this.tabs, function(object) {
             return object.idImage == 0;
          });
          return dashboardTab;
       }
    });
var ImageThumbView = Backbone.View.extend({

       events: {

       },

       initialize: function(options) {
          this.id = "thumb"+this.model.get('id');
          _.bindAll(this, 'render');
       },

       render: function() {
          this.model.set({ project : window.app.status.currentProject });
          var self = this;
          require(["text!application/templates/image/ImageThumb.tpl.html"], function(tpl) {
             $(self.el).html(_.template(tpl, self.model.toJSON()));
          });
          return this;
       }
    });


var ImageSelectView = Backbone.View.extend({

       events: {

       },

       initialize: function(options) {
          this.id = "thumb"+this.model.get('id');
          _.bindAll(this, 'render');
       },

       render: function() {
          var self = this;
          this.model.set({ project : window.app.status.currentProject });
          var self = this;
          require(["text!application/templates/image/ImageChoice.tpl.html"], function(tpl) {
             $(self.el).html(_.template(tpl, self.model.toJSON()));
          });
          return this;
       }
    });
var ImageView = Backbone.View.extend({
    tagName : "div",
    initialize: function(options) {
        this.images = null, //array of images that are printed
        this.container = options.container;
        this.page = options.page;
        if (this.page == undefined) this.page = 0;
    },
    render: function() {
        var self = this;

        self.appendThumbs(self.page);

        $(window).scroll(function(){
            if  (($(window).scrollTop() + 100) >= $(document).height() - $(window).height()){
                self.appendThumbs(++self.page);
            }
        });
        return this;
    },
    appendThumbs : function(page) {
        var self = this;
        var cpt = 0;
        var nb_thumb_by_page = 5000;
        var inf = Math.abs(page) * nb_thumb_by_page;
        var sup = (Math.abs(page) + 1) * nb_thumb_by_page;

        self.tabsContent = new Array();

        self.model.each(function(image) {
            if ((cpt >= inf) && (cpt < sup)) {
                var thumb = new ImageThumbView({
                    model : image,
                    className : "thumb-wrap",
                    id : "thumb"+image.get('id')
                }).render();
                $(self.el).append(thumb.el);
            }
            cpt++;
            self.tabsContent.push(image.id);
        });
    },
    /**
     * Add the thumb image
     * @param image Image model
     */
    add : function(image) {s
        var self = this;
        var thumb = new ImageThumbView({
            model : image,
            className : "thumb-wrap",
            id : "thumb"+image.get('id')
        }).render();
        $(self.el).append(thumb.el);

    },
    /**
     * Remove thumb image with id
     * @param idImage  Image id
     */
    remove : function (idImage) {
                $("#thumb"+idImage).remove();
    },
    /**
     * Refresh thumb with newImages collection:
     * -Add images thumb from newImages which are not already in the thumb set
     * -Remove images which are not in newImages but well in the thumb set
     * @param newImages newImages collection
     */
    refresh : function(newImages) {
        var self = this;
        var arrayDeletedImages = self.tabsContent;
        newImages.each(function(image) {
            //if image is not in table, add it
            if(_.indexOf(self.tabsContent, image.id)==-1){
                self.add(image);
                self.tabsContent.push(image.id);
            }
            /*
             * We remove each "new" image from  arrayDeletedImage
             * At the end of the loop, element from arrayDeletedImages must be deleted because they aren't
             * in the set of new images
             */
            arrayDeletedImages = _.without(arrayDeletedImages,image.id);
        });

        arrayDeletedImages.forEach(function(removeImage) {
            self.remove(removeImage);
            self.tabsContent = _.without(self.tabsContent,removeImage);
        }
                );

    }
});
var ImageTabsView = Backbone.View.extend({
    tagName : "div",
    images : null, //array of images that are printed
    idProject : null,
    initialize: function(options) {
        this.container = options.container;
        this.page = options.page;
        this.idProject = options.idProject;
        if (this.page == undefined) this.page = 0;
    },
    render: function() {
        var self = this;

        

        $(self.el).append('<table id=\"tablegrid\"><tr><td  WIDTH=\"50%\"><table id=\"listimage'+self.idProject+'\"></table><div id=\"pagerimage'+this.idProject+'\"></div></td><td width=50></td><td WIDTH=\"40%\">info</td></tr></table>');


        var lastsel;
        $(self.el).find("#listimage"+self.idProject).jqGrid({
            datatype: "local",
            //height: 100%,
            //height: "100%",
            width: "700",
            colNames:['id','filename','type','added','See'],
            colModel:[
                {name:'id',index:'id', width:30},
                {name:'filename',index:'filename', width:300},
                {name:'type',index:'type', width:40},
                {name:'added',index:'added', width:70,sorttype:"date"},
                {name:'See',index:'See', width:150,sortable:false}
            ],
            onSelectRow: function(id){
                if(id && id!==lastsel){
                    alert("Click on "+id);
                    lastsel=id;
                }
            },
            rowNum:10,
            pager: '#pagerimage'+self.idProject,
            sortname: 'id',
            viewrecords: true,
            sortorder: "asc",
            caption:"Array Example"
        });
        $(self.el).find("#listimage"+self.idProject).jqGrid('navGrid','#pagerimage'+self.idProject,{edit:false,add:false,del:false});

        var i=0;
        var data = [];

        self.model.each(function(image) {
            
            var createdDate = new Date();
            createdDate.setTime(image.get('created'));

            var url = '<a href=\"#tabs-image-'+self.idProject+'-' + image.id + '-\">Click here to see the image</a>'
            
            data[i] = {
                id: image.id,
                filename: image.get('filename'),
                type : image.get('mime'),
                added : createdDate.getFullYear() + "-" + createdDate.getMonth() + "-" + createdDate.getDate(),
                See : url
            };
            i++;
        });

        for(var j=0;j<=data.length;j++) {
            
            $(self.el).find("#listimage"+self.idProject).jqGrid('addRowData',j+1,data[j]);
        }


        $(self.el).find("#listimage"+self.idProject).jqGrid('sortGrid','filename',false);
        // $("#list3").jqGrid('sortGrid','filename',true);
        return this;
    },
    refresh : function(newImages) {
        var self = this;
    }
});
var OntologyPanelView = Backbone.View.extend({
       $tree : null,
       $infoOntology : null,
       $infoTerm : null,
       $panel : null,
       $addTerm : null,
       $editTerm : null,
       $deleteTerm : null,
       $buttonAddTerm : null,
       $buttonEditTerm : null,
       $buttonDeleteTerm : null,
       $buttonEditOntology : null,
       $buttonDeleteOntology : null,
       ontologiesPanel : null,
       expanse : false,
       events: {
          "click .addTerm": "addTerm",
          "click .editTerm": "editTerm",
          "click .deleteTerm": "deleteTerm",
          "click .editOntology": "editOntology",
          "click .deleteOntology": "deleteOntology"
       },
       initialize: function(options) {
          this.container = options.container;
          this.ontologiesPanel = options.ontologiesPanel;
       },
       render: function() {
          var self = this;
          self.$panel = $(self.el);
          self.$tree = self.$panel.find("#treeontology-"+self.model.id);
          self.$infoOntology = self.$panel.find("#infoontology-"+self.model.id);
          self.$infoTerm = self.$panel.find("#infoterm-"+self.model.id);

          self.$addTerm = self.$panel.find('#dialog-add-ontology-term');
          self.$editTerm = self.$panel.find('#dialog-edit-ontology-term');
          self.$deleteTerm = self.$panel.find('#dialogsTerm');

          self.$buttonAddTerm = self.$panel.find($('#buttonAddTerm'+self.model.id));
          self.$buttonEditTerm = self.$panel.find($('#buttonEditTerm'+self.model.id));
          self.$buttonDeleteTerm = self.$panel.find($('#buttonDeleteTerm'+self.model.id));

          self.$buttonEditOntology = self.$panel.find($('#buttonEditOntology'+self.model.id));
          self.$buttonDeleteOntology = self.$panel.find($('#buttonDeleteOntology'+self.model.id));

          self.buildOntologyTree();
          self.buildButton();
          self.buildInfoOntologyPanel();
          self.buildInfoTermPanel();

          return this;
       },

       refresh : function() {
          var self = this;
          self.model.fetch({
                 success : function (model, response) {
                    window.app.models.terms.fetch({
                           success : function (model, response) {

                              $('#ontologyTitle'+self.model.id).empty();
                              $('#ontologyTitle'+self.model.id).append(self.model.get('name'));
                              self.clear();
                              self.render();
                           }});
                 }});


       },

       clear : function() {
          var self = this;
          self.$panel.empty();
          require([
             "text!application/templates/ontology/OntologyTabContent.tpl.html"
          ],
              function(tpl) {
                 self.$panel.html(_.template(tpl, { id : self.model.get("id"), name : self.model.get("name")}));
                 return this;
              });

          return this;
       },

       getCurrentTermId : function() {
          var node = this.$tree.dynatree("getActiveNode");
          if(node==null) return null;
          else return node.data.id;
       },

       addTerm : function() {
          var self = this;
          self.$addTerm.remove();

          new OntologyAddOrEditTermView({
                 ontologyPanel:self,
                 el:self.el,
                 ontology:self.model,
                 model:null //add component so no term
              }).render();
       },

       editTerm : function() {
          var self = this;
          self.$editTerm.remove();

          var node = self.$tree.dynatree("getActiveNode");

          if(node==null) {
             window.app.view.message("Term", "You must select a term!", "");
             return;
          }

          new TermModel({id:node.data.id}).fetch({
                 success : function (model, response) {
                    new OntologyAddOrEditTermView({
                           ontologyPanel:self,
                           el:self.el,
                           model:model,
                           ontology:self.model
                        }).render();
                 }});
          return false;
       },


       deleteTerm : function() {
          var self = this;
          var idTerm = self.getCurrentTermId();
          var term = window.app.models.terms.get(idTerm);
          new AnnotationCollection({term:idTerm}).fetch({
                 success : function (collection, response) {
                    if(collection.length==0) self.buildDeleteTermConfirmDialog(term);
                    else self.buildDeleteTermWithAnnotationConfirmDialog(term,collection.length);
                 }});
       },
       editOntology : function() {
          var self = this;
          $('#editontology').remove();
          self.editOntologyDialog = new EditOntologyDialog({ontologyPanel:self,el:self.el,model:self.model}).render();
       },
       deleteOntology : function() {
          var self = this;
          //check if projects has this ontology
          new ProjectCollection({ontology:self.model.id}).fetch({
                 success:function(collection,response) {
                    if(collection.length>0) self.refuseDeleteOntology(collection.length);
                    else self.acceptDeleteOntology();

                 }})
       },
       refuseDeleteOntology : function(numberOfProject) {
          var self = this;
          $("#delete-ontology-refuse").replaceWith("");
          require(["text!application/templates/ontology/OntologyDeleteRefuseDialog.tpl.html"], function(tpl) {
             var dialog =  new ConfirmDialogView({
                    el:'#dialogsDeleteOntologyRefuse',
                    template : _.template(tpl, {name : self.model.get('name'),numberOfProject:numberOfProject}),
                    dialogAttr : {
                       dialogID : '#dialogsDeleteOntologyRefuse',
                       width : 400,
                       height : 200,
                       buttons: {
                          "close": function() {
                             //doesn't work! :-(
                             $('#dialogsDeleteOntologyRefuse').dialog( "close" ) ;
                          }
                       },
                       close :function (event) {
                       }
                    }
                 }).render();
          });
       },
       acceptDeleteOntology : function() {
          var self = this;
          $("#delete-ontology-confirm").replaceWith("");
          require(["text!application/templates/ontology/OntologyDeleteConfirmDialog.tpl.html"], function(tpl) {
             new ConfirmDialogView({
                    el:'#dialogsDeleteOntologyAccept',
                    template : _.template(tpl, {ontology : self.model.get('name')}),
                    dialogAttr : {
                       dialogID : '#dialogsDeleteOntologyAccept',
                       width : 400,
                       height : 300,
                       buttons: {
                          "Delete ontology and all terms": function() {
                             var dialog = this;
                             self.model.destroy({
                                    success : function (model, response) {
                                       $(dialog).dialog( "close" ) ;
                                       $(dialog).dialog("destroy");
                                       self.ontologiesPanel.refresh();
                                    },error: function (model, response) {
                                       var json = $.parseJSON(response.responseText);
                                    }});

                          },
                          "cancel": function() {
                             var dialog = this;
                             $(dialog).dialog( "close" ) ;
                             $(dialog).dialog("destroy");
                          }
                       },
                       close :function (event) {
                       }
                    }
                 }).render();
          });
       },
       selectTerm : function(idTerm) {
          var self = this;
          self.$tree.dynatree("getTree").activateKey(idTerm);
       },
       buildDeleteTermConfirmDialog : function(term) {
          var self = this;
          require(["text!application/templates/ontology/OntologyDeleteTermConfirmDialog.tpl.html"], function(tpl) {
             var dialog =  new ConfirmDialogView({
                    el:'#dialogsTerm',
                    template : _.template(tpl, {term : term.get('name'),ontology : self.model.get('name')}),
                    dialogAttr : {
                       dialogID : '#dialogsTerm',
                       width : 400,
                       height : 300,
                       buttons: {
                          "Delete term": function() {
                             new BeginTransactionModel({}).save({}, {
                                    success: function (model, response) {
                                       self.deleteTermWithoutAnnotationTerm(term);
                                    },
                                    error: function (model, response) {
                                       window.app.view.message("ERROR", "error transaction begin", "error");
                                    }
                                 });
                          },
                          "Cancel": function() {
                             //doesn't work! :-(
                             $('#dialogsTerm').dialog( "close" ) ;
                          }
                       },
                       close :function (event) {
                       }
                    }
                 }).render();
          });
       },
       /**
        * TODO: This method can be merge with  buildDeleteTermWithoutAnnotationConfirmDialog
        * But it's now separete to allow modify with delete term with annotation (which is critical)
        * @param term
        * @param numberOfAnnotation
        */
       buildDeleteTermWithAnnotationConfirmDialog : function(term,numberOfAnnotation) {
          //TODO:ask confirmation (and delete term  with annotation? or not...)
          var self = this;
          require(["text!application/templates/ontology/OntologyDeleteTermWithAnnotationConfirmDialog.tpl.html"], function(tpl) {
             var dialog =  new ConfirmDialogView({
                    el:'#dialogsTerm',
                    template : _.template(tpl, {term : term.get('name'),ontology : self.model.get('name'),numberOfAnnotation:numberOfAnnotation}),
                    dialogAttr : {
                       dialogID : '#dialogsTerm',
                       width : 400,
                       height : 300,
                       buttons: {
                          "Delete all link and delete term": function() {
                             new BeginTransactionModel({}).save({}, {
                                    success: function (model, response) {
                                       self.deleteTermWithAnnotationTerm(term);
                                    },
                                    error: function (model, response) {
                                       window.app.view.message("ERROR", "error transaction begin", "error");
                                    }
                                 });
                          },
                          "Cancel": function() {
                             //doesn't work! :-(
                             $('#dialogsTerm').dialog( "close" ) ;
                          }
                       },
                       close :function (event) {
                       }
                    }
                 }).render();
          });
       },
       /**
        * Delete a term which can have annotation and relation
        * @param term  term that must be deleted
        */
       deleteTermWithAnnotationTerm : function(term) {
          var self = this;
          var counter = 0;
          //delete all annotation term
          new AnnotationCollection({term:term.id}).fetch({
                 success:function (collection, response){
                    if (collection.size() == 0) {
                       self.removeAnnotationTermCallback(0,0, term);
                       return;
                    }
                    collection.each(function(annotation) {
                       new AnnotationTermModel({
                              term:term.id,
                              annotation:annotation.id
                           }).destroy({success : function (model, response) {
                              self.removeAnnotationTermCallback(collection.length, ++counter, term);
                           }});
                    });
                 }
              });
       },
       /**
        * Delete a term which can have relation but no annotation
        * @param term term that must be deleted
        */
       deleteTermWithoutAnnotationTerm : function(term) {
          var self = this;
          var counter = 0;
          //get all relation with this term and remove all of them
          new RelationTermCollection({term:term.id}).fetch({
                 success:function (collection, response){
                    if (collection.size() == 0) {
                       self.removeRelationTermCallback(0,0, term);
                       return;
                    }
                    collection.each(function(item) {
                       var json = item.toJSON();
                       new RelationTermModel({
                              relation:json.relation.id,
                              term1:json.term1.id,
                              term2:json.term2.id
                           }).destroy({success : function (model, response) {
                              self.removeRelationTermCallback(collection.length, ++counter, term);
                           }});

                    });

                 }});
       },
       removeAnnotationTermCallback : function(total, counter, term) {
          var self = this;
          if (counter < total) return;
          //all annotation-term are deleted for this term: delete term like a term with no annotation
          self.deleteTermWithoutAnnotationTerm(term);

       },
       removeRelationTermCallback : function(total, counter, term) {
          var self = this;
          if (counter < total) return;
          //term has no relation, delete term
          new TermModel({id:term.id}).destroy({
                 success : function (model, response) {
                    new EndTransactionModel({}).save();
                    window.app.view.message("Term", response.message, "");
                    self.refresh();
                    $('#dialogsTerm').dialog( "close" ) ;
                 },
                 error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    $("#delete-term-error-message").empty();
                    $("#delete-term-error-label").show();
                    $("#delete-term-error-message").append(json.errors)
                 }});
       },

       buildButton : function() {
          var self = this;

          self.$buttonAddTerm.button({
                 icons : {secondary: "ui-icon-plus" }
              });
          self.$buttonEditTerm.button({
                 icons : {secondary: "ui-icon-pencil" }
              });
          self.$buttonDeleteTerm.button({
                 icons : {secondary: "ui-icon-trash" }
              });

          self.$buttonEditOntology.button({
                 icons : {secondary: "ui-icon-pencil" }
              });
          self.$buttonDeleteOntology.button({
                 icons : {secondary: "ui-icon-trash" }
              });


       },
       buildInfoOntologyPanel : function() {
          var self = this;
          //bad code with html but waiting to know what info is needed...
          self.$infoOntology.empty();

          var idUserOwner = self.model.get('user');
          var userOwner = window.app.models.users.get(idUserOwner);
          var sharedTo = "Nobody";
          var users = self.model.get('users');
          if (_.size(users) > 0) {
             var userNames = []
             _.each(users,
                 function(idUser){
                    if (idUser == idUserOwner) return;
                    userNames.push(window.app.models.users.get(idUser).prettyName());
                 });
             sharedTo = userNames.join(', ');
          }
          var tpl = _.template("<div id='userontologyinfo-{{id}}' style='padding:5px;'><ul><li><b>Ontology</b> : {{ontologyName}}.</li><li><b>Owner</b> : {{owner}}.</li><li><b>Shared to</b> : {{sharedTo}}.</li><li class='projectsLinked'></li></ul></div>", { id : self.model.id, ontologyName : self.model.get('name'), owner : userOwner.prettyName(), sharedTo :  sharedTo});
          self.$infoOntology.html(tpl);

          //Load project linked to the ontology async
          new OntologyProjectModel({ontology : self.model.id}).fetch({
                 success : function (collection, response )  {
                    var projectsLinked = []
                    collection.each(function (project) {
                       var tpl = _.template("<a href='#tabs-dashboard-{{idProject}}'>{{projectName}}</a>", {idProject : project.get('id'), projectName : project.get('name')});
                       projectsLinked.push(tpl);
                    });
                    var tpl = _.template("<b>Projects</b> : {{projects}}", {projects : projectsLinked.join(", ")});
                    self.$infoOntology.find('.projectsLinked').html(tpl);
                 }
              });

       },
       buildInfoTermPanel : function() {

       },

       buildOntologyTree : function() {
          var self = this;
          var currentTime = new Date();

          self.$tree.empty();
          self.$tree.dynatree({
                 children: self.model.toJSON(),
                 onExpand : function() { },
                 onClick: function(node, event) {

                    /*var title = node.data.title;
                     var color = "black";
                     var htmlNode = "<a href='#'><label style='color:{{color}}'>{{title}}</label></a>" ;
                     var nodeTpl = _.template(htmlNode, {title : title, color : color});
                     node.setTitle(nodeTpl);  */

                    if(window.app.models.ontologies.get(node.data.id)==undefined)
                       self.updateInfoPanel(node.data.id,node.data.title);
                 },
                 onSelect: function(select, node) {
                    self.updateInfoPanel(node.data.id,node.data.title);
                 },
                 onActivate : function(node) {
                    self.updateInfoPanel(node.data.id,node.data.title);
                 },
                 onDblClick: function(node, event) {
                 },
                 onRender: function(node, nodeSpan) {
                    self.$tree.find("a.dynatree-title").css("color", "black");
                 },
                 //generateIds: true,
                 // The following options are only required, if we have more than one tree on one page:
                 initId: "treeDataOntology-"+self.model.id + currentTime.getTime(),
                 cookieId: "dynatree-Ontology-"+self.model.id+ currentTime.getTime(),
                 idPrefix: "dynatree-Ontology-"+self.model.id+ currentTime.getTime()+"-" ,
                 debugLevel: 3
              });
          self.$tree.dynatree("getRoot").visit(function(node){

             if (node.children != null) return; //title is ok

             var title = node.data.title
             var color = node.data.color
             var htmlNode = "<a href='#ontology/{{idOntology}}/{{idTerm}}' onClick='window.location.href = this.href;'>{{title}} <span style='background-color:{{color}}'>&nbsp;&nbsp;&nbsp;&nbsp;</span></a>"
             var nodeTpl = _.template(htmlNode, {idOntology : self.model.id, idTerm : node.data.id, title : title, color : color});
             node.setTitle(nodeTpl);
          });
          //expand all nodes
          self.$tree.dynatree("getRoot").visit(function(node){
             node.expand(true);
          });
       },

       updateInfoPanel : function(idTerm,name) {
          var self = this;
          // Create and populate the data table.
          var data = new google.visualization.DataTable();
          data.addColumn('string', 'Project');
          data.addColumn('number', 'Number of annotations');

          var i = 0;
          var statsCollection = new StatsCollection({term:idTerm});

          var drawPieChart = function(collection, response) {
             var divID = "piechart-"+self.model.id;
             $("#"+divID).empty();
             var dataToShow = false;
             data.addRows(_.size(collection));
             collection.each(function(stat) {
                /*colors.push(stat.get('color'));*/
                if (stat.get('value') > 0) dataToShow = true;
                data.setValue(i,0, stat.get('key'));
                data.setValue(i,1, stat.get('value'));
                i++;
             });

             if (!dataToShow) {
                $("#"+divID).hide();
                return
             };
             new google.visualization.PieChart(document.getElementById(divID)).
                 draw(data, {width: 500, height: 300,title:"Annotations by projects"});
             $("#"+divID).show();
          };
          var drawColumnChart = function (collection, response) {
             var divID = "columchart-"+self.model.id;
             $("#"+divID).empty();
             var dataToShow = false;
             // Create and populate the data table.
             var data = new google.visualization.DataTable();

             data.addRows(_.size(collection));

             data.addColumn('string', '');
             data.addColumn('number', 0);

             var j = 0;
             collection.each(function(stat) {
                if (stat.get('value') > 0) dataToShow = true;
                data.setValue(j, 0, stat.get("key"));
                data.setValue(j, 1, stat.get("value"));
                j++;
             });

             if (!dataToShow) {
                $("#"+divID).hide();
                return
             };


             // Create and draw the visualization.
             new google.visualization.ColumnChart(document.getElementById(divID)).
                 draw(data,
                 {title:"Annotations by projects",
                    width:500, height:300,
                    vAxis: {title: "Number of annotations"},
                    hAxis: {title: "Project"}}
             );
             $("#"+divID).show();
          };
          statsCollection.fetch({
                 success : function(model, response) {
                    drawColumnChart(model, response);
                    drawPieChart(model, response);
                 }
              });




          /*self.$infoTerm.append("<div id=\"termchart-"+self.model.id +"\"><h3>"+name+"</h3><div id=\"terminfo-"+self.model.id +"\"></div>");

           new TermModel({id:idTerm}).fetch({
           success : function (model, response) {
           var tpl = _.template("<a href='#' class='editTerm'>Color : <span name='color' id='color' style='display:inline;background-color: {{color}};'>&nbsp;&nbsp;&nbsp;&nbsp;</span></a><br />", {color : model.get('color')});
           $("#terminfo-"+self.model.id).append(tpl);
           var statsCollection = new StatsCollection({term:idTerm});
           var statsCallback = function(collection, response) {
           collection.each(function(stat) {
           $("#terminfo-"+self.model.id).append("Project "+stat.get('key') + ": " + stat.get('value') + " annotations<br>");
           });

           //$("#termchart-"+self.model.id).panel({
           //       collapsible:false
           //    });
           }
           statsCollection.fetch({
           success : function(model, response) {
           statsCallback(model, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
           }
           });
           ;
           }});  */

       }
    });/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:12
 * To change this template use File | Settings | File Templates.
 */
var OntologyView = Backbone.View.extend({
       tagName : "div",
       self : this,
       alreadyBuild : false,
       $tabsOntologies : null,
       ontologiesPanel : null,
       idOntology : null,
       addOntologyDialog : null,
       events: {
          "click .addOntology": "showAddOntologyPanel",
          "click .refreshOntology" : "refresh"
       },
       initLoading: function() {
          $(this.el).find("#ontologyLoading").html("Loading...");
       },
       showLoading : function() {
          $(this.el).find("#ontologytreepanel").hide();
          $(this.el).find("#ontologyLoading").show();
       },
       hideLoading : function() {
          $(this.el).find("#ontologyLoading").hide();
          $(this.el).find("#ontologytreepanel").show();
       },
       initialize: function(options) {
          this.container = options.container;
          this.idOntology = options.idOntology;
          this.idTerm =  options.idTerm;
       },
       refresh : function() {
          var self = this;
          window.app.models.terms.fetch({
                 success : function (collection, response) {
                    window.app.models.ontologies.fetch({
                           success : function (collection, response) {
                              self.render();
                           }});
                 }
              });

       },
       refresh : function(idOntology, idTerm) {
          var self = this;
          this.idOntology = idOntology;
          this.idTerm = idTerm;
          window.app.models.terms.fetch({
                 success : function (collection, response) {
                    window.app.models.ontologies.fetch({
                           success : function (collection, response) {
                              self.render();
                           }});
                 }
              });
       },
       select : function(idOntology) {
          var self = this;
          this.idOntology = idOntology;
          self.render();
       },
       render : function () {
          var self = this;
          require([
             "text!application/templates/ontology/OntologyList.tpl.html"
          ],
              function(tpl) {
                 self.doLayout(tpl);
              });

          return this;
       },
       doLayout: function(tpl) {
          var self = this;
          $(this.el).html(_.template(tpl, {}));
          self.initLoading();
          self.showLoading();
          self.$tabsOntologies = $(self.el).find("#tabsontology");
          $(self.el).find(".addOntology").button({
                 icons : {secondary: "ui-icon-plus" }
              });
          $(self.el).find(".refreshOntology").button({
                 icons : {secondary: "ui-icon-refresh" }
              });
          self.initOntologyTabs();

          self.hideLoading();
          return this;
       },
       showAddOntologyPanel : function() {
          var self = this;
          $('#addontology').remove();
          self.addOntologyDialog = new AddOntologyDialog({ontologiesPanel:self,el:self.el}).render();
       },
       select : function(idOntology,idTerm) {

          var self = this;
          //select ontology
          var selectedOntologyIndex = 0;
          var index = 0;
          self.model.each(function(ontology) {
             //get index of selected ontology
             if(idOntology== ontology.get("id")) {
                selectedOntologyIndex = index;
             }
             index = index + 1;
          });
          if (idTerm != undefined) self.ontologiesPanel[selectedOntologyIndex].selectTerm(idTerm);
          self.$tabsOntologies.accordion( "activate" , selectedOntologyIndex );
       },
       /**
        * Init annotation tabs
        */
       initOntologyTabs : function(){
          var self = this;
          require(["text!application/templates/ontology/OntologyTab.tpl.html", "text!application/templates/ontology/OntologyTabContent.tpl.html"], function(ontologyTabTpl, ontologyTabContentTpl) {
             self.ontologiesPanel = new Array();
             //add "All annotation from all term" tab
             self.model.each(function(ontology) {
                //add x term tab
                self.addOntologyToTab(ontologyTabTpl, ontologyTabContentTpl, { id : ontology.get("id"), name : ontology.get("name")});
                //create project search panel
                var view = new OntologyPanelView({
                       model : ontology,
                       el:$(self.el).find("#tabsontology-"+ontology.id),
                       container : self,
                       ontologiesPanel : self
                    });
                view.render();
                self.ontologiesPanel.push(view);
             });


             if(!self.alreadyBuild) {
                self.$tabsOntologies.accordion({ fillSpace: true });
                $("#tabsontology h3 a").click(function() {
                   window.location = $(this).attr('href'); //follow link
                   return false;
                });

             }

             self.select(self.idOntology,self.idTerm);
             $(".accordeonOntology").css("height", "auto");
             $(".accordeonOntology").css("width", "auto");

          });
       },
       /**
        * Add the the tab with ontology info
        * @param id  ontology id
        * @param name ontology name
        */
       addOntologyToTab : function(ontologyTabTpl, ontologyTabContentTpl, data) {
          /*this.$tabsOntologies.append("<h3><a id='ontologyTitle'"+ data.id+ "href='#ontology/'>"+data.name+"</a></h3>");*/
          this.$tabsOntologies.append(_.template("<h3><a id='ontologyTitle{{ontologyID}}' href='#ontology/{{ontologyID}}'>{{ontologyName}}</a></h3>", { ontologyID : data.id, ontologyName : data.name}));
          this.$tabsOntologies.append(_.template(ontologyTabContentTpl, data));
          //tabs;
          /*         $("#ultabsontology").append(_.template(ontologyTabTpl, data));
           $("#listtabontology").append(_.template(ontologyTabContentTpl, data)); */
       }
    });
var OntologyAddOrEditTermView = Backbone.View.extend({
    ontologyPanel : null, //Ontology panel in ontology view
    ontologyDialog : null, //dialog (add, edit)
    ontology : null,
    $tree : null,
    $panel : null,
    $textboxName : null,
    $colorChooser : null,
    $inputOldColor : null,
    $inputNewColor : null,
    $errorMessage : null,
    $errorLabel : null,
    $addFolderButton : null,
    action : null,
    events: {
        "click .addFolder": "addFolder"
    },
    initialize: function(options) {
        this.container = options.container;
        this.ontologyPanel = options.ontologyPanel;
        this.ontology = options.ontology;
        _.bindAll(this, 'render');
    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/ontology/OntologyAddOrEditTermView.tpl.html"
        ],
               function(ontologyAddOrEditTermViewTpl) {
                   self.doLayout(ontologyAddOrEditTermViewTpl);
               });
        return this;
    },
    doLayout : function(ontologyAddOrEditTermViewTpl) {

        var self = this;

        if(self.model==null) {
            //dialog to add
            self.action = "Add";
            //create an empty model (not saved)
            self.createNewEmpty();
        }
        else self.action = "Edit"; //dialog to edit

        //remove older dialog
        self.$termDialog  = $(self.el).find("#dialog-"+self.action+"-ontology-term");

        $("#dialog-Edit-ontology-term").replaceWith("");
        $("#dialog-Add-ontology-term").replaceWith("");

        var dialog = _.template(ontologyAddOrEditTermViewTpl, {
            oldColor : self.model.get('color'),
            action : self.action
        });
        $(self.el).append(dialog);

        self.$panel = $(self.el);
        self.$termDialog  = $(self.el).find("#dialog-"+self.action+"-ontology-term");
        self.$tree = self.$panel.find("#" + self.action +"ontologytermtree");
        self.$from = self.$panel.find($("#form-" + self.action +"-ontology-term"));
        self.$textboxName = self.$panel.find("#" + self.action +"termname");

        self.$colorChooser = self.$panel.find('#colorpicker1');
        self.$inputOldColor = self.$panel.find('#oldColor');
        self.$inputNewColor = self.$panel.find('#color1');

        self.$addFolderButton  = self.$panel.find('.addFolder');

        self.$errorMessage =  self.$panel.find("#" + self.action +"ontologytermerrormessage");
        self.$errorLabel =  self.$panel.find("#" + self.action +"ontologytermerrorlabel");

        self.$from.submit(function () {
            if(self.action == "Edit")
                self.updatedOntologyTerm();
            else  self.createOntologyTerm();
            return false;});
        self.$from.find("input").keydown(function(e){
            if (e.keyCode == 13) { //ENTER_KEY
                self.$from.submit();
                return false;
            }
        });
        self.clearOntologyTermPanel();
        self.buildNameInfo();
        self.buildColorInfo();
        self.buildParentInfo();

        //Build dialog
        self.ontologyDialog = self.$termDialog.dialog({
            width: "1000",
            autoOpen : false,
            modal:true,
            buttons : {
                "Save" : function() {
                    self.$from.submit();
                },
                "Cancel" : function() {
                    self.$termDialog.dialog("close");
                }
            }
        });
        self.open();
        return this;
    },
    /**
     * Create new empty model with default value
     */
    createNewEmpty : function() {
        var self = this;
        self.model = new TermModel({id:-1,name:"",color:"#ff0000"});
    },

    buildNameInfo : function () {
        var self = this;
        self.$textboxName.bind('keyup mouseup change click',function(e){
            var node = self.$tree.dynatree("getTree").getNodeByKey(self.model.id);
            var color = "#119b04"
            var htmlNode = "<label style='color:{{color}}'>{{title}}</label>"
            var nodeTpl = _.template(htmlNode, {title : self.$textboxName.val(), color : color});
            if (node != null) node.setTitle(nodeTpl);
        });
        self.$textboxName.val(self.model.get("name"));

    },

    buildColorInfo : function() {
        var self = this;
        var colorPicker = self.$colorChooser.farbtastic('#color1');
        var color = self.model.get('color');
        self.$inputOldColor.val(color);
        self.$inputNewColor.val(color);
        self.$inputOldColor.css("background", color);
        self.$inputNewColor.css("background", color);
        self.$inputNewColor.css("color", self.$inputOldColor.css("color"));
    },
    addFolder : function() {
        //create a new node as a folder

        //automatically put the new term node under this folder

        //MOVE IN ADD/UPDATED when you save it, check if the parent node is this new folder, if its true save the relation

    },

    buildParentInfo : function() {
        var self = this;
        self.$addFolderButton.button({
            icons : {secondary: "ui-icon-folder-collapsed" }
        });
        
        self.$tree.empty() ;
        self.$tree.dynatree({
            children: self.ontology.toJSON(),
            onExpand : function() { },
            onRender: function(node, nodeSpan) {
                self.$tree.find("a.dynatree-title").css("color", "black");
            },
            onClick: function(node, event) {
            },
            onSelect: function(select, node) {
            },
            onCustomRender: function(node) {
            },
            onDblClick: function(node, event) {
            },
            dnd: {
                onDragStart: function(node) {
                    /** This function MUST be defined to enable dragging for the tree.
                     *  Return false to cancel dragging of node.
                     */
                    if(node.data.key!=self.model.id) return false;
                    return true;
                },
                onDragStop: function(node) {
                },
                autoExpandMS: 1000,
                preventVoidMoves: true, // Prevent dropping nodes 'before self', etc.
                onDragEnter: function(node, sourceNode) {
                    return true;
                },
                onDragOver: function(node, sourceNode, hitMode) {
                },
                onDragLeave: function(node, sourceNode) {
                },
                onDrop: function(node, sourceNode, hitMode, ui, draggable) {
//                    if(!node.data.isFolder && hitMode=="over")
//                    {
//                        
//                    }
//                    else sourceNode.move(node, hitMode);
                    if(hitMode=="over") {
                        sourceNode.move(node, hitMode);
                        node.data.isFolder = true;
                        node.render();
                    }
                    else sourceNode.move(node, hitMode);
                }
            },
            generateIds: true,
            // The following options are only required, if we have more than one tree on one page:
            initId: "" + self.action +"treeDataOntology-"+self.model.id,
            cookieId: "" + self.action +"dynatree-Ontology-"+self.model.id,
            idPrefix: "" + self.action +"dynatree-Ontology-"+self.model.id+"-" ,
            debugLevel: 0
        });
        
        //if add panel, add the "temp" model to the tree (event if it's not yet a part of the ontology)
        if(self.action=="Add") {
            var node = self.$tree.dynatree("getTree").getNodeByKey(self.ontology.id);
            var childNode = node.addChild({
                title: "",
                key : -1,
                tooltip: "This folder and all child nodes were added programmatically.",
                isFolder: false
            });
        }
        
        //expand all nodes
        self.$tree.dynatree("getRoot").visit(function(node){
            node.expand(true);
        });

        if(self.action=="Edit") {
            self.$textboxName.click();
        }
    },

    getNewName : function() {
        var self = this;
        return self.$textboxName.val();
    },
    getNewParent : function() {
        var self = this;
        var node = self.$tree.dynatree("getTree").getNodeByKey(self.model.id);
        return node.parent.data.id;
    },
    getNewColor : function() {
        return  this.$inputNewColor.val();
    },
    refresh : function() {

    },
    open: function() {
        var self = this;
        self.clearOntologyTermPanel();
        self.ontologyDialog.dialog("open") ;
    },
    clearOntologyTermPanel : function() {
        var self = this;
        self.$errorMessage.empty();
        self.$errorLabel.hide();
    },
    updatedOntologyTerm : function() {
        var self = this;
        self.$errorMessage.empty();
        self.$errorLabel.hide();
        var id = self.model.id;
        var name =  self.getNewName();
        var idOldParent = self.model.get("parent");
        var idParent = self.getNewParent();
        var isOldParentOntology = true;
        if(idOldParent!=null && window.app.models.ontologies.get(idOldParent)==undefined) {
            isOldParentOntology = false;
        }
        var isParentOntology = true;

        if(window.app.models.ontologies.get(idParent)==undefined) {
            isParentOntology = false;
        }
        var color = self.getNewColor();
        self.model.set({name:name,color:color});
        self.model.save({name:name,color:color},{
            success: function (model, response) {
                //TODO: check it relation/term is changed
                if(idParent!=idOldParent) {
                    if(isOldParentOntology && isParentOntology) {self.close();}
                    else if(isOldParentOntology)  self.addRelation(id,idParent); //parent was ontology so nothing to delete
                    else if(isParentOntology) self.resetRelation(id,idOldParent,null); //new parent is ontology so nothing to add
                    else {
                        self.resetRelation(id,idOldParent,idParent);
                    }
                }
                else self.close();
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                self.$errorLabel.show();
                self.$errorMessage.append(json.errors);
            }
        } ); //TODO: catch error
    },

    createOntologyTerm : function() {
        var self = this;
        self.$errorMessage.empty();
        self.$errorLabel.hide();

        var id = self.model.id;
        var name =  self.getNewName();
        var isParentOntology = true;
        var idParent = self.getNewParent();
        if(window.app.models.ontologies.get(idParent)==undefined) {
            isParentOntology = false;
        }
        var color = self.getNewColor();
        self.model.set({name:name,color:color});
        self.model = new TermModel({name:name,color:color,ontology:self.ontology.id}).save({name:name,color:color,ontology:self.ontology.id},{
            success: function (model, response) {
                //TODO: check it relation/term is changed
                if(isParentOntology) {
                    //no link "parent" with a term
                    window.app.view.message("Term", response.message, "");
                    self.close();
                }
                else {
                    self.addRelation(response.term.id,idParent);
                }
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                self.$errorLabel.show();
                self.$errorMessage.append(json.errors);
            }
        } ); //TODO: catch error

    },
    resetRelation : function(child,oldParent,newParent) {
        var self = this;
        new RelationTermModel({term1:oldParent, term2:child}).destroy({
            success : function (model, response) {
                //create relation with new parent
                if(newParent!=null) {
                    self.addRelation(child,newParent);
                }
                else {
                    window.app.view.message("Term", response.message, "");
                    self.close();
                }
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                self.$errorLabel.show();
                self.$errorMessage.append(json.errors);
            }});
    },
    addRelation : function(child,newParent) {
        var self = this;
        new RelationTermModel({}).save({term1:newParent, term2:child},{
            success : function (model, response) {
                window.app.view.message("Term", response.message, "");
                self.close();
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                self.$errorLabel.show();
                self.$errorMessage.append(json.errors);
            }});
    },
    close : function() {
        var self = this;
        this.ontologyPanel.refresh();
        self.$termDialog.dialog("close") ;
    }
});/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 13/04/11
 * Time: 14:15
 * To change this template use File | Settings | File Templates.
 */
var OntologyTreeView = Backbone.View.extend({
       tagName : "div",

       //template : _.template($('#project-view-tpl').html()),
       initialize: function(options) {
          this.tree = null;
          this.activeEvent = true;
          this.browseImageView = options.browseImageView;
          this.idAnnotation  = null;
       },
       showColors : function() {
          $(this.el).find('.tree').dynatree("getRoot").visit(function(node){

             if (node.children != null) return; //title is ok

             var title = node.data.title
             var color = node.data.color
             var htmlNode = "{{title}} <span style='background-color:{{color}}'>&nbsp;&nbsp;</span>"
             var nodeTpl = _.template(htmlNode, {title : title, color : color});


             node.setTitle(nodeTpl);
          });
       },
       render : function () {
          var self = this;
          require(["text!application/templates/explorer/OntologyTreeWrapper.tpl.html"], function(tpl) {
             self.doLayout(tpl);
          });
          return this;
       },
       doLayout: function(tpl) {
          $(this.el).html(_.template(tpl,{}));
          this.tree = $(this.el).find('.tree');
          var self = this;

          $(this.el).find('.tree').dynatree({
                 checkbox: true,
                 selectMode: 3,
                 expand : true,
                 onExpand : function() {},
                 children: this.model.toJSON(),
                 onSelect: function(select, node) {

                    if(!self.activeEvent) return;
                    if (self.idAnnotation == null) return; // nothing to do

                    if (node.isSelected()) {
                       self.linkTerm(node.data.key);
                    } else if (!node.isSelected()) {
                       self.unlinkTerm(node.data.key);
                    }
                 },
                 onDblClick: function(node, event) {
                    node.toggleSelect();
                 },

                 // The following options are only required, if we have more than one tree on one page:
                 initId: "treeData"+this.model.id,
                 cookieId: "dynatree-Cb"+this.model.id,
                 idPrefix: "dynatree-Cb"+this.model.id+"-"
              });

          self.showColors();
          //expand all nodes
          $(this.el).find('.tree').dynatree("getRoot").visit(function(node){
             node.expand(true);
          });
          return this;
       },
       clear : function() {
          this.activeEvent = false;
          $(this.el).find('.tree').dynatree("getRoot").visit(function(node){
             node.select(false);
          });
          this.activeEvent = true;
       },
       clearAnnotation : function() {
          this.idAnnotation = null;
       },
       check : function(idTerm) {
          var self = this;
          self.activeEvent = false;
          (this.el).find('.tree').dynatree("getRoot").visit(function(node){
             if (node.data.key == idTerm) node.select(true);
          });
          self.activeEvent = true;
       },
       uncheck : function(idTerm) {
          var self = this;
          self.activeEvent = false;
          (this.el).find('.tree').dynatree("getRoot").visit(function(node){
             if (node.data.key == idTerm) node.select(false);
          });
          self.activeEvent = true;
       },
       refresh: function(idAnnotation) {

          var self = this;

          this.idAnnotation = idAnnotation;
          var refreshTree = function(model , response) {
             self.clear();
             self.activeEvent = false;
             model.each(function(term) {
                self.check(term.get("id"));
             });
             self.activeEvent = true;
          }


          new AnnotationTermCollection({idAnnotation:idAnnotation}).fetch({success:refreshTree});
       },
       getTermsChecked : function() {
          var terms = [];
          (this.el).find('.tree').dynatree("getRoot").visit(function(node){
             if (node.isSelected()) terms.push(node.data.key);
          });
          return terms;
       },
       linkTerm : function(idTerm) {
          var self = this;
          new AnnotationTermModel({annotation : this.idAnnotation, term : idTerm}).save({annotation : this.idAnnotation, term : idTerm},
              {
                 success: function (model, response) {
                    window.app.view.message("Annotation Term", response.message, "");
                    self.browseImageView.reloadAnnotation(self.idAnnotation);
                    self.browseImageView.refreshAnnotationTabs(idTerm);
                 },
                 error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Annotation-Term", json.errors, "");
                 }
              }
          );
       },
       unlinkTerm : function(idTerm) {
          var self = this;
          new AnnotationTermModel({annotation : this.idAnnotation, term : idTerm}).destroy(
              {
                 success: function (model, response) {
                    window.app.view.message("Annotation Term", response.message, "");
                    self.browseImageView.reloadAnnotation(self.idAnnotation);
                    self.browseImageView.refreshAnnotationTabs(idTerm);
                 },
                 error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Annotation-Term", json.errors, "");
                 }
              }

          );
       }

    });
var EditOntologyDialog = Backbone.View.extend({
       ontologyPanel : null,
       editOntologyDialog : null,
       initialize: function(options) {
          this.container = options.container;
          this.ontologyPanel = options.ontologyPanel;
          _.bindAll(this, 'render');
       },
       render : function() {
          var self = this;
          require([
             "text!application/templates/ontology/OntologyEditDialog.tpl.html"
          ],
              function(ontologyEditDialogTpl) {
                 self.doLayout(ontologyEditDialogTpl);
              });
          return this;
       },
       doLayout : function(ontologyEditDialogTpl) {

          var self = this;
          $("#editontology").replaceWith("");
          $("#addontology").replaceWith("");
          var dialog = _.template(ontologyEditDialogTpl, {});
          $(self.el).append(dialog);

          $("#login-form-edit-ontology").submit(function () {
             self.editOntology();
             return false;
          });
          $("#login-form-edit-ontology").find("input").keydown(function(e) {
             if (e.keyCode == 13) { //ENTER_KEY
                $("#login-form-edit-ontology").submit();
                return false;
             }
          });


          //Build dialog
          self.editOntologyDialog = $("#editontology").dialog({
                 width: 500,
                 autoOpen : false,
                 modal:true,
                 buttons : {
                    "Save" : function() {
                       $("#login-form-edit-ontology").submit();
                    },
                    "Cancel" : function() {
                       $("#editontology").dialog("close");
                    }
                 }
              });
          self.open();
          self.fillForm();
          return this;

       },
       fillForm : function() {
          var self = this;
          $("#ontology-edit-name").val(self.model.get('name'));
       },
       refresh : function() {
       },
       open: function() {
          var self = this;
          self.clearEditOntologyPanel();
          self.editOntologyDialog.dialog("open");
       },
       clearEditOntologyPanel : function() {
          var self = this;
          $("#ontologyediterrormessage").empty();
          $("#ontologyediterrorlabel").hide();
          $("#ontology-edit-name").val("");
       },
       editOntology : function() {
          var self = this;
          $("#ontologyediterrormessage").empty();
          $("#ontologyediterrorlabel").hide();

          var name = $("#ontology-edit-name").val().toUpperCase();

          //edit ontology
          var ontology = self.model;
          ontology.unset('children'); //remove children (terms), they are not use by server
          ontology.save({name : name}, {
                 success: function (model, response) {
                    window.app.view.message("Ontology", response.message, "");
                    self.editOntologyDialog.dialog('close');
                    self.ontologyPanel.refresh();
                 },
                 error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    $("#ontologyediterrorlabel").show();
                    $("#ontologyediterrormessage").append(json.errors);
                 }
              }
          );
       }
    });var AddOntologyDialog = Backbone.View.extend({
       ontologiesPanel : null,
       addOntologyDialog : null,
       initialize: function(options) {
          this.container = options.container;
          this.ontologiesPanel = options.ontologiesPanel;
          _.bindAll(this, 'render');
       },
       render : function() {
          var self = this;
          require([
             "text!application/templates/ontology/OntologyAddDialog.tpl.html"
          ],
              function(ontologyAddDialogTpl) {
                 self.doLayout(ontologyAddDialogTpl);
              });
          return this;
       },
       doLayout : function(ontologyAddDialogTpl) {

          var self = this;
          var dialog = _.template(ontologyAddDialogTpl, {});
          $("#editontology").replaceWith("");
          $("#addontology").replaceWith("");
          $(self.el).append(dialog);
          var user = window.app.models.users.get(window.app.status.user);
          $("#ontologyuser").append(user.get('username') + " ("+ user.get('firstname') + " " + user.get('lastname') +")");
          $("#login-form-add-ontology").submit(function () {self.createOntology(); return false;});
          $("#login-form-add-ontology").find("input").keydown(function(e){
             if (e.keyCode == 13) { //ENTER_KEY
                $("#login-form-add-ontology").submit();
                return false;
             }
          });

          //Build dialog
          self.addOntologyDialog = $("#addontology").dialog({
                 width: 500,
                 autoOpen : false,
                 modal:true,
                 buttons : {
                    "Save" : function() {
                       $("#login-form-add-ontology").submit();
                    },
                    "Cancel" : function() {
                       $("#addontology").dialog("close");
                    }
                 }
              });
          self.open();
          return this;

       },
       refresh : function() {
       },
       open: function() {
          var self = this;
          self.clearAddOntologyPanel();
          self.addOntologyDialog.dialog("open") ;
       },
       clearAddOntologyPanel : function() {
          $("#errormessage").empty();
          $("#ontologyerrorlabel").hide();
          $("#ontology-name").val("");
       },
       createOntology : function() {
          var self = this;

          $("#errormessage").empty();
          $("#ontologyerrorlabel").hide();

          var name =  $("#ontology-name").val().toUpperCase();
          var ontology = $('input[type=radio][name=ontologyradio]:checked').attr('value');
          var users = new Array();

          $('input[type=checkbox][name=usercheckbox]:checked').each(function(i,item){
             users.push($(item).attr("value"))
          });

          //create ontology
          new OntologyModel({name : name}).save({name : name},{
                 success: function (model, response) {
                    window.app.view.message("Ontology", response.message, "");
                    var id = response.ontology.id;
                    self.ontologiesPanel.refresh(id);
                    $("#addontology").dialog("close");
                 },
                 error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    $("#ontologyerrorlabel").show();
                    $("#errormessage").append(json.errors)
                 }
              }
          );
       }
    });var ProjectView = Backbone.View.extend({
       tagName : "div",
       searchProjectPanelElem : "#searchProjectPanel",
       projectListElem : "#projectlist",
       projectList : null,
       addSlideDialog : null,
       initialize: function(options) {
          this.container = options.container;
          this.model = options.model;
          this.el = options.el;
          this.searchProjectPanel = null;
          this.addProjectDialog = null;
       },
       render : function () {
          var self = this;
          require([
             "text!application/templates/project/ProjectList.tpl.html"
          ],
              function(tpl) {
                 self.doLayout(tpl);
              });

          return this;
       },
       doLayout: function(tpl) {
          

          var self = this;
          $(this.el).find("#projectdiv").html(_.template(tpl, {}));

          //print search panel
          self.loadSearchProjectPanel();

          //print all project panel
          self.loadProjectsListing();

          return this;
       },
       /**
        * Refresh all project panel
        */
       refresh : function() {
          
          var self = this;
          //TODO: project must be filter by user?
          var idUser =  undefined;

          
          //_.each(self.projectList, function(panel){ panel.refresh(); });
          if(self.addSlideDialog!=null) self.addSlideDialog.refresh();


          new ProjectCollection({user : idUser}).fetch({
                 success : function (collection, response) {
                    self.model = collection;
                    self.render();
                 }});



       },
       /**
        * Create search project panel
        */
       loadSearchProjectPanel : function() {
          

          var self = this;
          //create project search panel
          self.searchProjectPanel = new ProjectSearchPanel({
                 model : self.model,
                 ontologies : window.app.models.ontologies,
                 el:$("#projectViewNorth"),
                 container : self,
                 projectsPanel : self
              }).render();
       },
       /**
        * Print all project panel
        */
       loadProjectsListing : function() {
          var self = this;
          //clear de list
          $(self.projectListElem).empty();
          self.projectList = new Array();

          //print each project panel
          self.model.each(function(project) {
             var panel = new ProjectPanelView({
                    model : project,
                    projectsPanel : self,
                    container : self
                 }).render();
             self.projectList.push(panel);
             $(self.projectListElem).append(panel.el);

          });
       },
       /**
        * Show all project from the collection and hide the other
        * @param projectsShow  Project collection
        */
       showProjects : function(projectsShow) {
          var self = this;
          self.model.each(function(project) {
             //if project is in project result list, show it
             if(projectsShow.get(project.id)!=null)

                $(self.projectListElem+project.id).show();
             else
                $(self.projectListElem+project.id).hide();
          });
       }


    });
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 26/04/11
 * Time: 14:38
 * To change this template use File | Settings | File Templates.
 */
var ProjectPanelView = Backbone.View.extend({
    tagName : "div",
    loadImages : true, //load images from server or simply show/hide images
    imageOpened : false, //image are shown or not
    project : null,
    projectElem : "#projectlist",  //div with project info
    imageOpenElem : "#projectopenimages",
    imageAddElem : "#projectaddimages",
    projectChangeElem :"#radioprojectchange" ,
    projectChangeDialog : "div#projectchangedialog",
    loadImagesInAddPanel: true,
    projectsPanel : null,
    container : null,
    initialize: function(options) {
        this.container = options.container;
        this.projectsPanel = options.projectsPanel;
        _.bindAll(this, 'render');
    },
    events:{
        "click .addSlide": "showAddSlidesPanel",
        "click .seeSlide": "showSlidesPanel",
        "click .editProject": "editProject",
        "click .deleteProject": "deleteProject"
    },
    render: function() {
        var self = this;
        require([
            "text!application/templates/project/ProjectDetail.tpl.html"
        ],
               function(tpl) {
                   self.doLayout(tpl, false);
               });

        return this;
    },
    refresh : function() {
        
        var self = this;
        self.model.fetch({
            success : function (model, response) {
                
                self.loadImages = true;
                require([
                    "text!application/templates/project/ProjectDetail.tpl.html"
                ],
                       function(tpl) {
                           self.doLayout(tpl, true);
                       });
                self.projectsPanel.loadSearchProjectPanel();

            }
        });

    },
    clear : function() {
        var self = this;
        //$("#projectlist" + self.model.id).replaceWith("");
        self.projectsPanel.refresh();

    },
    doLayout : function(tpl, replace) {

        var self = this;

        var json = self.model.toJSON();

        //Get ontology name
        var idOntology = json.ontology;
        //json.ontology = window.app.models.ontologies.get(idOntology).get('name');

        var users = [];
        _.each(self.model.get('users'), function (idUser) {
            users.push(window.app.models.users.get(idUser).get('username'));
        });
        json.users = users.join(", ");
        json.ontologyId = idOntology;


        var html = _.template(tpl, json);

        if (replace) {
            $("#projectlist" + json.id).replaceWith(html);
        }
        else
            $(self.el).append(html);

        self.renderCurrentProjectButton();
        self.renderShowImageButton(json.numberOfImages);

        
        
        $(self.el).find("#projectedit" + self.model.id).button({
            icons : {secondary : "ui-icon-pencil"}
        });
        $(self.el).find("#projectdelete" + self.model.id).button({
            icons : {secondary : "ui-icon-trash"}
        });


        $(self.el).find(self.imageAddElem + self.model.id).button({
            icons : {secondary : "ui-icon-plus"}
        });

        $(self.el).find(self.projectElem + self.model.get('id')).panel({
            collapsible:false
        });
    },
    editProject : function(){
           
          var self = this;
          $('#editproject').remove();
          self.editProjectDialog = new EditProjectDialog({projectPanel:self,el:self.el,model:self.model}).render();
    },
    deleteProject : function() {
        
        var self = this;
        var idProject = self.model.id;
        

        //check if project is empty
        new ImageInstanceCollection({project:idProject}).fetch({

            success : function (collection, response) {

                

                if(collection.length==0) self.acceptDeleteProject();
                else self.refuseDeleteProject(collection.length);
            }});

        //start transaction

        //delete users

        //delete project

    },
    refuseDeleteProject : function(numberOfImage) {
       
        var self = this;
        require(["text!application/templates/project/ProjectDeleteRefuseDialog.tpl.html"], function(tpl) {
            // $('#dialogsTerm').empty();
            
            
            $("dialogsDeleteProject").replaceWith('');
            var dialog =  new ConfirmDialogView({
                el:'#dialogsDeleteProject',
                template : _.template(tpl, {project : self.model.get('name'),numberOfImage:numberOfImage}),
                dialogAttr : {
                    dialogID : '#dialogsDeleteProject',
                    width : 400,
                    height : 200,
                    buttons: {
                        "Close": function() {
                            
                            //doesn't work! :-(
                            $('#dialogsDeleteProject').dialog( "close" ) ;
                        }
                    },
                    close :function (event) {
                    }
                }
            }).render();
        });
    },
    acceptDeleteProject : function() {
        
        var self = this;
        require(["text!application/templates/project/ProjectDeleteConfirmDialog.tpl.html"], function(tpl) {
            // $('#dialogsTerm').empty();
            
            
            var dialog =  new ConfirmDialogView({
                el:'#dialogsDeleteProject',
                template : _.template(tpl, {project : self.model.get('name')}),
                dialogAttr : {
                    dialogID : '#dialogsDeleteProject',
                    width : 400,
                    height : 300,
                    buttons: {
                        "Delete project": function() {
                            
                            new ProjectModel({id : self.model.id}).destroy(
                            {
                                success: function (model, response) {
                                    
                                    window.app.view.message("Project", response.message, "");
                                    self.clear();
                                    $('#dialogsDeleteProject').dialog( "close" ) ;


                                },
                                error: function (model, response) {
                                    var json = $.parseJSON(response.responseText);
                                    window.app.view.message("Project", json.errors, "");
                                }
                            }

                                    );
                        },
                        "Cancel": function() {
                            
                            //doesn't work! :-(
                            $('#dialogsDeleteProject').dialog( "close" ) ;
                        }
                    },
                    close :function (event) {
                    }
                }
            }).render();
        });
    },
    showAddSlidesPanel : function () {
        var self = this;
        
        $("#projectdiv").hide();
        $("#addimagediv").show();
        self.container.addSlideDialog = new ProjectManageSlideDialog({model:this.model,projectPanel:this,el:self.el}).render();
    },
    showSlidesPanel : function () {
        var self = this;
        self.openImagesList(self.model.get('id'));

        //change the icon
        self.imageOpened = !self.imageOpened;
        $(self.imageOpenElem + self.model.id).button({icons : {secondary :self.imageOpened ? "ui-icon-carat-1-n" : "ui-icon-carat-1-s"}});
    },
    changeProject : function () {

        var self = this;
        var idProject = self.model.get('id');
        var cont = false;

        if (idProject == window.app.status.currentProject) return true;

        window.app.controllers.browse.closeAll();
        window.app.status.currentProject = idProject;

        
        return true;//go to dashboard

    },
    renderShowImageButton : function(imageNumber) {

        var self = this;

        var disabledButton = true;
        if (imageNumber > 0) disabledButton = false;

        $(self.imageOpenElem + self.model.id).button({
            icons : {secondary : "ui-icon-carat-1-s"},
            disabled: disabledButton
        });
    },
    renderCurrentProjectButton : function() {
        var self = this;

        var isCurrentProject = window.app.status.currentProject == self.model.id
        //change button style for current project
        $(self.el).find(self.projectChangeElem + self.model.id).button({
            icons : {secondary : "ui-icon-image"}
        });
        if (isCurrentProject) $(self.projectChangeElem + self.model.id).click();
    },
    openImagesList: function(idProject) {
    }
});/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 28/04/11
 * Time: 10:14
 * To change this template use File | Settings | File Templates.
 */
var ProjectManageSlideDialog = Backbone.View.extend({
    imageListing : null,
    imageThumb : null,
    projectPanel : null,
    addSlideDialog : null,
    imagesProject : null,
    divDialog : "div#projectaddimagedialog",
    /**
     * Grab the layout and call ask for render
     */
    render : function() {
        var self = this;
        require([
            "text!application/templates/project/ProjectAddImageDialog.tpl.html"
        ],
               function(tpl) {
                   self.doLayout(tpl);
               });
        return this;
    },
    initialize: function(options) {
        this.container = options.container;
        this.projectPanel = options.projectPanel;
        this.imagesProject = new ImageInstanceCollection({project:this.model.get('id')});
    },
    refresh : function() {
        if(this.imageListing!=undefined) this.imageListing.refresh();
        if(this.imageThumb!=undefined) this.imageThumb.refresh();
    },
    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout : function(tpl) {
        var self = this;
        

        

        $("#addimagediv").empty();


        var dialog = _.template(tpl, {id:self.model.get('id'),name:self.model.get('name')});
        $(self.el).append(dialog);

        $("button[class=goBackToProject]").click(function() {
            self.projectPanel.refresh();
            $("#projectdiv").show();
            $("#addimagediv").hide();
        });

        
        self.imageThumb = new ProjectAddImageThumbDialog({
            model : self.model,
            projectsPanel : self,
            imagesProject : self.imagesProject,
            slides : window.app.models.slides,
            el : "#tabsProjectaddimagedialog"+self.model.id+"-1"
        }).render();
        

        
        self.imageListing = new ProjectAddImageListingDialog({
            model : self.model,
            projectsPanel : self,
            imagesProject : self.imagesProject,
            el : "#tabsProjectaddimagedialog"+self.model.id+"-2"
        }).render();
        

        $("button[class=goBackToProject]").button({
            icons : {primary: "ui-icon-circle-triangle-w"}
        });

        $("input[class=showImageTable]").click(function() {
            self.imageListing.refresh();
            $("#tabsProjectaddimagedialog"+self.model.id+"-2").show();
            $("#tabsProjectaddimagedialog"+self.model.id+"-1").hide();
        });

        $("input[class=showImageTable]").click();//START WITH TABLE

        $("input[class=showImageTable]").button({
            icons : {primary: "ui-icon-document"}
        });



        $("input[class=showImageThumbs]").click(function() {
            self.imageThumb.refresh();
            $("#tabsProjectaddimagedialog"+self.model.id+"-1").show();
            $("#tabsProjectaddimagedialog"+self.model.id+"-2").hide();
        });

        $("input[class=showImageThumbs]").button({
            icons : {primary: "ui-icon-image"}
        });



        $("#addimagediv").append($(self.divDialog+self.model.get('id')));

        $(".ui-panel-header").css("display","block");

        self.open();


        return this;

    },

    /**
     * Open and ask to render image thumbs
     */
    open: function() {
        //this.addSlideDialog.dialog("open") ;
    }



});var AddProjectDialog = Backbone.View.extend({
       projectsPanel : null,
       addProjectDialog : null,
       initialize: function(options) {
          this.container = options.container;
          this.projectsPanel = options.projectsPanel;
          _.bindAll(this, 'render');
       },
       render : function() {
          var self = this;
          require([
             "text!application/templates/project/ProjectAddDialog.tpl.html",
             "text!application/templates/project/OntologiesChoicesRadio.tpl.html",
             "text!application/templates/project/UsersChoices.tpl.html"
          ],
              function(projectAddDialogTpl, ontologiesChoicesRadioTpl, usersChoicesTpl) {
                 self.doLayout(projectAddDialogTpl, ontologiesChoicesRadioTpl, usersChoicesTpl);
              });
          return this;
       },
       doLayout : function(projectAddDialogTpl, ontologiesChoicesRadioTpl, usersChoicesTpl) {

          var self = this;
          var dialog = _.template(projectAddDialogTpl, {});
        $("#editproject").replaceWith("");
        $("#addproject").replaceWith("");
          $(self.el).append(dialog);

          $("#login-form-add-project").submit(function () {self.createProject(); return false;});
          $("#login-form-add-project").find("input").keydown(function(e){
             if (e.keyCode == 13) { //ENTER_KEY
                $("#login-form-add-project").submit();
                return false;
             }
          });


          $("#projectontology").empty();
          window.app.models.ontologies.each(function(ontology){
             var choice = _.template(ontologiesChoicesRadioTpl, {id:ontology.id,name:ontology.get("name")});
             $("#projectontology").append(choice);
          });

          $("#projectuser").empty();
          window.app.models.users.each(function(user){
             var choice = _.template(usersChoicesTpl, {id:user.id,username:user.get("username")});
             $("#projectuser").append(choice);
          });

          //check current user
          $("#projectuser").find('#users'+window.app.status.user.id).attr('checked','checked');
         $("#projectuser").find('#users'+window.app.status.user.id).click(function() {

             $("#projectuser").find('#users'+window.app.status.user.id).attr('checked','checked');
         });
          $("#projectuser").find('[for="users'+window.app.status.user.id+'"]').css("font-weight","bold");


          //Build dialog
          
          self.addProjectDialog = $("#addproject").dialog({
                 width: 500,
                 autoOpen : false,
                 modal:true,
                 buttons : {
                    "Save" : function() {
                       $("#login-form-add-project").submit();
                    },
                    "Cancel" : function() {
                       $("#addproject").dialog("close");
                    }
                 }
              });
          self.open();

          return this;

       },
       refresh : function() {
       },
       open: function() {
          var self = this;
          self.clearAddProjectPanel();
          self.addProjectDialog.dialog("open") ;
       },
       clearAddProjectPanel : function() {
          var self = this;
          $("#errormessage").empty();
          $("#projecterrorlabel").hide();
          $("#project-name").val("");

          $(self.addProjectCheckedOntologiesRadioElem).attr("checked", false);
          $(self.addProjectCheckedUsersCheckboxElem).attr("checked", false);
       },
       createProject : function() {
          
          var self = this;

          $("#errormessage").empty();
          $("#projecterrorlabel").hide();

          var name =  $("#project-name").val().toUpperCase();
          var ontology = $('input[type=radio][name=ontologyradio]:checked').attr('value');
          var users = new Array();

          $('input[type=checkbox][name=usercheckbox]:checked').each(function(i,item){
             users.push($(item).attr("value"))
          });

          //create project
          new ProjectModel({name : name, ontology : ontology}).save({name : name, ontology : ontology},{
                 success: function (model, response) {
                    
                     window.app.view.message("Project", response.message, "");
                    var id = response.project.id;
                    
                    //create user-project "link"
                var total = users.length;
                var counter = 0;
                if(total==0) self.addDeleteUserProjectCallback(0,0);
                _.each(users,function(user){
                    
                    new ProjectUserModel({project: id,user:user}).save({}, {
                        success: function (model, response) {
                            self.addUserProjectCallback(total,++counter);
                        },error: function (model, response) {
                            
                            var json = $.parseJSON(response.responseText);
                            window.app.view.message("User", json.errors, "");
                        }});
                });

                 },
                 error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    

                    $("#projecterrorlabel").show();

                    
                 }
              }
          );
       },
     addUserProjectCallback : function(total, counter) {
        if (counter < total) return;
        var self = this;
        self.projectsPanel.refresh();
        $("#addproject").dialog("close") ;
    }
    });var EditProjectDialog = Backbone.View.extend({
    projectsPanel : null,
    editProjectDialog : null,
    initialize: function(options) {
        this.container = options.container;
        this.projectPanel = options.projectPanel;
        _.bindAll(this, 'render');
    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/project/ProjectEditDialog.tpl.html",
            "text!application/templates/project/UsersChoices.tpl.html"
        ],
               function(projectEditDialogTpl, usersChoicesTpl) {
                   self.doLayout(projectEditDialogTpl, usersChoicesTpl);
               });
        return this;
    },
    doLayout : function(projectEditDialogTpl, usersChoicesTpl) {

        var self = this;
        $("#editproject").replaceWith("");
        $("#addproject").replaceWith("");
        var dialog = _.template(projectEditDialogTpl, {});
        $(self.el).append(dialog);

        $("#login-form-edit-project").submit(function () {
            self.editProject();
            return false;
        });
        $("#login-form-edit-project").find("input").keydown(function(e) {
            if (e.keyCode == 13) { //ENTER_KEY
                $("#login-form-edit-project").submit();
                return false;
            }
        });

        $("#projectedituser").empty();
        window.app.models.users.each(function(user) {
            var choice = _.template(usersChoicesTpl, {id:user.id,username:user.get("username")});
            $("#projectedituser").append(choice);
        });
        //check current user
        $("#projectedituser").find('#users'+window.app.status.user.id).attr('checked','checked');
        $("#projectedituser").find('#users'+window.app.status.user.id).click(function() {
            $("#projectedituser").find('#users'+window.app.status.user.id).attr('checked','checked');
        });
        $("#projectedituser").find('[for="users'+window.app.status.user.id+'"]').css("font-weight","bold");

        //Build dialog
        
        self.editProjectDialog = $("#editproject").dialog({
            width: 500,
            autoOpen : false,
            modal:true,
            buttons : {
                "Save" : function() {
                    $("#login-form-edit-project").submit();
                },
                "Cancel" : function() {
                    $("#editproject").dialog("close");
                }
            }
        });
        self.open();
        self.fillForm();
        return this;

    },
    fillForm : function() {
        
        var self = this;
        $("#project-edit-name").val(self.model.get('name'));
        var jsonuser = self.model.get('users');
        _.each(jsonuser,
              function(user){
                  
                  $('#users'+user).attr('checked', true);
                  //TODO: if user.id == currentuser, lock the checkbox (a user cannot delete himself from a project)
                  if(window.app.status.user.id==user.id) {

                  }
              });

    },
    refresh : function() {
    },
    open: function() {
        var self = this;
        self.clearEditProjectPanel();
        self.editProjectDialog.dialog("open");
    },
    clearEditProjectPanel : function() {
        var self = this;
        
        $("#projectediterrormessage").empty();
        $("#projectediterrorlabel").hide();
        $("#project-edit-name").val("");

        $(self.editProjectCheckedUsersCheckboxElem).attr("checked", false);
    },
    /**
     * Function which returns the result of the subtraction method applied to
     * sets (mathematical concept).
     *
     * @param a Array one
     * @param b Array two
     * @return An array containing the result
     */
    diffArray: function(a, b) {
        var seen = [], diff = [];
        for ( var i = 0; i < b.length; i++)
            seen[b[i]] = true;
        for ( var i = 0; i < a.length; i++)
            if (!seen[a[i]])
                diff.push(a[i]);
        return diff;
    },


    editProject : function() {
        
        var self = this;

        $("#projectediterrormessage").empty();
        $("#projectediterrorlabel").hide();

        var name = $("#project-edit-name").val().toUpperCase();;
        var users = new Array();

        $('input[type=checkbox][name=usercheckbox]:checked').each(function(i, item) {
            users.push($(item).attr("value"))
        });

        //edit project
        var project = self.model;
        project.set({name:name});

        project.save({name : name}, {
            success: function (model, response) {
                

                window.app.view.message("Project", response.message, "");

                var id = response.project.id;
                
                //create user-project "link"


                var projectOldUsers = new Array(); //[a,b,c]
                var projectNewUsers = null;  //[a,b,x]
                var projectAddUser = null;
                var projectDeleteUser = null;

                var jsonuser = self.model.get('users');

                _.each(jsonuser,
                      function(user){
                          projectOldUsers.push(user)
                      });
                projectOldUsers.sort();
                projectNewUsers = users;
                projectNewUsers.sort();
                //var diff = self.diffArray(projectOldUsers,projectNewUsers);
                projectAddUser = self.diffArray(projectNewUsers,projectOldUsers); //[x] must be added
                projectDeleteUser =  self.diffArray(projectOldUsers,projectNewUsers); //[c] must be deleted

                
                /*_.each(projectOldUsers,function(user){
                
                _.each(projectNewUsers,function(user){
                
                _.each(projectAddUser,function(user){
                
                _.each(projectDeleteUser,function(user){*/
                var total = projectAddUser.length+projectDeleteUser.length;
                var counter = 0;
                if(total==0) self.addDeleteUserProjectCallback(0,0);
                _.each(projectAddUser,function(user){
                    
                    new ProjectUserModel({project: id,user:user}).save({}, {
                        success: function (model, response) {
                            self.addDeleteUserProjectCallback(total,++counter);
                        },error: function (model, response) {
                            
                            var json = $.parseJSON(response.responseText);
                            window.app.view.message("User", json.errors, "");
                        }});
                });
                _.each(projectDeleteUser,function(user){
                    
                    new ProjectUserModel({project: id,user:user}).destroy({
                        success: function (model, response) {
                            self.addDeleteUserProjectCallback(total,++counter);
                        },error: function (model, response) {
                            
                            var json = $.parseJSON(response.responseText);
                            window.app.view.message("User", json.errors, "");
                        }});
                });

            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                

                $("#projectediterrorlabel").show();

                
            }
        }
                );
    },
    addDeleteUserProjectCallback : function(total, counter) {
        if (counter < total) return;
        var self = this;
        self.projectPanel.refresh();
        $("#editproject").dialog("close");
    }
});var ProjectSearchPanel = Backbone.View.extend({
       ontologies : null,
       idUser : null,
       container : null,
       projectsPanel : null,
       allProjectsButtonElem: "#projectallbutton",
       addProjectButtonElem : "#projectaddbutton",
       searchProjectOntolgiesListElem : "#ontologyChoiceList",
       sliderNumberOfImagesElem : "#numberofimageSlider",
       labelNumberOfImagesElem : "#amountNumberOfImages",
       sliderNumberOfSlidesElem : "#numberofslideSlider",
       labelNumberOfSlidesElem : "#amountNumberOfSlides",
       sliderNumberOfAnnotationsElem : "#numberofannotationSlider",
       labelNumberOfAnnotationsElem : "#amountNumberOfAnnotations",
       searchProjectTextBoxElem : "#projectsearchtextbox",
       searchProjectButtonElem : "#projectsearchbutton",
       searchProjectCheckedOntologiesElem : 'input[type=checkbox][name=ontology]:checked',
       addProjectCheckedOntologiesRadioElem : 'input[type=radio][name=ontologyradio]:checked',
       addProjectCheckedUsersCheckboxElem : 'input[type=checkbox][name=usercheckbox]:checked',
       initialize: function(options) {
          this.ontologies = options.ontologies;
          this.idUser = options.idUser;
          this.container = options.container;
          this.projectsPanel = options.projectsPanel;
       },
       events: {
          "click .addProject": "showAddProjectPanel",
          "click .searchProjectCriteria": "searchProject",
          "click .showAllProject": "showAllProject"
       },
       render : function() {
          var self = this;
          require([
             "text!application/templates/project/ProjectSearchPanel.tpl.html"
          ],
              function(tpl) {
                 self.doLayout(tpl);
              });

          return this;
       },
       doLayout : function(tpl) {

          var self = this;
          var search = _.template(tpl, {});
          $(this.el).empty();
          $(this.el).append(search);


          require([
             "text!application/templates/project/OntologiesChoices.tpl.html"
          ],
              function(tpl) {
                 self.loadPanelAndButton(tpl);
              });


          self.loadSlider();

          self.loadAutocomplete();

       },
       /**
        * Load panel and all buttons/checkbox
        */
       loadPanelAndButton : function(tpl) {
          var self = this;
          //create search panel
          $(self.el).find("#searchProjectPanel").panel({
                 collapseSpeed:100
              });

          //configure "all projects" button
          $(self.el).find(self.allProjectsButtonElem).button({
                 icons : {secondary: "ui-icon-refresh" }

              });

          //configure "add projects" button
          $(self.el).find(self.addProjectButtonElem).button({
                 icons : {secondary: "ui-icon-plus" }
              });

          //render ontologies choice
          self.ontologies = window.app.models.ontologies;
          self.ontologies.each(function(ontology) {
             var choice = _.template(tpl, {id:ontology.id,name:ontology.get("name")});
             $(self.searchProjectOntolgiesListElem).append(choice);
          });
       },
       /**
        * Load slider for images, annotations,.. number and compute the min/max value
        */
       loadSlider : function() {

          var self = this;
          //init slider to serach by slides number, images number...
          var minNumberOfImage = Number.MAX_VALUE;
          var maxNumberOfImage = 0;
          var minNumberOfSlide = Number.MAX_VALUE;
          var maxNumberOfSlide = 0;
          var minNumberOfAnnotation = Number.MAX_VALUE;
          var maxNumberOfAnnotation = 0;
          self.model.each(function(project) {

             var numberOfImage = parseInt(project.get('numberOfImages'));
             var numberOfSlide = parseInt(project.get('numberOfSlides'));
             var numberOfAnnotation = parseInt(project.get('numberOfAnnotations'));

             if(numberOfImage<minNumberOfImage) minNumberOfImage =  numberOfImage;
             if(numberOfImage>maxNumberOfImage) maxNumberOfImage =  numberOfImage;
             if(numberOfSlide<minNumberOfSlide) minNumberOfSlide =  numberOfSlide;
             if(numberOfSlide>maxNumberOfSlide) maxNumberOfSlide =  numberOfSlide;
             if(numberOfAnnotation<minNumberOfAnnotation) minNumberOfAnnotation =  numberOfAnnotation;
             if(numberOfAnnotation>maxNumberOfAnnotation) maxNumberOfAnnotation =  numberOfAnnotation;

          });
          //create slider
          self.createSliderWithoutAmountPrint(self.sliderNumberOfImagesElem,self.labelNumberOfImagesElem,minNumberOfImage,maxNumberOfImage);
          self.createSliderWithoutAmountPrint(self.sliderNumberOfSlidesElem,self.labelNumberOfSlidesElem,minNumberOfSlide,maxNumberOfSlide);
          self.createSliderWithoutAmountPrint(self.sliderNumberOfAnnotationsElem,self.labelNumberOfAnnotationsElem,minNumberOfAnnotation,maxNumberOfAnnotation);
       },
       /**
        * Create autocomplete project name box in the textbox
        */
       loadAutocomplete : function() {
          var self = this;
          //array for autocompletion
          var projectNameArray = new Array();

          self.model.each(function(project) {
             projectNameArray.push(project.get('name'));
          });

          //autocomplete
          $(self.searchProjectTextBoxElem).autocomplete({
                 minLength : 0, //with min=0, if user erase its text, it will show all project withouth name constraint
                 source : projectNameArray,
                 select : function (event,ui)
                 {
                    $(self.searchProjectTextBoxElem).val(ui.item.label)
                    self.searchProject();

                 },
                 search : function(event)
                 {

                    
                    self.searchProject();
                 }
              });
       },
       /**
        * Create a slider elem in slideElem with min/max value and a label with its amount in labelElem
        * @param sliderElem Html Element that will be a slider
        * @param labelElem Html Element that will print info
        * @param min  Minimum value for slider
        * @param max Maximum value for slider
        */
       createSliderWithoutAmountPrint : function(sliderElem, labelElem,min,max) {
          var self = this;

          
          $(sliderElem).slider({
                 range: true,
                 min : min,
                 max : max,
                 values: [ min, max ],
                 change: function( event, ui ) {
                    $(labelElem).val( "" + ui.values[ 0 ] + " - " + ui.values[ 1 ] );
                    self.searchProject();
                 }
              });
          $(labelElem).val( "" + $(sliderElem).slider( "values", 0 ) +" - " + $(sliderElem).slider( "values", 1 ) );
       },
       /**
        * Refresh search panel with all projects info
        * @param Projects Projects list
        */
       refreshSearchPanel : function(Projects) {

          //refresh item from search panel
          //ex: if a user add 1 slide to the project that have the hight number of slide, number of slides slider value must be change
          var self = this;
          
          self.loadSlider();
          self.loadAutocomplete();
       },
       /**
        * Reset every item in the form by its "default" value (textbox empty, nothing check,..)
        */
       showAllProject:function() {
          var self = this;

          //reset every element
          $(self.searchProjectTextBoxElem).val("");
          $(self.searchProjectCheckedOntologiesElem).attr("checked", false);
          self.resetSlider(self.sliderNumberOfImagesElem);
          self.resetSlider(self.sliderNumberOfSlidesElem);
          self.resetSlider(self.sliderNumberOfAnnotationsElem);

          //start a search
          self.searchProject();
       },
       /**
        * Reset slider by putting its first cursor to min and the second one to max
        * @param sliderElem Element for slider
        */
       resetSlider : function(sliderElem) {
          //put the min slider cursor to min and the other to max
          var min = $(sliderElem).slider( "option", "min");
          var max = $(sliderElem).slider( "option", "max");
          $(sliderElem).slider( "values", [min,max] );
       },
       /**
        * Search project with all info from the form
        */
       searchProject : function() {

          var self = this;

          //get name
          var searchText = $(self.searchProjectTextBoxElem).val();

          //get ontologies
          var searchOntologies = new Array();
          $.each($(self.searchProjectCheckedOntologiesElem), function(index, value) {
             var idOntology =  $(value).attr('id').replace("ontologies","");
             searchOntologies.push(idOntology);
          });

          //get number of images [min,max]
          var numberOfImages = new Array();
          numberOfImages.push($(self.sliderNumberOfImagesElem).slider( "values", 0 ));
          numberOfImages.push($(self.sliderNumberOfImagesElem).slider( "values", 1 ));

          //get number of slides [min,max]
          var numberOfSlides = new Array();
          numberOfSlides.push($(self.sliderNumberOfSlidesElem).slider( "values", 0 ));
          numberOfSlides.push($(self.sliderNumberOfSlidesElem).slider( "values", 1 ));

          //get number of annotation [min,max]
          var numberOfAnnotations = new Array();
          numberOfAnnotations.push($(self.sliderNumberOfAnnotationsElem).slider( "values", 0 ));
          numberOfAnnotations.push($(self.sliderNumberOfAnnotationsElem).slider( "values", 1 ));
          
          self.filterProjects(searchText==""?undefined:searchText,searchOntologies.length==0?undefined:searchOntologies,numberOfImages,numberOfSlides,numberOfAnnotations);
       },
       /**
        * Show dialog to add a project
        */
       showAddProjectPanel : function() {
          
          var self = this;
          $('#addproject').remove();
          self.addProjectDialog = new AddProjectDialog({projectsPanel:self.projectsPanel,el:self.el}).render();
       },
       /**
        * Show only project that match with params
        * @param searchText Project Name
        * @param searchOntologies Ontologies
        * @param searchNumberOfImages Number of image array [min,max]
        * @param searchNumberOfSlides Number of slide array [min,max]
        * @param searchNumberOfAnnotations  Number of annotations array [min,max]
        */
       filterProjects : function(
           searchText,
           searchOntologies,
           searchNumberOfImages,
           searchNumberOfSlides,
           searchNumberOfAnnotations) {

          var self = this;
          var projects =  new ProjectCollection(self.model.models);

          //each search function takes a search data and a collection and it return a collection without elem that
          //don't match with data search
          projects = self.filterByProjectsByName(searchText,projects);
          projects = self.filterProjectsByOntology(searchOntologies,projects);
          projects = self.filterProjectsByNumberOfImages(searchNumberOfImages,projects);
          projects = self.filterProjectsByNumberOfSlides(searchNumberOfSlides,projects);
          projects = self.filterProjectsByNumberOfAnnotations(searchNumberOfAnnotations,projects);
          //add here filter function

          //show project from "projects" (and hide the other) in project view
          self.container.showProjects(projects);
       },
       filterProjectsOLD : function(
           searchText,
           searchOntologies,
           searchNumberOfImages,
           searchNumberOfSlides,
           searchNumberOfAnnotations) {



          var self = this;
          self.projects = new ProjectCollection({user : self.userID}).fetch({
                 success : function (collection, response) {
                    var projects =  new ProjectCollection(collection.models);

                    //each search function takes a search data and a collection and it return a collection without elem that
                    //don't match with data search
                    projects = self.filterByProjectsByName(searchText,projects);
                    projects = self.filterProjectsByOntology(searchOntologies,projects);
                    projects = self.filterProjectsByNumberOfImages(searchNumberOfImages,projects);
                    projects = self.filterProjectsByNumberOfSlides(searchNumberOfSlides,projects);
                    projects = self.filterProjectsByNumberOfAnnotations(searchNumberOfAnnotations,projects);
                    //add here filter function

                    //show project from "projects" (and hide the other) in project view
                    self.container.showProjects(projects);
                 }
              });
       },
       filterByProjectsByName : function(searchText,projectOldList) {

          var projectNewList =  new ProjectCollection(projectOldList.models);

          projectOldList.each(function(project) {
             //if text is undefined: don't hide project
             //if project name contains search text, don't hide project
             if(searchText!=undefined && !project.get('name').toLowerCase().contains(searchText.toLowerCase()))
                projectNewList.remove(project);
          });

          return projectNewList;
       },
       filterProjectsByOntology : function(searchOntologies,projectOldList) {
          var self = this;
          var projectNewList =  new ProjectCollection(projectOldList.models);

          projectOldList.each(function(project) {

             var idOntology = project.get('ontology') +"";
             if(searchOntologies!=undefined && _.indexOf(searchOntologies,idOntology)==-1)
                projectNewList.remove(project);
          });
          return projectNewList;
       },
       filterProjectsByNumberOfImages : function(searchNumberOfImages,projectOldList) {
          var self = this;
          var projectNewList =  new ProjectCollection(projectOldList.models);
          projectOldList.each(function(project) {
             var numberOfImages = project.get('numberOfImages');
             if(searchNumberOfImages[0]>numberOfImages || searchNumberOfImages[1]<numberOfImages)
                projectNewList.remove(project);
          });
          return projectNewList;
       },
       filterProjectsByNumberOfSlides : function(searchNumberOfSlides,projectOldList) {
          var self = this;
          var projectNewList =  new ProjectCollection(projectOldList.models);
          projectOldList.each(function(project) {
             var numberOfSlides = project.get('numberOfSlides');
             if(searchNumberOfSlides[0]>numberOfSlides || searchNumberOfSlides[1]<numberOfSlides)
                projectNewList.remove(project);
          });
          return projectNewList;
       },
       filterProjectsByNumberOfAnnotations : function(searchNumberOfAnnotations,projectOldList) {
          var self = this;
          var projectNewList =  new ProjectCollection(projectOldList.models);
          projectOldList.each(function(project) {
             var numberOfAnnotations = project.get('numberOfAnnotations');
             if(searchNumberOfAnnotations[0]>numberOfAnnotations || searchNumberOfAnnotations[1]<numberOfAnnotations)
                projectNewList.remove(project);
          });
          return projectNewList;
       }
    });var ProjectAddImageListingDialog = Backbone.View.extend({
    imagesProject : null, //collection containing the images contained in the project
    searchPanel : null,
    initialize: function(options) {
        var self = this;
        this.container = options.container;
        this.projectPanel = options.projectPanel;
        this.imagesProject = options.imagesProject;
        this.abstractImageProject = new Array();
        this.el = "#tabsProjectaddimagedialog"+self.model.id+"-2" ;
        this.listmanageproject = "listmanageproject"+this.model.id;
        this.pagemanageproject = "pagemanageproject"+this.model.id;
        this.listmanageall = "listmanageall"+this.model.id;
        this.pagemanageall = "pagemanageall"+this.model.id;
        this.addImageButton = "addimageprojectbutton"+this.model.id;
        this.delImageButton = "delimageprojectbutton"+this.model.id;
        this.tab = 2;
        this.timeoutHnd = null

    },
    render : function() {
        var self = this;
        //self.fillAbstractImageProjectCollection(self.imagesProject);
        require([
            "text!application/templates/project/ProjectAddImageListingDialog.tpl.html"
        ],
               function(tpl) {
                   self.doLayout(tpl);
               });
        return this;
    },
    refresh : function() {
        this.refreshImageList();
    },

    doLayout : function(tpl) {
        var self = this;

        var json = self.model.toJSON();
        json.tab = 2;
        var dialog = _.template(tpl, json);
        $(self.el).append(dialog);

        /*
        self.searchPanel = new ProjectAddImageSearchPanel({
            model : self.model,
            images : self.images,
            el:$("#tdsearchpanel"+self.model.id),
            container : self,
            tab : 2
        }).render(); */

        //print listing
        this.renderImageList();

        return this;
    },
    renderImageList: function() {
        var self = this;
        self.renderImageListing();
    },
    renderImageListing : function() {
        var self = this;

        $("#"+self.addImageButton).button({
            icons : {primary: "ui-icon-circle-arrow-w" } ,
            text: false
        });
        $("#"+self.delImageButton).button({
            icons : {primary: "ui-icon-circle-arrow-e"} ,
            text: false
        });

        $("#infoProjectPanel"+self.model.id).panel({
            collapseSpeed:100
        });

        $('#'+self.addImageButton).click(function() {
            self.addImageProjectFromTable();
        });

        $('#'+self.delImageButton).click(function() {
            self.deleteImageProjectFromTable();
        });

        //search panel

        

        $("#searchImagetPanelup"+self.model.id+"-"+self.tab).panel({
            collapseSpeed:100
        });
         $("#filenamesearchtextboxup"+self.model.id+"-"+self.tab).val("");


        $("#imagesallbutton"+self.model.id+"-"+self.tab).button({
            text: true
        });

            $("#filenamesearchtextboxup"+self.model.id+"-"+self.tab).keyup(function () {
                 self.doSearch();
            }).keyup();

        $("#imagesallbutton"+self.model.id+"-"+self.tab).click(function() {
            $("#filenamesearchtextboxup"+self.model.id+"-"+self.tab).val("");
            $("#datestartsearchup"+self.model.id+"-"+self.tab ).val("");
            $("#dateendsearchup"+self.model.id+"-"+self.tab).val("");
            self.doSearch();
        });

        $( "#datestartsearchup"+self.model.id+"-"+self.tab ).datepicker({
            onSelect: function(dateText, inst) { self.doSearch(); }
        });
        $( "#dateendsearchup"+self.model.id+"-"+self.tab ).datepicker({
            onSelect: function(dateText, inst) { self.doSearch(); }
        });

        self.renderImageListProject();
        self.renderImageListAll();

    },
    doSearch : function(){
        var self = this;
        

        if($.data(this, 'timer')!=null) {
            
           clearTimeout($.data(this, 'timer'));
        }
        
          var wait = setTimeout(function(){
                 self.searchImages()}
              ,500);
          $(this).data('timer', wait);
        //setTimeout("alert('foo');",5000);
        //this.searchImages();

    },

    /**
     * Look for search panel info and print result on grid
     */
    searchImages : function() {
        var self = this;
        
        //var images = self.searchPanel.search(self.images);
        var searchText = $("#filenamesearchtextboxup"+self.model.id+"-"+self.tab).val();
        var dateStart =  $("#datestartsearchup"+self.model.id+"-"+self.tab).datepicker("getDate");
        var dateEnd =  $("#dateendsearchup"+self.model.id+"-"+self.tab).datepicker("getDate");

        var dateTimestampStart="";
        if(dateStart!=null) dateTimestampStart=dateStart.getTime();
        var dateTimestampEnd="";
        if(dateEnd!=null) dateTimestampEnd=dateEnd.getTime();

        
        $("#"+self.listmanageall).jqGrid('setGridParam',{url:"api/currentuser/image.json?filename="+searchText+"&createdstart="+dateTimestampStart+"&createdstop="+dateTimestampEnd,page:1}).trigger("reloadGrid");


    },
    refreshImageList : function() {
        var self = this;
        
        //clear grid
        $("#"+self.listmanageproject).jqGrid("clearGridData", true);
        //get imagesproject on server
        self.imagesProject.fetch({
            success : function (collection, response) {

                //create abstractImagepProject collection
                self.fillAbstractImageProjectCollection(collection);

                //print data from project image table
                self.loadDataImageListProject(collection);

                //print data from all image table
                //self.searchImages();
                
                $("#"+self.listmanageall).trigger("reloadGrid");
            }
        });
    },
    /**
     * Fill collection of abstract image id from image project
     * @param images Project Images
     */
    fillAbstractImageProjectCollection : function(images) {
        var self = this;
        
        self.abstractImageProject = [];
        self.imagesProject.each(function(image) {
            self.abstractImageProject.push(image.get('baseImage'));
        });
    },
    loadDataImageListProject : function(collection) {
        
        var self = this;
        var data = new Array();
        var i = 0;

        collection.each(function(image) {
            //
            var createdDate = new Date();
            createdDate.setTime(image.get('created'));
            //
            data[i] = {
                id: image.id,
                base : image.get('baseImage'),
                thumb : "<img src='"+image.get('thumb')+"' width=30/>",
                filename: image.get('filename'),
                type : image.get('mime'),
                annotations : image.get('numberOfAnnotations'),
                added : createdDate.getFullYear() + "-" + createdDate.getMonth() + "-" + createdDate.getDate(),
                See : ''
            };
            i++;
        });
        
        for(var j=0;j<data.length;j++) {
            $("#"+self.listmanageproject).jqGrid('addRowData',data[j].id,data[j]);
        }
        $("#"+self.listmanageproject).jqGrid('sortGrid','filename',false);
    },
    renderImageListProject : function() {
        var self = this;

        $("#"+self.listmanageproject).jqGrid({
            datatype: "local",
            autowidth: true,
            height:500,
            colNames:['id','base','thumb','filename','type','annotations','added'],
            colModel:[
                {name:'id',index:'id', width:50, sorttype:"int"},
                {name:'base',index:'base', width:50, sorttype:"int"},
                {name:'thumb',index:'thumb', width:50},
                {name:'filename',index:'filename', width:220},
                {name:'type',index:'type', width:50},
                {name:'annotations',index:'annotations', width:50},
                {name:'added',index:'added', width:90,sorttype:"date"}
            ],
            onSelectRow: function(id){

                var checked = $("#"+self.listmanageproject).find("#" + id).find(".cbox").attr('checked');
                if(checked) $("#"+self.listmanageproject).find("#" + id).find("td").css("background-color", "CD661D");
                else $("#"+self.listmanageproject).find("#" + id).find("td").css("background-color", "a0dc4f");
            },
            loadComplete: function() {
                //change color of already selected image
                self.imagesProject.each(function(image) {
                    //
                    $("#"+self.listmanageproject).find("#" + image.id).find("td").css("background-color", "a0dc4f");
                });
            },
            //rowNum:10,
            pager: '#'+self.pagemanageproject,
            sortname: 'id',
            viewrecords: true,
            sortorder: "asc",
            caption:"Images in " + self.model.get('name'),
            multiselect: true
        });
        $("#"+self.listmanageproject).jqGrid('navGrid','#'+self.listmanageproject,{edit:false,add:false,del:false});
        $("#"+self.listmanageproject).jqGrid('hideCol',"id");
        $("#"+self.listmanageproject).jqGrid('hideCol',"base");
    },
    /**
     * Check if abstract image id is in project
     * @param id  Abstract Image id
     */
    isAbstractImageInProject : function(id) {
        if(_.indexOf(this.abstractImageProject, id)!=-1) return true;
        else return false;
    },
    renderImageListAll : function() {
        var self = this;
        var lastsel;
        var thumbColName = 'thumb';
        $("#"+self.listmanageall).jqGrid({
            datatype: "json",
            url : 'api/currentuser/image.json',
            autowidth: true,
            height:500,
            colNames:['id',thumbColName,'filename','mime','created'],
            colModel:[
                {name:'id',index:'id', width:30},
                {name:thumbColName,index:thumbColName, width:50},
                {name:'filename',index:'filename', width:220},
                {name:'mime',index:'mime', width:45},
                {name:'created',index:'created', width:100,sorttype:"date", formatter:self.dateFormatter}
            ],
            onSelectRow: function(id){
                if(self.isAbstractImageInProject(id)) {
                    //if image in project, row cannot be checked
                    $("#"+self.listmanageall).find("#" + id).find(".cbox").removeAttr('checked')
                }
            },
            loadComplete: function(data) {

                //load thumb
                $("#"+self.listmanageall).find("tr").each(function(index) {
                    if(index!=0) {
                        //0 is not a valid row

                        //replace the text of the thumb by a <img element with its src value
                        var thumbplace = $(this).find('[aria-describedby$="_'+thumbColName+'"]');
                        $(thumbplace).html('<img src="'+$(thumbplace).text()+'" width=30/>');
                    }
                });

                //aria-describedby="listmanageall3069_thumb">http://is1.cytomine.be:888/fcgi-bin/iipsrv.fcgi?FIF=/home/stevben/Slides/CERVIX/09-087214.mrxs&amp;SDS=0,90&amp;CNT=1.0&amp;WID=200&amp;SQL=99&amp;CVT=jpeg</td>

                //change color of already selected image
                self.imagesProject.each(function(image) {
                    // 
                    $("#"+self.listmanageall).find("#" + image.get('baseImage')).find("td").css("background-color", "a0dc4f");
                    $("#"+self.listmanageall).find("#" + image.get('baseImage')).find(".cbox").attr('disabled', true)
                    $("#"+self.listmanageall).find("#" + image.get('baseImage')).find(".cbox").css("visible", false);
                });
            },
            //rowNum:10,
            pager: '#'+self.pagemanageall,
            sortname: 'id',
            viewrecords: true,
            sortorder: "asc",
            caption:"Available images",
            multiselect: true,
            rowNum:10,
            rowList:[10,20,30],
            jsonReader: {
                repeatitems : false,
                id: "0"
            }
        });
        $("#"+self.listmanageall).jqGrid('navGrid','#'+self.pagemanageall,{edit:false,add:false,del:false});
        $("#"+self.listmanageall).jqGrid('sortGrid','filename',false);
        $("#"+self.listmanageall).jqGrid('hideCol',"id");
        //self.loadDataImageListAll(window.app.models.images);
    },
    dateFormatter : function (cellvalue, options, rowObject)
    {
       // do something here
                var createdDate = new Date();
                createdDate.setTime(cellvalue);

       return createdDate.getFullYear() + "-" + createdDate.getMonth() + "-" + createdDate.getDate()
    },

    addImageProjectFromTable : function() {
        
        var self = this;
        var idSelectedArray = $("#"+self.listmanageall).jqGrid('getGridParam','selarrrow');
        if (idSelectedArray.length == 0) return;
        var counter = 0;
        _.each(idSelectedArray, function(idImage){
            new ImageInstanceModel({}).save({project : self.model.id, user : null, baseImage :idImage},{
                success : function (image,response) {
                    
                    window.app.view.message("Image", response.message, "");
                    self.addImageProjectCallback(idSelectedArray.length, ++counter)
                },
                error: function (model, response) {
                    
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Image", json.errors, "");
                    self.addImageProjectCallback(idSelectedArray.length, ++counter)
                }
            });
        });
    },
    addImageProjectCallback : function(total, counter) {
        if (counter < total) return;
        var self = this;
        self.refreshImageList();
    },

    deleteImageProjectFromTable : function() {
        
        var self = this;
        var idSelectedArray = $("#"+self.listmanageproject).jqGrid('getGridParam','selarrrow');
        if (idSelectedArray.length == 0) return;
        var counter = 0;
        _.each(idSelectedArray, function(idImage){
            var idAsbtractImage = self.imagesProject.get(idImage).get('baseImage');
            new ImageInstanceModel({project : self.model.id, user : null, baseImage :idAsbtractImage}).destroy({
                success : function (image,response) {
                    
                    window.app.view.message("Image", response.message, "");
                    self.deleteImageProjectCallback(idSelectedArray.length, ++counter)
                },
                error: function (model, response) {
                    
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Image", json.errors, "");
                    self.deleteImageProjectCallback(idSelectedArray.length, ++counter)
                }
            });
        });
    },
    deleteImageProjectCallback : function(total, counter) {
        if (counter < total) return;
        var self = this;
        self.refreshImageList();
    }
});var ProjectAddImageThumbDialog = Backbone.View.extend({
    imageListing : null,
    imageThumb : null,
    slides : null,
    imagesProject : null,
    imagesinstanceProject : null,
    checklistChecked : ".checklist input:checked",
    checklistSelected : ".checklist .checkbox-select",
    checklistDeselected : ".checklist .checkbox-deselect",
    selectedClass : "selected",
    checkedAttr : "checked",
    liElem : "projectaddimageitemli",
    ulElem : "#projectaddimagedialoglist",
    allProjectUlElem : "ul[id^=projectaddimagedialoglist]",
    imageDivElem : "#projectaddimageitempict",


    page : 0, //start at the first page
    nb_slide_by_page : 20,
    nextPage : function() {
        var max_page = Math.round(_.size(window.app.models.slides) / this.nb_slide_by_page) - 1;
        this.page = Math.min(this.page+1, max_page);
        this.renderImageListLayout();
    },
    previousPage : function() {
        this.page = Math.max(this.page-1, 0);
        this.renderImageListLayout();
    },
    disablePrevious : function() {
    },
    enablePrevious : function() {
    },
    disableNext : function() {
    },
    enableNext : function() {
    },
    /**
     * ProjectManageSlideDialog constructor
     * @param options
     */
    initialize: function(options) {
        this.container = options.container;
        this.projectPanel = options.projectPanel;
        this.imagesProject = options.imagesProject;
        this.imagesinstanceProject = options.imagesinstanceProject;
        this.el = "#tabsProjectaddimagedialog"+this.model.id+"-1" ;

        this.slides = options.slides;
        _.bindAll(this, 'render');
        _.bindAll(this, 'nextPage');
        _.bindAll(this, 'previousPage');
    },
    /**
     * Grab the layout and call ask for render
     */
    render : function() {
        var self = this;
        require([
            "text!application/templates/project/ProjectAddImageThumbDialog.tpl.html"
        ],
               function(tpl) {
                   self.doLayout(tpl);
               });
        return this;
    },
    refresh : function() {
        this.renderImageList();
    },


    doLayout : function(tpl) {
        var self = this;
        var view = _.template(tpl, {id:this.model.get('id'),name:this.model.get('name')});
        $(self.el).append(view);

        $(self.el).find("a.next").bind("click", self.nextPage);
        $(self.el).find("a.next").button();
        $(self.el).find("a.previous").bind("click", self.previousPage);

        $(self.el).find("a.previous").button();

        
        // $(self.el).find("#tabsProjectaddimagedialog"+this.model.get('id')).tabs();
        //self.renderImageList();
        return this;

    },

    renderImageList: function() {
        var self = this;
        window.app.models.slides.fetch({
            success : function (collection, response) {
                self.renderImageListLayout();
            }
        });
    },
    renderImage : function(projectImages, image, domTarget) {
        var self = this;
        require([
            "text!application/templates/project/ProjectAddImageChoice.tpl.html"
        ],   function(tpl) {
            var thumb = new ImageSelectView({
                model : image
            }).render();

            var filename = image.get("filename");
            if (filename.length > 15)
                filename = filename.substring(0,12) + "...";
            var item = _.template(tpl, {id:image.id,name:filename, namefull: image.get("filename"), slide: image.get('slide'), info : image.get('info')});

            $(domTarget).append(item);
            $(domTarget + " " + self.imageDivElem+image.id).append(thumb.el);  //get the div elem (img id) which have this project as parent
            $(thumb.el).css({"width":30}); //thumb must be smaller

            //size of the filename text
            $(domTarget).find("label  > b").css("font-size",10);

            //if image is already in project, selected it

            if(projectImages.get(image.id)){
                //get the li elem (img id) which have this project as parent
                $(domTarget + " " + "#"+ self.liElem+image.id).addClass(self.selectedClass);
                $(domTarget + " " + "#"+self.liElem+image.id).find(":checkbox").attr(self.checkedAttr,self.checkedAttr);
            }

        });
    },
    selectAllImages : function (slideID) {
        $(".projectImageList" + slideID).find("li.imageThumbChoice").each(function(){
            $(this).find("a.checkbox-select").click();
        });
    },
    unselectAllImages : function (slideID) {
        $(".projectImageList" + slideID).find("li.imageThumbChoice").each(function(){
            $(this).find("a.checkbox-deselect").click();
        });
    },

    renderSlide : function(slide) {
        var self = this;
        require([
            "text!application/templates/project/ProjectSlideDetail.tpl.html"
        ],   function(tpl) {
            var item = _.template(tpl, { id : slide.get("id"), name : slide.get("name")});
            var el = $(self.ulElem+self.model.get('id'));
            el.append("<td>"+item+"</td>");
            el.find(".slideItem"+slide.get("id")).panel({collapsible:false});
            el.find("a[class=selectAll]").bind("click", function(){
                self.selectAllImages(slide.get("id"));
            });
            el.find("a[class=unselectAll]").bind("click", function(){
                self.unselectAllImages(slide.get("id"));
            });
            el.find("a[class=selectAll]").button({text: false,
                icons: {
                    secondary: "ui-icon-circle-plus"
                }});
            el.find("a[class=unselectAll]").button({text: false,
                icons: {
                    secondary: "ui-icon-circle-minus"
                }});
            var images = slide.get("images");
            var domTarget = ".projectImageList" + slide.get("id");
            _.each(images, function (image){
                var imagemodel = new ImageModel(image);
                self.renderImage(self.imagesProject, imagemodel, domTarget);
            });
            $("#projectImageList"+slide.get("id")).carousel();
            $(domTarget).append('<div style="clear:both;"></div>');

        });

    },
    /**
     * Render Image thumbs into the dialog
     **/

    renderImageListLayout : function() {
        var self = this;
        var cpt = 0;
        var inf = Math.abs(self.page) * self.nb_slide_by_page;
        var sup = (Math.abs(self.page) + 1) * self.nb_slide_by_page;
        $(self.ulElem+self.model.get('id')).empty();

        var maxCol = 4
        var col = 0

        $(self.ulElem+self.model.get('id')).append("<table><tr width='25%'>");

        window.app.models.slides.each(function(slide){
            if ((cpt >= inf) && (cpt < sup)) {
                self.renderSlide(slide);
                col++;
                if(col==maxCol) {
                    col=0;
                    $(self.ulElem+self.model.get('id')).append("</tr><tr width='25%'>");
                }
            }
            $(self.ulElem+self.model.get('id')).append("</tr></table>");
            $(".carousel-wrap").css("height","200");
            cpt++;
            if (cpt == sup) {
                self.initEvents();
            }
        });
    },
    /**
     * Init click events on Image thumbs
     */

    addImageToProject: function(idImage,idProject ) {
        
        //add slide to project
        new ImageInstanceModel({}).save({project : idProject, user : null, baseImage :idImage},{
            success : function (image,response) {
                
                window.app.view.message("ImageInstance", response.message, "");
            },
            error: function (model, response) {
                
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors[0], "");
            }
        });
    },

    deleteImageToProject: function(idImage,idProject ) {
        
        //add slide to project
        //delete slide from project
        new ImageInstanceModel({project : idProject, user : null, baseImage : idImage}).destroy({
            success : function (image,response) {
                
                window.app.view.message("ImageInstance", response.message, "");

            },
            error: function (model, response) {
                
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors[0], "");
            }
        });
    },

    initEvents : function() {

        /* TO DO
         Recode this method : don't repeat yourself
         Use Backbone view EVENTs to bind CLICK
         */
        var self = this;

        /* see if anything is previously checked and reflect that in the view*/
        $(self.checklistChecked).parent().addClass(self.selectedClass);

        $(self.checklistSelected).click(
                                       function(event) {
                                           event.preventDefault();
                                           var slideID = $(this).parent().attr("class");

                                           $(this).parent().addClass(self.selectedClass);
                                           $(this).parent().find(":checkbox").attr(self.checkedAttr,self.checkedAttr);

                                           //Get the id of the selected image....
                                           //TODO: a better way to do that?
                                           var fullId = $(this).parent().attr("id");   //"projectaddimageitemliXXX"
                                           var idImage = fullId.substring(self.liElem.length,fullId.length);  //XXX
                                           self.addImageToProject(idImage,self.model.id);

                                       }
                );

        $(self.checklistDeselected).click(
                                         function(event) {
                                             event.preventDefault();
                                             $(this).parent().removeClass(self.selectedClass);
                                             $(this).parent().find(":checkbox").removeAttr(self.checkedAttr);

                                             //Get the id of the selected image....a better way to do that?
                                             var fullId = $(this).parent().attr("id");   //"projectaddimageitemliXXX"
                                             var idImage = fullId.substring(self.liElem.length,fullId.length);  //XXX
                                             self.deleteImageToProject(idImage,self.model.id);

                                         });
    }

});var ProjectAddImageSearchPanel = Backbone.View.extend({
    images : null,
    tab : null,
    initialize: function(options) {
        var self = this;
        this.container = options.container;
        this.images = options.images;
        this.tab = options.tab;

    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/project/ProjectAddImageSearchDialog.tpl.html"
        ],
               function(tpl) {
                   self.doLayout(tpl);
               });
        return this;
    },
    doLayout : function(tpl) {
        var self = this;
        

        var json = self.model.toJSON();

        //Get ontology name
        var idOntology = json.ontology;
        json.ontologyId = idOntology;
        json.tab = self.tab;
        var dialog = _.template(tpl, json);
        $(self.el).append(dialog);

        
        // $(self.el).find("#tabsProjectaddimagedialog"+this.model.get('id')).tabs();

        self.renderImageListing();

        var imagesNameArray = new Array();

        //TODO: improve perf and fill it when browse in an other place?
        /*self.images.each(function(image) {
         imagesNameArray.push(image.get('filename'));
         }); */


        //autocomplete
        $("#filenamesearchtextboxup"+self.model.id+"-"+self.tab).autocomplete({
            minLength : 0, //with min=0, if user erase its text, it will show all project withouth name constraint
            source : imagesNameArray,
            select : function (event,ui)
            {
                $("#filenamesearchtextboxup"+self.model.id+"-"+self.tab).val(ui.item.label)
                self.container.searchImages();

            },
            search : function(event)
            {

                
                self.container.searchImages();
            }
        });

        $("#datestartsearchup"+self.model.id+"-"+self.tab).change(function() { self.container.searchImages(); });
        $("#dateendsearchup"+self.model.id+"-"+self.tab).change(function() { self.container.searchImages(); });

        return this;

    },
    search : function(images) {
        var self = this;




        //
        return self.filterImages(searchText==""?undefined:searchText,dateStart,dateEnd);
    },
    filterImages : function(searchText,dateStart,dateEnd) {

        var self = this;
        var images =  new ImageCollection(self.images.models);

        //each search function takes a search data and a collection and it return a collection without elem that
        //don't match with data search
        
        images = self.filterByImagesByName(searchText,images);
        
        images = self.filterByDateStart(dateStart,images);
        
        images = self.filterByDateEnd(dateEnd,images);
        
        //add here filter function
        return images;


    },
    filterByImagesByName : function(searchText,imagesOldList) {

        var imagesNewList =  new ImageCollection(imagesOldList.models);

        imagesOldList.each(function(image) {
            if(searchText!=undefined && !image.get('filename').toLowerCase().contains(searchText.toLowerCase()))
                imagesNewList.remove(image);
        });

        return imagesNewList;
    },

    filterByDateStart : function(dateStart,imagesOldList) {

        var imagesNewList =  new ImageCollection(imagesOldList.models);

        imagesOldList.each(function(image) {
            var dateAdded = new Date()
            dateAdded.setTime(image.get('created'));
            //
            if(dateStart!=undefined && dateAdded<dateStart) {
                imagesNewList.remove(image);
            }
        });

        return imagesNewList;

    },

    filterByDateEnd : function(dateEnd,imagesOldList) {
        var imagesNewList =  new ImageCollection(imagesOldList.models);

        imagesOldList.each(function(image) {
            var dateAdded = new Date()
            dateAdded.setTime(image.get('created'));
            //
            if(dateEnd!=undefined && dateAdded>dateEnd) {
                imagesNewList.remove(image);
            }
        });

        return imagesNewList;
    },

    renderImageListing : function() {
        var self = this;
        




    }
});/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:12
 * To change this template use File | Settings | File Templates.
 */
var AnnotationListView = Backbone.View.extend({
    tagName : "div",
    self : this,
    alreadyBuild : false,
    initialize: function(options) {
        this.container = options.container;
        this.idAnnotation = options.idAnnotation;
    },

    render : function () {
        var self = this;
        require([
            "text!application/templates/annotation/AnnotationList.tpl.html"
        ],
               function(tpl) {
                   self.doLayout(tpl);
               });

        return this;
    },
    doLayout: function(tpl) {
        

        var self = this;
        $(this.el).html(_.template(tpl, {name:"name",area : "area"}));

        
        self.model.each(function(annotation) {
            //$("#annotationList").append(annotation.get('name') + " <br>");
            var name = annotation.get('name');
            var area = annotation.get('area');
            //$("#tableImage").append("<tr><th>"+ name +"</th><th>" + area + "</th></tr>");

        });
        // $('#tableImage').dataTable();


        var grid;
        var i=0;
        var data = [];
        self.model.each(function(image) {
            data[i] = {
                id: image.id,
                filename: image.get('filename'),
                created: ''
            };
            i++;
        });




 $("#list2").jqGrid({
   	url:'http://localhost:8080/cytomine-web/api/image.json',
	datatype: "json",
   	colNames:['id','filename'],
   	colModel:[
   		{name:'id',index:'id', width:300},
   		{name:'filename', width:400}
   	],
   	rowNum:10,
   	rowList:[10,20,30],
   	pager: '#pager2',
   	sortname: 'id',
    viewrecords: true,
    sortorder: "desc",
    caption:"JSON Example"
});
jQuery("#list2").jqGrid('navGrid','#pager2',{edit:false,add:false,del:false});











$("#list3").jqGrid({
   	url:'api/image.json',
	datatype: "local",
    heighh: 500,
   	colNames:['id','filename'],
   	colModel:[
   		{name:'id',index:'id', width:300},
   		{name:'filename',index:'filename', width:300}
   	],
    rowNum:10,
   	pager: '#pager3',
   	sortname: 'id',
    viewrecords: true,
    sortorder: "asc",
    caption:"Array Example"
});
jQuery("#list3").jqGrid('navGrid','#pager3',{edit:false,add:false,del:false});



for(var j=0;j<=data.length;j++) {
    
	jQuery("#list3").jqGrid('addRowData',j+1,data[j]);
}


       $("#list3").jqGrid('sortGrid','filename',false);
      // $("#list3").jqGrid('sortGrid','filename',true);









        return this;
    },
    /**
     * Init annotation tabs
     */
    initAnnotation : function(){
        var self = this;







    }
});
var Component = Backbone.View.extend({
       tagName: "div",
       views: {},
       /* Component constructor */
       initialize: function (options) {
          this.divId = options.divId;
          this.el = options.el;
          this.template = options.template;
          this.buttonAttr = options.buttonAttr;
          if (options.activate != undefined) {
             this.activate = options.activate;
          }
          if (options.deactivate != undefined) {
             this.deactivate = options.deactivate;
          }
          if (options.show != undefined) {
             this.show = options.show;
          }
       },
       /**
        *  Render the component into it's DOM element and add it to the menu
        */
       render: function () {
          $(this.el).append(this.template);
          if (this.buttonAttr.elButton) {
             this.addToMenu();
          }
          return this;
       },
       /**
        * Add a button to the menu which activates the components when clicked
        */
       addToMenu: function () {
          var self = this;
          require(["text!application/templates/MenuButton.tpl.html"], function(tpl) {
             var button = _.template(tpl,{
                    id: self.buttonAttr.elButton,
                    route: self.buttonAttr.route,
                    text: self.buttonAttr.buttonText
                 }, true);
             $(self.buttonAttr.buttonWrapper).append(button);
             $("#" + self.buttonAttr.elButton).button({
                    icons: {
                       primary: self.buttonAttr.icon
                    }
                 });
             if (self.buttonAttr.click) {
                $("#" + self.buttonAttr.elButton).click(self.buttonAttr.click);
             }
          });
       },
       /**
        * Show the DOM element and disable the button associated to the component
        **/
       activate: function () {
          $("#" + this.divId).show();
          $("#" + this.buttonAttr.elButton).addClass("ui-state-disabled");
       },
       /**
        * Hide the DOM element and enable the button associated
        */
       deactivate: function () {
          $("#" + this.divId).hide();
          $("#" + this.buttonAttr.elButton).removeClass("ui-state-disabled");
       },
       /**
        * Show a subpage of the component
        * - view : the DOM element which contains the content of the page to activate
        * - scope : the DOM element name which contains pages
        * - name : the name of the page to activate
        */
       show: function (view, scope, name) {
          $(scope).find(".title.active").each(function () {
             $(this).removeClass("active");
          });
          $(scope).find("a[name=" + name + "]").addClass("active");
          for (var i in this.views) {
             var v = this.views[i];
             if (v != view) {
                $(v.el).hide();
             }
          }
          $(view.el).show();
       }
    });var ApplicationView = Backbone.View.extend({

       tagName : "div",
       className : "layout",
       components : {},
       events: {
          "click #undo":          "undo",
          "click #redo":          "redo"
       },

       /**
        *  UNDO the last command
        */
       undo : function () {
          window.app.controllers.command.undo();
       },

       /**
        * REDO the last command
        */
       redo : function () {
          window.app.controllers.command.redo();
       },

       /**
        * ApplicationView constructor. Call the initialization of its components
        * @param options
        */
       initialize: function(options) {
          this.initComponents();
       },
       /**
        * Render the html into the DOM element associated to the view
        * @param tpl
        */
       doLayout: function(tpl, renderCallback) {
          $(this.el).html(_.template(tpl, {}));
          _.each(this.components, function (component) {
             component.render();
          });

          renderCallback.call();


          return this;
       },
       /**
        * Grab the layout and call ask for render
        */
       render : function(renderCallback) {
          var self = this;
          require([
             "text!application/templates/BaseLayout.tpl.html"
          ],
              function(tpl) {
                 self.doLayout(tpl, renderCallback);
              });
          return this;
       },
       /**
        * Initialize the components of the application
        */
       initComponents : function() {
          var self = this;
          require([
             "text!application/templates/UploadComponent.tpl.html",
             "text!application/templates/WarehouseComponent.tpl.html",
             "text!application/templates/explorer/ExplorerComponent.tpl.html"
          ],
              function(uploadTpl, warehouseTpl, explorerTpl) {
                 self.components.upload = new Component({
                        el : "#content",
                        template : _.template(uploadTpl, {}),
                        buttonAttr : {
                           elButton : "upload-button",
                           buttonText : "Upload",
                           buttonWrapper : "#menu",
                           icon : "ui-icon-circle-arrow-s",
                           route : "#upload"
                        },
                        divId : "upload"
                     });
                 self.components.upload.render = function () {
                    $(this.el).append(this.template);
                    if (this.buttonAttr.elButton) {
                       this.addToMenu();
                    }
                    $('#file_upload').fileUploadUI({
                           uploadTable: $('#files'),
                           downloadTable: $('#files'),
                           buildUploadRow: function (files, index) {
                              return $('<tr><td class="file_upload_preview"><\/td>' +
                                  '<td>' + files[index].name + '<\/td>' +
                                  '<td class="file_upload_progress"><div><\/div><\/td>' +
                                  '<td class="file_upload_start">' +
                                  '<button class="ui-state-default ui-corner-all" title="Start Upload">' +
                                  '<span class="ui-icon ui-icon-circle-arrow-e">Start Upload<\/span>' +
                                  '<\/button><\/td>' +
                                  '<td class="file_upload_cancel">' +
                                  '<button class="ui-state-default ui-corner-all" title="Cancel">' +
                                  '<span class="ui-icon ui-icon-cancel">Cancel<\/span>' +
                                  '<\/button><\/td><\/tr>');
                           },
                           buildDownloadRow: function (file) {
                              return $('<tr><td>' + file.name + '<\/td><\/tr>');
                           },
                           beforeSend: function (event, files, index, xhr, handler, callBack) {
                              handler.uploadRow.find('.file_upload_start button').click(function () {
                                 callBack();
                                 return false;
                              });
                           }
                        });
                    $('#start_uploads').click(function () {
                       $('.file_upload_start button').click();
                       return false;
                    });
                    return this;
                 }
                 self.components.warehouse = new Component({
                        el : "#content",
                        template : _.template(warehouseTpl, {}),
                        buttonAttr : {
                           elButton : "warehouse-button",
                           buttonText : "Organize",
                           buttonWrapper : "#menu",
                           icon : "ui-icon-wrench",
                           route : "#warehouse"
                        },
                        divId : "warehouse"
                     });
                 self.components.explorer = new Component({
                        el : "#content",
                        template : _.template(explorerTpl, {}),
                        buttonAttr : {
                           elButton : "explorer-button",
                           buttonText : "Explore",
                           buttonWrapper : "#menu",
                           icon : "ui-icon-image",
                           route : "#explorer"
                        },
                        divId : "explorer",
                        activate: function () {
                           if (window.app.status.currentProject == undefined)
                              $("#explorer > .noProject").show();
                           else
                              $("#explorer > .noProject").hide();
                           $("#" + this.divId).show();
                           $("#" + this.buttonAttr.elButton).addClass("ui-state-disabled");
                        }
                     });
                 self.components.logout = new Component({
                        el : "#content",
                        template : "",
                        buttonAttr : {
                           elButton : "logout-button",
                           buttonText : "Logout",
                           buttonWrapper : "#menu",
                           icon : "ui-icon-power",
                           route : "#",
                           click : function() { window.app.controllers.auth.logout();return false; }
                        },
                        divId : "logout"
                     });
              });
       },
       /**
        * Show a component
        * @param Component the reference to the component
        */
       showComponent : function (component) {
          _.each(this.components, function (c) {
             if (c != component) c.deactivate();
          });
          $("#app").show();
          component.activate();
       }
    });

ApplicationView.prototype.message =  function(title, message, type, pnotify) {
   ApplicationView.prototype.message(title, message, type, pnotify, true);
}
ApplicationView.prototype.message =  function(title, message, type, pnotify,history) {
   type = type || 'status';

   if(message!=undefined)
   {
      message.responseText && (message = message.responseText);
   }

   var opts = {
      pnotify_title: title,
      pnotify_text: message,
      pnotify_notice_icon: "ui-icon ui-icon-info",
      pnotify_type : type,
      pnotify_history: history
   };
   $.pnotify(opts);

}



var ConfirmDialogView = Backbone.View.extend({
       tagName : "div",
       templateURL : null,
       templateData : null,
       initialize: function(options) {
          this.el = options.el;
          this.template = options.template;
          this.templateURL = options.templateURL;
          this.templateData = options.templateData;
          this.dialogAttr = options.dialogAttr;
          if (!options.dialogAttr.width) this.dialogAttr.width = 'auto';
          if (!options.dialogAttr.height) this.dialogAttr.height = 'auto';
       },
       doLayout : function(tpl)  {
          $(this.el).html(tpl);
          $(this.dialogAttr.dialogID).dialog({
                 create: function (event, ui) {
                    $(".ui-widget-header").hide();
                 },
                 resizable: false,
                 draggable : false,
                 width: this.dialogAttr.width,
                 height: this.dialogAttr.height,
                 closeOnEscape : true,
                 modal: true,
                 /*close : this.dialogAttr.close,*/
                 buttons: this.dialogAttr.buttons
              });
           $(".ui-panel-header").css("display","block");
       },
       render: function() {
          var self = this;
          if (this.template == null && this.templateURL != null && this.templateData != null) {
             require([this.templateURL], function(tpl) {
                self.template = _.template(tpl, self.templateData);
                self.doLayout(self.template);
             });
          } else {
             this.doLayout(this.template);
          }
          return this;
       }



    });