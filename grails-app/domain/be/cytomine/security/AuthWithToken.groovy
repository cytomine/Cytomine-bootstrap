package be.cytomine.security

class AuthWithToken {

    User user
    Date expiryDate
    String tokenKey

    def isValid(){
        return expiryDate  > new Date()
    }

}