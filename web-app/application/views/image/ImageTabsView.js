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
        var columns = [
            { sClass: 'center', "mData": "id", "bSearchable": false},
            { "mData": "baseImage.thumbURL", sDefaultContent: "", "bSearchable": false, "fnRender" : function (o) {
                return _.template("<div style='width : 130px;'><a href='#tabs-image-<%= project %>-<%=  id  %>-'><img src='<%= thumb %>' alt='originalFilename' style='max-height : 45px;max-width : 128px;'/></a></div>",
                    {
                        project : self.idProject,
                        id : o.aData.id,
                        thumb : o.aData["baseImage.thumbURL"]
                    });
            }},
            { "mDataProp": "baseImage.originalFilename", sDefaultContent: "", "bSearchable": true, "fnRender" : function (o) {
                //var imageInstanceModel = new ImageInstanceModel({});
                // imageInstanceModel.set(o.aData);
                // return imageInstanceModel.getVisibleName(window.app.status.currentProjectModel.get('blindMode'))
                return o.aData["baseImage.originalFilename"];
            }}
            ,
            { "mDataProp": "baseImage.width", sDefaultContent: "", "bSearchable": false, "fnRender" : function(o) {
                return o.aData["baseImage.width"] + " px";
            }},
            { "mDataProp": "baseImage.height", sDefaultContent: "", "bSearchable": false, "fnRender" : function(o) {
                return o.aData["baseImage.height"] + " px";
            } },
            { "mDataProp": "baseImage.magnification", sDefaultContent: "", "bSearchable": false, "fnRender" : function(o) {
                return o.aData["baseImage.magnification"] + " X";
            }},
            { "mDataProp": "baseImage.resolution", sDefaultContent: "", "bSearchable": false, "fnRender" : function(o) {
                var resolution = o.aData["baseImage.resolution"];
                try {
                    return resolution.toFixed(3) + " µm/pixel";
                }catch(e) {return "";}
            }},
            { "mDataProp": "countImageAnnotations", "bSearchable": false },
            { "mDataProp": "countImageJobAnnotations", "bSearchable": false },
            { "mDataProp": "countImageReviewedAnnotations", "bSearchable": false },
            { "mDataProp": "baseImage.mime.extension", sDefaultContent: "", "bSearchable": true, "fnRender" : function(o) {
                return o.aData["baseImage.mime.extension"];
            } },
            { "mDataProp": "created", sDefaultContent: "", "bSearchable": false, "fnRender" : function (o, created) {
                return window.app.convertLongToDate(created);
            }} ,
            { "mDataProp": "reviewStart", sDefaultContent: "", "bSearchable": false, "fnRender" : function (o) {
                if (o.aData.reviewStart) {
                    return '<span class="label label-success">Reviewed</span>';
                }
                //else if (inReview) {
                // return '<span class="label label-warning">In review</span>';
                // } else {
                //    return '<span class="label label-info">None</span>';
                //}
            }},
            { "mDataProp": "updated", sDefaultContent: "", "bSearchable": false, "fnRender" : function(o) {
                new ImageInstanceModel({ id : o.aData.id}).fetch({
                    success : function (model, response) {
                        var action = new ImageReviewAction({el:body,model:model, container : self});
                        action.configureAction();
                    }
                });
                o.aData["project"]  = self.idProject;
                return _.template(actionMenuTpl, o.aData);

            }}
        ];
        self.imagesdDataTables = table.dataTable({
            "bProcessing": true,
            "bServerSide": true,
            "sAjaxSource": new ImageInstanceCollection({project: this.idProject}).url(),
            "fnServerParams": function ( aoData ) {
                aoData.push( { "name": "datatables", "value": "true" } );
            },
            "aoColumns" : columns
        });
        $('#projectImageListing' + self.idProject).hide();
        $('#projectImageTable' + self.idProject).show();
    }
});
