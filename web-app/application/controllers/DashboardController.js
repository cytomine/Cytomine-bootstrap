
var DashboardController = Backbone.Controller.extend({

    routes: {
        "dashboard"  : "dashboard"
    },

    dashboard : function() {
        if (!this.view) {
             console.log("Dashboard controller");
            this.view = new ProjectDashboardView({
                model : window.app.models.projects.get(25),
                el:$("#warehouse > .dashboard"),
                container : window.app.view.components.warehouse
            }).render();

            this.view.container.views.project = this.view;
        }

        this.view.container.show(this.view, "#warehouse > .sidebar", "dashboard");
        window.app.view.showComponent(window.app.view.components.warehouse);
    }
});