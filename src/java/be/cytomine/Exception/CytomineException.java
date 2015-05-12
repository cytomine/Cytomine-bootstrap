package be.cytomine.Exception;

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 * This exception is the top exception for all cytomine exception
 * It store a message and a code, corresponding to an HTTP code
 */
public abstract class CytomineException extends RuntimeException{

    /**
     * Http code for an exception
     */
    public int code;

    /**
     * Message for exception
     */
    public String msg;

    /**
     * Message map with this exception
     * @param msg Message
     * @param code Http code
     */
    public CytomineException(String msg, int code) {
        System.out.println(code + "=>" + msg);
        this.msg=msg;
        this.code = code;
    }
}
