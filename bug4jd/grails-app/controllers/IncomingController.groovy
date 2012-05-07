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



import org.bug4j.ClientSession

class IncomingController {

    def bugService

    def createSession() {
        final String applicationCode = params.a
        final String applicationVersion = params.v
        final Long dataMillis = params.d as Long
        final String devBuild = params.dev
        final Integer buildNumber = params.n as Integer
        final String remoteAddr = request.remoteAddr

        try {
            ClientSession clientSession = bugService.createSession(applicationCode, applicationVersion, dataMillis, devBuild, buildNumber, remoteAddr)
            render(text: clientSession.id, contentType: 'text/plain')
        } catch (IllegalArgumentException e) {
            response.sendError(400, e.getMessage())
        } catch (IllegalStateException e) {
            response.sendError(500, e.getMessage())
        }
    }

    def check() {
        final String sessionId = params.e
        final String applicationCode = params.a
        final String message = params.m
        final String user = params.u
        final String hash = params.h

        try {
            if (bugService.isNewBug(sessionId, applicationCode, message, user, hash)) {
                render(text: 'New', contentType: 'text/plain')
            } else {
                render(text: 'Old', contentType: 'text/plain')
            }
        } catch (IllegalArgumentException e) {
            response.sendError(400, e.getMessage())
        } catch (IllegalStateException e) {
            response.sendError(500, e.getMessage())
        }
    }

    def bug() {
        final String sessionId = params.e
        final String applicationCode = params.a
        final String message = params.m
        final long dateReported = System.currentTimeMillis()
        final String user = params.u
        final String stackText = params.s
        bugService.reportBug(Long.parseLong(sessionId), applicationCode, message, dateReported, user, stackText)
    }
}
