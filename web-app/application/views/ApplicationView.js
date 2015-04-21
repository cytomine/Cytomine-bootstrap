Storage.prototype.setObject = function (key, value) {
    this.setItem(key, JSON.stringify(value));
}

Storage.prototype.getObject = function (key) {
    return JSON.parse(this.getItem(key));
}

var ApplicationView = Backbone.View.extend({

    tagName: "div",
    className: "layout",
    components: {},
    intervals: [], //references to followInterval, positionInterval...
    isMobile: ( navigator.userAgent.match(/iPad/i) != null ),
    events: {

    },
    clearIntervals: function () {
        _.each(this.intervals, function (interval) {
            clearInterval(interval);
        });
    },
    /**
     *  UNDO the last command
     */
    undo: function () {
        window.app.controllers.command.undo();
    },

    /**
     * REDO the last command
     */
    redo: function () {
        window.app.controllers.command.redo();
    },

    /**
     * ApplicationView constructor. Call the initialization of its components
     * @param options
     */
    initialize: function (options) {
    },
    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function (tpl, renderCallback) {
        var self = this;
        $("body").prepend(_.template(tpl, {}));
        _.each(this.components, function (component) {
            component.render();
        });
        self.initEvents();
        renderCallback.call();
        return this;
    },
    initEvents: function () {
        $(document).on('click',"#undo",this.undo);
        $(document).on('click',"#redo",this.redo);
//
//        $("#undo").on('click', this.undo);
//        $("#redo").on('click', this.redo);
    },
    /**
     * Grab the layout and call ask for render
     */
    render: function (renderCallback) {
        this.initComponents();
        var self = this;
        require([
            "text!application/templates/BaseLayout.tpl.html","text!application/templates/HotkeysDialog.tpl.html"
        ],
            function (tpl,tplHotkeys) {
                self.doLayout(tpl, renderCallback);
                var modal = new CustomModal({
                    idModal : "hotkeysModal",
                    button : $("#hotkeysModalButton"),
                    header :"HotKeys",
                    body :tplHotkeys,
                    width : 900,
                    height : 800
                });
                modal.addButtons("closeHotKeys","Close",true,true);
            });
        return this;
    },
    initPreferences: function () {
        _.each(this.panelsConfiguration, function (item) {
            if (window.localStorage.getItem(item.key)) {
                return;
            }
            window.localStorage.setItem(item.key, item.value);
        });
    },
    applyPreferences: function () {
        var self = this;
        _.each(self.panelsConfiguration, function (item) {
            self.updateMenuItem(item);
        });
    },
    initUserMenu: function () {
        var self = this;
        self.showHideMenuAction();
        //Init user menu
        $("#logout").click(function () {
            window.app.controllers.auth.logout();
            return false;
        });
        $("#loggedUser").html(window.app.status.user.model.prettyName());
        if(window.app.status.user.model.get("isSwitched")) {
            $("#loggedUser").css("color","#ff0000");
            $("#li-cancel-switch-user").show();
            $("#a-cancel-switch-user").append(" Go back to " + window.app.status.user.model.get("realUser"));
            $("#a-cancel-switch-user").css("color","#d2322d");
        } else {
            $("#li-cancel-switch-user").hide();
        }


        if(window.app.status.user.model.get("adminByNow")) {
            //user is admin and an admin session is open
            $("#userIcon").addClass("glyphicon");
            $("#userIcon").addClass("glyphicon-star");
        } else {
            $("#userIcon").addClass("glyphicon");
            $("#userIcon").addClass("glyphicon-user");
        }

        if(window.app.status.user.model.get("adminByNow")) {
            //user is admin and an admin session is open
            $("#warning-user-logged-as-admin").show();
            $("#li-close-admin-session").show();
            $("#li-close-admin-session").on("click", function (e) {
                e.preventDefault();
                $.get( "grantRole/closeAdminSession.json", function( data ) {
                    location.reload(true);
                });
            });
        } else if (window.app.status.user.model.get("admin")){
            $("#li-open-admin-session").show();
            $("#li-open-admin-session").on("click", function (e) {
                e.preventDefault();
                $.get( "grantRole/openAdminSession.json", function( data ) {
                    location.reload(true);
                });
            });
        }




        if(window.app.status.user.model.get('guest'))  {
            $("#feedback").hide();
        }

        $("#feedback").on("click", function (e) {
            e.preventDefault();
			FreshWidget.show();
        });

        $("#a-info-cytomine").click(function () {
            console.log("test");

            var body;
            require([
                    "text!application/templates/about/About.tpl.html"
                ],
                function (tpl) {
                    body = _.template(tpl, {version : window.app.status.version, mail : "info@cytomine.be"});

                    var modal = new CustomModal({
                        idModal: "about" + "DialogModal",
                        header: null,
                        body: body,
                        wide: true
                    });

                    modal.render();
                    $('#' + "about" + 'DialogModal').modal();// display the dialog box

                    //$(".modal-header").hide();
                    $(".modal-footer").hide();
                    $("#aboutCyt").css('width', 'auto');
                }
            );
            return false;
        });

    },
    showHideMenuAction : function() {
        _.each(window.app.status.customUI.global,function(val,key) {
            if(val) {
                $("#custom-ui-"+key).show();
            } else {
                $("#custom-ui-"+key).hide();
            }
        });
    },
//<li><a style="display:none;" id="userdashboard-button" href="#userdashboard"><i class="glyphicon glyphicon-dashboard" /> Dashboard</a></li>
//    <li><a style="display:none;" id="project-button" href="#project"><i class="glyphicon glyphicon-list-alt" /> Projects</a></li>
//    <li><a style="display:none;" id="ontology-button" href="#ontology"><i class="glyphicon glyphicon-book" /> Ontologies</a></li>
//    <li><a style="display:none;" id="explorer-button" href="#explorer"><i class="glyphicon glyphicon-eye-open" /> Explore</a></li>
//    <li><a style="display:none;" id="upload-button" href="#upload"><span class="glyphicon glyphicon-hdd"></span> Storage</a></li>
//    <li><a style="display:none;" id="activity-button" href="#activity-"><span class="glyphicon glyphicon-fire"></span> Activity</a></li>
//    <li><a style="display:none;" id="admin-button" href="admincyto"><span class="glyphicon glyphicon-wrench"></span> Admin</a></li>
//
//</ul>
//<div class="navbar-header  navbar-right">
//    <ul class="nav navbar-nav navbar-left">
//        <li class="divider-vertical"></li>
//        <li><a style="display:none;" id="hotkeysModalButton" href="#hotkeysModal"  data-toggle="modal"><i class="glyphicon glyphicon-question-sign" /> Help</a> <li>
//            <li><a style="display:none;" id="feedback"><i class="glyphicon glyphicon-bullhorn" /> Feedback</a></li>
//
    printTaskEvolution: function (task, divToFill, timeout) {
        this.printTaskEvolution(task, divToFill, timeout, false);
    },
    printTaskEvolution: function (task, divToFill, timeout, reverse) {
        function checkTask() {
            //load all job data
            console.log(task);
            new TaskModel({id: task.id}).fetch({
                    success: function (taskInfo, response) {
                        divToFill.empty();
                        divToFill.append('' +
                            '<div class="progress progress-striped active">' +
                            '   <div class="bar" style="background-color:#2C3E50;height:50px;width: ' + taskInfo.get('progress') + '%;"></div>' +
                            '</div>');
                        divToFill.append(taskInfo.get('comments').reverse().join('<br>'));
                    },
                    error: function (collection, response) {
                        console.log("error getting task");
                    }}
            );
        }

        checkTask();
        var timer = setInterval(function () {
            checkTask()
        }, timeout);
        return timer;
    },
    /**
     * Initialize the components of the application
     */
    initComponents: function () {
        var self = this;
        require([
            "text!application/templates/user/UserDashboardComponent.tpl.html",
            "text!application/templates/upload/UploadComponent.tpl.html",
            "text!application/templates/project/ProjectComponent.tpl.html",
            "text!application/templates/ontology/OntologyComponent.tpl.html",
            "text!application/templates/explorer/ExplorerComponent.tpl.html",
            "text!application/templates/AdminComponent.tpl.html",
            "text!application/templates/activity/ActivityComponent.tpl.html",
            "text!application/templates/account/AccountComponent.tpl.html",
            "text!application/templates/search/SearchComponent.tpl.html"
        ],
            function (userDashboardTpl,uploadTpl, projectTpl, ontologyTpl, explorerTpl, adminTpl, activityTpl, accountTpl,searchTpl) {

                self.components.userdashboard = new Component({
                    el: "#content",
                    template: _.template(userDashboardTpl, {}),
                    buttonAttr: {
                        elButton: "userdashboard-button"
                    },
                    divId: "userdashboard"
                });
                self.components.search = new Component({
                    el: "#content",
                    template: _.template(searchTpl, {}),
                    buttonAttr: {
                        elButton: "search-button"
                    },
                    divId: "search"
                });
                self.components.activity = new Component({
                    el: "#content",
                    template: _.template(activityTpl, {}),
                    buttonAttr: {
                        elButton: "activity-button"
                    },
                    divId: "activity"
                });
                self.components.upload = new Component({
                    el: "#content",
                    template: _.template(uploadTpl, {}),
                    buttonAttr: {
                        elButton: "upload-button"
                    },
                    divId: "upload"
                });
                self.components.account = new Component({
                    el: "#content",
                    template: _.template(accountTpl, {}),
                    buttonAttr: {
                        elButton: "upload-button"
                    },
                    divId: "account"
                });
                self.components.project = new Component({
                    el: "#content",
                    template: _.template(projectTpl, {}),
                    buttonAttr: {
                        elButton: "project-button"
                    },
                    divId: "project"
                });
                self.components.ontology = new Component({
                    el: "#content",
                    template: _.template(ontologyTpl, {}),
                    buttonAttr: {
                        elButton: "ontology-button"
                    },
                    divId: "ontology"
                });
                self.components.explorer = new Component({
                    el: "#content",
                    template: _.template(explorerTpl, {}),
                    buttonAttr: {
                        elButton: "explorer-button"
                    },
                    divId: "explorer",
                    activate: function () {
                        if (window.app.status.currentProject == undefined) {
                            $("#explorer > .noProject").show();
                        }
                        else {
                            $("#explorer > .noProject").hide();
                        }
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
    showComponent: function (component) {
        _.each(this.components, function (c) {
            if (c != component) {
                c.deactivate();
            }
        });
        $("#app").show();
        component.activate();
    },
    getUserNameById: function (userId) {
        if (window.app.models.projectUser.get(userId)) {
            return window.app.models.projectUser.get(userId).prettyName();
        } else if (window.app.models.projectUserJob.get(userId)) {
            return window.app.models.projectUserJob.get(userId).get("softwareName");
        } else {
            return "undefined"; //should not appear
        }
    }
});

ApplicationView.prototype.message = function (title, message, type,timer) {
    if (type == "error") type = "danger"; //Bootstrap 3
    if (type == "" || type == undefined) {
        type = 'alert-info';
    }
    else {
        type = 'alert-' + type;
    }

    if (message != undefined) {
        message.responseText && (message = message.responseText);
    }

    var tpl = '<div style="width : 400px;" id="alert<%=   timestamp %>" class="alert <%=   type %> alert-dismissable" data-alert="alert"><p><strong><%=   alert %></strong> <%=   message %></p></div>';
    var timestamp = new Date().getTime();
    var left = ($(window).width() / 2 - 200);

    var numberOfOpenedDiv = $("#alerts").find("div.alert").length;
    var maxOtherOpenedAlert = 1;
    var divToClose = numberOfOpenedDiv-maxOtherOpenedAlert;
    if(divToClose>0) {
        $("#alerts").find("div.alert:lt("+divToClose+")").remove()
    }
    $("#alerts").css("left", left).append(_.template(tpl, { alert: title, message: message, timestamp: timestamp, type: type}));

    if(!timer) {
        timer = 3000;
    }
    setTimeout(function () {
        $("#alert" + timestamp).remove();
    }, timer);

}




