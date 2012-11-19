/**
 * Created by IntelliJ IDEA.
 * User: stevben
 * Date: 12/06/11
 * Time: 12:32
 * To change this template use File | Settings | File Templates.
 */

var ReviewPanel = Backbone.View.extend({
    tagName:"div",
    userLayers : null,
    userJobLayers : null,
    reviewLayer : null,
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
    addReviewLayerToReview : function() {
        var self = this;
        var panelElem = $("#"+this.browseImageView.divId).find("#reviewPanel" + self.model.get("id"));
        var layer= "REVIEW";
        console.log("addReviewLayerToReview:"+layer);

        console.log("*** " + self.browseImageView.ontologyPanel);
        console.log("*** " + self.browseImageView.ontologyPanel.ontologyTreeView);

        var layerAnnotation = new AnnotationLayer(layer, self.model.get('id'), 0,"#ff0000", self.browseImageView.ontologyPanel.ontologyTreeView, self.browseImageView, self.browseImageView.map,true);

        layerAnnotation.loadAnnotations(self.browseImageView);
        self.printedLayer.push({id:layer,vectorsLayer:layerAnnotation.vectorsLayer,layer:layerAnnotation});

        var selectFeature = new OpenLayers.Control.SelectFeature([layerAnnotation.vectorsLayer]);
        layerAnnotation.isOwner = true;
        layerAnnotation.initControls(self.browseImageView, selectFeature);
        layerAnnotation.registerEvents(self.browseImageView.map);

        self.browseImageView.userLayer = layerAnnotation;
        layerAnnotation.vectorsLayer.setVisibility(true);
        layerAnnotation.toggleIrregular();
        //Simulate click on None toolbar
        var toolbar = $("#"+self.browseImageView.divId).find('#toolbar' + self.model.get('id'));
        toolbar.find('input[id=none' + self.model.get('id') + ']').click();

        layerAnnotation.controls.select.activate();
        self.reviewLayer = layerAnnotation;
        self.initControl();
    },
    initControl : function() {
            var self = this;
             //Init Controls on Layers
        console.log("printedLayer:");
        console.log(this.printedLayer);
            var vectorLayers = _.map(this.printedLayer, function (layer) {
                return layer.vectorsLayer;
            });
        console.log("vectorLayers:");
        console.log(vectorLayers);
            var selectFeature = new OpenLayers.Control.SelectFeature(vectorLayers);
            _.each(this.printedLayer, function (item) {
                console.log("item:"+item.id);
                var layer = item.layer;
                layer.initControls(self.browseImageView, selectFeature);
                layer.registerEvents(self.browseImageView.map);
//                if (layer.isOwner) {
//                    self.userLayer = layer;
//                    layer.vectorsLayer.setVisibility(true);
//                    layer.toggleIrregular();
//                    //Simulate click on None toolbar
//                    var toolbar = $("#"+self.divId).find('#toolbar' + self.model.get('id'));
//                    toolbar.find('input[id=none' + self.model.get('id') + ']').click();
//                } else {
                    layer.controls.select.activate();
//                    layer.vectorsLayer.setVisibility(false);
//                }
            });

            if (_.isFunction(self.browseImageView.initCallback)) self.browseImageView.initCallback.call();
            self.browseImageView.initAutoAnnoteTools();
    },


    addLayerToReview : function(layer) {
        var self = this;
        var panelElem = $("#"+this.browseImageView.divId).find("#reviewPanel" + self.model.get("id"));
        console.log("addLayerToReview:"+layer);

        self.addToListLayer(layer);
        console.log("*** " + self.browseImageView.ontologyPanel);

        var user = self.userLayers.get(layer);
        if(user==undefined) user = self.userJobLayers.get(layer);
        if(user==undefined) console.log("ERROR! LAYER NOT DEFINED!!!");

        var layerAnnotation = new AnnotationLayer(user.prettyName(), self.model.get('id'), user.get('id'), user.get('color'), self.browseImageView.ontologyPanel.ontologyTreeView, self.browseImageView, self.browseImageView.map,true);
        layerAnnotation.isOwner = (user.get('id') == window.app.status.user.id);
        layerAnnotation.loadAnnotations(self.browseImageView);

        self.printedLayer.push({id:layer,vectorsLayer:layerAnnotation.vectorsLayer,layer:layerAnnotation});

        //disable from selecy list
        var selectElem = panelElem.find("#reviewChoice"+self.model.get("id")).find("select");
        selectElem.find("option#"+layer).attr("disabled","disabled");

        //select the first not selected
        selectElem.val(selectElem.find("option[disabled!=disabled]").first().attr("value"));




        var selectFeature = new OpenLayers.Control.SelectFeature([layerAnnotation.vectorsLayer]); //may be other param?

        layerAnnotation.initControls(self.browseImageView, selectFeature);
        layerAnnotation.registerEvents(self.browseImageView.map);

        //self.browseImageView.userLayer = layerAnnotation;
        layerAnnotation.vectorsLayer.setVisibility(true);
        layerAnnotation.toggleIrregular();
        //Simulate click on None toolbar
        var toolbar = $("#"+self.browseImageView.divId).find('#toolbar' + self.model.get('id'));
        toolbar.find('input[id=none' + self.model.get('id') + ']').click();

        layerAnnotation.controls.select.activate();

//        self.reviewLayer.redraw();
//        console.log(self.reviewLayer);
//        self.reviewLayer.vectorsLayer.refresh();
//        console.log("getZIndex refresh:"+self.reviewLayer.getZIndex());
//       // console.log("getZIndex layer:"+layerAnnotation.getZIndex());
        self.initControl();

    },
    removeLayerFromReview : function(layer) {
        var self = this;
        console.log("removeLayerFromReview"+layer);
        var panelElem = $("#"+this.browseImageView.divId).find("#reviewPanel" + self.model.get("id"));
        //hide this layer
        _.each(self.printedLayer, function(elem) {
            if(elem.id==layer) elem.vectorsLayer.setVisibility(false);
        });

        //remove from layer list
        self.printedLayer = _.filter(self.printedLayer, function(elem){ return elem.id!=layer });
        $("#reviewLayerElem"+layer).replaceWith("");


        //enable from select list
        var selectElem = panelElem.find("#reviewChoice"+self.model.get("id")).find("select");
        selectElem.find("option#"+layer).removeAttr("disabled");

    },
    addVectorLayer:function (layer, model, userID) {
        var self = this;
        console.log("addVectorLayer " + model.get("id"));
        layer.vectorsLayer.setVisibility(true);
     },
    addToListLayer : function(layer) {
        var self= this;
        //disable from select box
        var panelElem = $("#"+this.browseImageView.divId).find("#reviewPanel" + self.model.get("id"));
         console.log(self.layerName);
        //add to list
        panelElem.find("#reviewSelection"+self.model.id).append('<label style="display:inline;" id="reviewLayerElem'+layer+'">'+self.layerName[layer]+'<i class="icon-remove icon-white" id="removeReviewLayer'+layer+'"></i>&nbsp;&nbsp;</label>');
        $("#removeReviewLayer"+layer).click(function(elem) {
            console.log("click on remove layer");
            self.removeLayerFromReview(layer);
        });
    },
    doLayout:function (tpl) {
        var self = this;
        var panelElem = $("#"+this.browseImageView.divId).find("#reviewPanel" + self.model.get("id"));
        var params = {id:self.model.get("id"), isDesktop:!window.app.view.isMobile};
        var content = _.template(tpl,params);
        panelElem.html(content);
        self.showCurrentAnnotation(null);


        var selectElem = panelElem.find("#reviewChoice"+self.model.get("id")).find("select");

        this.userLayers.each(function(layer) {
            console.log(layer.layerName());
            self.layerName[layer.id] = layer.layerName();
            selectElem.append('<option value="'+layer.id+'" id="'+layer.id+'">'+layer.layerName()+'</option>');
        });

        this.userJobLayers.each(function(layer) {
            self.layerName[layer.id] = layer.layerName();
            selectElem.append('<option value="'+layer.id+'" id="'+layer.id+'">'+layer.layerName()+'</option>');
        });

        $("#addReviewLayers"+self.model.id).click(function() {
            self.addLayerToReview(panelElem.find("#reviewChoice"+self.model.get("id")).find("select").val());
        });

        new DraggablePanelView({
            el:$("#"+self.browseImageView.divId).find('#reviewPanel' + self.model.get('id')),
            className:"reviewPanel"
        }).render();

        $("#reviewMultiple"+self.model.id).click(function() {
            self.addAllReviewAnnotation();
        });

    },
    addReviewAnnotation : function() {
        var self = this;
        console.log("addReviewAnnotation");
        var annotation = self.browseImageView.currentAnnotation;
            self.browseImageView.removeFeature(annotation.id);
            new AnnotationReviewedModel({id:annotation.id}).save({}, {
                success:function (model, response) {
                    window.app.view.message("Annotation", response.message, "success");
                    var newFeature = AnnotationLayerUtils.createFeatureFromAnnotation(response.reviewedannotation);
                    self.reviewLayer.addFeature(newFeature);
                    self.reviewLayer.controls.select.unselectAll();
                    self.reviewLayer.controls.select.select(newFeature);
                    var cropURL = annotation.get('cropURL');
                    var cropImage = _.template("<img src='<%=   url %>' alt='<%=   alt %>' style='max-width: 175px;max-height: 175px;' />", { url:cropURL, alt:cropURL});
                    var alertMessage = _.template("<p><%=   message %></p><div><%=   cropImage %></div>", { message:response.message, cropImage:cropImage});
                    window.app.view.message("Reviewed annotation", alertMessage, "success");
                    //self.reviewLayer.moveTo(self.browseImageView.map.getExtent(), true);

                },
                error:function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Annotation", json.errors, "error");
            }});
    },
    deleteReviewAnnotation : function() {
        var self = this;
        console.log("deleteReviewAnnotation");
        var annotation = self.browseImageView.currentAnnotation;
        self.browseImageView.removeFeature(annotation.id);
            new AnnotationReviewedModel({id:annotation.id}).destroy({
                success:function (model, response) {
                    window.app.view.message("Annotation", response.message, "success");
                },
                error:function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Annotation", json.errors, "error");
            }});
    },
    showCurrentAnnotation : function(annotation) {
        var self = this;
        console.log("showCurrentAnnotation="+annotation);
        $("#currentReviewAnnotation"+self.model.id).empty();
        require([
            "text!application/templates/explorer/ReviewPanelSelectedAnnotation.tpl.html"
        ], function (tpl) {
            var params = {};
            var idAnnotation;
            if(annotation==null) {
                params = {id:self.model.get("id"), username:"",date:"",yourTerms:"",otherTerms:"",isReviewed:false,idAnnotation:"-1"};
                idAnnotation = "";
            }
            else {
                params = {
                    id:self.model.get("id"),
                    username:window.app.models.projectUser.get(annotation.get("user")).prettyName(),
                    date:window.app.convertLongToDate(annotation.get("created")),
                    yourTerms:"",
                    otherTerms:"",
                    isReviewed:annotation.get("reviewed"),
                    idAnnotation : annotation.id }
                idAnnotation= annotation.id;
            }

            var content = _.template(tpl,params);
            $("#currentReviewAnnotation"+self.model.id).append(content);

            if(params.idAnnotation="") {
                $("#currentReviewAnnotation"+self.model.id).find("#reviewSingle"+idAnnotation).attr("disabled","disabled")
                $("#currentReviewAnnotation"+self.model.id).find("#unreviewSingle"+idAnnotation).attr("disabled","disabled")
            } else if(params.isReviewed) {
                $("#currentReviewAnnotation"+self.model.id).find("#reviewSingle"+idAnnotation).attr("disabled","disabled")
            } else {
                $("#currentReviewAnnotation"+self.model.id).find("#unreviewSingle"+idAnnotation).attr("disabled","disabled")
            }

            $("#currentReviewAnnotation"+self.model.id).find("#reviewSingle"+idAnnotation).click(function() {
                self.addReviewAnnotation();
            });
            $("#currentReviewAnnotation"+self.model.id).find("#unreviewSingle"+idAnnotation).click(function() {
                self.deleteReviewAnnotation();
            });
        });
    },
    addAllReviewAnnotation : function() {
        var self = this;
        console.log("addReviewAllAnnotation");

        var layers = _.map(_.filter(self.printedLayer, function(num){ return num.id!="REVIEW"; }),function(item) {
            return item.id
        });
        console.log(layers);
            new AnnotationImageReviewedModel({image: self.model.id,layers:layers}).save({}, {
                success:function (model, response) {
                    window.app.view.message("Annotation", "Annotation are reviewed!", "success");
                },
                error:function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Annotation", json.errors, "error");
            }});
    }
});
