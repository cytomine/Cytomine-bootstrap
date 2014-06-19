package be.cytomine.utils.browser

class UserAgentIdentService extends WebTierService {

    public final static String CHROME = "chrome"
    public final static String FIREFOX = "firefox"
    public final static String SAFARI = "safari"
    public final static String OTHER = "other"
    public final static String MSIE = "msie"
    public final static String UNKNOWN = "unknown"
    public final static String BLACKBERRY = "blackberry"
    public final static String SEAMONKEY = "seamonkey"

    public final static int CLIENT_CHROME = 0
    public final static int CLIENT_FIREFOX = 1
    public final static int CLIENT_SAFARI = 2
    public final static int CLIENT_OTHER = 3
    public final static int CLIENT_MSIE = 4
    public final static int CLIENT_UNKNOWN = 5
    public final static int CLIENT_BLACKBERRY = 6
    public final static int CLIENT_SEAMONKEY = 7

    boolean transactional = false


    def getUserAgentTag()
    {
        getRequest().getHeader("user-agent")
    }

    def getUserAgentInfo()
    {

        def userAgent = getUserAgentTag()

        def agentInfo = getRequest().getSession().getAttribute("myapp.service.UserAgentIdentService.agentInfo")
        if (agentInfo != null && agentInfo.agentString == userAgent) {
            return agentInfo
        } else if (agentInfo != null && agentInfo.agentString != userAgent) {
            log.warn "User agent string has changed in a single session!"
            log.warn "Previous User Agent: ${agentInfo.agentString}"
            log.warn "New User Agent: ${userAgent}"
            log.warn "Discarding existing agent info and creating new..."
        } else {
            log.debug "User agent info does not exist in session scope, creating..."
        }

        agentInfo = [:]




        def browserVersion
        def browserType
        def operatingSystem

        def platform
        def security = "unknown"
        def language = "en-US"

        if (userAgent == null) {
            agentInfo.browserType = UserAgentIdentService.CLIENT_UNKNOWN
            return agentInfo
        }

        browserType = UserAgentIdentService.CLIENT_OTHER;

        int pos = -1;
        if ((pos = userAgent.indexOf("Firefox")) >= 0) {
            browserType = UserAgentIdentService.CLIENT_FIREFOX;
            browserVersion = userAgent.substring(pos + 8).trim();
            if (browserVersion.indexOf(" ") > 0)
                browserVersion = browserVersion.substring(0, browserVersion.indexOf(" "));
            log.debug("Browser type: Firefox " + browserVersion);
        }
        if ((pos = userAgent.indexOf("Chrome")) >= 0) {
            browserType = UserAgentIdentService.CLIENT_CHROME;
            browserVersion = userAgent.substring(pos + 7).trim();
            if (browserVersion.indexOf(" ") > 0)
                browserVersion = browserVersion.substring(0, browserVersion.indexOf(" "));
            log.debug("Browser type: Chrome " + browserVersion);

        }
        if ((pos = userAgent.indexOf("Safari")) >= 0 && (userAgent.indexOf("Chrome") == -1)) {
            browserType = UserAgentIdentService.CLIENT_SAFARI;
            browserVersion = userAgent.substring(pos + 7).trim();
            if (browserVersion.indexOf(" ") > 0)
                browserVersion = browserVersion.substring(0, browserVersion.indexOf(" "));
            log.debug("Browser type: Safari " + browserVersion);

        }
        if ((pos = userAgent.indexOf("BlackBerry")) >= 0) {
            browserType = UserAgentIdentService.CLIENT_BLACKBERRY;
            browserVersion = userAgent.substring(userAgent.indexOf("/")).trim();
            if (browserVersion.indexOf(" ") > 0)
                browserVersion = browserVersion.substring(0, browserVersion.indexOf(" "));
            log.debug("Browser type: BlackBerry " + browserVersion);

        }
        if ((pos = userAgent.indexOf("SeaMonkey")) >= 0) {
            browserType = UserAgentIdentService.CLIENT_SEAMONKEY;
            browserVersion = userAgent.substring(userAgent.indexOf("/")).trim();
            if (browserVersion.indexOf(" ") > 0)
                browserVersion = browserVersion.substring(0, browserVersion.indexOf(" "));
            log.debug("Browser type: SeaMonkey " + browserVersion);

        }
        if ((pos = userAgent.indexOf("MSIE")) >= 0) {
            browserType = UserAgentIdentService.CLIENT_MSIE;
            browserVersion = userAgent.substring(pos + 5).trim();
            if (browserVersion.indexOf(" ") > 0)
                browserVersion = browserVersion.substring(0, browserVersion.indexOf(" "));
            if (browserVersion.indexOf(";") > 0)
                browserVersion = browserVersion.substring(0, browserVersion.indexOf(";"));
            log.debug("Browser type: MSIE " + browserVersion);

        }

        println "userAgent=$userAgent"
        println "browserVersion=$browserVersion"
//
//        if (userAgent.indexOf("(") > 0) {
//            String osInfo = userAgent.substring(userAgent.indexOf("(") + 1);
//            osInfo = osInfo.substring(0, osInfo.indexOf(")"));
//            println "osInfo=$osInfo"
//            String[] infoParts = osInfo.split("; ");
//            platform = infoParts[0];
//            operatingSystem = infoParts[2];
//
//            if (browserType != UserAgentIdentService.CLIENT_MSIE) {
//                if (infoParts[1].equals("U"))
//                    security = "strong";
//                if (infoParts[1].equals("I"))
//                    security = "weak";
//                if (infoParts[1].equals("N"))
//                    security = "none";
//
//                language = infoParts[3];
//
//            }
//
//        } else {
//            if (browserType == UserAgentIdentService.CLIENT_BLACKBERRY) {
//                operatingSystem = "BlackBerry " + browserVersion;
//            }
//        }

        agentInfo.browserVersion = browserVersion
        agentInfo.browserType = browserType
        agentInfo.operatingSystem = operatingSystem
        agentInfo.platform = platform
        agentInfo.security = security
        agentInfo.language = language
        agentInfo.agentString = userAgent


        getRequest().getSession().setAttribute("myapp.service.UserAgentIdentService.agentInfo", agentInfo)
        return agentInfo
    }


    public boolean isChrome()
    {
        return (getUserAgentInfo().browserType == UserAgentIdentService.CLIENT_CHROME);
    }

    public boolean isFirefox()
    {
        return (getUserAgentInfo().browserType == UserAgentIdentService.CLIENT_FIREFOX);
    }

    public boolean isMsie()
    {
        return (getUserAgentInfo().browserType == UserAgentIdentService.CLIENT_MSIE);
    }

    public boolean isOther()
    {
        return (getUserAgentInfo().browserType == UserAgentIdentService.CLIENT_OTHER);
    }

    public boolean isSafari()
    {
        return (getUserAgentInfo().browserType == UserAgentIdentService.CLIENT_SAFARI);
    }

    public boolean isBlackberry()
    {
        return (getUserAgentInfo().browserType == UserAgentIdentService.CLIENT_BLACKBERRY);
    }

    public boolean isSeamonkey()
    {
        return (getUserAgentInfo().browserType == UserAgentIdentService.CLIENT_SEAMONKEY);
    }


    public String getBrowserVersion()
    {
        return getUserAgentInfo().browserVersion;
    }


    public Double getBrowserVersionNumber()
    {
        try {
            return Double.parseDouble(getUserAgentInfo().browserVersion);
        }
        catch(Exception e) {
            return Double.MAX_VALUE
        }
    }

    public String getOperatingSystem()
    {
        return getUserAgentInfo().operatingSystem;
    }

    public String getPlatform()
    {
        return getUserAgentInfo().platform;
    }

    public String getSecurity()
    {
        return getUserAgentInfo().security;
    }

    public String getLanguage()
    {
        return getUserAgentInfo().language;
    }

    public String getBrowserType()
    {
        switch (getUserAgentInfo().browserType) {
            case CLIENT_FIREFOX:
                return FIREFOX;
            case CLIENT_CHROME:
                return CHROME;
            case CLIENT_SAFARI:
                return SAFARI;
            case CLIENT_SEAMONKEY:
                return SEAMONKEY;
            case CLIENT_MSIE:
                return MSIE;
            case CLIENT_BLACKBERRY:
                return BLACKBERRY;
            case CLIENT_OTHER:
            case CLIENT_UNKNOWN:
            default:
                return OTHER;
        }
    }
}
