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

var AnnotationQuestionableView = Backbone.View.extend({
    tagName: "div",
    terms: null,
    term: null,
    suggestTerm: null,
    initialize: function (options) {
        this.container = options.container;
        this.page = options.page;
        this.annotations = null; //array of annotations that are printed
        this.terms = options.terms;
        this.term = options.term;
        this.suggestTerm = options.suggestTerm;
        window.app.status.currentTermsCollection = options.terms;
        if (this.page == undefined) {
            this.page = 0;
        }
    },
    render: function () {
        var self = this;
        $(self.el).html('<div id="questionableThumb"></div>');
        self.createThumbView(self.page);
        return this;

    },
    createTitle: function () {
        var self = this;
        var termCorrect = self.terms.get(self.term).get('name');
        var termSuggest = self.terms.get(self.suggestTerm).get('name');
        return "Annotation with term " + termCorrect + " and algo suggest " + termSuggest;
    },
    createThumbView: function (page) {
        this.appendThumbs(page);
    },
    appendThumbs: function (page) {
        var self = this;
        var cpt = 0;
        var nb_thumb_by_page = 2500;
        var inf = Math.abs(page) * nb_thumb_by_page;
        var sup = (Math.abs(page) + 1) * nb_thumb_by_page;

        self.annotations = [];

        self.model.each(function (annotation) {

            if ((cpt >= inf) && (cpt < sup)) {
                var annotationModel = new AnnotationModel({});
                annotationModel.set(annotation.toJSON());
                var thumb = new AnnotationThumbView({
                    model: annotationModel,
                    terms : window.app.status.currentTermsCollection,
                    className: "thumb-wrap",
                    id: "annotationthumb" + annotationModel.id
                }).render();
                console.log( $("#questionableThumb").length);
                $("#questionableThumb").append(thumb.el);
            }
            cpt++;
            self.annotations.push(annotation.id);
        });
    },
    /**
     * Add the thumb annotation
     * @param annotation Annotation model
     */
    add: function (annotation) {

        var self = this;
        var thumb = new AnnotationThumbView({
            model: annotation,
            terms : window.app.status.currentTermsCollection,
            className: "thumb-wrap",
            id: "thumb" + annotation.get('id')
        }).render();
        $(self.el).prepend(thumb.el);

    },
    /**
     * Remove thumb annotation with id
     * @param idAnnotation  Annotation id
     */
    remove: function (idAnnotation) {
        $("#thumb" + idAnnotation).remove();
    },
    /**
     * Refresh thumb with newAnnotations collection:
     * -Add annotations thumb from newAnnotations which are not already in the thumb set
     * -Remove annotations which are not in newAnnotations but well in the thumb set
     * @param newAnnotations newAnnotations collection
     */
    refresh: function (newAnnotations) {
        var self = this;

        var arrayDeletedAnnotations = self.annotations;
        newAnnotations.each(function (annotation) {
            //if annotation is not in table, add it
            if (_.indexOf(self.annotations, annotation.id) == -1) {
                self.add(annotation);
                self.annotations.push(annotation.id);
            }
            /*
             * We remove each "new" annotation from  arrayDeletedAnnotations
             * At the end of the loop, element from arrayDeletedAnnotations must be deleted because they aren't
             * in the set of new annotations
             */
            //
            arrayDeletedAnnotations = _.without(arrayDeletedAnnotations, annotation.id);

        });

        arrayDeletedAnnotations.forEach(function (removeAnnotation) {
            self.remove(removeAnnotation);
            self.annotations = _.without(self.annotations, removeAnnotation);
        });

    }


});
