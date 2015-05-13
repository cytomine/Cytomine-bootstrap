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

var LoadingDialogView = Backbone.View.extend({
    tagName: "div",
    initialize: function (options) {
    },
    doLayout: function (tpl) {
        var dialog = new ConfirmDialogView({
            el: '#dialogs',
            template: _.template(tpl, {})
        }).render();

    },
    render: function () {
        var self = this;
        require(["text!application/templates/auth/LoadingDialog.tpl.html"], function (tpl) {
            self.doLayout(tpl);
        });
        return this;
    },
    initProgressBar: function () {
        $("#progress").show();
        $("#login-progressbar").progressbar({
            value: 0
        });
    },
    progress: function (value) {
        $("#login-progressbar").progressbar({
            value: value
        });
    },
    close: function () {

    }
});