var ImageTabsView = Backbone.View.extend({
    tagName: "div",
    images: null, //array of images that are printed
    idProject: null,
    searchPanel: null,
    initialize: function (options) {
        this.idProject = options.idProject;
        this.container = options.container;
    },
    refresh: function () {

    },
    render : function() {
        var self = this;
        require(["text!application/templates/image/ImageReviewAction.tpl.html"], function (actionMenuTpl) {
            self.doLayout(actionMenuTpl)
        });
        return this;
    },
    doLayout: function (actionMenuTpl) {
        var self = this;
        var table = $("#imageProjectTable" + self.idProject);
        var body = $("#imageProjectArray" + self.idProject);
        self.imagesdDataTables = table.dataTable({
            "bProcessing": true,
            "bServerSide": true,
            "bSearch" : false,
            "sAjaxSource": new ImageInstanceCollection({project: this.idProject}).url(),
            "fnServerParams": function ( aoData ) {
                aoData.push( { "name": "datatables", "value": "true" } );
            },
            "aoColumns": [
                { "mDataProp": "id", "fnRender" : function (o, id ) {
                    return id;
                }},
                { "mDataProp": "thumb", "fnRender" : function(o, thumb) {
                    return _.template("<div style='width : 130px;'><a href='#tabs-image-<%= project %>-<%=  id  %>-'><img src='<%= thumb %>' alt='originalFilename' style='max-height : 45px;max-width : 128px;'/></a></div>", o.aData);
                }, "bSearchable": true},
                { "mDataProp": "originalFilename", "fnRender" : function (o, originalFilename) {
                    var imageInstanceModel = new ImageInstanceModel({});
                    imageInstanceModel.set(o.aData);
                    return imageInstanceModel.getVisibleName(window.app.status.currentProjectModel.get('blindMode'))
                }},
                { "mDataProp": "width" },
                { "mDataProp": "height" },
                { "mDataProp": "magnification", "fnRender" : function(o, magnification) {
                    return magnification + "X";
                }},
                { "mDataProp": "resolution", "fnRender" : function(o, resolution) {
                    return resolution.toFixed(3) + " µm/pixel";
                }},
                { "mDataProp": "numberOfAnnotations" },
                { "mDataProp": "numberOfJobAnnotations" },
                { "mDataProp": "numberOfReviewedAnnotations" },
                { "mDataProp": "mime" },
                { "mDataProp": "created", "fnRender" : function (o, created) {
                    return window.app.convertLongToDate(created);
                }} ,
                { "mDataProp": "inReview", "fnRender" : function (o, inReview) {
                    if (inReview) {
                        return '<span class="label label-warning">In review</span>';
                    } else {
                        return '<span class="label label-info">None</span>';
                    }
                }},
                { "mDataProp": "updated", "fnRender" : function(o, useless) {
                    new ImageInstanceModel({ id : o.aData.id}).fetch({
                        success : function (model, response) {
                            var action = new ImageReviewAction({el:body,model:model, container : self});
                            action.configureAction();
                        }
                    });
                    return _.template(actionMenuTpl, o.aData);

                }}
            ]
        });
        $('#projectImageListing' + self.idProject).hide();
        $('#projectImageTable' + self.idProject).show();
    }
});
