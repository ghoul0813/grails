/* Copyright 2006-2007 Graeme Rocher
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
package grails.config


/**
* A ConfigObject at a simple level is a Map that creates configuration entries (other ConfigObjects) when referencing them.
* This means that navigating to foo.bar.stuff will not return null but nested ConfigObjects which are of course empty maps
* The Groovy truth can be used to check for the existance of "real" entries.
*
* @author Graeme Rocher
* @since 0.6
*/
class ConfigObject extends LinkedHashMap implements Writable { 

    // would be better to have these availabe as constants from Groovy, but couldn't find
    static final KEYWORDS = ['class', 'extends', 'implements', 'package','return','def',
                             'try','finally','this','new','catch','switch','case','default','while','if',
                             'else','elseif','private','protected','final','for','in','byte','short','break',
                             'instanceof','synchronized','const','float','null','throws','do','super','with',
                             'threadsafe','transient','native','interface','any','double','volatile','as',
                             'assert','goto','enum','int','boolean','char','false','true','static','abstract',
                             'continue','import','void','long']

    static final TAB_CHARACTER = '\t'

    URL file

    ConfigObject(URL file) {
        this.file = file
    }

    ConfigObject() {}

    URL getConfigFile() {
        return this.file    
    }         

    /**
	 * Writes this config object into a String serialized representation which can later be parsed back using the parse()
	 * method
	 *
     * @see groovy.lang.Writable#writeTo(java.io.Writer)
     * @see #parse(URL)
     */ 
	Writer writeTo(Writer outArg) {
        def out
        try {
            out = new BufferedWriter(outArg)
            writeConfig("",this, out, 0, false)
        } finally {
            out.flush()
        }

		return outArg
	}
                  

    /**
     * Overrides the default getProperty implementation to create nested ConfigObject instances on demand
     * for non-existant keys
     */
    def getProperty(String name) {
        def prop = get(name)
        if(prop == null) prop = new ConfigObject(this.file)
        put(name, prop)
        return prop
    }

    /**
     * A ConfigObject is a tree structure consisting of nested maps. This flattens the maps into
     * a single level structure like a properties file
     */
    Map flatten() {
        return flatten(null)
    }
    /**
     * Flattens this ConfigObject populating the results into the target Map
     *
     * @see ConfigObject#flatten()
     */
    Map flatten(Map target) {
        if(target == null)target = new ConfigObject()
        populate("", target, this)
        target
    }

    /**
     * Merges the given map with this ConfigObject overriding any matching configuration entries in this ConfigObject
     *
     * @param other The ConfigObject to merge with
     * @return The result of the merge
     */
    Map merge(ConfigObject other) {
        return merge(this,other)
    }


    /**
     * Converts this ConfigObject into a the java.util.Properties format, flattening the tree structure beforehand
     * @return A java.util.Properties instance
     */
    Properties toProperties() {
        def props = new Properties()
        flatten(props)
        props = convertValuesToString(props)
        return props
    }

    /**
     * Converts this ConfigObject ino the java.util.Properties format, flatten the tree and prefixing all entries with the given prefix
     * @param prefix The prefix to append before property entries
     * @return A java.util.Properties instance
     */
    Properties toProperties(String prefix) {
        def props = new Properties()
        populate("${prefix}.", props, this)
        props = convertValuesToString(props)
        return props
    }

    private merge(Map config, Map other) {
        for(entry in other) {

            def configEntry = config[entry.key]
            if(configEntry == null) {
                config[entry.key] = entry.value
                continue
            }
            else {
                if(configEntry instanceof Map && configEntry.size() > 0 && entry.value instanceof Map) {
                    // recurse
                    merge(configEntry, entry.value)
               }
               else {
                    config[entry.key] = entry.value
               }
            }
        }
        return config

    }

    private writeConfig(String prefix,ConfigObject map, out, Integer tab, boolean apply) {
        def space = apply ? TAB_CHARACTER*tab : ''
        for(key in map.keySet()) {
            def value = map.get(key)


			if(value instanceof ConfigObject) {
                def dotsInKeys = value.find { entry -> entry.key.indexOf('.') > -1 }
                def configSize = value.size()
                def firstKey = value.keySet().iterator().next()
                def firstValue = value.values().iterator().next()
                def firstSize
                if(firstValue instanceof ConfigObject){
                    firstSize = firstValue.size()
                }
                else { firstSize = 1 }
				if(configSize == 1|| dotsInKeys )  {

                    if(firstSize == 1 && firstValue instanceof ConfigObject) {
                        key = KEYWORDS.contains(key) ? key.inspect() : key
                        def writePrefix = "${prefix}${key}.${firstKey}."
                        writeConfig(writePrefix, firstValue, out, tab, true)
                    }
                    else if(!dotsInKeys && firstValue instanceof ConfigObject) {
                        writeNode(key, space, tab,value, out)
                    }  else {
                        for(j in value.keySet()) {
                            def v2 = value.get(j)
                            def k2 = j.indexOf('.') > -1 ? j.inspect() : j
                            if(v2 instanceof ConfigObject) {
                                key = KEYWORDS.contains(key) ? key.inspect() : key
                                writeConfig("${prefix}${key}", v2, out, tab, false)
                            }
                            else {
                                writeValue("${key}.${k2}", space, prefix, v2, out)
                            }
                        }
                    }

				}
				else {
                    writeNode(key, space,tab, value, out)
				}
			}
			else {

                writeValue(key, space, prefix, value, out)
			}
		}
	}

    private writeValue(key, space, prefix, value, out) {
        key = key.indexOf('.') > -1 ? key.inspect() : key
        boolean isKeyword = KEYWORDS.contains(key)
        key = isKeyword ? key.inspect() : key

        if(!prefix && isKeyword) prefix = "this."
        out << "${space}${prefix}$key=${value.inspect()}"
        out.newLine()
    }

    private writeNode(key, space, tab, value, out) {
        key = KEYWORDS.contains(key) ? key.inspect() : key
        out << "${space}$key {"
        out.newLine()
        writeConfig("",value, out, tab+1, true)
        def last = "${space}}"
        out << last
        out.newLine()
    }

    private convertValuesToString(props) {
        def newProps = [:]
        for(e in props) {
            newProps[e.key] = e.value?.toString()
        }
        return newProps
    }

    private populate(suffix, config, map) {
        for(key in map.keySet()) {
            def value = map.get(key)
            if(value instanceof Map) {
                populate(suffix+"${key}.", config, value)
            }
            else {
                config[suffix+key] = value
            }
        }
    }
}