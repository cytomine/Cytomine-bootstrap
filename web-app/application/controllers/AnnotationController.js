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

var AnnotationController = Backbone.Router.extend({

    routes: {
        "annotation": "annotation",
        "annotation/:idAnnotation": "annotation",
        "share-annotation/:idAnnotation": "share",
        "copy-annotation/:idAnnotation": "copy"
    },

    annotation: function (idAnnotation) {
        var self = this;
        if (!self.view) {
            window.app.models.images.fetch({
                success: function (collection, response) {
                    self.view = new AnnotationListView({
                        model: collection,
                        el: $("#warehouse > .annotation"),
                        container: window.app.view.components.warehouse,
                        idAnnotation: idAnnotation //selected annotation
                    }).render();
                    self.view.container.views.annotation = self.view;
                    self.view.container.show(self.view, "#warehouse > .sidebar", "annotation");
                    window.app.view.showComponent(window.app.view.components.warehouse);
                }
            });
        }
    },

    share: function (idAnnotation) {

        new AnnotationModel({id: idAnnotation}).fetch({
            success: function (model, response) {
                var shareAnnotationView = new ShareAnnotationView({
                    model: model,
                    image: model.get("image"),
                    project: model.get("project")
                });
                if (!window.app.models.projectUser) {  //direct access
                    new ProjectModel({id: model.get("project")}).fetch({
                        success: function (projectModel, response) {
                            window.app.status.currentProject = projectModel.get("id");
                            window.app.status.currentProjectModel = projectModel;
                            new UserCollection({project: window.app.status.currentProject}).fetch({
                                success: function (collection, response) {
                                    window.app.models.projectUser = collection;
                                    shareAnnotationView.render();
                                }
                            });

                        }
                    });
                } else {
                    shareAnnotationView.render();
                }

            }
        });
    }
});