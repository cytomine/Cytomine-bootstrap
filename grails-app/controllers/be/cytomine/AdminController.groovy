package be.cytomine

import be.cytomine.api.RestController
import be.cytomine.command.Command
import be.cytomine.command.CommandHistory
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.Ontology
import be.cytomine.project.Discipline
import be.cytomine.project.Project
import grails.plugins.springsecurity.Secured
import groovy.sql.Sql
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import be.cytomine.ontology.ReviewedAnnotation

@Secured(['ROLE_ADMIN'])
class AdminController extends RestController {


    def grailsApplication

    def index() {



    }
////    def createDataSet() {
////        def brand = ["VW","BMW","Peugeot"
////
////
////        ]
////        for(i in 1..10000) {
////
////        }
////
////
////        def car = []
////        for(i in 1..10000) {
////            car << [plate:i, brand:brand.get(i%brand.size())]
////        }
////        return [brand:brand,car:car]
////    }
//
//
//
//    def createDataSet() {
//        def brand = []
//
//        for(i in 1..5000) {
//            brand << i.toString()
//
//        }
//
//        def car = []
//        for(i in 1..100000) {
//            car << [plate:i, brand:brand.get(i%brand.size())]
//        }
//        return [brand:brand,car:car]
//    }
//
//
//
//    def testss() {
//
//        println "create dataset..."
//        def dataset = createDataSet()
//        def cars = dataset.car
//        def brands = dataset.brand
//        def brandMap = [:]
//
//        long start = System.currentTimeMillis()
//        println "add brands in database..."
//        brands.eachWithIndex { it, i ->
//            Brand b = new Brand()
//            b.name = it
//            b.save()
//            brandMap.put(it,b)
//            if(i%100==0) cleanUpGorm()
//        }
//        start = computeTimeAndReset(start)
//
//        println "add cars in database..."
//        cars.eachWithIndex { it, i ->
//            Car c = new Car()
//            c.plate = it.plate
//            c.brand = brandMap.get(it.brand)
//            c.save()
//            if(i%100==0) cleanUpGorm()
//        }
//        start = computeTimeAndReset(start)
//
//        println "list all cars..."
//        def response = []
//        String request = "SELECT c.id as id, c.plate as plate, b.name as brandName FROM car c, brand b WHERE c.brand_id = b.id"
//        new Sql(dataSource).eachRow(request) {
//            response << [id:it.id, plate:  it.plate, brand: it.brandName]
//        }
//        start = computeTimeAndReset(start)
//        responseSuccess(response)
//        start = computeTimeAndReset(start)
//    }
//
//    def computeTimeAndReset(long time) {
//         long now = System.currentTimeMillis()
//         println (now-time) +" ms"
//         return now
//
//    }
//
//
//    def testSlow2() {
//        println "create dataset..."
//        def dataset = createDataSet()
//        def brandMap = [:]
//
//        long start = System.currentTimeMillis()
//        println "add brands in database..."
//        dataset.brand.each {
//            Brand b = new Brand(name:it)
//            b.save()
//            brandMap.put(it,b)
//        }
//        start = computeTimeAndReset(start)
//
//        println "add cars in database..."
//        dataset.car.eachWithIndex { it, i ->
//            Car c = new Car(plate:it.plate,brand:brandMap.get(it.brand))
//            c.save()
//        }
//        start = computeTimeAndReset(start)
//
//        println "list all cars..."
//        def response = []
//        Car.list().each{
//            response << [id:it.id, plate:it.plate, brand: it.brand.name]
//        }
//        start = computeTimeAndReset(start)
//        println "respose"
//        responseSuccess(response)
//        start = computeTimeAndReset(start)
//    }
//
////    def test() {
////        def response = []
////        Car.list().each{
////            response << [id:it.id, plate:it.plate, brand: it.brand.name]
////        }
////        println "respose"
////    }
//
//    def  dataSource
//
//
////    def testSlow() {
////        def response = []
////        .list().each{
////            response << [id:it.id, plate:it.plate, brand: it.brand.name]
////        }
////        println "respose"
////    }
//
//
//    def testSlow() {
//        def response = []
//        AlgoAnnotation.findAllByProject(Project.read(67)).each {
//            response << [id: it.id, location:  it.location.toString()]
//        }
//        responseSuccess(response)
//    }
//
//    def testFast() {
//        def response = []
//        String request = "SELECT a.id as id, ST_AsText(a.location) as location FROM algo_annotation a WHERE a.project_id = 67"
//        new Sql(dataSource).eachRow(request) {
//            response << [id:it.id, location:  it.location]
//        }
//        responseSuccess(response)
//    }
//
//    def testVeryFast() {
//        def response = []
//        String request = "SELECT a.id as id, wkt_location as location FROM algo_annotation a WHERE a.project_id = 67"
//        new Sql(dataSource).eachRow(request) {
//            response << [id:it.id, location:  it.location]
//        }
//        responseSuccess(response)
//    }
//
//
//
//
//    def testold() {
//
//
//        for ( i in 1..10000) {
//            Test test = new Test()
//            test.name = i.toString()
//            test.save()
//            if(i%100==0) cleanUpGorm()
//        }
//        cleanUpGorm()
//
//
//    }
//
//
//
//    public Object addMultiple(def service, def json) {
//        def result = [:]
//        result.data = []
//        int i = 0
//        json.each {
//            def resp = addOne(service, it)  //TODO:: when exception here, what should we do? For the time being, stop everything and response error
//            result.data << resp
//            //sometimes, call clean cache (improve very well perf for big set)
//            if (i % 100 == 0) cleanUpGorm()
//            i++
//        }
//        cleanUpGorm()
//        result.status = 200
//        result
//    }
//
//
//    def sessionFactory
//    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
//    /**
//     * Clean GORM cache
//     */
//    public void cleanUpGorm() {
//        def session = sessionFactory.currentSession
//        session.flush()
//        session.clear()
//        propertyInstanceMap.get().clear()
//    }
}
