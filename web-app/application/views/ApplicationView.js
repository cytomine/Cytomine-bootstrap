Storage.prototype.setObject = function (key, value) {
    this.setItem(key, JSON.stringify(value));
}

Storage.prototype.getObject = function (key) {
    return JSON.parse(this.getItem(key));
}

var ApplicationView = Backbone.View.extend({

    tagName:"div",
    className:"layout",
    components:{},
    intervals:[], //references to followInterval, positionInterval...
    isMobile:( navigator.userAgent.match(/iPad/i) != null ),
    panelsConfiguration:[
        /*{key:"sidebar-map-left", linkID:"toggle-sidebar-map-left", name:"Left panels", className:["sidebar-map-left", "olControlZoomPanel"], value:{ visible:true}},*/
        {key:"sidebar-map-right", linkID:"toggle-sidebar-map-right", name:"Panels", className:["sidebar-map-right"], value:{ visible:true}}
    ],
    events:{

    },
    clearIntervals:function () {
        _.each(this.intervals, function (interval) {
            clearInterval(interval);
        });
    },
    /**
     *  UNDO the last command
     */
    undo:function () {
        window.app.controllers.command.undo();
    },

    /**
     * REDO the last command
     */
    redo:function () {
        window.app.controllers.command.redo();
    },
    hideFloatingPanels:function () {
        var self = this;
        _.each(this.panelsConfiguration, function (item) {
            self.hideFloatingPanel(item);
        });
    },
    hideFloatingPanel : function (item) {
        if (_.isArray(item.className)) {
            _.each(item.className, function(_className){
                $("." + _className).hide();
            });
        } else {
            $("." + item.className).hide();
        }
    },
    showFloatingPanels:function () {
        var self = this;
        _.each(this.panelsConfiguration, function (item) {
           self.showFloatingPanel(item);
        });
    },
    showFloatingPanel : function(item){
        if (_.isArray(item.className)) {
            _.each(item.className, function(_className){
                $("." + _className).show()
            });
        } else {
            $("." + item.className).show();
        }
    },
    toggleVisibility:function (item) {
        var self = this;
        var preference = localStorage.getObject(item.key);
        preference.visible = !preference.visible;
        if (preference.visible && this.isMobile) { //hide others panel

            _.each(self.panelsConfiguration, function (panel) {
                if (panel.key == item.key) return;
                var visible = false;
                preferencePanel = localStorage.getObject(panel.key);
                preferencePanel.visible = visible;
                localStorage.setObject(panel.key, preferencePanel);
                self.updateMenuItem(panel);
            });
        }
        localStorage.setObject(item.key, preference);
        this.updateMenuItem(item);

    },
    updateMenuItem:function (item) {
        var self = this;
        var preference = localStorage.getObject(item.key);
        if (preference != undefined && preference.visible != undefined && preference.visible == true) {
            $("#" + item.linkID).html("<i class='icon-eye-close' /> " + item.name);
            self.showFloatingPanel(item);
        }
        else {
            $("#" + item.linkID).html("<i class='icon-eye-open' /> " + item.name);
            self.hideFloatingPanel(item);
        }
    },
    /**
     * ApplicationView constructor. Call the initialization of its components
     * @param options
     */
    initialize:function (options) {
    },
    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout:function (tpl, renderCallback) {
        var self = this;
        $("body").prepend(_.template(tpl, {}));
        _.each(this.components, function (component) {
            component.render();
        });
        self.initEvents();
        renderCallback.call();
        return this;
    },
    initEvents:function () {
        $("#undo").live('click', this.undo);
        $("#redo").live('click', this.redo);
    },
    /**
     * Grab the layout and call ask for render
     */
    render:function (renderCallback) {
        this.initComponents();
        var self = this;
        require([
            "text!application/templates/BaseLayout.tpl.html"
        ],
            function (tpl) {
                self.doLayout(tpl, renderCallback);
            });
        return this;
    },
    initPreferences:function () {
        _.each(this.panelsConfiguration, function (item) {
            if (localStorage.getObject(item.key)) return;
            localStorage.setObject(item.key, item.value);
        });
    },
    applyPreferences:function () {
        var self = this;
        _.each(self.panelsConfiguration, function (item) {
            self.updateMenuItem(item);
        });
    },
    initUserMenu:function () {
        var self = this;
        //Init user menu
        require([
            "text!application/templates/MenuDropDown.tpl.html"
        ], function (tpl) {
            $("#menu-right").append(tpl);
            $("#logout").click(function () {
                window.app.controllers.auth.logout();
                return false;
            });
            $("#loggedUser").html(window.app.status.user.model.prettyName());
            _.each(self.panelsConfiguration, function (item) {
                self.updateMenuItem(item);
                $("#" + item.linkID).on('click', function () {
                    self.toggleVisibility(item);
                });
            });
            $("#toggle-floating-panel").on("click", function () {
                var key = "toggle-floating-panel";
                var preference = localStorage.getObject(key);
                preference.activated = !preference.activated;
                localStorage.setObject(key, preference);
            });
        });

    },
    printTaskEvolution:function(task,divToFill, timeout) {
        this.printTaskEvolution(task,divToFill,timeout, false);
    },
    printTaskEvolution:function(task,divToFill, timeout, reverse) {
        function checkTask() {
            //load all job data
            new TaskModel({id:task.id}).fetch({
                    success:function(taskInfo,response) {
                        divToFill.empty();
                        divToFill.append('' +
                            '<div class="progress progress-striped active">' +
                            '   <div class="bar" style="width: '+taskInfo.get('progress')+'%;"></div>' +
                            '</div>');
                        divToFill.append(taskInfo.get('comments').reverse().join('<br>'));
                    },
                    error:function (collection, response) {
                        console.log("error getting task");
                    }}
            );
        }
        checkTask();
        var timer=setInterval(function(){checkTask()}, timeout);
        return timer;
    },
    /**
     * Initialize the components of the application
     */
    initComponents:function () {
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
            function (uploadTpl, projectTpl, ontologyTpl, explorerTpl, adminTpl, activityTpl, accountTpl) {
                self.components.activity = new Component({
                    el:"#content",
                    template:_.template(activityTpl, {}),
                    buttonAttr:{
                        elButton:"activity-button"
                    },
                    divId:"activity"
                });
                self.components.upload = new Component({
                    el:"#content",
                    template:_.template(uploadTpl, {}),
                    buttonAttr:{
                        elButton:"upload-button"
                    },
                    divId:"upload"
                });
                self.components.account = new Component({
                    el:"#content",
                    template:_.template(accountTpl, {}),
                    buttonAttr:{
                        elButton:"upload-button"
                    },
                    divId:"account"
                });
                self.components.project = new Component({
                    el:"#content",
                    template:_.template(projectTpl, {}),
                    buttonAttr:{
                        elButton:"project-button"
                    },
                    divId:"project"
                });
                self.components.ontology = new Component({
                    el:"#content",
                    template:_.template(ontologyTpl, {}),
                    buttonAttr:{
                        elButton:"ontology-button"
                    },
                    divId:"ontology"
                });
                self.components.explorer = new Component({
                    el:"#content",
                    template:_.template(explorerTpl, {}),
                    buttonAttr:{
                        elButton:"explorer-button"
                    },
                    divId:"explorer",
                    activate:function () {
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
    showComponent:function (component) {
        _.each(this.components, function (c) {
            if (c != component) c.deactivate();
        });
        $("#app").show();
        component.activate();
    },
    getUserNameById:function (userId) {
        if (window.app.models.projectUser.get(userId)) {
            return window.app.models.projectUser.get(userId).prettyName();
        } else if (window.app.models.projectUserJob.get(userId)) {
            return window.app.models.projectUserJob.get(userId).get("softwareName");
        } else {
            return "undefined"; //should not appear
        }
    }
});

ApplicationView.prototype.message = function (title, message, type) {
    if (type == "" || type == undefined)
        type = 'alert-info';
    else
        type = 'alert-' + type;

    if (message != undefined) {
        message.responseText && (message = message.responseText);
    }

    var tpl = '<div style="min-width: 200px" id="alert<%=   timestamp %>" class="alert <%=   type %> fade in" data-alert="alert"><a class="close" data-dismiss="alert">×</a><p><strong><%=   alert %></strong> <%=   message %></p></div>';
    var timestamp = new Date().getTime();
    $("#alerts").append(_.template(tpl, { alert:title, message:message, timestamp:timestamp, type:type}));
    setTimeout(function () {
        $("#alert" + timestamp).remove();
    }, 2000);

}




