var Processing = Processing || {};

Processing.ColorChannel = $.extend({}, Processing.Utils,
    {
        RED: 0,
        GREEN: 1,
        BLUE: 2,
        ALPHA: 3,
        process: function (params) {
            console.log("Thresholding...");
            var canvas = params.canvas;
            var channel = params.channel;
            var d = canvas.data;
            for (var i = 0; i < d.length; i += 4) {
                var v = d[i + channel];
                d[i] = d[i + 1] = d[i + 2] = v
            }
            return canvas;
        }
    });