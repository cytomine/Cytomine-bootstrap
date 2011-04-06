
var BrowseController = Backbone.Controller.extend({



    routes: {
        "browse/:image"   :   "browse"
    },

    browse : function (image) {
        //create tabs if not exist
        if (!this.view) {
            this.view = new Tabs({
                el:$("#explorer > .browser"),
                container : window.app.components.explorer
            }).render();

            this.view.container.views.tabs = this.view;
        }

        this.view.openTab(image);

        this.view.container.show(this.view);
    }

});