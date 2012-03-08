var JobComparatorView = Backbone.View.extend({
    width : null,
    software: null,
    project : null,
    job1 : null,
    job2 : null,
    jobs : null,
    parent : null,
    initialize: function(options) {
        this.width = options.width;
        this.software = options.software;
        this.project = options.project;
        this.job1 = options.job1;
        this.job2 = options.job2;
        this.jobs = options.jobs;
        this.parent = options.parent;

    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/processing/JobComparator.tpl.html"
        ],
               function(JobComparatorTpl) {
                   self.loadResult(JobComparatorTpl);
               });
        return this;
    },
    loadResult : function (JobComparatorTpl) {
        var self = this;
        var content = _.template(JobComparatorTpl, {});
        $(self.el).empty();
        $(self.el).append(content);

        var width = ($(window).width() - 200);
        var height = ($(window).height() - 200);
        $(self.el).dialog({ width: width, height: height, modal:true });
        self.printJobSelection($("#comparatorJobSelection"));

        $("#comparatorJobSelection").find('.job1').find('select').val(self.jobs.at(0).id);
        $("#comparatorJobSelection").find('.job2').find('select').val(self.jobs.at(1).id);

        self.refreshCompareJob();
    },
    retrieveSelectedJob : function(num) {
        return $("#comparatorJobSelection").find('.job'+num).find('select').val();
    },
    cleanCompareJob : function() {
         console.log("cleanCompareJob");
        $("#comparatorJobInfo").find(".job1").empty();
        $("#comparatorJobInfo").find(".job2").empty();
        $("#comparatorJobParam").find(".job1").empty();
        $("#comparatorJobParam").find(".job2").empty();
        $("#comparatorJobResult").find(".job1").empty();
        $("#comparatorJobResult").find(".job2").empty();

    },
    refreshCompareJob : function() {
        var self = this;
        console.log("refreshCompareJob");
        self.cleanCompareJob();
        var idJob1 = self.retrieveSelectedJob('1');
        var idJob2 = self.retrieveSelectedJob('2');

         if (idJob1 == undefined || idJob2 == undefined) return;
            new JobModel({ id : idJob1}).fetch({
                success : function (job1, response) {
                    new JobModel({ id : idJob2}).fetch({
                        success : function (job2, response) {
                             self.job1 = job1;
                             self.job2 = job2;
                            self.printJobInfo($("#comparatorJobInfo"));
                            self.printParamJob($("#comparatorJobParam"));
                            self.printResultJob($("#comparatorJobResult"));
                        }
                   });
                }
           });

    },
    printJobSelection : function(elemParent) {
        var self = this;
        self.addSelectionView(elemParent.find('.job1'));
        self.addSelectionView(elemParent.find('.job2'));
    },
    printJobInfo : function(elemParent) {
        var self = this;
        self.addJobView(elemParent.find('.job1'),self.job1);
        self.addJobView(elemParent.find('.job2'),self.job2);
    },
    printParamJob : function(elemParent) {
        var self= this;
        self.addParamView(elemParent.find('.job1'),self.job1);
        self.addParamView(elemParent.find('.job2'),self.job2);
    },
    printResultJob : function(elemParent) {
        var self = this;
        self.addResultView(elemParent.find('.job1'),self.job1);
        self.addResultView(elemParent.find('.job2'),self.job2);
    },
    addSelectionView : function(elemParent) {
        var self = this;
        elemParent.append('<select></select>');
        self.jobs.each(function(job) {
            elemParent.find("select").append('<option value="'+ job.id +'">'+job.id+'</option>');
        });
        elemParent.find("select").change(function() {
            self.refreshCompareJob();
        });
    },
    addJobView : function(elemParent,job) {
        var self = this;
        console.log(elemParent.length);
        elemParent.append('<div style="margin: 0px auto;min-width:100px;max-width:200px" id="'+job.id+'"></div>');
        console.log(elemParent);
        self.parent.buildJobInfoElem(job,elemParent.find("#"+job.id));
        console.log(elemParent);
    },
    addParamView : function(elemParent,job) {
        var self = this;

        elemParent.append('<table width="100%" style="width:100%;max-width:100%" cellpadding="0" cellspacing="0" border="0" class="table table-striped table-bordered table-condensed" id="runParamsTable" ></table>');
        elemParent.find('#runParamsTable').append('<thead><tr><th>Name</th><th>Value</th><th>Type</th></tr></thead>');
        elemParent.find('#runParamsTable').append('<tbody></tbody>');


        //var datatable = elemParent.find('#runParamsTable').dataTable();
        //print data from project image table
        var tbody = elemParent.find('#runParamsTable').find("tbody");

         _.each(job.get('jobParameter'), function (param) {
            tbody.append('<tr><td>'+param.name+'</td><td>'+param.value+'</td><td>'+param.type+'</td></tr>');
         });
        elemParent.find('#runParamsTable').dataTable( {
            //"sDom": "<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>>",
            "sPaginationType": "bootstrap",
            "oLanguage": {
                "sLengthMenu": "_MENU_ records per page"
            },
            "iDisplayLength": 10 ,
            "bLengthChange" : false,
            bDestroy: true,
                 "aoColumnDefs": [
                { "sWidth": "40%", "aTargets": [ 0 ] },
                { "sWidth": "40%", "aTargets": [ 1 ] },
                { "sWidth": "20%", "aTargets": [ 2 ] }
             ]
        });
    },
    addResultView: function(elemParent,job) {
        var self = this;

        if(window.app.status.currentTermsCollection==undefined || window.app.status.currentAnnotationsCollection==undefined) {
             new AnnotationCollection({project:self.project.id}).fetch({
                success : function (collection, response) {
                    window.app.status.currentAnnotationsCollection = collection;
                    new TermCollection({idProject:self.project.id}).fetch({
                        success : function (terms, response) {
                            window.app.status.currentTermsCollection = terms;
                            self.initJobResult(job,elemParent);

                        }
                    });
                }
            });
        } else {
            self.initJobResult(job,elemParent);
        }
    },
    initJobResult : function(job,elemParent) {
        var self = this;
        var result = new RetrievalAlgoResult({
            model : job,
            terms : window.app.status.currentTermsCollection,
            annotations: window.app.status.currentAnnotationsCollection,
            project : self.project,
            el : elemParent
        }).render();
    }



//        <div id="comparatorJobResult" style="padding : 2px;margin-right:5px;min-width:90%;"  class="span5 hero-unit">
//            <div class="job1" style="float:left"></div>
//            <div class="job2" style="float:right"></div>
//       </div>


});