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
Processing.Utils = {
    tolerance: 30,
    startColorR: null,
    startColorG: null,
    startColorB: null,
    fillColorR: 255,
    fillColorG: 0,
    fillColorB: 0,
    canvasWidth: 256,
    canvasHeight: 256,

    getPixelPos: function (x, y) {
        return (y * this.canvasWidth + x) * 4;
    },

    matchStartColor: function (canvas, pixelPos) {
        return this.matchColor(canvas, pixelPos, this.startColorR, this.startColorG, this.startColorB, this.tolerance);
    },
    matchReplacementColor: function (canvas, pixelPos) {
        return this.matchColor(canvas, pixelPos, this.fillColorR, this.fillColorG, this.fillColorB, 0);
    },
    matchColor: function (canvas, pixelPos, colorR, colorG, colorB, tolerance) {
        var r = canvas.data[pixelPos];
        var g = canvas.data[pixelPos + 1];
        var b = canvas.data[pixelPos + 2];
        var deltaR = r - colorR;
        var deltaG = g - colorG;
        var deltaB = b - colorB;
        var euclidian_distance = Math.sqrt(deltaR * deltaR + deltaG * deltaG + deltaB * deltaB);
        return euclidian_distance <= tolerance;
    },
    colorPixel: function (canvas, pixelPos, fillColorR, fillColorG, fillColorB) {
        var _fillColorR = fillColorR || this.fillColorR;
        var _fillColorG = fillColorG || this.fillColorG;
        var _fillColorB = fillColorB || this.fillColorB;
        canvas.data[pixelPos] = _fillColorR;
        canvas.data[pixelPos + 1] = _fillColorG;
        canvas.data[pixelPos + 2] = _fillColorB;
        canvas.data[pixelPos + 3] = 255;
        return canvas;
    }
};