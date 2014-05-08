package be.cytomine.utils.browser

import org.springframework.web.context.request.RequestContextHolder as RCH

class WebTierService {

    boolean transactional = false

    static scope = "prototype"

    def getRequest()
    {
        return RCH.currentRequestAttributes().currentRequest
    }

    def getSession()
    {
        return RCH.currentRequestAttributes().session
    }

}
