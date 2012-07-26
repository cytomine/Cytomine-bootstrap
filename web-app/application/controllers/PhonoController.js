var PhonoController = Backbone.Router.extend({

    routes: {
    },

    initialize : function(options) {
        new PhonoMenu().render();
    }

});