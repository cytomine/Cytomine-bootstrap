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
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */
var TermModel = Backbone.Model.extend({

    url: function () {
        var base = 'api/term';
        var format = '.json';
        if (this.isNew()) {
            return base + format;
        }
        return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
    }
});

var AnnotationTermModel = Backbone.Model.extend({
    url: function () {
        if (this.term == null) {
            return 'api/annotation/' + this.userannotation + '/term.json';
        }
        else if (this.clear != null) {
            return 'api/annotation/' + this.userannotation + '/term/' + this.term + '/clearBefore.json';
        }
        else if (this.clearForAll != null) {
            return 'api/annotation/' + this.userannotation + '/term/' + this.term + '/clearBefore.json?clearForAll=true';
        }
        else {
            return 'api/annotation/' + this.userannotation + '/term/' + this.term + '.json';
        }
    },
    initialize: function (options) {
        this.userannotation = options.userannotation;
        this.term = options.term;
        this.clear = options.clear;
        this.clearForAll = options.clearForAll;
    }
});

var AnnotationTermCollection = PaginatedCollection.extend({
    model: TermModel,
    url: function () {
        console.log("this.idUser=" + this.idUser + " this.idNotThisUser=" + this.idNotThisUser);
        if (this.idUser == null) {
            return 'api/annotation/' + this.idAnnotation + '/term.json';
        }
        else if (this.idUser != undefined) {
            return 'api/annotation/' + this.idAnnotation + '/user/' + this.idUser + '/term.json';
        }
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.idAnnotation = options.idAnnotation;
        this.idUser = options.idUser;

    }
});

var RelationTermModel = Backbone.Model.extend({
    url: function () {
        if (this.term == null) {
            return 'api/annotation/' + this.annotation + '/term.json';
        }
        else {
            return 'api/annotation/' + this.annotation + '/term/' + this.term + '.json';
        }
    },
    initialize: function (options) {
        this.annotation = options.annotation;
        this.term = options.term;
    }
});

var RelationTermCollection = PaginatedCollection.extend({
    model: TermModel,
    url: function () {
        return 'api/annotation/' + this.idAnnotation + '/term.json';
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.idAnnotation = options.idAnnotation;

    }
});


// define our collection
var TermCollection = PaginatedCollection.extend({
    model: TermModel,
    CLASS_NAME: "be.cytomine.ontology.Term",
    url: function () {
        if (this.idProject != undefined) {
            return 'api/project/' + this.idProject + '/term.json';
        }
        else if (this.idOntology == undefined && this.idAnnotation == undefined) {
            return 'api/term.json';
        }
        else if (this.idOntology != undefined && this.idAnnotation == undefined) {
            return 'api/ontology/' + this.idOntology + '/term.json';
        }
        else if (this.idOntology != undefined && this.idAnnotation != undefined) {
            return 'api/annotation/' + this.idAnnotation + '/ontology/' + this.idOntology + '/term.json';
        }
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.idOntology = options.idOntology;
        this.idAnnotation = options.idAnnotation;
        this.idProject = options.idProject;
        // something
    },
    comparator: function (term) {
        return term.get("name");
    }
});