var ImageReviewAction = Backbone.View.extend({
    tagName: "div",
    initialize: function (options) {
        this.el = options.el;
        this.model = options.model;
        this.container  = options.container;
    },
    configureAction: function () {
        var self = this;
        var el = $(self.el);
        el.find("#exploreButton" + self.model.id).click(function () {
            window.location = '#tabs-image-' + self.model.get('project') + '-' + self.model.get('id') + '-';
        });

        if (self.isNotReviewed()) {
            
            el.find("#explore" + self.model.id).show();
            el.find("#review" + self.model.id).hide();
            el.find("#startreview" + self.model.id).show();
            el.find("#cancelreview" + self.model.id).hide();
            el.find("#validateimage" + self.model.id).hide();
            el.find("#unvalidateimage" + self.model.id).hide();
            el.find("#moreinfo" + self.model.id).show();
        } else if (self.isInReviewing()) {
            el.find("#explore" + self.model.id).show();
            el.find("#review" + self.model.id).show();
            el.find("#startreview" + self.model.id).hide();
            if (self.model.get('numberOfReviewedAnnotations') == 0) {
                el.find("#cancelreview" + self.model.id).show();
            }
            else {
                el.find("#cancelreview" + self.model.id).hide();
            }
            el.find("#validateimage" + self.model.id).show();
            el.find("#unvalidateimage" + self.model.id).hide();
            el.find("#moreinfo" + self.model.id).show();
        } else {
            el.find("#explore" + self.model.id).show();
            el.find("#review" + self.model.id).show();
            el.find("#startreview" + self.model.id).hide();
            el.find("#cancelreview" + self.model.id).hide();
            el.find("#validateimage" + self.model.id).hide();
            el.find("#unvalidateimage" + self.model.id).show();
            el.find("#moreinfo" + self.model.id).show();
        }

        el.find("#startreview" + self.model.id).on("click", function () {
            self.startReviewing();
            return false;
        });
        el.find("#cancelreview" + self.model.id).on("click", function () {
            self.cancelReviewing();
            return false;
        });
        el.find("#validateimage" + self.model.id).on("click", function () {
            self.validateImage();
            return false;
        });
        el.find("#unvalidateimage" + self.model.id).on("click", function () {
            self.cancelReviewing();
            return false;
        });
        //el.find("#image-properties-" + self.model.id).html(_.template(tplProperties, self.model.toJSON()));
        $("a.moreinfo" + self.model.id).live("click", function () {
            $("#image-properties").remove();
            new ImagePropertiesView({model: self.model}).render();
            return false;
        });
    },
    startReviewing: function () {
        var self = this;
        console.log("startReviewing");
        new ImageReviewModel({id: self.model.id}).save({}, {
            success: function (model, response) {
                //window.location = "#tabs-images-"+self.model.get('project');
//                window.app.controllers.dashboard.view.projectDashboardImages.refreshImagesThumbs();
                window.app.view.message("Image", response.message, "success");
                self.model = new ImageModel(response.imageinstance);
                self.container.refresh();
                window.location = '#tabs-review-' + self.model.get('project') + '-' + self.model.get('id') + '-';
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors, "error");
            }});
    },
    cancelReviewing: function () {
        var self = this;
        console.log("cancelReviewing");
        new ImageReviewModel({id: self.model.id, cancel: true}).destroy({
            success: function (model, response) {
                //window.location = "#tabs-images-"+self.model.get('project');
//                window.app.controllers.dashboard.view.projectDashboardImages.refreshImagesThumbs();
                window.app.view.message("Image", response.message, "success");
                console.log(response);
                self.model = new ImageModel(response.imageinstance);
                self.container.refresh();
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors, "error");
            }});
    },
    validateImage: function () {
        var self = this;
        console.log("validateImage");
        new ImageReviewModel({id: self.model.id}).destroy({
            success: function (model, response) {
                window.app.view.message("Image", response.message, "success");
                console.log(response);
                self.model = new ImageModel(response.imageinstance);
                self.container.refresh();
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors, "error");
            }});
    },
    isNotReviewed: function () {
        return this.model.get("reviewStart") == null
    },
    isInReviewing: function () {
        return this.model.get("reviewStart") != null && this.model.get("reviewStop") == null
    }
});
