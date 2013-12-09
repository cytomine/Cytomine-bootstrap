var ProjectDashboardAlgos = Backbone.View.extend({
    rendered: false,
    jobCollection: null,
    softwares: null,
    disableSelect: false,
    software: null,
    jobSelectView: undefined,
    jobsLight: null,
    initialize: function (options) {
        if(this.model) {
            this.el = "#tabs-algos-" + this.model.id;
        }
        this.idJob = options.idJob;
        this.idSoftware = options.idSoftware;
        this.software = options.software;
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/processing/SoftwareInfo.tpl.html"
        ],
            function (tpl) {
                self.doLayout(tpl);
                this.rendered = true;
            });
        return this;
    },
    doLayout: function (tpl) {
        var self = this;
        $(this.el).html(_.template(tpl, {}));

        //get all software from project and print menu
        new SoftwareCollection({ project: self.model.id}).fetch({
            success: function (collection, response) {
                console.log("succes!");
                if (collection.length == 0) {
                    $(self.el).html('<div class="alert alert-info" style="width : 50%; margin:auto; margin-top : 30px;">No software available for this project</div>');
                    return;
                }
                if (self.idSoftware == undefined) {
                    var lastSoftware = collection.last();
                    self.idSoftware = lastSoftware.id;
                    window.location = "#tabs-algos-" + self.model.id + "-" + self.idSoftware + "-";
                }

                self.software = collection.get(self.idSoftware);
                self.softwares = collection;
                self.initProjectSoftwareList();
                self.printProjectSoftwareInfo();
                self.printSoftwareButton();
                new JobCollection({ project: self.model.id, software: self.idSoftware, light: true}).fetch({
                    success: function (collection, response) {
                        self.jobsLight = collection;
                        self.printComparatorLaunch();
                        self.fillJobSelectView();
                    }
                });
            }});


        return this;
    },
    refresh: function () {

        if (!this.rendered) {
            this.render();
        }
        if (this.softwares == null) {
            return;
        }

        this.software = this.softwares.get(this.idSoftware);
        this.printProjectSoftwareInfo();
    },
    refresh: function (idSoftware, idJob) {
        if (!this.softwares || this.softwares.length < 1) {
            return;
        }
        this.idJob = idJob;
        if (idSoftware == undefined) {
            idSoftware = this.idSoftware;
        }
        this.software = this.softwares.get(idSoftware);
        if (idSoftware != this.idSoftware) {
            this.idSoftware = idSoftware;
            console.log("Change software");
            this.changeSoftware();
        } else {
            this.idSoftware = idSoftware;
        }

        this.printProjectSoftwareInfo();
    },
    initProjectSoftwareList: function () {
        var self = this;
        self.softwares.each(function (software) {
            $("#projectSoftwareListUl").append('<li class="list-group-item" id="consultSoftware-' + software.id + '"><a href="#tabs-algos-' + self.model.id + '-' + software.id + '-">' + software.get('name') + '</a></li>');
            $("#projectSoftwareListUl").children().removeClass("active");
            if (software.id == self.idSoftware) {
                $("#consultSoftware-" + software.id).addClass("active");
            }
        });

    },
    changeSoftware: function () {
        var self = this;
        self.idJob = undefined;
        self.softwares.each(function (software) {
            $("#consultSoftware-" + software.id).removeClass("active");
        });
        $("#consultSoftware-" + self.software.id).addClass("active");
        //clean param list
        $('#selectRunParamsTable').find('tbody').empty();
        //clean result
        $("#panelJobResultsDiv").empty();
        //load result
        self.fillJobSelectView();

        self.printSoftwareButton();
    },
    printProjectSoftwareInfo: function () {
        var self = this;

        console.log("printProjectSoftwareInfo1");
        //Print software details
        self.printProjectSoftwareDetails();

        //Print last job + n last job details
        //self.printLastNRun();

        console.log("printProjectSoftwareInfo2");
        //Print selected job from this software
        self.printProjectJobInfo();

    },
    changeJobSelection: function (idJob) {
        var self = this;
        window.location = '#tabs-algos-' + self.model.id + '-' + self.idSoftware + '-' + idJob;
        if (self.jobSelectView != undefined) {
            self.jobSelectView.refresh();
        }
    },
    printSoftwareButton: function () {
        var self = this;

        //init modal for job launch
        var launchView = new LaunchJobView({
            software: self.software,
            project: self.model,
            el: '#jobComparatorDialogParent',
            parent: self
        });

        var modalLaunch = new CustomModal({
            idModal : "launchJobModal",
            button : $("#softwareLaunchJobButton"),
            header :"Launch new job",
            body :"<div id='jobComparatorDialogParent'></div>",
            wide : true,
            callBack: function() {launchView.render();}
        });
        modalLaunch.addButtons("closeNewJob","Close",false,true);
        modalLaunch.addButtons("createNewJob","Create new job",true,true,function() {launchView.createJobFromParam(launchView.executeJob);});


        //init modal for job compare
        var modalCompare = new CustomModal({
            idModal : "compareJobModal",
            button : $("#softwareCompareJobButton"),
            header :"Compare jobs",
            body :"<div id='jobComparatorDialogParent'></div>",
            wide : true,
            callBack: function() {
                self.jobsLight.fetch({
                            success: function (collection, response) {
                                var compareView =  new JobComparatorView({
                                       software: self.software,
                                       project: self.model,
                                       el: "#jobComparatorDialogParent",
                                       parent: self,
                                       job1: undefined,
                                       job2: undefined,
                                       softwares: self.softwares,
                                       jobs : collection
                                 }).render();
                            }
                        });
            }
        });
        modalCompare.addButtons("closeCompare","Close",false,true);


        //init modal for job filter
        var modalCompare = new CustomModal({
            idModal : "filterJobModal",
            button :  $("#softwareFilterJobButton"),
            header :"Filter jobs",
            body :"<div id='jobFilterDialogParent'></div>",
            width : Math.round($(window).width() - 200),
            height : Math.round($(window).height() - 200),
            callBack: function() {
                new JobSearchView({
                    software: self.software,
                    project: self.model,
                    idJob: self.idJob,
                    parent: self,
                    el: "#softwareSearchDialogParent"
                }).render();
            }
        });
        modalCompare.addButtons("closeCompare","Close",false,true);
    },
    printComparatorLaunch: function () {
        var self = this;

        $(document).on('click',"#launchComparator", function () {
            self.jobsLight.fetch({
                success: function (collection, response) {

                    new JobComparatorView({
                        software: self.software,
                        project: self.model,
                        el: $("#jobComparatorDialogParent"),
                        parent: self,
                        jobs: collection,
                        job1: collection.get(self.idJob),
                        softwares: self.softwares
                    }).render();
                }
            });
        });
    },
    printLastNRun: function () {
        var self = this;
        var refreshData = function () {
            new JobCollection({ project: self.model.id, software: self.software.id, max: 3}).fetch({
                success: function (collection, response) {
                    var job = collection.models.length > 0 ? collection.models[0] : undefined;
                    self.fillNLastRun(collection);
                }
            });
        };
        refreshData();
        var interval = setInterval(refreshData, 5000);
        $(window).bind('hashchange', function () {
            clearInterval(interval);
        });
    },
    printProjectJobInfo: function () {
        var self = this;
        if (self.idJob != undefined) {
            new JobModel({ id: self.idJob}).fetch({
                success: function (model, response) {
                    self.fillSelectedJobDetails(model);
                    $("#panelJobDetails").show();
                    $("#panelJobResults").show();
                    var targetOffset = $("#panelJobDetails").offset().top;
                    var currentOffset = $('html').offset().top;

                    //if not visible, scroll to job details div
                    if (!(Math.abs(targetOffset) >= Math.abs(currentOffset)) || !(Math.abs(targetOffset) <= Math.abs(currentOffset) + $(window).height() - 200)) {
                        $('html,body').animate({scrollTop: targetOffset - 50}, 500);
                    }

                }
            });
        } else {
            $("#panelJobDetails").hide();
            $("#panelJobResults").hide();
        }

    },
    printProjectSoftwareDetails: function () {
        var self = this;
        new StatsProjectSoftwareModel({project: self.model.id, software: self.software.id}).fetch({
            success: function (model, response) {
                new SoftwareDetailsView({
                    model: self.software,
                    stats: model,
                    project: self.model,
                    el: $("#softwareDetails")
                }).render();
                console.log("printProjectSoftwareDetails OK");
            }
        });
    },
    fillJobSelectView: function () {
        var self = this;

        $('#jobSelection').empty();

        new JobCollection({ project: self.model.id, software: self.software.id, light: true}).fetch({
            success: function (collection, response) {
                self.jobsLight = collection;
                self.jobSelectView = new JobSelectionView({
                    software: self.software,
                    project: self.model,
                    el: $('#jobSelection'),
                    parent: self,
                    jobs: collection,
                    comparator: false
                }).render();
            }
        });
    },
    fillNLastRun: function (jobs) {
        var self = this;

        $("#fullSoftwareDashboard").find('#panelSoftwareLastRunList').empty();
        var i = 0;
        jobs.each(function (job) {
            $("#fullSoftwareDashboard").find('#panelSoftwareLastRunList').append('<div style="margin: 0px auto;min-width:100px;max-width:200px" id="' + job.id + '"></div>');
            self.buildJobInfoElem(job, $("#fullSoftwareDashboard").find('#panelSoftwareLastRunList').find('#' + job.id));
            i++;

        });

    },
    fillSelectedJobDetails: function (job) {
        var self = this;

        if (job == undefined) {
            $('.selectRunDetails').empty();
            $('#selectRunParamsTable').find('tbody').empty();
            $("#panelJobResultsDiv").empty();
            return;
        }
        self.idJob = job.id;
        var refreshData = function () {
            var selectRunElem = $("#panelJobDetails").find('.selectRunDetails');
            new JobModel({ id: self.idJob}).fetch({
                success: function (model, response) {
                    selectRunElem.empty();
                    self.buildJobInfoElem(model, selectRunElem);
                }
            });
        };
        refreshData();
        var interval = setInterval(refreshData, 5000);
        $(window).bind('hashchange', function () {
            clearInterval(interval);
        });

        var selectRunParamElem = $('#selectRunParamsTable').find('tbody').empty();
        selectRunParamElem.empty();
        self.buildJobParamElem(job, selectRunParamElem);
        self.printJobResult(job);
    },
    buildJobInfoElem: function (job, elem) {
        var self = this;
        if (job == undefined) {
            return;
        }
        var width = $('#panelSoftwareLastRunList').find('#' + job.id).width() - 5;
        require(["text!application/templates/processing/JobInfo.tpl.html"], function (tpl) {
            var jobIcon = job.isSuccess() ? "icon-star" : "icon-star-empty";
            job.set({
                created: window.app.convertLongToDate(job.get("created")),
                status: self.getStatusElement(job, width),
                icon: jobIcon
            });
            var tpl_data = $.extend({}, {idProject: self.model.id, idSoftware: self.software.id }, job.toJSON());
            elem.append(_.template(tpl, tpl_data));
        });
    },
    getStatusElement: function (job, width) {
        var self = this;
        if (job.isNotLaunch()) {
            return self.getJobLabel("btn-inverse", "not launch", width);
        }
        else if (job.isInQueue()) {
            return self.getJobLabel("btn-info", "in queue", width);
        }
        else if (job.isRunning()) {
            return self.getJobProgress(job, "active", 'progress', width);
        } //progress-bar not blue by default if  progress-striped (<> doc)
        else if (job.isSuccess()) {
            return self.getJobLabel("btn-success", "success", width);
        }
        else if (job.isFailed()) {
            return self.getJobLabel("btn-danger", "failed", width);
        }
        else if (job.isIndeterminate()) {
            return self.getJobLabel("btn-warning", "indeterminate", width);
        }
        else if (job.isWait()) {
            return self.getJobProgress(job, "progress-warning", 'wait ', width);
        } //progress-warning doesn't work (<> doc) :-/
        else {
            return "no supported";
        }
    },
    getJobLabel: function (className, text, width) {
        return _.template('<span class="badge <%= className %>"><%= text %></span>', { className: className, text: text});
        //return '<span class="'+className+'""> '+text+'</span>';
    },
    getJobProgress: function (job, className, text, width) {   //todo: add class " progress-striped"
        var tpl = '<div id="progresstext"> <%= text %></div><div class="progress progress-striped <%= className %>"><div class="bar" style="width : <%= progress %>%;"></div></div>';
        return _.template(tpl, {text: text, className: className, progress: job.get('progress')});

    },

    buildJobParamElem: function (job, ulElem) {
        var self = this;
        if (job == undefined) {
            return;
        }

        var datatable = $('#selectRunParamsTable').dataTable();
        console.log("buildJobParamElem=" + datatable);
        console.log("buildJobParamElem=" + $('#selectRunParamsTable').length);
        datatable.fnClearTable();
        //print data from project image table
        var tbody = $('#selectRunParamsTable').find("tbody");

        _.each(job.get('jobParameters'), function (param) {

            tbody.append('<tr><td>' + param.name + '</td><td id="' + param.id + '"><div class="alert alert-info" style="margin-left : 10px;margin-right: 10px;"><i class="icon-refresh" /> Loading...</div></td><td>' + param.type + '</td></tr>');
            window.app.controllers.dashboard.printJobParameterValue(param, $('#selectRunParamsTable').find("tbody").find("td#" + param.id), 100);
        });
        $('#selectRunParamsTable').dataTable({
            //"sDom": "<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>>",
            "sPaginationType": "bootstrap",
            "oLanguage": {
                "sLengthMenu": "_MENU_ records per page"
            },
            "iDisplayLength": 5,
            "bLengthChange": false,
            bDestroy: true,
            "aoColumnDefs": [
                { "sWidth": "40%", "aTargets": [ 0 ] },
                { "sWidth": "40%", "aTargets": [ 1 ] },
                { "sWidth": "20%", "aTargets": [ 2 ] }
            ]
        });
    },
    printJobResult: function (job) {
        if (job == undefined) {
            return;
        }
        var self = this;

        if (window.app.status.currentTermsCollection == undefined) {
            new TermCollection({idProject: self.model.id}).fetch({
                success: function (terms, response) {
                    window.app.status.currentTermsCollection = terms;
                    self.initJobResult(job);
                }
            });
        } else {
            self.initJobResult(job);
        }
    },
    initJobResult: function (job) {
        $("#panelJobResultsDiv").empty();
        var self = this;
        var createJobResultView = function() {
            new JobResultView({
                model: job,
                project: self.model,
                el: $("#panelJobResultsDiv"),
                jobs: self.jobsLight,
                software: self.software,
                terms : window.app.status.currentTermsCollection
            }).render();
        }
        if (window.app.status.currentTermsCollection == undefined) {
            new TermCollection({idProject: self.model.id}).fetch({
                success: function (terms, response) {
                    window.app.status.currentTermsCollection = terms;
                    createJobResultView();
                }
            });
        } else {
            createJobResultView();
        }
    }

});