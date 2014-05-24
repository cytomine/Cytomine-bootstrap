/**
 * Created by lrollus on 5/8/14.
 */
class GlobalTagLib {

    static namespace = "wthr"
    def userAgentIdentService

    def isOldMsie = { attrs, body ->
        if (userAgentIdentService.isMsie() && userAgentIdentService.getBrowserVersionNumber()<=8) {
            out << body()
        }
    }

    def isNotOldMsie = { attrs, body ->
        if (!userAgentIdentService.isMsie() || (userAgentIdentService.isMsie() && userAgentIdentService.getBrowserVersionNumber()>8)) {
            out << body()
        }
    }

    def isMsie = { attrs, body ->
        if (userAgentIdentService.isMsie()) {
            out << body()
        }
    }

    def isNotMsie = { attrs, body ->
        if (!userAgentIdentService.isMsie()) {
            out << body()
        }
    }

    def isFirefox = { attrs, body ->
        if (userAgentIdentService.isFirefox()) {
            out << body()
        }
    }

    def isChrome = { attrs, body ->
        if (userAgentIdentService.isChrome()) {
            out << body()
        }
    }


    def isBlackberry = { attrs, body ->
        if (userAgentIdentService.isBlackberry()) {
            out << body()
        }
    }
}