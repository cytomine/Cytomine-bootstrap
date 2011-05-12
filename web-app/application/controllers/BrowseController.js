
var BrowseController = Backbone.Controller.extend({

    tabs : null,

    routes: {
        "browse/:idProject/:idImage"   :   "browse",
        "browse/:idProject/:idImage/:idAnnotation"   :   "browseAnnotation",
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

    browse : function (idProject, idImage) {
        //window.app.controllers.dashboard.dashboard(idProject);
        this.initTabs();
        this.tabs.addTab(idImage);
        this.tabs.showTab(idImage);
        window.app.view.showComponent(this.tabs.container);
        this.showView();

    },
    browseAnnotation : function (idProject, idImage,idAnnotation) {
        //window.app.controllers.dashboard.dashboard(idProject);
        console.log("browseAnnotation");
        this.browse(idProject,idImage);


    },

    closeAll : function () {
        if (this.tabs == null) return;

        this.tabs.closeAll();

        /*window.app.view.showComponent(this.tabs.container);*/
    },

    showView : function() {
        $("#explorer > .browser").show();
        $("#explorer > .noProject").hide();
        window.app.view.showComponent(window.app.view.components.explorer);
    }

});