var ImageTabsView = Backbone.View.extend({
    tagName: "div",
    images: null, //array of images that are printed
    idProject: null,
    searchPanel: null,
    initialize: function (options) {
        this.idProject = options.idProject;
        this.container = options.container;
        this.listproject = "listimage" + this.idProject;
        this.pageproject = "pagerimage" + this.idProject;
        this.tab = 2;
        this.timeoutHnd = null
    },
    refresh: function () {
    },
    render: function () {
        var self = this;
        self.model.fetch({
            success: function (collection, response) {

                //print data from project image table
                var tbody = $('#projectImageTable' + self.idProject).find("tbody");


                require(["text!application/templates/image/ImageReviewAction.tpl.html"], function (tplReviewAction) {

                    var thumbImgTpl = "<img class='lazy' src='<%= thumb %>' alt='<%= filename %>' style='max-height: 75px;'/>";
                    var rowTpl = "<tr><td><%= thumImg %></td><td><%= originalFilename %></td><td><%= mime %></td><td><%= width %></td><td><%= height %></td><td><%= magnification %></td><td><%= resolution %></td><td><%= numberOfAnnotations %></td><td><%= numberOfJobAnnotations %></td><td><%= created %></td><td><%= action %></td></tr>";
                    collection.each(function (image) {
                        var thumImg = _.template(thumbImgTpl, { thumb: image.get("thumb"), filename: image.get("originalFilename")});
                        image.set({"action": _.template(tplReviewAction, image.toJSON())});
                        image.set({"thumImg": thumImg});
                        image.set({"resolution": image.get("resolution").toFixed(2)});
                        image.set({"created": window.app.convertLongToDate(image.get("created"))});
                        tbody.append(_.template(rowTpl, image.toJSON()));
                        var action = new ImageReviewAction({container:{el:tbody,model:image}});
                        action.configureAction();
                    });
                    $('#projectImageTable' + self.idProject).dataTable({
                        "sDom": "<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>>",
                        "sPaginationType": "bootstrap",
                        "oLanguage": {
                            "sLengthMenu": "_MENU_ records per page"
                        },
                        "aaSorting": [[ 9, "desc" ]]
                        /*"bProcessing": true,
                         "bServerSide": true,
                         "sAjaxSource":  self.model.url() + "?dataTables=true"*/
                    });

                    $('#projectImageListing' + self.idProject).hide();
                    $('#projectImageTable' + self.idProject).show();

                });

            }
        });
        return this;
    }
});
