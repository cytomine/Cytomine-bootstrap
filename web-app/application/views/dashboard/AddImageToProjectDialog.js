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

var AddImageToProjectDialog = Backbone.View.extend({
    initialize: function (options) {
        _.bindAll(this, 'render');
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/dashboard/AddImageToProjectDialog.tpl.html"
        ],
            function (tpl) {
                self.doLayout(tpl);
            });
        return this;
    },

    doLayout: function (tpl) {
        var self = this;
        var htmlCode = _.template(tpl, self.model.toJSON());
        $(this.el).html(htmlCode);
        $("#addImageToProjectProject").modal('show');
        self.createArray();
    },
    createArray: function () {
        var self = this;
        self.images = [];
        var table = $("#addImageToProjectTable" + self.model.id);
        var columns = [
            { sClass: 'center', "mData": "id", "bSearchable": false,"bSortable": true},
            { "mData": "macroURL", sDefaultContent: "", "bSearchable": false,"bSortable": false, "fnRender" : function (o) {
                return _.template("<div style='width : 130px;'><a href='#tabs-image-<%= project %>-<%=  id  %>-'><img src='<%= thumb %>' alt='originalFilename' style='max-height : 45px;max-width : 128px;'/></a></div>",
                    {
                        project : self.model.id,
                        id : o.aData.id,
                        thumb : o.aData["macroURL"]
                    });
            }},
            { "mDataProp": "originalFilename", sDefaultContent: "", "bSearchable": true,"bSortable": true, "fnRender" : function (o) {
                return o.aData["originalFilename"];
            }} ,
            { "mDataProp": "created", sDefaultContent: "", "bSearchable": false,"bSortable": true, "fnRender" : function (o, created) {
                return window.app.convertLongToDate(created);
            }},
            { "mDataProp": "updated", sDefaultContent: "", "bSearchable": false,"bSortable": false, "fnRender" : function(o) {
                self.images.push(o.aData);
//                new ImageInstanceModel({ id : o.aData.id}).fetch({
//                    success : function (model, response) {
//                        var model = new ImageInstanceModel(o.aData);
//                        var action = new ImageReviewAction({el:body,model:model, container : self});
//                        action.configureAction();
//                    }
//                });
                o.aData["project"]  = self.idProject;
                if(o.aData["inProject"]) {
                    return '<label id="labelAddToProject'+o.aData["id"]+'">Already in project</label>';
                } else {
                    return '<button id="btnAddToProject'+o.aData["id"]+'" type="button" class="btn btn-success btn-xs"><span class="glyphicon glyphicon-plus"></span> Add</button>';
                }
            }}
        ];
        self.imagesdDataTables = table.dataTable({
            "bProcessing": true,
            "bServerSide": true,
            "sAjaxSource": new ImageCollection({project: self.model.id}).url(),
            "fnServerParams": function ( aoData ) {
                aoData.push( { "name": "datatables", "value": "true" } );
            },
            "fnDrawCallback": function(oSettings, json) {

                _.each(self.images, function(aData) {
                    var idProject = self.model.id;
                    var idImage = aData["id"];

                    $("#btnAddToProject"+idImage).click(function() {
                        new ImageInstanceModel({}).save({project: idProject, user: null, baseImage: idImage}, {
                            success: function (image, response) {
                                $("#btnAddToProject"+idImage).replaceWith('<label id="labelAddToProject'+idImage+'">Already in project</label>');
                                window.app.view.message("Image", response.message, "success");
                            },
                            error: function (model, response) {
                                var json = $.parseJSON(response.responseText);
                                window.app.view.message("Image", json.errors.message, "error");
                            }
                        });
                    });



                });
                self.images = [];
            },
            "aoColumns" : columns
        });
        $('#addImageToProjectTable' + self.idProject).show();
    }
});