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

var PhonoController = Backbone.Router.extend({

    routes: {
        "phono": "createMenu"
    },

    initialize: function (options) {

    },

    createMenu: function () {
        //<!-- Phono -->
        // <script type="text/javascript" src="http://s.phono.com/releases/0.3/jquery.phono.js"></script>
        require(["http://s.phono.com/releases/0.3/jquery.phono.js"], function() {
            new PhonoMenu().render();
        });

    }

});