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

var ReviewLastReviewListing = Backbone.View.extend({
    project: null,
    user : null,
    initialize: function (options) {
        this.container = options.container;
       this.project = options.project;
       this.user = options.user;
    },
    render: function () {
        var self = this;
        console.log("*********************"+self.project);
        self.model = new AnnotationCollection({project: self.project, user: self.user, max: 5,reviewed:true});
        self.refresh();
        return this;
    },
    refresh :function() {
        var self = this;

        $(self.el).find("#lastReviewedAnnotation").empty();
        $(self.el).find("#lastReviewedAnnotation").append('<div class="alert alert-info"><i class="icon-refresh"/> Loading...</div>');


        self.model.fetch({
            success: function (collection, response) {
                self.model = collection;
                $(self.el).find("#lastReviewedAnnotation").empty();

                if(collection.length==0) {
                    $(self.el).append("<div class='alert alert-block'>No data to display</div> ");
                }

                self.model.each(function(rev) {
                    var thumb = new AnnotationThumbView({
                        model: rev,
                        className: "thumb-wrap",
                        terms : window.app.status.currentTermsCollection,
                        term: "all",
                        reviewMode : true
                    }).render();

                    $(thumb.el).draggable({
                        scroll: true,
                        //scrollSpeed : 00,
                        revert: true,
                        delay: 500,
                        opacity: 0.35,
                        cursorAt: { top: 85, left: 90}
                    });

                    $(thumb.el).append("<p class='terms text-center'></p>")
                    var termNames = []
                    _.each(rev.get("term"), function (it) {
                        var term = window.app.status.currentTermsCollection.get(it);

                        termNames.push(_.template(self.container.termSpanTemplate,term.toJSON()));
                    });

                    $(thumb.el).find(".terms").append(termNames.join(", "));
                    $(thumb.el).find('div').css("margin","0px auto");
                    $(thumb.el).find('div').css("float","none");
                    $(thumb.el).append("<p class='text-center'>"+window.app.convertLongToDate(rev.get('created'))+ "</p>");
                    $(self.el).find("#lastReviewedAnnotation").append("<hr/>");
                    $(self.el).find("#lastReviewedAnnotation").append(thumb.el);
//                    $(thumb.el).find(".thumb-wrap").data("reviewed",'true');
//                    $(thumb.el).data("reviewed",'true');
                    $(self.el).attr("data-reviewed", "true");
                });
            }
        });

        $("#lastReviewListing").find(".thumb-wrap").draggable( {
              containment: self.el,
              stack: "#lastReviewListing .thumb-wrap",
              cursor: 'move',
              revert: true
         } );
    }
});