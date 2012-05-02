var LaunchJobView = Backbone.View.extend({
    width:null,
    software:null,
    project:null,
    parent:null,
    params:[],
    paramsViews:[],
    initialize:function (options) {
        this.width = options.width;
        this.software = options.software;
        this.project = options.project;
        this.parent = options.parent;
    },
    render:function () {
        var self = this;
        require([
            "text!application/templates/processing/JobLaunch.tpl.html"
        ],
                function (JobLaunchTpl) {
                    self.loadResult(JobLaunchTpl);
                });
        return this;
    },
    loadResult:function (JobLaunchTpl) {
        var self = this;
        var content = _.template(JobLaunchTpl, {});
        $(self.el).empty();
        $(self.el).append(content);
        self.params = [];
        self.paramsViews = [];

        var width = ($(window).width() - 200);
        var height = ($(window).height() - 200);
        $(self.el).dialog({
            modal:true,
            minWidth:Math.min(Math.round($(window).width() - 75), 800),
            minHeight:Math.round($(window).height() - 75),
            maxWidth:800,
            buttons:[
                {
                    text:"Cancel",
                    click:function () {
                        $(this).dialog("close");
                    }
                },
                {
                    text:"Create job",
                    click:function () {
                        self.createJobFromParam();

                    }
                }
            ], close:function (event, ui) {
                $("#userRetrievalSuggestMatrixDataTable").empty();
            }
        });
        $("#jobTitle").append("<h3>Run job from " + self.software.get('name') + " on project " + self.project.get('name') + " </h3>");

        _.each(self.software.get('parameters'), function (param) {
            if (param.name.toLowerCase() != "privatekey" && param.name.toLowerCase() != "publickey") {
                self.params.push(param);
                self.paramsViews.push(self.getParamView(param));
            }
        });

        self.printSoftwareParams();
//        self.checkEntryValidation();
    },
    getParamView:function (param) {
        if (param.type == "String") return new InputTextView({param:param});
        if (param.type == "Number") return new InputNumberView({param:param});
        if (param.type == "Date") return new InputDateView({param:param});
        if (param.type == "Boolean") return new InputBooleanView({param:param});
        if (param.type == "List") return new InputListView({param:param});
        if (param.type == "ListProject") return new InputListDomainView({param:param, multiple:true, collection:window.app.models.projects, printAttribut:"name"});
        if (param.type == "Project") return new InputListDomainView({param:param, multiple:false, collection:window.app.models.projects, printAttribut:"name"});


        else return new InputTextView({param:param});
    },
    printSoftwareParams:function () {
        var self = this;
        if (self.software == undefined) return;

        //build datatables
        $('#launchJobParamsTable').find('tbody').empty();
        var datatable = $('#launchJobParamsTable').dataTable();
        datatable.fnClearTable();
        var tbody = $('#launchJobParamsTable').find("tbody");

        _.each(self.paramsViews, function (paramView) {
            paramView.addRow(tbody);
            paramView.checkEntryValidation();
        });
        $('#launchJobParamsTable').dataTable({
            "sDom":"<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>>",
            "sPaginationType":"bootstrap",
            "oLanguage":{
                "sLengthMenu":"_MENU_ records per page"
            },
            "bSort":false,
            "iDisplayLength":1000,
            bDestroy:true,
            "aoColumnDefs":[
                { "sWidth":"25%", "aTargets":[ 0 ] },
                { "sWidth":"50%", "aTargets":[ 1 ] },
                { "sWidth":"25%", "aTargets":[ 2 ] }
            ]
        });
    },


    createJobFromParam:function () {
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
    createJobModel:function (params) {
        var self = this;
        var job = new JobModel({
            software:self.software.id,
            project:self.project.id,
            params:params
        });
        return job;
    },
    retrieveParams:function () {
        var self = this;
        var jobParams = [];

        _.each(self.paramsViews, function (paramView) {
            var softwareParam = paramView.param;
            var value = paramView.getStringValue();
            var param = {value:'"' + value + '"', softwareParameter:softwareParam.id};
            jobParams.push(param);
        });
        return jobParams;
    },
    saveJobModel:function (job) {
        var self = this;
        job.save({}, {
            success:function (model, response) {
                window.app.view.message("Add Job", response.message, "success");
                console.log(model.get('job').id);
                self.parent.changeJobSelection(model.get('job').id);
                $(self.el).dialog("close");
            },
            error:function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Add Job", "error:" + json.errors, "error");
            }
        });
    }
});


var InputTextView = Backbone.View.extend({
    param:null,
    parent:null,
    trElem:null,
    initialize:function (options) {
        this.param = options.param;
        this.parent = options.parent;
    },
    addRow:function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td style="text-align:left;">' + self.param.name + '</td><td style="text-align:center;">' + self.getHtmlElem() + '</td><td style="text-align:center;"><span class="label label-important hidden"></span></td></tr>');
        self.trElem = $('tr#' + self.param.id);
        self.trElem.find('input').keyup(function () {
            self.checkEntryValidation();
        });
    },
    getHtmlElem:function () {
        var self = this;
        var classRequier = "";
        if (self.param.required)  classRequier = 'border-style: solid; border-width: 2px;';
        else classRequier = 'border-style: dotted;border-width: 2px;';
        return '<div class="control-group success"><div class="controls"><input type="text" class="span3" value="' + self.getDefaultValue() + '" style="text-align:center;' + classRequier + '"></div></div>';
    },
    getDefaultValue:function () {
        return InputView.getDefaultValueWithVariable(this.param.defaultParamValue)
    },
    checkEntryValidation:function () {
        var self = this;
        console.log("checkEntryValidation");
        if (self.param.required && self.getValue().trim() == "") self.changeStyle(self.trElem, false, "Field require");
        else self.changeStyle(self.trElem, true, "");
    },
    getValue:function () {
        return this.trElem.find("input").val();
    },
    getStringValue:function () {
        return this.getValue();
    },
    changeStyle:function (elem, success, message) {
        InputView.changeStyle(elem, success, message);
    }
});

var InputNumberView = Backbone.View.extend({
    param:null,
    parent:null,
    trElem:null,
    initialize:function (options) {
        this.param = options.param;
        this.parent = options.parent;
    },
    addRow:function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td style="text-align:left;">' + self.param.name + '</td><td style="text-align:center;">' + self.getHtmlElem() + '</td><td style="text-align:center;"><span class="label label-important hidden"></span></td></tr>');
        self.trElem = $('tr#' + self.param.id);
        self.trElem.find('input').keyup(function () {
            self.checkEntryValidation();
        });
    },
    getHtmlElem:function () {
        var self = this;
        var classRequier = "";
        if (self.param.required)  classRequier = 'border-style: solid; border-width: 2px;';
        else classRequier = 'border-style: dotted;border-width: 2px;';
        return '<div class="control-group success"><div class="controls"><input type="text" class="span3" value="' + self.getDefaultValue() + '" style="text-align:center;' + classRequier + '"></div></div>';
    },
    getDefaultValue:function () {
        return InputView.getDefaultValueWithVariable(this.param.defaultParamValue)
    },
    checkEntryValidation:function () {
        var self = this;
        console.log("checkEntryValidation");
        if (self.param.required && self.getValue().trim() == "") self.changeStyle(self.trElem, false, "Field require");
        else if (self.getValue().trim() != "" && isNaN(self.getValue())) self.changeStyle(self.trElem, false, "Not valid number");
        else self.changeStyle(self.trElem, true, "");
    },
    getValue:function () {
        return this.trElem.find("input").val();
    },
    getStringValue:function () {
        return this.getValue();
    },
    changeStyle:function (elem, success, message) {
        InputView.changeStyle(elem, success, message);
    }
});


var InputDateView = Backbone.View.extend({
    param:null,
    parent:null,
    trElem:null,
    initialize:function (options) {
        this.param = options.param;
        this.parent = options.parent;
    },
    addRow:function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td style="text-align:left;">' + self.param.name + '</td><td style="text-align:center;">' + self.getHtmlElem() + '</td><td style="text-align:center;"><span class="label label-important hidden"></span></td></tr>');
        self.trElem = $('tr#' + self.param.id);

        var defaultValue = self.getDefaultValue();
        console.log("addRow defaultValue =" + defaultValue);
        var dateDefault = null;
        if (defaultValue != null) dateDefault = new Date(Number(defaultValue));
        console.log("dateDefault =" + dateDefault);

        self.trElem.find("input").datepicker({
            dateFormat:"yy-mm-dd",
            onSelect:function (dateText, inst) {
                self.checkEntryValidation();
            }
        });
        if (dateDefault != null)
            self.trElem.find("input").datepicker("setDate", dateDefault);

        self.trElem.find('input').keyup(function () {
            self.checkEntryValidation();
        });
    },
    getHtmlElem:function () {
        var self = this;
        var classRequier = "";
        if (self.param.required)  classRequier = 'border-style: solid; border-width: 2px;';
        else classRequier = 'border-style: dotted;border-width: 2px;';
        return '<div class="control-group success"><div class="controls"><input type="text" class="span3" value="" style="text-align:center;' + classRequier + '"></div></div>';
    },
    getDefaultValue:function () {
        return InputView.getDefaultValueWithVariable(this.param.defaultParamValue)
    },
    checkEntryValidation:function () {
        var self = this;
        var date = self.trElem.find("input").datepicker("getDate");
        console.log("checkEntryValidation");
        console.log(date);
        console.log((self.param.required && date == null));
        if (self.param.required && date == null) self.changeStyle(self.trElem, false, "Field require");
        else self.changeStyle(self.trElem, true, "");
    },
    getValue:function () {
        var self = this;
        var date = self.trElem.find("input").datepicker("getDate");
        if (date == null) return null;
        else return date.getTime();
    },
    getStringValue:function () {
        return this.getValue();
    },
    changeStyle:function (elem, success, message) {
        InputView.changeStyle(elem, success, message);
    }
});

var InputBooleanView = Backbone.View.extend({
    param:null,
    parent:null,
    trElem:null,
    initialize:function (options) {
        this.param = options.param;
        this.parent = options.parent;
    },
    addRow:function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td style="text-align:left;">' + self.param.name + '</td><td style="text-align:center;">' + self.getHtmlElem() + '</td><td style="text-align:center;"><span class="label label-important hidden"></span></td></tr>');
        self.trElem = $('tr#' + self.param.id);
        self.trElem.find('input').keyup(function () {
            self.checkEntryValidation();
        });
    },
    getHtmlElem:function () {
        return '<input type="checkbox" class="span3" ' + this.getDefaultValue() + ' />';
    },
    getDefaultValue:function () {
        if (this.param.type == "Boolean" && this.param.defaultParamValue != undefined && this.param.defaultParamValue.toLowerCase() == "true") return 'checked="checked"';
        return "";
    },
    checkEntryValidation:function () {
        var self = this;
        console.log("checkEntryValidation");
        self.changeStyle(self.trElem, true, "");
    },
    getValue:function () {
        return this.trElem.find("input").is(":checked");
    },
    getStringValue:function () {
        return this.getValue() + '';
    },
    changeStyle:function (elem, success, message) {
        InputView.changeStyle(elem, success, message);
    }
});

var InputListView = Backbone.View.extend({
    param:null,
    parent:null,
    trElem:null,
    initialize:function (options) {
        this.param = options.param;
        this.parent = options.parent;
    },
    addRow:function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td style="text-align:left;">' + self.param.name + '</td><td style="text-align:center;">' + self.getHtmlElem() + '</td><td style="text-align:center;"><span class="label label-important hidden"></span></td></tr>');
        self.trElem = $('tr#' + self.param.id);
        self.trElem.find('.icon-plus-sign').click(function () {
            console.log("Add entry");
            var value = $(this).parent().find("input").val();
            if (value.trim() != "") {
                $(this).parent().find("select").append('<option value="' + value + '">' + value + '</option>');
                $(this).parent().find("input").val("");
                $(this).parent().find("select").val(value);
                self.checkEntryValidation();
            }
        });

        self.trElem.find('.icon-minus-sign').click(function () {

            var value = $(this).parent().find("select").val();
            console.log("delete entry:" + value);
            console.log("delete entry:" + $(this).parent().find("select").find('[value="' + value + '"]').length);
            $(this).parent().find("select").find('[value="' + value + '"]').remove();
            self.checkEntryValidation();
        });
    },
    getHtmlElem:function () {
        var self = this;
        var classRequier = ""
        if (self.param.required)  classRequier = 'border-style: solid; border-width: 2px;'
        else classRequier = 'border-style: dotted;border-width: 2px;'
        var defaultValues = self.getDefaultValue();
        var valueStr = '<div class="control-group success"><div class="controls"><input type="text" class="span3" value="' + "" + '" style="text-align:center;' + classRequier + '"><i class="icon-plus-sign"></i><select>';
        _.each(defaultValues, function (value) {
            valueStr = valueStr + '<option value="' + value + '">' + value + '</option>';
        });
        valueStr = valueStr + '</select><i class="icon-minus-sign"></i></div></div>';
        return valueStr;
    },
    getDefaultValue:function () {
        var self = this;
        var split = self.param.defaultParamValue.split(",");
        var values = [];
        console.log(split);
        _.each(split, function (s) {
            console.log(s);
            values.push(InputView.getDefaultValueWithVariable(s));
        });
        console.log(values);
        return values;
    },
    checkEntryValidation:function () {
        var self = this;
        console.log("checkEntryValidation");
        if (self.trElem.find("select").children().length == 0) self.changeStyle(self.trElem, false, "Field require");
        else self.changeStyle(self.trElem, true, "");
    },
    getValue:function () {
        var self = this;
        var valueArray = [];
        self.trElem.find("select").find("option").each(function () {
            valueArray.push($(this).attr("value"));
        });
        return valueArray.join(',');
    },
    getStringValue:function () {
        return this.getValue();
    },
    changeStyle:function (elem, success, message) {
        InputView.changeStyle(elem, success, message);
    }
});

var InputListDomainView = Backbone.View.extend({
    param:null,
    parent:null,
    trElem:null,
    multiple:true,
    collection:null,
    printAttribut:null,
    initialize:function (options) {
        this.param = options.param;
        this.parent = options.parent;
        this.multiple = options.multiple;
        this.collection = options.collection;
        this.printAttribut = options.printAttribut;
    },
    addRow:function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td style="text-align:left;">' + self.param.name + '</td><td id="' + self.param.id + '" style="text-align:center;"></td><td style="text-align:center;"><span class="errorMessage label label-important hidden"></span></td></tr>');
        self.trElem = tbody.find('tr#' + self.param.id);
        console.log("*****************************");
        console.log(self.collection);
        console.log(self.collection.at(0));
        if (self.collection == undefined || (self.collection.length > 0 && self.collection.at(0).id == undefined)) {
            self.collection.fetch({
                success:function (collection, response) {
                    console.log("*****************************");
                    console.log(collection);
                    self.collection = collection;
                    self.addHtmlElem();
                }
            });
        } else self.addHtmlElem();
    },
    addHtmlElem:function () {
        var self = this;
        self.trElem.find("td#" + self.param.id).append(self.getHtmlElem());


        var fnSelectionView = null;

        if (self.multiple) {
            fnSelectionView = function (numChecked, numTotal, checkedItem) {
                var selectTitle = [];
                $(checkedItem).each(function () {
                    selectTitle.push($(this).attr("title"));
                });
                var selectText = selectTitle.join(", ");
                selectText = selectText.substring(0, Math.min(15, selectText.length));
                return numChecked + " selected: " + selectText + "...";
            }
        } else {
            fnSelectionView = function (numChecked, numTotal, checkedItem) {
                if (numChecked == 0) return "0 selected";
                var selectTitle = [];
                $(checkedItem).each(function () {
                    selectTitle.push($(this).attr("title"));
                });
                var selectText = selectTitle.join(", ");
                selectText = selectText.substring(0, Math.min(15, selectText.length));
                return selectText;
            };
        }

        self.trElem.find(".domainList").multiselect({'autoOpen':false, minWidth:300, 'height':200, 'multiple':self.multiple, 'selectedText':fnSelectionView}).multiselectfilter();
        //put header menu option on the same line
        self.trElem.find(".ui-multiselect-menu").find("span").css("display", "inline");
        self.trElem.find(".ui-multiselect-menu").find(".ui-multiselect-header").find("li").css("display", "inline");
        //put check all on left and deselect all on right
        self.trElem.find(".ui-multiselect-menu").find(".ui-multiselect-header").find("li").eq(0).css("float", "left");
        self.trElem.find(".ui-multiselect-menu").find(".ui-multiselect-header").find("li").eq(1).css("float", "right");
        self.trElem.find(".ui-multiselect-menu").find("li").css("display", "block");
        //print scroll only vertical
        self.trElem.find("ul.ui-multiselect-checkboxes").css('overflow-y', 'scroll');
        self.trElem.find("ul.ui-multiselect-checkboxes").css('overflow-x', 'hidden');
        //autoOpen:false doesn't work, so click to hide open multiselect
//                self.trElem.find('button.ui-multiselect').click();
//                self.trElem.find('button.ui-multiselect').click();
        self.trElem.find(".domainList").multiselect("close");

        self.checkEntryValidation();


        self.trElem.find(".domainList").bind("multiselectclick", function (event, ui) {
            self.checkEntryValidationWithAllValue(ui.value, ui.checked);
        });

    },
    getDefaultValue:function () {
        var self = this;
        var split = self.param.defaultParamValue.split(",");
        var values = [];
        console.log(split);
        _.each(split, function (s) {
            console.log(s);
            values.push(InputView.getDefaultValueWithVariable(s));
        });
        console.log(values);
        return values;
    },
    getHtmlElem:function () {
        var self = this;
        var classRequier = "";
        //mark default value as selected:
        var defaultValues = self.getDefaultValue();
        if (self.param.required)  classRequier = 'border-style: solid; border-width: 2px;'
        else classRequier = 'border-style: dotted;border-width: 2px;'
        var valueStr = '<select class="domainList" multiple="multiple">';
        self.collection.each(function (value) {
            var selClass = "";
            _.each(defaultValues, function (def) {
                if (def == value.id) {
                    selClass = 'selected="selected"';
                }
            });
            valueStr = valueStr + '<option ' + selClass + ' value="' + value.id + '">' + value.get(self.printAttribut) + '</option>';
        });
        valueStr = valueStr + '</select>';
        return valueStr;
    },
    checkEntryValidationWithAllValue:function (value, checked) {
        var self = this;
        console.log("checkEntryValidation");

        var values = self.getValue();
        var length = values.length;
        if (checked) length++;
        else length--;
        console.log(self.param.required + "|" + values);
        if (self.param.required && length == 0) self.changeStyle(self.trElem, false, "Field require");
        else self.changeStyle(self.trElem, true, "");

    },
    checkEntryValidation:function () {
        var self = this;
        console.log("checkEntryValidation");
        var values = self.getValue();
        console.log(self.param.required + "|" + values.length);
        if (self.param.required && values.length == 0) self.changeStyle(self.trElem, false, "Field require");
        else self.changeStyle(self.trElem, true, "");
    },
    getValue:function () {
        var self = this;
        var values = self.trElem.find(".domainList").val();
        if (values == undefined) return [];
        else return values;
    },
    getStringValue:function () {
        var self = this;
        var values = self.trElem.find(".domainList").val();
        if (values == undefined) return "";
        else return values.join(",");
    },
    changeStyle:function (elem, success, message) {
        var labelElem = elem.find('.errorMessage');
        if (success) {
            labelElem.addClass("hidden");
            labelElem.text("");
        } else {
            labelElem.removeClass("hidden");
            labelElem.text("");
            labelElem.text(message);
        }
    }
});

var InputView = {
    changeStyle:function (elem, success, message) {
        var className = "";
        //color input
        if (success) className = "success";
        else  className = "error";
        var valueElem = elem.children().eq(1).children().eq(0);
        valueElem.removeClass("success");
        valueElem.removeClass("error");
        valueElem.addClass(className);

        var labelElem = elem.find('span');
        if (success) {
            labelElem.addClass("hidden");
            labelElem.text("");
        } else {
            labelElem.removeClass("hidden");
            labelElem.text("");
            labelElem.text(message);
        }
    },
    getDefaultValueWithVariable:function (value) {
        var self = this;
        if (value == "$currentProject") return window.app.status.currentProject;
        else if (value == "$cytomineHost") return window.location.protocol + "//" + window.location.host;
        else if (value == "$currentDate") return new Date().getTime();
        else if (value == "$currentProjectCreationDate") return  window.app.status.currentProjectModel.get('created');
        else return value;
    }
};
