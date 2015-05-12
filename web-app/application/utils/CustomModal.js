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

var CustomModal = Backbone.View.extend({
    initialize: function (options) {
        this.buttons = [];
        this.idModal = options.idModal;
        this.button = options.button;
        this.header = options.header;
        this.body = options.body;
        this.swide = options.swide || false;
        this.wide = options.wide || false;
        this.xwide = options.xwide || false;
        this.callBack = options.callBack;
        this.callBackAfterCreation = options.callBackAfterCreation;
        this.registerModal();
        //style="width: ' + (width - 100) + 'px;height: ' + (height - 100) + 'px;"
        this.width = options.width;
        this.height = options.height;
    },
    addButtons: function (id, text, primary, close, callBack) {
        this.buttons.push({id: id, text: text, close: (close ? 'modal' : ''), primaryClass: (primary ? 'btn-primary' : ''), callBack: callBack});
    },
    registerModal: function () {
        var self = this;

        this.render = function () {

            require([
                    "text!application/templates/utils/CustomModal.tpl.html"
                ],
                function (tplModal) {

                    var modal = $("#modals");
                    modal.empty();

                    var htmlModal = _.template(tplModal,{
                        id : self.idModal,
                        header : self.header,
                        body : self.body,
                        swide : (self.swide ? "modal-swide" : ""),
                        wide : (self.wide ? "modal-wide" : ""),
                        xwide : (self.xwide ? "modal-xwide" : ""),
                        buttons : self.buttons
                    });

                    modal.append(htmlModal);

                    if(self.width) {
                        $('#'+self.idModal).css("width",self.width)
                    }
                    if(self.height) {
                        $('#'+self.idModal).css("height",self.height)
                    }
                    _.each(self.buttons, function (b) {
                        $("#" + b.id).click(function () {
                            if (b.callBack) {
                                b.callBack();
                            }
                            return true;
                        });
                    });

                    if (self.callBack) {
                        self.callBack();
                    }
                    if (self.callBackAfterCreation) {
                        self.callBackAfterCreation();
                    }

                });
            return true;
        };
        if(self.button) {
            //when click on button to open modal, build modal html, append to doc and open modal
            self.button.unbind();
            self.button.click(function(evt) {
                console.log("click sur le button");
                self.render();
            });
        }// else, the owner must call render() by its own.
    },
    close: function () {
        $('#' + this.idModal).modal('hide').remove();
        $('body').removeClass('modal-open');
        $('.modal-backdrop').remove();
    }
});


var DescriptionModal = {

    initDescriptionModal: function (container, idDescription, domainIdent, domainClassName, text, callback) {
        var width = Math.round($(window).width() * 0.58);
        var height = Math.round($(window).height() * 0.58);
        var self = this;

        //add host url for images
        text = text.split('/api/attachedfile').join(window.location.protocol + "//" + window.location.host + '/api/attachedfile');

        var message = "Add the keyword STOP_PREVIEW where you want to delimit the preview text.";
        if (!self.editable) {
            text = text.replace("STOP_PREVIEW", "");
            message = "";
        }

        var callBackAfterCreation = function() {

            $("iframe").contents().find("body").attr('contenteditable', self.editable);
            if (window.app.status.currentProjectModel.isReadOnly(window.app.models.projectAdmin)) {
                $("#saveDescription" + idDescription).hide();
            }


            $('.modal-header button').after('<span class="resize-button" aria-hidden="true"><i class="glyphicon glyphicon-resize-full"/></span>');
            $('.modal-header span').on("click", ".glyphicon-resize-full", function(e) {
                $(this).toggleClass('glyphicon-resize-full');
                $(this).toggleClass('glyphicon-resize-small');
                //as we don't set width and height for the description modal, it takes these parameters of this parents. So I change these.
                $('.modal-dialog').css({
                    'width': '80%',
                    'max-width': '80%'
                });
                $('iframe').parent().css({
                    'height': function( index, value ) {
                        return parseFloat( value ) * 1.5;
                    }
                });
            });
            $('.modal-header span').on("click", ".glyphicon-resize-small", function(e) {
                $(this).toggleClass('glyphicon-resize-full');
                $(this).toggleClass('glyphicon-resize-small');
                $('.modal-dialog').css({
                    'width': '60%',
                    'max-width': '60%'
                });
                $('iframe').parent().css({
                    'height': function( index, value ) {
                        return parseFloat( value ) / 1.5;
                    }
                });
            });
        }

        var modal = new CustomModal({
            idModal: "descriptionModal" + domainIdent,
            button: container.find("a.description"),
            header: "Description",
            body: message + '<div id="description' + domainIdent + '"><textarea style="width: ' + (width - 100) + 'px;height: ' + (height - 100) + 'px;" id="descriptionArea' + domainIdent + '" placeholder="Enter text ...">' + text + '</textarea></div>',
            wide: true,
            callBack: function () {
                //$("#descriptionArea" + domainIdent).wysihtml5({});

//                <textarea name="editor1" id="editor1" rows="10" cols="80">
//                This is my textarea to be replaced with CKEditor.
//                </textarea>


                if (self.editable) {
                    CKEDITOR.replace("descriptionArea" + domainIdent,
                        {    filebrowserBrowseUrl: '/test/browse.php',
                            filebrowserImageBrowseUrl: '/browser/browse.php?type=Images',
                            filebrowserUploadUrl: '/api/attachedfileCKEditor.json?domainClassName=' + domainClassName + "&domainIdent=" + domainIdent,
                            filebrowserImageUploadUrl: '/api/attachedfileCKEditor.json?domainClassName=' + domainClassName + "&domainIdent=" + domainIdent});


                } else {
                    $("#description" + domainIdent).html(text);
                }


                $("#saveDescription" + idDescription).click(function (e) {
                    // remove the host url for images

                    text = CKEDITOR.instances["descriptionArea" + domainIdent].getData().split(window.location.protocol + "//" + window.location.host + '/api/attachedfile').join('/api/attachedfile');
                    new DescriptionModel({id: idDescription, domainIdent: domainIdent, domainClassName: domainClassName}).save({
                        domainIdent: domainIdent,
                        domainClassName: domainClassName,
                        data: text
                    }, {success: function (termModel, response) {

                        callback();
                    }, error: function (model, response) {
                        var json = $.parseJSON(response.responseText);
                        window.app.view.message("Auth error", "error:" + json.errors, "");
                    }});

                });

            },
            callBackAfterCreation : callBackAfterCreation
        });

        modal.addButtons("saveDescription" + idDescription, "Save", true, true);
        modal.addButtons("closeDescription" + idDescription, "Close", false, true);
    },
    initDescriptionView: function (domainIdent, domainClassName, container, maxPreviewCharNumber, callbackGet, callbackUpdate) {
        var self = this;
        self.editable = !window.app.status.currentProjectModel.isReadOnly(window.app.models.projectAdmin)

        new DescriptionModel({domainIdent: domainIdent, domainClassName: domainClassName}).fetch(
                {success: function (description, response) {
                    container.empty();
                    var text = description.get('data');
                    var textButton = "See full text";
                    if(self.editable) {
                        textButton = textButton + " and edit";
                    }
                    text = text.split('STOP_PREVIEW')[0];
                    text = text.split('\\"').join('"');
                    if (text.replace(/<[^>]*>/g, "").length > maxPreviewCharNumber) {
                        text = text.substr(0, maxPreviewCharNumber) + "...";
                    }
                    container.append(text);
                    container.append(' <a href="#descriptionModal' + domainIdent + '" role="button" class="description" data-toggle="modal">' + textButton + '</a>');
                    callbackGet();

                    self.initDescriptionModal(container, description.id, domainIdent, domainClassName, description.get('data'), callbackUpdate);
                }, error: function (model, response) {
                    container.empty();
                    var html = "No description yet";
                    if(self.editable) {
                        html = ' <a href="#descriptionModal' + domainIdent + '" role="button" class="description" data-toggle="modal">Add description</a>';
                    }
                    container.append(html);
                    self.initDescriptionModal(container, null, domainIdent, domainClassName, "", callbackUpdate);

                }});

    }
}


var ImportAnnotationModal = {

    initImportAnnotationModal: function (container, idImage, callback) {
        var width = Math.round($(window).width() * 0.75);
        var height = Math.round($(window).height() * 0.75);


        var modal = new CustomModal({

            idModal: "importAnnotationModal" + idImage,
            button: container.find("a.importannotation"),
            header: "Import Annotation Layer",
            body: '<div id="importLayer' + idImage + '">Import Annotation allow you to get annotations (terms, descriptions, properties) from an image from another project. ' +
                    '<br/>The image must be the same image, in an other project. You can only import annotation from layer (user) with at least 1 annotation and layer that are in the current project.<br/><br/>  <div id="layersSelection' + idImage + '"><div class="alert alert-info"><i class="icon-refresh"/> Loading...</div></div></div>',
            width: width,
            height: height,
            callBack: function () {

                $.get("/api/imageinstance/" + idImage + "/sameimagedata.json",function (data) {
                    $("#layersSelection" + idImage).empty();
                    if (data.collection.length == 0) {
                        $("#layersSelection" + idImage).append("This image has no other layers in other projects.");
                    } else {
                        $("#layersSelection" + idImage).append('<input type="checkbox" id="giveMeAnnotations"> Copy all annotations on my layer (if not checked, annotation will stay on the same layers) </input><br/><br/><br/> ');
                        _.each(data.collection, function (item) {
                            var layer = item.image + "_" + item.user
                            var templ = '<input type="checkbox" class="layerSelection" id="' + layer + '"> Import annotation from project ' + item.projectName + ' -> ' + item.lastname + " " + item.firstname + ' (' + item.username + ') <br/>';
                            $("#layersSelection" + idImage).append(templ);
                        });
                    }
                }).fail(function (json) {
                            window.app.view.message("Import data", json.responseJSON.errors, "error", 5000);
                            $("#closeImportLayer" + idImage).click();
                        });

                $("#importLayersButton" + idImage).click(function (e) {

                    $("#closeImportLayer" + idImage).hide();
                    $("#importLayersButton" + idImage).hide();
                    var layers = []
                    _.each($("#importLayer" + idImage).find("input.layerSelection"), function (item) {
                        if ($(item).is(':checked')) {
                            layers.push(item.id)
                        }
                    });
                    var giveMe = $("#giveMeAnnotations").is(':checked');
                    $("#layersSelection" + idImage).empty();
                    new TaskModel({project: window.app.status.currentProject}).save({}, {
                                success: function (task, response) {
                                    console.log(response.task);
                                    $("#layersSelection" + idImage).append('<div id="task-' + response.task.id + '"></div>');
                                    var timer = window.app.view.printTaskEvolution(response.task, $("#layersSelection" + idImage).find("#task-" + response.task.id), 2000);


                                    $.post("/api/imageinstance/" + idImage + "/copyimagedata.json?task=" + response.task.id + "&layers=" + layers.join(",") + "&giveMe=" + giveMe,function (data) {
                                        clearInterval(timer);
                                        $("#closeImportLayer" + idImage).show();
                                        $("#closeImportLayer" + idImage).click();
                                    }).fail(function (json) {
                                                clearInterval(timer);
                                                window.app.view.message("Import data", json.errors, "error");
                                            });
                                },
                                error: function (model, response) {
                                    var json = $.parseJSON(response.responseText);
                                    window.app.view.message("Task", json.errors, "error");
                                }
                            }
                    );
                });

            }
        });
        modal.addButtons("importLayersButton" + idImage, "Import these layers", true, false);
        modal.addButtons("closeImportLayer" + idImage, "Close", false, true);

    }
}


var copyImageModal = {

    initCopyImageModal: function (container, idImage, idProject, idBaseImage, callback) {
        var width = Math.round($(window).width() * 0.75);
        var height = Math.round($(window).height() * 0.75);


        var modal = new CustomModal({

            idModal: "copyImageModal" + idImage,
            button: container.find("a.copyimage"),
            header: "Copy image to another project",
            body: '<div id="importLayer' + idImage + '">Copy image allow you to copy an image to another project. It can copy image data (description, annotations,...).' +
                    '<br/>You can only import annotation from layer (user) with at least 1 annotation. Layer must be in project dest and project source!<br/><br/>  ' +
                    '<div id="projectSelection' + idImage + '"><div class="alert alert-info"><i class="icon-refresh"/> Loading...</div></div>' +
                    '<div id="layersSelection' + idImage + '"></div></div>',
            width: width,
            height: height,
            callBack: function () {
                var modal = $("#importLayer" + idImage)
                $("#importLayersButton" + idImage).hide();
                window.app.models.projects.fetch({
                    success: function (collection1, response) {
                        window.app.models.projects = collection1;
                        modal.find("#projectSelection" + idImage).empty();
                        modal.find("#projectSelection" + idImage).append('<select></select><br/><br/><button class="btn copytoproject">Copy to project</button>')


                        window.app.models.projects.each(function (project) {
                            modal.find("#projectSelection" + idImage).find("select").append('<option value="' + project.id + '">' + project.get('name') + '</option>');
                        });

                        modal.find(".copytoproject").click(function () {
                             $("#layersSelection" + idImage).append('<br><div class="alert alert-info"><i class="icon-refresh"/> Loading...Please wait...</div>');
                            $("#closeImportLayer" + idImage).hide();
                            new ImageInstanceModel({}).save({project: modal.find("#projectSelection" + idImage).find("select").val(), user: null, baseImage: idBaseImage}, {
                                success: function (image, response) {



                                    var newImageId = image.get('imageinstance').id

                                    $.post("/api/imageinstance/" + newImageId + "/copymetadata.json?based=" + idImage,function (data) {

                                        $.get("/api/imageinstance/" + newImageId + "/sameimagedata.json?project=" + idProject,function (data) {
                                            $("#layersSelection" + idImage).empty();
                                            $("#layersSelection" + idImage).append('<br><div class="alert alert-info">The image is now in the selected project. You can now import layer data (annotations):</div>');


                                            $("#closeImportLayer" + idImage).show();
                                            if (data.collection.length == 0) {
                                                $("#layersSelection" + idImage).append("This image has no other layers with annotations and commons layers with the selected project in the current project.");
                                            } else {
                                                $("#importLayersButton" + idImage).show();



                                                $("#layersSelection" + idImage).append('<input type="checkbox" id="giveMeAnnotations"> Copy all annotations on my layer (if not checked, annotation will stay on the same layers) </input><br/><br/><br/> ');
                                                _.each(data.collection, function (item) {
                                                    var layer = item.image + "_" + item.user
                                                    var templ = '<input type="checkbox" class="layerSelection" id="' + layer + '"> Import annotation from project ' + item.projectName + ' -> ' + item.lastname + " " + item.firstname + ' (' + item.username + ') <br/>';
                                                    $("#layersSelection" + idImage).append(templ);
                                                });
                                            }
                                        }).fail(function (json) {
                                                    window.app.view.message("Import data", json.responseJSON.errors, "error", 5000);
                                                    $("#closeImportLayer" + idImage).click();
                                                });


                                        $("#importLayersButton" + idImage).click(function (e) {

                                            $("#closeImportLayer" + idImage).hide();
                                            $("#importLayersButton" + idImage).hide();
                                            var layers = []
                                            _.each($("#importLayer" + idImage).find("input.layerSelection"), function (item) {
                                                if ($(item).is(':checked')) {
                                                    layers.push(item.id)
                                                }
                                            });
                                            var giveMe = $("#giveMeAnnotations").is(':checked');
                                            $("#layersSelection" + idImage).empty();
                                            new TaskModel({project: window.app.status.currentProject}).save({}, {
                                                        success: function (task, response) {
                                                            console.log(response.task);
                                                            $("#layersSelection" + idImage).append('<div id="task-' + response.task.id + '"></div>');
                                                            var timer = window.app.view.printTaskEvolution(response.task, $("#layersSelection" + idImage).find("#task-" + response.task.id), 2000);


                                                            $.post("/api/imageinstance/" + newImageId + "/copyimagedata.json?task=" + response.task.id + "&layers=" + layers.join(",") + "&giveMe=" + giveMe,function (data) {
                                                                clearInterval(timer);
                                                                window.app.view.message("Copy","Image copy success", "success");
                                                                $("#closeImportLayer" + idImage).show();
                                                                $("#closeImportLayer" + idImage).click();
                                                            }).fail(function (json) {

                                                                        clearInterval(timer);
                                                                        window.app.view.message("Import data", json.errors, "error");
                                                                    });
                                                        },
                                                        error: function (model, response) {
                                                            var json = $.parseJSON(response.responseText);
                                                            window.app.view.message("Task", json.errors, "error");
                                                        }
                                                    }
                                            );
                                        });


                                    }).fail(function (json) {
                                                window.app.view.message("Import data", json.errors, "error");
                                            });

                                },
                                error: function (model, response) {
                                    var json = $.parseJSON(response.responseText);
                                    console.log(json.errors);
                                    window.app.view.message("Image", json.errors, "error");
                                }
                            });

                        });
                    }});


            }
        });
        modal.addButtons("importLayersButton" + idImage, "Import these layers", true, false);
        modal.addButtons("closeImportLayer" + idImage, "Close", false, true);

    }
}

var UpdateTextFiedsModal = {

    initUpdateTextFiedsModal: function (id, type, title, text, values, callback) {

        // create a text en loopant sur le tab [{field, nom, value}]
        var body = '<p>'+text+'</p>';
        for(var i=0;i<values.length;i++){
            body = body + '<br/>'
            body = body + '<div class="row"><div class="col-md-3 col-md-offset-1"> '+values[i].name +'</div>';
            body = body + '<div class="col-md-7"><input type="text" class="form-control" id="'+values[i].field + id +'" value="'+values[i].value+'"></div></div>';
        }



        var modal = new CustomModal({
            idModal: "updateModal" + type + id,
            header: title,
            body: body,
            wide: true
        });

        // here the callback returns the values to update.
        modal.addButtons("UpdateFields" + id, "Save", true, true, function(){
            for(var i=0;i<values.length;i++){
                values[i].value = $('#'+values[i].field + id)[0].value;
            }

            callback(values);
        });
        modal.addButtons("closeUpdateFields" + id, "Close", false, true);

        modal.render();
        $('#updateModal' + type + id).modal();// display the dialog box
    }
}


// if a more custom dialogbox is required, check for http://bootboxjs.com/ (user CustomDialog to create our dialog box)
var DialogModal = {
    // if needed, pass as an argument of the callback the value of a checkbox (created in a footer) "don't ask me this for the next annotations in this location ("for this layer" or "for this image" need more development)".
    initDialogModal: function (container, id, type, text, level, callback) {

        if (level != 'WARNING' && level != 'CONFIRMATIONWARNING' && level != 'INFO' && level != 'ERROR') {
            level = 'INFO';
        }
        var body;
        var header;
        if(level == 'WARNING' || level == 'CONFIRMATIONWARNING'){
            body = '<div class="alert alert-warning">';
            header = "Be carefull !"
        } else if(level == 'INFO'){
            body = '<div class="alert alert-info">';
            header = "Information"
        } else {
            body = '<div class="alert alert-error">';
            header = "Error"
        }
        body = body + '<div id="'+ type +'DialogBox' + id + '">' + text + '</div></div>';

        var modal = new CustomModal({
            idModal: type + "DialogModal" + id,
            header: header,
            body: body,
            swide: true
        });
        if(level == 'CONFIRMATIONWARNING'){
            modal.addButtons("DialogBoxYesButton", "Yes", false, true, callback);
            modal.addButtons("DialogBoxNoButton", "No", true, true); // do nothing
        } else {
            modal.addButtons("DialogBoxOkButton", "Ok", true, true, callback);
        }
        modal.render();
        $('#' + type + 'DialogModal' + id).modal();// display the dialog box
    }
};