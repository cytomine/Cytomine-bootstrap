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

/**
 * Created with IntelliJ IDEA.
 * User: lrollus
 * Date: 20/02/14
 * Time: 16:52
 * To change this template use File | Settings | File Templates.
 */

var JobTemplatePanel = SideBarPanel.extend({
    tagName: "div",
    currentAnnotation : null,
    currentInterval : null,
    /**
     * ExplorerTabs constructor
     * @param options
     */
    initialize: function (options) {
        this.browseImageView = options.browseImageView;
    },
    /**
     * Grab the layout and call ask for render
     */
    render: function () {
        var self = this;
        require([
            "text!application/templates/explorer/JobTemplatePanel.tpl.html"
        ], function (tpl) {
            self.doLayout(tpl);
        });
        return this;
    },
    refresh: function() {
        this.doLayout();
    },
    printROILayerChoice : function() {
        var self = this;
        var panel = $('#jobTemplatePanel' + self.model.get('id'));
        panel.find('input[id=showRoiLayer' + this.model.get('id') + ']').click(function () {
            if($(this).is(':checked')) {
                self.browseImageView.layerSwitcherPanel.showROI();
            } else {
                self.browseImageView.layerSwitcherPanel.hideROI();
            }

        });


    },
    changeAnnotation : function(idAnnotation) {
        var self = this;
        self.currentAnnotation = idAnnotation;
        var panel = $('#jobTemplatePanel' + self.model.get('id'));
        panel.find(".jobTemplateInfo").empty();
        panel.find(".jobTemplateInfo").append('<img src="'+window.location.origin+'/api/annotation/'+idAnnotation+ '/crop.png?maxSize=128&draw=true" /><br/>');
        panel.find(".jobTemplateInfo").append("Annotation " + idAnnotation + "<br/>");

        panel.find(".jobTemplateROI").css("border-color","#47a447");

    },
    linkTemplateToAnntation : function() {
        var self = this;

        var jobTemplate = $('input[name=groupJobTemplate'+self.model.id+']:checked').val();

        if(!jobTemplate || !self.currentAnnotation) {
            window.app.view.message("Job", "Select a ROI and a job shortcut!", "error");
        } else {
            new JobTemplateAnnotationModel({annotationIdent: self.currentAnnotation, jobTemplate:jobTemplate}).save({}, {
                    success: function (model, response) {

                        var job = new JobModel({ id : model.get('job').id});
                        $.post(job.executeUrl())
                            .done(function() {
                                window.app.view.message("Job", "Job running!", "success");
                                self.printJobStatus( model.get('job').id);
                            })
                            .fail(function() { console.log("error"); })
                            .always(function() { console.log("finished"); });
                    },
                    error: function (model, response) {
                        var json = $.parseJSON(response.responseText);
                        window.app.view.message("Job", json.errors, "error");
                    }
                }
            );
        }


    },
    printJobStatus : function(idJob) {
        var self = this;
        var panel = $('#jobTemplatePanel' + self.model.get('id'));
        var algoView = new ProjectDashboardAlgos({model:{id:-1}});
        console.log("printJobStatus");
        var refreshData = function () {
            var selectRunElem = panel.find(".jobTemplateStatus");
            new JobModel({ id: idJob}).fetch({
                success: function (model, response) {
                    selectRunElem.empty();
                    var item = algoView.getStatusElement(model,100);
                    selectRunElem.append(item);
                    if(model.get('status')=="3" && self.currentInterval!=null) {
                        panel.find(".jobTemplateStatus").css("border-color","#47a447");
                        clearInterval(self.currentInterval);
                        self.currentInterval = null
                    }

                }
            });
        };
        refreshData();
        if(self.currentInterval!=null) {
            clearInterval(self.currentInterval);
        }
        self.currentInterval = setInterval(refreshData, 2000);
        $(window).bind('hashchange', function () {
            clearInterval(self.currentInterval);
        });



    },

    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function (tpl) {
        var self = this;
        var panel = $('#jobTemplatePanel' + self.model.get('id'));
        var content =_.template(tpl, {id:self.model.get('id')});
        panel.html(content);
        var elContent1 = panel.find(".JobTemplateContent1");
        var sourceEvent1 = panel.find(".toggle-content1");
        this.initToggle(panel, elContent1, sourceEvent1, "JobTemplateContent1");
        var list = panel.find(".jobTemplateList");
        list.empty();

        new JobTemplateCollection({project: self.model.get('project')}).fetch({
            success: function (collection, response) {

                if(collection.size()==0) {
                    panel.hide();
                } else {
                    //get all project id/name
                    var softwares = {};
                    collection.each(function(jobTemplate) {
                        softwares[jobTemplate.get('software')]=jobTemplate.get('softwareName');
                    });

                    //create div for each software
                    for (var prop in softwares) {
                        if (softwares.hasOwnProperty(prop)) {
                            list.append('<ul style="padding:10px;" class="'+prop+'">'+softwares[prop]+'</ul>');
                        }
                    }

                    //during each, add the template under the good software

                    collection.each(function(jobTemplate) {
                        var str = '<li><input type="radio" name="groupJobTemplate'+self.model.get('id')+'" value="'+jobTemplate.get('id')+'"> '+jobTemplate.get('name')+'</li>';
                        list.find("."+jobTemplate.get('software')).append(str);
                    });
                    panel.find("input[name=groupJobTemplate"+self.model.get('id')+"]").click(function() {
                        panel.find(".jobTemplateList").css("border-color","#47a447");
                    });
                }
            }
        });

        console.log('**************** 1');
        panel.find("button.Launch").click(function() {
            panel.find(".jobTemplateAction").css("border-color","#47a447");
            panel.find(".jobTemplateStatus").css("border-color","#5E5E5E");

            self.linkTemplateToAnntation();
        });


        console.log('**************** 2');
        var toolbar = $("#" + self.browseImageView.divId).find('#toolbar' + self.model.get('id'));
        var cssActivate = function(elToActivate) {
            toolbar.find("button").removeClass("active");
            $('button[id=roi' + self.model.get('id') + ']').removeClass("active");
            $(elToActivate).addClass("active");
        }

        console.log('**************** 3');
        var checkBox = panel.find('input[id=showRoiLayer' + this.model.get('id') + ']');

        panel.find('button[id=roi' + this.model.get('id') + ']').click(function () {
            //if ROI layer is not visible, show this layer
            if(!checkBox.is(':checked')) {
                checkBox.click();
            }
            cssActivate(this);
            self.browseImageView.initROI();
        });

        self.printROILayerChoice();
    }
});
