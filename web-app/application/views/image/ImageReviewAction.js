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

var ImageReviewAction = Backbone.View.extend({
    tagName: "div",
    initialize: function (options) {
        this.el = options.el;
        this.model = options.model;
        this.container  = options.container;
    },


    configureAction: function () {
        var self = this;
        var el = $(self.el);
        el.find("#exploreButton" + self.model.id).click(function () {
            window.location = '#tabs-image-' + self.model.get('project') + '-' + self.model.get('id') + '-';
        });
        console.log(self.model)
        if (self.isNotReviewed()) {
            
            el.find("#explore" + self.model.id).show();
            el.find("#review" + self.model.id).hide();
            el.find("#reviewCyto" + self.model.id).hide();
            el.find("#startreview" + self.model.id).show();
            el.find("#startCytoreview" + self.model.id).show();
            el.find("#cancelreview" + self.model.id).hide();
            el.find("#validateimage" + self.model.id).hide();
            el.find("#unvalidateimage" + self.model.id).hide();
            el.find("#moreinfo" + self.model.id).show();
            el.find("#description" + self.model.id).show();
        } else if (self.isInReviewing()) {
            el.find("#explore" + self.model.id).show();
            el.find("#review" + self.model.id).show();
            el.find("#reviewCyto" + self.model.id).show();
            el.find("#startreview" + self.model.id).hide();
            el.find("#startCytoreview" + self.model.id).hide();
            if (self.model.get('numberOfReviewedAnnotations') == 0) {
                el.find("#cancelreview" + self.model.id).show();
            }
            else {
                el.find("#cancelreview" + self.model.id).hide();
            }
            el.find("#validateimage" + self.model.id).show();
            el.find("#unvalidateimage" + self.model.id).hide();
            el.find("#moreinfo" + self.model.id).show();
            el.find("#description" + self.model.id).show();
        } else {
            el.find("#explore" + self.model.id).show();
            el.find("#review" + self.model.id).show();
            el.find("#reviewCyto" + self.model.id).show();
            el.find("#startreview" + self.model.id).hide();
            el.find("#startCytoreview" + self.model.id).hide();
            el.find("#cancelreview" + self.model.id).hide();
            el.find("#validateimage" + self.model.id).hide();
            el.find("#unvalidateimage" + self.model.id).show();
            el.find("#moreinfo" + self.model.id).show();
            el.find("#description" + self.model.id).show();
        }

        el.find("#startreview" + self.model.id).on("click", function () {
            console.log("START REVIEW CLICK");
            self.startReviewing();
            return false;
        });
        if(window.app.status.currentProjectModel.isReadOnly(window.app.models.projectAdmin) || window.app.status.currentProjectModel.get('blindMode')) {
            el.find("a#renameImage" + self.model.id).hide();
        }
        el.find("a#renameImage" + self.model.id).bind('click',function(){
            console.log("rename"+self.model.id);
            self.renameImage();
            return false;
        });

        if(window.app.status.currentProjectModel.get('blindMode') && window.app.models.projectAdmin.get(window.app.status.user.id) == undefined) {
            el.find("#moreinfo" + self.model.id).hide();
        }


        if(window.app.status.currentProjectModel.isReadOnly(window.app.models.projectAdmin)) {
            el.find("a.deleteImage" + self.model.id).hide();
        }
        el.find("a.deleteImage" + self.model.id).bind('click',function(){
            console.log("del"+self.model.id);
            self.deleteImage();
            return false;
        });

        el.find("#startCytoreview" + self.model.id).on("click", function () {
            self.startCytoReviewing();
            return false;
        });

        el.find("#cancelreview" + self.model.id).on("click", function () {
            self.cancelReviewing();
            return false;
        });
        el.find("#validateimage" + self.model.id).on("click", function () {
            self.validateImage();
            return false;
        });
        el.find("#unv" +
                "alidateimage" + self.model.id).on("click", function () {
            self.cancelReviewing();
            return false;
        });
        //el.find("#image-properties-" + self.model.id).html(_.template(tplProperties, self.model.toJSON()));
        $(self.el).on('click',"a#moreinfo" + self.model.id,function () {
            $("#image-properties").remove();
            if(!window.app.status.currentProjectModel.get('blindMode')) {
                new ImagePropertiesView({model: self.model}).render();
            } else {
                //this protection should be done in server too!
                window.app.view.message("Blind mode", "The project is in blind mode...you can't see metdata!", "error");
            }
            return false;
        });

        var openDescription = function () {
                    if(!self.disableEvent) {
                        new DescriptionModel({domainIdent: self.model.id, domainClassName: self.model.get('class')}).fetch(
                               {success: function (description, response) {
                                   self.disableEvent = true;
                                   DescriptionModal.initDescriptionModal(el.find(".action"+self.model.id),description.id,self.model.id,self.model.get('class'),description.get('data'),function() { });
                                   el.find(".action"+self.model.id).find('a.description').click();
                               }, error: function (model, response) {
                                   self.disableEvent = true;
                                   DescriptionModal.initDescriptionModal(el.find(".action"+self.model.id),null,self.model.id,self.model.get('class'),"",function() { });
                                   el.find(".action"+self.model.id).find('a.description').click();

                               }});
                    } else {
                        self.disableEvent = false;
                    }
                    return false;
                }
        $(self.el).find("a.description" + self.model.id).unbind('click',openDescription).bind('click',openDescription);


        var openImportImage = function () {
                if(!self.disableEvent) {
                        ImportAnnotationModal.initImportAnnotationModal(el.find(".action"+self.model.id),self.model.id,function() {});
                        el.find(".action"+self.model.id).find('a.importannotation').click();
                } else {
                    self.disableEvent = false;
                }
                return false;
         }
        $(self.el).find("a.importannotation" + self.model.id).unbind('click',openImportImage).bind('click',openImportImage);
       // $(document).on('click',"a.description" + self.model.id,openDescription);

        var openCopyImage = function () {
                if(!self.disableEvent) {
                    copyImageModal.initCopyImageModal(el.find(".action"+self.model.id),self.model.id,self.model.get('project'),self.model.get('baseImage'),function() {});
                        el.find(".action"+self.model.id).find('a.copyimage').click();
                } else {
                    self.disableEvent = false;
                }
                return false;
            }
        $(self.el).find("a.copyimage" + self.model.id).unbind('click',openCopyImage).bind('click',openCopyImage);


    },
    disableEvent : false,
    startReviewing: function () {
        var self = this;
        console.log("startReviewing");
        new ImageReviewModel({id: self.model.id}).save({}, {
            success: function (model, response) {
                //window.location = "#tabs-images-"+self.model.get('project');
//                window.app.controllers.dashboard.view.projectDashboardImages.refreshImagesThumbs();
                window.app.view.message("Image", response.message, "success");
                self.model = new ImageModel(response.imageinstance);
                if(self.container) {
                    self.container.refresh();
                }

                console.log("Go to review!")
                window.location = '#tabs-review-' + self.model.get('project') + '-' + self.model.get('id') + '-';
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors, "error");
            }});
    },
    startCytoReviewing: function () {
        var self = this;
        console.log("startCytoReviewing");
        new ImageReviewModel({id: self.model.id}).save({}, {
            success: function (model, response) {
                window.app.view.message("Image", response.message, "success");
                self.model = new ImageModel(response.imageinstance);
                if(self.container) {
                    self.container.refresh();
                }
                window.location = '#tabs-reviewdash-' + self.model.get('project') + '-' + self.model.get('id') + '-null-null';
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors, "error");
            }});
    },
    cancelReviewing: function () {
        var self = this;
        console.log("cancelReviewing");
        new ImageReviewModel({id: self.model.id, cancel: true}).destroy({
            success: function (model, response) {
                //window.location = "#tabs-images-"+self.model.get('project');
//                window.app.controllers.dashboard.view.projectDashboardImages.refreshImagesThumbs();
                window.app.view.message("Image", response.message, "success");
                console.log(response);
                self.model = new ImageModel(response.imageinstance);
                if(self.container) {
                    self.container.refresh();
                }
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors, "error");
            }});
    },
    validateImage: function () {
        var self = this;
        console.log("validateImage");
        new ImageReviewModel({id: self.model.id}).destroy({
            success: function (model, response) {
                window.app.view.message("Image", response.message, "success");
                console.log(response);
                self.model = new ImageModel(response.imageinstance);
                if(self.container) {
                    self.container.refresh();
                }
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors, "error");
            }});
    },
    isNotReviewed: function () {
        return this.model.get("reviewStart") == null
    },
    isInReviewing: function () {
        return this.model.get("reviewStart") != null && this.model.get("reviewStop") == null
    },

    renameImage: function () {
        var self = this;

        new ImageInstanceModel({id: self.model.id}).fetch(
            {
                success: function (model, response) {
                    var imageInstanceModel = model;
                    var values =[];
                    values.push({field : 'instanceFilename', name : 'Name', value : imageInstanceModel.getVisibleName(window.app.status.currentProjectModel.get('blindMode'))});

                    UpdateTextFiedsModal.initUpdateTextFiedsModal(self.model.id, "Image", 'Rename an image', 'Enter the new name of this image', values, function(newValues){
                        for(var i=0;i<values.length;i++){
                            imageInstanceModel.set(values[i].field, values[i].value);
                        }
                        imageInstanceModel.save(null,{
                            success: function (model, response) {
                                window.app.view.message("Image", response.message, "success");
                                self.container.refresh();
                            }
                        });
                    });
                }
            }
        );

    },
    deleteImage: function () {
        var self = this;
        require(["text!application/templates/dashboard/ImageDeleteConfirmDialog.tpl.html"], function (tpl) {
            // $('#dialogsTerm').empty();
            var dialog = new ConfirmDialogView({
                el: '#dialogsDeleteImage',
                template: _.template(tpl, {image: self.model.get('originalFilename')}),
                dialogAttr: {
                    dialogID: '#delete-image-confirm'
                }
            }).render();
            $("#closeImageDeleteConfirmDialog").click(function (event) {
                event.preventDefault();
                new TaskModel({project: self.model.get('project')}).save({}, {
                        success: function (taskResponse, response) {
                            var task = taskResponse.get('task');

                            console.log("task"+task.id);
                            var timer = window.app.view.printTaskEvolution(task, $("#deleteImageDialogContent"), 1000);


                            new ImageInstanceModel({id: self.model.id,task: task.id}).destroy(
                                {
                                    success: function (model, response) {
                                        window.app.view.message("Image", response.message, "success");
                                        self.container.afterDeleteImageEvent();
                                        clearInterval(timer);
                                        dialog.close();

                                    },
                                    error: function (model, response) {
                                        clearInterval(timer);
                                        console.log(response);
                                        var json = $.parseJSON(response.responseText);
                                        window.app.view.message("Image", json.errors, "error");
                                    }
                                }
                            );
                            return false;
                        },
                        error: function (model, response) {
                            var json = $.parseJSON(response.responseText);
                            window.app.view.message("Task", json.errors, "error");
                        }
                    }
                );
            });
        });

    }
});
