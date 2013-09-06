var UserDashboardController = Backbone.Router.extend({
    manageView: null,
    routes: {
        "userdashboard": "userdashboard"
    },

    initView: function () {
        var self = this;

        var projects = null;
        var loadHandler = function () {
            console.log("controller.loadHandler");
            if (projects == null) {
                return;
            }
            self.view = new UserDashboardView({
                model: projects,
                el: $("#userdashboard"),
                container: window.app.view.components.userdashboard
            }).render();

            self.view.container.views.userdashboard = self.view;

            self.view.container.show(self.view, "#userdashboard", "userdashboard");
            window.app.view.showComponent(window.app.view.components.userdashboard);
        }


        window.app.models.projects.fetch({
            success: function (collection1, response) {
                projects = collection1;
                loadHandler();
        }});
    },

    userdashboard: function (callback) {
        console.log("controller.userdashboard");
        var self = this;
        $("#warehouse-button").attr("href", "#userdashboard");
        $("#userdashboarddiv").show();

        var userdashboardCallback = function () {

            self.view.container.show(self.view, "#userdashboard", "userdashboard");
            window.app.view.showComponent(window.app.view.components.userdashboard);
            if (_.isFunction(callback)) {
                callback.call();
            }
            self.view.render();
        }

        if (!this.view) {
            this.initView();
        } else {
            userdashboardCallback.call();
        }


    }
});