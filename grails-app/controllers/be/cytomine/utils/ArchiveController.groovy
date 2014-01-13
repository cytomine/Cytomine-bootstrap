package be.cytomine.utils

import be.cytomine.api.RestController

//@ApiObject(name = "city")
class ArchiveController extends RestController {

//    @ApiMethod(
//            path="/city/get/{name}",
//            verb=ApiVerb.GET,
//            description="Gets a city with the given name, provided that the name is between sydney, melbourne and perth"
//    )
//    def simple() {
//
//    }
//
//
//    def test() {
//        println "test"
////        grailsApplication.controllerClasses.findAll {
////            if(it.clazz.name.equals("be.cytomine.utils.ArchiveController")) {
////                println "###############################################"
////                println "123=" + it.clazz
////                println it.clazz.methods
////
////
////
////
////                it.clazz.methods.collect{
////                    if(it.name.equals("simple")) {
////                        println it.name
////                        println it.annotations
////                        it.annotations.eachWithIndex { ann, indx ->
////                            if(ann.annotationType().name=="org.jsondoc.core.annotation.ApiMethod") {
////                                println "***********************"
////                                ann.each {prop ->
////                                    println prop
////                                    println prop
////                                }
////                                println it.annotations
////                                println "replace annotation to index"
////
////
////
////
////
////
////
////
////
////                                ((ApiMethod)ann).path("update")
////                                println it.annotations
////                            }
////
////                        }
////
////
////
////                    }
////
////                }
////
//////                if(it.clazz.methods.get)
//////                [0].annotations.annotations.each {
//////                    println it
//////                    it.putAt("a","b")
//////                }
////                //println it.clazz.getMethods()
////            }
////
////        }
//
//        //pool creation
//        ClassPool pool = ClassPool.getDefault();
//        //extracting the class
//        CtClass cc = pool.getCtClass("ArchiveController");
//        //looking for the method to apply the annotation on
//        //CtMethod sayHelloMethodDescriptor = cc.getDeclaredMethod(methodName);
//        // create the annotation
//        ClassFile ccFile = cc.getClassFile();
//        ConstPool constpool = ccFile.getConstPool();
//        AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
//        Annotation annot = new Annotation("sample.PersonneName", constpool);
//        annot.addMemberValue("name", new StringMemberValue("World!! (dynamic annotation)",ccFile.getConstPool()));
//        attr.addAnnotation(annot);
//        // add the annotation to the method descriptor
//
//
//        sayHelloMethodDescriptor.getMethodInfo().addAttribute(attr);
//
//
//        // transform the ctClass to java class
//        Class dynamiqueBeanClass = cc.toClass();
//        //instanciating the updated class
//        SayHelloBean sayHelloBean = (SayHelloBean) dynamiqueBeanClass.newInstance();
//
//        try{
//
//            Method helloMessageMethod = sayHelloBean.getClass().getDeclaredMethod(methodName, String.class);
//            //getting the annotation
//            PersonneName personneName = (PersonneName) helloMessageMethod.getAnnotation(PersonneName.class);
//            System.out.println(sayHelloBean.sayHelloTo(personneName.name()));
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }
//
//
////
////        grailsApplication.controllerClasses.findAll {
////            if(it.clazz.name.equals("be.cytomine.utils.ArchiveController")) {
////                println "123=" + it.clazz
////                println it.clazz.annotations.each {
////                    println it
////                }
////
////
////                Field field = Class.class.getDeclaredField("annotations");
////                field.setAccessible(true);
////                Map<Class<? extends Annotation>, Annotation> annotations = (Map<Class<? extends Annotation>, Annotation>) field.get(ArchiveController.class);
////
////                Annotation annotation = new ApiObject() {
////                    @Override
////                    public String name() {
////                        return "another value";
////                    }
////
////                    @Override
////                    public String description() {
////                        return "another description";
////                    }
////
////
////                    boolean show() {return true};
////
////                    @Override
////                    Class<? extends Annotation> annotationType() {
////                        return null
////                    }
////                }
////
////                annotations.put(ApiObject.class,annotation);
////
////                ApiObject modifiedAnnotation = (ApiObject) ArchiveController.class.getAnnotations()[0];
////                println annotation
////                println modifiedAnnotation
////            }
////
////        }
//    }

    def grailsApplication
    def modelService
    def springSecurityService
    def archiveCommandService
    def simplifyGeometryService
    def scriptService


    def archive() {
        archiveCommandService.archiveOldCommand()
        responseSuccess([])
    }

    def script() {
        scriptService.statsProject()
    }
}
