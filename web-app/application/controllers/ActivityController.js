var ActivityController = Backbone.Router.extend({
    routes: {
        "activity": "activity"
    },

    activity: function () {
        if (!this.view) {
            this.view = new ActivityView({
                el: "#activity-content"
            }).render();
        }
        window.app.view.showComponent(window.app.view.components.activity);
    }
});
