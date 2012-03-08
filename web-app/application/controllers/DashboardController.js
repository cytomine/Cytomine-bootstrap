
var DashboardController = Backbone.Router.extend({

    view : null,
    routes: {
        "tabs-images-:project"  : "images",
        "tabs-images-:project/:page"  : "imagespage",
        "tabs-thumbs-:project"  : "imagesthumbs",
        "tabs-imagesarray-:project"  : "imagesarray",
        "tabs-annotations-:project-:terms-:users"  : "annotations",
        "tabs-annotations-:project"  : "annotations",
        "tabs-dashboard-:project"  : "dashboard",
        "tabs-config-:project"  : "config",
        "tabs-algos-:project-:software-:job"  : "algos",
        "tabs-algos-:project-:software"  : "algos",
        "tabs-algos-:project"  : "algos"
    },

    init : function (project, callback) {

        if (window.app.status.currentProject != undefined && window.app.status.currentProject != project) {

            this.destroyView();
            window.app.controllers.browse.closeAll();
            window.app.status.currentProject = undefined;

        }

        if (window.app.status.currentProject == undefined) {

            window.app.status.currentProject = project;
            window.app.controllers.browse.initTabs();
            if (this.view == null) this.createView(callback);
            this.showView();
        } else {
            callback.call();
            this.showView();
        }

    },
    images : function(project) {
        var self = this;
        var func = function() {
            self.view.refreshImagesThumbs();
            var tabs = $("#explorer > .browser").find(".nav-tabs");
            tabs.find('a[href=#tabs-images-'+window.app.status.currentProject+']').click();
        };
        this.init(project, func);
    },
    imagespage : function(project,page) {
        var self = this;
        var func = function() {
            self.view.changeImagePage(page);
            self.view.showImagesThumbs();
            var tabs = $("#explorer > .browser").find(".nav-tabs");
            tabs.find('a[href=#tabs-images-'+window.app.status.currentProject+']').click();
        };
        this.init(project, func);
    },
    imagesthumbs :  function(project) {
        var self = this;
        var func = function() {
            self.view.refreshImagesTable();
            self.view.showImagesThumbs();
            var tabs = $("#explorer > .browser").find(".nav-tabs");
            tabs.find('a[href=#tabs-images-'+window.app.status.currentProject+']').click();
        };
        this.init(project, func);
    },
    imagesarray : function(project) {
        var self = this;
        var func = function() {
            self.view.refreshImagesTable();
            self.view.showImagesTable();
            var tabs = $("#explorer > .browser").find(".nav-tabs");
            tabs.find('a[href=#tabs-images-'+window.app.status.currentProject+']').click();
        };
        this.init(project, func);
    },
    annotations : function(project, terms, users) {
        var self = this;
        var func = function() {
            window.app.controllers.browse.tabs.triggerRoute = false;
            var tabs = $("#explorer > .browser").find(".nav-tabs");
            tabs.find('a[href=#tabs-annotations-'+window.app.status.currentProject+']').click();
            self.view.refreshAnnotations(terms, users);
            window.app.controllers.browse.tabs.triggerRoute = true;

        };
        this.init(project, func);
    },
    algos : function(project,software,job) {
        console.log("Dashboard algos:"+project+"-"+software+"-"+job);
        var self = this;
        var func = function() {
            window.app.controllers.browse.tabs.triggerRoute = false;
            var tabs = $("#explorer > .browser").find(".nav-tabs");
            tabs.find('a[href^=#tabs-algos-'+window.app.status.currentProject+']').click();
            self.view.refreshAlgos(software,job==''?undefined:job);
            window.app.controllers.browse.tabs.triggerRoute = true;
        };
        this.init(project, func);
    },

    config : function(project) {
        var self = this;
        var func = function() {
            self.view.refreshConfig();
            var tabs = $("#explorer > .browser").find(".nav-tabs");
            tabs.find('a[href=#tabs-config-'+window.app.status.currentProject+']').click();
        };
        this.init(project, func);
    },

    dashboard : function(project, callback) {
        var self = this;
        var func = function() {
            self.view.refreshDashboard();
            window.app.controllers.browse.tabs.triggerRoute = false;
            var tabs = $("#explorer > .browser").find(".nav-tabs");
            tabs.find('a[href=#tabs-dashboard-'+window.app.status.currentProject+']').click();
            window.app.controllers.browse.tabs.triggerRoute = true;
            if (callback != undefined) callback.call();

        };
        this.init(project, func);
    },

    createView : function (callback) {
        var self = this;
        new ProjectModel({id : window.app.status.currentProject}).fetch({
            success : function(model, response) {
                window.app.status.currentProjectModel = model;
                self.view = new ProjectDashboardView({
                    model : model,
                    el: $("#explorer-tab-content")
                }).render();
                callback.call();
            }
        });

    },

    destroyView : function() {
        this.view = null;
    },

    showView : function() {
        $("#explorer > .browser").show();
        $("#explorer > .noProject").hide();
        window.app.view.showComponent(window.app.view.components.explorer);
    }
});