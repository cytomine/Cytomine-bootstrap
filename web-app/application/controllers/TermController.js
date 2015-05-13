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
 * Time: 10:05
 * To change this template use File | Settings | File Templates.
 */

var TermController = Backbone.Router.extend({

    routes: {
        "term": "term",
        "term/o:ontology": "term"
    },

    term: function (ontology) {
        if (!this.view) {
            this.view = new TermView({
                model: window.app.models.terms,
                ontology: ontology,
                el: $("#explorer > .term"),
                container: window.app.view.components.explorer
            }).render();

            this.view.container.views.term = this.view;
        }

        this.view.container.show(this.view);
    }


});