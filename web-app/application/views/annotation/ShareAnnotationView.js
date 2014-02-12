var ShareAnnotationView = Backbone.View.extend({
    tagName: "div",
    initialize: function (options) {
        this.image = options.image;
        this.project = options.project;
        this.dialog = null;
    },

    doLayout: function (shareAnnotationViewTpl) {
        var self = this;
        this.model.set({ "username": window.app.view.getUserNameById(this.model.get("user"))});
        this.model.set({ "terms": "undefined"});
        this.dialog = new ConfirmDialogView({
            el: '#dialogs',
            template: _.template(shareAnnotationViewTpl, this.model.toJSON()),
            dialogAttr: {
                dialogID: "#share-confirm"
            }
        }).render();


        var selectUser = $("#selectUserShare" + self.model.id);
        var comments = $("#comments" + self.model.id);
        var shareWithOption = $("input[name=shareWithOption]");

        new AnnotationCommentCollection({ annotation: self.model.id}).fetch({
            success: function (collection, response) {
                var commentTpl = "<ul><li><b>( <%= hour %> )</b> <%= sender %> : <i><%= comment %></i></li></ul>";
                var lastDateString = undefined;
                collection.each(function (model) {
                    var date = new Date();
                    date.setTime(model.get("created"));
                    var dateString = date.toLocaleDateString();

                    if (dateString != lastDateString) {
                        comments.append(_.template("<h4><%= date %></h4>", {date: dateString}));
                        lastDateString = dateString
                    }
                    var hours = (date.getHours() < 10) ? "0" + date.getHours() : date.getHours();
                    var minutes = (date.getMinutes() < 10) ? "0" + date.getMinutes() : date.getMinutes();
                    comments.append(_.template(commentTpl, {
                        sender: model.get("sender"),
                        comment: model.get("comment"),
                        hour: hours + "h" + minutes
                    }));
                });
            },
            error: function (collection, response) {
                //window.app.view.message("Error", response.message, "error")
            }
        });


        var usersId = [];
        window.app.models.projectUser.each(function(user) {
            usersId.push({id:user.id,label:user.prettyName()});
        });

        self.userMaggicSuggest = selectUser.magicSuggest({
            data: usersId,
            displayField: 'label',
            value: [],
            width: 300,
            maxSelection:null
        });
        $("#selectUserShare" + self.model.id).hide();


        var getSelectedUsers = function () {
            var value = $("input[name=shareWithOption]:checked").val();
            if (value == "everyone") {
                var users = []
                window.app.models.projectUser.each(function (user) {
                    users.push(user.get("id"));
                });
                return users;
            } else if (value == "somebody") {
                return self.userMaggicSuggest.getValue();
            } else return null;
        }

        shareWithOption.change(function () {
            var value = $(this).val();
            if (value == "everyone") {
                $("#selectUserShare" + self.model.id).hide();
                $("#mailShare" + self.model.id).hide();
            } else if (value == "somebody") {
                $("#mailShare" + self.model.id).hide();
                $("#selectUserShare" + self.model.id).show();
            } else if (value == "email") {
                $("#mailShare" + self.model.id).show();
                $("#selectUserShare" + self.model.id).hide();
            }
        });

        $("#shareCancelButton" + self.model.id).click(function (event) {
            event.preventDefault();
            self.dialog.close();
            window.history.back();
            return false;
        });

        $("#shareButton" + self.model.id).click(function (event) {
            event.preventDefault();
            var shareButton = $(this);
            shareButton.html("Sending...");

            var users = getSelectedUsers();
            var emails = null;
            if (users) {
                var userName = (_.size(users) == 1) ? window.app.models.projectUser.get(users[0]).prettyName() : "user";
            } else { //unexisting user, mail entered
                emails = $("#mailShare" + self.model.id).val();
            }
            var comment = $("#annotationComment" + self.model.id).val();
            var shareAnnotationURL = _.template("<%= serverURL %>/#share-annotation/<%= id %>", {
                serverURL: window.app.status.serverURL,
                id: self.model.id
            });
            var annotationURL = _.template("<%= serverURL %>/#tabs-image-<%= idProject %>-<%= idImage %>-<%= idAnnotation %>",
                {   serverURL: window.app.status.serverURL,
                    idProject: self.project,
                    idImage: self.image,
                    idAnnotation: self.model.id
                });
            var subject = _.template("Cytomine : <%= from %> shared an annotation with you", { from: window.app.models.projectUser.get(window.app.status.user.id).prettyName()});
            new AnnotationCommentModel({
                annotation: self.model.id
            }).save({
                    users: users,
                    emails : emails,
                    from: window.app.models.projectUser.get(window.app.status.user.id).prettyName(),
                    to: userName,
                    comment: comment,
                    annotationURL: annotationURL,
                    shareAnnotationURL: shareAnnotationURL,
                    by: window.app.status.serverURL,
                    comment: comment,
                    subject: subject,
                    annotationURL: annotationURL,
                    cid : "annotation" + self.model.id
                }, {
                    success: function (model, response) {
                        shareButton.html("Share");
                        window.app.view.message("Success", response.message, "success");
                        self.dialog.close();
                        window.history.back();
                    },
                    error: function (model, response) {
                        shareButton.html("Share");
                        window.app.view.message("Error", response.message, "error");
                    }
                });
            return false;
        });

        return this;
    },
    render: function () {
        var self = this;
        require(["text!application/templates/annotation/ShareAnnotationView.tpl.html"], function (shareAnnotationViewTpl, shareAnnotationMailTpl) {
            self.doLayout(shareAnnotationViewTpl);
        });
    }
});