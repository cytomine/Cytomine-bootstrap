var PhonoController = Backbone.Router.extend({

    routes: {
        "phono": "createMenu"
    },

    initialize: function (options) {

    },

    createMenu: function () {
        new PhonoMenu().render();
    }

});