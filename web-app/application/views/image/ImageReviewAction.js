var ImageReviewAction = Backbone.View.extend({
    tagName: "div",
    initialize: function (options) {
        this.container = options.container;
    },
    configureAction: function () {
        var self = this;
        $(self.container.el).find("#exploreButton" + self.container.model.id).click(function () {
            window.location = '#tabs-image-' + self.container.model.get('project') + '-' + self.container.model.get('id') + '-';
        });

        if (self.isNotReviewed()) {
            $(self.container.el).find("#explore" + self.container.model.id).show();
            $(self.container.el).find("#review" + self.container.model.id).hide();
            $(self.container.el).find("#startreview" + self.container.model.id).show();
            $(self.container.el).find("#cancelreview" + self.container.model.id).hide();
            $(self.container.el).find("#validateimage" + self.container.model.id).hide();
            $(self.container.el).find("#unvalidateimage" + self.container.model.id).hide();
            $(self.container.el).find("#moreinfo" + self.container.model.id).show();
        } else if (self.isInReviewing()) {
            $(self.container.el).find("#explore" + self.container.model.id).show();
            $(self.container.el).find("#review" + self.container.model.id).show();
            $(self.container.el).find("#startreview" + self.container.model.id).hide();
            if (self.container.model.get('numberOfReviewedAnnotations') == 0) {
                $(self.container.el).find("#cancelreview" + self.container.model.id).show();
            }
            else {
                $(self.container.el).find("#cancelreview" + self.container.model.id).hide();
            }
            $(self.container.el).find("#validateimage" + self.container.model.id).show();
            $(self.container.el).find("#unvalidateimage" + self.container.model.id).hide();
            $(self.container.el).find("#moreinfo" + self.container.model.id).show();
        } else {
            $(self.container.el).find("#explore" + self.container.model.id).show();
            $(self.container.el).find("#review" + self.container.model.id).show();
            $(self.container.el).find("#startreview" + self.container.model.id).hide();
            $(self.container.el).find("#cancelreview" + self.container.model.id).hide();
            $(self.container.el).find("#validateimage" + self.container.model.id).hide();
            $(self.container.el).find("#unvalidateimage" + self.container.model.id).show();
            $(self.container.el).find("#moreinfo" + self.container.model.id).show();
        }

        $(self.container.el).find("#startreview" + self.container.model.id).on("click", function () {
            self.startReviewing();
            return false;
        });
        $(self.container.el).find("#cancelreview" + self.container.model.id).on("click", function () {
            self.cancelReviewing();
            return false;
        });
        $(self.container.el).find("#validateimage" + self.container.model.id).on("click", function () {
            self.validateImage();
            return false;
        });
        $(self.container.el).find("#unvalidateimage" + self.container.model.id).on("click", function () {
            self.cancelReviewing();
            return false;
        });
    },
    startReviewing: function () {
        var self = this;
        console.log("startReviewing");
        new ImageReviewModel({id: self.container.model.id}).save({}, {
            success: function (model, response) {
                //window.location = "#tabs-images-"+self.model.get('project');
//                window.app.controllers.dashboard.view.projectDashboardImages.refreshImagesThumbs();
                window.app.view.message("Image", response.message, "success");
                self.container.model = new ImageModel(response.imageinstance);
                self.container.render();
                window.location = '#tabs-review-' + self.container.model.get('project') + '-' + self.container.model.get('id') + '-';
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors, "error");
            }});
    },
    cancelReviewing: function () {
        var self = this;
        console.log("cancelReviewing");
        new ImageReviewModel({id: self.container.model.id, cancel: true}).destroy({
            success: function (model, response) {
                //window.location = "#tabs-images-"+self.model.get('project');
//                window.app.controllers.dashboard.view.projectDashboardImages.refreshImagesThumbs();
                window.app.view.message("Image", response.message, "success");
                console.log(response);
                self.container.model = new ImageModel(response.imageinstance);
                self.container.render();
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors, "error");
            }});
    },
    validateImage: function () {
        var self = this;
        console.log("validateImage");
        new ImageReviewModel({id: self.container.model.id}).destroy({
            success: function (model, response) {
                window.app.view.message("Image", response.message, "success");
                console.log(response);
                self.container.model = new ImageModel(response.imageinstance);
                self.container.render();
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors, "error");
            }});
    },
    isNotReviewed: function () {
        return this.container.model.get("reviewStart") == null
    },
    isInReviewing: function () {
        return this.container.model.get("reviewStart") != null && this.container.model.get("reviewStop") == null
    }
});
