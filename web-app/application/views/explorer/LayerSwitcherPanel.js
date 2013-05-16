/**
 * Created by IntelliJ IDEA.
 * User: stevben
 * Date: 12/06/11
 * Time: 12:32
 * To change this template use File | Settings | File Templates.
 */

var LayerSwitcherPanel = SideBarPanel.extend({
    tagName: "div",

    /**
     * ExplorerTabs constructor
     * @param options
     */
    initialize: function (options) {
        this.browseImageView = options.browseImageView;
        this.followInterval = null;
        this.userFollowed = null;
        this.vectorLayers = [];
    },
    /**
     * Grab the layout and call ask for render
     */
    render: function () {
        var self = this;
        require([
            "text!application/templates/explorer/LayerSwitcher.tpl.html"
        ], function (tpl) {
            self.doLayout(tpl);
        });
        return this;
    },
    addBaseLayer: function (layer, model) {
        var self = this;
        var radioName = "layerSwitch-" + model.get("id");
        var layerID = "layerSwitch-" + model.get("id") + "-" + new Date().getTime(); //index of the layer in this.layers array
        var liLayer = _.template("<li><input type='radio' id='<%=   id %>' name='<%=   radioName %>' checked/><span style='color : #ffffff;'> <%=   name %></span></li>", {id: layerID, radioName: radioName, name: layer.name.substr(0, 15)});
        $("#" + this.browseImageView.divId).find("#layerSwitcher" + this.model.get("id")).find(".baseLayers").append(liLayer);
        $("#" + this.browseImageView.divId).find("#layerSwitcher" + this.model.get("id")).find(".baseLayers").find("#" + layerID);
        $("#" + layerID).change(function () {
            self.browseImageView.map.setBaseLayer(layer);
        });
    },
    addVectorLayer: function (layer, model, userID) {
        var self = this;
        this.vectorLayers.push({ id: userID, vectorsLayer: layer.vectorsLayer});
        var layerID = "layerSwitch-" + model.get("id") + "-" + userID + "-" + new Date().getTime(); //index of the layer in this.layers array
        var color = "#FFF";
        if (userID == "REVIEW") {
            color = "#5BB75B";
        }
        var layerOptionTpl;
        if (layer.isOwner) {
            layerOptionTpl = _.template("<li id='entry<%= userID %>'><input id='<%= id %>' class='showUser' type='checkbox'  value='<%= name %>' checked />&nbsp;&nbsp;<input type='checkbox' disabled/><span style='color :<%=   color %>;'> <%=   name %> <span class='numberOfAnnotation'></span></span></li>", {id: layerID, name: layer.vectorsLayer.name, color: color,userID:userID});
        } else {
            /*layerOptionTpl = _.template("<li><input id='<%= id %>' type='checkbox' value='<%=   name %>' /> <span style='color : #ffffff;'><%=   name %></span> <a class='followUser' data-user-id='<%= userID %>' href='#'>Follow</a></li>", {userID : userID, id : layerID, name : layer.vectorsLayer.name, color : color});*/
            layerOptionTpl = _.template("<li id='entry<%= userID %>' data-id='<%= userID %>'><input id='<%= id %>' class='showUser' type='checkbox' value='<%= name %>' />&nbsp;&nbsp;<input type='checkbox' class='followUser' data-user-id='<%= userID %>' disabled/>&nbsp;<span style='color : <%=   color %>;'><%= name %> <span class='numberOfAnnotation'></span></span></a> </li>", {userID: userID, id: layerID, name: layer.vectorsLayer.name, color: color});
        }
        console.log("*** addVectorLayer " + model.get("id"));
        $("#" + this.browseImageView.divId).find("#layerSwitcher" + model.get("id")).find("ul.annotationLayers").append(layerOptionTpl);

        $("#" + layerID).click(function () {
            console.log("click");
            var checked = $(this).is(':checked');
            console.log("visible:" + checked);
            layer.vectorsLayer.setVisibility(checked);
            self.browseImageView.annotationProperties.updateAnnotationProperyLayers(); //update annotation proprety layers
        });

    },
    updateOnlineUsers: function (onlineUsers) {
        var self = this;
        var userList = $("#" + this.browseImageView.divId).find("#layerSwitcher" + this.model.get("id")).find("ul.annotationLayers");
        var projectUsers = _.pluck(window.app.models.projectUser.models, 'id');
        //check if the the user we are following is always connected, if not disconneted
        if (!_.include(onlineUsers, self.userFollowed)) {
            userList.find("li[data-id=" + self.userFollowed + "]").find('input.followUser').removeAttr('checked');
            self.stopFollowing();
        }
        _.each(projectUsers, function (userID) {
            if (!_.include(onlineUsers, userID)) {
                userList.find("li[data-id=" + userID + "]").find('span').css('color', 'white');
                userList.find("li[data-id=" + userID + "]").find('input.followUser').attr("disabled", "disabled")
            }
        });
        _.each(onlineUsers, function (userID) {
            userList.find("li[data-id=" + userID + "]").find('span').css('color', '#46a546');
            userList.find("li[data-id=" + userID + "]").find('input.followUser').removeAttr("disabled");
        });
    },
    startFollowing: function () {
        var self = this;
        window.app.view.message("", "Start following " + window.app.models.projectUser.get(self.userFollowed).prettyName(), "success");
        var image = this.model.get("id");
        this.followInterval = setInterval(function () {
            new UserPositionModel({ image: image, user: self.userFollowed }).fetch({
                success: function (model, response) {
                    self.browseImageView.map.moveTo(new OpenLayers.LonLat(model.get("longitude"), model.get("latitude")), model.get("zoom"));
                    var layerWrapper = _.detect(self.vectorLayers, function (item) {
                        return item.id == self.userFollowed;
                    });
                    if (layerWrapper) {
                        layerWrapper.vectorsLayer.refresh();
                    }
                },
                error: function (model, response) {
                }
            });
        }, 1000);
    },
    stopFollowing: function () {
        var self = this;
        if (self.followInterval != undefined) {
            window.app.view.message("", "Stop following " + window.app.models.projectUser.get(self.userFollowed).prettyName(), "success");
            clearInterval(self.followInterval);
            self.followInterval = null;
            self.userFollowed = null;
        }
    },
    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function (tpl) {
        var self = this;
        var content = _.template(tpl, {id: self.model.get("id"), isDesktop: !window.app.view.isMobile});
        $("#" + this.browseImageView.divId).find("#layerSwitcher" + self.model.get("id")).html(content);
        console.log("test");
        console.log("test: "+ self.browseImageView.divId + " " + "#layerSwitcher" + self.model.get("id") + " .followUser");
        console.log($("#" + self.browseImageView.divId).length);
        console.log($("#" + self.browseImageView.divId).find("#layerSwitcher" + self.model.get("id")).length);
        console.log($("#" + self.browseImageView.divId).find("#layerSwitcher" + self.model.get("id")).find(".followUser").length);
        console.log($("#" + self.browseImageView.divId + " > #layerSwitcher" + self.model.get("id")+" > .followUser").length);


        $("#" + self.browseImageView.divId).on('change',".followUser", function (e) {
            var followUser = $(this).is(':checked');
            $("#" + self.browseImageView.divId).find("#layerSwitcher" + self.model.get("id")).find('.followUser:checked').each(function () {
                $(this).attr('checked', false);
            });
            console.log(followUser);
            $(this).attr('checked', followUser);
            console.log(this);
            self.stopFollowing();
            if (!followUser) {
                return;
            }
            var user = $(this).attr("data-user-id");
            self.userFollowed = user;
            self.startFollowing();

        });

        $("#selectLayersIcon" + self.model.get("id")).off("click");

        $(document).on("change", "#selectLayersIcon" + self.model.get("id"), function (event) {
            console.log("change user annotation");
            var userList = $("#" + self.browseImageView.divId).find("#layerSwitcher" + self.model.get("id")).find("ul.annotationLayers");
            var projectUsers = _.pluck(window.app.models.projectUser, 'id');
            var almostOneCheckedState = false;
            _.each(projectUsers, function (userID) {
                var checked = userList.find("li[data-id=" + userID + "]").find('input.showUser').is(':checked');
                if (!almostOneCheckedState && checked) {
                    almostOneCheckedState = true;
                }
            });
            if (userList.find("li[data-id=REVIEW]").find('input.showUser').is(':checked')) {
                almostOneCheckedState = true;
            }
            self.browseImageView.setAllLayersVisibility(!almostOneCheckedState);
        });

        var el = $('#layerSwitcher' + self.model.get('id'));
        var elContent1 = el.find(".layerSwitcherContent1");
        var sourceEvent1 = el.find(".toggle-content1");
        var sourceEvent2 = el.find(".toggle-content2");
        var elContent2 = el.find(".layerSwitcherContent2");
        this.initToggle(el, elContent1, sourceEvent1, "layerSwitcherContent1");
        this.initToggle(el, elContent2, sourceEvent2, "layerSwitcherContent2");

        console.log($("#" + self.browseImageView.divId).find("#layerSwitcher" + self.model.get("id")).find(".followUser").length);
    }
});
