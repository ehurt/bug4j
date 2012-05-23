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
        try {
            final String appCode = params.a
            final String appVersion = params.v
            final Long buildDateMillis = params.d as Long
            final String devBuild = params.dev
            final Integer buildNumber = params.n as Integer
            final String remoteAddr = request.remoteAddr

            try {
                ClientSession clientSession = bugService.createSession(appCode, appVersion, buildDateMillis, devBuild, buildNumber, remoteAddr)
                render(text: clientSession.id, contentType: 'text/plain')
                log.info "Created a session"
            } catch (IllegalArgumentException e) {
                log.error("Failed to create a session", e)
                response.sendError(400, e.getMessage())
            } catch (IllegalStateException e) {
                log.error("Failed to create a session", e)
                response.sendError(500, e.getMessage())
            }
        } catch (Exception e) {
            log.error("Failed to create a session", e)
        }
    }

    def check() {
        try {
            final String sessionId = params.e
            final String appCode = params.a
            final String message = params.m
            final String user = params.u
            final String hash = params.h
            final String remoteAddr = request.remoteAddr

            try {
                if (bugService.isNewBug(sessionId, appCode, message, user, remoteAddr, hash)) {
                    render(text: 'New', contentType: 'text/plain')
                } else {
                    log.info "Reported hit"
                    render(text: 'Old', contentType: 'text/plain')
                }
            } catch (IllegalArgumentException e) {
                log.error("Failed to check", e)
                response.sendError(400, e.getMessage())
            } catch (IllegalStateException e) {
                log.error("Failed to check", e)
                response.sendError(500, e.getMessage())
            }
        } catch (Exception e) {
            log.error("Failed to check", e)
        }
    }

    def bug() {
        try {
            final String sessionId = params.e
            final String appCode = params.a
            final String message = params.m
            final long dateReported = System.currentTimeMillis()
            final String user = params.u
            final String stackText = params.s
            final String remoteAddr = request.remoteAddr

            def bugId = bugService.reportBug(Long.parseLong(sessionId), appCode, message, dateReported, user, remoteAddr, stackText)
            log.info "Reported bug ${bugId}"
            render(text: bugId, contentType: 'text/plain')
        } catch (Throwable e) {
            log.error("Failed to create a session", e)
        }
    }
}
