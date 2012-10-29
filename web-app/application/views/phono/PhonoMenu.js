var PhonoMenu = Backbone.View.extend({

    itemsMenuActivated:["phono-deactivate", "phono-call", "phono-status", "phono-menu-divider"],
    itemsMenuDeactivated:["phono-activate"],
    phono:null,
    sip_cytomine_header:"x-cytomine-id",
    hangupTimeout:10000, //10 seconds
    onlineUsers:null,
    loadUsersInterval:null,
    render:function () {
        var self = this;
        require([
            "text!application/templates/phono/phonoMenu.tpl.html", "text!application/templates/phono/phonoUser.tpl.html"
        ], function (tplPhonoMenu) {
            $("#menu-right").append(tplPhonoMenu);
            self.bindEvents();
            self.activate();
            $(".online-user").live('click', function (e) {
                e.preventDefault();
                var idUser = $(this).attr("data-call-id");
                var model = self.onlineUsers.get(idUser);
                $("#phono-call").attr("disabled", true).val("Busy");
                console.log("call " + model.get("sipAccount"));
                self.phono.phone.dial(model.get("sipAccount"), {
                    headers:[
                        {
                            name:self.sip_cytomine_header,
                            value:window.app.status.user.id,
                            data:window.app.status.user.id
                        }
                    ],
                    onRing:function () {
                        ringBox = self.message("Info", "Ringing...", undefined);
                    },
                    onAnswer:function () {
                        var answerBoxContent = "In conversation with <%= firstname %> <%= lastname %>";
                        self.message("Info", _.template(answerBoxContent, model.toJSON()), undefined);
                        if (ringBox) {
                            ringBox.remove();
                            ringBox = null;
                        }

                    },
                    onHangup:function () {
                        $("#phono-call").attr("disabled", false).val("Call");
                        if (ringBox) {
                            ringBox.remove();
                            ringBox = null;
                        }
                        if (answerBox) {
                            answerBox.remove();
                            answerBox = null;
                        }
                        self.message("Info", "Hangup", 4000);

                    }
                });

            });
        });
    },

    loadUsers:function (tpl) {
        var self = this;
        new UserFriendCollection({ id:window.app.status.user.model.id}).fetch({
            success:function (collection, response) {
                self.onlineUsers = collection;
                $("#online-users").empty();
                collection.each(function (user) {
                    $("#online-users").append(_.template(tpl, user.toJSON()));
                });
            }
        });
    },

    bindEvents:function () {
        var self = this;
        $("#phono-activate").on('click', function (e) {
            e.preventDefault();
            self.activate();
        });
        $("#phono-deactivate").on('click', function (e) {
            e.preventDefault();
            self.deactivate();
        });
    },

    deactivate:function () {
        _.each(this.itemsMenuActivated, function (item) {
            $("#" + item).hide();
        });
        _.each(this.itemsMenuDeactivated, function (item) {
            $("#" + item).show();
        });
        if (self.phono) self.phono.disconnect();
        self.phono = null;
        clearInterval(self.loadUsersInterval);
        self.loadUsersInterval = null;
        this.message("Info", "Live chat/call offline", 3000);
    },

    activate:function () {
        var loggingBox = this.message("Info", "Logging in progress...");
        var self = this;
        require([
            "text!application/templates/phono/phonoUser.tpl.html"
        ], function (tplPhonoUser) {
            self.loadUsersInterval = setInterval(function () {
                self.loadUsers(tplPhonoUser);
            }, 5000);
        });
        self.phono = $.phono({
            apiKey:"96e914764866dfaf87b26b3cc3d23d03",
            onReady:function () {
                $("#phono-call").attr("disabled", false).val("Call");
                window.app.status.user.model.save({ sipAccount:"sip:" + this.sessionId}, {
                    success:function (model, response) {
                        window.app.status.user.model = model;
                        loggingBox.remove();

                        self.message("Info", "Live chat/call online", 2000);
                        _.each(self.itemsMenuDeactivated, function (item) {
                            $("#" + item).hide();
                        });
                        _.each(self.itemsMenuActivated, function (item) {
                            $("#" + item).show();
                        });
                    }
                });
            },
            phone:{
                onIncomingCall:function (event) {
                    var call = event.call;
                    var cytomine_header = _.find(call.headers, function (header) {
                        return header["name"] == self.sip_cytomine_header
                    });
                    var cytomine_id = cytomine_header["value"];
                    new UserModel({id:cytomine_id}).fetch({
                        success:function (model, response) {
                            var incomingCallMessage = _.template("<%= user %> is calling you.<br /><a class='btn btn-success' id='answercall-<%=id %>'>Answer</a> <a class='btn btn-warning' id='hangupcall-<%=id %>'>Hangup</a>", { user:model.prettyName(), id:call.id});
                            var messageBox = self.message("Incoming call", incomingCallMessage, self.hangupTimeout);
                            $("#answercall-" + call.id).on("click", function () {
                                call.answer();
                                messageBox.remove();
                            });
                            $("#hangupcall-" + call.id).on("click", function () {
                                call.hangup();
                                messageBox.remove();
                            });
                            /*setTimeout(function(){
                             call.hangup();
                             }, self.hangupTimeout);*/
                        }
                    });
                },
                onError:function (event) {
                    self.message("Phone error: ", e.reason, 3000);
                }
            }
        });

        var ringBox = null;
        var answerBox = null;
        $("#phono-call").click(function () {

        });
    },

    message:function (title, message, timeout) {
        //var _timeout = timeout || 3000;
        var timestamp = new Date().getTime();
        var idMessage = "phono-message" + timestamp;
        var tpl = '<div style="min-width: 200px;" id="<%= idMessage %>" class="alert alert-info fade in" data-alert="alert"><a class="close" data-dismiss="alert">×</a><p><strong><%=   alert %></strong> <%=   message %></p></div>';

        var left = ($(window).width() / 2 - 100);
        $("#phono-messages").css("left", left);
        $("#phono-messages").append(_.template(tpl, { alert:title, message:message, idMessage:idMessage}));
        if (timeout != undefined) {
            setTimeout(function () {
                $("#phono-message" + timestamp).remove();
            }, timeout);
        }
        return $("#" + idMessage);
    }


});