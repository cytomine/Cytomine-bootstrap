import be.cytomine.image.AbstractImage
import be.cytomine.image.AbstractImageGroup
import be.cytomine.image.ImageInstance
import be.cytomine.image.Mime
import be.cytomine.image.acquisition.Scanner
import be.cytomine.processing.ImageFilter
import be.cytomine.processing.Job
import be.cytomine.processing.Software
import be.cytomine.project.Discipline
import be.cytomine.project.Project
import be.cytomine.project.Slide
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Polygon
import com.vividsolutions.jts.io.WKTReader
import grails.util.GrailsUtil
import java.lang.management.ManagementFactory
import org.perf4j.LoggingStopWatch
import org.perf4j.StopWatch
import be.cytomine.data.*
import be.cytomine.image.server.*
import be.cytomine.ontology.*
import be.cytomine.security.*
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import static org.springframework.security.acls.domain.BasePermission.DELETE
import static org.springframework.security.acls.domain.BasePermission.READ
import static org.springframework.security.acls.domain.BasePermission.WRITE
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import be.cytomine.test.Infos
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclObjectIdentity
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid
import be.cytomine.project.ProjectGroup
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder




class BootStrap {
    def springSecurityService
    def sequenceService
    def marshallersService
    def indexService
    def grailsApplication
    def storageService
    def messageSource
    def imagePropertiesService
    def countersService
    def triggerService
    def grantService
    def userGroupService
    def aclService
    def aclUtilService
    def objectIdentityRetrievalStrategy
    def sessionFactory


    static def development = "development"
    static def production = "production"
    static def test = "test"


    def init = { servletContext ->
        //Register API Authentifier
        SpringSecurityUtils.clientRegisterFilter( 'apiAuthentificationFilter', SecurityFilterPosition.OPENID_FILTER.order + 10)
        println "###################" + ConfigurationHolder.config.grails.serverURL + "##################"
        println "GrailsUtil.environment= " + GrailsUtil.environment + " BootStrap.development=" + BootStrap.development
        if (GrailsUtil.environment == BootStrap.development) { //scripts are not present in productions mode
            compileJS();
        }

        marshallersService.initMarshallers()
        sequenceService.initSequences()
        triggerService.initTrigger()
        indexService.initIndex()
        grantService.initGrant()

        grailsApplication.domainClasses.each {domainClass ->//iterate over the domainClasses
            if (domainClass.clazz.name.contains("be.cytomine")) {//only add it to the domains in my plugin

                domainClass.metaClass.retrieveErrors = {
                    def list = delegate?.errors?.allErrors?.collect {messageSource.getMessage(it, null)}
                    return list?.join('\n')
                }
            }
        }

        /* Print JVM infos like XMX/XMS */
        List inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (int i = 0; i < inputArgs.size(); i++) {
            println inputArgs.get(i)
        }

        StopWatch stopWatch = new LoggingStopWatch();
        initData(GrailsUtil.environment)
        //countersService.updateCounters()
        //updateImageProperties()
        stopWatch.stop("initData");
        //end of init


    }

    private def compileJS() {
        println "========= C O M P I L E == J S ========= "
        def proc = "./scripts/yui-compressor-ant-task/doc/example/deploy.sh".execute()
        proc.in.eachLine { line -> println line }
        proc = "./scripts/yui-compressor-ant-task/doc/lib/deploy.sh".execute()
        proc.in.eachLine { line -> println line }
        println "======================================== "
    }

    private def initData(String env) {

        loadAnnotationTermData()

        createStorage(BootStrapData.storages)
        createImageFilters(BootStrapData.imageFiltersSamples)
        createGroups(BootStrapData.groupsSamples)
        createUsers(BootStrapData.usersSamples)
        createScanners(BootStrapData.scannersSamples)
        createMimes(BootStrapData.mimeSamples)
        createImageServers(BootStrapData.imageServerSamples)
        createRetrievalServers(BootStrapData.retrievalServerSamples)
        createOntology(BootStrapData.ontologySamples)
        createProjects(BootStrapData.projectSamples)
        createSoftware(BootStrapData.softwareSamples)
        createDiscipline(BootStrapData.disciplineSamples)

        /* Slides */
        if (env != BootStrap.test) {
            createSlidesAndAbstractImages(ImageData.ULBAnapathASP_DATA)
            createSlidesAndAbstractImages(ImageData.ULBAnapathFrottisEBUS_DATA)
            createSlidesAndAbstractImages(ImageData.ULBAnapathFrottisPAPA_DATA)
            createSlidesAndAbstractImages(ImageData.ULBAnapathLBACB_DATA)
            createSlidesAndAbstractImages(ImageData.ULBAnapathLBADQ_DATA)
            createSlidesAndAbstractImages(ImageData.ULBAnapathLBApapa_DATA)
            createSlidesAndAbstractImages(ImageData.ULBAnapathTPP_DATA)
            createSlidesAndAbstractImages(ImageData3.ULGLBTDNEO13_DATA)
            createSlidesAndAbstractImages(ImageData3.ULGTESTPHILIPS_DATA)
            createSlidesAndAbstractImages(ImageData3.ULGLBTDNEO04_DATA)
            createSlidesAndAbstractImages(ImageData3.ULGLBTDLBA_DATA)
            createSlidesAndAbstractImages(ImageData4.ULGBMGGZEBRACTL_DATA)
            createSlidesAndAbstractImages(ImageData4.BOTA)
        }

        if (env == BootStrap.production) {
            createSlidesAndAbstractImages(ImageData2.CELLSOLUTIONSBESTCYTECERVIX_DATA)
            createSlidesAndAbstractImages(ImageData5.CELLSOLUTIONSBESTCYTECERVIX_DATA)
        }

        if (env != BootStrap.test) {
            createTerms(BootStrapData.termSamples)
            createRelation(BootStrapData.relationSamples)
            createRelationTerm(BootStrapData.relationTermSamples)
            createAnnotations(BootStrapData.annotationSamples)
        }

        createProjectGrant()
        createProjectOwner()
        createAnnotationGrant()

        testAnnotationPolygone()

        Thread.sleep(5000)


        def destroy = {
        }
        //end of init
    }

    private def testAnnotationPolygone() {

//        println "testAnnotationPolygone"
//        String wkt = "POLYGON((710 160,711 160,711 163,712 163,712 165,713 165,713 170,714 170,714 176,715 176,715 181,716 181,716 184,717 184,717 187,718 187,718 189,719 189,719 194,720 194,720 202,721 202,721 207,722 207,722 214,723 214,723 219,724 219,724 227,725 227,725 232,726 232,726 239,727 239,727 244,728 244,728 249,729 249,729 251,730 251,730 256,731 256,731 258,732 258,732 261,733 261,733 263,734 263,734 268,735 268,735 281,736 281,736 288,737 288,737 293,738 293,738 301,737 301,737 306,736 306,736 308,735 308,735 311,734 311,734 312,733 312,733 314,732 314,732 315,731 315,731 317,730 317,730 318,729 318,729 319,728 319,728 320,727 320,727 321,725 321,725 322,724 322,724 323,722 323,722 324,721 324,721 325,718 325,718 326,716 326,716 327,713 327,713 328,712 328,712 329,711 329,711 330,712 330,712 331,711 331,711 332,712 332,712 345,713 345,713 351,714 351,714 357,715 357,715 365,716 365,716 370,717 370,717 378,718 378,718 383,719 383,719 395,720 395,720 400,721 400,721 403,722 403,722 408,723 408,723 415,724 415,724 419,725 419,725 422,726 422,726 425,727 425,727 427,728 427,728 432,729 432,729 440,728 440,728 445,727 445,727 448,726 448,726 452,725 452,725 457,724 457,724 459,723 459,723 462,722 462,722 463,721 463,721 465,720 465,720 466,719 466,719 468,718 468,718 469,717 469,717 470,716 470,716 471,715 471,715 472,713 472,713 473,712 473,712 474,710 474,710 475,709 475,709 476,707 476,707 477,706 477,706 478,705 478,705 479,703 479,703 480,702 480,702 481,700 481,700 482,699 482,699 483,696 483,696 484,694 484,694 485,689 485,689 486,682 486,682 487,677 487,677 488,668 488,668 487,663 487,663 486,661 486,661 485,658 485,658 484,657 484,657 483,655 483,655 482,654 482,654 481,652 481,652 480,651 480,651 479,650 479,650 478,649 478,649 477,648 477,648 476,647 476,647 475,646 475,646 474,645 474,645 473,643 473,643 472,642 472,642 471,641 471,641 470,640 470,640 469,639 469,639 467,638 467,638 466,637 466,637 464,636 464,636 463,635 463,635 460,634 460,634 458,633 458,633 454,632 454,632 451,631 451,631 449,630 449,630 447,629 447,629 445,628 445,628 443,627 443,627 441,626 441,626 440,625 440,625 437,624 437,624 435,623 435,623 430,622 430,622 427,620 427,620 428,612 428,612 429,607 429,607 430,595 430,595 431,590 431,590 432,587 432,587 433,582 433,582 434,569 434,569 435,557 435,557 436,549 436,549 435,544 435,544 434,536 434,536 433,531 433,531 432,529 432,529 431,526 431,526 430,525 430,525 429,522 429,522 428,519 428,519 427,512 427,512 426,507 426,507 425,500 425,500 424,488 424,488 423,486 423,486 424,485 424,485 425,483 425,483 426,482 426,482 427,479 427,479 428,477 428,477 429,472 429,472 430,470 430,470 431,467 431,467 432,465 432,465 433,460 433,460 434,452 434,452 433,447 433,447 432,445 432,445 431,442 431,442 430,441 430,441 429,439 429,439 428,438 428,438 427,436 427,436 426,435 426,435 425,434 425,434 424,433 424,433 423,432 423,432 421,431 421,431 420,430 420,430 418,429 418,429 417,428 417,428 414,427 414,427 412,426 412,426 407,425 407,425 399,426 399,426 394,427 394,427 392,428 392,428 389,429 389,429 388,430 388,430 386,431 386,431 385,432 385,432 383,433 383,433 382,434 382,434 381,435 381,435 380,436 380,436 379,438 379,438 378,439 378,439 377,441 377,441 376,442 376,442 375,445 375,445 374,447 374,447 373,450 373,450 372,452 372,452 371,453 371,453 370,454 370,454 369,455 369,455 368,456 368,456 367,457 367,457 366,458 366,458 365,459 365,459 364,461 364,461 363,462 363,462 362,464 362,464 361,465 361,465 360,467 360,467 359,468 359,468 358,469 358,469 357,471 357,471 356,473 356,473 355,475 355,475 354,477 354,477 353,482 353,482 352,485 352,485 351,488 351,488 350,490 350,490 349,495 349,495 348,507 348,507 347,512 347,512 346,515 346,515 345,520 345,520 344,526 344,526 343,529 343,529 342,532 342,532 341,534 341,534 340,536 340,536 339,538 339,538 338,542 338,542 337,544 337,544 336,546 336,546 335,547 335,547 334,549 334,549 333,550 333,550 332,551 332,551 331,552 331,552 330,553 330,553 329,554 329,554 328,556 328,556 327,557 327,557 326,558 326,558 325,560 325,560 324,561 324,561 323,562 323,562 322,563 322,563 321,564 321,564 320,566 320,566 319,567 319,567 318,569 318,569 317,570 317,570 316,571 316,571 315,573 315,573 314,574 314,574 312,575 312,575 311,576 311,576 309,577 309,577 308,578 308,578 305,579 305,579 303,580 303,580 301,581 301,581 298,582 298,582 295,583 295,583 293,584 293,584 291,585 291,585 290,586 290,586 288,587 288,587 287,588 287,588 285,589 285,589 284,590 284,590 283,591 283,591 282,592 282,592 281,594 281,594 280,595 280,595 279,597 279,597 278,598 278,598 275,597 275,597 270,596 270,596 267,595 267,595 264,594 264,594 262,593 262,593 259,592 259,592 257,591 257,591 256,590 256,590 253,589 253,589 252,588 252,588 251,587 251,587 249,586 249,586 248,585 248,585 246,584 246,584 245,583 245,583 243,582 243,582 242,581 242,581 241,580 241,580 239,579 239,579 238,578 238,578 237,577 237,577 236,576 236,576 235,575 235,575 233,574 233,574 232,573 232,573 231,572 231,572 230,571 230,571 229,570 229,570 228,569 228,569 226,567 226,567 225,566 225,566 224,565 224,565 223,564 223,564 222,563 222,563 221,562 221,562 220,560 220,560 219,559 219,559 218,557 218,557 217,556 217,556 216,553 216,553 215,552 215,552 214,550 214,550 213,546 213,546 212,544 212,544 211,541 211,541 210,540 210,540 209,539 209,539 210,538 210,538 211,537 211,537 212,536 212,536 213,535 213,535 215,534 215,534 216,533 216,533 217,532 217,532 218,531 218,531 220,530 220,530 221,529 221,529 222,528 222,528 223,527 223,527 225,526 225,526 226,525 226,525 227,524 227,524 228,523 228,523 229,521 229,521 230,520 230,520 231,518 231,518 232,517 232,517 233,515 233,515 234,513 234,513 235,511 235,511 236,510 236,510 237,508 237,508 238,506 238,506 239,504 239,504 240,502 240,502 241,500 241,500 242,499 242,499 243,496 243,496 244,495 244,495 245,492 245,492 246,490 246,490 247,485 247,485 248,481 248,481 249,478 249,478 250,473 250,473 251,452 251,452 250,447 250,447 249,444 249,444 248,440 248,440 247,435 247,435 246,433 246,433 245,430 245,430 244,427 244,427 243,421 243,421 242,413 242,413 241,403 241,403 240,398 240,398 239,395 239,395 238,391 238,391 237,387 237,387 238,378 238,378 237,374 237,374 238,369 238,369 239,364 239,364 240,356 240,356 241,352 241,352 242,330 242,330 243,324 243,324 244,317 244,317 245,312 245,312 246,305 246,305 247,299 247,299 248,294 248,294 249,292 249,292 250,287 250,287 251,279 251,279 252,274 252,274 253,268 253,268 254,265 254,265 255,262 255,262 256,260 256,260 257,257 257,257 258,255 258,255 259,252 259,252 260,250 260,250 261,249 261,249 262,247 262,247 263,245 263,245 264,244 264,244 265,243 265,243 266,242 266,242 267,241 267,241 268,240 268,240 270,239 270,239 271,238 271,238 272,237 272,237 273,236 273,236 274,235 274,235 275,234 275,234 276,233 276,233 277,232 277,232 278,231 278,231 279,230 279,230 280,229 280,229 281,228 281,228 282,227 282,227 283,226 283,226 284,225 284,225 286,224 286,224 287,223 287,223 289,222 289,222 296,221 296,221 307,222 307,222 313,223 313,223 318,224 318,224 320,225 320,225 325,226 325,226 330,227 330,227 332,228 332,228 334,229 334,229 336,230 336,230 338,231 338,231 340,232 340,232 342,233 342,233 343,234 343,234 345,235 345,235 348,236 348,236 349,237 349,237 352,238 352,238 354,239 354,239 357,240 357,240 359,241 359,241 361,242 361,242 362,243 362,243 363,244 363,244 364,245 364,245 365,246 365,246 367,247 367,247 368,248 368,248 370,249 370,249 371,250 371,250 374,251 374,251 376,252 376,252 378,253 378,253 379,254 379,254 381,255 381,255 382,256 382,256 383,257 383,257 384,258 384,258 385,259 385,259 386,260 386,260 388,261 388,261 389,262 389,262 391,263 391,263 392,264 392,264 394,265 394,265 395,266 395,266 396,267 396,267 398,268 398,268 399,269 399,269 401,270 401,270 402,271 402,271 403,272 403,272 404,273 404,273 405,274 405,274 406,275 406,275 408,276 408,276 409,277 409,277 410,278 410,278 411,280 411,280 412,281 412,281 413,282 413,282 414,283 414,283 415,284 415,284 416,285 416,285 417,287 417,287 418,288 418,288 419,290 419,290 420,291 420,291 421,292 421,292 422,293 422,293 423,295 423,295 424,296 424,296 425,297 425,297 426,298 426,298 427,300 427,300 428,301 428,301 429,303 429,303 430,304 430,304 431,305 431,305 432,307 432,307 433,308 433,308 434,310 434,310 435,311 435,311 436,312 436,312 437,314 437,314 438,315 438,315 439,318 439,318 440,321 440,321 441,323 441,323 442,326 442,326 443,328 443,328 444,333 444,333 445,335 445,335 446,338 446,338 447,339 447,339 448,341 448,341 449,342 449,342 450,344 450,344 451,345 451,345 452,347 452,347 453,349 453,349 454,351 454,351 455,354 455,354 456,356 456,356 457,358 457,358 458,361 458,361 459,363 459,363 460,367 460,367 461,369 461,369 462,372 462,372 463,374 463,374 464,376 464,376 465,379 465,379 466,382 466,382 467,384 467,384 468,388 468,388 469,391 469,391 470,393 470,393 471,396 471,396 472,397 472,397 473,400 473,400 474,402 474,402 475,406 475,406 476,411 476,411 477,422 477,422 478,427 478,427 479,429 479,429 480,432 480,432 481,435 481,435 482,439 482,439 483,441 483,441 484,444 484,444 485,446 485,446 486,448 486,448 487,451 487,451 488,454 488,454 489,465 489,465 490,475 490,475 491,480 491,480 492,482 492,482 493,485 493,485 494,488 494,488 495,492 495,492 496,495 496,495 497,500 497,500 498,505 498,505 499,512 499,512 500,517 500,517 501,525 501,525 502,530 502,530 503,537 503,537 504,542 504,542 505,545 505,545 506,549 506,549 507,554 507,554 508,556 508,556 509,559 509,559 510,560 510,560 511,562 511,562 512,563 512,563 513,565 513,565 514,566 514,566 515,567 515,567 516,568 516,568 517,569 517,569 519,570 519,570 520,571 520,571 522,572 522,572 523,573 523,573 526,574 526,574 528,575 528,575 533,576 533,576 539,577 539,577 544,578 544,578 554,577 554,577 559,576 559,576 561,575 561,575 563,574 563,574 565,573 565,573 567,572 567,572 568,571 568,571 569,570 569,570 570,569 570,569 571,568 571,568 572,567 572,567 573,566 573,566 574,565 574,565 575,563 575,563 576,562 576,562 577,561 577,561 578,560 578,560 579,559 579,559 580,558 580,558 581,556 581,556 582,555 582,555 583,553 583,553 584,552 584,552 585,549 585,549 586,547 586,547 587,542 587,542 588,530 588,530 589,522 589,522 590,517 590,517 591,505 591,505 592,496 592,496 591,492 591,492 592,484 592,484 591,471 591,471 590,466 590,466 589,464 589,464 588,461 588,461 587,460 587,460 586,458 586,458 585,457 585,457 584,455 584,455 583,454 583,454 582,453 582,453 581,452 581,452 580,451 580,451 578,447 578,447 577,442 577,442 576,440 576,440 575,437 575,437 574,434 574,434 573,423 573,423 572,410 572,410 571,405 571,405 570,403 570,403 569,397 569,397 568,392 568,392 567,388 567,388 566,385 566,385 565,380 565,380 564,377 564,377 563,373 563,373 562,368 562,368 561,365 561,365 560,360 560,360 559,355 559,355 558,349 558,349 557,344 557,344 556,342 556,342 555,338 555,338 554,336 554,336 553,331 553,331 552,324 552,324 551,319 551,319 550,316 550,316 549,311 549,311 548,299 548,299 547,294 547,294 546,292 546,292 545,289 545,289 544,287 544,287 543,285 543,285 542,282 542,282 541,275 541,275 540,270 540,270 539,268 539,268 538,265 538,265 537,264 537,264 536,261 536,261 535,258 535,258 534,256 534,256 533,253 533,253 532,252 532,252 531,250 531,250 530,248 530,248 529,246 529,246 528,245 528,245 527,242 527,242 526,241 526,241 525,238 525,238 524,235 524,235 523,233 523,233 522,230 522,230 521,228 521,228 520,223 520,223 519,221 519,221 518,218 518,218 517,217 517,217 516,215 516,215 515,214 515,214 514,212 514,212 513,211 513,211 512,210 512,210 511,208 511,208 510,207 510,207 509,205 509,205 508,204 508,204 507,202 507,202 506,201 506,201 505,200 505,200 504,199 504,199 503,197 503,197 502,196 502,196 501,194 501,194 500,193 500,193 499,192 499,192 498,191 498,191 497,189 497,189 496,187 496,187 495,186 495,186 494,184 494,184 493,183 493,183 492,181 492,181 491,180 491,180 490,179 490,179 489,178 489,178 488,177 488,177 487,176 487,176 486,174 486,174 485,173 485,173 484,171 484,171 483,170 483,170 482,169 482,169 481,168 481,168 480,167 480,167 479,166 479,166 478,165 478,165 477,164 477,164 476,162 476,162 475,161 475,161 474,160 474,160 473,159 473,159 472,158 472,158 470,157 470,157 469,156 469,156 468,155 468,155 466,154 466,154 465,153 465,153 464,152 464,152 463,151 463,151 462,150 462,150 461,149 461,149 460,148 460,148 458,147 458,147 457,146 457,146 456,145 456,145 455,144 455,144 454,143 454,143 453,142 453,142 451,141 451,141 450,140 450,140 448,139 448,139 447,138 447,138 446,137 446,137 445,136 445,136 444,135 444,135 443,134 443,134 441,133 441,133 440,132 440,132 438,131 438,131 437,130 437,130 435,129 435,129 433,128 433,128 432,127 432,127 430,126 430,126 429,125 429,125 427,124 427,124 426,123 426,123 424,122 424,122 422,121 422,121 420,120 420,120 419,119 419,119 417,118 417,118 415,117 415,117 414,116 414,116 413,115 413,115 411,114 411,114 409,113 409,113 408,112 408,112 406,111 406,111 405,110 405,110 402,109 402,109 401,108 401,108 399,107 399,107 397,106 397,106 395,105 395,105 393,104 393,104 391,103 391,103 390,102 390,102 389,101 389,101 387,100 387,100 386,99 386,99 384,98 384,98 383,97 383,97 380,96 380,96 378,95 378,95 375,94 375,94 372,93 372,93 371,92 371,92 368,91 368,91 366,90 366,90 364,89 364,89 363,88 363,88 361,87 361,87 360,86 360,86 357,85 357,85 355,84 355,84 350,83 350,83 342,82 342,82 339,81 339,81 337,80 337,80 336,79 336,79 333,78 333,78 331,77 331,77 328,76 328,76 325,75 325,75 324,74 324,74 321,73 321,73 319,72 319,72 316,71 316,71 313,70 313,70 312,69 312,69 309,68 309,68 307,67 307,67 302,66 302,66 300,65 300,65 297,64 297,64 295,63 295,63 293,62 293,62 290,61 290,61 289,60 289,60 286,59 286,59 284,58 284,58 281,57 281,57 279,56 279,56 277,55 277,55 274,54 274,54 272,53 272,53 269,52 269,52 267,51 267,51 265,50 265,50 263,49 263,49 261,48 261,48 257,47 257,47 254,46 254,46 252,45 252,45 249,44 249,44 244,43 244,43 241,42 241,42 237,41 237,41 233,40 233,40 230,39 230,39 227,38 227,38 225,37 225,37 220,36 220,36 215,35 215,35 213,34 213,34 208,33 208,33 196,32 196,32 187,30 187,30 186,25 186,25 185,23 185,23 184,20 184,20 183,19 183,19 182,17 182,17 181,16 181,16 180,14 180,14 179,13 179,13 178,12 178,12 177,11 177,11 176,10 176,10 174,9 174,9 173,8 173,8 171,7 171,7 170,6 170,6 167,5 167,5 165,4 165,4 161,3 161,3 157,2 157,2 155,1 155,1 150,0 150,0 142,1 142,1 129,2 129,2 124,3 124,3 122,4 122,4 119,5 119,5 118,6 118,6 116,7 116,7 114,8 114,8 112,9 112,9 111,10 111,10 109,11 109,11 108,12 108,12 106,13 106,13 104,14 104,14 103,15 103,15 101,16 101,16 99,17 99,17 98,18 98,18 96,19 96,19 94,20 94,20 93,21 93,21 92,22 92,22 90,23 90,23 89,24 89,24 88,25 88,25 87,26 87,26 85,27 85,27 84,28 84,28 82,29 82,29 81,30 81,30 80,31 80,31 79,32 79,32 78,33 78,33 77,34 77,34 76,35 76,35 75,36 75,36 74,37 74,37 73,38 73,38 72,39 72,39 71,40 71,40 69,41 69,41 68,42 68,42 67,43 67,43 66,44 66,44 64,45 64,45 63,46 63,46 62,47 62,47 61,48 61,48 60,49 60,49 59,50 59,50 58,52 58,52 57,53 57,53 56,55 56,55 55,57 55,57 54,59 54,59 53,61 53,61 52,62 52,62 51,64 51,64 50,66 50,66 49,67 49,67 48,68 48,68 47,69 47,69 46,68 46,68 45,67 45,67 44,68 44,68 42,69 42,69 41,74 41,74 42,76 42,76 41,78 41,78 40,79 40,79 39,80 39,80 38,81 38,81 37,82 37,82 36,85 36,85 35,87 35,87 34,90 34,90 33,92 33,92 32,94 32,94 31,97 31,97 30,99 30,99 29,101 29,101 28,103 28,103 27,105 27,105 26,107 26,107 25,109 25,109 24,111 24,111 23,113 23,113 22,116 22,116 21,119 21,119 20,120 20,120 19,123 19,123 18,125 18,125 17,128 17,128 16,131 16,131 15,132 15,132 14,135 14,135 13,136 13,136 12,140 12,140 11,143 11,143 10,146 10,146 9,148 9,148 8,149 8,149 7,152 7,152 6,155 6,155 7,160 7,160 6,166 6,166 5,172 5,172 4,178 4,178 3,215 3,215 4,216 4,216 3,225 3,225 4,232 4,232 5,254 5,254 4,267 4,267 3,293 3,293 2,300 2,300 1,303 1,303 0,330 0,330 1,332 1,332 2,336 2,336 3,341 3,341 4,346 4,346 5,352 5,352 6,363 6,363 7,369 7,369 8,376 8,376 9,389 9,389 10,393 10,393 11,395 11,395 12,401 12,401 13,406 13,406 14,409 14,409 15,414 15,414 16,426 16,426 17,431 17,431 18,433 18,433 19,438 19,438 20,443 20,443 21,451 21,451 22,456 22,456 23,458 23,458 24,463 24,463 25,468 25,468 26,470 26,470 27,473 27,473 28,475 28,475 29,480 29,480 30,487 30,487 31,492 31,492 32,500 32,500 33,505 33,505 34,512 34,512 35,517 35,517 36,521 36,521 37,525 37,525 38,530 38,530 39,532 39,532 40,534 40,534 41,537 41,537 42,541 42,541 43,543 43,543 44,546 44,546 45,547 45,547 46,550 46,550 47,553 47,553 48,555 48,555 49,558 49,558 50,559 50,559 51,562 51,562 52,565 52,565 53,567 53,567 54,572 54,572 55,577 55,577 56,579 56,579 57,582 57,582 58,584 58,584 59,589 59,589 60,591 60,591 61,594 61,594 62,595 62,595 63,598 63,598 64,600 64,600 65,602 65,602 66,605 66,605 67,607 67,607 68,612 68,612 69,614 69,614 70,617 70,617 71,618 71,618 72,620 72,620 73,621 73,621 74,624 74,624 75,626 75,626 76,628 76,628 77,630 77,630 78,632 78,632 79,635 79,635 80,637 80,637 81,640 81,640 82,641 82,641 83,643 83,643 84,644 84,644 85,646 85,646 86,647 86,647 87,648 87,648 88,650 88,650 89,651 89,651 90,653 90,653 91,654 91,654 92,656 92,656 93,657 93,657 94,658 94,658 95,659 95,659 96,660 96,660 98,661 98,661 99,662 99,662 100,663 100,663 101,665 101,665 102,666 102,666 103,667 103,667 104,668 104,668 105,669 105,669 107,670 107,670 108,671 108,671 109,672 109,672 110,674 110,674 111,675 111,675 112,676 112,676 113,677 113,677 114,678 114,678 116,679 116,679 117,681 117,681 119,683 119,683 120,684 120,684 121,685 121,685 122,686 122,686 123,687 123,687 124,688 124,688 125,689 125,689 126,690 126,690 127,691 127,691 128,692 128,692 129,693 129,693 130,694 130,694 131,695 131,695 132,696 132,696 133,697 133,697 134,698 134,698 136,699 136,699 138,700 138,700 140,701 140,701 141,702 141,702 142,703 142,703 144,704 144,704 145,705 145,705 147,706 147,706 148,707 148,707 151,708 151,708 153,709 153,709 158,710 158,710 160))"
//        Annotation a = new Annotation(name:"", location: new WKTReader().read(wkt), image:ImageInstance.read(16630),user:User.findByUsername("lrollus"))
//        assert a.validate()==true
//        a.save(flush:true)
//
//         println "testAnnotationPolygone a="+a



    }



    private def loadAnnotationTermData() {
//        def oldAnnotationTerm = AnnotationTerm.list()
//
//        oldAnnotationTerm.each {
//            UserAnnotationTerm userTerm = new UserAnnotationTerm()
//            userTerm.id = it.id
//            userTerm.annotation = it.annotation
//            userTerm.term = it.term
//            userTerm.user = it.user
//            userTerm.created = it.created
//            userTerm.updated = it.updated
//            userTerm.save(flush:true)
//
//
//        }
//
//


    }

    private def createProjectGrant() {
        //Remove admin ritht for non-giga user
        println "createProjectGrant..."
        List<User> usersList = User.list()
        usersList.each { user ->
            if (!user.username.equals("lrollus") && !user.username.equals("stevben") && !user.username.equals("rmaree")) {
                SecRole admin = SecRole.findByAuthority("ROLE_ADMIN")
                SecUserSecRole.remove(user, admin, true)
            }

        }
        SCH.context.authentication = new UsernamePasswordAuthenticationToken(Infos.GOODLOGIN, Infos.GOODPASSWORD, AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
        List<Project> projects = Project.list()
        projects.each { project ->
            println "createProjectGrant for project $project.name..."
            def objectACL = AclObjectIdentity.findByObjectId(project.id)

            if (!objectACL) {
                try {
                    //Create object security id for each project
                    aclService.createAcl(objectIdentityRetrievalStrategy.getObjectIdentity(project))
                    //For each project, create ADMIN grant for each user

                    List<User> users = []
                    ProjectGroup.findAllByProject(project).each {
                        Group group = it.group
                        group.users().each { user ->
                            if(!users.contains(user))
                                users.add(user)
                        }

                    }
                    users.each { user ->
                        println "add user $user.username..."
                        aclUtilService.addPermission(project, user.username, ADMINISTRATION)
                    }
                } catch (Exception e) { e.printStackTrace()}
            }
        }
        sessionFactory.currentSession.flush()
        SCH.clearContext()
    }

    private def createProjectOwner() {
        SCH.context.authentication = new UsernamePasswordAuthenticationToken(Infos.GOODLOGIN, Infos.GOODPASSWORD, AuthorityUtils.createAuthorityList('ROLE_ADMIN'))

        changeOwner("BOTANIQUE-LEAVES","aempain")
        changeOwner("ROSTOCK-HJTHIESEN-KIDNEY","rmaree")
        changeOwner("ULG-LBTD-LBA","dcataldo")
        changeOwner("ULB-ANAPATH-TPP-CB","isalmon")
        changeOwner("ULB-ANAPATH-FROTTIS-EBUS","isalmon")
        changeOwner("ULB-ANAPATH-PCT-CB","isalmon")
        changeOwner("ULB-ANAPATH-ASP-CB","isalmon")
        changeOwner("ULB-ANAPATH-LBA-DQ","isalmon")
        changeOwner("ULG-BMGG-ZEBRA_CTL","stern")
        changeOwner("ULG-LBTD-NEO13","dcataldo")
        changeOwner("ULB-ANAPATH-FROTTIS-PAPA","isalmon")
        changeOwner("ULB-ANAPATH-ASP","isalmon")
        changeOwner("ULG-LBTD-NEO04","dcataldo")
        changeOwner("XCELLSOLUTIONS-BESTCYTE-CERVIX","rmaree")
        changeOwner("ULB-ANAPATH-LBA-PAPA","isalmon")
        changeOwner("ULB-ANAPATH-LBA-CB","isalmon")
        changeOwner("ZEBRA_CTL","stern")
        sessionFactory.currentSession.flush()
        SCH.clearContext()
    }

    private void changeOwner(String projectName, String username) {
        Project project = Project.findByName(projectName)
        if(project) {
            println "Project " + project.name + " id=" + project.id  +" will be owned by " + username
            aclUtilService.changeOwner project, username
        } else {
            println "Project not found " + projectName
        }
//        AclObjectIdentity acl = AclObjectIdentity.findByObjectId(project.id)
        //        acl.owner = AclSid.findBySid(username)
        //        acl.save(flush:true)

    }


    private def createAnnotationGrant() {
        //if(Annotation.list().first().project) return
        Annotation.findAllByProjectIsNull().each{
            it.project = it.image.project
            it.save(flush:true)
        }
        //Annotation.findAllByProjectIsNull().each {it -> println it}

    }






    private def updateImageProperties() {
        def c = new ImportController()
        c.imageproperties()
    }

    /* Methods */

    def createImageFilters(imageFilters) {
        imageFilters.each { item ->
            if (ImageFilter.findByName(item.name) != null) return
            ImageFilter imageFilter = new ImageFilter(name: item.name, baseUrl: item.baseUrl)
            if (imageFilter.validate()) {
                imageFilter.save();
            } else {
                println("\n\n\n Errors in creating imageFilter for ${it.name}!\n\n\n")
                imageFilter.errors.each {
                    err -> println err
                }
            }
        }
    }

    def createStorage(storages) {
        println "createStorages"
        storages.each {
            if (Storage.findByName(it.name)) {
                def storage = Storage.findByName(it.name)
                storage.basePath = it.basePath
                storage.serviceUrl = it.serviceUrl
                storage.username = it.username
                storage.password = it.password
                storage.ip = it.ip
                storage.port = it.port
                storage.save()
            }
            else {
                def storage = new Storage(name: it.name, basePath: it.basePath, serviceUrl: it.serviceUrl, username: it.username, password: it.password, ip: it.ip, port: it.port)
                if (storage.validate()) {
                    storage.save();
                } else {
                    println("\n\n\n Errors in creating storage for ${it.name}!\n\n\n")
                    storage.errors.each {
                        err -> println err
                    }
                }
            }
        }
    }

    def createSlidesAndAbstractImages(abstractImages) {

        StopWatch stopWatch = new LoggingStopWatch();
        //Storage storage = Storage.findByName("cytomine")
        Group giga = Group.findByName('GIGA')
        User user = User.findByUsername("rmaree")
        abstractImages.each { item ->
            if (!item.name) {
                item.name = new File(item.filename).getName()
            }
            if (AbstractImage.findByFilename(item.name)) return

            def slide
            if (item.slidename != null)
                slide = Slide.findByName(item.slidename)

            if (!slide) {
                String slideName;
                if (item.slidename == null) {
                    slideName = "SLIDE " + item.name
                }
                else {
                    slideName = item.slidename
                }

                //create one with slidename name
                slide = new Slide(name: slideName, order: item.order ?: 1)

                if (slide.validate()) {

                    slide.save(flush: true)
                }
            }
            def extension = item.extension ?: "jp2"

            def mime = Mime.findByExtension(extension)

            def scanner = Scanner.findByBrand("gigascan")

            Long lo = new Long("1309250380");
            Long hi = new Date().getTime()
            Random random = new Random()
            Long randomInt = (Math.abs(random.nextLong()) % (hi.longValue() - lo.longValue() + 1)) + lo.longValue();
            Date created = new Date(randomInt);


            AbstractImage image = new AbstractImage(
                    filename: item.name,
                    scanner: scanner,
                    slide: slide,
                    width: item.width,
                    height: item.height,
                    magnification: item.magnification,
                    resolution: item.resolution,
                    path: item.filename,
                    mime: mime,
                    created: created
            )

            if (image.validate()) {


                Project project = Project.findByName(item.study)
                //assert(project != null)
                image.save(flush: true)
                //AbstractImageGroup.link(image,giga)

                if (project != null) {
                    project.groups().each { group ->
                        println "GROUP " + group.name + " IMAGE " + image.filename
                        AbstractImageGroup.link(image, group)
                    }

                    /*Storage.list().each { storage->
                        storageService.metadata(storage, image)
                    }*/


                    ImageInstance imageinstance = new ImageInstance(
                            baseImage: image,
                            user: user,
                            project: project,
                            slide: image.slide
                    )
                    if (imageinstance.validate()) {
                        imageinstance.save(flush: true)
                    } else {
                        imageinstance.errors.each { println it }
                    }

                } else { //link with stevben by default
                    Group group = Group.findByName("stevben")
                    AbstractImageGroup.link(image, group)
                }


                Storage.list().each {
                    StorageAbstractImage.link(it, image)
                }
                //StorageAbstractImage.link(storage, image)

            } else {
                println("\n\n\n Errors in image boostrap for ${item.filename}!\n\n\n")
                image.errors.each {
                    err -> println err
                }

            }
        }
    }


    def createGroups(groupsSamples) {
        groupsSamples.each { item ->
            if (Group.findByName(item.name)) return
            def group = new Group(name: item.name)
            if (group.validate()) {
                println "Creating group ${group.name}..."
                group.save(flush: true)
                println "Creating group ${group.name}... OK"
            }
            else {
                println("\n\n\n Errors in group boostrap for ${item.name}!\n\n\n")
                group.errors.each {
                    err -> println err
                }
            }
        }
    }


    def createUsers(usersSamples) {
        def userRole = SecRole.findByAuthority("ROLE_USER") ?: new SecRole(authority: "ROLE_USER").save(flush: true)
        def adminRole = SecRole.findByAuthority("ROLE_ADMIN") ?: new SecRole(authority: "ROLE_ADMIN").save(flush: true)
        usersSamples.each { item ->
            User user = User.findByUsername(item.username)
            if (user)  return
            user = new User(
                    username: item.username,
                    firstname: item.firstname,
                    lastname: item.lastname,
                    email: item.email,
                    color: item.color,
                    password: item.password,
                    enabled: true)
            user.generateKeys()
            if (user.validate()) {
                println "Creating user ${user.username}..."
                // user.addToTransactions(new Transaction())
                //user.encodePassword()
                user.save(flush: true)

                /* Create a special group the user */
                def userGroupName = item.username
                def userGroup = [
                        [name: userGroupName]
                ]
                createGroups(userGroup)
                Group group = Group.findByName(userGroupName)
                userGroupService.link(user, group)

                /* Handle groups */
                item.group.each { elem ->
                    group = Group.findByName(elem.name)
                    userGroupService.link(user, group)
                }

                /* Add Roles */
                item.roles.each { authority ->
                    println "Add SecRole " + authority + " for user " + user.username
                    SecRole secRole = SecRole.findByAuthority(authority)
                    if (secRole) SecUserSecRole.create(user, secRole)
                }

            } else {
                println("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
                user.errors.each {
                    err -> println err
                }
            }
        }
    }

    def createScanners(scannersSamples) {
        scannersSamples.each { item ->
            if (Scanner.findByBrandAndModel(item.brand, item.model)) return
            Scanner scanner = new Scanner(brand: item.brand, model: item.model)

            if (scanner.validate()) {
                println "Creating scanner ${scanner.brand} - ${scanner.model}..."
                scanner.save(flush: true)
            } else {
                println("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
                scanner.errors.each {
                    err -> println err
                }
            }
        }
    }

    def createMimes(mimeSamples) {
        mimeSamples.each { item ->
            if (Mime.findByExtension(item.extension)) return
            Mime mime = new Mime(extension: item.extension,
                    mimeType: item.mimeType)
            if (mime.validate()) {
                println "Creating mime ${mime.extension} : ${mime.mimeType}..."
                mime.save(flush: true)
            } else {
                println("\n\n\n Errors in account boostrap for ${mime.extension} : ${mime.mimeType}!\n\n\n")
                mime.errors.each {
                    err -> println err
                }
            }
        }
    }

    def createRetrievalServers(retrievalServerSamples) {
        retrievalServerSamples.each { item ->
            if (RetrievalServer.findByDescription(item.description)) return
            RetrievalServer retrievalServer = new RetrievalServer(url: item.url, description: item.description)
            if (retrievalServer.validate()) {
                println "Creating retrieval server ${item.description}... "
                retrievalServer.save(flush: true)

            } else {
                println("\n\n\n Errors in retrieval server boostrap for ${item.description} !\n\n\n")
                item.errors.each {
                    err -> println err
                }
            }
        }
    }

    def createImageServers(imageServerSamples) {
        imageServerSamples.each { item ->
            def imageServer = ImageServer.findByName(item.name)
            if (imageServer) { //exist => update
                /*imageServer.url = item.url
                imageServer.service = item.service
                imageServer.className = item.className
                imageServer.storage = Storage.findByName(item.storage)
                imageServer.save()
                item.extension.each { ext->
                    Mime mime = Mime.findByExtension(ext)
                    if (!MimeImageServer.findByImageServerAndMime(imageServer, mime)){
                        MimeImageServer.link(imageServer, mime)
                    }
                }
                return*/
            } else {
                imageServer = new ImageServer(
                        name: item.name,
                        url: item.url,
                        service: item.service,
                        className: item.className,
                        storage: Storage.findByName(item.storage),
                        available: true)

                if (imageServer.validate()) {
                    println "Creating image server ${imageServer.name}... : ${imageServer.url}"

                    imageServer.save(flush: true)

                    /* Link with MIME Types */
                    item.extension.each { ext ->
                        Mime mime = Mime.findByExtension(ext)
                        MimeImageServer.link(imageServer, mime)
                    }
                } else {
                    println("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
                    imageServer.errors.each {
                        err -> println err
                    }
                }
            }
        }
    }

    def createProjects(projectSamples) {

        //Comment: DON'T ADD AGAIN A PROJECT A USER RENAME IT
        //=> What should we do with bootsrap data?

        //        projectSamples.each { item->
        //            if(Project.findByNameIlike(item.name)) return
        //            def ontology = Ontology.findByName(item.ontology)
        //            def project = new Project(
        //                    name : item.name.toString().toUpperCase(),
        //                    ontology : ontology,
        //                    created : new Date(),
        //                    updated : item.updated,
        //                    deleted : item.deleted
        //            )
        //            if (project.validate()){
        //                println "Creating project  ${project.name}..."
        //
        //                project.save(flush : true)
        //
        //                /* Handle groups */
        //                item.groups.each { elem ->
        //                    Group group = Group.findByName(elem.name)
        //                    ProjectGroup.link(project, group)
        //                }
        //
        //
        //
        //            } else {
        //                println("\n\n\n Errors in project boostrap for ${item.name}!\n\n\n")
        //                project.errors.each {
        //                    err -> println err
        //                }
        //            }
        //        }
    }

    def createSlides(slideSamples) {
        slideSamples.each {item ->
            if (Slide.findByName(item.name)) return
            def slide = new Slide(name: item.name, order: item.order)

            if (slide.validate()) {
                println "Creating slide  ${item.name}..."

                slide.save(flush: true)

                /* Link to projects */
                /*item.projects.each { elem ->
                    Project project = Project.findByName(elem.name)
                    ProjectSlide.link(project, slide)
                }*/


            } else {
                println("\n\n\n Errors in slide boostrap for ${item.name}!\n\n\n")
                slide.errors.each {
                    err -> println err
                }
            }
        }
    }

    def createScans(scanSamples, slides) {
        scanSamples.each { item ->
            if (AbstractImage.findByPath(item.path)) return
            def extension = item.extension ?: "jp2"
            def mime = Mime.findByExtension(extension)

            def scanner = Scanner.findByBrand("gigascan")
            def user = User.findByUsername("lrollus")


            Random random = new Random()
            Long randomInt = random.nextLong()
            Date created = new Date(randomInt);

            //  String path
            //Mime mime
            def image = new AbstractImage(
                    filename: item.filename,
                    path: item.path,
                    mime: mime,
                    scanner: scanner,
                    slide: slides[item.slide],
                    created: created
            )

            if (image.validate()) {
                println "Creating image : ${image.filename}..."

                image.save(flush: true)
/*
            *//* Link to projects *//*
            item.annotations.each { elem ->
              Annotation annotation = Annotation.findByName(elem.name)
              println 'ScanAnnotation:' + image.filename + " " + annotation.name
              ScanAnnotation.link(image, annotation)
              println 'ScanAnnotation: OK'
            }*/
            } else {
                println("\n\n\n Errors in account boostrap for ${item.filename}!\n\n\n")
                image.errors.each {
                    err -> println err
                }
            }
        }
    }

    def createAnnotations(annotationSamples) {


        def annotation = null
        GeometryFactory geometryFactory = new GeometryFactory()
        annotationSamples.each { item ->
            if (Annotation.findByName(item.name)) return
            /* Read spatial data an create annotation*/
            def geom
            if (item.location[0].startsWith('POINT')) {
                //point
                geom = new WKTReader().read(item.location[0]);
            }
            else {
                //multipolygon
                Polygon[] polygons = new Polygon[(item.location).size()];
                int i = 0
                (item.location).each {itemPoly ->
                    polygons[i] = new WKTReader().read(itemPoly);
                    i++;
                }
                geom = geometryFactory.createMultiPolygon(polygons)
            }
            def scanParent = AbstractImage.findByFilename(item.scan.filename)
            def imageParent = ImageInstance.findByBaseImage(scanParent)


            def user = User.findByUsername(item.user)
            println "user " + item.user + "=" + user.username

            annotation = new Annotation(name: item.name, location: geom, image: imageParent, user: user)

            /* Save annotation */
            if (annotation.validate()) {
                println "Creating annotation : ${annotation.name}..."

                annotation.save(flush: true)

                item.term.each {  term ->
                    println "add Term " + term
                    //annotation.addToTerm(Term.findByName(term))
                    AnnotationTerm.link(annotation, Term.findByName(term))
                }


            } else {
                println("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                annotation.errors.each {
                    err -> println err
                }

            }
        }
    }

    def createOntology(ontologySamples) {
        ontologySamples.each { item ->
            if (Ontology.findByName(item.name)) return
            User user = User.findByUsername(item.user)
            def ontology = new Ontology(name: item.name, user: user)
            println "create ontology=" + ontology.name

            if (ontology.validate()) {
                println "Creating ontology : ${ontology.name}..."
                ontology.save(flush: true)
            } else {
                println("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                ontology.errors.each {
                    err -> println err
                }

            }
        }
    }

    def createTerms(termSamples) {
        println "createTerms"
        termSamples.each { item ->
            if (Term.findByNameAndOntology(item.name, Ontology.findByName(item.ontology.name))) return
            def term = new Term(name: item.name, comment: item.comment, ontology: Ontology.findByName(item.ontology.name), color: item.color)
            println "create term=" + term.name

            if (term.validate()) {
                println "Creating term : ${term.name}..."
                term.save(flush: true)

                /*  item.ontology.each {  ontology ->
                  println "add Ontology " + ontology.name
                  //annotation.addToTerm(Term.findByName(term))
                  TermOntology.link(term, Ontology.findByName(ontology.name),ontology.color)
                }*/

            } else {
                println("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                term.errors.each {
                    err -> println err
                }

            }
        }
    }


    def createRelation(relationsSamples) {
        println "createRelation"
        relationsSamples.each { item ->
            if (Relation.findByName(item.name)) return
            def relation = new Relation(name: item.name)
            println "create relation=" + relation.name

            if (relation.validate()) {
                println "Creating relation : ${relation.name}..."
                relation.save(flush: true)

            } else {
                println("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                relation.errors.each {
                    err -> println err
                }

            }
        }
    }

    def createRelationTerm(relationTermSamples) {
        relationTermSamples.each {item ->
            def ontology = Ontology.findByName(item.ontology);
            def relation = Relation.findByName(item.relation)
            def term1 = Term.findByNameAndOntology(item.term1, ontology)
            def term2 = Term.findByNameAndOntology(item.term2, ontology)

            if (!RelationTerm.findWhere('relation': relation, 'term1': term1, 'term2': term2)) {
                println "Creating term/relation  ${relation.name}:${item.term1}/${item.term2}..."
                RelationTerm.link(relation, term1, term2)
            }

        }
    }

    def createSoftware(softwareSamples) {
        println "createRelation"
        softwareSamples.each { item ->
            if (Software.findByName(item.name)) return
            Software software = new Software(name: item.name)
            println "create software=" + software.name

            if (software.validate()) {
                println "Creating software : ${software.name}..."
                software.save(flush: true)

                if (Job.findAllBySoftware(software).isEmpty()) {
                    Job job = new Job(user: User.findByUsername("lrollus"), software: software)
                    job.save(flush: true)
                }

            } else {
                println("\n\n\n Errors in account boostrap for ${software.name}!\n\n\n")
                software.errors.each {
                    err -> println err
                }

            }
        }
    }

    static def disciplineSamples = [
            [name: "IMMUNOHISTOCHEMISTRY"],
            [name: "CYTOLOGY"],
            [name: "HISTOLOGY"]
    ]

    def createDiscipline(disciplineSamples) {
        println "createDiscipline"
        disciplineSamples.each { item ->
            if (Discipline.findByName(item.name)) return
            Discipline discipline = new Discipline(name: item.name)
            println "create discipline=" + discipline.name

            if (discipline.validate()) {
                println "Creating discipline : ${discipline.name}..."
                discipline.save(flush: true)

            } else {
                println("\n\n\n Errors in account boostrap for ${discipline.name}!\n\n\n")
                discipline.errors.each {
                    err -> println err
                }

            }
        }

        mapProjectDiscipline("ROSTOCK-HJTHIESEN-KIDNEY", "IMMUNOHISTOCHEMISTRY")

        mapProjectDiscipline("ULB-ANAPATH-ASP", "CYTOLOGY")
        mapProjectDiscipline("ULB-ANAPATH-FROTTIS-EBUS", "CYTOLOGY")
        mapProjectDiscipline("ULB-ANAPATH-FROTTIS-PAPA", "CYTOLOGY")
        mapProjectDiscipline("ULB-ANAPATH-LBA-CB", "CYTOLOGY")
        mapProjectDiscipline("ULB-ANAPATH-LBA-DQ", "CYTOLOGY")
        mapProjectDiscipline("ULB-ANAPATH-LBA-PAPA", "CYTOLOGY")
        mapProjectDiscipline("ULB-ANAPATH-TPP", "CYTOLOGY")

        mapProjectDiscipline("ULG-LBTD-LBA", "CYTOLOGY")
        mapProjectDiscipline("ULG-LBTD-NEO04", "HISTOLOGY")
        mapProjectDiscipline("ULG-LBTD-NEO13", "HISTOLOGY")

        mapProjectDiscipline("XCELLSOLUTIONS-BESTCYTE-CERVIX", "CYTOLOGY")


    }

    void mapProjectDiscipline(String projectName, String disciplineName) {
        Project project = Project.findByNameIlike(projectName)
        if (!project || project.discipline) return
        project.setDiscipline(Discipline.findByNameIlike(disciplineName))
        project.save(flush: true)
    }


}
