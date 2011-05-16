
var DashboardController = Backbone.Controller.extend({

    view : null,
    routes: {
        "dashboard/:project"  : "dashboard"
    },

    dashboard : function(project) {

        if (window.app.status.currentProject != undefined && window.app.status.currentProject != project) {
            console.log("close previous project");
            this.destroyView();
            window.app.controllers.browse.closeAll();
            window.app.status.currentProject = project;
        }

        if (window.app.status.currentProject == project || window.app.status.currentProject == undefined) {
            console.log("init dashboard view");
            window.app.status.currentProject = project;
            window.app.controllers.browse.initTabs();
            if (this.view == null) this.createView();
            this.showView();
        }


    },

    createView : function () {
        var tabs = $("#explorer > .browser").children(".tabs");
        var self = this;
        self.view = new ProjectDashboardView({
            model : window.app.models.projects.get(window.app.status.currentProject),
            el: tabs,
            container : window.app.view.components.explorer
        }).render();
    },

    destroyView : function() {
        //if (this.view != null) this.view.remove();
        this.view = null;
    },

    showView : function() {
        $("#explorer > .browser").show();
        $("#explorer > .noProject").hide();
        window.app.view.showComponent(window.app.view.components.explorer);
    }
});