
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
            if (this.view == null) this.createView();
            this.showView();
        }


    },

    createView : function () {
        console.log("create view...");
        window.app.controllers.browse.initTabs();
        var tabs = $("#explorer > .browser").children(".tabs");
        var self = this
        new ProjectModel({id : window.app.status.currentProject}).fetch({
            success : function (model, response) {
                self.view = new ProjectDashboardView({
                    model : model,
                    el: tabs,
                    container : window.app.view.components.explorer
                }).render();
            }});

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