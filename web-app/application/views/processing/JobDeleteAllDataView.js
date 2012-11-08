var JobDeleteAllDataView = Backbone.View.extend({
    tagName:"div",
    initialize:function (options) {
        this.project = options.project;
        this.dialog = null;
        this.container = options.container;
    },
    doLayout:function (jobDeleteAllDataViewTpl) {
        var self = this;


        self.dialog = new ConfirmDialogView({
            el:'#dialogs', //TODO:: create element?
            template:_.template(jobDeleteAllDataViewTpl, this.model.toJSON()),
            dialogAttr:{
                dialogID:"#job-delete-confirm",
                backdrop:true
            }
        }).render();
        $("#jobDeleteDataButton"+self.model.id).hide();

        console.log("data loading...");


        new TaskModel({project:self.project.id}).save({}, {
                 success:function (task, response) {
                     $("#jobDataStat-"+self.model.id).append('<div id="task-'+task.id+'"></div>');
                     var timer = self.printTaskEvolution(task,$("#jobDataStat-"+self.model.id).find("#task-"+task.id),1000);

                    //load all job data
                    new JobDataStatsModel({id:self.model.id, task:task.id}).fetch({
                        success:function(model,response) {
                            console.log("data loaded:"+model.toJSON()) ;
                            clearInterval(timer);

                            $("#jobDataStat-"+self.model.id).empty();
                            $("#jobDataStat-"+self.model.id).append("This job has all these data:<br>");
                            $("#jobDataStat-"+self.model.id).append(model.get('reviewed') + " reviewed annotations<br>");
                            $("#jobDataStat-"+self.model.id).append(model.get('annotations') + " annotations<br>");
                            $("#jobDataStat-"+self.model.id).append(model.get('annotationsTerm') + " term added to annotations<br>");
                            $("#jobDataStat-"+self.model.id).append(model.get('jobDatas') + " files<br>");


                            if(model.get('reviewed')!=0) {
                                $("#jobDataStat-"+self.model.id).append('<br><br><div class="alert alert-warning" style="min-width: 300px;">You cannot delete job data with reviewed annotation!' +model.get('reviewed')+ ' reviewed annotation)');
                            } else {
                                $("#jobDeleteDataButton"+self.model.id).show();
                                $("#jobDataStat-"+self.model.id).append('<br><br>The delete operation may take some time...');
                            }
                        },
                        error:function (collection, response) {
                            clearInterval(timer);
                             console.log("error getting job data stat");
                        }}
                    );

                 },
                 error:function (model, response) {
                     var json = $.parseJSON(response.responseText);
                     window.app.view.message("Task", json.errors, "error");
                 }
             }
         );





        console.log("button listener...");
        $("#jobDeleteDataCancelButton" + self.model.id).click(function () {
            self.dialog.close();
            return false;
        });

        $("#jobDeleteDataButton" + self.model.id).click(function () {

            $("#jobDataStat-"+self.model.id).empty();
            $("#jobDataStat-"+self.model.id).append('<div class="alert alert-info" ><i class="icon-refresh"/> Deleting all data...this may take some time...</div>');




            new TaskModel({project:self.project.id}).save({}, {
                      success:function (task, response) {
                          $("#jobDataStat-"+self.model.id).append('<div id="task-'+task.id+'"></div>');
                          var timer = self.printTaskEvolution(task,$("#jobDataStat-"+self.model.id).find("#task-"+task.id),1000);

                          new JobDataStatsModel({id:self.model.id, task:task.id}).destroy(
                              {
                                  success:function (model, response) {
                                      clearInterval(timer);
                                      $("#jobDataStat-"+self.model.id).find("#task-"+task.id).empty();
                                      window.app.view.message("Project", response.message, "success");
                                      self.dialog.close();
                                      self.container.refresh();
                                  },
                                  error:function (model, response) {
                                      clearInterval(timer);
                                      var json = $.parseJSON(response.responseText);
                                      window.app.view.message("Job data", json.errors[0], "error");
                                  }
                              }
                          );

                      },
                      error:function (model, response) {
                          var json = $.parseJSON(response.responseText);
                          window.app.view.message("Task", json.errors, "error");
                      }
                  }
              );

            return false;
        });

        return this;
    },
    render:function () {
        console.log("render");
        var self = this;
        require(["text!application/templates/processing/JobDeleteData.tpl.html"], function (jobDeleteDataViewTpl) {
            self.doLayout(jobDeleteDataViewTpl);
        });
    },
    //TODO:: create a specific view for this
    printTaskEvolution:function(task,divToFill, timeout) {
        function checkTask() {
           //load all job data
           new TaskModel({id:task.id}).fetch({
               success:function(taskInfo,response) {
                   divToFill.empty();
                   divToFill.append('' +
                               '<div class="progress progress-striped active">' +
                               '   <div class="bar" style="width: '+taskInfo.get('progress')+'%;"></div>' +
                               '</div>');
                   divToFill.append(taskInfo.get('comments').join('<br>'));
               },
               error:function (collection, response) {
                    console.log("error getting task");
               }}
           );
        }
        checkTask();
        var timer=setInterval(function(){checkTask()}, timeout);
        return timer;
    }
});