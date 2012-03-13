var LaunchJobView = Backbone.View.extend({
    width : null,
    software: null,
    project : null,
    parent : null,
    params : [],
    initialize: function(options) {
        this.width = options.width;
        this.software = options.software;
        this.project = options.project;
        this.parent = options.parent;
    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/processing/JobLaunch.tpl.html"
        ],
               function(JobLaunchTpl) {
                   self.loadResult(JobLaunchTpl);
               });
        return this;
    },
    loadResult : function (JobLaunchTpl) {
        var self = this;
        var content = _.template(JobLaunchTpl, {});
        $(self.el).empty();
        $(self.el).append(content);
        self.params = [];

        var width = ($(window).width() - 200);
        var height = ($(window).height() - 200);
        $(self.el).dialog({
            modal : true,
            minWidth : Math.min(Math.round($(window).width() - 75),800),
            minHeight : Math.round($(window).height() - 75),
            maxWidth : 800,
            buttons: [
                {
                    text: "Cancel",
                    click: function() {
                        $(this).dialog("close");
                    }
                },{
                    text: "Create job",
                    click: function() {
                        self.createJobFromParam();

                    }
                }
            ],close: function(event, ui) {
                $("#userRetrievalSuggestMatrixDataTable").empty();
            }
        });
        $("#jobTitle").append("<h3>Run job from " + self.software.get('name') + " on project " + self.project.get('name') +" </h3>");
         _.each(self.software.get('parameters'), function (param) {
            if(param.name.toLowerCase() != "privatekey" && param.name.toLowerCase() != "publickey")
               self.params.push(param);
            });
        self.printSoftwareParams();
        self.checkEntryValidation();
    },
    printSoftwareParams: function() {
        var self = this;
        if(self.software==undefined) return;
        $('#launchJobParamsTable').find('tbody').empty();
        var datatable = $('#launchJobParamsTable').dataTable();
        datatable.fnClearTable();
        //print data from project image table
        var tbody = $('#launchJobParamsTable').find("tbody");

         _.each(self.params, function (param) {
               tbody.append('<tr><td style="text-align:left;">'+param.name+ '</td><td style="text-align:center;">'+self.getEntryByType(param)+'</td><td style="text-align:center;"><span class="label label-important hidden"></span></td></tr>');
         });
        $('#launchJobParamsTable').dataTable( {
            "sDom": "<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>>",
            "sPaginationType": "bootstrap",
            "oLanguage": {
                "sLengthMenu": "_MENU_ records per page"
            },
            "bSort": false,
            "iDisplayLength": 1000 ,
            bDestroy: true,
                 "aoColumnDefs": [
                { "sWidth": "25%", "aTargets": [ 0 ] },
                { "sWidth": "50%", "aTargets": [ 1 ] },
                { "sWidth": "25%", "aTargets": [ 2 ] }
             ]
        });

        $('#launchJobParamsTable').find('input').keyup(function() {
             self.checkEntryValidation();
        });

    },
    getEntryByType : function(param) {
        var self = this;
        console.log("defaultValue=" + param.defaultParamValue);
        var classRequier = ""
        if(param.required)  classRequier = 'border-style: solid; border-width: 2px;'
        else classRequier = 'border-style: dotted;border-width: 2px;'

        //TODO: décoder les variables (style $currentProject)

        if (param.type == "String") {
            return '<div class="control-group success"><div class="controls"><input type="text" class="span3" value="' + self.getDefaultValue(param) + '" style="text-align:center;'+classRequier+'"></div></div>';
        }
        if (param.type == "Number") {
            return '<div class="control-group success"><div class="controls"><input type="text" class="span3" value="' + self.getDefaultValue(param) + '" style="text-align:center;'+classRequier+'" ></div></div>';
        }
        if (param.type == "Boolean") {
            return '<input type="checkbox" class="span3" ' + self.getDefaultValue(param) + ' />';
        }
    },
    getDefaultValue : function(param) {
        var self = this;
        if(param.type=="Boolean" && param.defaultParamValue!=undefined && param.defaultParamValue.toLowerCase()=="true") return 'checked="checked"';
        if(param.defaultParamValue=="$currentProject") return self.project.id;
        if(param.defaultParamValue=="$cytomineHost") return window.location.protocol + "//" + window.location.host;
        return param.defaultParamValue;
    },
    checkEntryValidation : function() {
        //browse software param and // datatables
        var self = this;
        $('#launchJobParamsTable tbody tr').each( function (index,elem) {
            var trElem = $(this);
            var param = self.params[index];

            var value = self.getValue(param,trElem);
            console.log("value="+value);
            if(!self.checkRequire(param, value)) {
                self.changeInputRender(trElem,false);
                self.changeLabelRender(trElem,false,"Field require");
            }
            else if(!self.checkType(param, value)) {
                self.changeInputRender(trElem,false);
                self.changeLabelRender(trElem,false,"Must be valid " + param.type);
            }
            else {
                self.changeInputRender(trElem,true);
                self.changeLabelRender(trElem,true,"");
            }
        });

    },
    checkRequire : function(param, value) {
        return !(param.required && value.trim()=="");
    },
    checkType : function(param, value) {
        if (param.type == "Number") return !isNaN(value);
        return true;
    },
    getValue: function (param,elem) {
        console.log("getValue="+elem.find("input").val());
        if(param.type=="String") return elem.find("input").val();
        if(param.type=="Number") return elem.find("input").val();
        if(param.type=="Boolean") {
            console.log(elem.find("input").is(":checked"));
            if(elem.find("input").is(":checked")) return true;
            else return false;
        }
        return "";
    },
    changeInputRender :function(elem,success) {
        var className = "";
        //color input
        if(success) className = "success";
        else  className = "error";
        var valueElem = elem.children().eq(1).children().eq(0);
        valueElem.removeClass("success");
        valueElem.removeClass("error");
        valueElem.addClass(className);
    },
    changeLabelRender :function(elem,success,message) {
        var labelElem = elem.find('span');
        if(success) {
            labelElem.addClass("hidden");
            labelElem.text("");
        }else {
            labelElem.removeClass("hidden");
            labelElem.text("");
            labelElem.text(message);
        }
    },
    createJobFromParam :function() {
        var self = this;
        //retrieve an array of param
        console.log("retrieveParams()...");
        var params = self.retrieveParams();
        //create job model
        console.log("createJobModel()...");
        var job = self.createJobModel(params);
        //create a job, in post data, add param array
//        console.log("job.set()...");
//        job.set('jobParameter',params);
//        //send job
        console.log("self.saveJobModel(job)...");
        self.saveJobModel(job);

        //adapt grails to support jobparams inside job (put private and public key)

        //close windows (ok in dialog), refresh daashboardalog view
    },
    createJobModel : function(params) {
        var self = this;
        var job = new JobModel({
            software: self.software.id,
            project : self.project.id,
            params : params
        });
        return job;
    },
    retrieveParams : function() {
        var self = this;
        var jobParams = [];
        $('#launchJobParamsTable tbody tr').each( function (index,elem) {
            var trElem = $(this);
            var softwareParam = self.params[index];
            var value = self.getValue(softwareParam,trElem);
            console.log(softwareParam);
            var param = {value:'"'+value+'"', softwareParameter: softwareParam.id};
            jobParams.push(param);
        });
        return jobParams;
    },
    saveJobModel : function(job) {
        var self = this;
        job.save({}, {
            success: function (model, response) {
                window.app.view.message("Add Job",response.message, "success");
                console.log(model.get('job').id);
                self.parent.changeJobSelection(model.get('job').id);
                $(self.el).dialog("close");
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Add Job", "error:" + json.errors, "error");
            }
        });
    }
});
