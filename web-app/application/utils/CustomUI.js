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

var CustomUI = {
    customizeUI : function(callback) {
        var self = this;
        var project = "";
        if(window.app.status.currentProject) {
            project = "?project="+window.app.status.currentProject;
        }

        $.get("custom-ui/config.json"+project, function(data){
            window.app.status.customUI = data;
            if(callback) callback();
        });
    },
    mustBeShow : function(id) {
        return window.app.status.customUI[id];
    },
    hideOrShowComponents : function() {
        var self = this;
        console.log("hideOrShowProjectComponents");
        _.each(window.app.status.customUI,function(value,key) {
            if(!self.mustBeShow(key)) {
                $(".custom-ui-"+key).hide();
            } else {
                $(".custom-ui-"+key).show();
            }
        });
    },
    components: [
        //  {componentId: "project-images-tab", componentName: "Image tab"},
        {componentId: "project-annotations-tab", componentName: "Annotation tab"},
        {componentId: "project-images-tab", componentName: "Images tab"}, //TODO: if you need to add a new panel
        {componentId: "project-properties-tab", componentName: "Properties tab"},
        {componentId: "project-jobs-tab", componentName: "Jobs tab"},
        {componentId: "project-configuration-tab", componentName: "Config tab"} //TODO: cannot be hide by project-admin

    ],
    componentsPanels: [
        //  {componentId: "project-images-tab", componentName: "Image tab"},
        {componentId: "project-explore-hide-tools", componentName: "All panels"},
        {componentId: "project-explore-overview", componentName: "Overview panel"}, //TODO: if you need to add a new panel
        {componentId: "project-explore-info", componentName: "Info panel"},
        {componentId: "project-explore-image-layers", componentName: "Layer panel"},
        {componentId: "project-explore-ontology", componentName: "Ontology panel"}, //TODO: cannot be hide by project-admin
        {componentId: "project-explore-review", componentName: "Review panel"},
        {componentId: "project-explore-job", componentName: "Job panel"},
        {componentId: "project-explore-multidim", componentName: "Multidim panel"},
        {componentId: "project-explore-property", componentName: "Property panel"},
        {componentId: "project-explore-annotation-main", componentName: "Current annotation - main panel"},
        {componentId: "project-explore-annotation-info", componentName: "Current annotation - info panel"},
        {componentId: "project-explore-annotation-comments", componentName: "Current annotation - comments panel"},
        {componentId: "project-explore-annotation-preview", componentName: "Current annotation - preview panel"},
        {componentId: "project-explore-annotation-properties", componentName: "Current annotation - properties panel"},
        {componentId: "project-explore-annotation-description", componentName: "Current annotation - description panel"},
        {componentId: "project-explore-annotation-similarities", componentName: "Current annotation - similarities panel"},
        {componentId: "project-explore-annotation-panel", componentName: "Annotations panel (under the image)"}
    ],
    componentsTools: [
        //  {componentId: "project-images-tab", componentName: "Image tab"},
        {componentId: "project-tools-main", componentName: "All tools  <i class='glyphicon glyphicon-th'/>"},
        {componentId: "project-tools-select", componentName: "Select tool  <i class='glyphicon glyphicon-move'/>"},
        {componentId: "project-tools-point", componentName: "Draw point tool  <i class='glyphicon glyphicon-map-marker/>"},
        {componentId: "project-tools-arrow", componentName: "Draw arrow tool  <i class='glyphicon glyphicon-arrow-up'/>"},
        {componentId: "project-tools-rectangle", componentName: "Draw rectangle tool  <i class='glyphicon glyphicon-vector-path-square'/>"},
        {componentId: "project-tools-diamond", componentName: "Draw diamond tool  <i class='glyphicon glyphicon-irregular-circle'/>"},
        {componentId: "project-tools-circle", componentName: "Draw circle tool  <i class='glyphicon glyphicon-vector-path-circle'/>"},
        {componentId: "project-tools-polygon", componentName: "Draw polygon tool  <i class='glyphicon glyphicon-vector-path-polygon'/>"},
        {componentId: "project-tools-magic", componentName: "Magic wand tool  <i class='glyphicon glyphicon-magic'/>"},
        {componentId: "project-tools-freehand", componentName: "Draw freehand tool  <i class='glyphicon glyphicon-pencil'/>"},
        {componentId: "project-tools-union", componentName: "Union tool <i class='glyphicon glyphicon-plus'/>"},
        {componentId: "project-tools-diff", componentName: "Difference tool  <i class='glyphicon glyphicon-minus'/>"},
        {componentId: "project-tools-fill", componentName: "Fill tool "},
        {componentId: "project-tools-rule", componentName: "Rule tool  <i class='glyphicon glyphicon-ruller'/>"},
        {componentId: "project-tools-edit", componentName: "Edit tool "},
        {componentId: "project-tools-resize", componentName: "Resize tool"},
        {componentId: "project-tools-rotate", componentName: "Rotate tool"},
        {componentId: "project-tools-move", componentName: "Move tool"},
        {componentId: "project-tools-delete", componentName: "Delete tool  <i class='glyphicon glyphicon-trash'/>"},
        {componentId: "project-tools-screenshot", componentName: "Screenshot tool  <i class='glyphicon glyphicon-screenshot'/>"}


    ],

    roles:[
        { "authority": "ADMIN_PROJECT","name": "project manager"},
        { "authority": "USER_PROJECT", "name": "project user" },
        {"authority": "GUEST_PROJECT","name": "project guest user"}
    ]
};