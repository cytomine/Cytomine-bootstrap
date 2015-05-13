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

var ImageThumbView = Backbone.View.extend({
    className: "thumb-wrap",
    tplProperties: null,

    events: {

    },

    initialize: function (options) {
        this.id = "thumb" + this.model.get('id');
        _.bindAll(this, 'render');
    },

    render: function () {
        this.model.set({ project: window.app.status.currentProject });
        var self = this;
        require(["text!application/templates/image/ImageThumb.tpl.html", "text!application/templates/image/ImageThumbProperties.tpl.html","text!application/templates/image/ImageReviewAction.tpl.html"], function (tpl, tplProperties,tplReviewAction) {
            self.tplProperties = tplProperties;
            var filename = self.model.getVisibleName(window.app.status.currentProjectModel.get('blindMode'));
            self.model.set('originalFilename',filename);
            var title = (filename.length < 27) ? filename : filename.substr(0, 24) + "...";
            var resolution = Math.round(1000 * self.model.get('resolution')) / 1000; //round to third decimal
            self.model.set({title: title, resolution: resolution});
            $(self.el).html(_.template(tpl, self.model.toJSON()));

            $(self.el).find("#image-properties-" + self.model.id).html(_.template(tplProperties, self.model.toJSON()));
            $(self.el).find('#imagereviewaction-thumb-'+self.model.id).append(_.template(tplReviewAction, self.model.toJSON()));
            self.addReviewInfo();
            self.configureAction();

        });

        return this;
    },
    isNotReviewed: function () {
        return this.model.get("reviewStart") == null
    },
    isInReviewing: function () {
        return this.model.get("reviewStart") != null && this.model.get("reviewStop") == null
    },
    addReviewInfo: function () {
        var self = this;
        var reviewingDiv = $(self.el).find("#reviewingImageData" + self.model.id);
        if (self.isNotReviewed()) {
            reviewingDiv.append('<span class="label">Not yet reviewed</span>');
        } else if (self.isInReviewing()) {
            reviewingDiv.append('<span class="label label-warning">' + window.app.models.projectUser.get(self.model.get("reviewUser")).prettyName() + ' starts reviewing</span>');
        } else {
            reviewingDiv.append('<span class="label label-success">' + window.app.models.projectUser.get(self.model.get("reviewUser")).prettyName() + ' has reviewed</span>');
        }
    },

    refresh: function () {
        this.render();
    },
    configureAction: function () {
        var action = new ImageReviewAction({el:this.el, model : this.model, container : this});
        action.configureAction();
    }
});

