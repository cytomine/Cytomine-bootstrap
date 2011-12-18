var ShareAnnotationView = Backbone.View.extend({
    tagName : "div",
    initialize: function(options) {
        this.image = options.image;
        this.project = options.project;
    },

    doLayout: function(shareAnnotationViewTpl, shareAnnotationMailTpl) {
        var self = this;
        this.model.set({ "username" :  window.app.models.users.get(this.model.get("user")).prettyName()});
        this.model.set({ "terms" :  "undefined"});
        var dialog = new ConfirmDialogView({
            el:'#dialogs',
            template : _.template(shareAnnotationViewTpl, this.model.toJSON()),
            dialogAttr : {
                dialogID : "#share-confirm"
            }
        }).render();

        var selectUser = $("#selectUserShare" + self.model.id);

        var userCollection = new UserCollection({project : this.project}).fetch({
            success : function (collection, response) {
                userCollection = collection;
                var optionTpl = "<option value='<%= id %>'><%= name %></option>"
                collection.each(function (user) {
                    selectUser.append(_.template(optionTpl, { id : user.id, name : user.prettyName()}));
                });
            },
            error : function(collection, response) {
                window.app.view.message("Error", response.message, "error")
            }
        });

        $("#shareCancelButton"+self.model.id).click(function(){
            self.close();
            return false;
        });

        $("#shareButton"+self.model.id).click(function(){
            var shareButton = $(this);
            shareButton.html("Sending...");
            var sendMail = function(imageData) {
                var user = selectUser.val();
                var comment = $("#annotationComment"+self.model.id).val();

                var annotationURL = _.template(window.app.status.serverURL+"/#tabs-image-<%= idProject %>-<%= idImage %>-<%= idAnnotation %>",
                    { idProject : self.project,
                        idImage : self.image,
                        idAnnotation : self.model.id
                    });
                var message = _.template(shareAnnotationMailTpl, {
                    from : userCollection.get(window.app.status.user.id).prettyName(),
                    to : userCollection.get(user).prettyName(),
                    comment : comment,
                    annotationURL : annotationURL,
                    imageData : imageData,
                    by : window.app.status.serverURL
                });
                var subject = _.template("Cytomine : <%= from %> shared an annotation with you",{ from : userCollection.get(window.app.status.user.id).prettyName()});
                new ShareAnnotationModel().save({
                    user : user,
                    annotation : self.model.id,
                    message : message,
                    comment : comment,
                    subject : subject,
                    annotationURL : annotationURL
                }, {
                    success : function (model, response) {
                        shareButton.html("Share");
                        window.app.view.message("Success", response.message, "success");
                        self.close();
                    },
                    error : function (model, response) {
                        shareButton.html("Share");
                        window.app.view.message("Error", response.message, "error");
                    }
                });

            }
            var canvas = document.getElementById("imageDrawer" + self.model.id);
            var context = canvas.getContext("2d");
            // load image from data url
            var imageObj = new Image();
            imageObj.onload = function(){
                canvas.width = imageObj.width;
                canvas.height = imageObj.height;
                context.drawImage(this, 0, 0);
                sendMail(canvas.toDataURL());
            };
            imageObj.src = self.model.get("cropURL");

            return false;
        });

        return this;
    },
    render: function() {
        var self = this;
        require(["text!application/templates/annotation/ShareAnnotationView.tpl.html",
            "text!application/templates/annotation/ShareAnnotationMail.tpl.html"], function(shareAnnotationViewTpl, shareAnnotationMailTpl) {
            self.doLayout(shareAnnotationViewTpl, shareAnnotationMailTpl);
        });
    },
    close : function() {
        $("#share-confirm").modal('hide');
        $("#share-confirm").remove();
        window.history.back();
    }
});