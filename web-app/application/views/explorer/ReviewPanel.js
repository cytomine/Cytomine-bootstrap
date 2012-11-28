/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 12/11/15
 * Time: 14:38
 * To change this template use File | Settings | File Templates.
 */
var ReviewPanel = Backbone.View.extend({
    tagName:"div",
    userLayers:null,
    userJobLayers:null,
    reviewLayer:null,
    /**
     * ExplorerTabs constructor
     * @param options
     */
    initialize:function (options) {
        this.browseImageView = options.browseImageView;
        this.userLayers = options.userLayers;
        this.userJobLayers = options.userJobLayers;
        this.printedLayer = [];
        this.layerName = {};
    },
    isLayerPrinted : function(layer) {

        console.log("isLayerPrinted "+layer);
        var isPresent = false;
        _.each(this.printedLayer,function(item) {
              if(item.id==layer) isPresent = true;
        });
        console.log("isLayerPrinted="+isPresent);
        return isPresent;
    },
    /**
     * Grab the layout and call ask for render
     */
    render:function () {
        var self = this;
        require([
            "text!application/templates/explorer/ReviewPanel.tpl.html"
        ], function (tpl) {
            self.doLayout(tpl);
        });

        return this;
    },
    initControl:function (layerAnnotation) {
        var self = this;
        console.log("initControl="+layerAnnotation.name);
        var selectFeature = new OpenLayers.Control.SelectFeature(layerAnnotation.vectorsLayer);
            var layer = layerAnnotation;
            layer.initControls(self.browseImageView, selectFeature);
            layer.registerEvents(self.browseImageView.map);
            layer.controls.select.activate();


        if (_.isFunction(self.browseImageView.initCallback)) self.browseImageView.initCallback.call();
        self.browseImageView.initAutoAnnoteTools();
    },
    /**
     * Add the review layer on the map
     */
    addReviewLayerToReview:function () {
        var self = this;

        var layer = "REVIEW";
        var layerAnnotation = new AnnotationLayer(layer, self.model.get('id'), 0, "#ff0000", self.browseImageView.ontologyPanel.ontologyTreeView, self.browseImageView, self.browseImageView.map, true);

        layerAnnotation.loadAnnotations(self.browseImageView);
        self.printedLayer.push({id:layer, vectorsLayer:layerAnnotation.vectorsLayer, layer:layerAnnotation});

        var selectFeature = new OpenLayers.Control.SelectFeature([layerAnnotation.vectorsLayer]);
        layerAnnotation.isOwner = true;
        layerAnnotation.initControls(self.browseImageView, selectFeature);
        layerAnnotation.registerEvents(self.browseImageView.map);

        self.browseImageView.userLayer = layerAnnotation;
        layerAnnotation.vectorsLayer.setVisibility(true);
        layerAnnotation.toggleIrregular();
        //Simulate click on None toolbar
        var toolbar = $("#" + self.browseImageView.divId).find('#toolbar' + self.model.get('id'));
        toolbar.find('input[id=none' + self.model.get('id') + ']').click();

        layerAnnotation.controls.select.activate();
        self.reviewLayer = layerAnnotation;
        //self.initControl(layerAnnotation);


        _.each(this.printedLayer, function(item) {
            item.layer.controls.select.activate();
        });
    },
    /**
     * Add a specific user/job layer on the map
     * @param layer User/UserJob id
     */
    addLayerToReview:function (layer) {
        var self = this;
        var panelElem = $("#" + this.browseImageView.divId).find("#reviewPanel" + self.model.get("id"));

        self.addToListLayer(layer);

        var user = self.userLayers.get(layer);
        if (user == undefined) user = self.userJobLayers.get(layer);
        if (user == undefined) console.log("ERROR! LAYER NOT DEFINED!!!");

        var layerAnnotation = new AnnotationLayer(user.prettyName(), self.model.get('id'), user.get('id'), user.get('color'), self.browseImageView.ontologyPanel.ontologyTreeView, self.browseImageView, self.browseImageView.map, true);
        layerAnnotation.isOwner = (user.get('id') == window.app.status.user.id);
        if(layerAnnotation.isOwner) {
            self.browseImageView.userLayer = layerAnnotation;
        }
        layerAnnotation.loadAnnotations(self.browseImageView);

        self.printedLayer.push({id:layer, vectorsLayer:layerAnnotation.vectorsLayer, layer:layerAnnotation});

        //disable from selecy list
        var selectElem = panelElem.find("#reviewChoice" + self.model.get("id")).find("select");
        selectElem.find("option#" + layer).attr("disabled", "disabled");

        //select the first not selected
        selectElem.val(selectElem.find("option[disabled!=disabled]").first().attr("value"));

        var selectFeature = new OpenLayers.Control.SelectFeature(self.getVisibleVectorsLayer());

        layerAnnotation.initControls(self.browseImageView, selectFeature);
        layerAnnotation.registerEvents(self.browseImageView.map);

        if(layer.isOwner) {
            layerAnnotation.vectorsLayer.setVisibility(true);
            layerAnnotation.toggleIrregular();
            //Simulate click on None toolbar
            var toolbar = $("#" + self.browseImageView.divId).find('#toolbar' + self.model.get('id'));
            toolbar.find('input[id=none' + self.model.get('id') + ']').click();
        } else {
            layerAnnotation.controls.select.activate();
        }
        _.each(this.printedLayer, function(item) {
            item.layer.controls.select.activate();
        });
    },
    getVisibleVectorsLayer : function() {
        var vectorLayers = _.map(this.printedLayer, function (layer) {
            return layer.vectorsLayer;
        });
        return vectorLayers;
    },
    /**
     * Remove a specific layer on the map
     * @param layer User/UserJob id
     */
    removeLayerFromReview:function (layer) {
        var self = this;
        var panelElem = $("#" + this.browseImageView.divId).find("#reviewPanel" + self.model.get("id"));
        //hide this layer
        _.each(self.printedLayer, function (elem) {
            if (elem.id == layer) elem.vectorsLayer.setVisibility(false);
        });
        //remove from layer list
        self.printedLayer = _.filter(self.printedLayer, function (elem) {
            return elem.id != layer
        });
        $("#reviewLayerElem" + layer).replaceWith("");

        //enable from select list
        var selectElem = panelElem.find("#reviewChoice" + self.model.get("id")).find("select");
        selectElem.find("option#" + layer).removeAttr("disabled");
    },
    addVectorLayer:function (layer, model, userID) {
        var self = this;
        layer.vectorsLayer.setVisibility(true);
    },
    addToListLayer:function (layer) {
        var self = this;
        //disable from select box
        var panelElem = $("#" + this.browseImageView.divId).find("#reviewPanel" + self.model.get("id"));
        //add to list
        panelElem.find("#reviewSelection" + self.model.id).append('<label style="display:inline;" id="reviewLayerElem' + layer + '">' + self.layerName[layer] + '<i class="icon-remove icon-white" id="removeReviewLayer' + layer + '"></i>&nbsp;&nbsp;</label>');
        $("#removeReviewLayer" + layer).click(function (elem) {
            self.removeLayerFromReview(layer);
        });
    },
    doLayout:function (tpl) {
        var self = this;
        var panelElem = $("#" + this.browseImageView.divId).find("#reviewPanel" + self.model.get("id"));
        var params = {id:self.model.get("id"), isDesktop:!window.app.view.isMobile};
        var content = _.template(tpl, params);
        panelElem.html(content);
        self.showCurrentAnnotation(null);

        if (!self.model.get("reviewed")) {
            //image is not reviewed

            var selectElem = panelElem.find("#reviewChoice" + self.model.get("id")).find("select");

            //fill select with all possible layers
            this.userLayers.each(function (layer) {
                self.layerName[layer.id] = layer.layerName();
                selectElem.append('<option value="' + layer.id + '" id="' + layer.id + '">' + layer.layerName() + '</option>');
            });

            this.userJobLayers.each(function (layer) {
                if(!layer.get("isDeleted")) {
                    self.layerName[layer.id] = layer.layerName();
                    selectElem.append('<option value="' + layer.id + '" id="' + layer.id + '">' + layer.layerName() + '</option>');
                }
            });

            //init event
            $("#addReviewLayers" + self.model.id).click(function () {
                self.addLayerToReview(panelElem.find("#reviewChoice" + self.model.get("id")).find("select").val());
            });
            $("#reviewMultiple" + self.model.id).click(function () {
                self.addAllReviewAnnotation();
            });
            $("#unReviewMultiple" + self.model.id).click(function () {
                self.deleteAllReviewAnnotation();
            });
            $("#reviewValidate" + self.model.id).click(function () {
                self.validatePicture();
            });

            panelElem.find(".toggleShowAnnotation").click(function () {
                panelElem.find("#currentReviewAnnotation" + self.model.get("id")).toggle(150);
                return false;
            });

            panelElem.find(".toggleShowLayers").click(function () {
                panelElem.find("#reviewChoice" + self.model.get("id")).toggle(150);
                return false;
            });

            panelElem.find(".toggleShowAction").click(function () {
                panelElem.find("#reviewAction" + self.model.get("id")).toggle(150);
                return false;
            });

            $('#showReviewLayer'+self.model.id).click (function () {
                self.reviewLayer.vectorsLayer.setVisibility($(this).attr("checked"));
            });

        } else {
            //image is validate
            var panel = $("#" + self.browseImageView.divId).find("#reviewPanel" + self.model.get("id"));
            panel.find('#showReviewLayer'+self.model.id).attr("disabled", "disabled")
            panel.find("#addReviewLayers" + self.model.id).attr("disabled", "disabled");
            panel.find("select").attr("disabled", "disabled");
            panel.find("#reviewMultiple" + self.model.id).attr("disabled", "disabled");
            panel.find("#reviewValidate" + self.model.id).hide();
            panel.find("#reviewUnValidate" + self.model.id).show();

            $("#reviewUnValidate" + self.model.id).click(function () {
                self.unvalidatePicture();
            });

            //hide and lock action
            panelElem.find("#currentReviewAnnotation" + self.model.get("id")).hide();
            panelElem.find("#reviewChoice" + self.model.get("id")).hide();
            panelElem.find(".toggleShowAnnotation").click(function () {
                return false;
            });
            panelElem.find(".toggleShowLayers").click(function () {
                return false;
            });
            panelElem.find(".toggleShowAction").click(function () {
                return false;
            });
        }

        new DraggablePanelView({
            el:$("#" + self.browseImageView.divId).find('#reviewPanel' + self.model.get('id')),
            className:"reviewPanel"
        }).render();
    },
    /**
     * Accept curent annotation for review
     */
    addReviewAnnotation:function () {
        var self = this;
        var annotation = self.browseImageView.currentAnnotation;

        //get all term selected by the current user
        var terms = self.getSelectedTerm(annotation);

        //remove the based annotation from the layer
        self.browseImageView.removeFeature(annotation.id);
        new AnnotationReviewedModel({id:annotation.id}).save({terms:terms}, {
            success:function (model, response) {
                //add the reviewed annotation on the layer + print message
                var newFeature = AnnotationLayerUtils.createFeatureFromAnnotation(response.reviewedannotation);
                var cropURL = annotation.get('cropURL');
                var cropImage = _.template("<img src='<%=   url %>' alt='<%=   alt %>' style='max-width: 175px;max-height: 175px;' />", { url:cropURL, alt:cropURL});
                var alertMessage = _.template("<p><%=   message %></p><div><%=   cropImage %></div>", { message:response.message, cropImage:cropImage});
                window.app.view.message("Reviewed annotation", alertMessage, "success");
                self.reviewLayer.addFeature(newFeature);
                self.reviewLayer.controls.select.unselectAll();
                self.reviewLayer.controls.select.select(newFeature);
                _.each(self.printedLayer,function(layer) {
                    layer.vectorsLayer.refresh();
                });
            },
            error:function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Annotation", json.errors, "error");
            }});
    },
    deleteReviewAnnotation:function () {
        var self = this;
        var annotation = self.browseImageView.currentAnnotation;
        self.browseImageView.removeFeature(annotation.id);
        new AnnotationReviewedModel({id:annotation.id}).destroy({
            success:function (model, response) {
                //remove the reviewed annotation from the layer
                var layerItem = _.find(self.printedLayer, function (item) {
                    return item.id == response.basedannotation.user;
                });

                if (layerItem) {
                    //if layer is undefined, don't need to add old annotation
                    var newFeature = AnnotationLayerUtils.createFeatureFromAnnotation(response.basedannotation);
                    layerItem.layer.addFeature(newFeature);
                    layerItem.layer.controls.select.unselectAll();
                    layerItem.layer.controls.select.select(newFeature);
                }
                window.app.view.message("Annotation", response.message, "success");
                _.each(self.printedLayer,function(layer) {
                    layer.vectorsLayer.refresh();
                });
            },
            error:function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Annotation", json.errors, "error");
            }});
    },
    showCurrentAnnotation:function (annotation) {
        var self = this;
        console.log("showCurrentAnnotation=" + annotation);
        $("#currentReviewAnnotation" + self.model.id).empty();
        require([
            "text!application/templates/explorer/ReviewPanelSelectedAnnotation.tpl.html"
        ], function (tpl) {
            var params = {};
            var idAnnotation;

            //fill current annotation info
            if (annotation == null) {
                params = {id:self.model.get("id"), username:"", date:"", isReviewed:false, idAnnotation:""};
                idAnnotation = "";
            }
            else {
                var user = window.app.models.projectUser.get(annotation.get("user"));
                if (user == undefined) user = window.app.models.projectUserJob.get(annotation.get("user"));
                params = {id:self.model.get("id"), username:user.prettyName(), date:window.app.convertLongToDate(annotation.get("created")), isReviewed:annotation.get("reviewed"), idAnnotation:annotation.id }
                idAnnotation = annotation.id;
            }
            var content = _.template(tpl, params);
            $("#currentReviewAnnotation" + self.model.id).append(content);

            if (params.idAnnotation == "") {
                //no annotation selected
                $("#currentReviewAnnotation" + self.model.id).find("#reviewSingle" + idAnnotation).attr("disabled", "disabled")
                $("#currentReviewAnnotation" + self.model.id).find("#unreviewSingle" + idAnnotation).attr("disabled", "disabled")
            } else if (params.isReviewed) {
                //annotation is reviewed, cannot re-review
                $("#currentReviewAnnotation" + self.model.id).find("#reviewSingle" + idAnnotation).attr("disabled", "disabled")
            } else {
                //annotation is not reviewed, cannot unreview
                $("#currentReviewAnnotation" + self.model.id).find("#unreviewSingle" + idAnnotation).attr("disabled", "disabled")
            }

            $("#currentReviewAnnotation" + self.model.id).find("#reviewSingle" + idAnnotation).click(function () {
                self.addReviewAnnotation();
            });
            $("#currentReviewAnnotation" + self.model.id).find("#unreviewSingle" + idAnnotation).click(function () {
                self.deleteReviewAnnotation();
            });
            $("#currentReviewAnnotation" + self.model.id).find("#reviewValidate" + idAnnotation).click(function () {
                self.validatePicture();
            });

            self.showAnnotationTerm(annotation)
        });
    },
    /**
     * Show all term map with this annotation and add a checkbox to each one
     * @param annotation
     */
    showAnnotationTerm:function (annotation) {
        var self = this;

        if (annotation != null) {
            var termsListElem = $("#currentReviewAnnotation" + self.model.id).find("#termsChoice" + annotation.id);
            termsListElem.empty();
            _.each(annotation.get('term'), function (term) {
                self.addTermChoice(term, annotation.id, annotation.get("reviewed"));
            });
            $("#termsChoice" + annotation.id +" .termchoice").sort(self.asc_sort).appendTo("#termsChoice" + annotation.id);
        }
    },
    addTermChoice:function (idTerm, idAnnotation) {
        this.addTermChoice(idTerm, idAnnotation, false);
    },
    addTermChoice:function (idTerm, idAnnotation, lock) {
        var self = this;
        var termsListElem = $("#currentReviewAnnotation" + self.model.id).find("#termsChoice" + idAnnotation);
        var lockCheckBox = ""
        if (lock) lockCheckBox = 'disabled="disabled"';
        termsListElem.append('<div class="termchoice"><input type="checkbox" ' + lockCheckBox + ' checked="checked" name="terms" value="' + idTerm + '" id="termInput' + idTerm + '"> ' + window.app.status.currentTermsCollection.get(idTerm).get('name') + "&nbsp;&nbsp;</div>");
    },
    asc_sort:function(a, b){
      return ($(b).text()) < ($(a).text());
    },
    deleteTermChoice:function (idTerm, idAnnotation) {
        var self = this;
        var termsListElem = $("#currentReviewAnnotation" + self.model.id).find("#termsChoice" + idAnnotation)
        termsListElem.find("input#termInput" + idTerm).replaceWith("");
    },
    getSelectedTerm:function (annotation) {
        var self = this;
        var termsListElem = $("#currentReviewAnnotation" + self.model.id).find("#termsChoice" + annotation.id);
        var selectedInput = termsListElem.find("input[name='terms']:checked:enabled");
        var selectedTermsId = [];
        _.each(selectedInput, function (input) {
            selectedTermsId.push($(input).val())
        });
        return selectedTermsId;
    },
    /**
     * Review all visible layers
     */
    addAllReviewAnnotation:function () {
        var self = this;

        var layers = _.map(_.filter(self.printedLayer, function (num) {
            return num.id != "REVIEW";
        }), function (item) {
            return item.id
        });

        if (layers.length == 0) {
            window.app.view.message("Annotation", "You must add at least one layer!", "error");
        } else {
            new TaskModel({project:self.model.get('project')}).save({}, {
                        success:function (task, response) {
                            console.log("task=" + task);


                            $("#taskreview" + self.model.id).append('<div id="task-' + task.id + '"></div>');
                            $("#reviewChoice" + self.model.id).hide();
                            $("#taskreview" + self.model.id).show();


                            var timer = window.app.view.printTaskEvolution(task, $("#taskreview" + self.model.id).find("#task-" + task.id), 2000, true);

                            new AnnotationImageReviewedModel({image:self.model.id, layers:layers, task:task.id}).save({}, {
                                success:function (model, response) {
                                    clearInterval(timer);
                                    $("#taskreview" + self.model.id).empty();
                                    $("#taskreview" + self.model.id).hide();
                                    $("#reviewChoice" + self.model.id).show();
                                    window.app.view.message("Annotation", "Annotation are reviewed!", "success");
                                    _.each(self.printedLayer,function(layer) {
                                        layer.vectorsLayer.refresh();
                                    });
                                },
                                error:function (model, response) {
                                    console.log("AnnotationImageReviewedModel error");
                                    var json = $.parseJSON(response.responseText);
                                    window.app.view.message("Annotation", json.errors, "error");
                                    $("#taskreview" + self.model.id).empty();
                                    $("#taskreview" + self.model.id).hide();
                                    $("#reviewChoice" + self.model.id).show();
                                    clearInterval(timer);

                                }});

                        },
                        error:function (model, response) {
                            var json = $.parseJSON(response.responseText);
                            window.app.view.message("Task", json.errors, "error");
                        }
                    }
            );
        }
    },
    deleteAllReviewAnnotation:function () {
        var self = this;

        var layers = _.map(_.filter(self.printedLayer, function (num) {
            return num.id != "REVIEW";
        }), function (item) {
            return item.id
        });

        if (layers.length == 0) {
            window.app.view.message("Annotation", "You must add at least one layer!", "error");
        } else {

            var x=window.confirm("Are you sure you to reject all annotation from these layers?")
            if (x) {
                new TaskModel({project:self.model.get('project')}).save({}, {
                                        success:function (task, response) {
                                            console.log("task=" + task);

                                            $("#taskreview" + self.model.id).append('<div id="task-' + task.id + '"></div>');
                                            $("#reviewChoice" + self.model.id).hide();
                                            $("#taskreview" + self.model.id).show();

                                            var timer = window.app.view.printTaskEvolution(task, $("#taskreview" + self.model.id).find("#task-" + task.id), 2000, true);

                                            new AnnotationImageReviewedModel({image:self.model.id, layers:layers, task:task.id}).destroy({
                                                success:function (model, response) {
                                                    clearInterval(timer);
                                                    $("#taskreview" + self.model.id).empty();
                                                    $("#taskreview" + self.model.id).hide();
                                                    $("#reviewChoice" + self.model.id).show();
                                                    window.app.view.message("Annotation", "All visible annotations are rejected!", "success");
                                                    _.each(self.printedLayer,function(layer) {
                                                        layer.vectorsLayer.refresh();
                                                    });
                                                },
                                                error:function (model, response) {
                                                    console.log("AnnotationImageReviewedModel error");
                                                    var json = $.parseJSON(response.responseText);
                                                    window.app.view.message("Annotation", json.errors, "error");
                                                    $("#taskreview" + self.model.id).empty();
                                                    $("#taskreview" + self.model.id).hide();
                                                    $("#reviewChoice" + self.model.id).show();
                                                    clearInterval(timer);

                                                }});

                                        },
                                        error:function (model, response) {
                                            var json = $.parseJSON(response.responseText);
                                            window.app.view.message("Task", json.errors, "error");
                                        }
                                    }
                            );
            }

        }
    },
    validatePicture:function () {
        var self = this;
        self.browseImageView.validatePicture();
    },
    unvalidatePicture:function () {
        var self = this;
        self.browseImageView.unvalidatePicture();
    },
    refresh:function (model) {
        var self = this;
        self.model = model;
        self.render();
    }
});
