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

var ProjectController = Backbone.Router.extend({
    manageView: null,
    routes: {
        "project": "project"
    },

    initView: function () {
        var self = this;

        var projects = null;
        var loadHandler = function () {
            if (projects == null) {
                return;
            }
            self.view = new ProjectView({
                model: projects,
                el: $("#project"),
                container: window.app.view.components.project
            }).render();

            self.view.container.views.project = self.view;

            self.view.container.show(self.view, "#project", "project");
            window.app.view.showComponent(window.app.view.components.project);
        }


        window.app.models.projects.fetch({
            success: function (collection1, response) {
                projects = collection1;
                loadHandler();
        }});
    },

    project: function (callback) {
        console.log("controller.project");
        var self = this;
        $("#warehouse-button").attr("href", "#project");
        $("#addimagediv").hide();
        $("#projectdiv").show();

        var projectCallback = function () {

            self.view.container.show(self.view, "#project", "project");
            window.app.view.showComponent(window.app.view.components.project);
            if (_.isFunction(callback)) {
                callback.call();
            }
        }

        if (!this.view) {
            this.initView();
        } else {
            projectCallback.call();
        }


    }
});