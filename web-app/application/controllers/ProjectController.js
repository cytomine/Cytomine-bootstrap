
var ProjectController = Backbone.Controller.extend({

    routes: {
        "project"     :   "project"
    },

    project : function() {
        if (!this.view) {

            this.view = new ProjectView({
                model : new ImageCollection(),
                el:$("#warehouse > .project"),
                container : window.app.components.explorer
            }).render();

            this.view.container.views.project = this.view;
        }

        this.view.container.show(this.view, "#warehouse > .sidebar", "project");
    }
});