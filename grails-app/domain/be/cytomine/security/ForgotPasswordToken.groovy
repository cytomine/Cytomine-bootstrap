package be.cytomine.security

class ForgotPasswordToken {

    User user
    Date expiryDate
    String tokenKey

    def isValid(){
        return expiryDate  > new Date()
    }

}