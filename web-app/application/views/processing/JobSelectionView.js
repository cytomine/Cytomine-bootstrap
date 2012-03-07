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
   initialize: function(options) {
       var self = this;
        this.software = options.software;
        this.project = options.project;
        this.parent = options.parent;
        this.jobs = options.jobs;
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
       console.log("LOAD RESULT");
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
        console.log("refreshWithDate="+date);
        new JobCollection({ project : self.project.id, software: self.software.id, light:true}).fetch({
            success : function (collection, response) {
               self.jobs = collection;
               self.loadDateArray();
               self.printDatatables(self.jobs.models,date);
               console.log("refreshWithDate="+date);
                self.printDataPicker(date);
                $(self.el).find("#datepicker").datepicker('setDate', date);
                self.refreshDatePicker();
               //self.printDataPicker(date);
            }
        });
    },
    loadDateArray : function() {
        var self= this;
        self.availableDate = [];
        self.jobs.each(function (job) {
            //fill availableDate array with (yyyy/mm/dd) timestamp (without hour/min/sec)
            var createdDate = new Date();
            createdDate.setTime(job.get('created'));
            createdDate = new Date(createdDate. getFullYear(), createdDate.getMonth(), createdDate.getDate());
            self.availableDate.push(createdDate.getTime());
        });
    },

   printDataPicker : function(date) {
       var self = this;
       console.log("printDataPicker:"+date);

       $(self.el).find("#datepicker").datepicker({
                beforeShowDay: function(date)
                {
                   if(_.indexOf(self.availableDate, date.getTime())!=-1) {
                       return [true,"",""];
                   } else {
                       return [false,"","No job was run at this date!"];
                   }
                },
                onSelect: function(dateStr) {
                    console.log("onSelect event!");
                   self.refreshDatePicker();
                }
        });
        if(date!=undefined) {
            self.disableDateChangeEvent = true;
             $(self.el).find("#datepicker").datepicker('setDate', date);
            self.disableDateChangeEvent = false;
        }

   },
   refreshDatePicker : function() {
       var self = this;
        console.log("onSelect event!");
        if(!self.disableDateChangeEvent) {
           var date = $(self.el).find("#datepicker").datepicker( "getDate" );
            if(date!=null) {
               self.currentDate = date;
               console.log("self.currentDate="+self.currentDate);
               var indx = self.findJobIndiceBetweenDateTime(date.getTime(),date.getTime() + 86400000); //86400000ms in a day
               var jobs = self.findJobByIndice(indx);
                console.log(jobs);
                self.printDatatables(jobs,date);
            }
        }
    },
   findJobIndiceBetweenDateTime : function(min,max) {
       var self = this;
       var correctDateArray = _.map(self.availableDate, function(date, indx){
           if(date>=min && date<max) return indx;
           else return -1;
       });
       correctDateArray = _.without(correctDateArray,-1);
       return correctDateArray;
   },
   findJobByIndice : function (indiceArray) {
       var self = this;
       var jobArray = [];
       console.log(self.jobs);
       _.each(indiceArray,function(indx) {
             console.log(indx);
           console.log(self.jobs.at(indx));
            jobArray.push(self.jobs.at(indx));
       });
       return jobArray;
   },
   printDatatables : function(jobs,date) {
       var self = this;
       var selectRunParamElem = $('#selectJobTable').find('tbody').empty();
       var datatable = $('#selectJobTable').dataTable();
        datatable.fnClearTable();
        //print data from project image table
        var tbody = $('#selectJobTable').find("tbody");
       if(jobs!=undefined) {
             _.each(jobs, function (job) {
                var cellIcon = '<i class="icon-plus"></i>';
                var cellId = job.id;
                var cellDate = window.app.convertLongToDate(job.get('created'));
                var cellState = self.getStateElement(job);
                var cellSee = '<h4 style="display:inline;"><a href="#tabs-algos-'+self.project.id + "-" + self.software.id + "-" +job.id+'" id="'+job.id+'">See details <br></a></h4>'
                tbody.append('<tr><td>'+cellIcon+'</td><td>'+cellId+'</td><td>'+cellDate+'</td><td>'+cellState+'</td><td>'+cellSee+'</td></tr>');
             });
       }
       self.table = $('#selectJobTable').dataTable( {
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
                { "sWidth": "10%", "aTargets": [ 0 ] },
                { "sWidth": "10%", "aTargets": [ 1 ] },
                { "sWidth": "30%", "aTargets": [ 2 ] },
                { "sWidth": "20%", "aTargets": [ 3 ] },
                { "sWidth": "30%", "aTargets": [ 3 ] }
             ]
        });
       self.initSubGridDatatables();

   },
    getStateElement : function(job) {
        if(job.get('running')) {
            //return progress
            return '<span class="label btn-primary">Progress!</span> '
        } else {
            if(job.get('successful')) {
                //return success
                return '<span class="label btn-success">Success!</span>';
            }
            else {
                //return fail
                return '<span class="label btn-danger">Fail!</span>'
            }

        }
    },
    initSubGridDatatables : function() {
        var self = this;

        $('#selectJobTable tbody td i').live( 'click', function () {
            console.log('CLICK:' + self.table.fnIsOpen(nTr));
            var nTr = $(this).parents('tr')[0];
            if ( self.table.fnIsOpen(nTr) )
            {
                /* This row is already open - close it */
                this.class = "icon-plus";
                self.table.fnClose( nTr );
            }
            else
            {
                /* Open this row */
                this.class = "icon-minus";
                self.table.fnOpen( nTr, self.seeDetails(nTr), 'details' );
                var aData = self.table.fnGetData( nTr );
                console.log("aData[1]="+aData[1]);
                new JobModel({ id : aData[1]}).fetch({
                    success : function (model, response) {
                        console.log('SUCCESS');
                        console.log(model.get('jobParameter'));
                        var tableParam = $('#selectJobTable').find('table[id='+aData[1]+']');
                        _.each(model.get('jobParameter'),function(param) {
                             tableParam.append('<tr><td>'+param.name+'</td><td>'+param.value+'</td><td>'+param.type+'</td></tr>');
                        });
                    }
                });

            }

    });
    },
/* Formating function for row details */
    seeDetails : function( nTr )
    {
        var self = this;
        var aData = self.table.fnGetData( nTr );

        var sOut = '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;" id="'+aData[1]+'">';
        sOut += '</table>';

        return sOut;
    }

});