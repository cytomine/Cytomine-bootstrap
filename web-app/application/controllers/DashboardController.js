
var DashboardController = Backbone.Router.extend({

    view : null,
    routes: {
        "tabs-images-:project"  : "images",
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
            window.app.view.clearIntervals();
        }

        if (window.app.status.currentProject == undefined) {

            window.app.view.clearIntervals();
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
    imagesthumbs :  function(project) {
        var self = this;
        var func = function() {
            self.view.refreshImagesThumbs();
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
        var self = this;
        var func = function() {
            window.app.controllers.browse.tabs.triggerRoute = false;
            var tabs = $("#explorer > .browser").find(".nav-tabs");
            tabs.find('a[href^=#tabs-algos-'+window.app.status.currentProject+']').click();
            self.view.refreshAlgos(software, job || undefined);
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

        var nbCollectionToFetch = 5;
        var nbCollectionToFetched = 0;
        var collectionFetched = function(expected) {
            nbCollectionToFetched++;
            if (nbCollectionToFetched < expected) return;
            self.view = new ProjectDashboardView({
                model : window.app.status.currentProjectModel,
                el: $("#explorer-tab-content")
            }).render();
            callback.call();
        }
        new UserJobCollection({project : window.app.status.currentProject}).fetch({
            success : function(collection, response) {
                window.app.models.projectUserJob = collection;
                collectionFetched(nbCollectionToFetch);
            }
        });
        new UserJobCollection({project : window.app.status.currentProject, tree : true}).fetch({
            success : function(collection, response) {
                window.app.models.projectUserJobTree = collection;
                collectionFetched(nbCollectionToFetch);
            }
        });
        new UserCollection({project : window.app.status.currentProject}).fetch({
            success : function(collection, response) {
                window.app.models.projectUser = collection;
                collectionFetched(nbCollectionToFetch);
            }
        });
        new ProjectModel({id : window.app.status.currentProject}).fetch({
            success : function(model, response) {
                window.app.status.currentProjectModel = model;
                collectionFetched(nbCollectionToFetch);
                new OntologyModel({id:window.app.status.currentProjectModel.get("ontology")}).fetch({
                    success : function(model, response) {
                        window.app.status.currentOntologyModel = model;
                        collectionFetched(nbCollectionToFetch);
                    }
                });
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
    },
    //print job param value in cell
    printJobParameterValue : function(param,cell,maxSize) {
        var self = this;
        if(param.type=="Date") {
            cell.html(window.app.convertLongToDate(param.value));
        } else if(param.type=="Boolean") {
            if(param.value=="true") {
                cell.html('<input type="checkbox" name="" checked="checked" />');
            }
            else {
                cell.html('<input type="checkbox" name="" />');
            }
        }
        else if(param.type=="ListDomain" || param.type=="Domain")  {
            var ids = param.value.split(",");
            console.log("Domain or ListDomain:"+ids);
            var collection =  window.app.getFromCache(window.app.replaceVariable(param.uri));
            if (collection == undefined || (collection.length > 0 && collection.at(0).id == undefined)) {
                console.log("Collection is NOT CACHE - Reload collection");
                collection = new SoftwareParameterModelCollection({uri: window.app.replaceVariable(param.uri), sortAttribut : param.uriSortAttribut});
                collection.fetch({
                    success:function (col, response) {
                        window.app.addToCache(window.app.replaceVariable(param.uri),col);
                        cell.html(self.createJobParameterDomainValue(ids, col,param,maxSize));
                        cell.find("a").popover();
                    }
                });
            } else {
                console.log("Collection is CACHE");
                cell.html(self.createJobParameterDomainValue(ids, collection,param,maxSize));
                cell.find("a").popover();
            }
        }
        else {
            var computeValue = param.value;
            if(param.name.toLowerCase()=="privatekey" || param.name.toLowerCase()=="publickey") computeValue = "************************************";
            cell.html(computeValue);
        }
    },
    createJobParameterDomainValue : function(ids, collection, param,maxSize){
        var names = new Array();
        _.each(ids, function(id){
            var name = collection.get(id);
            if(name==undefined) names.push("Unknown");
            else names.push(name.get(param.uriPrintAttribut));

        });
        names = _.sortBy(names, function(name){ return name;});
        var computeValue = names.join(', ');
        var shortValue=computeValue;
        if(computeValue.length>maxSize) {
            shortValue = computeValue.substring(0,maxSize)+"...";
        }
        return '<a class="cellPopover" data-placement="top" rel="popover" data-content="'+computeValue+'" data-original-title="">'+shortValue+'</a>';
    }
});