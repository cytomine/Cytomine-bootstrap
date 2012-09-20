
var ProjectController = Backbone.Router.extend({
    manageView : null,
    routes: {
        "project"     :   "project",
        "project-manage-:idProject" : "manage"
    },

    initView : function() {
        var self = this;

        var projects = null;
        var users = null;
        var loadHandler = function() {
            if (projects == null || users == null) return;
            self.view = new ProjectView({
                model : projects,
                el:$("#project"),
                container : window.app.view.components.project
            }).render();

            self.view.container.views.project = self.view;

            self.view.container.show(self.view, "#project", "project");
            window.app.view.showComponent(window.app.view.components.project);
        }

//        new OntologyCollection({ light : true }).fetch({
//            success : function (collection, response) {
//                ontologies = collection;
//                loadHandler();
//            }});

//        window.app.models.disciplines.fetch({
//            success : function (collection, response) {
//                disciplines = collection;
//                loadHandler();
//            }});

        window.app.models.projects.fetch({
            success : function (collection, response) {
                projects = collection;
                loadHandler();
            }});

        window.app.models.users.fetch({
            success : function (collection, response) {
                users = collection;
                loadHandler();
            }
        })
    },

    project : function(callback) {

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

    }
});