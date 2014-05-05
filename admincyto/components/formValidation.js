angular.module("cytomineUserArea")
.factory("formValidation",function() {

    return {
        //move to service
        getError : function (error) {
            if (angular.isDefined(error)) {
                if (error.required) {
                    return "Please enter a value";
                } else if (error.email) {
                    return "Please enter a valid email address";
                }
            }
         }
    };
});