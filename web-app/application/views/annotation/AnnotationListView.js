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

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:12
 * To change this template use File | Settings | File Templates.
 */
var AnnotationListView = Backbone.View.extend({
    tagName: "div",
    self: this,
    alreadyBuild: false,
    initialize: function (options) {
        this.container = options.container;
        this.idAnnotation = options.idAnnotation;
    },

    render: function () {
        var self = this;
        require([
            "text!application/templates/annotation/AnnotationList.tpl.html"
        ],
            function (tpl) {
                self.doLayout(tpl);
            });

        return this;
    },
    doLayout: function (tpl) {


        var self = this;
        $(this.el).html(_.template(tpl, {name: "name", area: "area"}));

        self.model.each(function (annotation) {
            //$("#annotationList").append(annotation.get('name') + " <br>");
            var name = annotation.get('name');
            var area = annotation.get('area');
            //$("#tableImage").append("<tr><th>"+ name +"</th><th>" + area + "</th></tr>");

        });
        // $('#tableImage').dataTable();

        var grid;
        var i = 0;
        var data = [];
        self.model.each(function (image) {
            data[i] = {
                id: image.id,
                filename: image.get('filename'),
                created: ''
            };
            i++;
        });
        return this;
    },
    /**
     * Init annotation tabs
     */
    initAnnotation: function () {
        var self = this;


    }
});
