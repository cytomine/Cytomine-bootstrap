var ProjectDashboardAlgos = Backbone.View.extend({
    rendered : false,
    jobCollection : null,
    softwares : null,
    disableSelect : false,
    software : null,
    initialize : function (options) {
        this.el = "#tabs-algos-" + this.model.id;
        this.idJob = options.idJob;
        this.idSoftware = options.idSoftware;
        this.software = options.software;
    },
    render : function() {
        console.log('render');
          var self = this;
          require([
             "text!application/templates/processing/SoftwareInfo.tpl.html"
          ],
              function(tpl) {
                 self.doLayout(tpl);
              });
           this.rendered = true;
          return this;
    },
   doLayout: function(tpl) {
      console.log("this.idJob="+this.idJob);
      console.log("this.idSoftware="+this.idSoftware);
      var self = this;
      $(this.el).empty();
      $(this.el).append(_.template(tpl, {}));

       //get all software from project and print menu
       new SoftwareCollection({ project : self.model.id}).fetch({
           success : function (collection, response) {
              self.software = collection.get(self.idSoftware);
              self.softwares = collection;
              self.initProjectSoftwareList();
              var softModel = collection.get(self.idSoftware);
               console.log("1. self.idJob="+self.idJob);
              self.printProjectSoftwareInfo();
       }});
      return this;
   },
    refresh : function() {
        console.log("refresh()" + this.idJob);
        if (!this.rendered) this.render();
        //this.printProjectJobInfo(this.idJob);
        this.software = this.softwares.get(this.idSoftware);
        this.printProjectSoftwareInfo();
    },
    refresh : function(idSoftware,idJob) {
        console.log("refresh(idSoftware,idJob)" + this.idJob);
        this.idSoftware = idSoftware;
        this.idJob = idJob;
        //this.render();
//        if(idSoftware!=this.idSoftware) {
//            this.idSoftware = idSoftware;
//            this.printProjectSoftwareInfo(this.idSoftware,idJob);
//        }
        this.software = this.softwares.get(this.idSoftware);
        this.printProjectSoftwareInfo();
        //this.printProjectJobInfo(idJob);
    },
    initProjectSoftwareList : function () {
        var self = this;

        self.softwares.each(function (software) {
            $("#projectSoftwareListUl").append('<li id="consultSoftware-' + software.id + '"><a href="#tabs-algos-'+self.model.id + '-' +software.id + '-">' + software.get('name') + '</a></li>');
            $("#projectSoftwareListUl").children().removeClass("active");
            console.log('COMPARE: |' + software.id + '|' + self.idSoftware + '|' + (software.id==self.idSoftware));

            if(software.id==self.idSoftware) {
                 $("#consultSoftware-" + software.id).addClass("active");
            }
        });
    },
    printProjectSoftwareInfo : function() {

        var self = this;
        console.log("printProjectSoftwareInfo: software=" + self.software.id);

        //Print software details
        self.printProjectSoftwareDetails( self.software);

        //Print last job + n last job details
        self.printLastNRun();


        //Print selected job from this software
        self.printProjectJobInfo( self.idJob);


          //button click run software
          $("#softwareLaunchJobButton").click(function() {
              console.log("project=" + self.model.id + " software.id=" +  self.software.id);
              new JobModel({ project : self.model.id, software :  self.software.id}).save({}, {
                  success : function (job, response) {
                      console.log("SUCCESS JOB");
                      //TODO: go to job!

                  },
                  error : function (response) {
                      console.log("BAD JOB");
                  }
              });
          });


    },
    printLastNRun : function() {
        var self = this;
         var refreshData = function() {
                new JobCollection({ project : self.model.id, software:  self.software.id, max: 3}).fetch({
                    success : function (collection, response) {
                        var job = collection.models.length>0? collection.models[0] : undefined;
                        //self.fillLastRun(job);
                        self.fillNLastRun(collection);
                    }
                });
         };
        refreshData();
        setInterval(refreshData, 5000);
    },
    printProjectJobInfo : function(idJob) {
         var self = this;
        self.updateJobSelect(idJob);
        console.log("---" +idJob);
        //Print selected job details (if undefined, last job
        if(idJob==undefined) {
            //print info/result from last job
            new JobCollection({ project : self.model.id, software: self.idSoftware, max: 1}).fetch({
                success : function (collection, response) {
                    var job = collection.models.length>0? collection.models[0] : undefined;
                    self.fillSelectedJobDetails(job);
                }
            });
        } else {
            //print info/result from job
            new JobModel({ id : idJob}).fetch({
                success : function (model, response) {

                    self.fillSelectedJobDetails(model);
                }
            });
        }

    },
    printAcitivtyDiagram : function(success, total) {
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Success');
        data.addColumn('number', 'Succes rate');
         console.log("printAcitivtyDiagram="+success+"#"+total);
         data.addRows([
              ['Success',    success],
              ['Fail',      total-success]
        ]);

        var options = {
          title: 'Algorithm success rate',
          width: 200, height: 150,
          vAxis: {title: "Success rate"},
          hAxis: {title: "#"},
          backgroundColor : "whiteSmoke",
            strictFirstColumnType: false,
            is3D: true,
          lineWidth: 1,
          colors : ["#5ebc5e","#d34842"]
        };

        var chart = new google.visualization.PieChart(document.getElementById('softwareInfoDiagram'));
        chart.draw(data, options);
    },



    printProjectSoftwareDetails : function(software) {
        $("#panelSoftwareResume").find('.softwareMainInfo').empty();
        $("#panelSoftwareResume").find('.softwareMainInfo').append('<li><h2>'+software.get('name')+'</h2></li>');
        $("#panelSoftwareResume").find('.softwareMainInfo').append('<li>'+ software.get('numberOfJob') +' job has been run ('+ software.get('numberOfJobSuccesfull')+' success)</li>');
//        $("#panelSoftwareResume").find('.softwareMainInfo').append('<li> Number of Succesfull Job: '+software.get('numberOfJobSuccesfull')+'</li>');
//        $("#panelSoftwareResume").find('.softwareMainInfo').append('<li> Ratio of Succesfull Job: '+Math.min(Math.round(software.get('ratioOfJobSuccesfull')*100),100)+'%</li>');
        this.printAcitivtyDiagram(software.get('numberOfJobSuccesfull'),software.get('numberOfJob'));
        this.fillSelectJob(software);
    },
    fillSelectJob : function(software) {
        var self = this;
        var elem = $("#fullSoftwareDashboard").find('.chosseJob');
        $("#fullSoftwareDashboard").find('.chosseJob').empty();
        //$("#menuAlgoSoftware").find('.chosseJob').append('<br><br><h3>Select a job: </h3><br>');
        $("#fullSoftwareDashboard").find('.chosseJob').append('<select id="chosseJobSelect" style="min-width: 50%;max-width:75%;text-align:center;"></select>');
        var selectElem = $("#fullSoftwareDashboard").find('#chosseJobSelect');
        new JobCollection({ project : self.model.id, software: software.id, light:true}).fetch({
            success : function (collection, response) {
                collection.each(function (job) {
                    var classSucess = "btn-danger";
                    if(job.get('running')) classSucess = "btn-primary";
                    else if(job.get('successful')) classSucess = "btn-success";
                    console.log("#####" + job.get('running'));
                    var textOption = self.convertLongToDate(job.get('created')) + " (Job " + job.id +")";
                    $("#fullSoftwareDashboard").find('#chosseJobSelect').append('<option value="'+job.id+'" class="'+classSucess+'" style="text-align:left;">'+textOption+'</option>');
                });
                //color the select with the same color as the selected options
                self.refreshSelectJobStyle();
                selectElem.change(function() {
                    console.log("change disableSelect="+self.disableSelect);
                     //if(!self.disableSelect) self.printProjectJobInfo(selectElem.val());
                    if(!self.disableSelect) window.location = '#tabs-algos-'+self.model.id + '-' +software.id + '-' + selectElem.val();
                });

            }
        });


        $("#avancedJobSearchLink").click(function() {
            console.log("Open job listing");
            var result = new JobListingView({
            software : self.software,
            project : self.model,
            el : $("#jobListingDialogParent"),
            parent : self
        }).render();
        });

    },
    updateJobSelect: function(idJob){
        var self = this;
        self.disableSelect = true;
        var selectElem = $("#fullSoftwareDashboard").find('#chosseJobSelect');
        console.log("val disableSelect="+self.disableSelect);
        selectElem.val(idJob);
        $("#fullSoftwareDashboard").find('#chosseJobSelect option[value='+idJob+']').attr("selected", "selected");

        console.log("val ok disableSelect="+self.disableSelect);
        self.refreshSelectJobStyle();
        console.log("refreshSelectJobStyle disableSelect="+self.disableSelect);
        self.disableSelect = false;
    },
    refreshSelectJobStyle : function () {
        //todo: check if running and put correct style
        var selectElem = $("#fullSoftwareDashboard").find('#chosseJobSelect');
        console.log("selectElem refrsh");
        selectElem.removeClass('btn-success btn-success');
        var selectElemId = selectElem.val();
        var selectElemOption = selectElem.find('option[value="'+selectElemId+'"]');
        var choiceClass = selectElemOption.attr('class');
        selectElem.addClass(choiceClass);
    },
    fillLastRun : function (job) {
        var self = this;
       var lastRunElem = $("#panelSoftwareResume").find('.lastRunDetails');
       lastRunElem.empty();
       if(job==undefined) return;
       console.log(job);

       lastRunElem.append('<h4>Last Run Info</h4>');
       self.buildJobInfoElem(job,lastRunElem);
    },
    fillNLastRun : function (jobs) {
        var self = this;

        $("#fullSoftwareDashboard").find('#panelSoftwareLastRunList').empty();
        var i = 0;
        jobs.each(function (job) {
            $("#fullSoftwareDashboard").find('#panelSoftwareLastRunList').append('<div style="margin: 0px auto;min-width:150px;max-width:200px" id="'+job.id+'"></div>');
            self.buildJobInfoElem(job,$("#fullSoftwareDashboard").find('#panelSoftwareLastRunList').find('#'+job.id));
            i++;

        });

    },
    fillSelectedJobDetails : function(job) {
        var self = this;
        if(job==undefined) return;
        self.idJob = job.id;

        console.log("Print info for job="+job);
//        if(job!=undefined) window.location = '#tabs-algos-'+self.model.id +"-" + self.idSoftware + "-" + job.id;
//        else window.location = '#tabs-algos-'+self.model.id +"-" + self.idSoftware + "-" + job;;

         var refreshData = function() {
                var selectRunElem = $("#panelJobDetails").find('.selectRunDetails');
                new JobModel({ id : self.idJob}).fetch({
                    success : function (model, response) {
                        selectRunElem.empty();
                        self.buildJobInfoElem(model,selectRunElem);
                    }
                });
         };
        refreshData();
        setInterval(refreshData, 5000);



        var selectRunParamElem = $("#panelJobDetails").find('.selectRunParams');
         selectRunParamElem.empty();
        self.buildJobParamElem(job,selectRunParamElem);
        self.printJobResult(job);
    },
    //Print job view
    buildJobInfoElem : function (job, elem) {
        var self = this;
        if(job==undefined) return;
        var width = $('#panelSoftwareLastRunList').find('#'+job.id).width()-5;
        elem.append('<ul  style="display:inline;" id="'+job.id+'"></ul>');
        var subelem = elem.find('#'+job.id);
        var successfulIcon = job.get('successful') ? "icon-star" : "icon-star-empty";

       subelem.append('<li><i class="'+successfulIcon+'"></i><h4 style="display:inline;"><a href="#tabs-algos-'+self.model.id + "-" + self.idSoftware + "-" +job.id+'" id="'+job.id+'">Job ' + job.id + '<br></a></h4></li>');
       subelem.find("#"+job.id).click(function() {
            self.printProjectJobInfo(job.id);
        });

        if(job.get('running')) {
            //job is still running
            subelem.append('<li><div id="progresstext">Progress </div><div id="progBar" style="margin:0 auto;min-width:'+width+';max-width:'+width+';">'+job.get('progress')+'</div></li>');
        } else {
            //job is not running: success or fail
           var classSucess = "btn-danger";
           var textSucces = "Fail!";
           if(job.get('successful')) {
               classSucess = "btn-success";
               textSucces = "Success!";
           }
           subelem.append('<li><div class="'+classSucess+'" style="margin:0 auto;min-width:'+width+';max-width:'+width+';">'+textSucces+'</div></li>');
        }
       subelem.append('<li>Launch: '+self.convertLongToDate(job.get('created'))+'</li>');
//       subelem.append('<li>Last update: <br>'+self.convertLongToDate(job.get('updated'))+'</li>');

        //TODO: make a resume + dialog?
//       subelem.append('<li>See full parameter list...</li>');



       subelem.find('div#progBar').each(function(index) {
          var progVal = eval($(this).text());
          $(this).text('');
          $(this).progressbar({
              value: progVal
          });
      });
      subelem.find('div#progresstext').append(job.get('progress') + "%");
    },
    buildJobParamElem: function(job, ulElem) {
        if(job==undefined) return;
        _.each(job.get('jobParameter'), function(param) {
             ulElem.append("<li>"+param.name + ": "+param.value+"</li>");

        });

//         "id": 530355,
//        "value": "cytomine",
//        "job": 530348,
//        "softwareParameter": 133677,
//        "name": "execType",
//        "type": "String"
    },
    printJobResult: function(job) {
        //panelJobResultsDiv
        if(job==undefined) return;
        var self = this;
        console.log("Print result for job="+job.id);
        console.log(window.app.status.currentTermsCollection);
        console.log(window.app.status.currentAnnotationsCollection);


        if(window.app.status.currentTermsCollection==undefined || window.app.status.currentAnnotationsCollection==undefined) {
            console.log("Collection are undefined");
             new AnnotationCollection({project:self.model.id}).fetch({
                success : function (collection, response) {

                    console.log("AnnotationCollection ok");
                    window.app.status.currentAnnotationsCollection = collection;
                    new TermCollection({idProject:self.model.id}).fetch({
                        success : function (terms, response) {
                            console.log("TermCollection ok:"+terms.length);
                            window.app.status.currentTermsCollection = terms;
                            //self.fetchWorstAnnotations(collection,terms);
                            self.initJobResult(job);

                        }
                    });
                }
            });
        } else {
            self.initJobResult(job);
        }
    },
    initJobResult : function(job) {
//        var result = new RetrievalAlgoResult({
//            model : job,
//            terms : window.app.status.currentTermsCollection,
//            annotations: window.app.status.currentAnnotationsCollection,
//            el : $("#panelJobResultsDiv")
//        }).render();
    },
    convertLongToDate : function (longDate) {
          var createdDate = new Date();
          createdDate.setTime(longDate);

          //date format
          var year = createdDate.getFullYear();
          var month = (createdDate.getMonth()+1)  < 10 ? "0"+(createdDate.getMonth()+1) : (createdDate.getMonth()+1);
          var day =  (createdDate.getDate())  < 10 ? "0"+(createdDate.getDate()) : (createdDate.getDate());

          var hour =  (createdDate.getHours())  < 10 ? "0"+(createdDate.getHours()) : (createdDate.getHours());
          var min =  (createdDate.getMinutes())  < 10 ? "0"+(createdDate.getMinutes()) : (createdDate.getMinutes());

          var dateStr = year + "-" + month +"-" + day + " " + hour + "h" + min;
        return dateStr;
    }
});