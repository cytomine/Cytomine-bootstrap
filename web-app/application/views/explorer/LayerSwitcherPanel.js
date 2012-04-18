/**
 * Created by IntelliJ IDEA.
 * User: stevben
 * Date: 12/06/11
 * Time: 12:32
 * To change this template use File | Settings | File Templates.
 */

var LayerSwitcherPanel = Backbone.View.extend({
    tagName : "div",

    /**
     * ExplorerTabs constructor
     * @param options
     */
    initialize: function(options) {
        this.browseImageView = options.browseImageView;
        this.followInterval = null;
        this.userFollowed = null;
		this.vectorLayers = [];
    },
    /**
     * Grab the layout and call ask for render
     */
    render : function() {
        var self = this;
        require([
            "text!application/templates/explorer/LayerSwitcher.tpl.html"
        ], function(tpl) {
            self.doLayout(tpl);
        });
        return this;
    },
    addBaseLayer : function (layer, model) {
        var self = this;
        var radioName = "layerSwitch-" + model.get("id");
        var layerID = "layerSwitch-" + model.get("id") + "-" + new Date().getTime(); //index of the layer in this.layers array
        var liLayer = _.template("<li><input type='radio' id='<%=   id %>' name='<%=   radioName %>' checked/><span style='color : #ffffff;'> <%=   name %></span></li>", {id : layerID, radioName:radioName, name : layer.name.substr(0,15)});
        $("#layerSwitcher"+this.model.get("id")).find(".baseLayers").append(liLayer);
        $("#layerSwitcher"+this.model.get("id")).find(".baseLayers").find("#"+layerID);
        $("#"+layerID).change(function(){
            self.browseImageView.map.setBaseLayer(layer);
        });
    },
    addVectorLayer : function (layer, model, userID) {
        var layerID = window.app.models.users.get(userID).prettyName();
		this.vectorLayers.push({ id : userID, vectorsLayer : layer.vectorsLayer});
        var layerID = "layerSwitch-" + model.get("id") + "-" + userID + "-"  + new Date().getTime(); //index of the layer in this.layers array
        var color = window.app.models.users.get(userID).get('color');
        var layerOptionTpl;
        if (layer.isOwner) {
            layerOptionTpl = _.template("<li><input id='<%= id %>' class='showUser' type='checkbox'  value='<%= name %>' checked />&nbsp;&nbsp;<input type='checkbox' disabled/><span style='color : #ffffff;'> <%=   name %></span></li>", {id : layerID, name : layer.vectorsLayer.name, color : color});
        } else {
            /*layerOptionTpl = _.template("<li><input id='<%= id %>' type='checkbox' value='<%=   name %>' /> <span style='color : #ffffff;'><%=   name %></span> <a class='followUser' data-user-id='<%= userID %>' href='#'>Follow</a></li>", {userID : userID, id : layerID, name : layer.vectorsLayer.name, color : color});*/
            layerOptionTpl = _.template("<li data-id='<%= userID %>'><input id='<%= id %>' class='showUser' type='checkbox' value='<%= name %>' />&nbsp;&nbsp;<input type='checkbox' class='followUser' data-user-id='<%= userID %>' disabled/>&nbsp;<span style='color : #ffffff;'><%= name %></span></a> </li>", {userID : userID, id : layerID, name : layer.vectorsLayer.name, color : color});
        }
        $("#layerSwitcher"+model.get("id")).find("ul.annotationLayers").append(layerOptionTpl);

        $("#"+layerID).click(function(){
            var checked = $(this).attr("checked");
            layer.vectorsLayer.setVisibility(checked);
        });

    },
    updateOnlineUsers : function (onlineUsers) {
        var self = this;
        var userList = $("#layerSwitcher"+this.model.get("id")).find("ul.annotationLayers");
        var project = window.app.status.currentProjectModel;
        var projectUsers = window.app.models.users.select(function(user){
            return _.include(project.get("users"), user.id);
        });
        projectUsers = _.pluck(projectUsers, 'id');
        //check if the the user we are following is always connected, if not disconneted
        if (!_.include(onlineUsers, self.userFollowed)) {
            userList.find("li[data-id="+self.userFollowed+"]").find('input.followUser').removeAttr('checked');
            self.stopFollowing();
        }
        _.each(projectUsers, function (userID) {
            if (!_.include(onlineUsers, userID)) {
                userList.find("li[data-id="+userID+"]").find('span').css('color', 'white');
                userList.find("li[data-id="+userID+"]").find('input.followUser').attr("disabled", "disabled")
            }
        });
        _.each(onlineUsers, function (userID) {
            userList.find("li[data-id="+userID+"]").find('span').css('color', '#46a546');
            userList.find("li[data-id="+userID+"]").find('input.followUser').removeAttr("disabled");
        });
    },
    startFollowing : function () {
        var self = this;
        window.app.view.message("","Start following " + window.app.models.users.get(self.userFollowed).prettyName(), "success");
        var image = this.model.get("id");
        this.followInterval = setInterval(function() {
            new UserPositionModel({ image : image, user : self.userFollowed }).fetch({
                success : function (model, response) {
                    self.browseImageView.map.moveTo(new OpenLayers.LonLat(model.get("longitude"), model.get("latitude")), model.get("zoom"));
					var layerWrapper = _.detect(self.vectorLayers, function (item) {
						return item.id == self.userFollowed;
					});
					if (layerWrapper) layerWrapper.vectorsLayer.refresh();
                },
                error : function(model, response ) {
                }
            });
        }, 1000);
    },
    stopFollowing: function() {
        var self = this;
        if (self.followInterval != undefined) {
            window.app.view.message("","Stop following " + window.app.models.users.get(self.userFollowed).prettyName(), "success");
            clearInterval(self.followInterval);
            self.followInterval = null;
            self.userFollowed = null;
        }
    },
    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function(tpl) {
        var self = this;
        var content = _.template(tpl, {id : self.model.get("id"),isDesktop : !window.app.view.isMobile});
        $("#layerSwitcher"+self.model.get("id")).html(content);

        $("#layerSwitcher"+self.model.get("id")).find(".toggleShowBaseLayers").click(function () {
            $("#layerSwitcher"+self.model.get("id")).find("ul.baseLayers").toggle(150);
            return false;
        });

        $("#layerSwitcher"+self.model.get("id")).find(".toggleShowVectorLayers").click(function () {
            $("#layerSwitcher"+self.model.get("id")).find("ul.annotationLayers").toggle(150);
            return false;
        });

        $("#layerSwitcher"+self.model.get("id")).find(".followUser").live('click', function (e){
            var followUser = $(this).attr("checked") == "checked";
            $("#layerSwitcher"+self.model.get("id")).find('.followUser:checked').each(function () {
                $(this).attr('checked', false);
            });
            $(this).attr('checked', followUser);
            self.stopFollowing();
            if (!followUser) return;
            var user = $(this).attr("data-user-id");
            self.userFollowed = user;
            self.startFollowing();

        });

        $("#selectLayersIcon"+self.model.get("id")).off("click");
        $("#selectLayersIcon"+self.model.get("id")).on("click", function (event) {
            var project = window.app.status.currentProjectModel;
            var projectUsers = window.app.models.users.select(function(user){
                return _.include(project.get("users"), user.id);
            });
            var userList = $("#layerSwitcher"+self.model.get("id")).find("ul.annotationLayers");
            projectUsers = _.pluck(projectUsers, 'id');
            var almostOneCheckedState = false;
            _.each(projectUsers, function (userID) {
                var checked = userList.find("li[data-id="+userID+"]").find('input.showUser').attr("checked") == "checked";
                if (!almostOneCheckedState && checked) almostOneCheckedState = true;
            });
            self.browseImageView.setAllLayersVisibility(!almostOneCheckedState);
        });
        new DraggablePanelView({
            el : $('#layerSwitcher' + self.model.get('id')),
            className : "layerSwitcherPanel"
        }).render();
    }
});
