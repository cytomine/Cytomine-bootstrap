var Processing = Processing || {};

Processing.Threshold =  $.extend({}, Processing.Utils,
    {
        defaultTheshold : 165,
        process : function(params) {
            console.log("Thresholding...");
            var canvas = params.canvas;
            var threshold = params.threshold;
            var d = canvas.data;
            for (var i=0; i<d.length; i+=4) {
                var r = d[i];
                var g = d[i+1];
                var b = d[i+2];
                var v = (0.2126*r + 0.7152*g + 0.0722*b >= threshold) ? 255 : 0;
                d[i] = d[i+1] = d[i+2] = v
            }
            return canvas;
        }
    });