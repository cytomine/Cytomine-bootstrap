Storage.prototype.setObject = function(key, value) {
    this.setItem(key, JSON.stringify(value));
}

Storage.prototype.getObject = function(key) {
    return JSON.parse(this.getItem(key));
}

var ApplicationView = Backbone.View.extend({

    tagName : "div",
    className : "layout",
    components : {},
    intervals : [], //references to followInterval, positionInterval...
    isMobile : ( navigator.userAgent.match(/iPad/i) != null ),
    panelsConfiguration : [
        {key : "toolbar-panel", linkID : "toggle-toolbar-panel", name : "Toolbar", className : "toolbarPanel", value : { visible : true , position : { bottom : 0}, align : "center"}},
        {key : "overview-panel", linkID : "toggle-overview-panel", name : "Overview", className : "overviewPanel", value : { visible : true , position : { right : 20, top : 325}}},
        {key : "ontology-panel", linkID : "toggle-ontology-panel", name : "Ontology", className : "ontologyPanel", value : { visible : true , position : { left : 20, top : 280}}},
        {key : "layer-panel", linkID : "toggle-layer-panel", name : "Layer switcher", className : "layerSwitcherPanel", value : { visible : false , position : { right : 20, top : 100}}},
        {key : "filters-panel", linkID : "toggle-filters-panel", name : "Filters", className : "imageFiltersPanel", value : { visible : false , position : { right : 20, bottom : 15}}}
    ],
    events: {

    },
    clearIntervals : function (){
        _.each(this.intervals, function (interval) {
            clearInterval(interval);
        });
    },
    /**
     *  UNDO the last command
     */
    undo : function () {
        window.app.controllers.command.undo();
    },

    /**
     * REDO the last command
     */
    redo : function () {
        window.app.controllers.command.redo();
    },
    hideFloatingPanels : function() {
        _.each(this.panelsConfiguration, function (item) {
            if (item.key == "toolbar-panel") return;
            $("."+item.className).hide();
        });
    },
    showFloatingPanels : function() {
        _.each(this.panelsConfiguration, function (item) {
            if (item.key == "toolbar-panel") return;
            $("."+item.className).show();
        });
    },
    toggleVisibility : function (item) {
        var self = this;
        var preference = localStorage.getObject(item.key);
        preference.visible = !preference.visible;
        if (preference.visible && this.isMobile) { //hide others panel

            _.each(self.panelsConfiguration, function (panel) {
                if (panel.key == item.key) return;
                var visible = false;
                if (panel.key == "toolbar-panel") {
                    visible = true;
                }
                preferencePanel = localStorage.getObject(panel.key);
                preferencePanel.visible = visible;
                localStorage.setObject(panel.key , preferencePanel);
                self.updateMenuItem(panel);
            });
        }
        localStorage.setObject(item.key , preference);
        this.updateMenuItem(item);

    },
    updateMenuItem : function (item) {
        var preference = localStorage.getObject(item.key);
        if (preference != undefined && preference.visible != undefined && preference.visible == true) {
            $("#"+item.linkID).html("<i class='icon-eye-close' /> " + item.name);
            $("."+item.className).each(
                    function( intIndex ){
                        $(this).show('fast');
                    }
            );
        }
        else {
            $("#"+item.linkID).html("<i class='icon-eye-open' /> " + item.name);
            $("."+item.className).each(
                    function( intIndex ){
                        $(this).hide('fast');
                    }
            );
        }
    },
    /**
     * ApplicationView constructor. Call the initialization of its components
     * @param options
     */
    initialize: function(options) {
        var resizeTO = null;
        $(window).resize(function() {
            if(resizeTO) clearTimeout(resizeTO);
            resizeTO = setTimeout(function() {
                $(window).trigger('resizeEnd');
            }, 500);
        });
    },
    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function(tpl, renderCallback) {
        var self = this;
        $("body").prepend(_.template(tpl, {}));
        _.each(this.components, function (component) {
            component.render();
        });
        /*/$(window).resize(function(){
         self.applyPreferences()
         });*/
        self.initEvents();
        renderCallback.call();
        return this;
    },
    initEvents : function (){
        $("#undo").live('click', this.undo);
        $("#redo").live('click', this.redo);
    },
    /**
     * Grab the layout and call ask for render
     */
    render : function(renderCallback) {
        this.initComponents();
        var self = this;
        require([
            "text!application/templates/BaseLayout.tpl.html"
        ],
                function(tpl) {
                    self.doLayout(tpl, renderCallback);
                });
        return this;
    },
    initPreferences : function () {
        _.each(this.panelsConfiguration, function (item) {
            if (localStorage.getObject(item.key)) return;
            localStorage.setObject(item.key, item.value);
        });
    },
    applyPreferences : function() {
        var self = this;
        _.each(self.panelsConfiguration, function (item) {
            self.updatePanelPosition(item, true);
            self.updateMenuItem(item);
        });
    },
    updatePanelPosition : function (item, triggerMoveEvent) {

        var preference = localStorage.getObject(item.key);

        if (preference == undefined) return;

        //TMP CODE, we should create mobilePreferences and DesktopPreferences object
        //and have a versionning in order to override localstorage data on clients
        if (this.isMobile) { //force bottom right
            if (item.key == "toolbar-panel")
                preference.position.bottom = 0;
            else
                preference.position.bottom = 40;
            if (item.key != "toolbar-panel")
                preference.position.right = 5;
            else
                delete preference.position.right;
            delete preference.position.left;
            delete preference.position.top;
        } else { //force bottom right desktop
            if (item.key == "toolbar-panel") {
                delete preference.position.right;
                delete preference.position.left;
                delete preference.position.top;
                preference.position.bottom = 0;
            }
        }
        var panelWidth = 0;
        var panelHeight = 0;
       $.each($("."+item.className) , function(index, value) {
           var width = $(value).width();
           var height = $(value).height();
           if (width > 0) panelWidth = width;
           if (height > 0) panelHeight = height;
        });
        var panel = $("."+item.className);

        var windowWidth = $(window).width();
        var windowHeight = $(window).height();
        // Alignment

        if (panelWidth != 0 && preference.align != undefined && preference.align == "center") {
            var leftPosition = (windowWidth / 2) - (panelWidth / 2);
            preference.position.left = leftPosition;
        }
        // Check if out of bounds
        if (panelWidth != 0 && preference.position.left + panelWidth > windowWidth) {
            preference.position.left = windowWidth - panelWidth
        }
        if (panelHeight != 0 && preference.position.top + panelHeight > windowHeight) {
            preference.position.top = windowHeight - panelHeight
        }

        // Update values
        localStorage.setObject(item.key, preference);

        // Position
        var position = preference.position;
        if (position.top == undefined) position.top = '';
        else $("."+item.className).css("top", position.top);
        if (position.left == undefined) position.left = '';
        else $("."+item.className).css("left", position.left);
        if (position.right == undefined) position.right = '';
        else $("."+item.className).css("right", position.right);
        if (position.bottom == undefined) position.bottom = '';
        else $("."+item.className).css("bottom", position.bottom);

        if (triggerMoveEvent) $("."+item.className).trigger("movedProgramatically");
    },
    updatePosition : function (className, position, triggerMoveEvent) {
        var self = this;
        var panelConfig = _.find(self.panelsConfiguration, function (item) {
            return item.className == className;
        });
        if (!panelConfig) return;
        var preference = localStorage.getObject(panelConfig.key);
        preference.position = position;
        localStorage.setObject(panelConfig.key, preference);
        self.updatePanelPosition(panelConfig, triggerMoveEvent);
    },
    initUserMenu : function () {
        var self = this;
        //Init user menu
        require([
            "text!application/templates/MenuDropDown.tpl.html"
        ],function(tpl) {
            $("#menu-right").append(tpl);
            $("#logout").click(function() {
                window.app.controllers.auth.logout();
                return false;
            });
            $("#loggedUser").html(window.app.status.user.model.prettyName());
            _.each(self.panelsConfiguration, function (item){
                self.updateMenuItem(item);
                $("#"+item.linkID).on('click',function(){
                    self.toggleVisibility(item);
                    self.updatePanelPosition(item, true);
                });
            });
            $("#toggle-floating-panel").on("click", function(){
                var key = "toggle-floating-panel";
                var preference = localStorage.getObject(key);
                preference.activated = !preference.activated;
                localStorage.setObject(key, preference);
            });
        });

    },
    /**
     * Initialize the components of the application
     */
    initComponents : function() {
        var self = this;
        require([
            "text!application/templates/upload/UploadComponent.tpl.html",
            "text!application/templates/project/ProjectComponent.tpl.html",
            "text!application/templates/ontology/OntologyComponent.tpl.html",
            "text!application/templates/explorer/ExplorerComponent.tpl.html",
            "text!application/templates/AdminComponent.tpl.html",
            "text!application/templates/activity/ActivityComponent.tpl.html",
            "text!application/templates/account/AccountComponent.tpl.html"
        ],
                function(uploadTpl, projectTpl, ontologyTpl, explorerTpl, adminTpl, activityTpl, accountTpl) {
                    self.components.activity = new Component({
                        el : "#content",
                        template : _.template(activityTpl, {}),
                        buttonAttr : {
                            elButton : "activity-button"
                        },
                        divId : "activity"
                    });
                    self.components.upload = new Component({
                        el : "#content",
                        template : _.template(uploadTpl, {}),
                        buttonAttr : {
                            elButton : "upload-button"
                        },
                        divId : "upload"
                    });
                    self.components.account = new Component({
                        el : "#content",
                        template : _.template(accountTpl, {}),
                        buttonAttr : {
                            elButton : "upload-button"
                        },
                        divId : "account"
                    });
                    self.components.project = new Component({
                        el : "#content",
                        template : _.template(projectTpl, {}),
                        buttonAttr : {
                            elButton : "project-button"
                        },
                        divId : "project"
                    });
                    self.components.ontology = new Component({
                        el : "#content",
                        template : _.template(ontologyTpl, {}),
                        buttonAttr : {
                            elButton : "ontology-button"
                        },
                        divId : "ontology"
                    });
                    self.components.explorer = new Component({
                        el : "#content",
                        template : _.template(explorerTpl, {}),
                        buttonAttr : {
                            elButton : "explorer-button"
                        },
                        divId : "explorer",
                        activate: function () {
                            if (window.app.status.currentProject == undefined)
                                $("#explorer > .noProject").show();
                            else
                                $("#explorer > .noProject").hide();
                            $("#" + this.divId).show();
                            $("#" + this.buttonAttr.elButton).parent().addClass("active");
                        }
                    });
                    /*self.components.admin = new Component({
                     el : "#content",
                     template : _.template(adminTpl, {}),
                     buttonAttr : {
                     elButton : "admin-button",
                     buttonText : "Admin",
                     buttonWrapper : "#menu",
                     icon : "ui-icon-wrench",
                     route : "#admin/users"
                     },
                     divId : "admin"
                     });*/

                    /*self.components.logout = new Component({
                     el : "#content",
                     template : "",
                     buttonAttr : {
                     elButton : "user-button",
                     buttonText :,
                     buttonWrapper : "#menu",
                     dataContent : "we have to delete this popover for logout",
                     dataTitle : "huhu",
                     icon : "ui-icon-power",
                     route : "#",
                     click :
                     },
                     divId : "logout"
                     });*/
                });
    },
    /**
     * Show a component
     * @param Component the reference to the component
     */
    showComponent : function (component) {
        _.each(this.components, function (c) {
            if (c != component) c.deactivate();
        });
        $("#app").show();
        component.activate();
    },
    getUserNameById : function(userId) {
        if (window.app.models.projectUser.get(userId)) {
            return window.app.models.projectUser.get(userId).prettyName();
        } else if (window.app.models.projectUserJob.get(userId)) {
            return window.app.models.projectUserJob.get(userId).get("softwareName");
        } else {
            return "undefined"; //should not appear
        }
    }
});

ApplicationView.prototype.message =  function(title, message, type) {
    if (type == "" || type == undefined)
        type = 'alert-info';
    else
        type = 'alert-'+type;

    if(message!=undefined) {
        message.responseText && (message = message.responseText);
    }

    var tpl = '<div style="min-width: 200px" id="alert<%=   timestamp %>" class="alert <%=   type %> fade in" data-alert="alert"><a class="close" data-dismiss="alert">×</a><p><strong><%=   alert %></strong> <%=   message %></p></div>';
    var timestamp = new Date().getTime();
    $("#alerts").append(_.template(tpl, { alert : title, message : message, timestamp : timestamp, type : type}));
    setTimeout(function(){
        $("#alert"+timestamp).remove();
    }, 3000);

}




