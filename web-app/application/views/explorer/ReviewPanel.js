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
        self.printedLayer.push({id:layer,vectorsLayer:layerAnnotation.vectorsLayer});

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

        self.printedLayer.push({id:layer,vectorsLayer:layerAnnotation.vectorsLayer});

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
        var content = _.template(tpl, {id:self.model.get("id"), isDesktop:!window.app.view.isMobile});
        panelElem.html(content);
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

        $("#reviewSingle"+self.model.get('id')).click(function() {
            self.addReviewAnnotation();
        });
    },
    addReviewAnnotation : function() {
        var self = this;
        var annotation = self.browseImageView.currentAnnotation;

            new AnnotationReviewedModel({id:annotation.id}).save({}, {
                success:function (model, response) {
                    window.app.view.message("Annotation", response.message, "success");
                },
                error:function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Annotation", json.errors, "error");
            }});
    }

});
