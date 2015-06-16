
// THIS IS THE PRODUCTION EXTERNAL CYTOMINE IRIS CONFIGURATION FILE
// use ConfigSlurper Syntax to configure the settings
println "loading production config..."

grails.logging.jul.usebridge = false

grails.host = "IRIS_URL"
grails.port = ""
grails.protocol = "http"
grails.serverURL = grails.protocol + "://" + grails.host + ((grails.port=="")?"":":" + grails.port)
grails.cytomine.apps.iris.host = grails.serverURL + "/iris"

// set some synchronization settings
grails.cytomine.apps.iris.sync.clientIdentifier = "DEMO_CYTOMINE_IRIS"
grails.cytomine.apps.iris.sync.irisHost = grails.host

// Job configuration
// disable the jobs using the "disabled"=true flag
PingCytomineHostJob.disabled = true
SynchronizeUserProgressJob.disabled = false

// MAIL SERVER CONFIGURATION
grails.mail.default.from="cytomine.ulg@gmail.com"
grails.mail.username = "cytomine.ulg@gmail.com"
grails.mail.password = MAIL_PWD #TODO
grails.mail.host = "smtp.gmail.com"
grails.mail.port = 465
grails.mail.props = [
        "mail.smtp.auth":"true",
        "mail.smtp.socketFactory.port":"465",
        "mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory",
        "mail.smtp.socketFactory.fallback":"false"
                ]


println "loaded production config."
