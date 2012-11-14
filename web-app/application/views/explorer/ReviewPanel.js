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
    addLayerToReview : function(layer) {
        var self = this;
        var panelElem = $("#"+this.browseImageView.divId).find("#reviewPanel" + self.model.get("id"));
        console.log("addLayerToReview:"+layer);

        self.addToListLayer(layer);

        var user = self.userLayers.get(layer);
        if(user==undefined) user = self.userJobLayers(layer);
        if(user==undefined) console.log("ERROR! LAYER NOT DEFINED!!!");

        var layerAnnotation = new AnnotationLayer(user.prettyName(), self.model.get('id'), user.get('id'), user.get('color'), self.browseImageView.ontologyPanel.ontologyTreeView, self.browseImageView, self.browseImageView.map);
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

        self.browseImageView.userLayer = layer;
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
        panelElem.find("#reviewSelection"+self.model.id).append('<label style="display:inline;" id="reviewLayerElem'+layer+'">'+self.layerName[layer]+'<i class="icon-remove icon-white" id="removeReviewLayer'+layer+'"></i></label>');
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


//        $("#selectLayersIcon" + self.model.get("id")).off("click");
//        $("#selectLayersIcon" + self.model.get("id")).on("click", function (event) {
//            var project = window.app.status.currentProjectModel;
//            var userList = $("#"+self.browseImageView.divId).find("#layerSwitcher" + self.model.get("id")).find("ul.annotationLayers");
//            var projectUsers = _.pluck(window.app.models.projectUser, 'id');
//            var almostOneCheckedState = false;
//            _.each(projectUsers, function (userID) {
//                var checked = userList.find("li[data-id=" + userID + "]").find('input.showUser').attr("checked") == "checked";
//                if (!almostOneCheckedState && checked) almostOneCheckedState = true;
//            });
//            self.browseImageView.setAllLayersVisibility(!almostOneCheckedState);
//        });

        new DraggablePanelView({
            el:$("#"+self.browseImageView.divId).find('#reviewPanel' + self.model.get('id')),
            className:"reviewPanel"
        }).render();

//        new DraggablePanelView({
//            el:$('#'+this.browseImageView.divId).find('#ontologyTree' + this.model.get('id')),
//            className:"ontologyPanel",
//            template:_.template(tpl, {id:this.model.get('id')})
//        }).render();

    }

//    updateOnlineUsers:function (onlineUsers) {
//        var self = this;
//        var userList = $("#"+this.browseImageView.divId).find("#layerSwitcher" + this.model.get("id")).find("ul.annotationLayers");
//        var projectUsers = _.pluck(window.app.models.projectUser, 'id');
//        //check if the the user we are following is always connected, if not disconneted
//        if (!_.include(onlineUsers, self.userFollowed)) {
//            userList.find("li[data-id=" + self.userFollowed + "]").find('input.followUser').removeAttr('checked');
//            self.stopFollowing();
//        }
//        _.each(projectUsers, function (userID) {
//            if (!_.include(onlineUsers, userID)) {
//                userList.find("li[data-id=" + userID + "]").find('span').css('color', 'white');
//                userList.find("li[data-id=" + userID + "]").find('input.followUser').attr("disabled", "disabled")
//            }
//        });
//        _.each(onlineUsers, function (userID) {
//            userList.find("li[data-id=" + userID + "]").find('span').css('color', '#46a546');
//            userList.find("li[data-id=" + userID + "]").find('input.followUser').removeAttr("disabled");
//        });
//    },
//    startFollowing:function () {
//        var self = this;
//        window.app.view.message("", "Start following " + window.app.models.projectUser.get(self.userFollowed).prettyName(), "success");
//        var image = this.model.get("id");
//        this.followInterval = setInterval(function () {
//            new UserPositionModel({ image:image, user:self.userFollowed }).fetch({
//                success:function (model, response) {
//                    self.browseImageView.map.moveTo(new OpenLayers.LonLat(model.get("longitude"), model.get("latitude")), model.get("zoom"));
//                    var layerWrapper = _.detect(self.vectorLayers, function (item) {
//                        return item.id == self.userFollowed;
//                    });
//                    if (layerWrapper) layerWrapper.vectorsLayer.refresh();
//                },
//                error:function (model, response) {
//                }
//            });
//        }, 1000);
//    },
//    stopFollowing:function () {
//        var self = this;
//        if (self.followInterval != undefined) {
//            window.app.view.message("", "Stop following " + window.app.models.projectUser.get(self.userFollowed).prettyName(), "success");
//            clearInterval(self.followInterval);
//            self.followInterval = null;
//            self.userFollowed = null;
//        }
//    },

});
