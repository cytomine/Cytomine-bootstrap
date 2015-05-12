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

var ConfirmDialogView = Backbone.View.extend({
    tagName: "div",
    templateURL: null,
    templateData: null,
    initialize: function (options) {
        this.el = options.el;
        this.template = options.template;
        this.templateURL = options.templateURL;
        this.autoOpen = options.autoOpen;
        this.templateData = options.templateData;
        this.dialogAttr = {
            autoOpen: true,
            width: 'auto',
            height: 'auto'
        };
        if (options.dialogAttr != undefined) {
            if (options.dialogAttr.autoOpen) {
                this.dialogAttr.autoOpen = options.dialogAttr.autoOpen;
            }
            if (options.dialogAttr.width) {
                this.dialogAttr.width = options.dialogAttr.width;
            }
            if (options.dialogAttr.height) {
                this.dialogAttr.height = options.dialogAttr.height;
            }
            if (options.dialogAttr.dialogID) {
                this.dialogAttr.dialogID = options.dialogAttr.dialogID;
            }
        }

    },
    doLayout: function (tpl) {
        var self = this;
        $(this.el).html(tpl);

        $(this.dialogAttr.dialogID).modal({
            keyboard: true,
            show: true,
            backdrop: (this.dialogAttr.backdrop != undefined) ? this.dialogAttr.backdrop : true
        });

        $(this.dialogAttr.dialogID).on('hidden', function () {
            $(self.dialogAttr.dialogID).remove();
        });
    },
    render: function () {
        var self = this;
        if (this.template == null && this.templateURL != null && this.templateData != null) {
            require([this.templateURL], function (tpl) {
                self.template = _.template(tpl, self.templateData);
                self.doLayout(self.template);
            });
        } else {
            this.doLayout(this.template);
        }
        return this;
    },
    close: function () {
        $(this.dialogAttr.dialogID).modal('hide').remove();
        $('body').removeClass('modal-open');
        $('.modal-backdrop').remove();
    }



});