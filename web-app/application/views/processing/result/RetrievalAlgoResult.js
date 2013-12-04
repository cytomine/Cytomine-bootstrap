var RetrievalAlgoResult = Backbone.View.extend({
    width: null,
    project: null,
    terms: null,
    jobs: null,
    software: null,
    initialize: function (options) {
        this.terms = window.app.status.currentTermsCollection;
        this.project = options.project;
        this.jobs = options.jobs;
        this.software = options.software;
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/processing/RetrievalAlgoResult.tpl.html"
        ],
            function (retrievalAlgoViewTpl) {
                self.loadResult(retrievalAlgoViewTpl);
            });
        return this;
    },
    loadResult: function (retrievalAlgoViewTpl) {
        var self = this;
        var content = _.template(retrievalAlgoViewTpl, {});

        $(self.el).empty();
        $(self.el).append(content);


        new StatsRetrievalSuggestionWorstTermWithSuggest({job: self.model.id}).fetch({

            success: function (model, response) {
                self.drawWorstTermTable(model, response, self.terms);

            }
        });


        new StatsRetrievalSuggestionWorstTermModel({job: self.model.id}).fetch({
            success: function (model, response) {
                self.drawWorstTermPieChart(model, response, self.terms);

            }
        });


        new StatsRetrievalSuggestionWorstAnnotationModel({job: self.model.id}).fetch({
            success: function (model, response) {
                self.drawWorstAnnotationsTable(model, response, self.terms);

            }
        });


        new StatsRetrievalSuggestionEvolutionModel({job: self.model.id}).fetch({
            success: function (model, response) {
                self.drawAVGEvolution(model, response);

            }
        });
    },
    reduceTermName: function (termName) {
        //var termReduce = termName.substring(0,Math.min(4,termName.length));
        var termReduce = "";
        var termNameItems = termName.split(" ");
        for (var i = 0; i < termNameItems.length; i++) {
            termReduce = termReduce + termNameItems[i].substring(0, Math.min(4, termNameItems[i].length)) + ". ";
        }
        return termReduce;
    },
    //worstTermList
    drawWorstTermTable: function (model, response, terms) {

        var termList = model.get('worstTerms');
        if (termList == undefined) {
            $(self.el).find("#worstTermListPanel").hide();
            return;
        }
        var self = this;
        require([
            "text!application/templates/dashboard/WorstTermList.tpl.html"],
            function (worstTermListTpl) {
                $(self.el).find("#worstTermList").empty();


                terms.each(function (term) {

                    var action = _.template(worstTermListTpl, {term: term.get('name'), id: term.id, idProject: self.project.id});


                    var entry = termList[term.id];
                    if (entry.length > 0) {
                        $(self.el).find("#worstTermList").append(action);
                    } //if no annotation, don't print info
                    for (var i = 0; i < entry.length && i < 3; i++) {

                        for (var propertyName in entry[i]) {
                            if (propertyName != term.id) {
                                var elemId = "term" + term.id + "suggest" + propertyName;

                                $(self.el).find("#list-suggest-" + term.id).append("<a role='button'  data-toggle='modal' href='#"+elemId+"Modal' id=\"" + elemId + "\"><b>" + terms.get(propertyName).get('name') + "</b> (" + entry[i][propertyName] + "%) </a>");
                                self.linkAnnotationMapWithBadTerm($("#" + elemId), term.id, propertyName, terms,elemId+"Modal");
                            } else {
                                $(self.el).find("#success-suggest-" + term.id).html("");
                                $(self.el).find("#success-suggest-" + term.id).append(entry[i][propertyName]);
                            }

                        }

                    }
                });


                //Gobal sucess rate (good annotation / total annotations)
                var worstTermList = $(self.el).find("#worstTermList");
                worstTermList.append(_.template("<li><b>Average</b> : <%= average %></li>", { average : (model.get('avg') * 100).toFixed(2)}));
                //Global sucess rate per class (For each class, compute sucess + make avg)
                worstTermList.append(_.template("<li><b>Average (per class)</b> : <%= average %></li>", { average : (model.get('avgMiddlePerClass') * 100).toFixed(2)}));
                worstTermList.append('<br><a data-toggle="modal" href="#confusionMatrixModal" id="matrix-suggest" class="btn btn-info">View confusion matrix</a> ');
                worstTermList.append('<a href="#tabs-annotations-<%= project %>-all-<%= userJob %>" class="btn btn-info">View predicted galleries</a>', {project : self.project.id, userJob : self.model.get("userJob")});

                var fillMatrix = function(){
                    $(self.el).find('#userRetrievalSuggestMatrixDataTable').empty();
                    new StatsRetrievalSuggestionMatrixModel({job: self.model.id}).fetch({
                        success: function (model, response) {
                            self.drawRetrievalSuggestionTable(model, response, terms);
                        }
                    });
                }

                var modal = new CustomModal({
                    idModal : "confusionMatrixModal",
                    button : $("#matrix-suggest"),
                    header :"Confusion Matrix",
                    wide : true,
                    body :"<div id='userRetrievalSuggestMatrixDataTable'></div>",
                    callBack: fillMatrix
                });
                modal.addButtons("closeHotKeys","Close",true,true);
            }
        );
    },
    linkAnnotationMapWithBadTerm: function ($item, term, suggestTerm, terms,modalId) {
        var self = this;
         //init modal for job filter
         var modalCompare = new CustomModal({
             idModal : modalId,
             button :  $item,
             header :"",
             body :"<div id='annotationQuestionable'></div>",
             xwide : true,
             callBack: function() {
                 new AnnotationCollection({project: self.project.id, term: term, suggestedTerm: suggestTerm, jobForTermAlgo: self.model.get('userJob')}).fetch({
                     success: function (collection, response) {
                         new AnnotationQuestionableView({
                             model: collection,
                             container: self,
                             el: "#annotationQuestionable",
                             terms: terms,
                             term: term,
                             suggestTerm: suggestTerm
                         }).render();

                     }
                 });
             }
         });
         modalCompare.addButtons("closeCompare","Close",false,true);
    },

    tableElement: 'userRetrievalSuggestMatrixDataTable',
    tableElementHtml: 'userRetrievalSuggestMatrixDataTableHtml',
    addLine: function (idLine) {
        $('#userRetrievalSuggestMatrixDataTableHtml').append('<tr onMouseOver="this.className=\'confusionMatrixBadValueHover\'" id="' + idLine + '" class="confusionMatrixRow"></tr>');
    },
    addCell: function (idLine, idColumn, value, style) {
        this.addCell(idLine, idColumn, value, style, '');
    },
    addCell: function (idLine, idColumn, value, style, tooltip) {
        var modalId = 'confusionMatrix'+idLine+"-"+idColumn+'Modal';
        var el = $("#userRetrievalSuggestMatrixDataTableHtml");
        var dataToggle = '';
        if (tooltip) dataToggle = 'data-toggle="tooltip"';
        el.find("tr#" + idLine).append(_.template('<td id="<%= idColumn %>" title="<%= title %>" <%= dataToggle %> class="<%= style %>"><a data-Toggle="modal" href="#'+modalId+'">' + value + '</a></td>', {idColumn : idColumn, title : tooltip, dataToggle : dataToggle, style : style}));
        var cell = el.find("tr#" + idLine).find("td#" + idColumn);
        if (tooltip) cell.tooltip();
        if (idLine > 0 && idColumn > 0 && value != '') {
            this.linkAnnotationMapWithBadTerm(cell.find("a"), idLine, idColumn, this.terms,modalId)
        }
    },
    drawRetrievalSuggestionTable: function (model, response, terms) {
        var self = this;
        this.terms = terms;
        var matrixJSON = model.get('matrix');

        if (matrixJSON == undefined) {
            return;
        }

        var matrix = eval('(' + matrixJSON + ')');

        $('#userRetrievalSuggestMatrixDataTable').append('<table id="userRetrievalSuggestMatrixDataTableHtml" class="table table-condensed"></table>');
        //add title line
        self.addLine(-1);

        //add topleft cell
        self.addCell(-1, -1, 'X', 'confusionMatrixHeader');

        //add each header cell
        for (var i = 1; i < matrix[0].length - 1; i++) {
            var termName = "";
            var term = terms.get(matrix[0][i]);
            if (term != undefined) {
                termName = self.reduceTermName(term.get('name'));
            }
            self.addCell(-1, term.id, termName, 'confusionMatrixHeader', term.get('name'));
            self.addLine(term.id);
            self.addCell(term.id, -1, termName, 'confusionMatrixHeader', term.get('name'));
        }
        self.addCell(-1, 'total', 'total', 'confusionMatrixHeader');

        for (i = 0; i < matrix.length - 1; i++) {

            var indx = i + 1;

            for (j = 0; j < matrix[indx].length; j++) {

                //diagonal
                if (indx == j) {
                    self.addCell(matrix[0][j], matrix[indx][0], '<a>' + matrix[indx][j] + '</a>', 'confusionMatrixDiagonal', "Suggest Term " + terms.get(matrix[0][j]).get('name') + " for annotation " + terms.get(matrix[indx][0]).get('name'));

                }
                else if (j == 0) {
                    //first column
                    var idTerm = matrix[indx][j];
                    var term = terms.get(idTerm);
                    self.addCell(matrix[0][j], matrix[indx][0], term.get('name'), 'confusionMatrixHeader');

                }
                else if (j == matrix[indx].length - 1) {
                    //total column, fill at the end
                }
                else {
                    //value
                    if (matrix[indx][j] > 0 && j > 0) {
                        //bad value, should be 0
                        self.addCell(matrix[indx][0], matrix[0][j], '<a>' + matrix[indx][j] + '</a>', 'confusionMatrixBadValue', "Suggest Term " + terms.get(matrix[0][j]).get('name') + " for annotation " + terms.get(matrix[indx][0]).get('name'));
                    } else {
                        self.addCell(matrix[indx][0], matrix[0][j], '', 'confusionMatrixSimple', "Suggest Term " + terms.get(matrix[0][j]).get('name') + " for annotation " + terms.get(matrix[indx][0]).get('name'));
                    }
                }
            }
        }
        var indx = matrix.length - 1;
        for (i = 0; i < matrix.length; i++) {
            var printValue = ""
            var value = matrix[i][matrix[i].length - 1];
            if (value != -1) {
                printValue = Math.round(value * 100) + "%";
            }
            self.addCell(matrix[0][i], 'total', printValue, 'confusionMatrixSimple');
        }

    },
    drawWorstTermPieChart: function (model, response, terms) {
        var elName = "#worstTermprojectPieChart";
        var el = $(this.el).find(elName);
        el.empty();
        var dataJSON = model.get('worstTerms');
        if (dataJSON == undefined) {
            el.hide();
            return;
        }

        el.html("<svg></svg>");
        var colors = [];
        var chartData = [];
        for (var i = 0; i < dataJSON.length; i++) {
            chartData.push({
                label : dataJSON[i].name,
                value : dataJSON[i].rate
            });
            colors.push(dataJSON[i].color);
        }

        nv.addGraph(function() {
            var chart = nv.models.pieChart()
                .x(function(d) { return d.label })
                .y(function(d) { return d.value })
                .showLabels(true)
                .color(colors);

            d3.select(elName + " svg")
                .datum(chartData)
                .transition().duration(1200)
                .call(chart);

            nv.utils.windowResize(chart.update);

            return chart;
        });
    },

    drawWorstAnnotationsTable: function (model, response, terms) {
        var self = this;
        var elName = "#worstAnnotationPanel";
        var el = $(elName);
        var annotationsTerms = model.get('worstAnnotations');
        if (annotationsTerms == undefined) {
            el.hide();
            return;
        }
        require([
            "text!application/templates/dashboard/SuggestedAnnotationTerm.tpl.html"],
            function (suggestedAnnotationTermTpl) {
                el.empty();

                if (annotationsTerms.length == 0) {
                    el.append("You must run Retrieval Validate Algo for this project...");
                }

                for (var i = 0; i < annotationsTerms.length; i++) {
                    var annotationTerm = annotationsTerms[i];
                    var rate = Math.round(annotationTerm.rate * 100) - 1 + "%";
                    var suggestedTerm = terms.get(annotationTerm.term).get('name');
                    var termsAnnotation = terms.get(annotationTerm.expectedTerm).get('name');
                    var text = "Annotation " + annotationTerm.annotation + " is predicted " + suggestedTerm + " instead of " + termsAnnotation;
                    var cropStyle = "block";
                    var cropURL = annotationTerm.cropURL;
                    var action = _.template(suggestedAnnotationTermTpl, {idProject: self.project.id, idAnnotation: annotationTerm.annotation, idImage: annotationTerm.image, icon: "add.png", text: text, rate: rate, cropURL: cropURL, cropStyle: cropStyle});
                    el.append(action);
                }
            }
        );
    },
    drawAVGEvolution: function (model, response) {
        var self = this;
        var elName = "#avgEvolutionLineChartPanel";
        var el = $(elName);

        // Create and populate the data table.
        var evolution = model.get('evolution');
        if (evolution == undefined) {
            $(self.el).find("#avgEvolutionLineChartPanel").hide();
            return;
        }

        el.html("<svg></svg>");
        var chartData = [
            {
                key : "Number of user annotations" ,
                bar: false,
                area: false,
                values : []
            },
            {
                key : "Success rate (%)" ,
                bar: false,
                area: true,
                values : []
            }
        ];

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
            if (evolution[i].avg != -1) {
                avg = (evolution[i].avg * 100);
            }
            chartData[0].values.push({
                date : date,
                value : evolution[i].size
            });
            chartData[1].values.push({
                date : date,
                value : avg
            });

        }

        chartData.map(function(series) {
            series.values = series.values.map(function(d) { return {x: d.date, y: d.value } }).reverse();
            return series;
        });

        var chart;

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

            d3.select(elName + ' svg')
                .datum(chartData)
                .transition().duration(500).call(chart);

            nv.utils.windowResize(chart.update);

            return chart;
        });
    }
});