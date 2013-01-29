var SoftwareDetailsView = Backbone.View.extend({
    project: null,
    detailsRendered: false,
    initialize: function (options) {
        this.project = options.project;
        this.stats = options.stats;
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/processing/SoftwareDetails.tpl.html"
        ],
            function (softwareDetailsTpl) {
                self.doLayout(softwareDetailsTpl);
            });
        return this;
    },
    doLayout: function (softwareDetailsTpl) {
        var self = this;
        self.model.set({_created: window.app.convertLongToDate(self.model.get("created"))});
        $(self.el).html(_.template(softwareDetailsTpl, $.extend({}, self.model.toJSON(), self.stats.toJSON())));
        $("#softwareHideDetailsButton").on("click", function (e) {
            $("#softwareDetailsPanel").hide();
            $("#softwareDescription").show();
        });
        $("#softwareShowDetailsButton").on("click", function (e) {
            $("#softwareDetailsPanel").show();
            $("#softwareDescription").hide();
            if (!self.detailsRendered) {
                self.printJobsChart();
                self.printSoftwareParams();
                self.detailsRendered = true;
            }
        });

    },
    printSoftwareParams: function () {
        var self = this;
        $('#softwareParamsTable').find("tbody").empty();
        var datatable = $('#softwareParamsTable').dataTable();
        datatable.fnClearTable();

        var tbody = $('#softwareParamsTable').find("tbody");

        _.each(self.model.get('parameters'), function (param) {
            var name = '<td>' + param.name + '</td>';
            var type = '<td style="text-align:center;">' + param.type + '</td>';
            var defaultVal = '<td>' + param.defaultParamValue + '</td>';
            var checked = "";
            if (param.required) {
                checked = 'checked="yes"';
            }
            var require = '<td style="text-align:center;"><input type="checkbox" ' + checked + ' disabled /></td>';
            var index = '<td style="text-align:center;">' + param.index + '</td>';
            tbody.append('<tr>' + name + type + defaultVal + require + index + '</tr>');
        });

        $('#softwareParamsTable').dataTable({
            //"sDom": "<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>>",
            "sPaginationType": "bootstrap",
            "oLanguage": {
                "sLengthMenu": "_MENU_ records per page"
            },
            "iDisplayLength": 999999,
            "bLengthChange": false,
            bDestroy: true,
            "aoColumnDefs": [
                { "sWidth": "30%", "aTargets": [ 0 ] },
                { "sWidth": "10%", "aTargets": [ 1 ] },
                { "sWidth": "40%", "aTargets": [ 2 ] },
                { "sWidth": "10%", "aTargets": [ 3 ] },
                { "sWidth": "10%", "aTargets": [ 4 ] }
            ]
        });
    },
    printJobsChart: function () {
        var self = this;
        var software = self.model;
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Success');
        data.addColumn('number', 'Number');

        data.addRows([
            ['Not Launch', software.get('numberOfNotLaunch')],
            ['In Queue', software.get('numberOfInQueue')],
            ['Running', software.get('numberOfRunning')],
            ['Success', software.get('numberOfSuccess')],
            ['Failed', software.get('numberOfFailed')],
            ['Indeterminate', software.get('numberOfIndeterminate')],
            ['Wait', software.get('numberOfWait')]
        ]);
        var width = $("#softwareInfoChart").width();
        var options = {
            title: 'Job status for ' + self.model.get('name') + ' (over all projects)',
            legend: {position: "right"},
            width: width, height: 350,
            vAxis: {title: "Amount"},
            hAxis: {title: "Job status"},
            backgroundColor: "whiteSmoke",
            strictFirstColumnType: false,
            lineWidth: 1,
            colors: ["#434141", "#65d7f8", "#005ccc", "#52a652", "#c43c35", "#434343", "#faaa38"]
        };

        var chart = new google.visualization.ColumnChart(document.getElementById('softwareInfoChart'));
        chart.draw(data, options);
    }
});