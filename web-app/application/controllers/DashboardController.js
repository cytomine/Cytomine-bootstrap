
var DashboardController = Backbone.Controller.extend({

    routes: {
        "dashboard"  : "dashboard"
    },



    dashboard : function() {

        console.log("initTabs");
        window.app.controllers.browse.initTabs();
        console.log("tabs");
        var tabs = $("#explorer > .browser").children(".tabs");

        console.log("this.view");
        this.view = new ProjectDashboardView({
            model : window.app.models.projects.get(window.app.status.currentProject),
            el: tabs,
            container : window.app.view.components.explorer
        }).render();

        console.log("show/hide");
        $("#explorer > .browser").show();
        $("#explorer > .noProject").hide();

        this.view.container.views.project = this.view;

        console.log("this.view.container.show()");
        this.view.container.show(this.view, "#warehouse > .sidebar", "dashboard");

        console.log("window.app.view.showComponent()");
        window.app.view.showComponent(window.app.view.components.explorer);
    }
});