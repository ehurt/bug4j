/*
 * Copyright 2012 Cedric Dandoy
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.bug4j

import groovy.io.FileType
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.filefilter.WildcardFileFilter
import org.codehaus.groovy.runtime.InvokerHelper

import java.util.jar.JarFile

class ExtensionService {
    private final Map<String, String> _actions = [:]
    private final Map<String, List<String>> _events = [:]
    private final Binding _binding = new Binding()
    private GroovyScriptEngine _groovyScriptEngine

    public void init(File extensionRoot) {
        if (extensionRoot.isDirectory()) {
            final File[] jarFiles = extensionRoot.listFiles((FileFilter) new WildcardFileFilter("*.jar"))
            loadJarExtensions(jarFiles)
            loadDirectoryExtensions(extensionRoot)
            println "Actions: ${_actions}"
            _events.each {println "Event ${it.key}=${it.value}"}

            def fileNames = jarFiles*.getAbsoluteFile()
            fileNames += extensionRoot
            final URL[] urls = fileNames*.toURI()*.toURL()
            _groovyScriptEngine = new GroovyScriptEngine(urls)
        }
    }

    public void whenBug(Bug bug) {
        whenEvent('events/whenBug', [
                bug: bug,
        ])
    }

    public void whenHit(Hit hit) {
        whenEvent('events/whenHit', [
                hit: hit,
        ])
    }

    public void reportBug(Bug bug) {
        whenAction('actions/reportBug', [
                bug: bug,
        ])
    }

    public String getBugURL(Bug bug) {
        whenAction('action/getBug', [
                bug: bug,
        ])
    }

    private void whenEvent(String eventName, Map<String, Object> params) {
        params.each {
            _binding.setVariable(it.key, it.value)
        }
        final List<String> scriptNames = _events.get(eventName)
        scriptNames.each {
            runScriptOrClass(it)
        }
    }

    private Object whenAction(String actionName, Map<String, Object> params) {
        Object ret = null
        final scriptName = _actions.get(actionName)
        if (scriptName) {
            params.each {
                _binding.setVariable(it.key, it.value)
            }
            ret = runScriptOrClass(scriptName)
        }
        return ret
    }

    private def runScriptOrClass(String name) {
        if (_groovyScriptEngine) { // will be null if the extension directory did not exist
            try {
                if (name.endsWith('.groovy')) {
                    return _groovyScriptEngine.run(name, _binding)
                } else {
                    final classLoader = _groovyScriptEngine.getGroovyClassLoader()
                    final Class clazz = classLoader.loadClass(name)
                    final script = InvokerHelper.createScript(clazz, _binding)
                    return script.run()
                }
            } catch (Exception e) {
                LOGGER.error("Exception while running " + name, e)
            }
        }
    }

    private void loadDirectoryExtensions(File extensionRoot) {
        final baseRootName = extensionRoot.getPath()
        extensionRoot.eachFileRecurse(FileType.FILES) {
            final path = it.getPath()
            path = path.substring(baseRootName.length())
            registerExtention(path)
        }
    }

    private List<String> loadJarExtensions(File[] jarFiles) {
        final List<String> ret = []
        for (File jarFile : jarFiles) {
            loadJarExtensions(jarFile)
        }
        return ret
    }

    private List<String> loadJarExtensions(File file) {
        final jarFile = new JarFile(file)
        final entries = jarFile.entries()
        while (entries.hasMoreElements()) {
            final entry = entries.nextElement()
            if (!entry.isDirectory()) {
                final name = entry.getName()
                registerExtention(name)
            }
        }
        return []
    }

    /**
     * @param name actions/reportHit.groovy or actions/reportHit.class
     */
    @SuppressWarnings("GroovyEmptyStatementBody")
    private void registerExtention(String name) {
        if (name.endsWith('.groovy') || name.endsWith('.class')) {
            name = name.replace('\\', '/')
            if (name.startsWith('/')) {
                name = name.substring(1)
            }
            if (name.startsWith('events/')) {
                registerEvent(name)
            } else if (name.startsWith('actions/')) {
                registerAction(name)
            }
        }
    }

    /**
     * @param name events/whenHit/printHit.groovy or events/whenHit/printHit.class
     */
    private void registerEvent(String name) {
        final eventName = FilenameUtils.getPathNoEndSeparator(name)
        final events = _events.get(eventName)
        if (!events) {
            events = []
            _events.put(eventName, events)
        }
        final registeredName = toScriptOrClassName(name)
        events.add(registeredName)
    }

    /**
     * @param name actions/reportBug.groovy or actions/reportBug.class
     */
    private void registerAction(String name) {
        final eventName = FilenameUtils.removeExtension(name)
        final scriptOrClassName = toScriptOrClassName(name)
        _actions.put(eventName, scriptOrClassName)
    }

    private static String toScriptOrClassName(String fileName) {
        if (fileName.endsWith('.groovy')) {
            return fileName
        } else {
            fileName = FilenameUtils.removeExtension(fileName)
            fileName = fileName.replace('/', '.')
            return fileName
        }
    }
}
