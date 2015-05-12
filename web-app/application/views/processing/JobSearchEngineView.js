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

var JobSearchEngineView = Backbone.View.extend({
    width: null,
    software: null,
    project: null,
    parent: null,
    listing: null,
    allJobs: null,
    paramViews: null,
    initialize: function (options) {
        var self = this;
        this.software = options.software;
        this.project = options.project;
        this.parent = options.parent;
        this.listing = options.listing;
        this.idJob = options.idJob;
        this.allJobs = options.allJobs;
        this.paramViews = [];
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/processing/JobSearchEngine.tpl.html"
        ],
            function (JobSearchEngineTpl) {
                self.loadResult(JobSearchEngineTpl);
            });
        return this;
    },
    loadResult: function (JobSearchEngineTpl) {
        console.log("JobSearchEngineView.loadResult");
        var self = this;
        var content = _.template(JobSearchEngineTpl, {});
        $(self.el).empty();
        $(self.el).append(content);

        console.log("$('#myTab')=" + $('#myTab').length);
        console.log("$('#myTab').find('a')=" + $('#myTab').find('a').length);

        $('#myTab').find('a').click(function (e) {
            console.log("click");
            e.preventDefault();
            $(this).tab('show');
            history.replaceState(null, '', '#tabs-algos-' + self.project.id + "-" + self.software.id + "-" + self.idJob);
            return false;
        });

        self.printBasicSearchPanel();
        self.printAdvancedSearchPanel();

    },
    printBasicSearchPanel: function () {
        var self = this;

        var launchQuickSearch = function () {
            console.log(self);
            console.log("launchQuickSearch:" + $("#searchJobFilterAll").val());
            self.listing.table.fnFilter(
                $("#searchJobFilterAll").val(),
                null,
                false,
                true
            );
        };
        $("#searchJobFilterAll").keyup(launchQuickSearch);
        $("#searchJobFilterAll").click(launchQuickSearch);
        $("#searchJobFilterDateAfter").datepicker({
            numberOfMonths: 3,
            showButtonPanel: true
        });

        $("#searchJobFilterDateBefore").datepicker({
            numberOfMonths: 3,
            showButtonPanel: true
        });

        $("#searchJobFilterStatusNotLaunch").append('<span class="label btn-inverse">Not Launch!</span>');
        $("#searchJobFilterStatusInQueue").append('<span class="label btn-info">In queue!</span>');
        $("#searchJobFilterStatusRunning").append('<span class="label btn-primary">Running!</span>');
        $("#searchJobFilterStatusSuccess").append('<span class="label btn-success">Success!</span>');
        $("#searchJobFilterStatusFailed").append('<span class="label btn-danger">Failed!</span>');
        $("#searchJobFilterStatusIndetereminate").append('<span class="label btn-inverse">Indetereminate!</span>');
        $("#searchJobFilterStatusWait").append('<span class="label btn-warning">Wait!</span>');


        $("#searchJobFilterButton").click(function () {
            self.launchSearch();
        });

        $("#resetJobFilterButton").click(function () {
            self.launchReset();
        });
    },
    printAdvancedSearchPanel: function () {
        var self = this;
        _.each(self.software.get('parameters'), function (param) {
            var paramView = self.getParamView(param);
            self.paramViews.push(paramView);
            console.log("### addRow " + param.type);
            paramView.addRow($("#searchJobFilterParameterTable"));
        });
    },
    getParamView: function (param) {
        if (param.type == "String") {
            return new InputTextViewSearch({param: param});
        }
        if (param.type == "Number") {
            return new InputNumberViewSearch({param: param});
        }
        if (param.type == "Boolean") {
            return new InputBooleanViewSearch({param: param});
        }
        if (param.type == "List") {
            return new InputListViewSearch({param: param});
        }
        if (param.type == "Date") {
            return new InputDateViewSearch({param: param});
        }
        if (param.type == "ListDomain") {
            return new InputListDomainViewSearch({param: param, multiple: true});
        }
        if (param.type == "Domain") {
            return new InputListDomainViewSearch({param: param, multiple: false});
        }
        else {
            return new InputTextViewSearch({param: param});
        }
    },
    launchSearch: function () {
        var self = this;
        console.log("search job");

        //clean quicksearch input
        $("#searchJobFilterAll").val("");

        var filterJobs = self.allJobs.models;

        filterJobs = self.searchById(filterJobs, $("#searchJobFilterId").val());
        filterJobs = self.searchByNumber(filterJobs, $("#searchJobFilterNumber").val());
        filterJobs = self.searchByCreationDateAfter(filterJobs, $("#searchJobFilterDateAfter").datepicker("getDate"));
        filterJobs = self.searchByCreationDateBefore(filterJobs, $("#searchJobFilterDateBefore").datepicker("getDate"));
        filterJobs = self.searchByStatus(filterJobs, 0, $("#searchJobFilterStatusNotLaunchCheck").is(':checked'));
        filterJobs = self.searchByStatus(filterJobs, 1, $("#searchJobFilterStatusInQueueCheck").is(':checked'));
        filterJobs = self.searchByStatus(filterJobs, 2, $("#searchJobFilterStatusRunningCheck").is(':checked'));
        filterJobs = self.searchByStatus(filterJobs, 3, $("#searchJobFilterStatusSuccessCheck").is(':checked'));
        filterJobs = self.searchByStatus(filterJobs, 4, $("#searchJobFilterStatusFailedCheck").is(':checked'));
        filterJobs = self.searchByStatus(filterJobs, 5, $("#searchJobFilterStatusIndetereminateCheck").is(':checked'));
        filterJobs = self.searchByStatus(filterJobs, 6, $("#searchJobFilterStatusWaitCheck").is(':checked'));

        _.each(self.paramViews, function (paramView) {
            var paramName = paramView.param.name;
            var paramValue = paramView.getStringValue();
            filterJobs = self.searchByParam(filterJobs, paramName, paramValue);

        });
        self.listing.refresh(new JobCollection(filterJobs));
    },
    searchById: function (jobs, num) {
        console.log("Before searchById:" + jobs.length + " value=" + num);
        if (num == undefined || num == null || num.trim() == "") {
            return jobs;
        }
        var filterJobs = [];

        _.each(jobs, function (job) {
            if (job.get('id') == num) {
                filterJobs.push(job);
            }
        });
        console.log("After searchById:" + filterJobs.length);
        return filterJobs;
    },
    searchByNumber: function (jobs, num) {
        console.log("Before searchByNumber:" + jobs.length + " value=" + num);
        if (num == undefined || num == null || num.trim() == "") {
            return jobs;
        }
        var filterJobs = [];
        _.each(jobs, function (job) {
            if (job.get('number') == num) {
                filterJobs.push(job);
            }
        });
        console.log("After searchByNumber:" + filterJobs.length);
        return filterJobs;
    },
    searchByCreationDateAfter: function (jobs, datetime) {
        console.log("Before searchByCreationDateAfter:" + jobs.length + " datetime=" + datetime);
        if (datetime == undefined || datetime == null) {
            return jobs;
        }
        var filterJobs = [];
        _.each(jobs, function (job) {
            if (job.get('created') >= datetime.getTime()) {
                filterJobs.push(job);
            }
        });
        console.log("After searchByCreationDateAfter:" + filterJobs.length);
        return filterJobs;
    },
    searchByCreationDateBefore: function (jobs, datetime) {
        console.log("Before searchByCreationDateBefore:" + jobs.length + " datetime=" + datetime);
        if (datetime == undefined || datetime == null) {
            return jobs;
        }
        var filterJobs = [];
        _.each(jobs, function (job) {
            if (job.get('created') < datetime.getTime()) {
                filterJobs.push(job);
            }
        });
        console.log("After searchByCreationDateBefore:" + filterJobs.length);
        return filterJobs;
    },
    searchByStatus: function (jobs, status, check) {
        console.log("Before searchByStatus:" + jobs.length + " status=" + status + " check=" + check);
        if (check) {
            return jobs;
        } //if check, include jobs from this status so no filter
        var filterJobs = [];
        _.each(jobs, function (job) {
            if (job.get('status') != status) {
                filterJobs.push(job);
            }
        });
        console.log("After searchByStatus:" + filterJobs.length);
        return filterJobs;

    },
    searchByParam: function (jobs, paramName, paramValue) {
        var self = this;
        console.log("Before searchByParam:" + jobs.length + " paramName=" + paramName + " paramValue=" + paramValue);
        if (paramValue == undefined || paramValue == null || paramValue.trim() == "") {
            return jobs;
        }
        var filterJobs = [];

        _.each(jobs, function (job) {
            var jobParam = self.getJobParam(job, paramName);
            if (jobParam != undefined) {
                console.log("jobParam.value=" + jobParam.value);
            }
            if (jobParam != undefined && jobParam != null && self.isJobParamCorrect(jobParam, paramValue)) {
                filterJobs.push(job);
            }
        });
        console.log("After searchByParam:" + filterJobs.length);
        return filterJobs;
    },
    isJobParamCorrect: function (jobParam, paramValue) {
        if (jobParam.type == "Date") {
            var date = paramValue.split("-");
            var isCorrectStart = true;
            var isCorrectStop = true;
            console.log("isJobParamCorrect:" + window.app.convertLongToDate(jobParam.value) + "=>" + window.app.convertLongToDate(date[0]) + "-" + window.app.convertLongToDate(date[1]));

            if (date[0] != "null") {
                isCorrectStart = (jobParam.value >= date[0])
            }
            if (date[1] != "null") {
                isCorrectStop = (jobParam.value <= date[1])
            }
            return (isCorrectStart && isCorrectStop)
        } else {
            return jobParam.value.toLowerCase() == paramValue.toLowerCase();
        }
    },
    getJobParam: function (job, name) {
        var goodParam = undefined;
        _.each(job.get('jobParameters'), function (param) {
            if (param.name == name) {
                goodParam = param;
            }
        });
        return goodParam;
    },
    launchReset: function () {
        var self = this;
        console.log("search job");
        var filterJobs = self.allJobs.models;
        $("#searchJobFilterId").val("");
        $("#searchJobFilterNumber").val("");
        $("#searchJobFilterDateAfter").datepicker("setDate", null);
        $("#searchJobFilterDateBefore").datepicker("setDate", null);
        $("#searchJobFilterStatusNotLaunchCheck").attr('checked', true);
        $("#searchJobFilterStatusInQueueCheck").attr('checked', true);
        $("#searchJobFilterStatusRunningCheck").attr('checked', true);
        $("#searchJobFilterStatusSuccessCheck").attr('checked', true);
        $("#searchJobFilterStatusFailedCheck").attr('checked', true);
        $("#searchJobFilterStatusIndetereminateCheck").attr('checked', true);
        $("#searchJobFilterStatusWaitCheck").attr('checked', true);

        _.each(self.paramViews, function (paramView) {
            paramView.reset();
        });
        self.listing.refresh(self.allJobs);
    }
});


var InputTextViewSearch = Backbone.View.extend({
    param: null,
    parent: null,
    trElem: null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
    },
    addRow: function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td style="text-align:left;"><b>' + self.param.name + '</b><br>' + self.getHtmlElem() + '</td></tr>');
        self.trElem = tbody.find('tr#' + self.param.id);
    },
    getHtmlElem: function () {
        var self = this;
        return '<input type="text" value="' + '" style="text-align:center;" class="input-medium">';
    },
    getValue: function () {
        return this.trElem.find("input").val();
    },
    getStringValue: function () {
        return this.getValue();
    },
    reset: function () {
        this.trElem.find("input").val("");
    }
});

var InputNumberViewSearch = Backbone.View.extend({
    param: null,
    parent: null,
    trElem: null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
    },
    addRow: function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td style="text-align:left;"><b>' + self.param.name + '</b><br>' + self.getHtmlElem() + '</td></tr>');
        self.trElem = tbody.find('tr#' + self.param.id);
    },
    getHtmlElem: function () {
        var self = this;
        return '<input type="text" value="' + '" style="text-align:center;" class="input-medium">';
    },
    getValue: function () {
        return this.trElem.find("input").val();
    },
    getStringValue: function () {
        return this.getValue();
    },
    reset: function () {
        this.trElem.find("input").val("");
    }
});

var InputBooleanViewSearch = Backbone.View.extend({
    param: null,
    parent: null,
    trElem: null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
    },
    addRow: function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td style="text-align:left;"><b>' + self.param.name + '</b><br>' + self.getHtmlElem() + '</td></tr>');
        self.trElem = tbody.find('tr#' + self.param.id);
    },
    getHtmlElem: function () {
        return '<select class="input-medium"><option value="">All</option><option value="true">Yes</option><option value="false">No</option></select>';
    },
    getValue: function () {
        return this.trElem.find("select").val();
    },
    getStringValue: function () {
        return this.getValue() + '';
    },
    reset: function () {
        this.trElem.find('option[value=""]').attr('selected', 'selected');
    }
});

var InputListViewSearch = Backbone.View.extend({
    param: null,
    parent: null,
    trElem: null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
    },
    addRow: function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td style="text-align:left;"><b>' + self.param.name + '</b><br>' + self.getHtmlElem() + '</td></tr>');
        self.trElem = tbody.find('tr#' + self.param.id);
        self.trElem.find('.icon-plus-sign').click(function () {
            console.log("Add entry");
            var value = $(this).parent().find("input").val();
            if (value.trim() != "") {
                $(this).parent().find("select").append('<option value="' + value + '">' + value + '</option>');
                $(this).parent().find("input").val("");
                $(this).parent().find("select").val(value);
            }
        });

        self.trElem.find('.icon-minus-sign').click(function () {

            var value = $(this).parent().find("select").val();
            console.log("delete entry:" + value);
            console.log("delete entry:" + $(this).parent().find("select").find('[value="' + value + '"]').length);
            $(this).parent().find("select").find('[value="' + value + '"]').remove();
        });
    },
    getHtmlElem: function () {
        var self = this;
        var defaultValues = self.getDefaultValue();
        var valueStr = '<div class="controls"><input type="text" value="" style="text-align:center;"><i class="icon-plus-sign"></i><select><option value="">All</option>';
        _.each(defaultValues, function (value) {
            valueStr = valueStr + '<option value="' + value + '">' + value + '</option>';
        });
        valueStr = valueStr + '</select><i class="icon-minus-sign"></i></div></div>';
        return valueStr;
    },
    getDefaultValue: function () {
        return [];
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
    reset: function () {
        this.trElem.find('option[value=""]').attr('selected', 'selected');
    }
});


var InputDateViewSearch = Backbone.View.extend({
    param: null,
    parent: null,
    trElem: null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
    },
    addRow: function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td style="text-align:left;"><b>' + self.param.name + '</b><br>' + self.getHtmlElem() + '</td></tr>');
        self.trElem = tbody.find('tr#' + self.param.id);

        var dates = self.trElem.find("#from" + self.param.id + ", #to" + self.param.id).datepicker({
            defaultDate: "+1w",
            changeMonth: true,
            numberOfMonths: 3,
            onSelect: function (selectedDate) {
                var option = this.id == "from" + self.param.id ? "minDate" : "maxDate",
                    instance = $(this).data("datepicker"),
                    date = $.datepicker.parseDate(instance.settings.dateFormat || $.datepicker._defaults.dateFormat, selectedDate, instance.settings);
                dates.not(this).datepicker("option", option, date);
            }
        });

    },
    getHtmlElem: function () {
        var self = this;
        return 'From <input type="text" id="from' + self.param.id + '" value="' + '" style="text-align:center;" class="input-medium"> to <input type="text" id="to' + self.param.id + '" name="to"/>';
    },
    getValue: function () {
        var self = this;
        var dateStart = this.trElem.find("#from" + self.param.id).datepicker("getDate");
        var dateEnd = this.trElem.find("#to" + self.param.id).datepicker("getDate");

        var dateStartString = dateStart == null ? "null" : dateStart.getTime();
        var dateEndString = dateEnd == null ? "null" : dateEnd.getTime();
        return dateStartString + "-" + dateEndString;
    },
    getStringValue: function () {
        return this.getValue();
    },
    reset: function () {
        var self = this;
        this.trElem.find("#from" + self.param.id).datepicker("setDate", null);
        this.trElem.find("#to" + self.param.id).datepicker("setDate", null);
    }
});

var InputListDomainViewSearch = Backbone.View.extend({
    param: null,
    parent: null,
    trElem: null,
    multiple: true,
    collection: null,
    printAttribut: null,
    initialize: function (options) {
        this.param = options.param;
        this.parent = options.parent;
        this.multiple = options.multiple;
        this.printAttribut = this.param.uriPrintAttribut;
    },
    addRow: function (tbody) {
        var self = this;
        tbody.append('<tr id="' + self.param.id + '"><td id="' + self.param.id + '" style="text-align:left;"><b>' + self.param.name + '</b><br></tr>');
        self.trElem = tbody.find('tr#' + self.param.id);
        console.log("### " + window.app.replaceVariable(self.param.uri) + " ###" + self.param.uriSortAttribut);
        self.collection = new SoftwareParameterModelCollection({uri: window.app.replaceVariable(self.param.uri), sortAttribut: self.param.uriSortAttribut});
        if (self.collection == undefined || (self.collection.length > 0 && self.collection.at(0).id == undefined)) {
            self.collection.fetch({
                success: function (collection, response) {
                    console.log(collection);
                    self.collection = collection;
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
    },
    addHtmlElem: function () {
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
                return numChecked + " selected";
            }
        } else {
            fnSelectionView = function (numChecked, numTotal, checkedItem) {
                if (numChecked == 0) {
                    return "0 selected";
                }
                var selectTitle = [];
                $(checkedItem).each(function () {
                    selectTitle.push($(this).attr("title"));
                });
                var selectText = selectTitle.join(", ");
                selectText = selectText.substring(0, Math.min(10, selectText.length));
                return selectText;
            };
        }

        self.trElem.find(".domainList").multiselect({'autoOpen': false, minWidth: 300, 'height': 200, 'multiple': self.multiple, 'selectedText': fnSelectionView}).multiselectfilter();
        self.trElem.find("button").width("150");
        //put header menu option on the same line
        self.trElem.find(".ui-multiselect-menu").find("span").css("display", "inline");
        self.trElem.find(".ui-multiselect-menu").find("input").css("display", "inline");
        self.trElem.find(".ui-multiselect-menu").find(".ui-multiselect-header").find("li").css("display", "inline");
        //put check all on left and deselect all on right
        self.trElem.find(".ui-multiselect-menu").find(".ui-multiselect-header").find("li").eq(0).css("float", "left");
        self.trElem.find(".ui-multiselect-menu").find(".ui-multiselect-header").find("li").eq(1).css("float", "right");
        self.trElem.find(".ui-multiselect-menu").find("li").css("display", "block");
        //print scroll only vertical
        self.trElem.find("ul.ui-multiselect-checkboxes").css('overflow-y', 'scroll');
        self.trElem.find("ul.ui-multiselect-checkboxes").css('overflow-x', 'hidden');
        self.trElem.find(".domainList").multiselect("close");

    },
    getDefaultValue: function () {
        var self = this;
        return [];
    },
    getHtmlElem: function () {
        var self = this;
        var classRequier = "";
        //mark default value as selected:
        var defaultValues = self.getDefaultValue();
        if (self.param.required) {
            classRequier = 'border-style: solid; border-width: 2px;'
        }
        else {
            classRequier = 'border-style: dotted;border-width: 2px;'
        }
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
    getValue: function () {
        var self = this;
        var values = self.trElem.find(".domainList").val();
        if (values == undefined) {
            return [];
        }
        else {
            return values;
        }
    },
    getStringValue: function () {
        var self = this;
        var values = self.trElem.find(".domainList").val();
        if (values == undefined) {
            return "";
        }
        else {
            return values.join(",");
        }
    },
    reset: function () {
        this.trElem.find(".domainList").multiselect("uncheckAll");
    }
});
