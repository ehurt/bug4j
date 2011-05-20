/*
 * Copyright 2011 Cedric Dandoy
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

package org.dandoy.bug4j.common;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StackAnalyzer {
    private static final Pattern STACK_PATTERN = Pattern.compile("\tat ([^()]*)\\((.*)\\)");
    private static final String[] STD_PACKAGES = {
            "java.", "javax.", "sun."
    };
    private List<String> _applicationPackages;

    public StackAnalyzer() {
    }

    public void setApplicationPackages(List<String> applicationPackages) {
        _applicationPackages = applicationPackages;
    }

    public String analyze(List<String> stackLines) {
        final Iterator<String> iterator = stackLines.iterator();
        if (iterator.hasNext()) {
            final String messageLine = iterator.next();
            final String exceptionClass = getExceptionClass(messageLine);
            final String ret = analyze(exceptionClass, iterator);
            return ret;
        }
        return null;
    }

    protected String analyze(String exceptionClass, Iterator<String> iterator) {
        String ret = null;
        boolean findBetter = true;
        while (iterator.hasNext()) {
            final String stackLine = iterator.next();
            if (stackLine.startsWith("Caused by: ")) {
                final String substring = stackLine.substring("Caused by: ".length());
                exceptionClass = getExceptionClass(substring);
                findBetter = true;
            } else {
                if (findBetter) {
                    final Matcher matcher = STACK_PATTERN.matcher(stackLine);
                    if (matcher.matches()) {
                        final String methodCall = matcher.group(1);
                        if (isInApplicationPackages(methodCall)) {
                            final String location = matcher.group(2);
                            final int pos = location.indexOf(':');
                            if (pos > 0) {
                                final String shortExceptionClassName;
                                if (exceptionClass != null) {
                                    shortExceptionClassName = getShortExceptionClassName(exceptionClass);
                                } else {
                                    shortExceptionClassName = "Exception";
                                }
                                ret = shortExceptionClassName + " at " + location;
                                findBetter = false;
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    static String getShortExceptionClassName(String exceptionClass) {
        final int pos = exceptionClass.lastIndexOf('.');
        return exceptionClass.substring(pos + 1);
    }

    private boolean isInApplicationPackages(String methodCall) {
        if (_applicationPackages != null && !_applicationPackages.isEmpty()) {
            for (String applicationPackage : _applicationPackages) {
                if (methodCall.startsWith(applicationPackage)) {
                    return true;
                }
            }
            return false;
        } else {
            for (String stdPackage : STD_PACKAGES) {
                if (methodCall.startsWith(stdPackage)) {
                    return false;
                }
            }
            return true;
        }
    }

    static String getExceptionClass(String messageLine) {
        final int pos = messageLine.indexOf(':');
        if (pos < 0) {
            throw new IllegalStateException("Invalid first line in a stack trace: \"" + messageLine + "\"");
        }
        final String ret = messageLine.substring(0, pos);
        return ret;
    }
}