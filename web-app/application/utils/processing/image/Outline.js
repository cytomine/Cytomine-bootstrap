var Processing = Processing || {};
Processing.Outline = $.extend({}, Processing.Utils,
    {
        canvas : null,
        points : null,
        xmin : null,
        maxIter : 5000,
        process : function (params) {
            var self = this;
            this.canvasWidth = params.canvasWidth;
            this.canvasHeight = params.canvasHeight;
            this.canvas = params.canvas;


            var firstPoint = this.findOutlineFirstPoint(params.canvas, params.bbox);
            var startX = firstPoint.x;
            var startY = firstPoint.y;
            var first = true;
            var xmin = this.canvasWidth;
            var x = startX;
            var y = startY;
            var iter= 0;
            while (iter < this.maxIter) {                  // loop until we have not traced an inner hole
                var insideSelected = this.traceEdge(startX, startY);
                if (insideSelected) {       // not an inner hole
                    if (first) return {startX : startX, startY : startY, points : this.points};      // started at seed, so we got it (sucessful)
                    if (xmin<=startX) {      // possibly the correct particle
                        /*Polygon poly = new Polygon(xpoints, ypoints, npoints);
                        if (poly.contains(startX, startY))
                            return {startX : startX, startY : startY, points : this.points};        // if we have done a clockwise loop, inside pixels are enclosed*/
                        console.log("check polygon needed");
                        return {startX : startX, startY : startY, points : this.points};
                    }
                }
                first = false;
                // we have traced an inner hole or the wrong particle
                if (!this.inside(x,y)) do {
                    x++;                    // traverse the hole
                    if (x>this.canvasWidth) console.log("Wand Malfunction"); //should never happen
                } while (!this.inside(x,y));
                do {x++;} while (this.inside(x,y)); //retry here; maybe no inner hole any more
                iter++;
            }

        },
        traceEdge : function(startX, startY) {
            var self = this;
            // Let us name the crossings between 4 pixels vertices, then the
            // vertex (x,y) marked with '+', is between pixels (x-1, y-1) and (x,y):
            //
            //    pixel    x-1    x
            //      y-1        |
            //             ----+----
            //       y         |
            //
            // The four principal directions are numbered such that the direction
            // number * 90 degrees gives the angle in the mathematical sense; and
            // the directions to the adjacent pixels (for inside(x,y,direction) are
            // at (number * 90 - 45) degrees:
            //      walking                     pixel
            //   directions:   1           directions:     2 | 1
            //              2  +  0                      ----+----
            //                 3                           3 | 0
            //
            // Directions, like angles, are cyclic; direction -1 = direction 3, etc.
            //
            // The algorithm: We walk along the border, from one vertex to the next,
            // with the outside pixels always being at the left-hand side.
            // For 8-connected tracing, we always trying to turn left as much as
            // possible, to encompass an area as large as possible.
            // Thus, when walking in direction 1 (up, -y), we start looking
            // at the pixel in direction 2; if it is inside, we proceed in this
            // direction (left); otherwise we try with direction 1 (up); if pixel 1
            // is not inside, we must proceed in direction 0 (right).
            //
            //                     2 | 1                 (i=inside, o=outside)
            //      direction 2 < ---+---- > direction 0
            //                     o | i
            //                       ^ direction 1 = up = starting direction
            //
            // For 4-connected pixels, we try to go right as much as possible:
            // First try with pixel 1; if it is outside we go in direction 0 (right).
            // Otherwise, we examine pixel 2; if it is outside, we go in
            // direction 1 (up); otherwise in direction 2 (left).
            //
            // When moving a closed loop, 'direction' gets incremented or decremented
            // by a total of 360 degrees (i.e., 4) for counterclockwise and clockwise
            // loops respectively. As the inside pixels are at the right side, we have
            // got an outline of inner pixels after a cw loop (direction decremented
            // by 4).
            //
            var fourConnected = false;
            var allPoints = true;
            this.xmin = this.canvasWidth;
            this.points = []
            var startDirection;
            if (this.inside(startX,startY))      // inside at left, outside right
                startDirection = 1;         // starting in direction 1 = up
            else {
                startDirection = 3;         // starting in direction 3 = down
                startY++;                   // continue after the boundary that has direction 3
            }
            var x = startX;
            var y = startY;
            var direction = startDirection;
            var iter = 0;
            do {
                iter++;
                var newDirection;
                if (fourConnected) {
                    newDirection = direction;
                    do {
                        if (!this.inside_dir(x, y, newDirection)) break;
                        newDirection++;
                    } while (newDirection < direction+2);
                    newDirection--;
                } else { // 8-connected
                    newDirection = direction + 1;
                    do {
                        if (this.inside_dir(x, y, newDirection)) break;
                        newDirection--;
                    } while (newDirection >= direction);
                }
                if (allPoints || newDirection!=direction)
                    this.addPoint(x,y);          // a corner point of the outline polygon: add to list
                switch (newDirection & 3) { // '& 3' is remainder modulo 4
                    case 0: x++; break;
                    case 1: y--; break;
                    case 2: x--; break;
                    case 3: y++; break;
                }
                direction = newDirection;
            } while ((x!=startX || y!=startY || (direction&3)!=startDirection) && (iter < 10000));
            if (allPoints || this.points[0].x!=x)            // if the start point = end point is a corner: add to list
                this.addPoint(x, y);
            return (direction <= 0);        // if we have done a clockwise loop, inside pixels are enclosed

        },
        inside : function(x,y) {
            if (x<0 || x>=this.canvasWidth || y<0 || y>=this.canvasHeight)
                return false;
            var pixelPos = this.getPixelPos(x,y);

           return this.matchReplacementColor(this.canvas, pixelPos);


        },
        inside_dir : function (x,y, direction) {
            switch(direction & 3) {         // '& 3' is remainder modulo 4
                case 0: return this.inside(x, y);
                case 1: return this.inside(x, y-1);
                case 2: return this.inside(x-1, y-1);
                case 3: return this.inside(x-1, y);
            }
            return false; //will never occur
        },
        addPoint : function(x,y) {
            this.points.push({x:x-1,y:y});
            if (this.xmin > x) this.xmin = x;
        },
        findOutlineFirstPoint : function(canvas, bbox) {
            var startX = bbox.xmin;
            var startY = Math.round(bbox.ymin + (bbox.ymax - bbox.ymin) / 2);
            while (!this.matchReplacementColor(canvas, this.getPixelPos(startX, startY)) && startX < this.canvasWidth) {
                startX++;
            }
            return {x:startX,y:startY};
        }
    });