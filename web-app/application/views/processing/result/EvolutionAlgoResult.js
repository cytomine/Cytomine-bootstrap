var EvolutionAlgoResult = Backbone.View.extend({
    //model = job
    //terms
    //annotations
    //el
    width:null,
    project:null,
    annotations:null,
    terms:null,
    jobs:null,
    software:null,
    initialize:function (options) {
        this.annotations = window.app.status.currentAnnotationsCollection;
        this.terms = window.app.status.currentTermsCollection;
        this.project = options.project;
        this.jobs = options.jobs;
        this.software = options.software;
    },
    render:function () {
        var self = this;
        require([
            "text!application/templates/processing/EvolutionAlgoResult.tpl.html"
        ],
                function (retrievalAlgoViewTpl) {
                    self.loadResult(retrievalAlgoViewTpl);
                });
        return this;
    },
    loadResult:function (retrievalAlgoViewTpl) {
        var self = this;
        var width = ((($(self.el).width() - 125))) + "px"
        var height = "400px"
        var content = _.template(retrievalAlgoViewTpl, {
            width:width,
            height:height
        });
        self.width = width;
        console.log($(self.el));
        $(self.el).empty();
        $(self.el).append(content);

        console.log("StatsRetrievalSuggestionEvolutionModel");
        new StatsRetrievalEvolutionModel({job:self.model.id}).fetch({
            success:function (model, response) {
                self.drawAVGEvolution(model, response);

            }
        });

        var select = $(self.el).find("#avgEvolutionLineChartByTermSelect");
        select.empty();
        this.terms.each(function (term) {
            select.append('<option value="' + term.id + '">' + term.get('name') + '</option>');
        });

        select.change(function () {
            self.drawAVGEvolutionByTermAction();
        });

        self.drawAVGEvolutionByTermAction()
    },
    drawAVGEvolution:function (model, response) {
        var self = this;
        // Create and populate the data table.
        var evolution = model.get('evolution');
        if (evolution == undefined) {
            $(self.el).find("#avgEvolutionLineChartPanel").hide();
            return;
        }
        var data = new google.visualization.DataTable();
        data.addColumn('date', 'Date');
        data.addColumn('number', 'Number of annotations');
        data.addColumn('number', 'Success rate (%)');


        var indiceJob = 0;
        var dateSelect = new Date();
        dateSelect.setTime(this.model.get('created'));

        for (var i = 0; i < evolution.length; i++) {
            var date = new Date();
            date.setTime(evolution[i].date);
            if (dateSelect.getTime() == date.getTime()) {
                indiceJob = i;
            }
            var avg = 0;
            if(evolution[i].avg!=-1) avg = (evolution[i].avg*100);
            data.addRow([date, evolution[i].size, avg ]);
        }

        var width = Math.round($(window).width() / 2 - 150);
        // Create and draw the visualization.
        var evolChart = new google.visualization.LineChart($(this.el).find('#avgEvolutionLineChart')[0]);
        evolChart.draw(data, {
                    colors:['#dc3912', '#3366cc'],
                    title:'',
                    width:this.width, height:350,
                    vAxes:{
                        0:{
                            label:'Y1'
                        },
                        1:{
                            label:'Y2'
                        }
                    },
                    vAxis:{title:"Success rate", minValue:0, maxValue:100},
                    hAxis:{title:"Time"},
                    backgroundColor:"whiteSmoke",
                    seriesType:"line",
                    series:{0:{targetAxisIndex:0}, 1:{type:"area", targetAxisIndex:1}},
                    lineWidth:1}
        );
        evolChart.setSelection([
            {row:indiceJob, column:1}
        ]);
        var handleClick = function () {
            var row = evolChart.getSelection()[0]['row'];
            var col = evolChart.getSelection()[0]['column'];
            var dateSelected = new Date();
            dateSelected.setTime(evolution[row].date);
            if (self.jobs != null) {
                var jobSelected = null;
                self.jobs.each(function (job) {
                    var dateSelection = new Date();
                    dateSelection.setTime(job.get('created'));
                    if (dateSelection.getTime() == dateSelected.getTime()) {
                        jobSelected = job;
                    }
                });
                window.location = '#tabs-algos-' + self.project.id + '-' + self.software.id + '-' + jobSelected.id;
            }
        };
        google.visualization.events.addListener(evolChart, 'select', handleClick);

    },
    drawAVGEvolutionByTermAction:function () {
        var self = this;
        var termSelected = $(self.el).find("#avgEvolutionLineChartByTermSelect").val();
        new StatsRetrievalEvolutionModel({job:self.model.id, term:termSelected}).fetch({
            success:function (model, response) {
                self.drawAVGEvolutionByTerm(model, response);

            }
        });
    },
    drawAVGEvolutionByTerm:function (model, response) {
        var self = this;
        // Create and populate the data table.
        var evolution = model.get('evolution');
        console.log("drawAVGEvolutionByTerm.evolution=" + evolution);
        if (evolution == undefined) {
            $(self.el).find("#avgEvolutionLineChartByTerm").text("No data for this term!");
            return;
        }
        $(self.el).find("#avgEvolutionLineChartByTerm").empty();
        var data = new google.visualization.DataTable();
        data.addColumn('date', 'Date');
        data.addColumn('number', 'Number of annotations');
        data.addColumn('number', 'Success rate (%)');


        var indiceJob = 0;
        var dateSelect = new Date();
        dateSelect.setTime(this.model.get('created'));

        for (var i = 0; i < evolution.length; i++) {
            var date = new Date();
            date.setTime(evolution[i].date);
            if (dateSelect.getTime() == date.getTime()) {
                indiceJob = i;
            }
            var avg = 0;
            if(evolution[i].avg!=-1) avg = (evolution[i].avg*100);
            data.addRow([date, evolution[i].size, avg ]);
        }

        var width = Math.round($(window).width() / 2 - 150);
        // Create and draw the visualization.
        var evolChart = new google.visualization.LineChart($(this.el).find('#avgEvolutionLineChartByTerm')[0]);
        evolChart.draw(data, {
                    colors:['#dc3912', '#3366cc'],
                    title:'',
                    width:this.width, height:350,
                    vAxes:{
                        0:{
                            label:'Y1'
                        },
                        1:{
                            label:'Y2'
                        }
                    },
                    vAxis:{title:"Success rate", minValue:0, maxValue:100},
                    hAxis:{title:"Time"},
                    backgroundColor:"whiteSmoke",
                    seriesType:"line",
                    series:{0:{targetAxisIndex:0}, 1:{type:"area", targetAxisIndex:1}},
                    lineWidth:1}
        );
        evolChart.setSelection([
            {row:indiceJob, column:1}
        ]);
        var handleClick = function () {
            var row = evolChart.getSelection()[0]['row'];
            var col = evolChart.getSelection()[0]['column'];
            var dateSelected = new Date();
            dateSelected.setTime(evolution[row].date);
            if (self.jobs != null) {
                var jobSelected = null;
                self.jobs.each(function (job) {
                    var dateSelection = new Date();
                    dateSelection.setTime(job.get('created'));
                    if (dateSelection.getTime() == dateSelected.getTime()) {
                        jobSelected = job;
                    }
                });
                window.location = '#tabs-algos-' + self.project.id + '-' + self.software.id + '-' + jobSelected.id;
            }
        };
        google.visualization.events.addListener(evolChart, 'select', handleClick);

    }
});