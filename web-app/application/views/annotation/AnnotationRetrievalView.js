var AnnotationRetrievalView = Backbone.View.extend({
    tagName : "div",
    baseAnnotation : null,
    terms : null,
    initialize: function(options) {
        this.container = options.container;
        this.page = options.page;
        this.annotations = null; //array of annotations that are printed
        this.baseAnnotation = options.baseAnnotation;
        this.terms = options.terms;
        window.app.status.currentTermsCollection = options.terms;
        if (this.page == undefined) this.page = 0;
    },
    render: function() {

        var self = this;
        console.log("AnnotationRetrievalView: main elem "+$(self.el).length);
        $("#retrievalMenu").replaceWith("");
        $("#retrievalThumb").replaceWith("");
        $("#retrievalPieChart").replaceWith("");
        $(self.el).append("<ul id=\"retrievalMenu\"><li><a href=\"#retrievalThumb\">Thumb view</a></li><li><a href=\"#retrievalPieChart\">Stats view</a></li></ul>");
        $(self.el).append("<div id=\"retrievalThumb\"><div>");
        $(self.el).append("<div id=\"retrievalPieChart\"><div id=\"retrievalPieChartTerm\" style=\"float: left; width: 50%;\"></div><div id=\"retrievalPieChartProject\" style=\"float: left; width: 50%;\"></div></div>");




                $("#annotationRetrieval").tabs();
                $(self.el).dialog({
                    title : self.createTitle(),
                    width: 900,
                    height: 500,
                    autoOpen : true,
                    modal:true,
                    buttons : {
                        "Close" : function() {
                            $(self.el).dialog("close");

                        }
                    }
                });
                self.createThumbView(self.page);
                self.createStatsView();
        return this;

    },
    createTitle : function() {
        var self = this;
        var id = self.baseAnnotation.get('id');
        var termsNameArray = new Array();
        var termArray = self.baseAnnotation.get('term');
        _.each(termArray, function(termid){
            //console.log(termid);
            //console.log(self.terms);
            if(self.terms.get(termid)!=undefined) {
                termsNameArray.push(self.terms.get(termid).get('name'));
            }
        });
        return "Annotation " + id + " with term " +  termsNameArray.join(',');

    },
    createThumbView : function(page) {
        this.appendThumbs(page);
    },
    createStatsView : function() {
        var self = this;
        //Get term from annotation
        var dataTerm = new Object();
        self.model.each(function(annotation) {
            var termArray = annotation.get('term');
            _.each(termArray, function(termid){
                console.log(termid);
                console.log(self.terms);
                var isTermFromCurrentProject = self.terms!=undefined && self.terms.get(termid)!=undefined;
                if(isTermFromCurrentProject) {
                    if(dataTerm[termid] != null ) {
                        dataTerm[termid] = dataTerm[termid] + Number(annotation.get('similarity'));
                    } else {
                        dataTerm[termid] = Number(annotation.get('similarity'));
                    }
                }
            });
        });
        console.log("drawPieChartTerm");
        self.drawPieChartTerm(dataTerm);

        var dataProject = new Object();
        self.model.each(function(annotation) {
            var projectId = annotation.get('project');
            if(dataProject[projectId] != null ) {
                dataProject[projectId] = dataProject[projectId] + 1;
            } else {
                dataProject[projectId] = 1;
            }
        });
        console.log("drawPieChartProject");

        window.app.models.projects.fetch({
            success : function (collection, response) {
                self.drawPieChartProject(dataProject,collection);
        }});
    },
    drawPieChartTerm : function (collection) {
        var self = this;
        $("#retrievalPieChartTerm").empty();
        // Create and populate the data table.
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Term');
        data.addColumn('number', 'Similarity weighted sum');
        data.addRows(_.size(collection));
        var i = 0;
        var colors = [];

        for (var key in collection) {
            if (collection.hasOwnProperty(key)) {
                var value = collection[key];
                var term = self.terms.get(key);
                //colors.push(term.get('color'));
                data.setValue(i,0, term.get('name'));
                data.setValue(i,1, value);
                i++;
            }
        }

        // Create and draw the visualization.
        new google.visualization.PieChart(document.getElementById('retrievalPieChartTerm')).
                draw(data, {width: 500, height: 350,title:""});
    },


    drawPieChartProject : function (collection, projects) {
        var self = this;
        $("#retrievalPieChartProject").empty();
        // Create and populate the data table.
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Project');
        data.addColumn('number', 'Similarity in project');
        data.addRows(_.size(collection));
        var i = 0;
        var colors = [];

        console.log(collection);
        for (var key in collection) {
            if (collection.hasOwnProperty(key)) {
                var value = collection[key];
                var projectName = "Project not accessible"
                if(projects.get(key)!=undefined) {
                    projectName = projects.get(key).get('name');
                }
                //colors.push(term.get('color'));
                data.setValue(i,0, projectName);
                data.setValue(i,1, value);
                i++;
            }
        }
        console.log("creation:"+$("#retrievalPieChartProject").length);
        // Create and draw the visualization.
        new google.visualization.PieChart(document.getElementById('retrievalPieChartProject')).
                draw(data, {width: 500, height: 350,title:""});
    },

    appendThumbs : function(page) {
        var self = this;
        var cpt = 0;
        var nb_thumb_by_page = 2500;
        var inf = Math.abs(page) * nb_thumb_by_page;
        var sup = (Math.abs(page) + 1) * nb_thumb_by_page;

        self.annotations = new Array();

        var thumb = new AnnotationThumbView({
            model : self.baseAnnotation,
            className : "thumb-wrap",
            id : "annotationthumb"+self.baseAnnotation.get('id')
        }).render();
        $(thumb.el).css("border","50px");
        $(thumb.el).css("color","green");
        $(thumb.el).css("background-color","green");
        $("#retrievalThumb").append(thumb.el);

        self.model.each(function(annotation) {

            if ((cpt >= inf) && (cpt < sup)) {
                annotation.set({name:annotation.get('similarity')});
                var annotationModel = new AnnotationModel(annotation);

                  var thumb = new AnnotationThumbView({
                    model : annotationModel,
                    className : "thumb-wrap",
                    id : "annotationthumb"+annotationModel.id
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
    add : function(annotation) {

        var self = this;
        var thumb = new AnnotationThumbView({
            model : annotation,
            className : "thumb-wrap",
            id : "thumb"+annotation.get('id')
        }).render();
        $(self.el).prepend(thumb.el);

    },
    /**
     * Remove thumb annotation with id
     * @param idAnnotation  Annotation id
     */
    remove : function (idAnnotation) {
        $("#thumb"+idAnnotation).remove();
    },
    /**
     * Refresh thumb with newAnnotations collection:
     * -Add annotations thumb from newAnnotations which are not already in the thumb set
     * -Remove annotations which are not in newAnnotations but well in the thumb set
     * @param newAnnotations newAnnotations collection
     */
    refresh : function(newAnnotations) {
        var self = this;

        var arrayDeletedAnnotations = self.annotations;
        newAnnotations.each(function(annotation) {
            //if annotation is not in table, add it
            if(_.indexOf(self.annotations, annotation.id)==-1){
                self.add(annotation);
                self.annotations.push(annotation.id);
            }
            /*
             * We remove each "new" annotation from  arrayDeletedAnnotations
             * At the end of the loop, element from arrayDeletedAnnotations must be deleted because they aren't
             * in the set of new annotations
             */
            //
            arrayDeletedAnnotations = _.without(arrayDeletedAnnotations,annotation.id);

        });

        arrayDeletedAnnotations.forEach(function(removeAnnotation) {
            self.remove(removeAnnotation);
            self.annotations = _.without(self.annotations,removeAnnotation);
        });

    }


});
