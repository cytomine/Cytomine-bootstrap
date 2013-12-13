/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 26/04/11
 * Time: 14:38
 * To change this template use File | Settings | File Templates.
 */
var ProjectPanelView = Backbone.View.extend({
    tagName: "div",
    loadImages: true, //load images from server or simply show/hide images
    imageOpened: false, //image are shown or not
    project: null,
    projectElem: "#projectlist", //div with project info
    imageOpenElem: "#projectopenimages",
    imageAddElem: "#projectaddimages",
    projectChangeElem: "#radioprojectchange",
    projectChangeDialog: "div#projectchangedialog",
    loadImagesInAddPanel: true,
    projectsPanel: null,
    container: null,
    connectionInfo: null,
    initialize: function (options) {
        this.container = options.container;
        this.projectsPanel = options.projectsPanel;
        this.connectionInfo = options.connectionInfo;
        _.bindAll(this, 'render');
    },
    events: {
        "click .addSlide": "showAddSlidesPanel",
        "click .seeSlide": "showSlidesPanel",
        "click .editProject": "editProject",
        "click .deleteProject": "deleteProject",
        "click .infoProject": "infoProject"
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/project/ProjectDetail.tpl.html"
        ],
            function (tpl) {
                self.doLayout(tpl, false);
            });

        return this;
    },
    refresh: function () {

        var self = this;
        self.model.fetch({
            success: function (model, response) {

                self.loadImages = true;
                require([
                    "text!application/templates/project/ProjectDetail.tpl.html"
                ],
                    function (tpl) {
                        self.doLayout(tpl, true);
                    });
                self.projectsPanel.loadSearchProjectPanel();

            }
        });

    },
    clear: function () {
        var self = this;
        //$("#projectlist" + self.model.id).replaceWith("");
        self.projectsPanel.refresh();

    },
    doLayout: function (tpl, replace) {

        var self = this;

        var json = self.model.toJSON();

        //Get ontology name
        var idOntology = json.ontology;
        //json.ontology = window.app.models.ontologies.get(idOntology).get('name');

        var maxNumberOfChar = 15;
        var title = json.name;
        if (title.length > maxNumberOfChar) {
            title = title.substr(0, maxNumberOfChar) + "...";
        }
        json.title = title;

        if (json.name.length > 50) {
            json.name = json.name.substr(0, 50) + "...";
        }

        if (json.disciplineName == undefined) {
            json.disciplineName = "Undefined";
        }

        if (json.disciplineName.length > maxNumberOfChar) {
            json.disciplineName = json.disciplineName.substr(0, maxNumberOfChar) + "...";
        }
        if (json.ontologyName.length > maxNumberOfChar) {
            json.ontologyName = json.ontologyName.substr(0, maxNumberOfChar) + "...";
        }
        json.ontologyId = idOntology;

        if(self.connectionInfo!=null && self.connectionInfo!=undefined) {
            //dateConnection //lastConnection
            json.dateConnection =  window.app.convertLongToDate(self.connectionInfo.date);
            json.lastConnection =  self.connectionInfo.opened;
        } else {
            json.dateConnection = null;
        }

        var html = _.template(tpl, json);


        if (replace) {
            $("#projectlist" + json.id).replaceWith(html);
        }
        else {
            $(self.el).append(html);
        }

        self.renderCurrentProjectButton();
        self.renderShowImageButton(json.numberOfImages)

    },
    infoProject: function () {
        var self = this;
        new ProjectInfoDialog({el: "#dialogs", model: self.model}).render();
    },
    editProject: function () {

        var self = this;
        $('#editproject').remove();
        self.editProjectDialog = new EditProjectDialog({projectPanel: self, el: self.el, model: self.model}).render();
    },
    deleteProject: function () {
        var self = this;
        require(["text!application/templates/project/ProjectDeleteConfirmDialog.tpl.html"], function (tpl) {
            // $('#dialogsTerm').empty();
            var dialog = new ConfirmDialogView({
                el: '#dialogsDeleteProject',
                template: _.template(tpl, {project: self.model.get('name')}),
                dialogAttr: {
                    dialogID: '#delete-project-confirm'
                }
            }).render();
            /*$("#closeProjectDeleteCancelDialog").click(function (event) {
             event.preventDefault();
             $('#delete-project-confirm').modal("hide");
             $('#delete-project-confirm').remove();
             return false;
             });*/
            /*$('#delete-project-confirm').find("a.close").click(function(event) {
             event.preventDefault();
             $('#delete-project-confirm').modal("hide");
             $('#delete-project-confirm').remove();
             return false;
             });*/


            $("#closeProjectDeleteConfirmDialog").click(function (event) {
                event.preventDefault();
                new TaskModel({project: self.model.id}).save({}, {
                        success: function (taskResponse, response) {
                            var task = taskResponse.get('task');

                            console.log("task"+task.id);
                            var timer = window.app.view.printTaskEvolution(task, $("#deleteProjectDialogContent"), 1000);


                            new ProjectModel({id: self.model.id,task: task.id}).destroy(
                                {
                                    success: function (model, response) {
                                        window.app.view.message("Project", response.message, "success");
                                        self.clear();
                                        clearInterval(timer);
                                        dialog.close();

                                    },
                                    error: function (model, response) {
                                        window.app.view.message("Project", "Errors!", "error");
                                        clearInterval(timer);
                                        var json = $.parseJSON(response.responseText);
                                        window.app.view.message("Project", json.errors[0], "error");
                                    }
                                }
                            );
                            return false;
                        },
                        error: function (model, response) {
                            var json = $.parseJSON(response.responseText);
                            window.app.view.message("Task", json.errors, "error");
                        }
                    }
                );
            });
        });

    },
    showAddSlidesPanel: function () {
        window.location = "#project-manage-" + this.model.id;
    },
    showSlidesPanel: function () {
        var self = this;
        self.openImagesList(self.model.get('id'));

        //change the icon
        self.imageOpened = !self.imageOpened;
        $(self.imageOpenElem + self.model.id).button({icons: {secondary: self.imageOpened ? "ui-icon-carat-1-n" : "ui-icon-carat-1-s" }});
    },
    changeProject: function () {

        var self = this;
        var idProject = self.model.get('id');

        if (idProject == window.app.status.currentProject) {
            return true;
        }

        window.app.controllers.browse.closeAll();
        window.app.status.currentProject = idProject;

        return true;//go to dashboard
    },
    renderShowImageButton: function (imageNumber) {

        var self = this;

        var disabledButton = true;
        if (imageNumber > 0) {
            disabledButton = false;
        }

        $(self.imageOpenElem + self.model.id).button({
            icons: {secondary: "ui-icon-carat-1-s"},
            disabled: disabledButton
        });
    },
    renderCurrentProjectButton: function () {
        var self = this;

        var isCurrentProject = window.app.status.currentProject == self.model.id
        //change button style for current project
        /*$(self.el).find(self.projectChangeElem + self.model.id).button({
         icons : {secondary : "ui-icon-image"}
         }); */
        if (isCurrentProject) {
            $(self.projectChangeElem + self.model.id).click();
        }
    },
    openImagesList: function (idProject) {
    }
});