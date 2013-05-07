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


        var tbody = $('#softwareParamsTable').find("tbody");

        _.each(self.model.get('parameters'), function (param) {
            var tpl = "<tr><td><%= name %></td><td><%= type %></td><td><%= defaultParamValue %></td><td><input type='checkbox' <%= checked %> disabled /></td><td><%= index %></td></tr>"
            var rowHtml = _.template(tpl, {
                name : param.name,
                type : param.type,
                defaultParamValue : param.defaultParamValue,
                checked : (param.required ? "checked" : ""),
                index : param.index
            });
            tbody.append(rowHtml);
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