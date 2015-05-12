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

var JobResultView = Backbone.View.extend({
    software: null,
    project: null,
    jobs: null,
    parent: null,
    terms : null,

    initialize: function (options) {
        this.software = options.software;
        this.project = options.project;
        this.jobs = options.jobs;
        this.terms = options.terms;
    },
    render: function () {
        var self = this;
        console.log("self.software.get('resultName')=" + self.software.get('resultName'));
        switch (self.software.get('resultName')) {
            case 'ValidateAnnotation':
                self.valideAnnotation();
                break;
            case 'ValidateEvolution':
                self.valideEvolution();
                break;
            case 'DownloadFiles':
                self.downloadFiles();
                break;
            case 'Default':
                self.defaultResult();
                break;
            default:
                self.defaultResult();
                break;
        }
        return this;
    },
    valideAnnotation: function () {
        var self = this;
        new RetrievalAlgoResult({
            model: self.model,
            project: self.project,
            el: self.el,
            jobs: self.jobs,
            terms : self.terms,
            software: self.software
        }).render();
    },
    valideEvolution: function () {
        var self = this;
        new EvolutionAlgoResult({
            model: self.model,
            project: self.project,
            el: self.el,
            jobs: self.jobs,
            terms : self.terms,
            software: self.software
        }).render();
    },
    defaultResult: function () {
        var self = this;
        new DefaultResult({
            model: self.model,
            project: self.project,
            el: self.el,
            jobs: self.jobs,
            terms : self.terms,
            software: self.software
        }).render();
    },
    downloadFiles: function () {
        var self = this;
        new DownloadFiles({
            model: self.model,
            project: self.project,
            el: self.el,
            jobs: self.jobs,
            terms : self.terms,
            software: self.software
        }).render();
    }
});