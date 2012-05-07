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



import org.bug4j.Application
import org.bug4j.Bug
import org.bug4j.ClientSession
import org.bug4j.Hit

class ApplicationController {

    def index() {
        final applications = Application.list(params)
        final total = Application.count
        return [
                applications: applications,
                total: total
        ]
    }

    def create() {
        final Application applicationInstance = new Application(params)
        render(view: 'edit', model: [applicationInstance: applicationInstance])
    }

    def edit() {
        final id = params.id
        final Application applicationInstance = Application.get(id)
        return [
                applicationInstance: applicationInstance
        ]
    }

    def save() {
        final Application applicationInstance
        final id = params.id
        if (id) {
            applicationInstance = Application.get(id)
            applicationInstance.properties = params
        } else {
            applicationInstance = new Application(params)
        }
        if (!applicationInstance.save(flush: true)) {
            render(view: "edit", model: [applicationInstance: applicationInstance])
            return
        }

        flash.message = id ?
                        message(code: 'default.updated.message', args: [message(code: 'application.label', default: 'Application'), applicationInstance.label]) :
                        message(code: 'default.created.message', args: [message(code: 'application.label', default: 'Application'), applicationInstance.label])
        redirect(action: "index", id: applicationInstance.id)
    }

    def delete() {
        final id = params.id
        final applicationInstance = Application.get(id)
        ClientSession.findAllByApplication(applicationInstance).each {ClientSession clientSession ->
            Hit.findAllByClientSession(clientSession)*.delete()
            clientSession.delete()
        }
        Bug.findAllByApplication(applicationInstance).each {Bug bug ->
            bug.strains*.delete()
            bug.delete()
        }
        applicationInstance.delete()
        flash.message = message(code: 'default.deleted.message', args: [message(code: 'application.label', default: 'Application'), applicationInstance.label])
        redirect(action: 'index')
    }
}
