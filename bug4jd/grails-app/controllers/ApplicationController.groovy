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

import org.bug4j.*

class ApplicationController {

    def index() {
        final apps = App.list(params)
        final total = App.count
        return [
                apps: apps,
                total: total
        ]
    }

    def create() {
        final App appInstance = new App(params)
        render(view: 'edit', model: [appInstance: appInstance])
    }

    def edit() {
        final id = params.id
        final App appInstance = App.get(id)
        return [
                appInstance: appInstance
        ]
    }

    def save() {
        final App appInstance
        final id = params.id
        if (id) {
            appInstance = App.get(id)
            appInstance.properties = params
            appInstance.appPackages*.delete()
            appInstance.appPackages.clear()
        } else {
            appInstance = new App(params)
        }
        params.appPackageValue?.each {
            if (it) {
                final appPackages = new AppPackages(packageName: it)
                appPackages.setApp(appInstance)
                appInstance.addToAppPackages(appPackages)
            }
        }
        if (!appInstance.save(flush: true)) {
            render(view: "edit", model: [appInstance: appInstance])
            return
        }

        flash.message = id ?
                        message(code: 'default.updated.message', args: [message(code: 'app.label', default: 'Application'), appInstance.label]) :
                        message(code: 'default.created.message', args: [message(code: 'app.label', default: 'Application'), appInstance.label])
        redirect(action: "index", id: appInstance.id)
    }

    def delete() {
        final id = params.id
        final appInstance = App.get(id)
        ClientSession.findAllByApp(appInstance).each {ClientSession clientSession ->
            Hit.findAllByClientSession(clientSession)*.delete()
            clientSession.delete()
        }
        Bug.findAllByApp(appInstance).each {Bug bug ->
            bug.strains*.delete()
            bug.delete()
        }
        appInstance.delete()
        flash.message = message(code: 'default.deleted.message', args: [message(code: 'app.label', default: 'Application'), appInstance.label])
        redirect(action: 'index')
    }
}
