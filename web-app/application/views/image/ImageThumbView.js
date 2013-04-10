var ImageThumbView = Backbone.View.extend({
    className: "thumb-wrap",
    tplProperties: null,

    events: {

    },

    initialize: function (options) {
        this.id = "thumb" + this.model.get('id');
        _.bindAll(this, 'render');
    },

    render: function () {
        this.model.set({ project: window.app.status.currentProject });
        var self = this;
        require(["text!application/templates/image/ImageThumb.tpl.html", "text!application/templates/image/ImageThumbProperties.tpl.html","text!application/templates/image/ImageReviewAction.tpl.html"], function (tpl, tplProperties,tplReviewAction) {
            self.tplProperties = tplProperties;
            var filename = self.model.getVisibleName();
            self.model.set('originalFilename',filename);
            var title = (filename.length < 27) ? filename : filename.substr(0, 24) + "...";
            var resolution = Math.round(1000 * self.model.get('resolution')) / 1000; //round to third decimal
            self.model.set({title: title, resolution: resolution});
            $(self.el).html(_.template(tpl, self.model.toJSON()));

            $(self.el).find("#image-properties-" + self.model.id).html(_.template(tplProperties, self.model.toJSON()));
            $(self.el).find("#moreinfo" + self.model.id).on("click", function () {
                $("#image-properties").remove();
                new ImagePropertiesView({model: self.model}).render();
                return false;
            });
            $(self.el).find('#imagereviewaction-thumb-'+self.model.id).append(_.template(tplReviewAction, self.model.toJSON()));
            self.addReviewInfo();
            self.configureAction();

               //

        });

        return this;
    },
    isNotReviewed: function () {
        return this.model.get("reviewStart") == null
    },
    isInReviewing: function () {
        return this.model.get("reviewStart") != null && this.model.get("reviewStop") == null
    },
    addReviewInfo: function () {
        var self = this;
        var reviewingDiv = $(self.el).find("#reviewingImageData" + self.model.id);
        if (self.isNotReviewed()) {
            reviewingDiv.append('<span class="label">Not yet reviewed</span>');
        } else if (self.isInReviewing()) {
            reviewingDiv.append('<span class="label label-warning">' + window.app.models.projectUser.get(self.model.get("reviewUser")).prettyName() + ' starts reviewing</span>');
        } else {
            reviewingDiv.append('<span class="label label-success">' + window.app.models.projectUser.get(self.model.get("reviewUser")).prettyName() + ' has reviewed</span>');
        }
    },

    refresh: function () {
        this.render();
    },
    configureAction: function () {
        var action = new ImageReviewAction({container:this});
        action.configureAction();
    }
});

