var RetrievalAlgoResult = Backbone.View.extend({
   //model = job
    //terms
    //annotations
    //el

    annotations : null,
    terms : null,
   initialize: function(options) {
      this.annotations = options.annotations;
      this.terms = options.terms;
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
      console.log("loadResult for job="+self.model.id);
       console.log("width="+ $(self.el).width());
      var content = _.template(retrievalAlgoViewTpl, {
         width :  ($(self.el).width()-10)+"px"
      });
       console.log("elem="+$(self.el));
      $(self.el).empty();
      $(self.el).append(content);

       console.log("StatsRetrievalSuggestionWorstTermWithSuggest");
      new StatsRetrievalSuggestionWorstTermWithSuggest({job:self.model.id}).fetch({
         success : function(model, response) {
             self.drawWorstTermTable(model,response, self.terms);

         }
      });

       console.log("StatsRetrievalSuggestionWorstTermModel");
      new StatsRetrievalSuggestionWorstTermModel({job:self.model.id}).fetch({
         success : function(model, response) {
                     self.drawWorstTermPieChart(model,response, self.terms);

         }
      });

       console.log("StatsRetrievalSuggestionWorstAnnotationModel");
      new StatsRetrievalSuggestionWorstAnnotationModel({job:self.model.id}).fetch({
         success : function(model, response) {
                     self.drawWorstAnnotationsTable(model,response, self.terms,self.annotations);

         }
      });

         console.log("StatsRetrievalSuggestionEvolutionModel");
       new StatsRetrievalSuggestionEvolutionModel({job:self.model.id}).fetch({
          success : function(model, response) {
                      self.drawAVGEvolution(model,response);

          }
       });
   },
   reduceTermName : function(termName) {
        //var termReduce = termName.substring(0,Math.min(4,termName.length));
       var termReduce="";
       var termNameItems = termName.split(" ");
       for (var i=0; i<termNameItems.length; i++) {
            termReduce = termReduce + termNameItems[i].substring(0,Math.min(4,termNameItems[i].length))+ ". ";
       }
       return termReduce;
   },
    //worstTermList
    drawWorstTermTable : function (model, response,terms) {
        console.log("drawWorstTermTable");
        var termList = model.get('worstTerms');
         console.log(model);
        console.log(model.get('project'));
        console.log(model.get('worstTerms'));
        if(termList==undefined)
        {
            $("#worstTermListPanel").hide();
            return;
        }
        var self = this;
        require([
            "text!application/templates/dashboard/WorstTermList.tpl.html"],
                function(worstTermListTpl) {
                        $("#worstTermList").empty();

                            console.log(terms);

                            terms.each(function(term) {

                                console.log("term="+term.get('name'));
                                var action = _.template(worstTermListTpl, {term:term.get('name'),id:term.id, idProject:self.model.id});

                                var max = 3;
                                var entry = termList[term.id];
                                if(entry.length>0) $("#worstTermList").append(action); //if no annotation, don't print info
                                for(var i=0;i<entry.length && i<max;i++) {
                                    //console.log(entry[i]);
                                    for(var propertyName in entry[i]) {
                                        if(propertyName!=term.id) {
                                            var elemId =  "term" + term.id + "suggest"+ propertyName;
                                            console.log("elemId="+elemId);
                                             $("#list-suggest-"+term.id).append("<a id=\""+elemId+ "\"><b>"+terms.get(propertyName).get('name') + "</b> ("+entry[i][propertyName] + "%) </a>");
                                            console.log("elemId action="+elemId + " " + $("#"+elemId).length);






                                            self.linkAnnotationMapWithBadTerm( $("#"+elemId),term.id,propertyName,terms);

//                                            $("#"+elemId).click(function() {
//                                                var idTerm = term.id;
//                                                var idTermSuggest = propertyName;
//                                                //alert('elemId='+elemId);
//                                                 alert('Click term ' + terms.get(idTerm).get('name') + " suggest " + terms.get(idTermSuggest).get('name'));
//                                            });

                                            console.log(entry[i][propertyName]);
                                        } else {
                                              $("#success-suggest-"+term.id).html("");
                                              $("#success-suggest-"+term.id).append(entry[i][propertyName]);
                                        }

                                    }

                                }
                            });

                            $("#worstTermList").append('<br><button id="matrix-suggest" class="btn">See full information</button>');
                            $("#matrix-suggest").button();
                            $('#matrix-suggest').click(function(){
                                self.initMatrixDialog(terms);
                            });
                    }
                );
    },
    linkAnnotationMapWithBadTerm: function($item, term, suggestTerm, terms) {
        var self = this;
        $item.click(function() {

            $('#annotationQuestionable').replaceWith("");
            $("#annotationQuestionableMain").empty();
            $("#annotationQuestionableMain").append("<div id=\"annotationQuestionable\"></div>");

           new AnnotationCollection({project:self.model.get('id'),term:term, suggestTerm:suggestTerm}).fetch({
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
        $('#userRetrievalSuggestMatrixDataTable').empty();
      new StatsRetrievalSuggestionMatrixModel({project:self.model.get('id')}).fetch({
         success : function(model, response) {
            self.drawRetrievalSuggestionTable(model,response, terms);
             $("#userRetrievalSuggestMatrixDataTable").dialog({
                 modal : true,
                 minWidth : Math.round($(window).width() - 75),
                 minHeight : Math.round($(window).height() - 75),
                 buttons: [
                {
                    text: "Ok",
                    click: function() { $(this).dialog("close"); }
                }
            ] });

         }
      });
    },
    tableElement : 'userRetrievalSuggestMatrixDataTable',
    tableElementHtml : 'userRetrievalSuggestMatrixDataTableHtml',
    addLine: function(idLine) {
        console.log('<tr id="' + idLine + '" class="confusionMatrixRow"></tr>');
       $('#userRetrievalSuggestMatrixDataTableHtml').append('<tr onMouseOver="this.className=\'confusionMatrixBadValueHover\'" id="' + idLine + '" class="confusionMatrixRow"></tr>');
    },
    addCell: function(idLine, idColumn, value,style) {
       this.addCell(idLine,idColumn,value,style,'');
    },
    addCell: function(idLine, idColumn, value,style,tooltip) {
       console.log('<td id="' + idColumn + '">'+value+'</td> => add to tr#'+idLine);
       $("#userRetrievalSuggestMatrixDataTableHtml").find("tr#"+idLine).append('<td id="' + idColumn + '"title="'+tooltip+'" class="'+style+'">'+value+'</td>');
        var elem = $("#userRetrievalSuggestMatrixDataTableHtml").find("tr#"+idLine).find("td#"+idColumn);
        if(tooltip!='' && tooltip!=undefined) {
            elem.tooltip();
        }

        if(idLine>0 && idColumn>0 && value!='') {
            console.log("term="+this.terms.get(idLine) + " suggest="+this.terms.get(idColumn));
            this.linkAnnotationMapWithBadTerm(elem,idLine,idColumn,this.terms)
        }
    },
   drawRetrievalSuggestionTable: function(model, response, terms){
       var self = this;
       this.terms = terms;
       var matrixJSON = model.get('matrix');

       if(matrixJSON==undefined) return;

       var matrix = eval('('+matrixJSON +')');

       $('#userRetrievalSuggestMatrixDataTable').append('<table id="userRetrievalSuggestMatrixDataTableHtml" class="table table-condensed"></table>');
       //add title line
       self.addLine(-1);

       //add topleft cell
       self.addCell(-1,-1,'X','confusionMatrixHeader');

       //add each header cell
       for(var i = 1; i<matrix[0].length-1;i++){
           var termName ="";
           var term = terms.get(matrix[0][i]);
           if(term!=undefined) termName = self.reduceTermName(term.get('name'));
           self.addCell(-1,term.id,termName,'confusionMatrixHeader',term.get('name'));
           self.addLine(term.id);
           self.addCell(term.id,-1,termName,'confusionMatrixHeader',term.get('name'));
       }
       self.addCell(-1, 'total', 'total','confusionMatrixHeader');

       for(i = 0; i<matrix.length-1;i++){

           var indx = i+1;

           for(j=0; j<matrix[indx].length;j++) {

               //diagonal
               if(indx==j) {
                   self.addCell(matrix[0][j],matrix[indx][0],'<a>'+matrix[indx][j]+'</a>','confusionMatrixDiagonal',"Suggest Term " + terms.get(matrix[0][j]).get('name') + " for annotation " + terms.get(matrix[indx][0]).get('name'));

               }
               else if(j==0) {
                   //first column
                   var idTerm = matrix[indx][j];
                   var term = terms.get(idTerm);
                   //data.setCell(i, j,term.get('name'));
                   console.log("ADD COLUMN "+term.get('name') +" id="+term.id);
                   self.addCell(matrix[0][j],matrix[indx][0],term.get('name'),'confusionMatrixHeader');

               }
               else if(j==matrix[indx].length-1) {
                   //total column, fill at the end
               }
               else {
               //value
                    if(matrix[indx][j]>0 && j>0) {
                        //bad value, should be 0
                        self.addCell(matrix[indx][0],matrix[0][j],'<a>'+matrix[indx][j]+'</a>','confusionMatrixBadValue',"Suggest Term " + terms.get(matrix[0][j]).get('name') + " for annotation " + terms.get(matrix[indx][0]).get('name'));
                    }else {
                        self.addCell(matrix[indx][0],matrix[0][j],'','confusionMatrixSimple',"Suggest Term " + terms.get(matrix[0][j]).get('name') + " for annotation " + terms.get(matrix[indx][0]).get('name'));
                        }
                    }
             }
       }
       var indx = matrix.length-1;
       for(i=0; i<matrix.length;i++) {
           var printValue =""
           var value = matrix[i][matrix[i].length-1];
           if(value!=-1) {
               printValue = Math.round(value*100)+"%";
           }
           self.addCell(matrix[0][i],'total',printValue,'confusionMatrixSimple');
       }

   },
   drawRetrievalSuggestionTableGoogleCharts: function(model, response, terms){
      var self = this;
       var visualization = new google.visualization.Table(document.getElementById('userRetrievalSuggestMatrixDataTable'));
        var matrixJSON = model.get('matrix');
       if(matrixJSON==undefined) return;

        var matrix = eval('('+matrixJSON +')');
       var data = new google.visualization.DataTable();
       //add column
       data.addColumn('string', 'X');
       for(var i = 1; i<matrix[0].length-1;i++){
           var termName ="";
           var term = terms.get(matrix[0][i]);
           if(term!=undefined) termName = self.reduceTermName(term.get('name'));
           data.addColumn('number', termName);
       }
       data.addColumn('string', matrix[0][matrix[0].length-1]);
       data.addRows((matrix.length-1));

//       data.setColumnProperties(2, {style: 'font-style:bold; width : 500px;'});  => dosen't work :-(
//       data.setColumnProperty(2, "width", "500px");     => dosen't work :-(

         for(i = 0; i<matrix.length-1;i++){
             var indx = i+1;
             //console.log("i:"+i);

             for(j=0; j<matrix[indx].length;j++) {
                 //console.log("j:"+j);
                var value = matrix[indx][j];
                 //console.log("value:"+value);

                    if(indx==j) {
                        //diagonal
                        data.setCell(i, j, matrix[indx][j],undefined,{style: 'font-style:bold; background-color:#90c140;'});
                    } else if(j==0) {
                        //first column
                        var idTerm = matrix[indx][j];
                        var term = terms.get(idTerm);
                        data.setCell(i, j,term.get('name'));
                    } else if(j==matrix[indx].length-1) {
                        //last column
                        var printValue =""
                        var value = matrix[indx][j];
                        if(value!=-1) {
                           printValue = Math.round(value*100)+"%";
                        }
                        data.setCell(i, j, printValue);
                    }
                    else {
                        //value
                        if(matrix[indx][j]>0 && j>0) {
                            //bad value, should be 0
                            data.setCell(i, j, matrix[indx][j],undefined,{style: 'font-style:bold; background-color:#ff5800;'});
                        }else {
                            //good value (=0)
                            //don't print 0
                        }
                    }
             }
       }
       var width = Math.round($(window).width() - 95);
      google.visualization.events.addListener(visualization, 'select', function() {
                console.log(visualization.getSelection());
                  var selection  = visualization.getSelection();
                  for (var i = 0; i < selection.length; i++) {
                        var item = selection[i];
                        console.log("item.row="+item.row +" item.column="+item.column);
                      //TODO: cannot GET COLUMN ID!!! (item.row => OK, item.column = undefined
//                        if (item.row != null && item.column != null) {
//
//                        }
                  }

        });
      visualization.draw(data, {title:"",
             legend : "none",
             //backgroundColor : "whiteSmoke",
             width:"98%",
             allowHtml : true
          });

   },
   drawWorstTermPieChart : function (model, response, terms) {
      $("#worstTermprojectPieChart").empty();
      var dataJSON = model.get('worstTerms');
       if(dataJSON==undefined) {
           $("#worstTermPieChartPanel").hide();
           return;
       }

        //var worstTerm = eval('('+dataJSON +')');
      // Create and populate the data table.
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Term');
      data.addColumn('number', 'Number of questionable annotations');
      data.addRows(dataJSON.length);
      var colors = [];
       for(var i=0;i<dataJSON.length;i++) {
         colors.push(dataJSON[i].color);
         data.setValue(i,0, dataJSON[i].name);
         data.setValue(i,1, dataJSON[i].rate);
       }
      var width = Math.round($(window).width()/2 - 95);
      // Create and draw the visualization.
      new google.visualization.PieChart(document.getElementById('worstTermprojectPieChart')).
          draw(data, {width: width, height: 350,title:"", backgroundColor : "whiteSmoke",colors : colors});
   },

    drawWorstAnnotationsTable : function (model, response,terms, annotations) {
        console.log("drawWorstAnnotationsTable");
        var annotationsTerms = model.get('worstAnnotations');
         if(annotationsTerms==undefined) {
             $("#worstAnnotationPanel").hide();
             return;
         }
        var self = this;
        require([
            "text!application/templates/dashboard/SuggestedAnnotationTerm.tpl.html"],
                function(suggestedAnnotationTermTpl) {
                        $("#worstannotationitem").empty();

                        if(annotationsTerms.length==0) {
                            $("#worstannotationitem").append("You must run Retrieval Validate Algo for this project...");
                        }

                        for(var i=0;i<annotationsTerms.length;i++) {
                            var annotationTerm = annotationsTerms[i];
                            var rate = Math.round(annotationTerm.rate*100)-1+"%";
                            var annotation = annotations.get(annotationTerm.annotation);
                            var suggestedTerm = terms.get(annotationTerm.term).get('name');
                            var termsAnnotation = terms.get(annotationTerm.expectedTerm).get('name');
//                            _.each(annotation.get('term'), function(idTerm){ realTerms.push(terms.get(idTerm).get('name')); });
                            //var termsAnnotation =  realTerms.join();
                            var text = "<b>" + suggestedTerm +"</b> for annotation " + annotation.id + " instead of <b>" + termsAnnotation +"</b>";

                            var cropStyle = "block";
                            var cropURL = annotation.get("cropURL");

                            var action = _.template(suggestedAnnotationTermTpl, {idProject : self.model.id, idAnnotation : annotation.id, idImage : annotation.get('image'), icon:"add.png",text:text,rate:rate,cropURL:cropURL, cropStyle:cropStyle});
                            $("#worstannotationitem").append(action);
                        }
                    }
                );
    },
    drawAVGEvolution : function (model, response) {
        // Create and populate the data table.
        var evolution = model.get('evolution');
        if(evolution==undefined) {
            $("#avgEvolutionLineChartPanel").hide();
            return;
        }
        var data = new google.visualization.DataTable();
        data.addColumn('date', 'Date');
        data.addColumn('number', 'Success rate (%)');
//        var date1 = new Date();
//        date1.setTime(1325576699000);
//        var date2 = new Date(2012, 0, 10);
//        var date3 = new Date(2012, 0, 15);
//        var date4 = new Date(2012, 1, 10);

        for(var i=0;i<evolution.length;i++) {
            var date = new Date();
            date.setTime(evolution[i].date);
            data.addRow([date, evolution[i].avg]);
        }
//        data.addRow([date1, 60]);
//        data.addRow([date2, 70]);
//        data.addRow([date3, 72]);
//        data.addRow([date4, 79]);
//        data.addRow([new Date(2012, 0, 1), 10]);
//        data.addRow([new Date(2012, 0, 10), 15]);
//        data.addRow([new Date(2012, 0, 15), 40]);
//        data.addRow([new Date(2012, 1, 10), 50]);

         var width = Math.round($(window).width()/2 - 95);
        // Create and draw the visualization.
        var chart = new google.visualization.AreaChart(
            document.getElementById('avgEvolutionLineChart'));
        chart.draw(data, {title: '',
                          width: width, height: 350,
                          vAxis: {title: "Success rate",minValue:0,maxValue:100},
                          hAxis: {title: "Time"},
                          backgroundColor : "whiteSmoke",
                          lineWidth: 1}
                  );
        //          draw(data, {width: width, height: 350,title:"", backgroundColor : "whiteSmoke",colors : colors});
    }
    //drawWorstAnnotationsTable
});