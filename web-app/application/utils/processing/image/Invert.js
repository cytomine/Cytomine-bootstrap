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

var Processing = Processing || {};
Processing.invert = {
    process: function (imgd) {
        var data = imgd.data;
        for (var pix = 0, n = data.length; pix < n; pix += 4) {
            data[pix] = 255 - data[pix]
            data[pix + 1] = 255 - data[pix + 1]
            data[pix + 2] = 255 - data[pix + 2]
        }
    }
};
