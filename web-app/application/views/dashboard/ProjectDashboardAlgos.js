var ProjectDashboardAlgos = Backbone.View.extend({
    rendered : false,
    jobCollection : null,
    softwares : null,
    disableSelect : false,
    software : null,
    jobSelectView : undefined,
    jobsLight : null,
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

               if(collection.length==0) {
                   $(self.el).empty();
                   $(self.el).append('<br/><divstyle="text-align:left;"><h2>No software available for this project!</h2></div>');
                   return;
               }

               if(self.idSoftware==undefined) {
                   var lastSoftware = collection.last();
                   self.idSoftware = lastSoftware.id;
               }
              self.software = collection.get(self.idSoftware);
              self.softwares = collection;
              self.initProjectSoftwareList();
              var softModel = collection.get(self.idSoftware);
               console.log("1. self.idJob="+self.idJob);
              self.printProjectSoftwareInfo();

                //button click run software
                self.printSoftwareLaunchButton();


                new JobCollection({ project : self.model.id, software: self.idSoftware, light:true}).fetch({
                    success : function (collection, response) {
                        self.jobsLight = collection;
                        self.printComparatorLaunch();
                        self.fillJobSelectView();
                    }
                });


       }});


      return this;
   },
    refresh : function() {
        console.log("refresh()" + this.idJob);
        if (!this.rendered) this.render();
        if(this.softwares==null) return;
        //this.printProjectJobInfo(this.idJob);
        this.software = this.softwares.get(this.idSoftware);
        this.printProjectSoftwareInfo();
    },
    refresh : function(idSoftware,idJob) {
        console.log("refresh(idSoftware,idJob)" + this.idJob);
        if(this.softwares==null || this.softwares.length<1) return;
        this.idJob = idJob;
        if(idSoftware==undefined) {
            idSoftware = this.idSoftware;
        }
        this.software = this.softwares.get(idSoftware);
        if(idSoftware!=this.idSoftware)
        {   this.idSoftware = idSoftware;
            this.changeSoftware();
        } else {
            this.idSoftware = idSoftware;
        }

        this.printProjectSoftwareInfo();
    },
    initProjectSoftwareList : function () {
        var self = this;

        self.softwares.each(function (software) {
            $("#projectSoftwareListUl").append('<li id="consultSoftware-' + software.id + '"><a href="#tabs-algos-'+self.model.id + '-' +software.id + '-">' + software.get('name') + '</a></li>');
            $("#projectSoftwareListUl").children().removeClass("active");

            if(software.id==self.idSoftware) {
                 $("#consultSoftware-" + software.id).addClass("active");
            }
        });

    },
    changeSoftware : function() {
        var self = this;
        self.softwares.each(function (software) {
            $("#consultSoftware-" + software.id).removeClass("active");
        });
        $("#consultSoftware-" + self.software.id).addClass("active");
        //clean param list
        $('#selectRunParamsTable').find('tbody').empty();
        //clean result
        $("#panelJobResultsDiv").empty();
        //load result
        console.log("changeSoftware") ;
        self.fillJobSelectView();
    },
    printProjectSoftwareInfo : function() {
        var self = this;

        //Print software details
        self.printProjectSoftwareDetails( self.software);

        //Print last job + n last job details
        self.printLastNRun();

        //Print selected job from this software
        self.printProjectJobInfo( self.idJob);

       $("#softwareCompareJobButton").click(function() {
            self.jobsLight.fetch({
                success : function (collection, response) {
                      console.log("project=" + self.model.id + " software.id=" +  self.software.id);
                        new JobComparatorView({
                            software : self.software,
                            project : self.model,
                            el : $("#jobComparatorDialogParent"),
                            parent : self,
                            jobs: collection,
                            job1: undefined,
                            softwares : self.softwares
                        }).render();
                }
                });
            });

    },
    changeJobSelection : function(idJob)  {
        var self = this;
        console.log("refresh:idJob="+idJob);
        window.location = '#tabs-algos-'+self.model.id + '-' + self.idSoftware + '-' + idJob;
        if(self.jobSelectView!=undefined) self.jobSelectView.refresh();
    },
    printSoftwareLaunchButton : function() {
        var self = this;
        //jobLaunchDialogParent
          $("#softwareLaunchJobButton").click(function() {
              console.log("project=" + self.model.id + " software.id=" +  self.software.id);
               new LaunchJobView({
                    software : self.software,
                    project : self.model,
                    el : $("#jobComparatorDialogParent"),
                    parent : self
                }).render();
          });


    },
    printComparatorLaunch : function() {
        var self = this;

       $("#launchComparator").click(function() {
            self.jobsLight.fetch({
                success : function (collection, response) {
                      console.log("project=" + self.model.id + " software.id=" +  self.software.id);
                        new JobComparatorView({
                            software : self.software,
                            project : self.model,
                            el : $("#jobComparatorDialogParent"),
                            parent : self,
                            jobs: collection,
                            job1: collection.get(self.idJob),
                            softwares : self.softwares
                        }).render();
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
                        self.fillNLastRun(collection);
                    }
                });
         };
        refreshData();
        var interval = setInterval(refreshData, 5000);
        $(window).bind('hashchange', function() {
          clearInterval(interval);
        });
    },
    printProjectJobInfo : function(idJob) {
         var self = this;
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
    printAcitivtyDiagram : function(software) {
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Success');
        data.addColumn('number', 'Succes rate');
         console.log("printAcitivtyDiagram="+software.id);
         data.addRows([
             ['Not Launch',software.get('numberOfNotLaunch')],
             ['In Queue',software.get('numberOfInQueue')],
             ['Running',software.get('numberOfRunning')],
             ['Success',software.get('numberOfSuccess')],
             ['Failed',software.get('numberOfFailed')],
             ['Indeterminate',software.get('numberOfIndeterminate')],
             ['Wait',software.get('numberOfWait')]
        ]);
        var width = $("#softwareInfoDiagram").width()-100;
        var options = {
          title: 'Job software status for all project',
          width: width, height: 150,
          vAxis: {title: "Success rate"},
          hAxis: {title: "#"},
          backgroundColor : "whiteSmoke",
            strictFirstColumnType: false,
            is3D: true,
          lineWidth: 1,
          colors : ["#434141","#65d7f8","#005ccc","#52a652","#c43c35","#434343","#faaa38"]
        };

        var chart = new google.visualization.PieChart(document.getElementById('softwareInfoDiagram'));
        chart.draw(data, options);
    },
    printProjectSoftwareDetails : function(software) {
        $("#panelSoftwareResume").find('.softwareMainInfo').empty();
        $("#panelSoftwareResume").find('.softwareMainInfo').append('<li><h2>'+software.get('name')+'</h2></li>');
        $("#panelSoftwareResume").find('.softwareMainInfo').append('<li>'+ software.get('numberOfJob') +' job has been run</li>');
        this.printAcitivtyDiagram(software);

    },
    fillJobSelectView : function() {
        var self = this;
        console.log("fillJobSelectView:"+self.software.get('name'));
        $('#jobSelection').empty();


        new JobCollection({ project : self.model.id, software: self.software.id, light:true}).fetch({
            success : function (collection, response) {
                self.jobsLight = collection;
                 self.jobSelectView = new JobSelectionView({
                    software : self.software,
                    project : self.model,
                    el : $("#panelAlgoSoftware").find('#jobSelection'),
                    parent : self,
                    jobs: collection,
                    comparator : false
                }).render();
            }
        });
    },
    fillNLastRun : function (jobs) {
        var self = this;

        $("#fullSoftwareDashboard").find('#panelSoftwareLastRunList').empty();
        var i = 0;
        jobs.each(function (job) {
            $("#fullSoftwareDashboard").find('#panelSoftwareLastRunList').append('<div style="margin: 0px auto;min-width:100px;max-width:200px" id="'+job.id+'"></div>');
            self.buildJobInfoElem(job,$("#fullSoftwareDashboard").find('#panelSoftwareLastRunList').find('#'+job.id));
            i++;

        });

    },
    fillSelectedJobDetails : function(job) {
        var self = this;
        console.log("fillSelectedJobDetails="+job);
        if(job==undefined) {
            $('.selectRunDetails').empty();
            $('#selectRunParamsTable').find('tbody').empty();
            $("#panelJobResultsDiv").empty();
            return;
        }
        self.idJob = job.id;
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
        var interval = setInterval(refreshData, 5000);
        $(window).bind('hashchange', function() {
          clearInterval(interval);
        });

        var selectRunParamElem = $('#selectRunParamsTable').find('tbody').empty();
         selectRunParamElem.empty();
        self.buildJobParamElem(job,selectRunParamElem);
        self.printJobResult(job);
    },
    buildJobInfoElem : function (job, elem) {
        var self = this;
        if(job==undefined) return;
        var width = $('#panelSoftwareLastRunList').find('#'+job.id).width()-5;
        elem.append('<ul  style="display:inline;" id="'+job.id+'"></ul>');
        var subelem = elem.find('#'+job.id);
        var successfulIcon = job.get('status')==JobModel.SUCCESS ? "icon-star" : "icon-star-empty";

       subelem.append('<li><i class="'+successfulIcon+'"></i><h4 style="display:inline;"><a href="#tabs-algos-'+self.model.id + "-" + self.idSoftware + "-" +job.id+'" id="'+job.id+'">Job ' + job.get('number') + '<br></a></h4></li>');
       subelem.find("#"+job.id).click(function() {
            self.printProjectJobInfo(job.id);
        });

        subelem.append(self.getStatusElement(job,width));

       subelem.append('<li>'+window.app.convertLongToDate(job.get('created'))+'</li>');

       subelem.find('div#progBar').each(function(index) {
          var progVal = eval($(this).text());
          $(this).text('');
          $(this).progressbar({
              value: progVal
          });
      });
      subelem.find('div#progresstext').append(job.get('progress') + "%");
    },
    getStatusElement : function(job,width) {
        var self = this;
        if(job.isNotLaunch()) return self.getJobLabel("btn-inverse","Not Launch!",width);
        else if(job.isInQueue()) return self.getJobLabel("btn-info","In queue!",width);
        else if(job.isRunning()) return self.getJobProgress(job,"active",'Progress ',width); //progress-bar not blue by default if  progress-striped (<> doc)
        else if(job.isSuccess()) return self.getJobLabel("btn-success","Success!",width);
        else if(job.isFailed()) return self.getJobLabel("btn-danger","Failed!",width);
        else if(job.isIndeterminate()) return self.getJobLabel("btn-warning","Indetereminate!",width);
        else if(job.isWait()) return self.getJobProgress(job,"progress-warning",'Wait ',width); //progress-warnoing doesn't work (<> doc) :-/
        else return "no supported";
    },
    getJobLabel : function(className,text,width) {
       return '<li><div class="'+className+'" style="margin:0 auto;min-width:'+width+';max-width:'+width+';">'+text+'</div></li>';
    },
    getJobProgress : function(job, className, text, width) {   //todo: add class " progress-striped"
        return '<li><div id="progresstext">'+text+' </div><div class="progress '+className+' progress-striped"><div class="bar" style="width: '+job.get('progress')+'%;"></div></div></li>'
    },
    buildJobParamElem: function(job, ulElem) {
        if(job==undefined) return;

        var datatable = $('#selectRunParamsTable').dataTable();
        datatable.fnClearTable();
        //print data from project image table
        var tbody = $('#selectRunParamsTable').find("tbody");

         _.each(job.get('jobParameter'), function (param) {
            tbody.append('<tr><td>'+param.name+'</td><td>'+param.value+'</td><td>'+param.type+'</td></tr>');
         });
        $('#selectRunParamsTable').dataTable( {
            //"sDom": "<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>>",
            "sPaginationType": "bootstrap",
            "oLanguage": {
                "sLengthMenu": "_MENU_ records per page"
            },
            "iDisplayLength": 5 ,
            "bLengthChange" : false,
            bDestroy: true,
                 "aoColumnDefs": [
                { "sWidth": "40%", "aTargets": [ 0 ] },
                { "sWidth": "40%", "aTargets": [ 1 ] },
                { "sWidth": "20%", "aTargets": [ 2 ] }
             ]
        });
    },
    printJobResult: function(job) {
        if(job==undefined) {
            return;
        }
        var self = this;

        if(window.app.status.currentTermsCollection==undefined || window.app.status.currentAnnotationsCollection==undefined) {
             new AnnotationCollection({project:self.model.id}).fetch({
                success : function (collection, response) {
                    window.app.status.currentAnnotationsCollection = collection;
                    new TermCollection({idProject:self.model.id}).fetch({
                        success : function (terms, response) {
                            window.app.status.currentTermsCollection = terms;
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
        var self = this;
        var result = new JobResultView({
            model : job,
            project : self.model,
            el : $("#panelJobResultsDiv"),
            jobs : self.jobsLight,
            software : self.software
        }).render();
    }

});