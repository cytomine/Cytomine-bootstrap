var ImageThumbView = Backbone.View.extend({
    className:"thumb-wrap",
    tplProperties:null,

    events:{

    },

    initialize:function (options) {
        this.id = "thumb" + this.model.get('id');
        _.bindAll(this, 'render');
    },

    render:function () {
        this.model.set({ project:window.app.status.currentProject });
        var self = this;
        require(["text!application/templates/image/ImageThumb.tpl.html", "text!application/templates/image/ImageThumbProperties.tpl.html"], function (tpl, tplProperties) {
            self.tplProperties = tplProperties;
            var filename = self.model.get('filename');
            var title = (filename.length < 27) ? filename : filename.substr(0, 24) + "...";
            var resolution = Math.round(1000 * self.model.get('resolution')) / 1000; //round to third decimal
            self.model.set({title:title, resolution:resolution});
            $(self.el).html(_.template(tpl, self.model.toJSON()));
            $(self.el).find("#image-properties-" + self.model.id).html(_.template(tplProperties, self.model.toJSON()));
            $(self.el).find("#moreinfo" + self.model.id).on("click", function () {
                $("#image-properties").remove();
                new ImagePropertiesView({model:self.model}).render();
                return false;
            });
            self.addReviewInfo();
            self.configureAction();
        });

        return this;
    },
    isNotReviewed : function() {
        return this.model.get("reviewStart")==null
    },
    isInReviewing : function() {
        return this.model.get("reviewStart")!=null && this.model.get("reviewStop")==null
    },
    addReviewInfo : function() {
        var self = this;
        var reviewingDiv = $(self.el).find("#reviewingImageData"+self.model.id);
        if(self.isNotReviewed()) {
            reviewingDiv.append('<span class="label">Not yet reviewed</span>');
        } else if(self.isInReviewing()) {
            reviewingDiv.append('<span class="label label-warning">'+window.app.models.projectUser.get(self.model.get("reviewUser")).prettyName()+' starts reviewing</span>');
        } else {
            reviewingDiv.append('<span class="label label-success">'+window.app.models.projectUser.get(self.model.get("reviewUser")).prettyName()+' has reviewed</span>');
        }
    },
    configureAction : function() {
        var self = this;
        $(self.el).find("#exploreButton"+self.model.id).click(function() {
            window.location = '#tabs-image-'+self.model.get('project')+'-'+self.model.get('id')+'-';
        });

        if(self.isNotReviewed()) {
            $(self.el).find("#explore"+self.model.id).show();
            $(self.el).find("#review"+self.model.id).hide();
            $(self.el).find("#startreview"+self.model.id).show();
            $(self.el).find("#cancelreview"+self.model.id).hide();
            $(self.el).find("#validateimage"+self.model.id).hide();
            $(self.el).find("#unvalidateimage"+self.model.id).hide();
            $(self.el).find("#moreinfo"+self.model.id).show();
        } else if(self.isInReviewing()) {
            $(self.el).find("#explore"+self.model.id).show();
            $(self.el).find("#review"+self.model.id).show();
            $(self.el).find("#startreview"+self.model.id).hide();
            if(self.model.get('numberOfReviewedAnnotations')==0) $(self.el).find("#cancelreview"+self.model.id).show();
            else $(self.el).find("#cancelreview"+self.model.id).hide();
            $(self.el).find("#validateimage"+self.model.id).show();
            $(self.el).find("#unvalidateimage"+self.model.id).hide();
            $(self.el).find("#moreinfo"+self.model.id).show();
        } else {
            $(self.el).find("#explore"+self.model.id).show();
            $(self.el).find("#review"+self.model.id).show();
            $(self.el).find("#startreview"+self.model.id).hide();
            $(self.el).find("#cancelreview"+self.model.id).hide();
            $(self.el).find("#validateimage"+self.model.id).hide();
            $(self.el).find("#unvalidateimage"+self.model.id).show();
            $(self.el).find("#moreinfo"+self.model.id).show();
        }

        $(self.el).find("#startreview"+self.model.id).on("click", function () {
            self.startReviewing();
            return false;
        });
        $(self.el).find("#cancelreview"+self.model.id).on("click", function () {
            self.cancelReviewing();
            return false;
        });
        $(self.el).find("#validateimage"+self.model.id).on("click", function () {
            self.validateImage();
            return false;
        });
        $(self.el).find("#unvalidateimage"+self.model.id).on("click", function () {
            self.cancelReviewing();
            return false;
        });
    },
    refresh:function () {
        this.render();
    },
    startReviewing : function() {
        var self = this;
        console.log("startReviewing");
        new ImageReviewModel({id:self.model.id}).save({}, {
            success:function (model, response) {
                //window.location = "#tabs-images-"+self.model.get('project');
//                window.app.controllers.dashboard.view.projectDashboardImages.refreshImagesThumbs();
                window.app.view.message("Image", response.message, "success");
                self.model = new ImageModel(response.imageinstance);
                self.render();
            },
            error:function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors, "error");
            }});
    },
    cancelReviewing : function() {
        var self = this;
        console.log("cancelReviewing");
        new ImageReviewModel({id:self.model.id, cancel:true}).destroy({
            success:function (model, response) {
                //window.location = "#tabs-images-"+self.model.get('project');
//                window.app.controllers.dashboard.view.projectDashboardImages.refreshImagesThumbs();
                window.app.view.message("Image", response.message, "success");
                console.log(response);
                self.model = new ImageModel(response.imageinstance);
                self.render();
            },
            error:function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors, "error");
            }});
    },
    validateImage : function() {
        var self = this;
        console.log("validateImage");
        new ImageReviewModel({id:self.model.id}).destroy({
            success:function (model, response) {
                window.app.view.message("Image", response.message, "success");
                console.log(response);
                self.model = new ImageModel(response.imageinstance);
                self.render();
            },
            error:function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Image", json.errors, "error");
            }});
    }
});

