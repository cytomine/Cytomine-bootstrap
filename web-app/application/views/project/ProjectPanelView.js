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

        $(self.el).find(self.imageAddElem + self.model.id).button({
            icons : {secondary : "ui-icon-plus"}
        });

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
        $(self.el).find(self.projectChangeElem + self.model.id).button({
            icons : {secondary : "ui-icon-image"}
        });
        if (isCurrentProject) $(self.projectChangeElem + self.model.id).click();
    },
    openImagesList: function(idProject) {
    }
});