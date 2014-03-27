package jsondoc

import grails.converters.JSON
import jsondoc.pojo.ApiObjectFieldDocLight
import jsondoc.pojo.ApiParamDocLight
import jsondoc.pojo.ApiResponseObjectDocLight
import org.jsondoc.core.pojo.*

/**
 * Created by stevben on 16/12/13.
 */
class JSONUtils {

    static def registerMarshallers () {
        JSON.registerObjectMarshaller(ApiObjectDoc) {
            def returnArray = [:]
            returnArray['jsondocId'] = it.jsondocId
            returnArray['description'] = it.description
            returnArray['name'] = it.name
            returnArray['fields'] = it.fields
            return returnArray
        }

        JSON.registerObjectMarshaller(ApiDoc) {
            def returnArray = [:]
            returnArray['jsondocId'] = it.jsondocId
            returnArray['name'] = it.name
            returnArray['description'] = it.description
            returnArray['methods'] = it.methods
            return returnArray
        }


        JSON.registerObjectMarshaller(ApiResponseObjectDocLight) {
            def returnArray = [:]
            returnArray['jsondocId'] = it.jsondocId
            returnArray['mapKeyObject'] = it.mapKeyObject
            returnArray['mapValueObject'] = it.mapValueObject
            returnArray['multiple'] = it.multiple
            returnArray['object'] = it.object
            return returnArray
        }



        JSON.registerObjectMarshaller(ApiParamDocLight) {
            def returnArray = [:]
            returnArray['jsondocId'] = it.jsondocId
            returnArray['name'] = it.name
            returnArray['description'] = it.description
            returnArray['type'] = it.type
            returnArray['required'] = it.required
            returnArray['allowedvalues'] = it.allowedvalues
            returnArray['format'] = it.format
            return returnArray
        }




        JSON.registerObjectMarshaller(ApiObjectFieldDocLight) {
            def returnArray = [:]
            returnArray['jsondocId'] = it.jsondocId
            returnArray['name'] = it.name
            returnArray['description'] = it.description
            returnArray["type"] = it.type
            returnArray["multiple"] = it.multiple
            returnArray["description"] = it.description
            returnArray["format"] = it.format
            returnArray["allowedvalues"] = it.allowedvalues
            returnArray["mandatory"] = it.mandatory
            returnArray["useForCreation"] = it.useForCreation
            returnArray["defaultValue"] = it.defaultValue
            returnArray["presentInResponse"] = it.presentInResponse
            return returnArray
        }

        JSON.registerObjectMarshaller(ApiHeaderDoc) {
            def returnArray = [:]
            returnArray['jsondocId'] = it.jsondocId
            returnArray['name'] = it.name
            returnArray['description'] = it.description
            return returnArray
        }


        JSON.registerObjectMarshaller(ApiMethodDoc) {
            def returnArray = [:]
            returnArray['jsondocId'] = it.jsondocId
            returnArray['path'] = it.path
            returnArray['description'] = it.description
            if (it.verb == ApiVerb.GET)
                returnArray['verb'] = "GET"
            if (it.verb == ApiVerb.PUT)
                returnArray['verb'] = "PUT"
            if (it.verb == ApiVerb.POST)
                returnArray['verb'] = "POST"
            if (it.verb == ApiVerb.DELETE)
                returnArray['verb'] = "DELETE"
            returnArray['produces'] = it.produces
            returnArray['consumes'] = it.consumes
            returnArray['headers'] = it.headers
            returnArray['pathparameters'] = it.pathparameters
            returnArray['queryparameters'] = it.queryparameters
            returnArray['bodyobject'] = it.bodyobject
            returnArray['response'] = it.response
            returnArray['apierrors'] = it.apierrors
            return returnArray
        }

        JSON.registerObjectMarshaller(ApiErrorDoc) {
            def returnArray = [:]
            returnArray['jsondocId'] = it.jsondocId
            returnArray['code'] = it.code
            returnArray['description'] = it.description
            return returnArray
        }

        JSON.registerObjectMarshaller(ApiErrorDoc) {
            def returnArray = [:]
            returnArray['jsondocId'] = it.jsondocId
            returnArray['code'] = it.code
            returnArray['description'] = it.description
            return returnArray
        }

         JSON.registerObjectMarshaller(ApiBodyObjectDoc) {
             def returnArray = [:]

             returnArray['jsondocId'] = it.jsondocId
             returnArray['object'] = it.getObject()
             returnArray['multiple'] = it.getMultiple()
             returnArray['mapKeyObject'] = it.getMapKeyObject()
             returnArray['mapValueObject'] = it.getMapValueObject()
             returnArray['map'] = it.getMap()

             return returnArray
         }



    }
}
