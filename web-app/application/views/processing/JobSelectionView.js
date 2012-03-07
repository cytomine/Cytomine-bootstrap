var JobSelectionView = Backbone.View.extend({
    width : null,
    software: null,
    project : null,
    jobs : null,
    parent : null,
    availableDate : null,
   initialize: function(options) {
       var self = this;
        this.software = options.software;
        this.project = options.project;
        this.parent = options.parent;
        this.jobs = options.jobs;

        this.availableDate = [];
        this.jobs.each(function (job) {
            //fill availableDate array with (yyyy/mm/dd) timestamp (without hour/min/sec)
            var createdDate = new Date();
            createdDate.setTime(job.get('created'));
            createdDate = new Date(createdDate. getFullYear(), createdDate.getMonth(), createdDate.getDate());
            self.availableDate.push(createdDate.getTime());
        });
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
      $(self.el).empty();
      $(self.el).append(content);
      self.printDatatables(undefined);
      self.printDataPicker(undefined);

   },
   printDataPicker : function(date) {
       var self = this;
       console.log(self.availableDate);
       $(self.el).find("#datepicker").datepicker({
                beforeShowDay: function(date)
                {
                    console.log(date.getTime());
                    console.log(_.indexOf(self.availableDate, date.getTime()));
                   if(_.indexOf(self.availableDate, date.getTime())!=-1) {
                       return [true,"",""];
                   } else {
                       return [false,"","No job was run at this date!"];
                   }
                },
                onSelect: function(dateStr) {

                   var date = $(self.el).find("#datepicker").datepicker( "getDate" );
                    console.log(date);
                   var indx = self.findJobIndiceBetweenDateTime(date.getTime(),date.getTime() + 86400000); //86400000ms in a day
                    console.log(indx);
                   var jobs = self.findJobByIndice(indx);
                    console.log(jobs);
                    self.printDatatables(jobs,date);

                }
        });

        if(date!=undefined) {
             $(self.el).find("#datepicker").datepicker('setDate', date);
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
                tbody.append('<tr><td>'+job.get('id')+'</td><td>'+window.app.convertLongToDate(job.get('created'))+'</td><td>'+job.get('successful')+'</td></tr>');
             });
       }

        $('#selectJobTable').dataTable( {
            "sDom": '<"toolbar">frtip',
            "sPaginationType": "bootstrap",
            "oLanguage": {
                "sLengthMenu": "_MENU_ records per page"
            },
            "iDisplayLength": 5 ,
            "bLengthChange" : false,
            bDestroy: true
        });
        $("div.toolbar").html('Date: <input type="text" id="datepicker" class="datepicker">');
       self.printDataPicker(date);
   }
});