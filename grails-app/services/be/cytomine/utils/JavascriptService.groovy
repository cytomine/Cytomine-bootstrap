package be.cytomine.utils

import be.cytomine.ViewPortToBuildXML

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 13/03/13
 * Time: 11:48
 */
class JavascriptService {

    def compile() {
        log.info("========= C O M P I L E == J S ========= ")
        ViewPortToBuildXML.process()
        def proc = "./scripts/yui-compressor-ant-task/doc/example/deploy.sh".execute()
        proc.in.eachLine { line -> log.info line }
        proc = "./scripts/yui-compressor-ant-task/doc/lib/deploy.sh".execute()
        proc.in.eachLine { line -> log.info line }
        log.info("======================================== ")
    }
}
