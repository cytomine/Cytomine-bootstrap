package be.cytomine.utils

import be.cytomine.Exception.ServerException
import be.cytomine.Exception.WrongArgumentException
import grails.converters.JSON
import org.json.JSONArray

/**
 * User: lrollus
 * Date: 11/01/13
 * GIGA-ULg
 *
 *
 * Utility class to extract/read data from JSON
 * Usefull when you want to create a cytomine domain from a JSON
 */
class JSONUtils {

    /**
     * Get attr string value from JSON and check if not null (if mandatory = true)
     * @return Value as String
     */
    static public String getJSONAttrStr(def json, String attr, boolean mandatory = false) {
        if (json[attr] != null && !json[attr].toString().equals("null")) {
            return json[attr].toString()
        } else {
            if (mandatory) {
                throw new WrongArgumentException("$attr must be set! value=${json[attr]}")
            }
            return null
        }
    }

    /**
     * Get attr date value from JSON
     * @return Value as Date
     */
    static public Date getJSONAttrDate(def json, String attr) {
        if (json[attr] != null && !json[attr].toString().equals("null")) {
            return new Date(Long.parseLong(json.created))
        } else {
            return null
        }
    }

    /**
     * Get attr long value from JSON and check if not null (if mandatory = true)
     * @return Value as Long
     */
    static public Long getJSONAttrLong(def json, String attr, Long defaultValue) {
        if (json[attr] != null && !json[attr].toString().equals("null")) {
            try {
                return Long.parseLong(json[attr].toString())
            } catch (Exception e) {
                return defaultValue
            }
        } else {
            return defaultValue
        }
    }

    /**
     * Get attr int value from JSON and check if not null (if mandatory = true)
     * @return Value as Integer
     */
    static public Integer getJSONAttrInteger(def json, String attr, Long defaultValue) {
        if (json[attr] != null && !json[attr].toString().equals("null")) {
            try {
                return Integer.parseInt(json[attr].toString())
            } catch (Exception e) {
                return defaultValue
            }
        } else {
            return defaultValue
        }
    }

    /**
     * Get attr double value from JSON and check if not null (if mandatory = true)
     * @return Value as Double
     */
    static public Double getJSONAttrDouble(def json, String attr, Double defaultValue) {
        if (json[attr] != null && !json[attr].toString().equals("null")) {
            try {
                return Double.parseDouble(json[attr].toString())
            } catch (Exception e) {
                return defaultValue
            }
        } else {
            return defaultValue
        }
    }

    /**
     * Get attr bool value from JSON and check if not null (if mandatory = true)
     * @return Value as Boolean
     */
    static public Boolean getJSONAttrBoolean(def json, String attr, Boolean defaultValue) {
        if (json[attr] != null && !json[attr].toString().equals("null")) {
            try {
                return Boolean.parseBoolean(json[attr].toString())
            } catch (Exception e) {
                return defaultValue
            }
        } else {
            return defaultValue
        }
    }

    /**
     * Get attr domain value from json
     * Read domain thanks to domain argument class and its id (domain.read)
     * If mandatory flag is true, check if domain exists
     * @return  Value as Cytomine Domain
     */
    static public def getJSONAttrDomain(def json, String attr, def domain, boolean mandatory) {
        getJSONAttrDomain(json, attr, domain, 'id', 'Long', mandatory)
    }

    /**
     * Get attr domain value from json
     * Read domain thanks to domain argument, get the correct object thanks to value from column (type: columnType)
     * If mandatory flag is true, check if domain exists
     * @return  Value as Cytomine Domain
     */
    static public def getJSONAttrDomain(def json, String attr, def domain, String column, String columnType, boolean mandatory) {
        if (json[attr] != null && !json[attr].toString().equals("null")) {
            def domainRead
            if(column.equals('id')) {
                domainRead = domain.read(Long.parseLong(json[attr].toString()))
            } else {
                domainRead = domain.findWhere("$column": convertValue(json[attr].toString(), columnType))
            }
            if (!domainRead) {
                throw new WrongArgumentException("$attr was not found with id:${json[attr]}")
            }
            return domainRead
        } else {
            if (mandatory) {
                throw new WrongArgumentException("$attr must be set! value=${json[attr]}")
            }
            return null
        }
    }

    static public def getJSONList(def item) {
        if(item==null) {
            return []
        } else if(item instanceof List || item instanceof ArrayList) {
           return item
        } else if(item instanceof JSONArray) {
            return item
        }else if(item instanceof String) {
            return JSON.parse(item)
        }
        println "item.class=${item.class}"
        return item
    }



    static public def convertValue(String value, String type) {
        if (type.equals("String")) {
            return value
        } else if (type.equals("Long")) {
            return Long.parseLong(value);
        }
        throw new ServerException("Type $type not supported! See cytominedomain class")
    }
}
