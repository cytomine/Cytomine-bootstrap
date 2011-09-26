
var ProjectController = Backbone.Controller.extend({
    manageView : null,
    routes: {
        "project"     :   "project",
        "project-manage-:idProject" : "manage"
    },

    initView : function(callback) {
        var self = this;

        window.app.models.ontologies.fetch({
            success : function (ontologies, response) {

                window.app.models.projects.fetch({
                    success : function (collection, response) {
                        self.view = new ProjectView({
                            model : collection,
                            ontologies : ontologies,
                            el:$("#warehouse > .project"),
                            container : window.app.view.components.warehouse
                        }).render();

                        self.view.container.views.project = self.view;

                        if (_.isFunction(callback)) callback.call();
                    }});
            }});
    },

    project : function() {

        var self = this;
        $("#warehouse-button").attr("href", "#project");
        $("#addimagediv").hide();
        $("#projectdiv").show();
        if (!this.view) {
            this.initView(function(){self.view.container.show(self.view, "#warehouse > .sidebar", "project");window.app.view.showComponent(window.app.view.components.warehouse);});
            return;
        } else {
            self.view.refresh();
        }
        self.view.container.show(self.view, "#warehouse > .sidebar", "project");
        window.app.view.showComponent(window.app.view.components.warehouse);
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