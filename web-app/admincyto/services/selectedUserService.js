/**
 * Created by lrollus on 05/05/14.
 */
angular.module("cytomineUserArea")
.factory("selectedUserService",function() {
    var selectedUser={};

    return {
        getSelectedUser : function() {
            return selectedUser;
        },
        getSelectedUserId : function() {
            return this.getSelectedUser()? this.getSelectedUser().id : undefined;
        },
        setSelectedUser : function(user) {
            selectedUser = user;
        }
    }
});