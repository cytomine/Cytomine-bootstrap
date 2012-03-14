
var ProjectController = Backbone.Router.extend({
    manageView : null,
    routes: {
        "project"     :   "project",
        "project-manage-:idProject" : "manage"
    },

    initView : function(callback) {
        var self = this;

        var ontologies = null;
        var disciplines = null;
        var projects = null;

        var loadHandler = function() {
            if (ontologies == null || disciplines == null || projects == null) return;
            self.view = new ProjectView({
                model : projects,
                ontologies : ontologies,
                disciplines : disciplines,
                el:$("#project"),
                container : window.app.view.components.project
            }).render();

            self.view.container.views.project = self.view;

            if (_.isFunction(callback)) callback.call();
        }

        new OntologyCollection({ light : true }).fetch({
            success : function (collection, response) {
                ontologies = collection;
                loadHandler();
            }});

        window.app.models.disciplines.fetch({
            success : function (collection, response) {
                disciplines = collection;
                loadHandler();
            }});

        window.app.models.projects.fetch({
            success : function (collection, response) {
                projects = collection;
                loadHandler();
            }});
    },

    project : function() {

        var self = this;
        $("#warehouse-button").attr("href", "#project");
        $("#addimagediv").hide();
        $("#projectdiv").show();
        if (!this.view) {
            this.initView(function(){self.view.container.show(self.view, "#warehouse > .sidebar", "project");window.app.view.showComponent(window.app.view.components.project);});
            return;
        }
        self.view.container.show(self.view, "#warehouse > .sidebar", "project");
        window.app.view.showComponent(window.app.view.components.project);
    },

    manage : function(idProject) {
        var self = this;

        if (self.view==undefined)
            self.project();

        $("#projectdiv").hide();
        $("#addimagediv").show();
        new ProjectModel({id:idProject}).fetch({
            success : function (model, response) {
                self.manageView = new ProjectManageSlideDialog({model:model,projectPanel:null,el:$("#warehouse > .project")}).render();
            }});
    }
});