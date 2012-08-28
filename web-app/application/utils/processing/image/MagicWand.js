var Processing = Processing || {};
Processing.MagicWand = $.extend({}, Processing.Utils,
    {
        bbox :null,
        canvas : null,
        startX : null,
        startY : null,
        process : function(canvas, canvasWidth, canvasHeight, startX, startY, _tolerance){
            this.tolerance = _tolerance || this.tolerance;
            this.canvasWidth = canvasWidth;
            this.canvasHeight = canvasHeight;
            this.canvas = canvas;
            this.startX = startX;
            this.startY = startY;
            var firstPixelPos = this.getPixelPos(startX, startY);
            this.startColorR = canvas.data[firstPixelPos];
            this.startColorG = canvas.data[firstPixelPos+1];
            this.startColorB = canvas.data[firstPixelPos+2];
            if (this.matchReplacementColor(canvas, this.getPixelPos(startX, startY))) return; //already processed.
            this.initBBOX(startX,startY);

            this.wand(this.canvas, startX, startY);

            return {canvas : canvas, bbox : this.bbox }

        },
        wand : function(canvas, startX, startY) {
            var pixelStack = [[startX, startY]];
            var drawingBoundTop = 0;
            while(pixelStack.length) {
                var newPos, x, y, pixelPos, reachLeft, reachRight;
                newPos = pixelStack.pop();
                x = newPos[0];
                y = newPos[1];
                this.updateBBOX(x,y);

                pixelPos = this.getPixelPos(x, y);
                while(y-- >= drawingBoundTop && this.matchStartColor(canvas, pixelPos))
                {
                    pixelPos -= this.canvasWidth * 4;
                }
                pixelPos += this.canvasWidth * 4;
                ++y;
                reachLeft = false;
                reachRight = false;
                while(y++ < this.canvasHeight-1 && this.matchStartColor(canvas, pixelPos))
                {
                    this.updateBBOX(x,y);
                    canvas = this.colorPixel(canvas, pixelPos);

                    if(x > 0)
                    {
                        if(this.matchStartColor(canvas, pixelPos - 4))
                        {
                            if(!reachLeft){
                                pixelStack.push([x - 1, y]);
                                reachLeft = true;
                            }
                        }
                        else if(reachLeft)
                        {
                            reachLeft = false;
                        }
                    }

                    if(x < this.canvasWidth-1)
                    {
                        if(this.matchStartColor(canvas, pixelPos + 4))
                        {
                            if(!reachRight)
                            {
                                pixelStack.push([x + 1, y]);
                                reachRight = true;
                            }
                        }
                        else if(reachRight)
                        {
                            reachRight = false;
                        }
                    }

                    pixelPos += this.canvasWidth * 4;
                }
            }
        },

        initBBOX : function(x,y) {
            this.bbox = {};
            this.bbox.xmin = x;
            this.bbox.xmax = x;
            this.bbox.ymin = y;
            this.bbox.ymax = y;
        },
        updateBBOX : function(x,y) {
            this.bbox.xmin = Math.min(this.bbox.xmin, x);
            this.bbox.xmax = Math.max(this.bbox.xmax, x);
            this.bbox.ymin = Math.min(this.bbox.ymin, y);
            this.bbox.ymax = Math.max(this.bbox.ymax, y);
        }



    });