var AnnotationRetrievalView = Backbone.View.extend({
    tagName: "div",
    baseAnnotation: null,
    terms: null,
    initialize: function (options) {
        this.container = options.container;
        this.page = options.page;
        this.annotations = null; //array of annotations that are printed
        this.baseAnnotation = options.baseAnnotation;
        this.terms = options.terms;
        this.bestTerms = options.bestTerms;
        this.bestTermsValue = options.bestTermsValue;
        if (this.page == undefined) {
            this.page = 0;
        }
    },
    render: function () {

        var self = this;
        console.log("AnnotationRetrievalView: main elem " + $(self.el).length);
        $("#myModalRetrieval").find("#retrievalPieChartTerm").empty();
        $("#myModalRetrieval").find("#retrievalPieChartProject").empty();
        $("#myModalRetrieval").find("#retrievalThumb").empty();

        console.log("3");
        self.createThumbView(self.page);
        console.log("4");
        self.createStatsView();
        return this;

    },
    createTitle: function () {
        var self = this;
        var id = self.baseAnnotation.get('id');
        var termsNameArray = [];

        _.each(self.bestTerms, function (term, i) {
            if (term != undefined && term != null) {
                termsNameArray.push(term.get('name') + " (" + (self.bestTermsValue[i]).toFixed(2) + "%)");
            }
        });
        return "Annotation " + id + ": similar annotations " + termsNameArray.join(', ');

    },
    createThumbView: function (page) {
        this.appendThumbs(page);
    },
    createStatsView: function () {
        var self = this;
        //Get term from annotation
        var dataTerm = {};
        self.model.each(function (annotation) {
            var termArray = annotation.get('term');
            _.each(termArray, function (termid) {
                console.log(termid);
                console.log(self.terms);
                var isTermFromCurrentProject = self.terms != undefined && self.terms.get(termid) != undefined;
                if (isTermFromCurrentProject) {
                    if (dataTerm[termid] != null) {
                        dataTerm[termid] = dataTerm[termid] + Number(annotation.get('similarity'));
                    } else {
                        dataTerm[termid] = Number(annotation.get('similarity'));
                    }
                }
            });
        });
        console.log("drawPieChartTerm");
        self.drawPieChartTerm(dataTerm);

        var dataProject = {};
        self.model.each(function (annotation) {
            var projectId = annotation.get('project');
            if (dataProject[projectId] != null) {
                dataProject[projectId] = dataProject[projectId] + 1;
            } else {
                dataProject[projectId] = 1;
            }
        });
        console.log("drawPieChartProject");

        window.app.models.projects.fetch({
            success: function (collection, response) {
                self.drawPieChartProject(dataProject, collection);
            }});
    },
    drawPieChartTerm: function (collection) {
        var self = this;
        var el = "#retrievalPieChartTerm";
        $(el).html("<svg></svg>");
        var chartData = [];
        var colors = [];
        for (var key in collection) {
            if (collection.hasOwnProperty(key)) {
                var value = collection[key];
                var term = self.terms.get(key);
                chartData.push({
                    label : term.get('name'),
                    value : value
                });
                colors.push(term.get('color'));
            }
        }
        if(BrowserSupport.isTooOld()) {
            BrowserSupport.addMessage($(el),BrowserSupport.CHARTS);
        }
        else {
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

                nv.utils.windowResize(chart.update);

                return chart;
            });
        }
    },


    drawPieChartProject: function (collection, projects) {
        var el = "#retrievalPieChartProject";
        $(el).html("<svg></svg>");
        var chartData = [];

        for (var key in collection) {
            if (collection.hasOwnProperty(key)) {
                var value = collection[key];
                var projectName = "Project not accessible"
                if (projects.get(key) != undefined) {
                    projectName = projects.get(key).get('name');
                }
                chartData.push({
                    label : projectName,
                    value : value
                });
            }
        }

        if(BrowserSupport.isTooOld()) {
            BrowserSupport.addMessage($(el),BrowserSupport.CHARTS);
        }
        else {
            nv.addGraph(function() {
                var chart = nv.models.pieChart()
                    .x(function(d) { return d.label })
                    .y(function(d) { return d.value })
                    .showLabels(true);

                d3.select(el + " svg")
                    .datum(chartData)
                    .transition().duration(1200)
                    .call(chart);

                nv.utils.windowResize(chart.update);

                return chart;
            });
        }

    },

    appendThumbs: function (page) {
        var self = this;
        var cpt = 0;
        var nb_thumb_by_page = 2500;
        var inf = Math.abs(page) * nb_thumb_by_page;
        var sup = (Math.abs(page) + 1) * nb_thumb_by_page;

        self.annotations = [];

        console.log("**************************");
        console.log(self.baseAnnotation);
        var thumb = new AnnotationThumbView({
            model: self.baseAnnotation,
            terms : window.app.status.currentTermsCollection,
            className: "thumb-wrap",
            id: "annotationthumb" + self.baseAnnotation.get('id')
        }).render();
        $(thumb.el).css("border", "50px");
        $(thumb.el).css("color", "green");
        $(thumb.el).css("background-color", "green");
        $("#retrievalThumb").append(thumb.el);

        self.model.each(function (annotation) {

            if ((cpt >= inf) && (cpt < sup)) {
                annotation.set({name: annotation.get('similarity')});
                //var annotationModel = new AnnotationModel(annotation);

                annotation.set("reviewed",false);
                var thumb = new AnnotationThumbView({
                    model: annotation,
                    terms : window.app.status.currentTermsCollection,
                    className: "thumb-wrap",
                    id: "annotationthumb" + annotation.id
                }).render();
                $("#retrievalThumb").append(thumb.el);
            }
            cpt++;
            self.annotations.push(annotation.id);
        });


    },
    /**
     * Add the thumb annotation
     * @param annotation Annotation model
     */
    add: function (annotation) {

        var self = this;
        var thumb = new AnnotationThumbView({
            model: annotation,
            terms : window.app.status.currentTermsCollection,
            className: "thumb-wrap",
            id: "thumb" + annotation.get('id')
        }).render();
        $(self.el).prepend(thumb.el);

    },
    /**
     * Remove thumb annotation with id
     * @param idAnnotation  Annotation id
     */
    remove: function (idAnnotation) {
        $("#thumb" + idAnnotation).remove();
    },
    /**
     * Refresh thumb with newAnnotations collection:
     * -Add annotations thumb from newAnnotations which are not already in the thumb set
     * -Remove annotations which are not in newAnnotations but well in the thumb set
     * @param newAnnotations newAnnotations collection
     */
    refresh: function (newAnnotations) {
        var self = this;

        var arrayDeletedAnnotations = self.annotations;
        newAnnotations.each(function (annotation) {
            //if annotation is not in table, add it
            if (_.indexOf(self.annotations, annotation.id) == -1) {
                self.add(annotation);
                self.annotations.push(annotation.id);
            }
            /*
             * We remove each "new" annotation from  arrayDeletedAnnotations
             * At the end of the loop, element from arrayDeletedAnnotations must be deleted because they aren't
             * in the set of new annotations
             */
            //
            arrayDeletedAnnotations = _.without(arrayDeletedAnnotations, annotation.id);

        });

        arrayDeletedAnnotations.forEach(function (removeAnnotation) {
            self.remove(removeAnnotation);
            self.annotations = _.without(self.annotations, removeAnnotation);
        });

    }


});
