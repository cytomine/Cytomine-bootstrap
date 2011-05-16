
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
        var self = this;
        this.initTabs();

        var initTab = function(){
            self.tabs.addTab(idImage);
            self.tabs.showTab(idImage);
            window.app.view.showComponent(self.tabs.container);
            self.showView();
        }

        if (window.app.status.currentProject == undefined) {//direct access -> create dashboard
            window.app.controllers.dashboard.dashboard(idProject);
            setTimeout(function(){initTab();}, 1500); //wait dashboard creation

        } else {
            initTab();
        }





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