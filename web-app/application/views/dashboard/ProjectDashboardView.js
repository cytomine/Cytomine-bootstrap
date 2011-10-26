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
   maxCommandsView : 20,
   maxSuggestView : 30,
   projectDashboardAnnotations : null,
   projectDashboardImages : null,
   rendered : false,
   initialize: function(options) {
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
         self.doLayout(tpl);
         self.rendered = true;
      });
      return this;
   },
   doLayout : function(tpl) {
      var self = this;
      var width = Math.round($(window).width()/2 - 200);
      self.model.set({"width" : width+"px"});
      var html = _.template(tpl, self.model.toJSON());
      $(self.el).append(html);
      window.app.controllers.browse.tabs.addDashboard(self);
      /*self.initTabs();*/
      self.showImagesThumbs();
   },
   refreshImagesThumbs : function() {
      if (this.projectDashboardImages == null)
         this.projectDashboardImages = new ProjectDashboardImages({ model : this.model});

      this.projectDashboardImages.refreshImagesThumbs();

   },
   refreshAlgos : function() {
      if (this.projectDashboardAlgos == null) {
         this.projectDashboardAlgos = new ProjectDashboardAlgos({
            model : this.model
         });
      }

      this.projectDashboardAlgos.refresh();

   },
   refreshImagesTable : function() {
      if (this.projectDashboardImages == null)
         this.projectDashboardImages = new ProjectDashboardImages({ model : this.model});

      this.projectDashboardImages.refreshImagesTable();
   },
   refreshAnnotations : function() {
      if (this.projectDashboardAnnotations == null) {
         this.projectDashboardAnnotations = new ProjectDashboardAnnotations({ model : this.model, el : this.el});
         this.projectDashboardAnnotations.render();
         return;
      }
      this.projectDashboardAnnotations.refreshSelectedTerms();
   },
   /**
    * Refresh all information for this project
    */
   refreshDashboard : function() {
      var self = this;
      if (!self.rendered) return;

      var refreshDashboard = function(model, response) {
         self.fetchProjectInfo();
         self.model = model;
         //TODO: must be improve!
         new AnnotationCollection({project:self.model.id}).fetch({
            success : function (collection, response) {
               self.fetchCommands(collection);
               console.log("AnnotationCollection ok");
               new TermCollection({idOntology:self.model.get("ontology")}).fetch({
                  success : function (terms, response) {
                     console.log("TermCollection ok:"+terms.length);
                     window.app.status.currentTermsCollection = terms;
                     self.fetchWorstAnnotations(collection,terms);

                  }
               });
            }
         });
         new ProjectDashboardStats({model : self.model}).fetchStats();
      }

      self.model.fetch({
         success : function(model, response) {
            refreshDashboard(model, response);
         }
      });
   },

   fetchProjectInfo : function () {
      var self = this;
      var json = self.model.toJSON();

      //Get created/updated date
      /*var dateCreated = new Date();
       dateCreated.setTime(json.created);
       json.created = dateCreated.toLocaleDateString() + " " + dateCreated.toLocaleTimeString();
       var dateUpdated = new Date();
       dateUpdated.setTime(json.updated);
       json.updated = dateUpdated.toLocaleDateString() + " " + dateUpdated.toLocaleTimeString();*/

      var resetElem = function(elem,txt) {
         $(this.el).find(elem).empty();
         $(this.el).find(elem).append(txt);
      };

      resetElem("#projectInfoName",json.name);
      resetElem("#projectInfoOntology",json.ontologyName);
      resetElem("#projectInfoNumberOfSlides",json.numberOfSlides);
      resetElem("#projectInfoNumberOfImages",json.numberOfImages);
      resetElem("#projectInfoNumberOfAnnotations",json.numberOfAnnotations);
      resetElem("#projectInfoCreated",json.created);
      resetElem("#projectInfoUpdated",json.updated);


      //Get users list
      $("#projectInfoUserList").empty();
      var users = []
      _.each(self.model.get('users'), function (idUser) {
         users.push(window.app.models.users.get(idUser).prettyName());
      });
      $("#projectInfoUserList").html(users.join(", "));
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

                   if(json.command.CLASSNAME=="be.cytomine.command.annotation.AddAnnotationCommand") {
                      var cropStyle = "block";
                      var cropURL = jsonCommand.cropURL;
                      if (annotations.get(jsonCommand.id) == undefined) {
                         cropStyle = "none";
                         cropURL = "";
                      }
                      var action = _.template(commandAnnotationTpl, {idProject : self.model.id, idAnnotation : jsonCommand.id, idImage : jsonCommand.image,imageFilename : jsonCommand.imageFilename, icon:"add.png",text:json.prefixAction+ " " + json.command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});
                      $("#lastactionitem").append(action);
                   }
                   else if(json.command.CLASSNAME=="be.cytomine.command.annotation.EditAnnotationCommand") {
                      var cropStyle = "";
                      var cropURL = jsonCommand.newAnnotation.cropURL;
                      if (annotations.get(jsonCommand.newAnnotation.id) == undefined) {
                         cropStyle = "display : none;";
                         cropURL = "";
                      }
                      var action = _.template(commandAnnotationTpl, {idProject : self.model.id, idAnnotation : jsonCommand.newAnnotation.id, idImage : jsonCommand.newAnnotation.image,imageFilename : jsonCommand.newAnnotation.imageFilename,icon:"delete.gif",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});
                      $("#lastactionitem").append(action);
                   }
                   else if(json.command.CLASSNAME=="be.cytomine.command.annotation.DeleteAnnotationCommand") {
                      var cropStyle = "";
                      var cropURL = jsonCommand.cropURL;
                      if (annotations.get(jsonCommand.id) == undefined) {
                         cropStyle = "display : none;";
                         cropURL = "";
                      }
                      var action = _.template(commandAnnotationTpl, {idProject : self.model.id, idAnnotation : jsonCommand.id, idImage : jsonCommand.image,imageFilename : jsonCommand.imageFilename,icon:"delete.gif",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});
                      $("#lastactionitem").append(action);
                   }
                   else if(json.command.CLASSNAME=="be.cytomine.command.annotationterm.AddAnnotationTermCommand") {
                      var action = _.template(commandAnnotationTermTpl, {icon:"ui-icon-plus",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,image:""});
                      $("#lastactionitem").append(action);
                   }
                   else if(json.command.CLASSNAME=="be.cytomine.command.annotationterm.EditAnnotationTermCommand") {
                      var action = _.template(commandAnnotationTermTpl, {icon:"ui-icon-pencil",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,image:""});
                      $("#lastactionitem").append(action);
                   }
                   else if(json.command.CLASSNAME=="be.cytomine.command.annotationterm.DeleteAnnotationTermCommand") {
                      var action = _.template(commandAnnotationTermTpl, {icon:"ui-icon-trash",text:json.prefixAction+ " " +json.command.action,datestr:dateStr,image:""});
                      $("#lastactionitem").append(action);
                   }
                   else if(json.command.CLASSNAME=="be.cytomine.command.imageinstance.AddImageInstanceCommand") {
                      var cropStyle = "block";
                      var cropURL = jsonCommand.thumb;
                      var action = _.template(commandImageInstanceTpl, {idProject : self.model.id, idImage : jsonCommand.id, imageFilename : jsonCommand.filename, icon:"add.png",text:json.prefixAction+ " " + json.command.action,datestr:dateStr,cropURL:cropURL, cropStyle:cropStyle});
                      $("#lastactionitem").append(action);
                   }
                   else if(json.command.CLASSNAME=="be.cytomine.command.imageinstance.DeleteImageInstanceCommand") {
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

   fetchWorstAnnotations : function (annotations, terms) {
      console.log("fetchWorstAnnotations");
      var self = this;
      require([
         "text!application/templates/dashboard/SuggestedAnnotationTerm.tpl.html"],
          function(suggestedAnnotationTermTpl) {

             var suggestedCollection = new SuggestedAnnotationTermCollection({project:self.model.get('id'),max:self.maxSuggestView});
             var suggestedCallback = function(collection, response) {
                $("#worstannotationitem").empty();

                if(collection.length==0) {
                   $("#worstannotationitem").append("You must run Retrieval Validate Algo for this project...");
                }

                collection.each(function(suggest) {
                   var json = suggest.toJSON()
                   var rate = Math.round(json.rate*100)-1+"%"
                   var annotation = annotations.get(json.annotation);
                   var suggestedTerm =  terms.get(json.term).get('name');
                   var realTerms = new Array();
                   _.each(annotation.get('term'), function(idTerm){ realTerms.push(terms.get(idTerm).get('name')); });
                   var termsAnnotation =  realTerms.join();
                   var text = "<b>" + suggestedTerm +"</b> for annotation " + annotation.id + " instead of <b>" + termsAnnotation +"</b>";

                   var cropStyle = "block";
                   var cropURL = annotation.get("cropURL");

                   var action = _.template(suggestedAnnotationTermTpl, {idProject : self.model.id, idAnnotation : annotation.id, idImage : annotation.get('image'), icon:"add.png",text:text,rate:rate,cropURL:cropURL, cropStyle:cropStyle});
                   $("#worstannotationitem").append(action);


                });
             }
             suggestedCollection.fetch({
                success : function(model, response) {
                   suggestedCallback(model, response); //fonctionne mais très bourrin de tout refaire à chaque fois...
                }
             });
          });
   },
   showImagesThumbs : function() {
      $("#tabs-projectImageThumb"+this.model.id).show();
      $("#tabs-projectImageListing"+this.model.id).hide();
      $('#imageThumbs'+this.model.id).button( "disable" );
      $('#imageArray'+this.model.id).button( "enable");
   },
   showImagesTable : function() {
      $("#tabs-projectImageThumb"+this.model.id).hide();
      $("#tabs-projectImageListing"+this.model.id).show();
      $('#imageThumbs'+this.model.id).button( "enable" );
      $('#imageArray'+this.model.id).button( "disable");
   }

});