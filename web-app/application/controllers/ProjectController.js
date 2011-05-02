
var ProjectController = Backbone.Controller.extend({

    routes: {
        "project"     :   "project"
    },

    project : function() {
        if (!this.view) {
             console.log("Project controller");
            this.view = new ProjectView({
                model : window.app.models.projects,
                el:$("#warehouse > .project"),
                container : window.app.view.components.warehouse
            }).render();

            this.view.container.views.project = this.view;
        }

        this.view.container.show(this.view, "#warehouse > .sidebar", "project");
    }
});