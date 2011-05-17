
var ProjectController = Backbone.Controller.extend({

    routes: {
        "project"     :   "project"
    },

    project : function() {
        var self = this;
        if (!this.view) {
            console.log("Project controller");
            //TODO: project must be filter by user?
            var idUser =  undefined;
            new ProjectCollection({user : idUser}).fetch({
                success : function (collection, response) {

                    self.view = new ProjectView({
                        model : window.app.models.projects,
                        el:$("#warehouse > .project"),
                        container : window.app.view.components.warehouse
                    }).render();

                    self.view.container.views.project = this.view;
                    self.view.container.show(this.view, "#warehouse > .sidebar", "project");
                    window.app.view.showComponent(window.app.view.components.warehouse);
                }});
        }
        else
        {
            this.view.container.show(this.view, "#warehouse > .sidebar", "project");
            window.app.view.showComponent(window.app.view.components.warehouse);
        }
    }
});