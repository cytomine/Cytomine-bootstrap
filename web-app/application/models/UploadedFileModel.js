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

var UploadedFileModel = Backbone.Model.extend({

    initialize: function (options) {
        if (!options) {
            return;
        }
        this.image = options.image;
    },
    url: function () {
        var base = 'api/uploadedfile';
        var format = '.json';
        if(this.image != null && this.image != undefined) {
            return base + '/image/'+ this.image + format;
        } else{
            if (this.isNew()) {
                return base + format;
            }
            return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + this.id + format;
        }
    }
});


var UploadedFileCollection = PaginatedCollection.extend({
    model: UploadedFileModel,
    initialize: function (options) {
        this.initPaginator(options);
        if (!options) {
            return;
        }
        this.dataTables = options.dataTables;
    },
    url: function () {
        if (this.dataTables) {
            return 'api/uploadedfile.json?dataTables=true';
        } else {
            return 'api/uploadedfile.json';
        }
    }
});