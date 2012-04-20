var JobTableView = Backbone.View.extend({
    width : null,
    software: null,
    project : null,
    jobs : null,
    parent : null,
    table : null,
    datatable : null,
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
        self.printDatatables();
    },
    printDatatables : function() {
        var self = this;
        console.log("JobTableView.printDatatables");
        $(self.el).find('#searchJobTable').find('#searchJobHeader').empty();
        $(self.el).find('#searchJobTable').find("tbody").empty();

        //add column
        if(self.jobs != undefined && self.jobs.length>0) {
             self.addColumns(self.jobs.models[0]);
        }

        //clear row
        $(self.el).find('#searchJobTable').find("tbody").empty();
        var datatable = $(self.el).find('#searchJobTable').dataTable();
        datatable.fnClearTable();
        $(self.el).find('#searchJobTable').find("tbody").empty();

        //show hidden column (if not done, datatable is not filled)
        self.buildShowHideAllColumn(datatable,true);
        datatable.fnClearTable();

        //add row
        if (self.jobs != undefined) {
            self.jobs.each(function (job) {
                self.addRow(job);
            });
        }

        $(self.el).find('#searchJobTable').find("tbody").find("button").click(function() {
            var id = $(this).attr("id");
            $(self.parent.el).dialog("close");
            window.location = '#tabs-algos-' + self.project.id + "-" + self.software.id + "-" + id + '';
        });

        self.table = $(self.el).find('#searchJobTable').dataTable({
            "bFilter": true,
            "sDom": '<"toolbar">rtip',
            "sPaginationType": "bootstrap",
            "oLanguage": {
                "sLengthMenu": "_MENU_ records per page"
            },
            "iDisplayLength": 15 ,
            "bLengthChange" : false,
            bDestroy: true
        });

        //print sub grid datatables
        self.initSubGridDatatables();

        //hide id column
        self.buildSHowHideColumnParamPanel(self.table);
    },
    addColumns : function(job) {
        var self = this;

        $(self.el).find('#searchJobTable').find('#searchJobHeader').append('<th></th>');
        $(self.el).find('#searchJobTable').find('#searchJobHeader').append('<th>Id</th>');
        $(self.el).find('#searchJobTable').find('#searchJobHeader').append('<th>#</th>');
        $(self.el).find('#searchJobTable').find('#searchJobHeader').append('<th>Date</th>');
        $(self.el).find('#searchJobTable').find('#searchJobHeader').append('<th>State</th>');
        $(self.el).find('#searchJobTable').find('#searchJobHeader').append('<th>See</th>');

        _.each(self.software.get('parameters'), function(param) {
            $(self.el).find('#searchJobTable').find('#searchJobHeader').append("<th>"+param.name+"</th>");
        });
    },
    addRow : function(job) {
        var self = this;
        var tbody = $(self.el).find('#searchJobTable').find("tbody");
        var cellIcon = '<i class="icon-plus"></i>';
        var cellId = job.id;
        var cellNumber = job.get('number');
        var cellDate = window.app.convertLongToDate(job.get('created'));
        var cellState = self.getStateElement(job);
        var cellSee =  '<button id="'+job.id+'">See details</button>';

        //fill array with first column info
        var colArray = ['<tr>','<td>' + cellIcon + '</td>','<td  style="text-align:left;">'+cellId + '</td>','<td  style="text-align:center;">' + cellNumber + '</td>','<td  style="text-align:center;">' + cellDate + '</td>','<td  style="text-align:center;">' + cellState + '</td>','<td>' + cellSee + '</td>']

        //get all params form job to print in table column
        var jobParamArray = job.get('jobParameter');
        if(jobParamArray.length==0) {
            _.each(self.paramsFromSoftwares, function(softParam) {
                colArray.push('<td></td>')
            });
        } else {
            for (var i = 0, j=0; i < self.paramsFromSoftwares.length && j<jobParamArray.length;i++) {
                if(self.paramsFromSoftwares[i]==jobParamArray[j].name) {
                    colArray.push(self.getValueElement(jobParamArray[j]));
                    j++;
                } else {
                    //this job has no info for this param => add empty cell
                    colArray.push('<td></td>')
                }
            }
        }
        colArray.push('</tr>');
        var rowString = colArray.join("");
        tbody.append(rowString);

    },
    getValueElement : function(param) {
        if(param.type=="String" || param.type=="Number") {
            return '<td>'+param.value+'</td>';
        }else if(param.type=="Boolean") {
            if(param.value) return '<td><input type="checkbox" name="checkbox" value="checkbox" checked="checked"></td>'
            else return '<td><input type="checkbox" name="checkbox" value="checkbox" checked="checked"></td>'
        } else if (param.type=="List") {
            return '<td>'+param.value+'</td>';
        } else if(param.type == 'ListProject' || param.type == 'Project') {
            var list = param.value.split(",");
            var projectNames = [];
            _.each(list,function(id) {
                var project = window.app.models.projects.get(id);
                if(project!=undefined)
                    projectNames.push(window.app.models.projects.get(id).get('name'));
            });
            return '<td>'+projectNames.join(', ')+'</td>';
        } else '<td>'+param.value+'</td>';

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

        $(self.el).find('#searchJobTable tbody td i').unbind('click');
        $(self.el).find('#searchJobTable tbody td i').click(function () {

            var nTr = $(this).parents('tr')[0];

            if (self.table.fnIsOpen(nTr)) {
                /* This row is already open - close it */
                console.log("close");
                $(this).removeClass("class","icon-minus");
                $(this).addClass("class","icon-plus");
                self.table.fnClose(nTr);
            }
            else {
                /* Open this row */
                console.log("open");
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
    },
//    buildShowHideAllColumn : function(datatable, show) {
//        var self = this;
//        for(var i=0;i<datatable.fnSettings().aoColumns.length;i++) {
//            datatable.fnSetColumnVis( i, show);
//        }
//    },
//    buildSHowHideColumnParamPanel : function(datatable) {
//        var self = this;
//        console.log("buildHideColumnParamPanel="+datatable.fnSettings().aoColumns.length);
//        for(var i=self.FIRSTCOLUMNWITHJOBDATA;i<datatable.fnSettings().aoColumns.length;i++) {
//            var visibility = self.paramsFromSoftwaresVisibility[i-self.FIRSTCOLUMNWITHJOBDATA];
//            datatable.fnSetColumnVis( i, visibility);
//        }
//
//        $("#showParamColumnSearchJobTable").empty();
//        for(var i=self.FIRSTCOLUMNWITHJOBDATA;i<datatable.fnSettings().aoColumns.length;i++) {
//            var showOrHide = !self.paramsFromSoftwaresVisibility[i-self.FIRSTCOLUMNWITHJOBDATA]?"Show" : "Hide";
//            $("#showParamColumnSearchJobTable").append('<button id="'+(i-self.FIRSTCOLUMNWITHJOBDATA)+'">'+ showOrHide +' '+datatable.fnSettings().aoColumns[i].sTitle+'</button>');
//
//            $("#showParamColumnSearchJobTable").find('#'+(i-self.FIRSTCOLUMNWITHJOBDATA)).click(function () {
//                var $this = $(this);
//                console.log("Show/Hide:"+$this.attr("id"));
//                self.showOrHideColumnVisibility((parseInt($this.attr("id"))+self.FIRSTCOLUMNWITHJOBDATA), datatable);
//
//            });
//        }
//    },
//    showOrHideColumnVisibility : function(colIndex, datatable) {
//        console.log("Click show/hide column:"+colIndex);
//        var self = this;
//        var columnIndex = colIndex - self.FIRSTCOLUMNWITHJOBDATA;
//        var columnParamIndex = parseInt(colIndex);
//        console.log("columnIndex="+columnIndex +" columnParamIndex="+columnParamIndex);
//        var oldVisibility = self.paramsFromSoftwaresVisibility[columnIndex];
//        self.paramsFromSoftwaresVisibility[columnIndex] = !self.paramsFromSoftwaresVisibility[columnIndex];
//        console.log("Change visibility to "+self.paramsFromSoftwaresVisibility[columnIndex]);
//        datatable.fnSetColumnVis( columnParamIndex, self.paramsFromSoftwaresVisibility[columnIndex]);
//
//        if(oldVisibility) {
//            //hide
//            $("#showParamColumnSearchJobTable").find('#'+columnIndex).text("Show " + datatable.fnSettings().aoColumns[(columnParamIndex)].sTitle);
//        } else {
//            //show
//            $("#showParamColumnSearchJobTable").find('#'+columnIndex).text("Hide " + datatable.fnSettings().aoColumns[(columnParamIndex)].sTitle);
//        }
//    }
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
        var strSelectBox = '<select id="selectShowParamColumnSearchJobTable">';

        for(var i=self.FIRSTCOLUMNWITHJOBDATA;i<datatable.fnSettings().aoColumns.length;i++) {

            strSelectBox = strSelectBox + '<option value="'+(i-self.FIRSTCOLUMNWITHJOBDATA)+'">'+datatable.fnSettings().aoColumns[i].sTitle+'</option>';

//            $("#showParamColumnSearchJobTable").find('#'+(i-self.FIRSTCOLUMNWITHJOBDATA)).click(function () {
//                var $this = $(this);
//                console.log("Show/Hide:"+$this.attr("id"));
//                self.showOrHideColumnVisibility((parseInt($this.attr("id"))+self.FIRSTCOLUMNWITHJOBDATA), datatable);
//
//            });
        }
        strSelectBox = strSelectBox + "</select>";
        $("#showParamColumnSearchJobTable").append(strSelectBox);

        $("#selectShowParamColumnSearchJobTable").multiselect({
           selectedText: "Show/Hide  Parameters columns: # of # selected",
            noneSelectedText : "Show/Hide Parameters columns",
            minWidth : ($("#showParamColumnSearchJobTable").width()-50),
            height : 'auto'
        });

        var selectElem = $("#showParamColumnSearchJobTable");
        selectElem.find("button").width( ($("#showParamColumnSearchJobTable").width()-50));
        //put header menu option on the same line
        selectElem.find(".ui-multiselect-menu").find("span").css("display", "inline");
        selectElem.find(".ui-multiselect-menu").find("input").css("display", "inline");
        selectElem.find(".ui-multiselect-menu").find(".ui-multiselect-header").find("li").css("display", "inline");
        //put check all on left and deselect all on right
        selectElem.find(".ui-multiselect-menu").find(".ui-multiselect-header").find("li").eq(0).css("float", "left");
        selectElem.find(".ui-multiselect-menu").find(".ui-multiselect-header").find("li").eq(1).css("float", "right");
        selectElem.find(".ui-multiselect-menu").find("li").css("display", "inline");
        selectElem.find(".ui-multiselect-menu").find("li").css("padding-left", "5px");
        selectElem.find(".ui-multiselect-menu").find("label").css("display", "inline");
        //print scroll only vertical
        selectElem.find("ul.ui-multiselect-checkboxes").css('overflow-y', 'scroll');
        selectElem.find("ul.ui-multiselect-checkboxes").css('overflow-x', 'hidden');
        selectElem.find("#selectShowParamColumnSearchJobTable").multiselect("close");
        selectElem.find("#selectShowParamColumnSearchJobTable").multiselect("uncheckAll");


        $("#selectShowParamColumnSearchJobTable").bind("multiselectclick", function(event, ui){
            console.log("Show/Hide:"+ui.value);
            self.showOrHideColumnVisibility((parseInt(ui.value)+self.FIRSTCOLUMNWITHJOBDATA), datatable,ui.checked);
        });
        $("#selectShowParamColumnSearchJobTable").bind("multiselectcheckall", function(event, ui){
            self.showOrHideAllColumnVisibility(datatable,true);
        });

        $("#selectShowParamColumnSearchJobTable").bind("multiselectuncheckall", function(event, ui){
            self.showOrHideAllColumnVisibility(datatable,false);
        });
    },
    showOrHideAllColumnVisibility : function(datatable,show) {
        var self = this;
        self.paramsFromSoftwaresVisibility = [];
        _.each(self.software.get('parameters'), function(param) {
            self.paramsFromSoftwaresVisibility.push(show)
        });

        var i = 0;
        _.each(self.software.get('parameters'), function(param) {
            self.showOrHideColumnVisibility((parseInt(i)+self.FIRSTCOLUMNWITHJOBDATA), datatable,show);
            i++;
        });
    },
    showOrHideColumnVisibility : function(colIndex, datatable, show) {
        console.log("Click show/hide column:"+colIndex);
        var self = this;
        var columnIndex = colIndex - self.FIRSTCOLUMNWITHJOBDATA;
        var columnParamIndex = parseInt(colIndex);
        console.log("columnIndex="+columnIndex +" columnParamIndex="+columnParamIndex);
        self.paramsFromSoftwaresVisibility[columnIndex] = show
        console.log("Change visibility to "+self.paramsFromSoftwaresVisibility[columnIndex]);
        datatable.fnSetColumnVis( columnParamIndex, self.paramsFromSoftwaresVisibility[columnIndex]);

//        if(oldVisibility) {
//            //hide
//            $("#showParamColumnSearchJobTable").find('#'+columnIndex).text("Show " + datatable.fnSettings().aoColumns[(columnParamIndex)].sTitle);
//        } else {
//            //show
//            $("#showParamColumnSearchJobTable").find('#'+columnIndex).text("Hide " + datatable.fnSettings().aoColumns[(columnParamIndex)].sTitle);
//        }
    }
});