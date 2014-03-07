var ProjectController = Backbone.Router.extend({
    manageView: null,
    routes: {
        "project": "project"
    },

    initView: function () {
        var self = this;

        var projects = null;
        var loadHandler = function () {
            if (projects == null) {
                return;
            }
            self.view = new ProjectView({
                model: projects,
                el: $("#project"),
                container: window.app.view.components.project
            }).render();

            self.view.container.views.project = self.view;

            self.view.container.show(self.view, "#project", "project");
            window.app.view.showComponent(window.app.view.components.project);
        }


        window.app.models.projects.fetch({
            success: function (collection1, response) {
                projects = collection1;
                loadHandler();
        }});
    },

    project: function (callback) {
        console.log("controller.project");
        var self = this;
        $("#warehouse-button").attr("href", "#project");
        $("#addimagediv").hide();
        $("#projectdiv").show();

        var projectCallback = function () {

            self.view.container.show(self.view, "#project", "project");
            window.app.view.showComponent(window.app.view.components.project);
            if (_.isFunction(callback)) {
                callback.call();
            }
        }

        if (!this.view) {
            this.initView();
        } else {
            projectCallback.call();
        }


    }
});