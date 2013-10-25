import be.cytomine.ViewPortToBuildXML
import grails.util.Environment

eventGenerateWebXmlEnd = {
    println "*************************** eventGenerateWebXmlEnd ***************************"
    println Environment.getCurrent() == Environment.PRODUCTION

    if (Environment.getCurrent() == Environment.PRODUCTION) {
        println("========= C O M P I L E == J S ========= ")
        ViewPortToBuildXML.process()
        def proc = "./scripts/yui-compressor-ant-task/doc/example/deploy.sh".execute()
        proc.in.eachLine { line -> println line }
        proc = "./scripts/yui-compressor-ant-task/doc/lib/deploy.sh".execute()
        proc.in.eachLine { line -> println line }
        println("======================================== ")
    }
}
//
//eventCreateWarStart = { warName, stagingDir ->
//  // ..
//    println "*************************** CREATE 2. WAR $warName"
//    log.info("========= C O M P I L E == J S ========= ")
//    ViewPortToBuildXML.process()
//    def proc = "./scripts/yui-compressor-ant-task/doc/example/deploy.sh".execute()
//    proc.in.eachLine { line -> log.info line }
//    proc = "./scripts/yui-compressor-ant-task/doc/lib/deploy.sh".execute()
//    proc.in.eachLine { line -> log.info line }
//    log.info("======================================== ")
//}