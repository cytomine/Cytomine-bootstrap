package be.cytomine.utils

import be.cytomine.Exception.WrongArgumentException
import be.cytomine.Exception.ServerException

/**
 * User: lrollus
 * Date: 11/01/13
 * GIGA-ULg
 * 
 */
class JSONUtils {




    static public String getJSONAttrStr(def json, String attr) {
        getJSONAttrStr(json,attr,false)
       }

    static public String getJSONAttrStr(def json, String attr,boolean mandatory) {
        println "get attr $attr"
       if(json[attr]!=null && !json[attr].toString().equals("null")) {
            return json[attr].toString()
       } else {
           if(mandatory) {
               throw new WrongArgumentException("$attr must be set! value=${json[attr]}")
           }
           return null
       }
    }




       static public Date getJSONAttrDate(def json, String attr) {
           println "get attr $attr"
          if(json[attr]!=null && !json[attr].toString().equals("null")) {
               return new Date(Long.parseLong(json.created))
          } else {
              return null
          }
       }

       static public def getJSONAttrDomain(def json, String attr, def domain, boolean mandatory) {
           getJSONAttrDomain(json,attr,domain,'id','Long',mandatory)
       }

       static public def getJSONAttrDomain(def json, String attr, def domain,String column,String columnType, boolean mandatory) {
           println "get attr $attr"
          if(json[attr]!=null && !json[attr].toString().equals("null")) {
                def domainRead = domain.findWhere("$column":convertValue(json[attr].toString(),columnType))
                if(!domainRead)  {
                    throw new WrongArgumentException("$attr was not found with id:${json[attr]}")
                }
              return domainRead
          } else {
              if(mandatory) {
                  throw new WrongArgumentException("$attr must be set! value=${json[attr]}")
              }
              return null
          }
       }

       static public def convertValue(String value, String type) {
           if(type.equals("String")) {
               return value
           } else if(type.equals("Long")) {
               return Long.parseLong(value);
           }
           throw new ServerException("Type $type not supported! See cytominedomain class")
       }

       static public Long getJSONAttrLong(def json, String attr, Long defaultValue) {
           println "get attr $attr"
          if(json[attr]!=null && !json[attr].toString().equals("null")) {
              try {
                  return Long.parseLong(json[attr].toString())
              } catch(Exception e) {
                  return defaultValue
              }
          } else {
              return defaultValue
          }
       }


       static public Long getJSONAttrInteger(def json, String attr, Long defaultValue) {
           println "get attr $attr"
          if(json[attr]!=null && !json[attr].toString().equals("null")) {
              try {
                  return Integer.parseInt(json[attr].toString())
              } catch(Exception e) {
                  return defaultValue
              }
          } else {
              return defaultValue
          }
       }


       static public Double getJSONAttrDouble(def json, String attr, Double defaultValue) {
           println "get attr $attr"
          if(json[attr]!=null && !json[attr].toString().equals("null")) {
              try {
                  return Double.parseDouble(json[attr].toString())
              } catch(Exception e) {
                  return defaultValue
              }
          } else {
              return defaultValue
          }
       }

    static public Boolean getJSONAttrBoolean(def json, String attr, Boolean defaultValue) {
        println "get attr $attr"
       if(json[attr]!=null && !json[attr].toString().equals("null")) {
           try {
               return Boolean.parseBoolean(json[attr].toString())
           } catch(Exception e) {
               return defaultValue
           }
       } else {
           return defaultValue
       }
    }
}
