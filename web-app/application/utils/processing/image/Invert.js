var Processing = Processing || {};
Processing.invert = {
    process:function (imgd) {
        var data = imgd.data;
        for (var pix = 0, n = data.length; pix < n; pix += 4) {
            data[pix] = 255 - data[pix]
            data[pix + 1] = 255 - data[pix + 1]
            data[pix + 2] = 255 - data[pix + 2]
        }
    }
};
