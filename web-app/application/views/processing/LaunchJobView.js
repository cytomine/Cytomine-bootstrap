/*
 * Copyright (c) 2009-2015. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var LaunchJobView = Backbone.View.extend({
    width: null,
    software: null,
    project: null,
    parent: null,
    params: [],
    paramsViews: [],
    jobTemplate: null,
    templates: null,
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
        var content = _.template(JobLaunchTpl, {softwareName : self.software.get('name'), projectName : self.project.get('name')});

        $(self.el).empty();
        $(self.el).append(content);

        self.loadTemplate();

    },
    loadTemplate : function() {
        var self = this;
        console.log($("#preFilledjobTemplate"));
        new JobTemplateCollection({project: this.project.get("id"), software: this.software.get("id")}).fetch({
            success: function (collection, response) {
                console.log(collection);
                if(collection.size()==0) {
                    $("#preFilledjobTemplate").replaceWith("(Not available: no Job template)");
                    $("#optionPrefilled").prop('disabled', 'disabled');
                } else {
                    //get all project id/name
                    $("#preFilledjobTemplate").empty();
                    collection.each(function(jobTemplate) {
                        console.log("apprend "+jobTemplate.get('name'));
                        $("#preFilledjobTemplate").append('<option value="'+jobTemplate.get("id")+'">'+jobTemplate.get('name')+'</option>');
                    });
                }
                self.templates = collection;
                self.loadChoice();
            }
        });
    },
    loadChoice : function() {
        var self = this;
        var refreshSelection = function() {
            console.log("refreshSelection");
            var selectedOption = $('input[name=customOrTemplate]:checked').val();
            console.log("selectedOption="+selectedOption);
            if(selectedOption=="optionCustomForm") {
                $("#preFilledjobTemplate").prop('disabled', 'disabled');
                self.jobTemplate = null;
            } else {
                $("#preFilledjobTemplate").prop('disabled',false);
                var id = $('#preFilledjobTemplate').val();
                self.jobTemplate = self.templates.get(id);
            }
            self.loadParams();
        }
        refreshSelection();

        $("#optionCustomForm").change(function () {
            console.log("change");
            refreshSelection();
        });
        $("#optionPrefilled").change(function () {
            console.log("change");
            refreshSelection();
        });

        $("#preFilledjobTemplate").change(function () {
            console.log("change");
            refreshSelection();
        });
    },

    loadParams : function() {
        var self = this;
        self.params = [];
        self.paramsViews = [];

        //if(self.jobTemplate==null) {
            _.each(self.software.get('parameters'), function (param) {
                self.params.push(param);
                self.paramsViews.push(self.getParamView(param));
            });
        //}

        self.printSoftwareParams();

    },

    retrieveDefaultValue : function(param) {
        var self = this;

        var defaultValue = param.defaultParamValue;

        if(self.jobTemplate!=null) {

            var paramsTemplate = self.jobTemplate.get("jobParameters");
            console.log("Looking for "+param.name + " in template");
            var paramTemplate = _.find(paramsTemplate, function(paramT){ return paramT.name==param.name; });
            console.log(paramTemplate);
            if(paramTemplate) {
                defaultValue = paramTemplate.value;
            }
        }

        if(param.type == "Boolean") {
            if (defaultValue != undefined && defaultValue.toLowerCase() == "true") {
                return 'checked="checked"';
            } else {
                return "";
            }
        } else if(param.type=="Domain" || param.type=="ListDomain") {

            if (!defaultValue) return [];
            var split = defaultValue.split(",");
            var values = [];
            _.each(split, function (s) {
                values.push(window.app.replaceVariable(s));
            });

            return values;
        } else {
            return window.app.replaceVariable(defaultValue)
        }


    },


    getParamView: function (param) {

        if (param.type == "String") {
            return new InputTextView({param: param, container:this});
        }
        if (param.type == "Number" || (param.type == "Domain" && !param.uri)) {
            return new InputNumberView({param: param, container:this});
        }
        if (param.type == "Date") {
            return new InputDateView({param: param, container:this});
        }
        if (param.type == "Boolean") {
            return new InputBooleanView({param: param, container:this});
        }
        if (param.type == "List") {
            return new InputListView({param: param, container:this});
        }
        if (param.type == "ListDomain") {
            return new InputListDomainView({param: param, container:this, multiple: true});
        }
        if (param.type == "Domain") {
            return new InputListDomainView({param: param, container:this, multiple: false});
        }
        else {
            return new InputTextView({param: param, container:this});
        }
    },
    printSoftwareParams: function () {
        var self = this;

        if (self.software == undefined) {
            return;
        }

        var launchJobParamsTable = $('#launchJobParamsTable');
        var tbody = launchJobParamsTable.find("tbody");

        tbody.empty();

        _.each(self.paramsViews, function (paramView) {
            paramView.addRow(tbody);
            paramView.checkEntryValidation();
        });

    },


    createJobFromParam: function (callback) {
        //create job model
        var job = this.createJobModel(this.retrieveParams());

        //save it
        this.saveJobModel(job, callback);

        //adapt grails to support jobparams inside job (put private and public key)

        //close windows (ok in dialog), refresh daashboardalog view
    },
    executeJob : function(idJob) {
        var job = new JobModel({ id : idJob})
        $.post(job.executeUrl())
            .done(function() {
             console.log("job launched");
            })
            .fail(function() { console.log("error"); })
            .always(function() { console.log("finished"); });
    },
    previewJob : function(idJob) {
        var job = new JobModel({ id : idJob})
        $.post(job.previewUrl())
        .done(function() {
            var interval = setInterval(function() {
                var previewJob = $("#previewJob");
                previewJob.html('<div class="progress progress-striped active"><div class="bar" style="width: 100%;"></div></div>');
                job.fetch({
                    success : function(model, response) {
                        if (model.isPreviewed()) {
                            $("#previewJob").empty();
                            $("#previewJob").html(_.template("<img src='<%= previewRoiUrl %>' style='height : 200px;'/><img src='<%= previewUrl %>' style='height : 200px;'/>", {
                                previewRoiUrl : model.previewRoiUrl(),
                                previewUrl : model.previewUrl()
                            }));
                            clearInterval(interval);
                        }
                    },
                    error : function(model, response){
                        //display error message + clear interval
                        clearInterval(interval);
                    }
                })
            }, 2000);
        })
        .fail(function() { console.log("error"); })
        .always(function() { console.log("finished"); });
    },
    createJobModel: function (params) {
        var self = this;
        return new JobModel({
            software: self.software.id,
            project: self.project.id,
            params: params
        });
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
    saveJobModel: function (job, callback) {
        var self = this;
        job.save({}, {
            success: function (model, response) {
                window.app.view.message("Add Job", response.message, "success");
                self.parent.changeJobSelection(model.get('job').id);
                if (callback) {
                    callback(model.get('job').id);
                }
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
    container:null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
        this.container = options.container;
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
        return _.template('<div class="control-group success"><div class="controls"><input type="text" class="col-md-3" value="<%= value%>" <%= required %>></div></div>', {
               value : this.getDefaultValue(),
               required: (self.param.required) ? "required" : ""
        });
    },
    getDefaultValue: function () {
        return this.container.retrieveDefaultValue(this.param);
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
    container:null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
        this.container = options.container;
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
        return _.template('<div class="control-group success"><div class="controls"><input type="text" class="col-md-3" value="<%= value%>" <%= required %>></div></div>', {
            value : this.getDefaultValue(),
            required: (this.param.required) ? "required" : ""
        });
    },
    getDefaultValue: function () {
        return this.container.retrieveDefaultValue(this.param);
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
    container:null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
        this.container = options.container;
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
        return _.template('<div class="control-group success"><div class="controls"><input type="text" class="col-md-3" value="" <%= required %>></div></div>', {
            required: (self.param.required) ? "required" : ""
        });
    },
    getDefaultValue: function () {
        return this.container.retrieveDefaultValue(this.param);
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
    container: null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
        this.container = options.container;
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
        return _.template('<div><input type="checkbox" class="col-md-3" <%= value %> ></input></div>', {
            value : this.getDefaultValue()
        });
    },
    getDefaultValue: function () {
        return this.container.retrieveDefaultValue(this.param);

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
    container:null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
        this.container = options.container;
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
        var valueStr = '<div class="control-group success"><div class="controls"><input type="text" class="col-md-3" value="' + "" + '" style="' + classRequier + '"><i class="icon-plus-sign"></i><select>';
        _.each(defaultValues, function (value) {
            valueStr = valueStr + '<option value="' + value + '">' + value + '</option>';
        });
        valueStr = valueStr + '</select><i class="icon-minus-sign"></i></div></div>';
        return valueStr;
    },
    getDefaultValue: function () {
        return this.container.retrieveDefaultValue(this.param);
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
    container:null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
        this.multiple = options.multiple;
        this.printAttribut = this.param.uriPrintAttribut;
        this.container = options.container;
    },
    addRow: function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td>' + self.param.name + '</td><td id="' + self.param.id + '"></td><td><span class="errorMessage label label-important hidden"></span></td></tr>');
        self.trElem = tbody.find('tr#' + self.param.id);

        self.collection = new SoftwareParameterModelCollection({uri: window.app.replaceVariable(self.param.uri), sortAttribut: self.param.uriSortAttribut});

        //Check if collection data are still loaded in "currentCollection" (cache objet)
        var currentCollection = window.app.getFromCache(window.app.replaceVariable(self.param.uri));
        // if the currentCollection is empty or is a job, we will not use the cache.
        var toReload = currentCollection == undefined || currentCollection.length == 0 || currentCollection.uri.indexOf("/api/job") >=0;
        if (toReload) {
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
        var magicSuggestValue = self.getDefaultValue();

        self.elemSuggest = cell.find(".suggest").magicSuggest({
            allowFreeEntries: false,
            data: magicSuggestData,
            selectionRenderer: function(v){
                var displayField;
                if (v['class'] == 'be.cytomine.processing.Job') {
                    displayField = v.softwareName+" "+ window.app.convertLongToDate(v.created);
                } else {
                    displayField = v[self.printAttribut];
                }
                return displayField;
            },
            selectionPosition: 'inner',
            selectionStacked: false,
            maxSelection: (self.multiple ? null : 1),
            value : magicSuggestValue,
            width : 750,
            renderer: function(v){
                var item
                if (v.thumb) { //image/annotation model
                    item =  _.template('<div><div style="float:left; width : 128px;"><img src="<%= thumb %>" style="max-width : 64px; max-height : 64px;" /></div><div style="padding-left: 20px;"><%= name %></div></div><div style="clear:both;"></div>', { thumb : v.thumb, name : v[self.printAttribut]});
                } else if (v['class'] == 'be.cytomine.processing.Job') {
                    item = _.template('<%= name %>', { name : v.softwareName+" "+ window.app.convertLongToDate(v.created) });
                } else {
                    item = _.template('<%= name %>', { name : v[self.printAttribut] });
                }
                return item;
            }
        });
        $("#checkAll"+self.param.id).click(function() {
            self.elemSuggest.addToSelection(magicSuggestData);
        });
        $("#uncheckAll"+self.param.id).click(function() {
            self.elemSuggest.removeFromSelection(magicSuggestData);
        });



        if (self.multiple) {
            cell.append(_.template("<a class='checkAll<%= id %>'>check all</a>, <a class='uncheckAll<%= id %>'>uncheck all</a>", { id : self.param.id}));
            $("a.checkAll" + self.param.id).on("click", function (e) {
                self.elemSuggest.setValue(_.pluck(magicSuggestData, 'id'));
            });
            $("a.uncheckAll" + self.param.id).on("click", function (e) {
                self.elemSuggest.clear();
            });
        }

        $(self.elemSuggest).on("selectionchange", function (e) {
            self.checkEntryValidation();
        });

    },
    getDefaultValue: function () {
        return this.container.retrieveDefaultValue(this.param);

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
