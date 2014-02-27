var JobListingView = Backbone.View.extend({
    width: null,
    software: null,
    project: null,
    jobs: null,
    openParameterGrid: [],
    parent: null,
    initialize: function (options) {
        this.software = options.software;
        this.project = options.project;
        this.parent = options.parent;
    },
    render: function () {
        var self = this;
        console.log("trace");
        require([
            "text!application/templates/processing/JobListing.tpl.html"
        ],
            function (jobListingViewTpl) {
                self.loadResult(jobListingViewTpl);
            });
        return this;
    },
    loadResult: function (jobListingViewTpl) {
        var self = this;
        var content = _.template(jobListingViewTpl, {});
        $(self.el).empty();
        $(self.el).append(content);

        var width = ($(window).width() - 200);
        var height = ($(window).height() - 200);
        $(self.el).dialog({ width: width, height: height, modal: true });


        $("#panelSoftwareInfo").empty();
        self.printJobListingPanel(self.software, width);
    },
    printJobListingPanel: function (software) {
        var self = this;
        console.log("printJobListingPanel: software=" + software.id);

        //add list+pager
        $("#jobListDiv").append('<table id="listAlgoInfo" style="margin:0 auto;"></table><div id="pagerAlgoInfo"></div>');
        var width = $(self.el).width() * 0.9;
        $("#listAlgoInfo").jqGrid({
            datatype: "local",
            height: "100%",
            width: width,
            colNames: ['id', 'result', 'running', 'indeterminate', 'progress', 'successful', "created"],
            colModel: [
                {name: 'id', index: 'id', width: 50, align: "center"},
                {name: 'result', index: 'result', width: 60, align: "center"},
                {name: 'running', index: 'running', width: 30, editable: true, edittype: 'checkbox', formatter: 'checkbox', align: "center"},
                {name: 'indeterminate', index: 'indeterminate', width: 30, editable: true, edittype: 'checkbox', formatter: 'checkbox', align: "center"},
                {name: 'progress', index: 'progress', width: 70, align: "center"},
                {name: 'successful', index: 'successful', width: 30, editable: true, edittype: 'checkbox', formatter: 'checkbox', align: "center"},
                {name: 'created', index: 'created', width: 75, align: "center"}
            ],
            caption: "Job listing from " + software.get('name') + " for project " + self.project.get('name'),
            subGrid: true,
            shrinkToFit: true,
            pager: $('#pagerAlgoInfo'),
            sortname: 'id',
            //rowNum:5,   //doesnt work :-(
            //rowList:[5,10,20,30],
            viewrecords: true,
            sortorder: "desc",
            subGridRowExpanded: function (subgrid_id, row_id) {
                var idJob = $("#listAlgoInfo").jqGrid('getCell', row_id, 1);

                if (!_.include(self.openParameterGrid, idJob)) {
                    self.openParameterGrid.push(idJob);
                }

                self.printJobParameter(subgrid_id, row_id, idJob);
                console.log("openParameterGrid=" + self.openParameterGrid);
            },
            subGridRowColapsed: function (subgrid_id, row_id) {
                var idJob = $("#listAlgoInfo").jqGrid('getCell', row_id, 2);
                self.openParameterGrid = _.without(self.openParameterGrid, idJob);
                console.log("openParameterGrid=" + self.openParameterGrid);
            }
        });

        var refreshData = function () {

            new JobCollection({ project: self.project.id, software: software.id}).fetch({
                success: function (jobs, response) {
                    self.jobCollection = jobs;
                    var i = 0;
                    $("#listAlgoInfo").jqGrid('clearGridData');
                    jobs.each(function (job) {

                        var dateStr = self.convertLongToDate(job.get('created'));

                        //button format
                        var buttonStr = '<button id="seeResult' + job.id + '">See algo result</button>';


                        var data = {
                            id: job.id,
                            result: buttonStr,
                            running: job.get('running'),
                            indeterminate: job.get('indeterminate'),
                            progress: '<div class="progBar">' + job.get('progress') + '</div>',
                            successful: job.get('successful'),
                            software: job.get('software'),
                            created: dateStr
                        };
                        $("#listAlgoInfo").jqGrid('addRowData', i + 1, data);
                        $("#seeResult" + job.id).button().click(function (event) {
                            event.preventDefault();
                            self.selectJob(job);
                        });

                        i++;
                    });
                    $('div.progBar').each(function (index) {
                        var progVal = eval($(this).text());
                        $(this).text('');
                        $(this).progressbar({
                            value: progVal
                        });
                    });
                    self.reloadJobParameter();
                    $('#pagerAlgoInfo').find('select').val('10');
                }

            });
        };
        refreshData();
        $("#listAlgoInfo").jqGrid('navGrid', '#pagerAlgoInfo', {edit: false, add: false, del: false}, {}, {}, {}, {multipleSearch: true});

        $('#pagerAlgoInfo').find('select').val('5'); // selects "Two"
        //setInterval(refreshData, 5000);

    },
    selectJob: function (job) {
        var self = this;
        $(this.el).dialog("close");
        console.log('#tabs-algos-' + self.project.id + "-" + self.software.id + "-" + job.id);
        console.log(job);
        console.log(self.parent);
        self.parent.printProjectJobInfo(job.id);
    },
    reloadJobParameter: function () {
        var self = this;
        console.log("reloadJobParameter");
        _.each(self.openParameterGrid, function (idJob) {
            var gridButton = $("td[title='" + idJob + "']").prev().find("a");
            gridButton.click();
        });
    },
    printJobParameter: function (subgrid_id, row_id, idJob) {
        var self = this;

        console.log("printJobParameter=" + subgrid_id + "|" + row_id);
        var subgrid_table_id, pager_id;
        subgrid_table_id = subgrid_id + "_t";
        pager_id = "p_" + subgrid_table_id;
        $("#" + subgrid_id).html("<table id='" + subgrid_table_id + "' class='scroll'></table><div id='" + pager_id + "' class='scroll'></div>");
        var width = Math.round($(window).width() * 0.5);
        $("#" + subgrid_table_id).jqGrid({
            datatype: "local",
            colNames: ['name', 'value', 'type'],
            colModel: [
                {name: "name", index: "name", width: 100, key: true, sortable: false},
                {name: "value", index: "value", width: 300, sortable: false},
                {name: "type", index: "type", width: 70, sortable: false}
            ],
            rowNum: 20,
            pager: pager_id,
            sortname: 'name',
            sortorder: "asc",
            height: '100%',
            width: width
        });
        $("#" + subgrid_table_id).jqGrid('navGrid', "#" + pager_id, {edit: false, add: false, del: false});

        var i = 0;
        $("#" + subgrid_table_id).jqGrid('clearGridData');

        _.each(self.jobCollection.get(idJob).get('jobParameters'), function (jobparam) {
            var data = {name: jobparam.name, value: jobparam.value, type: jobparam.type};
            $("#" + subgrid_table_id).jqGrid('addRowData', i + 1, data);
            i++;
        });

        $("#" + subgrid_table_id).jqGrid('navGrid', "#" + pager_id, {edit: false, add: false, del: false});
        $("#" + subgrid_table_id).trigger("reloadGrid");

        //trigger(“reloadGrid”)
    },
    convertLongToDate: function (longDate) {
        var createdDate = new Date();
        createdDate.setTime(longDate);

        //date format
        var year = createdDate.getFullYear();
        var month = (createdDate.getMonth() + 1) < 10 ? "0" + (createdDate.getMonth() + 1) : (createdDate.getMonth() + 1);
        var day = (createdDate.getDate()) < 10 ? "0" + (createdDate.getDate()) : (createdDate.getDate());

        var hour = (createdDate.getHours()) < 10 ? "0" + (createdDate.getHours()) : (createdDate.getHours());
        var min = (createdDate.getMinutes()) < 10 ? "0" + (createdDate.getMinutes()) : (createdDate.getMinutes());

        var dateStr = year + "-" + month + "-" + day + " " + hour + "h" + min;
        return dateStr;
    }
});