selenium {
    browser = "*googlechrome" //BS:"*googlechrome /Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
    screenshot {
        onFail = true
        dir = "./target/test-reports/screenshots"
    }
/*
	slow = false									// true to run tests in slow resources mode
	singleWindow = true								// true for single window mode, false for multi-window mode
	browser = "*firefox"							// can include full path to executable, default value is *firefox or *iexplore on Windows
	url = null										// the base URL for tests, defaults to Grails server url
    defaultTimeout = 60000  						// the timeout after which selenium commands will fail
	windowMaximize = false  						// true to maximize browser on startup
	screenshot {
		dir = "./target/test-reports/screenshots"	// directory where screenshots are placed relative to project root
		onFail = false								// true to capture screenshots on test failures
	}
	server {
		host = "localhost"							// the host the selenium server will run on
		port = 4444									// the port the selenium server will run on
	}
	userExtensions = ""								// path to user extensions javascript file
*/
}