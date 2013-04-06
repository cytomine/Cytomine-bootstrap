var PhonoController = Backbone.Router.extend({

    routes: {
        "phono": "createMenu"
    },

    initialize: function (options) {

    },

    createMenu: function () {
        //<!-- Phono -->
        // <script type="text/javascript" src="http://s.phono.com/releases/0.3/jquery.phono.js"></script>
        require(["http://s.phono.com/releases/0.3/jquery.phono.js"], function() {
            new PhonoMenu().render();
        });

    }

});