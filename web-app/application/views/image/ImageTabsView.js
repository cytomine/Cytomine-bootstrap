var ImageInstanceDataSource = function (options) {
    this._formatter = options.formatter;
    this._columns = options.columns;
    this._delay = options.delay || 0;
    this._collection = options.collection;
    this._container = options.container;
};

ImageInstanceDataSource.prototype = {

    columns: function () {
        return this._columns;
    },

    data: function (options, callback) {
        var self = this;
		console.log("options.pageSize"+options.pageSize);
        options.pageSize = options.pageSize || 10;

        setTimeout(function () {
            var data = $.extend(true, [], self._data);

            // SEARCHING
            if (options.search) {
                self._collection.server_api.search = options.search;
            } else {
				delete self._collection.server_api.search;
			}

            // SORTING
            if (options.sortProperty) {
                self._collection.server_api.sortColumn = options.sortProperty;
                self._collection.server_api.sortDirection = options.sortDirection;
            }

            // PAGING
            var startIndex = options.pageIndex * options.pageSize;
            var endIndex = startIndex + options.pageSize;

            self._collection.server_api.offset = startIndex;
            self._collection.server_api.max = endIndex - startIndex;
            self._collection.fetch({
                success: function (collection, response) {
                    var data = []
                    collection.each(function(item) {
                        item.set("originalFilename",item.getVisibleName(window.app.status.currentProjectModel.get('blindMode')));
                    });

                    data = collection.toJSON();
                    var count =  collection.fullSize;
                    var end = (endIndex > count) ? count : endIndex;
                    var pages = Math.ceil(count / options.pageSize);
                    var page = options.pageIndex + 1;
                    var start = startIndex + 1;

                    if (self._formatter) self._formatter(data);
                    callback({ data: data, start: start, end: end, count: count, pages: pages, page: page });

                    collection.each(function(model) {
                        var action = new ImageReviewAction({el:("#MyGrid > tbody"),model:model, container : self._container});
                        action.configureAction();
                    });
                }
            });


        }, this._delay)
    }
};


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
        $('#MyGrid').datagrid('renderData');
    },
    render : function() {
        var self = this;
        require(["text!application/templates/image/ImageReviewAction.tpl.html"], function (actionMenuTpl) {
           self.doLayout(actionMenuTpl)
        });
    },
    doLayout: function (actionMenuTpl) {
        var self = this;

        var dataSource = new ImageInstanceDataSource({
            container : self,
            columns: [
				{
                    property: 'id',
                    label: 'ID',
                    sortable: true
                },
                {
                    property: 'thumb',
                    label: 'Preview',
                    sortable: false
                },
                {
                    property: 'originalFilename',
                    label: 'Name',
                    sortable: true
                },
                {
                    property: 'width',
                    label: 'Width',
                    sortable: true
                },
                {
                    property: 'height',
                    label: 'Height',
                    sortable: true
                },
                {
                    property: 'magnification',
                    label: 'Magnification',
                    sortable: true
                },
                {
                    property: 'resolution',
                    label: 'Resolution',
                    sortable: true
                },
                {
                    property: 'numberOfAnnotations',
                    label: 'a',
                    sortable: true
                },
                {
                    property: 'numberOfJobAnnotations',
                    label: 'b',
                    sortable: true
                },
                {
                    property: 'numberOfJobAnnotations',
                    label: 'c',
                    sortable: true
                },
                {
                    property: 'mime',
                    label: 'Mime',
                    sortable: true
                },
                {
                    property: 'created',
                    label: 'Created',
                    sortable: true
                },
				{
                    property: 'status',
                    label: 'Status',
                    sortable: false
                },
                {
                    property: 'action',
                    label: 'Action',
                    sortable: false
                }
            ],
            formatter: function (items) {
                $.each(items, function (index, item) {
                    item.thumb = _.template("<div style='width : 130px;'><img src='<%= thumb %>' alt='originalFilename' style='max-height : 45px;max-width : 128px;'/></div>", item);
                    item.action = _.template(actionMenuTpl, item);
                    item.created = window.app.convertLongToDate(item.created);
                    item.resolution = item.resolution.toFixed(3);
					if (item.reviewed) {
							item.status = '<span class="label label-success">Reviewed</span>';
					}
					else if (item.inReview) {
							item.status = '<span class="label label-warning">In review</span>';
					} else {
						item.status = '<span class="label label-info">None</span>';
					}
					
                });
            },
            collection : new ImageInstanceCollection({project: this.idProject}),
            delay: 250
        });

        $('#MyGrid').datagrid({
            dataSource: dataSource,
            dataOptions : { pageIndex: 0, pageSize: 10 },
            stretchHeight: true
        });

        $('#MyGrid').datagrid({ dataSource: dataSource, stretchHeight: false})

		$('#projectImageListing' + self.idProject).hide();
         $('#projectImageTable' + self.idProject).show();
    }
});
