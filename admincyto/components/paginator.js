/**
 * Created by lrollus on 29/04/14.
 */
//We already have a limitTo filter built-in to angular,
//let's make a startFrom filter
angular.module("cytomineUserArea").filter('startFrom', function() {
    return function(input, start) {
        if(angular.isDefined(input)) {
            start = +start; //parse to int
            return input.slice(start);
        }
    }
});