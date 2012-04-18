var JobTableView = Backbone.View.extend({
    width : null,
    software: null,
    project : null,
    jobs : null,
    parent : null,
    table : null,
    paramsFromSoftwares : null,
    paramsFromSoftwaresVisibility : null,
    FIRSTCOLUMNWITHJOBDATA : 6,
    initialize: function(options) {
        var self = this;
        this.software = options.software;
        this.project = options.project;
        this.parent = options.parent;
        this.jobs = options.jobs;
        this.paramsFromSoftwares = [];
        _.each(self.software.get('parameters'), function(param) {
            self.paramsFromSoftwares.push(param.name)
        });
        this.paramsFromSoftwaresVisibility = [];
        _.each(self.software.get('parameters'), function(param) {
            self.paramsFromSoftwaresVisibility.push(false)
        });
    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/processing/JobListing.tpl.html"
        ],
               function(jobListingViewTpl) {
                   self.loadResult(jobListingViewTpl);
               });
        return this;
    },
    loadResult : function (jobListingViewTpl) {
        console.log("JobTableView.loadResult");
        var self = this;
        var content = _.template(jobListingViewTpl, {});
        $(self.el).empty();
        $(self.el).append(content);
        self.printDatatables();
    },
    refresh : function(jobs) {
        var self = this;
        console.log("JobTableView.refresh");
        self.jobs = jobs;
        $(self.el).find('#searchJobTable').find('#searchJobHeader').empty();
        $(self.el).find('#searchJobTable').find("tbody").empty();

        if(self.jobs != undefined && self.jobs.length>0) {
             self.addParamsColumn(self.jobs.models[0]);
        }
//        self.printDatatables()
        $(self.el).find('#searchJobTable').find("tbody").empty();
        var datatable = $(self.el).find('#searchJobTable').dataTable();

        datatable.fnClearTable();
        $(self.el).find('#searchJobTable').find("tbody").empty();
        datatable.fnClearTable();
        console.log("#### tbody="+$(self.el).find('#searchJobTable').find("tbody").children().length);
        self.buildShowHideAllColumn(datatable,true);
        datatable.fnClearTable();
        console.log("#### self.jobs="+self.jobs.length);
        console.log("#### tbody="+$(self.el).find('#searchJobTable').find("tbody").children().length);
        if (self.jobs != undefined) {
            self.jobs.each(function (job) {
//                console.log("job="+job.id);
                self.addJobData(job);
            });
        }
        self.initSubGridDatatables();
        self.table = $(self.el).find('#searchJobTable').dataTable({
//            "bFilter": false,
//            "sDom": '<"toolbar">frtip',
            "sPaginationType": "bootstrap",
            "oLanguage": {
                "sLengthMenu": "_MENU_ records per page"
            },
            "iDisplayLength": 15 ,
            "bLengthChange" : false,
//            "aoColumnDefs": [
//                { "sWidth": "5%", "aTargets": [ 0 ] },
//                { "sWidth": "10%", "aTargets": [ 1 ]},
//                { "sWidth": "10%", "aTargets": [ 2 ] },
//                { "sWidth": "25%", "aTargets": [ 3 ] },
//                { "sWidth": "20%", "aTargets": [ 4 ] },
//                { "sWidth": "30%", "aTargets": [ 5 ] }
//            ],
            bDestroy: true
        });
        self.buildSHowHideColumnParamPanel(datatable)
    },
    addParamsColumn : function(job) {
        var self = this;

        $(self.el).find('#searchJobTable').find('#searchJobHeader').append('<th></th>');
        $(self.el).find('#searchJobTable').find('#searchJobHeader').append('<th>Id</th>');
        $(self.el).find('#searchJobTable').find('#searchJobHeader').append('<th>#</th>');
        $(self.el).find('#searchJobTable').find('#searchJobHeader').append('<th>Date</th>');
        $(self.el).find('#searchJobTable').find('#searchJobHeader').append(' <th>State</th>');
        $(self.el).find('#searchJobTable').find('#searchJobHeader').append('<th>See</th>');

        _.each(self.software.get('parameters'), function(param) {
            $(self.el).find('#searchJobTable').find('#searchJobHeader').append("<th>"+param.name+"</th>");
        });
    },
    addJobData : function(job) {
        var self = this;
        var tbody = $(self.el).find('#searchJobTable').find("tbody");
        var cellIcon = '<i class="icon-plus"></i>';
        var cellId = job.id;
        var cellNumber = job.get('number');
        var cellDate = window.app.convertLongToDate(job.get('created'));
        var cellState = self.getStateElement(job);
        var cellSee = "";
        if (self.comparator) {
            //if comparator then print "compare" and click must refresh parent
            cellSee = '<a id="select' + job.id + '">Compare</a>'
        } else {
            //else if comparator then print "see details" and click must select job
            cellSee = '<a href="#tabs-algos-' + self.project.id + "-" + self.software.id + "-" + job.id + '" id="' + job.id + '">See details<br></a>'
        }

        var colArray = ['<tr>','<td>' + cellIcon + '</td>','<td  style="text-align:left;">'+cellId + '</td>','<td  style="text-align:center;">' + cellNumber + '</td>','<td  style="text-align:center;">' + cellDate + '</td>','<td  style="text-align:center;">' + cellState + '</td>','<td>' + cellSee + '</td>']

        var jobParamArray = job.get('jobParameter');
//        console.log(jobParamArray.length);
        if(jobParamArray.length==0) {
            _.each(self.paramsFromSoftwares, function(softParam) {
                colArray.push('<td></td>')
            });
        } else {
            for (var i = 0, j=0; i < self.paramsFromSoftwares.length && j<jobParamArray.length;i++) {
                //console.log(self.paramsFromSoftwares[i] +"----------"+jobParamArray[j].name);
                if(self.paramsFromSoftwares[i]==jobParamArray[j].name) {
                    colArray.push('<td>'+jobParamArray[j].value+'</td>');
                    j++;
                } else {
                    colArray.push('<td></td>')
                }
            }
        }
        colArray.push('</tr>');
        var rowString = colArray.join("");
//        console.log("job="+job.id);
//        console.log("colArray="+colArray.length);
        tbody.append(rowString);
    },
    buildShowHideAllColumn : function(datatable, show) {
        var self = this;
        for(var i=0;i<datatable.fnSettings().aoColumns.length;i++) {
            datatable.fnSetColumnVis( i, show);
        }
    },
    buildSHowHideColumnParamPanel : function(datatable) {
        var self = this;
        console.log("buildHideColumnParamPanel="+datatable.fnSettings().aoColumns.length);
        for(var i=self.FIRSTCOLUMNWITHJOBDATA;i<datatable.fnSettings().aoColumns.length;i++) {
            var visibility = self.paramsFromSoftwaresVisibility[i-self.FIRSTCOLUMNWITHJOBDATA];
            datatable.fnSetColumnVis( i, visibility);
        }

        $("#showParamColumnSearchJobTable").empty();
        for(var i=self.FIRSTCOLUMNWITHJOBDATA;i<datatable.fnSettings().aoColumns.length;i++) {
            var showOrHide = !self.paramsFromSoftwaresVisibility[i-self.FIRSTCOLUMNWITHJOBDATA]?"Show" : "Hide";
            $("#showParamColumnSearchJobTable").append('<button id="'+(i-self.FIRSTCOLUMNWITHJOBDATA)+'">'+ showOrHide +' '+datatable.fnSettings().aoColumns[i].sTitle+'</button>');

            $("#showParamColumnSearchJobTable").find('#'+(i-self.FIRSTCOLUMNWITHJOBDATA)).click(function () {
                var $this = $(this);
                console.log("Show/Hide:"+$this.attr("id"));
                self.showOrHideColumnVisibility((parseInt($this.attr("id"))+self.FIRSTCOLUMNWITHJOBDATA), datatable);

            });
        }
    },
    showOrHideColumnVisibility : function(colIndex, datatable) {
        console.log("Click show/hide column:"+colIndex);
        var self = this;
        var columnIndex = colIndex - self.FIRSTCOLUMNWITHJOBDATA;
        var columnParamIndex = parseInt(colIndex);
        console.log("columnIndex="+columnIndex +" columnParamIndex="+columnParamIndex);
        var oldVisibility = self.paramsFromSoftwaresVisibility[columnIndex];
        self.paramsFromSoftwaresVisibility[columnIndex] = !self.paramsFromSoftwaresVisibility[columnIndex];
        console.log("Change visibility to "+self.paramsFromSoftwaresVisibility[columnIndex]);
        datatable.fnSetColumnVis( columnParamIndex, self.paramsFromSoftwaresVisibility[columnIndex]);

        if(oldVisibility) {
            //hide
            $("#showParamColumnSearchJobTable").find('#'+columnIndex).text("Show " + datatable.fnSettings().aoColumns[(columnParamIndex)].sTitle);
        } else {
            //show
            $("#showParamColumnSearchJobTable").find('#'+columnIndex).text("Hide " + datatable.fnSettings().aoColumns[(columnParamIndex)].sTitle);
        }
    },
    printDatatables : function() {
        var self = this;
        console.log("JobTableView.printDatatables");
        console.log(self.jobs);
        console.log(self.jobs.length);

        $(self.el).find('#searchJobTable').find('#searchJobHeader').empty();
        $(self.el).find('#searchJobTable').find("tbody").empty();

        if(self.jobs != undefined && self.jobs.length>0) {
             self.addParamsColumn(self.jobs.models[0]);
        }
//
        var datatable = $(self.el).find('#searchJobTable').dataTable();

        $(self.el).find('#searchJobTable').find("tbody").empty();


        //show hidden column (if not done, datatable is not filled)
        self.buildShowHideAllColumn(datatable,true);

        datatable.fnClearTable();

        //print data from project image table

        if (self.jobs != undefined) {
            self.jobs.each(function (job) {
                console.log("job="+job.id);
                self.addJobData(job);
            });
        }
        console.log("tbody="+$(self.el).find('#searchJobTable').find("tbody").length);
//        console.log("$(self.el).find('#searchJobTable')="+$(self.el).find('#searchJobTable').length);
//        console.log($(self.el).find('#searchJobTable').html());
        self.table = $(self.el).find('#searchJobTable').dataTable({
//            "bFilter": false,
//            "sDom": '<"toolbar">frtip',
            "sPaginationType": "bootstrap",
            "oLanguage": {
                "sLengthMenu": "_MENU_ records per page"
            },
            "iDisplayLength": 15 ,
            "bLengthChange" : false,
//            "aoColumnDefs": [
//                { "sWidth": "5%", "aTargets": [ 0 ] },
//                { "sWidth": "10%", "aTargets": [ 1 ]},
//                { "sWidth": "10%", "aTargets": [ 2 ] },
//                { "sWidth": "25%", "aTargets": [ 3 ] },
//                { "sWidth": "20%", "aTargets": [ 4 ] },
//                { "sWidth": "30%", "aTargets": [ 5 ] }
//            ],
            bDestroy: true
        });

        console.log("initSubGridDatatables");
        self.initSubGridDatatables();
        console.log("hide id column");
        //hide id column
        self.buildSHowHideColumnParamPanel(self.table);
    },
    getStateElement : function(job) {
        if(job.isNotLaunch()) return '<span class="label btn-inverse">Not Launch!</span> ';
        else if(job.isInQueue()) return '<span class="label btn-info">In queue!</span> ' ;
        else if(job.isRunning()) return '<span class="label btn-primary">Running!</span> ' ;
        else if(job.isSuccess()) return '<span class="label btn-success">Success!</span> ';
        else if(job.isFailed()) return '<span class="label btn-danger">Failed!</span> ';
        else if(job.isIndeterminate()) return '<span class="label btn-inverse">Indetereminate!</span> ' ;
        else if(job.isWait()) return '<span class="label btn-warning">Wait!</span> ' ;
        else return "no supported";
    },
    initSubGridDatatables : function() {
        console.log("JobTableView.initSubGridDatatables");
        var self = this;

        $(self.el).find('#searchJobTable tbody td i').live('click', function () {
            console.log('CLICK:' + self.table.fnIsOpen(nTr));
            var nTr = $(this).parents('tr')[0];
            if (self.table.fnIsOpen(nTr)) {
                /* This row is already open - close it */
                $(this).removeClass("class","icon-minus");
                $(this).addClass("class","icon-plus");
                self.table.fnClose(nTr);
            }
            else {
                /* Open this row */
                $(this).removeClass("class","icon-plus");
                $(this).addClass("class","icon-minus");
                self.table.fnOpen(nTr, self.seeDetails(nTr), 'details');
                var aData = self.table.fnGetData(nTr);
                console.log("aData[1]=" + aData[1]);
                new JobModel({ id : aData[1]}).fetch({
                    success : function (model, response) {
                        var tableParam = $(self.el).find('#searchJobTable').find('table[id=' + aData[1] + ']');
                        _.each(model.get('jobParameter'), function(param) {
                            tableParam.append('<tr><td>' + param.name + '</td><td>' + param.value + '</td><td>' + param.type + '</td></tr>');
                        });
                    }
                });
            }
        });
    },
    /* Formating function for row details */
    seeDetails : function(nTr) {
        var self = this;
        var aData = self.table.fnGetData(nTr);

        var sOut = '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;" id="' + aData[1] + '">';
        sOut += '</table>';

        return sOut;
    }

});