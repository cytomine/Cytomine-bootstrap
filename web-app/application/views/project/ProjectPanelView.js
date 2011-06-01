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
       addSlideDialog : null,
       initialize: function(options) {
          this.container = options.container;
          this.projectsPanel = options.projectsPanel;
          _.bindAll(this, 'render');
       },
       events: {
          "click .addSlide": "showAddSlidesPanel",
          "click .seeSlide": "showSlidesPanel"
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
                    console.log("refresh project panel");
                    self.loadImages=true;
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
       doLayout : function(tpl, replace) {

          var self = this;

          var json = self.model.toJSON();

          //Get ontology name
          var idOntology = json.ontology;
          //json.ontology = window.app.models.ontologies.get(idOntology).get('name');

          var users= [];
          _.each(self.model.get('users'), function (idUser) {
             users.push(window.app.models.users.get(idUser).get('username'));
          });
          json.users = users.join(", ");
          json.ontologyId = idOntology;


          var html = _.template(tpl, json);

          if(replace) {
             $("#projectlist"+json.id).replaceWith(html);
          }
          else
             $(self.el).append(html);

          self.renderCurrentProjectButton();
          self.renderShowImageButton(json.numberOfImages);

          $(self.el).find(self.imageAddElem + self.model.id).button({
                 icons : {secondary : "ui-icon-plus"}
              });

          $(self.el).find(self.projectElem+self.model.get('id')).panel({
                 collapsible:false
              });
       },

       showAddSlidesPanel : function () {
          var self = this;
          console.log("build dialog with project:"+this.model);
          new ProjectManageSlideDialog({model:this.model,projectPanel:this,el:self.el}).render();
       },
       showSlidesPanel : function () {
          var self = this;
          self.openImagesList(self.model.get('id'));

          //change the icon
          self.imageOpened=!self.imageOpened;
          $(self.imageOpenElem + self.model.id).button({icons : {secondary :self.imageOpened? "ui-icon-carat-1-n":"ui-icon-carat-1-s"}});
       },
       changeProject : function () {

          var self = this;
          var idProject = self.model.get('id');
          var cont = false;

          if(idProject==window.app.status.currentProject) return true;

          window.app.controllers.browse.closeAll();
          window.app.status.currentProject = idProject;

          console.log("New current project:"+window.app.status.currentProject);
          return true;//go to dashboard

       },
       renderShowImageButton : function(imageNumber) {

          var self = this;

          var disabledButton= true;
          if(imageNumber>0) disabledButton= false;

          $(self.imageOpenElem + self.model.id).button({
                 icons : {secondary : "ui-icon-carat-1-s"},
                 disabled: disabledButton
              });
       },
       renderCurrentProjectButton : function() {
          var self = this;

          var isCurrentProject = window.app.status.currentProject==self.model.id
          //change button style for current project
          $(self.el).find(self.projectChangeElem + self.model.id).button({
                 icons : {secondary : "ui-icon-image"}
              });
          if(isCurrentProject) $(self.projectChangeElem + self.model.id).click();
       },
       openImagesList: function(idProject) {
       }
    });