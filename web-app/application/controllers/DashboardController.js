
var DashboardController = Backbone.Controller.extend({

    routes: {
        "dashboard"  : "dashboard"
    },



    dashboard : function() {

        window.app.controllers.browse.initTabs();
        var tabs = $("#explorer > .browser").children(".tabs");


        this.view = new ProjectDashboardView({
            model : window.app.models.projects.get(window.app.status.currentProject),
            el: tabs,
            container : window.app.view.components.explorer
        }).render();


        $("#explorer > .browser").show();
        $("#explorer > .noProject").hide();

        this.view.container.views.project = this.view;



        this.view.container.show(this.view, "#warehouse > .sidebar", "dashboard");
        window.app.view.showComponent(window.app.view.components.explorer);
    }
});