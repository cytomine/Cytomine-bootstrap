var Processing = Processing || {};
Processing.MagicWand = $.extend({}, Processing.Utils,
    {
        defaultTolerance : 70,
        bbox :null,
        canvas : null,
        startX : null,
        startY : null,
        maxIter : 10000,
        process : function(params){ //canvas, canvasWidth, canvasHeight, startX, startY, tolerance
            this.tolerance = params.tolerance || this.tolerance;
            this.canvasWidth = params.canvasWidth;
            this.canvasHeight = params.canvasHeight;
            this.canvas = params.canvas;
            this.startX = params.startX;
            this.startY = params.startY;
            this.bbox = null;
            var firstPixelPos = this.getPixelPos(params.startX, params.startY);
            this.startColorR = this.canvas.data[firstPixelPos];
            this.startColorG = this.canvas.data[firstPixelPos+1];
            this.startColorB = this.canvas.data[firstPixelPos+2];
            if (this.matchReplacementColor(this.canvas, this.getPixelPos(this.startX, this.startY))) return; //already processed.
            this.initBBOX(this.startX,this.startY);

            var success = this.wand(this.canvas, this.startX, this.startY);

            return {success : success, canvas : this.canvas, bbox : this.bbox }

        },
        wand : function(canvas, startX, startY) {
            var pixelStack = [[startX, startY]];
            var drawingBoundTop = 0;
            var iter = 0;
            while(pixelStack.length && iter < this.maxIter) {
                iter++;
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
            return iter < this.maxIter;
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