var ImageTabsView = Backbone.View.extend({
    tagName : "div",
    images : null, //array of images that are printed
    idProject : null,
    searchPanel : null,
    initialize: function(options) {
        this.idProject = options.idProject;
        this.container = options.container;
        this.listproject = "listimage"+this.idProject;
        this.pageproject = "pagerimage"+this.idProject;
        this.tab = 2;
        this.timeoutHnd = null
    },
    refresh : function() {
    },
    render : function () {
        var self = this;
        self.model.fetch({
            success : function (collection, response) {

                //print data from project image table
                var tbody = $('#projectImageTable'+self.idProject).find("tbody");
                var exploreButtonTpl = "<a href='#tabs-image-<%= project %>-<%= image %>-' class='btn btn-primary' style='color : #FFF'';><i class='icon-eye-open icon-white'></i> Explore</a>";
                var thumbImgTpl = "<img class='lazy' src='<%= thumb %>' alt='<%= filename %>' style='max-height: 75px;'/>";
                var rowTpl = "<tr><td><%= thumImg %></td><td><%= filename %></td><td><%= mime %></td><td><%= width %></td><td><%= height %></td><td><%= magnification %></td><td><%= resolution %></td><td><%= numberOfAnnotations %></td><td><%= created %></td><td><%= action %></td></tr>";
                collection.each(function (image) {
                    var exploreButton = _.template(exploreButtonTpl,{ project : self.idProject, image : image.get("id")});
                    var thumImg = _.template(thumbImgTpl,{ thumb : image.get("thumb"), filename : image.get("filename")});
                    image.set({"action" : exploreButton});
                    image.set({"thumImg" : thumImg});
                    image.set({"created":window.app.convertLongToDate(image.get("created"))});
                    tbody.append(_.template(rowTpl, image.toJSON()));
                });
                $('#projectImageTable'+self.idProject).dataTable( {
                    "sDom": "<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sLengthMenu": "_MENU_ records per page"
                    }
                    /*"bProcessing": true,
                     "bServerSide": true,
                     "sAjaxSource":  self.model.url() + "?dataTables=true"*/
                });

                $('#projectImageListing'+self.idProject).hide();
                $('#projectImageTable'+self.idProject).show();
            }
        });
        return this;
    }
});
