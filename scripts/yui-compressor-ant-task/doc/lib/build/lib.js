/** vim: et:ts=4:sw=4:sts=4
 * @license RequireJS 0.24.0 Copyright (c) 2010-2011, The Dojo Foundation All Rights Reserved.
 * Available via the MIT or new BSD license.
 * see: http://github.com/jrburke/requirejs for details
 */
/*jslint strict: false, plusplus: false */
/*global window: false, navigator: false, document: false, importScripts: false,
  jQuery: false, clearInterval: false, setInterval: false, self: false,
  setTimeout: false, opera: false */

var require, define;
(function () {
    //Change this version number for each release.
    var version = "0.24.0",
        commentRegExp = /(\/\*([\s\S]*?)\*\/|\/\/(.*)$)/mg,
        cjsRequireRegExp = /require\(["']([^'"\s]+)["']\)/g,
        currDirRegExp = /^\.\//,
        jsSuffixRegExp = /\.js$/,
        ostring = Object.prototype.toString,
        ap = Array.prototype,
        aps = ap.slice,
        apsp = ap.splice,
        isBrowser = !!(typeof window !== "undefined" && navigator && document),
        isWebWorker = !isBrowser && typeof importScripts !== "undefined",
        //PS3 indicates loaded and complete, but need to wait for complete
        //specifically. Sequence is "loading", "loaded", execution,
        // then "complete". The UA check is unfortunate, but not sure how
        //to feature test w/o causing perf issues.
        readyRegExp = isBrowser && navigator.platform === 'PLAYSTATION 3' ?
                      /^complete$/ : /^(complete|loaded)$/,
        defContextName = "_",
        //Oh the tragedy, detecting opera. See the usage of isOpera for reason.
        isOpera = typeof opera !== "undefined" && opera.toString() === "[object Opera]",
        reqWaitIdPrefix = "_r@@",
        empty = {},
        contexts = {},
        globalDefQueue = [],
        interactiveScript = null,
        isDone = false,
        useInteractive = false,
        req, cfg = {}, currentlyAddingScript, s, head, baseElement, scripts, script,
        src, subPath, mainScript, dataMain, i, scrollIntervalId, setReadyState, ctx;

    function isFunction(it) {
        return ostring.call(it) === "[object Function]";
    }

    function isArray(it) {
        return ostring.call(it) === "[object Array]";
    }

    /**
     * Simple function to mix in properties from source into target,
     * but only if target does not already have a property of the same name.
     * This is not robust in IE for transferring methods that match
     * Object.prototype names, but the uses of mixin here seem unlikely to
     * trigger a problem related to that.
     */
    function mixin(target, source, force) {
        for (var prop in source) {
            if (!(prop in empty) && (!(prop in target) || force)) {
                target[prop] = source[prop];
            }
        }
        return req;
    }

    /**
     * Used to set up package paths from a packagePaths or packages config object.
     * @param {Object} pkgs the object to store the new package config
     * @param {Array} currentPackages an array of packages to configure
     * @param {String} [dir] a prefix dir to use.
     */
    function configurePackageDir(pkgs, currentPackages, dir) {
        var i, location, pkgObj;

        for (i = 0; (pkgObj = currentPackages[i]); i++) {
            pkgObj = typeof pkgObj === "string" ? { name: pkgObj } : pkgObj;
            location = pkgObj.location;

            //Add dir to the path, but avoid paths that start with a slash
            //or have a colon (indicates a protocol)
            if (dir && (!location || (location.indexOf("/") !== 0 && location.indexOf(":") === -1))) {
                location = dir + "/" + (location || pkgObj.name);
            }

            //Create a brand new object on pkgs, since currentPackages can
            //be passed in again, and config.pkgs is the internal transformed
            //state for all package configs.
            pkgs[pkgObj.name] = {
                name: pkgObj.name,
                location: location || pkgObj.name,
                lib: pkgObj.lib || "lib",
                //Remove leading dot in main, so main paths are normalized,
                //and remove any trailing .js, since different package
                //envs have different conventions: some use a module name,
                //some use a file name.
                main: (pkgObj.main || "lib/main")
                      .replace(currDirRegExp, '')
                      .replace(jsSuffixRegExp, '')
            };
        }
    }

    //Check for an existing version of require. If so, then exit out. Only allow
    //one version of require to be active in a page. However, allow for a require
    //config object, just exit quickly if require is an actual function.
    if (typeof require !== "undefined") {
        if (isFunction(require)) {
            return;
        } else {
            //assume it is a config object.
            cfg = require;
        }
    }

    /**
     * Creates a new context for use in require and define calls.
     * Handle most of the heavy lifting. Do not want to use an object
     * with prototype here to avoid using "this" in require, in case it
     * needs to be used in more super secure envs that do not want this.
     * Also there should not be that many contexts in the page. Usually just
     * one for the default context, but could be extra for multiversion cases
     * or if a package needs a special context for a dependency that conflicts
     * with the standard context.
     */
    function newContext(contextName) {
        var context, resume,
            config = {
                waitSeconds: 7,
                baseUrl: s.baseUrl || "./",
                paths: {},
                pkgs: {}
            },
            defQueue = [],
            specified = {
                "require": true,
                "exports": true,
                "module": true
            },
            urlMap = {},
            defined = {},
            loaded = {},
            waiting = {},
            waitAry = [],
            waitIdCounter = 0,
            managerCallbacks = {},
            plugins = {},
            pluginsQueue = {},
            resumeDepth = 0,
            normalizedWaiting = {};

        /**
         * Trims the . and .. from an array of path segments.
         * It will keep a leading path segment if a .. will become
         * the first path segment, to help with module name lookups,
         * which act like paths, but can be remapped. But the end result,
         * all paths that use this function should look normalized.
         * NOTE: this method MODIFIES the input array.
         * @param {Array} ary the array of path segments.
         */
        function trimDots(ary) {
            var i, part;
            for (i = 0; (part = ary[i]); i++) {
                if (part === ".") {
                    ary.splice(i, 1);
                    i -= 1;
                } else if (part === "..") {
                    if (i === 1 && (ary[2] === '..' || ary[0] === '..')) {
                        //End of the line. Keep at least one non-dot
                        //path segment at the front so it can be mapped
                        //correctly to disk. Otherwise, there is likely
                        //no path mapping for a path starting with '..'.
                        //This can still fail, but catches the most reasonable
                        //uses of ..
                        break;
                    } else if (i > 0) {
                        ary.splice(i - 1, 2);
                        i -= 2;
                    }
                }
            }
        }

        /**
         * Given a relative module name, like ./something, normalize it to
         * a real name that can be mapped to a path.
         * @param {String} name the relative name
         * @param {String} baseName a real name that the name arg is relative
         * to.
         * @returns {String} normalized name
         */
        function normalize(name, baseName) {
            var pkgName, pkgConfig;

            //Adjust any relative paths.
            if (name.charAt(0) === ".") {
                //If have a base name, try to normalize against it,
                //otherwise, assume it is a top-level require that will
                //be relative to baseUrl in the end.
                if (baseName) {
                    if (config.pkgs[baseName]) {
                        //If the baseName is a package name, then just treat it as one
                        //name to concat the name with.
                        baseName = [baseName];
                    } else {
                        //Convert baseName to array, and lop off the last part,
                        //so that . matches that "directory" and not name of the baseName's
                        //module. For instance, baseName of "one/two/three", maps to
                        //"one/two/three.js", but we want the directory, "one/two" for
                        //this normalization.
                        baseName = baseName.split("/");
                        baseName = baseName.slice(0, baseName.length - 1);
                    }

                    name = baseName.concat(name.split("/"));
                    trimDots(name);

                    //Some use of packages may use a . path to reference the
                    //"main" module name, so normalize for that.
                    pkgConfig = config.pkgs[(pkgName = name[0])];
                    name = name.join("/");
                    if (pkgConfig && name === pkgName + '/' + pkgConfig.main) {
                        name = pkgName;
                    }
                }
            }
            return name;
        }

        /**
         * Creates a module mapping that includes plugin prefix, module
         * name, and path. If parentModuleMap is provided it will
         * also normalize the name via require.normalize()
         *
         * @param {String} name the module name
         * @param {String} [parentModuleMap] parent module map
         * for the module name, used to resolve relative names.
         *
         * @returns {Object}
         */
        function makeModuleMap(name, parentModuleMap) {
            var index = name ? name.indexOf("!") : -1,
                prefix = null,
                parentName = parentModuleMap ? parentModuleMap.name : null,
                originalName = name,
                normalizedName, url, pluginModule;

            if (index !== -1) {
                prefix = name.substring(0, index);
                name = name.substring(index + 1, name.length);
            }

            if (prefix) {
                prefix = normalize(prefix, parentName);
            }

            //Account for relative paths if there is a base name.
            if (name) {
                if (prefix) {
                    pluginModule = defined[prefix];
                    if (pluginModule) {
                        //Plugin is loaded, use its normalize method, otherwise,
                        //normalize name as usual.
                        if (pluginModule.normalize) {
                            normalizedName = pluginModule.normalize(name, function (name) {
                                return normalize(name, parentName);
                            });
                        } else {
                            normalizedName = normalize(name, parentName);
                        }
                    } else {
                        //Plugin is not loaded yet, so do not normalize
                        //the name, wait for plugin to load to see if
                        //it has a normalize method. To avoid possible
                        //ambiguity with relative names loaded from another
                        //plugin, use the parent's name as part of this name.
                        normalizedName = '__$p' + parentName + '@' + name;
                    }
                } else {
                    normalizedName = normalize(name, parentName);
                }

                url = urlMap[normalizedName];
                if (!url) {
                    //Calculate url for the module, if it has a name.
                    if (req.toModuleUrl) {
                        //Special logic required for a particular engine,
                        //like Node.
                        url = req.toModuleUrl(context, name, parentModuleMap);
                    } else {
                        url = context.nameToUrl(name, null, parentModuleMap);
                    }

                    //Store the URL mapping for later.
                    urlMap[normalizedName] = url;
                }
            }

            return {
                prefix: prefix,
                name: normalizedName,
                parentMap: parentModuleMap,
                url: url,
                originalName: originalName,
                fullName: prefix ? prefix + "!" + normalizedName : normalizedName
            };
        }

        /**
         * Determine if priority loading is done. If so clear the priorityWait
         */
        function isPriorityDone() {
            var priorityDone = true,
                priorityWait = config.priorityWait,
                priorityName, i;
            if (priorityWait) {
                for (i = 0; (priorityName = priorityWait[i]); i++) {
                    if (!loaded[priorityName]) {
                        priorityDone = false;
                        break;
                    }
                }
                if (priorityDone) {
                    delete config.priorityWait;
                }
            }
            return priorityDone;
        }

        /**
         * Helper function that creates a setExports function for a "module"
         * CommonJS dependency. Do this here to avoid creating a closure that
         * is part of a loop.
         */
        function makeSetExports(moduleObj) {
            return function (exports) {
                moduleObj.exports = exports;
            };
        }

        function makeContextModuleFunc(func, relModuleMap, enableBuildCallback) {
            return function () {
                //A version of a require function that passes a moduleName
                //value for items that may need to
                //look up paths relative to the moduleName
                var args = [].concat(aps.call(arguments, 0)), lastArg;
                if (enableBuildCallback &&
                    isFunction((lastArg = args[args.length - 1]))) {
                    lastArg.__requireJsBuild = true;
                }
                args.push(relModuleMap);
                return func.apply(null, args);
            };
        }

        /**
         * Helper function that creates a require function object to give to
         * modules that ask for it as a dependency. It needs to be specific
         * per module because of the implication of path mappings that may
         * need to be relative to the module name.
         */
        function makeRequire(relModuleMap, enableBuildCallback) {
            var modRequire = makeContextModuleFunc(context.require, relModuleMap, enableBuildCallback);

            mixin(modRequire, {
                nameToUrl: makeContextModuleFunc(context.nameToUrl, relModuleMap),
                toUrl: makeContextModuleFunc(context.toUrl, relModuleMap),
                isDefined: makeContextModuleFunc(context.isDefined, relModuleMap),
                ready: req.ready,
                isBrowser: req.isBrowser
            });
            //Something used by node.
            if (req.paths) {
                modRequire.paths = req.paths;
            }
            return modRequire;
        }

        /**
         * Used to update the normalized name for plugin-based dependencies
         * after a plugin loads, since it can have its own normalization structure.
         * @param {String} pluginName the normalized plugin module name.
         */
        function updateNormalizedNames(pluginName) {

            var oldFullName, oldModuleMap, moduleMap, fullName, callbacks,
                i, j, k, depArray, existingCallbacks,
                maps = normalizedWaiting[pluginName];

            if (maps) {
                for (i = 0; (oldModuleMap = maps[i]); i++) {
                    oldFullName = oldModuleMap.fullName;
                    moduleMap = makeModuleMap(oldModuleMap.originalName, oldModuleMap.parentMap);
                    fullName = moduleMap.fullName;
                    //Callbacks could be undefined if the same plugin!name was
                    //required twice in a row, so use empty array in that case.
                    callbacks = managerCallbacks[oldFullName] || [];
                    existingCallbacks = managerCallbacks[fullName];

                    if (fullName !== oldFullName) {
                        //Update the specified object, but only if it is already
                        //in there. In sync environments, it may not be yet.
                        if (oldFullName in specified) {
                            delete specified[oldFullName];
                            specified[fullName] = true;
                        }

                        //Update managerCallbacks to use the correct normalized name.
                        //If there are already callbacks for the normalized name,
                        //just add to them.
                        if (existingCallbacks) {
                            managerCallbacks[fullName] = existingCallbacks.concat(callbacks);
                        } else {
                            managerCallbacks[fullName] = callbacks;
                        }
                        delete managerCallbacks[oldFullName];

                        //In each manager callback, update the normalized name in the depArray.
                        for (j = 0; j < callbacks.length; j++) {
                            depArray = callbacks[j].depArray;
                            for (k = 0; k < depArray.length; k++) {
                                if (depArray[k] === oldFullName) {
                                    depArray[k] = fullName;
                                }
                            }
                        }
                    }
                }
            }

            delete normalizedWaiting[pluginName];
        }

        /*
         * Queues a dependency for checking after the loader is out of a
         * "paused" state, for example while a script file is being loaded
         * in the browser, where it may have many modules defined in it.
         *
         * depName will be fully qualified, no relative . or .. path.
         */
        function queueDependency(dep) {
            //Make sure to load any plugin and associate the dependency
            //with that plugin.
            var prefix = dep.prefix,
                fullName = dep.fullName;

            //Do not bother if the depName is already in transit
            if (specified[fullName] || fullName in defined) {
                return;
            }

            if (prefix && !plugins[prefix]) {
                //Queue up loading of the dependency, track it
                //via context.plugins. Mark it as a plugin so
                //that the build system will know to treat it
                //special.
                plugins[prefix] = undefined;

                //Remember this dep that needs to have normaliztion done
                //after the plugin loads.
                (normalizedWaiting[prefix] || (normalizedWaiting[prefix] = []))
                    .push(dep);

                //Register an action to do once the plugin loads, to update
                //all managerCallbacks to use a properly normalized module
                //name.
                (managerCallbacks[prefix] ||
                (managerCallbacks[prefix] = [])).push({
                    onDep: function (name, value) {
                        if (name === prefix) {
                            updateNormalizedNames(prefix);
                        }
                    }
                });

                queueDependency(makeModuleMap(prefix));
            }

            context.paused.push(dep);
        }

        function execManager(manager) {
            var i, ret, waitingCallbacks,
                cb = manager.callback,
                fullName = manager.fullName,
                args = [],
                ary = manager.depArray;

            //Call the callback to define the module, if necessary.
            if (cb && isFunction(cb)) {
                //Pull out the defined dependencies and pass the ordered
                //values to the callback.
                if (ary) {
                    for (i = 0; i < ary.length; i++) {
                        args.push(manager.deps[ary[i]]);
                    }
                }

                ret = req.execCb(fullName, manager.callback, args);

                if (fullName) {
                    //If using exports and the function did not return a value,
                    //and the "module" object for this definition function did not
                    //define an exported value, then use the exports object.
                    if (manager.usingExports && ret === undefined && (!manager.cjsModule || !("exports" in manager.cjsModule))) {
                        ret = defined[fullName];
                    } else {
                        if (manager.cjsModule && "exports" in manager.cjsModule) {
                            ret = defined[fullName] = manager.cjsModule.exports;
                        } else {
                            if (fullName in defined && !manager.usingExports) {
                                return req.onError(new Error(fullName + " has already been defined"));
                            }
                            defined[fullName] = ret;
                        }
                    }
                }
            } else if (fullName) {
                //May just be an object definition for the module. Only
                //worry about defining if have a module name.
                ret = defined[fullName] = cb;
            }

            if (fullName) {
                //If anything was waiting for this module to be defined,
                //notify them now.
                waitingCallbacks = managerCallbacks[fullName];
                if (waitingCallbacks) {
                    for (i = 0; i < waitingCallbacks.length; i++) {
                        waitingCallbacks[i].onDep(fullName, ret);
                    }
                    delete managerCallbacks[fullName];
                }
            }

            //Clean up waiting.
            if (waiting[manager.waitId]) {
                delete waiting[manager.waitId];
                manager.isDone = true;
                context.waitCount -= 1;
                if (context.waitCount === 0) {
                    //Clear the wait array used for cycles.
                    waitAry = [];
                }
            }

            return undefined;
        }

        function main(inName, depArray, callback, relModuleMap) {
            var moduleMap = makeModuleMap(inName, relModuleMap),
                name = moduleMap.name,
                fullName = moduleMap.fullName,
                uniques = {},
                manager = {
                    //Use a wait ID because some entries are anon
                    //async require calls.
                    waitId: name || reqWaitIdPrefix + (waitIdCounter++),
                    depCount: 0,
                    depMax: 0,
                    prefix: moduleMap.prefix,
                    name: name,
                    fullName: fullName,
                    deps: {},
                    depArray: depArray,
                    callback: callback,
                    onDep: function (depName, value) {
                        if (!(depName in manager.deps)) {
                            manager.deps[depName] = value;
                            manager.depCount += 1;
                            if (manager.depCount === manager.depMax) {
                                //All done, execute!
                                execManager(manager);
                            }
                        }
                    }
                },
                i, depArg, depName, cjsMod;

            if (fullName) {
                //If module already defined for context, or already loaded,
                //then leave.
                if (fullName in defined || loaded[fullName] === true) {
                    return;
                }

                //Set specified/loaded here for modules that are also loaded
                //as part of a layer, where onScriptLoad is not fired
                //for those cases. Do this after the inline define and
                //dependency tracing is done.
                //Also check if auto-registry of jQuery needs to be skipped.
                specified[fullName] = true;
                loaded[fullName] = true;
                context.jQueryDef = (fullName === "jquery");
            }

            //Add the dependencies to the deps field, and register for callbacks
            //on the dependencies.
            for (i = 0; i < depArray.length; i++) {
                depArg = depArray[i];
                //There could be cases like in IE, where a trailing comma will
                //introduce a null dependency, so only treat a real dependency
                //value as a dependency.
                if (depArg) {
                    //Split the dependency name into plugin and name parts
                    depArg = makeModuleMap(depArg, (name ? moduleMap : relModuleMap));
                    depName = depArg.fullName;

                    //Fix the name in depArray to be just the name, since
                    //that is how it will be called back later.
                    depArray[i] = depName;

                    //Fast path CommonJS standard dependencies.
                    if (depName === "require") {
                        manager.deps[depName] = makeRequire(moduleMap);
                    } else if (depName === "exports") {
                        //CommonJS module spec 1.1
                        manager.deps[depName] = defined[fullName] = {};
                        manager.usingExports = true;
                    } else if (depName === "module") {
                        //CommonJS module spec 1.1
                        manager.cjsModule = cjsMod = manager.deps[depName] = {
                            id: name,
                            uri: name ? context.nameToUrl(name, null, relModuleMap) : undefined
                        };
                        cjsMod.setExports = makeSetExports(cjsMod);
                    } else if (depName in defined && !(depName in waiting)) {
                        //Module already defined, no need to wait for it.
                        manager.deps[depName] = defined[depName];
                    } else if (!uniques[depName]) {

                        //A dynamic dependency.
                        manager.depMax += 1;

                        queueDependency(depArg);

                        //Register to get notification when dependency loads.
                        (managerCallbacks[depName] ||
                        (managerCallbacks[depName] = [])).push(manager);

                        uniques[depName] = true;
                    }
                }
            }

            //Do not bother tracking the manager if it is all done.
            if (manager.depCount === manager.depMax) {
                //All done, execute!
                execManager(manager);
            } else {
                waiting[manager.waitId] = manager;
                waitAry.push(manager);
                context.waitCount += 1;
            }
        }

        /**
         * Convenience method to call main for a require.def call that was put on
         * hold in the defQueue.
         */
        function callDefMain(args) {
            main.apply(null, args);
            //Mark the module loaded. Must do it here in addition
            //to doing it in require.def in case a script does
            //not call require.def
            loaded[args[0]] = true;
        }

        /**
         * As of jQuery 1.4.3, it supports a readyWait property that will hold off
         * calling jQuery ready callbacks until all scripts are loaded. Be sure
         * to track it if readyWait is available. Also, since jQuery 1.4.3 does
         * not register as a module, need to do some global inference checking.
         * Even if it does register as a module, not guaranteed to be the precise
         * name of the global. If a jQuery is tracked for this context, then go
         * ahead and register it as a module too, if not already in process.
         */
        function jQueryCheck(jqCandidate) {
            if (!context.jQuery) {
                var $ = jqCandidate || (typeof jQuery !== "undefined" ? jQuery : null);
                if ($ && "readyWait" in $) {
                    context.jQuery = $;

                    //Manually create a "jquery" module entry if not one already
                    //or in process.
                    callDefMain(["jquery", [], function () {
                        return jQuery;
                    }]);

                    //Increment jQuery readyWait if ncecessary.
                    if (context.scriptCount) {
                        $.readyWait += 1;
                        context.jQueryIncremented = true;
                    }
                }
            }
        }

        function forceExec(manager, traced) {
            if (manager.isDone) {
                return undefined;
            }

            var fullName = manager.fullName,
                depArray = manager.depArray,
                depName, i;
            if (fullName) {
                if (traced[fullName]) {
                    return defined[fullName];
                }

                traced[fullName] = true;
            }

            //forceExec all of its dependencies.
            for (i = 0; i < depArray.length; i++) {
                //Some array members may be null, like if a trailing comma
                //IE, so do the explicit [i] access and check if it has a value.
                depName = depArray[i];
                if (depName) {
                    if (!manager.deps[depName] && waiting[depName]) {
                        manager.onDep(depName, forceExec(waiting[depName], traced));
                    }
                }
            }

            return fullName ? defined[fullName] : undefined;
        }

        /**
         * Checks if all modules for a context are loaded, and if so, evaluates the
         * new ones in right dependency order.
         *
         * @private
         */
        function checkLoaded() {
            var waitInterval = config.waitSeconds * 1000,
                //It is possible to disable the wait interval by using waitSeconds of 0.
                expired = waitInterval && (context.startTime + waitInterval) < new Date().getTime(),
                noLoads = "", hasLoadedProp = false, stillLoading = false, prop,
                err, manager;

            //If there are items still in the paused queue processing wait.
            //This is particularly important in the sync case where each paused
            //item is processed right away but there may be more waiting.
            if (context.pausedCount > 0) {
                return undefined;
            }

            //Determine if priority loading is done. If so clear the priority. If
            //not, then do not check
            if (config.priorityWait) {
                if (isPriorityDone()) {
                    //Call resume, since it could have
                    //some waiting dependencies to trace.
                    resume();
                } else {
                    return undefined;
                }
            }

            //See if anything is still in flight.
            for (prop in loaded) {
                if (!(prop in empty)) {
                    hasLoadedProp = true;
                    if (!loaded[prop]) {
                        if (expired) {
                            noLoads += prop + " ";
                        } else {
                            stillLoading = true;
                            break;
                        }
                    }
                }
            }

            //Check for exit conditions.
            if (!hasLoadedProp && !context.waitCount) {
                //If the loaded object had no items, then the api of
                //the work below does not need to be done.
                return undefined;
            }
            if (expired && noLoads) {
                //If wait time expired, throw error of unloaded modules.
                err = new Error("require.js load timeout for modules: " + noLoads);
                err.requireType = "timeout";
                err.requireModules = noLoads;
                return req.onError(err);
            }
            if (stillLoading || context.scriptCount) {
                //Something is still waiting to load. Wait for it.
                if (isBrowser || isWebWorker) {
                    setTimeout(checkLoaded, 50);
                }
                return undefined;
            }

            //If still have items in the waiting cue, but all modules have
            //been loaded, then it means there are some circular dependencies
            //that need to be broken.
            //However, as a waiting thing is fired, then it can add items to
            //the waiting cue, and those items should not be fired yet, so
            //make sure to redo the checkLoaded call after breaking a single
            //cycle, if nothing else loaded then this logic will pick it up
            //again.
            if (context.waitCount) {
                //Cycle through the waitAry, and call items in sequence.
                for (i = 0; (manager = waitAry[i]); i++) {
                    forceExec(manager, {});
                }

                checkLoaded();
                return undefined;
            }

            //Check for DOM ready, and nothing is waiting across contexts.
            req.checkReadyState();

            return undefined;
        }

        function callPlugin(pluginName, dep) {
            var name = dep.name,
                fullName = dep.fullName,
                load;

            //Do not bother if plugin is already defined or being loaded.
            if (fullName in defined || fullName in loaded) {
                return;
            }

            if (!plugins[pluginName]) {
                plugins[pluginName] = defined[pluginName];
            }

            //Only set loaded to false for tracking if it has not already been set.
            if (!loaded[fullName]) {
                loaded[fullName] = false;
            }

            load = function (ret) {
                //Allow the build process to register plugin-loaded dependencies.
                if (require.onPluginLoad) {
                    require.onPluginLoad(context, pluginName, name, ret);
                }

                execManager({
                    prefix: dep.prefix,
                    name: dep.name,
                    fullName: dep.fullName,
                    callback: function () {
                        return ret;
                    }
                });
                loaded[fullName] = true;
            };

            //Allow plugins to load other code without having to know the
            //context or how to "complete" the load.
            load.fromText = function (moduleName, text) {
                /*jslint evil: true */
                var hasInteractive = useInteractive;

                //Indicate a the module is in process of loading.
                context.loaded[moduleName] = false;
                context.scriptCount += 1;

                //Turn off interactive script matching for IE for any define
                //calls in the text, then turn it back on at the end.
                if (hasInteractive) {
                    useInteractive = false;
                }
                eval(text);
                if (hasInteractive) {
                    useInteractive = true;
                }

                //Support anonymous modules.
                context.completeLoad(moduleName);
            };

            //Use parentName here since the plugin's name is not reliable,
            //could be some weird string with no path that actually wants to
            //reference the parentName's path.
            plugins[pluginName].load(name, makeRequire(dep.parentMap, true), load, config);
        }

        function loadPaused(dep) {
            //Renormalize dependency if its name was waiting on a plugin
            //to load, which as since loaded.
            if (dep.prefix && dep.name.indexOf('__$p') === 0 && defined[dep.prefix]) {
                dep = makeModuleMap(dep.originalName, dep.parentMap);
            }

            var pluginName = dep.prefix,
                fullName = dep.fullName;

            //Do not bother if the dependency has already been specified.
            if (specified[fullName] || loaded[fullName]) {
                return;
            } else {
                specified[fullName] = true;
            }

            if (pluginName) {
                //If plugin not loaded, wait for it.
                //set up callback list. if no list, then register
                //managerCallback for that plugin.
                if (defined[pluginName]) {
                    callPlugin(pluginName, dep);
                } else {
                    if (!pluginsQueue[pluginName]) {
                        pluginsQueue[pluginName] = [];
                        (managerCallbacks[pluginName] ||
                        (managerCallbacks[pluginName] = [])).push({
                            onDep: function (name, value) {
                                if (name === pluginName) {
                                    var i, oldModuleMap, ary = pluginsQueue[pluginName];

                                    //Now update all queued plugin actions.
                                    for (i = 0; i < ary.length; i++) {
                                        oldModuleMap = ary[i];
                                        //Update the moduleMap since the
                                        //module name may be normalized
                                        //differently now.
                                        callPlugin(pluginName,
                                                   makeModuleMap(oldModuleMap.originalName, oldModuleMap.parentMap));
                                    }
                                    delete pluginsQueue[pluginName];
                                }
                            }
                        });
                    }
                    pluginsQueue[pluginName].push(dep);
                }
            } else {
                req.load(context, fullName, dep.url);
            }
        }

        /**
         * Resumes tracing of dependencies and then checks if everything is loaded.
         */
        resume = function () {
            var args, i, p;

            resumeDepth += 1;

            if (context.scriptCount <= 0) {
                //Synchronous envs will push the number below zero with the
                //decrement above, be sure to set it back to zero for good measure.
                //require() calls that also do not end up loading scripts could
                //push the number negative too.
                context.scriptCount = 0;
            }

            //Make sure any remaining defQueue items get properly processed.
            while (defQueue.length) {
                args = defQueue.shift();
                if (args[0] === null) {
                    return req.onError(new Error('Mismatched anonymous require.def modules'));
                } else {
                    callDefMain(args);
                }
            }

            //Skip the resume of paused dependencies
            //if current context is in priority wait.
            if (!config.priorityWait || isPriorityDone()) {
                while (context.paused.length) {
                    p = context.paused;
                    context.pausedCount += p.length;
                    //Reset paused list
                    context.paused = [];

                    for (i = 0; (args = p[i]); i++) {
                        loadPaused(args);
                    }
                    //Move the start time for timeout forward.
                    context.startTime = (new Date()).getTime();
                    context.pausedCount -= p.length;
                }
            }

            //Only check if loaded when resume depth is 1. It is likely that
            //it is only greater than 1 in sync environments where a factory
            //function also then calls the callback-style require. In those
            //cases, the checkLoaded should not occur until the resume
            //depth is back at the top level.
            if (resumeDepth === 1) {
                checkLoaded();
            }

            resumeDepth -= 1;

            return undefined;
        };

        //Define the context object. Many of these fields are on here
        //just to make debugging easier.
        context = {
            contextName: contextName,
            config: config,
            defQueue: defQueue,
            waiting: waiting,
            waitCount: 0,
            specified: specified,
            loaded: loaded,
            urlMap: urlMap,
            scriptCount: 0,
            urlFetched: {},
            defined: defined,
            paused: [],
            pausedCount: 0,
            plugins: plugins,
            managerCallbacks: managerCallbacks,
            makeModuleMap: makeModuleMap,
            normalize: normalize,
            /**
             * Set a configuration for the context.
             * @param {Object} cfg config object to integrate.
             */
            configure: function (cfg) {
                var paths, prop, packages, pkgs, packagePaths, requireWait;

                //Make sure the baseUrl ends in a slash.
                if (cfg.baseUrl) {
                    if (cfg.baseUrl.charAt(cfg.baseUrl.length - 1) !== "/") {
                        cfg.baseUrl += "/";
                    }
                }

                //Save off the paths and packages since they require special processing,
                //they are additive.
                paths = config.paths;
                packages = config.packages;
                pkgs = config.pkgs;

                //Mix in the config values, favoring the new values over
                //existing ones in context.config.
                mixin(config, cfg, true);

                //Adjust paths if necessary.
                if (cfg.paths) {
                    for (prop in cfg.paths) {
                        if (!(prop in empty)) {
                            paths[prop] = cfg.paths[prop];
                        }
                    }
                    config.paths = paths;
                }

                packagePaths = cfg.packagePaths;
                if (packagePaths || cfg.packages) {
                    //Convert packagePaths into a packages config.
                    if (packagePaths) {
                        for (prop in packagePaths) {
                            if (!(prop in empty)) {
                                configurePackageDir(pkgs, packagePaths[prop], prop);
                            }
                        }
                    }

                    //Adjust packages if necessary.
                    if (cfg.packages) {
                        configurePackageDir(pkgs, cfg.packages);
                    }

                    //Done with modifications, assing packages back to context config
                    config.pkgs = pkgs;
                }

                //If priority loading is in effect, trigger the loads now
                if (cfg.priority) {
                    //Hold on to requireWait value, and reset it after done
                    requireWait = context.requireWait;

                    //Allow tracing some require calls to allow the fetching
                    //of the priority config.
                    context.requireWait = false;

                    //But first, call resume to register any defined modules that may
                    //be in a data-main built file before the priority config
                    //call. Also grab any waiting define calls for this context.
                    context.takeGlobalQueue();
                    resume();

                    context.require(cfg.priority);

                    //Trigger a resume right away, for the case when
                    //the script with the priority load is done as part
                    //of a data-main call. In that case the normal resume
                    //call will not happen because the scriptCount will be
                    //at 1, since the script for data-main is being processed.
                    resume();

                    //Restore previous state.
                    context.requireWait = requireWait;
                    config.priorityWait = cfg.priority;
                }

                //If a deps array or a config callback is specified, then call
                //require with those args. This is useful when require is defined as a
                //config object before require.js is loaded.
                if (cfg.deps || cfg.callback) {
                    context.require(cfg.deps || [], cfg.callback);
                }

                //Set up ready callback, if asked. Useful when require is defined as a
                //config object before require.js is loaded.
                if (cfg.ready) {
                    req.ready(cfg.ready);
                }
            },

            isDefined: function (moduleName, relModuleMap) {
                return makeModuleMap(moduleName, relModuleMap).fullName in defined;
            },

            require: function (deps, callback, relModuleMap) {
                var moduleName, ret, moduleMap;
                if (typeof deps === "string") {
                    //Synchronous access to one module. If require.get is
                    //available (as in the Node adapter), prefer that.
                    //In this case deps is the moduleName and callback is
                    //the relModuleMap
                    if (req.get) {
                        return req.get(context, deps, callback);
                    }

                    //Just return the module wanted. In this scenario, the
                    //second arg (if passed) is just the relModuleMap.
                    moduleName = deps;
                    relModuleMap = callback;

                    //Normalize module name, if it contains . or ..
                    moduleMap = makeModuleMap(moduleName, relModuleMap);

                    ret = defined[moduleMap.fullName];
                    if (ret === undefined) {
                        return req.onError(new Error("require: module name '" +
                                    moduleMap.fullName +
                                    "' has not been loaded yet for context: " +
                                    contextName));
                    }
                    return ret;
                }

                main(null, deps, callback, relModuleMap);

                //If the require call does not trigger anything new to load,
                //then resume the dependency processing.
                if (!context.requireWait) {
                    while (!context.scriptCount && context.paused.length) {
                        resume();
                    }
                }
                return undefined;
            },

            /**
             * Internal method to transfer globalQueue items to this context's
             * defQueue.
             */
            takeGlobalQueue: function () {
                //Push all the globalDefQueue items into the context's defQueue
                if (globalDefQueue.length) {
                    //Array splice in the values since the context code has a
                    //local var ref to defQueue, so cannot just reassign the one
                    //on context.
                    apsp.apply(context.defQueue,
                               [context.defQueue.length - 1, 0].concat(globalDefQueue));
                    globalDefQueue = [];
                }
            },

            /**
             * Internal method used by environment adapters to complete a load event.
             * A load event could be a script load or just a load pass from a synchronous
             * load call.
             * @param {String} moduleName the name of the module to potentially complete.
             */
            completeLoad: function (moduleName) {
                var args;

                context.takeGlobalQueue();

                while (defQueue.length) {
                    args = defQueue.shift();

                    if (args[0] === null) {
                        args[0] = moduleName;
                        break;
                    } else if (args[0] === moduleName) {
                        //Found matching require.def call for this script!
                        break;
                    } else {
                        //Some other named require.def call, most likely the result
                        //of a build layer that included many require.def calls.
                        callDefMain(args);
                        args = null;
                    }
                }
                if (args) {
                    callDefMain(args);
                } else {
                    //A script that does not call define(), so just simulate
                    //the call for it. Special exception for jQuery dynamic load.
                    callDefMain([moduleName, [],
                                moduleName === "jquery" && typeof jQuery !== "undefined" ?
                                function () {
                                    return jQuery;
                                } : null]);
                }

                //Mark the script as loaded. Note that this can be different from a
                //moduleName that maps to a require.def call. This line is important
                //for traditional browser scripts.
                loaded[moduleName] = true;

                //If a global jQuery is defined, check for it. Need to do it here
                //instead of main() since stock jQuery does not register as
                //a module via define.
                jQueryCheck();

                //Doing this scriptCount decrement branching because sync envs
                //need to decrement after resume, otherwise it looks like
                //loading is complete after the first dependency is fetched.
                //For browsers, it works fine to decrement after, but it means
                //the checkLoaded setTimeout 50 ms cost is taken. To avoid
                //that cost, decrement beforehand.
                if (req.isAsync) {
                    context.scriptCount -= 1;
                }
                resume();
                if (!req.isAsync) {
                    context.scriptCount -= 1;
                }
            },

            /**
             * Converts a module name + .extension into an URL path.
             * *Requires* the use of a module name. It does not support using
             * plain URLs like nameToUrl.
             */
            toUrl: function (moduleNamePlusExt, relModuleMap) {
                var index = moduleNamePlusExt.lastIndexOf("."),
                    ext = null;

                if (index !== -1) {
                    ext = moduleNamePlusExt.substring(index, moduleNamePlusExt.length);
                    moduleNamePlusExt = moduleNamePlusExt.substring(0, index);
                }

                return context.nameToUrl(moduleNamePlusExt, ext, relModuleMap);
            },

            /**
             * Converts a module name to a file path. Supports cases where
             * moduleName may actually be just an URL.
             */
            nameToUrl: function (moduleName, ext, relModuleMap) {
                var paths, pkgs, pkg, pkgPath, syms, i, parentModule, url,
                    config = context.config;

                if (moduleName.indexOf("./") === 0 || moduleName.indexOf("../") === 0) {
                    //A relative ID, just map it relative to relModuleMap's url
                    syms = relModuleMap && relModuleMap.url ? relModuleMap.url.split('/') : [];
                    //Pop off the file name.
                    if (syms.length) {
                        syms.pop();
                    }
                    syms = syms.concat(moduleName.split('/'));
                    trimDots(syms);
                    url = syms.join('/') +
                          (ext ? ext :
                          (req.jsExtRegExp.test(moduleName) ? "" : ".js"));
                } else {

                    //Normalize module name if have a base relative module name to work from.
                    moduleName = normalize(moduleName, relModuleMap);

                    //If a colon is in the URL, it indicates a protocol is used and it is just
                    //an URL to a file, or if it starts with a slash or ends with .js, it is just a plain file.
                    //The slash is important for protocol-less URLs as well as full paths.
                    if (req.jsExtRegExp.test(moduleName)) {
                        //Just a plain path, not module name lookup, so just return it.
                        //Add extension if it is included. This is a bit wonky, only non-.js things pass
                        //an extension, this method probably needs to be reworked.
                        url = moduleName + (ext ? ext : "");
                    } else {
                        //A module that needs to be converted to a path.
                        paths = config.paths;
                        pkgs = config.pkgs;

                        syms = moduleName.split("/");
                        //For each module name segment, see if there is a path
                        //registered for it. Start with most specific name
                        //and work up from it.
                        for (i = syms.length; i > 0; i--) {
                            parentModule = syms.slice(0, i).join("/");
                            if (paths[parentModule]) {
                                syms.splice(0, i, paths[parentModule]);
                                break;
                            } else if ((pkg = pkgs[parentModule])) {
                                //If module name is just the package name, then looking
                                //for the main module.
                                if (moduleName === pkg.name) {
                                    pkgPath = pkg.location + '/' + pkg.main;
                                } else {
                                    pkgPath = pkg.location + '/' + pkg.lib;
                                }
                                syms.splice(0, i, pkgPath);
                                break;
                            }
                        }

                        //Join the path parts together, then figure out if baseUrl is needed.
                        url = syms.join("/") + (ext || ".js");
                        url = (url.charAt(0) === '/' || url.match(/^\w+:/) ? "" : config.baseUrl) + url;
                    }
                }

                return config.urlArgs ? url +
                                        ((url.indexOf('?') === -1 ? '?' : '&') +
                                         config.urlArgs) : url;
            }
        };

        //Make these visible on the context so can be called at the very
        //end of the file to bootstrap
        context.jQueryCheck = jQueryCheck;
        context.resume = resume;

        return context;
    }

    /**
     * Main entry point.
     *
     * If the only argument to require is a string, then the module that
     * is represented by that string is fetched for the appropriate context.
     *
     * If the first argument is an array, then it will be treated as an array
     * of dependency string names to fetch. An optional function callback can
     * be specified to execute when all of those dependencies are available.
     *
     * Make a local req variable to help Caja compliance (it assumes things
     * on a require that are not standardized), and to give a short
     * name for minification/local scope use.
     */
    req = require = function (deps, callback) {

        //Find the right context, use default
        var contextName = defContextName,
            context, config;

        // Determine if have config object in the call.
        if (!isArray(deps) && typeof deps !== "string") {
            // deps is a config object
            config = deps;
            if (isArray(callback)) {
                // Adjust args if there are dependencies
                deps = callback;
                callback = arguments[2];
            } else {
                deps = [];
            }
        }

        if (config && config.context) {
            contextName = config.context;
        }

        context = contexts[contextName] ||
                  (contexts[contextName] = newContext(contextName));

        if (config) {
            context.configure(config);
        }

        return context.require(deps, callback);
    };

    req.version = version;
    req.isArray = isArray;
    req.isFunction = isFunction;
    req.mixin = mixin;
    //Used to filter out dependencies that are already paths.
    req.jsExtRegExp = /^\/|:|\?|\.js$/;
    s = req.s = {
        contexts: contexts,
        //Stores a list of URLs that should not get async script tag treatment.
        skipAsync: {},
        isPageLoaded: !isBrowser,
        readyCalls: []
    };

    req.isAsync = req.isBrowser = isBrowser;
    if (isBrowser) {
        head = s.head = document.getElementsByTagName("head")[0];
        //If BASE tag is in play, using appendChild is a problem for IE6.
        //When that browser dies, this can be removed. Details in this jQuery bug:
        //http://dev.jquery.com/ticket/2709
        baseElement = document.getElementsByTagName("base")[0];
        if (baseElement) {
            head = s.head = baseElement.parentNode;
        }
    }

    /**
     * Any errors that require explicitly generates will be passed to this
     * function. Intercept/override it if you want custom error handling.
     * @param {Error} err the error object.
     */
    req.onError = function (err) {
        throw err;
    };

    /**
     * Does the request to load a module for the browser case.
     * Make this a separate function to allow other environments
     * to override it.
     *
     * @param {Object} context the require context to find state.
     * @param {String} moduleName the name of the module.
     * @param {Object} url the URL to the module.
     */
    req.load = function (context, moduleName, url) {
        var contextName = context.contextName,
            urlFetched = context.urlFetched,
            loaded = context.loaded;
        isDone = false;

        //Only set loaded to false for tracking if it has not already been set.
        if (!loaded[moduleName]) {
            loaded[moduleName] = false;
        }

        if (!urlFetched[url]) {
            context.scriptCount += 1;
            req.attach(url, contextName, moduleName);
            urlFetched[url] = true;

            //If tracking a jQuery, then make sure its readyWait
            //is incremented to prevent its ready callbacks from
            //triggering too soon.
            if (context.jQuery && !context.jQueryIncremented) {
                context.jQuery.readyWait += 1;
                context.jQueryIncremented = true;
            }
        }
    };

    function getInteractiveScript() {
        var scripts, i, script;
        if (interactiveScript && interactiveScript.readyState === 'interactive') {
            return interactiveScript;
        }

        scripts = document.getElementsByTagName('script');
        for (i = scripts.length - 1; i > -1 && (script = scripts[i]); i--) {
            if (script.readyState === 'interactive') {
                return (interactiveScript = script);
            }
        }

        return null;
    }

    /**
     * The function that handles definitions of modules. Differs from
     * require() in that a string for the module should be the first argument,
     * and the function to execute after dependencies are loaded should
     * return a value to define the module corresponding to the first argument's
     * name.
     */
    define = req.def = function (name, deps, callback) {
        var node, context;

        //Allow for anonymous functions
        if (typeof name !== 'string') {
            //Adjust args appropriately
            callback = deps;
            deps = name;
            name = null;
        }

        //This module may not have dependencies
        if (!req.isArray(deps)) {
            callback = deps;
            deps = [];
        }

        //If no name, and callback is a function, then figure out if it a
        //CommonJS thing with dependencies.
        if (!name && !deps.length && req.isFunction(callback)) {
            //Remove comments from the callback string,
            //look for require calls, and pull them into the dependencies,
            //but only if there are function args.
            if (callback.length) {
                callback
                    .toString()
                    .replace(commentRegExp, "")
                    .replace(cjsRequireRegExp, function (match, dep) {
                        deps.push(dep);
                    });

                //May be a CommonJS thing even without require calls, but still
                //could use exports, and such, so always add those as dependencies.
                //This is a bit wasteful for RequireJS modules that do not need
                //an exports or module object, but erring on side of safety.
                //REQUIRES the function to expect the CommonJS variables in the
                //order listed below.
                deps = ["require", "exports", "module"].concat(deps);
            }
        }

        //If in IE 6-8 and hit an anonymous define() call, do the interactive
        //work.
        if (useInteractive) {
            node = currentlyAddingScript || getInteractiveScript();
            if (!node) {
                return req.onError(new Error("ERROR: No matching script interactive for " + callback));
            }
            if (!name) {
                name = node.getAttribute("data-requiremodule");
            }
            context = contexts[node.getAttribute("data-requirecontext")];
        }

        //Always save off evaluating the def call until the script onload handler.
        //This allows multiple modules to be in a file without prematurely
        //tracing dependencies, and allows for anonymous module support,
        //where the module name is not known until the script onload event
        //occurs. If no context, use the global queue, and get it processed
        //in the onscript load callback.
        (context ? context.defQueue : globalDefQueue).push([name, deps, callback]);

        return undefined;
    };

    define.amd = {
        multiversion: true,
        plugins: true
    };

    /**
     * Executes a module callack function. Broken out as a separate function
     * solely to allow the build system to sequence the files in the built
     * layer in the right sequence.
     *
     * @private
     */
    req.execCb = function (name, callback, args) {
        return callback.apply(null, args);
    };

    /**
     * callback for script loads, used to check status of loading.
     *
     * @param {Event} evt the event from the browser for the script
     * that was loaded.
     *
     * @private
     */
    req.onScriptLoad = function (evt) {
        //Using currentTarget instead of target for Firefox 2.0's sake. Not
        //all old browsers will be supported, but this one was easy enough
        //to support and still makes sense.
        var node = evt.currentTarget || evt.srcElement, contextName, moduleName,
            context;

        if (evt.type === "load" || readyRegExp.test(node.readyState)) {
            //Reset interactive script so a script node is not held onto for
            //to long.
            interactiveScript = null;

            //Pull out the name of the module and the context.
            contextName = node.getAttribute("data-requirecontext");
            moduleName = node.getAttribute("data-requiremodule");
            context = contexts[contextName];

            contexts[contextName].completeLoad(moduleName);

            //Clean up script binding. Favor detachEvent because of IE9
            //issue, see attachEvent/addEventListener comment elsewhere
            //in this file.
            if (node.detachEvent && !isOpera) {
                //Probably IE. If not it will throw an error, which will be
                //useful to know.
                node.detachEvent("onreadystatechange", req.onScriptLoad);
            } else {
                node.removeEventListener("load", req.onScriptLoad, false);
            }
        }
    };

    /**
     * Attaches the script represented by the URL to the current
     * environment. Right now only supports browser loading,
     * but can be redefined in other environments to do the right thing.
     * @param {String} url the url of the script to attach.
     * @param {String} contextName the name of the context that wants the script.
     * @param {moduleName} the name of the module that is associated with the script.
     * @param {Function} [callback] optional callback, defaults to require.onScriptLoad
     * @param {String} [type] optional type, defaults to text/javascript
     */
    req.attach = function (url, contextName, moduleName, callback, type) {
        var node, loaded, context;
        if (isBrowser) {
            //In the browser so use a script tag
            callback = callback || req.onScriptLoad;
            node = document.createElement("script");
            node.type = type || "text/javascript";
            node.charset = "utf-8";
            //Use async so Gecko does not block on executing the script if something
            //like a long-polling comet tag is being run first. Gecko likes
            //to evaluate scripts in DOM order, even for dynamic scripts.
            //It will fetch them async, but only evaluate the contents in DOM
            //order, so a long-polling script tag can delay execution of scripts
            //after it. But telling Gecko we expect async gets us the behavior
            //we want -- execute it whenever it is finished downloading. Only
            //Helps Firefox 3.6+
            //Allow some URLs to not be fetched async. Mostly helps the order!
            //plugin
            node.async = !s.skipAsync[url];

            node.setAttribute("data-requirecontext", contextName);
            node.setAttribute("data-requiremodule", moduleName);

            //Set up load listener. Test attachEvent first because IE9 has
            //a subtle issue in its addEventListener and script onload firings
            //that do not match the behavior of all other browsers with
            //addEventListener support, which fire the onload event for a
            //script right after the script execution. See:
            //https://connect.microsoft.com/IE/feedback/details/648057/script-onload-event-is-not-fired-immediately-after-script-execution
            //UNFORTUNATELY Opera implements attachEvent but does not follow the script
            //script execution mode.
            if (node.attachEvent && !isOpera) {
                //Probably IE. IE (at least 6-8) do not fire
                //script onload right after executing the script, so
                //we cannot tie the anonymous require.def call to a name.
                //However, IE reports the script as being in "interactive"
                //readyState at the time of the require.def call.
                useInteractive = true;
                node.attachEvent("onreadystatechange", callback);
            } else {
                node.addEventListener("load", callback, false);
            }
            node.src = url;

            //For some cache cases in IE 6-8, the script executes before the end
            //of the appendChild execution, so to tie an anonymous require.def
            //call to the module name (which is stored on the node), hold on
            //to a reference to this node, but clear after the DOM insertion.
            currentlyAddingScript = node;
            if (baseElement) {
                head.insertBefore(node, baseElement);
            } else {
                head.appendChild(node);
            }
            currentlyAddingScript = null;
            return node;
        } else if (isWebWorker) {
            //In a web worker, use importScripts. This is not a very
            //efficient use of importScripts, importScripts will block until
            //its script is downloaded and evaluated. However, if web workers
            //are in play, the expectation that a build has been done so that
            //only one script needs to be loaded anyway. This may need to be
            //reevaluated if other use cases become common.
            context = contexts[contextName];
            loaded = context.loaded;
            loaded[moduleName] = false;

            importScripts(url);

            //Account for anonymous modules
            context.completeLoad(moduleName);
        }
        return null;
    };

    //Look for a data-main script attribute, which could also adjust the baseUrl.
    if (isBrowser) {
        //Figure out baseUrl. Get it from the script tag with require.js in it.
        scripts = document.getElementsByTagName("script");

        for (i = scripts.length - 1; i > -1 && (script = scripts[i]); i--) {
            //Set the "head" where we can append children by
            //using the script's parent.
            if (!head) {
                head = script.parentNode;
            }

            //Look for a data-main attribute to set main script for the page
            //to load. If it is there, the path to data main becomes the
            //baseUrl, if it is not already set.
            if ((dataMain = script.getAttribute('data-main'))) {
                if (!cfg.baseUrl) {
                    //Pull off the directory of data-main for use as the
                    //baseUrl.
                    src = dataMain.split('/');
                    mainScript = src.pop();
                    subPath = src.length ? src.join('/')  + '/' : './';

                    //Set final config.
                    cfg.baseUrl = subPath;
                    //Strip off any trailing .js since dataMain is now
                    //like a module name.
                    dataMain = mainScript.replace(jsSuffixRegExp, '');
                }

                //Put the data-main script in the files to load.
                cfg.deps = cfg.deps ? cfg.deps.concat(dataMain) : [dataMain];

                break;
            }
        }
    }

    //Set baseUrl based on config.
    s.baseUrl = cfg.baseUrl;

    //****** START page load functionality ****************
    /**
     * Sets the page as loaded and triggers check for all modules loaded.
     */
    req.pageLoaded = function () {
        if (!s.isPageLoaded) {
            s.isPageLoaded = true;
            if (scrollIntervalId) {
                clearInterval(scrollIntervalId);
            }

            //Part of a fix for FF < 3.6 where readyState was not set to
            //complete so libraries like jQuery that check for readyState
            //after page load where not getting initialized correctly.
            //Original approach suggested by Andrea Giammarchi:
            //http://webreflection.blogspot.com/2009/11/195-chars-to-help-lazy-loading.html
            //see other setReadyState reference for the api of the fix.
            if (setReadyState) {
                document.readyState = "complete";
            }

            req.callReady();
        }
    };

    //See if there is nothing waiting across contexts, and if not, trigger
    //callReady.
    req.checkReadyState = function () {
        var contexts = s.contexts, prop;
        for (prop in contexts) {
            if (!(prop in empty)) {
                if (contexts[prop].waitCount) {
                    return;
                }
            }
        }
        s.isDone = true;
        req.callReady();
    };

    /**
     * Internal function that calls back any ready functions. If you are
     * integrating RequireJS with another library without require.ready support,
     * you can define this method to call your page ready code instead.
     */
    req.callReady = function () {
        var callbacks = s.readyCalls, i, callback, contexts, context, prop;

        if (s.isPageLoaded && s.isDone) {
            if (callbacks.length) {
                s.readyCalls = [];
                for (i = 0; (callback = callbacks[i]); i++) {
                    callback();
                }
            }

            //If jQuery with readyWait is being tracked, updated its
            //readyWait count.
            contexts = s.contexts;
            for (prop in contexts) {
                if (!(prop in empty)) {
                    context = contexts[prop];
                    if (context.jQueryIncremented) {
                        context.jQuery.ready(true);
                        context.jQueryIncremented = false;
                    }
                }
            }
        }
    };

    /**
     * Registers functions to call when the page is loaded
     */
    req.ready = function (callback) {
        if (s.isPageLoaded && s.isDone) {
            callback();
        } else {
            s.readyCalls.push(callback);
        }
        return req;
    };

    if (isBrowser) {
        if (document.addEventListener) {
            //Standards. Hooray! Assumption here that if standards based,
            //it knows about DOMContentLoaded.
            document.addEventListener("DOMContentLoaded", req.pageLoaded, false);
            window.addEventListener("load", req.pageLoaded, false);
            //Part of FF < 3.6 readystate fix (see setReadyState refs for more info)
            if (!document.readyState) {
                setReadyState = true;
                document.readyState = "loading";
            }
        } else if (window.attachEvent) {
            window.attachEvent("onload", req.pageLoaded);

            //DOMContentLoaded approximation, as found by Diego Perini:
            //http://javascript.nwbox.com/IEContentLoaded/
            if (self === self.top) {
                scrollIntervalId = setInterval(function () {
                    try {
                        //From this ticket:
                        //http://bugs.dojotoolkit.org/ticket/11106,
                        //In IE HTML Application (HTA), such as in a selenium test,
                        //javascript in the iframe can't see anything outside
                        //of it, so self===self.top is true, but the iframe is
                        //not the top window and doScroll will be available
                        //before document.body is set. Test document.body
                        //before trying the doScroll trick.
                        if (document.body) {
                            document.documentElement.doScroll("left");
                            req.pageLoaded();
                        }
                    } catch (e) {}
                }, 30);
            }
        }

        //Check if document already complete, and if so, just trigger page load
        //listeners. NOTE: does not work with Firefox before 3.6. To support
        //those browsers, manually call require.pageLoaded().
        if (document.readyState === "complete") {
            req.pageLoaded();
        }
    }
    //****** END page load functionality ****************

    //Set up default context. If require was a configuration object, use that as base config.
    req(cfg);

    //If modules are built into require.js, then need to make sure dependencies are
    //traced. Use a setTimeout in the browser world, to allow all the modules to register
    //themselves. In a non-browser env, assume that modules are not built into require.js,
    //which seems odd to do on the server.
    if (req.isAsync && typeof setTimeout !== "undefined") {
        ctx = s.contexts[(cfg.context || defContextName)];
        //Indicate that the script that includes require() is still loading,
        //so that require()'d dependencies are not traced until the end of the
        //file is parsed (approximated via the setTimeout call).
        ctx.requireWait = true;
        setTimeout(function () {
            ctx.requireWait = false;

            //Any modules included with the require.js file will be in the
            //global queue, assign them to this context.
            ctx.takeGlobalQueue();

            //Allow for jQuery to be loaded/already in the page, and if jQuery 1.4.3,
            //make sure to hold onto it for readyWait triggering.
            ctx.jQueryCheck();

            if (!ctx.scriptCount) {
                ctx.resume();
            }
            req.checkReadyState();
        }, 0);
    }
}());
/* =========================================================
 * bootstrap-modal.js v1.3.0
 * http://twitter.github.com/bootstrap/javascript.html#modal
 * =========================================================
 * Copyright 2011 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================= */


(function( $ ){

 /* CSS TRANSITION SUPPORT (https://gist.github.com/373874)
  * ======================================================= */

  var transitionEnd

  $(document).ready(function () {

    $.support.transition = (function () {
      var thisBody = document.body || document.documentElement
        , thisStyle = thisBody.style
        , support = thisStyle.transition !== undefined || thisStyle.WebkitTransition !== undefined || thisStyle.MozTransition !== undefined || thisStyle.MsTransition !== undefined || thisStyle.OTransition !== undefined
      return support
    })()

    // set CSS transition event type
    if ( $.support.transition ) {
      transitionEnd = "TransitionEnd"
      if ( $.browser.webkit ) {
      	transitionEnd = "webkitTransitionEnd"
      } else if ( $.browser.mozilla ) {
      	transitionEnd = "transitionend"
      } else if ( $.browser.opera ) {
      	transitionEnd = "oTransitionEnd"
      }
    }

  })


 /* MODAL PUBLIC CLASS DEFINITION
  * ============================= */

  var Modal = function ( content, options ) {
    this.settings = $.extend({}, $.fn.modal.defaults)
    this.$element = $(content)
      .delegate('.close', 'click.modal', $.proxy(this.hide, this))

    if ( options ) {
      $.extend( this.settings, options )

      if ( options.show ) {
        this.show()
      }
    }

    return this
  }

  Modal.prototype = {

      toggle: function () {
        return this[!this.isShown ? 'show' : 'hide']()
      }

    , show: function () {
        var that = this
        this.isShown = true
        this.$element.trigger('show')

        escape.call(this)
        backdrop.call(this, function () {
          that.$element
            .appendTo(document.body)
            .show()

          setTimeout(function () {
            that.$element
              .addClass('in')
              .trigger('shown')
          }, 1)
        })

        return this
      }

    , hide: function (e) {
        e && e.preventDefault()

        var that = this
        this.isShown = false

        escape.call(this)

        this.$element
          .trigger('hide')
          .removeClass('in')

        function removeElement () {
          that.$element
            .hide()
            .trigger('hidden')

          backdrop.call(that)
        }

        $.support.transition && this.$element.hasClass('fade') ?
          this.$element.one(transitionEnd, removeElement) :
          removeElement()

        return this
      }

  }


 /* MODAL PRIVATE METHODS
  * ===================== */

  function backdrop ( callback ) {
    var that = this
      , animate = this.$element.hasClass('fade') ? 'fade' : ''
    if ( this.isShown && this.settings.backdrop ) {
      this.$backdrop = $('<div class="modal-backdrop ' + animate + '" />')
        .click($.proxy(this.hide, this))
        .appendTo(document.body)

      setTimeout(function () {
        that.$backdrop && that.$backdrop.addClass('in')
        $.support.transition && that.$backdrop.hasClass('fade') ?
          that.$backdrop.one(transitionEnd, callback) :
          callback()
      })
    } else if ( !this.isShown && this.$backdrop ) {
      this.$backdrop.removeClass('in')

      function removeElement() {
        that.$backdrop.remove()
        that.$backdrop = null
      }

      $.support.transition && this.$element.hasClass('fade')?
        this.$backdrop.one(transitionEnd, removeElement) :
        removeElement()
    } else if ( callback ) {
       callback()
    }
  }

  function escape() {
    var that = this
    if ( this.isShown && this.settings.keyboard ) {
      $('body').bind('keyup.modal', function ( e ) {
        if ( e.which == 27 ) {
          that.hide()
        }
      })
    } else if ( !this.isShown ) {
      $('body').unbind('keyup.modal')
    }
  }


 /* MODAL PLUGIN DEFINITION
  * ======================= */

  $.fn.modal = function ( options ) {
    var modal = this.data('modal')

    if (!modal) {

      if (typeof options == 'string') {
        options = {
          show: /show|toggle/.test(options)
        }
      }

      return this.each(function () {
        $(this).data('modal', new Modal(this, options))
      })
    }

    if ( options === true ) {
      return modal
    }

    if ( typeof options == 'string' ) {
      modal[options]()
    } else if ( modal ) {
      modal.toggle()
    }

    return this
  }

  $.fn.modal.Modal = Modal

  $.fn.modal.defaults = {
    backdrop: false
  , keyboard: false
  , show: true
  }


 /* MODAL DATA- IMPLEMENTATION
  * ========================== */

  $(document).ready(function () {
    $('body').delegate('[data-controls-modal]', 'click', function (e) {
      e.preventDefault()
      var $this = $(this).data('show', true)
      $('#' + $this.attr('data-controls-modal')).modal( $this.data() )
    })
  })

})( window.jQuery || window.ender );/* ============================================================
 * bootstrap-dropdown.js v1.3.0
 * http://twitter.github.com/bootstrap/javascript.html#dropdown
 * ============================================================
 * Copyright 2011 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============================================================ */


(function( $ ){

  var d = 'a.menu, .dropdown-toggle'

  function clearMenus() {
    $(d).parent('li').removeClass('open')
  }

  $(function () {
    $('html').bind("click", clearMenus)
    $('body').dropdown( '[data-dropdown] a.menu, [data-dropdown] .dropdown-toggle' )
  })

  /* DROPDOWN PLUGIN DEFINITION
   * ========================== */

  $.fn.dropdown = function ( selector ) {
    return this.each(function () {
      $(this).delegate(selector || d, 'click', function (e) {
        var li = $(this).parent('li')
          , isActive = li.hasClass('open')

        clearMenus()
        !isActive && li.toggleClass('open')
        return false
      })
    })
  }

})( window.jQuery || window.ender );/* ==========================================================
 * bootstrap-twipsy.js v1.3.0
 * http://twitter.github.com/bootstrap/javascript.html#twipsy
 * Adapted from the original jQuery.tipsy by Jason Frame
 * ==========================================================
 * Copyright 2011 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================== */


(function( $ ) {

 /* CSS TRANSITION SUPPORT (https://gist.github.com/373874)
  * ======================================================= */

  var transitionEnd

  $(document).ready(function () {

    $.support.transition = (function () {
      var thisBody = document.body || document.documentElement
        , thisStyle = thisBody.style
        , support = thisStyle.transition !== undefined || thisStyle.WebkitTransition !== undefined || thisStyle.MozTransition !== undefined || thisStyle.MsTransition !== undefined || thisStyle.OTransition !== undefined
      return support
    })()

    // set CSS transition event type
    if ( $.support.transition ) {
      transitionEnd = "TransitionEnd"
      if ( $.browser.webkit ) {
      	transitionEnd = "webkitTransitionEnd"
      } else if ( $.browser.mozilla ) {
      	transitionEnd = "transitionend"
      } else if ( $.browser.opera ) {
      	transitionEnd = "oTransitionEnd"
      }
    }

  })


 /* TWIPSY PUBLIC CLASS DEFINITION
  * ============================== */

  var Twipsy = function ( element, options ) {
    this.$element = $(element)
    this.options = options
    this.enabled = true
    this.fixTitle()
  }

  Twipsy.prototype = {

    show: function() {
      var pos
        , actualWidth
        , actualHeight
        , placement
        , $tip
        , tp

      if (this.getTitle() && this.enabled) {
        $tip = this.tip()
        this.setContent()

        if (this.options.animate) {
          $tip.addClass('fade')
        }

        $tip
          .remove()
          .css({ top: 0, left: 0, display: 'block' })
          .prependTo(document.body)

        pos = $.extend({}, this.$element.offset(), {
          width: this.$element[0].offsetWidth
        , height: this.$element[0].offsetHeight
        })

        actualWidth = $tip[0].offsetWidth
        actualHeight = $tip[0].offsetHeight
        placement = _.maybeCall(this.options.placement, this.$element[0])

        switch (placement) {
          case 'below':
            tp = {top: pos.top + pos.height + this.options.offset, left: pos.left + pos.width / 2 - actualWidth / 2}
            break
          case 'above':
            tp = {top: pos.top - actualHeight - this.options.offset, left: pos.left + pos.width / 2 - actualWidth / 2}
            break
          case 'left':
            tp = {top: pos.top + pos.height / 2 - actualHeight / 2, left: pos.left - actualWidth - this.options.offset}
            break
          case 'right':
            tp = {top: pos.top + pos.height / 2 - actualHeight / 2, left: pos.left + pos.width + this.options.offset}
            break
        }

        $tip
          .css(tp)
          .addClass(placement)
          .addClass('in')
      }
    }

  , setContent: function () {
      var $tip = this.tip()
      $tip.find('.twipsy-inner')[this.options.html ? 'html' : 'text'](this.getTitle())
      $tip[0].className = 'twipsy'
    }

  , hide: function() {
      var that = this
        , $tip = this.tip()

      $tip.removeClass('in')

      function removeElement () {
        $tip.remove()
      }

      $.support.transition && this.$tip.hasClass('fade') ?
        $tip.bind(transitionEnd, removeElement) :
        removeElement()
    }

  , fixTitle: function() {
      var $e = this.$element
      if ($e.attr('title') || typeof($e.attr('data-original-title')) != 'string') {
        $e.attr('data-original-title', $e.attr('title') || '').removeAttr('title')
      }
    }

  , getTitle: function() {
      var title
        , $e = this.$element
        , o = this.options

        this.fixTitle()

        if (typeof o.title == 'string') {
          title = $e.attr(o.title == 'title' ? 'data-original-title' : o.title)
        } else if (typeof o.title == 'function') {
          title = o.title.call($e[0])
        }

        title = ('' + title).replace(/(^\s*|\s*$)/, "")

        return title || o.fallback
    }

  , tip: function() {
      if (!this.$tip) {
        this.$tip = $('<div class="twipsy" />').html('<div class="twipsy-arrow"></div><div class="twipsy-inner"></div>')
      }
      return this.$tip
    }

  , validate: function() {
      if (!this.$element[0].parentNode) {
        this.hide()
        this.$element = null
        this.options = null
      }
    }

  , enable: function() {
      this.enabled = true
    }

  , disable: function() {
      this.enabled = false
    }

  , toggleEnabled: function() {
      this.enabled = !this.enabled
    }

  }


 /* TWIPSY PRIVATE METHODS
  * ====================== */

   var _ = {

     maybeCall: function ( thing, ctx ) {
       return (typeof thing == 'function') ? (thing.call(ctx)) : thing
     }

   }


 /* TWIPSY PLUGIN DEFINITION
  * ======================== */

  $.fn.twipsy = function (options) {
    $.fn.twipsy.initWith.call(this, options, Twipsy, 'twipsy')
    return this
  }

  $.fn.twipsy.initWith = function (options, Constructor, name) {
    var twipsy
      , binder
      , eventIn
      , eventOut

    if (options === true) {
      return this.data(name)
    } else if (typeof options == 'string') {
      twipsy = this.data(name)
      if (twipsy) {
        twipsy[options]()
      }
      return this
    }

    options = $.extend({}, $.fn[name].defaults, options)

    function get(ele) {
      var twipsy = $.data(ele, name)

      if (!twipsy) {
        twipsy = new Constructor(ele, $.fn.twipsy.elementOptions(ele, options))
        $.data(ele, name, twipsy)
      }

      return twipsy
    }

    function enter() {
      var twipsy = get(this)
      twipsy.hoverState = 'in'

      if (options.delayIn == 0) {
        twipsy.show()
      } else {
        twipsy.fixTitle()
        setTimeout(function() {
          if (twipsy.hoverState == 'in') {
            twipsy.show()
          }
        }, options.delayIn)
      }
    }

    function leave() {
      var twipsy = get(this)
      twipsy.hoverState = 'out'
      if (options.delayOut == 0) {
        twipsy.hide()
      } else {
        setTimeout(function() {
          if (twipsy.hoverState == 'out') {
            twipsy.hide()
          }
        }, options.delayOut)
      }
    }

    if (!options.live) {
      this.each(function() {
        get(this)
      })
    }

    if (options.trigger != 'manual') {
      binder   = options.live ? 'live' : 'bind'
      eventIn  = options.trigger == 'hover' ? 'mouseenter' : 'focus'
      eventOut = options.trigger == 'hover' ? 'mouseleave' : 'blur'
      this[binder](eventIn, enter)[binder](eventOut, leave)
    }

    return this
  }

  $.fn.twipsy.Twipsy = Twipsy

  $.fn.twipsy.defaults = {
    animate: true
  , delayIn: 0
  , delayOut: 0
  , fallback: ''
  , placement: 'above'
  , html: false
  , live: false
  , offset: 0
  , title: 'title'
  , trigger: 'hover'
  }

  $.fn.twipsy.elementOptions = function(ele, options) {
    return $.metadata ? $.extend({}, options, $(ele).metadata()) : options
  }

})( window.jQuery || window.ender );/* ===========================================================
 * bootstrap-popover.js v1.3.0
 * http://twitter.github.com/bootstrap/javascript.html#popover
 * ===========================================================
 * Copyright 2011 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================================================== */


(function( $ ) {

  var Popover = function ( element, options ) {
    this.$element = $(element)
    this.options = options
    this.enabled = true
    this.fixTitle()
  }

  /* NOTE: POPOVER EXTENDS BOOTSTRAP-TWIPSY.js
     ========================================= */

  Popover.prototype = $.extend({}, $.fn.twipsy.Twipsy.prototype, {

    setContent: function () {
      var $tip = this.tip()
      $tip.find('.title')[this.options.html ? 'html' : 'text'](this.getTitle())
      $tip.find('.content p')[this.options.html ? 'html' : 'text'](this.getContent())
      $tip[0].className = 'popover'
    }

  , getContent: function () {
      var contentvar
       , $e = this.$element
       , o = this.options

      if (typeof this.options.content == 'string') {
        content = $e.attr(o.content)
      } else if (typeof this.options.content == 'function') {
        content = this.options.content.call(this.$element[0])
      }
      return content
    }

  , tip: function() {
      if (!this.$tip) {
        this.$tip = $('<div class="popover" />')
          .html('<div class="arrow"></div><div class="inner"><h3 class="title"></h3><div class="content"><p></p></div></div>')
      }
      return this.$tip
    }

  })


 /* POPOVER PLUGIN DEFINITION
  * ======================= */

  $.fn.popover = function (options) {
    if (typeof options == 'object') options = $.extend({}, $.fn.popover.defaults, options)
    $.fn.twipsy.initWith.call(this, options, Popover, 'popover')
    return this
  }

  $.fn.popover.defaults = $.extend({} , $.fn.twipsy.defaults, { content: 'data-content', placement: 'right'})

})( window.jQuery || window.ender );/* ==========================================================
 * bootstrap-alerts.js v1.3.0
 * http://twitter.github.com/bootstrap/javascript.html#alerts
 * ==========================================================
 * Copyright 2011 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================== */


(function( $ ){

  /* CSS TRANSITION SUPPORT (https://gist.github.com/373874)
   * ======================================================= */

   var transitionEnd

   $(document).ready(function () {

     $.support.transition = (function () {
       var thisBody = document.body || document.documentElement
         , thisStyle = thisBody.style
         , support = thisStyle.transition !== undefined || thisStyle.WebkitTransition !== undefined || thisStyle.MozTransition !== undefined || thisStyle.MsTransition !== undefined || thisStyle.OTransition !== undefined
       return support
     })()

     // set CSS transition event type
     if ( $.support.transition ) {
       transitionEnd = "TransitionEnd"
       if ( $.browser.webkit ) {
       	transitionEnd = "webkitTransitionEnd"
       } else if ( $.browser.mozilla ) {
       	transitionEnd = "transitionend"
       } else if ( $.browser.opera ) {
       	transitionEnd = "oTransitionEnd"
       }
     }

   })

 /* ALERT CLASS DEFINITION
  * ====================== */

  var Alert = function ( content, selector ) {
    this.$element = $(content)
      .delegate(selector || '.close', 'click', this.close)
  }

  Alert.prototype = {

    close: function (e) {
      var $element = $(this).parent('.alert-message')

      e && e.preventDefault()
      $element.removeClass('in')

      function removeElement () {
        $element.remove()
      }

      $.support.transition && $element.hasClass('fade') ?
        $element.bind(transitionEnd, removeElement) :
        removeElement()
    }

  }


 /* ALERT PLUGIN DEFINITION
  * ======================= */

  $.fn.alert = function ( options ) {

    if ( options === true ) {
      return this.data('alert')
    }

    return this.each(function () {
      var $this = $(this)

      if ( typeof options == 'string' ) {
        return $this.data('alert')[options]()
      }

      $(this).data('alert', new Alert( this ))

    })
  }

  $(document).ready(function () {
    new Alert($('body'), '.alert-message[data-alert] .close')
  })

})( window.jQuery || window.ender );//     Underscore.js 1.1.6
//     (c) 2011 Jeremy Ashkenas, DocumentCloud Inc.
//     Underscore is freely distributable under the MIT license.
//     Portions of Underscore are inspired or borrowed from Prototype,
//     Oliver Steele's Functional, and John Resig's Micro-Templating.
//     For all details and documentation:
//     http://documentcloud.github.com/underscore

(function() {

  // Baseline setup
  // --------------

  // Establish the root object, `window` in the browser, or `global` on the server.
  var root = this;

  // Save the previous value of the `_` variable.
  var previousUnderscore = root._;

  // Establish the object that gets returned to break out of a loop iteration.
  var breaker = {};

  // Save bytes in the minified (but not gzipped) version:
  var ArrayProto = Array.prototype, ObjProto = Object.prototype, FuncProto = Function.prototype;

  // Create quick reference variables for speed access to core prototypes.
  var slice            = ArrayProto.slice,
      unshift          = ArrayProto.unshift,
      toString         = ObjProto.toString,
      hasOwnProperty   = ObjProto.hasOwnProperty;

  // All **ECMAScript 5** native function implementations that we hope to use
  // are declared here.
  var
    nativeForEach      = ArrayProto.forEach,
    nativeMap          = ArrayProto.map,
    nativeReduce       = ArrayProto.reduce,
    nativeReduceRight  = ArrayProto.reduceRight,
    nativeFilter       = ArrayProto.filter,
    nativeEvery        = ArrayProto.every,
    nativeSome         = ArrayProto.some,
    nativeIndexOf      = ArrayProto.indexOf,
    nativeLastIndexOf  = ArrayProto.lastIndexOf,
    nativeIsArray      = Array.isArray,
    nativeKeys         = Object.keys,
    nativeBind         = FuncProto.bind;

  // Create a safe reference to the Underscore object for use below.
  var _ = function(obj) { return new wrapper(obj); };

  // Export the Underscore object for **CommonJS**, with backwards-compatibility
  // for the old `require()` API. If we're not in CommonJS, add `_` to the
  // global object.
  if (typeof module !== 'undefined' && module.exports) {
    module.exports = _;
    _._ = _;
  } else {
    root._ = _;
  }

  // Current version.
  _.VERSION = '1.1.6';

  // Collection Functions
  // --------------------

  // The cornerstone, an `each` implementation, aka `forEach`.
  // Handles objects implementing `forEach`, arrays, and raw objects.
  // Delegates to **ECMAScript 5**'s native `forEach` if available.
  var each = _.each = _.forEach = function(obj, iterator, context) {
    if (obj == null) return;
    if (nativeForEach && obj.forEach === nativeForEach) {
      obj.forEach(iterator, context);
    } else if (_.isNumber(obj.length)) {
      for (var i = 0, l = obj.length; i < l; i++) {
        if (iterator.call(context, obj[i], i, obj) === breaker) return;
      }
    } else {
      for (var key in obj) {
        if (hasOwnProperty.call(obj, key)) {
          if (iterator.call(context, obj[key], key, obj) === breaker) return;
        }
      }
    }
  };

  // Return the results of applying the iterator to each element.
  // Delegates to **ECMAScript 5**'s native `map` if available.
  _.map = function(obj, iterator, context) {
    var results = [];
    if (obj == null) return results;
    if (nativeMap && obj.map === nativeMap) return obj.map(iterator, context);
    each(obj, function(value, index, list) {
      results[results.length] = iterator.call(context, value, index, list);
    });
    return results;
  };

  // **Reduce** builds up a single result from a list of values, aka `inject`,
  // or `foldl`. Delegates to **ECMAScript 5**'s native `reduce` if available.
  _.reduce = _.foldl = _.inject = function(obj, iterator, memo, context) {
    var initial = memo !== void 0;
    if (obj == null) obj = [];
    if (nativeReduce && obj.reduce === nativeReduce) {
      if (context) iterator = _.bind(iterator, context);
      return initial ? obj.reduce(iterator, memo) : obj.reduce(iterator);
    }
    each(obj, function(value, index, list) {
      if (!initial && index === 0) {
        memo = value;
        initial = true;
      } else {
        memo = iterator.call(context, memo, value, index, list);
      }
    });
    if (!initial) throw new TypeError("Reduce of empty array with no initial value");
    return memo;
  };

  // The right-associative version of reduce, also known as `foldr`.
  // Delegates to **ECMAScript 5**'s native `reduceRight` if available.
  _.reduceRight = _.foldr = function(obj, iterator, memo, context) {
    if (obj == null) obj = [];
    if (nativeReduceRight && obj.reduceRight === nativeReduceRight) {
      if (context) iterator = _.bind(iterator, context);
      return memo !== void 0 ? obj.reduceRight(iterator, memo) : obj.reduceRight(iterator);
    }
    var reversed = (_.isArray(obj) ? obj.slice() : _.toArray(obj)).reverse();
    return _.reduce(reversed, iterator, memo, context);
  };

  // Return the first value which passes a truth test. Aliased as `detect`.
  _.find = _.detect = function(obj, iterator, context) {
    var result;
    any(obj, function(value, index, list) {
      if (iterator.call(context, value, index, list)) {
        result = value;
        return true;
      }
    });
    return result;
  };

  // Return all the elements that pass a truth test.
  // Delegates to **ECMAScript 5**'s native `filter` if available.
  // Aliased as `select`.
  _.filter = _.select = function(obj, iterator, context) {
    var results = [];
    if (obj == null) return results;
    if (nativeFilter && obj.filter === nativeFilter) return obj.filter(iterator, context);
    each(obj, function(value, index, list) {
      if (iterator.call(context, value, index, list)) results[results.length] = value;
    });
    return results;
  };

  // Return all the elements for which a truth test fails.
  _.reject = function(obj, iterator, context) {
    var results = [];
    if (obj == null) return results;
    each(obj, function(value, index, list) {
      if (!iterator.call(context, value, index, list)) results[results.length] = value;
    });
    return results;
  };

  // Determine whether all of the elements match a truth test.
  // Delegates to **ECMAScript 5**'s native `every` if available.
  // Aliased as `all`.
  _.every = _.all = function(obj, iterator, context) {
    var result = true;
    if (obj == null) return result;
    if (nativeEvery && obj.every === nativeEvery) return obj.every(iterator, context);
    each(obj, function(value, index, list) {
      if (!(result = result && iterator.call(context, value, index, list))) return breaker;
    });
    return result;
  };

  // Determine if at least one element in the object matches a truth test.
  // Delegates to **ECMAScript 5**'s native `some` if available.
  // Aliased as `any`.
  var any = _.some = _.any = function(obj, iterator, context) {
    iterator || (iterator = _.identity);
    var result = false;
    if (obj == null) return result;
    if (nativeSome && obj.some === nativeSome) return obj.some(iterator, context);
    each(obj, function(value, index, list) {
      if (result = iterator.call(context, value, index, list)) return breaker;
    });
    return result;
  };

  // Determine if a given value is included in the array or object using `===`.
  // Aliased as `contains`.
  _.include = _.contains = function(obj, target) {
    var found = false;
    if (obj == null) return found;
    if (nativeIndexOf && obj.indexOf === nativeIndexOf) return obj.indexOf(target) != -1;
    any(obj, function(value) {
      if (found = value === target) return true;
    });
    return found;
  };

  // Invoke a method (with arguments) on every item in a collection.
  _.invoke = function(obj, method) {
    var args = slice.call(arguments, 2);
    return _.map(obj, function(value) {
      return (method.call ? method || value : value[method]).apply(value, args);
    });
  };

  // Convenience version of a common use case of `map`: fetching a property.
  _.pluck = function(obj, key) {
    return _.map(obj, function(value){ return value[key]; });
  };

  // Return the maximum element or (element-based computation).
  _.max = function(obj, iterator, context) {
    if (!iterator && _.isArray(obj)) return Math.max.apply(Math, obj);
    var result = {computed : -Infinity};
    each(obj, function(value, index, list) {
      var computed = iterator ? iterator.call(context, value, index, list) : value;
      computed >= result.computed && (result = {value : value, computed : computed});
    });
    return result.value;
  };

  // Return the minimum element (or element-based computation).
  _.min = function(obj, iterator, context) {
    if (!iterator && _.isArray(obj)) return Math.min.apply(Math, obj);
    var result = {computed : Infinity};
    each(obj, function(value, index, list) {
      var computed = iterator ? iterator.call(context, value, index, list) : value;
      computed < result.computed && (result = {value : value, computed : computed});
    });
    return result.value;
  };

  // Sort the object's values by a criterion produced by an iterator.
  _.sortBy = function(obj, iterator, context) {
    return _.pluck(_.map(obj, function(value, index, list) {
      return {
        value : value,
        criteria : iterator.call(context, value, index, list)
      };
    }).sort(function(left, right) {
      var a = left.criteria, b = right.criteria;
      return a < b ? -1 : a > b ? 1 : 0;
    }), 'value');
  };
  
  // Groups the object's values by a criterion produced by an iterator
  _.groupBy = function(obj, iterator) {
    var result = {};
    each(obj, function(value, index) {
      var key = iterator(value, index);
      (result[key] || (result[key] = [])).push(value)
    });
    return result;
  }

  // Use a comparator function to figure out at what index an object should
  // be inserted so as to maintain order. Uses binary search.
  _.sortedIndex = function(array, obj, iterator) {
    iterator || (iterator = _.identity);
    var low = 0, high = array.length;
    while (low < high) {
      var mid = (low + high) >> 1;
      iterator(array[mid]) < iterator(obj) ? low = mid + 1 : high = mid;
    }
    return low;
  };

  // Safely convert anything iterable into a real, live array.
  _.toArray = function(iterable) {
    if (!iterable)                return [];
    if (iterable.toArray)         return iterable.toArray();
    if (_.isArray(iterable))      return iterable;
    if (_.isArguments(iterable))  return slice.call(iterable);
    return _.values(iterable);
  };

  // Return the number of elements in an object.
  _.size = function(obj) {
    return _.toArray(obj).length;
  };

  // Array Functions
  // ---------------

  // Get the first element of an array. Passing **n** will return the first N
  // values in the array. Aliased as `head`. The **guard** check allows it to work
  // with `_.map`.
  _.first = _.head = function(array, n, guard) {
    return (n != null) && !guard ? slice.call(array, 0, n) : array[0];
  };

  // Returns everything but the first entry of the array. Aliased as `tail`.
  // Especially useful on the arguments object. Passing an **index** will return
  // the api of the values in the array from that index onward. The **guard**
  // check allows it to work with `_.map`.
  _.rest = _.tail = function(array, index, guard) {
    return slice.call(array, (index == null) || guard ? 1 : index);
  };

  // Get the last element of an array.
  _.last = function(array) {
    return array[array.length - 1];
  };

  // Trim out all falsy values from an array.
  _.compact = function(array) {
    return _.filter(array, function(value){ return !!value; });
  };

  // Return a completely flattened version of an array.
  _.flatten = function(array) {
    return _.reduce(array, function(memo, value) {
      if (_.isArray(value)) return memo.concat(_.flatten(value));
      memo[memo.length] = value;
      return memo;
    }, []);
  };

  // Return a version of the array that does not contain the specified value(s).
  _.without = function(array) {
    var values = slice.call(arguments, 1);
    return _.filter(array, function(value){ return !_.include(values, value); });
  };

  // Produce a duplicate-free version of the array. If the array has already
  // been sorted, you have the option of using a faster algorithm.
  // Aliased as `unique`.
  _.uniq = _.unique = function(array, isSorted) {
    return _.reduce(array, function(memo, el, i) {
      if (0 == i || (isSorted === true ? _.last(memo) != el : !_.include(memo, el))) memo[memo.length] = el;
      return memo;
    }, []);
  };

  // Produce an array that contains every item shared between all the
  // passed-in arrays.
  _.intersect = function(array) {
    var rest = slice.call(arguments, 1);
    return _.filter(_.uniq(array), function(item) {
      return _.every(rest, function(other) {
        return _.indexOf(other, item) >= 0;
      });
    });
  };

  // Zip together multiple lists into a single array -- elements that share
  // an index go together.
  _.zip = function() {
    var args = slice.call(arguments);
    var length = _.max(_.pluck(args, 'length'));
    var results = new Array(length);
    for (var i = 0; i < length; i++) results[i] = _.pluck(args, "" + i);
    return results;
  };

  // If the browser doesn't supply us with indexOf (I'm looking at you, **MSIE**),
  // we need this function. Return the position of the first occurrence of an
  // item in an array, or -1 if the item is not included in the array.
  // Delegates to **ECMAScript 5**'s native `indexOf` if available.
  // If the array is large and already in sort order, pass `true`
  // for **isSorted** to use binary search.
  _.indexOf = function(array, item, isSorted) {
    if (array == null) return -1;
    var i, l;
    if (isSorted) {
      i = _.sortedIndex(array, item);
      return array[i] === item ? i : -1;
    }
    if (nativeIndexOf && array.indexOf === nativeIndexOf) return array.indexOf(item);
    for (i = 0, l = array.length; i < l; i++) if (array[i] === item) return i;
    return -1;
  };


  // Delegates to **ECMAScript 5**'s native `lastIndexOf` if available.
  _.lastIndexOf = function(array, item) {
    if (array == null) return -1;
    if (nativeLastIndexOf && array.lastIndexOf === nativeLastIndexOf) return array.lastIndexOf(item);
    var i = array.length;
    while (i--) if (array[i] === item) return i;
    return -1;
  };

  // Generate an integer Array containing an arithmetic progression. A port of
  // the native Python `range()` function. See
  // [the Python documentation](http://docs.python.org/library/functions.html#range).
  _.range = function(start, stop, step) {
    if (arguments.length <= 1) {
      stop = start || 0;
      start = 0;
    }
    step = arguments[2] || 1;

    var len = Math.max(Math.ceil((stop - start) / step), 0);
    var idx = 0;
    var range = new Array(len);

    while(idx < len) {
      range[idx++] = start;
      start += step;
    }

    return range;
  };

  // Function (ahem) Functions
  // ------------------

  // Create a function bound to a given object (assigning `this`, and arguments,
  // optionally). Binding with arguments is also known as `curry`.
  // Delegates to **ECMAScript 5**'s native `Function.bind` if available.
  // We check for `func.bind` first, to fail fast when `func` is undefined.
  _.bind = function(func, obj) {
    if (func.bind === nativeBind && nativeBind) return nativeBind.apply(func, slice.call(arguments, 1));
    var args = slice.call(arguments, 2);
    return function() {
      return func.apply(obj, args.concat(slice.call(arguments)));
    };
  };

  // Bind all of an object's methods to that object. Useful for ensuring that
  // all callbacks defined on an object belong to it.
  _.bindAll = function(obj) {
    var funcs = slice.call(arguments, 1);
    if (funcs.length == 0) funcs = _.functions(obj);
    each(funcs, function(f) { obj[f] = _.bind(obj[f], obj); });
    return obj;
  };

  // Memoize an expensive function by storing its results.
  _.memoize = function(func, hasher) {
    var memo = {};
    hasher || (hasher = _.identity);
    return function() {
      var key = hasher.apply(this, arguments);
      return hasOwnProperty.call(memo, key) ? memo[key] : (memo[key] = func.apply(this, arguments));
    };
  };

  // Delays a function for the given number of milliseconds, and then calls
  // it with the arguments supplied.
  _.delay = function(func, wait) {
    var args = slice.call(arguments, 2);
    return setTimeout(function(){ return func.apply(func, args); }, wait);
  };

  // Defers a function, scheduling it to run after the current call stack has
  // cleared.
  _.defer = function(func) {
    return _.delay.apply(_, [func, 1].concat(slice.call(arguments, 1)));
  };

  // Internal function used to implement `_.throttle` and `_.debounce`.
  var limit = function(func, wait, debounce) {
    var timeout;
    return function() {
      var context = this, args = arguments;
      var throttler = function() {
        timeout = null;
        func.apply(context, args);
      };
      if (debounce) clearTimeout(timeout);
      if (debounce || !timeout) timeout = setTimeout(throttler, wait);
    };
  };

  // Returns a function, that, when invoked, will only be triggered at most once
  // during a given window of time.
  _.throttle = function(func, wait) {
    return limit(func, wait, false);
  };

  // Returns a function, that, as long as it continues to be invoked, will not
  // be triggered. The function will be called after it stops being called for
  // N milliseconds.
  _.debounce = function(func, wait) {
    return limit(func, wait, true);
  };

  // Returns a function that will be executed at most one time, no matter how
  // often you call it. Useful for lazy initialization.
  _.once = function(func) {
    var ran = false, memo;
    return function() {
      if (ran) return memo;
      ran = true;
      return memo = func.apply(this, arguments);
    };
  };

  // Returns the first function passed as an argument to the second,
  // allowing you to adjust arguments, run code before and after, and
  // conditionally execute the original function.
  _.wrap = function(func, wrapper) {
    return function() {
      var args = [func].concat(slice.call(arguments));
      return wrapper.apply(this, args);
    };
  };

  // Returns a function that is the composition of a list of functions, each
  // consuming the return value of the function that follows.
  _.compose = function() {
    var funcs = slice.call(arguments);
    return function() {
      var args = slice.call(arguments);
      for (var i=funcs.length-1; i >= 0; i--) {
        args = [funcs[i].apply(this, args)];
      }
      return args[0];
    };
  };

  // Returns a function that will only be executed after being called N times.
  _.after = function(times, func) {
    return function() {
      if (--times < 1) { return func.apply(this, arguments); }
    };
  };


  // Object Functions
  // ----------------

  // Retrieve the names of an object's properties.
  // Delegates to **ECMAScript 5**'s native `Object.keys`
  _.keys = nativeKeys || function(obj) {
    if (obj !== Object(obj)) throw new TypeError('Invalid object');
    var keys = [];
    for (var key in obj) if (hasOwnProperty.call(obj, key)) keys[keys.length] = key;
    return keys;
  };

  // Retrieve the values of an object's properties.
  _.values = function(obj) {
    return _.map(obj, _.identity);
  };

  // Return a sorted list of the function names available on the object.
  // Aliased as `methods`
  _.functions = _.methods = function(obj) {
    return _.filter(_.keys(obj), function(key){ return _.isFunction(obj[key]); }).sort();
  };

  // Extend a given object with all the properties in passed-in object(s).
  _.extend = function(obj) {
    each(slice.call(arguments, 1), function(source) {
      for (var prop in source) {
        if (source[prop] !== void 0) obj[prop] = source[prop];
      }
    });
    return obj;
  };

  // Fill in a given object with default properties.
  _.defaults = function(obj) {
    each(slice.call(arguments, 1), function(source) {
      for (var prop in source) {
        if (obj[prop] == null) obj[prop] = source[prop];
      }
    });
    return obj;
  };

  // Create a (shallow-cloned) duplicate of an object.
  _.clone = function(obj) {
    return _.isArray(obj) ? obj.slice() : _.extend({}, obj);
  };

  // Invokes interceptor with the obj, and then returns obj.
  // The primary purpose of this method is to "tap into" a method chain, in
  // order to perform operations on intermediate results within the chain.
  _.tap = function(obj, interceptor) {
    interceptor(obj);
    return obj;
  };

  // Perform a deep comparison to check if two objects are equal.
  _.isEqual = function(a, b) {
    // Check object identity.
    if (a === b) return true;
    // Different types?
    var atype = typeof(a), btype = typeof(b);
    if (atype != btype) return false;
    // Basic equality test (watch out for coercions).
    if (a == b) return true;
    // One is falsy and the other truthy.
    if ((!a && b) || (a && !b)) return false;
    // Unwrap any wrapped objects.
    if (a._chain) a = a._wrapped;
    if (b._chain) b = b._wrapped;
    // One of them implements an isEqual()?
    if (a.isEqual) return a.isEqual(b);
    // Check dates' integer values.
    if (_.isDate(a) && _.isDate(b)) return a.getTime() === b.getTime();
    // Both are NaN?
    if (_.isNaN(a) && _.isNaN(b)) return false;
    // Compare regular expressions.
    if (_.isRegExp(a) && _.isRegExp(b))
      return a.source     === b.source &&
             a.global     === b.global &&
             a.ignoreCase === b.ignoreCase &&
             a.multiline  === b.multiline;
    // If a is not an object by this point, we can't handle it.
    if (atype !== 'object') return false;
    // Check for different array lengths before comparing contents.
    if (a.length && (a.length !== b.length)) return false;
    // Nothing else worked, deep compare the contents.
    var aKeys = _.keys(a), bKeys = _.keys(b);
    // Different object sizes?
    if (aKeys.length != bKeys.length) return false;
    // Recursive comparison of contents.
    for (var key in a) if (!(key in b) || !_.isEqual(a[key], b[key])) return false;
    return true;
  };

  // Is a given array or object empty?
  _.isEmpty = function(obj) {
    if (_.isArray(obj) || _.isString(obj)) return obj.length === 0;
    for (var key in obj) if (hasOwnProperty.call(obj, key)) return false;
    return true;
  };

  // Is a given value a DOM element?
  _.isElement = function(obj) {
    return !!(obj && obj.nodeType == 1);
  };

  // Is a given value an array?
  // Delegates to ECMA5's native Array.isArray
  _.isArray = nativeIsArray || function(obj) {
    return toString.call(obj) === '[object Array]';
  };

  // Is a given variable an arguments object?
  _.isArguments = function(obj) {
    return !!(obj && hasOwnProperty.call(obj, 'callee'));
  };

  // Is a given value a function?
  _.isFunction = function(obj) {
    return !!(obj && obj.constructor && obj.call && obj.apply);
  };

  // Is a given value a string?
  _.isString = function(obj) {
    return !!(obj === '' || (obj && obj.charCodeAt && obj.substr));
  };

  // Is a given value a number?
  _.isNumber = function(obj) {
    return !!(obj === 0 || (obj && obj.toExponential && obj.toFixed));
  };

  // Is the given value `NaN`? `NaN` happens to be the only value in JavaScript
  // that does not equal itself.
  _.isNaN = function(obj) {
    return obj !== obj;
  };

  // Is a given value a boolean?
  _.isBoolean = function(obj) {
    return obj === true || obj === false;
  };

  // Is a given value a date?
  _.isDate = function(obj) {
    return !!(obj && obj.getTimezoneOffset && obj.setUTCFullYear);
  };

  // Is the given value a regular expression?
  _.isRegExp = function(obj) {
    return !!(obj && obj.test && obj.exec && (obj.ignoreCase || obj.ignoreCase === false));
  };

  // Is a given value equal to null?
  _.isNull = function(obj) {
    return obj === null;
  };

  // Is a given variable undefined?
  _.isUndefined = function(obj) {
    return obj === void 0;
  };

  // Utility Functions
  // -----------------

  // Run Underscore.js in *noConflict* mode, returning the `_` variable to its
  // previous owner. Returns a reference to the Underscore object.
  _.noConflict = function() {
    root._ = previousUnderscore;
    return this;
  };

  // Keep the identity function around for default iterators.
  _.identity = function(value) {
    return value;
  };

  // Run a function **n** times.
  _.times = function (n, iterator, context) {
    for (var i = 0; i < n; i++) iterator.call(context, i);
  };

  // Add your own custom functions to the Underscore object, ensuring that
  // they're correctly added to the OOP wrapper as well.
  _.mixin = function(obj) {
    each(_.functions(obj), function(name){
      addToWrapper(name, _[name] = obj[name]);
    });
  };

  // Generate a unique integer id (unique within the entire client session).
  // Useful for temporary DOM ids.
  var idCounter = 0;
  _.uniqueId = function(prefix) {
    var id = idCounter++;
    return prefix ? prefix + id : id;
  };

  // By default, Underscore uses ERB-style template delimiters, change the
  // following template settings to use alternative delimiters.
  _.templateSettings = {
    evaluate    : /<%([\s\S]+?)%>/g,
    interpolate : /<%=([\s\S]+?)%>/g
  };

  // JavaScript micro-templating, similar to John Resig's implementation.
  // Underscore templating handles arbitrary delimiters, preserves whitespace,
  // and correctly escapes quotes within interpolated code.
  _.template = function(str, data) {
    var c  = _.templateSettings;
    var tmpl = 'var __p=[],print=function(){__p.push.apply(__p,arguments);};' +
      'with(obj||{}){__p.push(\'' +
      str.replace(/\\/g, '\\\\')
         .replace(/'/g, "\\'")
         .replace(c.interpolate, function(match, code) {
           return "'," + code.replace(/\\'/g, "'") + ",'";
         })
         .replace(c.evaluate || null, function(match, code) {
           return "');" + code.replace(/\\'/g, "'")
                              .replace(/[\r\n\t]/g, ' ') + "__p.push('";
         })
         .replace(/\r/g, '\\r')
         .replace(/\n/g, '\\n')
         .replace(/\t/g, '\\t')
         + "');}return __p.join('');";
    var func = new Function('obj', tmpl);
    return data ? func(data) : func;
  };

  // The OOP Wrapper
  // ---------------

  // If Underscore is called as a function, it returns a wrapped object that
  // can be used OO-style. This wrapper holds altered versions of all the
  // underscore functions. Wrapped objects may be chained.
  var wrapper = function(obj) { this._wrapped = obj; };

  // Expose `wrapper.prototype` as `_.prototype`
  _.prototype = wrapper.prototype;

  // Helper function to continue chaining intermediate results.
  var result = function(obj, chain) {
    return chain ? _(obj).chain() : obj;
  };

  // A method to easily add functions to the OOP wrapper.
  var addToWrapper = function(name, func) {
    wrapper.prototype[name] = function() {
      var args = slice.call(arguments);
      unshift.call(args, this._wrapped);
      return result(func.apply(_, args), this._chain);
    };
  };

  // Add all of the Underscore functions to the wrapper object.
  _.mixin(_);

  // Add all mutator Array functions to the wrapper.
  each(['pop', 'push', 'reverse', 'shift', 'sort', 'splice', 'unshift'], function(name) {
    var method = ArrayProto[name];
    wrapper.prototype[name] = function() {
      method.apply(this._wrapped, arguments);
      return result(this._wrapped, this._chain);
    };
  });

  // Add all accessor Array functions to the wrapper.
  each(['concat', 'join', 'slice'], function(name) {
    var method = ArrayProto[name];
    wrapper.prototype[name] = function() {
      return result(method.apply(this._wrapped, arguments), this._chain);
    };
  });

  // Start chaining a wrapped Underscore object.
  wrapper.prototype.chain = function() {
    this._chain = true;
    return this;
  };

  // Extracts the result from a wrapped and chained object.
  wrapper.prototype.value = function() {
    return this._wrapped;
  };

})();
//     Backbone.js 0.5.3
//     (c) 2010 Jeremy Ashkenas, DocumentCloud Inc.
//     Backbone may be freely distributed under the MIT license.
//     For all details and documentation:
//     http://documentcloud.github.com/backbone

(function(){

  // Initial Setup
  // -------------

  // Save a reference to the global object.
  var root = this;

  // Save the previous value of the `Backbone` variable.
  var previousBackbone = root.Backbone;

  // The top-level namespace. All public Backbone classes and modules will
  // be attached to this. Exported for both CommonJS and the browser.
  var Backbone;
  if (typeof exports !== 'undefined') {
    Backbone = exports;
  } else {
    Backbone = root.Backbone = {};
  }

  // Current version of the library. Keep in sync with `package.json`.
  Backbone.VERSION = '0.5.3';

  // Require Underscore, if we're on the server, and it's not already present.
  var _ = root._;
  if (!_ && (typeof require !== 'undefined')) _ = require('underscore')._;

  // For Backbone's purposes, jQuery or Zepto owns the `$` variable.
  var $ = root.jQuery || root.Zepto;

  // Runs Backbone.js in *noConflict* mode, returning the `Backbone` variable
  // to its previous owner. Returns a reference to this Backbone object.
  Backbone.noConflict = function() {
    root.Backbone = previousBackbone;
    return this;
  };

  // Turn on `emulateHTTP` to support legacy HTTP servers. Setting this option will
  // fake `"PUT"` and `"DELETE"` requests via the `_method` parameter and set a
  // `X-Http-Method-Override` header.
  Backbone.emulateHTTP = false;

  // Turn on `emulateJSON` to support legacy servers that can't deal with direct
  // `application/json` requests ... will encode the body as
  // `application/x-www-form-urlencoded` instead and will send the model in a
  // form param named `model`.
  Backbone.emulateJSON = false;

  // Backbone.Events
  // -----------------

  // A module that can be mixed in to *any object* in order to provide it with
  // custom events. You may `bind` or `unbind` a callback function to an event;
  // `trigger`-ing an event fires all callbacks in succession.
  //
  //     var object = {};
  //     _.extend(object, Backbone.Events);
  //     object.bind('expand', function(){ alert('expanded'); });
  //     object.trigger('expand');
  //
  Backbone.Events = {

    // Bind an event, specified by a string name, `ev`, to a `callback` function.
    // Passing `"all"` will bind the callback to all events fired.
    bind : function(ev, callback, context) {
      var calls = this._callbacks || (this._callbacks = {});
      var list  = calls[ev] || (calls[ev] = []);
      list.push([callback, context]);
      return this;
    },

    // Remove one or many callbacks. If `callback` is null, removes all
    // callbacks for the event. If `ev` is null, removes all bound callbacks
    // for all events.
    unbind : function(ev, callback) {
      var calls;
      if (!ev) {
        this._callbacks = {};
      } else if (calls = this._callbacks) {
        if (!callback) {
          calls[ev] = [];
        } else {
          var list = calls[ev];
          if (!list) return this;
          for (var i = 0, l = list.length; i < l; i++) {
            if (list[i] && callback === list[i][0]) {
              list[i] = null;
              break;
            }
          }
        }
      }
      return this;
    },

    // Trigger an event, firing all bound callbacks. Callbacks are passed the
    // same arguments as `trigger` is, apart from the event name.
    // Listening for `"all"` passes the true event name as the first argument.
    trigger : function(eventName) {
      var list, calls, ev, callback, args;
      var both = 2;
      if (!(calls = this._callbacks)) return this;
      while (both--) {
        ev = both ? eventName : 'all';
        if (list = calls[ev]) {
          for (var i = 0, l = list.length; i < l; i++) {
            if (!(callback = list[i])) {
              list.splice(i, 1); i--; l--;
            } else {
              args = both ? Array.prototype.slice.call(arguments, 1) : arguments;
              callback[0].apply(callback[1] || this, args);
            }
          }
        }
      }
      return this;
    }

  };

  // Backbone.Model
  // --------------

  // Create a new model, with defined attributes. A client id (`cid`)
  // is automatically generated and assigned for you.
  Backbone.Model = function(attributes, options) {
    var defaults;
    attributes || (attributes = {});
    if (defaults = this.defaults) {
      if (_.isFunction(defaults)) defaults = defaults.call(this);
      attributes = _.extend({}, defaults, attributes);
    }
    this.attributes = {};
    this._escapedAttributes = {};
    this.cid = _.uniqueId('c');
    this.set(attributes, {silent : true});
    this._changed = false;
    this._previousAttributes = _.clone(this.attributes);
    if (options && options.collection) this.collection = options.collection;
    this.initialize(attributes, options);
  };

  // Attach all inheritable methods to the Model prototype.
  _.extend(Backbone.Model.prototype, Backbone.Events, {

    // A snapshot of the model's previous attributes, taken immediately
    // after the last `"change"` event was fired.
    _previousAttributes : null,

    // Has the item been changed since the last `"change"` event?
    _changed : false,

    // The default name for the JSON `id` attribute is `"id"`. MongoDB and
    // CouchDB users may want to set this to `"_id"`.
    idAttribute : 'id',

    // Initialize is an empty function by default. Override it with your own
    // initialization logic.
    initialize : function(){},

    // Return a copy of the model's `attributes` object.
    toJSON : function() {
      return _.clone(this.attributes);
    },

    // Get the value of an attribute.
    get : function(attr) {
      return this.attributes[attr];
    },

    // Get the HTML-escaped value of an attribute.
    escape : function(attr) {
      var html;
      if (html = this._escapedAttributes[attr]) return html;
      var val = this.attributes[attr];
      return this._escapedAttributes[attr] = escapeHTML(val == null ? '' : '' + val);
    },

    // Returns `true` if the attribute contains a value that is not null
    // or undefined.
    has : function(attr) {
      return this.attributes[attr] != null;
    },

    // Set a hash of model attributes on the object, firing `"change"` unless you
    // choose to silence it.
    set : function(attrs, options) {

      // Extract attributes and options.
      options || (options = {});
      if (!attrs) return this;
      if (attrs.attributes) attrs = attrs.attributes;
      var now = this.attributes, escaped = this._escapedAttributes;

      // Run validation.
      if (!options.silent && this.validate && !this._performValidation(attrs, options)) return false;

      // Check for changes of `id`.
      if (this.idAttribute in attrs) this.id = attrs[this.idAttribute];

      // We're about to start triggering change events.
      var alreadyChanging = this._changing;
      this._changing = true;

      // Update attributes.
      for (var attr in attrs) {
        var val = attrs[attr];
        if (!_.isEqual(now[attr], val)) {
          now[attr] = val;
          delete escaped[attr];
          this._changed = true;
          if (!options.silent) this.trigger('change:' + attr, this, val, options);
        }
      }

      // Fire the `"change"` event, if the model has been changed.
      if (!alreadyChanging && !options.silent && this._changed) this.change(options);
      this._changing = false;
      return this;
    },

    // Remove an attribute from the model, firing `"change"` unless you choose
    // to silence it. `unset` is a noop if the attribute doesn't exist.
    unset : function(attr, options) {
      if (!(attr in this.attributes)) return this;
      options || (options = {});
      var value = this.attributes[attr];

      // Run validation.
      var validObj = {};
      validObj[attr] = void 0;
      if (!options.silent && this.validate && !this._performValidation(validObj, options)) return false;

      // Remove the attribute.
      delete this.attributes[attr];
      delete this._escapedAttributes[attr];
      if (attr == this.idAttribute) delete this.id;
      this._changed = true;
      if (!options.silent) {
        this.trigger('change:' + attr, this, void 0, options);
        this.change(options);
      }
      return this;
    },

    // Clear all attributes on the model, firing `"change"` unless you choose
    // to silence it.
    clear : function(options) {
      options || (options = {});
      var attr;
      var old = this.attributes;

      // Run validation.
      var validObj = {};
      for (attr in old) validObj[attr] = void 0;
      if (!options.silent && this.validate && !this._performValidation(validObj, options)) return false;

      this.attributes = {};
      this._escapedAttributes = {};
      this._changed = true;
      if (!options.silent) {
        for (attr in old) {
          this.trigger('change:' + attr, this, void 0, options);
        }
        this.change(options);
      }
      return this;
    },

    // Fetch the model from the server. If the server's representation of the
    // model differs from its current attributes, they will be overriden,
    // triggering a `"change"` event.
    fetch : function(options) {
      options || (options = {});
      var model = this;
      var success = options.success;
      options.success = function(resp, status, xhr) {
        if (!model.set(model.parse(resp, xhr), options)) return false;
        if (success) success(model, resp);
      };
      options.error = wrapError(options.error, model, options);
      return (this.sync || Backbone.sync).call(this, 'read', this, options);
    },

    // Set a hash of model attributes, and sync the model to the server.
    // If the server returns an attributes hash that differs, the model's
    // state will be `set` again.
    save : function(attrs, options) {
      options || (options = {});
      if (attrs && !this.set(attrs, options)) return false;
      var model = this;
      var success = options.success;
      options.success = function(resp, status, xhr) {
        if (!model.set(model.parse(resp, xhr), options)) return false;
        if (success) success(model, resp, xhr);
      };
      options.error = wrapError(options.error, model, options);
      var method = this.isNew() ? 'create' : 'update';
      return (this.sync || Backbone.sync).call(this, method, this, options);
    },

    // Destroy this model on the server if it was already persisted. Upon success, the model is removed
    // from its collection, if it has one.
    destroy : function(options) {
      options || (options = {});
      if (this.isNew()) return this.trigger('destroy', this, this.collection, options);
      var model = this;
      var success = options.success;
      options.success = function(resp) {
        model.trigger('destroy', model, model.collection, options);
        if (success) success(model, resp);
      };
      options.error = wrapError(options.error, model, options);
      return (this.sync || Backbone.sync).call(this, 'delete', this, options);
    },

    // Default URL for the model's representation on the server -- if you're
    // using Backbone's restful methods, override this to change the endpoint
    // that will be called.
    url : function() {
      var base = getUrl(this.collection) || this.urlRoot || urlError();
      if (this.isNew()) return base;
      return base + (base.charAt(base.length - 1) == '/' ? '' : '/') + encodeURIComponent(this.id);
    },

    // **parse** converts a response into the hash of attributes to be `set` on
    // the model. The default implementation is just to pass the response along.
    parse : function(resp, xhr) {
      return resp;
    },

    // Create a new model with identical attributes to this one.
    clone : function() {
      return new this.constructor(this);
    },

    // A model is new if it has never been saved to the server, and lacks an id.
    isNew : function() {
      return this.id == null;
    },

    // Call this method to manually fire a `change` event for this model.
    // Calling this will cause all objects observing the model to update.
    change : function(options) {
      this.trigger('change', this, options);
      this._previousAttributes = _.clone(this.attributes);
      this._changed = false;
    },

    // Determine if the model has changed since the last `"change"` event.
    // If you specify an attribute name, determine if that attribute has changed.
    hasChanged : function(attr) {
      if (attr) return this._previousAttributes[attr] != this.attributes[attr];
      return this._changed;
    },

    // Return an object containing all the attributes that have changed, or false
    // if there are no changed attributes. Useful for determining what parts of a
    // view need to be updated and/or what attributes need to be persisted to
    // the server.
    changedAttributes : function(now) {
      now || (now = this.attributes);
      var old = this._previousAttributes;
      var changed = false;
      for (var attr in now) {
        if (!_.isEqual(old[attr], now[attr])) {
          changed = changed || {};
          changed[attr] = now[attr];
        }
      }
      return changed;
    },

    // Get the previous value of an attribute, recorded at the time the last
    // `"change"` event was fired.
    previous : function(attr) {
      if (!attr || !this._previousAttributes) return null;
      return this._previousAttributes[attr];
    },

    // Get all of the attributes of the model at the time of the previous
    // `"change"` event.
    previousAttributes : function() {
      return _.clone(this._previousAttributes);
    },

    // Run validation against a set of incoming attributes, returning `true`
    // if all is well. If a specific `error` callback has been passed,
    // call that instead of firing the general `"error"` event.
    _performValidation : function(attrs, options) {
      var error = this.validate(attrs);
      if (error) {
        if (options.error) {
          options.error(this, error, options);
        } else {
          this.trigger('error', this, error, options);
        }
        return false;
      }
      return true;
    }

  });

  // Backbone.Collection
  // -------------------

  // Provides a standard collection class for our sets of models, ordered
  // or unordered. If a `comparator` is specified, the Collection will maintain
  // its models in sort order, as they're added and removed.
  Backbone.Collection = function(models, options) {
    options || (options = {});
    if (options.comparator) this.comparator = options.comparator;
    _.bindAll(this, '_onModelEvent', '_removeReference');
    this._reset();
    if (models) this.reset(models, {silent: true});
    this.initialize.apply(this, arguments);
  };

  // Define the Collection's inheritable methods.
  _.extend(Backbone.Collection.prototype, Backbone.Events, {

    // The default model for a collection is just a **Backbone.Model**.
    // This should be overridden in most cases.
    model : Backbone.Model,

    // Initialize is an empty function by default. Override it with your own
    // initialization logic.
    initialize : function(){},

    // The JSON representation of a Collection is an array of the
    // models' attributes.
    toJSON : function() {
      return this.map(function(model){ return model.toJSON(); });
    },

    // Add a model, or list of models to the set. Pass **silent** to avoid
    // firing the `added` event for every new model.
    add : function(models, options) {
      if (_.isArray(models)) {
        for (var i = 0, l = models.length; i < l; i++) {
          this._add(models[i], options);
        }
      } else {
        this._add(models, options);
      }
      return this;
    },

    // Remove a model, or a list of models from the set. Pass silent to avoid
    // firing the `removed` event for every model removed.
    remove : function(models, options) {
      if (_.isArray(models)) {
        for (var i = 0, l = models.length; i < l; i++) {
          this._remove(models[i], options);
        }
      } else {
        this._remove(models, options);
      }
      return this;
    },

    // Get a model from the set by id.
    get : function(id) {
      if (id == null) return null;
      return this._byId[id.id != null ? id.id : id];
    },

    // Get a model from the set by client id.
    getByCid : function(cid) {
      return cid && this._byCid[cid.cid || cid];
    },

    // Get the model at the given index.
    at: function(index) {
      return this.models[index];
    },

    // Force the collection to re-sort itself. You don't need to call this under normal
    // circumstances, as the set will maintain sort order as each item is added.
    sort : function(options) {
      options || (options = {});
      if (!this.comparator) throw new Error('Cannot sort a set without a comparator');
      this.models = this.sortBy(this.comparator);
      if (!options.silent) this.trigger('reset', this, options);
      return this;
    },

    // Pluck an attribute from each model in the collection.
    pluck : function(attr) {
      return _.map(this.models, function(model){ return model.get(attr); });
    },

    // When you have more items than you want to add or remove individually,
    // you can reset the entire set with a new list of models, without firing
    // any `added` or `removed` events. Fires `reset` when finished.
    reset : function(models, options) {
      models  || (models = []);
      options || (options = {});
      this.each(this._removeReference);
      this._reset();
      this.add(models, {silent: true});
      if (!options.silent) this.trigger('reset', this, options);
      return this;
    },

    // Fetch the default set of models for this collection, resetting the
    // collection when they arrive. If `add: true` is passed, appends the
    // models to the collection instead of resetting.
    fetch : function(options) {
      options || (options = {});
      var collection = this;
      var success = options.success;
      options.success = function(resp, status, xhr) {
        collection[options.add ? 'add' : 'reset'](collection.parse(resp, xhr), options);
        if (success) success(collection, resp);
      };
      options.error = wrapError(options.error, collection, options);
      return (this.sync || Backbone.sync).call(this, 'read', this, options);
    },

    // Create a new instance of a model in this collection. After the model
    // has been created on the server, it will be added to the collection.
    // Returns the model, or 'false' if validation on a new model fails.
    create : function(model, options) {
      var coll = this;
      options || (options = {});
      model = this._prepareModel(model, options);
      if (!model) return false;
      var success = options.success;
      options.success = function(nextModel, resp, xhr) {
        coll.add(nextModel, options);
        if (success) success(nextModel, resp, xhr);
      };
      model.save(null, options);
      return model;
    },

    // **parse** converts a response into a list of models to be added to the
    // collection. The default implementation is just to pass it through.
    parse : function(resp, xhr) {
      return resp;
    },

    // Proxy to _'s chain. Can't be proxied the same way the rest of the
    // underscore methods are proxied because it relies on the underscore
    // constructor.
    chain: function () {
      return _(this.models).chain();
    },

    // Reset all internal state. Called when the collection is reset.
    _reset : function(options) {
      this.length = 0;
      this.models = [];
      this._byId  = {};
      this._byCid = {};
    },

    // Prepare a model to be added to this collection
    _prepareModel: function(model, options) {
      if (!(model instanceof Backbone.Model)) {
        var attrs = model;
        model = new this.model(attrs, {collection: this});
        if (model.validate && !model._performValidation(attrs, options)) model = false;
      } else if (!model.collection) {
        model.collection = this;
      }
      return model;
    },

    // Internal implementation of adding a single model to the set, updating
    // hash indexes for `id` and `cid` lookups.
    // Returns the model, or 'false' if validation on a new model fails.
    _add : function(model, options) {
      options || (options = {});
      model = this._prepareModel(model, options);
      if (!model) return false;
      var already = this.getByCid(model);
      if (already) throw new Error(["Can't add the same model to a set twice", already.id]);
      this._byId[model.id] = model;
      this._byCid[model.cid] = model;
      var index = options.at != null ? options.at :
                  this.comparator ? this.sortedIndex(model, this.comparator) :
                  this.length;
      this.models.splice(index, 0, model);
      model.bind('all', this._onModelEvent);
      this.length++;
      if (!options.silent) model.trigger('add', model, this, options);
      return model;
    },

    // Internal implementation of removing a single model from the set, updating
    // hash indexes for `id` and `cid` lookups.
    _remove : function(model, options) {
      options || (options = {});
      model = this.getByCid(model) || this.get(model);
      if (!model) return null;
      delete this._byId[model.id];
      delete this._byCid[model.cid];
      this.models.splice(this.indexOf(model), 1);
      this.length--;
      if (!options.silent) model.trigger('remove', model, this, options);
      this._removeReference(model);
      return model;
    },

    // Internal method to remove a model's ties to a collection.
    _removeReference : function(model) {
      if (this == model.collection) {
        delete model.collection;
      }
      model.unbind('all', this._onModelEvent);
    },

    // Internal method called every time a model in the set fires an event.
    // Sets need to update their indexes when models change ids. All other
    // events simply proxy through. "add" and "remove" events that originate
    // in other collections are ignored.
    _onModelEvent : function(ev, model, collection, options) {
      if ((ev == 'add' || ev == 'remove') && collection != this) return;
      if (ev == 'destroy') {
        this._remove(model, options);
      }
      if (model && ev === 'change:' + model.idAttribute) {
        delete this._byId[model.previous(model.idAttribute)];
        this._byId[model.id] = model;
      }
      this.trigger.apply(this, arguments);
    }

  });

  // Underscore methods that we want to implement on the Collection.
  var methods = ['forEach', 'each', 'map', 'reduce', 'reduceRight', 'find', 'detect',
    'filter', 'select', 'reject', 'every', 'all', 'some', 'any', 'include',
    'contains', 'invoke', 'max', 'min', 'sortBy', 'sortedIndex', 'toArray', 'size',
    'first', 'rest', 'last', 'without', 'indexOf', 'lastIndexOf', 'isEmpty', 'groupBy'];

  // Mix in each Underscore method as a proxy to `Collection#models`.
  _.each(methods, function(method) {
    Backbone.Collection.prototype[method] = function() {
      return _[method].apply(_, [this.models].concat(_.toArray(arguments)));
    };
  });

  // Backbone.Router
  // -------------------

  // Routers map faux-URLs to actions, and fire events when routes are
  // matched. Creating a new one sets its `routes` hash, if not set statically.
  Backbone.Router = function(options) {
    options || (options = {});
    if (options.routes) this.routes = options.routes;
    this._bindRoutes();
    this.initialize.apply(this, arguments);
  };

  // Cached regular expressions for matching named param parts and splatted
  // parts of route strings.
  var namedParam    = /:([\w\d]+)/g;
  var splatParam    = /\*([\w\d]+)/g;
  var escapeRegExp  = /[-[\]{}()+?.,\\^$|#\s]/g;

  // Set up all inheritable **Backbone.Router** properties and methods.
  _.extend(Backbone.Router.prototype, Backbone.Events, {

    // Initialize is an empty function by default. Override it with your own
    // initialization logic.
    initialize : function(){},

    // Manually bind a single named route to a callback. For example:
    //
    //     this.route('search/:query/p:num', 'search', function(query, num) {
    //       ...
    //     });
    //
    route : function(route, name, callback) {
      Backbone.history || (Backbone.history = new Backbone.History);
      if (!_.isRegExp(route)) route = this._routeToRegExp(route);
      Backbone.history.route(route, _.bind(function(fragment) {
        var args = this._extractParameters(route, fragment);
        callback.apply(this, args);
        this.trigger.apply(this, ['route:' + name].concat(args));
      }, this));
    },

    // Simple proxy to `Backbone.history` to save a fragment into the history.
    navigate : function(fragment, triggerRoute) {
      Backbone.history.navigate(fragment, triggerRoute);
    },

    // Bind all defined routes to `Backbone.history`. We have to reverse the
    // order of the routes here to support behavior where the most general
    // routes can be defined at the bottom of the route map.
    _bindRoutes : function() {
      if (!this.routes) return;
      var routes = [];
      for (var route in this.routes) {
        routes.unshift([route, this.routes[route]]);
      }
      for (var i = 0, l = routes.length; i < l; i++) {
        this.route(routes[i][0], routes[i][1], this[routes[i][1]]);
      }
    },

    // Convert a route string into a regular expression, suitable for matching
    // against the current location hash.
    _routeToRegExp : function(route) {
      route = route.replace(escapeRegExp, "\\$&")
                   .replace(namedParam, "([^\/]*)")
                   .replace(splatParam, "(.*?)");
      return new RegExp('^' + route + '$');
    },

    // Given a route, and a URL fragment that it matches, return the array of
    // extracted parameters.
    _extractParameters : function(route, fragment) {
      return route.exec(fragment).slice(1);
    }

  });

  // Backbone.History
  // ----------------

  // Handles cross-browser history management, based on URL fragments. If the
  // browser does not support `onhashchange`, falls back to polling.
  Backbone.History = function() {
    this.handlers = [];
    _.bindAll(this, 'checkUrl');
  };

  // Cached regex for cleaning hashes.
  var hashStrip = /^#*/;

  // Cached regex for detecting MSIE.
  var isExplorer = /msie [\w.]+/;

  // Has the history handling already been started?
  var historyStarted = false;

  // Set up all inheritable **Backbone.History** properties and methods.
  _.extend(Backbone.History.prototype, {

    // The default interval to poll for hash changes, if necessary, is
    // twenty times a second.
    interval: 50,

    // Get the cross-browser normalized URL fragment, either from the URL,
    // the hash, or the override.
    getFragment : function(fragment, forcePushState) {
      if (fragment == null) {
        if (this._hasPushState || forcePushState) {
          fragment = window.location.pathname;
          var search = window.location.search;
          if (search) fragment += search;
          if (fragment.indexOf(this.options.root) == 0) fragment = fragment.substr(this.options.root.length);
        } else {
          fragment = window.location.hash;
        }
      }
      return decodeURIComponent(fragment.replace(hashStrip, ''));
    },

    // Start the hash change handling, returning `true` if the current URL matches
    // an existing route, and `false` otherwise.
    start : function(options) {

      // Figure out the initial configuration. Do we need an iframe?
      // Is pushState desired ... is it available?
      if (historyStarted) throw new Error("Backbone.history has already been started");
      this.options          = _.extend({}, {root: '/'}, this.options, options);
      this._wantsPushState  = !!this.options.pushState;
      this._hasPushState    = !!(this.options.pushState && window.history && window.history.pushState);
      var fragment          = this.getFragment();
      var docMode           = document.documentMode;
      var oldIE             = (isExplorer.exec(navigator.userAgent.toLowerCase()) && (!docMode || docMode <= 7));
      if (oldIE) {
        this.iframe = $('<iframe src="javascript:0" tabindex="-1" />').hide().appendTo('body')[0].contentWindow;
        this.navigate(fragment);
      }

      // Depending on whether we're using pushState or hashes, and whether
      // 'onhashchange' is supported, determine how we check the URL state.
      if (this._hasPushState) {
        $(window).bind('popstate', this.checkUrl);
      } else if ('onhashchange' in window && !oldIE) {
        $(window).bind('hashchange', this.checkUrl);
      } else {
        setInterval(this.checkUrl, this.interval);
      }

      // Determine if we need to change the base url, for a pushState link
      // opened by a non-pushState browser.
      this.fragment = fragment;
      historyStarted = true;
      var loc = window.location;
      var atRoot  = loc.pathname == this.options.root;
      if (this._wantsPushState && !this._hasPushState && !atRoot) {
        this.fragment = this.getFragment(null, true);
        window.location.replace(this.options.root + '#' + this.fragment);
        // Return immediately as browser will do redirect to new url
        return true;
      } else if (this._wantsPushState && this._hasPushState && atRoot && loc.hash) {
        this.fragment = loc.hash.replace(hashStrip, '');
        window.history.replaceState({}, document.title, loc.protocol + '//' + loc.host + this.options.root + this.fragment);
      }

      if (!this.options.silent) {
        return this.loadUrl();
      }
    },

    // Add a route to be tested when the fragment changes. Routes added later may
    // override previous routes.
    route : function(route, callback) {
      this.handlers.unshift({route : route, callback : callback});
    },

    // Checks the current URL to see if it has changed, and if it has,
    // calls `loadUrl`, normalizing across the hidden iframe.
    checkUrl : function(e) {
      var current = this.getFragment();
      if (current == this.fragment && this.iframe) current = this.getFragment(this.iframe.location.hash);
      if (current == this.fragment || current == decodeURIComponent(this.fragment)) return false;
      if (this.iframe) this.navigate(current);
      this.loadUrl() || this.loadUrl(window.location.hash);
    },

    // Attempt to load the current URL fragment. If a route succeeds with a
    // match, returns `true`. If no defined routes matches the fragment,
    // returns `false`.
    loadUrl : function(fragmentOverride) {
      var fragment = this.fragment = this.getFragment(fragmentOverride);
      var matched = _.any(this.handlers, function(handler) {
        if (handler.route.test(fragment)) {
          handler.callback(fragment);
          return true;
        }
      });
      return matched;
    },

    // Save a fragment into the hash history. You are responsible for properly
    // URL-encoding the fragment in advance. This does not trigger
    // a `hashchange` event.
    navigate : function(fragment, triggerRoute) {
      var frag = (fragment || '').replace(hashStrip, '');
      if (this.fragment == frag || this.fragment == decodeURIComponent(frag)) return;
      if (this._hasPushState) {
        var loc = window.location;
        if (frag.indexOf(this.options.root) != 0) frag = this.options.root + frag;
        this.fragment = frag;
        window.history.pushState({}, document.title, loc.protocol + '//' + loc.host + frag);
      } else {
        window.location.hash = this.fragment = frag;
        if (this.iframe && (frag != this.getFragment(this.iframe.location.hash))) {
          this.iframe.document.open().close();
          this.iframe.location.hash = frag;
        }
      }
      if (triggerRoute) this.loadUrl(fragment);
    }

  });

  // Backbone.View
  // -------------

  // Creating a Backbone.View creates its initial element outside of the DOM,
  // if an existing element is not provided...
  Backbone.View = function(options) {
    this.cid = _.uniqueId('view');
    this._configure(options || {});
    this._ensureElement();
    this.delegateEvents();
    this.initialize.apply(this, arguments);
  };

  // Element lookup, scoped to DOM elements within the current view.
  // This should be prefered to global lookups, if you're dealing with
  // a specific view.
  var selectorDelegate = function(selector) {
    return $(selector, this.el);
  };

  // Cached regex to split keys for `delegate`.
  var eventSplitter = /^(\S+)\s*(.*)$/;

  // List of view options to be merged as properties.
  var viewOptions = ['model', 'collection', 'el', 'id', 'attributes', 'className', 'tagName'];

  // Set up all inheritable **Backbone.View** properties and methods.
  _.extend(Backbone.View.prototype, Backbone.Events, {

    // The default `tagName` of a View's element is `"div"`.
    tagName : 'div',

    // Attach the `selectorDelegate` function as the `$` property.
    $       : selectorDelegate,

    // Initialize is an empty function by default. Override it with your own
    // initialization logic.
    initialize : function(){},

    // **render** is the core function that your view should override, in order
    // to populate its element (`this.el`), with the appropriate HTML. The
    // convention is for **render** to always return `this`.
    render : function() {
      return this;
    },

    // Remove this view from the DOM. Note that the view isn't present in the
    // DOM by default, so calling this method may be a no-op.
    remove : function() {
      $(this.el).remove();
      return this;
    },

    // For small amounts of DOM Elements, where a full-blown template isn't
    // needed, use **make** to manufacture elements, one at a time.
    //
    //     var el = this.make('li', {'class': 'row'}, this.model.escape('title'));
    //
    make : function(tagName, attributes, content) {
      var el = document.createElement(tagName);
      if (attributes) $(el).attr(attributes);
      if (content) $(el).html(content);
      return el;
    },

    // Set callbacks, where `this.callbacks` is a hash of
    //
    // *{"event selector": "callback"}*
    //
    //     {
    //       'mousedown .title':  'edit',
    //       'click .button':     'save'
    //     }
    //
    // pairs. Callbacks will be bound to the view, with `this` set properly.
    // Uses event delegation for efficiency.
    // Omitting the selector binds the event to `this.el`.
    // This only works for delegate-able events: not `focus`, `blur`, and
    // not `change`, `submit`, and `reset` in Internet Explorer.
    delegateEvents : function(events) {
      if (!(events || (events = this.events))) return;
      if (_.isFunction(events)) events = events.call(this);
      $(this.el).unbind('.delegateEvents' + this.cid);
      for (var key in events) {
        var method = this[events[key]];
        if (!method) throw new Error('Event "' + events[key] + '" does not exist');
        var match = key.match(eventSplitter);
        var eventName = match[1], selector = match[2];
        method = _.bind(method, this);
        eventName += '.delegateEvents' + this.cid;
        if (selector === '') {
          $(this.el).bind(eventName, method);
        } else {
          $(this.el).delegate(selector, eventName, method);
        }
      }
    },

    // Performs the initial configuration of a View with a set of options.
    // Keys with special meaning *(model, collection, id, className)*, are
    // attached directly to the view.
    _configure : function(options) {
      if (this.options) options = _.extend({}, this.options, options);
      for (var i = 0, l = viewOptions.length; i < l; i++) {
        var attr = viewOptions[i];
        if (options[attr]) this[attr] = options[attr];
      }
      this.options = options;
    },

    // Ensure that the View has a DOM element to render into.
    // If `this.el` is a string, pass it through `$()`, take the first
    // matching element, and re-assign it to `el`. Otherwise, create
    // an element from the `id`, `className` and `tagName` proeprties.
    _ensureElement : function() {
      if (!this.el) {
        var attrs = this.attributes || {};
        if (this.id) attrs.id = this.id;
        if (this.className) attrs['class'] = this.className;
        this.el = this.make(this.tagName, attrs);
      } else if (_.isString(this.el)) {
        this.el = $(this.el).get(0);
      }
    }

  });

  // The self-propagating extend function that Backbone classes use.
  var extend = function (protoProps, classProps) {
    var child = inherits(this, protoProps, classProps);
    child.extend = this.extend;
    return child;
  };

  // Set up inheritance for the model, collection, and view.
  Backbone.Model.extend = Backbone.Collection.extend =
    Backbone.Router.extend = Backbone.View.extend = extend;

  // Map from CRUD to HTTP for our default `Backbone.sync` implementation.
  var methodMap = {
    'create': 'POST',
    'update': 'PUT',
    'delete': 'DELETE',
    'read'  : 'GET'
  };

  // Backbone.sync
  // -------------

  // Override this function to change the manner in which Backbone persists
  // models to the server. You will be passed the type of request, and the
  // model in question. By default, uses makes a RESTful Ajax request
  // to the model's `url()`. Some possible customizations could be:
  //
  // * Use `setTimeout` to batch rapid-fire updates into a single request.
  // * Send up the models as XML instead of JSON.
  // * Persist models via WebSockets instead of Ajax.
  //
  // Turn on `Backbone.emulateHTTP` in order to send `PUT` and `DELETE` requests
  // as `POST`, with a `_method` parameter containing the true HTTP method,
  // as well as all requests with the body as `application/x-www-form-urlencoded` instead of
  // `application/json` with the model in a param named `model`.
  // Useful when interfacing with server-side languages like **PHP** that make
  // it difficult to read the body of `PUT` requests.
  Backbone.sync = function(method, model, options) {
    var type = methodMap[method];

    // Default JSON-request options.
    var params = _.extend({
      type:         type,
      dataType:     'json'
    }, options);

    // Ensure that we have a URL.
    if (!params.url) {
      params.url = getUrl(model) || urlError();
    }

    // Ensure that we have the appropriate request data.
    if (!params.data && model && (method == 'create' || method == 'update')) {
      params.contentType = 'application/json';
      params.data = JSON.stringify(model.toJSON());
    }

    // For older servers, emulate JSON by encoding the request into an HTML-form.
    if (Backbone.emulateJSON) {
      params.contentType = 'application/x-www-form-urlencoded';
      params.data        = params.data ? {model : params.data} : {};
    }

    // For older servers, emulate HTTP by mimicking the HTTP method with `_method`
    // And an `X-HTTP-Method-Override` header.
    if (Backbone.emulateHTTP) {
      if (type === 'PUT' || type === 'DELETE') {
        if (Backbone.emulateJSON) params.data._method = type;
        params.type = 'POST';
        params.beforeSend = function(xhr) {
          xhr.setRequestHeader('X-HTTP-Method-Override', type);
        };
      }
    }

    // Don't process data on a non-GET request.
    if (params.type !== 'GET' && !Backbone.emulateJSON) {
      params.processData = false;
    }

    // Make the request.
    return $.ajax(params);
  };

  // Helpers
  // -------

  // Shared empty constructor function to aid in prototype-chain creation.
  var ctor = function(){};

  // Helper function to correctly set up the prototype chain, for subclasses.
  // Similar to `goog.inherits`, but uses a hash of prototype properties and
  // class properties to be extended.
  var inherits = function(parent, protoProps, staticProps) {
    var child;

    // The constructor function for the new subclass is either defined by you
    // (the "constructor" property in your `extend` definition), or defaulted
    // by us to simply call `super()`.
    if (protoProps && protoProps.hasOwnProperty('constructor')) {
      child = protoProps.constructor;
    } else {
      child = function(){ return parent.apply(this, arguments); };
    }

    // Inherit class (static) properties from parent.
    _.extend(child, parent);

    // Set the prototype chain to inherit from `parent`, without calling
    // `parent`'s constructor function.
    ctor.prototype = parent.prototype;
    child.prototype = new ctor();

    // Add prototype properties (instance properties) to the subclass,
    // if supplied.
    if (protoProps) _.extend(child.prototype, protoProps);

    // Add static properties to the constructor function, if supplied.
    if (staticProps) _.extend(child, staticProps);

    // Correctly set child's `prototype.constructor`.
    child.prototype.constructor = child;

    // Set a convenience property in case the parent's prototype is needed later.
    child.__super__ = parent.prototype;

    return child;
  };

  // Helper function to get a URL from a Model or Collection as a property
  // or as a function.
  var getUrl = function(object) {
    if (!(object && object.url)) return null;
    return _.isFunction(object.url) ? object.url() : object.url;
  };

  // Throw an error when a URL is needed, and none is supplied.
  var urlError = function() {
    throw new Error('A "url" property or function must be specified');
  };

  // Wrap an optional error callback with a fallback error event.
  var wrapError = function(onError, model, options) {
    return function(resp) {
      if (onError) {
        onError(model, resp, options);
      } else {
        model.trigger('error', model, resp, options);
      }
    };
  };

  // Helper function to escape a string for HTML rendering.
  var escapeHTML = function(string) {
    return string.replace(/&(?!\w+;|#\d+;|#x[\da-f]+;)/gi, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#x27;').replace(/\//g,'&#x2F;');
  };

}).call(this);/*
 * jQuery File Upload User Interface Plugin 3.9
 * https://github.com/blueimp/jQuery-File-Upload
 *
 * Copyright 2010, Sebastian Tschan
 * https://blueimp.net
 *
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 */

/*jslint browser: true */
/*global jQuery, FileReader, URL */

(function ($) {

    var undef = 'undefined',
        func = 'function',
        UploadHandler,
        methods,

        LocalImage = function (file, imageTypes) {
            var img,
                fileReader;
            if (!imageTypes.test(file.type)) {
                return null;
            }
            img = document.createElement('img');
            if (typeof URL !== undef && typeof URL.createObjectURL === func) {
                img.src = URL.createObjectURL(file);
                img.onload = function () {
                    URL.revokeObjectURL(this.src);
                };
                return img;
            }
            if (typeof FileReader !== undef) {
                fileReader = new FileReader();
                if (typeof fileReader.readAsDataURL === func) {
                    fileReader.onload = function (e) {
                        img.src = e.target.result;
                    };
                    fileReader.readAsDataURL(file);
                    return img;
                }
            }
            return null;
        };
        
    UploadHandler = function (container, options) {
        var uploadHandler = this,
            dragOverTimeout,
            isDropZoneEnlarged;
        
        this.requestHeaders = {'Accept': 'application/json, text/javascript, */*; q=0.01'};
        this.dropZone = container;
        this.imageTypes = /^image\/(gif|jpeg|png)$/;
        this.previewSelector = '.file_upload_preview';
        this.progressSelector = '.file_upload_progress div';
        this.cancelSelector = '.file_upload_cancel button';
        this.cssClassSmall = 'file_upload_small';
        this.cssClassLarge = 'file_upload_large';
        this.cssClassHighlight = 'file_upload_highlight';
        this.dropEffect = 'highlight';
        this.uploadTable = this.downloadTable = null;
        
        this.buildUploadRow = this.buildDownloadRow = function () {
            return null;
        };

        this.addNode = function (parentNode, node, callBack) {
            if (node) {
                node.css('display', 'none').appendTo(parentNode).fadeIn(function () {
                    if (typeof callBack === func) {
                        try {
                            callBack();
                        } catch (e) {
                            // Fix endless exception loop:
                            $(this).stop();
                            throw e;
                        }
                    }
                });
            } else if (typeof callBack === func) {
                callBack();
            }
        };

        this.removeNode = function (node, callBack) {
            if (node) {
                node.fadeOut(function () {
                    $(this).remove();
                    if (typeof callBack === func) {
                        try {
                            callBack();
                        } catch (e) {
                            // Fix endless exception loop:
                            $(this).stop();
                            throw e;
                        }
                    }
                });
            } else if (typeof callBack === func) {
                callBack();
            }
        };

        this.onAbort = function (event, files, index, xhr, handler) {
            handler.removeNode(handler.uploadRow);
        };
        
        this.cancelUpload = function (event, files, index, xhr, handler) {
            var readyState = xhr.readyState;
            xhr.abort();
            // If readyState is below 2, abort() has no effect:
            if (isNaN(readyState) || readyState < 2) {
                handler.onAbort(event, files, index, xhr, handler);
            }
        };
        
        this.initProgressBar = function (node, value) {
            if (typeof node.progressbar === func) {
                return node.progressbar({
                    value: value
                });
            } else {
                var progressbar = $('<progress value="' + value + '" max="100"/>').appendTo(node);
                progressbar.progressbar = function (key, value) {
                    progressbar.attr('value', value);
                };
                return progressbar;
            }
        };
        
        this.initUploadRow = function (event, files, index, xhr, handler, callBack) {
            var uploadRow = handler.uploadRow = handler.buildUploadRow(files, index, handler);
            if (uploadRow) {
                handler.progressbar = handler.initProgressBar(
                    uploadRow.find(handler.progressSelector),
                    0
                );
                uploadRow.find(handler.cancelSelector).click(function (e) {
                    handler.cancelUpload(e, files, index, xhr, handler);
                    return false;
                });
                uploadRow.find(handler.previewSelector).each(function () {
                    $(this).append(new LocalImage(files[index], handler.imageTypes));
                });
            }
            handler.addNode(
                (typeof handler.uploadTable === func ? handler.uploadTable(handler) : handler.uploadTable),
                uploadRow,
                callBack
            );
        };
        
        this.initUploadProgress = function (xhr, handler) {
            if (!xhr.upload && handler.progressbar) {
                handler.progressbar.progressbar(
                    'value',
                    100 // indeterminate progress displayed by a full animated progress bar
                );
            }
        };
        
        this.onSend = function (event, files, index, xhr, handler) {
            handler.initUploadProgress(xhr, handler);
        };
        
        this.initUpload = function (event, files, index, xhr, handler, callBack) {
            handler.initUploadRow(event, files, index, xhr, handler, function () {
                if (typeof handler.beforeSend === func) {
                    handler.beforeSend(event, files, index, xhr, handler, callBack);
                } else {
                    callBack();
                }
            });
        };
        
        this.onProgress = function (event, files, index, xhr, handler) {
            if (handler.progressbar) {
                handler.progressbar.progressbar(
                    'value',
                    parseInt(event.loaded / event.total * 100, 10)
                );
            }
        };
        
        this.parseResponse = function (xhr) {
            if (typeof xhr.responseText !== undef) {
                return $.parseJSON(xhr.responseText);
            } else {
                // Instead of an XHR object, an iframe is used for legacy browsers:
                return $.parseJSON(xhr.contents().text());
            }
        };
        
        this.initDownloadRow = function (event, files, index, xhr, handler, callBack) {
            var json, downloadRow;
            try {
                json = handler.response = handler.parseResponse(xhr);
                downloadRow = handler.downloadRow = handler.buildDownloadRow(json, handler);
                handler.addNode(
                    (typeof handler.downloadTable === func ? handler.downloadTable(handler) : handler.downloadTable),
                    downloadRow,
                    callBack
                );
            } catch (e) {
                if (typeof handler.onError === func) {
                    handler.originalEvent = event;
                    handler.onError(e, files, index, xhr, handler);
                } else {
                    throw e;
                }
            }
        };
        
        this.onLoad = function (event, files, index, xhr, handler) {
            handler.removeNode(handler.uploadRow, function () {
                handler.initDownloadRow(event, files, index, xhr, handler, function () {
                    if (typeof handler.onComplete === func) {
                        handler.onComplete(event, files, index, xhr, handler);
                    }
                });
            });
        };

        this.dropZoneEnlarge = function () {
            if (!isDropZoneEnlarged) {
                if (typeof uploadHandler.dropZone.switchClass === func) {
                    uploadHandler.dropZone.switchClass(
                        uploadHandler.cssClassSmall,
                        uploadHandler.cssClassLarge
                    );
                } else {
                    uploadHandler.dropZone.addClass(uploadHandler.cssClassLarge);
                    uploadHandler.dropZone.removeClass(uploadHandler.cssClassSmall);
                }
                isDropZoneEnlarged = true;
            }
        };
        
        this.dropZoneReduce = function () {
            if (typeof uploadHandler.dropZone.switchClass === func) {
                uploadHandler.dropZone.switchClass(
                    uploadHandler.cssClassLarge,
                    uploadHandler.cssClassSmall
                );
            } else {
                uploadHandler.dropZone.addClass(uploadHandler.cssClassSmall);
                uploadHandler.dropZone.removeClass(uploadHandler.cssClassLarge);
            }
            isDropZoneEnlarged = false;
        };

        this.onDocumentDragEnter = function (event) {
            uploadHandler.dropZoneEnlarge();
        };
        
        this.onDocumentDragOver = function (event) {
            if (dragOverTimeout) {
                clearTimeout(dragOverTimeout);
            }
            dragOverTimeout = setTimeout(function () {
                uploadHandler.dropZoneReduce();
            }, 200);
        };
        
        this.onDragEnter = this.onDragLeave = function (event) {
            uploadHandler.dropZone.toggleClass(uploadHandler.cssClassHighlight);
        };
        
        this.onDrop = function (event) {
            if (dragOverTimeout) {
                clearTimeout(dragOverTimeout);
            }
            if (uploadHandler.dropEffect && typeof uploadHandler.dropZone.effect === func) {
                uploadHandler.dropZone.effect(uploadHandler.dropEffect, function () {
                    uploadHandler.dropZone.removeClass(uploadHandler.cssClassHighlight);
                    uploadHandler.dropZoneReduce();
                });
            } else {
                uploadHandler.dropZone.removeClass(uploadHandler.cssClassHighlight);
                uploadHandler.dropZoneReduce();
            }
        };

        $.extend(this, options);
    };

    methods = {
        init : function (options) {
            return this.each(function () {
                $(this).fileUpload(new UploadHandler($(this), options));
            });
        },
        
        option: function (option, value, namespace) {
            if (typeof option === undef || (typeof option === 'string' && typeof value === undef)) {
                return $(this).fileUpload('option', option, value, namespace);
            }
            return this.each(function () {
                $(this).fileUpload('option', option, value, namespace);
            });
        },
            
        destroy : function (namespace) {
            return this.each(function () {
                $(this).fileUpload('destroy', namespace);
            });
        }
    };
    
    $.fn.fileUploadUI = function (method) {
        if (methods[method]) {
            return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof method === 'object' || !method) {
            return methods.init.apply(this, arguments);
        } else {
            $.error('Method ' + method + ' does not exist on jQuery.fileUploadUI');
        }
    };
    
}(jQuery));/*
 * jQuery File Upload Plugin 3.9.1
 * https://github.com/blueimp/jQuery-File-Upload
 *
 * Copyright 2010, Sebastian Tschan
 * https://blueimp.net
 *
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 */

/*jslint browser: true */
/*global File, FileReader, FormData, unescape, jQuery */

(function ($) {

    var defaultNamespace = 'file_upload',
        undef = 'undefined',
        func = 'function',
        num = 'number',
        FileUpload,
        methods,

        MultiLoader = function (callBack, numberComplete) {
            var loaded = 0;
            this.complete = function () {
                loaded += 1;
                if (loaded === numberComplete) {
                    callBack();
                }
            };
        };
        
    FileUpload = function (container) {
        var fileUpload = this,
            uploadForm,
            fileInput,
            settings = {
                namespace: defaultNamespace,
                uploadFormFilter: function (index) {
                    return true;
                },
                fileInputFilter: function (index) {
                    return true;
                },
                cssClass: defaultNamespace,
                dragDropSupport: true,
                dropZone: container,
                url: function (form) {
                    return form.attr('action');
                },
                method: function (form) {
                    return form.attr('method');
                },
                fieldName: function (input) {
                    return input.attr('name');
                },
                formData: function (form) {
                    return form.serializeArray();
                },
                multipart: true,
                multiFileRequest: false,
                withCredentials: false,
                forceIframeUpload: false
            },
            documentListeners = {},
            dropZoneListeners = {},
            protocolRegExp = /^http(s)?:\/\//,
            optionsReference,

            isXHRUploadCapable = function () {
                return typeof XMLHttpRequest !== undef && typeof File !== undef && (
                    !settings.multipart || typeof FormData !== undef || typeof FileReader !== undef
                );
            },

            initEventHandlers = function () {
                if (settings.dragDropSupport) {
                    if (typeof settings.onDocumentDragEnter === func) {
                        documentListeners['dragenter.' + settings.namespace] = function (e) {
                            settings.onDocumentDragEnter(e);
                        };
                    }
                    if (typeof settings.onDocumentDragLeave === func) {
                        documentListeners['dragleave.' + settings.namespace] = function (e) {
                            settings.onDocumentDragLeave(e);
                        };
                    }
                    documentListeners['dragover.'   + settings.namespace] = fileUpload.onDocumentDragOver;
                    documentListeners['drop.'       + settings.namespace] = fileUpload.onDocumentDrop;
                    $(document).bind(documentListeners);
                    if (typeof settings.onDragEnter === func) {
                        dropZoneListeners['dragenter.' + settings.namespace] = function (e) {
                            settings.onDragEnter(e);
                        };
                    }
                    if (typeof settings.onDragLeave === func) {
                        dropZoneListeners['dragleave.' + settings.namespace] = function (e) {
                            settings.onDragLeave(e);
                        };
                    }
                    dropZoneListeners['dragover.'   + settings.namespace] = fileUpload.onDragOver;
                    dropZoneListeners['drop.'       + settings.namespace] = fileUpload.onDrop;
                    settings.dropZone.bind(dropZoneListeners);
                }
                fileInput.bind('change.' + settings.namespace, fileUpload.onChange);
            },

            removeEventHandlers = function () {
                $.each(documentListeners, function (key, value) {
                    $(document).unbind(key, value);
                });
                $.each(dropZoneListeners, function (key, value) {
                    settings.dropZone.unbind(key, value);
                });
                fileInput.unbind('change.' + settings.namespace);
            },

            initUploadEventHandlers = function (files, index, xhr, settings) {
                if (typeof settings.onProgress === func) {
                    xhr.upload.onprogress = function (e) {
                        settings.onProgress(e, files, index, xhr, settings);
                    };
                }
                if (typeof settings.onLoad === func) {
                    xhr.onload = function (e) {
                        settings.onLoad(e, files, index, xhr, settings);
                    };
                }
                if (typeof settings.onAbort === func) {
                    xhr.onabort = function (e) {
                        settings.onAbort(e, files, index, xhr, settings);
                    };
                }
                if (typeof settings.onError === func) {
                    xhr.onerror = function (e) {
                        settings.onError(e, files, index, xhr, settings);
                    };
                }
            },

            getUrl = function (settings) {
                if (typeof settings.url === func) {
                    return settings.url(settings.uploadForm || uploadForm);
                }
                return settings.url;
            },
            
            getMethod = function (settings) {
                if (typeof settings.method === func) {
                    return settings.method(settings.uploadForm || uploadForm);
                }
                return settings.method;
            },
            
            getFieldName = function (settings) {
                if (typeof settings.fieldName === func) {
                    return settings.fieldName(settings.fileInput || fileInput);
                }
                return settings.fieldName;
            },

            getFormData = function (settings) {
                var formData;
                if (typeof settings.formData === func) {
                    return settings.formData(settings.uploadForm || uploadForm);
                } else if ($.isArray(settings.formData)) {
                    return settings.formData;
                } else if (settings.formData) {
                    formData = [];
                    $.each(settings.formData, function (name, value) {
                        formData.push({name: name, value: value});
                    });
                    return formData;
                }
                return [];
            },

            isSameDomain = function (url) {
                if (protocolRegExp.test(url)) {
                    var host = location.host,
                        indexStart = location.protocol.length + 2,
                        index = url.indexOf(host, indexStart),
                        pathIndex = index + host.length;
                    if ((index === indexStart || index === url.indexOf('@', indexStart) + 1) &&
                            (url.length === pathIndex || $.inArray(url.charAt(pathIndex), ['/', '?', '#']) !== -1)) {
                        return true;
                    }
                    return false;
                }
                return true;
            },

            setRequestHeaders = function (xhr, settings, sameDomain) {
                if (sameDomain) {
                    xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
                } else if (settings.withCredentials) {
                    xhr.withCredentials = true;
                }
                if ($.isArray(settings.requestHeaders)) {
                    $.each(settings.requestHeaders, function (index, header) {
                        xhr.setRequestHeader(header.name, header.value);
                    });
                } else if (settings.requestHeaders) {
                    $.each(settings.requestHeaders, function (name, value) {
                        xhr.setRequestHeader(name, value);
                    });
                }
            },

            nonMultipartUpload = function (file, xhr, sameDomain) {
                if (sameDomain) {
                    xhr.setRequestHeader('X-File-Name', unescape(encodeURIComponent(file.name)));
                }
                xhr.setRequestHeader('Content-Type', file.type);
                xhr.send(file);
            },

            formDataUpload = function (files, xhr, settings) {
                var formData = new FormData(),
                    i;
                $.each(getFormData(settings), function (index, field) {
                    formData.append(field.name, field.value);
                });
                for (i = 0; i < files.length; i += 1) {
                    formData.append(getFieldName(settings), files[i]);
                }
                xhr.send(formData);
            },

            loadFileContent = function (file, callBack) {
                var fileReader = new FileReader();
                fileReader.onload = function (e) {
                    file.content = e.target.result;
                    callBack();
                };
                fileReader.readAsBinaryString(file);
            },

            buildMultiPartFormData = function (boundary, files, filesFieldName, fields) {
                var doubleDash = '--',
                    crlf     = '\r\n',
                    formData = '';
                $.each(fields, function (index, field) {
                    formData += doubleDash + boundary + crlf +
                        'Content-Disposition: form-data; name="' +
                        unescape(encodeURIComponent(field.name)) +
                        '"' + crlf + crlf +
                        unescape(encodeURIComponent(field.value)) + crlf;
                });
                $.each(files, function (index, file) {
                    formData += doubleDash + boundary + crlf +
                        'Content-Disposition: form-data; name="' +
                        unescape(encodeURIComponent(filesFieldName)) +
                        '"; filename="' + unescape(encodeURIComponent(file.name)) + '"' + crlf +
                        'Content-Type: ' + file.type + crlf + crlf +
                        file.content + crlf;
                });
                formData += doubleDash + boundary + doubleDash + crlf;
                return formData;
            },
            
            fileReaderUpload = function (files, xhr, settings) {
                var boundary = '----MultiPartFormBoundary' + (new Date()).getTime(),
                    loader,
                    i;
                xhr.setRequestHeader('Content-Type', 'multipart/form-data; boundary=' + boundary);
                loader = new MultiLoader(function () {
                    xhr.sendAsBinary(buildMultiPartFormData(
                        boundary,
                        files,
                        getFieldName(settings),
                        getFormData(settings)
                    ));
                }, files.length);
                for (i = 0; i < files.length; i += 1) {
                    loadFileContent(files[i], loader.complete);
                }
            },

            upload = function (event, files, index, xhr, settings) {
                var url = getUrl(settings),
                    sameDomain = isSameDomain(url),
                    filesToUpload;
                initUploadEventHandlers(files, index, xhr, settings);
                xhr.open(getMethod(settings), url, true);
                setRequestHeaders(xhr, settings, sameDomain);
                if (typeof settings.onSend !== func || settings.onSend(event, files, index, xhr, settings) !== false) {
                    if (!settings.multipart) {
                        nonMultipartUpload(files[index], xhr, sameDomain);
                    } else {
                        if (typeof index === num) {
                            filesToUpload = [files[index]];
                        } else {
                            filesToUpload = files;
                        }
                        if (typeof FormData !== undef) {
                            formDataUpload(filesToUpload, xhr, settings);
                        } else if (typeof FileReader !== undef) {
                            fileReaderUpload(filesToUpload, xhr, settings);
                        } else {
                            $.error('Browser does neither support FormData nor FileReader interface');
                        }
                    }
                }
            },

            handleUpload = function (event, files, input, form, index) {
                var xhr = new XMLHttpRequest(),
                    uploadSettings = $.extend({}, settings);
                uploadSettings.fileInput = input;
                uploadSettings.uploadForm = form;
                if (typeof uploadSettings.initUpload === func) {
                    uploadSettings.initUpload(
                        event,
                        files,
                        index,
                        xhr,
                        uploadSettings,
                        function () {
                            upload(event, files, index, xhr, uploadSettings);
                        }
                    );
                } else {
                    upload(event, files, index, xhr, uploadSettings);
                }
            },

            handleFiles = function (event, files, input, form) {
                var i;
                if (settings.multiFileRequest) {
                    handleUpload(event, files, input, form);
                } else {
                    for (i = 0; i < files.length; i += 1) {
                        handleUpload(event, files, input, form, i);
                    }
                }
            },

            legacyUploadFormDataInit = function (input, form, settings) {
                var formData = getFormData(settings);
                form.find(':input').not(':disabled')
                    .attr('disabled', true)
                    .addClass(settings.namespace + '_disabled');
                $.each(formData, function (index, field) {
                    $('<input type="hidden"/>')
                        .attr('name', field.name)
                        .val(field.value)
                        .addClass(settings.namespace + '_form_data')
                        .appendTo(form);
                });
                input
                    .attr('name', getFieldName(settings))
                    .appendTo(form);
            },

            legacyUploadFormDataReset = function (input, form, settings) {
                input.detach();
                form.find('.' + settings.namespace + '_disabled')
                    .removeAttr('disabled')
                    .removeClass(settings.namespace + '_disabled');
                form.find('.' + settings.namespace + '_form_data').remove();
            },

            legacyUpload = function (event, input, form, iframe, settings) {
                var originalAction = form.attr('action'),
                    originalMethod = form.attr('method'),
                    originalTarget = form.attr('target'),
                    files = event.target.files || [{name: input.val(), type: null, size: null}];
                iframe
                    .unbind('abort')
                    .bind('abort', function (e) {
                        iframe.readyState = 0;
                        // javascript:false as iframe src prevents warning popups on HTTPS in IE6
                        // concat is used here to prevent the "Script URL" JSLint error:
                        iframe.unbind('load').attr('src', 'javascript'.concat(':false;'));
                        if (typeof settings.onAbort === func) {
                            settings.onAbort(e, files, 0, iframe, settings);
                        }
                    })
                    .unbind('load')
                    .bind('load', function (e) {
                        iframe.readyState = 4;
                        if (typeof settings.onLoad === func) {
                            settings.onLoad(e, files, 0, iframe, settings);
                        }
                        // Fix for IE endless progress bar activity bug (happens on form submits to iframe targets):
                        $('<iframe src="javascript:false;" style="display:none"></iframe>').appendTo(form).remove();
                    });
                form
                    .attr('action', getUrl(settings))
                    .attr('method', getMethod(settings))
                    .attr('target', iframe.attr('name'));
                legacyUploadFormDataInit(input, form, settings);
                if (typeof settings.onSend !== func || settings.onSend(event, files, 0, iframe, settings) !== false) {
                    iframe.readyState = 2;
                    form.get(0).submit();
                }
                legacyUploadFormDataReset(input, form, settings);
                form
                    .attr('action', originalAction)
                    .attr('method', originalMethod)
                    .attr('target', originalTarget);
            },

            handleLegacyUpload = function (event, input, form) {
                // javascript:false as iframe src prevents warning popups on HTTPS in IE6:
                var iframe = $('<iframe src="javascript:false;" style="display:none" name="iframe_' +
                    settings.namespace + '_' + (new Date()).getTime() + '"></iframe>'),
                    uploadSettings = $.extend({}, settings);
                uploadSettings.fileInput = input;
                uploadSettings.uploadForm = form;
                iframe.readyState = 0;
                iframe.abort = function () {
                    iframe.trigger('abort');
                };
                iframe.bind('load', function () {
                    iframe.unbind('load');
                    if (typeof uploadSettings.initUpload === func) {
                        uploadSettings.initUpload(
                            event,
                            [{name: input.val(), type: null, size: null}],
                            0,
                            iframe,
                            uploadSettings,
                            function () {
                                legacyUpload(event, input, form, iframe, uploadSettings);
                            }
                        );
                    } else {
                        legacyUpload(event, input, form, iframe, uploadSettings);
                    }
                }).appendTo(form);
            },
            
            initUploadForm = function () {
                uploadForm = (container.is('form') ? container : container.find('form'))
                    .filter(settings.uploadFormFilter);
            },
            
            initFileInput = function () {
                fileInput = (uploadForm.length ? uploadForm : container).find('input:file')
                    .filter(settings.fileInputFilter);
            },
            
            replaceFileInput = function (input) {
                var inputClone = input.clone(true);
                $('<form/>').append(inputClone).get(0).reset();
                input.after(inputClone).detach();
                initFileInput();
            };

        this.onDocumentDragOver = function (e) {
            if (typeof settings.onDocumentDragOver === func &&
                    settings.onDocumentDragOver(e) === false) {
                return false;
            }
            e.preventDefault();
        };
        
        this.onDocumentDrop = function (e) {
            if (typeof settings.onDocumentDrop === func &&
                    settings.onDocumentDrop(e) === false) {
                return false;
            }
            e.preventDefault();
        };

        this.onDragOver = function (e) {
            if (typeof settings.onDragOver === func &&
                    settings.onDragOver(e) === false) {
                return false;
            }
            var dataTransfer = e.originalEvent.dataTransfer;
            if (dataTransfer && dataTransfer.files) {
                dataTransfer.dropEffect = dataTransfer.effectAllowed = 'copy';
                e.preventDefault();
            }
        };

        this.onDrop = function (e) {
            if (typeof settings.onDrop === func &&
                    settings.onDrop(e) === false) {
                return false;
            }
            var dataTransfer = e.originalEvent.dataTransfer;
            if (dataTransfer && dataTransfer.files && isXHRUploadCapable()) {
                handleFiles(e, dataTransfer.files);
            }
            e.preventDefault();
        };
        
        this.onChange = function (e) {
            if (typeof settings.onChange === func &&
                    settings.onChange(e) === false) {
                return false;
            }
            var input = $(e.target),
                form = $(e.target.form);
            if (form.length === 1) {
                input.data(defaultNamespace + '_form', form);
                replaceFileInput(input);
            } else {
                form = input.data(defaultNamespace + '_form');
            }
            if (!settings.forceIframeUpload && e.target.files && isXHRUploadCapable()) {
                handleFiles(e, e.target.files, input, form);
            } else {
                handleLegacyUpload(e, input, form);
            }
        };

        this.init = function (options) {
            if (options) {
                $.extend(settings, options);
                optionsReference = options;
            }
            initUploadForm();
            initFileInput();
            if (container.data(settings.namespace)) {
                $.error('FileUpload with namespace "' + settings.namespace + '" already assigned to this element');
                return;
            }
            container
                .data(settings.namespace, fileUpload)
                .addClass(settings.cssClass);
            settings.dropZone.not(container).addClass(settings.cssClass);
            initEventHandlers();
        };

        this.options = function (options) {
            var oldCssClass,
                oldDropZone,
                uploadFormFilterUpdate,
                fileInputFilterUpdate;
            if (typeof options === undef) {
                return $.extend({}, settings);
            }
            if (optionsReference) {
                $.extend(optionsReference, options);
            }
            removeEventHandlers();
            $.each(options, function (name, value) {
                switch (name) {
                case 'namespace':
                    $.error('The FileUpload namespace cannot be updated.');
                    return;
                case 'uploadFormFilter':
                    uploadFormFilterUpdate = true;
                    fileInputFilterUpdate = true;
                    break;
                case 'fileInputFilter':
                    fileInputFilterUpdate = true;
                    break;
                case 'cssClass':
                    oldCssClass = settings.cssClass;
                    break;
                case 'dropZone':
                    oldDropZone = settings.dropZone;
                    break;
                }
                settings[name] = value;
            });
            if (uploadFormFilterUpdate) {
                initUploadForm();
            }
            if (fileInputFilterUpdate) {
                initFileInput();
            }
            if (typeof oldCssClass !== undef) {
                container
                    .removeClass(oldCssClass)
                    .addClass(settings.cssClass);
                (oldDropZone ? oldDropZone : settings.dropZone).not(container)
                    .removeClass(oldCssClass);
                settings.dropZone.not(container).addClass(settings.cssClass);
            } else if (oldDropZone) {
                oldDropZone.not(container).removeClass(settings.cssClass);
                settings.dropZone.not(container).addClass(settings.cssClass);
            }
            initEventHandlers();
        };
        
        this.option = function (name, value) {
            var options;
            if (typeof value === undef) {
                return settings[name];
            }
            options = {};
            options[name] = value;
            fileUpload.options(options);
        };
        
        this.destroy = function () {
            removeEventHandlers();
            container
                .removeData(settings.namespace)
                .removeClass(settings.cssClass);
            settings.dropZone.not(container).removeClass(settings.cssClass);
        };
    };

    methods = {
        init : function (options) {
            return this.each(function () {
                (new FileUpload($(this))).init(options);
            });
        },
        
        option: function (option, value, namespace) {
            namespace = namespace ? namespace : defaultNamespace;
            var fileUpload = $(this).data(namespace);
            if (fileUpload) {
                if (typeof option === 'string') {
                    return fileUpload.option(option, value);
                }
                return fileUpload.options(option);
            } else {
                $.error('No FileUpload with namespace "' + namespace + '" assigned to this element');
            }
        },
                
        destroy : function (namespace) {
            namespace = namespace ? namespace : defaultNamespace;
            return this.each(function () {
                var fileUpload = $(this).data(namespace);
                if (fileUpload) {
                    fileUpload.destroy();
                } else {
                    $.error('No FileUpload with namespace "' + namespace + '" assigned to this element');
                }
            });

        }
    };
    
    $.fn.fileUpload = function (method) {
        if (methods[method]) {
            return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof method === 'object' || !method) {
            return methods.init.apply(this, arguments);
        } else {
            $.error('Method ' + method + ' does not exist on jQuery.fileUpload');
        }
    };
    
}(jQuery));(function(a){a.widget("ui.panel",{options:{event:"click",collapsible:true,collapseType:"default",collapsed:false,accordion:false,collapseSpeed:"fast",draggable:false,trueVerticalText:true,vHeight:"220px",stackable:true,width:"auto",controls:false,cookie:null,widgetClass:"ui-helper-reset ui-widget ui-panel",headerClass:"ui-helper-reset ui-widget-header ui-panel-header ui-corner-top",contentClass:"ui-helper-reset ui-widget-content ui-panel-content ui-corner-bottom",contentTextClass:"ui-panel-content-text",rightboxClass:"ui-panel-rightbox",controlsClass:"ui-panel-controls",titleClass:"ui-panel-title",titleTextClass:"ui-panel-title-text",iconClass:"ui-icon",hoverClass:"ui-state-hover",collapsePnlClass:"ui-panel-clps-pnl",headerIconClpsd:"ui-icon-triangle-1-e",headerIcon:"ui-icon-triangle-1-s",slideRIconClpsd:"ui-icon-arrowthickstop-1-w",slideRIcon:"ui-icon-arrowthickstop-1-e",slideLIconClpsd:"ui-icon-arrowthickstop-1-e",slideLIcon:"ui-icon-arrowthickstop-1-w"},_init:function(){this._panelize()},_panelize:function(){if(this.element.is("div")){var b=this,c=this.options;this.panelBox=this.element;if(c.width=="auto"){c.width=this.panelBox.css("width")}else{this.panelBox.css("width",c.width)}this.panelBox.attr("role","panel");c.id=this.panelBox.attr("id");this.headerBox=this.element.children(":first");this.contentBox=this.element.children().eq(1);c.content=this.contentBox.html();this.contentBox.wrapInner("<div/>");this.contentTextBox=this.contentBox.children(":first").addClass(c.contentTextClass);this.headerBox.wrapInner("<div><span/></div>");this.titleBox=this.headerBox.children(":first");this.titleTextBox=this.titleBox.children(":first");this.titleText=this.titleTextBox.html();this.headerBox.prepend("<span/>");this.rightBox=this.headerBox.children(":first").addClass(c.rightboxClass);if(c.controls!=false){this.rightBox.append("<span/>");this.controlsBox=this.rightBox.children(":first").addClass(c.controlsClass).html(c.controls)}else{this.controlsBox=null}this.panelBox.addClass(c.widgetClass);this.headerBox.addClass(c.headerClass);this.titleBox.addClass(c.titleClass);this.titleTextBox.addClass(c.titleTextClass);this.contentBox.addClass(c.contentClass);if(c.collapsible){switch(c.collapseType){case"slide-right":var d=0;if(c.controls){d=1}this.rightBox.append("<span><span/></span>");this.collapsePanel=this.rightBox.children().eq(d).addClass(c.collapsePnlClass);this.collapseButton=this.collapsePanel.children(":first").addClass(c.slideRIcon);this.iconBtnClpsd=c.slideRIconClpsd;this.iconBtn=c.slideRIcon;this.ctrlBox=this.controlsBox;break;case"slide-left":this.headerBox.prepend("<span><span/></span>");this.collapsePanel=this.headerBox.children(":first").addClass(c.collapsePnlClass);this.collapseButton=this.collapsePanel.children(":first").addClass(c.slideLIcon);this.iconBtnClpsd=c.slideLIconClpsd;this.iconBtn=c.slideLIcon;this.ctrlBox=this.rightBox;break;default:this.headerBox.prepend("<span><span/></span>");this.collapseButton=this.headerBox.children(":first").addClass(c.headerIcon);this.iconBtnClpsd=c.headerIconClpsd;this.iconBtn=c.headerIcon;this.ctrlBox=this.controlsBox;break}this._buttonHover(this.collapseButton);this.collapseButton.addClass(c.iconClass);if(c.event){this.collapseButton.bind((c.event)+".panel",function(e){return b._clickHandler.call(b,e,this)});this.titleTextBox.bind((c.event)+".panel",function(e){return b._clickHandler.call(b,e,this)})}if(c.accordion){c.collapsed=true;c.trueVerticalText=false}if(c.cookie){if(b._cookie()==0){c.collapsed=false}else{c.collapsed=true}}this.panelBox.data("collapsed",c.collapsed);if(c.stackable&&a.inArray(c.collapseType,["slide-right","slide-left"])>-1){this.panelDock=this.panelBox.siblings("div[role=panelDock]:first");this.panelFrame=this.panelBox.siblings("div[role=panelFrame]:first");if(this.panelDock.length==0){this.panelDock=this.panelBox.parent(0).prepend("<div>").children(":first");this.panelFrame=this.panelDock.after("<div>").next(":first");this.panelDock.attr("role","panelDock").css("float",c.collapseType=="slide-left"?"left":"right");this.panelFrame.attr("role","panelFrame").css({"float":c.collapseType=="slide-left"?"left":"right",overflow:"hidden"})}if(c.collapsed){this.panelDock.append(this.panelBox)}else{this.panelFrame.append(this.panelBox)}}if(c.collapsed){b.toggle(0,true)}}else{this.titleTextBox.css("cursor","default")}if(!c.accordion&&c.draggable&&a.fn.draggable){this._makeDraggable()}this.panelBox.show()}},_cookie:function(){var b=this.cookie||(this.cookie=this.options.cookie.name||"ui-panel-"+this.options.id);return a.cookie.apply(null,[b].concat(a.makeArray(arguments)))},_makeDraggable:function(){this.panelBox.draggable({containment:"document",handle:".ui-panel-header",cancel:".ui-panel-content",cursor:"move"});this.contentBox.css("position","absolute")},_clickHandler:function(b,c){var d=this.options;if(d.disabled){return false}this.toggle(d.collapseSpeed);return false},toggle:function(f,i){var m=this,d=this.options,l=this.panelBox,h=this.contentBox,b=this.headerBox,k=this.titleTextBox,n=this.titleText,j=this.ctrlBox,g=this.panelDock,c="";if(!jQuery.support.leadingWhitespace){c="-ie"}if(h.css("display")=="none"){this._trigger("unfold")}else{this._trigger("fold")}if(j){j.toggle(0)}if(d.collapseType=="default"){if(f==0){if(j){j.hide()}h.hide()}else{h.slideToggle(f)}}else{if(f==0){d.collapsed=false;if(j){j.hide()}h.hide()}else{h.toggle()}if(d.collapsed==false){if(d.trueVerticalText){b.toggleClass("ui-panel-vtitle").css("height",d.vHeight);if(c==""){var e="height:"+(parseInt(d.vHeight)-50)+"px;width:100%;position:absolute;bottom:0;left:0;";k.empty().append('<div style="'+e+'z-index:3;"></div><object style="'+e+'z-index:2;" type="image/svg+xml" data="data:image/svg+xml;charset=utf-8,<svg xmlns=\'http://www.w3.org/2000/svg\'><text x=\'-'+(parseInt(d.vHeight)-60)+"px' y='16px' style='font-weight:"+k.css("font-weight")+";font-family:"+k.css("font-family").replace(/"/g,"")+";font-size:"+k.css("font-size")+";fill:"+k.css("color")+";' transform='rotate(-90)' text-rendering='optimizeSpeed'>"+n+'</text></svg>"></object>').css("height",d.vHeight)}k.toggleClass("ui-panel-vtext"+c)}else{b.attr("align","center");k.html(k.text().replace(/(.)/g,"$1<BR>"))}l.animate({width:"2.4em"},f);if(d.stackable){if(i){g.append(l)}else{g.prepend(l)}}}else{if(d.stackable){this.panelFrame.append(l)}if(d.trueVerticalText){b.toggleClass("ui-panel-vtitle").css("height","auto");k.empty().append(n);k.toggleClass("ui-panel-vtext"+c)}else{b.attr("align","left");k.html(k.text().replace(/<BR>/g," "))}l.animate({width:d.width},f)}}if(((f!=0||d.trueVerticalText)&&d.cookie==null)||(!i&&d.cookie!=null)){d.collapsed=!d.collapsed}this.panelBox.data("collapsed",d.collapsed);if(!i){if(d.cookie){m._cookie(Number(d.collapsed),d.cookie)}if(d.accordion){a("."+d.accordion+"[role='panel'][id!='"+(d.id)+"']:not(:data(collapsed))").panel("toggle",f,true)}}this.collapseButton.toggleClass(this.iconBtnClpsd).toggleClass(this.iconBtn);b.toggleClass("ui-corner-all")},content:function(b){this.contentTextBox.html(b)},destroy:function(){var b=this.options;this.headerBox.html(this.titleText).removeAttr("align").removeAttr("style").removeClass("ui-panel-vtitle ui-corner-all "+b.headerClass);this.contentBox.removeClass(b.contentClass).removeAttr("style").html(b.content);this.panelBox.removeAttr("role").removeAttr("style").removeData("collapsed").unbind(".panel").removeClass(b.widgetClass);if(b.stackable&&a.inArray(b.collapseType,["slide-right","slide-left"])>-1){this.panelDock.before(this.panelBox);if(this.panelDock.children("div[role=panel]").length==0&&this.panelFrame.children("div[role=panel]").length==0){this.panelDock.remove();this.panelFrame.remove()}}if(b.cookie){this._cookie(null,b.cookie)}return this},_buttonHover:function(b){var c=this.options;b.bind({mouseover:function(){a(this).addClass(c.hoverClass)},mouseout:function(){a(this).removeClass(c.hoverClass)}})}});a.extend(a.ui.panel,{version:"0.6"})})(jQuery);/*************************************************************************
	jquery.dynatree.js
	Dynamic tree view control, with support for lazy loading of branches.

	Copyright (c) 2008-2011, Martin Wendt (http://wwWendt.de)
	Dual licensed under the MIT or GPL Version 2 licenses.
	http://code.google.com/p/dynatree/wiki/LicenseInfo

	A current version and some documentation is available at
		http://dynatree.googlecode.com/

	$Version: 1.1.1$
	$Revision: 481, 2011-03-02 07:25:35$

	@depends: jquery.js
	@depends: jquery.ui.core.js
	@depends: jquery.cookie.js
*************************************************************************/

// Note: We currently allow eval() to parse the 'data' attribtes, when initializing from HTML.
/*jslint laxbreak: true, browser: true, evil: true, indent: 0, white: false, onevar: false */

/*************************************************************************
 *	Debug functions
 */

var _canLog = false;

function _log(mode, msg) {
	/**
	 * Usage: logMsg("%o was toggled", this);
	 */
	if( !_canLog ){
		return;
	}
	// Remove first argument
	var args = Array.prototype.slice.apply(arguments, [1]);
	// Prepend timestamp
	var dt = new Date();
	var tag = dt.getHours()+":"+dt.getMinutes()+":"+dt.getSeconds()+"."+dt.getMilliseconds();
	args[0] = tag + " - " + args[0];

	try {
		switch( mode ) {
		case "info":
			window.console.info.apply(window.console, args);
			break;
		case "warn":
			window.console.warn.apply(window.console, args);
			break;
		default:
			window.console.log.apply(window.console, args);
			break;
		}
	} catch(e) {
		if( !window.console ){
			_canLog = false; // Permanently disable, when logging is not supported by the browser
		}
	}
}

function logMsg(msg) {
	Array.prototype.unshift.apply(arguments, ["debug"]);
	_log.apply(this, arguments);
}


// Forward declaration
var getDynaTreePersistData = null;



/*************************************************************************
 *	Constants
 */
var DTNodeStatus_Error   = -1;
var DTNodeStatus_Loading = 1;
var DTNodeStatus_Ok      = 0;


// Start of local namespace
(function($) {

/*************************************************************************
 *	Common tool functions.
 */

var Class = {
	create: function() {
		return function() {
			this.initialize.apply(this, arguments);
		};
	}
};

// Tool function to get dtnode from the event target:
function getDtNodeFromElement(el) {
	var iMax = 5;
	while( el && iMax-- ) {
		if(el.dtnode) { return el.dtnode; }
		el = el.parentNode;
	}
	return null;
}

function noop() {
}

/*************************************************************************
 *	Class DynaTreeNode
 */
var DynaTreeNode = Class.create();

DynaTreeNode.prototype = {
	initialize: function(parent, tree, data) {
		/**
		 * @constructor
		 */
		this.parent = parent;
		this.tree = tree;
		if ( typeof data === "string" ){
			data = { title: data };
		}
		if( data.key === undefined ){
			data.key = "_" + tree._nodeCount++;
		}
		this.data = $.extend({}, $.ui.dynatree.nodedatadefaults, data);
		this.li = null; // not yet created
		this.span = null; // not yet created
		this.ul = null; // not yet created
		this.childList = null; // no subnodes yet
		this.isLoading = false; // Lazy content is being loaded
		this.hasSubSel = false;
		this.bExpanded = false;
		this.bSelected = false;

	},

	toString: function() {
		return "DynaTreeNode<" + this.data.key + ">: '" + this.data.title + "'";
	},

	toDict: function(recursive, callback) {
		var dict = $.extend({}, this.data);
		dict.activate = ( this.tree.activeNode === this );
		dict.focus = ( this.tree.focusNode === this );
		dict.expand = this.bExpanded;
		dict.select = this.bSelected;
		if( callback ){
			callback(dict);
		}
		if( recursive && this.childList ) {
			dict.children = [];
			for(var i=0, l=this.childList.length; i<l; i++ ){
				dict.children.push(this.childList[i].toDict(true, callback));
			}
		} else {
			delete dict.children;
		}
		return dict;
	},

	fromDict: function(dict) {
		/**
		 * Update node data. If dict contains 'children', then also replace
		 * the hole sub tree.
		 */
		var children = dict.children;
		if(children === undefined){
			this.data = $.extend(this.data, dict);
			this.render();
			return;
		}
		dict = $.extend({}, dict);
		dict.children = undefined;
		this.data = $.extend(this.data, dict);
		this.removeChildren();
		this.addChild(children);
	},

	_getInnerHtml: function() {
		var tree = this.tree,
			opts = tree.options,
			cache = tree.cache,
			level = this.getLevel(),
			data = this.data,
			res = "";
		// connector (expanded, expandable or simple)
		if( level < opts.minExpandLevel ) {
			if(level > 1){
				res += cache.tagConnector;
			}
			// .. else (i.e. for root level) skip expander/connector altogether
		} else if( this.hasChildren() !== false ) {
			res += cache.tagExpander;
		} else {
			res += cache.tagConnector;
		}
		// Checkbox mode
		if( opts.checkbox && data.hideCheckbox !== true && !data.isStatusNode ) {
			res += cache.tagCheckbox;
		}
		// folder or doctype icon
		if ( data.icon ) {
			res += "<img src='" + opts.imagePath + data.icon + "' alt='' />";
		} else if ( data.icon === false ) {
			// icon == false means 'no icon'
			noop(); // keep JSLint happy
		} else {
			// icon == null means 'default icon'
			res += cache.tagNodeIcon;
		}
		// node title
		var nodeTitle = "";
		if ( opts.onCustomRender ){
			nodeTitle = opts.onCustomRender.call(tree, this) || "";
		}
		if(!nodeTitle){
			var tooltip = data.tooltip ? " title='" + data.tooltip + "'" : "";
			if( opts.noLink || data.noLink ) {
				nodeTitle = "<span style='display: inline-block;' class='" + opts.classNames.title + "'" + tooltip + ">" + data.title + "</span>";
			}else{
				nodeTitle = "<a href='#' class='" + opts.classNames.title + "'" + tooltip + ">" + data.title + "</a>";
			}
		}
		res += nodeTitle;
		return res;
	},


	_fixOrder: function() {
		/**
		 * Make sure, that <li> order matches childList order.
		 */
		var cl = this.childList;
		if( !cl || !this.ul ){
			return;
		}
		var childLI = this.ul.firstChild;
		for(var i=0, l=cl.length-1; i<l; i++) {
			var childNode1 = cl[i];
			var childNode2 = childLI.dtnode;
			if( childNode1 !== childNode2 ) {
				this.tree.logDebug("_fixOrder: mismatch at index " + i + ": " + childNode1 + " != " + childNode2);
				this.ul.insertBefore(childNode1.li, childNode2.li);
			} else {
				childLI = childLI.nextSibling;
			}
		}
	},


	render: function(useEffects, includeInvisible) {
		/**
		 * Create <li><span>..</span> .. </li> tags for this node.
		 *
		 * <li id='KEY' dtnode=NODE> // This div contains the node's span and list of child div's.
		 *   <span class='title'>S S S A</span> // Span contains graphic spans and title <a> tag
		 *   <ul> // only present, when node has children
		 *       <li id='KEY' dtnode=NODE>child1</li>
		 *       <li id='KEY' dtnode=NODE>child2</li>
		 *   </ul>
		 * </li>
		 */
//		this.tree.logDebug("%s.render(%s)", this, useEffects);
		// ---
		var tree = this.tree,
			parent = this.parent,
			data = this.data,
			opts = tree.options,
			cn = opts.classNames,
			isLastSib = this.isLastSibling();

		if( !parent && !this.ul ) {
			// Root node has only a <ul>
			this.li = this.span = null;
			this.ul = document.createElement("ul");
			if( opts.minExpandLevel > 1 ){
				this.ul.className = cn.container + " " + cn.noConnector;
			}else{
				this.ul.className = cn.container;
			}
		} else if( parent ) {
			// Create <li><span /> </li>
			if( ! this.li ) {
				this.li = document.createElement("li");
				this.li.dtnode = this;
				if( data.key && opts.generateIds ){
					this.li.id = opts.idPrefix + data.key;
				}
				this.span = document.createElement("span");
				this.span.className = cn.title;
				this.li.appendChild(this.span);

				if( !parent.ul ) {
					// This is the parent's first child: create UL tag
					// (Hidden, because it will be
					parent.ul = document.createElement("ul");
					parent.ul.style.display = "none";
					parent.li.appendChild(parent.ul);
//					if( opts.minExpandLevel > this.getLevel() ){
//						parent.ul.className = cn.noConnector;
//					}
				}
				parent.ul.appendChild(this.li);
			}
			// set node connector images, links and text
			this.span.innerHTML = this._getInnerHtml();
			// Set classes for current status
			var cnList = [];
			cnList.push(cn.node);
			if( data.isFolder ){
				cnList.push(cn.folder);
			}
			if( this.bExpanded ){
				cnList.push(cn.expanded);
			}
			if( this.hasChildren() !== false ){
				cnList.push(cn.hasChildren);
			}
			if( data.isLazy && this.childList === null ){
				cnList.push(cn.lazy);
			}
			if( isLastSib ){
				cnList.push(cn.lastsib);
			}
			if( this.bSelected ){
				cnList.push(cn.selected);
			}
			if( this.hasSubSel ){
				cnList.push(cn.partsel);
			}
			if( tree.activeNode === this ){
				cnList.push(cn.active);
			}
			if( data.addClass ){
				cnList.push(data.addClass);
			}
			// IE6 doesn't correctly evaluate multiple class names,
			// so we create combined class names that can be used in the CSS
			cnList.push(cn.combinedExpanderPrefix
					+ (this.bExpanded ? "e" : "c")
					+ (data.isLazy && this.childList === null ? "d" : "")
					+ (isLastSib ? "l" : "")
					);
			cnList.push(cn.combinedIconPrefix
					+ (this.bExpanded ? "e" : "c")
					+ (data.isFolder ? "f" : "")
					);
			this.span.className = cnList.join(" ");

			// TODO: we should not set this in the <span> tag also, if we set it here:
			this.li.className = isLastSib ? cn.lastsib : "";

			// Hide children, if node is collapsed
//			this.ul.style.display = ( this.bExpanded || !parent ) ? "" : "none";
			// Allow tweaking, binding, ...
			if(opts.onRender){
				opts.onRender.call(tree, this, this.span);
			}
		}
		// Visit child nodes
		if( (this.bExpanded || includeInvisible === true) && this.childList ) {
			for(var i=0, l=this.childList.length; i<l; i++) {
				this.childList[i].render(false, includeInvisible);
			}
			// Make sure the tag order matches the child array
			this._fixOrder();
		}
		// Hide children, if node is collapsed
		if( this.ul ) {
			var isHidden = (this.ul.style.display === "none");
			var isExpanded = !!this.bExpanded;
//			logMsg("isHidden:%s", isHidden);
			if( useEffects && opts.fx && (isHidden === isExpanded) ) {
				var duration = opts.fx.duration || 200;
				$(this.ul).animate(opts.fx, duration);
			} else {
				this.ul.style.display = ( this.bExpanded || !parent ) ? "" : "none";
			}
		}
	},
	/** Return '/id1/id2/id3'. */
	getKeyPath: function(excludeSelf) {
		var path = [];
		this.visitParents(function(node){
			if(node.parent){
				path.unshift(node.data.key);
			}
		}, !excludeSelf);
		return "/" + path.join(this.tree.options.keyPathSeparator);
	},

	getParent: function() {
		return this.parent;
	},

	getChildren: function() {
		return this.childList;
	},

	/** Check if node has children (returns undefined, if not sure). */
	hasChildren: function() {
		if(this.data.isLazy){
			if(this.childList === null || this.childList === undefined){
				// Not yet loaded
				return undefined;
			}else if(this.childList.length === 0){
				// Loaded, but response was empty
				return false;
			}else if(this.childList.length === 1 && this.childList[0].isStatusNode()){
				// Currently loading or load error
				return undefined;
			}
			return true;
		}
		return !!this.childList;
	},

	isFirstSibling: function() {
		var p = this.parent;
		return !p || p.childList[0] === this;
	},

	isLastSibling: function() {
		var p = this.parent;
		return !p || p.childList[p.childList.length-1] === this;
	},

	getPrevSibling: function() {
		if( !this.parent ){
			return null;
		}
		var ac = this.parent.childList;
		for(var i=1, l=ac.length; i<l; i++){ // start with 1, so prev(first) = null
			if( ac[i] === this ){
				return ac[i-1];
			}
		}
		return null;
	},

	getNextSibling: function() {
		if( !this.parent ){
			return null;
		}
		var ac = this.parent.childList;
		for(var i=0, l=ac.length-1; i<l; i++){ // up to length-2, so next(last) = null
			if( ac[i] === this ){
				return ac[i+1];
			}
		}
		return null;
	},

	isStatusNode: function() {
		return (this.data.isStatusNode === true);
	},

	isChildOf: function(otherNode) {
		return (this.parent && this.parent === otherNode);
	},

	isDescendantOf: function(otherNode) {
		if(!otherNode){
			return false;
		}
		var p = this.parent;
		while( p ) {
			if( p === otherNode ){
				return true;
			}
			p = p.parent;
		}
		return false;
	},

	countChildren: function() {
		var cl = this.childList;
		if( !cl ){
			return 0;
		}
		var n = cl.length;
		for(var i=0, l=n; i<l; i++){
			var child = cl[i];
			n += child.countChildren();
		}
		return n;
	},

	/**Sort child list by title.
	 * cmd: optional compare function.
	 * deep: optional: pass true to sort all descendant nodes.
	 */
	sortChildren: function(cmp, deep) {
		var cl = this.childList;
		if( !cl ){
			return;
		}
		cmp = cmp || function(a, b) {
			return a.data.title === b.data.title ? 0 : a.data.title > b.data.title ? 1 : -1;
			};
		cl.sort(cmp);
		if( deep ){
			for(var i=0, l=cl.length; i<l; i++){
				if( cl[i].childList ){
					cl[i].sortChildren(cmp, "$norender$");
				}
			}
		}
		if( deep !== "$norender$" ){
			this.render();
		}
	},

	_setStatusNode: function(data) {
		// Create, modify or remove the status child node (pass 'null', to remove it).
		var firstChild = ( this.childList ? this.childList[0] : null );
		if( !data ) {
			if ( firstChild && firstChild.isStatusNode()) {
				try{
					// I've seen exceptions here with loadKeyPath...
					if(this.ul){
						this.ul.removeChild(firstChild.li);
					}
				}catch(e){}
				if( this.childList.length === 1 ){
					this.childList = [];
				}else{
					this.childList.shift();
				}
			}
		} else if ( firstChild ) {
			data.isStatusNode = true;
			data.key = "_statusNode";
			firstChild.data = data;
			firstChild.render();
		} else {
			data.isStatusNode = true;
			data.key = "_statusNode";
			firstChild = this.addChild(data);
		}
	},

	setLazyNodeStatus: function(lts, opts) {
		var tooltip = (opts && opts.tooltip) ? opts.tooltip : null;
		var info = (opts && opts.info) ? " (" + opts.info + ")" : "";
		switch( lts ) {
			case DTNodeStatus_Ok:
				this._setStatusNode(null);
				$(this.span).removeClass(this.tree.options.classNames.nodeLoading);
				this.isLoading = false;
//				this.render();
				if( this.tree.options.autoFocus ) {
					if( this === this.tree.tnRoot && this.childList && this.childList.length > 0) {
						// special case: using ajaxInit
						this.childList[0].focus();
					} else {
						this.focus();
					}
				}
				break;
			case DTNodeStatus_Loading:
				this.isLoading = true;
				$(this.span).addClass(this.tree.options.classNames.nodeLoading);
				// The root is hidden, so we set a temporary status child
				if(!this.parent){
					this._setStatusNode({
						title: this.tree.options.strings.loading + info,
						tooltip: tooltip,
						addClass: this.tree.options.classNames.nodeWait
					});
				}
				break;
			case DTNodeStatus_Error:
				this.isLoading = false;
//				$(this.span).addClass(this.tree.options.classNames.nodeError);
				this._setStatusNode({
					title: this.tree.options.strings.loadError + info,
					tooltip: tooltip,
					addClass: this.tree.options.classNames.nodeError
				});
				break;
			default:
				throw "Bad LazyNodeStatus: '" + lts + "'.";
		}
	},

	_parentList: function(includeRoot, includeSelf) {
		var l = [];
		var dtn = includeSelf ? this : this.parent;
		while( dtn ) {
			if( includeRoot || dtn.parent ){
				l.unshift(dtn);
			}
			dtn = dtn.parent;
		}
		return l;
	},
	getLevel: function() {
		/**
		 * Return node depth. 0: System root node, 1: visible top-level node.
		 */
		var level = 0;
		var dtn = this.parent;
		while( dtn ) {
			level++;
			dtn = dtn.parent;
		}
		return level;
	},

	_getTypeForOuterNodeEvent: function(event) {
		/** Return the inner node span (title, checkbox or expander) if
		 *  event.target points to the outer span.
		 *  This function should fix issue #93:
		 *  FF2 ignores empty spans, when generating events (returning the parent instead).
		 */
		var cns = this.tree.options.classNames;
		var target = event.target;
		// Only process clicks on an outer node span (probably due to a FF2 event handling bug)
		if( target.className.indexOf(cns.node) < 0 ) {
			return null;
		}
		// Event coordinates, relative to outer node span:
		var eventX = event.pageX - target.offsetLeft;
		var eventY = event.pageY - target.offsetTop;

		for(var i=0, l=target.childNodes.length; i<l; i++) {
			var cn = target.childNodes[i];
			var x = cn.offsetLeft - target.offsetLeft;
			var y = cn.offsetTop - target.offsetTop;
			var nx = cn.clientWidth, ny = cn.clientHeight;
//	        alert (cn.className + ": " + x + ", " + y + ", s:" + nx + ", " + ny);
			if( eventX >= x && eventX <= (x+nx) && eventY >= y && eventY <= (y+ny) ) {
//	            alert("HIT "+ cn.className);
				if( cn.className==cns.title ){
					return "title";
				}else if( cn.className==cns.expander ){
					return "expander";
				}else if( cn.className==cns.checkbox ){
					return "checkbox";
				}else if( cn.className==cns.nodeIcon ){
					return "icon";
				}
			}
		}
		return "prefix";
	},

	getEventTargetType: function(event) {
		// Return the part of a node, that a click event occured on.
		// Note: there is no check, if the event was fired on TIHS node.
		var tcn = event && event.target ? event.target.className : "";
		var cns = this.tree.options.classNames;

		if( tcn === cns.title ){
			return "title";
		}else if( tcn === cns.expander ){
			return "expander";
		}else if( tcn === cns.checkbox ){
			return "checkbox";
		}else if( tcn === cns.nodeIcon ){
			return "icon";
		}else if( tcn === cns.empty || tcn === cns.vline || tcn === cns.connector ){
			return "prefix";
		}else if( tcn.indexOf(cns.node) >= 0 ){
			// FIX issue #93
			return this._getTypeForOuterNodeEvent(event);
		}
		return null;
	},

	isVisible: function() {
		// Return true, if all parents are expanded.
		var parents = this._parentList(true, false);
		for(var i=0, l=parents.length; i<l; i++){
			if( ! parents[i].bExpanded ){ return false; }
		}
		return true;
	},

	makeVisible: function() {
		// Make sure, all parents are expanded
		var parents = this._parentList(true, false);
		for(var i=0, l=parents.length; i<l; i++){
			parents[i]._expand(true);
		}
	},

	focus: function() {
		// TODO: check, if we already have focus
//		this.tree.logDebug("dtnode.focus(): %o", this);
		this.makeVisible();
		try {
			$(this.span).find(">a").focus();
		} catch(e) { }
	},

	isFocused: function() {
		return (this.tree.tnFocused === this);
	},

	_activate: function(flag, fireEvents) {
		// (De)Activate - but not focus - this node.
		this.tree.logDebug("dtnode._activate(%o, fireEvents=%o) - %o", flag, fireEvents, this);
		var opts = this.tree.options;
		if( this.data.isStatusNode ){
			return;
		}
		if ( fireEvents && opts.onQueryActivate && opts.onQueryActivate.call(this.tree, flag, this) === false ){
			return; // Callback returned false
		}
		if( flag ) {
			// Activate
			if( this.tree.activeNode ) {
				if( this.tree.activeNode === this ){
					return;
				}
				this.tree.activeNode.deactivate();
			}
			if( opts.activeVisible ){
				this.makeVisible();
			}
			this.tree.activeNode = this;
			if( opts.persist ){
				$.cookie(opts.cookieId+"-active", this.data.key, opts.cookie);
			}
			this.tree.persistence.activeKey = this.data.key;
			$(this.span).addClass(opts.classNames.active);
			if ( fireEvents && opts.onActivate ){
				opts.onActivate.call(this.tree, this);
			}
		} else {
			// Deactivate
			if( this.tree.activeNode === this ) {
				if ( opts.onQueryActivate && opts.onQueryActivate.call(this.tree, false, this) === false ){
					return; // Callback returned false
				}
				$(this.span).removeClass(opts.classNames.active);
				if( opts.persist ) {
					// Note: we don't pass null, but ''. So the cookie is not deleted.
					// If we pass null, we also have to pass a COPY of opts, because $cookie will override opts.expires (issue 84)
					$.cookie(opts.cookieId+"-active", "", opts.cookie);
				}
				this.tree.persistence.activeKey = null;
				this.tree.activeNode = null;
				if ( fireEvents && opts.onDeactivate ){
					opts.onDeactivate.call(this.tree, this);
				}
			}
		}
	},

	activate: function() {
		// Select - but not focus - this node.
//		this.tree.logDebug("dtnode.activate(): %o", this);
		this._activate(true, true);
	},

	activateSilently: function() {
		this._activate(true, false);
	},

	deactivate: function() {
//		this.tree.logDebug("dtnode.deactivate(): %o", this);
		this._activate(false, true);
	},

	isActive: function() {
		return (this.tree.activeNode === this);
	},

	_userActivate: function() {
		// Handle user click / [space] / [enter], according to clickFolderMode.
		var activate = true;
		var expand = false;
		if ( this.data.isFolder ) {
			switch( this.tree.options.clickFolderMode ) {
			case 2:
				activate = false;
				expand = true;
				break;
			case 3:
				activate = expand = true;
				break;
			}
		}
		if( this.parent === null ) {
			expand = false;
		}
		if( expand ) {
			this.toggleExpand();
			this.focus();
		}
		if( activate ) {
			this.activate();
		}
	},

	_setSubSel: function(hasSubSel) {
		if( hasSubSel ) {
			this.hasSubSel = true;
			$(this.span).addClass(this.tree.options.classNames.partsel);
		} else {
			this.hasSubSel = false;
			$(this.span).removeClass(this.tree.options.classNames.partsel);
		}
	},

	_fixSelectionState: function() {
		// fix selection status, for multi-hier mode
//		this.tree.logDebug("_fixSelectionState(%o) - %o", this.bSelected, this);
		var p, i, l;
		if( this.bSelected ) {
			// Select all children
			this.visit(function(node){
				node.parent._setSubSel(true);
				node._select(true, false, false);
			});
			// Select parents, if all children are selected
			p = this.parent;
			while( p ) {
				p._setSubSel(true);
				var allChildsSelected = true;
				for(i=0, l=p.childList.length; i<l;  i++) {
					var n = p.childList[i];
					if( !n.bSelected && !n.data.isStatusNode ) {
						allChildsSelected = false;
						break;
					}
				}
				if( allChildsSelected ){
					p._select(true, false, false);
				}
				p = p.parent;
			}
		} else {
			// Deselect all children
			this._setSubSel(false);
			this.visit(function(node){
				node._setSubSel(false);
				node._select(false, false, false);
			});
			// Deselect parents, and recalc hasSubSel
			p = this.parent;
			while( p ) {
				p._select(false, false, false);
				var isPartSel = false;
				for(i=0, l=p.childList.length; i<l;  i++) {
					if( p.childList[i].bSelected || p.childList[i].hasSubSel ) {
						isPartSel = true;
						break;
					}
				}
				p._setSubSel(isPartSel);
				p = p.parent;
			}
		}
	},

	_select: function(sel, fireEvents, deep) {
		// Select - but not focus - this node.
//		this.tree.logDebug("dtnode._select(%o) - %o", sel, this);
		var opts = this.tree.options;
		if( this.data.isStatusNode ){
			return;
		}
		//
		if( this.bSelected === sel ) {
//			this.tree.logDebug("dtnode._select(%o) IGNORED - %o", sel, this);
			return;
		}
		// Allow event listener to abort selection
		if ( fireEvents && opts.onQuerySelect && opts.onQuerySelect.call(this.tree, sel, this) === false ){
			return; // Callback returned false
		}
		// Force single-selection
		if( opts.selectMode==1 && sel ) {
			this.tree.visit(function(node){
				if( node.bSelected ) {
					// Deselect; assuming that in selectMode:1 there's max. one other selected node
					node._select(false, false, false);
					return false;
				}
			});
		}

		this.bSelected = sel;
//        this.tree._changeNodeList("select", this, sel);

		if( sel ) {
			if( opts.persist ){
				this.tree.persistence.addSelect(this.data.key);
			}
			$(this.span).addClass(opts.classNames.selected);

			if( deep && opts.selectMode === 3 ){
				this._fixSelectionState();
			}
			if ( fireEvents && opts.onSelect ){
				opts.onSelect.call(this.tree, true, this);
			}
		} else {
			if( opts.persist ){
				this.tree.persistence.clearSelect(this.data.key);
			}
			$(this.span).removeClass(opts.classNames.selected);

			if( deep && opts.selectMode === 3 ){
				this._fixSelectionState();
			}
			if ( fireEvents && opts.onSelect ){
				opts.onSelect.call(this.tree, false, this);
			}
		}
	},

	select: function(sel) {
		// Select - but not focus - this node.
//		this.tree.logDebug("dtnode.select(%o) - %o", sel, this);
		if( this.data.unselectable ){
			return this.bSelected;
		}
		return this._select(sel!==false, true, true);
	},

	toggleSelect: function() {
//		this.tree.logDebug("dtnode.toggleSelect() - %o", this);
		return this.select(!this.bSelected);
	},

	isSelected: function() {
		return this.bSelected;
	},

	_loadContent: function() {
		try {
			var opts = this.tree.options;
			this.tree.logDebug("_loadContent: start - %o", this);
			this.setLazyNodeStatus(DTNodeStatus_Loading);
			if( true === opts.onLazyRead.call(this.tree, this) ) {
				// If function returns 'true', we assume that the loading is done:
				this.setLazyNodeStatus(DTNodeStatus_Ok);
				// Otherwise (i.e. if the loading was started as an asynchronous process)
				// the onLazyRead(dtnode) handler is expected to call dtnode.setLazyNodeStatus(DTNodeStatus_Ok/_Error) when done.
				this.tree.logDebug("_loadContent: succeeded - %o", this);
			}
		} catch(e) {
			this.tree.logWarning("_loadContent: failed - %o", e);
			this.setLazyNodeStatus(DTNodeStatus_Error, {tooltip: ""+e});
		}
	},

	_expand: function(bExpand, forceSync) {
		if( this.bExpanded === bExpand ) {
			this.tree.logDebug("dtnode._expand(%o) IGNORED - %o", bExpand, this);
			return;
		}
		this.tree.logDebug("dtnode._expand(%o) - %o", bExpand, this);
		var opts = this.tree.options;
		if( !bExpand && this.getLevel() < opts.minExpandLevel ) {
			this.tree.logDebug("dtnode._expand(%o) prevented collapse - %o", bExpand, this);
			return;
		}
		if ( opts.onQueryExpand && opts.onQueryExpand.call(this.tree, bExpand, this) === false ){
			return; // Callback returned false
		}
		this.bExpanded = bExpand;

		// Persist expand state
		if( opts.persist ) {
			if( bExpand ){
				this.tree.persistence.addExpand(this.data.key);
			}else{
				this.tree.persistence.clearExpand(this.data.key);
			}
		}
		// Do not apply animations in init phase, or before lazy-loading
		var allowEffects = !(this.data.isLazy && this.childList === null)
			&& !this.isLoading
			&& !forceSync;
		this.render(allowEffects);

		// Auto-collapse mode: collapse all siblings
		if( this.bExpanded && this.parent && opts.autoCollapse ) {
			var parents = this._parentList(false, true);
			for(var i=0, l=parents.length; i<l; i++){
				parents[i].collapseSiblings();
			}
		}
		// If the currently active node is now hidden, deactivate it
		if( opts.activeVisible && this.tree.activeNode && ! this.tree.activeNode.isVisible() ) {
			this.tree.activeNode.deactivate();
		}
		// Expanding a lazy node: set 'loading...' and call callback
		if( bExpand && this.data.isLazy && this.childList === null && !this.isLoading ) {
			this._loadContent();
			return;
		}
		if ( opts.onExpand ){
			opts.onExpand.call(this.tree, bExpand, this);
		}
	},

	expand: function(flag) {
		flag = (flag !== false);
		if( !this.childList && !this.data.isLazy && flag ){
			return; // Prevent expanding empty nodes
		} else if( this.parent === null && !flag ){
			return; // Prevent collapsing the root
		}
		this._expand(flag);
	},

	scheduleAction: function(mode, ms) {
		/** Schedule activity for delayed execution (cancel any pending request).
		 *  scheduleAction('cancel') will cancel the request.
		 */
		if( this.tree.timer ) {
			clearTimeout(this.tree.timer);
			this.tree.logDebug("clearTimeout(%o)", this.tree.timer);
		}
		var self = this; // required for closures
		switch (mode) {
		case "cancel":
			// Simply made sure that timer was cleared
			break;
		case "expand":
			this.tree.timer = setTimeout(function(){
				self.tree.logDebug("setTimeout: trigger expand");
				self.expand(true);
			}, ms);
			break;
		case "activate":
			this.tree.timer = setTimeout(function(){
				self.tree.logDebug("setTimeout: trigger activate");
				self.activate();
			}, ms);
			break;
		default:
			throw "Invalid mode " + mode;
		}
		this.tree.logDebug("setTimeout(%s, %s): %s", mode, ms, this.tree.timer);
	},

	toggleExpand: function() {
		this.expand(!this.bExpanded);
	},

	collapseSiblings: function() {
		if( this.parent === null ){
			return;
		}
		var ac = this.parent.childList;
		for (var i=0, l=ac.length; i<l; i++) {
			if ( ac[i] !== this && ac[i].bExpanded ){
				ac[i]._expand(false);
			}
		}
	},

	_onClick: function(event) {
//		this.tree.logDebug("dtnode.onClick(" + event.type + "): dtnode:" + this + ", button:" + event.button + ", which: " + event.which);
		var targetType = this.getEventTargetType(event);
		if( targetType === "expander" ) {
			// Clicking the expander icon always expands/collapses
			this.toggleExpand();
			this.focus(); // issue 95
		} else if( targetType === "checkbox" ) {
			// Clicking the checkbox always (de)selects
			this.toggleSelect();
			this.focus(); // issue 95
		} else {
			this._userActivate();
			var aTag = this.span.getElementsByTagName("a");
			if(aTag[0]){
				// issue 154
				// TODO: check if still required on IE 9:
				// Chrome and Safari don't focus the a-tag on click,
				// but calling focus() seem to have problems on IE:
				// http://code.google.com/p/dynatree/issues/detail?id=154
				if(!$.browser.msie){
					aTag[0].focus();
				}
			}else{
				// 'noLink' option was set
				return true;
			}
		}
		// Make sure that clicks stop, otherwise <a href='#'> jumps to the top
		event.preventDefault();
	},

	_onDblClick: function(event) {
//		this.tree.logDebug("dtnode.onDblClick(" + event.type + "): dtnode:" + this + ", button:" + event.button + ", which: " + event.which);
	},

	_onKeydown: function(event) {
//		this.tree.logDebug("dtnode.onKeydown(" + event.type + "): dtnode:" + this + ", charCode:" + event.charCode + ", keyCode: " + event.keyCode + ", which: " + event.which);
		var handled = true,
			sib;
//		alert("keyDown" + event.which);

		switch( event.which ) {
			// charCodes:
//			case 43: // '+'
			case 107: // '+'
			case 187: // '+' @ Chrome, Safari
				if( !this.bExpanded ){ this.toggleExpand(); }
				break;
//			case 45: // '-'
			case 109: // '-'
			case 189: // '+' @ Chrome, Safari
				if( this.bExpanded ){ this.toggleExpand(); }
				break;
			//~ case 42: // '*'
				//~ break;
			//~ case 47: // '/'
				//~ break;
			// case 13: // <enter>
				// <enter> on a focused <a> tag seems to generate a click-event.
				// this._userActivate();
				// break;
			case 32: // <space>
				this._userActivate();
				break;
			case 8: // <backspace>
				if( this.parent ){
					this.parent.focus();
				}
				break;
			case 37: // <left>
				if( this.bExpanded ) {
					this.toggleExpand();
					this.focus();
//				} else if( this.parent && (this.tree.options.rootVisible || this.parent.parent) ) {
				} else if( this.parent && this.parent.parent ) {
					this.parent.focus();
				}
				break;
			case 39: // <right>
				if( !this.bExpanded && (this.childList || this.data.isLazy) ) {
					this.toggleExpand();
					this.focus();
				} else if( this.childList ) {
					this.childList[0].focus();
				}
				break;
			case 38: // <up>
				sib = this.getPrevSibling();
				while( sib && sib.bExpanded && sib.childList ){
					sib = sib.childList[sib.childList.length-1];
				}
//				if( !sib && this.parent && (this.tree.options.rootVisible || this.parent.parent) )
				if( !sib && this.parent && this.parent.parent ){
					sib = this.parent;
				}
				if( sib ){
					sib.focus();
				}
				break;
			case 40: // <down>
				if( this.bExpanded && this.childList ) {
					sib = this.childList[0];
				} else {
					var parents = this._parentList(false, true);
					for(var i=parents.length-1; i>=0; i--) {
						sib = parents[i].getNextSibling();
						if( sib ){ break; }
					}
				}
				if( sib ){
					sib.focus();
				}
				break;
			default:
				handled = false;
		}
		// Return false, if handled, to prevent default processing
//		return !handled;
		if(handled){
			event.preventDefault();
		}
	},

	_onKeypress: function(event) {
		// onKeypress is only hooked to allow user callbacks.
		// We don't process it, because IE and Safari don't fire keypress for cursor keys.
//		this.tree.logDebug("dtnode.onKeypress(" + event.type + "): dtnode:" + this + ", charCode:" + event.charCode + ", keyCode: " + event.keyCode + ", which: " + event.which);
	},

	_onFocus: function(event) {
		// Handles blur and focus events.
//		this.tree.logDebug("dtnode.onFocus(%o): %o", event, this);
		var opts = this.tree.options;
		if ( event.type == "blur" || event.type == "focusout" ) {
			if ( opts.onBlur ){
				opts.onBlur.call(this.tree, this);
			}
			if( this.tree.tnFocused ){
				$(this.tree.tnFocused.span).removeClass(opts.classNames.focused);
			}
			this.tree.tnFocused = null;
			if( opts.persist ){
				$.cookie(opts.cookieId+"-focus", "", opts.cookie);
			}
		} else if ( event.type=="focus" || event.type=="focusin") {
			// Fix: sometimes the blur event is not generated
			if( this.tree.tnFocused && this.tree.tnFocused !== this ) {
				this.tree.logDebug("dtnode.onFocus: out of sync: curFocus: %o", this.tree.tnFocused);
				$(this.tree.tnFocused.span).removeClass(opts.classNames.focused);
			}
			this.tree.tnFocused = this;
			if ( opts.onFocus ){
				opts.onFocus.call(this.tree, this);
			}
			$(this.tree.tnFocused.span).addClass(opts.classNames.focused);
			if( opts.persist ){
				$.cookie(opts.cookieId+"-focus", this.data.key, opts.cookie);
			}
		}
		// TODO: return anything?
//		return false;
	},

	visit: function(fn, includeSelf) {
		// Call fn(node) for all child nodes. Stop iteration, if fn() returns false.
		var res = true;
		if( includeSelf === true ) {
			res = fn(this);
			if( res === false || res == "skip" ){
				return res;
			}
		}
		if(this.childList){
			for(var i=0, l=this.childList.length; i<l; i++){
				res = this.childList[i].visit(fn, true);
				if( res === false ){
					break;
				}
			}
		}
		return res;
	},

	visitParents: function(fn, includeSelf) {
		// Visit parent nodes (bottom up)
		if(includeSelf && fn(this) === false){
			return false;
		}
		var p = this.parent;
		while( p ) {
			if(fn(p) === false){
				return false;
			}
			p = p.parent;
		}
		return true;
	},

	remove: function() {
		// Remove this node
//		this.tree.logDebug ("%s.remove()", this);
		if ( this === this.tree.root ){
			throw "Cannot remove system root";
		}
		return this.parent.removeChild(this);
	},

	removeChild: function(tn) {
		// Remove tn from list of direct children.
		var ac = this.childList;
		if( ac.length == 1 ) {
			if( tn !== ac[0] ){
				throw "removeChild: invalid child";
			}
			return this.removeChildren();
		}
		if( tn === this.tree.activeNode ){
			tn.deactivate();
		}
		if( this.tree.options.persist ) {
			if( tn.bSelected ){
				this.tree.persistence.clearSelect(tn.data.key);
			}
			if ( tn.bExpanded ){
				this.tree.persistence.clearExpand(tn.data.key);
			}
		}
		tn.removeChildren(true);
//		this.div.removeChild(tn.div);
		this.ul.removeChild(tn.li);
		for(var i=0, l=ac.length; i<l; i++) {
			if( ac[i] === tn ) {
				this.childList.splice(i, 1);
//				delete tn;  // JSLint complained
				break;
			}
		}
	},

	removeChildren: function(isRecursiveCall, retainPersistence) {
		// Remove all child nodes (more efficiently than recursive remove())
		this.tree.logDebug("%s.removeChildren(%o)", this, isRecursiveCall);
		var tree = this.tree;
		var ac = this.childList;
		if( ac ) {
			for(var i=0, l=ac.length; i<l; i++) {
				var tn = ac[i];
				if ( tn === tree.activeNode && !retainPersistence ){
					tn.deactivate();
				}
				if( this.tree.options.persist && !retainPersistence ) {
					if( tn.bSelected ){
						this.tree.persistence.clearSelect(tn.data.key);
					}
					if ( tn.bExpanded ){
						this.tree.persistence.clearExpand(tn.data.key);
					}
				}
				tn.removeChildren(true, retainPersistence);
				if(this.ul){
					this.ul.removeChild(tn.li);
				}
/*
				try{
					this.ul.removeChild(tn.li);
				}catch(e){
					this.tree.logDebug("%s.removeChildren: couldnt remove LI", this, e);
				}
*/
//				delete tn;  JSLint complained
			}
			// Set to 'null' which is interpreted as 'not yet loaded' for lazy
			// nodes
			this.childList = null;
		}
		if( ! isRecursiveCall ) {
//			this._expand(false);
//			this.isRead = false;
			this.isLoading = false;
			this.render();
		}
	},

	setTitle: function(title) {
		this.fromDict({title: title});
	},

	reload: function(force) {
		throw "Use reloadChildren() instead";
	},

	reloadChildren: function(callback) {
		// Reload lazy content (expansion state is maintained).
		if( this.parent === null ){
			throw "Use tree.reload() instead";
		}else if( ! this.data.isLazy ){
			throw "node.reloadChildren() requires lazy nodes.";
		}
		// appendAjax triggers 'nodeLoaded' event.
		// We listen to this, if a callback was passed to reloadChildren
		if(callback){
			var self = this;
			var eventType = "nodeLoaded.dynatree." + this.tree.$tree.attr("id")
				+ "." + this.data.key;
			this.tree.$tree.bind(eventType, function(e, node, isOk){
				self.tree.$tree.unbind(eventType);
				self.tree.logInfo("loaded %o, %o, %o", e, node, isOk);
				if(node !== self){
					throw "got invalid load event";
				}
				callback.call(self.tree, node, isOk);
			});
		}
		// The expansion state is maintained
		this.removeChildren();
		this._loadContent();
//		if( this.bExpanded ) {
//			// Remove children first, to prevent effects being applied
//			this.removeChildren();
//			// then force re-expand to trigger lazy loading
////			this.expand(false);
////			this.expand(true);
//			this._loadContent();
//		} else {
//			this.removeChildren();
//			this._loadContent();
//		}
	},

	/**
	 * Make sure the node with a given key path is available in the tree.
	 */
	_loadKeyPath: function(keyPath, callback) {
		var tree = this.tree;
		tree.logDebug("%s._loadKeyPath(%s)", this, keyPath);
		if(keyPath === ""){
			throw "Key path must not be empty";
		}
		var segList = keyPath.split(tree.options.keyPathSeparator);
		if(segList[0] === ""){
			throw "Key path must be relative (don't start with '/')";
		}
		var seg = segList.shift();

		for(var i=0, l=this.childList.length; i < l; i++){
			var child = this.childList[i];
			if( child.data.key === seg ){
				if(segList.length === 0) {
					// Found the end node
					callback.call(tree, child, "ok");

				}else if(child.data.isLazy && (child.childList === null || child.childList === undefined)){
					tree.logDebug("%s._loadKeyPath(%s) -> reloading %s...", this, keyPath, child);
					var self = this;
					child.reloadChildren(function(node, isOk){
						// After loading, look for direct child with that key
						if(isOk){
							tree.logDebug("%s._loadKeyPath(%s) -> reloaded %s.", node, keyPath, node);
							callback.call(tree, child, "loaded");
							node._loadKeyPath(segList.join(tree.options.keyPathSeparator), callback);
						}else{
							tree.logWarning("%s._loadKeyPath(%s) -> reloadChildren() failed.", self, keyPath);
							callback.call(tree, child, "error");
						}
					}); // Note: this line gives a JSLint warning (Don't make functions within a loop)
					// we can ignore it, since it will only be exectuted once, the the loop is ended
					// See also http://stackoverflow.com/questions/3037598/how-to-get-around-the-jslint-error-dont-make-functions-within-a-loop
				} else {
					callback.call(tree, child, "loaded");
					// Look for direct child with that key
					child._loadKeyPath(segList.join(tree.options.keyPathSeparator), callback);
				}
				return;
			}
		}
		// Could not find key
		tree.logWarning("Node not found: " + seg);
		return;
	},

	resetLazy: function() {
		// Discard lazy content.
		if( this.parent === null ){
			throw "Use tree.reload() instead";
		}else if( ! this.data.isLazy ){
			throw "node.resetLazy() requires lazy nodes.";
		}
		this.expand(false);
		this.removeChildren();
	},

	_addChildNode: function(dtnode, beforeNode) {
		/**
		 * Internal function to add one single DynatreeNode as a child.
		 *
		 */
		var tree = this.tree;
		var opts = tree.options;
		var pers = tree.persistence;

//		tree.logDebug("%s._addChildNode(%o)", this, dtnode);

		// --- Update and fix dtnode attributes if necessary
		dtnode.parent = this;
//		if( beforeNode && (beforeNode.parent !== this || beforeNode === dtnode ) )
//			throw "<beforeNode> must be another child of <this>";

		// --- Add dtnode as a child
		if ( this.childList === null ) {
			this.childList = [];
		} else if( ! beforeNode ) {
			// Fix 'lastsib'
			if(this.childList.length > 0) {
				$(this.childList[this.childList.length-1].span).removeClass(opts.classNames.lastsib);
			}
		}
		if( beforeNode ) {
			var iBefore = $.inArray(beforeNode, this.childList);
			if( iBefore < 0 ){
				throw "<beforeNode> must be a child of <this>";
			}
			this.childList.splice(iBefore, 0, dtnode);
//			alert(this.childList);
		} else {
			// Append node
			this.childList.push(dtnode);
		}

		// --- Handle persistence
		// Initial status is read from cookies, if persistence is active and
		// cookies are already present.
		// Otherwise the status is read from the data attributes and then persisted.
		var isInitializing = tree.isInitializing();
		if( opts.persist && pers.cookiesFound && isInitializing ) {
			// Init status from cookies
//			tree.logDebug("init from cookie, pa=%o, dk=%o", pers.activeKey, dtnode.data.key);
			if( pers.activeKey == dtnode.data.key ){
				tree.activeNode = dtnode;
			}
			if( pers.focusedKey == dtnode.data.key ){
				tree.focusNode = dtnode;
			}
			dtnode.bExpanded = ($.inArray(dtnode.data.key, pers.expandedKeyList) >= 0);
			dtnode.bSelected = ($.inArray(dtnode.data.key, pers.selectedKeyList) >= 0);
//			tree.logDebug("    key=%o, bSelected=%o", dtnode.data.key, dtnode.bSelected);
		} else {
			// Init status from data (Note: we write the cookies after the init phase)
//			tree.logDebug("init from data");
			if( dtnode.data.activate ) {
				tree.activeNode = dtnode;
				if( opts.persist ){
					pers.activeKey = dtnode.data.key;
				}
			}
			if( dtnode.data.focus ) {
				tree.focusNode = dtnode;
				if( opts.persist ){
					pers.focusedKey = dtnode.data.key;
				}
			}
			dtnode.bExpanded = ( dtnode.data.expand === true ); // Collapsed by default
			if( dtnode.bExpanded && opts.persist ){
				pers.addExpand(dtnode.data.key);
			}
			dtnode.bSelected = ( dtnode.data.select === true ); // Deselected by default
/*
			Doesn't work, cause pers.selectedKeyList may be null
			if( dtnode.bSelected && opts.selectMode==1
				&& pers.selectedKeyList && pers.selectedKeyList.length>0 ) {
				tree.logWarning("Ignored multi-selection in single-mode for %o", dtnode);
				dtnode.bSelected = false; // Fixing bad input data (multi selection for mode:1)
			}
*/
			if( dtnode.bSelected && opts.persist ){
				pers.addSelect(dtnode.data.key);
			}
		}

		// Always expand, if it's below minExpandLevel
//		tree.logDebug ("%s._addChildNode(%o), l=%o", this, dtnode, dtnode.getLevel());
		if ( opts.minExpandLevel >= dtnode.getLevel() ) {
//			tree.logDebug ("Force expand for %o", dtnode);
			this.bExpanded = true;
		}

		// In multi-hier mode, update the parents selection state
		// issue #82: only if not initializing, because the children may not exist yet
//		if( !dtnode.data.isStatusNode && opts.selectMode==3 && !isInitializing )
//			dtnode._fixSelectionState();

		// In multi-hier mode, update the parents selection state
		if( dtnode.bSelected && opts.selectMode==3 ) {
			var p = this;
			while( p ) {
				if( !p.hasSubSel ){
					p._setSubSel(true);
				}
				p = p.parent;
			}
		}
		// render this node and the new child
		if ( tree.bEnableUpdate ){
			this.render();
		}
		return dtnode;
	},

	addChild: function(obj, beforeNode) {
		/**
		 * Add a node object as child.
		 *
		 * This should be the only place, where a DynaTreeNode is constructed!
		 * (Except for the root node creation in the tree constructor)
		 *
		 * @param obj A JS object (may be recursive) or an array of those.
		 * @param {DynaTreeNode} beforeNode (optional) sibling node.
		 *
		 * Data format: array of node objects, with optional 'children' attributes.
		 * [
		 *	{ title: "t1", isFolder: true, ... }
		 *	{ title: "t2", isFolder: true, ...,
		 *		children: [
		 *			{title: "t2.1", ..},
		 *			{..}
		 *			]
		 *	}
		 * ]
		 * A simple object is also accepted instead of an array.
		 *
		 */
//		this.tree.logDebug("%s.addChild(%o, %o)", this, obj, beforeNode);
		if(typeof(obj) == "string"){
			throw "Invalid data type for " + obj;
		}else if( !obj || obj.length === 0 ){ // Passed null or undefined or empty array
			return;
		}else if( obj instanceof DynaTreeNode ){
			return this._addChildNode(obj, beforeNode);
		}

		if( !obj.length ){ // Passed a single data object
			obj = [ obj ];
		}
		var prevFlag = this.tree.enableUpdate(false);

		var tnFirst = null;
		for (var i=0, l=obj.length; i<l; i++) {
			var data = obj[i];
			var dtnode = this._addChildNode(new DynaTreeNode(this, this.tree, data), beforeNode);
			if( !tnFirst ){
				tnFirst = dtnode;
			}
			// Add child nodes recursively
			if( data.children ){
				dtnode.addChild(data.children, null);
			}
		}
		this.tree.enableUpdate(prevFlag);
		return tnFirst;
	},

	append: function(obj) {
		this.tree.logWarning("node.append() is deprecated (use node.addChild() instead).");
		return this.addChild(obj, null);
	},

	appendAjax: function(ajaxOptions) {
		var self = this;
		this.removeChildren(false, true);
		this.setLazyNodeStatus(DTNodeStatus_Loading);
		// Debug feature: force a delay, to simulate slow loading...
		if(ajaxOptions.debugLazyDelay){
			var ms = ajaxOptions.debugLazyDelay;
			ajaxOptions.debugLazyDelay = 0;
			this.tree.logInfo("appendAjax: waiting for debugLazyDelay " + ms);
			setTimeout(function(){self.appendAjax(ajaxOptions);}, ms);
			return;
		}
		// Ajax option inheritance: $.ajaxSetup < $.ui.dynatree.prototype.options.ajaxDefaults < tree.options.ajaxDefaults < ajaxOptions
		var orgSuccess = ajaxOptions.success;
		var orgError = ajaxOptions.error;
		var eventType = "nodeLoaded.dynatree." + this.tree.$tree.attr("id")
			+ "." + this.data.key;
		var options = $.extend({}, this.tree.options.ajaxDefaults, ajaxOptions, {
			success: function(data, textStatus){
				// <this> is the request options
//				self.tree.logDebug("appendAjax().success");
				var prevPhase = self.tree.phase;
				self.tree.phase = "init";
				// postProcess is similar to the standard dataFilter hook,
				// but it is also called for JSONP
				if( options.postProcess ){
					data = options.postProcess.call(this, data, this.dataType);
				}
				if(!$.isArray(data) || data.length !== 0){
					self.addChild(data, null);
				}
				self.tree.phase = "postInit";
				if( orgSuccess ){
					orgSuccess.call(options, self);
				}
				self.tree.logInfo("trigger " + eventType);
				self.tree.$tree.trigger(eventType, [self, true]);
				self.tree.phase = prevPhase;
				// This should be the last command, so node.isLoading is true
				// while the callbacks run
				self.setLazyNodeStatus(DTNodeStatus_Ok);
				if($.isArray(data) && data.length === 0){
					// Set to [] which is interpreted as 'no children' for lazy
					// nodes
					self.childList = [];
					self.render();
				}
				},
			error: function(XMLHttpRequest, textStatus, errorThrown){
				// <this> is the request options
				self.tree.logWarning("appendAjax failed:", textStatus, ":\n", XMLHttpRequest, "\n", errorThrown);
				if( orgError ){
					orgError.call(options, self, XMLHttpRequest, textStatus, errorThrown);
				}
				self.tree.$tree.trigger(eventType, [self, false]);
				self.setLazyNodeStatus(DTNodeStatus_Error, {info: textStatus, tooltip: ""+errorThrown});
				}
		});
		$.ajax(options);
	},

	move: function(targetNode, mode) {
		/**Move this node to targetNode.
		 *  mode 'child': append this node as last child of targetNode.
		 *                This is the default. To be compatble with the D'n'd
		 *                hitMode, we also accept 'over'.
		 *  mode 'before': add this node as sibling before targetNode.
		 *  mode 'after': add this node as sibling after targetNode.
		 */
		var pos;
		if(this === targetNode){
			return;
		}
		if( !this.parent  ){
			throw "Cannot move system root";
		}
		if(mode === undefined || mode == "over"){
			mode = "child";
		}
		var prevParent = this.parent;
		var targetParent = (mode === "child") ? targetNode : targetNode.parent;
		if( targetParent.isDescendantOf(this) ){
			throw "Cannot move a node to it's own descendant";
		}
		// Unlink this node from current parent
		if( this.parent.childList.length == 1 ) {
			this.parent.childList = null;
			this.parent.bExpanded = false;
		} else {
			pos = $.inArray(this, this.parent.childList);
			if( pos < 0 ){
				throw "Internal error";
			}
			this.parent.childList.splice(pos, 1);
		}
		// Remove from source DOM parent
		this.parent.ul.removeChild(this.li);

		// Insert this node to target parent's child list
		this.parent = targetParent;
		if( targetParent.hasChildren() ) {
			switch(mode) {
			case "child":
				// Append to existing target children
				targetParent.childList.push(this);
				break;
			case "before":
				// Insert this node before target node
				pos = $.inArray(targetNode, targetParent.childList);
				if( pos < 0 ){
					throw "Internal error";
				}
				targetParent.childList.splice(pos, 0, this);
				break;
			case "after":
				// Insert this node after target node
				pos = $.inArray(targetNode, targetParent.childList);
				if( pos < 0 ){
					throw "Internal error";
				}
				targetParent.childList.splice(pos+1, 0, this);
				break;
			default:
				throw "Invalid mode " + mode;
			}
		} else {
			targetParent.childList = [ this ];
			// Parent has no <ul> tag yet:
			if( !targetParent.ul ) {
				// This is the parent's first child: create UL tag
				// (Hidden, because it will be
				targetParent.ul = document.createElement("ul");
				targetParent.ul.style.display = "none";
				targetParent.li.appendChild(targetParent.ul);
			}
		}
		// Add to target DOM parent
		targetParent.ul.appendChild(this.li);

		if( this.tree !== targetNode.tree ) {
			// Fix node.tree for all source nodes
			this.visit(function(node){
				node.tree = targetNode.tree;
			}, null, true);
			throw "Not yet implemented.";
		}
		// TODO: fix selection state
		// TODO: fix active state
		if( !prevParent.isDescendantOf(targetParent)) {
			prevParent.render();
		}
		if( !targetParent.isDescendantOf(prevParent) ) {
			targetParent.render();
		}
//		this.tree.redraw();
/*
		var tree = this.tree;
		var opts = tree.options;
		var pers = tree.persistence;


		// Always expand, if it's below minExpandLevel
//		tree.logDebug ("%s._addChildNode(%o), l=%o", this, dtnode, dtnode.getLevel());
		if ( opts.minExpandLevel >= dtnode.getLevel() ) {
//			tree.logDebug ("Force expand for %o", dtnode);
			this.bExpanded = true;
		}

		// In multi-hier mode, update the parents selection state
		// issue #82: only if not initializing, because the children may not exist yet
//		if( !dtnode.data.isStatusNode && opts.selectMode==3 && !isInitializing )
//			dtnode._fixSelectionState();

		// In multi-hier mode, update the parents selection state
		if( dtnode.bSelected && opts.selectMode==3 ) {
			var p = this;
			while( p ) {
				if( !p.hasSubSel )
					p._setSubSel(true);
				p = p.parent;
			}
		}
		// render this node and the new child
		if ( tree.bEnableUpdate )
			this.render();

		return dtnode;

*/
	},

	// --- end of class
	lastentry: undefined
};

/*************************************************************************
 * class DynaTreeStatus
 */

var DynaTreeStatus = Class.create();


DynaTreeStatus._getTreePersistData = function(cookieId, cookieOpts) {
	// Static member: Return persistence information from cookies
	var ts = new DynaTreeStatus(cookieId, cookieOpts);
	ts.read();
	return ts.toDict();
};
// Make available in global scope
getDynaTreePersistData = DynaTreeStatus._getTreePersistData;


DynaTreeStatus.prototype = {
	// Constructor
	initialize: function(cookieId, cookieOpts) {
		this._log("DynaTreeStatus: initialize");
		if( cookieId === undefined ){
			cookieId = $.ui.dynatree.prototype.options.cookieId;
		}
		cookieOpts = $.extend({}, $.ui.dynatree.prototype.options.cookie, cookieOpts);

		this.cookieId = cookieId;
		this.cookieOpts = cookieOpts;
		this.cookiesFound = undefined;
		this.activeKey = null;
		this.focusedKey = null;
		this.expandedKeyList = null;
		this.selectedKeyList = null;
	},
	// member functions
	_log: function(msg) {
		//	this.logDebug("_changeNodeList(%o): nodeList:%o, idx:%o", mode, nodeList, idx);
		Array.prototype.unshift.apply(arguments, ["debug"]);
		_log.apply(this, arguments);
	},
	read: function() {
		this._log("DynaTreeStatus: read");
		// Read or init cookies.
		this.cookiesFound = false;

		var cookie = $.cookie(this.cookieId + "-active");
		this.activeKey = ( cookie === null ) ? "" : cookie;
		if( cookie !== null ){
			this.cookiesFound = true;
		}
		cookie = $.cookie(this.cookieId + "-focus");
		this.focusedKey = ( cookie === null ) ? "" : cookie;
		if( cookie !== null ){
			this.cookiesFound = true;
		}
		cookie = $.cookie(this.cookieId + "-expand");
		this.expandedKeyList = ( cookie === null ) ? [] : cookie.split(",");
		if( cookie !== null ){
			this.cookiesFound = true;
		}
		cookie = $.cookie(this.cookieId + "-select");
		this.selectedKeyList = ( cookie === null ) ? [] : cookie.split(",");
		if( cookie !== null ){
			this.cookiesFound = true;
		}
	},
	write: function() {
		this._log("DynaTreeStatus: write");
		$.cookie(this.cookieId + "-active", ( this.activeKey === null ) ? "" : this.activeKey, this.cookieOpts);
		$.cookie(this.cookieId + "-focus", ( this.focusedKey === null ) ? "" : this.focusedKey, this.cookieOpts);
		$.cookie(this.cookieId + "-expand", ( this.expandedKeyList === null ) ? "" : this.expandedKeyList.join(","), this.cookieOpts);
		$.cookie(this.cookieId + "-select", ( this.selectedKeyList === null ) ? "" : this.selectedKeyList.join(","), this.cookieOpts);
	},
	addExpand: function(key) {
		this._log("addExpand(%o)", key);
		if( $.inArray(key, this.expandedKeyList) < 0 ) {
			this.expandedKeyList.push(key);
			$.cookie(this.cookieId + "-expand", this.expandedKeyList.join(","), this.cookieOpts);
		}
	},
	clearExpand: function(key) {
		this._log("clearExpand(%o)", key);
		var idx = $.inArray(key, this.expandedKeyList);
		if( idx >= 0 ) {
			this.expandedKeyList.splice(idx, 1);
			$.cookie(this.cookieId + "-expand", this.expandedKeyList.join(","), this.cookieOpts);
		}
	},
	addSelect: function(key) {
		this._log("addSelect(%o)", key);
		if( $.inArray(key, this.selectedKeyList) < 0 ) {
			this.selectedKeyList.push(key);
			$.cookie(this.cookieId + "-select", this.selectedKeyList.join(","), this.cookieOpts);
		}
	},
	clearSelect: function(key) {
		this._log("clearSelect(%o)", key);
		var idx = $.inArray(key, this.selectedKeyList);
		if( idx >= 0 ) {
			this.selectedKeyList.splice(idx, 1);
			$.cookie(this.cookieId + "-select", this.selectedKeyList.join(","), this.cookieOpts);
		}
	},
	isReloading: function() {
		return this.cookiesFound === true;
	},
	toDict: function() {
		return {
			cookiesFound: this.cookiesFound,
			activeKey: this.activeKey,
			focusedKey: this.activeKey,
			expandedKeyList: this.expandedKeyList,
			selectedKeyList: this.selectedKeyList
		};
	},
	// --- end of class
	lastentry: undefined
};


/*************************************************************************
 * class DynaTree
 */

var DynaTree = Class.create();

// --- Static members ----------------------------------------------------------

DynaTree.version = "$Version: 1.1.1$";
/*
DynaTree._initTree = function() {
};

DynaTree._bind = function() {
};
*/
//--- Class members ------------------------------------------------------------

DynaTree.prototype = {
	// Constructor
//	initialize: function(divContainer, options) {
	initialize: function($widget) {
		// instance members
		this.phase = "init";
		this.$widget = $widget;
		this.options = $widget.options;
		this.$tree = $widget.element;
		this.timer = null;
		// find container element
		this.divTree = this.$tree.get(0);

//		var parentPos = $(this.divTree).parent().offset();
//		this.parentTop = parentPos.top;
//		this.parentLeft = parentPos.left;

		_initDragAndDrop(this);
	},

	// member functions

	_load: function(callback) {
		var $widget = this.$widget;
		var opts = this.options;
		this.bEnableUpdate = true;
		this._nodeCount = 1;
		this.activeNode = null;
		this.focusNode = null;

		// Some deprecation warnings to help with migration
		if( opts.rootVisible !== undefined ){
			_log("warn", "Option 'rootVisible' is no longer supported.");
		}
//		if( opts.title  !== undefined ){
//			_log("warn", "Option 'title' is no longer supported.");
//		}
		if( opts.minExpandLevel < 1 ) {
			_log("warn", "Option 'minExpandLevel' must be >= 1.");
			opts.minExpandLevel = 1;
		}
//		_log("warn", "jQuery.support.boxModel " + jQuery.support.boxModel);

		// If a 'options.classNames' dictionary was passed, still use defaults
		// for undefined classes:
		if( opts.classNames !== $.ui.dynatree.prototype.options.classNames ) {
			opts.classNames = $.extend({}, $.ui.dynatree.prototype.options.classNames, opts.classNames);
		}
		if( opts.ajaxDefaults !== $.ui.dynatree.prototype.options.ajaxDefaults ) {
			opts.ajaxDefaults = $.extend({}, $.ui.dynatree.prototype.options.ajaxDefaults, opts.ajaxDefaults);
		}
		if( opts.dnd !== $.ui.dynatree.prototype.options.dnd ) {
			opts.dnd = $.extend({}, $.ui.dynatree.prototype.options.dnd, opts.dnd);
		}
		// Guess skin path, if not specified
		if(!opts.imagePath) {
			$("script").each( function () {
				var _rexDtLibName = /.*dynatree[^\/]*\.js$/i;
				if( this.src.search(_rexDtLibName) >= 0 ) {
					if( this.src.indexOf("/")>=0 ){ // issue #47
						opts.imagePath = this.src.slice(0, this.src.lastIndexOf("/")) + "/skin/";
					}else{
						opts.imagePath = "skin/";
					}
					logMsg("Guessing imagePath from '%s': '%s'", this.src, opts.imagePath);
					return false; // first match
				}
			});
		}

		this.persistence = new DynaTreeStatus(opts.cookieId, opts.cookie);
		if( opts.persist ) {
			if( !$.cookie ){
				_log("warn", "Please include jquery.cookie.js to use persistence.");
			}
			this.persistence.read();
		}
		this.logDebug("DynaTree.persistence: %o", this.persistence.toDict());

		// Cached tag strings
		this.cache = {
			tagEmpty: "<span class='" + opts.classNames.empty + "'></span>",
			tagVline: "<span class='" + opts.classNames.vline + "'></span>",
			tagExpander: "<span class='" + opts.classNames.expander + "'></span>",
			tagConnector: "<span class='" + opts.classNames.connector + "'></span>",
			tagNodeIcon: "<span class='" + opts.classNames.nodeIcon + "'></span>",
			tagCheckbox: "<span class='" + opts.classNames.checkbox + "'></span>",
			lastentry: undefined
		};

		// Clear container, in case it contained some 'waiting' or 'error' text
		// for clients that don't support JS.
		// We don't do this however, if we try to load from an embedded UL element.
		if( opts.children || (opts.initAjax && opts.initAjax.url) || opts.initId ){
			$(this.divTree).empty();
		}
		var $ulInitialize = this.$tree.find(">ul:first").hide();

		// Create the root element
		this.tnRoot = new DynaTreeNode(null, this, {});
		this.tnRoot.bExpanded = true;
		this.tnRoot.render();
		this.divTree.appendChild(this.tnRoot.ul);

		var root = this.tnRoot;
		var isReloading = ( opts.persist && this.persistence.isReloading() );
		var isLazy = false;
		var prevFlag = this.enableUpdate(false);

		this.logDebug("Dynatree._load(): read tree structure...");

		// Init tree structure
		if( opts.children ) {
			// Read structure from node array
			root.addChild(opts.children);

		} else if( opts.initAjax && opts.initAjax.url ) {
			// Init tree from AJAX request
			isLazy = true;
			root.data.isLazy = true;
			this._reloadAjax(callback);

		} else if( opts.initId ) {
			// Init tree from another UL element
			this._createFromTag(root, $("#"+opts.initId));

		} else {
			// Init tree from the first UL element inside the container <div>
//			var $ul = this.$tree.find(">ul:first").hide();
			this._createFromTag(root, $ulInitialize);
			$ulInitialize.remove();
		}

		this._checkConsistency();
		// Render html markup
		this.logDebug("Dynatree._load(): render nodes...");
		this.enableUpdate(prevFlag);

		// bind event handlers
		this.logDebug("Dynatree._load(): bind events...");
		this.$widget.bind();

		// --- Post-load processing
		this.logDebug("Dynatree._load(): postInit...");
		this.phase = "postInit";

		// In persist mode, make sure that cookies are written, even if they are empty
		if( opts.persist ) {
			this.persistence.write();
		}
		// Set focus, if possible (this will also fire an event and write a cookie)
		if( this.focusNode && this.focusNode.isVisible() ) {
			this.logDebug("Focus on init: %o", this.focusNode);
			this.focusNode.focus();
		}
		if( !isLazy && opts.onPostInit ) {
			opts.onPostInit.call(this, isReloading, false);
		}
		this.phase = "idle";
	},

//	_setNoUpdate: function(silent) {
//		// TODO: set options to disable and re-enable updates while loading
//		var opts = this.options;
//		var prev = {
//			fx: opts.fx,
//			autoFocus: opts.autoFocus,
//			autoCollapse: opts.autoCollapse };
//		if(silent === true){
//			opts.autoFocus = false;
//			opts.fx = null;
//			opts.autoCollapse = false;
//		} else {
//			opts.autoFocus = silent.autoFocus;
//			opts.fx = silent.fx;
//			opts.autoCollapse = silent.autoCollapse;
//		}
//		return prev;
//	},

	_reloadAjax: function(callback) {
		// Reload
		var opts = this.options;
		if( ! opts.initAjax || ! opts.initAjax.url ){
			throw "tree.reload() requires 'initAjax' mode.";
		}
		var pers = this.persistence;
		var ajaxOpts = $.extend({}, opts.initAjax);
		// Append cookie info to the request
//		this.logDebug("reloadAjax: key=%o, an.key:%o", pers.activeKey, this.activeNode?this.activeNode.data.key:"?");
		if( ajaxOpts.addActiveKey ){
			ajaxOpts.data.activeKey = pers.activeKey;
		}
		if( ajaxOpts.addFocusedKey ){
			ajaxOpts.data.focusedKey = pers.focusedKey;
		}
		if( ajaxOpts.addExpandedKeyList ){
			ajaxOpts.data.expandedKeyList = pers.expandedKeyList.join(",");
		}
		if( ajaxOpts.addSelectedKeyList ){
			ajaxOpts.data.selectedKeyList = pers.selectedKeyList.join(",");
		}
		// Set up onPostInit callback to be called when Ajax returns
		if( opts.onPostInit ) {
			if( ajaxOpts.success ){
				this.logWarning("initAjax: success callback is ignored when onPostInit was specified.");
			}
			if( ajaxOpts.error ){
				this.logWarning("initAjax: error callback is ignored when onPostInit was specified.");
			}
			var isReloading = pers.isReloading();
			ajaxOpts.success = function(dtnode) {
				opts.onPostInit.call(dtnode.tree, isReloading, false);
				if(callback){
					callback.call(dtnode.tree, "ok");
				}
			};
			ajaxOpts.error = function(dtnode) {
				opts.onPostInit.call(dtnode.tree, isReloading, true);
				if(callback){
					callback.call(dtnode.tree, "error");
				}
			};
		}
		this.logDebug("Dynatree._init(): send Ajax request...");
		this.tnRoot.appendAjax(ajaxOpts);
	},

	toString: function() {
//		return "DynaTree '" + this.options.title + "'";
		return "Dynatree '" + this.$tree.attr("id") + "'";
	},

	toDict: function() {
		return this.tnRoot.toDict(true);
	},

	serializeArray: function(stopOnParents) {
		// Return a JavaScript array of objects, ready to be encoded as a JSON
		// string for selected nodes
		var nodeList = this.getSelectedNodes(stopOnParents),
			name = this.$tree.attr("name") || this.$tree.attr("id"),
			arr = [];
		for(var i=0, l=nodeList.length; i<l; i++){
			arr.push({name: name, value: nodeList[i].data.key});
		}
		return arr;
	},

	getPersistData: function() {
		return this.persistence.toDict();
	},

	logDebug: function(msg) {
		if( this.options.debugLevel >= 2 ) {
			Array.prototype.unshift.apply(arguments, ["debug"]);
			_log.apply(this, arguments);
		}
	},

	logInfo: function(msg) {
		if( this.options.debugLevel >= 1 ) {
			Array.prototype.unshift.apply(arguments, ["info"]);
			_log.apply(this, arguments);
		}
	},

	logWarning: function(msg) {
		Array.prototype.unshift.apply(arguments, ["warn"]);
		_log.apply(this, arguments);
	},

	isInitializing: function() {
		return ( this.phase=="init" || this.phase=="postInit" );
	},
	isReloading: function() {
		return ( this.phase=="init" || this.phase=="postInit" ) && this.options.persist && this.persistence.cookiesFound;
	},
	isUserEvent: function() {
		return ( this.phase=="userEvent" );
	},

	redraw: function() {
//		this.logDebug("dynatree.redraw()...");
		this.tnRoot.render(false, false);
//		this.logDebug("dynatree.redraw() done.");
	},
	renderInvisibleNodes: function() {
		this.tnRoot.render(false, true);
	},
	reload: function(callback) {
		this._load(callback);
	},

	getRoot: function() {
		return this.tnRoot;
	},

	enable: function() {
		this.$widget.enable();
	},

	disable: function() {
		this.$widget.disable();
	},

	getNodeByKey: function(key) {
		// Search the DOM by element ID (assuming this is faster than traversing all nodes).
		// $("#...") has problems, if the key contains '.', so we use getElementById()
		var el = document.getElementById(this.options.idPrefix + key);
		if( el ){
			return el.dtnode ? el.dtnode : null;
		}
		// Not found in the DOM, but still may be in an unrendered part of tree
		var match = null;
		this.visit(function(node){
//			window.console.log("%s", node);
			if(node.data.key == key) {
				match = node;
				return false;
			}
		}, true);
		return match;
	},

	getActiveNode: function() {
		return this.activeNode;
	},

	reactivate: function(setFocus) {
		// Re-fire onQueryActivate and onActivate events.
		var node = this.activeNode;
//		this.logDebug("reactivate %o", node);
		if( node ) {
			this.activeNode = null; // Force re-activating
			node.activate();
			if( setFocus ){
				node.focus();
			}
		}
	},

	getSelectedNodes: function(stopOnParents) {
		var nodeList = [];
		this.tnRoot.visit(function(node){
			if( node.bSelected ) {
				nodeList.push(node);
				if( stopOnParents === true ){
					return "skip"; // stop processing this branch
				}
			}
		});
		return nodeList;
	},

	activateKey: function(key) {
		var dtnode = (key === null) ? null : this.getNodeByKey(key);
		if( !dtnode ) {
			if( this.activeNode ){
				this.activeNode.deactivate();
			}
			this.activeNode = null;
			return null;
		}
		dtnode.focus();
		dtnode.activate();
		return dtnode;
	},

	loadKeyPath: function(keyPath, callback) {
		var segList = keyPath.split(this.options.keyPathSeparator);
		// Remove leading '/'
		if(segList[0] === ""){
			segList.shift();
		}
		// Remove leading system root key
		if(segList[0] == this.tnRoot.data.key){
			this.logDebug("Removed leading root key.");
			segList.shift();
		}
		keyPath = segList.join(this.options.keyPathSeparator);
		return this.tnRoot._loadKeyPath(keyPath, callback);
	},

	selectKey: function(key, select) {
		var dtnode = this.getNodeByKey(key);
		if( !dtnode ){
			return null;
		}
		dtnode.select(select);
		return dtnode;
	},

	enableUpdate: function(bEnable) {
		if ( this.bEnableUpdate==bEnable ){
			return bEnable;
		}
		this.bEnableUpdate = bEnable;
		if ( bEnable ){
			this.redraw();
		}
		return !bEnable; // return previous value
	},

	count: function() {
		return this.tnRoot.countChildren();
	},

	visit: function(fn, includeRoot) {
		return this.tnRoot.visit(fn, includeRoot);
	},

	_createFromTag: function(parentTreeNode, $ulParent) {
		// Convert a <UL>...</UL> list into children of the parent tree node.
		var self = this;
/*
TODO: better?
		this.$lis = $("li:has(a[href])", this.element);
		this.$tabs = this.$lis.map(function() { return $("a", this)[0]; });
 */
		$ulParent.find(">li").each(function() {
			var $li = $(this);
			var $liSpan = $li.find(">span:first");
			var title;
			if( $liSpan.length ) {
				// If a <li><span> tag is specified, use it literally.
				title = $liSpan.html();
			} else {
				// If only a <li> tag is specified, use the trimmed string up to the next child <ul> tag.
				title = $li.html();
				var iPos = title.search(/<ul/i);
				if( iPos>=0 ){
					title = $.trim(title.substring(0, iPos));
				}else{
					title = $.trim(title);
				}
//				self.logDebug("%o", title);
			}
			// Parse node options from ID, title and class attributes
			var data = {
				title: title,
				isFolder: $li.hasClass("folder"),
				isLazy: $li.hasClass("lazy"),
				expand: $li.hasClass("expanded"),
				select: $li.hasClass("selected"),
				activate: $li.hasClass("active"),
				focus: $li.hasClass("focused"),
				noLink: $li.hasClass("noLink")
			};
			if( $li.attr("title") ){
				data.tooltip = $li.attr("title");
			}
			if( $li.attr("id") ){
				data.key = $li.attr("id");
			}
			// If a data attribute is present, evaluate as a JavaScript object
			if( $li.attr("data") ) {
				var dataAttr = $.trim($li.attr("data"));
				if( dataAttr ) {
					if( dataAttr.charAt(0) != "{" ){
						dataAttr = "{" + dataAttr + "}";
					}
					try {
						$.extend(data, eval("(" + dataAttr + ")"));
					} catch(e) {
						throw ("Error parsing node data: " + e + "\ndata:\n'" + dataAttr + "'");
					}
				}
			}
			var childNode = parentTreeNode.addChild(data);
			// Recursive reading of child nodes, if LI tag contains an UL tag
			var $ul = $li.find(">ul:first");
			if( $ul.length ) {
				self._createFromTag(childNode, $ul); // must use 'self', because 'this' is the each() context
			}
		});
	},

	_checkConsistency: function() {
//		this.logDebug("tree._checkConsistency() NOT IMPLEMENTED - %o", this);
	},

	_setDndStatus: function(sourceNode, targetNode, helper, hitMode, accept) {
		// hitMode: 'after', 'before', 'over', 'out', 'start', 'stop'
		var $source = sourceNode ? $(sourceNode.span) : null;
		var $target = $(targetNode.span);
		if( !this.$dndMarker ) {
			this.$dndMarker = $("<div id='dynatree-drop-marker'></div>")
				.hide()
				.prependTo($(this.divTree).parent());
//				.prependTo("body");
//			logMsg("Creating marker: %o", this.$dndMarker);
		}
/*
		if(hitMode === "start"){
		}
		if(hitMode === "stop"){
//			sourceNode.removeClass("dynatree-drop-target");
		}
*/
//		this.$dndMarker.attr("class", hitMode);
		if(hitMode === "after" || hitMode === "before" || hitMode === "over"){
//			$source && $source.addClass("dynatree-drag-source");
			var pos = $target.position();
			switch(hitMode){
			case "before":
				this.$dndMarker.removeClass("dynatree-drop-after dynatree-drop-over");
				this.$dndMarker.addClass("dynatree-drop-before");
				pos.top -= 8;
				break;
			case "after":
				this.$dndMarker.removeClass("dynatree-drop-before dynatree-drop-over");
				this.$dndMarker.addClass("dynatree-drop-after");
				pos.top += 8;
				break;
			default:
				this.$dndMarker.removeClass("dynatree-drop-after dynatree-drop-before");
				this.$dndMarker.addClass("dynatree-drop-over");
				$target.addClass("dynatree-drop-target");
				pos.left += 8;
			}
			this.$dndMarker.css({"left": (pos.left) + "px", "top": (pos.top) + "px" })
				.show();
//			helper.addClass("dynatree-drop-hover");
		} else {
//			$source && $source.removeClass("dynatree-drag-source");
			$target.removeClass("dynatree-drop-target");
			this.$dndMarker.hide();
//			helper.removeClass("dynatree-drop-hover");
		}
		if(hitMode === "after"){
			$target.addClass("dynatree-drop-after");
		} else {
			$target.removeClass("dynatree-drop-after");
		}
		if(hitMode === "before"){
			$target.addClass("dynatree-drop-before");
		} else {
			$target.removeClass("dynatree-drop-before");
		}
		if(accept === true){
			if($source){
				$source.addClass("dynatree-drop-accept");
			}
			$target.addClass("dynatree-drop-accept");
			helper.addClass("dynatree-drop-accept");
		}else{
			if($source){
				$source.removeClass("dynatree-drop-accept");
			}
			$target.removeClass("dynatree-drop-accept");
			helper.removeClass("dynatree-drop-accept");
		}
		if(accept === false){
			if($source){
				$source.addClass("dynatree-drop-reject");
			}
			$target.addClass("dynatree-drop-reject");
			helper.addClass("dynatree-drop-reject");
		}else{
			if($source){
				$source.removeClass("dynatree-drop-reject");
			}
			$target.removeClass("dynatree-drop-reject");
			helper.removeClass("dynatree-drop-reject");
		}
	},

	_onDragEvent: function(eventName, node, otherNode, event, ui, draggable) {
		/**
		 * Handles drag'n'drop functionality.
		 *
		 * A standard jQuery drag-and-drop process may generate these calls:
		 *
		 * draggable helper():
		 *     _onDragEvent("helper", sourceNode, null, event, null, null);
		 * start:
		 *     _onDragEvent("start", sourceNode, null, event, ui, draggable);
		 * drag:
		 *     _onDragEvent("leave", prevTargetNode, sourceNode, event, ui, draggable);
		 *     _onDragEvent("over", targetNode, sourceNode, event, ui, draggable);
		 *     _onDragEvent("enter", targetNode, sourceNode, event, ui, draggable);
		 * stop:
		 *     _onDragEvent("drop", targetNode, sourceNode, event, ui, draggable);
		 *     _onDragEvent("leave", targetNode, sourceNode, event, ui, draggable);
		 *     _onDragEvent("stop", sourceNode, null, event, ui, draggable);
		 */
//		if(eventName !== "over"){
//			this.logDebug("tree._onDragEvent(%s, %o, %o) - %o", eventName, node, otherNode, this);
//		}
		var opts = this.options;
		var dnd = this.options.dnd;
		var res = null;
		var nodeTag = $(node.span);
		var hitMode;

		switch (eventName) {
		case "helper":
			// Only event and node argument is available
			var helper = $("<div class='dynatree-drag-helper'><span class='dynatree-drag-helper-img' /></div>")
				.append($(event.target).closest('a').clone());
			// Attach node reference to helper object
			helper.data("dtSourceNode", node);
//			this.logDebug("helper.sourceNode=%o", helper.data("dtSourceNode"));
			res = helper;
			break;
		case "start":
			if(node.isStatusNode()) {
				res = false;
			} else if(dnd.onDragStart) {
				res = dnd.onDragStart(node);
			}
			if(res === false) {
				this.logDebug("tree.onDragStart() cancelled");
				//draggable._clear();
				// NOTE: the return value seems to be ignored (drag is not canceled, when false is returned)
				ui.helper.trigger("mouseup");
				ui.helper.hide();
			} else {
				nodeTag.addClass("dynatree-drag-source");
			}
			break;
		case "enter":
			res = dnd.onDragEnter ? dnd.onDragEnter(node, otherNode) : null;
			res = {
				over: (res !== false) && ((res === true) || (res === "over") || $.inArray("over", res) >= 0),
				before: (res !== false) && ((res === true) || (res === "before") || $.inArray("before", res) >= 0),
				after: (res !== false) && ((res === true) || (res === "after") || $.inArray("after", res) >= 0)
			};
			ui.helper.data("enterResponse", res);
//			this.logDebug("helper.enterResponse: %o", res);
			break;
		case "over":
			var enterResponse = ui.helper.data("enterResponse");
			hitMode = null;
			if(enterResponse === false){
				// Don't call onDragOver if onEnter returned false.
				break;
			} else if(typeof enterResponse === "string") {
				// Use hitMode from onEnter if provided.
				hitMode = enterResponse;
			} else {
				// Calculate hitMode from relative cursor position.
				var nodeOfs = nodeTag.offset();
//				var relPos = { x: event.clientX - nodeOfs.left,
//							y: event.clientY - nodeOfs.top };
//				nodeOfs.top += this.parentTop;
//				nodeOfs.left += this.parentLeft;
				var relPos = { x: event.pageX - nodeOfs.left,
							   y: event.pageY - nodeOfs.top };
				var relPos2 = { x: relPos.x / nodeTag.width(),
								y: relPos.y / nodeTag.height() };
//				this.logDebug("event.page: %s/%s", event.pageX, event.pageY);
//				this.logDebug("event.client: %s/%s", event.clientX, event.clientY);
//				this.logDebug("nodeOfs: %s/%s", nodeOfs.left, nodeOfs.top);
////				this.logDebug("parent: %s/%s", this.parentLeft, this.parentTop);
//				this.logDebug("relPos: %s/%s", relPos.x, relPos.y);
//				this.logDebug("relPos2: %s/%s", relPos2.x, relPos2.y);
				if( enterResponse.after && relPos2.y > 0.75 ){
					hitMode = "after";
				} else if(!enterResponse.over && enterResponse.after && relPos2.y > 0.5 ){
					hitMode = "after";
				} else if(enterResponse.before && relPos2.y <= 0.25) {
					hitMode = "before";
				} else if(!enterResponse.over && enterResponse.before && relPos2.y <= 0.5) {
					hitMode = "before";
				} else if(enterResponse.over) {
					hitMode = "over";
				}
				// Prevent no-ops like 'before source node'
				// TODO: these are no-ops when moving nodes, but not in copy mode
				if( dnd.preventVoidMoves ){
					if(node === otherNode){
//						this.logDebug("    drop over source node prevented");
						hitMode = null;
					}else if(hitMode === "before" && otherNode && node === otherNode.getNextSibling()){
//						this.logDebug("    drop after source node prevented");
						hitMode = null;
					}else if(hitMode === "after" && otherNode && node === otherNode.getPrevSibling()){
//						this.logDebug("    drop before source node prevented");
						hitMode = null;
					}else if(hitMode === "over" && otherNode
							&& otherNode.parent === node && otherNode.isLastSibling() ){
//						this.logDebug("    drop last child over own parent prevented");
						hitMode = null;
					}
				}
//				this.logDebug("hitMode: %s - %s - %s", hitMode, (node.parent === otherNode), node.isLastSibling());
				ui.helper.data("hitMode", hitMode);
			}
			// Auto-expand node (only when 'over' the node, not 'before', or 'after')
			if(hitMode === "over"
				&& dnd.autoExpandMS && node.hasChildren() !== false && !node.bExpanded) {
				node.scheduleAction("expand", dnd.autoExpandMS);
			}
			if(hitMode && dnd.onDragOver){
				res = dnd.onDragOver(node, otherNode, hitMode);
			}
			this._setDndStatus(otherNode, node, ui.helper, hitMode, res!==false);
			break;
		case "drop":
			hitMode = ui.helper.data("hitMode");
			if(hitMode && dnd.onDrop){
				dnd.onDrop(node, otherNode, hitMode, ui, draggable);
			}
			break;
		case "leave":
			// Cancel pending expand request
			node.scheduleAction("cancel");
			ui.helper.data("enterResponse", null);
			ui.helper.data("hitMode", null);
			this._setDndStatus(otherNode, node, ui.helper, "out", undefined);
			if(dnd.onDragLeave){
				dnd.onDragLeave(node, otherNode);
			}
			break;
		case "stop":
			nodeTag.removeClass("dynatree-drag-source");
			if(dnd.onDragStop){
				dnd.onDragStop(node);
			}
			break;
		default:
			throw "Unsupported drag event: " + eventName;
		}
		return res;
	},

	cancelDrag: function() {
		 var dd = $.ui.ddmanager.current;
		 if(dd){
			 dd.cancel();
		 }
	},

	// --- end of class
	lastentry: undefined
};

/*************************************************************************
 * Widget $(..).dynatree
 */

$.widget("ui.dynatree", {
/*
	init: function() {
		// ui.core 1.6 renamed init() to _init(): this stub assures backward compatibility
		_log("warn", "ui.dynatree.init() was called; you should upgrade to jquery.ui.core.js v1.8 or higher.");
		return this._init();
	},
 */
	_init: function() {
		if( parseFloat($.ui.version) < 1.8 ) {
			// jquery.ui.core 1.8 renamed _init() to _create(): this stub assures backward compatibility
			_log("warn", "ui.dynatree._init() was called; you should upgrade to jquery.ui.core.js v1.8 or higher.");
			return this._create();
		}
		// jquery.ui.core 1.8 still uses _init() to perform "default functionality"
		_log("debug", "ui.dynatree._init() was called; no current default functionality.");
	},

	_create: function() {
		logMsg("Dynatree._create(): version='%s', debugLevel=%o.", DynaTree.version, this.options.debugLevel);

		var opts = this.options;
		// The widget framework supplies this.element and this.options.
		this.options.event += ".dynatree"; // namespace event

		var divTree = this.element.get(0);
/*		// Clear container, in case it contained some 'waiting' or 'error' text
		// for clients that don't support JS
		if( opts.children || (opts.initAjax && opts.initAjax.url) || opts.initId )
			$(divTree).empty();
*/
		// Create the DynaTree object
		this.tree = new DynaTree(this);
		this.tree._load();
		this.tree.logDebug("Dynatree._init(): done.");
	},

	bind: function() {
		// Prevent duplicate binding
		this.unbind();

		var eventNames = "click.dynatree dblclick.dynatree";
		if( this.options.keyboard ){
			// Note: leading ' '!
			eventNames += " keypress.dynatree keydown.dynatree";
		}
		this.element.bind(eventNames, function(event){
			var dtnode = getDtNodeFromElement(event.target);
			if( !dtnode ){
				return true;  // Allow bubbling of other events
			}
			var tree = dtnode.tree;
			var o = tree.options;
			tree.logDebug("event(%s): dtnode: %s", event.type, dtnode);
			var prevPhase = tree.phase;
			tree.phase = "userEvent";
			try {
				switch(event.type) {
				case "click":
					return ( o.onClick && o.onClick.call(tree, dtnode, event)===false ) ? false : dtnode._onClick(event);
				case "dblclick":
					return ( o.onDblClick && o.onDblClick.call(tree, dtnode, event)===false ) ? false : dtnode._onDblClick(event);
				case "keydown":
					return ( o.onKeydown && o.onKeydown.call(tree, dtnode, event)===false ) ? false : dtnode._onKeydown(event);
				case "keypress":
					return ( o.onKeypress && o.onKeypress.call(tree, dtnode, event)===false ) ? false : dtnode._onKeypress(event);
				}
			} catch(e) {
				var _ = null; // issue 117
				tree.logWarning("bind(%o): dtnode: %o, error: %o", event, dtnode, e);
			} finally {
				tree.phase = prevPhase;
			}
		});

		// focus/blur don't bubble, i.e. are not delegated to parent <div> tags,
		// so we use the addEventListener capturing phase.
		// See http://www.howtocreate.co.uk/tutorials/javascript/domevents
		function __focusHandler(event) {
			// Handles blur and focus.
			// Fix event for IE:
			// doesn't pass JSLint:
//			event = arguments[0] = $.event.fix( event || window.event );
			// what jQuery does:
//			var args = jQuery.makeArray( arguments );
//			event = args[0] = jQuery.event.fix( event || window.event );
			event = $.event.fix( event || window.event );
			var dtnode = getDtNodeFromElement(event.target);
			return dtnode ? dtnode._onFocus(event) : false;
		}
		var div = this.tree.divTree;
		if( div.addEventListener ) {
			div.addEventListener("focus", __focusHandler, true);
			div.addEventListener("blur", __focusHandler, true);
		} else {
			div.onfocusin = div.onfocusout = __focusHandler;
		}
		// EVENTS
		// disable click if event is configured to something else
//		if (!(/^click/).test(o.event))
//			this.$tabs.bind("click.tabs", function() { return false; });

	},

	unbind: function() {
		this.element.unbind(".dynatree");
	},

/* TODO: we could handle option changes during runtime here (maybe to re-render, ...)
	setData: function(key, value) {
		this.tree.logDebug("dynatree.setData('" + key + "', '" + value + "')");
	},
*/
	enable: function() {
		this.bind();
		// Call default disable(): remove -disabled from css:
		$.Widget.prototype.enable.apply(this, arguments);
	},

	disable: function() {
		this.unbind();
		// Call default disable(): add -disabled to css:
		$.Widget.prototype.disable.apply(this, arguments);
	},

	// --- getter methods (i.e. NOT returning a reference to $)
	getTree: function() {
		return this.tree;
	},

	getRoot: function() {
		return this.tree.getRoot();
	},

	getActiveNode: function() {
		return this.tree.getActiveNode();
	},

	getSelectedNodes: function() {
		return this.tree.getSelectedNodes();
	},

	// ------------------------------------------------------------------------
	lastentry: undefined
});


// The following methods return a value (thus breaking the jQuery call chain):
if( parseFloat($.ui.version) < 1.8 ) {
	$.ui.dynatree.getter = "getTree getRoot getActiveNode getSelectedNodes";
}


/*******************************************************************************
 * Plugin default options:
 */
$.ui.dynatree.prototype.options = {
	title: "Dynatree", // Tree's name (only used for debug outpu)
	minExpandLevel: 1, // 1: root node is not collapsible
	imagePath: null, // Path to a folder containing icons. Defaults to 'skin/' subdirectory.
	children: null, // Init tree structure from this object array.
	initId: null, // Init tree structure from a <ul> element with this ID.
	initAjax: null, // Ajax options used to initialize the tree strucuture.
	autoFocus: true, // Set focus to first child, when expanding or lazy-loading.
	keyboard: true, // Support keyboard navigation.
	persist: false, // Persist expand-status to a cookie
	autoCollapse: false, // Automatically collapse all siblings, when a node is expanded.
	clickFolderMode: 3, // 1:activate, 2:expand, 3:activate and expand
	activeVisible: true, // Make sure, active nodes are visible (expanded).
	checkbox: false, // Show checkboxes.
	selectMode: 2, // 1:single, 2:multi, 3:multi-hier
	fx: null, // Animations, e.g. null or { height: "toggle", duration: 200 }
	noLink: false, // Use <span> instead of <a> tags for all nodes
	// Low level event handlers: onEvent(dtnode, event): return false, to stop default processing
	onClick: null, // null: generate focus, expand, activate, select events.
	onDblClick: null, // (No default actions.)
	onKeydown: null, // null: generate keyboard navigation (focus, expand, activate).
	onKeypress: null, // (No default actions.)
	onFocus: null, // null: set focus to node.
	onBlur: null, // null: remove focus from node.

	// Pre-event handlers onQueryEvent(flag, dtnode): return false, to stop processing
	onQueryActivate: null, // Callback(flag, dtnode) before a node is (de)activated.
	onQuerySelect: null, // Callback(flag, dtnode) before a node is (de)selected.
	onQueryExpand: null, // Callback(flag, dtnode) before a node is expanded/collpsed.

	// High level event handlers
	onPostInit: null, // Callback(isReloading, isError) when tree was (re)loaded.
	onActivate: null, // Callback(dtnode) when a node is activated.
	onDeactivate: null, // Callback(dtnode) when a node is deactivated.
	onSelect: null, // Callback(flag, dtnode) when a node is (de)selected.
	onExpand: null, // Callback(dtnode) when a node is expanded/collapsed.
	onLazyRead: null, // Callback(dtnode) when a lazy node is expanded for the first time.
	onCustomRender: null, // Callback(dtnode) before a node is rendered. Return a HTML string to override.
	onRender: null, // Callback(dtnode, nodeSpan) after a node was rendered.

	// Drag'n'drop support
	dnd: {
		// Make tree nodes draggable:
		onDragStart: null, // Callback(sourceNode), return true, to enable dnd
		onDragStop: null, // Callback(sourceNode)
//		helper: null,
		// Make tree nodes accept draggables
		autoExpandMS: 1000, // Expand nodes after n milliseconds of hovering.
		preventVoidMoves: true, // Prevent dropping nodes 'before self', etc.
		onDragEnter: null, // Callback(targetNode, sourceNode)
		onDragOver: null, // Callback(targetNode, sourceNode, hitMode)
		onDrop: null, // Callback(targetNode, sourceNode, hitMode)
		onDragLeave: null // Callback(targetNode, sourceNode)
	},
	ajaxDefaults: { // Used by initAjax option
		cache: false, // false: Append random '_' argument to the request url to prevent caching.
		dataType: "json" // Expect json format and pass json object to callbacks.
	},
	strings: {
		loading: "Loading&#8230;",
		loadError: "Load error!"
	},
	generateIds: false, // Generate id attributes like <span id='dynatree-id-KEY'>
	idPrefix: "dynatree-id-", // Used to generate node id's like <span id="dynatree-id-<key>">.
	keyPathSeparator: "/", // Used by node.getKeyPath() and tree.loadKeyPath().
//    cookieId: "dynatree-cookie", // Choose a more unique name, to allow multiple trees.
	cookieId: "dynatree", // Choose a more unique name, to allow multiple trees.
	cookie: {
		expires: null //7, // Days or Date; null: session cookie
//		path: "/", // Defaults to current page
//		domain: "jquery.com",
//		secure: true
	},
	// Class names used, when rendering the HTML markup.
	// Note: if only single entries are passed for options.classNames, all other
	// values are still set to default.
	classNames: {
		container: "dynatree-container",
		node: "dynatree-node",
		folder: "dynatree-folder",
//		document: "dynatree-document",

		empty: "dynatree-empty",
		vline: "dynatree-vline",
		expander: "dynatree-expander",
		connector: "dynatree-connector",
		checkbox: "dynatree-checkbox",
		nodeIcon: "dynatree-icon",
		title: "dynatree-title",
		noConnector: "dynatree-no-connector",

		nodeError: "dynatree-statusnode-error",
		nodeWait: "dynatree-statusnode-wait",
		hidden: "dynatree-hidden",
		combinedExpanderPrefix: "dynatree-exp-",
		combinedIconPrefix: "dynatree-ico-",
		nodeLoading: "dynatree-loading",
//		disabled: "dynatree-disabled",
		hasChildren: "dynatree-has-children",
		active: "dynatree-active",
		selected: "dynatree-selected",
		expanded: "dynatree-expanded",
		lazy: "dynatree-lazy",
		focused: "dynatree-focused",
		partsel: "dynatree-partsel",
		lastsib: "dynatree-lastsib"
	},
	debugLevel: 1,

	// ------------------------------------------------------------------------
	lastentry: undefined
};
//
if( parseFloat($.ui.version) < 1.8 ) {
	$.ui.dynatree.defaults = $.ui.dynatree.prototype.options;
}

/*******************************************************************************
 * Reserved data attributes for a tree node.
 */
$.ui.dynatree.nodedatadefaults = {
	title: null, // (required) Displayed name of the node (html is allowed here)
	key: null, // May be used with activate(), select(), find(), ...
	isFolder: false, // Use a folder icon. Also the node is expandable but not selectable.
	isLazy: false, // Call onLazyRead(), when the node is expanded for the first time to allow for delayed creation of children.
	tooltip: null, // Show this popup text.
	icon: null, // Use a custom image (filename relative to tree.options.imagePath). 'null' for default icon, 'false' for no icon.
	addClass: null, // Class name added to the node's span tag.
	noLink: false, // Use <span> instead of <a> tag for this node
	activate: false, // Initial active status.
	focus: false, // Initial focused status.
	expand: false, // Initial expanded status.
	select: false, // Initial selected status.
	hideCheckbox: false, // Suppress checkbox display for this node.
	unselectable: false, // Prevent selection.
//  disabled: false,
	// The following attributes are only valid if passed to some functions:
	children: null, // Array of child nodes.
	// NOTE: we can also add custom attributes here.
	// This may then also be used in the onActivate(), onSelect() or onLazyTree() callbacks.
	// ------------------------------------------------------------------------
	lastentry: undefined
};

/*******************************************************************************
 * Drag and drop support
 */
function _initDragAndDrop(tree) {
	var dnd = tree.options.dnd || null;
	// Register 'connectToDynatree' option with ui.draggable
	if(dnd && (dnd.onDragStart || dnd.onDrop)) {
		_registerDnd();
	}
	// Attach ui.draggable to this Dynatree instance
	if(dnd && dnd.onDragStart ) {
		tree.$tree.draggable({
			addClasses: false,
			appendTo: "body",
			containment: false,
			delay: 0,
			distance: 4,
			revert: false,
			// Delegate draggable.start, drag, and stop events to our handler
			connectToDynatree: true,
			// Let source tree create the helper element
			helper: function(event) {
				var sourceNode = getDtNodeFromElement(event.target);
				return sourceNode.tree._onDragEvent("helper", sourceNode, null, event, null, null);
			},
			_last: null
		});
	}
	// Attach ui.droppable to this Dynatree instance
	if(dnd && dnd.onDrop) {
		tree.$tree.droppable({
			addClasses: false,
			tolerance: "intersect",
			greedy: false,
			_last: null
		});
	}
}

//--- Extend ui.draggable event handling --------------------------------------
var didRegisterDnd = false;
var _registerDnd = function() {
	if(didRegisterDnd){
		return;
	}
	$.ui.plugin.add("draggable", "connectToDynatree", {
		start: function(event, ui) {
			var draggable = $(this).data("draggable");
			var sourceNode = ui.helper.data("dtSourceNode") || null;
//			logMsg("draggable-connectToDynatree.start, %s", sourceNode);
//			logMsg("    this: %o", this);
//			logMsg("    event: %o", event);
//			logMsg("    draggable: %o", draggable);
//			logMsg("    ui: %o", ui);
			if(sourceNode) {
				// Adjust helper offset, so cursor is slightly outside top/left corner
//				draggable.offset.click.top -= event.target.offsetTop;
//				draggable.offset.click.left -= event.target.offsetLeft;
				draggable.offset.click.top = -2;
				draggable.offset.click.left = + 16;
//				logMsg("    draggable.offset.click FIXED: %s/%s", draggable.offset.click.left, draggable.offset.click.top);
				// Trigger onDragStart event
				// TODO: when called as connectTo..., the return value is ignored(?)
				return sourceNode.tree._onDragEvent("start", sourceNode, null, event, ui, draggable);
			}
		},
		drag: function(event, ui) {
			var draggable = $(this).data("draggable");
			var sourceNode = ui.helper.data("dtSourceNode") || null;
			var prevTargetNode = ui.helper.data("dtTargetNode") || null;
			var targetNode = getDtNodeFromElement(event.target);
//			logMsg("getDtNodeFromElement(%o): %s", event.target, targetNode);
			if(event.target && !targetNode){
				// We got a drag event, but the targetNode could not be found
				// at the event location. This may happen, if the mouse
				// jumped over the drag helper, in which case we ignore it:
				var isHelper = $(event.target).closest("div.dynatree-drag-helper,#dynatree-drop-marker").length > 0;
				if(isHelper){
//					logMsg("Drag event over helper: ignored.");
					return;
				}
			}
//			logMsg("draggable-connectToDynatree.drag: targetNode(from event): %s, dtTargetNode: %s", targetNode, ui.helper.data("dtTargetNode"));
			ui.helper.data("dtTargetNode", targetNode);
			// Leaving a tree node
			if(prevTargetNode && prevTargetNode !== targetNode ) {
				prevTargetNode.tree._onDragEvent("leave", prevTargetNode, sourceNode, event, ui, draggable);
			}
			if(targetNode){
				if(!targetNode.tree.options.dnd.onDrop) {
					// not enabled as drop target
					noop(); // Keep JSLint happy
				} else if(targetNode === prevTargetNode) {
					// Moving over same node
					targetNode.tree._onDragEvent("over", targetNode, sourceNode, event, ui, draggable);
				}else{
					// Entering this node first time
					targetNode.tree._onDragEvent("enter", targetNode, sourceNode, event, ui, draggable);
				}
			}
			// else go ahead with standard event handling
		},
		stop: function(event, ui) {
			var draggable = $(this).data("draggable");
			var sourceNode = ui.helper.data("dtSourceNode") || null;
			var targetNode = ui.helper.data("dtTargetNode") || null;
//			logMsg("draggable-connectToDynatree.stop: targetNode(from event): %s, dtTargetNode: %s", targetNode, ui.helper.data("dtTargetNode"));
//			logMsg("draggable-connectToDynatree.stop, %s", sourceNode);
			var mouseDownEvent = draggable._mouseDownEvent;
			var eventType = event.type;
//			logMsg("    type: %o, downEvent: %o, upEvent: %o", eventType, mouseDownEvent, event);
//			logMsg("    targetNode: %o", targetNode);
			var dropped = (eventType == "mouseup" && event.which == 1);
			if(!dropped){
				logMsg("Drag was cancelled");
			}
			if(targetNode) {
				if(dropped){
					targetNode.tree._onDragEvent("drop", targetNode, sourceNode, event, ui, draggable);
				}
				targetNode.tree._onDragEvent("leave", targetNode, sourceNode, event, ui, draggable);
			}
			if(sourceNode){
				sourceNode.tree._onDragEvent("stop", sourceNode, null, event, ui, draggable);
			}
		}
	});
	didRegisterDnd = true;
};

// ---------------------------------------------------------------------------
})(jQuery);
// Farbtastic 2.0 alpha
(function ($) {
  
var __debug = false;

$.fn.farbtastic = function (options) {
  $.farbtastic(this, options);
  return this;
};

$.farbtastic = function (container, options) {
  var container = $(container)[0];
  return container.farbtastic || (container.farbtastic = new $._farbtastic(container, options));
}

$._farbtastic = function (container, options) {
  var fb = this;
  
  /////////////////////////////////////////////////////

  /**
   * Link to the given element(s) or callback.
   */
  fb.linkTo = function (callback) {
    // Unbind previous nodes
    if (typeof fb.callback == 'object') {
      $(fb.callback).unbind('keyup', fb.updateValue);
    }

    // Reset color
    fb.color = null;

    // Bind callback or elements
    if (typeof callback == 'function') {
      fb.callback = callback;
    }
    else if (typeof callback == 'object' || typeof callback == 'string') {
      fb.callback = $(callback);
      fb.callback.bind('keyup', fb.updateValue);
      if (fb.callback[0].value) {
        fb.setColor(fb.callback[0].value);
      }
    }
    return this;
  }
  fb.updateValue = function (event) {
    if (this.value && this.value != fb.color) {
      fb.setColor(this.value);
    }
  }

  /**
   * Change color with HTML syntax #123456
   */
  fb.setColor = function (color) {
    var unpack = fb.unpack(color);
    if (fb.color != color && unpack) {
      fb.color = color;
      fb.rgb = unpack;
      fb.hsl = fb.RGBToHSL(fb.rgb);
      fb.updateDisplay();
    }
    return this;
  }

  /**
   * Change color with HSL triplet [0..1, 0..1, 0..1]
   */
  fb.setHSL = function (hsl) {
    fb.hsl = hsl;
    fb.rgb = fb.HSLToRGB(hsl);
    fb.color = fb.pack(fb.rgb);
    fb.updateDisplay();
    return this;
  }

  /////////////////////////////////////////////////////

  /**
   * Initialize the color picker widget.
   */
  fb.initWidget = function () {

    // Insert markup and size accordingly.
    var dim = {
      width: options.width,
      height: options.width
    };
    $(container)
      .html(
        '<div class="farbtastic" style="position: relative">' +
          '<div class="farbtastic-solid"></div>' +
          '<canvas class="farbtastic-mask"></canvas>' +
          '<canvas class="farbtastic-overlay"></canvas>' +
        '</div>'
      )
      .find('*').attr(dim).css(dim).end()
      .find('div>*').css('position', 'absolute');

    // IE Fix: Recreate canvas elements with doc.createElement and excanvas.
    $.browser.msie && $('canvas', container).each(function () {
      // Fetch info.
      var attr = { 'class': $(this).attr('class'), style: this.getAttribute('style') },
          e = document.createElement('canvas');
      // Replace element.
      $(this).before($(e).attr(attr)).remove();
      // Init with explorerCanvas.
      G_vmlCanvasManager && G_vmlCanvasManager.initElement(e);
      // Set explorerCanvas elements dimensions and absolute positioning.
      $(e).attr(dim).css(dim).css('position', 'absolute')
        .find('*').attr(dim).css(dim);
    });

    // Determine layout
    fb.radius = (options.width - options.wheelWidth) / 2 - 1;
    fb.square = Math.floor((fb.radius - options.wheelWidth / 2) * 0.7) - 1;
    fb.mid = Math.floor(options.width / 2);
    fb.markerSize = options.wheelWidth * 0.3;
    fb.solidFill = $('.farbtastic-solid', container).css({
      width: fb.square * 2 - 1,
      height: fb.square * 2 - 1,
      left: fb.mid - fb.square,
      top: fb.mid - fb.square
    });

    // Set up drawing context.
    fb.cnvMask = $('.farbtastic-mask', container);
    fb.ctxMask = fb.cnvMask[0].getContext('2d');
    fb.cnvOverlay = $('.farbtastic-overlay', container);
    fb.ctxOverlay = fb.cnvOverlay[0].getContext('2d');
    fb.ctxMask.translate(fb.mid, fb.mid);
    fb.ctxOverlay.translate(fb.mid, fb.mid);
    
    // Draw widget base layers.
    fb.drawCircle();
    fb.drawMask();
  }

  /**
   * Draw the color wheel.
   */
  fb.drawCircle = function () {
    var tm = +(new Date());
    // Draw a hue circle with a bunch of gradient-stroked beziers.
    // Have to use beziers, as gradient-stroked arcs don't work.
    var n = 24,
        r = fb.radius,
        w = options.wheelWidth,
        nudge = 8 / r / n * Math.PI, // Fudge factor for seams.
        m = fb.ctxMask,
        angle1 = 0, color1, d1;
    m.save();
    m.lineWidth = w / r;
    m.scale(r, r);
    // Each segment goes from angle1 to angle2.
    for (var i = 0; i <= n; ++i) {
      var d2 = i / n,
          angle2 = d2 * Math.PI * 2,
          // Endpoints
          x1 = Math.sin(angle1), y1 = -Math.cos(angle1);
          x2 = Math.sin(angle2), y2 = -Math.cos(angle2),
          // Midpoint chosen so that the endpoints are tangent to the circle.
          am = (angle1 + angle2) / 2,
          tan = 1 / Math.cos((angle2 - angle1) / 2),
          xm = Math.sin(am) * tan, ym = -Math.cos(am) * tan,
          // New color
          color2 = fb.pack(fb.HSLToRGB([d2, 1, 0.5]));
      if (i > 0) {
        if ($.browser.msie) {
          // IE's gradient calculations mess up the colors. Correct along the diagonals.
          var corr = (1 + Math.min(Math.abs(Math.tan(angle1)), Math.abs(Math.tan(Math.PI / 2 - angle1)))) / n;
          color1 = fb.pack(fb.HSLToRGB([d1 - 0.15 * corr, 1, 0.5]));
          color2 = fb.pack(fb.HSLToRGB([d2 + 0.15 * corr, 1, 0.5]));
          // Create gradient fill between the endpoints.
          var grad = m.createLinearGradient(x1, y1, x2, y2);
          grad.addColorStop(0, color1);
          grad.addColorStop(1, color2);
          m.fillStyle = grad;
          // Draw quadratic curve segment as a fill.
          var r1 = (r + w / 2) / r, r2 = (r - w / 2) / r; // inner/outer radius.
          m.beginPath();
          m.moveTo(x1 * r1, y1 * r1);
          m.quadraticCurveTo(xm * r1, ym * r1, x2 * r1, y2 * r1);
          m.lineTo(x2 * r2, y2 * r2);
          m.quadraticCurveTo(xm * r2, ym * r2, x1 * r2, y1 * r2);
          m.fill();
        }
        else {
          // Create gradient fill between the endpoints.
          var grad = m.createLinearGradient(x1, y1, x2, y2);
          grad.addColorStop(0, color1);
          grad.addColorStop(1, color2);
          m.strokeStyle = grad;
          // Draw quadratic curve segment.
          m.beginPath();
          m.moveTo(x1, y1);
          m.quadraticCurveTo(xm, ym, x2, y2);
          m.stroke();
        }
      }
      // Prevent seams where curves join.
      angle1 = angle2 - nudge; color1 = color2; d1 = d2;
    }
    m.restore();
    __debug && $('body').append('<div>drawCircle '+ (+(new Date()) - tm) +'ms');
  };
  
  /**
   * Draw the saturation/luminance mask.
   */
  fb.drawMask = function () {
    var tm = +(new Date());

    // Iterate over sat/lum space and calculate appropriate mask pixel values.
    var size = fb.square * 2, sq = fb.square;
    function calculateMask(sizex, sizey, outputPixel) {
      var isx = 1 / sizex, isy = 1 / sizey;
      for (var y = 0; y <= sizey; ++y) {
        var l = 1 - y * isy;
        for (var x = 0; x <= sizex; ++x) {
          var s = 1 - x * isx;
          // From sat/lum to alpha and color (grayscale)
          var a = 1 - 2 * Math.min(l * s, (1 - l) * s);
          var c = (a > 0) ? ((2 * l - 1 + a) * .5 / a) : 0;
          outputPixel(x, y, c, a);
        }
      }      
    }
 
    // Method #1: direct pixel access (new Canvas).
    if (fb.ctxMask.getImageData) {
      // Create half-resolution buffer.
      var sz = Math.floor(size / 2);
      var buffer = document.createElement('canvas');
      buffer.width = buffer.height = sz + 1;
      var ctx = buffer.getContext('2d');
      var frame = ctx.getImageData(0, 0, sz + 1, sz + 1);

      var i = 0;
      calculateMask(sz, sz, function (x, y, c, a) {
        frame.data[i++] = frame.data[i++] = frame.data[i++] = c * 255;
        frame.data[i++] = a * 255;
      });

      ctx.putImageData(frame, 0, 0);
      fb.ctxMask.drawImage(buffer, 0, 0, sz + 1, sz + 1, -sq, -sq, sq * 2, sq * 2);
    }
    // Method #2: drawing commands (old Canvas).
    else if (!$.browser.msie) {
      // Render directly at half-resolution
      var sz = Math.floor(size / 2);
      calculateMask(sz, sz, function (x, y, c, a) {
        c = Math.round(c * 255);
        fb.ctxMask.fillStyle = 'rgba(' + c + ', ' + c + ', ' + c + ', ' + a +')';
        fb.ctxMask.fillRect(x * 2 - sq - 1, y * 2 - sq - 1, 2, 2);
      });
    }
    // Method #3: vertical DXImageTransform gradient strips (IE).
    else {
      var cache_last, cache, w = 6; // Each strip is 6 pixels wide.
      var sizex = Math.floor(size / w);
      // 6 vertical pieces of gradient per strip.
      calculateMask(sizex, 6, function (x, y, c, a) {
        if (x == 0) {
          cache_last = cache;
          cache = [];
        }
        c = Math.round(c * 255);
        a = Math.round(a * 255);
        // We can only start outputting gradients once we have two rows of pixels.
        if (y > 0) {
          var c_last = cache_last[x][0],
              a_last = cache_last[x][1],
              color1 = fb.packDX(c_last, a_last),
              color2 = fb.packDX(c, a),
              y1 = Math.round(fb.mid + ((y - 1) * .333 - 1) * sq),
              y2 = Math.round(fb.mid + (y * .333 - 1) * sq);
          $('<div>').css({
            position: 'absolute',
            filter: "progid:DXImageTransform.Microsoft.Gradient(StartColorStr="+ color1 +", EndColorStr="+ color2 +", GradientType=0)",
            top: y1,
            height: y2 - y1,
            // Avoid right-edge sticking out.
            left: fb.mid + (x * w - sq - 1),
            width: w - (x == sizex ? Math.round(w / 2) : 0)
          }).appendTo(fb.cnvMask);
        }
        cache.push([c, a]);
      });
    }    
    __debug && $('body').append('<div>drawMask '+ (+(new Date()) - tm) +'ms');
  }

  /**
   * Draw the selection markers.
   */
  fb.drawMarkers = function () {
    // Determine marker dimensions
    var sz = options.width, lw = Math.ceil(fb.markerSize / 4), r = fb.markerSize - lw + 1;
    var angle = fb.hsl[0] * 6.28,
        x1 =  Math.sin(angle) * fb.radius,
        y1 = -Math.cos(angle) * fb.radius,
        x2 = 2 * fb.square * (.5 - fb.hsl[1]),
        y2 = 2 * fb.square * (.5 - fb.hsl[2]),
        c1 = fb.invert ? '#fff' : '#000',
        c2 = fb.invert ? '#000' : '#fff';
    var circles = [
      { x: x1, y: y1, r: r,             c: '#000', lw: lw + 1 },
      { x: x1, y: y1, r: fb.markerSize, c: '#fff', lw: lw },
      { x: x2, y: y2, r: r,             c: c2,     lw: lw + 1 },
      { x: x2, y: y2, r: fb.markerSize, c: c1,     lw: lw },
    ];

    // Update the overlay canvas.
    fb.ctxOverlay.clearRect(-fb.mid, -fb.mid, sz, sz);
    for (i in circles) {
      var c = circles[i];
      fb.ctxOverlay.lineWidth = c.lw;
      fb.ctxOverlay.strokeStyle = c.c;
      fb.ctxOverlay.beginPath();
      fb.ctxOverlay.arc(c.x, c.y, c.r, 0, Math.PI * 2, true);
      fb.ctxOverlay.stroke();
    }
  }

  /**
   * Update the markers and styles
   */
  fb.updateDisplay = function () {
    // Determine whether labels/markers should invert.
    fb.invert = (fb.rgb[0] * 0.3 + fb.rgb[1] * .59 + fb.rgb[2] * .11) <= 0.6;

    // Update the solid background fill.
    fb.solidFill.css('backgroundColor', fb.pack(fb.HSLToRGB([fb.hsl[0], 1, 0.5])));

    // Draw markers
    fb.drawMarkers();
    
    // Linked elements or callback
    if (typeof fb.callback == 'object') {
      // Set background/foreground color
      $(fb.callback).css({
        backgroundColor: fb.color,
        color: fb.invert ? '#fff' : '#000'
      });

      // Change linked value
      $(fb.callback).each(function() {
        if ((typeof this.value == 'string') && this.value != fb.color) {
          this.value = fb.color;
        }
      });
    }
    else if (typeof fb.callback == 'function') {
      fb.callback.call(fb, fb.color);
    }
  }
  
  /**
   * Helper for returning coordinates relative to the center.
   */
  fb.widgetCoords = function (event) {
    return {
      x: event.pageX - fb.offset.left - fb.mid,    
      y: event.pageY - fb.offset.top - fb.mid
    };    
  }

  /**
   * Mousedown handler
   */
  fb.mousedown = function (event) {
    // Capture mouse
    if (!$._farbtastic.dragging) {
      $(document).bind('mousemove', fb.mousemove).bind('mouseup', fb.mouseup);
      $._farbtastic.dragging = true;
    }

    // Update the stored offset for the widget.
    fb.offset = $(container).offset();

    // Check which area is being dragged
    var pos = fb.widgetCoords(event);
    fb.circleDrag = Math.max(Math.abs(pos.x), Math.abs(pos.y)) > (fb.square + 2);

    // Process
    fb.mousemove(event);
    return false;
  }

  /**
   * Mousemove handler
   */
  fb.mousemove = function (event) {
    // Get coordinates relative to color picker center
    var pos = fb.widgetCoords(event);

    // Set new HSL parameters
    if (fb.circleDrag) {
      var hue = Math.atan2(pos.x, -pos.y) / 6.28;
      fb.setHSL([(hue + 1) % 1, fb.hsl[1], fb.hsl[2]]);
    }
    else {
      var sat = Math.max(0, Math.min(1, -(pos.x / fb.square / 2) + .5));
      var lum = Math.max(0, Math.min(1, -(pos.y / fb.square / 2) + .5));
      fb.setHSL([fb.hsl[0], sat, lum]);
    }
    return false;
  }

  /**
   * Mouseup handler
   */
  fb.mouseup = function () {
    // Uncapture mouse
    $(document).unbind('mousemove', fb.mousemove);
    $(document).unbind('mouseup', fb.mouseup);
    $._farbtastic.dragging = false;
  }

  /* Various color utility functions */
  fb.dec2hex = function (x) {
    return (x < 16 ? '0' : '') + x.toString(16);
  }

  fb.packDX = function (c, a) {
    return '#' + fb.dec2hex(a) + fb.dec2hex(c) + fb.dec2hex(c) + fb.dec2hex(c);
  };
  
  fb.pack = function (rgb) {
    var r = Math.round(rgb[0] * 255);
    var g = Math.round(rgb[1] * 255);
    var b = Math.round(rgb[2] * 255);
    return '#' + fb.dec2hex(r) + fb.dec2hex(g) + fb.dec2hex(b);
  };

  fb.unpack = function (color) {
    if (color.length == 7) {
      function x(i) {
        return parseInt(color.substring(i, i + 2), 16) / 255;
      }
      return [ x(1), x(3), x(5) ];
    }
    else if (color.length == 4) {
      function x(i) {
        return parseInt(color.substring(i, i + 1), 16) / 15;
      }
      return [ x(1), x(2), x(3) ];
    }
  };

  fb.HSLToRGB = function (hsl) {
    var m1, m2, r, g, b;
    var h = hsl[0], s = hsl[1], l = hsl[2];
    m2 = (l <= 0.5) ? l * (s + 1) : l + s - l * s;
    m1 = l * 2 - m2;
    return [
      this.hueToRGB(m1, m2, h + 0.33333),
      this.hueToRGB(m1, m2, h),
      this.hueToRGB(m1, m2, h - 0.33333)
    ];
  };

  fb.hueToRGB = function (m1, m2, h) {
    h = (h + 1) % 1;
    if (h * 6 < 1) return m1 + (m2 - m1) * h * 6;
    if (h * 2 < 1) return m2;
    if (h * 3 < 2) return m1 + (m2 - m1) * (0.66666 - h) * 6;
    return m1;
  };

  fb.RGBToHSL = function (rgb) {
    var r = rgb[0], g = rgb[1], b = rgb[2],
        min = Math.min(r, g, b),
        max = Math.max(r, g, b),
        delta = max - min,
        h = 0,
        s = 0,
        l = (min + max) / 2;
    if (l > 0 && l < 1) {
      s = delta / (l < 0.5 ? (2 * l) : (2 - 2 * l));
    }
    if (delta > 0) {
      if (max == r && max != g) h += (g - b) / delta;
      if (max == g && max != b) h += (2 + (b - r) / delta);
      if (max == b && max != r) h += (4 + (r - g) / delta);
      h /= 6;
    }
    return [h, s, l];
  };

  // Parse options.
  if (!options.callback) {
    options = { callback: options };
  }
  options = $.extend({
    width: 300,
    wheelWidth: (options.width || 300) / 10,
    callback: null
  }, options);

  // Initialize.
  fb.initWidget();

  // Install mousedown handler (the others are set on the document on-demand)
  $('canvas.farbtastic-overlay', container).mousedown(fb.mousedown);

  // Set linked elements/callback
  if (options.callback) {
    fb.linkTo(options.callback);
  }
  // Set to gray.
  fb.setColor('#808080');
}

})(jQuery);
/**
 *
 * Color picker
 * Author: Stefan Petre www.eyecon.ro
 * 
 * Dual licensed under the MIT and GPL licenses
 * 
 */
(function ($) {
	var ColorPicker = function () {
		var
			ids = {},
			inAction,
			charMin = 65,
			visible,
			tpl = '<div class="colorpicker"><div class="colorpicker_color"><div><div></div></div></div><div class="colorpicker_hue"><div></div></div><div class="colorpicker_new_color"></div><div class="colorpicker_current_color"></div><div class="colorpicker_hex"><input type="text" maxlength="6" size="6" /></div><div class="colorpicker_rgb_r colorpicker_field"><input type="text" maxlength="3" size="3" /><span></span></div><div class="colorpicker_rgb_g colorpicker_field"><input type="text" maxlength="3" size="3" /><span></span></div><div class="colorpicker_rgb_b colorpicker_field"><input type="text" maxlength="3" size="3" /><span></span></div><div class="colorpicker_hsb_h colorpicker_field"><input type="text" maxlength="3" size="3" /><span></span></div><div class="colorpicker_hsb_s colorpicker_field"><input type="text" maxlength="3" size="3" /><span></span></div><div class="colorpicker_hsb_b colorpicker_field"><input type="text" maxlength="3" size="3" /><span></span></div><div class="colorpicker_submit"></div></div>',
			defaults = {
				eventName: 'click',
				onShow: function () {},
				onBeforeShow: function(){},
				onHide: function () {},
				onChange: function () {},
				onSubmit: function () {},
				color: 'ff0000',
				livePreview: true,
				flat: false
			},
			fillRGBFields = function  (hsb, cal) {
				var rgb = HSBToRGB(hsb);
				$(cal).data('colorpicker').fields
					.eq(1).val(rgb.r).end()
					.eq(2).val(rgb.g).end()
					.eq(3).val(rgb.b).end();
			},
			fillHSBFields = function  (hsb, cal) {
				$(cal).data('colorpicker').fields
					.eq(4).val(hsb.h).end()
					.eq(5).val(hsb.s).end()
					.eq(6).val(hsb.b).end();
			},
			fillHexFields = function (hsb, cal) {
				$(cal).data('colorpicker').fields
					.eq(0).val(HSBToHex(hsb)).end();
			},
			setSelector = function (hsb, cal) {
				$(cal).data('colorpicker').selector.css('backgroundColor', '#' + HSBToHex({h: hsb.h, s: 100, b: 100}));
				$(cal).data('colorpicker').selectorIndic.css({
					left: parseInt(150 * hsb.s/100, 10),
					top: parseInt(150 * (100-hsb.b)/100, 10)
				});
			},
			setHue = function (hsb, cal) {
				$(cal).data('colorpicker').hue.css('top', parseInt(150 - 150 * hsb.h/360, 10));
			},
			setCurrentColor = function (hsb, cal) {
				$(cal).data('colorpicker').currentColor.css('backgroundColor', '#' + HSBToHex(hsb));
			},
			setNewColor = function (hsb, cal) {
				$(cal).data('colorpicker').newColor.css('backgroundColor', '#' + HSBToHex(hsb));
			},
			keyDown = function (ev) {
				var pressedKey = ev.charCode || ev.keyCode || -1;
				if ((pressedKey > charMin && pressedKey <= 90) || pressedKey == 32) {
					return false;
				}
				var cal = $(this).parent().parent();
				if (cal.data('colorpicker').livePreview === true) {
					change.apply(this);
				}
			},
			change = function (ev) {
				var cal = $(this).parent().parent(), col;
				if (this.parentNode.className.indexOf('_hex') > 0) {
					cal.data('colorpicker').color = col = HexToHSB(fixHex(this.value));
				} else if (this.parentNode.className.indexOf('_hsb') > 0) {
					cal.data('colorpicker').color = col = fixHSB({
						h: parseInt(cal.data('colorpicker').fields.eq(4).val(), 10),
						s: parseInt(cal.data('colorpicker').fields.eq(5).val(), 10),
						b: parseInt(cal.data('colorpicker').fields.eq(6).val(), 10)
					});
				} else {
					cal.data('colorpicker').color = col = RGBToHSB(fixRGB({
						r: parseInt(cal.data('colorpicker').fields.eq(1).val(), 10),
						g: parseInt(cal.data('colorpicker').fields.eq(2).val(), 10),
						b: parseInt(cal.data('colorpicker').fields.eq(3).val(), 10)
					}));
				}
				if (ev) {
					fillRGBFields(col, cal.get(0));
					fillHexFields(col, cal.get(0));
					fillHSBFields(col, cal.get(0));
				}
				setSelector(col, cal.get(0));
				setHue(col, cal.get(0));
				setNewColor(col, cal.get(0));
				cal.data('colorpicker').onChange.apply(cal, [col, HSBToHex(col), HSBToRGB(col)]);
			},
			blur = function (ev) {
				var cal = $(this).parent().parent();
				cal.data('colorpicker').fields.parent().removeClass('colorpicker_focus');
			},
			focus = function () {
				charMin = this.parentNode.className.indexOf('_hex') > 0 ? 70 : 65;
				$(this).parent().parent().data('colorpicker').fields.parent().removeClass('colorpicker_focus');
				$(this).parent().addClass('colorpicker_focus');
			},
			downIncrement = function (ev) {
				var field = $(this).parent().find('input').focus();
				var current = {
					el: $(this).parent().addClass('colorpicker_slider'),
					max: this.parentNode.className.indexOf('_hsb_h') > 0 ? 360 : (this.parentNode.className.indexOf('_hsb') > 0 ? 100 : 255),
					y: ev.pageY,
					field: field,
					val: parseInt(field.val(), 10),
					preview: $(this).parent().parent().data('colorpicker').livePreview					
				};
				$(document).bind('mouseup', current, upIncrement);
				$(document).bind('mousemove', current, moveIncrement);
			},
			moveIncrement = function (ev) {
				ev.data.field.val(Math.max(0, Math.min(ev.data.max, parseInt(ev.data.val + ev.pageY - ev.data.y, 10))));
				if (ev.data.preview) {
					change.apply(ev.data.field.get(0), [true]);
				}
				return false;
			},
			upIncrement = function (ev) {
				change.apply(ev.data.field.get(0), [true]);
				ev.data.el.removeClass('colorpicker_slider').find('input').focus();
				$(document).unbind('mouseup', upIncrement);
				$(document).unbind('mousemove', moveIncrement);
				return false;
			},
			downHue = function (ev) {
				var current = {
					cal: $(this).parent(),
					y: $(this).offset().top
				};
				current.preview = current.cal.data('colorpicker').livePreview;
				$(document).bind('mouseup', current, upHue);
				$(document).bind('mousemove', current, moveHue);
			},
			moveHue = function (ev) {
				change.apply(
					ev.data.cal.data('colorpicker')
						.fields
						.eq(4)
						.val(parseInt(360*(150 - Math.max(0,Math.min(150,(ev.pageY - ev.data.y))))/150, 10))
						.get(0),
					[ev.data.preview]
				);
				return false;
			},
			upHue = function (ev) {
				fillRGBFields(ev.data.cal.data('colorpicker').color, ev.data.cal.get(0));
				fillHexFields(ev.data.cal.data('colorpicker').color, ev.data.cal.get(0));
				$(document).unbind('mouseup', upHue);
				$(document).unbind('mousemove', moveHue);
				return false;
			},
			downSelector = function (ev) {
				var current = {
					cal: $(this).parent(),
					pos: $(this).offset()
				};
				current.preview = current.cal.data('colorpicker').livePreview;
				$(document).bind('mouseup', current, upSelector);
				$(document).bind('mousemove', current, moveSelector);
			},
			moveSelector = function (ev) {
				change.apply(
					ev.data.cal.data('colorpicker')
						.fields
						.eq(6)
						.val(parseInt(100*(150 - Math.max(0,Math.min(150,(ev.pageY - ev.data.pos.top))))/150, 10))
						.end()
						.eq(5)
						.val(parseInt(100*(Math.max(0,Math.min(150,(ev.pageX - ev.data.pos.left))))/150, 10))
						.get(0),
					[ev.data.preview]
				);
				return false;
			},
			upSelector = function (ev) {
				fillRGBFields(ev.data.cal.data('colorpicker').color, ev.data.cal.get(0));
				fillHexFields(ev.data.cal.data('colorpicker').color, ev.data.cal.get(0));
				$(document).unbind('mouseup', upSelector);
				$(document).unbind('mousemove', moveSelector);
				return false;
			},
			enterSubmit = function (ev) {
				$(this).addClass('colorpicker_focus');
			},
			leaveSubmit = function (ev) {
				$(this).removeClass('colorpicker_focus');
			},
			clickSubmit = function (ev) {
				var cal = $(this).parent();
				var col = cal.data('colorpicker').color;
				cal.data('colorpicker').origColor = col;
				setCurrentColor(col, cal.get(0));
				cal.data('colorpicker').onSubmit(col, HSBToHex(col), HSBToRGB(col), cal.data('colorpicker').el);
			},
			show = function (ev) {
				var cal = $('#' + $(this).data('colorpickerId'));
				cal.data('colorpicker').onBeforeShow.apply(this, [cal.get(0)]);
				var pos = $(this).offset();
				var viewPort = getViewport();
				var top = pos.top + this.offsetHeight;
				var left = pos.left;
				if (top + 176 > viewPort.t + viewPort.h) {
					top -= this.offsetHeight + 176;
				}
				if (left + 356 > viewPort.l + viewPort.w) {
					left -= 356;
				}
				cal.css({left: left + 'px', top: top + 'px'});
				if (cal.data('colorpicker').onShow.apply(this, [cal.get(0)]) != false) {
					cal.show();
				}
				$(document).bind('mousedown', {cal: cal}, hide);
				return false;
			},
			hide = function (ev) {
				if (!isChildOf(ev.data.cal.get(0), ev.target, ev.data.cal.get(0))) {
					if (ev.data.cal.data('colorpicker').onHide.apply(this, [ev.data.cal.get(0)]) != false) {
						ev.data.cal.hide();
					}
					$(document).unbind('mousedown', hide);
				}
			},
			isChildOf = function(parentEl, el, container) {
				if (parentEl == el) {
					return true;
				}
				if (parentEl.contains) {
					return parentEl.contains(el);
				}
				if ( parentEl.compareDocumentPosition ) {
					return !!(parentEl.compareDocumentPosition(el) & 16);
				}
				var prEl = el.parentNode;
				while(prEl && prEl != container) {
					if (prEl == parentEl)
						return true;
					prEl = prEl.parentNode;
				}
				return false;
			},
			getViewport = function () {
				var m = document.compatMode == 'CSS1Compat';
				return {
					l : window.pageXOffset || (m ? document.documentElement.scrollLeft : document.body.scrollLeft),
					t : window.pageYOffset || (m ? document.documentElement.scrollTop : document.body.scrollTop),
					w : window.innerWidth || (m ? document.documentElement.clientWidth : document.body.clientWidth),
					h : window.innerHeight || (m ? document.documentElement.clientHeight : document.body.clientHeight)
				};
			},
			fixHSB = function (hsb) {
				return {
					h: Math.min(360, Math.max(0, hsb.h)),
					s: Math.min(100, Math.max(0, hsb.s)),
					b: Math.min(100, Math.max(0, hsb.b))
				};
			}, 
			fixRGB = function (rgb) {
				return {
					r: Math.min(255, Math.max(0, rgb.r)),
					g: Math.min(255, Math.max(0, rgb.g)),
					b: Math.min(255, Math.max(0, rgb.b))
				};
			},
			fixHex = function (hex) {
				var len = 6 - hex.length;
				if (len > 0) {
					var o = [];
					for (var i=0; i<len; i++) {
						o.push('0');
					}
					o.push(hex);
					hex = o.join('');
				}
				return hex;
			}, 
			HexToRGB = function (hex) {
				var hex = parseInt(((hex.indexOf('#') > -1) ? hex.substring(1) : hex), 16);
				return {r: hex >> 16, g: (hex & 0x00FF00) >> 8, b: (hex & 0x0000FF)};
			},
			HexToHSB = function (hex) {
				return RGBToHSB(HexToRGB(hex));
			},
			RGBToHSB = function (rgb) {
				var hsb = {
					h: 0,
					s: 0,
					b: 0
				};
				var min = Math.min(rgb.r, rgb.g, rgb.b);
				var max = Math.max(rgb.r, rgb.g, rgb.b);
				var delta = max - min;
				hsb.b = max;
				if (max != 0) {
					
				}
				hsb.s = max != 0 ? 255 * delta / max : 0;
				if (hsb.s != 0) {
					if (rgb.r == max) {
						hsb.h = (rgb.g - rgb.b) / delta;
					} else if (rgb.g == max) {
						hsb.h = 2 + (rgb.b - rgb.r) / delta;
					} else {
						hsb.h = 4 + (rgb.r - rgb.g) / delta;
					}
				} else {
					hsb.h = -1;
				}
				hsb.h *= 60;
				if (hsb.h < 0) {
					hsb.h += 360;
				}
				hsb.s *= 100/255;
				hsb.b *= 100/255;
				return hsb;
			},
			HSBToRGB = function (hsb) {
				var rgb = {};
				var h = Math.round(hsb.h);
				var s = Math.round(hsb.s*255/100);
				var v = Math.round(hsb.b*255/100);
				if(s == 0) {
					rgb.r = rgb.g = rgb.b = v;
				} else {
					var t1 = v;
					var t2 = (255-s)*v/255;
					var t3 = (t1-t2)*(h%60)/60;
					if(h==360) h = 0;
					if(h<60) {rgb.r=t1;	rgb.b=t2; rgb.g=t2+t3}
					else if(h<120) {rgb.g=t1; rgb.b=t2;	rgb.r=t1-t3}
					else if(h<180) {rgb.g=t1; rgb.r=t2;	rgb.b=t2+t3}
					else if(h<240) {rgb.b=t1; rgb.r=t2;	rgb.g=t1-t3}
					else if(h<300) {rgb.b=t1; rgb.g=t2;	rgb.r=t2+t3}
					else if(h<360) {rgb.r=t1; rgb.g=t2;	rgb.b=t1-t3}
					else {rgb.r=0; rgb.g=0;	rgb.b=0}
				}
				return {r:Math.round(rgb.r), g:Math.round(rgb.g), b:Math.round(rgb.b)};
			},
			RGBToHex = function (rgb) {
				var hex = [
					rgb.r.toString(16),
					rgb.g.toString(16),
					rgb.b.toString(16)
				];
				$.each(hex, function (nr, val) {
					if (val.length == 1) {
						hex[nr] = '0' + val;
					}
				});
				return hex.join('');
			},
			HSBToHex = function (hsb) {
				return RGBToHex(HSBToRGB(hsb));
			},
			restoreOriginal = function () {
				var cal = $(this).parent();
				var col = cal.data('colorpicker').origColor;
				cal.data('colorpicker').color = col;
				fillRGBFields(col, cal.get(0));
				fillHexFields(col, cal.get(0));
				fillHSBFields(col, cal.get(0));
				setSelector(col, cal.get(0));
				setHue(col, cal.get(0));
				setNewColor(col, cal.get(0));
			};
		return {
			init: function (opt) {
				opt = $.extend({}, defaults, opt||{});
				if (typeof opt.color == 'string') {
					opt.color = HexToHSB(opt.color);
				} else if (opt.color.r != undefined && opt.color.g != undefined && opt.color.b != undefined) {
					opt.color = RGBToHSB(opt.color);
				} else if (opt.color.h != undefined && opt.color.s != undefined && opt.color.b != undefined) {
					opt.color = fixHSB(opt.color);
				} else {
					return this;
				}
				return this.each(function () {
					if (!$(this).data('colorpickerId')) {
						var options = $.extend({}, opt);
						options.origColor = opt.color;
						var id = 'collorpicker_' + parseInt(Math.random() * 1000);
						$(this).data('colorpickerId', id);
						var cal = $(tpl).attr('id', id);
						if (options.flat) {
							cal.appendTo(this).show();
						} else {
							cal.appendTo(document.body);
						}
						options.fields = cal
											.find('input')
												.bind('keyup', keyDown)
												.bind('change', change)
												.bind('blur', blur)
												.bind('focus', focus);
						cal
							.find('span').bind('mousedown', downIncrement).end()
							.find('>div.colorpicker_current_color').bind('click', restoreOriginal);
						options.selector = cal.find('div.colorpicker_color').bind('mousedown', downSelector);
						options.selectorIndic = options.selector.find('div div');
						options.el = this;
						options.hue = cal.find('div.colorpicker_hue div');
						cal.find('div.colorpicker_hue').bind('mousedown', downHue);
						options.newColor = cal.find('div.colorpicker_new_color');
						options.currentColor = cal.find('div.colorpicker_current_color');
						cal.data('colorpicker', options);
						cal.find('div.colorpicker_submit')
							.bind('mouseenter', enterSubmit)
							.bind('mouseleave', leaveSubmit)
							.bind('click', clickSubmit);
						fillRGBFields(options.color, cal.get(0));
						fillHSBFields(options.color, cal.get(0));
						fillHexFields(options.color, cal.get(0));
						setHue(options.color, cal.get(0));
						setSelector(options.color, cal.get(0));
						setCurrentColor(options.color, cal.get(0));
						setNewColor(options.color, cal.get(0));
						if (options.flat) {
							cal.css({
								position: 'relative',
								display: 'block'
							});
						} else {
							$(this).bind(options.eventName, show);
						}
					}
				});
			},
			showPicker: function() {
				return this.each( function () {
					if ($(this).data('colorpickerId')) {
						show.apply(this);
					}
				});
			},
			hidePicker: function() {
				return this.each( function () {
					if ($(this).data('colorpickerId')) {
						$('#' + $(this).data('colorpickerId')).hide();
					}
				});
			},
			setColor: function(col) {
				if (typeof col == 'string') {
					col = HexToHSB(col);
				} else if (col.r != undefined && col.g != undefined && col.b != undefined) {
					col = RGBToHSB(col);
				} else if (col.h != undefined && col.s != undefined && col.b != undefined) {
					col = fixHSB(col);
				} else {
					return this;
				}
				return this.each(function(){
					if ($(this).data('colorpickerId')) {
						var cal = $('#' + $(this).data('colorpickerId'));
						cal.data('colorpicker').color = col;
						cal.data('colorpicker').origColor = col;
						fillRGBFields(col, cal.get(0));
						fillHSBFields(col, cal.get(0));
						fillHexFields(col, cal.get(0));
						setHue(col, cal.get(0));
						setSelector(col, cal.get(0));
						setCurrentColor(col, cal.get(0));
						setNewColor(col, cal.get(0));
					}
				});
			}
		};
	}();
	$.fn.extend({
		ColorPicker: ColorPicker.init,
		ColorPickerHide: ColorPicker.hidePicker,
		ColorPickerShow: ColorPicker.showPicker,
		ColorPickerSetColor: ColorPicker.setColor
	});
})(jQuery);(function($){
/**
 * jqGrid English Translation
 * Tony Tomov tony@trirand.com
 * http://trirand.com/blog/ 
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
**/
$.jgrid = {
	defaults : {
		recordtext: "View {0} - {1} of {2}",
		emptyrecords: "No records to view",
		loadtext: "Loading...",
		pgtext : "Page {0} of {1}"
	},
	search : {
		caption: "Search...",
		Find: "Find",
		Reset: "Reset",
		odata : ['equal', 'not equal', 'less', 'less or equal','greater','greater or equal', 'begins with','does not begin with','is in','is not in','ends with','does not end with','contains','does not contain'],
		groupOps: [	{ op: "AND", text: "all" },	{ op: "OR",  text: "any" }	],
		matchText: " match",
		rulesText: " rules"
	},
	edit : {
		addCaption: "Add Record",
		editCaption: "Edit Record",
		bSubmit: "Submit",
		bCancel: "Cancel",
		bClose: "Close",
		saveData: "Data has been changed! Save changes?",
		bYes : "Yes",
		bNo : "No",
		bExit : "Cancel",
		msg: {
			required:"Field is required",
			number:"Please, enter valid number",
			minValue:"value must be greater than or equal to ",
			maxValue:"value must be less than or equal to",
			email: "is not a valid e-mail",
			integer: "Please, enter valid integer value",
			date: "Please, enter valid date value",
			url: "is not a valid URL. Prefix required ('http://' or 'https://')",
			nodefined : " is not defined!",
			novalue : " return value is required!",
			customarray : "Custom function should return array!",
			customfcheck : "Custom function should be present in case of custom checking!"
			
		}
	},
	view : {
		caption: "View Record",
		bClose: "Close"
	},
	del : {
		caption: "Delete",
		msg: "Delete selected record(s)?",
		bSubmit: "Delete",
		bCancel: "Cancel"
	},
	nav : {
		edittext: "",
		edittitle: "Edit selected row",
		addtext:"",
		addtitle: "Add new row",
		deltext: "",
		deltitle: "Delete selected row",
		searchtext: "",
		searchtitle: "Find records",
		refreshtext: "",
		refreshtitle: "Reload Grid",
		alertcap: "Warning",
		alerttext: "Please, select row",
		viewtext: "",
		viewtitle: "View selected row"
	},
	col : {
		caption: "Select columns",
		bSubmit: "Ok",
		bCancel: "Cancel"
	},
	errors : {
		errcap : "Error",
		nourl : "No url is set",
		norecords: "No records to process",
		model : "Length of colNames <> colModel!"
	},
	formatter : {
		integer : {thousandsSeparator: " ", defaultValue: '0'},
		number : {decimalSeparator:".", thousandsSeparator: " ", decimalPlaces: 2, defaultValue: '0.00'},
		currency : {decimalSeparator:".", thousandsSeparator: " ", decimalPlaces: 2, prefix: "", suffix:"", defaultValue: '0.00'},
		date : {
			dayNames:   [
				"Sun", "Mon", "Tue", "Wed", "Thr", "Fri", "Sat",
				"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
			],
			monthNames: [
				"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
				"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
			],
			AmPm : ["am","pm","AM","PM"],
			S: function (j) {return j < 11 || j > 13 ? ['st', 'nd', 'rd', 'th'][Math.min((j - 1) % 10, 3)] : 'th'},
			srcformat: 'Y-m-d',
			newformat: 'd/m/Y',
			masks : {
				ISO8601Long:"Y-m-d H:i:s",
				ISO8601Short:"Y-m-d",
				ShortDate: "n/j/Y",
				LongDate: "l, F d, Y",
				FullDateTime: "l, F d, Y g:i:s A",
				MonthDay: "F d",
				ShortTime: "g:i A",
				LongTime: "g:i:s A",
				SortableDateTime: "Y-m-d\\TH:i:s",
				UniversalSortableDateTime: "Y-m-d H:i:sO",
				YearMonth: "F, Y"
			},
			reformatAfterEdit : false
		},
		baseLinkUrl: '',
		showAction: '',
		target: '',
		checkbox : {disabled:true},
		idName : 'id'
	}
};
})(jQuery);
// ==ClosureCompiler==
// @compilation_level SIMPLE_OPTIMIZATIONS

/**
 * @license jqGrid  4.1  - jQuery Grid
 * Copyright (c) 2008, Tony Tomov, tony@trirand.com
 * Dual licensed under the MIT and GPL licenses
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl-2.0.html
 * Date: 2011-06-14
 */
//jsHint options
/*global document, window, jQuery, DOMParser, ActiveXObject $ */

(function ($) {

$.jgrid = $.jgrid || {};
$.extend($.jgrid,{
	htmlDecode : function(value){
		if(value && (value=='&nbsp;' || value=='&#160;' || (value.length==1 && value.charCodeAt(0)==160))) { return "";}
		return !value ? value : String(value).replace(/&amp;/g, "&").replace(/&gt;/g, ">").replace(/&lt;/g, "<").replace(/&quot;/g, '"');
	},
	htmlEncode : function (value){
		return !value ? value : String(value).replace(/&/g, "&amp;").replace(/>/g, "&gt;").replace(/</g, "&lt;").replace(/\"/g, "&quot;");
	},
	format : function(format){ //jqgformat
		var args = $.makeArray(arguments).slice(1);
		if(format===undefined) { format = ""; }
		return format.replace(/\{(\d+)\}/g, function(m, i){
			return args[i];
		});
	},
	getCellIndex : function (cell) {
		var c = $(cell);
		if (c.is('tr')) { return -1; }
		c = (!c.is('td') && !c.is('th') ? c.closest("td,th") : c)[0];
		if ($.browser.msie) { return $.inArray(c, c.parentNode.cells); }
		return c.cellIndex;
	},
	stripHtml : function(v) {
		v = v+"";
		var regexp = /<("[^"]*"|'[^']*'|[^'">])*>/gi;
		if (v) {
			v = v.replace(regexp,"");
			return (v && v !== '&nbsp;' && v !== '&#160;') ? v.replace(/\"/g,"'") : "";
		} else {
			return v;
		}
	},
	stringToDoc : function (xmlString) {
		var xmlDoc;
		if(typeof xmlString !== 'string') { return xmlString; }
		try	{
			var parser = new DOMParser();
			xmlDoc = parser.parseFromString(xmlString,"text/xml");
		}
		catch(e) {
			xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
			xmlDoc.async=false;
			xmlDoc.loadXML(xmlString);
		}
		return (xmlDoc && xmlDoc.documentElement && xmlDoc.documentElement.tagName != 'parsererror') ? xmlDoc : null;
	},
	parse : function(jsonString) {
		var js = jsonString;
		if (js.substr(0,9) == "while(1);") { js = js.substr(9); }
		if (js.substr(0,2) == "/*") { js = js.substr(2,js.length-4); }
		if(!js) { js = "{}"; }
		return ($.jgrid.useJSON===true && typeof (JSON) === 'object' && typeof (JSON.parse) === 'function') ?
		    JSON.parse(js) :
		    eval('(' + js + ')');
	},
	parseDate : function(format, date) {
		var tsp = {m : 1, d : 1, y : 1970, h : 0, i : 0, s : 0},k,hl,dM, regdate = /[\\\/:_;.,\t\T\s-]/;
		if(date && date !== null && date !== undefined){
			date = $.trim(date);
			date = date.split(regdate);
			format = format.split(regdate);
			var dfmt  = $.jgrid.formatter.date.monthNames;
			var afmt  = $.jgrid.formatter.date.AmPm;
			var h12to24 = function(ampm, h){
				if (ampm === 0){ if (h == 12) { h = 0;} }
				          else { if (h != 12) { h += 12; } }
				return h;
			};
			for(k=0,hl=format.length;k<hl;k++){
				if(format[k] == 'M') {
					dM = $.inArray(date[k],dfmt);
					if(dM !== -1 && dM < 12){date[k] = dM+1;}
				}
				if(format[k] == 'F') {
					dM = $.inArray(date[k],dfmt);
					if(dM !== -1 && dM > 11){date[k] = dM+1-12;}
				}
				if(format[k] == 'a') {
					dM = $.inArray(date[k],afmt);
					if(dM !== -1 && dM < 2 && date[k] == afmt[dM]){
						date[k] = dM;
						tsp.h = h12to24(date[k], tsp.h);
					}
				}
				if(format[k] == 'A') {
					dM = $.inArray(date[k],afmt);
					if(dM !== -1 && dM > 1 && date[k] == afmt[dM]){
						date[k] = dM-2;
						tsp.h = h12to24(date[k], tsp.h);
					}
				}
				if(date[k] !== undefined) {
					tsp[format[k].toLowerCase()] = parseInt(date[k],10);
				}
			}
			tsp.m = parseInt(tsp.m,10)-1;
			var ty = tsp.y;
			if (ty >= 70 && ty <= 99) {tsp.y = 1900+tsp.y;}
			else if (ty >=0 && ty <=69) {tsp.y= 2000+tsp.y;}
		}
		return new Date(tsp.y, tsp.m, tsp.d, tsp.h, tsp.i, tsp.s,0);
	},
	jqID : function(sid){
		return String(sid).replace(/[!"#$%&'()*+,.\/:;<=>?@\[\\\]\^`{|}~]/g,"\\$&");
	},
	guid : 1,
	uidPref: 'jqg',
	randId : function( prefix )	{
		return (prefix? prefix: $.jgrid.uidPref) + ($.jgrid.guid++);
	},
	getAccessor : function(obj, expr) {
		var ret,p,prm = [], i;
		if( typeof expr === 'function') { return expr(obj); }
		ret = obj[expr];
		if(ret===undefined) {
			try {
				if ( typeof expr === 'string' ) {
					prm = expr.split('.');
				}
				i = prm.length;
				if( i ) {
					ret = obj;
				    while (ret && i--) {
						p = prm.shift();
						ret = ret[p];
					}
				}
			} catch (e) {}
		}
		return ret;
	},
	ajaxOptions: {},
	from : function(source,initalQuery){
		// Original Author Hugo Bonacci
		// License MIT http://jlinq.codeplex.com/license
		var queryObject=function(d,q){
		if(typeof(d)=="string"){
			d=$.data(d);
		}
		var self=this,
		_data=d,
		_usecase=true,
		_trim=false,
		_query=q,
		_stripNum = /[\$,%]/g,
		_lastCommand=null,
		_lastField=null,
		_negate=false,
		_queuedOperator="",
		_sorting=[],
		_useProperties=true;
		if(typeof(d)=="object"&&d.push) {
			if(d.length>0){
				if(typeof(d[0])!="object"){
					_useProperties=false;
				}else{
					_useProperties=true;
				}
			}
		}else{
			throw "data provides is not an array";
		}
		this._hasData=function(){
			return _data===null?false:_data.length===0?false:true;
		};
		this._getStr=function(s){
			var phrase=[];
			if(_trim){
				phrase.push("jQuery.trim(");
			}
			phrase.push("String("+s+")");
			if(_trim){
				phrase.push(")");
			}
			if(!_usecase){
				phrase.push(".toLowerCase()");
			}
			return phrase.join("");
		};
		this._strComp=function(val){
			if(typeof(val)=="string"){
				return".toString()";
			}else{
				return"";
			}
		};
		this._group=function(f,u){
			return({field:f.toString(),unique:u,items:[]});
		};
		this._toStr=function(phrase){
			if(_trim){
				phrase=$.trim(phrase);
			}
			if(!_usecase){
				phrase=phrase.toLowerCase();
			}
			phrase=phrase.toString().replace(/\\/g,'\\\\').replace(/\"/g,'\\"');
			return phrase;
		};
		this._funcLoop=function(func){
			var results=[];
			$.each(_data,function(i,v){
				results.push(func(v));
			});
			return results;
		};
		this._append=function(s){
			if(_query===null){
				_query="";
			} else {
				_query+=_queuedOperator === "" ? " && " :_queuedOperator;
			}
			if(_negate){
				_query+="!";
			}
			_query+="("+s+")";
			_negate=false;
			_queuedOperator="";
		};
		this._setCommand=function(f,c){
			_lastCommand=f;
			_lastField=c;
		};
		this._resetNegate=function(){
			_negate=false;
		};
		this._repeatCommand=function(f,v){
			if(_lastCommand===null){
				return self;
			}
			if(f!==null&&v!==null){
				return _lastCommand(f,v);
			}
			if(_lastField===null){
				return _lastCommand(f);
			}
			if(!_useProperties){
				return _lastCommand(f);
			}
			return _lastCommand(_lastField,f);
		};
		this._equals=function(a,b){
			return(self._compare(a,b,1)===0);
		};
		this._compare=function(a,b,d){
			if( d === undefined) { d = 1; }
			if(a===undefined) { a = null; }
			if(b===undefined) { b = null; }
			if(a===null && b===null){
				return 0;
			}
			if(a===null&&b!==null){
				return 1;
			}
			if(a!==null&&b===null){
				return -1;
			}
			if(!_usecase && typeof(a) !== "number" && typeof(b) !== "number" ) {
				a=String(a).toLowerCase();
				b=String(b).toLowerCase();
			}
			if(a<b){return -d;}
			if(a>b){return d;}
			return 0;
		};
		this._performSort=function(){
			if(_sorting.length===0){return;}
			_data=self._doSort(_data,0);
		};
		this._doSort=function(d,q){
			var by=_sorting[q].by,
			dir=_sorting[q].dir,
			type = _sorting[q].type,
			dfmt = _sorting[q].datefmt;
			if(q==_sorting.length-1){
				return self._getOrder(d, by, dir, type, dfmt);
			}
			q++;
			var values=self._getGroup(d,by,dir,type,dfmt);
			var results=[];
			for(var i=0;i<values.length;i++){
				var sorted=self._doSort(values[i].items,q);
				for(var j=0;j<sorted.length;j++){
					results.push(sorted[j]);
				}
			}
			return results;
		};
		this._getOrder=function(data,by,dir,type, dfmt){
			var sortData=[],_sortData=[], newDir = dir=="a" ? 1 : -1, i,ab,j,
			findSortKey;

			if(type === undefined ) { type = "text"; }
			if (type == 'float' || type== 'number' || type== 'currency' || type== 'numeric') {
				findSortKey = function($cell, a) {
					var key = parseFloat( String($cell).replace(_stripNum, ''));
					return isNaN(key) ? 0.00 : key;
				};
			} else if (type=='int' || type=='integer') {
				findSortKey = function($cell, a) {
					return $cell ? parseFloat(String($cell).replace(_stripNum, '')) : 0;
				};
			} else if(type == 'date' || type == 'datetime') {
				findSortKey = function($cell, a) {
					return $.jgrid.parseDate(dfmt,$cell).getTime();
				};
			} else if($.isFunction(type)) {
				findSortKey = type;
			} else {
				findSortKey = function($cell, a) {
					if(!$cell) {$cell ="";}
					return $.trim(String($cell).toUpperCase());
				};
			}
			$.each(data,function(i,v){
				ab = by!=="" ? $.jgrid.getAccessor(v,by) : v;
				if(ab === undefined) { ab = ""; }
				ab = findSortKey(ab, v);
				_sortData.push({ 'vSort': ab,'index':i});
			});

			_sortData.sort(function(a,b){
				a = a.vSort;
				b = b.vSort;
				return self._compare(a,b,newDir);
			});
			j=0;
			var nrec= data.length;
			// overhead, but we do not change the original data.
			while(j<nrec) {
				i = _sortData[j].index;
				sortData.push(data[i]);
				j++;
			}
			return sortData;
		};
		this._getGroup=function(data,by,dir,type, dfmt){
			var results=[],
			group=null,
			last=null, val;
			$.each(self._getOrder(data,by,dir,type, dfmt),function(i,v){
				val = $.jgrid.getAccessor(v, by);
				if(val === undefined) { val = ""; }
				if(!self._equals(last,val)){
					last=val;
					if(group !== null){
						results.push(group);
					}
					group=self._group(by,val);
				}
				group.items.push(v);
			});
			if(group !== null){
				results.push(group);
			}
			return results;
		};
		this.ignoreCase=function(){
			_usecase=false;
			return self;
		};
		this.useCase=function(){
			_usecase=true;
			return self;
		};
		this.trim=function(){
			_trim=true;
			return self;
		};
		this.noTrim=function(){
			_trim=false;
			return self;
		};
		this.execute=function(){
			var match=_query, results=[];
			if(match === null){
				return self;
			}
			$.each(_data,function(){
				if(eval(match)){results.push(this);}
			});
			_data=results;
			return self;
		};
		this.data=function(){
			return _data;
		};
		this.select=function(f){
			self._performSort();
			if(!self._hasData()){ return[]; }
			self.execute();
			if($.isFunction(f)){
				var results=[];
				$.each(_data,function(i,v){
					results.push(f(v));
				});
				return results;
			}
			return _data;
		};
		this.hasMatch=function(f){
			if(!self._hasData()) { return false; }
			self.execute();
			return _data.length>0;
		};
		this.andNot=function(f,v,x){
			_negate=!_negate;
			return self.and(f,v,x);
		};
		this.orNot=function(f,v,x){
			_negate=!_negate;
			return self.or(f,v,x);
		};
		this.not=function(f,v,x){
			return self.andNot(f,v,x);
		};
		this.and=function(f,v,x){
			_queuedOperator=" && ";
			if(f===undefined){
				return self;
			}
			return self._repeatCommand(f,v,x);
		};
		this.or=function(f,v,x){
			_queuedOperator=" || ";
			if(f===undefined) { return self; }
			return self._repeatCommand(f,v,x);
		};
		this.isNot=function(f){
			_negate=!_negate;
			return self.is(f);
		};
		this.is=function(f){
			self._append('this.'+f);
			self._resetNegate();
			return self;
		};
		this._compareValues=function(func,f,v,how,t){
			var fld;
			if(_useProperties){
				fld='jQuery.jgrid.getAccessor(this,\''+f+'\')';
			}else{
				fld='this';
			}
			if(v===undefined) { v = null; }
			//var val=v===null?f:v,
			var val =v,
			swst = t.stype === undefined ? "text" : t.stype;
			if(v !== null) {
			switch(swst) {
				case 'int':
				case 'integer':
					val = (isNaN(Number(val)) || val==="") ? '0' : val; // To be fixed with more inteligent code
					fld = 'parseInt('+fld+',10)';
					val = 'parseInt('+val+',10)';
					break;
				case 'float':
				case 'number':
				case 'numeric':
					val = String(val).replace(_stripNum, '');
					val = (isNaN(Number(val)) || val==="") ? '0' : val; // To be fixed with more inteligent code
					fld = 'parseFloat('+fld+')';
					val = 'parseFloat('+val+')';
					break;
				case 'date':
				case 'datetime':
					val = String($.jgrid.parseDate(t.newfmt || 'Y-m-d',val).getTime());
					fld = 'jQuery.jgrid.parseDate("'+t.srcfmt+'",'+fld+').getTime()';
					break;
				default :
					fld=self._getStr(fld);
					val=self._getStr('"'+self._toStr(val)+'"');
			}
			}
			self._append(fld+' '+how+' '+val);
			self._setCommand(func,f);
			self._resetNegate();
			return self;
		};
		this.equals=function(f,v,t){
			return self._compareValues(self.equals,f,v,"==",t);
		};
		this.notEquals=function(f,v,t){
			return self._compareValues(self.equals,f,v,"!==",t);
		};
		this.isNull = function(f,v,t){
			return self._compareValues(self.equals,f,null,"===",t);
		};
		this.greater=function(f,v,t){
			return self._compareValues(self.greater,f,v,">",t);
		};
		this.less=function(f,v,t){
			return self._compareValues(self.less,f,v,"<",t);
		};
		this.greaterOrEquals=function(f,v,t){
			return self._compareValues(self.greaterOrEquals,f,v,">=",t);
		};
		this.lessOrEquals=function(f,v,t){
			return self._compareValues(self.lessOrEquals,f,v,"<=",t);
		};
		this.startsWith=function(f,v){
			var val = (v===undefined || v===null) ? f: v,
			length=_trim ? $.trim(val.toString()).length : val.toString().length;
			if(_useProperties){
				self._append(self._getStr('jQuery.jgrid.getAccessor(this,\''+f+'\')')+'.substr(0,'+length+') == '+self._getStr('"'+self._toStr(v)+'"'));
			}else{
				length=_trim?$.trim(v.toString()).length:v.toString().length;
				self._append(self._getStr('this')+'.substr(0,'+length+') == '+self._getStr('"'+self._toStr(f)+'"'));
			}
			self._setCommand(self.startsWith,f);
			self._resetNegate();
			return self;
		};
		this.endsWith=function(f,v){
			var val = (v===undefined || v===null) ? f: v,
			length=_trim ? $.trim(val.toString()).length:val.toString().length;
			if(_useProperties){
				self._append(self._getStr('jQuery.jgrid.getAccessor(this,\''+f+'\')')+'.substr('+self._getStr('jQuery.jgrid.getAccessor(this,\''+f+'\')')+'.length-'+length+','+length+') == "'+self._toStr(v)+'"');
			} else {
				self._append(self._getStr('this')+'.substr('+self._getStr('this')+'.length-"'+self._toStr(f)+'".length,"'+self._toStr(f)+'".length) == "'+self._toStr(f)+'"');
			}
			self._setCommand(self.endsWith,f);self._resetNegate();
			return self;
		};
		this.contains=function(f,v){
			if(_useProperties){
				self._append(self._getStr('jQuery.jgrid.getAccessor(this,\''+f+'\')')+'.indexOf("'+self._toStr(v)+'",0) > -1');
			}else{
				self._append(self._getStr('this')+'.indexOf("'+self._toStr(f)+'",0) > -1');
			}
			self._setCommand(self.contains,f);
			self._resetNegate();
			return self;
		};
		this.groupBy=function(by,dir,type, datefmt){
			if(!self._hasData()){
				return null;
			}
			return self._getGroup(_data,by,dir,type, datefmt);
		};
		this.orderBy=function(by,dir,stype, dfmt){
			dir =  dir === undefined || dir === null ? "a" :$.trim(dir.toString().toLowerCase());
			if(stype === null || stype === undefined) { stype = "text"; }
			if(dfmt === null || dfmt === undefined) { dfmt = "Y-m-d"; }
			if(dir=="desc"||dir=="descending"){dir="d";}
			if(dir=="asc"||dir=="ascending"){dir="a";}
			_sorting.push({by:by,dir:dir,type:stype, datefmt: dfmt});
			return self;
		};
		return self;
		};
	return new queryObject(source,null);
	},
	extend : function(methods) {
		$.extend($.fn.jqGrid,methods);
		if (!this.no_legacy_api) {
			$.fn.extend(methods);
		}
	}
});

$.fn.jqGrid = function( pin ) {
	if (typeof pin == 'string') {
		//var fn = $.fn.jqGrid[pin];
		var fn = $.jgrid.getAccessor($.fn.jqGrid,pin);
		if (!fn) {
			throw ("jqGrid - No such method: " + pin);
		}
		var args = $.makeArray(arguments).slice(1);
		return fn.apply(this,args);
	}
	return this.each( function() {
		if(this.grid) {return;}

		var p = $.extend(true,{
			url: "",
			height: 150,
			page: 1,
			rowNum: 20,
			rowTotal : null,
			records: 0,
			pager: "",
			pgbuttons: true,
			pginput: true,
			colModel: [],
			rowList: [],
			colNames: [],
			sortorder: "asc",
			sortname: "",
			datatype: "xml",
			mtype: "GET",
			altRows: false,
			selarrrow: [],
			savedRow: [],
			shrinkToFit: true,
			xmlReader: {},
			jsonReader: {},
			subGrid: false,
			subGridModel :[],
			reccount: 0,
			lastpage: 0,
			lastsort: 0,
			selrow: null,
			beforeSelectRow: null,
			onSelectRow: null,
			onSortCol: null,
			ondblClickRow: null,
			onRightClickRow: null,
			onPaging: null,
			onSelectAll: null,
			loadComplete: null,
			gridComplete: null,
			loadError: null,
			loadBeforeSend: null,
			afterInsertRow: null,
			beforeRequest: null,
			onHeaderClick: null,
			viewrecords: false,
			loadonce: false,
			multiselect: false,
			multikey: false,
			editurl: null,
			search: false,
			caption: "",
			hidegrid: true,
			hiddengrid: false,
			postData: {},
			userData: {},
			treeGrid : false,
			treeGridModel : 'nested',
			treeReader : {},
			treeANode : -1,
			ExpandColumn: null,
			tree_root_level : 0,
			prmNames: {page:"page",rows:"rows", sort: "sidx",order: "sord", search:"_search", nd:"nd", id:"id",oper:"oper",editoper:"edit",addoper:"add",deloper:"del", subgridid:"id", npage: null, totalrows:"totalrows"},
			forceFit : false,
			gridstate : "visible",
			cellEdit: false,
			cellsubmit: "remote",
			nv:0,
			loadui: "enable",
			toolbar: [false,""],
			scroll: false,
			multiboxonly : false,
			deselectAfterSort : true,
			scrollrows : false,
			autowidth: false,
			scrollOffset :18,
			cellLayout: 5,
			subGridWidth: 20,
			multiselectWidth: 20,
			gridview: false,
			rownumWidth: 25,
			rownumbers : false,
			pagerpos: 'center',
			recordpos: 'right',
			footerrow : false,
			userDataOnFooter : false,
			hoverrows : true,
			altclass : 'ui-priority-secondary',
			viewsortcols : [false,'vertical',true],
			resizeclass : '',
			autoencode : false,
			remapColumns : [],
			ajaxGridOptions :{},
			direction : "ltr",
			toppager: false,
			headertitles: false,
			scrollTimeout: 40,
			data : [],
			_index : {},
			grouping : false,
			groupingView : {groupField:[],groupOrder:[], groupText:[],groupColumnShow:[],groupSummary:[], showSummaryOnHide: false, sortitems:[], sortnames:[], groupDataSorted : false, summary:[],summaryval:[], plusicon: 'ui-icon-circlesmall-plus', minusicon: 'ui-icon-circlesmall-minus'},
			ignoreCase : false,
			cmTemplate : {}
		}, $.jgrid.defaults, pin || {});
		var grid={
			headers:[],
			cols:[],
			footers: [],
			dragStart: function(i,x,y) {
				this.resizing = { idx: i, startX: x.clientX, sOL : y[0]};
				this.hDiv.style.cursor = "col-resize";
				this.curGbox = $("#rs_m"+$.jgrid.jqID(p.id),"#gbox_"+$.jgrid.jqID(p.id));
				this.curGbox.css({display:"block",left:y[0],top:y[1],height:y[2]});
				if($.isFunction(p.resizeStart)) { p.resizeStart.call(this,x,i); }
				document.onselectstart=function(){return false;};
			},
			dragMove: function(x) {
				if(this.resizing) {
					var diff = x.clientX-this.resizing.startX,
					h = this.headers[this.resizing.idx],
					newWidth = p.direction === "ltr" ? h.width + diff : h.width - diff, hn, nWn;
					if(newWidth > 33) {
						this.curGbox.css({left:this.resizing.sOL+diff});
						if(p.forceFit===true ){
							hn = this.headers[this.resizing.idx+p.nv];
							nWn = p.direction === "ltr" ? hn.width - diff : hn.width + diff;
							if(nWn >33) {
								h.newWidth = newWidth;
								hn.newWidth = nWn;
							}
						} else {
							this.newWidth = p.direction === "ltr" ? p.tblwidth+diff : p.tblwidth-diff;
							h.newWidth = newWidth;
						}
					}
				}
			},
			dragEnd: function() {
				this.hDiv.style.cursor = "default";
				if(this.resizing) {
					var idx = this.resizing.idx,
					nw = this.headers[idx].newWidth || this.headers[idx].width;
					nw = parseInt(nw,10);
					this.resizing = false;
					$("#rs_m"+$.jgrid.jqID(p.id)).css("display","none");
					p.colModel[idx].width = nw;
					this.headers[idx].width = nw;
					this.headers[idx].el.style.width = nw + "px";
					this.cols[idx].style.width = nw+"px";
					if(this.footers.length>0) {this.footers[idx].style.width = nw+"px";}
					if(p.forceFit===true){
						nw = this.headers[idx+p.nv].newWidth || this.headers[idx+p.nv].width;
						this.headers[idx+p.nv].width = nw;
						this.headers[idx+p.nv].el.style.width = nw + "px";
						this.cols[idx+p.nv].style.width = nw+"px";
						if(this.footers.length>0) {this.footers[idx+p.nv].style.width = nw+"px";}
						p.colModel[idx+p.nv].width = nw;
					} else {
						p.tblwidth = this.newWidth || p.tblwidth;
						$('table:first',this.bDiv).css("width",p.tblwidth+"px");
						$('table:first',this.hDiv).css("width",p.tblwidth+"px");
						this.hDiv.scrollLeft = this.bDiv.scrollLeft;
						if(p.footerrow) {
							$('table:first',this.sDiv).css("width",p.tblwidth+"px");
							this.sDiv.scrollLeft = this.bDiv.scrollLeft;
						}
					}
					if($.isFunction(p.resizeStop)) { p.resizeStop.call(this,nw,idx); }
				}
				this.curGbox = null;
				document.onselectstart=function(){return true;};
			},
			populateVisible: function() {
				if (grid.timer) { clearTimeout(grid.timer); }
				grid.timer = null;
				var dh = $(grid.bDiv).height();
				if (!dh) { return; }
				var table = $("table:first", grid.bDiv);
				var rows, rh;
				if(table[0].rows.length) {
					try {
						rows = table[0].rows[1];
						rh = rows ? $(rows).outerHeight() || grid.prevRowHeight : grid.prevRowHeight;
					} catch (pv) {
						rh = grid.prevRowHeight;
					}
				}
				if (!rh) { return; }
				grid.prevRowHeight = rh;
				var rn = p.rowNum;
				var scrollTop = grid.scrollTop = grid.bDiv.scrollTop;
				var ttop = Math.round(table.position().top) - scrollTop;
				var tbot = ttop + table.height();
				var div = rh * rn;
				var page, npage, empty;
			    if ( tbot < dh && ttop <= 0 &&
					(p.lastpage===undefined||parseInt((tbot + scrollTop + div - 1) / div,10) <= p.lastpage))
				{
					npage = parseInt((dh - tbot + div - 1) / div,10);
					if (tbot >= 0 || npage < 2 || p.scroll === true) {
						page = Math.round((tbot + scrollTop) / div) + 1;
						ttop = -1;
					} else {
						ttop = 1;
					}
				}
				if (ttop > 0) {
					page = parseInt(scrollTop / div,10) + 1;
					npage = parseInt((scrollTop + dh) / div,10) + 2 - page;
					empty = true;
				}
				if (npage) {
					if (p.lastpage && page > p.lastpage || p.lastpage==1 || (page === p.page && page===p.lastpage) ) {
						return;
					}
					if (grid.hDiv.loading) {
						grid.timer = setTimeout(grid.populateVisible, p.scrollTimeout);
					} else {
						p.page = page;
						if (empty) {
							grid.selectionPreserver(table[0]);
							grid.emptyRows(grid.bDiv,false, false);
						}
						grid.populate(npage);
					}
				}
			},
			scrollGrid: function( e ) {
				if(p.scroll) {
					var scrollTop = grid.bDiv.scrollTop;
					if(grid.scrollTop === undefined) { grid.scrollTop = 0; }
					if (scrollTop != grid.scrollTop) {
						grid.scrollTop = scrollTop;
						if (grid.timer) { clearTimeout(grid.timer); }
						grid.timer = setTimeout(grid.populateVisible, p.scrollTimeout);
					}
				}
				grid.hDiv.scrollLeft = grid.bDiv.scrollLeft;
				if(p.footerrow) {
					grid.sDiv.scrollLeft = grid.bDiv.scrollLeft;
				}
				if( e ) { e.stopPropagation(); }
			},
			selectionPreserver : function(ts) {
				var p = ts.p;
				var sr = p.selrow, sra = p.selarrrow ? $.makeArray(p.selarrrow) : null;
				var left = ts.grid.bDiv.scrollLeft;
				var complete = p.gridComplete;
				p.gridComplete = function() {
					p.selrow = null;
					p.selarrrow = [];
					if(p.multiselect && sra && sra.length>0) {
						for(var i=0;i<sra.length;i++){
							if (sra[i] != sr) {
								$(ts).jqGrid("setSelection",sra[i],false);
							}
						}
					}
					if (sr) {
						$(ts).jqGrid("setSelection",sr,false);
					}
					ts.grid.bDiv.scrollLeft = left;
					p.gridComplete = complete;
					if (p.gridComplete) {
						complete();
					}
				};
			}
		};
		if(this.tagName.toUpperCase()!='TABLE') {
			alert("Element is not a table");
			return;
		}
		$(this).empty().attr("tabindex","1");
		this.p = p ;
		var i, dir,ts;
		if(this.p.colNames.length === 0) {
			for (i=0;i<this.p.colModel.length;i++){
				this.p.colNames[i] = this.p.colModel[i].label || this.p.colModel[i].name;
			}
		}
		if( this.p.colNames.length !== this.p.colModel.length ) {
			alert($.jgrid.errors.model);
			return;
		}
		var gv = $("<div class='ui-jqgrid-view'></div>"), ii,
		isMSIE = $.browser.msie ? true:false,
		isSafari = $.browser.webkit || $.browser.safari ? true : false;
		ts = this;
		ts.p.direction = $.trim(ts.p.direction.toLowerCase());
		if($.inArray(ts.p.direction,["ltr","rtl"]) == -1) { ts.p.direction = "ltr"; }
		dir = ts.p.direction;

		$(gv).insertBefore(this);
		$(this).appendTo(gv).removeClass("scroll");
		var eg = $("<div class='ui-jqgrid ui-widget ui-widget-content ui-corner-all'></div>");
		$(eg).insertBefore(gv).attr({"id" : "gbox_"+this.id,"dir":dir});
		$(gv).appendTo(eg).attr("id","gview_"+this.id);
		if (isMSIE && $.browser.version <= 6) {
			ii = '<iframe style="display:block;position:absolute;z-index:-1;filter:Alpha(Opacity=\'0\');" src="javascript:false;"></iframe>';
		} else { ii="";}
		$("<div class='ui-widget-overlay jqgrid-overlay' id='lui_"+this.id+"'></div>").append(ii).insertBefore(gv);
		$("<div class='loading ui-state-default ui-state-active' id='load_"+this.id+"'>"+this.p.loadtext+"</div>").insertBefore(gv);
		$(this).attr({cellspacing:"0",cellpadding:"0",border:"0","role":"grid","aria-multiselectable":!!this.p.multiselect,"aria-labelledby":"gbox_"+this.id});
		var sortkeys = ["shiftKey","altKey","ctrlKey"],
		intNum = function(val,defval) {
			val = parseInt(val,10);
			if (isNaN(val)) { return defval ? defval : 0;}
			else {return val;}
		},
		formatCol = function (pos, rowInd, tv, rawObject, rowId, rdata){
			var cm = ts.p.colModel[pos],
			ral = cm.align, result="style=\"", clas = cm.classes, nm = cm.name, celp, acp=[];
			if(ral) { result += "text-align:"+ral+";"; }
			if(cm.hidden===true) { result += "display:none;"; }
			if(rowInd===0) {
				result += "width: "+grid.headers[pos].width+"px;";
			} else if (cm.cellattr && $.isFunction(cm.cellattr))
			{
				celp = cm.cellattr.call(ts, rowId, tv, rawObject, cm, rdata);
				if(celp && typeof(celp) === "string") {
					celp = celp.replace(/style/i,'style').replace(/title/i,'title');
					if(celp.indexOf('title') > -1) { cm.title=false;}
					if(celp.indexOf('class') > -1) { clas = undefined;}
					acp = celp.split("style");
					if(acp.length === 2 ) {
						acp[1] =  $.trim(acp[1].replace("=",""));
						if(acp[1].indexOf("'") === 0 || acp[1].indexOf('"') === 0) {
							acp[1] = acp[1].substring(1);
						}
						result += acp[1].replace(/'/gi,'"');
					} else {
						result += "\"";
					}
				}
			}
			if(!acp.length) { acp[0] = ""; result += "\"";}
			result += (clas !== undefined ? (" class=\""+clas+"\"") :"") + ((cm.title && tv) ? (" title=\""+$.jgrid.stripHtml(tv)+"\"") :"");
			result += " aria-describedby=\""+ts.p.id+"_"+nm+"\"";
			return result + acp[0];
		},
		cellVal =  function (val) {
			return val === undefined || val === null || val === "" ? "&#160;" : (ts.p.autoencode ? $.jgrid.htmlEncode(val) : val+"");
		},
		formatter = function (rowId, cellval , colpos, rwdat, _act){
			var cm = ts.p.colModel[colpos],v;
			if(typeof cm.formatter !== 'undefined') {
				var opts= {rowId: rowId, colModel:cm, gid:ts.p.id, pos:colpos };
				if($.isFunction( cm.formatter ) ) {
					v = cm.formatter.call(ts,cellval,opts,rwdat,_act);
				} else if($.fmatter){
					v = $.fn.fmatter(cm.formatter, cellval,opts, rwdat, _act);
				} else {
					v = cellVal(cellval);
				}
			} else {
				v = cellVal(cellval);
			}
			return v;
		},
		addCell = function(rowId,cell,pos,irow, srvr) {
			var v,prp;
			v = formatter(rowId,cell,pos,srvr,'add');
			prp = formatCol( pos,irow, v, srvr, rowId, true);
			return "<td role=\"gridcell\" "+prp+">"+v+"</td>";
		},
		addMulti = function(rowid,pos,irow){
			var	v = "<input role=\"checkbox\" type=\"checkbox\""+" id=\"jqg_"+ts.p.id+"_"+rowid+"\" class=\"cbox\" name=\"jqg_"+ts.p.id+"_"+rowid+"\"/>",
			prp = formatCol( pos,irow,'',null, rowid, true);
			return "<td role=\"gridcell\" "+prp+">"+v+"</td>";
		},
		addRowNum = function (pos,irow,pG,rN) {
			var v =  (parseInt(pG,10)-1)*parseInt(rN,10)+1+irow,
			prp = formatCol( pos,irow,v, null, irow, true);
			return "<td role=\"gridcell\" class=\"ui-state-default jqgrid-rownum\" "+prp+">"+v+"</td>";
		},
		reader = function (datatype) {
			var field, f=[], j=0, i;
			for(i =0; i<ts.p.colModel.length; i++){
				field = ts.p.colModel[i];
				if (field.name !== 'cb' && field.name !=='subgrid' && field.name !=='rn') {
					if(datatype == "local") {
						f[j] = field.name;
					} else {
						f[j] = (datatype=="xml") ? field.xmlmap || field.name : field.jsonmap || field.name;
					}
					j++;
				}
			}
			return f;
		},
		orderedCols = function (offset) {
			var order = ts.p.remapColumns;
			if (!order || !order.length) {
				order = $.map(ts.p.colModel, function(v,i) { return i; });
			}
			if (offset) {
				order = $.map(order, function(v) { return v<offset?null:v-offset; });
			}
			return order;
		},
		emptyRows = function (parent, scroll, locdata) {
			if(ts.p.deepempty) {$("#"+$.jgrid.jqID(ts.p.id)+" tbody:first tr:gt(0)").remove();}
			else {
				var trf = $("#"+$.jgrid.jqID(ts.p.id)+" tbody:first tr:first")[0];
				$("#"+$.jgrid.jqID(ts.p.id)+" tbody:first").empty().append(trf);
			}
			if (scroll && ts.p.scroll) {
				$(">div:first", parent).css({height:"auto"}).children("div:first").css({height:0,display:"none"});
				parent.scrollTop = 0;
			}
			if(locdata === true) {
				if(ts.p.treeGrid === true ) {
				ts.p.data = []; ts.p._index = {};
			}
			}
		},
		refreshIndex = function() {
			var datalen = ts.p.data.length, idname, i, val,
			ni = ts.p.rownumbers===true ? 1 :0,
			gi = ts.p.multiselect ===true ? 1 :0,
			si = ts.p.subGrid===true ? 1 :0;

			if(ts.p.keyIndex === false || ts.p.loadonce === true) {
				idname = ts.p.localReader.id;
			} else {
				idname = ts.p.colModel[ts.p.keyIndex+gi+si+ni].name;
			}
			for(i =0;i < datalen; i++) {
				val = $.jgrid.getAccessor(ts.p.data[i],idname);
				ts.p._index[val] = i;
			}
		},
		addXmlData = function (xml,t, rcnt, more, adjust) {
			var startReq = new Date(),
			locdata = (ts.p.datatype != "local" && ts.p.loadonce) || ts.p.datatype == "xmlstring",
			xmlid = "_id_",
			frd = ts.p.datatype == "local" ? "local" : "xml";
			if(locdata) {
				ts.p.data = [];
				ts.p._index = {};
				ts.p.localReader.id = xmlid;
			}
			ts.p.reccount = 0;
			if($.isXMLDoc(xml)) {
				if(ts.p.treeANode===-1 && !ts.p.scroll) {
					emptyRows(t,false, true);
					rcnt=1;
				} else { rcnt = rcnt > 1 ? rcnt :1; }
			} else { return; }
			var i,fpos,ir=0,v,row,gi=0,si=0,ni=0,idn, getId,f=[],F,rd ={}, xmlr,rid, rowData=[], cn=(ts.p.altRows === true) ? " "+ts.p.altclass:"",cn1;
			if(!ts.p.xmlReader.repeatitems) {f = reader(frd);}
			if( ts.p.keyIndex===false) {
				idn = ts.p.xmlReader.id;
			} else {
				idn = ts.p.keyIndex;
			}
			if(f.length>0 && !isNaN(idn)) {
				if (ts.p.remapColumns && ts.p.remapColumns.length) {
					idn = $.inArray(idn, ts.p.remapColumns);
				}
				idn=f[idn];
			}
			if( (idn+"").indexOf("[") === -1 ) {
				if (f.length) {
					getId = function( trow, k) {return $(idn,trow).text() || k;};
				} else {
					getId = function( trow, k) {return $(ts.p.xmlReader.cell,trow).eq(idn).text() || k;};
				}
			}
			else {
				getId = function( trow, k) {return trow.getAttribute(idn.replace(/[\[\]]/g,"")) || k;};
			}
			ts.p.userData = {};
			$(ts.p.xmlReader.page,xml).each(function() {ts.p.page = this.textContent  || this.text || 0; });
			$(ts.p.xmlReader.total,xml).each(function() {ts.p.lastpage = this.textContent  || this.text; if(ts.p.lastpage===undefined) { ts.p.lastpage=1; } }  );
			$(ts.p.xmlReader.records,xml).each(function() {ts.p.records = this.textContent  || this.text  || 0; }  );
			$(ts.p.xmlReader.userdata,xml).each(function() {ts.p.userData[this.getAttribute("name")]=this.textContent || this.text;});
			var gxml = $(ts.p.xmlReader.root+" "+ts.p.xmlReader.row,xml);
			if (!gxml) { gxml = []; }
			var gl = gxml.length, j=0, grpdata={}, rn;
			if(gxml && gl){
			rn = parseInt(ts.p.rowNum,10);
			var br=ts.p.scroll?$.jgrid.randId():1,altr;
			if (adjust) { rn *= adjust+1; }
			var afterInsRow = $.isFunction(ts.p.afterInsertRow), hiderow="";
			if(ts.p.grouping && ts.p.groupingView.groupCollapse === true) {
				hiderow = " style=\"display:none;\"";
			}
			while (j<gl) {
				xmlr = gxml[j];
				rid = getId(xmlr,br+j);
				altr = rcnt === 0 ? 0 : rcnt+1;
				cn1 = (altr+j)%2 == 1 ? cn : '';
				rowData.push( "<tr"+hiderow+" id=\""+rid+"\" tabindex=\"-1\" role=\"row\" class =\"ui-widget-content jqgrow ui-row-"+ts.p.direction+""+cn1+"\">" );
				if(ts.p.rownumbers===true) {
					rowData.push( addRowNum(0,j,ts.p.page,ts.p.rowNum) );
					ni=1;
				}
				if(ts.p.multiselect===true) {
					rowData.push( addMulti(rid,ni,j) );
					gi=1;
				}
				if (ts.p.subGrid===true) {
					rowData.push( $(ts).jqGrid("addSubGridCell",gi+ni,j+rcnt) );
					si= 1;
				}
				if(ts.p.xmlReader.repeatitems){
					if (!F) { F=orderedCols(gi+si+ni); }
					var cells = $(ts.p.xmlReader.cell,xmlr);
					$.each(F, function (k) {
						var cell = cells[this];
						if (!cell) { return false; }
						v = cell.textContent || cell.text;
						rd[ts.p.colModel[k+gi+si+ni].name] = v;
						rowData.push( addCell(rid,v,k+gi+si+ni,j+rcnt,xmlr) );
					});
				} else {
					for(i = 0; i < f.length;i++) {
						v = $(f[i],xmlr).text();
						rd[ts.p.colModel[i+gi+si+ni].name] = v;
						rowData.push( addCell(rid, v, i+gi+si+ni, j+rcnt, xmlr) );
					}
				}
				rowData.push("</tr>");
				if(ts.p.grouping) {
					var grlen = ts.p.groupingView.groupField.length, grpitem = [];
					for(var z=0;z<grlen;z++) {
						grpitem.push(rd[ts.p.groupingView.groupField[z]]);
					}
					grpdata = $(ts).jqGrid('groupingPrepare',rowData, grpitem, grpdata, rd);
					rowData = [];
				}
				if(locdata || ts.p.treeGrid === true) {
					rd[xmlid] = rid;
					ts.p.data.push(rd);
					ts.p._index[rid] = ts.p.data.length-1;
				}
				if(ts.p.gridview === false ) {
					$("tbody:first",t).append(rowData.join(''));
					if(afterInsRow) {ts.p.afterInsertRow.call(ts,rid,rd,xmlr);}
					rowData=[];
				}
				rd={};
				ir++;
				j++;
				if(ir==rn) {break;}
			}
			}
			if(ts.p.gridview === true) {
				fpos = ts.p.treeANode > -1 ? ts.p.treeANode: 0;
				if(ts.p.grouping) {
					$(ts).jqGrid('groupingRender',grpdata,ts.p.colModel.length);
					grpdata = null;
				} else if(ts.p.treeGrid === true && fpos > 0) {
					$(ts.rows[fpos]).after(rowData.join(''));
				} else {
					$("tbody:first",t).append(rowData.join(''));
				}
			}
			if(ts.p.subGrid === true ) {
				try {$(ts).jqGrid("addSubGrid",gi+ni);} catch (_){}
			}
			ts.p.totaltime = new Date() - startReq;
			if(ir>0) { if(ts.p.records===0) { ts.p.records=gl;} }
			rowData =null;
			if( ts.p.treeGrid === true) {
				try {$(ts).jqGrid("setTreeNode", fpos+1, ir+fpos+1);} catch (e) {}
			}
			if(!ts.p.treeGrid && !ts.p.scroll) {ts.grid.bDiv.scrollTop = 0;}
			ts.p.reccount=ir;
			ts.p.treeANode = -1;
			if(ts.p.userDataOnFooter) { $(ts).jqGrid("footerData","set",ts.p.userData,true); }
			if(locdata) {
				ts.p.records = gl;
				ts.p.lastpage = Math.ceil(gl/ rn);
			}
			if (!more) { ts.updatepager(false,true); }
			if(locdata) {
				while (ir<gl) {
					xmlr = gxml[ir];
					rid = getId(xmlr,ir);
					if(ts.p.xmlReader.repeatitems){
						if (!F) { F=orderedCols(gi+si+ni); }
						var cells2 = $(ts.p.xmlReader.cell,xmlr);
						$.each(F, function (k) {
							var cell = cells2[this];
							if (!cell) { return false; }
							v = cell.textContent || cell.text;
							rd[ts.p.colModel[k+gi+si+ni].name] = v;
						});
					} else {
						for(i = 0; i < f.length;i++) {
							v = $(f[i],xmlr).text();
							rd[ts.p.colModel[i+gi+si+ni].name] = v;
						}
					}
					rd[xmlid] = rid;
					ts.p.data.push(rd);
					ts.p._index[rid] = ts.p.data.length-1;
					rd = {};
					ir++;
				}
			}
		},
		addJSONData = function(data,t, rcnt, more, adjust) {
			var startReq = new Date();
			if(data) {
				if(ts.p.treeANode === -1 && !ts.p.scroll) {
					emptyRows(t,false, true);
					rcnt=1;
				} else { rcnt = rcnt > 1 ? rcnt :1; }
			} else { return; }

			var dReader, locid = "_id_", frd,
			locdata = (ts.p.datatype != "local" && ts.p.loadonce) || ts.p.datatype == "jsonstring";
			if(locdata) { ts.p.data = []; ts.p._index = {}; ts.p.localReader.id = locid;}
			ts.p.reccount = 0;
			if(ts.p.datatype == "local") {
				dReader =  ts.p.localReader;
				frd= 'local';
			} else {
				dReader =  ts.p.jsonReader;
				frd='json';
			}
			var ir=0,v,i,j,f=[],F,cur,gi=0,si=0,ni=0,len,drows,idn,rd={}, fpos, idr,rowData=[],cn=(ts.p.altRows === true) ? " "+ts.p.altclass:"",cn1,lp;
			ts.p.page = $.jgrid.getAccessor(data,dReader.page) || 0;
			lp = $.jgrid.getAccessor(data,dReader.total);
			ts.p.lastpage = lp === undefined ? 1 : lp;
			ts.p.records = $.jgrid.getAccessor(data,dReader.records) || 0;
			ts.p.userData = $.jgrid.getAccessor(data,dReader.userdata) || {};
			if(!dReader.repeatitems) {
				F = f = reader(frd);
			}
			if( ts.p.keyIndex===false ) {
				idn = dReader.id;
			} else {
				idn = ts.p.keyIndex;
			}
			if(f.length>0 && !isNaN(idn)) {
				if (ts.p.remapColumns && ts.p.remapColumns.length) {
					idn = $.inArray(idn, ts.p.remapColumns);
				}
				idn=f[idn];
			}
			drows = $.jgrid.getAccessor(data,dReader.root);
			if (!drows) { drows = []; }
			len = drows.length; i=0;
			var rn = parseInt(ts.p.rowNum,10),br=ts.p.scroll?$.jgrid.randId():1, altr;
			if (adjust) { rn *= adjust+1; }
			var afterInsRow = $.isFunction(ts.p.afterInsertRow), grpdata={}, hiderow="";
			if(ts.p.grouping && ts.p.groupingView.groupCollapse === true) {
				hiderow = " style=\"display:none;\"";
			}
			while (i<len) {
				cur = drows[i];
				idr = $.jgrid.getAccessor(cur,idn);
				if(idr === undefined) {
					idr = br+i;
					if(f.length===0){
						if(dReader.cell){
							var ccur = $.jgrid.getAccessor(cur,dReader.cell);
							idr = ccur[idn] || idr;
							ccur=null;
						}
					}
				}
				altr = rcnt === 1 ? 0 : rcnt;
				cn1 = (altr+i)%2 == 1 ? cn : '';
				rowData.push("<tr"+hiderow+" id=\""+ idr +"\" tabindex=\"-1\" role=\"row\" class= \"ui-widget-content jqgrow ui-row-"+ts.p.direction+""+cn1+"\">");
				if(ts.p.rownumbers===true) {
					rowData.push( addRowNum(0,i,ts.p.page,ts.p.rowNum) );
					ni=1;
				}
				if(ts.p.multiselect){
					rowData.push( addMulti(idr,ni,i) );
					gi = 1;
				}
				if (ts.p.subGrid) {
					rowData.push( $(ts).jqGrid("addSubGridCell",gi+ni,i+rcnt) );
					si= 1;
				}
				if (dReader.repeatitems) {
					if(dReader.cell) {cur = $.jgrid.getAccessor(cur,dReader.cell);}
					if (!F) { F=orderedCols(gi+si+ni); }
				}
				for (j=0;j<F.length;j++) {
					v = $.jgrid.getAccessor(cur,F[j]);
					rowData.push( addCell(idr,v,j+gi+si+ni,i+rcnt,cur) );
					rd[ts.p.colModel[j+gi+si+ni].name] = v;
				}
				rowData.push( "</tr>" );
				if(ts.p.grouping) {
					var grlen = ts.p.groupingView.groupField.length, grpitem = [];
					for(var z=0;z<grlen;z++) {
						grpitem.push(rd[ts.p.groupingView.groupField[z]]);
					}
					grpdata = $(ts).jqGrid('groupingPrepare',rowData, grpitem, grpdata, rd);
					rowData = [];
				}
				if(locdata || ts.p.treeGrid===true) {
					rd[locid] = idr;
					ts.p.data.push(rd);
					ts.p._index[idr] = ts.p.data.length-1;
				}
				if(ts.p.gridview === false ) {
					$("#"+$.jgrid.jqID(ts.p.id)+" tbody:first").append(rowData.join(''));
					if(afterInsRow) {ts.p.afterInsertRow.call(ts,idr,rd,cur);}
					rowData=[];//ari=0;
				}
				rd={};
				ir++;
				i++;
				if(ir==rn) { break; }
			}
			if(ts.p.gridview === true ) {
				fpos = ts.p.treeANode > -1 ? ts.p.treeANode: 0;
				if(ts.p.grouping) {
					$(ts).jqGrid('groupingRender',grpdata,ts.p.colModel.length);
					grpdata = null;
				} else if(ts.p.treeGrid === true && fpos > 0) {
					$(ts.rows[fpos]).after(rowData.join(''));
				} else {
					$("#"+$.jgrid.jqID(ts.p.id)+" tbody:first").append(rowData.join(''));
				}
			}
			if(ts.p.subGrid === true ) {
				try { $(ts).jqGrid("addSubGrid",gi+ni);} catch (_){}
			}
			ts.p.totaltime = new Date() - startReq;
			if(ir>0) {
				if(ts.p.records===0) { ts.p.records=len; }
			}
			rowData = null;
			if( ts.p.treeGrid === true) {
				try {$(ts).jqGrid("setTreeNode", fpos+1, ir+fpos+1);} catch (e) {}
			}
			if(!ts.p.treeGrid && !ts.p.scroll) {ts.grid.bDiv.scrollTop = 0;}
			ts.p.reccount=ir;
			ts.p.treeANode = -1;
			if(ts.p.userDataOnFooter) { $(ts).jqGrid("footerData","set",ts.p.userData,true); }
			if(locdata) {
				ts.p.records = len;
				ts.p.lastpage = Math.ceil(len/ rn);
			}
			if (!more) { ts.updatepager(false,true); }
			if(locdata) {
				while (ir<len && drows[ir]) {
					cur = drows[ir];
					idr = $.jgrid.getAccessor(cur,idn);
					if(idr === undefined) {
						idr = br+ir;
						if(f.length===0){
							if(dReader.cell){
								var ccur2 = $.jgrid.getAccessor(cur,dReader.cell);
								idr = ccur2[idn] || idr;
								ccur2=null;
							}
						}
					}
					if(cur) {
						if (dReader.repeatitems) {
							if(dReader.cell) {cur = $.jgrid.getAccessor(cur,dReader.cell);}
							if (!F) { F=orderedCols(gi+si+ni); }
						}

						for (j=0;j<F.length;j++) {
							v = $.jgrid.getAccessor(cur,F[j]);
							rd[ts.p.colModel[j+gi+si+ni].name] = v;
						}
						rd[locid] = idr;
						ts.p.data.push(rd);
						ts.p._index[idr] = ts.p.data.length-1;
						rd = {};
					}
					ir++;
				}
			}
		},
		addLocalData = function() {
			var st, fndsort=false, cmtypes={}, grtypes=[], grindexes=[], srcformat, sorttype, newformat;
			if(!$.isArray(ts.p.data)) {
				return;
			}
			var grpview = ts.p.grouping ? ts.p.groupingView : false;
			$.each(ts.p.colModel,function(i,v){
				sorttype = this.sorttype || "text";
				if(sorttype == "date" || sorttype == "datetime") {
					if(this.formatter && typeof(this.formatter) === 'string' && this.formatter == 'date') {
						if(this.formatoptions && this.formatoptions.srcformat) {
							srcformat = this.formatoptions.srcformat;
						} else {
							srcformat = $.jgrid.formatter.date.srcformat;
						}
						if(this.formatoptions && this.formatoptions.newformat) {
							newformat = this.formatoptions.newformat;
						} else {
							newformat = $.jgrid.formatter.date.newformat;
						}
					} else {
						srcformat = newformat = this.datefmt || "Y-m-d";
					}
					cmtypes[this.name] = {"stype": sorttype, "srcfmt": srcformat,"newfmt":newformat};
				} else {
					cmtypes[this.name] = {"stype": sorttype, "srcfmt":'',"newfmt":''};
				}
				if(ts.p.grouping && this.name == grpview.groupField[0]) {
					var grindex = this.name;
					if (typeof this.index != 'undefined') {
						grindex = this.index;
					}
					grtypes[0] = cmtypes[grindex];
					grindexes.push(grindex);
				}
				if(!fndsort && (this.index == ts.p.sortname || this.name == ts.p.sortname)){
					st = this.name; // ???
					fndsort = true;
				}
			});
			if(ts.p.treeGrid) {
				$(ts).jqGrid("SortTree", st, ts.p.sortorder, cmtypes[st].stype, cmtypes[st].srcfmt);
				return;
			}
			var compareFnMap = {
				'eq':function(queryObj, op) {return queryObj.equals;},
				'ne':function(queryObj,op) {return queryObj.notEquals;},
				'lt':function(queryObj,op) {return queryObj.less;},
				'le':function(queryObj,op) {return queryObj.lessOrEquals;},
				'gt':function(queryObj,op) {return queryObj.greater;},
				'ge':function(queryObj,op) {return queryObj.greaterOrEquals;},
				'cn':function(queryObj,op) {return queryObj.contains;},
				'nc':function(queryObj,op) {return op === "OR" ? queryObj.orNot().contains : queryObj.andNot().contains;},
				'bw':function(queryObj,op) {return queryObj.startsWith;},
				'bn':function(queryObj,op) {return op === "OR" ? queryObj.orNot().startsWith : queryObj.andNot().startsWith;},
				'en':function(queryObj,op) {return op === "OR" ? queryObj.orNot().endsWith : queryObj.andNot().endsWith;},
				'ew':function(queryObj,op) {return queryObj.endsWith;},
				'ni':function(queryObj,op) {return op === "OR" ? queryObj.orNot().equals : queryObj.andNot().equals;},
				'in':function(queryObj,op) {return queryObj.equals;},
				'nu':function(queryObj,op) {return queryObj.isNull;},
				'nn':function(queryObj,op) {return op === "OR" ? queryObj.orNot().isNull : queryObj.andNot().isNull;}

			},
			query = $.jgrid.from(ts.p.data);
			if (ts.p.ignoreCase) { query = query.ignoreCase(); }
			function tojLinq ( group ) {
				var s = 0, index, opr, rule;
				if (group.groups !== undefined) {
					for (index = 0; index < group.groups.length; index++) {
						try {
							tojLinq(group.groups[index]);
						} catch (e) {alert(e);}
						s++;
					}
				}
				if (group.rules !== undefined) {
					if(s>0) {
						var result = query.select();
						query = $.jgrid.from( result);
					}
					try{
						for (index = 0; index < group.rules.length; index++) {
							rule = group.rules[index];
							opr = group.groupOp.toString().toUpperCase();
							if (compareFnMap[rule.op] && rule.field ) {
								if(s > 0 && opr && opr === "OR") {
									query = query.or();
								}
								query = compareFnMap[rule.op](query, opr)(rule.field, rule.data, cmtypes[rule.field]);
							}
							s++;
						}
					} catch (g) {alert(g);}
				}
			}

			if (ts.p.search === true) {
				var srules = ts.p.postData.filters;
				if(srules) {
					if(typeof srules == "string") { srules = $.jgrid.parse(srules);}
					tojLinq( srules );
				} else {
					try {
						query = compareFnMap[ts.p.postData.searchOper](query)(ts.p.postData.searchField, ts.p.postData.searchString,cmtypes[ts.p.postData.searchField]);
					} catch (se){}
				}
			}
			if(ts.p.grouping) {
				query.orderBy(grindexes,grpview.groupOrder[0],grtypes[0].stype, grtypes[0].srcfmt);
				grpview.groupDataSorted = true;
			}
			if (st && ts.p.sortorder && fndsort) {
				if(ts.p.sortorder.toUpperCase() == "DESC") {
					query.orderBy(ts.p.sortname, "d", cmtypes[st].stype, cmtypes[st].srcfmt);
				} else {
					query.orderBy(ts.p.sortname, "a", cmtypes[st].stype, cmtypes[st].srcfmt);
				}
			}
			var queryResults = query.select(),
			recordsperpage = parseInt(ts.p.rowNum,10),
			total = queryResults.length,
			page = parseInt(ts.p.page,10),
			totalpages = Math.ceil(total / recordsperpage),
			retresult = {};
			queryResults = queryResults.slice( (page-1)*recordsperpage , page*recordsperpage );
			query = null;
			cmtypes = null;
			retresult[ts.p.localReader.total] = totalpages;
			retresult[ts.p.localReader.page] = page;
			retresult[ts.p.localReader.records] = total;
			retresult[ts.p.localReader.root] = queryResults;
			queryResults = null;
			return  retresult;
		},
		updatepager = function(rn, dnd) {
			var cp, last, base, from,to,tot,fmt, pgboxes = "", sppg,
			tspg = ts.p.pager ? "_"+$.jgrid.jqID(ts.p.pager.substr(1)) : "",
			tspg_t = ts.p.toppager ? "_"+ts.p.toppager.substr(1) : "";
			base = parseInt(ts.p.page,10)-1;
			if(base < 0) { base = 0; }
			base = base*parseInt(ts.p.rowNum,10);
			to = base + ts.p.reccount;
			if (ts.p.scroll) {
				var rows = $("tbody:first > tr:gt(0)", ts.grid.bDiv);
				base = to - rows.length;
				ts.p.reccount = rows.length;
				var rh = rows.outerHeight() || ts.grid.prevRowHeight;
				if (rh) {
					var top = base * rh;
					var height = parseInt(ts.p.records,10) * rh;
				    $(">div:first",ts.grid.bDiv).css({height : height}).children("div:first").css({height:top,display:top?"":"none"});
				}
				ts.grid.bDiv.scrollLeft = ts.grid.hDiv.scrollLeft;
			}
			pgboxes = ts.p.pager ? ts.p.pager : "";
			pgboxes += ts.p.toppager ?  (pgboxes ? "," + ts.p.toppager : ts.p.toppager) : "";
			if(pgboxes) {
				fmt = $.jgrid.formatter.integer || {};
				cp = intNum(ts.p.page);
				last = intNum(ts.p.lastpage);
				$(".selbox",pgboxes).attr("disabled",false);
				if(ts.p.pginput===true) {
					$('.ui-pg-input',pgboxes).val(ts.p.page);
					sppg = ts.p.toppager ? '#sp_1'+tspg+",#sp_1"+tspg_t : '#sp_1'+tspg;
					$(sppg).html($.fmatter ? $.fmatter.util.NumberFormat(ts.p.lastpage,fmt):ts.p.lastpage);

				}
				if (ts.p.viewrecords){
					if(ts.p.reccount === 0) {
						$(".ui-paging-info",pgboxes).html(ts.p.emptyrecords);
					} else {
						from = base+1;
						tot=ts.p.records;
						if($.fmatter) {
							from = $.fmatter.util.NumberFormat(from,fmt);
							to = $.fmatter.util.NumberFormat(to,fmt);
							tot = $.fmatter.util.NumberFormat(tot,fmt);
						}
						$(".ui-paging-info",pgboxes).html($.jgrid.format(ts.p.recordtext,from,to,tot));
					}
				}
				if(ts.p.pgbuttons===true) {
					if(cp<=0) {cp = last = 0;}
					if(cp==1 || cp === 0) {
						$("#first"+tspg+", #prev"+tspg).addClass('ui-state-disabled').removeClass('ui-state-hover');
						if(ts.p.toppager) { $("#first_t"+tspg_t+", #prev_t"+tspg_t).addClass('ui-state-disabled').removeClass('ui-state-hover'); }
					} else {
						$("#first"+tspg+", #prev"+tspg).removeClass('ui-state-disabled');
						if(ts.p.toppager) { $("#first_t"+tspg_t+", #prev_t"+tspg_t).removeClass('ui-state-disabled'); }
					}
					if(cp==last || cp === 0) {
						$("#next"+tspg+", #last"+tspg).addClass('ui-state-disabled').removeClass('ui-state-hover');
						if(ts.p.toppager) { $("#next_t"+tspg_t+", #last_t"+tspg_t).addClass('ui-state-disabled').removeClass('ui-state-hover'); }
					} else {
						$("#next"+tspg+", #last"+tspg).removeClass('ui-state-disabled');
						if(ts.p.toppager) { $("#next_t"+tspg_t+", #last_t"+tspg_t).removeClass('ui-state-disabled'); }
					}
				}
			}
			if(rn===true && ts.p.rownumbers === true) {
				$("td.jqgrid-rownum",ts.rows).each(function(i){
					$(this).html(base+1+i);
				});
			}
			if(dnd && ts.p.jqgdnd) { $(ts).jqGrid('gridDnD','updateDnD');}
			if($.isFunction(ts.p.gridComplete)) {ts.p.gridComplete.call(ts);}
		},
		beginReq = function() {
			ts.grid.hDiv.loading = true;
			if(ts.p.hiddengrid) { return;}
			switch(ts.p.loadui) {
				case "disable":
					break;
				case "enable":
					$("#load_"+$.jgrid.jqID(ts.p.id)).show();
					break;
				case "block":
					$("#lui_"+$.jgrid.jqID(ts.p.id)).show();
					$("#load_"+$.jgrid.jqID(ts.p.id)).show();
					break;
			}
		},
		endReq = function() {
			ts.grid.hDiv.loading = false;
			switch(ts.p.loadui) {
				case "disable":
					break;
				case "enable":
					$("#load_"+$.jgrid.jqID(ts.p.id)).hide();
					break;
				case "block":
					$("#lui_"+$.jgrid.jqID(ts.p.id)).hide();
					$("#load_"+$.jgrid.jqID(ts.p.id)).hide();
					break;
			}
		},
		populate = function (npage) {
			if(!ts.grid.hDiv.loading) {
				var pvis = ts.p.scroll && npage === false;
				var prm = {}, dt, dstr, pN=ts.p.prmNames;
				if(ts.p.page <=0) { ts.p.page = 1; }
				if(pN.search !== null) {prm[pN.search] = ts.p.search;} if(pN.nd !== null) {prm[pN.nd] = new Date().getTime();}
				if(pN.rows !== null) {prm[pN.rows]= ts.p.rowNum;} if(pN.page !== null) {prm[pN.page]= ts.p.page;}
				if(pN.sort !== null) {prm[pN.sort]= ts.p.sortname;} if(pN.order !== null) {prm[pN.order]= ts.p.sortorder;}
				if(ts.p.rowTotal !== null && pN.totalrows !== null) { prm[pN.totalrows]= ts.p.rowTotal; }
				var lc = ts.p.loadComplete;
				var lcf = $.isFunction(lc);
				if (!lcf) { lc = null; }
				var adjust = 0;
				npage = npage || 1;
				if (npage > 1) {
					if(pN.npage !== null) {
						prm[pN.npage] = npage;
						adjust = npage - 1;
						npage = 1;
					} else {
						lc = function(req) {
							ts.p.page++;
							ts.grid.hDiv.loading = false;
							if (lcf) {
								ts.p.loadComplete.call(ts,req);
							}
							populate(npage-1);
						};
					}
				} else if (pN.npage !== null) {
					delete ts.p.postData[pN.npage];
				}
				if(ts.p.grouping) {
					$(ts).jqGrid('groupingSetup');
					if(ts.p.groupingView.groupDataSorted === true) {
						prm[pN.sort] = ts.p.groupingView.groupField[0] +" "+ ts.p.groupingView.groupOrder[0]+", "+prm[pN.sort];
					}
				}
				$.extend(ts.p.postData,prm);
				var rcnt = !ts.p.scroll ? 1 : ts.rows.length-1;
				if ($.isFunction(ts.p.datatype)) { ts.p.datatype.call(ts,ts.p.postData,"load_"+ts.p.id); return;}
				else if($.isFunction(ts.p.beforeRequest)) {ts.p.beforeRequest.call(ts);}
				dt = ts.p.datatype.toLowerCase();
				switch(dt)
				{
				case "json":
				case "jsonp":
				case "xml":
				case "script":
					$.ajax($.extend({
						url:ts.p.url,
						type:ts.p.mtype,
						dataType: dt ,
						data: $.isFunction(ts.p.serializeGridData)? ts.p.serializeGridData.call(ts,ts.p.postData) : ts.p.postData,
						success:function(data,st) {
							if(dt === "xml") { addXmlData(data,ts.grid.bDiv,rcnt,npage>1,adjust); }
							else { addJSONData(data,ts.grid.bDiv,rcnt,npage>1,adjust); }
							if(lc) { lc.call(ts,data); }
							if (pvis) { ts.grid.populateVisible(); }
							if( ts.p.loadonce || ts.p.treeGrid) {ts.p.datatype = "local";}
							data=null;
							endReq();
						},
						error:function(xhr,st,err){
							if($.isFunction(ts.p.loadError)) { ts.p.loadError.call(ts,xhr,st,err); }
							endReq();
							xhr=null;
						},
						beforeSend: function(xhr){
							beginReq();
							if($.isFunction(ts.p.loadBeforeSend)) { ts.p.loadBeforeSend.call(ts,xhr); }
						}
					},$.jgrid.ajaxOptions, ts.p.ajaxGridOptions));
				break;
				case "xmlstring":
					beginReq();
					dstr = $.jgrid.stringToDoc(ts.p.datastr);
					addXmlData(dstr,ts.grid.bDiv);
					if(lcf) {ts.p.loadComplete.call(ts,dstr);}
					ts.p.datatype = "local";
					ts.p.datastr = null;
					endReq();
				break;
				case "jsonstring":
					beginReq();
					if(typeof ts.p.datastr == 'string') { dstr = $.jgrid.parse(ts.p.datastr); }
					else { dstr = ts.p.datastr; }
					addJSONData(dstr,ts.grid.bDiv);
					if(lcf) {ts.p.loadComplete.call(ts,dstr);}
					ts.p.datatype = "local";
					ts.p.datastr = null;
					endReq();
				break;
				case "local":
				case "clientside":
					beginReq();
					ts.p.datatype = "local";
					var req = addLocalData();
					addJSONData(req,ts.grid.bDiv,rcnt,npage>1,adjust);
					if(lc) { lc.call(ts,req); }
					if (pvis) { ts.grid.populateVisible(); }
					endReq();
				break;
				}
			}
		},
        setPager = function (pgid, tp){
            // TBD - consider escaping pgid with pgid = $.jgrid.jqID(pgid);
			var sep = "<td class='ui-pg-button ui-state-disabled' style='width:4px;'><span class='ui-separator'></span></td>",
			pginp = "",
			pgl="<table cellspacing='0' cellpadding='0' border='0' style='table-layout:auto;' class='ui-pg-table'><tbody><tr>",
			str="", pgcnt, lft, cent, rgt, twd, tdw, i,
			clearVals = function(onpaging){
				var ret;
				if ($.isFunction(ts.p.onPaging) ) { ret = ts.p.onPaging.call(ts,onpaging); }
				ts.p.selrow = null;
				if(ts.p.multiselect) {ts.p.selarrrow =[];$('#cb_'+$.jgrid.jqID(ts.p.id),ts.grid.hDiv).attr("checked",false);}
				ts.p.savedRow = [];
				if(ret=='stop') {return false;}
				return true;
			};
			pgid = pgid.substr(1);
			tp += "_" + pgid;
			pgcnt = "pg_"+pgid;
			lft = pgid+"_left"; cent = pgid+"_center"; rgt = pgid+"_right";
			$("#"+$.jgrid.jqID(pgid) )
			.append("<div id='"+pgcnt+"' class='ui-pager-control' role='group'><table cellspacing='0' cellpadding='0' border='0' class='ui-pg-table' style='width:100%;table-layout:fixed;height:100%;' role='row'><tbody><tr><td id='"+lft+"' align='left'></td><td id='"+cent+"' align='center' style='white-space:pre;'></td><td id='"+rgt+"' align='right'></td></tr></tbody></table></div>")
			.attr("dir","ltr"); //explicit setting
			if(ts.p.rowList.length >0){
				str = "<td dir='"+dir+"'>";
				str +="<select class='ui-pg-selbox' role='listbox'>";
				for(i=0;i<ts.p.rowList.length;i++){
					str +="<option role=\"option\" value=\""+ts.p.rowList[i]+"\""+((ts.p.rowNum == ts.p.rowList[i])?" selected=\"selected\"":"")+">"+ts.p.rowList[i]+"</option>";
				}
				str +="</select></td>";
			}
			if(dir=="rtl") { pgl += str; }
			if(ts.p.pginput===true) { pginp= "<td dir='"+dir+"'>"+$.jgrid.format(ts.p.pgtext || "","<input class='ui-pg-input' type='text' size='2' maxlength='7' value='0' role='textbox'/>","<span id='sp_1_"+$.jgrid.jqID(pgid)+"'></span>")+"</td>";}
			if(ts.p.pgbuttons===true) {
				var po=["first"+tp,"prev"+tp, "next"+tp,"last"+tp]; if(dir=="rtl") { po.reverse(); }
				pgl += "<td id='"+po[0]+"' class='ui-pg-button ui-corner-all'><span class='ui-icon ui-icon-seek-first'></span></td>";
				pgl += "<td id='"+po[1]+"' class='ui-pg-button ui-corner-all'><span class='ui-icon ui-icon-seek-prev'></span></td>";
				pgl += pginp !== "" ? sep+pginp+sep:"";
				pgl += "<td id='"+po[2]+"' class='ui-pg-button ui-corner-all'><span class='ui-icon ui-icon-seek-next'></span></td>";
				pgl += "<td id='"+po[3]+"' class='ui-pg-button ui-corner-all'><span class='ui-icon ui-icon-seek-end'></span></td>";
			} else if (pginp !== "") { pgl += pginp; }
			if(dir=="ltr") { pgl += str; }
			pgl += "</tr></tbody></table>";
			if(ts.p.viewrecords===true) {$("td#"+pgid+"_"+ts.p.recordpos,"#"+pgcnt).append("<div dir='"+dir+"' style='text-align:"+ts.p.recordpos+"' class='ui-paging-info'></div>");}
			$("td#"+pgid+"_"+ts.p.pagerpos,"#"+pgcnt).append(pgl);
			tdw = $(".ui-jqgrid").css("font-size") || "11px";
			$(document.body).append("<div id='testpg' class='ui-jqgrid ui-widget ui-widget-content' style='font-size:"+tdw+";visibility:hidden;' ></div>");
			twd = $(pgl).clone().appendTo("#testpg").width();
			$("#testpg").remove();
			if(twd > 0) {
				if(pginp !== "") { twd += 50; } //should be param
				$("td#"+pgid+"_"+ts.p.pagerpos,"#"+pgcnt).width(twd);
			}
			ts.p._nvtd = [];
			ts.p._nvtd[0] = twd ? Math.floor((ts.p.width - twd)/2) : Math.floor(ts.p.width/3);
			ts.p._nvtd[1] = 0;
			pgl=null;
			$('.ui-pg-selbox',"#"+pgcnt).bind('change',function() {
				ts.p.page = Math.round(ts.p.rowNum*(ts.p.page-1)/this.value-0.5)+1;
				ts.p.rowNum = this.value;
				if(tp) { $('.ui-pg-selbox',ts.p.pager).val(this.value); }
				else if(ts.p.toppager) { $('.ui-pg-selbox',ts.p.toppager).val(this.value); }
				if(!clearVals('records')) { return false; }
				populate();
				return false;
			});
			if(ts.p.pgbuttons===true) {
			$(".ui-pg-button","#"+pgcnt).hover(function(e){
				if($(this).hasClass('ui-state-disabled')) {
					this.style.cursor='default';
				} else {
					$(this).addClass('ui-state-hover');
					this.style.cursor='pointer';
				}
			},function(e) {
				if($(this).hasClass('ui-state-disabled')) {
				} else {
					$(this).removeClass('ui-state-hover');
					this.style.cursor= "default";
				}
			});
			$("#first"+$.jgrid.jqID(tp)+", #prev"+$.jgrid.jqID(tp)+", #next"+$.jgrid.jqID(tp)+", #last"+$.jgrid.jqID(tp)).click( function(e) {
				var cp = intNum(ts.p.page,1),
				last = intNum(ts.p.lastpage,1), selclick = false,
				fp=true, pp=true, np=true,lp=true;
				if(last ===0 || last===1) {fp=false;pp=false;np=false;lp=false; }
				else if( last>1 && cp >=1) {
					if( cp === 1) { fp=false; pp=false; }
					else if( cp>1 && cp <last){ }
					else if( cp===last){ np=false;lp=false; }
				} else if( last>1 && cp===0 ) { np=false;lp=false; cp=last-1;}
				if( this.id === 'first'+tp && fp ) { ts.p.page=1; selclick=true;}
				if( this.id === 'prev'+tp && pp) { ts.p.page=(cp-1); selclick=true;}
				if( this.id === 'next'+tp && np) { ts.p.page=(cp+1); selclick=true;}
				if( this.id === 'last'+tp && lp) { ts.p.page=last; selclick=true;}
				if(selclick) {
					if(!clearVals(this.id)) { return false; }
					populate();
				}
				return false;
			});
			}
			if(ts.p.pginput===true) {
			$('input.ui-pg-input',"#"+pgcnt).keypress( function(e) {
				var key = e.charCode ? e.charCode : e.keyCode ? e.keyCode : 0;
				if(key == 13) {
					ts.p.page = ($(this).val()>0) ? $(this).val():ts.p.page;
					if(!clearVals('user')) { return false; }
					populate();
					return false;
				}
				return this;
			});
			}
		},
		sortData = function (index, idxcol,reload,sor){
			if(!ts.p.colModel[idxcol].sortable) { return; }
			var so;
			if(ts.p.savedRow.length > 0) {return;}
			if(!reload) {
				if( ts.p.lastsort == idxcol ) {
					if( ts.p.sortorder == 'asc') {
						ts.p.sortorder = 'desc';
					} else if(ts.p.sortorder == 'desc') { ts.p.sortorder = 'asc';}
				} else { ts.p.sortorder = ts.p.colModel[idxcol].firstsortorder || 'asc'; }
				ts.p.page = 1;
			}
			if(sor) {
				if(ts.p.lastsort == idxcol && ts.p.sortorder == sor && !reload) { return; }
				else { ts.p.sortorder = sor; }
			}
			var thd= $("thead:first",ts.grid.hDiv).get(0);
			$("tr th:eq("+ts.p.lastsort+") span.ui-grid-ico-sort",thd).addClass('ui-state-disabled');
			$("tr th:eq("+ts.p.lastsort+")",thd).attr("aria-selected","false");
			$("tr th:eq("+idxcol+") span.ui-icon-"+ts.p.sortorder,thd).removeClass('ui-state-disabled');
			$("tr th:eq("+idxcol+")",thd).attr("aria-selected","true");
			if(!ts.p.viewsortcols[0]) {
				if(ts.p.lastsort != idxcol) {
					$("tr th:eq("+ts.p.lastsort+") span.s-ico",thd).hide();
					$("tr th:eq("+idxcol+") span.s-ico",thd).show();
				}
			}
			index = index.substring(5);
			ts.p.sortname = ts.p.colModel[idxcol].index || index;
			so = ts.p.sortorder;
			if($.isFunction(ts.p.onSortCol)) {if (ts.p.onSortCol.call(ts,index,idxcol,so)=='stop') {ts.p.lastsort = idxcol; return;}}
			if(ts.p.datatype == "local") {
				if(ts.p.deselectAfterSort) {$(ts).jqGrid("resetSelection");}
			} else {
				ts.p.selrow = null;
				if(ts.p.multiselect){$("#cb_"+$.jgrid.jqID(ts.p.id),ts.grid.hDiv).attr("checked",false);}
				ts.p.selarrrow =[];
				ts.p.savedRow =[];
			}
			if(ts.p.scroll) {
				var sscroll = ts.grid.bDiv.scrollLeft;
				emptyRows(ts.grid.bDiv,true, false);
				ts.grid.hDiv.scrollLeft = sscroll;
			}
			if(ts.p.subGrid && ts.p.datatype=='local') {
				$("td.sgexpanded","#"+$.jgrid.jqID(ts.p.id)).each(function(){
					$(this).trigger("click");
				});
			}
			populate();
			ts.p.lastsort = idxcol;
			if(ts.p.sortname != index && idxcol) {ts.p.lastsort = idxcol;}
		},
		setColWidth = function () {
			var initwidth = 0, brd=isSafari? 0: ts.p.cellLayout, vc=0, lvc, scw=ts.p.scrollOffset,cw,hs=false,aw,gw=0,
			cl = 0, cr;
			$.each(ts.p.colModel, function(i) {
				if(typeof this.hidden === 'undefined') {this.hidden=false;}
				this.widthOrg = cw = intNum(this.width,0);
				if(this.hidden===false){
					initwidth += cw+brd;
					if(this.fixed) {
						gw += cw+brd;
					} else {
						vc++;
					}
					cl++;
				}
			});
			if(isNaN(ts.p.width)) {ts.p.width = grid.width = initwidth;}
			else { grid.width = ts.p.width;}
			ts.p.tblwidth = initwidth;
			if(ts.p.shrinkToFit ===false && ts.p.forceFit === true) {ts.p.forceFit=false;}
			if(ts.p.shrinkToFit===true && vc > 0) {
				aw = grid.width-brd*vc-gw;
				if(isNaN(ts.p.height)) {
				} else {
					aw -= scw;
					hs = true;
				}
				initwidth =0;
				$.each(ts.p.colModel, function(i) {
					if(this.hidden === false && !this.fixed){
						cw = Math.round(aw*this.width/(ts.p.tblwidth-brd*vc-gw));
						this.width =cw;
						initwidth += cw;
						lvc = i;
					}
				});
				cr =0;
				if (hs) {
					if(grid.width-gw-(initwidth+brd*vc) !== scw){
						cr = grid.width-gw-(initwidth+brd*vc)-scw;
					}
				} else if(!hs && Math.abs(grid.width-gw-(initwidth+brd*vc)) !== 1) {
					cr = grid.width-gw-(initwidth+brd*vc);
				}
				ts.p.colModel[lvc].width += cr;
				ts.p.tblwidth = initwidth+cr+brd*vc+gw;
				if(ts.p.tblwidth > ts.p.width) {
					ts.p.colModel[lvc].width -= (ts.p.tblwidth - parseInt(ts.p.width,10));
					ts.p.tblwidth = ts.p.width;
				}
			}
		},
		nextVisible= function(iCol) {
			var ret = iCol, j=iCol, i;
			for (i = iCol+1;i<ts.p.colModel.length;i++){
				if(ts.p.colModel[i].hidden !== true ) {
					j=i; break;
				}
			}
			return j-ret;
		},
		getOffset = function (iCol) {
			var i, ret = {}, brd1 = isSafari ? 0 : ts.p.cellLayout;
			ret[0] =  ret[1] = ret[2] = 0;
			for(i=0;i<=iCol;i++){
				if(ts.p.colModel[i].hidden === false ) {
					ret[0] += ts.p.colModel[i].width+brd1;
				}
			}
			if(ts.p.direction=="rtl") { ret[0] = ts.p.width - ret[0]; }
			ret[0] = ret[0] - ts.grid.bDiv.scrollLeft;
			if($(ts.grid.cDiv).is(":visible")) {ret[1] += $(ts.grid.cDiv).height() +parseInt($(ts.grid.cDiv).css("padding-top"),10)+parseInt($(ts.grid.cDiv).css("padding-bottom"),10);}
			if(ts.p.toolbar[0]===true && (ts.p.toolbar[1]=='top' || ts.p.toolbar[1]=='both')) {ret[1] += $(ts.grid.uDiv).height()+parseInt($(ts.grid.uDiv).css("border-top-width"),10)+parseInt($(ts.grid.uDiv).css("border-bottom-width"),10);}
			if(ts.p.toppager) {ret[1] += $(ts.grid.topDiv).height()+parseInt($(ts.grid.topDiv).css("border-bottom-width"),10);}
			ret[2] += $(ts.grid.bDiv).height() + $(ts.grid.hDiv).height();
			return ret;
		};
		this.p.id = this.id;
		if ($.inArray(ts.p.multikey,sortkeys) == -1 ) {ts.p.multikey = false;}
		ts.p.keyIndex=false;
		for (i=0; i<ts.p.colModel.length;i++) {
			ts.p.colModel[i] = $.extend(true, {}, ts.p.cmTemplate, ts.p.colModel[i].template || {}, ts.p.colModel[i]);
			if (ts.p.keyIndex === false && ts.p.colModel[i].key===true) {
				ts.p.keyIndex = i;
			}
		}
		ts.p.sortorder = ts.p.sortorder.toLowerCase();
		if(ts.p.grouping===true) {
			ts.p.scroll = false;
			ts.p.rownumbers = false;
			ts.p.subGrid = false;
			ts.p.treeGrid = false;
			ts.p.gridview = true;
		}
		if(this.p.treeGrid === true) {
			try { $(this).jqGrid("setTreeGrid");} catch (_) {}
			if(ts.p.datatype != "local") { ts.p.localReader = {id: "_id_"};	}
		}
		if(this.p.subGrid) {
			try { $(ts).jqGrid("setSubGrid");} catch (s){}
		}
		if(this.p.multiselect) {
			this.p.colNames.unshift("<input role='checkbox' id='cb_"+this.p.id+"' class='cbox' type='checkbox'/>");
			this.p.colModel.unshift({name:'cb',width:isSafari ? ts.p.multiselectWidth+ts.p.cellLayout : ts.p.multiselectWidth,sortable:false,resizable:false,hidedlg:true,search:false,align:'center',fixed:true});
		}
		if(this.p.rownumbers) {
			this.p.colNames.unshift("");
			this.p.colModel.unshift({name:'rn',width:ts.p.rownumWidth,sortable:false,resizable:false,hidedlg:true,search:false,align:'center',fixed:true});
		}
		ts.p.xmlReader = $.extend(true,{
			root: "rows",
			row: "row",
			page: "rows>page",
			total: "rows>total",
			records : "rows>records",
			repeatitems: true,
			cell: "cell",
			id: "[id]",
			userdata: "userdata",
			subgrid: {root:"rows", row: "row", repeatitems: true, cell:"cell"}
		}, ts.p.xmlReader);
		ts.p.jsonReader = $.extend(true,{
			root: "rows",
			page: "page",
			total: "total",
			records: "records",
			repeatitems: true,
			cell: "cell",
			id: "id",
			userdata: "userdata",
			subgrid: {root:"rows", repeatitems: true, cell:"cell"}
		},ts.p.jsonReader);
		ts.p.localReader = $.extend(true,{
			root: "rows",
			page: "page",
			total: "total",
			records: "records",
			repeatitems: false,
			cell: "cell",
			id: "id",
			userdata: "userdata",
			subgrid: {root:"rows", repeatitems: true, cell:"cell"}
		},ts.p.localReader);
		if(ts.p.scroll){
			ts.p.pgbuttons = false; ts.p.pginput=false; ts.p.rowList=[];
		}
		if(ts.p.data.length) { refreshIndex(); }
		var thead = "<thead><tr class='ui-jqgrid-labels' role='rowheader'>",
		tdc, idn, w, res, sort,
		td, ptr, tbody, imgs,iac="",idc="";
		if(ts.p.shrinkToFit===true && ts.p.forceFit===true) {
			for (i=ts.p.colModel.length-1;i>=0;i--){
				if(!ts.p.colModel[i].hidden) {
					ts.p.colModel[i].resizable=false;
					break;
				}
			}
		}
		if(ts.p.viewsortcols[1] == 'horizontal') {iac=" ui-i-asc";idc=" ui-i-desc";}
		tdc = isMSIE ?  "class='ui-th-div-ie'" :"";
		imgs = "<span class='s-ico' style='display:none'><span sort='asc' class='ui-grid-ico-sort ui-icon-asc"+iac+" ui-state-disabled ui-icon ui-icon-triangle-1-n ui-sort-"+dir+"'></span>";
		imgs += "<span sort='desc' class='ui-grid-ico-sort ui-icon-desc"+idc+" ui-state-disabled ui-icon ui-icon-triangle-1-s ui-sort-"+dir+"'></span></span>";
		for(i=0;i<this.p.colNames.length;i++){
			var tooltip = ts.p.headertitles ? (" title=\""+$.jgrid.stripHtml(ts.p.colNames[i])+"\"") :"";
			thead += "<th id='"+ts.p.id+"_"+ts.p.colModel[i].name+"' role='columnheader' class='ui-state-default ui-th-column ui-th-"+dir+"'"+ tooltip+">";
			idn = ts.p.colModel[i].index || ts.p.colModel[i].name;
			thead += "<div id='jqgh_"+ts.p.id+"_"+ts.p.colModel[i].name+"' "+tdc+">"+ts.p.colNames[i];
			if(!ts.p.colModel[i].width)  { ts.p.colModel[i].width = 150; }
			else { ts.p.colModel[i].width = parseInt(ts.p.colModel[i].width,10); }
			if(typeof(ts.p.colModel[i].title) !== "boolean") { ts.p.colModel[i].title = true; }
			if (idn == ts.p.sortname) {
				ts.p.lastsort = i;
			}
			thead += imgs+"</div></th>";
		}
		thead += "</tr></thead>";
		imgs = null;
		$(this).append(thead);
		$("thead tr:first th",this).hover(function(){$(this).addClass('ui-state-hover');},function(){$(this).removeClass('ui-state-hover');});
		if(this.p.multiselect) {
			var emp=[], chk;
			$('#cb_'+$.jgrid.jqID(ts.p.id),this).bind('click',function(){
				ts.p.selarrrow = [];
				if (this.checked) {
					$(ts.rows).each(function(i) {
						if ( i>0 ) {
							if(!$(this).hasClass("subgrid") && !$(this).hasClass("jqgroup") && !$(this).hasClass('ui-state-disabled')){
								$("#jqg_"+$.jgrid.jqID(ts.p.id)+"_"+$.jgrid.jqID(this.id) ).attr("checked","checked");
								$(this).addClass("ui-state-highlight").attr("aria-selected","true");
								ts.p.selarrrow.push(this.id);
								ts.p.selrow = this.id;
							}
						}
					});
					chk=true;
					emp=[];
				}
				else {
					$(ts.rows).each(function(i) {
						if(i>0) {
							if(!$(this).hasClass("subgrid") && !$(this).hasClass('ui-state-disabled')){
								$("#jqg_"+$.jgrid.jqID(ts.p.id)+"_"+$.jgrid.jqID(this.id) ).removeAttr("checked");
								$(this).removeClass("ui-state-highlight").attr("aria-selected","false");
								emp.push(this.id);
							}
						}
					});
					ts.p.selrow = null;
					chk=false;
				}
				if($.isFunction(ts.p.onSelectAll)) {ts.p.onSelectAll.call(ts, chk ? ts.p.selarrrow : emp,chk);}
			});
		}

		if(ts.p.autowidth===true) {
			var pw = $(eg).innerWidth();
			ts.p.width = pw > 0?  pw: 'nw';
		}
		setColWidth();
		$(eg).css("width",grid.width+"px").append("<div class='ui-jqgrid-resize-mark' id='rs_m"+ts.p.id+"'>&#160;</div>");
		$(gv).css("width",grid.width+"px");
		thead = $("thead:first",ts).get(0);
		var	tfoot = "";
		if(ts.p.footerrow) { tfoot += "<table role='grid' style='width:"+ts.p.tblwidth+"px' class='ui-jqgrid-ftable' cellspacing='0' cellpadding='0' border='0'><tbody><tr role='row' class='ui-widget-content footrow footrow-"+dir+"'>"; }
		var thr = $("tr:first",thead),
		firstr = "<tr class='jqgfirstrow' role='row' style='height:auto'>";
		ts.p.disableClick=false;
		$("th",thr).each(function ( j ) {
			w = ts.p.colModel[j].width;
			if(typeof ts.p.colModel[j].resizable === 'undefined') {ts.p.colModel[j].resizable = true;}
			if(ts.p.colModel[j].resizable){
				res = document.createElement("span");
				$(res).html("&#160;").addClass('ui-jqgrid-resize ui-jqgrid-resize-'+dir);
				if(!$.browser.opera) { $(res).css("cursor","col-resize"); }
				$(this).addClass(ts.p.resizeclass);
			} else {
				res = "";
			}
			$(this).css("width",w+"px").prepend(res);
			var hdcol = "";
			if( ts.p.colModel[j].hidden ) {
				$(this).css("display","none");
				hdcol = "display:none;";
			}
			firstr += "<td role='gridcell' style='height:0px;width:"+w+"px;"+hdcol+"'></td>";
			grid.headers[j] = { width: w, el: this };
			sort = ts.p.colModel[j].sortable;
			if( typeof sort !== 'boolean') {ts.p.colModel[j].sortable =  true; sort=true;}
			var nm = ts.p.colModel[j].name;
			if( !(nm == 'cb' || nm=='subgrid' || nm=='rn') ) {
				if(ts.p.viewsortcols[2]){
					$("div",this).addClass('ui-jqgrid-sortable');
				}
			}
			if(sort) {
				if(ts.p.viewsortcols[0]) {$("div span.s-ico",this).show(); if(j==ts.p.lastsort){ $("div span.ui-icon-"+ts.p.sortorder,this).removeClass("ui-state-disabled");}}
				else if( j == ts.p.lastsort) {$("div span.s-ico",this).show();$("div span.ui-icon-"+ts.p.sortorder,this).removeClass("ui-state-disabled");}
			}
			if(ts.p.footerrow) { tfoot += "<td role='gridcell' "+formatCol(j,0,'', null, '', false)+">&#160;</td>"; }
		}).mousedown(function(e) {
			if ($(e.target).closest("th>span.ui-jqgrid-resize").length != 1) { return; }
			var ci = $.jgrid.getCellIndex(this);
			if(ts.p.forceFit===true) {ts.p.nv= nextVisible(ci);}
			grid.dragStart(ci, e, getOffset(ci));
			return false;
		}).click(function(e) {
			if (ts.p.disableClick) {
				ts.p.disableClick = false;
				return false;
			}
			var s = "th>div.ui-jqgrid-sortable",r,d;
			if (!ts.p.viewsortcols[2]) { s = "th>div>span>span.ui-grid-ico-sort"; }
			var t = $(e.target).closest(s);
			if (t.length != 1) { return; }
			var ci = $.jgrid.getCellIndex(this);
			if (!ts.p.viewsortcols[2]) { r=true;d=t.attr("sort"); }
			sortData( $('div',this)[0].id, ci, r, d);
			return false;
		});
		if (ts.p.sortable && $.fn.sortable) {
			try {
				$(ts).jqGrid("sortableColumns", thr);
			} catch (e){}
		}
		if(ts.p.footerrow) { tfoot += "</tr></tbody></table>"; }
		firstr += "</tr>";
		tbody = document.createElement("tbody");
		this.appendChild(tbody);
		$(this).addClass('ui-jqgrid-btable').append(firstr);
		firstr = null;
		var hTable = $("<table class='ui-jqgrid-htable' style='width:"+ts.p.tblwidth+"px' role='grid' aria-labelledby='gbox_"+this.id+"' cellspacing='0' cellpadding='0' border='0'></table>").append(thead),
		hg = (ts.p.caption && ts.p.hiddengrid===true) ? true : false,
		hb = $("<div class='ui-jqgrid-hbox" + (dir=="rtl" ? "-rtl" : "" )+"'></div>");
		thead = null;
		grid.hDiv = document.createElement("div");
		$(grid.hDiv)
			.css({ width: grid.width+"px"})
			.addClass("ui-state-default ui-jqgrid-hdiv")
			.append(hb);
		$(hb).append(hTable);
		hTable = null;
		if(hg) { $(grid.hDiv).hide(); }
        if(ts.p.pager){
            // TBD -- escape ts.p.pager here?
			if(typeof ts.p.pager == "string") {if(ts.p.pager.substr(0,1) !="#") { ts.p.pager = "#"+ts.p.pager;} }
			else { ts.p.pager = "#"+ $(ts.p.pager).attr("id");}
			$(ts.p.pager).css({width: grid.width+"px"}).appendTo(eg).addClass('ui-state-default ui-jqgrid-pager ui-corner-bottom');
			if(hg) {$(ts.p.pager).hide();}
			setPager(ts.p.pager,'');
		}
		if( ts.p.cellEdit === false && ts.p.hoverrows === true) {
		$(ts).bind('mouseover',function(e) {
			ptr = $(e.target).closest("tr.jqgrow");
			if($(ptr).attr("class") !== "subgrid") {
				$(ptr).addClass("ui-state-hover");
			}
			return false;
		}).bind('mouseout',function(e) {
			ptr = $(e.target).closest("tr.jqgrow");
			$(ptr).removeClass("ui-state-hover");
			return false;
		});
		}
		var ri,ci;
		$(ts).before(grid.hDiv).click(function(e) {
			td = e.target;
			ptr = $(td,ts.rows).closest("tr.jqgrow");
			if($(ptr).length === 0 || ptr[0].className.indexOf( 'ui-state-disabled' ) > -1 ) {
				return this;
			}
			var scb = $(td).hasClass("cbox"),
			cSel = true;
			if($.isFunction(ts.p.beforeSelectRow)) { cSel = ts.p.beforeSelectRow.call(ts,ptr[0].id, e); }
			if (td.tagName == 'A' || ((td.tagName == 'INPUT' || td.tagName == 'TEXTAREA' || td.tagName == 'OPTION' || td.tagName == 'SELECT' ) && !scb) ) { return this; }
			if(cSel === true) {
				if(ts.p.cellEdit === true) {
					if(ts.p.multiselect && scb){
						$(ts).jqGrid("setSelection",ptr[0].id,true);
					} else {
						ri = ptr[0].rowIndex;
						ci = $.jgrid.getCellIndex(td);
						try {$(ts).jqGrid("editCell",ri,ci,true);} catch (_) {}
					}
				} else if ( !ts.p.multikey ) {
					if(ts.p.multiselect && ts.p.multiboxonly) {
						if(scb){$(ts).jqGrid("setSelection",ptr[0].id,true);}
						else {
							$(ts.p.selarrrow).each(function(i,n){
								var ind = ts.rows.namedItem(n);
								$(ind).removeClass("ui-state-highlight");
								$("#jqg_"+$.jgrid.jqID(ts.p.id)+"_"+$.jgrid.jqID(n)).attr("checked",false);
							});
							ts.p.selarrrow = [];
							$("#cb_"+$.jgrid.jqID(ts.p.id),ts.grid.hDiv).attr("checked",false);
							$(ts).jqGrid("setSelection",ptr[0].id,true);
						}
					} else {
						$(ts).jqGrid("setSelection",ptr[0].id,true);
					}
				} else {
					if(e[ts.p.multikey]) {
						$(ts).jqGrid("setSelection",ptr[0].id,true);
					} else if(ts.p.multiselect && scb) {
						scb = $("#jqg_"+$.jgrid.jqID(ts.p.id)+"_"+ptr[0].id).attr("checked");
						$("#jqg_"+$.jgrid.jqID(ts.p.id)+"_"+ptr[0].id).attr("checked",!scb);
					}
				}
				if($.isFunction(ts.p.onCellSelect)) {
					ri = ptr[0].id;
					ci = $.jgrid.getCellIndex(td);
					ts.p.onCellSelect.call(ts,ri,ci,$(td).html(),e);
				}
				//e.stopPropagation();
			}
			//else {
				return this;
			//}
		}).bind('reloadGrid', function(e,opts) {
			if(ts.p.treeGrid ===true) {	ts.p.datatype = ts.p.treedatatype;}
			if (opts && opts.current) {
				ts.grid.selectionPreserver(ts);
			}
			if(ts.p.datatype=="local"){ $(ts).jqGrid("resetSelection");  if(ts.p.data.length) { refreshIndex();} }
			else if(!ts.p.treeGrid) {
				ts.p.selrow=null;
				if(ts.p.multiselect) {ts.p.selarrrow =[];$('#cb_'+$.jgrid.jqID(ts.p.id),ts.grid.hDiv).attr("checked",false);}
				ts.p.savedRow = [];
			}
			if(ts.p.scroll) {emptyRows(ts.grid.bDiv,true, false);}
			if (opts && opts.page) {
				var page = opts.page;
				if (page > ts.p.lastpage) { page = ts.p.lastpage; }
				if (page < 1) { page = 1; }
				ts.p.page = page;
				if (ts.grid.prevRowHeight) {
					ts.grid.bDiv.scrollTop = (page - 1) * ts.grid.prevRowHeight * ts.p.rowNum;
				} else {
					ts.grid.bDiv.scrollTop = 0;
				}
			}
			if (ts.grid.prevRowHeight && ts.p.scroll) {
                delete ts.p.lastpage;
				ts.grid.populateVisible();
			} else {
				ts.grid.populate();
			}
			return false;
		});
		if( $.isFunction(this.p.ondblClickRow) ) {
			$(this).dblclick(function(e) {
				td = e.target;
				ptr = $(td,ts.rows).closest("tr.jqgrow");
				if($(ptr).length === 0 ){return false;}
				ri = ptr[0].rowIndex;
				ci = $.jgrid.getCellIndex(td);
				ts.p.ondblClickRow.call(ts,$(ptr).attr("id"),ri,ci, e);
				return false;
			});
		}
		if ($.isFunction(this.p.onRightClickRow)) {
			$(this).bind('contextmenu', function(e) {
				td = e.target;
				ptr = $(td,ts.rows).closest("tr.jqgrow");
				if($(ptr).length === 0 ){return false;}
				if(!ts.p.multiselect) {	$(ts).jqGrid("setSelection",ptr[0].id,true);	}
				ri = ptr[0].rowIndex;
				ci = $.jgrid.getCellIndex(td);
				ts.p.onRightClickRow.call(ts,$(ptr).attr("id"),ri,ci, e);
				return false;
			});
		}
		grid.bDiv = document.createElement("div");
		if(isMSIE) { if(String(ts.p.height).toLowerCase() === "auto") { ts.p.height = "100%"; } }
		$(grid.bDiv)
			.append($('<div style="position:relative;'+(isMSIE && $.browser.version < 8 ? "height:0.01%;" : "")+'"></div>').append('<div></div>').append(this))
			.addClass("ui-jqgrid-bdiv")
			.css({ height: ts.p.height+(isNaN(ts.p.height)?"":"px"), width: (grid.width)+"px"})
			.scroll(grid.scrollGrid);
		$("table:first",grid.bDiv).css({width:ts.p.tblwidth+"px"});
		if( isMSIE ) {
			if( $("tbody",this).size() == 2 ) { $("tbody:gt(0)",this).remove();}
			if( ts.p.multikey) {$(grid.bDiv).bind("selectstart",function(){return false;});}
		} else {
			if( ts.p.multikey) {$(grid.bDiv).bind("mousedown",function(){return false;});}
		}
		if(hg) {$(grid.bDiv).hide();}
		grid.cDiv = document.createElement("div");
		var arf = ts.p.hidegrid===true ? $("<a role='link' href='javascript:void(0)'/>").addClass('ui-jqgrid-titlebar-close HeaderButton').hover(
			function(){ arf.addClass('ui-state-hover');},
			function() {arf.removeClass('ui-state-hover');})
		.append("<span class='ui-icon ui-icon-circle-triangle-n'></span>").css((dir=="rtl"?"left":"right"),"0px") : "";
		$(grid.cDiv).append(arf).append("<span class='ui-jqgrid-title"+(dir=="rtl" ? "-rtl" :"" )+"'>"+ts.p.caption+"</span>")
		.addClass("ui-jqgrid-titlebar ui-widget-header ui-corner-top ui-helper-clearfix");
		$(grid.cDiv).insertBefore(grid.hDiv);
		if( ts.p.toolbar[0] ) {
			grid.uDiv = document.createElement("div");
			if(ts.p.toolbar[1] == "top") {$(grid.uDiv).insertBefore(grid.hDiv);}
			else if (ts.p.toolbar[1]=="bottom" ) {$(grid.uDiv).insertAfter(grid.hDiv);}
			if(ts.p.toolbar[1]=="both") {
				grid.ubDiv = document.createElement("div");
				$(grid.uDiv).insertBefore(grid.hDiv).addClass("ui-userdata ui-state-default").attr("id","t_"+this.id);
				$(grid.ubDiv).insertAfter(grid.hDiv).addClass("ui-userdata ui-state-default").attr("id","tb_"+this.id);
				if(hg)  {$(grid.ubDiv).hide();}
			} else {
				$(grid.uDiv).width(grid.width).addClass("ui-userdata ui-state-default").attr("id","t_"+this.id);
			}
			if(hg) {$(grid.uDiv).hide();}
		}
		if(ts.p.toppager) {
			ts.p.toppager = $.jgrid.jqID(ts.p.id)+"_toppager";
			grid.topDiv = $("<div id='"+ts.p.toppager+"'></div>")[0];
			ts.p.toppager = "#"+ts.p.toppager;
			$(grid.topDiv).insertBefore(grid.hDiv).addClass('ui-state-default ui-jqgrid-toppager').width(grid.width);
			setPager(ts.p.toppager,'_t');
		}
		if(ts.p.footerrow) {
			grid.sDiv = $("<div class='ui-jqgrid-sdiv'></div>")[0];
			hb = $("<div class='ui-jqgrid-hbox"+(dir=="rtl"?"-rtl":"")+"'></div>");
			$(grid.sDiv).append(hb).insertAfter(grid.hDiv).width(grid.width);
			$(hb).append(tfoot);
			grid.footers = $(".ui-jqgrid-ftable",grid.sDiv)[0].rows[0].cells;
			if(ts.p.rownumbers) { grid.footers[0].className = 'ui-state-default jqgrid-rownum'; }
			if(hg) {$(grid.sDiv).hide();}
		}
		hb = null;
		if(ts.p.caption) {
			var tdt = ts.p.datatype;
			if(ts.p.hidegrid===true) {
				$(".ui-jqgrid-titlebar-close",grid.cDiv).click( function(e){
					var onHdCl = $.isFunction(ts.p.onHeaderClick),
					elems = ".ui-jqgrid-bdiv, .ui-jqgrid-hdiv, .ui-jqgrid-pager, .ui-jqgrid-sdiv",
					counter, self = this;
					if(ts.p.toolbar[0]===true) {
						if( ts.p.toolbar[1]=='both') {
							elems += ', #' + $(grid.ubDiv).attr('id');
						}
						elems += ', #' + $(grid.uDiv).attr('id');
					}
					counter = $(elems,"#gview_"+$.jgrid.jqID(ts.p.id)).length;

					if(ts.p.gridstate == 'visible') {
						$(elems,"#gbox_"+$.jgrid.jqID(ts.p.id)).slideUp("fast", function() {
							counter--;
							if (counter === 0) {
								$("span",self).removeClass("ui-icon-circle-triangle-n").addClass("ui-icon-circle-triangle-s");
								ts.p.gridstate = 'hidden';
								if($("#gbox_"+$.jgrid.jqID(ts.p.id)).hasClass("ui-resizable")) { $(".ui-resizable-handle","#gbox_"+$.jgrid.jqID(ts.p.id)).hide(); }
								if(onHdCl) {if(!hg) {ts.p.onHeaderClick.call(ts,ts.p.gridstate,e);}}
							}
						});
					} else if(ts.p.gridstate == 'hidden'){
						$(elems,"#gbox_"+$.jgrid.jqID(ts.p.id)).slideDown("fast", function() {
							counter--;
							if (counter === 0) {
								$("span",self).removeClass("ui-icon-circle-triangle-s").addClass("ui-icon-circle-triangle-n");
								if(hg) {ts.p.datatype = tdt;populate();hg=false;}
								ts.p.gridstate = 'visible';
								if($("#gbox_"+$.jgrid.jqID(ts.p.id)).hasClass("ui-resizable")) { $(".ui-resizable-handle","#gbox_"+$.jgrid.jqID(ts.p.id)).show(); }
								if(onHdCl) {if(!hg) {ts.p.onHeaderClick.call(ts,ts.p.gridstate,e);}}
							}
						});
					}
					return false;
				});
				if(hg) {ts.p.datatype="local"; $(".ui-jqgrid-titlebar-close",grid.cDiv).trigger("click");}
			}
		} else {$(grid.cDiv).hide();}
		$(grid.hDiv).after(grid.bDiv)
		.mousemove(function (e) {
			if(grid.resizing){grid.dragMove(e);return false;}
		});
		$(".ui-jqgrid-labels",grid.hDiv).bind("selectstart", function () { return false; });
		$(document).mouseup(function (e) {
			if(grid.resizing) {	grid.dragEnd(); return false;}
			return true;
		});
		ts.formatCol = formatCol;
		ts.sortData = sortData;
		ts.updatepager = updatepager;
		ts.refreshIndex = refreshIndex;
		ts.formatter = function ( rowId, cellval , colpos, rwdat, act){return formatter(rowId, cellval , colpos, rwdat, act);};
		$.extend(grid,{populate : populate, emptyRows: emptyRows});
		this.grid = grid;
		ts.addXmlData = function(d) {addXmlData(d,ts.grid.bDiv);};
		ts.addJSONData = function(d) {addJSONData(d,ts.grid.bDiv);};
		this.grid.cols = this.rows[0].cells;

		populate();ts.p.hiddengrid=false;
		$(window).unload(function () {
			ts = null;
		});
	});
};
$.jgrid.extend({
	getGridParam : function(pName) {
		var $t = this[0];
		if (!$t || !$t.grid) {return;}
		if (!pName) { return $t.p; }
		else {return typeof($t.p[pName]) != "undefined" ? $t.p[pName] : null;}
	},
	setGridParam : function (newParams){
		return this.each(function(){
			if (this.grid && typeof(newParams) === 'object') {$.extend(true,this.p,newParams);}
		});
	},
	getDataIDs : function () {
		var ids=[], i=0, len, j=0;
		this.each(function(){
			len = this.rows.length;
			if(len && len>0){
				while(i<len) {
					if($(this.rows[i]).hasClass('jqgrow')) {
						ids[j] = this.rows[i].id;
						j++;
					}
					i++;
				}
			}
		});
		return ids;
	},
	setSelection : function(selection,onsr) {
		return this.each(function(){
			var $t = this, stat,pt, ner, ia, tpsr;
			if(selection === undefined) { return; }
			onsr = onsr === false ? false : true;
			pt=$t.rows.namedItem(selection+"");
			if(!pt || pt.className.indexOf( 'ui-state-disabled' ) > -1 ) { return; }
			function scrGrid(iR){
				var ch = $($t.grid.bDiv)[0].clientHeight,
				st = $($t.grid.bDiv)[0].scrollTop,
				rpos = $t.rows[iR].offsetTop,
				rh = $t.rows[iR].clientHeight;
				if(rpos+rh >= ch+st) { $($t.grid.bDiv)[0].scrollTop = rpos-(ch+st)+rh+st; }
				else if(rpos < ch+st) {
					if(rpos < st) {
						$($t.grid.bDiv)[0].scrollTop = rpos;
					}
				}
			}
			if($t.p.scrollrows===true) {
				ner = $t.rows.namedItem(selection).rowIndex;
				if(ner >=0 ){
					scrGrid(ner);
				}
			}
			if(!$t.p.multiselect) {
				if(pt.className !== "ui-subgrid") {
					if( $t.p.selrow != pt.id) {
						$($t.rows.namedItem($t.p.selrow)).removeClass("ui-state-highlight").attr({"aria-selected":"false", "tabindex" : "-1"});
						$(pt).addClass("ui-state-highlight").attr({"aria-selected":true, "tabindex" : "0"});//.focus();
						stat = true;
					} else {
						stat = false;
					}
					$t.p.selrow = pt.id;
					if( $t.p.onSelectRow && onsr) { $t.p.onSelectRow.call($t, pt.id, stat); }
				}
			} else {
				$t.p.selrow = pt.id;
				ia = $.inArray($t.p.selrow,$t.p.selarrrow);
				if (  ia === -1 ){
					if(pt.className !== "ui-subgrid") { $(pt).addClass("ui-state-highlight").attr("aria-selected","true");}
					stat = true;
					$("#jqg_"+$.jgrid.jqID($t.p.id)+"_"+$.jgrid.jqID($t.p.selrow)).attr("checked",stat);
					$t.p.selarrrow.push($t.p.selrow);
				} else {
					if(pt.className !== "ui-subgrid") { $(pt).removeClass("ui-state-highlight").attr("aria-selected","false");}
					stat = false;
					$("#jqg_"+$.jgrid.jqID($t.p.id)+"_"+$.jgrid.jqID($t.p.selrow)).attr("checked",stat);
					$t.p.selarrrow.splice(ia,1);
					tpsr = $t.p.selarrrow[0];
					$t.p.selrow = (tpsr === undefined) ? null : tpsr;
				}
				if( $t.p.onSelectRow && onsr) { $t.p.onSelectRow.call($t, pt.id , stat); }
			}
		});
	},
	resetSelection : function( rowid ){
		return this.each(function(){
			var t = this, ind, sr;
			if(typeof(rowid) !== "undefined" ) {
				sr = rowid === t.p.selrow ? t.p.selrow : rowid;
				$("#"+$.jgrid.jqID(t.p.id)+" tbody:first tr#"+$.jgrid.jqID(sr)).removeClass("ui-state-highlight").attr("aria-selected","false");
				if(t.p.multiselect) {
					$("#jqg_"+$.jgrid.jqID(t.p.id)+"_"+$.jgrid.jqID(sr)).attr("checked",false);
					$("#cb_"+$.jgrid.jqID(t.p.id)).attr("checked",false);
				}
				sr = null;
			} else if(!t.p.multiselect) {
				if(t.p.selrow) {
					$("#"+$.jgrid.jqID(t.p.id)+" tbody:first tr#"+$.jgrid.jqID(t.p.selrow)).removeClass("ui-state-highlight").attr("aria-selected","false");
					t.p.selrow = null;
				}
			} else {
				$(t.p.selarrrow).each(function(i,n){
					ind = t.rows.namedItem(n);
					$(ind).removeClass("ui-state-highlight").attr("aria-selected","false");
					$("#jqg_"+$.jgrid.jqID(t.p.id)+"_"+$.jgrid.jqID(n)).attr("checked",false);
				});
				$("#cb_"+$.jgrid.jqID(t.p.id)).attr("checked",false);
				t.p.selarrrow = [];
			}
			t.p.savedRow = [];
		});
	},
	getRowData : function( rowid ) {
		var res = {}, resall, getall=false, len, j=0;
		this.each(function(){
			var $t = this,nm,ind;
			if(typeof(rowid) == 'undefined') {
				getall = true;
				resall = [];
				len = $t.rows.length;
			} else {
				ind = $t.rows.namedItem(rowid);
				if(!ind) { return res; }
				len = 2;
			}
			while(j<len){
				if(getall) { ind = $t.rows[j]; }
				if( $(ind).hasClass('jqgrow') ) {
					$('td',ind).each( function(i) {
						nm = $t.p.colModel[i].name;
						if ( nm !== 'cb' && nm !== 'subgrid' && nm !== 'rn') {
							if($t.p.treeGrid===true && nm == $t.p.ExpandColumn) {
								res[nm] = $.jgrid.htmlDecode($("span:first",this).html());
							} else {
								try {
									res[nm] = $.unformat(this,{rowId:ind.id, colModel:$t.p.colModel[i]},i);
								} catch (e){
									res[nm] = $.jgrid.htmlDecode($(this).html());
								}
							}
						}
					});
					if(getall) { resall.push(res); res={}; }
				}
				j++;
			}
		});
		return resall ? resall: res;
	},
	delRowData : function(rowid) {
		var success = false, rowInd, ia, ri;
		this.each(function() {
			var $t = this;
			rowInd = $t.rows.namedItem(rowid);
			if(!rowInd) {return false;}
			else {
				ri = rowInd.rowIndex;
				$(rowInd).remove();
				$t.p.records--;
				$t.p.reccount--;
				$t.updatepager(true,false);
				success=true;
				if($t.p.multiselect) {
					ia = $.inArray(rowid,$t.p.selarrrow);
					if(ia != -1) { $t.p.selarrrow.splice(ia,1);}
				}
				if(rowid == $t.p.selrow) {$t.p.selrow=null;}
			}
			if($t.p.datatype == 'local') {
				var pos = $t.p._index[rowid];
				if(typeof(pos) != 'undefined') {
					$t.p.data.splice(pos,1);
					$t.refreshIndex();
				}
			}
			if( $t.p.altRows === true && success ) {
				var cn = $t.p.altclass;
				$($t.rows).each(function(i){
					if(i % 2 ==1) { $(this).addClass(cn); }
					else { $(this).removeClass(cn); }
				});
			}
		});
		return success;
	},
	setRowData : function(rowid, data, cssp) {
		var nm, success=true, title;
		this.each(function(){
			if(!this.grid) {return false;}
			var t = this, vl, ind, cp = typeof cssp, lcdata={};
			ind = t.rows.namedItem(rowid);
			if(!ind) { return false; }
			if( data ) {
				try {
					$(this.p.colModel).each(function(i){
						nm = this.name;
						if( data[nm] !== undefined) {
							lcdata[nm] = this.formatter && typeof(this.formatter) === 'string' && this.formatter == 'date' ? $.unformat.date(data[nm],this) : data[nm];
							vl = t.formatter( rowid, data[nm], i, data, 'edit');
							title = this.title ? {"title":$.jgrid.stripHtml(vl)} : {};
							if(t.p.treeGrid===true && nm == t.p.ExpandColumn) {
								$("td:eq("+i+") > span:first",ind).html(vl).attr(title);
							} else {
								$("td:eq("+i+")",ind).html(vl).attr(title);
							}
						}
					});
					if(t.p.datatype == 'local') {
						var pos = t.p._index[rowid];
						if(t.p.treeGrid) {
							for(var key in t.p.treeReader ){
								if(lcdata.hasOwnProperty(t.p.treeReader[key])) {
									delete lcdata[t.p.treeReader[key]];
								}
						}
						}
						if(typeof(pos) != 'undefined') {
							t.p.data[pos] = $.extend(true, t.p.data[pos], lcdata);
						}
						lcdata = null;
					}
				} catch (e) {
					success = false;
				}
			}
			if(success) {
				if(cp === 'string') {$(ind).addClass(cssp);} else if(cp === 'object') {$(ind).css(cssp);}
			}
		});
		return success;
	},
	addRowData : function(rowid,rdata,pos,src) {
		if(!pos) {pos = "last";}
		var success = false, nm, row, gi, si, ni,sind, i, v, prp="", aradd, cnm, cn, data, cm;
		if(rdata) {
			if($.isArray(rdata)) {
				aradd=true;
				pos = "last";
				cnm = rowid;
			} else {
				rdata = [rdata];
				aradd = false;
			}
			this.each(function() {
				var t = this, datalen = rdata.length;
				ni = t.p.rownumbers===true ? 1 :0;
				gi = t.p.multiselect ===true ? 1 :0;
				si = t.p.subGrid===true ? 1 :0;
				if(!aradd) {
					if(typeof(rowid) != 'undefined') { rowid = rowid+"";}
					else {
						rowid = $.jgrid.randId();
						if(t.p.keyIndex !== false) {
							cnm = t.p.colModel[t.p.keyIndex+gi+si+ni].name;
							if(typeof rdata[0][cnm] != "undefined") { rowid = rdata[0][cnm]; }
						}
					}
				}
				cn = t.p.altclass;
				var k = 0, cna ="", lcdata = {},
				air = $.isFunction(t.p.afterInsertRow) ? true : false;
				while(k < datalen) {
					data = rdata[k];
					row="";
					if(aradd) {
						try {rowid = data[cnm];}
						catch (e) {rowid = $.jgrid.randId();}
						cna = t.p.altRows === true ?  (t.rows.length-1)%2 === 0 ? cn : "" : "";
					}
					if(ni){
						prp = t.formatCol(0,1,'',null,rowid, true);
						row += "<td role=\"gridcell\" aria-describedby=\""+t.p.id+"_rn\" class=\"ui-state-default jqgrid-rownum\" "+prp+">0</td>";
					}
					if(gi) {
						v = "<input role=\"checkbox\" type=\"checkbox\""+" id=\"jqg_"+t.p.id+"_"+rowid+"\" class=\"cbox\"/>";
						prp = t.formatCol(ni,1,'', null, rowid, true);
						row += "<td role=\"gridcell\" aria-describedby=\""+t.p.id+"_cb\" "+prp+">"+v+"</td>";
					}
					if(si) {
						row += $(t).jqGrid("addSubGridCell",gi+ni,1);
					}
					for(i = gi+si+ni; i < t.p.colModel.length;i++){
						cm = t.p.colModel[i];
						nm = cm.name;
						lcdata[nm] = cm.formatter && typeof(cm.formatter) === 'string' && cm.formatter == 'date' ? $.unformat.date(data[nm],cm) : data[nm];
						v = t.formatter( rowid, $.jgrid.getAccessor(data,nm), i, data, 'edit');
						prp = t.formatCol(i,1,v, rowid, data, true);
						row += "<td role=\"gridcell\" aria-describedby=\""+t.p.id+"_"+nm+"\" "+prp+">"+v+"</td>";
					}
					row = "<tr id=\""+rowid+"\" role=\"row\" tabindex=\"-1\" class=\"ui-widget-content jqgrow ui-row-"+t.p.direction+" "+cna+"\">" + row+"</tr>";
					if(t.rows.length === 0){
						$("table:first",t.grid.bDiv).append(row);
					} else {
					switch (pos) {
						case 'last':
							$(t.rows[t.rows.length-1]).after(row);
							sind = t.rows.length-1;
							break;
						case 'first':
							$(t.rows[0]).after(row);
							sind = 1;
							break;
						case 'after':
							sind = t.rows.namedItem(src);
							if (sind) {
								if($(t.rows[sind.rowIndex+1]).hasClass("ui-subgrid")) { $(t.rows[sind.rowIndex+1]).after(row); }
								else { $(sind).after(row); }
							}
							sind++;
							break;
						case 'before':
							sind = t.rows.namedItem(src);
							if(sind) {$(sind).before(row);sind=sind.rowIndex;}
							sind--;
							break;
					}
					}
					if(t.p.subGrid===true) {
						$(t).jqGrid("addSubGrid",gi+ni, sind);
					}
					t.p.records++;
					t.p.reccount++;
					if(air) { t.p.afterInsertRow.call(t,rowid,data,data); }
					k++;
					if(t.p.datatype == 'local') {
						lcdata[t.p.localReader.id] = rowid;
						t.p._index[rowid] = t.p.data.length;
						t.p.data.push(lcdata);
						lcdata = {};
					}
				}
				if( t.p.altRows === true && !aradd) {
					if (pos == "last") {
						if ((t.rows.length-1)%2 == 1)  {$(t.rows[t.rows.length-1]).addClass(cn);}
					} else {
						$(t.rows).each(function(i){
							if(i % 2 ==1) { $(this).addClass(cn); }
							else { $(this).removeClass(cn); }
						});
					}
				}
				t.updatepager(true,true);
				success = true;
			});
		}
		return success;
	},
	footerData : function(action,data, format) {
		var nm, success=false, res={}, title;
		function isEmpty(obj) {
			for(var i in obj) {
				if (obj.hasOwnProperty(i)) { return false; }
			}
			return true;
		}
		if(typeof(action) == "undefined") { action = "get"; }
		if(typeof(format) != "boolean") { format  = true; }
		action = action.toLowerCase();
		this.each(function(){
			var t = this, vl;
			if(!t.grid || !t.p.footerrow) {return false;}
			if(action == "set") { if(isEmpty(data)) { return false; } }
			success=true;
			$(this.p.colModel).each(function(i){
				nm = this.name;
				if(action == "set") {
					if( data[nm] !== undefined) {
						vl = format ? t.formatter( "", data[nm], i, data, 'edit') : data[nm];
						title = this.title ? {"title":$.jgrid.stripHtml(vl)} : {};
						$("tr.footrow td:eq("+i+")",t.grid.sDiv).html(vl).attr(title);
						success = true;
					}
				} else if(action == "get") {
					res[nm] = $("tr.footrow td:eq("+i+")",t.grid.sDiv).html();
				}
			});
		});
		return action == "get" ? res : success;
	},
	showHideCol : function(colname,show) {
		return this.each(function() {
			var $t = this, fndh=false, brd=$.browser.webkit||$.browser.safari? 0: $t.p.cellLayout, cw;
			if (!$t.grid ) {return;}
			if( typeof colname === 'string') {colname=[colname];}
			show = show != "none" ? "" : "none";
			var sw = show === "" ? true :false;
			$(this.p.colModel).each(function(i) {
				if ($.inArray(this.name,colname) !== -1 && this.hidden === sw) {
					$("tr",$t.grid.hDiv).each(function(){
						$(this).children("th:eq("+i+")").css("display",show);
					});
					$($t.rows).each(function(j){
						$(this).children("td:eq("+i+")").css("display",show);
					});
					if($t.p.footerrow) { $($t.grid.sDiv).children("td:eq("+i+")").css("display", show); }
					cw = this.widthOrg? this.widthOrg: parseInt(this.width,10);
					if(show === "none") {$t.p.tblwidth -= cw+brd;} else {$t.p.tblwidth += cw+brd;}
					this.hidden = !sw;
					fndh=true;
				}
			});
			if(fndh===true) {
				if($t.p.shrinkToFit === false) {
					$($t).jqGrid("setGridWidth",$t.grid.width );
				} else if($t.grid.width!==$t.p.tblwidth) {
					$($t).jqGrid("setGridWidth",$t.p.tblwidth );
				}
			}
		});
	},
	hideCol : function (colname) {
		return this.each(function(){$(this).jqGrid("showHideCol",colname,"none");});
	},
	showCol : function(colname) {
		return this.each(function(){$(this).jqGrid("showHideCol",colname,"");});
	},
	remapColumns : function(permutation, updateCells, keepHeader)
	{
		function resortArray(a) {
			var ac;
			if (a.length) {
				ac = $.makeArray(a);
			} else {
				ac = $.extend({}, a);
			}
			$.each(permutation, function(i) {
				a[i] = ac[this];
			});
		}
		var ts = this.get(0);
		function resortRows(parent, clobj) {
			$(">tr"+(clobj||""), parent).each(function() {
				var row = this;
				var elems = $.makeArray(row.cells);
				$.each(permutation, function() {
					var e = elems[this];
					if (e) {
						row.appendChild(e);
					}
				});
			});
		}
		resortArray(ts.p.colModel);
		resortArray(ts.p.colNames);
		resortArray(ts.grid.headers);
		resortRows($("thead:first", ts.grid.hDiv), keepHeader && ":not(.ui-jqgrid-labels)");
		if (updateCells) {
			resortRows($("#"+$.jgrid.jqID(ts.p.id)+" tbody:first"), ".jqgfirstrow, tr.jqgrow, tr.jqfoot");
		}
		if (ts.p.footerrow) {
			resortRows($("tbody:first", ts.grid.sDiv));
		}
		if (ts.p.remapColumns) {
			if (!ts.p.remapColumns.length){
				ts.p.remapColumns = $.makeArray(permutation);
			} else {
				resortArray(ts.p.remapColumns);
			}
		}
		ts.p.lastsort = $.inArray(ts.p.lastsort, permutation);
		if(ts.p.treeGrid) { ts.p.expColInd = $.inArray(ts.p.expColInd, permutation); }
	},
	setGridWidth : function(nwidth, shrink) {
		return this.each(function(){
			if (!this.grid ) {return;}
			var $t = this, cw,
			initwidth = 0, brd=$.browser.webkit||$.browser.safari? 0: $t.p.cellLayout, lvc, vc=0, hs=false, scw=$t.p.scrollOffset, aw, gw=0,
			cl = 0,cr;
			if(typeof shrink != 'boolean') {
				shrink=$t.p.shrinkToFit;
			}
			if(isNaN(nwidth)) {return;}
			else { nwidth = parseInt(nwidth,10); $t.grid.width = $t.p.width = nwidth;}
			$("#gbox_"+$.jgrid.jqID($t.p.id)).css("width",nwidth+"px");
			$("#gview_"+$.jgrid.jqID($t.p.id)).css("width",nwidth+"px");
			$($t.grid.bDiv).css("width",nwidth+"px");
			$($t.grid.hDiv).css("width",nwidth+"px");
			if($t.p.pager ) {$($t.p.pager).css("width",nwidth+"px");}
			if($t.p.toppager ) {$($t.p.toppager).css("width",nwidth+"px");}
			if($t.p.toolbar[0] === true){
				$($t.grid.uDiv).css("width",nwidth+"px");
				if($t.p.toolbar[1]=="both") {$($t.grid.ubDiv).css("width",nwidth+"px");}
			}
			if($t.p.footerrow) { $($t.grid.sDiv).css("width",nwidth+"px"); }
			if(shrink ===false && $t.p.forceFit === true) {$t.p.forceFit=false;}
			if(shrink===true) {
				$.each($t.p.colModel, function(i) {
					if(this.hidden===false){
						cw = this.widthOrg? this.widthOrg: parseInt(this.width,10);
						initwidth += cw+brd;
						if(this.fixed) {
							gw += cw+brd;
						} else {
							vc++;
						}
						cl++;
					}
				});
				if(vc  === 0) { return; }
				$t.p.tblwidth = initwidth;
				aw = nwidth-brd*vc-gw;
				if(!isNaN($t.p.height)) {
					if($($t.grid.bDiv)[0].clientHeight < $($t.grid.bDiv)[0].scrollHeight || $t.rows.length === 1){
						hs = true;
						aw -= scw;
					}
				}
				initwidth =0;
				var cle = $t.grid.cols.length >0;
				$.each($t.p.colModel, function(i) {
					if(this.hidden === false && !this.fixed){
						cw = this.widthOrg? this.widthOrg: parseInt(this.width,10);
						cw = Math.round(aw*cw/($t.p.tblwidth-brd*vc-gw));
						if (cw < 0) { return; }
						this.width =cw;
						initwidth += cw;
						$t.grid.headers[i].width=cw;
						$t.grid.headers[i].el.style.width=cw+"px";
						if($t.p.footerrow) { $t.grid.footers[i].style.width = cw+"px"; }
						if(cle) { $t.grid.cols[i].style.width = cw+"px"; }
						lvc = i;
					}
				});
				cr =0;
				if (hs) {
					if(nwidth-gw-(initwidth+brd*vc) !== scw){
						cr = nwidth-gw-(initwidth+brd*vc)-scw;
					}
				} else if( Math.abs(nwidth-gw-(initwidth+brd*vc)) !== 1) {
					cr = nwidth-gw-(initwidth+brd*vc);
				}
				$t.p.colModel[lvc].width += cr;
				$t.p.tblwidth = initwidth+cr+brd*vc+gw;
				if($t.p.tblwidth > nwidth) {
					var delta = $t.p.tblwidth - parseInt(nwidth,10);
					$t.p.tblwidth = nwidth;
					cw = $t.p.colModel[lvc].width = $t.p.colModel[lvc].width-delta;
				} else {
					cw= $t.p.colModel[lvc].width;
				}
				$t.grid.headers[lvc].width = cw;
				$t.grid.headers[lvc].el.style.width=cw+"px";
				if(cle) { $t.grid.cols[lvc].style.width = cw+"px"; }
				if($t.p.footerrow) {
					$t.grid.footers[lvc].style.width = cw+"px";
				}
			}
			if($t.p.tblwidth) {
				$('table:first',$t.grid.bDiv).css("width",$t.p.tblwidth+"px");
				$('table:first',$t.grid.hDiv).css("width",$t.p.tblwidth+"px");
				$t.grid.hDiv.scrollLeft = $t.grid.bDiv.scrollLeft;
				if($t.p.footerrow) {
					$('table:first',$t.grid.sDiv).css("width",$t.p.tblwidth+"px");
				}
			}
		});
	},
	setGridHeight : function (nh) {
		return this.each(function (){
			var $t = this;
			if(!$t.grid) {return;}
			$($t.grid.bDiv).css({height: nh+(isNaN(nh)?"":"px")});
			$t.p.height = nh;
			if ($t.p.scroll) { $t.grid.populateVisible(); }
		});
	},
	setCaption : function (newcap){
		return this.each(function(){
			this.p.caption=newcap;
			$("span.ui-jqgrid-title",this.grid.cDiv).html(newcap);
			$(this.grid.cDiv).show();
		});
	},
	setLabel : function(colname, nData, prop, attrp ){
		return this.each(function(){
			var $t = this, pos=-1;
			if(!$t.grid) {return;}
			if(typeof(colname) != "undefined") {
				$($t.p.colModel).each(function(i){
					if (this.name == colname) {
						pos = i;return false;
					}
				});
			} else { return; }
			if(pos>=0) {
				var thecol = $("tr.ui-jqgrid-labels th:eq("+pos+")",$t.grid.hDiv);
				if (nData){
					var ico = $(".s-ico",thecol);
					$("[id^=jqgh_]",thecol).empty().html(nData).append(ico);
					$t.p.colNames[pos] = nData;
				}
				if (prop) {
					if(typeof prop === 'string') {$(thecol).addClass(prop);} else {$(thecol).css(prop);}
				}
				if(typeof attrp === 'object') {$(thecol).attr(attrp);}
			}
		});
	},
	setCell : function(rowid,colname,nData,cssp,attrp, forceupd) {
		return this.each(function(){
			var $t = this, pos =-1,v, title;
			if(!$t.grid) {return;}
			if(isNaN(colname)) {
				$($t.p.colModel).each(function(i){
					if (this.name == colname) {
						pos = i;return false;
					}
				});
			} else {pos = parseInt(colname,10);}
			if(pos>=0) {
				var ind = $t.rows.namedItem(rowid);
				if (ind){
					var tcell = $("td:eq("+pos+")",ind);
					if(nData !== "" || forceupd === true) {
						v = $t.formatter(rowid, nData, pos,ind,'edit');
						title = $t.p.colModel[pos].title ? {"title":$.jgrid.stripHtml(v)} : {};
						if($t.p.treeGrid && $(".tree-wrap",$(tcell)).length>0) {
							$("span",$(tcell)).html(v).attr(title);
						} else {
							$(tcell).html(v).attr(title);
						}
						if($t.p.datatype == "local") {
							var cm = $t.p.colModel[pos], index;
							nData = cm.formatter && typeof(cm.formatter) === 'string' && cm.formatter == 'date' ? $.unformat.date(nData,cm) : nData;
							index = $t.p._index[rowid];
							if(typeof index  != "undefined") {
								$t.p.data[index][cm.name] = nData;
							}
						}
					}
					if(typeof cssp === 'string'){
						$(tcell).addClass(cssp);
					} else if(cssp) {
						$(tcell).css(cssp);
					}
					if(typeof attrp === 'object') {$(tcell).attr(attrp);}
				}
			}
		});
	},
	getCell : function(rowid,col) {
		var ret = false;
		this.each(function(){
			var $t=this, pos=-1;
			if(!$t.grid) {return;}
			if(isNaN(col)) {
				$($t.p.colModel).each(function(i){
					if (this.name === col) {
						pos = i;return false;
					}
				});
			} else {pos = parseInt(col,10);}
			if(pos>=0) {
				var ind = $t.rows.namedItem(rowid);
				if(ind) {
					try {
						ret = $.unformat($("td:eq("+pos+")",ind),{rowId:ind.id, colModel:$t.p.colModel[pos]},pos);
					} catch (e){
						ret = $.jgrid.htmlDecode($("td:eq("+pos+")",ind).html());
					}
				}
			}
		});
		return ret;
	},
	getCol : function (col, obj, mathopr) {
		var ret = [], val, sum=0, min=0, max=0, v;
		obj = typeof (obj) != 'boolean' ? false : obj;
		if(typeof mathopr == 'undefined') { mathopr = false; }
		this.each(function(){
			var $t=this, pos=-1;
			if(!$t.grid) {return;}
			if(isNaN(col)) {
				$($t.p.colModel).each(function(i){
					if (this.name === col) {
						pos = i;return false;
					}
				});
			} else {pos = parseInt(col,10);}
			if(pos>=0) {
				var ln = $t.rows.length, i =0;
				if (ln && ln>0){
					while(i<ln){
						if($($t.rows[i]).hasClass('jqgrow')) {
							try {
								val = $.unformat($($t.rows[i].cells[pos]),{rowId:$t.rows[i].id, colModel:$t.p.colModel[pos]},pos);
							} catch (e) {
								val = $.jgrid.htmlDecode($t.rows[i].cells[pos].innerHTML);
							}
							if(mathopr) {
								v = parseFloat(val);
								sum += v;
								min = Math.min(min, v);
								max = Math.max(min, v);
							}
							else if(obj) { ret.push( {id:$t.rows[i].id,value:val} ); }
							else { ret.push( val ); }
						}
						i++;
					}
					if(mathopr) {
						switch(mathopr.toLowerCase()){
							case 'sum': ret =sum; break;
							case 'avg': ret = sum/ln; break;
							case 'count': ret = ln; break;
							case 'min': ret = min; break;
							case 'max': ret = max; break;
						}
					}
				}
			}
		});
		return ret;
	},
	clearGridData : function(clearfooter) {
		return this.each(function(){
			var $t = this;
			if(!$t.grid) {return;}
			if(typeof clearfooter != 'boolean') { clearfooter = false; }
			if($t.p.deepempty) {$("#"+$.jgrid.jqID($t.p.id)+" tbody:first tr:gt(0)").remove();}
			else {
				var trf = $("#"+$.jgrid.jqID($t.p.id)+" tbody:first tr:first")[0];
				$("#"+$.jgrid.jqID($t.p.id)+" tbody:first").empty().append(trf);
			}
			if($t.p.footerrow && clearfooter) { $(".ui-jqgrid-ftable td",$t.grid.sDiv).html("&#160;"); }
			$t.p.selrow = null; $t.p.selarrrow= []; $t.p.savedRow = [];
			$t.p.records = 0;$t.p.page=1;$t.p.lastpage=0;$t.p.reccount=0;
			$t.p.data = []; $t.p_index = {};
			$t.updatepager(true,false);
		});
	},
	getInd : function(rowid,rc){
		var ret =false,rw;
		this.each(function(){
			rw = this.rows.namedItem(rowid);
			if(rw) {
				ret = rc===true ? rw: rw.rowIndex;
			}
		});
		return ret;
	},
	bindKeys : function( settings ){
		var o = $.extend({
			onEnter: null,
			onSpace: null,
			onLeftKey: null,
			onRightKey: null,
			scrollingRows : true
		},settings || {});
		return this.each(function(){
			var $t = this;
			if( !$('body').is('[role]') ){$('body').attr('role','application');}
			$t.p.scrollrows = o.scrollingRows;
			$($t).keydown(function(event){
				var target = $($t).find('tr[tabindex=0]')[0], id, r, mind,
				expanded = $t.p.treeReader.expanded_field;
				//check for arrow keys
				if(target) {
					mind = $t.p._index[target.id];
					if(event.keyCode === 37 || event.keyCode === 38 || event.keyCode === 39 || event.keyCode === 40){
						// up key
						if(event.keyCode === 38 ){
							r = target.previousSibling;
							id = "";
							if(r) {
								if($(r).is(":hidden")) {
									while(r) {
										r = r.previousSibling;
										if(!$(r).is(":hidden") && $(r).hasClass('jqgrow')) {id = r.id;break;}
									}
								} else {
									id = r.id;
								}
							}
							$($t).jqGrid('setSelection', id);
						}
						//if key is down arrow
						if(event.keyCode === 40){
							r = target.nextSibling;
							id ="";
							if(r) {
								if($(r).is(":hidden")) {
									while(r) {
										r = r.nextSibling;
										if(!$(r).is(":hidden") && $(r).hasClass('jqgrow') ) {id = r.id;break;}
									}
								} else {
									id = r.id;
								}
							}
							$($t).jqGrid('setSelection', id);
						}
						// left
						if(event.keyCode === 37 ){
							if($t.p.treeGrid && $t.p.data[mind][expanded]) {
								$(target).find("div.treeclick").trigger('click');
							}
							if($.isFunction(o.onLeftKey)) {
								o.onLeftKey.call($t, $t.p.selrow);
						}
						}
						// right
						if(event.keyCode === 39 ){
							if($t.p.treeGrid && !$t.p.data[mind][expanded]) {
								$(target).find("div.treeclick").trigger('click');
							}
							if($.isFunction(o.onRightKey)) {
								o.onRightKey.call($t, $t.p.selrow);
						}
						}
						return false;
					}
					//check if enter was pressed on a grid or treegrid node
					else if( event.keyCode === 13 ){
						if($.isFunction(o.onEnter)) {
							o.onEnter.call($t, $t.p.selrow);
						}
						return false;
					} else if(event.keyCode === 32) {
						if($.isFunction(o.onSpace)) {
							o.onSpace.call($t, $t.p.selrow);
					    }
						return false;
				    }
				}
			});
		});
	},
	unbindKeys : function(){
		return this.each(function(){
			var $t = this;
			$($t).unbind('keydown');
		});
	},
	getLocalRow : function (rowid) {
		var ret = false, ind;
		this.each(function(){
			if(typeof(rowid) !== "undefined") {
				ind = this.p._index[rowid];
				if(ind >= 0 ) {
					ret = this.p.data[ind];
	}
			}
});
		return ret;
	}
});
})(jQuery);
(function($){
/**
 * jqGrid extension for custom methods
 * Tony Tomov tony@trirand.com
 * http://trirand.com/blog/ 
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl-2.0.html
**/
/*global jQuery, $ */

$.jgrid.extend({
	getColProp : function(colname){
		var ret ={}, $t = this[0];
		if ( !$t.grid ) { return false; }
		var cM = $t.p.colModel;
		for ( var i =0;i<cM.length;i++ ) {
			if ( cM[i].name == colname ) {
				ret = cM[i];
				break;
			}
		}
		return ret;
	},
	setColProp : function(colname, obj){
		//do not set width will not work
		return this.each(function(){
			if ( this.grid ) {
				if ( obj ) {
					var cM = this.p.colModel;
					for ( var i =0;i<cM.length;i++ ) {
						if ( cM[i].name == colname ) {
							$.extend(this.p.colModel[i],obj);
							break;
						}
					}
				}
			}
		});
	},
	sortGrid : function(colname,reload, sor){
		return this.each(function(){
			var $t=this,idx=-1;
			if ( !$t.grid ) { return;}
			if ( !colname ) { colname = $t.p.sortname; }
			for ( var i=0;i<$t.p.colModel.length;i++ ) {
				if ( $t.p.colModel[i].index == colname || $t.p.colModel[i].name==colname ) {
					idx = i;
					break;
				}
			}
			if ( idx!=-1 ){
				var sort = $t.p.colModel[idx].sortable;
				if ( typeof sort !== 'boolean' ) { sort =  true; }
				if ( typeof reload !=='boolean' ) { reload = false; }
				if ( sort ) { $t.sortData("jqgh_"+$t.p.id+"_"+colname, idx, reload, sor); }
			}
		});
	},
	GridDestroy : function () {
		return this.each(function(){
			if ( this.grid ) { 
				if ( this.p.pager ) { // if not part of grid
					$(this.p.pager).remove();
				}
				var gid = this.id;
				try {
					$("#gbox_"+gid).remove();
				} catch (_) {}
			}
		});
	},
	GridUnload : function(){
		return this.each(function(){
			if ( !this.grid ) {return;}
			var defgrid = {id: $(this).attr('id'),cl: $(this).attr('class')};
			if (this.p.pager) {
				$(this.p.pager).empty().removeClass("ui-state-default ui-jqgrid-pager corner-bottom");
			}
			var newtable = document.createElement('table');
			$(newtable).attr({id:defgrid.id});
			newtable.className = defgrid.cl;
			var gid = this.id;
			$(newtable).removeClass("ui-jqgrid-btable");
			if( $(this.p.pager).parents("#gbox_"+gid).length === 1 ) {
				$(newtable).insertBefore("#gbox_"+gid).show();
				$(this.p.pager).insertBefore("#gbox_"+gid);
			} else {
				$(newtable).insertBefore("#gbox_"+gid).show();
			}
			$("#gbox_"+gid).remove();
		});
	},
    setGridState : function(state) {
		return this.each(function(){
			if ( !this.grid ) {return;}
            var $t = this;
            if(state == 'hidden'){
				$(".ui-jqgrid-bdiv, .ui-jqgrid-hdiv","#gview_"+$t.p.id).slideUp("fast");
				if($t.p.pager) {$($t.p.pager).slideUp("fast");}
				if($t.p.toppager) {$($t.p.toppager).slideUp("fast");}
				if($t.p.toolbar[0]===true) {
					if( $t.p.toolbar[1]=='both') {
						$($t.grid.ubDiv).slideUp("fast");
					}
					$($t.grid.uDiv).slideUp("fast");
				}
				if($t.p.footerrow) { $(".ui-jqgrid-sdiv","#gbox_"+$t.p.id).slideUp("fast"); }
				$(".ui-jqgrid-titlebar-close span",$t.grid.cDiv).removeClass("ui-icon-circle-triangle-n").addClass("ui-icon-circle-triangle-s");
				$t.p.gridstate = 'hidden';
            } else if(state=='visible') {
				$(".ui-jqgrid-hdiv, .ui-jqgrid-bdiv","#gview_"+$t.p.id).slideDown("fast");
				if($t.p.pager) {$($t.p.pager).slideDown("fast");}
				if($t.p.toppager) {$($t.p.toppager).slideDown("fast");}
				if($t.p.toolbar[0]===true) {
					if( $t.p.toolbar[1]=='both') {
						$($t.grid.ubDiv).slideDown("fast");
					}
					$($t.grid.uDiv).slideDown("fast");
				}
				if($t.p.footerrow) { $(".ui-jqgrid-sdiv","#gbox_"+$t.p.id).slideDown("fast"); }
				$(".ui-jqgrid-titlebar-close span",$t.grid.cDiv).removeClass("ui-icon-circle-triangle-s").addClass("ui-icon-circle-triangle-n");
				$t.p.gridstate = 'visible';
			}

		});
	},
	filterToolbar : function(p){
		p = $.extend({
			autosearch: true,
			searchOnEnter : true,
			beforeSearch: null,
			afterSearch: null,
			beforeClear: null,
			afterClear: null,
			searchurl : '',
			stringResult: false,
			groupOp: 'AND',
			defaultSearch : "bw"
		},p  || {});
		return this.each(function(){
			var $t = this;
			if(this.ftoolbar) { return; }
			var triggerToolbar = function() {
				var sdata={}, j=0, v, nm, sopt={},so;
				$.each($t.p.colModel,function(i,n){
					nm = this.index || this.name;
					switch (this.stype) {
						case 'select' :
							so  = (this.searchoptions && this.searchoptions.sopt) ? this.searchoptions.sopt[0] : 'eq';
							v = $("#gs_"+$.jgrid.jqID(this.name),$t.grid.hDiv).val();
							if(v) {
								sdata[nm] = v;
								sopt[nm] = so;
								j++;
							} else {
								try {
									delete $t.p.postData[nm];
								} catch (e) {}
							}
							break;
						case 'text':
							so  = (this.searchoptions && this.searchoptions.sopt) ? this.searchoptions.sopt[0] : p.defaultSearch;
							v = $("#gs_"+$.jgrid.jqID(this.name), $t.grid.hDiv).val();
							if(v) {
								sdata[nm] = v;
								sopt[nm] = so;
								j++;
							} else {
								try {
									delete $t.p.postData[nm];
								} catch (z) {}
							}
							break;
					}
				});
				var sd =  j>0 ? true : false;
				if(p.stringResult === true || $t.p.datatype == "local") {
					var ruleGroup = "{\"groupOp\":\"" + p.groupOp + "\",\"rules\":[";
					var gi=0;
					$.each(sdata,function(i,n){
						if (gi > 0) {ruleGroup += ",";}
						ruleGroup += "{\"field\":\"" + i + "\",";
						ruleGroup += "\"op\":\"" + sopt[i] + "\",";
						n+="";
						ruleGroup += "\"data\":\"" + n.replace(/\\/g,'\\\\').replace(/\"/g,'\\"') + "\"}";
						gi++;
					});
					ruleGroup += "]}";
					$.extend($t.p.postData,{filters:ruleGroup});
					$.each(['searchField', 'searchString', 'searchOper'], function(i, n){
						if($t.p.postData.hasOwnProperty(n)) { delete $t.p.postData[n];}
					});
				} else {
					$.extend($t.p.postData,sdata);
				}
				var saveurl;
				if($t.p.searchurl) {
					saveurl = $t.p.url;
					$($t).jqGrid("setGridParam",{url:$t.p.searchurl});
				}
				var bsr = false;
				if($.isFunction(p.beforeSearch)){bsr = p.beforeSearch.call($t);}
				if(!bsr) { $($t).jqGrid("setGridParam",{search:sd}).trigger("reloadGrid",[{page:1}]); }
				if(saveurl) {$($t).jqGrid("setGridParam",{url:saveurl});}
				if($.isFunction(p.afterSearch)){p.afterSearch();}
			};
			var clearToolbar = function(trigger){
				var sdata={}, v, j=0, nm;
				trigger = (typeof trigger != 'boolean') ? true : trigger;
				$.each($t.p.colModel,function(i,n){
					v = (this.searchoptions && this.searchoptions.defaultValue) ? this.searchoptions.defaultValue : "";
					nm = this.index || this.name;
					switch (this.stype) {
						case 'select' :
							var v1;
							$("#gs_"+$.jgrid.jqID(nm)+" option",$t.grid.hDiv).each(function (i){
								if(i===0) { this.selected = true; }
								if ($(this).text() == v) {
									this.selected = true;
									v1 = $(this).val();
									return false;
								}
							});
							if (v1) {
								// post the key and not the text
								sdata[nm] = v1;
								j++;
							} else {
								try {
									delete $t.p.postData[nm];
								} catch(e) {}
							}
							break;
						case 'text':
							$("#gs_"+$.jgrid.jqID(nm),$t.grid.hDiv).val(v);
							if(v) {
								sdata[nm] = v;
								j++;
							} else {
								try {
									delete $t.p.postData[nm];
								} catch (y){}
							}
							break;
					}
				});
				var sd =  j>0 ? true : false;
				if(p.stringResult === true || $t.p.datatype == "local") {
					var ruleGroup = "{\"groupOp\":\"" + p.groupOp + "\",\"rules\":[";
					var gi=0;
					$.each(sdata,function(i,n){
						if (gi > 0) {ruleGroup += ",";}
						ruleGroup += "{\"field\":\"" + i + "\",";
						ruleGroup += "\"op\":\"" + "eq" + "\",";
						n+="";
						ruleGroup += "\"data\":\"" + n.replace(/\\/g,'\\\\').replace(/\"/g,'\\"') + "\"}";
						gi++;
					});
					ruleGroup += "]}";
					$.extend($t.p.postData,{filters:ruleGroup});
					$.each(['searchField', 'searchString', 'searchOper'], function(i, n){
						if($t.p.postData.hasOwnProperty(n)) { delete $t.p.postData[n];}
					});
				} else {
					$.extend($t.p.postData,sdata);
				}
				var saveurl;
				if($t.p.searchurl) {
					saveurl = $t.p.url;
					$($t).jqGrid("setGridParam",{url:$t.p.searchurl});
				}
				var bcv = false;
				if($.isFunction(p.beforeClear)){bcv = p.beforeClear.call($t);}
				if(!bcv) {
					if(trigger) {
						$($t).jqGrid("setGridParam",{search:sd}).trigger("reloadGrid",[{page:1}]);
					}
				}
				if(saveurl) {$($t).jqGrid("setGridParam",{url:saveurl});}
				if($.isFunction(p.afterClear)){p.afterClear();}
			};
			var toggleToolbar = function(){
				var trow = $("tr.ui-search-toolbar",$t.grid.hDiv);
				if(trow.css("display")=='none') { trow.show(); }
				else { trow.hide(); }
			};
			// create the row
			function bindEvents(selector, events) {
				var jElem = $(selector);
				if (jElem[0]) {
				    jQuery.each(events, function() {
				        if (this.data !== undefined) {
				            jElem.bind(this.type, this.data, this.fn);
				        } else {
				            jElem.bind(this.type, this.fn);
				        }
				    });
				}
			}
			var tr = $("<tr class='ui-search-toolbar' role='rowheader'></tr>");
			var timeoutHnd;
			$.each($t.p.colModel,function(i,n){
				var cm=this, thd , th, soptions,surl,self;
				th = $("<th role='columnheader' class='ui-state-default ui-th-column ui-th-"+$t.p.direction+"'></th>");
				thd = $("<div style='width:100%;position:relative;height:100%;padding-right:0.3em;'></div>");
				if(this.hidden===true) { $(th).css("display","none");}
				this.search = this.search === false ? false : true;
				if(typeof this.stype == 'undefined' ) {this.stype='text';}
				soptions = $.extend({},this.searchoptions || {});
				if(this.search){
					switch (this.stype)
					{
					case "select":
						surl = this.surl || soptions.dataUrl;
						if(surl) {
							// data returned should have already constructed html select
							// primitive jQuery load
							self = thd;
							$.ajax($.extend({
								url: surl,
								dataType: "html",
								complete: function(res,status) {
									if(soptions.buildSelect !== undefined) {
										var d = soptions.buildSelect(res);
										if (d) { $(self).append(d); }
									} else {
										$(self).append(res.responseText);
									}
									if(soptions.defaultValue) { $("select",self).val(soptions.defaultValue); }
									$("select",self).attr({name:cm.index || cm.name, id: "gs_"+cm.name});
									if(soptions.attr) {$("select",self).attr(soptions.attr);}
									$("select",self).css({width: "100%"});
									// preserve autoserch
									if(soptions.dataInit !== undefined) { soptions.dataInit($("select",self)[0]); }
									if(soptions.dataEvents !== undefined) { bindEvents($("select",self)[0],soptions.dataEvents); }
									if(p.autosearch===true){
										$("select",self).change(function(e){
											triggerToolbar();
											return false;
										});
									}
									res=null;
								}
							}, $.jgrid.ajaxOptions, $t.p.ajaxSelectOptions || {} ));
						} else {
							var oSv;
							if(cm.searchoptions && cm.searchoptions.value) {
								oSv = cm.searchoptions.value;
							} else if(cm.editoptions && cm.editoptions.value) {
								oSv = cm.editoptions.value;
							}
							if (oSv) {	
								var elem = document.createElement("select");
								elem.style.width = "100%";
								$(elem).attr({name:cm.index || cm.name, id: "gs_"+cm.name});
								var so, sv, ov;
								if(typeof oSv === "string") {
									so = oSv.split(";");
									for(var k=0; k<so.length;k++){
										sv = so[k].split(":");
										ov = document.createElement("option");
										ov.value = sv[0]; ov.innerHTML = sv[1];
										elem.appendChild(ov);
									}
								} else if(typeof oSv === "object" ) {
									for ( var key in oSv) {
										if(oSv.hasOwnProperty(key)) {
											ov = document.createElement("option");
											ov.value = key; ov.innerHTML = oSv[key];
											elem.appendChild(ov);
										}
									}
								}
								if(soptions.defaultValue) { $(elem).val(soptions.defaultValue); }
								if(soptions.attr) {$(elem).attr(soptions.attr);}
								if(soptions.dataInit !== undefined) { soptions.dataInit(elem); }
								if(soptions.dataEvents !== undefined) { bindEvents(elem, soptions.dataEvents); }
								$(thd).append(elem);
								if(p.autosearch===true){
									$(elem).change(function(e){
										triggerToolbar();
										return false;
									});
								}
							}
						}
						break;
					case 'text':
						var df = soptions.defaultValue ? soptions.defaultValue: "";
						$(thd).append("<input type='text' style='width:95%;padding:0px;' name='"+(cm.index || cm.name)+"' id='gs_"+cm.name+"' value='"+df+"'/>");
						if(soptions.attr) {$("input",thd).attr(soptions.attr);}
						if(soptions.dataInit !== undefined) { soptions.dataInit($("input",thd)[0]); }
						if(soptions.dataEvents !== undefined) { bindEvents($("input",thd)[0], soptions.dataEvents); }
						if(p.autosearch===true){
							if(p.searchOnEnter) {
								$("input",thd).keypress(function(e){
									var key = e.charCode ? e.charCode : e.keyCode ? e.keyCode : 0;
									if(key == 13){
										triggerToolbar();
										return false;
									}
									return this;
								});
							} else {
								$("input",thd).keydown(function(e){
									var key = e.which;
									switch (key) {
										case 13:
											return false;
										case 9 :
										case 16:
										case 37:
										case 38:
										case 39:
										case 40:
										case 27:
											break;
										default :
											if(timeoutHnd) { clearTimeout(timeoutHnd); }
											timeoutHnd = setTimeout(function(){triggerToolbar();},500);
									}
								});
							}
						}
						break;
					}
				}
				$(th).append(thd);
				$(tr).append(th);
			});
			$("table thead",$t.grid.hDiv).append(tr);
			this.ftoolbar = true;
			this.triggerToolbar = triggerToolbar;
			this.clearToolbar = clearToolbar;
			this.toggleToolbar = toggleToolbar;
		});
	}
});
})(jQuery);/*
 * jqModal - Minimalist Modaling with jQuery
 *   (http://dev.iceburg.net/jquery/jqmodal/)
 *
 * Copyright (c) 2007,2008 Brice Burgess <bhb@iceburg.net>
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 * 
 * $Version: 07/06/2008 +r13
 */
(function($) {
$.fn.jqm=function(o){
var p={
overlay: 50,
closeoverlay : true,
overlayClass: 'jqmOverlay',
closeClass: 'jqmClose',
trigger: '.jqModal',
ajax: F,
ajaxText: '',
target: F,
modal: F,
toTop: F,
onShow: F,
onHide: F,
onLoad: F
};
return this.each(function(){if(this._jqm)return H[this._jqm].c=$.extend({},H[this._jqm].c,o);s++;this._jqm=s;
H[s]={c:$.extend(p,$.jqm.params,o),a:F,w:$(this).addClass('jqmID'+s),s:s};
if(p.trigger)$(this).jqmAddTrigger(p.trigger);
});};

$.fn.jqmAddClose=function(e){return hs(this,e,'jqmHide');};
$.fn.jqmAddTrigger=function(e){return hs(this,e,'jqmShow');};
$.fn.jqmShow=function(t){return this.each(function(){$.jqm.open(this._jqm,t);});};
$.fn.jqmHide=function(t){return this.each(function(){$.jqm.close(this._jqm,t)});};

$.jqm = {
hash:{},
open:function(s,t){var h=H[s],c=h.c,cc='.'+c.closeClass,z=(parseInt(h.w.css('z-index')));z=(z>0)?z:3000;var o=$('<div></div>').css({height:'100%',width:'100%',position:'fixed',left:0,top:0,'z-index':z-1,opacity:c.overlay/100});if(h.a)return F;h.t=t;h.a=true;h.w.css('z-index',z);
 if(c.modal) {if(!A[0])setTimeout(function(){L('bind');},1);A.push(s);}
 else if(c.overlay > 0) {if(c.closeoverlay) h.w.jqmAddClose(o);}
 else o=F;

 h.o=(o)?o.addClass(c.overlayClass).prependTo('body'):F;
 if(ie6){$('html,body').css({height:'100%',width:'100%'});if(o){o=o.css({position:'absolute'})[0];for(var y in {Top:1,Left:1})o.style.setExpression(y.toLowerCase(),"(_=(document.documentElement.scroll"+y+" || document.body.scroll"+y+"))+'px'");}}

 if(c.ajax) {var r=c.target||h.w,u=c.ajax;r=(typeof r == 'string')?$(r,h.w):$(r);u=(u.substr(0,1) == '@')?$(t).attr(u.substring(1)):u;
  r.html(c.ajaxText).load(u,function(){if(c.onLoad)c.onLoad.call(this,h);if(cc)h.w.jqmAddClose($(cc,h.w));e(h);});}
 else if(cc)h.w.jqmAddClose($(cc,h.w));

 if(c.toTop&&h.o)h.w.before('<span id="jqmP'+h.w[0]._jqm+'"></span>').insertAfter(h.o);	
 (c.onShow)?c.onShow(h):h.w.show();e(h);return F;
},
close:function(s){var h=H[s];if(!h.a)return F;h.a=F;
 if(A[0]){A.pop();if(!A[0])L('unbind');}
 if(h.c.toTop&&h.o)$('#jqmP'+h.w[0]._jqm).after(h.w).remove();
 if(h.c.onHide)h.c.onHide(h);else{h.w.hide();if(h.o)h.o.remove();} return F;
},
params:{}};
var s=0,H=$.jqm.hash,A=[],ie6=$.browser.msie&&($.browser.version == "6.0"),F=false,
e=function(h){var i=$('<iframe src="javascript:false;document.write(\'\');" class="jqm"></iframe>').css({opacity:0});if(ie6)if(h.o)h.o.html('<p style="width:100%;height:100%"/>').prepend(i);else if(!$('iframe.jqm',h.w)[0])h.w.prepend(i); f(h);},
f=function(h){try{$(':input:visible',h.w)[0].focus();}catch(_){}},
L=function(t){$(document)[t]("keypress",m)[t]("keydown",m)[t]("mousedown",m);},
m=function(e){var h=H[A[A.length-1]],r=(!$(e.target).parents('.jqmID'+h.s)[0]);if(r)f(h);return !r;},
hs=function(w,t,c){return w.each(function(){var s=this._jqm;$(t).each(function() {
 if(!this[c]){this[c]=[];$(this).click(function(){for(var i in {jqmShow:1,jqmHide:1})for(var s in this[i])if(H[this[i][s]])H[this[i][s]].w[i](this);return F;});}this[c].push(s);});});};
})(jQuery);/*
 * jqDnR - Minimalistic Drag'n'Resize for jQuery.
 *
 * Copyright (c) 2007 Brice Burgess <bhb@iceburg.net>, http://www.iceburg.net
 * Licensed under the MIT License:
 * http://www.opensource.org/licenses/mit-license.php
 * 
 * $Version: 2007.08.19 +r2
 */

(function($){
$.fn.jqDrag=function(h){return i(this,h,'d');};
$.fn.jqResize=function(h,ar){return i(this,h,'r',ar);};
$.jqDnR={
	dnr:{},
	e:0,
	drag:function(v){
 		if(M.k == 'd')E.css({left:M.X+v.pageX-M.pX,top:M.Y+v.pageY-M.pY});
 		else {
			E.css({width:Math.max(v.pageX-M.pX+M.W,0),height:Math.max(v.pageY-M.pY+M.H,0)});
			if(M1){E1.css({width:Math.max(v.pageX-M1.pX+M1.W,0),height:Math.max(v.pageY-M1.pY+M1.H,0)});}
		}
  		return false;
  	},
	stop:function(){
		//E.css('opacity',M.o);
		$(document).unbind('mousemove',J.drag).unbind('mouseup',J.stop);
	}
};
var J=$.jqDnR,M=J.dnr,E=J.e,E1,
i=function(e,h,k,aR){
	return e.each(function(){
		h=(h)?$(h,e):e;
 		h.bind('mousedown',{e:e,k:k},function(v){
 			var d=v.data,p={};E=d.e;E1 = aR ? $(aR) : false;
 			// attempt utilization of dimensions plugin to fix IE issues
 			if(E.css('position') != 'relative'){try{E.position(p);}catch(e){}}
 			M={
 				X:p.left||f('left')||0,
 				Y:p.top||f('top')||0,
 				W:f('width')||E[0].scrollWidth||0,
 				H:f('height')||E[0].scrollHeight||0,
 				pX:v.pageX,
 				pY:v.pageY,
 				k:d.k
 				//o:E.css('opacity')
 			};
			// also resize
			if(E1 && d.k != 'd'){
 				M1={
					X:p.left||f1('left')||0,
					Y:p.top||f1('top')||0,
					W:E1[0].offsetWidth||f1('width')||0,
					H:E1[0].offsetHeight||f1('height')||0,
					pX:v.pageX,
					pY:v.pageY,
					k:d.k
				};
			} else {M1 = false;}			
 			//E.css({opacity:0.8});
			if($("input.hasDatepicker",E[0])[0]) {
			try {$("input.hasDatepicker",E[0]).datepicker('hide');}catch (dpe){}
			}
 			$(document).mousemove($.jqDnR.drag).mouseup($.jqDnR.stop);
 			return false;
 		});
	});
},
f=function(k){return parseInt(E.css(k))||false;};
f1=function(k){	return parseInt(E1.css(k))||false;};
})(jQuery);/*
	The below work is licensed under Creative Commons GNU LGPL License.

	Original work:

	License:     http://creativecommons.org/licenses/LGPL/2.1/
	Author:      Stefan Goessner/2006
	Web:         http://goessner.net/ 

	Modifications made:

	Version:     0.9-p5
	Description: Restructured code, JSLint validated (no strict whitespaces),
	             added handling of empty arrays, empty strings, and int/floats values.
	Author:      Michael Schøler/2008-01-29
	Web:         http://michael.hinnerup.net/blog/2008/01/26/converting-json-to-xml-and-xml-to-json/
	
	Description: json2xml added support to convert functions as CDATA
	             so it will be easy to write characters that cause some problems when convert
	Author:      Tony Tomov
*/

/*global alert */
var xmlJsonClass = {
	// Param "xml": Element or document DOM node.
	// Param "tab": Tab or indent string for pretty output formatting omit or use empty string "" to supress.
	// Returns:     JSON string
	xml2json: function(xml, tab) {
		if (xml.nodeType === 9) {
			// document node
			xml = xml.documentElement;
		}
		var nws = this.removeWhite(xml);
		var obj = this.toObj(nws);
		var json = this.toJson(obj, xml.nodeName, "\t");
		return "{\n" + tab + (tab ? json.replace(/\t/g, tab) : json.replace(/\t|\n/g, "")) + "\n}";
	},

	// Param "o":   JavaScript object
	// Param "tab": tab or indent string for pretty output formatting omit or use empty string "" to supress.
	// Returns:     XML string
	json2xml: function(o, tab) {
		var toXml = function(v, name, ind) {
			var xml = "";
			var i, n;
			if (v instanceof Array) {
				if (v.length === 0) {
					xml += ind + "<"+name+">__EMPTY_ARRAY_</"+name+">\n";
				}
				else {
					for (i = 0, n = v.length; i < n; i += 1) {
						var sXml = ind + toXml(v[i], name, ind+"\t") + "\n";
						xml += sXml;
					}
				}
			}
			else if (typeof(v) === "object") {
				var hasChild = false;
				xml += ind + "<" + name;
				var m;
				for (m in v) if (v.hasOwnProperty(m)) {
					if (m.charAt(0) === "@") {
						xml += " " + m.substr(1) + "=\"" + v[m].toString() + "\"";
					}
					else {
						hasChild = true;
					}
				}
				xml += hasChild ? ">" : "/>";
				if (hasChild) {
					for (m in v) if (v.hasOwnProperty(m)) {
						if (m === "#text") {
							xml += v[m];
						}
						else if (m === "#cdata") {
							xml += "<![CDATA[" + v[m] + "]]>";
						}
						else if (m.charAt(0) !== "@") {
							xml += toXml(v[m], m, ind+"\t");
						}
					}
					xml += (xml.charAt(xml.length - 1) === "\n" ? ind : "") + "</" + name + ">";
				}
			}
			else if (typeof(v) === "function") {
				xml += ind + "<" + name + ">" + "<![CDATA[" + v + "]]>" + "</" + name + ">";
			}
			else {
				if (v.toString() === "\"\"" || v.toString().length === 0) {
					xml += ind + "<" + name + ">__EMPTY_STRING_</" + name + ">";
				} 
				else {
					xml += ind + "<" + name + ">" + v.toString() + "</" + name + ">";
				}
			}
			return xml;
		};
		var xml = "";
		var m;
		for (m in o) if (o.hasOwnProperty(m)) {
			xml += toXml(o[m], m, "");
		}
		return tab ? xml.replace(/\t/g, tab) : xml.replace(/\t|\n/g, "");
	},
	// Internal methods
	toObj: function(xml) {
		var o = {};
		var FuncTest = /function/i;
		if (xml.nodeType === 1) {
			// element node ..
			if (xml.attributes.length) {
				// element with attributes ..
				var i;
				for (i = 0; i < xml.attributes.length; i += 1) {
					o["@" + xml.attributes[i].nodeName] = (xml.attributes[i].nodeValue || "").toString();
				}
			}
			if (xml.firstChild) {
				// element has child nodes ..
				var textChild = 0, cdataChild = 0, hasElementChild = false;
				var n;
				for (n = xml.firstChild; n; n = n.nextSibling) {
					if (n.nodeType === 1) {
						hasElementChild = true;
					}
					else if (n.nodeType === 3 && n.nodeValue.match(/[^ \f\n\r\t\v]/)) {
						// non-whitespace text
						textChild += 1;
					}
					else if (n.nodeType === 4) {
						// cdata section node
						cdataChild += 1;
					}
				}
				if (hasElementChild) {
					if (textChild < 2 && cdataChild < 2) {
						// structured element with evtl. a single text or/and cdata node ..
						this.removeWhite(xml);
						for (n = xml.firstChild; n; n = n.nextSibling) {
							if (n.nodeType === 3) {
								// text node
								o["#text"] = this.escape(n.nodeValue);
							}
							else if (n.nodeType === 4) {
								// cdata node
								if (FuncTest.test(n.nodeValue)) {
									o[n.nodeName] = [o[n.nodeName], n.nodeValue];
								} else {
									o["#cdata"] = this.escape(n.nodeValue);
								}
							}
							else if (o[n.nodeName]) {
								// multiple occurence of element ..
								if (o[n.nodeName] instanceof Array) {
									o[n.nodeName][o[n.nodeName].length] = this.toObj(n);
								}
								else {
									o[n.nodeName] = [o[n.nodeName], this.toObj(n)];
								}
							}
							else {
								// first occurence of element ..
								o[n.nodeName] = this.toObj(n);
							}
						}
					}
					else {
						// mixed content
						if (!xml.attributes.length) {
							o = this.escape(this.innerXml(xml));
						}
						else {
							o["#text"] = this.escape(this.innerXml(xml));
						}
					}
				}
				else if (textChild) {
					// pure text
					if (!xml.attributes.length) {
						o = this.escape(this.innerXml(xml));
						if (o === "__EMPTY_ARRAY_") {
							o = "[]";
						} else if (o === "__EMPTY_STRING_") {
							o = "";
						}
					}
					else {
						o["#text"] = this.escape(this.innerXml(xml));
					}
				}
				else if (cdataChild) {
					// cdata
					if (cdataChild > 1) {
						o = this.escape(this.innerXml(xml));
					}
					else {
						for (n = xml.firstChild; n; n = n.nextSibling) {
							if(FuncTest.test(xml.firstChild.nodeValue)) {
								o = xml.firstChild.nodeValue;
								break;
							} else {
								o["#cdata"] = this.escape(n.nodeValue);
							}
						}
					}
				}
			}
			if (!xml.attributes.length && !xml.firstChild) {
				o = null;
			}
		}
		else if (xml.nodeType === 9) {
			// document.node
			o = this.toObj(xml.documentElement);
		}
		else {
			alert("unhandled node type: " + xml.nodeType);
		}
		return o;
	},
	toJson: function(o, name, ind, wellform) {
		if(wellform === undefined) wellform = true;
		var json = name ? ("\"" + name + "\"") : "", tab = "\t", newline = "\n";
		if(!wellform) {
			tab= ""; newline= "";
		}

		if (o === "[]") {
			json += (name ? ":[]" : "[]");
		}
		else if (o instanceof Array) {
			var n, i, ar=[];
			for (i = 0, n = o.length; i < n; i += 1) {
				ar[i] = this.toJson(o[i], "", ind + tab, wellform);
			}
			json += (name ? ":[" : "[") + (ar.length > 1 ? (newline + ind + tab + ar.join(","+newline + ind + tab) + newline + ind) : ar.join("")) + "]";
		}
		else if (o === null) {
			json += (name && ":") + "null";
		}
		else if (typeof(o) === "object") {
			var arr = [], m;
			for (m in o) {
				if (o.hasOwnProperty(m)) {
					arr[arr.length] = this.toJson(o[m], m, ind + tab, wellform);
			}
		}
			json += (name ? ":{" : "{") + (arr.length > 1 ? (newline + ind + tab + arr.join(","+newline + ind + tab) + newline + ind) : arr.join("")) + "}";
		}
		else if (typeof(o) === "string") {
			/*
			var objRegExp  = /(^-?\d+\.?\d*$)/;
			var FuncTest = /function/i;
			var os = o.toString();
			if (objRegExp.test(os) || FuncTest.test(os) || os==="false" || os==="true") {
				// int or float
				json += (name && ":")  + "\"" +os + "\"";
			} 
			else {
			*/
				json += (name && ":") + "\"" + o.replace(/\\/g,'\\\\').replace(/\"/g,'\\"') + "\"";
			//}
			}
		else {
			json += (name && ":") +  "\"" + o.toString()+ "\"";
		}
		return json;
	},
	innerXml: function(node) {
		var s = "";
		if ("innerHTML" in node) {
			s = node.innerHTML;
		}
		else {
			var asXml = function(n) {
				var s = "", i;
				if (n.nodeType === 1) {
					s += "<" + n.nodeName;
					for (i = 0; i < n.attributes.length; i += 1) {
						s += " " + n.attributes[i].nodeName + "=\"" + (n.attributes[i].nodeValue || "").toString() + "\"";
					}
					if (n.firstChild) {
						s += ">";
						for (var c = n.firstChild; c; c = c.nextSibling) {
							s += asXml(c);
						}
						s += "</" + n.nodeName + ">";
					}
					else {
						s += "/>";
					}
				}
				else if (n.nodeType === 3) {
					s += n.nodeValue;
				}
				else if (n.nodeType === 4) {
					s += "<![CDATA[" + n.nodeValue + "]]>";
				}
				return s;
			};
			for (var c = node.firstChild; c; c = c.nextSibling) {
				s += asXml(c);
			}
		}
		return s;
	},
	escape: function(txt) {
		return txt.replace(/[\\]/g, "\\\\").replace(/[\"]/g, '\\"').replace(/[\n]/g, '\\n').replace(/[\r]/g, '\\r');
	},
	removeWhite: function(e) {
		e.normalize();
		var n;
		for (n = e.firstChild; n; ) {
			if (n.nodeType === 3) {
				// text node
				if (!n.nodeValue.match(/[^ \f\n\r\t\v]/)) {
					// pure whitespace text node
					var nxt = n.nextSibling;
					e.removeChild(n);
					n = nxt;
				}
				else {
					n = n.nextSibling;
				}
			}
			else if (n.nodeType === 1) {
				// element node
				this.removeWhite(n);
				n = n.nextSibling;
			}
			else {
				// any other node
				n = n.nextSibling;
			}
		}
		return e;
	}
};/*
**
 * formatter for values but most of the values if for jqGrid
 * Some of this was inspired and based on how YUI does the table datagrid but in jQuery fashion
 * we are trying to keep it as light as possible
 * Joshua Burnett josh@9ci.com	
 * http://www.greenbill.com
 *
 * Changes from Tony Tomov tony@trirand.com
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl-2.0.html
 * 
**/

;(function($) {
	$.fmatter = {};
	//opts can be id:row id for the row, rowdata:the data for the row, colmodel:the column model for this column
	//example {id:1234,}
	$.extend($.fmatter,{
		isBoolean : function(o) {
			return typeof o === 'boolean';
		},
		isObject : function(o) {
			return (o && (typeof o === 'object' || $.isFunction(o))) || false;
		},
		isString : function(o) {
			return typeof o === 'string';
		},
		isNumber : function(o) {
			return typeof o === 'number' && isFinite(o);
		},
		isNull : function(o) {
			return o === null;
		},
		isUndefined : function(o) {
			return typeof o === 'undefined';
		},
		isValue : function (o) {
			return (this.isObject(o) || this.isString(o) || this.isNumber(o) || this.isBoolean(o));
		},
		isEmpty : function(o) {
			if(!this.isString(o) && this.isValue(o)) {
				return false;
			}else if (!this.isValue(o)){
				return true;
			}
			o = $.trim(o).replace(/\&nbsp\;/ig,'').replace(/\&#160\;/ig,'');
			return o==="";	
		}
	});
	$.fn.fmatter = function(formatType, cellval, opts, rwd, act) {
		// build main options before element iteration
		var v=cellval;
		opts = $.extend({}, $.jgrid.formatter, opts);

		if ($.fn.fmatter[formatType]){
			v = $.fn.fmatter[formatType](cellval, opts, rwd, act);
		}
		return v;
	};
	$.fmatter.util = {
		// Taken from YAHOO utils
		NumberFormat : function(nData,opts) {
			if(!$.fmatter.isNumber(nData)) {
				nData *= 1;
			}
			if($.fmatter.isNumber(nData)) {
				var bNegative = (nData < 0);
				var sOutput = nData + "";
				var sDecimalSeparator = (opts.decimalSeparator) ? opts.decimalSeparator : ".";
				var nDotIndex;
				if($.fmatter.isNumber(opts.decimalPlaces)) {
					// Round to the correct decimal place
					var nDecimalPlaces = opts.decimalPlaces;
					var nDecimal = Math.pow(10, nDecimalPlaces);
					sOutput = Math.round(nData*nDecimal)/nDecimal + "";
					nDotIndex = sOutput.lastIndexOf(".");
					if(nDecimalPlaces > 0) {
					// Add the decimal separator
						if(nDotIndex < 0) {
							sOutput += sDecimalSeparator;
							nDotIndex = sOutput.length-1;
						}
						// Replace the "."
						else if(sDecimalSeparator !== "."){
							sOutput = sOutput.replace(".",sDecimalSeparator);
						}
					// Add missing zeros
						while((sOutput.length - 1 - nDotIndex) < nDecimalPlaces) {
							sOutput += "0";
						}
					}
				}
				if(opts.thousandsSeparator) {
					var sThousandsSeparator = opts.thousandsSeparator;
					nDotIndex = sOutput.lastIndexOf(sDecimalSeparator);
					nDotIndex = (nDotIndex > -1) ? nDotIndex : sOutput.length;
					var sNewOutput = sOutput.substring(nDotIndex);
					var nCount = -1;
					for (var i=nDotIndex; i>0; i--) {
						nCount++;
						if ((nCount%3 === 0) && (i !== nDotIndex) && (!bNegative || (i > 1))) {
							sNewOutput = sThousandsSeparator + sNewOutput;
						}
						sNewOutput = sOutput.charAt(i-1) + sNewOutput;
					}
					sOutput = sNewOutput;
				}
				// Prepend prefix
				sOutput = (opts.prefix) ? opts.prefix + sOutput : sOutput;
				// Append suffix
				sOutput = (opts.suffix) ? sOutput + opts.suffix : sOutput;
				return sOutput;
				
			} else {
				return nData;
			}
		},
		// Tony Tomov
		// PHP implementation. Sorry not all options are supported.
		// Feel free to add them if you want
		DateFormat : function (format, date, newformat, opts)  {
			var	token = /\\.|[dDjlNSwzWFmMntLoYyaABgGhHisueIOPTZcrU]/g,
			timezone = /\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g,
			timezoneClip = /[^-+\dA-Z]/g,
			msDateRegExp = new RegExp("^\/Date\\((([-+])?[0-9]+)(([-+])([0-9]{2})([0-9]{2}))?\\)\/$"),
			msMatch = ((typeof date === 'string') ? date.match(msDateRegExp): null),
			pad = function (value, length) {
				value = String(value);
				length = parseInt(length,10) || 2;
				while (value.length < length)  { value = '0' + value; }
				return value;
			},
			ts = {m : 1, d : 1, y : 1970, h : 0, i : 0, s : 0, u:0},
			timestamp=0, dM, k,hl,
			dateFormat=["i18n"];
			// Internationalization strings
			dateFormat.i18n = {
				dayNames: opts.dayNames,
				monthNames: opts.monthNames
			};
			if( format in opts.masks ) { format = opts.masks[format]; }
			if(date.constructor === Number) {
				//Unix timestamp
				if(String(format).toLowerCase() == "u") {
					date = date*1000;
				}
			    timestamp = new Date(date);
			} else if(date.constructor === Date) {
				timestamp = date;
				// Microsoft date format support
			} else if( msMatch !== null ) {
				timestamp = new Date(parseInt(msMatch[1], 10));
				if (msMatch[3]) {
					var offset = Number(msMatch[5]) * 60 + Number(msMatch[6]);
					offset *= ((msMatch[4] == '-') ? 1 : -1);
					offset -= timestamp.getTimezoneOffset();
					timestamp.setTime(Number(Number(timestamp) + (offset * 60 * 1000)));
				}
			} else {
				date = String(date).split(/[\\\/:_;.,\t\T\s-]/);
				format = format.split(/[\\\/:_;.,\t\T\s-]/);
				// parsing for month names
				for(k=0,hl=format.length;k<hl;k++){
					if(format[k] == 'M') {
						dM = $.inArray(date[k],dateFormat.i18n.monthNames);
						if(dM !== -1 && dM < 12){date[k] = dM+1;}
					}
					if(format[k] == 'F') {
						dM = $.inArray(date[k],dateFormat.i18n.monthNames);
						if(dM !== -1 && dM > 11){date[k] = dM+1-12;}
					}
					if(date[k]) {
						ts[format[k].toLowerCase()] = parseInt(date[k],10);
					}
				}
				if(ts.f) {ts.m = ts.f;}
				if( ts.m === 0 && ts.y === 0 && ts.d === 0) {
					return "&#160;" ;
				}
				ts.m = parseInt(ts.m,10)-1;
				var ty = ts.y;
				if (ty >= 70 && ty <= 99) {ts.y = 1900+ts.y;}
				else if (ty >=0 && ty <=69) {ts.y= 2000+ts.y;}
				timestamp = new Date(ts.y, ts.m, ts.d, ts.h, ts.i, ts.s, ts.u);
			}
			
			if( newformat in opts.masks )  {
				newformat = opts.masks[newformat];
			} else if ( !newformat ) {
				newformat = 'Y-m-d';
			}
			var 
				G = timestamp.getHours(),
				i = timestamp.getMinutes(),
				j = timestamp.getDate(),
				n = timestamp.getMonth() + 1,
				o = timestamp.getTimezoneOffset(),
				s = timestamp.getSeconds(),
				u = timestamp.getMilliseconds(),
				w = timestamp.getDay(),
				Y = timestamp.getFullYear(),
				N = (w + 6) % 7 + 1,
				z = (new Date(Y, n - 1, j) - new Date(Y, 0, 1)) / 86400000,
				flags = {
					// Day
					d: pad(j),
					D: dateFormat.i18n.dayNames[w],
					j: j,
					l: dateFormat.i18n.dayNames[w + 7],
					N: N,
					S: opts.S(j),
					//j < 11 || j > 13 ? ['st', 'nd', 'rd', 'th'][Math.min((j - 1) % 10, 3)] : 'th',
					w: w,
					z: z,
					// Week
					W: N < 5 ? Math.floor((z + N - 1) / 7) + 1 : Math.floor((z + N - 1) / 7) || ((new Date(Y - 1, 0, 1).getDay() + 6) % 7 < 4 ? 53 : 52),
					// Month
					F: dateFormat.i18n.monthNames[n - 1 + 12],
					m: pad(n),
					M: dateFormat.i18n.monthNames[n - 1],
					n: n,
					t: '?',
					// Year
					L: '?',
					o: '?',
					Y: Y,
					y: String(Y).substring(2),
					// Time
					a: G < 12 ? opts.AmPm[0] : opts.AmPm[1],
					A: G < 12 ? opts.AmPm[2] : opts.AmPm[3],
					B: '?',
					g: G % 12 || 12,
					G: G,
					h: pad(G % 12 || 12),
					H: pad(G),
					i: pad(i),
					s: pad(s),
					u: u,
					// Timezone
					e: '?',
					I: '?',
					O: (o > 0 ? "-" : "+") + pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4),
					P: '?',
					T: (String(timestamp).match(timezone) || [""]).pop().replace(timezoneClip, ""),
					Z: '?',
					// Full Date/Time
					c: '?',
					r: '?',
					U: Math.floor(timestamp / 1000)
				};	
			return newformat.replace(token, function ($0) {
				return $0 in flags ? flags[$0] : $0.substring(1);
			});			
		}
	};
	$.fn.fmatter.defaultFormat = function(cellval, opts) {
		return ($.fmatter.isValue(cellval) && cellval!=="" ) ?  cellval : opts.defaultValue ? opts.defaultValue : "&#160;";
	};
	$.fn.fmatter.email = function(cellval, opts) {
		if(!$.fmatter.isEmpty(cellval)) {
			return "<a href=\"mailto:" + cellval + "\">" + cellval + "</a>";
		}else {
			return $.fn.fmatter.defaultFormat(cellval,opts );
		}
	};
	$.fn.fmatter.checkbox =function(cval, opts) {
		var op = $.extend({},opts.checkbox), ds;
		if(!$.fmatter.isUndefined(opts.colModel.formatoptions)) {
			op = $.extend({},op,opts.colModel.formatoptions);
		}
		if(op.disabled===true) {ds = "disabled=\"disabled\"";} else {ds="";}
		if($.fmatter.isEmpty(cval) || $.fmatter.isUndefined(cval) ) {cval = $.fn.fmatter.defaultFormat(cval,op);}
		cval=cval+"";cval=cval.toLowerCase();
		var bchk = cval.search(/(false|0|no|off)/i)<0 ? " checked='checked' " : "";
		return "<input type=\"checkbox\" " + bchk  + " value=\""+ cval+"\" offval=\"no\" "+ds+ "/>";
	};
	$.fn.fmatter.link = function(cellval, opts) {
		var op = {target:opts.target};
		var target = "";
		if(!$.fmatter.isUndefined(opts.colModel.formatoptions)) {
			op = $.extend({},op,opts.colModel.formatoptions);
		}
		if(op.target) {target = 'target=' + op.target;}
		if(!$.fmatter.isEmpty(cellval)) {
			return "<a "+target+" href=\"" + cellval + "\">" + cellval + "</a>";
		}else {
			return $.fn.fmatter.defaultFormat(cellval,opts);
		}
	};
	$.fn.fmatter.showlink = function(cellval, opts) {
		var op = {baseLinkUrl: opts.baseLinkUrl,showAction:opts.showAction, addParam: opts.addParam || "", target: opts.target, idName: opts.idName},
		target = "", idUrl;
		if(!$.fmatter.isUndefined(opts.colModel.formatoptions)) {
			op = $.extend({},op,opts.colModel.formatoptions);
		}
		if(op.target) {target = 'target=' + op.target;}
		idUrl = op.baseLinkUrl+op.showAction + '?'+ op.idName+'='+opts.rowId+op.addParam;
		if($.fmatter.isString(cellval) || $.fmatter.isNumber(cellval)) {	//add this one even if its blank string
			return "<a "+target+" href=\"" + idUrl + "\">" + cellval + "</a>";
		}else {
			return $.fn.fmatter.defaultFormat(cellval,opts);
		}
	};
	$.fn.fmatter.integer = function(cellval, opts) {
		var op = $.extend({},opts.integer);
		if(!$.fmatter.isUndefined(opts.colModel.formatoptions)) {
			op = $.extend({},op,opts.colModel.formatoptions);
		}
		if($.fmatter.isEmpty(cellval)) {
			return op.defaultValue;
		}
		return $.fmatter.util.NumberFormat(cellval,op);
	};
	$.fn.fmatter.number = function (cellval, opts) {
		var op = $.extend({},opts.number);
		if(!$.fmatter.isUndefined(opts.colModel.formatoptions)) {
			op = $.extend({},op,opts.colModel.formatoptions);
		}
		if($.fmatter.isEmpty(cellval)) {
			return op.defaultValue;
		}
		return $.fmatter.util.NumberFormat(cellval,op);
	};
	$.fn.fmatter.currency = function (cellval, opts) {
		var op = $.extend({},opts.currency);
		if(!$.fmatter.isUndefined(opts.colModel.formatoptions)) {
			op = $.extend({},op,opts.colModel.formatoptions);
		}
		if($.fmatter.isEmpty(cellval)) {
			return op.defaultValue;
		}
		return $.fmatter.util.NumberFormat(cellval,op);
	};
	$.fn.fmatter.date = function (cellval, opts, rwd, act) {
		var op = $.extend({},opts.date);
		if(!$.fmatter.isUndefined(opts.colModel.formatoptions)) {
			op = $.extend({},op,opts.colModel.formatoptions);
		}
		if(!op.reformatAfterEdit && act=='edit'){
			return $.fn.fmatter.defaultFormat(cellval, opts);
		} else if(!$.fmatter.isEmpty(cellval)) {
			return  $.fmatter.util.DateFormat(op.srcformat,cellval,op.newformat,op);
		} else {
			return $.fn.fmatter.defaultFormat(cellval, opts);
		}
	};
	$.fn.fmatter.select = function (cellval,opts, rwd, act) {
		// jqGrid specific
		cellval = cellval + "";
		var oSelect = false, ret=[];
		if(!$.fmatter.isUndefined(opts.colModel.formatoptions)){
			oSelect= opts.colModel.formatoptions.value;
		} else if(!$.fmatter.isUndefined(opts.colModel.editoptions)){
			oSelect= opts.colModel.editoptions.value;
		}
		if (oSelect) {
			var	msl =  opts.colModel.editoptions.multiple === true ? true : false,
			scell = [], sv;
			if(msl) {scell = cellval.split(",");scell = $.map(scell,function(n){return $.trim(n);});}
			if ($.fmatter.isString(oSelect)) {
				// mybe here we can use some caching with care ????
				var so = oSelect.split(";"), j=0;
				for(var i=0; i<so.length;i++){
					sv = so[i].split(":");
					if(sv.length > 2 ) {
						sv[1] = jQuery.map(sv,function(n,i){if(i>0) {return n;}}).join(":");
					}
					if(msl) {
						if(jQuery.inArray(sv[0],scell)>-1) {
							ret[j] = sv[1];
							j++;
						}
					} else if($.trim(sv[0])==$.trim(cellval)) {
						ret[0] = sv[1];
						break;
					}
				}
			} else if($.fmatter.isObject(oSelect)) {
				// this is quicker
				if(msl) {
					ret = jQuery.map(scell, function(n, i){
						return oSelect[n];
					});
				} else {
					ret[0] = oSelect[cellval] || "";
				}
			}
		}
		cellval = ret.join(", ");
		return  cellval === "" ? $.fn.fmatter.defaultFormat(cellval,opts) : cellval;
	};
	$.fn.fmatter.rowactions = function(rid,gid,act,pos) {
		var op ={
			keys:false,
			onEdit : null, 
			onSuccess: null, 
			afterSave:null,
			onError: null,
			afterRestore: null,
			extraparam: {oper:'edit'},
			url: null,
			delOptions: {},
			editOptions : {}
		},
		cm = $('#'+gid)[0].p.colModel[pos];
		if(!$.fmatter.isUndefined(cm.formatoptions)) {
			op = $.extend(op,cm.formatoptions);
		}
		if( !$.fmatter.isUndefined($('#'+gid)[0].p.editOptions) ) {
			op.editOptions = $('#'+gid)[0].p.editOptions;
		}
		if( !$.fmatter.isUndefined($('#'+gid)[0].p.delOptions) ) {
			op.delOptions = $('#'+gid)[0].p.delOptions;
		}
		var saverow = function( rowid)	{
			if(op.afterSave) op.afterSave(rowid);
			$("tr#"+rid+" div.ui-inline-edit, "+"tr#"+rid+" div.ui-inline-del","#"+gid).show();
			$("tr#"+rid+" div.ui-inline-save, "+"tr#"+rid+" div.ui-inline-cancel","#"+gid).hide();
		},
		restorerow = function( rowid)	{
			if(op.afterRestore) op.afterRestore(rowid);
			$("tr#"+rid+" div.ui-inline-edit, "+"tr#"+rid+" div.ui-inline-del","#"+gid).show();
			$("tr#"+rid+" div.ui-inline-save, "+"tr#"+rid+" div.ui-inline-cancel","#"+gid).hide();
		};

		switch(act)
		{
			case 'edit':
				$('#'+gid).jqGrid('editRow',rid, op.keys, op.onEdit, op.onSuccess, op.url, op.extraparam, saverow, op.onError,restorerow);
				$("tr#"+rid+" div.ui-inline-edit, "+"tr#"+rid+" div.ui-inline-del","#"+gid).hide();
				$("tr#"+rid+" div.ui-inline-save, "+"tr#"+rid+" div.ui-inline-cancel","#"+gid).show();
				break;
			case 'save':
				if ( $('#'+gid).jqGrid('saveRow',rid,  op.onSuccess,op.url, op.extraparam, saverow, op.onError,restorerow) ) {
				$("tr#"+rid+" div.ui-inline-edit, "+"tr#"+rid+" div.ui-inline-del","#"+gid).show();
				$("tr#"+rid+" div.ui-inline-save, "+"tr#"+rid+" div.ui-inline-cancel","#"+gid).hide();
				}
				break;
			case 'cancel' :
				$('#'+gid).jqGrid('restoreRow',rid, restorerow);
				$("tr#"+rid+" div.ui-inline-edit, "+"tr#"+rid+" div.ui-inline-del","#"+gid).show();
				$("tr#"+rid+" div.ui-inline-save, "+"tr#"+rid+" div.ui-inline-cancel","#"+gid).hide();
				break;
			case 'del':
				$('#'+gid).jqGrid('delGridRow',rid, op.delOptions);
				break;
			case 'formedit':
				$('#'+gid).jqGrid('setSelection',rid);
				$('#'+gid).jqGrid('editGridRow',rid, op.editOptions);
				break;
		}
	};
	$.fn.fmatter.actions = function(cellval,opts, rwd) {
		var op ={keys:false, editbutton:true, delbutton:true, editformbutton: false};
		if(!$.fmatter.isUndefined(opts.colModel.formatoptions)) {
			op = $.extend(op,opts.colModel.formatoptions);
		}
		var rowid = opts.rowId, str="",ocl;
		if(typeof(rowid) =='undefined' || $.fmatter.isEmpty(rowid)) {return "";}
		if(op.editformbutton){
			ocl = "onclick=$.fn.fmatter.rowactions('"+rowid+"','"+opts.gid+"','formedit',"+opts.pos+"); onmouseover=jQuery(this).addClass('ui-state-hover'); onmouseout=jQuery(this).removeClass('ui-state-hover'); "
			str =str+ "<div style='margin-left:8px;'><div title='"+$.jgrid.nav.edittitle+"' style='float:left;cursor:pointer;' class='ui-pg-div ui-inline-edit' "+ocl+"><span class='ui-icon ui-icon-pencil'></span></div>";
		} else 	if(op.editbutton){
			ocl = "onclick=$.fn.fmatter.rowactions('"+rowid+"','"+opts.gid+"','edit',"+opts.pos+"); onmouseover=jQuery(this).addClass('ui-state-hover'); onmouseout=jQuery(this).removeClass('ui-state-hover') ";
			str =str+ "<div style='margin-left:8px;'><div title='"+$.jgrid.nav.edittitle+"' style='float:left;cursor:pointer;' class='ui-pg-div ui-inline-edit' "+ocl+"><span class='ui-icon ui-icon-pencil'></span></div>";
		}
		if(op.delbutton) {
			ocl = "onclick=$.fn.fmatter.rowactions('"+rowid+"','"+opts.gid+"','del',"+opts.pos+"); onmouseover=jQuery(this).addClass('ui-state-hover'); onmouseout=jQuery(this).removeClass('ui-state-hover'); ";
			str = str+"<div title='"+$.jgrid.nav.deltitle+"' style='float:left;margin-left:5px;' class='ui-pg-div ui-inline-del' "+ocl+"><span class='ui-icon ui-icon-trash'></span></div>";
		}
		ocl = "onclick=$.fn.fmatter.rowactions('"+rowid+"','"+opts.gid+"','save',"+opts.pos+"); onmouseover=jQuery(this).addClass('ui-state-hover'); onmouseout=jQuery(this).removeClass('ui-state-hover'); ";
		str = str+"<div title='"+$.jgrid.edit.bSubmit+"' style='float:left;display:none' class='ui-pg-div ui-inline-save'><span class='ui-icon ui-icon-disk' "+ocl+"></span></div>";
		ocl = "onclick=$.fn.fmatter.rowactions('"+rowid+"','"+opts.gid+"','cancel',"+opts.pos+"); onmouseover=jQuery(this).addClass('ui-state-hover'); onmouseout=jQuery(this).removeClass('ui-state-hover'); ";
		str = str+"<div title='"+$.jgrid.edit.bCancel+"' style='float:left;display:none;margin-left:5px;' class='ui-pg-div ui-inline-cancel'><span class='ui-icon ui-icon-cancel' "+ocl+"></span></div></div>";
		return str;
	};
	$.unformat = function (cellval,options,pos,cnt) {
		// specific for jqGrid only
		var ret, formatType = options.colModel.formatter,
		op =options.colModel.formatoptions || {}, sep,
		re = /([\.\*\_\'\(\)\{\}\+\?\\])/g,
		unformatFunc = options.colModel.unformat||($.fn.fmatter[formatType] && $.fn.fmatter[formatType].unformat);
		if(typeof unformatFunc !== 'undefined' && $.isFunction(unformatFunc) ) {
			ret = unformatFunc($(cellval).text(), options, cellval);
		} else if(!$.fmatter.isUndefined(formatType) && $.fmatter.isString(formatType) ) {
			var opts = $.jgrid.formatter || {}, stripTag;
			switch(formatType) {
				case 'integer' :
					op = $.extend({},opts.integer,op);
					sep = op.thousandsSeparator.replace(re,"\\$1");
					stripTag = new RegExp(sep, "g");
					ret = $(cellval).text().replace(stripTag,'');
					break;
				case 'number' :
					op = $.extend({},opts.number,op);
					sep = op.thousandsSeparator.replace(re,"\\$1");
					stripTag = new RegExp(sep, "g");
					ret = $(cellval).text().replace(stripTag,"").replace(op.decimalSeparator,'.');
					break;
				case 'currency':
					op = $.extend({},opts.currency,op);
					sep = op.thousandsSeparator.replace(re,"\\$1");
					stripTag = new RegExp(sep, "g");
					ret = $(cellval).text().replace(stripTag,'').replace(op.decimalSeparator,'.').replace(op.prefix,'').replace(op.suffix,'');
					break;
				case 'checkbox':
					var cbv = (options.colModel.editoptions) ? options.colModel.editoptions.value.split(":") : ["Yes","No"];
					ret = $('input',cellval).attr("checked") ? cbv[0] : cbv[1];
					break;
				case 'select' :
					ret = $.unformat.select(cellval,options,pos,cnt);
					break;
				case 'actions':
					return "";
				default:
					ret= $(cellval).text();
			}
		}
		return ret ? ret : cnt===true ? $(cellval).text() : $.jgrid.htmlDecode($(cellval).html());
	};
	$.unformat.select = function (cellval,options,pos,cnt) {
		// Spacial case when we have local data and perform a sort
		// cnt is set to true only in sortDataArray
		var ret = [];
		var cell = $(cellval).text();
		if(cnt===true) {return cell;}
		var op = $.extend({},options.colModel.editoptions);
		if(op.value){
			var oSelect = op.value,
			msl =  op.multiple === true ? true : false,
			scell = [], sv;
			if(msl) {scell = cell.split(",");scell = $.map(scell,function(n){return $.trim(n);});}
			if ($.fmatter.isString(oSelect)) {
				var so = oSelect.split(";"), j=0;
				for(var i=0; i<so.length;i++){
					sv = so[i].split(":");
					if(sv.length > 2 ) {
						sv[1] = jQuery.map(sv,function(n,i){if(i>0) {return n;}}).join(":");
					}					
					if(msl) {
						if(jQuery.inArray(sv[1],scell)>-1) {
							ret[j] = sv[0];
							j++;
						}
					} else if($.trim(sv[1])==$.trim(cell)) {
						ret[0] = sv[0];
						break;
					}
				}
			} else if($.fmatter.isObject(oSelect) || $.isArray(oSelect) ){
				if(!msl) {scell[0] =  cell;}
				ret = jQuery.map(scell, function(n){
					var rv;
					$.each(oSelect, function(i,val){
						if (val == n) {
							rv = i;
							return false;
						}
					});
					if( typeof(rv) != 'undefined' ) {return rv;}
				});
			}
			return ret.join(", ");
		} else {
			return cell || "";
		}
	};
	$.unformat.date = function (cellval, opts) {
		var op = $.jgrid.formatter.date || {};
		if(!$.fmatter.isUndefined(opts.formatoptions)) {
			op = $.extend({},op,opts.formatoptions);
		}		
		if(!$.fmatter.isEmpty(cellval)) {
			return  $.fmatter.util.DateFormat(op.newformat,cellval,op.srcformat,op);
		} else {
			return $.fn.fmatter.defaultFormat(cellval, opts);
		}
	};
})(jQuery);;(function($){
/*
 * jqGrid common function
 * Tony Tomov tony@trirand.com
 * http://trirand.com/blog/ 
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl-2.0.html
*/
/*global jQuery, $ */

$.extend($.jgrid,{
// Modal functions
	showModal : function(h) {
		h.w.show();
	},
	closeModal : function(h) {
		h.w.hide().attr("aria-hidden","true");
		if(h.o) {h.o.remove();}
	},
	hideModal : function (selector,o) {
		o = $.extend({jqm : true, gb :''}, o || {});
		if(o.onClose) {
			var oncret =  o.onClose(selector);
			if (typeof oncret == 'boolean'  && !oncret ) { return; }
		}
		if ($.fn.jqm && o.jqm === true) {
			$(selector).attr("aria-hidden","true").jqmHide();
		} else {
			if(o.gb !== '') {
				try {$(".jqgrid-overlay:first",o.gb).hide();} catch (e){}
			}
			$(selector).hide().attr("aria-hidden","true");
		}
	},
//Helper functions
	findPos : function(obj) {
		var curleft = 0, curtop = 0;
		if (obj.offsetParent) {
			do {
				curleft += obj.offsetLeft;
				curtop += obj.offsetTop;
			} while (obj = obj.offsetParent);
			//do not change obj == obj.offsetParent
		}
		return [curleft,curtop];
	},
	createModal : function(aIDs, content, p, insertSelector, posSelector, appendsel, css) {		
		var mw  = document.createElement('div'), rtlsup, self = this;
		css = $.extend({}, css || {});
		rtlsup = $(p.gbox).attr("dir") == "rtl" ? true : false;
		mw.className= "ui-widget ui-widget-content ui-corner-all ui-jqdialog";
		mw.id = aIDs.themodal;
		var mh = document.createElement('div');
		mh.className = "ui-jqdialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix";
		mh.id = aIDs.modalhead;
		$(mh).append("<span class='ui-jqdialog-title'>"+p.caption+"</span>");
		var ahr= $("<a href='javascript:void(0)' class='ui-jqdialog-titlebar-close ui-corner-all'></a>")
		.hover(function(){ahr.addClass('ui-state-hover');},
			   function(){ahr.removeClass('ui-state-hover');})
		.append("<span class='ui-icon ui-icon-closethick'></span>");
		$(mh).append(ahr);
		if(rtlsup) {
			mw.dir = "rtl";
			$(".ui-jqdialog-title",mh).css("float","right");
			$(".ui-jqdialog-titlebar-close",mh).css("left",0.3+"em");
		} else {
			mw.dir = "ltr";
			$(".ui-jqdialog-title",mh).css("float","left");
			$(".ui-jqdialog-titlebar-close",mh).css("right",0.3+"em");
		}
		var mc = document.createElement('div');
		$(mc).addClass("ui-jqdialog-content ui-widget-content").attr("id",aIDs.modalcontent);
		$(mc).append(content);
		mw.appendChild(mc);
		$(mw).prepend(mh);
		if(appendsel===true) { $('body').append(mw); } //append as first child in body -for alert dialog
		else if (typeof appendsel == "string")
			$(appendsel).append(mw);
		else {$(mw).insertBefore(insertSelector);}
		$(mw).css(css);
		if(typeof p.jqModal === 'undefined') {p.jqModal = true;} // internal use
		var coord = {};
		if ( $.fn.jqm && p.jqModal === true) {
			if(p.left ===0 && p.top===0 && p.overlay) {
				var pos = [];
				pos = this.findPos(posSelector);
				p.left = pos[0] + 4;
				p.top = pos[1] + 4;
			}
			coord.top = p.top+"px";
			coord.left = p.left;
		} else if(p.left !==0 || p.top!==0) {
			coord.left = p.left;
			coord.top = p.top+"px";
		}
		$("a.ui-jqdialog-titlebar-close",mh).click(function(e){
			var oncm = $("#"+aIDs.themodal).data("onClose") || p.onClose;
			var gboxclose = $("#"+aIDs.themodal).data("gbox") || p.gbox;
			self.hideModal("#"+aIDs.themodal,{gb:gboxclose,jqm:p.jqModal,onClose:oncm});
			return false;
		});
		if (p.width === 0 || !p.width) {p.width = 300;}
		if(p.height === 0 || !p.height) {p.height =200;}
		if(!p.zIndex) {
			var parentZ = $(insertSelector).parents("*[role=dialog]").filter(':first').css("z-index");
			if(parentZ) {
				p.zIndex = parseInt(parentZ,10)+1;
			} else {
				p.zIndex = 950;
		}
		}
		var rtlt = 0;
		if( rtlsup && coord.left && !appendsel) {
			rtlt = $(p.gbox).width()- (!isNaN(p.width) ? parseInt(p.width,10) :0) - 8; // to do
		// just in case
			coord.left = parseInt(coord.left,10) + parseInt(rtlt,10);
		}
		if(coord.left) { coord.left += "px"; }
		$(mw).css($.extend({
			width: isNaN(p.width) ? "auto": p.width+"px",
			height:isNaN(p.height) ? "auto" : p.height + "px",
			zIndex:p.zIndex,
			overflow: 'hidden'
		},coord))
		.attr({tabIndex: "-1","role":"dialog","aria-labelledby":aIDs.modalhead,"aria-hidden":"true"});
		if(typeof p.drag == 'undefined') { p.drag=true;}
		if(typeof p.resize == 'undefined') {p.resize=true;}
		if (p.drag) {
			$(mh).css('cursor','move');
			if($.fn.jqDrag) {
				$(mw).jqDrag(mh);
			} else {
				try {
					$(mw).draggable({handle: $("#"+mh.id)});
				} catch (e) {}
			}
		}
		if(p.resize) {
			if($.fn.jqResize) {
				$(mw).append("<div class='jqResize ui-resizable-handle ui-resizable-se ui-icon ui-icon-gripsmall-diagonal-se ui-icon-grip-diagonal-se'></div>");
				$("#"+aIDs.themodal).jqResize(".jqResize",aIDs.scrollelm ? "#"+aIDs.scrollelm : false);
			} else {
				try {
					$(mw).resizable({handles: 'se, sw',alsoResize: aIDs.scrollelm ? "#"+aIDs.scrollelm : false});
				} catch (r) {}
			}
		}
		if(p.closeOnEscape === true){
			$(mw).keydown( function( e ) {
				if( e.which == 27 ) {
					var cone = $("#"+aIDs.themodal).data("onClose") || p.onClose;
					self.hideModal(this,{gb:p.gbox,jqm:p.jqModal,onClose: cone});
				}
			});
		}
	},
	viewModal : function (selector,o){
		o = $.extend({
			toTop: true,
			overlay: 10,
			modal: false,
			onShow: this.showModal,
			onHide: this.closeModal,
			gbox: '',
			jqm : true,
			jqM : true
		}, o || {});
		if ($.fn.jqm && o.jqm === true) {
			if(o.jqM) { $(selector).attr("aria-hidden","false").jqm(o).jqmShow(); }
			else {$(selector).attr("aria-hidden","false").jqmShow();}
		} else {
			if(o.gbox !== '') {
				$(".jqgrid-overlay:first",o.gbox).show();
				$(selector).data("gbox",o.gbox);
			}
			$(selector).show().attr("aria-hidden","false");
			try{$(':input:visible',selector)[0].focus();}catch(_){}
		}
	},

	info_dialog : function(caption, content,c_b, modalopt) {
		var mopt = {
			width:290,
			height:'auto',
			dataheight: 'auto',
			drag: true,
			resize: false,
			caption:"<b>"+caption+"</b>",
			left:250,
			top:170,
			zIndex : 1000,
			jqModal : true,
			modal : false,
			closeOnEscape : true,
			align: 'center',
			buttonalign : 'center',
			buttons : []
		// {text:'textbutt', id:"buttid", onClick : function(){...}}
		// if the id is not provided we set it like info_button_+ the index in the array - i.e info_button_0,info_button_1...
		};
		$.extend(mopt,modalopt || {});
		var jm = mopt.jqModal, self = this;
		if($.fn.jqm && !jm) { jm = false; }
		// in case there is no jqModal
		var buttstr ="";
		if(mopt.buttons.length > 0) {
			for(var i=0;i<mopt.buttons.length;i++) {
				if(typeof mopt.buttons[i].id == "undefined") { mopt.buttons[i].id = "info_button_"+i; }
				buttstr += "<a href='javascript:void(0)' id='"+mopt.buttons[i].id+"' class='fm-button ui-state-default ui-corner-all'>"+mopt.buttons[i].text+"</a>";
			}
		}
		var dh = isNaN(mopt.dataheight) ? mopt.dataheight : mopt.dataheight+"px",
		cn = "text-align:"+mopt.align+";";
		var cnt = "<div id='info_id'>";
		cnt += "<div id='infocnt' style='margin:0px;padding-bottom:1em;width:100%;overflow:auto;position:relative;height:"+dh+";"+cn+"'>"+content+"</div>";
		cnt += c_b ? "<div class='ui-widget-content ui-helper-clearfix' style='text-align:"+mopt.buttonalign+";padding-bottom:0.8em;padding-top:0.5em;background-image: none;border-width: 1px 0 0 0;'><a href='javascript:void(0)' id='closedialog' class='fm-button ui-state-default ui-corner-all'>"+c_b+"</a>"+buttstr+"</div>" :
			buttstr !== ""  ? "<div class='ui-widget-content ui-helper-clearfix' style='text-align:"+mopt.buttonalign+";padding-bottom:0.8em;padding-top:0.5em;background-image: none;border-width: 1px 0 0 0;'>"+buttstr+"</div>" : "";
		cnt += "</div>";

		try {
			if($("#info_dialog").attr("aria-hidden") == "false") {
				this.hideModal("#info_dialog",{jqm:jm});
			}
			$("#info_dialog").remove();
		} catch (e){}
		this.createModal({
			themodal:'info_dialog',
			modalhead:'info_head',
			modalcontent:'info_content',
			scrollelm: 'infocnt'},
			cnt,
			mopt,
			'','',true
		);
		// attach onclick after inserting into the dom
		if(buttstr) {
			$.each(mopt.buttons,function(i){
				$("#"+this.id,"#info_id").bind('click',function(){mopt.buttons[i].onClick.call($("#info_dialog")); return false;});
			});
		}
		$("#closedialog", "#info_id").click(function(e){
			self.hideModal("#info_dialog",{jqm:jm});
			return false;
		});
		$(".fm-button","#info_dialog").hover(
			function(){$(this).addClass('ui-state-hover');},
			function(){$(this).removeClass('ui-state-hover');}
		);
		if($.isFunction(mopt.beforeOpen) ) { mopt.beforeOpen(); }
		this.viewModal("#info_dialog",{
			onHide: function(h) {
				h.w.hide().remove();
				if(h.o) { h.o.remove(); }
			},
			modal :mopt.modal,
			jqm:jm
		});
		if($.isFunction(mopt.afterOpen) ) { mopt.afterOpen(); }
		try{ $("#info_dialog").focus();} catch (m){}
	},
// Form Functions
	createEl : function(eltype,options,vl,autowidth, ajaxso) {
		var elem = "";
		function bindEv (el, opt) {
			if($.isFunction(opt.dataInit)) {
				opt.dataInit(el);
			}
			if(opt.dataEvents) {
				$.each(opt.dataEvents, function() {
					if (this.data !== undefined) {
						$(el).bind(this.type, this.data, this.fn);
					} else {
						$(el).bind(this.type, this.fn);
					}
				});
			}
			return opt;
		}
		function setAttributes(elm, atr, exl ) {
			var exclude = ['dataInit','dataEvents','dataUrl', 'buildSelect','sopt', 'searchhidden', 'defaultValue', 'attr'];
			if(typeof(exl) != "undefined" && $.isArray(exl)) {
				exclude = $.extend(exclude, exl);
			}
			$.each(atr, function(key, value){
				if($.inArray(key, exclude) === -1) {
					$(elm).attr(key,value);
				}
			});
			if(!atr.hasOwnProperty('id')) {
				$(elm).attr('id', $.jgrid.randId());
		}
		}
		switch (eltype)
		{
			case "textarea" :
				elem = document.createElement("textarea");
				if(autowidth) {
					if(!options.cols) { $(elem).css({width:"98%"});}
				} else if (!options.cols) { options.cols = 20; }
				if(!options.rows) { options.rows = 2; }
				if(vl=='&nbsp;' || vl=='&#160;' || (vl.length==1 && vl.charCodeAt(0)==160)) {vl="";}
				elem.value = vl;
				setAttributes(elem, options);
				options = bindEv(elem,options);
				$(elem).attr({"role":"textbox","multiline":"true"});
			break;
			case "checkbox" : //what code for simple checkbox
				elem = document.createElement("input");
				elem.type = "checkbox";
				if( !options.value ) {
					var vl1 = vl.toLowerCase();
					if(vl1.search(/(false|0|no|off|undefined)/i)<0 && vl1!=="") {
						elem.checked=true;
						elem.defaultChecked=true;
						elem.value = vl;
					} else {
						elem.value = "on";
					}
					$(elem).attr("offval","off");
				} else {
					var cbval = options.value.split(":");
					if(vl === cbval[0]) {
						elem.checked=true;
						elem.defaultChecked=true;
					}
					elem.value = cbval[0];
					$(elem).attr("offval",cbval[1]);
				}
				setAttributes(elem, options, ['value']);
				options = bindEv(elem,options);
				$(elem).attr("role","checkbox");
			break;
			case "select" :
				elem = document.createElement("select");
				elem.setAttribute("role","select");
				var msl, ovm = [];
				if(options.multiple===true) {
					msl = true;
					elem.multiple="multiple";
					$(elem).attr("aria-multiselectable","true");
				} else { msl = false; }
				if(typeof(options.dataUrl) != "undefined") {
					$.ajax($.extend({
						url: options.dataUrl,
						type : "GET",
						dataType: "html",
						context: {elem:elem, options:options, vl:vl},
						success: function(data,status){
							var a,	ovm = [], elem = this.elem, vl = this.vl,
							options = $.extend({},this.options),
							msl = options.multiple===true;
							if(typeof(options.buildSelect) != "undefined") {
								var b = options.buildSelect(data);
								a = $(b).html();
							} else {
								a = $(data).html();
							}
							if(a) {
								$(elem).append(a);
								setAttributes(elem, options);
								options = bindEv(elem,options);
								if(typeof options.size === 'undefined') { options.size =  msl ? 3 : 1;}
								if(msl) {
									ovm = vl.split(",");
									ovm = $.map(ovm,function(n){return $.trim(n);});
								} else {
									ovm[0] = $.trim(vl);
								}
								//$(elem).attr(options);
								setTimeout(function(){
									$("option",elem).each(function(i){
										//if(i===0) { this.selected = ""; }
										$(this).attr("role","option");
										if($.inArray($.trim($(this).text()),ovm) > -1 || $.inArray($.trim($(this).val()),ovm) > -1 ) {
											this.selected= "selected";
										}
									});
								},0);
							}
						}
					},ajaxso || {}));
				} else if(options.value) {
					var i;
					if(msl) {
						ovm = vl.split(",");
						ovm = $.map(ovm,function(n){return $.trim(n);});
						if(typeof options.size === 'undefined') {options.size = 3;}
					} else {
						options.size = 1;
					}
					if(typeof options.value === 'function') { options.value = options.value(); }
					var so,sv, ov;
					if(typeof options.value === 'string') {
						so = options.value.split(";");
						for(i=0; i<so.length;i++){
							sv = so[i].split(":");
							if(sv.length > 2 ) {
								sv[1] = $.map(sv,function(n,i){if(i>0) { return n;} }).join(":");
							}
							ov = document.createElement("option");
							ov.setAttribute("role","option");
							ov.value = sv[0]; ov.innerHTML = sv[1];
							if (!msl &&  ($.trim(sv[0]) == $.trim(vl) || $.trim(sv[1]) == $.trim(vl))) { ov.selected ="selected"; }
							if (msl && ($.inArray($.trim(sv[1]), ovm)>-1 || $.inArray($.trim(sv[0]), ovm)>-1)) {ov.selected ="selected";}
							elem.appendChild(ov);
						}
					} else if (typeof options.value === 'object') {
						var oSv = options.value;
						for ( var key in oSv) {
							if (oSv.hasOwnProperty(key ) ){
								ov = document.createElement("option");
								ov.setAttribute("role","option");
								ov.value = key; ov.innerHTML = oSv[key];
								if (!msl &&  ( $.trim(key) == $.trim(vl) || $.trim(oSv[key]) == $.trim(vl)) ) { ov.selected ="selected"; }
								if (msl && ($.inArray($.trim(oSv[key]),ovm)>-1 || $.inArray($.trim(key),ovm)>-1)) { ov.selected ="selected"; }
								elem.appendChild(ov);
							}
						}
					}
					setAttributes(elem, options, ['value']);
					options = bindEv(elem,options);
				}
			break;
			case "text" :
			case "password" :
			case "button" :
				var role;
				if(eltype=="button") { role = "button"; }
				else { role = "textbox"; }
				elem = document.createElement("input");
				elem.type = eltype;
				elem.value = vl;
				setAttributes(elem, options);
				options = bindEv(elem,options);
				if(eltype != "button"){
					if(autowidth) {
						if(!options.size) { $(elem).css({width:"98%"}); }
					} else if (!options.size) { options.size = 20; }
				}
				$(elem).attr("role",role);
			break;
			case "image" :
			case "file" :
				elem = document.createElement("input");
				elem.type = eltype;
				setAttributes(elem, options);
				options = bindEv(elem,options);
				break;
			case "custom" :
				elem = document.createElement("span");
				try {
					if($.isFunction(options.custom_element)) {
						var celm = options.custom_element.call(this,vl,options);
						if(celm) {
							celm = $(celm).addClass("customelement").attr({id:options.id,name:options.name});
							$(elem).empty().append(celm);
						} else {
							throw "e2";
						}
					} else {
						throw "e1";
					}
				} catch (e) {
					if (e=="e1") { this.info_dialog($.jgrid.errors.errcap,"function 'custom_element' "+$.jgrid.edit.msg.nodefined, $.jgrid.edit.bClose);}
					if (e=="e2") { this.info_dialog($.jgrid.errors.errcap,"function 'custom_element' "+$.jgrid.edit.msg.novalue,$.jgrid.edit.bClose);}
					else { this.info_dialog($.jgrid.errors.errcap,typeof(e)==="string"?e:e.message,$.jgrid.edit.bClose); }
				}
			break;
		}
		return elem;
	},
// Date Validation Javascript
	checkDate : function (format, date) {
		var daysInFebruary = function(year){
		// February has 29 days in any year evenly divisible by four,
		// EXCEPT for centurial years which are not also divisible by 400.
			return (((year % 4 === 0) && ( year % 100 !== 0 || (year % 400 === 0))) ? 29 : 28 );
		},
		DaysArray = function(n) {
			for (var i = 1; i <= n; i++) {
				this[i] = 31;
				if (i==4 || i==6 || i==9 || i==11) {this[i] = 30;}
				if (i==2) {this[i] = 29;}
			}
			return this;
		};

		var tsp = {}, sep;
		format = format.toLowerCase();
		//we search for /,-,. for the date separator
		if(format.indexOf("/") != -1) {
			sep = "/";
		} else if(format.indexOf("-") != -1) {
			sep = "-";
		} else if(format.indexOf(".") != -1) {
			sep = ".";
		} else {
			sep = "/";
		}
		format = format.split(sep);
		date = date.split(sep);
		if (date.length != 3) { return false; }
		var j=-1,yln, dln=-1, mln=-1;
		for(var i=0;i<format.length;i++){
			var dv =  isNaN(date[i]) ? 0 : parseInt(date[i],10);
			tsp[format[i]] = dv;
			yln = format[i];
			if(yln.indexOf("y") != -1) { j=i; }
			if(yln.indexOf("m") != -1) { mln=i; }
			if(yln.indexOf("d") != -1) { dln=i; }
		}
		if (format[j] == "y" || format[j] == "yyyy") {
			yln=4;
		} else if(format[j] =="yy"){
			yln = 2;
		} else {
			yln = -1;
		}
		var daysInMonth = DaysArray(12),
		strDate;
		if (j === -1) {
			return false;
		} else {
			strDate = tsp[format[j]].toString();
			if(yln == 2 && strDate.length == 1) {yln = 1;}
			if (strDate.length != yln || (tsp[format[j]]===0 && date[j]!="00")){
				return false;
			}
		}
		if(mln === -1) {
			return false;
		} else {
			strDate = tsp[format[mln]].toString();
			if (strDate.length<1 || tsp[format[mln]]<1 || tsp[format[mln]]>12){
				return false;
			}
		}
		if(dln === -1) {
			return false;
		} else {
			strDate = tsp[format[dln]].toString();
			if (strDate.length<1 || tsp[format[dln]]<1 || tsp[format[dln]]>31 || (tsp[format[mln]]==2 && tsp[format[dln]]>daysInFebruary(tsp[format[j]])) || tsp[format[dln]] > daysInMonth[tsp[format[mln]]]){
				return false;
			}
		}
		return true;
	},
	isEmpty : function(val)
	{
		if (val.match(/^\s+$/) || val === "")	{
			return true;
		} else {
			return false;
		}
	},
	checkTime : function(time){
	// checks only hh:ss (and optional am/pm)
		var re = /^(\d{1,2}):(\d{2})([ap]m)?$/,regs;
		if(!this.isEmpty(time))
		{
			regs = time.match(re);
			if(regs) {
				if(regs[3]) {
					if(regs[1] < 1 || regs[1] > 12) { return false; }
				} else {
					if(regs[1] > 23) { return false; }
				}
				if(regs[2] > 59) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	},
	checkValues : function(val, valref,g, customobject, nam) {
		var edtrul,i, nm, dft, len;
		if(typeof(customobject) === "undefined") {
			if(typeof(valref)=='string'){
				for( i =0, len=g.p.colModel.length;i<len; i++){
					if(g.p.colModel[i].name==valref) {
						edtrul = g.p.colModel[i].editrules;
						valref = i;
						try { nm = g.p.colModel[i].formoptions.label; } catch (e) {}
						break;
					}
				}
			} else if(valref >=0) {
				edtrul = g.p.colModel[valref].editrules;
			}
		} else {
			edtrul = customobject;
			nm = nam===undefined ? "_" : nam;
		}
		if(edtrul) {
			if(!nm) { nm = g.p.colNames[valref]; }
			if(edtrul.required === true) {
				if( this.isEmpty(val) )  { return [false,nm+": "+$.jgrid.edit.msg.required,""]; }
			}
			// force required
			var rqfield = edtrul.required === false ? false : true;
			if(edtrul.number === true) {
				if( !(rqfield === false && this.isEmpty(val)) ) {
					if(isNaN(val)) { return [false,nm+": "+$.jgrid.edit.msg.number,""]; }
				}
			}
			if(typeof edtrul.minValue != 'undefined' && !isNaN(edtrul.minValue)) {
				if (parseFloat(val) < parseFloat(edtrul.minValue) ) { return [false,nm+": "+$.jgrid.edit.msg.minValue+" "+edtrul.minValue,""];}
			}
			if(typeof edtrul.maxValue != 'undefined' && !isNaN(edtrul.maxValue)) {
				if (parseFloat(val) > parseFloat(edtrul.maxValue) ) { return [false,nm+": "+$.jgrid.edit.msg.maxValue+" "+edtrul.maxValue,""];}
			}
			var filter;
			if(edtrul.email === true) {
				if( !(rqfield === false && this.isEmpty(val)) ) {
				// taken from $ Validate plugin
					filter = /^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i;
					if(!filter.test(val)) {return [false,nm+": "+$.jgrid.edit.msg.email,""];}
				}
			}
			if(edtrul.integer === true) {
				if( !(rqfield === false && this.isEmpty(val)) ) {
					if(isNaN(val)) { return [false,nm+": "+$.jgrid.edit.msg.integer,""]; }
					if ((val % 1 !== 0) || (val.indexOf('.') != -1)) { return [false,nm+": "+$.jgrid.edit.msg.integer,""];}
				}
			}
			if(edtrul.date === true) {
				if( !(rqfield === false && this.isEmpty(val)) ) {
					if(g.p.colModel[valref].formatoptions && g.p.colModel[valref].formatoptions.newformat) {
						dft = g.p.colModel[valref].formatoptions.newformat;
					} else {
						dft = g.p.colModel[valref].datefmt || "Y-m-d";
					}
					if(!this.checkDate (dft, val)) { return [false,nm+": "+$.jgrid.edit.msg.date+" - "+dft,""]; }
				}
			}
			if(edtrul.time === true) {
				if( !(rqfield === false && this.isEmpty(val)) ) {
					if(!this.checkTime (val)) { return [false,nm+": "+$.jgrid.edit.msg.date+" - hh:mm (am/pm)",""]; }
				}
			}
			if(edtrul.url === true) {
				if( !(rqfield === false && this.isEmpty(val)) ) {
					filter = /^(((https?)|(ftp)):\/\/([\-\w]+\.)+\w{2,3}(\/[%\-\w]+(\.\w{2,})?)*(([\w\-\.\?\\\/+@&#;`~=%!]*)(\.\w{2,})?)*\/?)/i;
					if(!filter.test(val)) {return [false,nm+": "+$.jgrid.edit.msg.url,""];}
				}
			}
			if(edtrul.custom === true) {
				if( !(rqfield === false && this.isEmpty(val)) ) {
					if($.isFunction(edtrul.custom_func)) {
						var ret = edtrul.custom_func.call(g,val,nm);
						if($.isArray(ret)) {
							return ret;
						} else {
							return [false,$.jgrid.edit.msg.customarray,""];
						}
					} else {
						return [false,$.jgrid.edit.msg.customfcheck,""];
					}
				}
			}
		}
		return [true,"",""];
	}
});
})(jQuery);/*
 * jqFilter  jQuery jqGrid filter addon.
 * Copyright (c) 2011, Tony Tomov, tony@trirand.com
 * Dual licensed under the MIT and GPL licenses
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl-2.0.html
 * 
 * The work is inspired from this Stefan Pirvu
 * http://www.codeproject.com/KB/scripting/json-filtering.aspx
 *
 * The filter uses JSON entities to hold filter rules and groups. Here is an example of a filter:

{ "groupOp": "AND",
      "groups" : [ 
        { "groupOp": "OR",
            "rules": [
                { "field": "name", "op": "eq", "data": "England" }, 
                { "field": "id", "op": "le", "data": "5"}
             ]
        } 
      ],
      "rules": [
        { "field": "name", "op": "eq", "data": "Romania" }, 
        { "field": "id", "op": "le", "data": "1"}
      ]
}
*/
/*global jQuery, $, window, navigator */

(function ($) {

$.fn.jqFilter = function( arg ) {
	if (typeof arg === 'string') {
		
		var fn = $.fn.jqFilter[arg];
		if (!fn) {
			throw ("jqFilter - No such method: " + arg);
		}
		var args = $.makeArray(arguments).slice(1);
		return fn.apply(this,args);
	}

	var p = $.extend(true,{
		filter: null,
		columns: [],
		onChange : null,
		checkValues : null,
		error: false,
		errmsg : "",
		errorcheck : true,
		showQuery : true,
		sopt : null,
		ops : [
			{"name": "eq", "description": "equal", "operator":"="},
			{"name": "ne", "description": "not equal", "operator":"<>"},
			{"name": "lt", "description": "less", "operator":"<"},
			{"name": "le", "description": "less or equal","operator":"<="},
			{"name": "gt", "description": "greater", "operator":">"},
			{"name": "ge", "description": "greater or equal", "operator":">="},
			{"name": "bw", "description": "begins with", "operator":"LIKE"},
			{"name": "bn", "description": "does not begin with", "operator":"NOT LIKE"},
			{"name": "in", "description": "in", "operator":"IN"},
			{"name": "ni", "description": "not in", "operator":"NOT IN"},
			{"name": "ew", "description": "ends with", "operator":"LIKE"},
			{"name": "en", "description": "does not end with", "operator":"NOT LIKE"},
			{"name": "cn", "description": "contains", "operator":"LIKE"},
			{"name": "nc", "description": "does not contain", "operator":"NOT LIKE"},
			{"name": "nu", "description": "is null", "operator":"IS NULL"},
			{"name": "nn", "description": "is not null", "operator":"IS NOT NULL"}
		],
		numopts : ['eq','ne', 'lt', 'le', 'gt', 'ge', 'nu', 'nn', 'in', 'ni'],
		stropts : ['eq', 'ne', 'bw', 'bn', 'ew', 'en', 'cn', 'nc', 'nu', 'nn', 'in', 'ni'],
		_gridsopt : [], // grid translated strings, do not tuch
		groupOps : ["AND", "OR"],
		groupButton : true
	}, arg || {});
	return this.each( function() {
		if (this.filter) {return;}
		this.p = p;
		// setup filter in case if they is not defined
		if (this.p.filter === null || this.p.filter === undefined) {
			this.p.filter = {
				groupOp: this.p.groupOps[0],
				rules: [],
				groups: []
			};
		}
		var i, len = this.p.columns.length, cl,
		isIE = /msie/i.test(navigator.userAgent) && !window.opera;

		// translating the options
		if(this.p._gridsopt.length) {
			// ['eq','ne','lt','le','gt','ge','bw','bn','in','ni','ew','en','cn','nc']
			for(i=0;i<this.p._gridsopt.length;i++) {
				this.p.ops[i].description = this.p._gridsopt[i];
			}
		}
		this.p.initFilter = $.extend(true,{},this.p.filter);

		// set default values for the columns if they are not set
		if( !len ) {return;}
		for(i=0; i < len; i++) {
			cl = this.p.columns[i];
			if( cl.stype ) {
				// grid compatibility
				cl.inputtype = cl.stype;
			} else if(!cl.inputtype) {
				cl.inputtype = 'text';
			}
			if( cl.sorttype ) {
				// grid compatibility
				cl.searchtype = cl.sorttype;
			} else if (!cl.searchtype) {
				cl.searchtype = 'string';
			}
			if(cl.hidden === undefined) {
				// jqGrid compatibility
				cl.hidden = false;
			}
			if(!cl.label) {
				cl.label = cl.name;
			}
			if(cl.index) {
				cl.name = cl.index;
			}
			if(!cl.hasOwnProperty('searchoptions')) {
				cl.searchoptions = {};
			}
			if(!cl.hasOwnProperty('searchrules')) {
				cl.searchrules = {};
			}

		}
		if(this.p.showQuery) {
			$(this).append("<table class='queryresult ui-widget ui-widget-content' style='display:block;max-width:440px;border:0px none;'><tbody><tr><td class='query'></td></tr></tbody></table>");
		}
		/*
		 *Perform checking.
		 *
		*/
		var checkData = function(val, colModelItem) {
			var ret = [true,""];
			if($.isFunction(colModelItem.searchrules)) {
				ret = colModelItem.searchrules(val, colModelItem);
			} else if($.jgrid && $.jgrid.checkValues) {
				try {
					ret = $.jgrid.checkValues(val, -1, null, colModelItem.searchrules, colModelItem.label);
				} catch (e) {}
			}
			if(ret && ret.length && ret[0] === false) {
				p.error = !ret[0];
				p.errmsg = ret[1];
			}
		};
		/* moving to common
		randId = function() {
			return Math.floor(Math.random()*10000).toString();
		};
		*/

		this.onchange = function (  ){
			// clear any error 
			this.p.error = false;
			this.p.errmsg="";
			return $.isFunction(this.p.onChange) ? this.p.onChange.call( this, this.p ) : false;
		};
		/*
		 * Redrow the filter every time when new field is added/deleted
		 * and field is  changed
		 */
		this.reDraw = function() {
			$("table.group:first",this).remove();
			var t = this.createTableForGroup(p.filter, null);
			$(this).append(t);
		};
		/*
		 * Creates a grouping data for the filter
		 * @param group - object
		 * @param parentgroup - object
		 */
		this.createTableForGroup = function(group, parentgroup) {
			var that = this,  i;

			// this table will hold all the group (tables) and rules (rows)
			var table = $("<table class='group ui-widget ui-widget-content' style='border:0px none;'><tbody></tbody></table>");
			// create error message row
			if(parentgroup === null) {
				$(table).append("<tr class='error' style='display:none;'><th colspan='5' class='ui-state-error' align='left'></th></tr>");
			}

			var tr = $("<tr></tr>");
			$(table).append(tr);
			// this header will hold the group operator type and group action buttons for
			// creating subgroup "+ {}", creating rule "+" or deleting the group "-"
			var th = $("<th colspan='5' align='left'></th>");
			tr.append(th);

			// dropdown for: choosing group operator type
			var groupOpSelect = $("<select class='opsel'></select>");
			th.append(groupOpSelect);
			// populate dropdown with all posible group operators: or, and
			var str= "", selected;
			for (i = 0; i < p.groupOps.length; i++) {
				selected =  group.groupOp === that.p.groupOps[i] ? " selected='selected'" :"";
				str += "<option value='"+that.p.groupOps[i]+"'" + selected+">"+that.p.groupOps[i]+"</option>";
			}

			groupOpSelect
			.append(str)
			.bind('change',function() {
				group.groupOp = $(groupOpSelect).val();
				that.onchange(); // signals that the filter has changed
			});

			// button for adding a new subgroup
			var inputAddSubgroup ="<span></span>";
			if(this.p.groupButton) {
				inputAddSubgroup = $("<input type='button' value='+ {}' title='Add subgroup' class='add-group'/>");
				inputAddSubgroup.bind('click',function() {
					if (group.groups === undefined ) {
						group.groups = [];
					}

					group.groups.push({
						groupOp: p.groupOps[0],
						rules: [],
						groups: []
					}); // adding a new group

					that.reDraw(); // the html has changed, force reDraw

					that.onchange(); // signals that the filter has changed
					return false;
				});
			}
			th.append(inputAddSubgroup);

			// button for adding a new rule
			var inputAddRule = $("<input type='button' value='+' title='Add rule' class='add-rule ui-add'/>"), cm;
			inputAddRule.bind('click',function() {
				//if(!group) { group = {};}
				if (group.rules === undefined) {
					group.rules = [];
				}
				for (i = 0; i < that.p.columns.length; i++) {
				// but show only serchable and serchhidden = true fields
					var searchable = (typeof that.p.columns[i].search === 'undefined') ?  true: that.p.columns[i].search ,
					hidden = (that.p.columns[i].hidden === true),
					ignoreHiding = (that.p.columns[i].searchoptions.searchhidden === true);
					if ((ignoreHiding && searchable) || (searchable && !hidden)) {
						cm = that.p.columns[i];
						break;
					}
				}
				var opr;
				if( cm.searchoptions.sopt ) {opr = cm.searchoptions.sopt;}
				else if(that.p.sopt) { opr= that.p.sopt; }
				else if  (cm.searchtype === 'string') {opr = that.p.stropts;}
				else {opr = that.p.numopts;}

				group.rules.push({
					field: cm.name,
					op: opr[0],
					data: ""
				}); // adding a new rule

				that.reDraw(); // the html has changed, force reDraw
				// for the moment no change have been made to the rule, so
				// this will not trigger onchange event
				return false;
			});
			th.append(inputAddRule);

			// button for delete the group
			if (parentgroup !== null) { // ignore the first group
				var inputDeleteGroup = $("<input type='button' value='-' title='Delete group' class='delete-group'/>");
				th.append(inputDeleteGroup);
				inputDeleteGroup.bind('click',function() {
				// remove group from parent
					for (i = 0; i < parentgroup.groups.length; i++) {
						if (parentgroup.groups[i] === group) {
							parentgroup.groups.splice(i, 1);
							break;
						}
					}

					that.reDraw(); // the html has changed, force reDraw

					that.onchange(); // signals that the filter has changed
					return false;
				});
			}

			// append subgroup rows
			if (group.groups !== undefined) {
				for (i = 0; i < group.groups.length; i++) {
					var trHolderForSubgroup = $("<tr></tr>");
					table.append(trHolderForSubgroup);

					var tdFirstHolderForSubgroup = $("<td class='first'></td>");
					trHolderForSubgroup.append(tdFirstHolderForSubgroup);

					var tdMainHolderForSubgroup = $("<td colspan='4'></td>");
					tdMainHolderForSubgroup.append(this.createTableForGroup(group.groups[i], group));
					trHolderForSubgroup.append(tdMainHolderForSubgroup);
				}
			}
			if(group.groupOp === undefined) {
				group.groupOp = that.p.groupOps[0];
			}

			// append rules rows
			if (group.rules !== undefined) {
				for (i = 0; i < group.rules.length; i++) {
					table.append(
                       this.createTableRowForRule(group.rules[i], group)
					);
				}
			}

			return table;
		};
		/*
		 * Create the rule data for the filter
		 */
		this.createTableRowForRule = function(rule, group ) {
			// save current entity in a variable so that it could
			// be referenced in anonimous method calls

			var that=this, tr = $("<tr></tr>"),
			//document.createElement("tr"),

			// first column used for padding
			//tdFirstHolderForRule = document.createElement("td"),
			i, op, trpar, cm, str="", selected;
			//tdFirstHolderForRule.setAttribute("class", "first");
			tr.append("<td class='first'></td>");


			// create field container
			var ruleFieldTd = $("<td class='columns'></td>");
			tr.append(ruleFieldTd);


			// dropdown for: choosing field
			var ruleFieldSelect = $("<select></select>"), ina, aoprs = [];
			ruleFieldTd.append(ruleFieldSelect);
			ruleFieldSelect.bind('change',function() {
				rule.field = $(ruleFieldSelect).val();

				trpar = $(this).parents("tr:first");
				for (i=0;i<that.p.columns.length;i++) {
					if(that.p.columns[i].name ===  rule.field) {
						cm = that.p.columns[i];
						break;
					}
				}
				if(!cm) {return;}
				cm.searchoptions.id = $.jgrid.randId();
				if(isIE && cm.inputtype === "text") {
					if(!cm.searchoptions.size) {
						cm.searchoptions.size = 10;
					}
				}
				var elm = $.jgrid.createEl(cm.inputtype,cm.searchoptions, "", true, that.p.ajaxSelectOptions, true);
				$(elm).addClass("input-elm");
				//that.createElement(rule, "");

				if( cm.searchoptions.sopt ) {op = cm.searchoptions.sopt;}
				else if(that.p.sopt) { op= that.p.sopt; }
				else if  (cm.searchtype === 'string') {op = that.p.stropts;}
				else {op = that.p.numopts;}
				// operators
				var s ="",so="";
				aoprs = [];
				$.each(that.p.ops, function() { aoprs.push(this.name) });
				for ( i = 0 ; i < op.length; i++) {
					ina = $.inArray(op[i],aoprs);
					if(ina !== -1) {
						so ="";
						if(i===0) {
							rule.op = that.p.ops[ina].name;
							so = " selected='selected'";
						}
						s += "<option value='"+that.p.ops[ina].name+"'"+ so+">"+that.p.ops[ina].description+"</option>";
					}
				}
				$(".selectopts",trpar).empty().append( s );

				// data
				$(".data",trpar).empty().append( elm );
				$(".input-elm",trpar).bind('change',function() {
					rule.data = $(this).val();
					that.onchange(); // signals that the filter has changed
				});
				setTimeout(function(){ //IE, Opera, Chrome
				rule.data = $(elm).val();
				that.onchange();  // signals that the filter has changed
				}, 0);
			});

			// populate drop down with user provided column definitions
			var j=0;
			for (i = 0; i < that.p.columns.length; i++) {
				// but show only serchable and serchhidden = true fields
		        var searchable = (typeof that.p.columns[i].search === 'undefined') ?  true: that.p.columns[i].search ,
		        hidden = (that.p.columns[i].hidden === true),
				ignoreHiding = (that.p.columns[i].searchoptions.searchhidden === true);
				if ((ignoreHiding && searchable) || (searchable && !hidden)) {
					selected = "";
					if(rule.field === that.p.columns[i].name) {
						selected = " selected='selected'";
						j=i;
					}
					str += "<option value='"+that.p.columns[i].name+"'" +selected+">"+that.p.columns[i].label+"</option>";
				}
			}
			ruleFieldSelect.append( str );


			// create operator container
			var ruleOperatorTd = $("<td class='operators'></td>");
			tr.append(ruleOperatorTd);
			cm = p.columns[j];
			// create it here so it can be referentiated in the onchange event
			//var RD = that.createElement(rule, rule.data);
			cm.searchoptions.id = $.jgrid.randId();
			if(isIE && cm.inputtype === "text") {
				if(!cm.searchoptions.size) {
					cm.searchoptions.size = 10;
				}
			}
			var ruleDataInput = $.jgrid.createEl(cm.inputtype,cm.searchoptions, rule.data, true, that.p.ajaxSelectOptions, true);

			// dropdown for: choosing operator
			var ruleOperatorSelect = $("<select class='selectopts'></select>");
			ruleOperatorTd.append(ruleOperatorSelect);
			ruleOperatorSelect.bind('change',function() {
				rule.op = $(ruleOperatorSelect).val();
				trpar = $(this).parents("tr:first");
				var rd = $(".input-elm",trpar)[0];
				if (rule.op === "nu" || rule.op === "nn") { // disable for operator "is null" and "is not null"
					rule.data = "";
					rd.value = "";
					rd.setAttribute("readonly", "true");
					rd.setAttribute("disabled", "true");
				} else {
					rd.removeAttribute("readonly");
					rd.removeAttribute("disabled");
				}

				that.onchange();  // signals that the filter has changed
			});

			// populate drop down with all available operators
			if( cm.searchoptions.sopt ) {op = cm.searchoptions.sopt;}
			else if(that.p.sopt) { op= that.p.sopt; }
			else if  (cm.searchtype === 'string') {op = p.stropts;}
			else {op = that.p.numopts;}
			str="";
			$.each(that.p.ops, function() { aoprs.push(this.name) });
			for ( i = 0; i < op.length; i++) {
				ina = $.inArray(op[i],aoprs);
				if(ina !== -1) {
					selected = rule.op === that.p.ops[ina].name ? " selected='selected'" : "";
					str += "<option value='"+that.p.ops[ina].name+"'"+selected+">"+that.p.ops[ina].description+"</option>";
				}
			}
			ruleOperatorSelect.append( str );
			// create data container
			var ruleDataTd = $("<td class='data'></td>");
			tr.append(ruleDataTd);

			// textbox for: data
			// is created previously
			//ruleDataInput.setAttribute("type", "text");
			ruleDataTd.append(ruleDataInput);

			$(ruleDataInput)
			.addClass("input-elm")
			.bind('change', function() {
				rule.data = $(this).val();
				that.onchange(); // signals that the filter has changed
			});

			// create action container
			var ruleDeleteTd = $("<td></td>");
			tr.append(ruleDeleteTd);

			// create button for: delete rule
			var ruleDeleteInput = $("<input type='button' value='-' title='Delete rule' class='delete-rule ui-del'/>");
			ruleDeleteTd.append(ruleDeleteInput);
			//$(ruleDeleteInput).html("").height(20).width(30).button({icons: {  primary: "ui-icon-minus", text:false}});
			ruleDeleteInput.bind('click',function() {
				// remove rule from group
				for (i = 0; i < group.rules.length; i++) {
					if (group.rules[i] === rule) {
						group.rules.splice(i, 1);
						break;
					}
				}

				that.reDraw(); // the html has changed, force reDraw

				that.onchange(); // signals that the filter has changed
				return false;
			});

			return tr;
		};

		this.getStringForGroup = function(group) {
			var s = "(", index;
			if (group.groups !== undefined) {
				for (index = 0; index < group.groups.length; index++) {
					if (s.length > 1) {
						s += " " + group.groupOp + " ";
					}
					try {
						s += this.getStringForGroup(group.groups[index]);
					} catch (eg) {alert(eg);}
				}
			}

			if (group.rules !== undefined) {
				try{
					for (index = 0; index < group.rules.length; index++) {
						if (s.length > 1) {
							s += " " + group.groupOp + " ";
						}
						s += this.getStringForRule(group.rules[index]);
					}
				} catch (e) {alert(e);}
			}

			s += ")";

			if (s === "()") {
				return ""; // ignore groups that don't have rules
			} else {
				return s;
			}
		};
		this.getStringForRule = function(rule) {
			var opUF = "",opC="", i, cm, ret, val,
			numtypes = ['int', 'integer', 'float', 'number', 'currency']; // jqGrid
			for (i = 0; i < this.p.ops.length; i++) {
				if (this.p.ops[i].name === rule.op) {
					opUF = this.p.ops[i].operator;
					opC = this.p.ops[i].name;
					break;
				}
			}
			for (i=0; i<this.p.columns.length; i++) {
				if(this.p.columns[i].name === rule.field) {
					cm = this.p.columns[i];
					break;
				}
			}
			val = rule.data;
			if(opC === 'bw' || opC === 'bn') { val = val+"%"; }
			if(opC === 'ew' || opC === 'en') { val = "%"+val; }
			if(opC === 'cn' || opC === 'nc') { val = "%"+val+"%"; }
			if(opC === 'in' || opC === 'ni') { val = " ("+val+")"; }
			if(p.errorcheck) { checkData(rule.data, cm); }
			if($.inArray(cm.searchtype, numtypes) !== -1 || opC === 'nn' || opC === 'nu') { ret = rule.field + " " + opUF + " " + val; }
			else { ret = rule.field + " " + opUF + " \"" + val + "\""; }
			return ret;
		};
		this.resetFilter = function () {
			this.p.filter = $.extend(true,{},this.p.initFilter);
			this.reDraw();
			this.onchange();
		};
		this.hideError = function() {
			$("th.ui-state-error", this).html("");
			$("tr.error", this).hide();
		};
		this.showError = function() {
			$("th.ui-state-error", this).html(this.p.errmsg);
			$("tr.error", this).show();
		};
		this.toUserFriendlyString = function() {
			return this.getStringForGroup(p.filter);
		};
		this.toString = function() {
			// this will obtain a string that can be used to match an item.
			var that = this;
			function getStringRule(rule) {
				if(that.p.errorcheck) {
					var i, cm;
					for (i=0; i<that.p.columns.length; i++) {
						if(that.p.columns[i].name === rule.field) {
							cm = that.p.columns[i];
							break;
						}
					}
					if(cm) {checkData(rule.data, cm);}
				}
				return rule.op + "(item." + rule.field + ",'" + rule.data + "')";
			}

			function getStringForGroup(group) {
				var s = "(", index;

				if (group.groups !== undefined) {
					for (index = 0; index < group.groups.length; index++) {
						if (s.length > 1) {
							if (group.groupOp === "OR") {
								s += " || ";
							}
							else {
								s += " && ";
							}
						}
						s += getStringForGroup(group.groups[index]);
					}
				}

				if (group.rules !== undefined) {
					for (index = 0; index < group.rules.length; index++) {
						if (s.length > 1) {
							if (group.groupOp === "OR") {
								s += " || ";
							}
							else  {
								s += " && ";
							}
						}
						s += getStringRule(group.rules[index]);
					}
				}

				s += ")";

				if (s === "()") {
					return ""; // ignore groups that don't have rules
				} else {
					return s;
				}
			}

			return getStringForGroup(this.p.filter);
		};

		// Here we init the filter
		this.reDraw();

		if(this.p.showQuery) {
			this.onchange();
		}
		// mark is as created so that it will not be created twice on this element
		this.filter = true;
	});
};
$.extend($.fn.jqFilter,{
	/*
	 * Return SQL like string. Can be used directly
	 */
	toSQLString : function()
	{
		var s ="";
		this.each(function(){
			s = this.toUserFriendlyString();
		});
		return s;
	},
	/*
	 * Return filter data as object.
	 */
	filterData : function()
	{
		var s;
		this.each(function(){
			s = this.p.filter;
		});
		return s;

	},
	getParameter : function (param) {
		if(param !== undefined) {
			if (this.p.hasOwnProperty(param) ) {
				return this.p[param];
			}
		}
		return this.p;
	},
	resetFilter: function() {
		return this.each(function(){
			this.resetFilter();
		});
	},
	addFilter: function (pfilter) {
		if (typeof pfilter === "string") {
			pfilter = jQuery.jgrid.parse( pfilter );
	}
		this.each(function(){
			this.p.filter = pfilter;
			this.reDraw();
			this.onchange();
		});
	}

});
})(jQuery);
(function($){
/**
 * jqGrid extension for form editing Grid Data
 * Tony Tomov tony@trirand.com
 * http://trirand.com/blog/ 
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl-2.0.html
**/
/*global xmlJsonClass, jQuery, $  */
var rp_ge = null;
$.jgrid.extend({
	searchGrid : function (p) {
		p = $.extend({
			recreateFilter: false,
			drag: true,
			sField:'searchField',
			sValue:'searchString',
			sOper: 'searchOper',
			sFilter: 'filters',
            loadDefaults: true, // this options activates loading of default filters from grid's postData for Multipe Search only.
			beforeShowSearch: null,
			afterShowSearch : null,
			onInitializeSearch: null,
			closeAfterSearch : false,
			closeAfterReset: false,
			closeOnEscape : false,
			multipleSearch : false,
			multipleGroup : false,
			//cloneSearchRowOnAdd: true,
			top : 0,
			left: 0,
			jqModal : true,
			modal: false,
			resize : false,
			width: 450,
			height: 'auto',
			dataheight: 'auto',
			showQuery: false,
			errorcheck : true,
			// translation
			// if you want to change or remove the order change it in sopt
			// ['eq','ne','lt','le','gt','ge','bw','bn','in','ni','ew','en','cn','nc'],
			sopt: null,
			stringResult: undefined,
			onClose : null,
			onSearch : null,
			onReset : null,
			toTop : true,
			overlay : 10,
			columns : [],
			tmplNames : null,
			tmplFilters : null,
			// translations - later in lang file
			tmplLabel : ' Template: ',
            showOnLoad: false,
            layer: null
		}, $.jgrid.search, p || {});
		return this.each(function() {
			var $t = this;
			if(!$t.grid) {return;}
			var fid = "fbox_"+$t.p.id,
			showFrm = true,
			IDs = {themodal:'searchmod'+fid,modalhead:'searchhd'+fid,modalcontent:'searchcnt'+fid, scrollelm : fid},
			defaultFilters  = $t.p.postData[p.sFilter];
			if(typeof(defaultFilters) === "string") {
				defaultFilters = $.jgrid.parse( defaultFilters );
			}
			if(p.recreateFilter === true) {
				$("#"+IDs.themodal).remove();
			}
			function showFilter() {
				if($.isFunction(p.beforeShowSearch)) {
					showFrm = p.beforeShowSearch($("#"+fid));
					if(typeof(showFrm) === "undefined") {
						showFrm = true;
					}
				}
				if(showFrm) {
					$.jgrid.viewModal("#"+IDs.themodal,{gbox:"#gbox_"+fid,jqm:p.jqModal, modal:p.modal, overlay: p.overlay, toTop: p.toTop});
					if($.isFunction(p.afterShowSearch)) {
						p.afterShowSearch($("#"+fid));
					}
				}
			}
			function hideButtons()
			{
				$(".add-rule","#"+fid).hide();
				$(".delete-rule","#"+fid).hide();
				$(".opsel","#"+fid).hide();
			}
			if ( $("#"+IDs.themodal).html() !== null ) {
				showFilter();
			} else {
				var fil = $("<span><div id='"+fid+"' class='searchFilter' style='overflow:auto'></div></span>").insertBefore("#gview_"+$t.p.id);
				if($.isFunction(p.onInitializeSearch) ) {
					p.onInitializeSearch($("#"+fid));
				}
				var columns = $.extend([],$t.p.colModel),
				bS  ="<a href='javascript:void(0)' id='"+fid+"_search' class='fm-button ui-state-default ui-corner-all fm-button-icon-right ui-reset'><span class='ui-icon ui-icon-search'></span>"+p.Find+"</a>",
				bC  ="<a href='javascript:void(0)' id='"+fid+"_reset' class='fm-button ui-state-default ui-corner-all fm-button-icon-left ui-search'><span class='ui-icon ui-icon-arrowreturnthick-1-w'></span>"+p.Reset+"</a>",
				bQ = "", tmpl="", colnm, found = false, bt, cmi=-1;
				if(p.showQuery) {
					bQ ="<a href='javascript:void(0)' id='"+fid+"_query' class='fm-button ui-state-default ui-corner-all fm-button-icon-left'><span class='ui-icon ui-icon-comment'></span>Query</a>";
				}
				if(!p.columns.length) {
					$.each(columns, function(i,n){
						if(!n.label) {
							n.label = $t.p.colNames[i];
						}
						// find first searchable column and set it if no default filter
						if(!found) {
							var searchable = (typeof n.search === 'undefined') ?  true: n.search ,
							hidden = (n.hidden === true),
							ignoreHiding = (n.searchoptions && n.searchoptions.searchhidden === true);
							if ((ignoreHiding && searchable) || (searchable && !hidden)) {
								found = true;
								colnm = n.index || n.name;
								cmi =i;
							}
						}
					});
				} else {
					columns = p.columns;
				}
				// old behaviour
				if( (!defaultFilters && colnm) || p.multipleSearch === false  ) {
					var cmop = "eq";
					if(cmi >=0 && columns[cmi].searchoptions && columns[cmi].searchoptions.sopt) {
						cmop = columns[cmi].searchoptions.sopt[0];
					} else if(p.sopt && p.sopt.length) {
						cmop = p.sopt[0];
				}
					defaultFilters = {"groupOp": "AND",rules:[{"field":colnm,"op":cmop,"data":""}]};
				}
				found = false;
				if(p.tmplNames && p.tmplNames.length) {
					found = true;
					tmpl = p.tmplLabel;
					tmpl += "<select class='ui-template'>";
					tmpl += "<option value='default'>Default</option>";
					$.each(p.tmplNames, function(i,n){
						tmpl += "<option value='"+i+"'>"+n+"</option>";
					});
					tmpl += "</select>";
				}

				bt = "<table class='EditTable' style='border:0px none;margin-top:5px' id='"+fid+"_2'><tbody><tr><td colspan='2'><hr class='ui-widget-content' style='margin:1px'/></td></tr><tr><td class='EditButton' style='text-align:left'>"+bC+tmpl+"</td><td class='EditButton'>"+bQ+bS+"</td></tr></tbody></table>";

				$("#"+fid).jqFilter({
					columns : columns,
					filter: p.loadDefaults ? defaultFilters : null,
					showQuery: p.showQuery,
					errorcheck : p.errorcheck,
					sopt: p.sopt,
					groupButton : p.multipleGroup,
					_gridsopt : $.jgrid.search.odata,
					onChange : function( sp ) {
						if(this.p.showQuery) {
							$('.query',this).html(this.toUserFriendlyString());
						}
					}
				});
				fil.append( bt );
				if(found && p.tmplFilters && p.tmplFilters.length) {
					$(".ui-template", fil).bind('change', function(e){
						var curtempl = $(this).val();
						if(curtempl=="default") {
							$("#"+fid).jqFilter('addFilter', defaultFilters);
						} else {
							$("#"+fid).jqFilter('addFilter', p.tmplFilters[parseInt(curtempl,10)]);
						}
						return false;
					});
				}
				if(p.multipleSearch === false) {
					hideButtons();
				}
				if($.isFunction(p.onInitializeSearch) ) {
					p.onInitializeSearch($("#"+fid));
				}
				if (p.layer)
					$.jgrid.createModal(IDs ,fil,p,"#gview_"+$t.p.id,$("#gbox_"+$t.p.id)[0], "#"+p.layer, {position: "relative"});
				else	
					$.jgrid.createModal(IDs ,fil,p,"#gview_"+$t.p.id,$("#gbox_"+$t.p.id)[0]);
				if(bQ) {
					$("#"+fid+"_query").bind('click', function(e){
						$(".queryresult", fil).toggle();
						return false;
					});
				}
				if (p.stringResult===undefined) {
					// to provide backward compatibility, inferring stringResult value from multipleSearch
					p.stringResult = p.multipleSearch;
				}
				$("#"+fid+"_search").bind('click', function(){
					var fl = $("#"+fid),
					sdata={}, res ,
					filters = fl.jqFilter('filterData');
					if(p.errorcheck) {
						fl[0].hideError();
						if(!p.showQuery) { fl.jqFilter('toSQLString'); }
						if(fl[0].p.error) {
							fl[0].showError();
							return false;
						}
					}

					if(p.stringResult) {
						try {
							// xmlJsonClass or JSON.stringify
							res = xmlJsonClass.toJson(filters, '', '', false);
						} catch (e) {
							try {
								res = JSON.stringify(filters);
							} catch (e2) { }
						}
						if(typeof(res)==="string") {
							sdata[p.sFilter] = res;
							$.each([p.sField,p.sValue, p.sOper], function() { sdata[this] = "";});
						}
					} else {
						if(p.multipleSearch) {
							sdata[p.sFilter] = filters;
							$.each([p.sField,p.sValue, p.sOper], function() { sdata[this] = "";});
						} else {
							sdata[p.sField] = filters.rules[0].field;
							sdata[p.sValue] = filters.rules[0].data;
							sdata[p.sOper] = filters.rules[0].op;
							sdata[p.sFilter] = "";
						}
					}
					$t.p.search = true;
					$.extend($t.p.postData,sdata);
					if($.isFunction(p.onSearch) ) {
						p.onSearch();
					}
					$($t).trigger("reloadGrid",[{page:1}]);
					if(p.closeAfterSearch) {
						$.jgrid.hideModal("#"+IDs.themodal,{gb:"#gbox_"+$t.p.id,jqm:p.jqModal,onClose: p.onClose});
					}
					return false;
				});
				$("#"+fid+"_reset").bind('click', function(){
					var sdata={},
					fl = $("#"+fid);
					$t.p.search = false;
					if(p.multipleSearch===false) {
						sdata[p.sField] = sdata[p.sValue] = sdata[p.sOper] = "";
					} else {
						sdata[p.sFilter] = "";
					}
					fl[0].resetFilter();
					if(found) {
						$(".ui-template", fil).val("default");
					}
					$.extend($t.p.postData,sdata);
					if($.isFunction(p.onReset) ) {
						p.onReset();
					}
					if(p.multipleSearch === false) {
						hideButtons();
					}
					$($t).trigger("reloadGrid",[{page:1}]);
					return false;
				});
				showFilter();
				$(".fm-button:not(.ui-state-disabled)",fil).hover(
				   function(){$(this).addClass('ui-state-hover');},
				   function(){$(this).removeClass('ui-state-hover');}
				);
			}
		});
	},
	editGridRow : function(rowid, p){
		p = $.extend({
			top : 0,
			left: 0,
			width: 300,
			height: 'auto',
			dataheight: 'auto',
			modal: false,
			overlay : 10,
			drag: true,
			resize: true,
			url: null,
			mtype : "POST",
			clearAfterAdd :true,
			closeAfterEdit : false,
			reloadAfterSubmit : true,
			onInitializeForm: null,
			beforeInitData: null,
			beforeShowForm: null,
			afterShowForm: null,
			beforeSubmit: null,
			afterSubmit: null,
			onclickSubmit: null,
			afterComplete: null,
			onclickPgButtons : null,
			afterclickPgButtons: null,
			editData : {},
			recreateForm : false,
			jqModal : true,
			closeOnEscape : false,
			addedrow : "first",
			topinfo : '',
			bottominfo: '',
			saveicon : [],
			closeicon : [],
			savekey: [false,13],
			navkeys: [false,38,40],
			checkOnSubmit : false,
			checkOnUpdate : false,
			_savedData : {},
			processing : false,
			onClose : null,
			ajaxEditOptions : {},
			serializeEditData : null,
			viewPagerButtons : true
		}, $.jgrid.edit, p || {});
		rp_ge = p;
		return this.each(function(){
			var $t = this;
			if (!$t.grid || !rowid) { return; }
			var gID = $t.p.id,
			frmgr = "FrmGrid_"+gID,frmtb = "TblGrid_"+gID,
			IDs = {themodal:'editmod'+gID,modalhead:'edithd'+gID,modalcontent:'editcnt'+gID, scrollelm : frmgr},
			onBeforeShow = $.isFunction(rp_ge.beforeShowForm) ? rp_ge.beforeShowForm : false,
			onAfterShow = $.isFunction(rp_ge.afterShowForm) ? rp_ge.afterShowForm : false,
			onBeforeInit = $.isFunction(rp_ge.beforeInitData) ? rp_ge.beforeInitData : false,
			onInitializeForm = $.isFunction(rp_ge.onInitializeForm) ? rp_ge.onInitializeForm : false,
			copydata = null,
			showFrm = true,
			maxCols = 1, maxRows=0,	postdata, extpost, newData, diff;
			if (rowid === "new") {
				rowid = "_empty";
				p.caption=rp_ge.addCaption;
			} else {
				p.caption=rp_ge.editCaption;
			}
			if(p.recreateForm===true && $("#"+IDs.themodal).html() !== null) {
				$("#"+IDs.themodal).remove();
			}
			var closeovrl = true;
			if(p.checkOnUpdate && p.jqModal && !p.modal) {
				closeovrl = false;
			}
			function getFormData(){
				$(".FormElement", "#"+frmtb).each(function(i) {
					var celm = $(".customelement", this);
					if (celm.length) {
						var  elem = celm[0], nm = $(elem).attr('name');
						$.each($t.p.colModel, function(i,n){
							if(this.name === nm && this.editoptions && $.isFunction(this.editoptions.custom_value)) {
								try {
									postdata[nm] = this.editoptions.custom_value($("#"+$.jgrid.jqID(nm),"#"+frmtb),'get');
									if (postdata[nm] === undefined) { throw "e1"; }
								} catch (e) {
									if (e==="e1") { $.jgrid.info_dialog(jQuery.jgrid.errors.errcap,"function 'custom_value' "+$.jgrid.edit.msg.novalue,jQuery.jgrid.edit.bClose);}
									else { $.jgrid.info_dialog(jQuery.jgrid.errors.errcap,e.message,jQuery.jgrid.edit.bClose); }
								}
								return true;
							}
						});
					} else {
					switch ($(this).get(0).type) {
						case "checkbox":
							if($(this).attr("checked")) {
								postdata[this.name]= $(this).val();
							}else {
								var ofv = $(this).attr("offval");
								postdata[this.name]= ofv;
							}
						break;
						case "select-one":
							postdata[this.name]= $("option:selected",this).val();
							extpost[this.name]= $("option:selected",this).text();
						break;
						case "select-multiple":
							postdata[this.name]= $(this).val();
							if(postdata[this.name]) { postdata[this.name] = postdata[this.name].join(","); }
							else { postdata[this.name] =""; }
							var selectedText = [];
							$("option:selected",this).each(
								function(i,selected){
									selectedText[i] = $(selected).text();
								}
							);
							extpost[this.name]= selectedText.join(",");
						break;								
						case "password":
						case "text":
						case "textarea":
						case "button":
							postdata[this.name] = $(this).val();
							
						break;
					}
					if($t.p.autoencode) { postdata[this.name] = $.jgrid.htmlEncode(postdata[this.name]); }
					}
				});
				return true;
			}
			function createData(rowid,obj,tb,maxcols){
				var nm, hc,trdata, cnt=0,tmp, dc,elc, retpos=[], ind=false,
				tdtmpl = "<td class='CaptionTD'>&#160;</td><td class='DataTD'>&#160;</td>", tmpl="", i; //*2
				for (i =1; i<=maxcols;i++) {
					tmpl += tdtmpl;
				}
				if(rowid != '_empty') {
					ind = $(obj).jqGrid("getInd",rowid);
				}
				$(obj.p.colModel).each( function(i) {
					nm = this.name;
					// hidden fields are included in the form
					if(this.editrules && this.editrules.edithidden === true) {
						hc = false;
					} else {
						hc = this.hidden === true ? true : false;
					}
					dc = hc ? "style='display:none'" : "";
					if ( nm !== 'cb' && nm !== 'subgrid' && this.editable===true && nm !== 'rn') {
						if(ind === false) {
							tmp = "";
						} else {
							if(nm == obj.p.ExpandColumn && obj.p.treeGrid === true) {
								tmp = $("td:eq("+i+")",obj.rows[ind]).text();
							} else {
								try {
									tmp =  $.unformat($("td:eq("+i+")",obj.rows[ind]),{rowId:rowid, colModel:this},i);
								} catch (_) {
									tmp =  (this.edittype && this.edittype == "textarea") ? $("td:eq("+i+")",obj.rows[ind]).text() : $("td:eq("+i+")",obj.rows[ind]).html();
								}
								if(tmp == "&nbsp;" || tmp == "&#160;" || (tmp.length==1 && tmp.charCodeAt(0)==160) ) {tmp='';}
							}
						}
						var opt = $.extend({}, this.editoptions || {} ,{id:nm,name:nm}),
						frmopt = $.extend({}, {elmprefix:'',elmsuffix:'',rowabove:false,rowcontent:''}, this.formoptions || {}),
						rp = parseInt(frmopt.rowpos,10) || cnt+1,
						cp = parseInt((parseInt(frmopt.colpos,10) || 1)*2,10);
						if(rowid == "_empty" && opt.defaultValue ) {
							tmp = $.isFunction(opt.defaultValue) ? opt.defaultValue() : opt.defaultValue; 
						}
						if(!this.edittype) { this.edittype = "text"; }
						if($t.p.autoencode) { tmp = $.jgrid.htmlDecode(tmp); }
						elc = $.jgrid.createEl(this.edittype,opt,tmp,false,$.extend({},$.jgrid.ajaxOptions,obj.p.ajaxSelectOptions || {}));
						if(tmp === "" && this.edittype == "checkbox") {tmp = $(elc).attr("offval");}
						if(tmp === "" && this.edittype == "select") {tmp = $("option:eq(0)",elc).text();}
						if(rp_ge.checkOnSubmit || rp_ge.checkOnUpdate) { rp_ge._savedData[nm] = tmp; }
						$(elc).addClass("FormElement");
						if(this.edittype == 'text' || this.edittype == 'textarea') {
							$(elc).addClass("ui-widget-content ui-corner-all");
						}
						trdata = $(tb).find("tr[rowpos="+rp+"]");
						if(frmopt.rowabove) {
							var newdata = $("<tr><td class='contentinfo' colspan='"+(maxcols*2)+"'>"+frmopt.rowcontent+"</td></tr>");
							$(tb).append(newdata);
							newdata[0].rp = rp;
						}
						if ( trdata.length===0 ) {
							trdata = $("<tr "+dc+" rowpos='"+rp+"'></tr>").addClass("FormData").attr("id","tr_"+nm);
							$(trdata).append(tmpl);
							$(tb).append(trdata);
							trdata[0].rp = rp;
						}
						$("td:eq("+(cp-2)+")",trdata[0]).html( typeof frmopt.label === 'undefined' ? obj.p.colNames[i]: frmopt.label);
						$("td:eq("+(cp-1)+")",trdata[0]).append(frmopt.elmprefix).append(elc).append(frmopt.elmsuffix);
						retpos[cnt] = i;
						cnt++;
					}
				});
				if( cnt > 0) {
					var idrow = $("<tr class='FormData' style='display:none'><td class='CaptionTD'></td><td colspan='"+ (maxcols*2-1)+"' class='DataTD'><input class='FormElement' id='id_g' type='text' name='"+obj.p.id+"_id' value='"+rowid+"'/></td></tr>");
					idrow[0].rp = cnt+999;
					$(tb).append(idrow);
					if(rp_ge.checkOnSubmit || rp_ge.checkOnUpdate) { rp_ge._savedData[obj.p.id+"_id"] = rowid; }
				}
				return retpos;
			}
			function fillData(rowid,obj,fmid){
				var nm,cnt=0,tmp, fld,opt,vl,vlc;
				if(rp_ge.checkOnSubmit || rp_ge.checkOnUpdate) {rp_ge._savedData = {};rp_ge._savedData[obj.p.id+"_id"]=rowid;}
				var cm = obj.p.colModel;
				if(rowid == '_empty') {
					$(cm).each(function(i){
						nm = this.name;
						opt = $.extend({}, this.editoptions || {} );
						fld = $("#"+$.jgrid.jqID(nm),"#"+fmid);
						if(fld && fld.length && fld[0] !== null) {
							vl = "";
							if(opt.defaultValue ) {
								vl = $.isFunction(opt.defaultValue) ? opt.defaultValue() : opt.defaultValue;
								if(fld[0].type=='checkbox') {
									vlc = vl.toLowerCase();
									if(vlc.search(/(false|0|no|off|undefined)/i)<0 && vlc!=="") {
										fld[0].checked = true;
										fld[0].defaultChecked = true;
										fld[0].value = vl;
									} else {
										fld.attr({checked:"",defaultChecked:""});
									}
								} else {fld.val(vl); }
							} else {
								if( fld[0].type=='checkbox' ) {
									fld[0].checked = false;
									fld[0].defaultChecked = false;
									vl = $(fld).attr("offval");
								} else if (fld[0].type && fld[0].type.substr(0,6)=='select') {
									fld[0].selectedIndex = 0; 
								} else {
									fld.val(vl);
								}
							}
							if(rp_ge.checkOnSubmit===true || rp_ge.checkOnUpdate) { rp_ge._savedData[nm] = vl; }
						}
					});
					$("#id_g","#"+fmid).val(rowid);
					return;
				}
				var tre = $(obj).jqGrid("getInd",rowid,true);
				if(!tre) { return; }
				$('td',tre).each( function(i) {
					nm = cm[i].name;
					// hidden fields are included in the form
					if ( nm !== 'cb' && nm !== 'subgrid' && nm !== 'rn' && cm[i].editable===true) {
						if(nm == obj.p.ExpandColumn && obj.p.treeGrid === true) {
							tmp = $(this).text();
						} else {
							try {
								tmp =  $.unformat($(this),{rowId:rowid, colModel:cm[i]},i);
							} catch (_) {
								tmp = cm[i].edittype=="textarea" ? $(this).text() : $(this).html();
							}
						}
						if($t.p.autoencode) { tmp = $.jgrid.htmlDecode(tmp); }
						if(rp_ge.checkOnSubmit===true || rp_ge.checkOnUpdate) { rp_ge._savedData[nm] = tmp; }
						nm = $.jgrid.jqID(nm);
						switch (cm[i].edittype) {
							case "password":
							case "text":
							case "button" :
							case "image":
							case "textarea":
								if(tmp == "&nbsp;" || tmp == "&#160;" || (tmp.length==1 && tmp.charCodeAt(0)==160) ) {tmp='';}
								$("#"+nm,"#"+fmid).val(tmp);
								break;
							case "select":
								var opv = tmp.split(",");
								opv = $.map(opv,function(n){return $.trim(n);});
								$("#"+nm+" option","#"+fmid).each(function(j){
									if (!cm[i].editoptions.multiple && (opv[0] == $.trim($(this).text()) || opv[0] == $.trim($(this).val())) ){
										this.selected= true;
									} else if (cm[i].editoptions.multiple){
										if(  $.inArray($.trim($(this).text()), opv ) > -1 || $.inArray($.trim($(this).val()), opv ) > -1  ){
											this.selected = true;
										}else{
											this.selected = false;
										}
									} else {
										this.selected = false;
									}
								});
								break;
							case "checkbox":
								tmp = tmp+"";
								if(cm[i].editoptions && cm[i].editoptions.value) {
									var cb = cm[i].editoptions.value.split(":");
									if(cb[0] == tmp) {
										$("#"+nm,"#"+fmid).attr("checked",true);
										$("#"+nm,"#"+fmid).attr("defaultChecked",true); //ie
									} else {
										$("#"+nm,"#"+fmid).attr("checked",false);
										$("#"+nm,"#"+fmid).attr("defaultChecked",""); //ie
									}
								} else {
									tmp = tmp.toLowerCase();
									if(tmp.search(/(false|0|no|off|undefined)/i)<0 && tmp!=="") {
										$("#"+nm,"#"+fmid).attr("checked",true);
										$("#"+nm,"#"+fmid).attr("defaultChecked",true); //ie
									} else {
										$("#"+nm,"#"+fmid).attr("checked",false);
										$("#"+nm,"#"+fmid).attr("defaultChecked",""); //ie
									}
								}
								break;
							case 'custom' :
								try {
									if(cm[i].editoptions && $.isFunction(cm[i].editoptions.custom_value)) {
										cm[i].editoptions.custom_value($("#"+nm,"#"+fmid),'set',tmp);
									} else { throw "e1"; }
								} catch (e) {
									if (e=="e1") { $.jgrid.info_dialog(jQuery.jgrid.errors.errcap,"function 'custom_value' "+$.jgrid.edit.msg.nodefined,jQuery.jgrid.edit.bClose);}
									else { $.jgrid.info_dialog(jQuery.jgrid.errors.errcap,e.message,jQuery.jgrid.edit.bClose); }
								}
								break;
						}
						cnt++;
					}
				});
				if(cnt>0) { $("#id_g","#"+frmtb).val(rowid); }
			}
			function postIt() {
				var copydata, ret=[true,"",""], onCS = {}, opers = $t.p.prmNames, idname, oper, key, selr;
				if($.isFunction(rp_ge.beforeCheckValues)) {
					var retvals = rp_ge.beforeCheckValues(postdata,$("#"+frmgr),postdata[$t.p.id+"_id"] == "_empty" ? opers.addoper : opers.editoper);
					if(retvals && typeof(retvals) === 'object') { postdata = retvals; }
				}
				for( key in postdata ){
					if(postdata.hasOwnProperty(key)) {
						ret = $.jgrid.checkValues(postdata[key],key,$t);
						if(ret[0] === false) { break; }
					}
				}
				if(ret[0]) {
					if( $.isFunction( rp_ge.onclickSubmit)) { onCS = rp_ge.onclickSubmit(rp_ge,postdata) || {}; }
					if( $.isFunction(rp_ge.beforeSubmit))  { ret = rp_ge.beforeSubmit(postdata,$("#"+frmgr)); }
				}

				if(ret[0] && !rp_ge.processing) {
					rp_ge.processing = true;
					$("#sData", "#"+frmtb+"_2").addClass('ui-state-active');
					oper = opers.oper;
					idname = opers.id;
					// we add to pos data array the action - the name is oper
					postdata[oper] = ($.trim(postdata[$t.p.id+"_id"]) == "_empty") ? opers.addoper : opers.editoper;
					if(postdata[oper] != opers.addoper) {
						postdata[idname] = postdata[$t.p.id+"_id"];
					} else {
						// check to see if we have allredy this field in the form and if yes lieve it
						if( postdata[idname] === undefined ) { postdata[idname] = postdata[$t.p.id+"_id"]; }
					}
					delete postdata[$t.p.id+"_id"];
					postdata = $.extend(postdata,rp_ge.editData,onCS);
					if($t.p.treeGrid === true)  {
						if(postdata[oper] == opers.addoper) {
						selr = $($t).jqGrid("getGridParam", 'selrow');
							var tr_par_id = $t.p.treeGridModel == 'adjacency' ? $t.p.treeReader.parent_id_field : 'parent_id';
							postdata[tr_par_id] = selr;
						}
						for(i in $t.p.treeReader){
							var itm = $t.p.treeReader[i];
							if(postdata.hasOwnProperty(itm)) {
								if(postdata[oper] == opers.addoper && i === 'parent_id_field') { continue; }
								delete postdata[itm];
					}
					}
					}

					var ajaxOptions = $.extend({
						url: rp_ge.url ? rp_ge.url : $($t).jqGrid('getGridParam','editurl'),
						type: rp_ge.mtype,
						data: $.isFunction(rp_ge.serializeEditData) ? rp_ge.serializeEditData(postdata) :  postdata,
						complete:function(data,Status){
							if(Status != "success") {
							    ret[0] = false;
							    if ($.isFunction(rp_ge.errorTextFormat)) {
							        ret[1] = rp_ge.errorTextFormat(data);
							    } else {
							        ret[1] = Status + " Status: '" + data.statusText + "'. Error code: " + data.status;
								}
							} else {
								// data is posted successful
								// execute aftersubmit with the returned data from server
								if( $.isFunction(rp_ge.afterSubmit) ) {
									ret = rp_ge.afterSubmit(data,postdata);
								}
							}
							if(ret[0] === false) {
								$("#FormError>td","#"+frmtb).html(ret[1]);
								$("#FormError","#"+frmtb).show();
							} else {
								// remove some values if formattaer select or checkbox
								$.each($t.p.colModel, function(i,n){
									if(extpost[this.name] && this.formatter && this.formatter=='select') {
										try {delete extpost[this.name];} catch (e) {}
									}
								});
								postdata = $.extend(postdata,extpost);
								if($t.p.autoencode) {
									$.each(postdata,function(n,v){
										postdata[n] = $.jgrid.htmlDecode(v);
									});
								}
								rp_ge.reloadAfterSubmit = rp_ge.reloadAfterSubmit && $t.p.datatype != "local";
								// the action is add
								if(postdata[oper] == opers.addoper ) {
									//id processing
									// user not set the id ret[2]
									if(!ret[2]) { ret[2] = $.jgrid.randId(); }
									postdata[idname] = ret[2];
									if(rp_ge.closeAfterAdd) {
										if(rp_ge.reloadAfterSubmit) { $($t).trigger("reloadGrid"); }
										else {
											if($t.p.treeGrid === true){
												$($t).jqGrid("addChildNode",ret[2],selr,postdata );
											} else {
											$($t).jqGrid("addRowData",ret[2],postdata,p.addedrow);
											$($t).jqGrid("setSelection",ret[2]);
										}
										}
										$.jgrid.hideModal("#"+IDs.themodal,{gb:"#gbox_"+gID,jqm:p.jqModal,onClose: rp_ge.onClose});
									} else if (rp_ge.clearAfterAdd) {
										if(rp_ge.reloadAfterSubmit) { $($t).trigger("reloadGrid"); }
										else {
											if($t.p.treeGrid === true){
												$($t).jqGrid("addChildNode",ret[2],selr,postdata );
											} else {
												$($t).jqGrid("addRowData",ret[2],postdata,p.addedrow);
											}
										}
										fillData("_empty",$t,frmgr);
									} else {
										if(rp_ge.reloadAfterSubmit) { $($t).trigger("reloadGrid"); }
										else {
											if($t.p.treeGrid === true){
												$($t).jqGrid("addChildNode",ret[2],selr,postdata );
											} else {
												$($t).jqGrid("addRowData",ret[2],postdata,p.addedrow);
									}
										}
									}
								} else {
									// the action is update
									if(rp_ge.reloadAfterSubmit) {
										$($t).trigger("reloadGrid");
										if( !rp_ge.closeAfterEdit ) { setTimeout(function(){$($t).jqGrid("setSelection",postdata[idname]);},1000); }
									} else {
										if($t.p.treeGrid === true) {
											$($t).jqGrid("setTreeRow",postdata[idname],postdata);
										} else {
											$($t).jqGrid("setRowData",postdata[idname],postdata);
										}
									}
									if(rp_ge.closeAfterEdit) { $.jgrid.hideModal("#"+IDs.themodal,{gb:"#gbox_"+gID,jqm:p.jqModal,onClose: rp_ge.onClose}); }
								}
								if($.isFunction(rp_ge.afterComplete)) {
									copydata = data;
									setTimeout(function(){rp_ge.afterComplete(copydata,postdata,$("#"+frmgr));copydata=null;},500);
								}
							if(rp_ge.checkOnSubmit || rp_ge.checkOnUpdate) {
								$("#"+frmgr).data("disabled",false);
								if(rp_ge._savedData[$t.p.id+"_id"] !="_empty"){
									for(var key in rp_ge._savedData) {
										if(postdata[key]) {
											rp_ge._savedData[key] = postdata[key];
										}
									}
								}
							}
							}
							rp_ge.processing=false;
							$("#sData", "#"+frmtb+"_2").removeClass('ui-state-active');
							try{$(':input:visible',"#"+frmgr)[0].focus();} catch (e){}
						}
					}, $.jgrid.ajaxOptions, rp_ge.ajaxEditOptions );
					
					if (!ajaxOptions.url && !rp_ge.useDataProxy) {
						if ($.isFunction($t.p.dataProxy)) {
							rp_ge.useDataProxy = true;
						} else {
							ret[0]=false; ret[1] += " "+$.jgrid.errors.nourl;
						}
					}
					if (ret[0]) { 
						if (rp_ge.useDataProxy) { $t.p.dataProxy.call($t, ajaxOptions, "set_"+$t.p.id); }
						else { $.ajax(ajaxOptions); }
					}
				}
				if(ret[0] === false) {
					$("#FormError>td","#"+frmtb).html(ret[1]);
					$("#FormError","#"+frmtb).show();
					// return; 
				}
			}
			function compareData(nObj, oObj ) {
				var ret = false,key;
				for (key in nObj) {
					if(nObj[key] != oObj[key]) {
						ret = true;
						break;
					}
				}
				return ret;
			}
			function setNulls() {
				$.each($t.p.colModel, function(i,n){
					if(n.editoptions && n.editoptions.NullIfEmpty === true) {
						if(postdata.hasOwnProperty(n.name) && postdata[n.name] == "") {
							postdata[n.name] = 'null';
						}
					}
				});
			}
			function checkUpdates () {
				var stat = true;
				$("#FormError","#"+frmtb).hide();
				if(rp_ge.checkOnUpdate) {
					postdata = {}; extpost={};
					getFormData();
					newData = $.extend({},postdata,extpost);
					diff = compareData(newData,rp_ge._savedData);
					if(diff) {
						$("#"+frmgr).data("disabled",true);
						$(".confirm","#"+IDs.themodal).show();
						stat = false;
					} else {
						setNulls();
					}
				}
				return stat;
			}
			function restoreInline()
			{
				if (rowid !== "_empty" && typeof($t.p.savedRow) !== "undefined" && $t.p.savedRow.length > 0 && $.isFunction($.fn.jqGrid.restoreRow)) {
					for (var i=0;i<$t.p.savedRow.length;i++) {
						if ($t.p.savedRow[i].id == rowid) {
							$($t).jqGrid('restoreRow',rowid);
							break;
						}
					}
				}
			}
			function updateNav(cr,totr){
				if (cr===0) { $("#pData","#"+frmtb+"_2").addClass('ui-state-disabled'); } else { $("#pData","#"+frmtb+"_2").removeClass('ui-state-disabled'); }
				if (cr==totr) { $("#nData","#"+frmtb+"_2").addClass('ui-state-disabled'); } else { $("#nData","#"+frmtb+"_2").removeClass('ui-state-disabled'); }
			}
			function getCurrPos() {
				var rowsInGrid = $($t).jqGrid("getDataIDs"),
				selrow = $("#id_g","#"+frmtb).val(),
				pos = $.inArray(selrow,rowsInGrid);
				return [pos,rowsInGrid];
			}

			if ( $("#"+IDs.themodal).html() !== null ) {
				if(onBeforeInit) {
					showFrm = onBeforeInit($("#"+frmgr));
					if(typeof(showFrm) == "undefined") {
						showFrm = true;
					}
				}
				if(showFrm === false) { return; }
				restoreInline();
				$(".ui-jqdialog-title","#"+IDs.modalhead).html(p.caption);
				$("#FormError","#"+frmtb).hide();
				if(rp_ge.topinfo) {
					$(".topinfo","#"+frmtb+"_2").html(rp_ge.topinfo);
					$(".tinfo","#"+frmtb+"_2").show();
				} else {
					$(".tinfo","#"+frmtb+"_2").hide();
				}
				if(rp_ge.bottominfo) {
					$(".bottominfo","#"+frmtb+"_2").html(rp_ge.bottominfo);
					$(".binfo","#"+frmtb+"_2").show();
				} else {
					$(".binfo","#"+frmtb+"_2").hide();
				}
				// filldata
				fillData(rowid,$t,frmgr);
				///
				if(rowid=="_empty" || !rp_ge.viewPagerButtons) {
					$("#pData, #nData","#"+frmtb+"_2").hide();
				} else { 
					$("#pData, #nData","#"+frmtb+"_2").show();
				}
				if(rp_ge.processing===true) {
					rp_ge.processing=false;
					$("#sData", "#"+frmtb+"_2").removeClass('ui-state-active');
				}
				if($("#"+frmgr).data("disabled")===true) {
					$(".confirm","#"+IDs.themodal).hide();
					$("#"+frmgr).data("disabled",false);
				}
				if(onBeforeShow) { onBeforeShow($("#"+frmgr)); }
				$("#"+IDs.themodal).data("onClose",rp_ge.onClose);
				$.jgrid.viewModal("#"+IDs.themodal,{gbox:"#gbox_"+gID,jqm:p.jqModal, jqM: false, overlay: p.overlay, modal:p.modal});
				if(!closeovrl) {
					$(".jqmOverlay").click(function(){
						if(!checkUpdates()) { return false; }
						$.jgrid.hideModal("#"+IDs.themodal,{gb:"#gbox_"+gID,jqm:p.jqModal, onClose: rp_ge.onClose});
						return false;
					});
				}
				if(onAfterShow) { onAfterShow($("#"+frmgr)); }
			} else {
				var dh = isNaN(p.dataheight) ? p.dataheight : p.dataheight+"px",
				frm = $("<form name='FormPost' id='"+frmgr+"' class='FormGrid' onSubmit='return false;' style='width:100%;overflow:auto;position:relative;height:"+dh+";'></form>").data("disabled",false),
				tbl = $("<table id='"+frmtb+"' class='EditTable' cellspacing='0' cellpadding='0' border='0'><tbody></tbody></table>");
				if(onBeforeInit) {
					showFrm = onBeforeInit($("#"+frmgr));
					if(typeof(showFrm) == "undefined") {
						showFrm = true;
					}
				}
				if(showFrm === false) { return; }
				restoreInline();
				$($t.p.colModel).each( function(i) {
					var fmto = this.formoptions;
					maxCols = Math.max(maxCols, fmto ? fmto.colpos || 0 : 0 );
					maxRows = Math.max(maxRows, fmto ? fmto.rowpos || 0 : 0 );
				});
				$(frm).append(tbl);
				var flr = $("<tr id='FormError' style='display:none'><td class='ui-state-error' colspan='"+(maxCols*2)+"'></td></tr>");
				flr[0].rp = 0;
				$(tbl).append(flr);
				//topinfo
				flr = $("<tr style='display:none' class='tinfo'><td class='topinfo' colspan='"+(maxCols*2)+"'>"+rp_ge.topinfo+"</td></tr>");
				flr[0].rp = 0;
				$(tbl).append(flr);
				// set the id.
				// use carefull only to change here colproperties.
				// create data
				var rtlb = $t.p.direction == "rtl" ? true :false,
				bp = rtlb ? "nData" : "pData",
				bn = rtlb ? "pData" : "nData";
				createData(rowid,$t,tbl,maxCols);
				// buttons at footer
				var bP = "<a href='javascript:void(0)' id='"+bp+"' class='fm-button ui-state-default ui-corner-left'><span class='ui-icon ui-icon-triangle-1-w'></span></a>",
				bN = "<a href='javascript:void(0)' id='"+bn+"' class='fm-button ui-state-default ui-corner-right'><span class='ui-icon ui-icon-triangle-1-e'></span></a>",
				bS  ="<a href='javascript:void(0)' id='sData' class='fm-button ui-state-default ui-corner-all'>"+p.bSubmit+"</a>",
				bC  ="<a href='javascript:void(0)' id='cData' class='fm-button ui-state-default ui-corner-all'>"+p.bCancel+"</a>";
				var bt = "<table border='0' cellspacing='0' cellpadding='0' class='EditTable' id='"+frmtb+"_2'><tbody><tr><td colspan='2'><hr class='ui-widget-content' style='margin:1px'/></td></tr><tr id='Act_Buttons'><td class='navButton'>"+(rtlb ? bN+bP : bP+bN)+"</td><td class='EditButton'>"+bS+bC+"</td></tr>";
				bt += "<tr style='display:none' class='binfo'><td class='bottominfo' colspan='2'>"+rp_ge.bottominfo+"</td></tr>";
				bt += "</tbody></table>";
				if(maxRows >  0) {
					var sd=[];
					$.each($(tbl)[0].rows,function(i,r){
						sd[i] = r;
					});
					sd.sort(function(a,b){
						if(a.rp > b.rp) {return 1;}
						if(a.rp < b.rp) {return -1;}
						return 0;
					});
					$.each(sd, function(index, row) {
						$('tbody',tbl).append(row);
					});
				}
				p.gbox = "#gbox_"+gID;
				var cle = false;
				if(p.closeOnEscape===true){
					p.closeOnEscape = false;
					cle = true;
				}
				var tms = $("<span></span>").append(frm).append(bt);
				$.jgrid.createModal(IDs,tms,p,"#gview_"+$t.p.id,$("#gbox_"+$t.p.id)[0]);
				if(rtlb) {
					$("#pData, #nData","#"+frmtb+"_2").css("float","right");
					$(".EditButton","#"+frmtb+"_2").css("text-align","left");
				}
				if(rp_ge.topinfo) { $(".tinfo","#"+frmtb+"_2").show(); }
				if(rp_ge.bottominfo) { $(".binfo","#"+frmtb+"_2").show(); } 
				tms = null; bt=null;
				$("#"+IDs.themodal).keydown( function( e ) {
					var wkey = e.target;
					if ($("#"+frmgr).data("disabled")===true ) { return false; }//??
					if(rp_ge.savekey[0] === true && e.which == rp_ge.savekey[1]) { // save
						if(wkey.tagName != "TEXTAREA") {
							$("#sData", "#"+frmtb+"_2").trigger("click");
							return false;
						}
					}
					if(e.which === 27) {
						if(!checkUpdates()) { return false; }
						if(cle)	{ $.jgrid.hideModal(this,{gb:p.gbox,jqm:p.jqModal, onClose: rp_ge.onClose}); }
						return false;
					}
					if(rp_ge.navkeys[0]===true) {
						if($("#id_g","#"+frmtb).val() == "_empty") { return true; }
						if(e.which == rp_ge.navkeys[1]){ //up
							$("#pData", "#"+frmtb+"_2").trigger("click");
							return false;
						}
						if(e.which == rp_ge.navkeys[2]){ //down
							$("#nData", "#"+frmtb+"_2").trigger("click");
							return false;
						}
					}
				});
				if(p.checkOnUpdate) {
					$("a.ui-jqdialog-titlebar-close span","#"+IDs.themodal).removeClass("jqmClose");
					$("a.ui-jqdialog-titlebar-close","#"+IDs.themodal).unbind("click")
					.click(function(){
						if(!checkUpdates()) { return false; }
						$.jgrid.hideModal("#"+IDs.themodal,{gb:"#gbox_"+gID,jqm:p.jqModal,onClose: rp_ge.onClose});
						return false;
					});
				}
				p.saveicon = $.extend([true,"left","ui-icon-disk"],p.saveicon);
				p.closeicon = $.extend([true,"left","ui-icon-close"],p.closeicon);
				// beforeinitdata after creation of the form
				if(p.saveicon[0]===true) {
					$("#sData","#"+frmtb+"_2").addClass(p.saveicon[1] == "right" ? 'fm-button-icon-right' : 'fm-button-icon-left')
					.append("<span class='ui-icon "+p.saveicon[2]+"'></span>");
				}
				if(p.closeicon[0]===true) {
					$("#cData","#"+frmtb+"_2").addClass(p.closeicon[1] == "right" ? 'fm-button-icon-right' : 'fm-button-icon-left')
					.append("<span class='ui-icon "+p.closeicon[2]+"'></span>");
				}
				if(rp_ge.checkOnSubmit || rp_ge.checkOnUpdate) {
					bS  ="<a href='javascript:void(0)' id='sNew' class='fm-button ui-state-default ui-corner-all' style='z-index:1002'>"+p.bYes+"</a>";
					bN  ="<a href='javascript:void(0)' id='nNew' class='fm-button ui-state-default ui-corner-all' style='z-index:1002'>"+p.bNo+"</a>";
					bC  ="<a href='javascript:void(0)' id='cNew' class='fm-button ui-state-default ui-corner-all' style='z-index:1002'>"+p.bExit+"</a>";
					var ii, zI = p.zIndex  || 999; zI ++;
					if ($.browser.msie && $.browser.version ==6) {
						ii = '<iframe style="display:block;position:absolute;z-index:-1;filter:Alpha(Opacity=\'0\');" src="javascript:false;"></iframe>';
					} else { ii="";}
					$("<div class='ui-widget-overlay jqgrid-overlay confirm' style='z-index:"+zI+";display:none;'>&#160;"+ii+"</div><div class='confirm ui-widget-content ui-jqconfirm' style='z-index:"+(zI+1)+"'>"+p.saveData+"<br/><br/>"+bS+bN+bC+"</div>").insertAfter("#"+frmgr);
					$("#sNew","#"+IDs.themodal).click(function(){
						postIt();
						$("#"+frmgr).data("disabled",false);
						$(".confirm","#"+IDs.themodal).hide();
						return false;
					});
					$("#nNew","#"+IDs.themodal).click(function(){
						$(".confirm","#"+IDs.themodal).hide();
						$("#"+frmgr).data("disabled",false);
						setTimeout(function(){$(":input","#"+frmgr)[0].focus();},0);
						return false;
					});
					$("#cNew","#"+IDs.themodal).click(function(){
						$(".confirm","#"+IDs.themodal).hide();
						$("#"+frmgr).data("disabled",false);
						$.jgrid.hideModal("#"+IDs.themodal,{gb:"#gbox_"+gID,jqm:p.jqModal,onClose: rp_ge.onClose});
						return false;
					});
				}
				// here initform - only once
				if(onInitializeForm) { onInitializeForm($("#"+frmgr)); }
				if(rowid=="_empty" || !rp_ge.viewPagerButtons) { $("#pData,#nData","#"+frmtb+"_2").hide(); } else { $("#pData,#nData","#"+frmtb+"_2").show(); }
				if(onBeforeShow) { onBeforeShow($("#"+frmgr)); }
				$("#"+IDs.themodal).data("onClose",rp_ge.onClose);
				$.jgrid.viewModal("#"+IDs.themodal,{gbox:"#gbox_"+gID,jqm:p.jqModal, overlay: p.overlay,modal:p.modal});
				if(!closeovrl) {
					$(".jqmOverlay").click(function(){
						if(!checkUpdates()) { return false; }
						$.jgrid.hideModal("#"+IDs.themodal,{gb:"#gbox_"+gID,jqm:p.jqModal, onClose: rp_ge.onClose});
						return false;
					});
				}
				if(onAfterShow) { onAfterShow($("#"+frmgr)); }
				$(".fm-button","#"+IDs.themodal).hover(
				   function(){$(this).addClass('ui-state-hover');}, 
				   function(){$(this).removeClass('ui-state-hover');}
				);
				$("#sData", "#"+frmtb+"_2").click(function(e){
					postdata = {}; extpost={};
					$("#FormError","#"+frmtb).hide();
					// all depend on ret array
					//ret[0] - succes
					//ret[1] - msg if not succes
					//ret[2] - the id  that will be set if reload after submit false
					getFormData();
					setNulls();
					if(postdata[$t.p.id+"_id"] == "_empty")	{ postIt(); }
					else if(p.checkOnSubmit===true ) {
						newData = $.extend({},postdata,extpost);
						diff = compareData(newData,rp_ge._savedData);
						if(diff) {
							$("#"+frmgr).data("disabled",true);
							$(".confirm","#"+IDs.themodal).show();
						} else {
							postIt();
						}
					} else {
						postIt();
					}
					return false;
				});
				$("#cData", "#"+frmtb+"_2").click(function(e){
					if(!checkUpdates()) { return false; }
					$.jgrid.hideModal("#"+IDs.themodal,{gb:"#gbox_"+gID,jqm:p.jqModal,onClose: rp_ge.onClose});
					return false;
				});
				$("#nData", "#"+frmtb+"_2").click(function(e){
					if(!checkUpdates()) { return false; }
					$("#FormError","#"+frmtb).hide();
					var npos = getCurrPos();
					npos[0] = parseInt(npos[0],10);
					if(npos[0] != -1 && npos[1][npos[0]+1]) {
						if($.isFunction(p.onclickPgButtons)) {
							p.onclickPgButtons('next',$("#"+frmgr),npos[1][npos[0]]);
						}
						fillData(npos[1][npos[0]+1],$t,frmgr);
						$($t).jqGrid("setSelection",npos[1][npos[0]+1]);
						if($.isFunction(p.afterclickPgButtons)) {
							p.afterclickPgButtons('next',$("#"+frmgr),npos[1][npos[0]+1]);
						}
						updateNav(npos[0]+1,npos[1].length-1);
					}
					return false;
				});
				$("#pData", "#"+frmtb+"_2").click(function(e){
					if(!checkUpdates()) { return false; }
					$("#FormError","#"+frmtb).hide();
					var ppos = getCurrPos();
					if(ppos[0] != -1 && ppos[1][ppos[0]-1]) {
						if($.isFunction(p.onclickPgButtons)) {
							p.onclickPgButtons('prev',$("#"+frmgr),ppos[1][ppos[0]]);
						}
						fillData(ppos[1][ppos[0]-1],$t,frmgr);
						$($t).jqGrid("setSelection",ppos[1][ppos[0]-1]);
						if($.isFunction(p.afterclickPgButtons)) {
							p.afterclickPgButtons('prev',$("#"+frmgr),ppos[1][ppos[0]-1]);
						}
						updateNav(ppos[0]-1,ppos[1].length-1);
					}
					return false;
				});
			}
			var posInit =getCurrPos();
			updateNav(posInit[0],posInit[1].length-1);

		});
	},
	viewGridRow : function(rowid, p){
		p = $.extend({
			top : 0,
			left: 0,
			width: 0,
			height: 'auto',
			dataheight: 'auto',
			modal: false,
			overlay: 10,
			drag: true,
			resize: true,
			jqModal: true,
			closeOnEscape : false,
			labelswidth: '30%',
			closeicon: [],
			navkeys: [false,38,40],
			onClose: null,
			beforeShowForm : null,
			beforeInitData : null,
			viewPagerButtons : true
		}, $.jgrid.view, p || {});
		return this.each(function(){
			var $t = this;
			if (!$t.grid || !rowid) { return; }
			if(!p.imgpath) { p.imgpath= $t.p.imgpath; }
			// I hate to rewrite code, but ...
			var gID = $t.p.id,
			frmgr = "ViewGrid_"+gID , frmtb = "ViewTbl_"+gID,
			IDs = {themodal:'viewmod'+gID,modalhead:'viewhd'+gID,modalcontent:'viewcnt'+gID, scrollelm : frmgr},
			onBeforeInit = $.isFunction(p.beforeInitData) ? p.beforeInitData : false,
			showFrm = true,
			maxCols = 1, maxRows=0;
			function focusaref(){ //Sfari 3 issues
				if(p.closeOnEscape===true || p.navkeys[0]===true) {
					setTimeout(function(){$(".ui-jqdialog-titlebar-close","#"+IDs.modalhead).focus();},0);
				}
			}
			function createData(rowid,obj,tb,maxcols){
				var nm, hc,trdata, cnt=0,tmp, dc, retpos=[], ind=false,
				tdtmpl = "<td class='CaptionTD form-view-label ui-widget-content' width='"+p.labelswidth+"'>&#160;</td><td class='DataTD form-view-data ui-helper-reset ui-widget-content'>&#160;</td>", tmpl="",
				tdtmpl2 = "<td class='CaptionTD form-view-label ui-widget-content'>&#160;</td><td class='DataTD form-view-data ui-widget-content'>&#160;</td>",
				fmtnum = ['integer','number','currency'],max1 =0, max2=0 ,maxw,setme, viewfld;
				for (var i =1;i<=maxcols;i++) {
					tmpl += i == 1 ? tdtmpl : tdtmpl2;
				}
				// find max number align rigth with property formatter
				$(obj.p.colModel).each( function(i) {
					if(this.editrules && this.editrules.edithidden === true) {
						hc = false;
					} else {
						hc = this.hidden === true ? true : false;
					}
					if(!hc && this.align==='right') {
						if(this.formatter && $.inArray(this.formatter,fmtnum) !== -1 ) {
							max1 = Math.max(max1,parseInt(this.width,10));
						} else {
							max2 = Math.max(max2,parseInt(this.width,10));
						}
					}
				});
				maxw  = max1 !==0 ? max1 : max2 !==0 ? max2 : 0;
				ind = $(obj).jqGrid("getInd",rowid);
				$(obj.p.colModel).each( function(i) {
					nm = this.name;
					setme = false;
					// hidden fields are included in the form
					if(this.editrules && this.editrules.edithidden === true) {
						hc = false;
					} else {
						hc = this.hidden === true ? true : false;
					}
					dc = hc ? "style='display:none'" : "";
					viewfld = (typeof this.viewable != 'boolean') ? true : this.viewable;
					if ( nm !== 'cb' && nm !== 'subgrid' && nm !== 'rn' && viewfld) {
						if(ind === false) {
							tmp = "";
						} else {
							if(nm == obj.p.ExpandColumn && obj.p.treeGrid === true) {
								tmp = $("td:eq("+i+")",obj.rows[ind]).text();
							} else {
								tmp = $("td:eq("+i+")",obj.rows[ind]).html();
							}
						}
						setme = this.align === 'right' && maxw !==0 ? true : false;
						var opt = $.extend({}, this.editoptions || {} ,{id:nm,name:nm}),
						frmopt = $.extend({},{rowabove:false,rowcontent:''}, this.formoptions || {}),
						rp = parseInt(frmopt.rowpos,10) || cnt+1,
						cp = parseInt((parseInt(frmopt.colpos,10) || 1)*2,10);
						if(frmopt.rowabove) {
							var newdata = $("<tr><td class='contentinfo' colspan='"+(maxcols*2)+"'>"+frmopt.rowcontent+"</td></tr>");
							$(tb).append(newdata);
							newdata[0].rp = rp;
						}
						trdata = $(tb).find("tr[rowpos="+rp+"]");
						if ( trdata.length===0 ) {
							trdata = $("<tr "+dc+" rowpos='"+rp+"'></tr>").addClass("FormData").attr("id","trv_"+nm);
							$(trdata).append(tmpl);
							$(tb).append(trdata);
							trdata[0].rp = rp;
						}
						$("td:eq("+(cp-2)+")",trdata[0]).html('<b>'+ (typeof frmopt.label === 'undefined' ? obj.p.colNames[i]: frmopt.label)+'</b>');
						$("td:eq("+(cp-1)+")",trdata[0]).append("<span>"+tmp+"</span>").attr("id","v_"+nm);
						if(setme){
							$("td:eq("+(cp-1)+") span",trdata[0]).css({'text-align':'right',width:maxw+"px"});
						}
						retpos[cnt] = i;
						cnt++;
					}
				});
				if( cnt > 0) {
					var idrow = $("<tr class='FormData' style='display:none'><td class='CaptionTD'></td><td colspan='"+ (maxcols*2-1)+"' class='DataTD'><input class='FormElement' id='id_g' type='text' name='id' value='"+rowid+"'/></td></tr>");
					idrow[0].rp = cnt+99;
					$(tb).append(idrow);
				}
				return retpos;
			}
			function fillData(rowid,obj){
				var nm, hc,cnt=0,tmp, opt,trv;
				trv = $(obj).jqGrid("getInd",rowid,true);
				if(!trv) { return; }
				$('td',trv).each( function(i) {
					nm = obj.p.colModel[i].name;
					// hidden fields are included in the form
					if(obj.p.colModel[i].editrules && obj.p.colModel[i].editrules.edithidden === true) {
						hc = false;
					} else {
						hc = obj.p.colModel[i].hidden === true ? true : false;
					}
					if ( nm !== 'cb' && nm !== 'subgrid' && nm !== 'rn') {
						if(nm == obj.p.ExpandColumn && obj.p.treeGrid === true) {
							tmp = $(this).text();
						} else {
							tmp = $(this).html();
						}
						opt = $.extend({},obj.p.colModel[i].editoptions || {});
						nm = $.jgrid.jqID("v_"+nm);
						$("#"+nm+" span","#"+frmtb).html(tmp);
						if (hc) { $("#"+nm,"#"+frmtb).parents("tr:first").hide(); }
						cnt++;
					}
				});
				if(cnt>0) { $("#id_g","#"+frmtb).val(rowid); }
			}
			function updateNav(cr,totr){
				if (cr===0) { $("#pData","#"+frmtb+"_2").addClass('ui-state-disabled'); } else { $("#pData","#"+frmtb+"_2").removeClass('ui-state-disabled'); }
				if (cr==totr) { $("#nData","#"+frmtb+"_2").addClass('ui-state-disabled'); } else { $("#nData","#"+frmtb+"_2").removeClass('ui-state-disabled'); }
			}
			function getCurrPos() {
				var rowsInGrid = $($t).jqGrid("getDataIDs"),
				selrow = $("#id_g","#"+frmtb).val(),
				pos = $.inArray(selrow,rowsInGrid);
				return [pos,rowsInGrid];
			}

			if ( $("#"+IDs.themodal).html() !== null ) {
				if(onBeforeInit) {
					showFrm = onBeforeInit($("#"+frmgr));
					if(typeof(showFrm) == "undefined") {
						showFrm = true;
					}
				}
				if(showFrm === false) { return; }
				$(".ui-jqdialog-title","#"+IDs.modalhead).html(p.caption);
				$("#FormError","#"+frmtb).hide();
				fillData(rowid,$t);
				if($.isFunction(p.beforeShowForm)) { p.beforeShowForm($("#"+frmgr)); }
				$.jgrid.viewModal("#"+IDs.themodal,{gbox:"#gbox_"+gID,jqm:p.jqModal, jqM: false, overlay: p.overlay, modal:p.modal});
				focusaref();
			} else {
				var dh = isNaN(p.dataheight) ? p.dataheight : p.dataheight+"px";
				var frm = $("<form name='FormPost' id='"+frmgr+"' class='FormGrid' style='width:100%;overflow:auto;position:relative;height:"+dh+";'></form>"),
				tbl =$("<table id='"+frmtb+"' class='EditTable' cellspacing='1' cellpadding='2' border='0' style='table-layout:fixed'><tbody></tbody></table>");
				if(onBeforeInit) {
					showFrm = onBeforeInit($("#"+frmgr));
					if(typeof(showFrm) == "undefined") {
						showFrm = true;
					}
				}
				if(showFrm === false) { return; }
				$($t.p.colModel).each( function(i) {
					var fmto = this.formoptions;
					maxCols = Math.max(maxCols, fmto ? fmto.colpos || 0 : 0 );
					maxRows = Math.max(maxRows, fmto ? fmto.rowpos || 0 : 0 );
				});
				// set the id.
				$(frm).append(tbl);
				createData(rowid, $t, tbl, maxCols);
				var rtlb = $t.p.direction == "rtl" ? true :false,
				bp = rtlb ? "nData" : "pData",
				bn = rtlb ? "pData" : "nData",

				// buttons at footer
				bP = "<a href='javascript:void(0)' id='"+bp+"' class='fm-button ui-state-default ui-corner-left'><span class='ui-icon ui-icon-triangle-1-w'></span></a>",
				bN = "<a href='javascript:void(0)' id='"+bn+"' class='fm-button ui-state-default ui-corner-right'><span class='ui-icon ui-icon-triangle-1-e'></span></a>",
				bC  ="<a href='javascript:void(0)' id='cData' class='fm-button ui-state-default ui-corner-all'>"+p.bClose+"</a>";
				if(maxRows >  0) {
					var sd=[];
					$.each($(tbl)[0].rows,function(i,r){
						sd[i] = r;
					});
					sd.sort(function(a,b){
						if(a.rp > b.rp) {return 1;}
						if(a.rp < b.rp) {return -1;}
						return 0;
					});
					$.each(sd, function(index, row) {
						$('tbody',tbl).append(row);
					});
				}
				p.gbox = "#gbox_"+gID;
				var cle = false;
				if(p.closeOnEscape===true){
					p.closeOnEscape = false;
					cle = true;
				}				
				var bt = $("<span></span>").append(frm).append("<table border='0' class='EditTable' id='"+frmtb+"_2'><tbody><tr id='Act_Buttons'><td class='navButton' width='"+p.labelswidth+"'>"+(rtlb ? bN+bP : bP+bN)+"</td><td class='EditButton'>"+bC+"</td></tr></tbody></table>");
				$.jgrid.createModal(IDs,bt,p,"#gview_"+$t.p.id,$("#gview_"+$t.p.id)[0]);
				if(rtlb) {
					$("#pData, #nData","#"+frmtb+"_2").css("float","right");
					$(".EditButton","#"+frmtb+"_2").css("text-align","left");
				}
				if(!p.viewPagerButtons) { $("#pData, #nData","#"+frmtb+"_2").hide(); }
				bt = null;
				$("#"+IDs.themodal).keydown( function( e ) {
					if(e.which === 27) {
						if(cle)	{ $.jgrid.hideModal(this,{gb:p.gbox,jqm:p.jqModal, onClose: p.onClose}); }
						return false;
					}
					if(p.navkeys[0]===true) {
						if(e.which === p.navkeys[1]){ //up
							$("#pData", "#"+frmtb+"_2").trigger("click");
							return false;
						}
						if(e.which === p.navkeys[2]){ //down
							$("#nData", "#"+frmtb+"_2").trigger("click");
							return false;
						}
					}
				});
				p.closeicon = $.extend([true,"left","ui-icon-close"],p.closeicon);
				if(p.closeicon[0]===true) {
					$("#cData","#"+frmtb+"_2").addClass(p.closeicon[1] == "right" ? 'fm-button-icon-right' : 'fm-button-icon-left')
					.append("<span class='ui-icon "+p.closeicon[2]+"'></span>");
				}
				if($.isFunction(p.beforeShowForm)) { p.beforeShowForm($("#"+frmgr)); }
				$.jgrid.viewModal("#"+IDs.themodal,{gbox:"#gbox_"+gID,jqm:p.jqModal, modal:p.modal});
				$(".fm-button:not(.ui-state-disabled)","#"+frmtb+"_2").hover(
				   function(){$(this).addClass('ui-state-hover');}, 
				   function(){$(this).removeClass('ui-state-hover');}
				);
				focusaref();
				$("#cData", "#"+frmtb+"_2").click(function(e){
					$.jgrid.hideModal("#"+IDs.themodal,{gb:"#gbox_"+gID,jqm:p.jqModal, onClose: p.onClose});
					return false;
				});
				$("#nData", "#"+frmtb+"_2").click(function(e){
					$("#FormError","#"+frmtb).hide();
					var npos = getCurrPos();
					npos[0] = parseInt(npos[0],10);
					if(npos[0] != -1 && npos[1][npos[0]+1]) {
						if($.isFunction(p.onclickPgButtons)) {
							p.onclickPgButtons('next',$("#"+frmgr),npos[1][npos[0]]);
						}
						fillData(npos[1][npos[0]+1],$t);
						$($t).jqGrid("setSelection",npos[1][npos[0]+1]);
						if($.isFunction(p.afterclickPgButtons)) {
							p.afterclickPgButtons('next',$("#"+frmgr),npos[1][npos[0]+1]);
						}
						updateNav(npos[0]+1,npos[1].length-1);
					}
					focusaref();
					return false;
				});
				$("#pData", "#"+frmtb+"_2").click(function(e){
					$("#FormError","#"+frmtb).hide();
					var ppos = getCurrPos();
					if(ppos[0] != -1 && ppos[1][ppos[0]-1]) {
						if($.isFunction(p.onclickPgButtons)) {
							p.onclickPgButtons('prev',$("#"+frmgr),ppos[1][ppos[0]]);
						}
						fillData(ppos[1][ppos[0]-1],$t);
						$($t).jqGrid("setSelection",ppos[1][ppos[0]-1]);
						if($.isFunction(p.afterclickPgButtons)) {
							p.afterclickPgButtons('prev',$("#"+frmgr),ppos[1][ppos[0]-1]);
						}
						updateNav(ppos[0]-1,ppos[1].length-1);
					}
					focusaref();
					return false;
				});
			}
			var posInit =getCurrPos();
			updateNav(posInit[0],posInit[1].length-1);
		});
	},
	delGridRow : function(rowids,p) {
		p = $.extend({
			top : 0,
			left: 0,
			width: 240,
			height: 'auto',
			dataheight : 'auto',
			modal: false,
			overlay: 10,
			drag: true,
			resize: true,
			url : '',
			mtype : "POST",
			reloadAfterSubmit: true,
			beforeShowForm: null,
			beforeInitData : null,
			afterShowForm: null,
			beforeSubmit: null,
			onclickSubmit: null,
			afterSubmit: null,
			jqModal : true,
			closeOnEscape : false,
			delData: {},
			delicon : [],
			cancelicon : [],
			onClose : null,
			ajaxDelOptions : {},
			processing : false,
			serializeDelData : null,
			useDataProxy : false
		}, $.jgrid.del, p ||{});
		rp_ge = p;
		return this.each(function(){
			var $t = this;
			if (!$t.grid ) { return; }
			if(!rowids) { return; }
			var onBeforeShow = $.isFunction( p.beforeShowForm  ),
			onAfterShow = $.isFunction( p.afterShowForm ),
			onBeforeInit = $.isFunction(p.beforeInitData) ? p.beforeInitData : false,
			gID = $t.p.id, onCS = {},
			showFrm = true,
			dtbl = "DelTbl_"+gID,postd, idname, opers, oper,
			IDs = {themodal:'delmod'+gID,modalhead:'delhd'+gID,modalcontent:'delcnt'+gID, scrollelm: dtbl};
			if (jQuery.isArray(rowids)) { rowids = rowids.join(); }
			if ( $("#"+IDs.themodal).html() !== null ) {
				if(onBeforeInit) {
					showFrm = onBeforeInit( $("#"+dtbl));
					if(typeof(showFrm) == "undefined") {
						showFrm = true;
					}
				}
				if(showFrm === false) { return; }
				$("#DelData>td","#"+dtbl).text(rowids);
				$("#DelError","#"+dtbl).hide();
				if( rp_ge.processing === true) {
					rp_ge.processing=false;
					$("#dData", "#"+dtbl).removeClass('ui-state-active');
				}
				if(onBeforeShow) { p.beforeShowForm($("#"+dtbl)); }
				$.jgrid.viewModal("#"+IDs.themodal,{gbox:"#gbox_"+gID,jqm:p.jqModal,jqM: false, overlay: p.overlay, modal:p.modal});
				if(onAfterShow) { p.afterShowForm($("#"+dtbl)); }
			} else {
				var dh = isNaN(p.dataheight) ? p.dataheight : p.dataheight+"px";
				var tbl = "<div id='"+dtbl+"' class='formdata' style='width:100%;overflow:auto;position:relative;height:"+dh+";'>";
				tbl += "<table class='DelTable'><tbody>";
				// error data 
				tbl += "<tr id='DelError' style='display:none'><td class='ui-state-error'></td></tr>";
				tbl += "<tr id='DelData' style='display:none'><td >"+rowids+"</td></tr>";
				tbl += "<tr><td class=\"delmsg\" style=\"white-space:pre;\">"+p.msg+"</td></tr><tr><td >&#160;</td></tr>";
				// buttons at footer
				tbl += "</tbody></table></div>";
				var bS  = "<a href='javascript:void(0)' id='dData' class='fm-button ui-state-default ui-corner-all'>"+p.bSubmit+"</a>",
				bC  = "<a href='javascript:void(0)' id='eData' class='fm-button ui-state-default ui-corner-all'>"+p.bCancel+"</a>";
				tbl += "<table cellspacing='0' cellpadding='0' border='0' class='EditTable' id='"+dtbl+"_2'><tbody><tr><td><hr class='ui-widget-content' style='margin:1px'/></td></tr></tr><tr><td class='DelButton EditButton'>"+bS+"&#160;"+bC+"</td></tr></tbody></table>";
				p.gbox = "#gbox_"+gID;
				$.jgrid.createModal(IDs,tbl,p,"#gview_"+$t.p.id,$("#gview_"+$t.p.id)[0]);
				
				if(onBeforeInit) {
					showFrm = onBeforeInit( $("#"+dtbl) );
					if(typeof(showFrm) == "undefined") {
						showFrm = true;
					}
				}
				if(showFrm === false) { return; }

				$(".fm-button","#"+dtbl+"_2").hover(
				   function(){$(this).addClass('ui-state-hover');}, 
				   function(){$(this).removeClass('ui-state-hover');}
				);
				p.delicon = $.extend([true,"left","ui-icon-scissors"],p.delicon);
				p.cancelicon = $.extend([true,"left","ui-icon-cancel"],p.cancelicon);
				if(p.delicon[0]===true) {
					$("#dData","#"+dtbl+"_2").addClass(p.delicon[1] == "right" ? 'fm-button-icon-right' : 'fm-button-icon-left')
					.append("<span class='ui-icon "+p.delicon[2]+"'></span>");
				}
				if(p.cancelicon[0]===true) {
					$("#eData","#"+dtbl+"_2").addClass(p.cancelicon[1] == "right" ? 'fm-button-icon-right' : 'fm-button-icon-left')
					.append("<span class='ui-icon "+p.cancelicon[2]+"'></span>");
				}				
				$("#dData","#"+dtbl+"_2").click(function(e){
					var ret=[true,""]; onCS = {};
					var postdata = $("#DelData>td","#"+dtbl).text(); //the pair is name=val1,val2,...
					if( $.isFunction( p.onclickSubmit ) ) { onCS = p.onclickSubmit(rp_ge, postdata) || {}; }
					if( $.isFunction( p.beforeSubmit ) ) { ret = p.beforeSubmit(postdata); }
					if(ret[0] && !rp_ge.processing) {
						rp_ge.processing = true;
						$(this).addClass('ui-state-active');
						opers = $t.p.prmNames;
						postd = $.extend({},rp_ge.delData, onCS);
						oper = opers.oper;
						postd[oper] = opers.deloper;
						idname = opers.id;
						postd[idname] = postdata;

						var ajaxOptions = $.extend({
							url: rp_ge.url ? rp_ge.url : $($t).jqGrid('getGridParam','editurl'),
							type: p.mtype,
							data: $.isFunction(p.serializeDelData) ? p.serializeDelData(postd) : postd,
							complete:function(data,Status){
								if(Status != "success") {
									ret[0] = false;
									if ($.isFunction(rp_ge.errorTextFormat)) {
										ret[1] = rp_ge.errorTextFormat(data);
									} else {
										ret[1] = Status + " Status: '" + data.statusText + "'. Error code: " + data.status;
									}
								} else {
									// data is posted successful
									// execute aftersubmit with the returned data from server
									if( $.isFunction( rp_ge.afterSubmit ) ) {
										ret = rp_ge.afterSubmit(data,postd);
									}
								}
								if(ret[0] === false) {
									$("#DelError>td","#"+dtbl).html(ret[1]);
									$("#DelError","#"+dtbl).show();
								} else {
									if(rp_ge.reloadAfterSubmit && $t.p.datatype != "local") {
										$($t).trigger("reloadGrid");
									} else {
										var toarr = [];
										toarr = postdata.split(",");
										if($t.p.treeGrid===true){
												try {$($t).jqGrid("delTreeNode",toarr[0]);} catch(e){}
										} else {
											for(var i=0;i<toarr.length;i++) {
												$($t).jqGrid("delRowData",toarr[i]);
											}
										}
										$t.p.selrow = null;
										$t.p.selarrrow = [];
									}
									if($.isFunction(rp_ge.afterComplete)) {
										setTimeout(function(){rp_ge.afterComplete(data,postdata);},500);
									}
								}
								rp_ge.processing=false;
								$("#dData", "#"+dtbl+"_2").removeClass('ui-state-active');
								if(ret[0]) { $.jgrid.hideModal("#"+IDs.themodal,{gb:"#gbox_"+gID,jqm:p.jqModal, onClose: rp_ge.onClose}); }
							}
						}, $.jgrid.ajaxOptions, p.ajaxDelOptions);


						if (!ajaxOptions.url && !rp_ge.useDataProxy) {
							if ($.isFunction($t.p.dataProxy)) {
								rp_ge.useDataProxy = true;
							} else {
								ret[0]=false; ret[1] += " "+$.jgrid.errors.nourl;
							}
						}
						if (ret[0]) {
							if (rp_ge.useDataProxy) { $t.p.dataProxy.call($t, ajaxOptions, "del_"+$t.p.id); }
							else { $.ajax(ajaxOptions); }
						}
					}

					if(ret[0] === false) {
						$("#DelError>td","#"+dtbl).html(ret[1]);
						$("#DelError","#"+dtbl).show();
					}
					return false;
				});
				$("#eData", "#"+dtbl+"_2").click(function(e){
					$.jgrid.hideModal("#"+IDs.themodal,{gb:"#gbox_"+gID,jqm:p.jqModal, onClose: rp_ge.onClose});
					return false;
				});
				if(onBeforeShow) { p.beforeShowForm($("#"+dtbl)); }
				$.jgrid.viewModal("#"+IDs.themodal,{gbox:"#gbox_"+gID,jqm:p.jqModal, overlay: p.overlay, modal:p.modal});
				if(onAfterShow) { p.afterShowForm($("#"+dtbl)); }
			}
			if(p.closeOnEscape===true) {
				setTimeout(function(){$(".ui-jqdialog-titlebar-close","#"+IDs.modalhead).focus();},0);
			}
		});
	},
	navGrid : function (elem, o, pEdit,pAdd,pDel,pSearch, pView) {
		o = $.extend({
			edit: true,
			editicon: "ui-icon-pencil",
			add: true,
			addicon:"ui-icon-plus",
			del: true,
			delicon:"ui-icon-trash",
			search: true,
			searchicon:"ui-icon-search",
			refresh: true,
			refreshicon:"ui-icon-refresh",
			refreshstate: 'firstpage',
			view: false,
			viewicon : "ui-icon-document",
			position : "left",
			closeOnEscape : true,
			beforeRefresh : null,
			afterRefresh : null,
			cloneToTop : false
		}, $.jgrid.nav, o ||{});
		return this.each(function() {
			if(this.nav) { return; }
			var alertIDs = {themodal:'alertmod',modalhead:'alerthd',modalcontent:'alertcnt'},
			$t = this, vwidth, vheight, twd, tdw;
			if(!$t.grid || typeof elem != 'string') { return; }
			if ($("#"+alertIDs.themodal).html() === null) {
				if (typeof window.innerWidth != 'undefined') {
					vwidth = window.innerWidth;
					vheight = window.innerHeight;
				} else if (typeof document.documentElement != 'undefined' && typeof document.documentElement.clientWidth != 'undefined' && document.documentElement.clientWidth !== 0) {
					vwidth = document.documentElement.clientWidth;
					vheight = document.documentElement.clientHeight;
				} else {
					vwidth=1024;
					vheight=768;
				}
				$.jgrid.createModal(alertIDs,"<div>"+o.alerttext+"</div><span tabindex='0'><span tabindex='-1' id='jqg_alrt'></span></span>",{gbox:"#gbox_"+$t.p.id,jqModal:true,drag:true,resize:true,caption:o.alertcap,top:vheight/2-25,left:vwidth/2-100,width:200,height:'auto',closeOnEscape:o.closeOnEscape},"","",true);
			}
			var clone = 1;
			if(o.cloneToTop && $t.p.toppager) { clone = 2; }
			for(var i = 0; i<clone; i++) {
				var tbd,
				navtbl = $("<table cellspacing='0' cellpadding='0' border='0' class='ui-pg-table navtable' style='float:left;table-layout:auto;'><tbody><tr></tr></tbody></table>"),
				sep = "<td class='ui-pg-button ui-state-disabled' style='width:4px;'><span class='ui-separator'></span></td>",
				pgid, elemids;
				if(i===0) {
					pgid = elem;
					elemids = $t.p.id;
					if(pgid == $t.p.toppager) {
						elemids += "_top";
						clone = 1;
					}
				} else {
					pgid = $t.p.toppager;
					elemids = $t.p.id+"_top";
				}
				if($t.p.direction == "rtl") { $(navtbl).attr("dir","rtl").css("float","right"); }
				if (o.add) {
					pAdd = pAdd || {};
					tbd = $("<td class='ui-pg-button ui-corner-all'></td>");
					$(tbd).append("<div class='ui-pg-div'><span class='ui-icon "+o.addicon+"'></span>"+o.addtext+"</div>");
					$("tr",navtbl).append(tbd);
					$(tbd,navtbl)
					.attr({"title":o.addtitle || "",id : pAdd.id || "add_"+elemids})
					.click(function(){
						if (!$(this).hasClass('ui-state-disabled')) {
							if ($.isFunction( o.addfunc )) {
								o.addfunc();
							} else {
								$($t).jqGrid("editGridRow","new",pAdd);
							}
						}
						return false;
					}).hover(
						function () {
							if (!$(this).hasClass('ui-state-disabled')) {
								$(this).addClass("ui-state-hover");
							}
						},
						function () {$(this).removeClass("ui-state-hover");}
					);
					tbd = null;
				}
				if (o.edit) {
					tbd = $("<td class='ui-pg-button ui-corner-all'></td>");
					pEdit = pEdit || {};
					$(tbd).append("<div class='ui-pg-div'><span class='ui-icon "+o.editicon+"'></span>"+o.edittext+"</div>");
					$("tr",navtbl).append(tbd);
					$(tbd,navtbl)
					.attr({"title":o.edittitle || "",id: pEdit.id || "edit_"+elemids})
					.click(function(){
						if (!$(this).hasClass('ui-state-disabled')) {
							var sr = $t.p.selrow;
							if (sr) {
								if($.isFunction( o.editfunc ) ) {
									o.editfunc(sr);
								} else {
									$($t).jqGrid("editGridRow",sr,pEdit);
								}
							} else {
								$.jgrid.viewModal("#"+alertIDs.themodal,{gbox:"#gbox_"+$t.p.id,jqm:true});
								$("#jqg_alrt").focus();
							}
						}
						return false;
					}).hover(
						function () {
							if (!$(this).hasClass('ui-state-disabled')) {
								$(this).addClass("ui-state-hover");
							}
						},
						function () {$(this).removeClass("ui-state-hover");}
					);
					tbd = null;
				}
				if (o.view) {
					tbd = $("<td class='ui-pg-button ui-corner-all'></td>");
					pView = pView || {};
					$(tbd).append("<div class='ui-pg-div'><span class='ui-icon "+o.viewicon+"'></span>"+o.viewtext+"</div>");
					$("tr",navtbl).append(tbd);
					$(tbd,navtbl)
					.attr({"title":o.viewtitle || "",id: pView.id || "view_"+elemids})
					.click(function(){
						if (!$(this).hasClass('ui-state-disabled')) {
							var sr = $t.p.selrow;
							if (sr) {
								if($.isFunction( o.viewfunc ) ) {
									o.viewfunc(sr);
								} else {
								$($t).jqGrid("viewGridRow",sr,pView);
								}
							} else {
								$.jgrid.viewModal("#"+alertIDs.themodal,{gbox:"#gbox_"+$t.p.id,jqm:true});
								$("#jqg_alrt").focus();
							}
						}
						return false;
					}).hover(
						function () {
							if (!$(this).hasClass('ui-state-disabled')) {
								$(this).addClass("ui-state-hover");
							}
						},
						function () {$(this).removeClass("ui-state-hover");}
					);
					tbd = null;
				}
				if (o.del) {
					tbd = $("<td class='ui-pg-button ui-corner-all'></td>");
					pDel = pDel || {};
					$(tbd).append("<div class='ui-pg-div'><span class='ui-icon "+o.delicon+"'></span>"+o.deltext+"</div>");
					$("tr",navtbl).append(tbd);
					$(tbd,navtbl)
					.attr({"title":o.deltitle || "",id: pDel.id || "del_"+elemids})
					.click(function(){
						if (!$(this).hasClass('ui-state-disabled')) {
							var dr;
							if($t.p.multiselect) {
								dr = $t.p.selarrrow;
								if(dr.length===0) { dr = null; }
							} else {
								dr = $t.p.selrow;
							}
							if(dr){
								if("function" == typeof o.delfunc){
									o.delfunc(dr);
								}else{
									$($t).jqGrid("delGridRow",dr,pDel);
								}
							} else  {
								$.jgrid.viewModal("#"+alertIDs.themodal,{gbox:"#gbox_"+$t.p.id,jqm:true}); $("#jqg_alrt").focus();
							}
						}
						return false;
					}).hover(
						function () {
							if (!$(this).hasClass('ui-state-disabled')) {
								$(this).addClass("ui-state-hover");
							}
						},
						function () {$(this).removeClass("ui-state-hover");}
					);
					tbd = null;
				}
				if(o.add || o.edit || o.del || o.view) { $("tr",navtbl).append(sep); }
				if (o.search) {
					tbd = $("<td class='ui-pg-button ui-corner-all'></td>");
					pSearch = pSearch || {};
					$(tbd).append("<div class='ui-pg-div'><span class='ui-icon "+o.searchicon+"'></span>"+o.searchtext+"</div>");
					$("tr",navtbl).append(tbd);
					$(tbd,navtbl)
					.attr({"title":o.searchtitle  || "",id:pSearch.id || "search_"+elemids})
					.click(function(){
						if (!$(this).hasClass('ui-state-disabled')) {
							$($t).jqGrid("searchGrid",pSearch);
						}
						return false;
					}).hover(
						function () {
							if (!$(this).hasClass('ui-state-disabled')) {
								$(this).addClass("ui-state-hover");
							}
						},
						function () {$(this).removeClass("ui-state-hover");}
					);
					if (pSearch.showOnLoad && pSearch.showOnLoad === true)
						$(tbd,navtbl).click();
					tbd = null;
				}
				if (o.refresh) {
					tbd = $("<td class='ui-pg-button ui-corner-all'></td>");
					$(tbd).append("<div class='ui-pg-div'><span class='ui-icon "+o.refreshicon+"'></span>"+o.refreshtext+"</div>");
					$("tr",navtbl).append(tbd);
					$(tbd,navtbl)
					.attr({"title":o.refreshtitle  || "",id: "refresh_"+elemids})
					.click(function(){
						if (!$(this).hasClass('ui-state-disabled')) {
							if($.isFunction(o.beforeRefresh)) { o.beforeRefresh(); }
							$t.p.search = false;
							try {
								var gID = $t.p.id;
								$t.p.postData.filters ="";
								$("#fbox_"+gID).jqFilter('resetFilter');
							    if($.isFunction($t.clearToolbar)) { $t.clearToolbar(false); }
							} catch (e) {}
							switch (o.refreshstate) {
								case 'firstpage':
								    $($t).trigger("reloadGrid", [{page:1}]);
									break;
								case 'current':
								    $($t).trigger("reloadGrid", [{current:true}]);
									break;
							}
							if($.isFunction(o.afterRefresh)) { o.afterRefresh(); }
						}
						return false;
					}).hover(
						function () {
							if (!$(this).hasClass('ui-state-disabled')) {
								$(this).addClass("ui-state-hover");
							}
						},
						function () {$(this).removeClass("ui-state-hover");}
					);
					tbd = null;
				}
				tdw = $(".ui-jqgrid").css("font-size") || "11px";
				$('body').append("<div id='testpg2' class='ui-jqgrid ui-widget ui-widget-content' style='font-size:"+tdw+";visibility:hidden;' ></div>");
				twd = $(navtbl).clone().appendTo("#testpg2").width();
				$("#testpg2").remove();
				$(pgid+"_"+o.position,pgid).append(navtbl);
				if($t.p._nvtd) {
					if(twd > $t.p._nvtd[0] ) {
						$(pgid+"_"+o.position,pgid).width(twd);
						$t.p._nvtd[0] = twd;
					}
					$t.p._nvtd[1] = twd;
				}
				tdw =null; twd=null; navtbl =null;
				this.nav = true;
			}
		});
	},
	navButtonAdd : function (elem, p) {
		p = $.extend({
			caption : "newButton",
			title: '',
			buttonicon : 'ui-icon-newwin',
			onClickButton: null,
			position : "last",
			cursor : 'pointer'
		}, p ||{});
		return this.each(function() {
			if( !this.grid)  { return; }
			if( elem.indexOf("#") !== 0) { elem = "#"+elem; }
			var findnav = $(".navtable",elem)[0], $t = this;
			if (findnav) {
				if( p.id && $("#"+p.id, findnav).html() !== null )  { return; }
				var tbd = $("<td></td>");
				if(p.buttonicon.toString().toUpperCase() == "NONE") {
                    $(tbd).addClass('ui-pg-button ui-corner-all').append("<div class='ui-pg-div'>"+p.caption+"</div>");
				} else	{
					$(tbd).addClass('ui-pg-button ui-corner-all').append("<div class='ui-pg-div'><span class='ui-icon "+p.buttonicon+"'></span>"+p.caption+"</div>");
				}
				if(p.id) {$(tbd).attr("id",p.id);}
				if(p.position=='first'){
					if(findnav.rows[0].cells.length ===0 ) {
						$("tr",findnav).append(tbd);
					} else {
						$("tr td:eq(0)",findnav).before(tbd);
					}
				} else {
					$("tr",findnav).append(tbd);
				}
				$(tbd,findnav)
				.attr("title",p.title  || "")
				.click(function(e){
					if (!$(this).hasClass('ui-state-disabled')) {
						if ($.isFunction(p.onClickButton) ) { p.onClickButton.call($t,e); }
					}
					return false;
				})
				.hover(
					function () {
						if (!$(this).hasClass('ui-state-disabled')) {
							$(this).addClass('ui-state-hover');
						}
					},
					function () {$(this).removeClass("ui-state-hover");}
				);
			}
		});
	},
	navSeparatorAdd:function (elem,p) {
		p = $.extend({
			sepclass : "ui-separator",
			sepcontent: ''
		}, p ||{});		
		return this.each(function() {
			if( !this.grid)  { return; }
			if( elem.indexOf("#") !== 0) { elem = "#"+elem; }
			var findnav = $(".navtable",elem)[0];
			if(findnav) {
				var sep = "<td class='ui-pg-button ui-state-disabled' style='width:4px;'><span class='"+p.sepclass+"'></span>"+p.sepcontent+"</td>";
				$("tr",findnav).append(sep);
			}
		});
	},
	GridToForm : function( rowid, formid ) {
		return this.each(function(){
			var $t = this;
			if (!$t.grid) { return; } 
			var rowdata = $($t).jqGrid("getRowData",rowid);
			if (rowdata) {
				for(var i in rowdata) {
					if ( $("[name="+$.jgrid.jqID(i)+"]",formid).is("input:radio") || $("[name="+$.jgrid.jqID(i)+"]",formid).is("input:checkbox"))  {
						$("[name="+$.jgrid.jqID(i)+"]",formid).each( function() {
							if( $(this).val() == rowdata[i] ) {
								$(this).attr("checked","checked");
							} else {
								$(this).attr("checked","");
							}
						});
					} else {
					// this is very slow on big table and form.
						$("[name="+$.jgrid.jqID(i)+"]",formid).val(rowdata[i]);
					}
				}
			}
		});
	},
	FormToGrid : function(rowid, formid, mode, position){
		return this.each(function() {
			var $t = this;
			if(!$t.grid) { return; }
			if(!mode) { mode = 'set'; }
			if(!position) { position = 'first'; }
			var fields = $(formid).serializeArray();
			var griddata = {};
			$.each(fields, function(i, field){
				griddata[field.name] = field.value;
			});
			if(mode=='add') { $($t).jqGrid("addRowData",rowid,griddata, position); }
			else if(mode=='set') { $($t).jqGrid("setRowData",rowid,griddata); }
		});
	}
});
})(jQuery);
;(function($){
/**
 * jqGrid extension for manipulating Grid Data
 * Tony Tomov tony@trirand.com
 * http://trirand.com/blog/ 
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl-2.0.html
**/ 
$.jgrid.extend({
//Editing
	editRow : function(rowid,keys,oneditfunc,successfunc, url, extraparam, aftersavefunc,errorfunc, afterrestorefunc) {
		// Compatible mode old versions
		var settings = {
			"keys" : keys || false,
			"oneditfunc" : oneditfunc || null,
			"successfunc" : successfunc || null,
			"url" : url || null,
			"extraparam" : extraparam || {},
			"aftersavefunc" : aftersavefunc || null,
			"errorfunc": errorfunc || null,
			"afterrestorefunc" : afterrestorefunc|| null,
			"restoreAfterErorr" : true
		},
		args = $.makeArray(arguments).slice(1), o;

		if(args[0] && typeof(args[0]) == "object" && !$.isFunction(args[0])) {
			o = $.extend(settings,args[0]);
		} else {
			o = settings;
		}
		// End compatible
		return this.each(function(){
			var $t = this, nm, tmp, editable, cnt=0, focus=null, svr={}, ind,cm;
			if (!$t.grid ) { return; }
			ind = $($t).jqGrid("getInd",rowid,true);
			if( ind === false ) {return;}
			editable = $(ind).attr("editable") || "0";
			if (editable == "0" && !$(ind).hasClass("not-editable-row")) {
				cm = $t.p.colModel;
				$('td',ind).each( function(i) {
					nm = cm[i].name;
					var treeg = $t.p.treeGrid===true && nm == $t.p.ExpandColumn;
					if(treeg) { tmp = $("span:first",this).html();}
					else {
						try {
							tmp =  $.unformat(this,{rowId:rowid, colModel:cm[i]},i);
						} catch (_) {
							tmp =  ( cm[i].edittype && cm[i].edittype == 'textarea' ) ? $(this).text() : $(this).html();
						}
					}
					if ( nm != 'cb' && nm != 'subgrid' && nm != 'rn') {
						if($t.p.autoencode) { tmp = $.jgrid.htmlDecode(tmp); }
						svr[nm]=tmp;
						if(cm[i].editable===true) {
							if(focus===null) { focus = i; }
							if (treeg) { $("span:first",this).html(""); }
							else { $(this).html(""); }
							var opt = $.extend({},cm[i].editoptions || {},{id:rowid+"_"+nm,name:nm});
							if(!cm[i].edittype) { cm[i].edittype = "text"; }
							if(tmp == "&nbsp;" || tmp == "&#160;" || (tmp.length==1 && tmp.charCodeAt(0)==160) ) {tmp='';}
							var elc = $.jgrid.createEl(cm[i].edittype,opt,tmp,true,$.extend({},$.jgrid.ajaxOptions,$t.p.ajaxSelectOptions || {}));
							$(elc).addClass("editable");
							if(treeg) { $("span:first",this).append(elc); }
							else { $(this).append(elc); }
							//Again IE
							if(cm[i].edittype == "select" && cm[i].editoptions.multiple===true && $.browser.msie) {
								$(elc).width($(elc).width());
							}
							cnt++;
						}
					}
				});
				if(cnt > 0) {
					svr.id = rowid; $t.p.savedRow.push(svr);
					$(ind).attr("editable","1");
					$("td:eq("+focus+") input",ind).focus();
					if(o.keys===true) {
						$(ind).bind("keydown",function(e) {
							if (e.keyCode === 27) {$($t).jqGrid("restoreRow",rowid, afterrestorefunc);}
							if (e.keyCode === 13) {
								var ta = e.target;
								if(ta.tagName == 'TEXTAREA') { return true; }
								$($t).jqGrid("saveRow", rowid, o );
								return false;
							}
							e.stopPropagation();
						});
					}
					if( $.isFunction(o.oneditfunc)) { o.oneditfunc.call($t, rowid); }
				}
			}
		});
	},
	saveRow : function(rowid, successfunc, url, extraparam, aftersavefunc,errorfunc, afterrestorefunc) {
		// Compatible mode old versions
		var settings = {
			"successfunc" : successfunc || null,
			"url" : url || null,
			"extraparam" : extraparam || {},
			"aftersavefunc" : aftersavefunc || null,
			"errorfunc": errorfunc || null,
			"afterrestorefunc" : afterrestorefunc|| null,
			"restoreAfterErorr" : true
		},
		args = $.makeArray(arguments).slice(1), o;

		if(args[0] && typeof(args[0]) == "object" && !$.isFunction(args[0])) {
			o = $.extend(settings,args[0]);
		} else {
			o = settings;
		}
		// End compatible
		var success = false;
		var $t = this[0], nm, tmp={}, tmp2={}, tmp3= {}, editable, fr, cv, ind;
		if (!$t.grid ) { return success; }
		ind = $($t).jqGrid("getInd",rowid,true);
		if(ind === false) {return success;}
		editable = $(ind).attr("editable");
		o.url = o.url ? o.url : $t.p.editurl;
		if (editable==="1") {
			var cm;
			$("td",ind).each(function(i) {
				cm = $t.p.colModel[i];
				nm = cm.name;
				if ( nm != 'cb' && nm != 'subgrid' && cm.editable===true && nm != 'rn' && !$(this).hasClass('not-editable-cell')) {
					switch (cm.edittype) {
						case "checkbox":
							var cbv = ["Yes","No"];
							if(cm.editoptions ) {
								cbv = cm.editoptions.value.split(":");
							}
							tmp[nm]=  $("input",this).attr("checked") ? cbv[0] : cbv[1]; 
							break;
						case 'text':
						case 'password':
						case 'textarea':
						case "button" :
							tmp[nm]=$("input, textarea",this).val();
							break;
						case 'select':
							if(!cm.editoptions.multiple) {
								tmp[nm] = $("select>option:selected",this).val();
								tmp2[nm] = $("select>option:selected", this).text();
							} else {
								var sel = $("select",this), selectedText = [];
								tmp[nm] = $(sel).val();
								if(tmp[nm]) { tmp[nm]= tmp[nm].join(","); } else { tmp[nm] =""; }
								$("select > option:selected",this).each(
									function(i,selected){
										selectedText[i] = $(selected).text();
									}
								);
								tmp2[nm] = selectedText.join(",");
							}
							if(cm.formatter && cm.formatter == 'select') { tmp2={}; }
							break;
						case 'custom' :
							try {
								if(cm.editoptions && $.isFunction(cm.editoptions.custom_value)) {
									tmp[nm] = cm.editoptions.custom_value.call($t, $(".customelement",this),'get');
									if (tmp[nm] === undefined) { throw "e2"; }
								} else { throw "e1"; }
							} catch (e) {
								if (e=="e1") { $.jgrid.info_dialog(jQuery.jgrid.errors.errcap,"function 'custom_value' "+$.jgrid.edit.msg.nodefined,jQuery.jgrid.edit.bClose); }
								if (e=="e2") { $.jgrid.info_dialog(jQuery.jgrid.errors.errcap,"function 'custom_value' "+$.jgrid.edit.msg.novalue,jQuery.jgrid.edit.bClose); }
								else { $.jgrid.info_dialog(jQuery.jgrid.errors.errcap,e.message,jQuery.jgrid.edit.bClose); }
							}
							break;
					}
					cv = $.jgrid.checkValues(tmp[nm],i,$t);
					if(cv[0] === false) {
						cv[1] = tmp[nm] + " " + cv[1];
						return false;
					}
					if($t.p.autoencode) { tmp[nm] = $.jgrid.htmlEncode(tmp[nm]); }
					if(o.url !== 'clientArray' && cm.editoptions && cm.editoptions.NullIfEmpty === true) {
						if(tmp[nm] == "") {
							tmp3[nm] = 'null';
				}
					}
				}
			});
			if (cv[0] === false){
				try {
					var positions = $.jgrid.findPos($("#"+$.jgrid.jqID(rowid), $t.grid.bDiv)[0]);
					$.jgrid.info_dialog($.jgrid.errors.errcap,cv[1],$.jgrid.edit.bClose,{left:positions[0],top:positions[1]});
				} catch (e) {
					alert(cv[1]);
				}
				return success;
			}
			if(tmp) {
				var idname, opers, oper;
				opers = $t.p.prmNames;
				oper = opers.oper;
				idname = opers.id;
				tmp[oper] = opers.editoper;
				tmp[idname] = rowid;
				if(typeof($t.p.inlineData) == 'undefined') { $t.p.inlineData ={}; }
				tmp = $.extend({},tmp,$t.p.inlineData,o.extraparam);
			}
			if (o.url == 'clientArray') {
				tmp = $.extend({},tmp, tmp2);
				if($t.p.autoencode) {
					$.each(tmp,function(n,v){
						tmp[n] = $.jgrid.htmlDecode(v);
					});
				}
				var resp = $($t).jqGrid("setRowData",rowid,tmp);
				$(ind).attr("editable","0");
				for( var k=0;k<$t.p.savedRow.length;k++) {
					if( $t.p.savedRow[k].id == rowid) {fr = k; break;}
				}
				if(fr >= 0) { $t.p.savedRow.splice(fr,1); }
				if( $.isFunction(o.aftersavefunc) ) { o.aftersavefunc.call($t, rowid,resp); }
				success = true;
			} else {
				$("#lui_"+$t.p.id).show();
				tmp3 = $.extend({},tmp,tmp3);
				$.ajax($.extend({
					url:o.url,
					data: $.isFunction($t.p.serializeRowData) ? $t.p.serializeRowData.call($t, tmp3) : tmp3,
					type: "POST",
					async : false, //?!?
					complete: function(res,stat){
						$("#lui_"+$t.p.id).hide();
						if (stat === "success"){
							var ret;
							if( $.isFunction(o.successfunc)) { ret = o.successfunc.call($t, res);}
							else { ret = true; }
							if (ret===true) {
								if($t.p.autoencode) {
									$.each(tmp,function(n,v){
										tmp[n] = $.jgrid.htmlDecode(v);
									});
								}
								tmp = $.extend({},tmp, tmp2);
								$($t).jqGrid("setRowData",rowid,tmp);
								$(ind).attr("editable","0");
								for( var k=0;k<$t.p.savedRow.length;k++) {
									if( $t.p.savedRow[k].id == rowid) {fr = k; break;}
								}
								if(fr >= 0) { $t.p.savedRow.splice(fr,1); }
								if( $.isFunction(o.aftersavefunc) ) { o.aftersavefunc.call($t, rowid,res); }
								success = true;
							} else {
								if($.isFunction(o.errorfunc) ) {
									o.errorfunc.call($t, rowid, res, stat);
								}
								if(o.restoreAfterError === true) {
									$($t).jqGrid("restoreRow",rowid, o.afterrestorefunc);
							}
						}
						}
					},
					error:function(res,stat){
						$("#lui_"+$t.p.id).hide();
						if($.isFunction(o.errorfunc) ) {
							o.errorfunc.call($t, rowid, res, stat);
						} else {
							try {
								jQuery.jgrid.info_dialog(jQuery.jgrid.errors.errcap,'<div class="ui-state-error">'+ res.responseText +'</div>', jQuery.jgrid.edit.bClose,{buttonalign:'right'});
						}
							catch(e) {
								alert(res.responseText);
					}
						}
						if(o.restoreAfterError === true) {
							$($t).jqGrid("restoreRow",rowid, o.afterrestorefunc);
						}
					}
				}, $.jgrid.ajaxOptions, $t.p.ajaxRowOptions || {}));
			}
			$(ind).unbind("keydown");
		}
		return success;
	},
	restoreRow : function(rowid, afterrestorefunc) {
		return this.each(function(){
			var $t= this, fr, ind, ares={};
			if (!$t.grid ) { return; }
			ind = $($t).jqGrid("getInd",rowid,true);
			if(ind === false) {return;}
			for( var k=0;k<$t.p.savedRow.length;k++) {
				if( $t.p.savedRow[k].id == rowid) {fr = k; break;}
			}
			if(fr >= 0) {
				if($.isFunction($.fn.datepicker)) {
					try {
						$("input.hasDatepicker","#"+$.jgrid.jqID(ind.id)).datepicker('hide');
					} catch (e) {}
				}
				$.each($t.p.colModel, function(i,n){
					if(this.editable === true && this.name in $t.p.savedRow[fr] && !$(this).hasClass('not-editable-cell')) {
						ares[this.name] = $t.p.savedRow[fr][this.name];
					}
				});
				$($t).jqGrid("setRowData",rowid,ares);
				$(ind).attr("editable","0").unbind("keydown");
				$t.p.savedRow.splice(fr,1);
			}
			if ($.isFunction(afterrestorefunc))
			{
				afterrestorefunc.call($t, rowid);
			}
		});
	}
//end inline edit
});
})(jQuery);
;(function($){
/*
**
 * jqGrid extension for cellediting Grid Data
 * Tony Tomov tony@trirand.com
 * http://trirand.com/blog/ 
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl-2.0.html
**/ 
/**
 * all events and options here are aded anonynous and not in the base grid
 * since the array is to big. Here is the order of execution.
 * From this point we use jQuery isFunction
 * formatCell
 * beforeEditCell,
 * onSelectCell (used only for noneditable cels)
 * afterEditCell,
 * beforeSaveCell, (called before validation of values if any)
 * beforeSubmitCell (if cellsubmit remote (ajax))
 * afterSubmitCell(if cellsubmit remote (ajax)),
 * afterSaveCell,
 * errorCell,
 * serializeCellData - new
 * Options
 * cellsubmit (remote,clientArray) (added in grid options)
 * cellurl
 * ajaxCellOptions
* */
$.jgrid.extend({
	editCell : function (iRow,iCol, ed){
		return this.each(function (){
			var $t = this, nm, tmp,cc, cm;
			if (!$t.grid || $t.p.cellEdit !== true) {return;}
			iCol = parseInt(iCol,10);
			// select the row that can be used for other methods
			$t.p.selrow = $t.rows[iRow].id;
			if (!$t.p.knv) {$($t).jqGrid("GridNav");}
			// check to see if we have already edited cell
			if ($t.p.savedRow.length>0) {
				// prevent second click on that field and enable selects
				if (ed===true ) {
					if(iRow == $t.p.iRow && iCol == $t.p.iCol){
						return;
					}
				}
				// save the cell
				$($t).jqGrid("saveCell",$t.p.savedRow[0].id,$t.p.savedRow[0].ic);
			} else {
				window.setTimeout(function () { $("#"+$t.p.knv).attr("tabindex","-1").focus();},0);
			}
			cm = $t.p.colModel[iCol];
			nm = cm.name;
			if (nm=='subgrid' || nm=='cb' || nm=='rn') {return;}
			cc = $("td:eq("+iCol+")",$t.rows[iRow]);
			if (cm.editable===true && ed===true && !cc.hasClass("not-editable-cell")) {
				if(parseInt($t.p.iCol,10)>=0  && parseInt($t.p.iRow,10)>=0) {
					$("td:eq("+$t.p.iCol+")",$t.rows[$t.p.iRow]).removeClass("edit-cell ui-state-highlight");
					$($t.rows[$t.p.iRow]).removeClass("selected-row ui-state-hover");
				}
				$(cc).addClass("edit-cell ui-state-highlight");
				$($t.rows[iRow]).addClass("selected-row ui-state-hover");
				try {
					tmp =  $.unformat(cc,{rowId: $t.rows[iRow].id, colModel:cm},iCol);
				} catch (_) {
					tmp = ( cm.edittype && cm.edittype == 'textarea' ) ? $(cc).text() : $(cc).html();
				}
				if($t.p.autoencode) { tmp = $.jgrid.htmlDecode(tmp); }
				if (!cm.edittype) {cm.edittype = "text";}
				$t.p.savedRow.push({id:iRow,ic:iCol,name:nm,v:tmp});
				if(tmp == "&nbsp;" || tmp == "&#160;" || (tmp.length==1 && tmp.charCodeAt(0)==160) ) {tmp='';}
				if($.isFunction($t.p.formatCell)) {
					var tmp2 = $t.p.formatCell.call($t, $t.rows[iRow].id,nm,tmp,iRow,iCol);
					if(tmp2 !== undefined ) {tmp = tmp2;}
				}
				var opt = $.extend({}, cm.editoptions || {} ,{id:iRow+"_"+nm,name:nm});
				var elc = $.jgrid.createEl(cm.edittype,opt,tmp,true,$.extend({},$.jgrid.ajaxOptions,$t.p.ajaxSelectOptions || {}));
				if ($.isFunction($t.p.beforeEditCell)) {
					$t.p.beforeEditCell.call($t, $t.rows[iRow].id,nm,tmp,iRow,iCol);
				}
				$(cc).html("").append(elc).attr("tabindex","0");
				window.setTimeout(function () { $(elc).focus();},0);
				$("input, select, textarea",cc).bind("keydown",function(e) { 
					if (e.keyCode === 27) {
						if($("input.hasDatepicker",cc).length >0) {
							if( $(".ui-datepicker").is(":hidden") )  { $($t).jqGrid("restoreCell",iRow,iCol); }
							else { $("input.hasDatepicker",cc).datepicker('hide'); }
						} else {
							$($t).jqGrid("restoreCell",iRow,iCol);
						}
					} //ESC
					if (e.keyCode === 13) {$($t).jqGrid("saveCell",iRow,iCol);}//Enter
					if (e.keyCode == 9)  {
						if(!$t.grid.hDiv.loading ) {
							if (e.shiftKey) {$($t).jqGrid("prevCell",iRow,iCol);} //Shift TAb
							else {$($t).jqGrid("nextCell",iRow,iCol);} //Tab
						} else {
							return false;
						}
					}
					e.stopPropagation();
				});
				if ($.isFunction($t.p.afterEditCell)) {
					$t.p.afterEditCell.call($t, $t.rows[iRow].id,nm,tmp,iRow,iCol);
				}
			} else {
				if (parseInt($t.p.iCol,10)>=0  && parseInt($t.p.iRow,10)>=0) {
					$("td:eq("+$t.p.iCol+")",$t.rows[$t.p.iRow]).removeClass("edit-cell ui-state-highlight");
					$($t.rows[$t.p.iRow]).removeClass("selected-row ui-state-hover");
				}
				cc.addClass("edit-cell ui-state-highlight");
				$($t.rows[iRow]).addClass("selected-row ui-state-hover"); 
				if ($.isFunction($t.p.onSelectCell)) {
					tmp = cc.html().replace(/\&#160\;/ig,'');
					$t.p.onSelectCell.call($t, $t.rows[iRow].id,nm,tmp,iRow,iCol);
				}
			}
			$t.p.iCol = iCol; $t.p.iRow = iRow;
		});
	},
	saveCell : function (iRow, iCol){
		return this.each(function(){
			var $t= this, fr;
			if (!$t.grid || $t.p.cellEdit !== true) {return;}
			if ( $t.p.savedRow.length >= 1) {fr = 0;} else {fr=null;} 
			if(fr !== null) {
				var cc = $("td:eq("+iCol+")",$t.rows[iRow]),v,v2,
				cm = $t.p.colModel[iCol], nm = cm.name, nmjq = $.jgrid.jqID(nm) ;
				switch (cm.edittype) {
					case "select":
						if(!cm.editoptions.multiple) {
							v = $("#"+iRow+"_"+nmjq+">option:selected",$t.rows[iRow]).val();
							v2 = $("#"+iRow+"_"+nmjq+">option:selected",$t.rows[iRow]).text();
						} else {
							var sel = $("#"+iRow+"_"+nmjq,$t.rows[iRow]), selectedText = [];
							v = $(sel).val();
							if(v) { v.join(",");} else { v=""; }
							$("option:selected",sel).each(
								function(i,selected){
									selectedText[i] = $(selected).text();
								}
							);
							v2 = selectedText.join(",");
						}
						if(cm.formatter) { v2 = v; }
						break;
					case "checkbox":
						var cbv  = ["Yes","No"];
						if(cm.editoptions){
							cbv = cm.editoptions.value.split(":");
						}
						v = $("#"+iRow+"_"+nmjq,$t.rows[iRow]).attr("checked") ? cbv[0] : cbv[1];
						v2=v;
						break;
					case "password":
					case "text":
					case "textarea":
					case "button" :
						v = $("#"+iRow+"_"+nmjq,$t.rows[iRow]).val();
						v2=v;
						break;
					case 'custom' :
						try {
							if(cm.editoptions && $.isFunction(cm.editoptions.custom_value)) {
								v = cm.editoptions.custom_value.call($t, $(".customelement",cc),'get');
								if (v===undefined) { throw "e2";} else { v2=v; }
							} else { throw "e1"; }
						} catch (e) {
							if (e=="e1") { $.jgrid.info_dialog(jQuery.jgrid.errors.errcap,"function 'custom_value' "+$.jgrid.edit.msg.nodefined,jQuery.jgrid.edit.bClose); }
							if (e=="e2") { $.jgrid.info_dialog(jQuery.jgrid.errors.errcap,"function 'custom_value' "+$.jgrid.edit.msg.novalue,jQuery.jgrid.edit.bClose); }
							else {$.jgrid.info_dialog(jQuery.jgrid.errors.errcap,e.message,jQuery.jgrid.edit.bClose); }
						}
						break;
				}
				// The common approach is if nothing changed do not do anything
				if (v2 != $t.p.savedRow[fr].v){
					if ($.isFunction($t.p.beforeSaveCell)) {
						var vv = $t.p.beforeSaveCell.call($t, $t.rows[iRow].id,nm, v, iRow,iCol);
						if (vv) {v = vv; v2=vv;}
					}
					var cv = $.jgrid.checkValues(v,iCol,$t);
					if(cv[0] === true) {
						var addpost = {};
						if ($.isFunction($t.p.beforeSubmitCell)) {
							addpost = $t.p.beforeSubmitCell.call($t, $t.rows[iRow].id,nm, v, iRow,iCol);
							if (!addpost) {addpost={};}
						}
						if( $("input.hasDatepicker",cc).length >0) { $("input.hasDatepicker",cc).datepicker('hide'); }
						if ($t.p.cellsubmit == 'remote') {
							if ($t.p.cellurl) {
								var postdata = {};
								if($t.p.autoencode) { v = $.jgrid.htmlEncode(v); }
								postdata[nm] = v;
								var idname,oper, opers;
								opers = $t.p.prmNames;
								idname = opers.id;
								oper = opers.oper;
								postdata[idname] = $t.rows[iRow].id;
								postdata[oper] = opers.editoper;
								postdata = $.extend(addpost,postdata);
								$("#lui_"+$t.p.id).show();
								$t.grid.hDiv.loading = true;
								$.ajax( $.extend( {
									url: $t.p.cellurl,
									data :$.isFunction($t.p.serializeCellData) ? $t.p.serializeCellData.call($t, postdata) : postdata,
									type: "POST",
									complete: function (result, stat) {
										$("#lui_"+$t.p.id).hide();
										$t.grid.hDiv.loading = false;
										if (stat == 'success') {
											if ($.isFunction($t.p.afterSubmitCell)) {
												var ret = $t.p.afterSubmitCell.call($t, result,postdata.id,nm,v,iRow,iCol);
												if(ret[0] === true) {
													$(cc).empty();
													$($t).jqGrid("setCell",$t.rows[iRow].id, iCol, v2, false, false, true);
													$(cc).addClass("dirty-cell");
													$($t.rows[iRow]).addClass("edited");
													if ($.isFunction($t.p.afterSaveCell)) {
														$t.p.afterSaveCell.call($t, $t.rows[iRow].id,nm, v, iRow,iCol);
													}
													$t.p.savedRow.splice(0,1);
												} else {
													$.jgrid.info_dialog($.jgrid.errors.errcap,ret[1],$.jgrid.edit.bClose);
													$($t).jqGrid("restoreCell",iRow,iCol);
												}
											} else {
												$(cc).empty();
												$($t).jqGrid("setCell",$t.rows[iRow].id, iCol, v2, false, false, true);
												$(cc).addClass("dirty-cell");
												$($t.rows[iRow]).addClass("edited");
												if ($.isFunction($t.p.afterSaveCell)) {
													$t.p.afterSaveCell.call($t, $t.rows[iRow].id,nm, v, iRow,iCol);
												}
												$t.p.savedRow.splice(0,1);
											}
										}
									},
									error:function(res,stat) {
										$("#lui_"+$t.p.id).hide();
										$t.grid.hDiv.loading = false;
										if ($.isFunction($t.p.errorCell)) {
											$t.p.errorCell.call($t, res,stat);
											$($t).jqGrid("restoreCell",iRow,iCol);
										} else {
											$.jgrid.info_dialog($.jgrid.errors.errcap,res.status+" : "+res.statusText+"<br/>"+stat,$.jgrid.edit.bClose);
											$($t).jqGrid("restoreCell",iRow,iCol);
										}
									}
								}, $.jgrid.ajaxOptions, $t.p.ajaxCellOptions || {}));
							} else {
								try {
									$.jgrid.info_dialog($.jgrid.errors.errcap,$.jgrid.errors.nourl,$.jgrid.edit.bClose);
									$($t).jqGrid("restoreCell",iRow,iCol);
								} catch (e) {}
							}
						}
						if ($t.p.cellsubmit == 'clientArray') {
							$(cc).empty();
							$($t).jqGrid("setCell",$t.rows[iRow].id,iCol, v2, false, false, true);
							$(cc).addClass("dirty-cell");
							$($t.rows[iRow]).addClass("edited");
							if ($.isFunction($t.p.afterSaveCell)) {
								$t.p.afterSaveCell.call($t, $t.rows[iRow].id,nm, v, iRow,iCol);
							}
							$t.p.savedRow.splice(0,1);
						}
					} else {
						try {
							window.setTimeout(function(){$.jgrid.info_dialog($.jgrid.errors.errcap,v+" "+cv[1],$.jgrid.edit.bClose);},100);
							$($t).jqGrid("restoreCell",iRow,iCol);
						} catch (e) {}
					}
				} else {
					$($t).jqGrid("restoreCell",iRow,iCol);
				}
			}
			if ($.browser.opera) {
				$("#"+$t.p.knv).attr("tabindex","-1").focus();
			} else {
				window.setTimeout(function () { $("#"+$t.p.knv).attr("tabindex","-1").focus();},0);
			}
		});
	},
	restoreCell : function(iRow, iCol) {
		return this.each(function(){
			var $t= this, fr;
			if (!$t.grid || $t.p.cellEdit !== true ) {return;}
			if ( $t.p.savedRow.length >= 1) {fr = 0;} else {fr=null;}
			if(fr !== null) {
				var cc = $("td:eq("+iCol+")",$t.rows[iRow]);
				// datepicker fix
				if($.isFunction($.fn.datepicker)) {
					try {
						$("input.hasDatepicker",cc).datepicker('hide');
					} catch (e) {}
				}
				$(cc).empty().attr("tabindex","-1");
				$($t).jqGrid("setCell",$t.rows[iRow].id, iCol, $t.p.savedRow[fr].v, false, false, true);
				if ($.isFunction($t.p.afterRestoreCell)) {
					$t.p.afterRestoreCell.call($t, $t.rows[iRow].id, $t.p.savedRow[fr].v, iRow, iCol);
				}				
				$t.p.savedRow.splice(0,1);
			}
			window.setTimeout(function () { $("#"+$t.p.knv).attr("tabindex","-1").focus();},0);
		});
	},
	nextCell : function (iRow,iCol) {
		return this.each(function (){
			var $t = this, nCol=false;
			if (!$t.grid || $t.p.cellEdit !== true) {return;}
			// try to find next editable cell
			for (var i=iCol+1; i<$t.p.colModel.length; i++) {
				if ( $t.p.colModel[i].editable ===true) {
					nCol = i; break;
				}
			}
			if(nCol !== false) {
				$($t).jqGrid("editCell",iRow,nCol,true);
			} else {
				if ($t.p.savedRow.length >0) {
					$($t).jqGrid("saveCell",iRow,iCol);
				}
			}
		});
	},
	prevCell : function (iRow,iCol) {
		return this.each(function (){
			var $t = this, nCol=false;
			if (!$t.grid || $t.p.cellEdit !== true) {return;}
			// try to find next editable cell
			for (var i=iCol-1; i>=0; i--) {
				if ( $t.p.colModel[i].editable ===true) {
					nCol = i; break;
				}
			}
			if(nCol !== false) {
				$($t).jqGrid("editCell",iRow,nCol,true);
			} else {
				if ($t.p.savedRow.length >0) {
					$($t).jqGrid("saveCell",iRow,iCol);
				}
			}
		});
	},
	GridNav : function() {
		return this.each(function () {
			var  $t = this;
			if (!$t.grid || $t.p.cellEdit !== true ) {return;}
			// trick to process keydown on non input elements
			$t.p.knv = $t.p.id + "_kn";
			var selection = $("<span style='width:0px;height:0px;background-color:black;' tabindex='0'><span tabindex='-1' style='width:0px;height:0px;background-color:grey' id='"+$t.p.knv+"'></span></span>"),
			i, kdir;
			$(selection).insertBefore($t.grid.cDiv);
			$("#"+$t.p.knv)
			.focus()
			.keydown(function (e){
				kdir = e.keyCode;
				if($t.p.direction == "rtl") {
					if(kdir==37) { kdir = 39;}
					else if (kdir==39) { kdir = 37; }
				}
				switch (kdir) {
					case 38:
						if ($t.p.iRow-1 >0 ) {
							scrollGrid($t.p.iRow-1,$t.p.iCol,'vu');
							$($t).jqGrid("editCell",$t.p.iRow-1,$t.p.iCol,false);
						}
					break;
					case 40 :
						if ($t.p.iRow+1 <=  $t.rows.length-1) {
							scrollGrid($t.p.iRow+1,$t.p.iCol,'vd');
							$($t).jqGrid("editCell",$t.p.iRow+1,$t.p.iCol,false);
						}
					break;
					case 37 :
						if ($t.p.iCol -1 >=  0) {
							i = findNextVisible($t.p.iCol-1,'lft');
							scrollGrid($t.p.iRow, i,'h');
							$($t).jqGrid("editCell",$t.p.iRow, i,false);
						}
					break;
					case 39 :
						if ($t.p.iCol +1 <=  $t.p.colModel.length-1) {
							i = findNextVisible($t.p.iCol+1,'rgt');
							scrollGrid($t.p.iRow,i,'h');
							$($t).jqGrid("editCell",$t.p.iRow,i,false);
						}
					break;
					case 13:
						if (parseInt($t.p.iCol,10)>=0 && parseInt($t.p.iRow,10)>=0) {
							$($t).jqGrid("editCell",$t.p.iRow,$t.p.iCol,true);
						}
					break;
				}
				return false;
			});
			function scrollGrid(iR, iC, tp){
				if (tp.substr(0,1)=='v') {
					var ch = $($t.grid.bDiv)[0].clientHeight,
					st = $($t.grid.bDiv)[0].scrollTop,
					nROT = $t.rows[iR].offsetTop+$t.rows[iR].clientHeight,
					pROT = $t.rows[iR].offsetTop;
					if(tp == 'vd') {
						if(nROT >= ch) {
							$($t.grid.bDiv)[0].scrollTop = $($t.grid.bDiv)[0].scrollTop + $t.rows[iR].clientHeight;
						}
					}
					if(tp == 'vu'){
						if (pROT < st ) {
							$($t.grid.bDiv)[0].scrollTop = $($t.grid.bDiv)[0].scrollTop - $t.rows[iR].clientHeight;
						}
					}
				}
				if(tp=='h') {
					var cw = $($t.grid.bDiv)[0].clientWidth,
					sl = $($t.grid.bDiv)[0].scrollLeft,
					nCOL = $t.rows[iR].cells[iC].offsetLeft+$t.rows[iR].cells[iC].clientWidth,
					pCOL = $t.rows[iR].cells[iC].offsetLeft;
					if(nCOL >= cw+parseInt(sl,10)) {
						$($t.grid.bDiv)[0].scrollLeft = $($t.grid.bDiv)[0].scrollLeft + $t.rows[iR].cells[iC].clientWidth;
					} else if (pCOL < sl) {
						$($t.grid.bDiv)[0].scrollLeft = $($t.grid.bDiv)[0].scrollLeft - $t.rows[iR].cells[iC].clientWidth;
					}
				}
			}
			function findNextVisible(iC,act){
				var ind, i;
				if(act == 'lft') {
					ind = iC+1;
					for (i=iC;i>=0;i--){
						if ($t.p.colModel[i].hidden !== true) {
							ind = i;
							break;
						}
					}
				}
				if(act == 'rgt') {
					ind = iC-1;
					for (i=iC; i<$t.p.colModel.length;i++){
						if ($t.p.colModel[i].hidden !== true) {
							ind = i;
							break;
						}						
					}
				}
				return ind;
			}
		});
	},
	getChangedCells : function (mthd) {
		var ret=[];
		if (!mthd) {mthd='all';}
		this.each(function(){
			var $t= this,nm;
			if (!$t.grid || $t.p.cellEdit !== true ) {return;}
			$($t.rows).each(function(j){
				var res = {};
				if ($(this).hasClass("edited")) {
					$('td',this).each( function(i) {
						nm = $t.p.colModel[i].name;
						if ( nm !== 'cb' && nm !== 'subgrid') {
							if (mthd=='dirty') {
								if ($(this).hasClass('dirty-cell')) {
									try {
										res[nm] = $.unformat(this,{rowId:$t.rows[j].id, colModel:$t.p.colModel[i]},i);
									} catch (e){
										res[nm] = $.jgrid.htmlDecode($(this).html());
									}
								}
							} else {
								try {
									res[nm] = $.unformat(this,{rowId:$t.rows[j].id,colModel:$t.p.colModel[i]},i);
								} catch (e) {
									res[nm] = $.jgrid.htmlDecode($(this).html());
								}
							}
						}
					});
					res.id = this.id;
					ret.push(res);
				}
			});
		});
		return ret;
	}
/// end  cell editing
});
})(jQuery);
;(function($){
/**
 * jqGrid extension for SubGrid Data
 * Tony Tomov tony@trirand.com
 * http://trirand.com/blog/ 
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl-2.0.html
**/
$.jgrid.extend({
setSubGrid : function () {
	return this.each(function (){
		var $t = this, cm,
		suboptions = {
			plusicon : "ui-icon-plus",
			minusicon : "ui-icon-minus",
			openicon: "ui-icon-carat-1-sw",
			expandOnLoad:  false,
			delayOnLoad : 50,
			selectOnExpand : false,
			reloadOnExpand : true
		};
		$t.p.subGridOptions = $.extend(suboptions, $t.p.subGridOptions || {});
		$t.p.colNames.unshift("");
		$t.p.colModel.unshift({name:'subgrid',width: $.browser.safari ?  $t.p.subGridWidth+$t.p.cellLayout : $t.p.subGridWidth,sortable: false,resizable:false,hidedlg:true,search:false,fixed:true});
		cm = $t.p.subGridModel;
		if(cm[0]) {
			cm[0].align = $.extend([],cm[0].align || []);
			for(var i=0;i<cm[0].name.length;i++) { cm[0].align[i] = cm[0].align[i] || 'left';}
		}
	});
},
addSubGridCell :function (pos,iRow) {
	var prp='',ic,sid;
	this.each(function(){
		prp = this.formatCol(pos,iRow);
		sid= this.p.id;
		ic = this.p.subGridOptions.plusicon;
	});
	return "<td role=\"grid\" aria-describedby=\""+sid+"_subgrid\" class=\"ui-sgcollapsed sgcollapsed\" "+prp+"><a href='javascript:void(0);'><span class='ui-icon "+ic+"'></span></a></td>";
},
addSubGrid : function( pos, sind ) {
	return this.each(function(){
		var ts = this;
		if (!ts.grid ) { return; }
		//-------------------------
		var subGridCell = function(trdiv,cell,pos)
		{
			var tddiv = $("<td align='"+ts.p.subGridModel[0].align[pos]+"'></td>").html(cell);
			$(trdiv).append(tddiv);
		};
		var subGridXml = function(sjxml, sbid){
			var tddiv, i,  sgmap,
			dummy = $("<table cellspacing='0' cellpadding='0' border='0'><tbody></tbody></table>"),
			trdiv = $("<tr></tr>");
			for (i = 0; i<ts.p.subGridModel[0].name.length; i++) {
				tddiv = $("<th class='ui-state-default ui-th-subgrid ui-th-column ui-th-"+ts.p.direction+"'></th>");
				$(tddiv).html(ts.p.subGridModel[0].name[i]);
				$(tddiv).width( ts.p.subGridModel[0].width[i]);
				$(trdiv).append(tddiv);
			}
			$(dummy).append(trdiv);
			if (sjxml){
				sgmap = ts.p.xmlReader.subgrid;
				$(sgmap.root+" "+sgmap.row, sjxml).each( function(){
					trdiv = $("<tr class='ui-widget-content ui-subtblcell'></tr>");
					if(sgmap.repeatitems === true) {
						$(sgmap.cell,this).each( function(i) {
							subGridCell(trdiv, $(this).text() || '&#160;',i);
						});
					} else {
						var f = ts.p.subGridModel[0].mapping || ts.p.subGridModel[0].name;
						if (f) {
							for (i=0;i<f.length;i++) {
								subGridCell(trdiv, $(f[i],this).text() || '&#160;',i);
							}
						}
					}
					$(dummy).append(trdiv);
				});
			}
			var pID = $("table:first",ts.grid.bDiv).attr("id")+"_";
			$("#"+pID+sbid).append(dummy);
			ts.grid.hDiv.loading = false;
			$("#load_"+ts.p.id).hide();
			return false;
		};
		var subGridJson = function(sjxml, sbid){
			var tddiv,result , i,cur, sgmap,j,
			dummy = $("<table cellspacing='0' cellpadding='0' border='0'><tbody></tbody></table>"),
			trdiv = $("<tr></tr>");
			for (i = 0; i<ts.p.subGridModel[0].name.length; i++) {
				tddiv = $("<th class='ui-state-default ui-th-subgrid ui-th-column ui-th-"+ts.p.direction+"'></th>");
				$(tddiv).html(ts.p.subGridModel[0].name[i]);
				$(tddiv).width( ts.p.subGridModel[0].width[i]);
				$(trdiv).append(tddiv);
			}
			$(dummy).append(trdiv);
			if (sjxml){
				sgmap = ts.p.jsonReader.subgrid;
				result = sjxml[sgmap.root];
				if ( typeof result !== 'undefined' ) {
					for (i=0;i<result.length;i++) {
						cur = result[i];
						trdiv = $("<tr class='ui-widget-content ui-subtblcell'></tr>");
						if(sgmap.repeatitems === true) {
							if(sgmap.cell) { cur=cur[sgmap.cell]; }
							for (j=0;j<cur.length;j++) {
								subGridCell(trdiv, cur[j] || '&#160;',j);
							}
						} else {
							var f = ts.p.subGridModel[0].mapping || ts.p.subGridModel[0].name;
							if(f.length) {
								for (j=0;j<f.length;j++) {
									subGridCell(trdiv, cur[f[j]] || '&#160;',j);
								}
							}
						}
						$(dummy).append(trdiv);
					}
				}
			}
			var pID = $("table:first",ts.grid.bDiv).attr("id")+"_";
			$("#"+pID+sbid).append(dummy);
			ts.grid.hDiv.loading = false;
			$("#load_"+ts.p.id).hide();
			return false;
		};
		var populatesubgrid = function( rd )
		{
			var sid,dp, i, j;
			sid = $(rd).attr("id");
			dp = {nd_: (new Date().getTime())};
			dp[ts.p.prmNames.subgridid]=sid;
			if(!ts.p.subGridModel[0]) { return false; }
			if(ts.p.subGridModel[0].params) {
				for(j=0; j < ts.p.subGridModel[0].params.length; j++) {
					for(i=0; i<ts.p.colModel.length; i++) {
						if(ts.p.colModel[i].name == ts.p.subGridModel[0].params[j]) {
							dp[ts.p.colModel[i].name]= $("td:eq("+i+")",rd).text().replace(/\&#160\;/ig,'');
						}
					}
				}
			}
			if(!ts.grid.hDiv.loading) {
				ts.grid.hDiv.loading = true;
				$("#load_"+ts.p.id).show();
				if(!ts.p.subgridtype) { ts.p.subgridtype = ts.p.datatype; }
				if($.isFunction(ts.p.subgridtype)) {
					ts.p.subgridtype.call(ts, dp);
				} else {
					ts.p.subgridtype = ts.p.subgridtype.toLowerCase();
				}
				switch(ts.p.subgridtype) {
					case "xml":
					case "json":
					$.ajax($.extend({
						type:ts.p.mtype,
						url: ts.p.subGridUrl,
						dataType:ts.p.subgridtype,
						data: $.isFunction(ts.p.serializeSubGridData)? ts.p.serializeSubGridData.call(ts, dp) : dp,
						complete: function(sxml) {
							if(ts.p.subgridtype == "xml") {
								subGridXml(sxml.responseXML, sid);
							} else {
								subGridJson($.jgrid.parse(sxml.responseText),sid);
							}
							sxml=null;
						}
					}, $.jgrid.ajaxOptions, ts.p.ajaxSubgridOptions || {}));
					break;
				}
			}
			return false;
		};
		var _id, pID,atd, nhc=0, bfsc, r;
		$.each(ts.p.colModel,function(i,v){
			if(this.hidden === true || this.name == 'rn' || this.name == 'cb') {
				nhc++;
			}
		});
		var len = ts.rows.length, i=1;
		if( sind !== undefined && sind > 0) {
			i = sind;
			len = sind+1;
		}
		while(i < len) {
			if($(ts.rows[i]).hasClass('jqgrow')) {
				$(ts.rows[i].cells[pos]).bind('click', function(e) {
					var tr = $(this).parent("tr")[0];
					r = tr.nextSibling;
					if($(this).hasClass("sgcollapsed")) {
						pID = ts.p.id;
						_id = tr.id;
						if(ts.p.subGridOptions.reloadOnExpand === true || ( ts.p.subGridOptions.reloadOnExpand === false && !$(r).hasClass('ui-subgrid') ) ) {
							atd = pos >=1 ? "<td colspan='"+pos+"'>&#160;</td>":"";
							bfsc =true;
							if($.isFunction(ts.p.subGridBeforeExpand)) {
								bfsc = ts.p.subGridBeforeExpand.call(ts, pID+"_"+_id,_id);
							}
							if(bfsc === false) {return false;}
								$(tr).after( "<tr role='row' class='ui-subgrid'>"+atd+"<td class='ui-widget-content subgrid-cell'><span class='ui-icon "+ts.p.subGridOptions.openicon+"'></span></td><td colspan='"+parseInt(ts.p.colNames.length-1-nhc,10)+"' class='ui-widget-content subgrid-data'><div id="+pID+"_"+_id+" class='tablediv'></div></td></tr>" );
							if( $.isFunction(ts.p.subGridRowExpanded)) {
								ts.p.subGridRowExpanded.call(ts, pID+"_"+ _id,_id);
							} else {
								populatesubgrid(tr);
							}
						} else {
							$(r).show();
						}
						$(this).html("<a href='javascript:void(0);'><span class='ui-icon "+ts.p.subGridOptions.minusicon+"'></span></a>").removeClass("sgcollapsed").addClass("sgexpanded");
						if(ts.p.subGridOptions.selectOnExpand) {
							$(ts).jqGrid('setSelection',_id);
						}
					} else if($(this).hasClass("sgexpanded")) {
						bfsc = true;
						if( $.isFunction(ts.p.subGridRowColapsed)) {
							_id = tr.id;
							bfsc = ts.p.subGridRowColapsed.call(ts, pID+"_"+_id,_id );
						}
						if(bfsc===false) {return false;}
							if(ts.p.subGridOptions.reloadOnExpand === true) {
								$(r).remove(".ui-subgrid");
							} else if($(r).hasClass('ui-subgrid')) { // incase of dynamic deleting
								$(r).hide();
							}
							$(this).html("<a href='javascript:void(0);'><span class='ui-icon "+ts.p.subGridOptions.plusicon+"'></span></a>").removeClass("sgexpanded").addClass("sgcollapsed");
					}
					return false;
				});
				if(ts.p.subGridOptions.expandOnLoad === true) {
					setTimeout(function(){ $(tr.cells[pos]).trigger('click'); },i*ts.p.subGridOptions.delayOnLoad);
				}
			}
			i++;
		}
		ts.subGridXml = function(xml,sid) {subGridXml(xml,sid);};
		ts.subGridJson = function(json,sid) {subGridJson(json,sid);};
	});
},
expandSubGridRow : function(rowid) {
	return this.each(function () {
		var $t = this;
		if(!$t.grid && !rowid) {return;}
		if($t.p.subGrid===true) {
			var rc = $(this).jqGrid("getInd",rowid,true);
			if(rc) {
				var sgc = $("td.sgcollapsed",rc)[0];
				if(sgc) {
					$(sgc).trigger("click");
				}
			}
		}
	});
},
collapseSubGridRow : function(rowid) {
	return this.each(function () {
		var $t = this;
		if(!$t.grid && !rowid) {return;}
		if($t.p.subGrid===true) {
			var rc = $(this).jqGrid("getInd",rowid,true);
			if(rc) {
				var sgc = $("td.sgexpanded",rc)[0];
				if(sgc) {
					$(sgc).trigger("click");
				}
			}
		}
	});
},
toggleSubGridRow : function(rowid) {
	return this.each(function () {
		var $t = this;
		if(!$t.grid && !rowid) {return;}
		if($t.p.subGrid===true) {
			var rc = $(this).jqGrid("getInd",rowid,true);
			if(rc) {
				var sgc = $("td.sgcollapsed",rc)[0];
				if(sgc) {
					$(sgc).trigger("click");
				} else {
					sgc = $("td.sgexpanded",rc)[0];
					if(sgc) {
						$(sgc).trigger("click");
					}
				}
			}
		}
	});
}
});
})(jQuery);
/**
 * jqGrid extension - Tree Grid
 * Tony Tomov tony@trirand.com
 * http://trirand.com/blog/
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
**/

/*global document, jQuery, $ */
(function($) {
$.jgrid.extend({
	setTreeNode : function(i, len){
		return this.each(function(){
			var $t = this;
			if( !$t.grid || !$t.p.treeGrid ) {return;}
			var expCol = $t.p.expColInd,
			expanded = $t.p.treeReader.expanded_field,
			isLeaf = $t.p.treeReader.leaf_field,
			level = $t.p.treeReader.level_field,
			icon = $t.p.treeReader.icon_field,
			loaded = $t.p.treeReader.loaded,  lft, rgt, curLevel, ident,lftpos, twrap,
			ldat, lf;
			while(i<len) {
				var ind = $t.rows[i].id, dind = $t.p._index[ind], expan;
				ldat = $t.p.data[dind];
				//$t.rows[i].level = ldat[level];
				if($t.p.treeGridModel == 'nested') {
					if(!ldat[isLeaf]) {
					lft = parseInt(ldat[$t.p.treeReader.left_field],10);
					rgt = parseInt(ldat[$t.p.treeReader.right_field],10);
					// NS Model
						ldat[isLeaf] = (rgt === lft+1) ? 'true' : 'false';
						$t.rows[i].cells[$t.p._treeleafpos].innerHTML = ldat[isLeaf];
					}
				}
				//else {
					//row.parent_id = rd[$t.p.treeReader.parent_id_field];
				//}
				curLevel = parseInt(ldat[level],10);
				if($t.p.tree_root_level === 0) {
					ident = curLevel+1;
					lftpos = curLevel;
				} else {
					ident = curLevel;
					lftpos = curLevel -1;
				}
				twrap = "<div class='tree-wrap tree-wrap-"+$t.p.direction+"' style='width:"+(ident*18)+"px;'>";
				twrap += "<div style='"+($t.p.direction=="rtl" ? "right:" : "left:")+(lftpos*18)+"px;' class='ui-icon ";


				if(ldat[loaded] !== undefined) {
					if(ldat[loaded]=="true" || ldat[loaded]===true) {
						ldat[loaded] = true;
					} else {
						ldat[loaded] = false;
					}
				}
				if(ldat[isLeaf] == "true" || ldat[isLeaf] === true) {
					twrap += ((ldat[icon] !== undefined && ldat[icon] !== "") ? ldat[icon] : $t.p.treeIcons.leaf)+" tree-leaf treeclick'";
					ldat[isLeaf] = true;
					lf="leaf";
				} else {
					ldat[isLeaf] = false;
					lf="";
				}
				ldat[expanded] = ((ldat[expanded] == "true" || ldat[expanded] === true) ? true : false) && ldat[loaded];
				if(ldat[expanded] === true) {
					twrap += $t.p.treeIcons.minus+" tree-minus treeclick'";
				} else {
					twrap += $t.p.treeIcons.plus+" tree-plus treeclick'";
				}
				
				twrap += "</div></div>";
				$($t.rows[i].cells[expCol]).wrapInner("<span class='cell-wrapper"+lf+"'></span>").prepend(twrap);

				if(curLevel !== parseInt($t.p.tree_root_level,10)) {
					var pn = $($t).jqGrid('getNodeParent',ldat);
					expan = pn && pn.hasOwnProperty(expanded) ? pn[expanded] : true;
					if( !expan ){
						$($t.rows[i]).css("display","none");
					}
				}
				$($t.rows[i].cells[expCol])
					.find("div.treeclick")
					.bind("click",function(e){
						var target = e.target || e.srcElement,
						ind2 =$(target,$t.rows).closest("tr.jqgrow")[0].id,
						pos = $t.p._index[ind2];
						if(!$t.p.data[pos][isLeaf]){
							if($t.p.data[pos][expanded]){
								$($t).jqGrid("collapseRow",$t.p.data[pos]);
								$($t).jqGrid("collapseNode",$t.p.data[pos]);
							} else {
								$($t).jqGrid("expandRow",$t.p.data[pos]);
								$($t).jqGrid("expandNode",$t.p.data[pos]);
							}
						}
						return false;
					});
				if($t.p.ExpandColClick === true) {
					$($t.rows[i].cells[expCol])
						.find("span.cell-wrapper")
						.css("cursor","pointer")
						.bind("click",function(e) {
							var target = e.target || e.srcElement,
							ind2 =$(target,$t.rows).closest("tr.jqgrow")[0].id,
							pos = $t.p._index[ind2];
							if(!$t.p.data[pos][isLeaf]){
								if($t.p.data[pos][expanded]){
									$($t).jqGrid("collapseRow",$t.p.data[pos]);
									$($t).jqGrid("collapseNode",$t.p.data[pos]);
								} else {
									$($t).jqGrid("expandRow",$t.p.data[pos]);
									$($t).jqGrid("expandNode",$t.p.data[pos]);
								}
							}
							$($t).jqGrid("setSelection",ind2);
							return false;
						});
				}
				i++;
			}

		});
	},
	setTreeGrid : function() {
		return this.each(function (){
			var $t = this, i=0, pico, ecol = false, nm, key, dupcols=[];
			if(!$t.p.treeGrid) {return;}
			if(!$t.p.treedatatype ) {$.extend($t.p,{treedatatype: $t.p.datatype});}
			$t.p.subGrid = false;$t.p.altRows =false;
			$t.p.pgbuttons = false;$t.p.pginput = false;
			$t.p.gridview =  true;
			$t.p.multiselect = false;$t.p.rowList = [];
			$t.p.expColInd = 0;
			pico = 'ui-icon-triangle-1-' + ($t.p.direction=="rtl" ? 'w' : 'e');
			$t.p.treeIcons = $.extend({plus:pico,minus:'ui-icon-triangle-1-s',leaf:'ui-icon-radio-off'},$t.p.treeIcons || {});
			if($t.p.treeGridModel == 'nested') {
				$t.p.treeReader = $.extend({
					level_field: "level",
					left_field:"lft",
					right_field: "rgt",
					leaf_field: "isLeaf",
					expanded_field: "expanded",
					loaded: "loaded",
					icon_field: "icon"
				},$t.p.treeReader);
			} else if($t.p.treeGridModel == 'adjacency') {
				$t.p.treeReader = $.extend({
						level_field: "level",
						parent_id_field: "parent",
						leaf_field: "isLeaf",
						expanded_field: "expanded",
						loaded: "loaded",
						icon_field: "icon"
				},$t.p.treeReader );
			}
			for ( key in $t.p.colModel){
				if($t.p.colModel.hasOwnProperty(key)) {
					nm = $t.p.colModel[key].name;
					if( nm == $t.p.ExpandColumn && !ecol ) {
						ecol = true;
						$t.p.expColInd = i;
					}
					i++;
					//
					for(var tkey in $t.p.treeReader) {
						if($t.p.treeReader[tkey] == nm)
							dupcols.push(nm);
				}

			}
			}
			$.each($t.p.treeReader,function(j,n){
				if(n && $.inArray(n, dupcols) === -1){
					if(j==='leaf_field') { $t.p._treeleafpos= i; }
				i++;
					$t.p.colNames.push(n);
					$t.p.colModel.push({name:n,width:1,hidden:true,sortable:false,resizable:false,hidedlg:true,editable:true,search:false});
				}
			});			
		});
	},
	expandRow: function (record){
		this.each(function(){
			var $t = this;
			if(!$t.grid || !$t.p.treeGrid) {return;}
			var childern = $($t).jqGrid("getNodeChildren",record),
			//if ($($t).jqGrid("isVisibleNode",record)) {
			expanded = $t.p.treeReader.expanded_field;
			$(childern).each(function(i){
				var id  = $.jgrid.getAccessor(this,$t.p.localReader.id);
				$("#"+id,$t.grid.bDiv).css("display","");
				if(this[expanded]) {
					$($t).jqGrid("expandRow",this);
				}
			});
			//}
		});
	},
	collapseRow : function (record) {
		this.each(function(){
			var $t = this;
			if(!$t.grid || !$t.p.treeGrid) {return;}
			var childern = $($t).jqGrid("getNodeChildren",record),
			expanded = $t.p.treeReader.expanded_field;
			$(childern).each(function(i){
				var id  = $.jgrid.getAccessor(this,$t.p.localReader.id);
				$("#"+id,$t.grid.bDiv).css("display","none");
				if(this[expanded]){
					$($t).jqGrid("collapseRow",this);
				}
			});
		});
	},
	// NS ,adjacency models
	getRootNodes : function() {
		var result = [];
		this.each(function(){
			var $t = this;
			if(!$t.grid || !$t.p.treeGrid) {return;}
			switch ($t.p.treeGridModel) {
				case 'nested' :
					var level = $t.p.treeReader.level_field;
					$($t.p.data).each(function(i){
						if(parseInt(this[level],10) === parseInt($t.p.tree_root_level,10)) {
							result.push(this);
						}
					});
					break;
				case 'adjacency' :
					var parent_id = $t.p.treeReader.parent_id_field;
					$($t.p.data).each(function(i){
						if(this[parent_id] === null || String(this[parent_id]).toLowerCase() == "null") {
							result.push(this);
						}
					});
					break;
			}
		});
		return result;
	},
	getNodeDepth : function(rc) {
		var ret = null;
		this.each(function(){
			if(!this.grid || !this.p.treeGrid) {return;}
			var $t = this;
			switch ($t.p.treeGridModel) {
				case 'nested' :
					var level = $t.p.treeReader.level_field;
					ret = parseInt(rc[level],10) - parseInt($t.p.tree_root_level,10);
					break;
				case 'adjacency' :
					ret = $($t).jqGrid("getNodeAncestors",rc).length;
					break;
			}
		});
		return ret;
	},
	getNodeParent : function(rc) {
		var result = null;
		this.each(function(){
			var $t = this;
			if(!$t.grid || !$t.p.treeGrid) {return;}
			switch ($t.p.treeGridModel) {
				case 'nested' :
					var lftc = $t.p.treeReader.left_field,
					rgtc = $t.p.treeReader.right_field,
					levelc = $t.p.treeReader.level_field,
					lft = parseInt(rc[lftc],10), rgt = parseInt(rc[rgtc],10), level = parseInt(rc[levelc],10);
					$(this.p.data).each(function(){
						if(parseInt(this[levelc],10) === level-1 && parseInt(this[lftc],10) < lft && parseInt(this[rgtc],10) > rgt) {
							result = this;
							return false;
						}
					});
					break;
				case 'adjacency' :
					var parent_id = $t.p.treeReader.parent_id_field,
					dtid = $t.p.localReader.id;
					$(this.p.data).each(function(i,val){
						if(this[dtid] == rc[parent_id] ) {
							result = this;
							return false;
						}
					});
					break;
			}
		});
		return result;
	},
	getNodeChildren : function(rc) {
		var result = [];
		this.each(function(){
			var $t = this;
			if(!$t.grid || !$t.p.treeGrid) {return;}
			switch ($t.p.treeGridModel) {
				case 'nested' :
					var lftc = $t.p.treeReader.left_field,
					rgtc = $t.p.treeReader.right_field,
					levelc = $t.p.treeReader.level_field,
					lft = parseInt(rc[lftc],10), rgt = parseInt(rc[rgtc],10), level = parseInt(rc[levelc],10);
					$(this.p.data).each(function(i){
						if(parseInt(this[levelc],10) === level+1 && parseInt(this[lftc],10) > lft && parseInt(this[rgtc],10) < rgt) {
							result.push(this);
						}
					});
					break;
				case 'adjacency' :
					var parent_id = $t.p.treeReader.parent_id_field,
					dtid = $t.p.localReader.id;
					$(this.p.data).each(function(i,val){
						if(this[parent_id] == rc[dtid]) {
							result.push(this);
						}
					});
					break;
			}
		});
		return result;
	},
	getFullTreeNode : function(rc) {
		var result = [];
		this.each(function(){
			var $t = this, len;
			if(!$t.grid || !$t.p.treeGrid) {return;}
			switch ($t.p.treeGridModel) {
				case 'nested' :
					var lftc = $t.p.treeReader.left_field,
					rgtc = $t.p.treeReader.right_field,
					levelc = $t.p.treeReader.level_field,
					lft = parseInt(rc[lftc],10), rgt = parseInt(rc[rgtc],10), level = parseInt(rc[levelc],10);
					$(this.p.data).each(function(i){
						if(parseInt(this[levelc],10) >= level && parseInt(this[lftc],10) >= lft && parseInt(this[lftc],10) <= rgt) {
							result.push(this);
						}
					});
					break;
				case 'adjacency' :
					if(rc) {
					result.push(rc);
					var parent_id = $t.p.treeReader.parent_id_field,
					dtid = $t.p.localReader.id;
					$(this.p.data).each(function(i){
						len = result.length;
						for (i = 0; i < len; i++) {
							if (result[i][dtid] == this[parent_id]) {
								result.push(this);
								break;
							}
						}
					});
					}
					break;
			}
		});
		return result;
	},	
	// End NS, adjacency Model
	getNodeAncestors : function(rc) {
		var ancestors = [];
		this.each(function(){
			if(!this.grid || !this.p.treeGrid) {return;}
			var parent = $(this).jqGrid("getNodeParent",rc);
			while (parent) {
				ancestors.push(parent);
				parent = $(this).jqGrid("getNodeParent",parent);	
			}
		});
		return ancestors;
	},
	isVisibleNode : function(rc) {
		var result = true;
		this.each(function(){
			var $t = this;
			if(!$t.grid || !$t.p.treeGrid) {return;}
			var ancestors = $($t).jqGrid("getNodeAncestors",rc),
			expanded = $t.p.treeReader.expanded_field;
			$(ancestors).each(function(){
				result = result && this[expanded];
				if(!result) {return false;}
			});
		});
		return result;
	},
	isNodeLoaded : function(rc) {
		var result;
		this.each(function(){
			var $t = this;
			if(!$t.grid || !$t.p.treeGrid) {return;}
			var isLeaf = $t.p.treeReader.leaf_field;
			if(rc !== undefined ) {
				if(rc.loaded !== undefined) {
					result = rc.loaded;
				} else if( rc[isLeaf] || $($t).jqGrid("getNodeChildren",rc).length > 0){
					result = true;
				} else {
					result = false;
				}
			} else {
				result = false;
			}
		});
		return result;
	},
	expandNode : function(rc) {
		return this.each(function(){
			if(!this.grid || !this.p.treeGrid) {return;}
			var expanded = this.p.treeReader.expanded_field,
			parent = this.p.treeReader.parent_id_field,
			loaded = this.p.treeReader.loaded,
			level = this.p.treeReader.level_field,
			lft = this.p.treeReader.left_field,
			rgt = this.p.treeReader.right_field;

			if(!rc[expanded]) {
				var id = $.jgrid.getAccessor(rc,this.p.localReader.id);
				var rc1 = $("#"+id,this.grid.bDiv)[0];
				var position = this.p._index[id];
				if( $(this).jqGrid("isNodeLoaded",this.p.data[position]) ) {
					rc[expanded] = true;
					$("div.treeclick",rc1).removeClass(this.p.treeIcons.plus+" tree-plus").addClass(this.p.treeIcons.minus+" tree-minus");
				} else {
					rc[expanded] = true;
					$("div.treeclick",rc1).removeClass(this.p.treeIcons.plus+" tree-plus").addClass(this.p.treeIcons.minus+" tree-minus");
					this.p.treeANode = rc1.rowIndex;
					this.p.datatype = this.p.treedatatype;
					if(this.p.treeGridModel == 'nested') {
						$(this).jqGrid("setGridParam",{postData:{nodeid:id,n_left:rc[lft],n_right:rc[rgt],n_level:rc[level]}});
					} else {
						$(this).jqGrid("setGridParam",{postData:{nodeid:id,parentid:rc[parent],n_level:rc[level]}} );
					}
					$(this).trigger("reloadGrid");
					rc[loaded] = true;
					if(this.p.treeGridModel == 'nested') {
						$(this).jqGrid("setGridParam",{postData:{nodeid:'',n_left:'',n_right:'',n_level:''}});
					} else {
						$(this).jqGrid("setGridParam",{postData:{nodeid:'',parentid:'',n_level:''}}); 
					}
				}
			}
		});
	},
	collapseNode : function(rc) {
		return this.each(function(){
			if(!this.grid || !this.p.treeGrid) {return;}
			if(rc.expanded) {
				rc.expanded = false;
				var id = $.jgrid.getAccessor(rc,this.p.localReader.id);
				var rc1 = $("#"+id,this.grid.bDiv)[0];
				$("div.treeclick",rc1).removeClass(this.p.treeIcons.minus+" tree-minus").addClass(this.p.treeIcons.plus+" tree-plus");
			}
		});
	},
	SortTree : function( sortname, newDir, st, datefmt) {
		return this.each(function(){
			if(!this.grid || !this.p.treeGrid) {return;}
			var i, len,
			rec, records = [], $t = this, query, roots,
			rt = $(this).jqGrid("getRootNodes");
			// Sorting roots
			query = $.jgrid.from(rt);
			query.orderBy(sortname,newDir,st, datefmt);
			roots = query.select();

			// Sorting children
			for (i = 0, len = roots.length; i < len; i++) {
				rec = roots[i];
				records.push(rec);
				$(this).jqGrid("collectChildrenSortTree",records, rec, sortname, newDir,st, datefmt);
			}
			$.each(records, function(index, row) {
				var id  = $.jgrid.getAccessor(this,$t.p.localReader.id);
				$('#'+$t.p.id+ ' tbody tr:eq('+index+')').after($('tr#'+id,$t.grid.bDiv));
			});
			query = null;roots=null;records=null;
		});
	},
	collectChildrenSortTree : function(records, rec, sortname, newDir,st, datefmt) {
		return this.each(function(){
			if(!this.grid || !this.p.treeGrid) {return;}
			var i, len,
			child, ch, query, children;
			ch = $(this).jqGrid("getNodeChildren",rec);
			query = $.jgrid.from(ch);
			query.orderBy(sortname, newDir, st, datefmt);
			children = query.select();
			for (i = 0, len = children.length; i < len; i++) {
				child = children[i];
				records.push(child);
				$(this).jqGrid("collectChildrenSortTree",records, child, sortname, newDir, st, datefmt); 
			}
		});
	},
	// experimental 
	setTreeRow : function(rowid, data) {
		var success=false;
		this.each(function(){
			var t = this;
			if(!t.grid || !t.p.treeGrid) {return;}
			success = $(t).jqGrid("setRowData",rowid,data);
		});
		return success;
	},
	delTreeNode : function (rowid) {
		return this.each(function () {
			var $t = this, rid = $t.p.localReader.id,
			left = $t.p.treeReader.left_field,
			right = $t.p.treeReader.right_field, myright, width, res, key;
			if(!$t.grid || !$t.p.treeGrid) {return;}
			var rc = $t.p._index[rowid];
			if (rc !== undefined) {
				// nested
				myright = parseInt($t.p.data[rc][right],10);
				width = myright -  parseInt($t.p.data[rc][left],10) + 1;
				var dr = $($t).jqGrid("getFullTreeNode",$t.p.data[rc]);
				if(dr.length>0){
					for (var i=0;i<dr.length;i++){
						$($t).jqGrid("delRowData",dr[i][rid]);
					}
				}
				if( $t.p.treeGridModel === "nested") {
					// ToDo - update grid data
					res = $.jgrid.from($t.p.data)
						.greater(left,myright,{stype:'integer'})
						.select();
					if(res.length) {
						for( key in res) {
							res[key][left] = parseInt(res[key][left],10) - width ;
			}
					}
					res = $.jgrid.from($t.p.data)
						.greater(right,myright,{stype:'integer'})
						.select();
					if(res.length) {
						for( key in res) {
							res[key][right] = parseInt(res[key][right],10) - width ;
						}
					}
				}
			}
		});
	},
	addChildNode : function( nodeid, parentid, data ) {
		//return this.each(function(){
		var $t = this[0];
		if(data) {
			// we suppose tha the id is autoincremet and
			var expanded = $t.p.treeReader.expanded_field,
			isLeaf = $t.p.treeReader.leaf_field,
			level = $t.p.treeReader.level_field,
			icon = $t.p.treeReader.icon_field,
			parent = $t.p.treeReader.parent_id_field,
			left = $t.p.treeReader.left_field,
			right = $t.p.treeReader.right_field,
			loaded = $t.p.treeReader.loaded,
			method, parentindex, parentdata, parentlevel, i, len, max=0, rowind = parentid, leaf, maxright;

			if ( !nodeid ) {
				i = $t.p.data.length-1;
				if(	i>= 0 ) {
					while(i>=0){max = Math.max(max, parseInt($t.p.data[i][$t.p.localReader.id],10)); i--;}
				}
				nodeid = max+1;
			}
			var prow = $($t).jqGrid('getInd', parentid);
				leaf = false;
				// if not a parent we assume root
				if ( parentid === undefined  || parentid === null || parentid==="") {
					parentid = null;
					rowind = null;
					method = 'last';
					parentlevel = $t.p.tree_root_level;
					i = $t.p.data.length+1;
				} else {
					method = 'after';
					parentindex = $t.p._index[parentid];
					parentdata = $t.p.data[parentindex];
					parentid = parentdata[$t.p.localReader.id];
				parentlevel = parseInt(parentdata[level],10)+1;
				var childs = $($t).jqGrid('getFullTreeNode', parentdata);
					// if there are child nodes get the last index of it
					if(childs.length) {
						i = childs[childs.length-1][$t.p.localReader.id];
						rowind = i;
						i = $($t).jqGrid('getInd',rowind)+1;
					} else {
						i = $($t).jqGrid('getInd', parentid)+1;
					}
					// if the node is leaf
					if(parentdata[isLeaf]) {
						leaf = true;
						parentdata[expanded] = true;
						//var prow = $($t).jqGrid('getInd', parentid);
						$($t.rows[prow])
							.find("span.cell-wrapperleaf").removeClass("cell-wrapperleaf").addClass("cell-wrapper")
							.end()
							.find("div.tree-leaf").removeClass($t.p.treeIcons.leaf+" tree-leaf").addClass($t.p.treeIcons.minus+" tree-minus");
						$t.p.data[parentindex][isLeaf] = false;
						parentdata[loaded] = true;
					}
				}
				len = i+1;

			data[expanded] = false;
			data[loaded] = true;
			data[level] = parentlevel;
			data[isLeaf] = true;
			if( $t.p.treeGridModel === "adjacency") {
			data[parent] = parentid;
			}
			if( $t.p.treeGridModel === "nested") {
				// this method requiere more attention
				var query, res, key;
				//maxright = parseInt(maxright,10);
				// ToDo - update grid data
				if(parentid !== null) {
					maxright = parseInt(parentdata[right],10);
					query = $.jgrid.from($t.p.data);
					query = query.greaterOrEquals(right,maxright,{stype:'integer'});
					res = query.select();
					if(res.length) {
						for( key in res) {
							res[key][left] = res[key][left] > maxright ? parseInt(res[key][left],10) +2 : res[key][left];
							res[key][right] = res[key][right] >= maxright ? parseInt(res[key][right],10) +2 : res[key][right];
						}
					}
					data[left] = maxright;
					data[right]= maxright+1;
				} else {
					maxright = parseInt( $($t).jqGrid('getCol', right, false, 'max'), 10);
					res = $.jgrid.from($t.p.data)
						.greater(left,maxright,{stype:'integer'})
						.select();
					if(res.length) {
						for( key in res) {
							res[key][left] = parseInt(res[key][left],10) +2 ;
						}
					}
					res = $.jgrid.from($t.p.data)
						.greater(right,maxright,{stype:'integer'})
						.select();
					if(res.length) {
						for( key in res) {
							res[key][right] = parseInt(res[key][right],10) +2 ;
						}
					}
					data[left] = maxright+1;
					data[right] = maxright + 2;
				}
			}
			if( parentid === null || $($t).jqGrid("isNodeLoaded",parentdata) || leaf ) {
					$($t).jqGrid('addRowData', nodeid, data, method, rowind);
					$($t).jqGrid('setTreeNode', i, len);
			}
			if(parentdata && !parentdata[expanded]) {
				$($t.rows[prow])
					.find("div.treeclick")
					.click();
			}
		}
		//});
	}
});
})(jQuery);// Grouping module
;(function($){
$.jgrid.extend({
	groupingSetup : function () {
		return this.each(function (){
			var $t = this,
			grp = $t.p.groupingView;
			if(grp !== null && ( (typeof grp === 'object') || $.isFunction(grp) ) ) {
				if(!grp.groupField.length) {
					$t.p.grouping = false;
				} else {
					if ( typeof(grp.visibiltyOnNextGrouping) == 'undefined') {
						grp.visibiltyOnNextGrouping = [];
					}
					for(var i=0;i<grp.groupField.length;i++) {
						if(!grp.groupOrder[i]) {
							grp.groupOrder[i] = 'asc';
						}
						if(!grp.groupText[i]) {
							grp.groupText[i] = '{0}';
						}
						if( typeof(grp.groupColumnShow[i]) != 'boolean') {
							grp.groupColumnShow[i] = true;
						}
						if( typeof(grp.groupSummary[i]) != 'boolean') {
							grp.groupSummary[i] = false;
						}
						if(grp.groupColumnShow[i] === true) {
							grp.visibiltyOnNextGrouping[i] = true;
							$($t).jqGrid('showCol',grp.groupField[i]);
						} else {
							grp.visibiltyOnNextGrouping[i] = $("#"+$t.p.id+"_"+grp.groupField[i]).is(":visible");
							$($t).jqGrid('hideCol',grp.groupField[i]);
						}
						grp.sortitems[i] = [];
						grp.sortnames[i] = [];
						grp.summaryval[i] = [];
						if(grp.groupSummary[i]) {
							grp.summary[i] =[];
							var cm = $t.p.colModel;
							for(var j=0, cml = cm.length; j < cml; j++) {
								if(cm[j].summaryType) {
									grp.summary[i].push({nm:cm[j].name,st:cm[j].summaryType, v:''});
								}
							}
						}
					}
					$t.p.scroll = false;
					$t.p.rownumbers = false;
					$t.p.subGrid = false;
					$t.p.treeGrid = false;
					$t.p.gridview = true;
				}
			} else {
				$t.p.grouping = false;
			}
		});
	},
	groupingPrepare : function (rData, items, gdata, record) {
		this.each(function(){
			// currently only one level
			// Is this a good idea to do it so!!!!?????
			items[0]  += "";
			var itm = items[0].toString().split(' ').join('');
			
			var grp = this.p.groupingView, $t= this;
			if(gdata.hasOwnProperty(itm)) {
				gdata[itm].push(rData);
			} else {
				gdata[itm] = [];
				gdata[itm].push(rData);
				grp.sortitems[0].push(itm);
				grp.sortnames[0].push($.trim(items[0].toString()));
				grp.summaryval[0][itm] = $.extend(true,[],grp.summary[0]);
			}
			if(grp.groupSummary[0]) {
				$.each(grp.summaryval[0][itm],function(i,n) {
					if ($.isFunction(this.st)) {
						this.v = this.st.call($t, this.v, this.nm, record);
					} else {
						this.v = $($t).jqGrid('groupingCalculations.'+this.st, this.v, this.nm, record);
					}
				});
			}
		});
		return gdata;
	},
	groupingToggle : function(hid){
		this.each(function(){
			var $t = this,
			grp = $t.p.groupingView,
			strpos = hid.lastIndexOf('_'),
			uid = hid.substring(0,strpos+1),
			num = parseInt(hid.substring(strpos+1),10)+1,
			minus = grp.minusicon,
			plus = grp.plusicon,
			tar = $("#"+hid),
			r = tar[0].nextSibling,
			tarspan = $("#"+hid+" span."+"tree-wrap-"+$t.p.direction),
			collapsed = false;
			if( tarspan.hasClass(minus) ) {
				if(grp.showSummaryOnHide && grp.groupSummary[0]) {
					if(r){
						while(r) {
							if($(r).hasClass('jqfoot') ) { break; }
							$(r).hide();
							r = r.nextSibling;
						}
					}
				} else  {
					if(r){
						while(r) {
							if($(r).attr('id') ==uid+String(num) ) { break; }
							$(r).hide();
							r = r.nextSibling;
				}
					}
				}
				tarspan.removeClass(minus).addClass(plus);
				collapsed = true;
			} else {
				if(r){
					while(r) {
						if($(r).attr('id') ==uid+String(num) ) { break; }
						$(r).show();
						r = r.nextSibling;
					}
				}
				tarspan.removeClass(plus).addClass(minus);
				collapsed = false;
			}
			if( $.isFunction($t.p.onClickGroup)) { $t.p.onClickGroup.call($t, hid , collapsed); }

		});
		return false;
	},
	groupingRender : function (grdata, colspans ) {
		return this.each(function(){
			var $t = this,
			grp = $t.p.groupingView,
			str = "", icon = "", hid, pmrtl ="", gv, cp, ii;
			//only one level for now
			if(!grp.groupDataSorted) {
				// ???? TO BE IMPROVED
				grp.sortitems[0].sort();
				grp.sortnames[0].sort();
				if(grp.groupOrder[0].toLowerCase() == 'desc')
				{
					grp.sortitems[0].reverse();
					grp.sortnames[0].reverse();
				}
			}   
			if(grp.groupCollapse) { pmrtl = grp.plusicon; }
			else {pmrtl = grp.minusicon;}
			pmrtl += " tree-wrap-"+$t.p.direction; 
			ii = 0;
			while(ii < colspans) {
				if($t.p.colModel[ii].name == grp.groupField[0]) {
					cp = ii;
					break;
				}
				ii++;
			}
			$.each(grp.sortitems[0],function(i,n){
				hid = $t.p.id+"ghead_"+i;
				icon = "<span style='cursor:pointer;' class='ui-icon "+pmrtl+"' onclick=\"jQuery('#"+$t.p.id+"').jqGrid('groupingToggle','"+hid+"');return false;\"></span>";
				try {
					gv = $t.formatter(hid, grp.sortnames[0][i], cp, grp.sortitems[0] );
				} catch (egv) {
					gv = grp.sortnames[0][i];
				}
				str += "<tr id=\""+hid+"\" role=\"row\" class= \"ui-widget-content jqgroup ui-row-"+$t.p.direction+"\"><td colspan=\""+colspans+"\">"+icon+$.jgrid.format(grp.groupText[0], gv, grdata[n].length)+"</td></tr>";
				for(var kk=0;kk<grdata[n].length;kk++) {
					str += grdata[n][kk].join('');
				}
				if(grp.groupSummary[0]) {
					var hhdr = "";
					if(grp.groupCollapse && !grp.showSummaryOnHide) {
						hhdr = " style=\"display:none;\"";
					}
					str += "<tr"+hhdr+" role=\"row\" class=\"ui-widget-content jqfoot ui-row-"+$t.p.direction+"\">";
					var fdata = grp.summaryval[0][n],
					cm = $t.p.colModel,
					vv, grlen = grdata[n].length;
					for(var k=0; k<colspans;k++) {
						var tmpdata = "<td "+$t.formatCol(k,1,'')+">&#160;</td>",
						tplfld = "{0}";
						$.each(fdata,function(){
							if(this.nm == cm[k].name) {
								if(cm[k].summaryTpl)  {
									tplfld = cm[k].summaryTpl;
								}
								if(this.st == 'avg') {
									if(this.v && grlen > 0) {
										this.v = (this.v/grlen);
									}
								}
								try {
									vv = $t.formatter('', this.v, k, this);
								} catch (ef) {
									vv = this.v;
								}
								tmpdata= "<td "+$t.formatCol(k,1,'')+">"+$.jgrid.format(tplfld,vv)+ "</td>";
								return false;
							}
						});
						str += tmpdata;
					}
					str += "</tr>";
				}
			});
			$("#"+$t.p.id+" tbody:first").append(str);
			// free up memory
			str = null;
		});
	},
	groupingGroupBy : function (name, options, current) {
		return this.each(function(){
			var $t = this;
			if(typeof(name) == "string") {
				name = [name];
			}
			var grp = $t.p.groupingView;
			$t.p.grouping = true;
      // show previous hidden groups if they are hidden and weren't removed yet
      for(var i=0;i<grp.groupField.length;i++) {
        if(!grp.groupColumnShow[i] && grp.visibiltyOnNextGrouping[i]) {
          $($t).jqGrid('showCol',grp.groupField[i]);
        }
      }
      // set visibility status of current group columns on next grouping
      for(var i=0;i<name.length;i++) {
        grp.visibiltyOnNextGrouping[i] = $("#"+$t.p.id+"_"+name[i]).is(":visible");
      }
			$t.p.groupingView = $.extend($t.p.groupingView, options || {});
			grp.groupField = name;
			$($t).trigger("reloadGrid");
		});
	},
	groupingRemove : function (current) {
		return this.each(function(){
			var $t = this;
			if(typeof(current) == 'undefined') {
				current = true;
			}
			$t.p.grouping = false;
			if(current===true) {
        var grp = $t.p.groupingView;
        // show previous hidden groups if they are hidden and weren't removed yet
        for(var i=0;i<grp.groupField.length;i++) {
          if (!grp.groupColumnShow[i] && grp.visibiltyOnNextGrouping[i]) {
            $($t).jqGrid('showCol', grp.groupField);
          }
        }
				$("tr.jqgroup, tr.jqfoot","#"+$t.p.id+" tbody:first").remove();
				$("tr.jqgrow:hidden","#"+$t.p.id+" tbody:first").show();
			} else {
				$($t).trigger("reloadGrid");
			}
		});
	},
	groupingCalculations : {
		"sum" : function(v, field, rc) {
			return parseFloat(v||0) + parseFloat((rc[field]||0));
		},
		"min" : function(v, field, rc) {
			if(v==="") {
				return parseFloat(rc[field]||0);
			}
			return Math.min(parseFloat(v),parseFloat(rc[field]||0));
		},
		"max" : function(v, field, rc) {
			if(v==="") {
				return parseFloat(rc[field]||0);
			}
			return Math.max(parseFloat(v),parseFloat(rc[field]||0));
		},
		"count" : function(v, field, rc) {
			if(v==="") {v=0;}
			if(rc.hasOwnProperty(field)) {
				return v+1;
			} else {
				return 0;
			}
		},
		"avg" : function(v, field, rc) {
			// the same as sum, but at end we divide it
			return parseFloat(v||0) + parseFloat((rc[field]||0));
		}
	}
});
})(jQuery);
;(function($){
/*
 * jqGrid extension for constructing Grid Data from external file
 * Tony Tomov tony@trirand.com
 * http://trirand.com/blog/ 
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl-2.0.html
**/ 
    $.jgrid.extend({
        jqGridImport : function(o) {
            o = $.extend({
                imptype : "xml", // xml, json, xmlstring, jsonstring
                impstring: "",
                impurl: "",
                mtype: "GET",
                impData : {},
                xmlGrid :{
                    config : "roots>grid",
                    data: "roots>rows"
                },
                jsonGrid :{
                    config : "grid",
                    data: "data"
                },
                ajaxOptions :{}
            }, o || {});
            return this.each(function(){
                var $t = this;
                var XmlConvert = function (xml,o) {
                    var cnfg = $(o.xmlGrid.config,xml)[0];
                    var xmldata = $(o.xmlGrid.data,xml)[0], jstr, jstr1;
                    if(xmlJsonClass.xml2json && $.jgrid.parse) {
                        jstr = xmlJsonClass.xml2json(cnfg," ");
                        jstr = $.jgrid.parse(jstr);
                        for(var key in jstr) {
                            if(jstr.hasOwnProperty(key)) {
                                jstr1=jstr[key];
                            }
                        }
                        if(xmldata) {
                        // save the datatype
                            var svdatatype = jstr.grid.datatype;
                            jstr.grid.datatype = 'xmlstring';
                            jstr.grid.datastr = xml;
                            $($t).jqGrid( jstr1 ).jqGrid("setGridParam",{datatype:svdatatype});
                        } else {
                            $($t).jqGrid( jstr1 );
                        }
                        jstr = null;jstr1=null;
                    } else {
                        alert("xml2json or parse are not present");
                    }
                };
                var JsonConvert = function (jsonstr,o){
                    if (jsonstr && typeof jsonstr == 'string') {
                        var json = $.jgrid.parse(jsonstr);
                        var gprm = json[o.jsonGrid.config];
                        var jdata = json[o.jsonGrid.data];
                        if(jdata) {
                            var svdatatype = gprm.datatype;
                            gprm.datatype = 'jsonstring';
                            gprm.datastr = jdata;
                            $($t).jqGrid( gprm ).jqGrid("setGridParam",{datatype:svdatatype});
                        } else {
                            $($t).jqGrid( gprm );
                        }
                    }
                };
                switch (o.imptype){
                    case 'xml':
                        $.ajax($.extend({
                            url:o.impurl,
                            type:o.mtype,
                            data: o.impData,
                            dataType:"xml",
                            complete: function(xml,stat) {
                                if(stat == 'success') {
                                    XmlConvert(xml.responseXML,o);
                                    if($.isFunction(o.importComplete)) {
                                        o.importComplete(xml);
                                    }
                                }
                                xml=null;
                            }
                        }, o.ajaxOptions));
                        break;
                    case 'xmlstring' :
                        // we need to make just the conversion and use the same code as xml
                        if(o.impstring && typeof o.impstring == 'string') {
                            var xmld = $.jgrid.stringToDoc(o.impstring);
                            if(xmld) {
                                XmlConvert(xmld,o);
                                if($.isFunction(o.importComplete)) {
                                    o.importComplete(xmld);
                                }
                                o.impstring = null;
                            }
                            xmld = null;
                        }
                        break;
                    case 'json':
                        $.ajax($.extend({
                            url:o.impurl,
                            type:o.mtype,
                            data: o.impData,
                            dataType:"json",
                            complete: function(json,stat) {
                                if(stat == 'success') {
                                    JsonConvert(json.responseText,o );
                                    if($.isFunction(o.importComplete)) {
                                        o.importComplete(json);
                                    }
                                }
                                json=null;
                            }
                        }, o.ajaxOptions ));
                        break;
                    case 'jsonstring' :
                        if(o.impstring && typeof o.impstring == 'string') {
                            JsonConvert(o.impstring,o );
                            if($.isFunction(o.importComplete)) {
                                o.importComplete(o.impstring);
                            }
                            o.impstring = null;
                        }
                        break;
                }
            });
        },
        jqGridExport : function(o) {
            o = $.extend({
                exptype : "xmlstring",
                root: "grid",
                ident: "\t"
            }, o || {});
            var ret = null;
            this.each(function () {
                if(!this.grid) { return;}
                var gprm = $.extend({},$(this).jqGrid("getGridParam"));
                // we need to check for:
                // 1.multiselect, 2.subgrid  3. treegrid and remove the unneded columns from colNames
                if(gprm.rownumbers) {
                    gprm.colNames.splice(0,1);
                    gprm.colModel.splice(0,1);
                }
                if(gprm.multiselect) {
                    gprm.colNames.splice(0,1);
                    gprm.colModel.splice(0,1);
                }
                if(gprm.subGrid) {
                    gprm.colNames.splice(0,1);
                    gprm.colModel.splice(0,1);
                }
                gprm.knv = null;
                if(gprm.treeGrid) {
                    for (var key in gprm.treeReader) {
                        if(gprm.treeReader.hasOwnProperty(key)) {
                            gprm.colNames.splice(gprm.colNames.length-1);
                            gprm.colModel.splice(gprm.colModel.length-1);
                        }
                    }
                }
                switch (o.exptype) {
                    case 'xmlstring' :
                        ret = "<"+o.root+">"+xmlJsonClass.json2xml(gprm,o.ident)+"</"+o.root+">";
                        break;
                    case 'jsonstring' :
                        ret = "{"+ xmlJsonClass.toJson(gprm,o.root,o.ident,false)+"}";
                        if(gprm.postData.filters !== undefined) {
                            ret=ret.replace(/filters":"/,'filters":');
                            ret=ret.replace(/}]}"/,'}]}');
                        }
                        break;
                }
            });
            return ret;
        },
        excelExport : function(o) {
            o = $.extend({
                exptype : "remote",
                url : null,
                oper: "oper",
                tag: "excel",
                exportOptions : {}
            }, o || {});
            return this.each(function(){
                if(!this.grid) { return;}
                var url;
                if(o.exptype == "remote") {
                    var pdata = $.extend({},this.p.postData);
                    pdata[o.oper] = o.tag;
                    var params = jQuery.param(pdata);
                    if(o.url.indexOf("?") != -1) { url = o.url+"&"+params; }
                    else { url = o.url+"?"+params; }
                    window.location = url;
                }
            });
        }
    });
})(jQuery);;(function($){
/*
**
 * jqGrid addons using jQuery UI 
 * Author: Mark Williams
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl-2.0.html
 * depends on jQuery UI 
**/
if ($.browser.msie && $.browser.version==8) {
	$.expr[":"].hidden = function(elem) {
		return elem.offsetWidth === 0 || elem.offsetHeight === 0 ||
			elem.style.display == "none";
	};
}
// requiere load multiselect before grid
$.jgrid._multiselect = false;
if($.ui) {
	if ($.ui.multiselect ) {
		if($.ui.multiselect.prototype._setSelected) {
			var setSelected = $.ui.multiselect.prototype._setSelected;
		    $.ui.multiselect.prototype._setSelected = function(item,selected) {
		        var ret = setSelected.call(this,item,selected);
		        if (selected && this.selectedList) {
		            var elt = this.element;
				    this.selectedList.find('li').each(function() {
					    if ($(this).data('optionLink')) {
						    $(this).data('optionLink').remove().appendTo(elt);
					    }
				    });
		        }
		        return ret;
			};
		}
		if($.ui.multiselect.prototype.destroy) {
			$.ui.multiselect.prototype.destroy = function() {
				this.element.show();
				this.container.remove();
				if ($.Widget === undefined) {
					$.widget.prototype.destroy.apply(this, arguments);
				} else {
					$.Widget.prototype.destroy.apply(this, arguments);
	            }
			};
		}
		$.jgrid._multiselect = true;
	}
}
        
$.jgrid.extend({
	sortableColumns : function (tblrow)
	{
		return this.each(function (){
			var ts = this, tid= ts.p.id;
			function start() {ts.p.disableClick = true;}
			var sortable_opts = {
				"tolerance" : "pointer",
				"axis" : "x",
				"scrollSensitivity": "1",
				"items": '>th:not(:has(#jqgh_'+tid+'_cb'+',#jqgh_'+tid+'_rn'+',#jqgh_'+tid+'_subgrid),:hidden)',
				"placeholder": {
					element: function(item) {
						var el = $(document.createElement(item[0].nodeName))
						.addClass(item[0].className+" ui-sortable-placeholder ui-state-highlight")
						.removeClass("ui-sortable-helper")[0];
						return el;
					},
					update: function(self, p) {
						p.height(self.currentItem.innerHeight() - parseInt(self.currentItem.css('paddingTop')||0, 10) - parseInt(self.currentItem.css('paddingBottom')||0, 10));
						p.width(self.currentItem.innerWidth() - parseInt(self.currentItem.css('paddingLeft')||0, 10) - parseInt(self.currentItem.css('paddingRight')||0, 10));
					}
				},
				"update": function(event, ui) {
					var p = $(ui.item).parent(),
					th = $(">th", p),
					colModel = ts.p.colModel,
					cmMap = {}, tid= ts.p.id+"_";
					$.each(colModel, function(i) { cmMap[this.name]=i; });
					var permutation = [];
					th.each(function(i) {
						var id = $(">div", this).get(0).id.replace(/^jqgh_/, "").replace(tid,"");
							if (id in cmMap) {
								permutation.push(cmMap[id]);
							}
					});
	
					$(ts).jqGrid("remapColumns",permutation, true, true);
					if ($.isFunction(ts.p.sortable.update)) {
						ts.p.sortable.update(permutation);
					}
					setTimeout(function(){ts.p.disableClick=false;}, 50);
				}
			};
			if (ts.p.sortable.options) {
				$.extend(sortable_opts, ts.p.sortable.options);
			} else if ($.isFunction(ts.p.sortable)) {
				ts.p.sortable = { "update" : ts.p.sortable };
			}
			if (sortable_opts.start) {
				var s = sortable_opts.start;
				sortable_opts.start = function(e,ui) {
					start();
					s.call(this,e,ui);
				};
			} else {
				sortable_opts.start = start;
			}
			if (ts.p.sortable.exclude) {
				sortable_opts.items += ":not("+ts.p.sortable.exclude+")";
			}
			tblrow.sortable(sortable_opts).data("sortable").floating = true;
		});
	},
    columnChooser : function(opts) {
        var self = this;
		if($("#colchooser_"+self[0].p.id).length ) { return; }
        var selector = $('<div id="colchooser_'+self[0].p.id+'" style="position:relative;overflow:hidden"><div><select multiple="multiple"></select></div></div>');
        var select = $('select', selector);
		
		function insert(perm,i,v) {
			if(i>=0){
				var a = perm.slice();
				var b = a.splice(i,Math.max(perm.length-i,i));
				if(i>perm.length) { i = perm.length; }
				a[i] = v;
				return a.concat(b);
			}
		}
        opts = $.extend({
            "width" : 420,
            "height" : 240,
            "classname" : null,
            "done" : function(perm) { if (perm) { self.jqGrid("remapColumns", perm, true); } },
            /* msel is either the name of a ui widget class that
               extends a multiselect, or a function that supports
               creating a multiselect object (with no argument,
               or when passed an object), and destroying it (when
               passed the string "destroy"). */
            "msel" : "multiselect",
            /* "msel_opts" : {}, */

            /* dlog is either the name of a ui widget class that 
               behaves in a dialog-like way, or a function, that
               supports creating a dialog (when passed dlog_opts)
               or destroying a dialog (when passed the string
               "destroy")
               */
            "dlog" : "dialog",

            /* dlog_opts is either an option object to be passed 
               to "dlog", or (more likely) a function that creates
               the options object.
               The default produces a suitable options object for
               ui.dialog */
            "dlog_opts" : function(opts) {
                var buttons = {};
                buttons[opts.bSubmit] = function() {
                    opts.apply_perm();
                    opts.cleanup(false);
                };
                buttons[opts.bCancel] = function() {
                    opts.cleanup(true);
                };
                return {
                    "buttons": buttons,
                    "close": function() {
                        opts.cleanup(true);
                    },
					"modal" : opts.modal ? opts.modal : false,
					"resizable": opts.resizable ? opts.resizable : true,
                    "width": opts.width+20
                };
            },
            /* Function to get the permutation array, and pass it to the
               "done" function */
            "apply_perm" : function() {
                $('option',select).each(function(i) {
                    if (this.selected) {
                        self.jqGrid("showCol", colModel[this.value].name);
                    } else {
                        self.jqGrid("hideCol", colModel[this.value].name);
                    }
                });
                
                var perm = [];
				//fixedCols.slice(0);
                $('option[selected]',select).each(function() { perm.push(parseInt(this.value,10)); });
                $.each(perm, function() { delete colMap[colModel[parseInt(this,10)].name]; });
                $.each(colMap, function() {
					var ti = parseInt(this,10);
					perm = insert(perm,ti,ti);
				});
                if (opts.done) {
                    opts.done.call(self, perm);
                }
            },
            /* Function to cleanup the dialog, and select. Also calls the
               done function with no permutation (to indicate that the
               columnChooser was aborted */
            "cleanup" : function(calldone) {
                call(opts.dlog, selector, 'destroy');
                call(opts.msel, select, 'destroy');
                selector.remove();
                if (calldone && opts.done) {
                    opts.done.call(self);
                }
            },
			"msel_opts" : {}
        }, $.jgrid.col, opts || {});
		if($.ui) {
			if ($.ui.multiselect ) {
				if(opts.msel == "multiselect") {
					if(!$.jgrid._multiselect) {
						// should be in language file
						alert("Multiselect plugin loaded after jqGrid. Please load the plugin before the jqGrid!");
						return;
					}
					opts.msel_opts = $.extend($.ui.multiselect.defaults,opts.msel_opts);
				}
			}
		}
        if (opts.caption) {
            selector.attr("title", opts.caption);
        }
        if (opts.classname) {
            selector.addClass(opts.classname);
            select.addClass(opts.classname);
        }
        if (opts.width) {
            $(">div",selector).css({"width": opts.width,"margin":"0 auto"});
            select.css("width", opts.width);
        }
        if (opts.height) {
            $(">div",selector).css("height", opts.height);
            select.css("height", opts.height - 10);
        }
        var colModel = self.jqGrid("getGridParam", "colModel");
        var colNames = self.jqGrid("getGridParam", "colNames");
        var colMap = {}, fixedCols = [];

        select.empty();
        $.each(colModel, function(i) {
            colMap[this.name] = i;
            if (this.hidedlg) {
                if (!this.hidden) {
                    fixedCols.push(i);
                }
                return;
            }

            select.append("<option value='"+i+"' "+
                          (this.hidden?"":"selected='selected'")+">"+colNames[i]+"</option>");
        });
        function call(fn, obj) {
            if (!fn) { return; }
            if (typeof fn == 'string') {
                if ($.fn[fn]) {
                    $.fn[fn].apply(obj, $.makeArray(arguments).slice(2));
                }
            } else if ($.isFunction(fn)) {
                fn.apply(obj, $.makeArray(arguments).slice(2));
            }
        }

        var dopts = $.isFunction(opts.dlog_opts) ? opts.dlog_opts.call(self, opts) : opts.dlog_opts;
        call(opts.dlog, selector, dopts);
        var mopts = $.isFunction(opts.msel_opts) ? opts.msel_opts.call(self, opts) : opts.msel_opts;
        call(opts.msel, select, mopts);
    },
	sortableRows : function (opts) {
		// Can accept all sortable options and events
		return this.each(function(){
			var $t = this;
			if(!$t.grid) { return; }
			// Currently we disable a treeGrid sortable
			if($t.p.treeGrid) { return; }
			if($.fn.sortable) {
				opts = $.extend({
					"cursor":"move",
					"axis" : "y",
					"items": ".jqgrow"
					},
				opts || {});
				if(opts.start && $.isFunction(opts.start)) {
					opts._start_ = opts.start;
					delete opts.start;
				} else {opts._start_=false;}
				if(opts.update && $.isFunction(opts.update)) {
					opts._update_ = opts.update;
					delete opts.update;
				} else {opts._update_ = false;}
				opts.start = function(ev,ui) {
					$(ui.item).css("border-width","0px");
					$("td",ui.item).each(function(i){
						this.style.width = $t.grid.cols[i].style.width;
					});
					if($t.p.subGrid) {
						var subgid = $(ui.item).attr("id");
						try {
							$($t).jqGrid('collapseSubGridRow',subgid);
						} catch (e) {}
					}
					if(opts._start_) {
						opts._start_.apply(this,[ev,ui]);
					}
				};
				opts.update = function (ev,ui) {
					$(ui.item).css("border-width","");
					if($t.p.rownumbers === true) {
						$("td.jqgrid-rownum",$t.rows).each(function(i){
							$(this).html(i+1);
						});
					}
					if(opts._update_) {
						opts._update_.apply(this,[ev,ui]);
					}
				};
				$("tbody:first",$t).sortable(opts);
				$("tbody:first",$t).disableSelection();
			}
		});
	},
	gridDnD : function(opts) {
		return this.each(function(){
		var $t = this;
		if(!$t.grid) { return; }
		// Currently we disable a treeGrid drag and drop
		if($t.p.treeGrid) { return; }
		if(!$.fn.draggable || !$.fn.droppable) { return; }
		function updateDnD ()
		{
			var datadnd = $.data($t,"dnd");
		    $("tr.jqgrow:not(.ui-draggable)",$t).draggable($.isFunction(datadnd.drag) ? datadnd.drag.call($($t),datadnd) : datadnd.drag);
		}
		var appender = "<table id='jqgrid_dnd' class='ui-jqgrid-dnd'></table>";
		if($("#jqgrid_dnd").html() === null) {
			$('body').append(appender);
		}

		if(typeof opts == 'string' && opts == 'updateDnD' && $t.p.jqgdnd===true) {
			updateDnD();
			return;
		}
		opts = $.extend({
			"drag" : function (opts) {
				return $.extend({
					start : function (ev, ui) {
						// if we are in subgrid mode try to collapse the node
						if($t.p.subGrid) {
							var subgid = $(ui.helper).attr("id");
							try {
								$($t).jqGrid('collapseSubGridRow',subgid);
							} catch (e) {}
						}
						// hack
						// drag and drop does not insert tr in table, when the table has no rows
						// we try to insert new empty row on the target(s)
						for (var i=0;i<$.data($t,"dnd").connectWith.length;i++){
							if($($.data($t,"dnd").connectWith[i]).jqGrid('getGridParam','reccount') == "0" ){
								$($.data($t,"dnd").connectWith[i]).jqGrid('addRowData','jqg_empty_row',{});
							}
						}
						ui.helper.addClass("ui-state-highlight");
						$("td",ui.helper).each(function(i) {
							this.style.width = $t.grid.headers[i].width+"px";
						});
						if(opts.onstart && $.isFunction(opts.onstart) ) { opts.onstart.call($($t),ev,ui); }
					},
					stop :function(ev,ui) {
						if(ui.helper.dropped) {
							var ids = $(ui.helper).attr("id");
							$($t).jqGrid('delRowData',ids );
						}
						// if we have a empty row inserted from start event try to delete it
						for (var i=0;i<$.data($t,"dnd").connectWith.length;i++){
							$($.data($t,"dnd").connectWith[i]).jqGrid('delRowData','jqg_empty_row');
						}
						if(opts.onstop && $.isFunction(opts.onstop) ) { opts.onstop.call($($t),ev,ui); }
					}
				},opts.drag_opts || {});
			},
			"drop" : function (opts) {
				return $.extend({
					accept: function(d) {
						if (!$(d).hasClass('jqgrow')) { return d;}
						var tid = $(d).closest("table.ui-jqgrid-btable");
						if(tid.length > 0 && $.data(tid[0],"dnd") !== undefined) {
						    var cn = $.data(tid[0],"dnd").connectWith;
						    return $.inArray('#'+this.id,cn) != -1 ? true : false;
						}
						return d;
					},
					drop: function(ev, ui) {
						if (!$(ui.draggable).hasClass('jqgrow')) { return; }
						var accept = $(ui.draggable).attr("id");
						var getdata = ui.draggable.parent().parent().jqGrid('getRowData',accept);
						if(!opts.dropbyname) {
							var j =0, tmpdata = {}, dropname;
							var dropmodel = $("#"+this.id).jqGrid('getGridParam','colModel');
							try {
								for (var key in getdata) {
									if(getdata.hasOwnProperty(key) && dropmodel[j]) {
										dropname = dropmodel[j].name;
										tmpdata[dropname] = getdata[key];
									}
									j++;
								}
								getdata = tmpdata;
							} catch (e) {}
						}
						ui.helper.dropped = true;
						if(opts.beforedrop && $.isFunction(opts.beforedrop) ) {
							//parameters to this callback - event, element, data to be inserted, sender, reciever
							// should return object which will be inserted into the reciever
							var datatoinsert = opts.beforedrop.call(this,ev,ui,getdata,$('#'+$t.id),$(this));
							if (typeof datatoinsert != "undefined" && datatoinsert !== null && typeof datatoinsert == "object") { getdata = datatoinsert; }
						}
						if(ui.helper.dropped) {
							var grid;
							if(opts.autoid) {
								if($.isFunction(opts.autoid)) {
									grid = opts.autoid.call(this,getdata);
								} else {
									grid = Math.ceil(Math.random()*1000);
									grid = opts.autoidprefix+grid;
								}
							}
							// NULL is interpreted as undefined while null as object
							$("#"+this.id).jqGrid('addRowData',grid,getdata,opts.droppos);
						}
						if(opts.ondrop && $.isFunction(opts.ondrop) ) { opts.ondrop.call(this,ev,ui, getdata); }
					}}, opts.drop_opts || {});
			},
			"onstart" : null,
			"onstop" : null,
			"beforedrop": null,
			"ondrop" : null,
			"drop_opts" : {
				"activeClass": "ui-state-active",
				"hoverClass": "ui-state-hover"
			},
			"drag_opts" : {
				"revert": "invalid",
				"helper": "clone",
				"cursor": "move",
				"appendTo" : "#jqgrid_dnd",
				"zIndex": 5000
			},
			"dropbyname" : false,
			"droppos" : "first",
			"autoid" : true,
			"autoidprefix" : "dnd_"
		}, opts || {});
		
		if(!opts.connectWith) { return; }
		opts.connectWith = opts.connectWith.split(",");
		opts.connectWith = $.map(opts.connectWith,function(n){return $.trim(n);});
		$.data($t,"dnd",opts);
		
		if($t.p.reccount != "0" && !$t.p.jqgdnd) {
			updateDnD();
		}
		$t.p.jqgdnd = true;
		for (var i=0;i<opts.connectWith.length;i++){
			var cn =opts.connectWith[i];
			$(cn).droppable($.isFunction(opts.drop) ? opts.drop.call($($t),opts) : opts.drop);
		}
		});
	},
	gridResize : function(opts) {
		return this.each(function(){
			var $t = this;
			if(!$t.grid || !$.fn.resizable) { return; }
			opts = $.extend({}, opts || {});
			if(opts.alsoResize ) {
				opts._alsoResize_ = opts.alsoResize;
				delete opts.alsoResize;
			} else {
				opts._alsoResize_ = false;
			}
			if(opts.stop && $.isFunction(opts.stop)) {
				opts._stop_ = opts.stop;
				delete opts.stop;
			} else {
				opts._stop_ = false;
			}
			opts.stop = function (ev, ui) {
				$($t).jqGrid('setGridParam',{height:$("#gview_"+$t.p.id+" .ui-jqgrid-bdiv").height()});
				$($t).jqGrid('setGridWidth',ui.size.width,opts.shrinkToFit);
				if(opts._stop_) { opts._stop_.call($t,ev,ui); }
			};
			if(opts._alsoResize_) {
				var optstest = "{\'#gview_"+$t.p.id+" .ui-jqgrid-bdiv\':true,'" +opts._alsoResize_+"':true}";
				opts.alsoResize = eval('('+optstest+')'); // the only way that I found to do this
			} else {
				opts.alsoResize = $(".ui-jqgrid-bdiv","#gview_"+$t.p.id);
			}
			delete opts._alsoResize_;
			$("#gbox_"+$t.p.id).resizable(opts);
		});
	}
});
})(jQuery);
/*
 Transform a table to a jqGrid.
 Peter Romianowski <peter.romianowski@optivo.de> 
 If the first column of the table contains checkboxes or
 radiobuttons then the jqGrid is made selectable.
*/
// Addition - selector can be a class or id
function tableToGrid(selector, options) {
jQuery(selector).each(function() {
	if(this.grid) {return;} //Adedd from Tony Tomov
	// This is a small "hack" to make the width of the jqGrid 100%
	jQuery(this).width("99%");
	var w = jQuery(this).width();

	// Text whether we have single or multi select
	var inputCheckbox = jQuery('input[type=checkbox]:first', jQuery(this));
	var inputRadio = jQuery('input[type=radio]:first', jQuery(this));
	var selectMultiple = inputCheckbox.length > 0;
	var selectSingle = !selectMultiple && inputRadio.length > 0;
	var selectable = selectMultiple || selectSingle;
	//var inputName = inputCheckbox.attr("name") || inputRadio.attr("name");

	// Build up the columnModel and the data
	var colModel = [];
	var colNames = [];
	jQuery('th', jQuery(this)).each(function() {
		if (colModel.length === 0 && selectable) {
			colModel.push({
				name: '__selection__',
				index: '__selection__',
				width: 0,
				hidden: true
			});
			colNames.push('__selection__');
		} else {
			colModel.push({
				name: jQuery(this).attr("id") || jQuery.trim(jQuery.jgrid.stripHtml(jQuery(this).html())).split(' ').join('_'),
				index: jQuery(this).attr("id") || jQuery.trim(jQuery.jgrid.stripHtml(jQuery(this).html())).split(' ').join('_'),
				width: jQuery(this).width() || 150
			});
			colNames.push(jQuery(this).html());
		}
	});
	var data = [];
	var rowIds = [];
	var rowChecked = [];
	jQuery('tbody > tr', jQuery(this)).each(function() {
		var row = {};
		var rowPos = 0;
		jQuery('td', jQuery(this)).each(function() {
			if (rowPos === 0 && selectable) {
				var input = jQuery('input', jQuery(this));
				var rowId = input.attr("value");
				rowIds.push(rowId || data.length);
				if (input.attr("checked")) {
					rowChecked.push(rowId);
				}
				row[colModel[rowPos].name] = input.attr("value");
			} else {
				row[colModel[rowPos].name] = jQuery(this).html();
			}
			rowPos++;
		});
		if(rowPos >0) { data.push(row); }
	});

	// Clear the original HTML table
	jQuery(this).empty();

	// Mark it as jqGrid
	jQuery(this).addClass("scroll");

	jQuery(this).jqGrid(jQuery.extend({
		datatype: "local",
		width: w,
		colNames: colNames,
		colModel: colModel,
		multiselect: selectMultiple
		//inputName: inputName,
		//inputValueCol: imputName != null ? "__selection__" : null
	}, options || {}));

	// Add data
	var a;
	for (a = 0; a < data.length; a++) {
		var id = null;
		if (rowIds.length > 0) {
			id = rowIds[a];
			if (id && id.replace) {
				// We have to do this since the value of a checkbox
				// or radio button can be anything 
				id = encodeURIComponent(id).replace(/[.\-%]/g, "_");
			}
		}
		if (id === null) {
			id = a + 1;
		}
		jQuery(this).jqGrid("addRowData",id, data[a]);
	}

	// Set the selection
	for (a = 0; a < rowChecked.length; a++) {
		jQuery(this).jqGrid("setSelection",rowChecked[a]);
	}
});
};
