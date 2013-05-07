var LaunchJobView = Backbone.View.extend({
    width: null,
    software: null,
    project: null,
    parent: null,
    params: [],
    paramsViews: [],
    initialize: function (options) {
        this.width = options.width;
        this.software = options.software;
        this.project = options.project;
        this.parent = options.parent;
        this.el = options.el;
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/processing/JobLaunch.tpl.html"
        ],
            function (JobLaunchTpl) {

                self.loadResult(JobLaunchTpl);
            });
        return this;
    },
    loadResult: function (JobLaunchTpl) {

        var self = this;
        var content = _.template(JobLaunchTpl, {});

        $(self.el).empty();
        $(self.el).append(content);
        self.params = [];
        self.paramsViews = [];

        $("#jobTitle").append("<h3>Run job from " + self.software.get('name') + " on project " + self.project.get('name') + " </h3>");

        _.each(self.software.get('parameters'), function (param) {
            self.params.push(param);
            self.paramsViews.push(self.getParamView(param));
        });

        self.printSoftwareParams();
//        self.checkEntryValidation();
    },
    getParamView: function (param) {
        if (param.type == "String") {
            return new InputTextView({param: param});
        }
        if (param.type == "Number") {
            return new InputNumberView({param: param});
        }
        if (param.type == "Date") {
            return new InputDateView({param: param});
        }
        if (param.type == "Boolean") {
            return new InputBooleanView({param: param});
        }
        if (param.type == "List") {
            return new InputListView({param: param});
        }
        if (param.type == "ListDomain") {
            return new InputListDomainView({param: param, multiple: true});
        }
        if (param.type == "Domain") {
            return new InputListDomainView({param: param, multiple: false});
        }


        else {
            return new InputTextView({param: param});
        }
    },
    printSoftwareParams: function () {
        var self = this;
        if (self.software == undefined) {
            return;
        }

        $('#launchJobParamsTable').find('tbody').empty();

        var tbody = $('#launchJobParamsTable').find("tbody");

        _.each(self.paramsViews, function (paramView) {
            paramView.addRow(tbody);
            paramView.checkEntryValidation();
        });

    },


    createJobFromParam: function () {
        var self = this;
        //retrieve an array of param

        var params = self.retrieveParams();
        //create job model

        var job = self.createJobModel(params);
        //create a job, in post data, add param array
//        console.log("job.set()...");
//        job.set('jobParameters',params);
//        //send job

        self.saveJobModel(job);

        //adapt grails to support jobparams inside job (put private and public key)

        //close windows (ok in dialog), refresh daashboardalog view
    },
    createJobModel: function (params) {
        var self = this;
        var job = new JobModel({
            software: self.software.id,
            project: self.project.id,
            params: params
        });
        return job;
    },
    retrieveParams: function () {
        var self = this;
        var jobParams = [];

        _.each(self.paramsViews, function (paramView) {
            var softwareParam = paramView.param;
            var value = paramView.getStringValue();
            var param = {value: '"' + value + '"', softwareParameter: softwareParam.id};
            jobParams.push(param);
        });
        return jobParams;
    },
    saveJobModel: function (job) {
        var self = this;
        job.save({}, {
            success: function (model, response) {
                window.app.view.message("Add Job", response.message, "success");

                self.parent.changeJobSelection(model.get('job').id);
            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Add Job", "error:" + json.errors, "error");
            }
        });
    }
});


var InputTextView = Backbone.View.extend({
    param: null,
    parent: null,
    trElem: null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
    },
    addRow: function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td>' + self.param.name + '</td><td>' + self.getHtmlElem() + '</td><td ><span class="label label-important hidden"></span></td></tr>');
        self.trElem = tbody.find('tr#' + self.param.id);
        self.trElem.find('input').keyup(function () {
            self.checkEntryValidation();
        });
    },
    getHtmlElem: function () {
        var self = this;
        var classRequier = "";
        if (self.param.required) {
            classRequier = 'border-style: solid; border-width: 2px;';
        }
        else {
            classRequier = 'border-style: solid;border-width: 2px;';
        }
        return '<div class="control-group success"><div class="controls"><input type="text" class="span3" value="' + self.getDefaultValue() + '" style="' + classRequier + '"></div></div>';
    },
    getDefaultValue: function () {
        return window.app.replaceVariable(this.param.defaultParamValue)
    },
    checkEntryValidation: function () {
        var self = this;

        if (self.param.required && self.getValue().trim() == "") {
            self.changeStyle(self.trElem, false, "Field required");
        }
        else {
            self.changeStyle(self.trElem, true, "");
        }
    },
    getValue: function () {
        return this.trElem.find("input").val();
    },
    getStringValue: function () {
        return this.getValue();
    },
    changeStyle: function (elem, success, message) {
        InputView.changeStyle(elem, success, message);
    }
});

var InputNumberView = Backbone.View.extend({
    param: null,
    parent: null,
    trElem: null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
    },
    addRow: function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td>' + self.param.name + '</td><td>' + self.getHtmlElem() + '</td><td><span class="label label-important hidden"></span></td></tr>');
        self.trElem = tbody.find('tr#' + self.param.id);
        self.trElem.find('input').keyup(function () {
            self.checkEntryValidation();
        });
    },
    getHtmlElem: function () {
        var self = this;
        var classRequier = "";
        if (self.param.required) {
            classRequier = 'border-style: solid; border-width: 2px;';
        }
        else {
            classRequier = 'border-style: solid;border-width: 2px;';
        }
        return '<div class="control-group success"><div class="controls"><input type="text" class="span3" value="' + self.getDefaultValue() + '" ' + classRequier + '"></div></div>';
    },
    getDefaultValue: function () {
        return window.app.replaceVariable(this.param.defaultParamValue)
    },
    checkEntryValidation: function () {
        var self = this;

        if (self.param.required && self.getValue().trim() == "") {
            self.changeStyle(self.trElem, false, "Field required");
        }
        else if (self.getValue().trim() != "" && isNaN(self.getValue())) {
            self.changeStyle(self.trElem, false, "Not valid number");
        }
        else {
            self.changeStyle(self.trElem, true, "");
        }
    },
    getValue: function () {
        return this.trElem.find("input").val();
    },
    getStringValue: function () {
        return this.getValue();
    },
    changeStyle: function (elem, success, message) {
        InputView.changeStyle(elem, success, message);
    }
});


var InputDateView = Backbone.View.extend({
    param: null,
    parent: null,
    trElem: null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
    },
    addRow: function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td>' + self.param.name + '</td><td>' + self.getHtmlElem() + '</td><td><span class="label label-important hidden"></span></td></tr>');
        self.trElem = tbody.find('tr#' + self.param.id);

        var defaultValue = self.getDefaultValue();

        var dateDefault = null;
        if (defaultValue != null) {
            dateDefault = new Date(Number(defaultValue));
        }

        self.trElem.find("input").datepicker({
            dateFormat: "yy-mm-dd",
            onSelect: function (dateText, inst) {
                self.checkEntryValidation();
            }
        });
        if (dateDefault != null) {
            self.trElem.find("input").datepicker("setDate", dateDefault);
        }

        self.trElem.find('input').keyup(function () {
            self.checkEntryValidation();
        });
    },
    getHtmlElem: function () {
        var self = this;
        var classRequier = "";
        if (self.param.required) {
            classRequier = 'border-style: solid; border-width: 2px;';
        }
        else {
            classRequier = 'border-style: dotted;border-width: 2px;';
        }
        return '<div class="control-group success"><div class="controls"><input type="text" class="span3" value="" style="' + classRequier + '"></div></div>';
    },
    getDefaultValue: function () {
        return window.app.replaceVariable(this.param.defaultParamValue)
    },
    checkEntryValidation: function () {
        var self = this;
        var date = self.trElem.find("input").datepicker("getDate");

        if (self.param.required && date == null) {
            self.changeStyle(self.trElem, false, "Field required");
        }
        else {
            self.changeStyle(self.trElem, true, "");
        }
    },
    getValue: function () {
        var self = this;
        var date = self.trElem.find("input").datepicker("getDate");
        if (date == null) {
            return null;
        }
        else {
            return date.getTime();
        }
    },
    getStringValue: function () {
        return this.getValue();
    },
    changeStyle: function (elem, success, message) {
        InputView.changeStyle(elem, success, message);
    }
});

var InputBooleanView = Backbone.View.extend({
    param: null,
    parent: null,
    trElem: null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
    },
    addRow: function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td>' + self.param.name + '</td><td>' + self.getHtmlElem() + '</td><td><span class="label label-important hidden"></span></td></tr>');
        self.trElem = tbody.find('tr#' + self.param.id);
        self.trElem.find('input').keyup(function () {
            self.checkEntryValidation();
        });
    },
    getHtmlElem: function () {
        return '<div><input type="checkbox" class="span3" ' + this.getDefaultValue() + ' /></div>';
    },
    getDefaultValue: function () {
        if (this.param.type == "Boolean" && this.param.defaultParamValue != undefined && this.param.defaultParamValue.toLowerCase() == "true") {
            return 'checked="checked"';
        }
        return "";
    },
    checkEntryValidation: function () {
        var self = this;

        self.changeStyle(self.trElem, true, "");
    },
    getValue: function () {
        return this.trElem.find("input").is(":checked");
    },
    getStringValue: function () {
        return this.getValue() + '';
    },
    changeStyle: function (elem, success, message) {
        InputView.changeStyle(elem, success, message);
    }
});

var InputListView = Backbone.View.extend({
    param: null,
    parent: null,
    trElem: null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
    },
    addRow: function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td>' + self.param.name + '</td><td>' + self.getHtmlElem() + '</td><td ><span class="label label-important hidden"></span></td></tr>');
        self.trElem = tbody.find('tr#' + self.param.id);
        self.trElem.find('.icon-plus-sign').click(function () {

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

            $(this).parent().find("select").find('[value="' + value + '"]').remove();
            self.checkEntryValidation();
        });
    },
    getHtmlElem: function () {
        var self = this;
        var classRequier = ""
        if (self.param.required) {
            classRequier = 'border-style: solid; border-width: 2px;'
        }
        else {
            classRequier = 'border-style: dotted;border-width: 2px;'
        }
        var defaultValues = self.getDefaultValue();
        var valueStr = '<div class="control-group success"><div class="controls"><input type="text" class="span3" value="' + "" + '" style="' + classRequier + '"><i class="icon-plus-sign"></i><select>';
        _.each(defaultValues, function (value) {
            valueStr = valueStr + '<option value="' + value + '">' + value + '</option>';
        });
        valueStr = valueStr + '</select><i class="icon-minus-sign"></i></div></div>';
        return valueStr;
    },
    getDefaultValue: function () {
        var self = this;
        if (!self.param.defaultParamValue) return [];

        var split = self.param.defaultParamValue.split(",");
        var values = [];
        _.each(split, function (s) {
            values.push(window.app.replaceVariable(s));
        });
        return values;
    },
    checkEntryValidation: function () {
        var self = this;
        if (self.trElem.find("select").children().length == 0) {
            self.changeStyle(self.trElem, false, "Field required");
        }
        else {
            self.changeStyle(self.trElem, true, "");
        }
    },
    getValue: function () {
        var self = this;
        var valueArray = [];
        self.trElem.find("select").find("option").each(function () {
            valueArray.push($(this).attr("value"));
        });
        return valueArray.join(',');
    },
    getStringValue: function () {
        return this.getValue();
    },
    changeStyle: function (elem, success, message) {
        InputView.changeStyle(elem, success, message);
    }
});

var InputListDomainView = Backbone.View.extend({
    param: null,
    parent: null,
    trElem: null,
    multiple: true,
    collection: null,
    printAttribut: null,
    elemSuggest : null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
        this.multiple = options.multiple;
        this.printAttribut = this.param.uriPrintAttribut;
    },
    addRow: function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td>' + self.param.name + '</td><td id="' + self.param.id + '"></td><td><span class="errorMessage label label-important hidden"></span></td></tr>');
        self.trElem = tbody.find('tr#' + self.param.id);

        self.collection = new SoftwareParameterModelCollection({uri: window.app.replaceVariable(self.param.uri), sortAttribut: self.param.uriSortAttribut});

        //Check if collection data are still loaded in "currentCollection" (cache objet)
        if (window.app.getFromCache(window.app.replaceVariable(self.param.uri)) == undefined) {
            if (self.collection == undefined || (self.collection.length > 0 && self.collection.at(0).id == undefined)) {
                self.trElem.find("td#" + self.param.id).append('<div class="alert alert-info" style="margin-left : 10px;margin-right: 10px;"><i class="icon-refresh" /> Loading...</div>');
                if (self.param.required) {
                    self.changeStyle(self.trElem, false, "Field required");
                }
                self.collection.fetch({
                    success: function (collection, response) {
                        self.collection = collection;

                        window.app.addToCache(window.app.replaceVariable(self.param.uri), collection);
                        self.collection.comparator = function (item) {
                            return item.get(self.param.uriSortAttribut);
                        };
                        self.collection.sort();
                        self.addHtmlElem();
                    }
                });
            } else {
                self.addHtmlElem();
            }
        } else {
            self.collection = window.app.getFromCache(window.app.replaceVariable(self.param.uri));
            self.collection.comparator = function (item) {
                return item.get(self.param.uriSortAttribut);
            };
            self.collection.sort();
            self.addHtmlElem();
        }
    },
    addHtmlElem : function() {
        var self = this;
        var cell = self.trElem.find("td#" + self.param.id);
        cell.empty();
        cell.append(self.getHtmlElem());

        var magicSuggestData = self.collection.toJSON();
        var magicSuggestValue = undefined;

        if (magicSuggestData.length == 0) {
            magicSuggestValue = magicSuggestData[0];
        }

        self.elemSuggest = cell.find(".suggest").magicSuggest({
            displayField : self.printAttribut,
            data: magicSuggestData,
            selectionPosition: 'inner',
            selectionStacked: false,
            maxSelection: (self.multiple ? null : 1),
            value : magicSuggestValue,
            width : 550,
            renderer: function(v){
                if (v.thumb) { //image model
                    return _.template('<div><div style="float:left; width : 128px;"><img src="<%= thumb %>" style="max-width : 64px; max-height : 64px;" /></div><div style="padding-left: 20px;"><%= name %></div></div><div style="clear:both;"></div>', { thumb : v.thumb, name : v[self.printAttribut]});
                } else {
                    return _.template('<%= name %>', { name : v[self.printAttribut] });
                }
            }
        });

        $(self.elemSuggest).on("selectionchange", function (e) {
            self.checkEntryValidation();
        });

    },
    getDefaultValue: function () {
        var self = this;
        if (!self.param.defaultParamValue) return [];
        var split = self.param.defaultParamValue.split(",");
        var values = [];
        _.each(split, function (s) {
            values.push(window.app.replaceVariable(s));
        });

        return values;
    },
    getHtmlElem: function () {
        return "<div class='suggest' />";
    },
    checkEntryValidationWithAllValue: function (value, checked) {
        var self = this;

        var values = self.getValue();
        var length = values.length;
        if (checked) {
            length++;
        }
        else {
            length--;
        }

        if (self.param.required && length == 0) {
            self.changeStyle(self.trElem, false, "Field required");
        }
        else {
            self.changeStyle(self.trElem, true, "");
        }

    },
    checkEntryValidation: function () {
        var self = this;

        var values = self.getValue();

        if (self.param.required && values.length == 0) {
            self.changeStyle(self.trElem, false, "Field required");
        }
        else {
            self.changeStyle(self.trElem, true, "");
        }
    },
    getValue: function () {
        var self = this;

        if (!self.elemSuggest) return []; //collection may not be yet loaded

        var values = self.elemSuggest.getValue();
        if (values == undefined) {
            return [];
        }
        else {
            return values;
        }
    },
    getStringValue: function () {
        var self = this;
        var values = self.getValue();
        if (values == undefined) {
            return "";
        }
        else {
            return values.join(",");
        }
    },
    changeStyle: function (elem, success, message) {


        var labelElem = elem.find('.errorMessage');
        if (success) {
            labelElem.addClass("hidden");
            labelElem.text("");
        } else {
            labelElem.removeClass("hidden");
            labelElem.text("");
            labelElem.text(message);
        }

        var className = "";
        //color input
        if (success) {
            elem.removeClass("error")
            elem.addClass("success")
        }
        else {
            elem.removeClass("success")
            elem.addClass("error")

        }



    }
});

var InputView = {
    changeStyle: function (elem, success, message) {
        var className = "";
        //color input
        if (success) {
            className = "success";
        }
        else {
            className = "error";
        }

        elem.removeClass("error")
        elem.removeClass("success")
        elem.addClass(className)

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
    }
};
