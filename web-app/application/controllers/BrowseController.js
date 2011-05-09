
var BrowseController = Backbone.Controller.extend({

    tabs : null,
    imagesOpen : new Array(), //keep id of open images
    routes: {
        "browse/project/" : "project",
        "browse/:idImage"   :   "browse",
        "close"   :   "close"
    },


    initialize: function() {
        console.log("initBrowseController");
    },

    project : function () {
      console.log("open tab project" + window.app.status.currentProject);
    },
    browse : function (idImage) {
        //create tabs if not exist
        if (this.tabs == null) {
            this.tabs = new Tabs({
                el:$("#explorer > .browser"),
                container : window.app.view.components.explorer
            }).render();

            //   this.tabs.container.views.tabs = this.tabs;
        }

        this.tabs.addTab(idImage);
        this.tabs.showTab(idImage);
        this.imagesOpen[this.imagesOpen.length] = idImage;
        window.app.view.showComponent(this.tabs.container);
    },


    closeAll : function () {
        this.tabs.closeAll();
        this.tabs.remove();
        this.tabs = null;
        this.imagesOpen = new Array();
        window.app.view.showComponent(this.tabs.container);
    }

});