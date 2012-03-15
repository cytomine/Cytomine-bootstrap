var JobSelectionView = Backbone.View.extend({
    width : null,
    software: null,
    project : null,
    jobs : null,
    parent : null,
    availableDate : null,
    table : null,
    disableDateChangeEvent : false,
    currentDate : undefined,
    comparator : false,
    selectedJob : null,
    initialize: function(options) {
        var self = this;
        this.software = options.software;
        this.project = options.project;
        this.parent = options.parent;
        this.jobs = options.jobs;
        this.comparator = options.comparator;
        this.loadDateArray();
    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/processing/JobSelection.tpl.html"
        ],
               function(jobSelectionViewTpl) {
                   self.loadResult(jobSelectionViewTpl);
               });
        return this;
    },
    loadResult : function (jobSelectionViewTpl) {
        var self = this;
        var content = _.template(jobSelectionViewTpl, {});
        console.log("loadResult");
        $(self.el).empty();
        $(self.el).append(content);
        self.printDatatables(self.jobs.models);
        self.printDataPicker(undefined);

        $(self.el).find("#seeAllButton").click(function() {
            self.currentDate = undefined;
            self.disableDateChangeEvent = true;
            $(self.el).find("#datepicker").datepicker('setDate', null);
            self.disableDateChangeEvent = false;
            self.refresh();
        });

        $(self.el).find("#refreshButton").click(function() {
            self.refreshWithDate(self.currentDate);
            $(self.el).find("#datepicker").datepicker('setDate', self.currentDate);
        });

    },
    refresh : function() {
        this.refreshWithDate(undefined)
    },
    refreshWithDate : function(date) {
        var self = this;
        new JobCollection({ project : self.project.id, software: self.software.id, light:true}).fetch({
            success : function (collection, response) {
                self.jobs = collection;
                self.loadDateArray();
                self.printDatatables(self.jobs.models, date);
                self.printDataPicker(date);
                $(self.el).find("#datepicker").datepicker('setDate', date);
                self.refreshDatePicker();
                //self.printDataPicker(date);
            }
        });
    },
    loadDateArray : function() {
        var self = this;
        self.availableDate = [];
        self.jobs.each(function (job) {
            //fill availableDate array with (yyyy/mm/dd) timestamp (without hour/min/sec)
            var createdDate = new Date();
            createdDate.setTime(job.get('created'));
            createdDate = new Date(createdDate.getFullYear(), createdDate.getMonth(), createdDate.getDate());
            self.availableDate.push(createdDate.getTime());
        });
    },
    printDataPicker : function(date) {
        var self = this;
        $(self.el).find("#datepicker").datepicker({
            beforeShowDay: function(date) {
                if (_.indexOf(self.availableDate, date.getTime()) != -1) {
                    return [true,"",""];
                } else {
                    return [false,"","No job was run at this date!"];
                }
            },
            onSelect: function(dateStr) {
                self.refreshDatePicker();
            }
        });
        if (date != undefined) {
            self.disableDateChangeEvent = true;
            $(self.el).find("#datepicker").datepicker('setDate', date);
            self.disableDateChangeEvent = false;
        }

    },
    refreshDatePicker : function() {
        var self = this;
        if (!self.disableDateChangeEvent) {
            var date = $(self.el).find("#datepicker").datepicker("getDate");
            if (date != null) {
                self.currentDate = date;
                console.log("self.currentDate=" + self.currentDate);
                var indx = self.findJobIndiceBetweenDateTime(date.getTime(), date.getTime() + 86400000); //86400000ms in a day
                var jobs = self.findJobByIndice(indx);
                console.log(jobs);
                self.printDatatables(jobs, date);
            }
        }
    },
    findJobIndiceBetweenDateTime : function(min, max) {
        var self = this;
        var correctDateArray = _.map(self.availableDate, function(date, indx) {
            if (date >= min && date < max) return indx;
            else return -1;
        });
        correctDateArray = _.without(correctDateArray, -1);
        return correctDateArray;
    },
    findJobByIndice : function (indiceArray) {
        var self = this;
        var jobArray = [];
        _.each(indiceArray, function(indx) {
            jobArray.push(self.jobs.at(indx));
        });
        return jobArray;
    },
    printDatatables : function(jobs, date) {
        var self = this;

        //rebuilt table
        var selectRunParamElem = $(self.el).find('#selectJobTable').find('tbody').empty();
        var datatable = $(self.el).find('#selectJobTable').dataTable();

        //show hidden column (if not done, datatable is not filled)
        datatable.fnSetColumnVis( 1, true);

        datatable.fnClearTable();
        //print data from project image table
        var tbody = $(self.el).find('#selectJobTable').find("tbody");
        if (jobs != undefined) {
            _.each(jobs, function (job) {
                var cellIcon = '<i class="icon-plus"></i>';
                var cellId = job.id;
                var cellNumber = job.get('number');
                var cellDate = window.app.convertLongToDate(job.get('created'));
                var cellState = self.getStateElement(job);
                var cellSee = "";
                if (self.comparator) {
                    //if comparator then print "compare" and click must refresh parent
                    cellSee = '<a id="select' + job.id + '">Compare</a>'
                } else {
                    //else if comparator then print "see details" and click must select job
                    cellSee = '<a href="#tabs-algos-' + self.project.id + "-" + self.software.id + "-" + job.id + '" id="' + job.id + '">See details<br></a>'
                }
                tbody.append('<tr><td>' + cellIcon + '</td><td  style="text-align:left;">' + cellId + '</td><td  style="text-align:center;">' + cellNumber + '</td><td  style="text-align:center;">' + cellDate + '</td><td  style="text-align:center;">' + cellState + '</td><td>' + cellSee + '</td></tr>');

                if (self.comparator) {
                    tbody.find("#select" + job.id).click(function() {
                        self.selectedJob = job.id;
                        //self.parent.refresh();
                    });
                }
            });
        }
        self.table = $(self.el).find('#selectJobTable').dataTable({
            "bFilter": false,
            "sDom": '<"toolbar">frtip',
            "sPaginationType": "bootstrap",
            "oLanguage": {
                "sLengthMenu": "_MENU_ records per page"
            },
            "iDisplayLength": 5 ,
            "bLengthChange" : false,
            bDestroy: true,
            "aoColumnDefs": [
                { "sWidth": "5%", "aTargets": [ 0 ] },
                { "sWidth": "10%", "aTargets": [ 1 ]},
                { "sWidth": "10%", "aTargets": [ 2 ] },
                { "sWidth": "25%", "aTargets": [ 3 ] },
                { "sWidth": "20%", "aTargets": [ 4 ] },
                { "sWidth": "30%", "aTargets": [ 5 ] }
            ]
        });


        self.initSubGridDatatables();

        //hide id column
        self.table.fnSetColumnVis( 1, false);
    },
    getStateElement : function(job) {
        if(job.isNotLaunch()) return '<span class="label btn-inverse">Not Launch!</span> ';
        else if(job.isInQueue()) return '<span class="label btn-info">In queue!</span> ' ;
        else if(job.isRunning()) return '<span class="label btn-primary">Running!</span> ' ;
        else if(job.isSuccess()) return '<span class="label btn-success">Success!</span> ';
        else if(job.isFailed()) return '<span class="label btn-danger">Failed!</span> ';
        else if(job.isIndeterminate()) return '<span class="label btn-inverse">Indetereminate!</span> ' ;
        else if(job.isWait()) return '<span class="label btn-warning">Wait!</span> ' ;
        else return "no supported";
    },
    initSubGridDatatables : function() {
        var self = this;

        $(self.el).find('#selectJobTable tbody td i').live('click', function () {
            console.log('CLICK:' + self.table.fnIsOpen(nTr));
            var nTr = $(this).parents('tr')[0];
            if (self.table.fnIsOpen(nTr)) {
                /* This row is already open - close it */
                this.class = "icon-plus";
                self.table.fnClose(nTr);
            }
            else {
                /* Open this row */
                this.class = "icon-minus";
                self.table.fnOpen(nTr, self.seeDetails(nTr), 'details');
                var aData = self.table.fnGetData(nTr);
                console.log("aData[1]=" + aData[1]);
                new JobModel({ id : aData[1]}).fetch({
                    success : function (model, response) {
                        var tableParam = $(self.el).find('#selectJobTable').find('table[id=' + aData[1] + ']');
                        _.each(model.get('jobParameter'), function(param) {
                            tableParam.append('<tr><td>' + param.name + '</td><td>' + param.value + '</td><td>' + param.type + '</td></tr>');
                        });
                    }
                });

            }

        });
    },
    /* Formating function for row details */
    seeDetails : function(nTr) {
        var self = this;
        var aData = self.table.fnGetData(nTr);

        var sOut = '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;" id="' + aData[1] + '">';
        sOut += '</table>';

        return sOut;
    }

});