var ProjectDashboardAlgos = Backbone.View.extend({
    rendered : false,
    jobCollection : null,
    openParameterGrid : [],
    initialize : function (options) {
        this.el = "#tabs-algos-" + this.model.id;
    },
    render : function() {
        this.initProjectSoftwareList();
        this.rendered = true;
    },
    refresh : function() {
        if (!this.rendered) this.render();
    },
    initProjectSoftwareList : function () {
        var self = this;
        //todo: add image filter as a software?
        $("#projectSoftwareListUl").append('<li id="consultSoftware-0"><a>Image Filter</a></li>');
        $("#consultSoftware-0").click(function() {
            self.printImageFilterInfo();
        });

        //list software choice list
        new SoftwareCollection({ project : self.model.id}).fetch({
            success : function (collection, response) {
                collection.each(function (software) {
                    $("#projectSoftwareListUl").append('<a><li id="consultSoftware-' + software.id + '">' + software.get('name') + '</li></a>');
                    $("#consultSoftware-" + software.id).click(function() {
                        self.printProjectSoftwareInfo(software);
                    });
                });
                //just for dev, click on retrieval software
                $('#consultSoftware-133673').click();
            }
        });
    },
    printProjectSoftwareInfo : function(software) {
        console.log("printProjectSoftwareInfo: software=" + software.id);
        var self = this;
        //add button to launch algo
        $("#panelSoftwareInfo").empty();
        $("#panelSoftwareInfo").append('<button id="launchSoftwareButton" class="btn">Launch new Algo</button>');

        //button click
        $("#launchSoftwareButton").click(function() {
            console.log("project=" + self.model.id + " software.id=" + software.id);
            new JobModel({ project : self.model.id, software : software.id}).save({}, {
                success : function (job, response) {
                    console.log("SUCCESS JOB");
                },
                error : function (response) {
                    console.log("BAD JOB");
                }
            });
        });

        //add list+pager
        $("#panelSoftwareInfo").append('<table id="listAlgoInfo"></table><div id="pagerAlgoInfo"></div>');
        var width = Math.round($(window).width() * 0.7);
        $("#listAlgoInfo").jqGrid({
            datatype: "local",
            height: 600,
            width: width,
            colNames:['id','running', 'indeterminate', 'progress','successful','software',"created"],
            colModel:[
                {name:'id',index:'id', width:55,align:"center"},
                {name:'running',index:'running', width:50, editable: true, edittype: 'checkbox',formatter:'checkbox',align:"center"},
                {name:'indeterminate',index:'indeterminate', width:50, editable: true, edittype: 'checkbox',formatter:'checkbox',align:"center"},
                {name:'progress',index:'progress', width:100,align:"center"},
                {name:'successful',index:'successful', width:50, editable: true, edittype: 'checkbox',formatter:'checkbox',align:"center"},
                {name:'software',index:'software', width:80,align:"center"},
                {name:'created',index:'created', width:200,align:"center"}
            ],
            multiselect: true,
            caption: "Manipulating Array Data",
            subGrid: true,
            subGridRowExpanded: function(subgrid_id, row_id) {
                var idJob = $("#listAlgoInfo").jqGrid('getCell', row_id, 2);

                if (!_.include(self.openParameterGrid, idJob)) {
                    self.openParameterGrid.push(idJob);
                }

                self.printJobParameter(subgrid_id, row_id, idJob);
                console.log("openParameterGrid=" + self.openParameterGrid);
            },
            subGridRowColapsed: function(subgrid_id, row_id) {
                var idJob = $("#listAlgoInfo").jqGrid('getCell', row_id, 2);
                self.openParameterGrid = _.without(self.openParameterGrid, idJob);
                console.log("openParameterGrid=" + self.openParameterGrid);
                // this function is called before removing the data
                //var subgrid_table_id;
                //subgrid_table_id = subgrid_id+"_t";
                //jQuery("#"+subgrid_table_id).remove();
            }
        });

        console.log("listAlgoInfo");
        var refreshData = function() {

            new JobCollection({ project : self.model.id, software :software.id}).fetch({
                success : function (jobs, response) {
                    self.jobCollection = jobs;
                    var i = 0;
                    $("#listAlgoInfo").jqGrid('clearGridData');
                    jobs.each(function (job) {
                        var createdDate = new Date();
                        createdDate.setTime(job.get('created'));

                        var data = {id:job.id,running:job.get('running'),indeterminate:job.get('indeterminate'),progress:'<div class="progBar">' + job.get('progress') + '</div>',successful:job.get('successful'),software:job.get('software'),created:createdDate}
                        $("#listAlgoInfo").jqGrid('addRowData', i + 1, data);
                        i++;
                    });
                    $('div.progBar').each(function(index) {
                        var progVal = eval($(this).text());
                        $(this).text('');
                        $(this).progressbar({
                            value: progVal
                        });
                    });
                    self.reloadJobParameter();
                }

            });
        };
        refreshData();
        setInterval(refreshData, 5000);

    },
    reloadJobParameter: function() {
        var self = this;
        console.log("reloadJobParameter");
        _.each(self.openParameterGrid, function(idJob) {
            var gridButton = $("td[title='" + idJob + "']").prev().find("a");
            gridButton.click();
        });
    },
    printJobParameter : function(subgrid_id, row_id, idJob) {
        var self = this;
        console.log("printJobParameter=" + subgrid_id + "|" + row_id);
        var subgrid_table_id, pager_id;
        subgrid_table_id = subgrid_id + "_t";
        pager_id = "p_" + subgrid_table_id;
        $("#" + subgrid_id).html("<table id='" + subgrid_table_id + "' class='scroll'></table><div id='" + pager_id + "' class='scroll'></div>");
        var width = Math.round($(window).width() * 0.5);
        $("#" + subgrid_table_id).jqGrid({
            datatype: "local",
            colNames: ['name','value','type'],
            colModel: [
                {name:"name",index:"name",width:100,key:true,sortable:false},
                {name:"value",index:"value",width:300,sortable:false},
                {name:"type",index:"type",width:70,sortable:false}
            ],
            rowNum:20,
            pager: pager_id,
            sortname: 'name',
            sortorder: "asc",
            height: '100%',
            width: width
        });
        $("#" + subgrid_table_id).jqGrid('navGrid', "#" + pager_id, {edit:false,add:false,del:false});

        var i = 0;
        $("#" + subgrid_table_id).jqGrid('clearGridData');
        _.each(self.jobCollection.get(idJob).get('jobParameter'), function(jobparam) {
            var data = {name:jobparam.name,value:jobparam.value,type:jobparam.type}
            $("#" + subgrid_table_id).jqGrid('addRowData', i + 1, data);
            i++;
        });

        $("#" + subgrid_table_id).jqGrid('navGrid', "#" + pager_id, {edit:false,add:false,del:false});
        $("#" + subgrid_table_id).trigger("reloadGrid");

        //trigger(“reloadGrid”)
    },
    printImageFilterInfo : function() {
        $("#panelSoftwareInfo").empty();
        //recopier le contenu imagefilter
        $("#panelSoftwareInfo").append('<h3>Image filters</h3><ul class="image-filters"></ul><select id="addImageFilter"></select> <button id="addImageFilterButton" class="btn">Add</button></div>')
        this.initImageFilters();
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
        var tpl = _.template("<li class='imageFilter<%=   id %>'><%=   name %> <a class='removeImageFilter<%=   id %>' href='#'><span class='label label-important'>Remove</span></a></li>", imageFilter.toJSON());
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
    }
    /*
     * ALGO RETRIEVAL TEST
     */



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

});