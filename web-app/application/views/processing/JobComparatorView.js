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

var JobComparatorView = Backbone.View.extend({
    width: null,
    software: null, //current software
    softwares: null, //softwares from project
    project: null,
    software1: null, //selected software
    software2: null,
    job1: null, //selected job
    job2: null,
    jobs1: null, //job list
    jobs2: null,
    parent: null,
    initialize: function (options) {
        this.width = options.width;
        this.software = options.software;
        this.software1 = options.software;
        this.software2 = options.software;
        this.softwares = options.softwares;
        this.project = options.project;
        this.job1 = options.job1;
        this.job2 = options.job2;
        this.jobs1 = options.jobs;
        this.jobs2 = options.jobs;
        this.parent = options.parent;

    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/processing/JobComparator.tpl.html"
        ],
            function (JobComparatorTpl) {
                self.loadResult(JobComparatorTpl);
            });
        return this;
    },
    loadResult: function (JobComparatorTpl) {
        var self = this;
        var content = _.template(JobComparatorTpl, {});
        if (self.jobs1.length < 1 || self.jobs2.length < 1) {
            return;
        }
        $(self.el).empty();
        $(self.el).append(content);


        self.printSoftwareSelection($("#comparatorSoftwareSelection"));
        self.changeSelectionValue($("#comparatorSoftwareSelection").find('.job1'), self.software1.id);
        self.changeSelectionValue($("#comparatorSoftwareSelection").find('.job2'), self.software2.id);
        self.printJobSelection($("#comparatorJobSelection"));

        self.changeSelection();

        self.refreshSelectStyle($("#comparatorJobSelection").find('.job1'));
        self.refreshSelectStyle($("#comparatorJobSelection").find('.job2'));
        self.refreshCompareJob();
    },
    changeSelection: function () {
        var self = this;

        if (self.jobs1.get(self.job1) != undefined) {
            self.changeSelectionValue($("#comparatorJobSelection").find('.job1'), self.job1.id);
        } else {
            self.changeSelectionValue($("#comparatorJobSelection").find('.job1'), self.jobs1.at(0).id);
        }

        if (self.jobs2.get(self.job2) != undefined) {
            self.changeSelectionValue($("#comparatorJobSelection").find('.job2'), self.job2.id);
        } else {
            self.changeSelectionValue($("#comparatorJobSelection").find('.job2'), self.jobs2.at(0).id);
        }
    },
    changeSelectionValue: function (elem, value) {
        elem.find('select').val(value);
    },
    retrieveSelectedJob: function (num) {
        return $("#comparatorJobSelection").find('.job' + num).find('select').val();
    },
    retrieveSelectedSoftware: function (num) {
        return $("#comparatorSoftwareSelection").find('.job' + num).find('select').val();
    },
    cleanCompareJob: function () {
        $("#comparatorJobInfo").find(".job1").empty();
        $("#comparatorJobInfo").find(".job2").empty();
        $("#comparatorJobParam").find(".job1").empty();
        $("#comparatorJobParam").find(".job2").empty();
        $("#comparatorJobResult").find(".job1").empty();
        $("#comparatorJobResult").find(".job2").empty();

    },
    reloadSelection: function () {
        var self = this;
        $("#comparatorJobSelection").find('.job1').empty();
        $("#comparatorJobSelection").find('.job2').empty();
        $("#comparatorSoftwareSelection").find('.job1').empty();
        $("#comparatorSoftwareSelection").find('.job2').empty();
        self.printSoftwareSelection($("#comparatorSoftwareSelection"));
        self.changeSelectionValue($("#comparatorSoftwareSelection").find('.job1'), self.software1.id);
        self.changeSelectionValue($("#comparatorSoftwareSelection").find('.job2'), self.software2.id);
        self.printJobSelection($("#comparatorJobSelection"));
        self.changeSelection();
        self.refreshCompareJob();
    },
    refreshCompareJob: function () {
        var self = this;
        self.cleanCompareJob();
        var idJob1 = self.retrieveSelectedJob('1');
        var idJob2 = self.retrieveSelectedJob('2');
        console.log("idJob1=" + idJob1 + " idJob2=" + idJob2);
        if (idJob1 == undefined || idJob2 == undefined) {
            return;
        }
        new JobModel({ id: idJob1}).fetch({
            success: function (job1, response) {
                new JobModel({ id: idJob2}).fetch({
                    success: function (job2, response) {
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
    printJobSelection: function (elemParent) {
        var self = this;
        self.addSelectionView(elemParent.find('.job1'), self.jobs1);
        self.addSelectionView(elemParent.find('.job2'), self.jobs2);
    },
    printSoftwareSelection: function (elemParent) {
        var self = this;
        self.addSelectionSoftwareView(elemParent.find('.job1'));
        self.addSelectionSoftwareView(elemParent.find('.job2'));
    },
    printJobInfo: function (elemParent) {
        var self = this;
        console.log("PRINT JOB INFO");
        self.addJobView(elemParent.find('.job1'), self.job1);
        self.addJobView(elemParent.find('.job2'), self.job2);
    },
    printParamJob: function (elemParent) {
        var self = this;
        self.addParamView(elemParent.find('.job1'), self.job1);
        self.addParamView(elemParent.find('.job2'), self.job2);
    },
    printResultJob: function (elemParent) {
        var self = this;
        self.addResultView(elemParent.find('.job1'), self.job1);
        self.addResultView(elemParent.find('.job2'), self.job2);
    },
    addSelectionView: function (elemParent, collection) {
        var self = this;
        elemParent.append('<select></select>');
        collection.each(function (job) {
            var className = self.getClassName(job);
            elemParent.find("select").append('<option class="' + className + '" value="' + job.id + '">Job ' + job.get('number') + ' (' + window.app.convertLongToDate(job.get('created')) + ')' + '</option>');
        });
        elemParent.find("select").change(function () {
            self.refreshSelectStyle(elemParent);
            self.refreshCompareJob();
        });
    },
    addSelectionSoftwareView: function (elemParent) {
        var self = this;
        elemParent.append('<select></select>');
        self.softwares.each(function (software) {
            if (software.get('resultName') == software.get('resultName')) {
                elemParent.find("select").append('<option value="' + software.id + '">Software ' + software.get('name') + '</option>');
            }
        });
        elemParent.find("select").change(function () {
            self.refreshJobList();

        });
    },
    refreshJobList: function () {
        var self = this;
        self.cleanCompareJob();
        //retrieve software 1 & 2
        var idSoftware1 = self.retrieveSelectedSoftware('1');
        self.software1 = self.softwares.get(idSoftware1);
        var idSoftware2 = self.retrieveSelectedSoftware('2');
        self.software2 = self.softwares.get(idSoftware2);
        console.log("Selected software: 1#" + self.software1.id + "- 2#" + self.software2.id);
        //retrieve job from software selection 1
        new JobCollection({ project: self.project.id, software: self.software1.id, light: true}).fetch({
            success: function (collection, response) {
                self.jobs1 = collection;

                //retrieve job from software selection 2
                new JobCollection({ project: self.project.id, software: self.software2.id, light: true}).fetch({
                    success: function (collection, response) {
                        self.jobs2 = collection;
                        self.reloadSelection();
                    }
                });
            }
        });
    },
    refreshSelectStyle: function (elemParent) {
        var value = elemParent.find("select").val();
        elemParent.find('select').attr("class", "");
        var className = elemParent.find('option[value="' + value + '"]').attr("class");
        elemParent.find("select").addClass(className);
    },
    getClassName: function (job) {
        if (job.isNotLaunch()) {
            return "btn-inverse";
        }
        else if (job.isInQueue()) {
            return "btn-info";
        }
        else if (job.isRunning()) {
            return "btn-primary";
        }
        else if (job.isSuccess()) {
            return "btn-success";
        }
        else if (job.isFailed()) {
            return "btn-danger";
        }
        else if (job.isIndeterminate()) {
            return "btn-inverse";
        }
        else if (job.isWait()) {
            return "btn-primary";
        }
        else {
            return "no supported";
        }
    },
    addJobView: function (elemParent, job) {
        var self = this;
        console.log("addJobView");
        elemParent.append('<div style="margin: 0px auto;min-width:100px;max-width:200px" id="' + job.id + '"></div>');
        self.parent.buildJobInfoElem(job, elemParent.find("#" + job.id));
    },
    addParamView: function (elemParent, job) {
        var self = this;

        elemParent.append('<table width="100%" style="width:100%;max-width:100%" cellpadding="0" cellspacing="0" border="0" class="table table-striped table-bordered table-condensed" id="runParamsTable" ></table>');
        elemParent.find('#runParamsTable').append('<thead><tr><th>Name</th><th>Value</th><th>Type</th></tr></thead>');
        elemParent.find('#runParamsTable').append('<tbody></tbody>');

        //print data from project image table
        var tbody = elemParent.find('#runParamsTable').find("tbody");

        _.each(job.get('jobParameters'), function (param) {
            tbody.append('<tr><td>' + param.name + '</td><td>' + self.getJobParamValue(param) + '</td><td>' + param.type + '</td></tr>');
        });
        elemParent.find('#runParamsTable').dataTable({
            //"sDom": "<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>>",
            "sPaginationType": "bootstrap",
            "oLanguage": {
                "sLengthMenu": "_MENU_ records per page"
            },
            "iDisplayLength": 10,
            "bLengthChange": false,
            bDestroy: true,
            "aoColumnDefs": [
                { "sWidth": "40%", "aTargets": [ 0 ] },
                { "sWidth": "40%", "aTargets": [ 1 ] },
                { "sWidth": "20%", "aTargets": [ 2 ] }
            ]
        });
    },
    getJobParamValue: function (param) {
        if (param.type == "List") {
            var valueStr = "<select>";
            var values = param.value.split(',');
            _.each(values, function (value) {
                valueStr = valueStr + "<option>" + value + "</option>";
            });
            valueStr = valueStr + "</select>";
            return valueStr;
        } else {
            return param.value
        }
    },
    addResultView: function (elemParent, job) {
        var self = this;
        if (window.app.status.currentTermsCollection == undefined) {
            new TermCollection({idProject: self.project.id}).fetch({
                success: function (terms, response) {
                    window.app.status.currentTermsCollection = terms;
                    self.initJobResult(job, elemParent);

                }
            });
        } else {
            self.initJobResult(job, elemParent);
        }
    },
    initJobResult: function (job, elemParent) {
        var self = this;
        var result = new RetrievalAlgoResult({
            model: job,
            terms: window.app.status.currentTermsCollection,
            project: self.project,
            el: elemParent
        }).render();
    }
});