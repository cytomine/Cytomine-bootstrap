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
         users.push(window.app.models.users.get(idUser).prettyName());
      });
      if (_.size(users ) == 0) {
         json.shortUsers = users[0];
      } else if (_.size(users ) >= 1){
         json.shortUsers = users[0] + ", ...";
      }

      json.users = users.join(", ");



      json.ontologyId = idOntology;


      var html = _.template(tpl, json);

      if (replace) {
         $("#projectlist" + json.id).replaceWith(html);
      }
      else
         $(self.el).append(html);

      $(self.el).find(".usersPopover").popover();
      $(self.el).find(".usersPopover").click(function(){return false;});
      self.renderCurrentProjectButton();
      self.renderShowImageButton(json.numberOfImages);

      /*$(self.el).find(self.imageAddElem + self.model.id).button({
       icons : {secondary : "ui-icon-plus"}
       });*/

      /*$(self.el).find(self.projectElem + self.model.get('id')).panel({
       collapsible:false
       });*/
   },
   editProject : function(){

      var self = this;
      $('#editproject').remove();
      self.editProjectDialog = new EditProjectDialog({projectPanel:self,el:self.el,model:self.model}).render();
   },
   deleteProject : function() {
      var self = this;
      if (self.model.get("numberOfImages") > 0 || self.model.get("numberOfAnnotations") > 0 || self.model.get("numberOfSlides") > 0) {
         self.refuseDeleteProject(self.model.get("numberOfImages"));
      } else {
         self.acceptDeleteProject();
      }
   },
   refuseDeleteProject : function(numberOfImage) {
      var self = this;
      require(["text!application/templates/project/ProjectDeleteRefuseDialog.tpl.html"], function(tpl) {
         $("dialogsDeleteProject").replaceWith('');
         var dialog =  new ConfirmDialogView({
            el:'#dialogsDeleteProject',
            template : _.template(tpl, {project : self.model.get('name'),numberOfImage:numberOfImage}),
            dialogAttr : {
               dialogID : '#delete-project-refuse'
            }
         }).render();
         $("#closeProjectDeleteRefuseDialog").click(function(){$("#delete-project-refuse").modal('hide');$("#delete-project-refuse").remove();});
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
               dialogID : '#delete-project-confirm'
            }
         }).render();
         $("#closeProjectDeleteConfirmDialog").click(function(){
            new ProjectModel({id : self.model.id}).destroy(
                {
                   success: function (model, response) {
                      window.app.view.message("Project", response.message, "");
                      self.clear();
                      $('#delete-project-confirm').modal("hide");
                      $('#delete-project-confirm').remove();

                   },
                   error: function (model, response) {
                      var json = $.parseJSON(response.responseText);
                      window.app.view.message("Project", json.errors, "");
                   }
                }
            );
         });
      });

   },
   showAddSlidesPanel : function () {
      window.location = "#project-manage-" + this.model.id;
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
      /*$(self.el).find(self.projectChangeElem + self.model.id).button({
       icons : {secondary : "ui-icon-image"}
       }); */
      if (isCurrentProject) $(self.projectChangeElem + self.model.id).click();
   },
   openImagesList: function(idProject) {
   }
});