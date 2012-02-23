var ProjectDashboardAlgos = Backbone.View.extend({
    rendered : false,
    initialize : function (options) {
        this.el = "#tabs-algos-" + this.model.id;
    },
    render : function() {
        this.initImageFilters();
        this.initBatchProcessing();
        this.listRetrievalAlgo();
        this.rendered = true;
    },
    refresh : function() {
        if (!this.rendered) this.render();
    },
    removeImageFilter : function (imageFilter) {
        var self = this;
        //use fake ID since backbone > 0.5 : we should destroy only object saved or fetched
        new ProjectImageFilterModel({ id : 1, project : self.model.id, imageFilter : imageFilter.get("id")}).destroy({
            success : function (model, response) {
                $(self.el).find("li.imageFilter" + imageFilter.get("id")).remove();
            }
        });
        return false;
    },
    renderFilters : function() {
        var self = this;
        var el = $(this.el).find(".image-filters");
        el.empty();
        new ProjectImageFilterCollection({ project : self.model.id}).fetch({
            success : function (imageFilters, response) {
                imageFilters.each(function (imageFilter) {
                    self.renderImageFilter(imageFilter, el);
                });
            }
        });
    },
    renderImageFilter : function (imageFilter, el) {
        var self = this;
        var tpl = _.template("<li class='imageFilter<%=   id %>'><%=   name %> <a class='removeImageFilter<%=   id %>' href='#'><span class='label important'>Remove</span></a></li>", imageFilter.toJSON());
        $(el).append(tpl);
        $(this.el).find("a.removeImageFilter" + imageFilter.get("id")).click(function() {
            self.removeImageFilter(imageFilter);
            return false;
        });
    },
    initImageFilters : function() {
        var self = this;
        var el = $(this.el).find(".image-filters");

        new ImageFilterCollection().fetch({
            success : function (imageFilters, response) {
                imageFilters.each(function (imageFilter) {
                    var option = _.template("<option value='<%=   id %>'><%=   name %></option>", imageFilter.toJSON());
                    $(self.el).find("#addImageFilter").append(option);

                });
                $(self.el).find("#addImageFilterButton").click(function() {
                    new ProjectImageFilterModel({ project : self.model.id, imageFilter : $(self.el).find("#addImageFilter").val()}).save({}, {
                        success : function (imageFilter, response) {
                            self.renderImageFilter(imageFilter, el);
                        },
                        error : function (response) {

                        }
                    });
                    return false;
                });
            }
        });

        self.renderFilters();

    },
    initBatchProcessing : function () {
        console.log("initBatchProcessing");
        $(this.el).find(".batch-processing").append(":-) !");
    },
    /*
     * ALGO RETRIEVAL TEST
     */
    listRetrievalAlgo : function () {
        var self = this;

        $("#launchRetrievalAlgoButton").click(function() {
                    new JobModel({ project : self.model.id, software : 'retrieval'}).save({}, {
                                success : function (job, response) {
                                    console.log("SUCCESS JOB");
                                },
                                error : function (response) {
                                    console.log("BAD JOB");
                                }
                      });
        });
        $("#listAlgoRetrieval").jqGrid({
            datatype: "local",
            height: 250,
            colNames:['id','running', 'indeterminate', 'progress','successful','software'],
            colModel:[
                {name:'id',index:'id', width:55},
                {name:'running',index:'running', width:90, editable: true, edittype: 'checkbox',formatter:'checkbox'},
                {name:'indeterminate',index:'indeterminate', width:100, editable: true, edittype: 'checkbox',formatter:'checkbox'},
                {name:'progress',index:'progress', width:80, align:"right"},
                {name:'successful',index:'successful', width:80, align:"right", editable: true, edittype: 'checkbox',formatter:'checkbox'},
                {name:'software',index:'software', width:80,align:"right"}
            ],
            multiselect: true,
            caption: "Manipulating Array Data"
        });

        console.log("listRetrievalAlgo");
        var refreshData = function() {

            new JobCollection({ project : self.model.id, software :"retrieval"}).fetch({
                success : function (jobs, response) {
                    var i=0;
                    $("#listAlgoRetrieval").jqGrid('clearGridData');
                    jobs.each(function (job) {
                        var data = {id:job.id,running:job.get('running'),indeterminate:job.get('indeterminate'),progress:'<div class="progBar">'+job.get('progress')+'</div>',successful:job.get('successful'),software:job.get('software')}
                        $("#listAlgoRetrieval").jqGrid('addRowData', i + 1, data);
                        i++;
                    });
                     $('div.progBar').each(function(index) {
                        var progVal = eval($(this).text());
                        $(this).text('');
                        $(this).progressbar({
                      value: progVal
                     });
                 });

                }
            });
          };
         refreshData();
         setInterval(refreshData,5000);


//     $("#listAlgoRetrieval").jqGrid({
//        url:'api/project/' + self.model.id + "/job.json?software=retrieval",
//        datatype: "json",
//        colNames:['id'],//'running', 'indeterminate', 'progress','successful','software'],
//        colModel:[
//            {name:'id',index:'id', width:55}
////            {name:'running',index:'running', width:90},
////            {name:'indeterminate',index:'indeterminate', width:100},
////            {name:'progress',index:'progress', width:80, align:"right"},
////            {name:'successful',index:'successful', width:80, align:"right"},
////            {name:'software',index:'software', width:80,align:"right"}
//        ],
//         loadComplete: function(data) {
//             console.log("LOAD COMPLETE");
//             console.log(data);
//        },
//        rowNum:10,
//        rowList:[10,20,30],
//        pager: '#pagerAlgoRetrieval',
//        sortname: 'id',
//        viewrecords: true,
//        sortorder: "desc",
//        caption:"Algo Retrieval",
//         loadonce: true,
//         jsonReader: {
//            repeatitems : false,
//            id: "0"
//         }
//    });
//    $("#listAlgoRetrieval").jqGrid('navGrid','#pagerAlgoRetrieval',{edit:false,add:false,del:false});
    }

});