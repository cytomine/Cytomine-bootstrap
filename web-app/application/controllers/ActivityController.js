/*
 * Copyright (c) 2009-2015. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var ActivityController = Backbone.Router.extend({
    routes: {
        "activity-:project-:user-": "activity",
        "activity-:project-": "activity" ,
        "activity-": "activity"
    },
    activity : function() {
        this.activity(null);
    },
    activity : function(project) {
        this.activity(null,null);
    },
    activity: function (project,user) {
        var self = this;
        if(project==undefined || project=="null") {
            project = null;
        }
        console.log("activity.project="+project);

        var openView = function(project,idUser) {
            console.log("openView");
            console.log(project);
            console.log(idUser);
            //self.changeCurrentProject(project.id);

            if (!self.view) {
                console.log("View is not yet created");
                self.view = new ActivityView({
                    el: "#activity-content"
                }).render();
                self.view.refresh(project,idUser);
            } else {
                console.log("View is yet created, just refresh");
                self.view.refresh(project,idUser);
            }

            window.app.view.showComponent(window.app.view.components.activity);
        };

        var loadProject = function() {

            console.log("user has project");
            var projectModel = null;
            console.log("project="+project);
            if(project!=null) {
                projectModel = window.app.models.projects.get(project);
            }
            openView(projectModel,user);
        };

        console.log(window.app.models.projects);
        if(window.app.models.projects.length==0 || window.app.models.projects.at(0).id == undefined) {
            window.app.models.projects.fetch({
                success: function (collection, response) {
                    window.app.models.projects = collection;
                     if(collection.length==0) {
                         //user has no project
                         console.log("user has no project");
                         window.app.view.message("Project", "There is no project!", "error");
                         window.location = "#project"
                     } else {
                         loadProject();

                     }
                }
            });
        } else {
            loadProject();
        }
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
