/*
 * Copyright (c) 2009-2015. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var ProjectDashboardStats = Backbone.View.extend({
    annotationNumberSelectedTerm: -1,
    initialize: function () {
        var self = this;
        this.noDataAlert = _.template("<div class='alert alert-block'>No data to display</div>", {});
        var select = $("#annotationNumberEvolution");
        select.empty();
        select.append('<option value="' + -1 + '">All terms</option>');
        window.app.status.currentTermsCollection.each(function (term) {
            select.append('<option value="' + term.id + '">' + term.get('name') + '</option>');
        });
        select.val(self.annotationNumberSelectedTerm);
        select.unbind();
        select.change(function () {
            self.drawAnnotationNumberEvolutionByTermAction();
        });



        //draw all
        if(!window.app.status.currentProjectModel.get('blindMode')) {
            var statsCollection = new StatsTermCollection({project: self.model.get('id')});
            statsCollection.fetch({
                success: function (collection, response) {
                    self.drawColumnChart(collection, response, "#projectColumnChart", true);
                }
            });

            self.drawAnnotationNumberEvolutionByTermAction();
            var statsCollection = new StatsTermCollection({project: self.model.get('id')});
            statsCollection.fetch({
                success: function (collection, response) {
                    self.drawPieChart(collection, response, "#projectPieChart");
                }
            });
            new StatsUserCollection({project: self.model.get('id')}).fetch({
                success: function (collection, response) {
                    self.drawColumnChart(collection, response, "#userNbAnnotationsChart");
                }
            });
            new StatsTermSlideCollection({project: self.model.get('id')}).fetch({
                success: function (collection, response) {
                    self.drawColumnChart(collection, response, "#termSlideAnnotationsChart", true);
                }
            });
            new StatsUserSlideCollection({project: self.model.get('id')}).fetch({
                success: function (collection, response) {
                    self.drawColumnChart(collection, response, "#userSlideAnnotationsChart");
                }
            });
            self.drawAnnotationNumberEvolutionByTermAction();
        } else {
            var message = '<div style="margin: 10px 10px 10px 0px" class="alert alert-warning"> <i class="icon-remove"/> Not available in blind mode!</div>';
             $("#annotationNumberEvolution").replaceWith("");
            _.each(["#drawUserAnnotationsChart","#projectColumnChart","#projectPieChart","#userNbAnnotationsChart","#termSlideAnnotationsChart","#userSlideAnnotationsChart"], function(item) {
                $(item).empty();
                $(item).append(message);
                $(item).css("height","100px");
            });

//        $("#lastcommandsitem").empty();
//        $("#lastcommandsitem").append();

        }

    },

    drawUserNbAnnotationsChart: function (collection, response) {
        var self = this;
        $("#userNbAnnotationsChart").html("<svg></svg>");

        var chartData = [{
            key : "Number of annotations by users",
            bar : true,
            values : []
        }];
        collection.each(function (stat) {
            chartData[0].values.push({
                label : stat.get("key"),
                value : stat.get("value")
            });
        });

        if(BrowserSupport.isTooOld()) {
            BrowserSupport.addMessage($("#userNbAnnotationsChart"),BrowserSupport.CHARTS);
        }
        else {
            nv.addGraph(function() {
                var chart = nv.models.discreteBarChart()
                    .x(function(d) { return d.label })
                    .y(function(d) { return d.value });


                d3.select("#userNbAnnotationsChart svg")
                    .datum(chartData)
                    .transition().duration(1200)
                    .call(chart);

                nv.utils.windowResize(chart.update);

                return chart;
            });
        }
    },
    drawPieChart: function (collection, response, el) {
        if (this.model.get('numberOfAnnotations') == 0) {
            //set no data to display if pie Chart
            $("#projectPieChart").html(this.noDataAlert);
            return;
        }
        $(el).html("<svg></svg>");
        var colors = [];
        var chartData = [];
        collection.each(function (stat) {
            if (stat.get("value") > 0) {
                chartData.push({
                    label : stat.get("key"),
                    value : stat.get("value")
                })
                colors.push(stat.get("color"));
            }
        });

        if(BrowserSupport.isTooOld()) {
            BrowserSupport.addMessage($("#projectPieChart"),BrowserSupport.CHARTS);
        }
        else {
            console.log(chartData);
                nv.addGraph(function() {
                var chart = nv.models.pieChart()
                    .x(function(d) { return d.label })
                    .y(function(d) { return d.value })
                    .showLabels(true)
                    .color(colors);

                d3.select(el + " svg")
                    .datum(chartData)
                    .transition().duration(1200)
                    .call(chart);

                //nv.utils.windowResize(chart.update);

                return chart;
            });
        }
    },
    drawColumnChart: function (collection, response, el, withColor) {
        $(el).html("<svg></svg>");
        var colors = [];
        var chartData = [{
            key : "Number of annotations by terms",
            bar : true,
            values : []
        }];
        collection.each(function (stat) {
            chartData[0].values.push({
                label : stat.get("key"),
                value : stat.get("value")
            });
            colors.push(stat.get("color"));
        });

        if(BrowserSupport.isTooOld()) {
            BrowserSupport.addMessage($(el),BrowserSupport.CHARTS);
        }
        else {
        nv.addGraph(function() {
            var chart = nv.models.discreteBarChart()
                .x(function(d) { return d.label })
                .y(function(d) { return d.value })
                .showValues(true);


            if(withColor) {
                chart.color(colors);
            }
            chart.margin({bottom: 125});
            chart.xAxis.rotateLabels(-45);

            d3.select(el + " svg")
                .datum(chartData)
                .transition().duration(1200)
                .call(chart);

            nv.utils.windowResize(chart.update);

            return chart;
        });
        }
    },
    drawAnnotationNumberEvolutionByTermAction: function () {

        var self = this;
        var termSelected = $("#annotationNumberEvolution").val();
        self.annotationNumberSelectedTerm = termSelected;
        new StatsAnnotationEvolutionCollection({project: self.model.get('id'), daysRange: 7, term: termSelected}).fetch({
            success: function (collection, response) {
                self.drawAnnotationNumberEvolutionChart(collection, response);
            }
        });
    },
    drawAnnotationNumberEvolutionChart: function (collection, response) {

        if (collection == undefined) {
            $("#drawUserAnnotationsChart").text("No data for this term!");
            return;
        }
        var el = "#drawUserAnnotationsChart";
        $(el).html("<svg></svg>");
        var chartData = [
            {
                key : "Number of annotations" ,
                bar: false,
                area: true,
                values : []
            }
        ];
        collection.each(function (stat) {
            var date = new Date();
            date.setTime(stat.get("date"));
            chartData[0].values.push({
                date : date,
                value : stat.get("size")
            })
        });

        chartData.map(function(series) {
            series.values = series.values.map(function(d) { return {x: d.date, y: d.value } }).reverse();
            return series;
        });

        var chart;

        if(BrowserSupport.isTooOld()) {
            BrowserSupport.addMessage($(el),BrowserSupport.CHARTS);
        }
        else {
            nv.addGraph(function() {
                chart = nv.models.linePlusBarChart()
                    .margin({top: 30, right: 60, bottom: 50, left: 70})
                    .x(function(d,i) { return i })
                    .color(d3.scale.category10().range());

                chart.xAxis.tickFormat(function(d) {
                    var dx = chartData[0].values[d] && chartData[0].values[d].x || 0;
                     return dx ? d3.time.format('%x')(new Date(dx)) : '';
                })
                    .showMaxMin(false);

                chart.y1Axis
                    .tickFormat(d3.format(',f'));

                chart.y2Axis
                    .tickFormat(function(d) {
                        return d;
                    });

                chart.bars.forceY([0]).padData(false);

                d3.select(el + ' svg')
                    .datum(chartData)
                    .transition().duration(500).call(chart);

                nv.utils.windowResize(chart.update);

                return chart;
            });
        }
    }

});