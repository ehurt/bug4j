/*
 * Copyright 2013 Cedric Dandoy
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
package org.bug4j.server

import org.bug4j.ClientSession

class IncomingController {

    def bugService

    def createSession() {
        final String remoteAddr = request.remoteAddr
        try {
            final String appCode = params.a
            final String appVersion = params.v
            final Long buildDateMillis = params.d as Long
            final String devBuild = params.dev
            final Integer buildNumber = params.n as Integer

            try {
                ClientSession clientSession = bugService.createSession(appCode, appVersion, buildDateMillis, devBuild, buildNumber, remoteAddr)
                render(text: clientSession.id, contentType: 'text/plain')
                log.info "Created session ${clientSession.id} for ${remoteAddr}"
            } catch (MessageException e) {
                log.info(e.getMessage())
                response.sendError(e.getErrorCode(), e.getMessage())
            } catch (IllegalArgumentException e) {
                log.error("Failed to create a session for ${remoteAddr}", e)
                response.sendError(400, e.getMessage())
            } catch (IllegalStateException e) {
                log.error("Failed to create a session for ${remoteAddr}", e)
                response.sendError(500, e.getMessage())
            }
        } catch (Exception e) {
            log.error("Failed to create a session for ${remoteAddr}", e)
        }
    }

    def check() {
        final String remoteAddr = request.remoteAddr
        try {
            final String sessionId = params.e
            final String appCode = params.a
            final String message = params.m
            final String user = params.u
            final String hash = params.h

            try {
                if (bugService.isNewBug(sessionId, appCode, message, user, remoteAddr, hash)) {
                    render(text: 'New', contentType: 'text/plain')
                } else {
                    log.info "Reported hit"
                    render(text: 'Old', contentType: 'text/plain')
                }
            } catch (IllegalArgumentException e) {
                log.error("Failed to check for ${remoteAddr}", e)
                response.sendError(400, e.getMessage())
            } catch (IllegalStateException e) {
                log.error("Failed to check  for ${remoteAddr}", e)
                response.sendError(500, e.getMessage())
            }
        } catch (Exception e) {
            log.error("Failed to check for ${remoteAddr}", e)
        }
    }

    def bug() {
        final String remoteAddr = request.remoteAddr

        try {
            final String sessionIdValue = params.e
            final String appCode = params.a
            final String message = params.m
            final long dateReported = System.currentTimeMillis()
            final String user = params.u
            final String stackText = params.s
            if (sessionIdValue) {
                final long sessionId = Long.parseLong(sessionIdValue)

                def bugId = bugService.reportBug(sessionId, appCode, message, dateReported, user, remoteAddr, stackText)
                log.info "Reported bug ${bugId} for ${remoteAddr}"
                render(text: bugId, contentType: 'text/plain')
            } else {
                log.error("Missing session ID in IncomingController.bug for ${remoteAddr}")
            }
        } catch (Throwable e) {
            log.error("Failed to report a bug for ${remoteAddr}", e)
        }
    }
}
