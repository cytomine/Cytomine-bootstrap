
var ProjectController = Backbone.Router.extend({
    manageView : null,
    routes: {
        "project"     :   "project",
        "project-users-:idProject" : "users",
        "project-manage-:idProject" : "manage"
    },

    initView : function() {
        var self = this;

        var projects = null;
        var loadHandler = function() {
            if (projects == null) return;
            self.view = new ProjectView({
                model : projects,
                el:$("#project"),
                container : window.app.view.components.project
            }).render();

            self.view.container.views.project = self.view;

            self.view.container.show(self.view, "#project", "project");
            window.app.view.showComponent(window.app.view.components.project);
        }

        window.app.models.projects.fetch({
            success : function (collection, response) {
                projects = collection;
                loadHandler();
            }});
    },

    project : function(callback) {
        console.log("controller.project");
        var self = this;
        $("#warehouse-button").attr("href", "#project");
        $("#addimagediv").hide();
        $("#projectdiv").show();

        var projectCallback = function () {

            self.view.container.show(self.view, "#project", "project");
            window.app.view.showComponent(window.app.view.components.project);
            self.view.refresh();
            if (_.isFunction(callback)) {
                callback.call();
            }
        }

        if (!this.view) {
            this.initView();
        } else {
            projectCallback.call();
        }


    },

    manage : function(idProject) {
        var self = this;

        var showManageImages = function() {
            $("#projectdiv").hide();
            $("#addimagediv").show();
            new ProjectModel({id:idProject}).fetch({
                success : function (model, response) {
                    self.manageView = new ProjectManageSlideDialog({model:model,projectPanel:null,el:$("#project")}).render();
                }});
        }

        if (self.view==undefined) {
            self.project(showManageImages);
        } else {
            self.view.container.show(self.view, "#project", "project");
            window.app.view.showComponent(window.app.view.components.project);
            showManageImages();
        }

    },

    users : function(idProject,callback) {



        var self = this;
        $("#warehouse-button").attr("href", "#project");
        $("#addimagediv").hide();
        $("#projectdiv").show();

        var projectCallback = function () {

            self.view.container.show(self.view, "#project", "project");
            window.app.view.showComponent(window.app.view.components.project);
            self.view.refresh();
            if (_.isFunction(callback)) {
                callback.call();
            }
        }

        if (!this.view) {
            this.initView();
        } else {
            projectCallback.call();
        }

        console.log("controller.user="+idProject);
        new ProjectModel({id:idProject}).fetch({
            success : function (model, response) {
                new projectUsersDialog({model:model,el:$("#project")}).render();
            }});
    }
});