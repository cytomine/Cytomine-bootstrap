var SearchController = Backbone.Router.extend({
    routes: {
        "search-:project-": "search" ,
        "search-": "search"
    },
    search : function() {
        this.search(null);
    },
    search: function (project) {
        var self = this;
        if(project==undefined || project=="null") {
            project = null;
        }
        console.log("search.project="+project);

        var openView = function(project) {
            console.log("openView");
            console.log(project);
            //self.changeCurrentProject(project.id);

            if (!self.view) {
                console.log("View is not yet created");
                self.view = new SearchView({
                    el: "#search"
                }).render();
                self.view.refresh(project);
            } else {
                console.log("View is yet created, just refresh");
                self.view.refresh(project);
            }

            window.app.view.showComponent(window.app.view.components.search);
        };

        openView(project);
    },
//    changeCurrentProject: function (project, callback) {
//        //TODO: merge method with same method in dashboard
//        console.log("window.app.status.currentProject=" + window.app.status.currentProject + " new project=" + project);
//        if (window.app.status.currentProject != undefined && window.app.status.currentProject != project) {
//            this.destroyView();
//            window.app.controllers.browse.closeAll();
//            window.app.status.currentProject = undefined;
//            window.app.view.clearIntervals();
//        }
//
//        if (window.app.status.currentProject == undefined) {
//            window.app.view.clearIntervals();
//            window.app.status.currentProject = project;
//            window.app.controllers.browse.initTabs();
//        }
//    },

    destroyView: function () {
        $(".projectUserDialog").modal('hide');
        $(".projectUserDialog").remove();
        this.view = null;
    }
});
