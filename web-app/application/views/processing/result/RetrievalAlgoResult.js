var RetrievalAlgoResult = Backbone.View.extend({
    //model = job
    //terms
    //annotations
    //el
    width : null,
    project : null,
    annotations : null,
    terms : null,
    jobs: null,
    software : null,
    initialize: function(options) {
        this.annotations = options.annotations;
        this.terms = options.terms;
        this.project = options.project;
        this.jobs = options.jobs;
        this.software = options.software;
    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/processing/RetrievalAlgoResult.tpl.html"
        ],
               function(retrievalAlgoViewTpl) {
                   self.loadResult(retrievalAlgoViewTpl);
               });
        return this;
    },
    loadResult : function (retrievalAlgoViewTpl) {
        var self = this;
        var width = ((($(self.el).width() - 125) / 2)) + "px"
        var height = "400px"
        var content = _.template(retrievalAlgoViewTpl, {
            width : width,
            height : height
        });
        self.width = width;
        console.log($(self.el));
        $(self.el).empty();
        $(self.el).append(content);

        console.log("StatsRetrievalSuggestionWorstTermWithSuggest:" + self.model.id);
        new StatsRetrievalSuggestionWorstTermWithSuggest({job:self.model.id}).fetch({

            success : function(model, response) {
                console.log("StatsRetrievalSuggestionWorstTermWithSuggest model:");
                console.log(model);
                self.drawWorstTermTable(model, response, self.terms);

            }
        });

        console.log("StatsRetrievalSuggestionWorstTermModel");
        new StatsRetrievalSuggestionWorstTermModel({job:self.model.id}).fetch({
            success : function(model, response) {
                self.drawWorstTermPieChart(model, response, self.terms);

            }
        });

        console.log("StatsRetrievalSuggestionWorstAnnotationModel");
        new StatsRetrievalSuggestionWorstAnnotationModel({job:self.model.id}).fetch({
            success : function(model, response) {
                self.drawWorstAnnotationsTable(model, response, self.terms, self.annotations);

            }
        });

        console.log("StatsRetrievalSuggestionEvolutionModel");
        new StatsRetrievalSuggestionEvolutionModel({job:self.model.id}).fetch({
            success : function(model, response) {
                self.drawAVGEvolution(model, response);

            }
        });
    },
    reduceTermName : function(termName) {
        //var termReduce = termName.substring(0,Math.min(4,termName.length));
        var termReduce = "";
        var termNameItems = termName.split(" ");
        for (var i = 0; i < termNameItems.length; i++) {
            termReduce = termReduce + termNameItems[i].substring(0, Math.min(4, termNameItems[i].length)) + ". ";
        }
        return termReduce;
    },
    //worstTermList
    drawWorstTermTable : function (model, response, terms) {

        var termList = model.get('worstTerms');
        if (termList == undefined) {
            $(self.el).find("#worstTermListPanel").hide();
            return;
        }
        var self = this;
        require([
            "text!application/templates/dashboard/WorstTermList.tpl.html"],
               function(worstTermListTpl) {
                   $(self.el).find("#worstTermList").empty();


                   terms.each(function(term) {

                       var action = _.template(worstTermListTpl, {term:term.get('name'),id:term.id, idProject:self.project.id});

                       var max = 3;
                       var entry = termList[term.id];
                       if (entry.length > 0) $(self.el).find("#worstTermList").append(action); //if no annotation, don't print info
                       for (var i = 0; i < entry.length && i < max; i++) {

                           for (var propertyName in entry[i]) {
                               if (propertyName != term.id) {
                                   var elemId = "term" + term.id + "suggest" + propertyName;

                                   $(self.el).find("#list-suggest-" + term.id).append("<a id=\"" + elemId + "\"><b>" + terms.get(propertyName).get('name') + "</b> (" + entry[i][propertyName] + "%) </a>");
                                   self.linkAnnotationMapWithBadTerm($("#" + elemId), term.id, propertyName, terms);
                               } else {
                                   $(self.el).find("#success-suggest-" + term.id).html("");
                                   $(self.el).find("#success-suggest-" + term.id).append(entry[i][propertyName]);
                               }

                           }

                       }
                   });

                   $(self.el).find("#worstTermList").append('<br><button id="matrix-suggest" class="btn">See full information</button>');
                   $(self.el).find("#matrix-suggest").button();
                   $(self.el).find('#matrix-suggest').click(function() {
                       self.initMatrixDialog(terms);
                   });
               }
                );
    },
    linkAnnotationMapWithBadTerm: function($item, term, suggestTerm, terms) {
        var self = this;
        $item.click(function() {

            $(self.el).find('#annotationQuestionable').replaceWith("");
            $(self.el).find("#annotationQuestionableMain").empty();
            $(self.el).find("#annotationQuestionableMain").append("<div id=\"annotationQuestionable\"></div>");

            new AnnotationCollection({project:self.project.id,term:term, suggestTerm:suggestTerm, job:self.model.id}).fetch({
                success : function(collection, response) {
                    var panel = new AnnotationQuestionableView({
                        model : collection,
                        container : self,
                        el : "#annotationQuestionable",
                        terms : terms,
                        term : term,
                        suggestTerm : suggestTerm
                    }).render();

                }
            });
        });
    },
    initMatrixDialog: function(terms) {
        var self = this;
        $(self.el).find('#userRetrievalSuggestMatrixDataTable').empty();
        new StatsRetrievalSuggestionMatrixModel({job:self.model.id}).fetch({
            success : function(model, response) {
                console.log("build matrix");
                self.drawRetrievalSuggestionTable(model, response, terms);
                console.log("build dialog:" + $("#userRetrievalSuggestMatrixDataTable").length);
                $("#userRetrievalSuggestMatrixDataTable").dialog({
                    modal : true,
                    minWidth : Math.round($(window).width() - 75),
                    minHeight : Math.round($(window).height() - 75),
                    buttons: [
                        {
                            text: "Ok",
                            click: function() {
                                $(this).dialog("close");
                            }
                        }
                    ],close: function(event, ui) {
                        $("#userRetrievalSuggestMatrixDataTable").empty();
                    }
                });

            }
        });
    },
    tableElement : 'userRetrievalSuggestMatrixDataTable',
    tableElementHtml : 'userRetrievalSuggestMatrixDataTableHtml',
    addLine: function(idLine) {
        $('#userRetrievalSuggestMatrixDataTableHtml').append('<tr onMouseOver="this.className=\'confusionMatrixBadValueHover\'" id="' + idLine + '" class="confusionMatrixRow"></tr>');
    },
    addCell: function(idLine, idColumn, value, style) {
        this.addCell(idLine, idColumn, value, style, '');
    },
    addCell: function(idLine, idColumn, value, style, tooltip) {
        $("#userRetrievalSuggestMatrixDataTableHtml").find("tr#" + idLine).append('<td id="' + idColumn + '"title="' + tooltip + '" class="' + style + '">' + value + '</td>');
        var elem = $("#userRetrievalSuggestMatrixDataTableHtml").find("tr#" + idLine).find("td#" + idColumn);
        if (tooltip != '' && tooltip != undefined) {
            elem.tooltip();
        }

        if (idLine > 0 && idColumn > 0 && value != '') {
            this.linkAnnotationMapWithBadTerm(elem, idLine, idColumn, this.terms)
        }
    },
    drawRetrievalSuggestionTable: function(model, response, terms) {
        var self = this;
        this.terms = terms;
        var matrixJSON = model.get('matrix');

        if (matrixJSON == undefined) return;

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
            if (term != undefined) termName = self.reduceTermName(term.get('name'));
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
    drawWorstTermPieChart : function (model, response, terms) {
        $(this.el).find("#worstTermprojectPieChart").empty();
        var dataJSON = model.get('worstTerms');
        if (dataJSON == undefined) {
            $(this.el).find("#worstTermPieChartPanel").hide();
            return;
        }

        //var worstTerm = eval('('+dataJSON +')');
        // Create and populate the data table.
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Term');
        data.addColumn('number', 'Number of questionable annotations');
        data.addRows(dataJSON.length);
        var colors = [];
        for (var i = 0; i < dataJSON.length; i++) {
            colors.push(dataJSON[i].color);
            data.setValue(i, 0, dataJSON[i].name);
            data.setValue(i, 1, dataJSON[i].rate);
        }

        // Create and draw the visualization.
        new google.visualization.PieChart($(this.el).find('#worstTermprojectPieChart')[0]).
                draw(data, {width: this.width, height: 350,title:"", backgroundColor : "whiteSmoke",colors : colors});
    },

    drawWorstAnnotationsTable : function (model, response, terms, annotations) {
        var annotationsTerms = model.get('worstAnnotations');
        if (annotationsTerms == undefined) {
            $(self.el).find("#worstAnnotationPanel").hide();
            return;
        }
        var self = this;
        require([
            "text!application/templates/dashboard/SuggestedAnnotationTerm.tpl.html"],
               function(suggestedAnnotationTermTpl) {
                   $(self.el).find("#worstannotationitem").empty();

                   if (annotationsTerms.length == 0) {
                       $(self.el).find("#worstannotationitem").append("You must run Retrieval Validate Algo for this project...");
                   }

                   for (var i = 0; i < annotationsTerms.length; i++) {
                       var annotationTerm = annotationsTerms[i];
                       var rate = Math.round(annotationTerm.rate * 100) - 1 + "%";
                       var annotation = annotations.get(annotationTerm.annotation);
                       var suggestedTerm = terms.get(annotationTerm.term).get('name');
                       var termsAnnotation = terms.get(annotationTerm.expectedTerm).get('name');
//                            _.each(annotation.get('term'), function(idTerm){ realTerms.push(terms.get(idTerm).get('name')); });
                       //var termsAnnotation =  realTerms.join();
                       var text = "<b>" + suggestedTerm + "</b> for annotation " + annotation.id + " instead of <b>" + termsAnnotation + "</b>";

                       var cropStyle = "block";
                       var cropURL = annotation.get("cropURL");

                       var action = _.template(suggestedAnnotationTermTpl, {idProject : self.project.id, idAnnotation : annotation.id, idImage : annotation.get('image'), icon:"add.png",text:text,rate:rate,cropURL:cropURL, cropStyle:cropStyle});
                       $(self.el).find("#worstannotationitem").append(action);
                   }
               }
                );
    },
    drawAVGEvolution : function (model, response) {
        var self = this;
        // Create and populate the data table.
        var evolution = model.get('evolution');
        if (evolution == undefined) {
            $(self.el).find("#avgEvolutionLineChartPanel").hide();
            return;
        }
        var data = new google.visualization.DataTable();
        data.addColumn('date', 'Date');
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
            data.addRow([date, evolution[i].avg]);
        }

        var width = Math.round($(window).width() / 2 - 150);
        // Create and draw the visualization.
        var evolChart = new google.visualization.AreaChart($(this.el).find('#avgEvolutionLineChart')[0]);
        evolChart.draw(data, {title: '',
            width: this.width, height: 350,
            vAxis: {title: "Success rate",minValue:0,maxValue:100},
            hAxis: {title: "Time"},
            backgroundColor : "whiteSmoke",
            lineWidth: 1}
                );
        evolChart.setSelection([
            {row:indiceJob,column:1}
        ]);
        var handleClick = function() {
            var row = evolChart.getSelection()[0]['row'];
            var col = evolChart.getSelection()[0]['column'];
            var dateSelected = new Date();
            dateSelected.setTime(evolution[row].date);
            if (self.jobs != null) {
                var jobSelected = null;
                self.jobs.each(function(job) {
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