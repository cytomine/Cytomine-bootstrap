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
                dialogID:"#job-delete-confirm"
            }
        }).render();
        $("#jobDeleteDataButton"+self.model.id).hide();

        console.log("data loading...");
        //load all job data
        new JobDataStatsModel({id:self.model.id}).fetch({
            success:function(model,response) {
                console.log("data loaded:"+model.toJSON()) ;

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
                 console.log("error getting job data stat");
            }}
        );

        console.log("button listener...");
        $("#jobDeleteDataCancelButton" + self.model.id).click(function () {
            self.dialog.close();
            return false;
        });

        $("#jobDeleteDataButton" + self.model.id).click(function () {

            $("#jobDataStat-"+self.model.id).empty();
            $("#jobDataStat-"+self.model.id).append('<div class="alert alert-info" ><i class="icon-refresh"/> Deleting all data...this may take some time...</div>');



            new JobDataStatsModel({id:self.model.id}).destroy(
                {
                    success:function (model, response) {
                        window.app.view.message("Project", response.message, "success");
                        self.dialog.close();
                        self.container.refresh();
                    },
                    error:function (model, response) {
                        var json = $.parseJSON(response.responseText);
                        window.app.view.message("Job data", json.errors[0], "error");
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
    }
});