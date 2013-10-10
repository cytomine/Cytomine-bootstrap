var ExplorerController = Backbone.Router.extend({

    tabs: null,

    routes: {
        "tabs-annotation-:idAnnotation": "browseAnnotation",
        "tabs-image-:idProject-:idImage-": "browseSimple",
        "tabs-imagemergechannel-:idProject-:idImage-": "browseChannel",
        "tabs-image-:idProject-:idImage-:idAnnotation": "browse",
        "tabs-review-:idProject-:idImage-": "reviewSimple",
        "tabs-reviewmergechannel-:idProject-:idImage-": "reviewChannel",
        "close": "close",
        "tabs-leaflet-:idProject-:idImage-" : "leaflet"
    },

    initialize: function () {
    },

    initTabs: function () { //SHOULD BE OUTSIDE OF THIS CONTROLLER
        this.tabs = new ExplorerTabs({
            el: $("#explorer > .browser"),
            container: window.app.view.components.explorer
        }).render();
    },

    browseAnnotation: function (idAnnotation) {
        var self = this;
        console.log("browseAnnotation");
        //allow to access an annotation without knowing its projet/image (usefull to access annotation when we just have annotationTerm data).
        new AnnotationModel({id: idAnnotation}).fetch({
            success: function (model, response) {
                self.browse(model.get("project"), model.get("image"), idAnnotation);
            }
        });
    },
    leaflet : function(idProject, idImage) {  //tmp for test
        var self = this;
        console.log("activate leaflet");
        require([
            "http://cdn.leafletjs.com/leaflet-0.5.1/leaflet.js"
        ], function(){
            console.log("activate leaflet plugins");
            require([
                "lib/leaflet/plugins/zoomify-layer-master/zoomify_layer.js",
                "lib/leaflet/plugins/Leaflet.draw-master/dist/leaflet.draw.js"
            ], function() {
                BrowseImageView = LeafletView;
                self.browse(idProject, idImage);
            });

        });
    },

    browseSimple: function (idProject, idImage, idAnnotation) {
        this.browse(idProject,idImage,idAnnotation,undefined);
    },
    browseChannel: function (idProject, idImage, idAnnotation) {
        this.browse(idProject,idImage,idAnnotation,"channel");
    },
    browse: function (idProject, idImage, idAnnotation,merge) {
        $(window).scrollTop(0);
        /*
         if (window.app.secondaryWindow) {
         window.app.secondaryWindow.location = window.location;
         }
         */
        var self = this;
        //create tabs if not exist
        if (this.tabs == null) {
            console.log("this.tabs==null");
            this.initTabs();
        }
        var createBrowseImageViewTab = function () {
            console.log("createBrowseImageViewTab");
            var browseImageViewInitOptions = {};
            if (idAnnotation != "") {
                browseImageViewInitOptions.goToAnnotation = {value: idAnnotation};
            }
            self.tabs.addBrowseImageView(idImage, browseImageViewInitOptions,merge);
            //$('#tabs-image-'+idImage).tab('show');
            // window.app.view.showComponent(self.tabs.container);
            console.log("showView");

            self.showView();
        };

        if (window.app.status.currentProject == undefined || window.app.status.currentProject != idProject) {//direct access -> create dashboard
            console.log("project check");
            window.app.controllers.dashboard.dashboard(idProject, createBrowseImageViewTab);
            return;
        }

        createBrowseImageViewTab();
    },

    reviewSimple: function (idProject, idImage) {
        this.review(idProject,idImage,undefined);
    },
    reviewChannel: function (idProject, idImage) {
        this.review(idProject,idImage,"channel");
    },
    review: function (idProject, idImage,merge) {
        $(window).scrollTop(0);

        var self = this;
        //create tabs if not exist
        if (this.tabs == null) {
            console.log("this.tabs==null");
            this.initTabs();
        }
        var createReviewImageViewTab = function () {
            console.log("createReviewImageViewTab");
            var reviewImageViewInitOptions = {};
            self.tabs.addReviewImageView(idImage, reviewImageViewInitOptions,merge);
            //$('#tabs-image-'+idImage).tab('show');
            // window.app.view.showComponent(self.tabs.container);
            console.log("showView");

            self.showView();
        };

        if (window.app.status.currentProject == undefined || window.app.status.currentProject != idProject) {//direct access -> create dashboard
            console.log("project check");
            window.app.controllers.dashboard.dashboard(idProject, createReviewImageViewTab);
            return;
        }

        createReviewImageViewTab();


    },


    closeAll: function () {
        if (this.tabs == null) {
            return;
        }
        this.tabs = null;
        $("#explorer > .browser").empty();
    },

    showView: function () {
        $("#explorer > .browser").show();
        $("#explorer > .noProject").hide();
        window.app.view.showComponent(window.app.view.components.explorer);
    }

});