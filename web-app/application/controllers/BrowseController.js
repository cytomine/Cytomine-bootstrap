
var BrowseController = Backbone.Controller.extend({

    tabs : null,

    routes: {
        "browse/:idImage"   :   "browse",
        "close"   :   "close"
    },

    initialize: function() {
        console.log("initBrowseController");
    },

    initTabs : function() { //SHOULD BE OUTSIDE OF THIS CONTROLLER
        //create tabs if not exist
        if (this.tabs == null) {
            this.tabs = new Tabs({
                el:$("#explorer > .browser"),
                container : window.app.view.components.explorer
            }).render();

         //   this.tabs.container.views.tabs = this.tabs;
        }
    },

    browse : function (idImage) {
        this.initTabs();
        this.tabs.addTab(idImage);
        this.tabs.showTab(idImage);

        window.app.view.showComponent(this.tabs.container);

    },


    closeAll : function () {
        if (this.tabs == null) return;

        this.tabs.closeAll();

        /*window.app.view.showComponent(this.tabs.container);*/
    }

});