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
        var tbody = $('#softwareParamsTable').find("tbody");
        tbody.empty();
        _.each(self.model.get('parameters'), function (param) {
            var tpl = "<tr><td><%= name %></td><td><%= type %></td><td><%= defaultParamValue %></td><td><input type='checkbox' <%= checked %> disabled /></td><td><%= index %></td></tr>";
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
        $("#softwareInfoChart").html("<svg></svg>");
        var title = 'Job status for ' + self.model.get('name') + ' (over all projects)';
        var chartData = [{
            key : title,
            bar : true,
            values : [
                {
                    label : 'Not Launch',
                    value : software.get('numberOfNotLaunch')
                },
                {
                    label : 'In Queue',
                    value : software.get('numberOfInQueue')
                },
                {
                    label : 'Running',
                    value : software.get('numberOfRunning')
                },
                {
                    label : 'Success',
                    value : software.get('numberOfSuccess')
                },
                {
                    label : 'Failed',
                    value : software.get('numberOfFailed')
                },
                {
                    label : 'Indeterminate',
                    value : software.get('numberOfIndeterminate')
                },
                {
                    label : 'Wait',
                    value : software.get('numberOfWait')
                }
            ]
        }];

        nv.addGraph(function() {
            var chart = nv.models.discreteBarChart()
                .x(function(d) { return d.label })
                .y(function(d) { return d.value })
                .color(["#434141", "#65d7f8", "#005ccc", "#52a652", "#c43c35", "#434343", "#faaa38"]);


            d3.select("#softwareInfoChart svg")
                .datum(chartData)
                .transition().duration(1200)
                .call(chart);

            nv.utils.windowResize(chart.update);

            return chart;
        });

    }
});