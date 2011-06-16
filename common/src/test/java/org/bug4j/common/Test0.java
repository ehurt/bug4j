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

package org.bug4j.common;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test0 {
    private static final String TEXT = "" +
            "2011-06-15 14:10:15,717 WARN  [org.hibernate.util.JDBCExceptionReporter] (http-127.0.0.1-8181-5) SQL Error: -1, SQLState: 23505\n" +
            "2011-06-15 14:10:15,718 ERROR [org.hibernate.util.JDBCExceptionReporter] (http-127.0.0.1-8181-5) The statement was aborted because it would have caused a duplicate key value in a unique or primary key constraint or unique index identified by 'SQL110615140909511' defined on 'EDIS_PROJECT'.\n" +
            "2011-06-15 14:10:15,773 ERROR [com.hp.im.server.edm.rpc.ProjectServiceImpl] (http-127.0.0.1-8181-5) javax.persistence.PersistenceException: org.hibernate.exception.ConstraintViolationException: could not insert: [com.hp.im.server.edm.dto.ProjectDTO]: javax.ejb.EJBException: javax.persistence.PersistenceException: org.hibernate.exception.ConstraintViolationException: could not insert: [com.hp.im.server.edm.dto.ProjectDTO]\n" +
            "\tat org.jboss.ejb3.tx2.impl.CMTTxInterceptor.handleExceptionInOurTx(CMTTxInterceptor.java:183) [:0.0.1]\n" +
            "\tat org.jboss.ejb3.tx2.impl.CMTTxInterceptor.invokeInOurTx(CMTTxInterceptor.java:251) [:0.0.1]\n" +
            "\tat org.jboss.ejb3.tx2.impl.CMTTxInterceptor.required(CMTTxInterceptor.java:349) [:0.0.1]\n" +
            "\tat org.jboss.ejb3.tx2.impl.CMTTxInterceptor.invoke(CMTTxInterceptor.java:209) [:0.0.1]\n" +
            "\tat org.jboss.ejb3.tx2.aop.CMTTxInterceptorWrapper.invoke(CMTTxInterceptorWrapper.java:52) [:0.0.1]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.aspects.tx.TxPropagationInterceptor.invoke(TxPropagationInterceptor.java:76) [:1.0.0.GA]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.tx.NullInterceptor.invoke(NullInterceptor.java:42) [:1.0.3]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.security.Ejb3AuthenticationInterceptorv2.invoke(Ejb3AuthenticationInterceptorv2.java:182) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.ENCPropagationInterceptor.invoke(ENCPropagationInterceptor.java:41) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.BlockContainerShutdownInterceptor.invoke(BlockContainerShutdownInterceptor.java:67) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.core.context.CurrentInvocationContextInterceptor.invoke(CurrentInvocationContextInterceptor.java:47) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.aspects.currentinvocation.CurrentInvocationInterceptor.invoke(CurrentInvocationInterceptor.java:67) [:1.0.1]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.interceptor.EJB3TCCLInterceptor.invoke(EJB3TCCLInterceptor.java:86) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.stateless.StatelessContainer.dynamicInvoke(StatelessContainer.java:392) [:1.7.17]\n" +
            "\tat org.jboss.ejb3.remoting.IsLocalInterceptor.invokeLocal(IsLocalInterceptor.java:88) [:1.7.17]\n" +
            "\tat org.jboss.ejb3.remoting.IsLocalInterceptor.invoke(IsLocalInterceptor.java:75) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.async.impl.interceptor.AsynchronousClientInterceptor.invoke(AsynchronousClientInterceptor.java:143) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.aspects.remoting.PojiProxy.invoke(PojiProxy.java:62) [:1.0.1.GA]\n" +
            "\tat $Proxy195.invoke(Unknown Source)\tat org.jboss.ejb3.proxy.impl.handler.session.SessionProxyInvocationHandlerBase.invoke(SessionProxyInvocationHandlerBase.java:185) [:1.0.11]\n" +
            "\tat $Proxy242.createProject(Unknown Source)\tat com.hp.im.server.edm.rpc.ProjectServiceImpl.createProject(ProjectServiceImpl.java:79) [:]\n" +
            "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) [:1.6.0_23]\n" +
            "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39) [:1.6.0_23]\n" +
            "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25) [:1.6.0_23]\n" +
            "\tat java.lang.reflect.Method.invoke(Method.java:597) [:1.6.0_23]\n" +
            "\tat com.google.gwt.user.server.rpc.RPC.invokeAndEncodeResponse(RPC.java:562) [:]\n" +
            "\tat com.google.gwt.user.server.rpc.RemoteServiceServlet.processCall(RemoteServiceServlet.java:188) [:]\n" +
            "\tat com.google.gwt.user.server.rpc.RemoteServiceServlet.processPost(RemoteServiceServlet.java:224) [:]\n" +
            "\tat com.google.gwt.user.server.rpc.AbstractRemoteServiceServlet.doPost(AbstractRemoteServiceServlet.java:62) [:]\n" +
            "\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:754) [:1.0.0.Final]\n" +
            "\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:847) [:1.0.0.Final]\n" +
            "\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:324) [:6.0.0.Final]\n" +
            "\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:242) [:6.0.0.Final]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:368) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.access.intercept.FilterSecurityInterceptor.invoke(FilterSecurityInterceptor.java:109) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.access.intercept.FilterSecurityInterceptor.doFilter(FilterSecurityInterceptor.java:83) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.access.ExceptionTranslationFilter.doFilter(ExceptionTranslationFilter.java:97) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.session.SessionManagementFilter.doFilter(SessionManagementFilter.java:100) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.authentication.AnonymousAuthenticationFilter.doFilter(AnonymousAuthenticationFilter.java:78) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter.doFilter(SecurityContextHolderAwareRequestFilter.java:54) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.savedrequest.RequestCacheAwareFilter.doFilter(RequestCacheAwareFilter.java:35) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat com.hp.im.server.edm.servlet.filter.GWTCacheFilter.doFilter(GWTCacheFilter.java:39) [:]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter.doFilter(AbstractAuthenticationProcessingFilter.java:187) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.authentication.logout.LogoutFilter.doFilter(LogoutFilter.java:105) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.context.SecurityContextPersistenceFilter.doFilter(SecurityContextPersistenceFilter.java:79) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.session.ConcurrentSessionFilter.doFilter(ConcurrentSessionFilter.java:109) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.access.channel.ChannelProcessingFilter.doFilter(ChannelProcessingFilter.java:109) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy.doFilter(FilterChainProxy.java:169) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.web.filter.DelegatingFilterProxy.invokeDelegate(DelegatingFilterProxy.java:237) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.web.filter.DelegatingFilterProxy.doFilter(DelegatingFilterProxy.java:167) [:3.0.5.RELEASE]\n" +
            "\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:274) [:6.0.0.Final]\n" +
            "\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:242) [:6.0.0.Final]\n" +
            "\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:275) [:6.0.0.Final]\n" +
            "\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:191) [:6.0.0.Final]\n" +
            "\tat org.jboss.web.tomcat.security.SecurityAssociationValve.invoke(SecurityAssociationValve.java:181) [:6.0.0.Final]\n" +
            "\tat org.jboss.modcluster.catalina.CatalinaContext$RequestListenerValve.event(CatalinaContext.java:285) [:1.1.0.Final]\n" +
            "\tat org.jboss.modcluster.catalina.CatalinaContext$RequestListenerValve.invoke(CatalinaContext.java:261) [:1.1.0.Final]\n" +
            "\tat org.jboss.web.tomcat.security.JaccContextValve.invoke(JaccContextValve.java:88) [:6.0.0.Final]\n" +
            "\tat org.jboss.web.tomcat.security.SecurityContextEstablishmentValve.invoke(SecurityContextEstablishmentValve.java:100) [:6.0.0.Final]\n" +
            "\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:127) [:6.0.0.Final]\n" +
            "\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:102) [:6.0.0.Final]\n" +
            "\tat org.jboss.web.tomcat.service.jca.CachedConnectionValve.invoke(CachedConnectionValve.java:158) [:6.0.0.Final]\n" +
            "\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:109) [:6.0.0.Final]\n" +
            "\tat org.jboss.web.tomcat.service.request.ActiveRequestResponseCacheValve.invoke(ActiveRequestResponseCacheValve.java:53) [:6.0.0.Final]\n" +
            "\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:362) [:6.0.0.Final]\n" +
            "\tat org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:877) [:6.0.0.Final]\n" +
            "\tat org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler.process(Http11Protocol.java:654) [:6.0.0.Final]\n" +
            "\tat org.apache.tomcat.util.net.JIoEndpoint$Worker.run(JIoEndpoint.java:951) [:6.0.0.Final]\n" +
            "\tat java.lang.Thread.run(Thread.java:662) [:1.6.0_23]\n" +
            "Caused by: javax.persistence.PersistenceException: org.hibernate.exception.ConstraintViolationException: could not insert: [com.hp.im.server.edm.dto.ProjectDTO]\n" +
            "\tat org.hibernate.ejb.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1214) [:3.6.0.Final]\n" +
            "\tat org.hibernate.ejb.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1147) [:3.6.0.Final]\n" +
            "\tat org.hibernate.ejb.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1153) [:3.6.0.Final]\n" +
            "\tat org.hibernate.ejb.AbstractEntityManagerImpl.persist(AbstractEntityManagerImpl.java:678) [:3.6.0.Final]\n" +
            "\tat org.jboss.jpa.impl.tx.TransactionScopedEntityManager.persist(TransactionScopedEntityManager.java:206) [:2.0.0]\n" +
            "\tat com.hp.im.server.edm.buslogic.ProjectBL.createProject(ProjectBL.java:52) [:]\n" +
            "\tat com.hp.im.server.edm.ejb.ProjectBean.createProject(ProjectBean.java:90) [:]\n" +
            "\tat com.hp.im.server.edm.ejb.ProjectBean.createProject(ProjectBean.java:31) [:]\n" +
            "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) [:1.6.0_23]\n" +
            "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39) [:1.6.0_23]\n" +
            "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25) [:1.6.0_23]\n" +
            "\tat java.lang.reflect.Method.invoke(Method.java:597) [:1.6.0_23]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeTarget(MethodInvocation.java:122) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:111) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.interceptors.container.ContainerMethodInvocationWrapper.invokeNext(ContainerMethodInvocationWrapper.java:72) [:1.1.3]\n" +
            "\tat org.jboss.ejb3.interceptors.aop.InvocationContextInterceptor$InvocationContext.proceed(InvocationContextInterceptor.java:127) [:1.1.3]\n" +
            "\tat com.hp.im.server.edm.ejb.UserNameInterceptor.findUserName(UserNameInterceptor.java:58) [:]\n" +
            "\tat sun.reflect.GeneratedMethodAccessor382.invoke(Unknown Source) [:1.6.0_23]\n" +
            "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25) [:1.6.0_23]\n" +
            "\tat java.lang.reflect.Method.invoke(Method.java:597) [:1.6.0_23]\n" +
            "\tat org.jboss.ejb3.interceptors.aop.EJB3InterceptorInterceptor.invoke(EJB3InterceptorInterceptor.java:80) [:1.1.3]\n" +
            "\tat org.jboss.ejb3.interceptors.aop.EJB3InterceptorInterceptor.invoke(EJB3InterceptorInterceptor.java:71) [:1.1.3]\n" +
            "\tat org.jboss.ejb3.interceptors.container.ContainerMethodInvocationWrapper.invokeNext(ContainerMethodInvocationWrapper.java:62) [:1.1.3]\n" +
            "\tat org.jboss.ejb3.interceptors.aop.InterceptorSequencer.invoke(InterceptorSequencer.java:76) [:1.1.3]\n" +
            "\tat org.jboss.ejb3.interceptors.aop.InterceptorSequencer.aroundInvoke(InterceptorSequencer.java:62) [:1.1.3]\n" +
            "\tat sun.reflect.GeneratedMethodAccessor377.invoke(Unknown Source) [:1.6.0_23]\n" +
            "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25) [:1.6.0_23]\n" +
            "\tat java.lang.reflect.Method.invoke(Method.java:597) [:1.6.0_23]\n" +
            "\tat org.jboss.aop.advice.PerJoinpointAdvice.invoke(PerJoinpointAdvice.java:174) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.interceptors.aop.InvocationContextInterceptor.fillMethod(InvocationContextInterceptor.java:74) [:1.1.3]\n" +
            "\tat org.jboss.aop.advice.org.jboss.ejb3.interceptors.aop.InvocationContextInterceptor_z_fillMethod_1355334584.invoke(InvocationContextInterceptor_z_fillMethod_1355334584.java)\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.interceptors.aop.InvocationContextInterceptor.setup(InvocationContextInterceptor.java:90) [:1.1.3]\n" +
            "\tat org.jboss.aop.advice.org.jboss.ejb3.interceptors.aop.InvocationContextInterceptor_z_setup_1355334584.invoke(InvocationContextInterceptor_z_setup_1355334584.java)\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.async.impl.interceptor.AsynchronousServerInterceptor.invoke(AsynchronousServerInterceptor.java:128) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.connectionmanager.CachedConnectionInterceptor.invoke(CachedConnectionInterceptor.java:62) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.entity.TransactionScopedEntityManagerInterceptor.invoke(TransactionScopedEntityManagerInterceptor.java:56) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.AllowedOperationsInterceptor.invoke(AllowedOperationsInterceptor.java:47) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.tx.NullInterceptor.invoke(NullInterceptor.java:42) [:1.0.3]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.stateless.StatelessInstanceInterceptor.invoke(StatelessInstanceInterceptor.java:68) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.core.context.SessionInvocationContextAdapter.proceed(SessionInvocationContextAdapter.java:95) [:1.7.17]\n" +
            "\tat org.jboss.ejb3.tx2.impl.CMTTxInterceptor.invokeInOurTx(CMTTxInterceptor.java:247) [:0.0.1]\n" +
            "\t... 91 more\n" +
            "Caused by: org.hibernate.exception.ConstraintViolationException: could not insert: [com.hp.im.server.edm.dto.ProjectDTO]\n" +
            "\tat org.hibernate.exception.SQLStateConverter.convert(SQLStateConverter.java:96) [:3.6.0.Final]\n" +
            "\tat org.hibernate.exception.JDBCExceptionHelper.convert(JDBCExceptionHelper.java:66) [:3.6.0.Final]\n" +
            "\tat org.hibernate.id.insert.AbstractSelectingDelegate.performInsert(AbstractSelectingDelegate.java:64) [:3.6.0.Final]\n" +
            "\tat org.hibernate.persister.entity.AbstractEntityPersister.insert(AbstractEntityPersister.java:2327) [:3.6.0.Final]\n" +
            "\tat org.hibernate.persister.entity.AbstractEntityPersister.insert(AbstractEntityPersister.java:2834) [:3.6.0.Final]\n" +
            "\tat org.hibernate.action.EntityIdentityInsertAction.execute(EntityIdentityInsertAction.java:71) [:3.6.0.Final]\n" +
            "\tat org.hibernate.engine.ActionQueue.execute(ActionQueue.java:273) [:3.6.0.Final]\n" +
            "\tat org.hibernate.event.def.AbstractSaveEventListener.performSaveOrReplicate(AbstractSaveEventListener.java:320) [:3.6.0.Final]\n" +
            "\tat org.hibernate.event.def.AbstractSaveEventListener.performSave(AbstractSaveEventListener.java:203) [:3.6.0.Final]\n" +
            "\tat org.hibernate.event.def.AbstractSaveEventListener.saveWithGeneratedId(AbstractSaveEventListener.java:129) [:3.6.0.Final]\n" +
            "\tat org.hibernate.ejb.event.EJB3PersistEventListener.saveWithGeneratedId(EJB3PersistEventListener.java:69) [:3.6.0.Final]\n" +
            "\tat org.hibernate.event.def.DefaultPersistEventListener.entityIsTransient(DefaultPersistEventListener.java:179) [:3.6.0.Final]\n" +
            "\tat org.hibernate.event.def.DefaultPersistEventListener.onPersist(DefaultPersistEventListener.java:135) [:3.6.0.Final]\n" +
            "\tat org.hibernate.event.def.DefaultPersistEventListener.onPersist(DefaultPersistEventListener.java:61) [:3.6.0.Final]\n" +
            "\tat org.hibernate.impl.SessionImpl.firePersist(SessionImpl.java:808) [:3.6.0.Final]\n" +
            "\tat org.hibernate.impl.SessionImpl.persist(SessionImpl.java:782) [:3.6.0.Final]\n" +
            "\tat org.hibernate.impl.SessionImpl.persist(SessionImpl.java:786) [:3.6.0.Final]\n" +
            "\tat org.hibernate.ejb.AbstractEntityManagerImpl.persist(AbstractEntityManagerImpl.java:672) [:3.6.0.Final]\n" +
            "\t... 137 more\n" +
            "Caused by: java.sql.SQLIntegrityConstraintViolationException: The statement was aborted because it would have caused a duplicate key value in a unique or primary key constraint or unique index identified by 'SQL110615140909511' defined on 'EDIS_PROJECT'.\n" +
            "\tat org.apache.derby.client.am.SQLExceptionFactory40.getSQLException(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.am.SqlException.getSQLException(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.am.PreparedStatement.executeUpdate(Unknown Source) [:]\n" +
            "\tat org.jboss.resource.adapter.jdbc.CachedPreparedStatement.executeUpdate(CachedPreparedStatement.java:96) [:6.0.0.Final]\n" +
            "\tat org.jboss.resource.adapter.jdbc.WrappedPreparedStatement.executeUpdate(WrappedPreparedStatement.java:365) [:6.0.0.Final]\n" +
            "\tat org.hibernate.id.insert.AbstractSelectingDelegate.performInsert(AbstractSelectingDelegate.java:57) [:3.6.0.Final]\n" +
            "\t... 152 more\n" +
            "Caused by: org.apache.derby.client.am.SqlException: The statement was aborted because it would have caused a duplicate key value in a unique or primary key constraint or unique index identified by 'SQL110615140909511' defined on 'EDIS_PROJECT'.\n" +
            "\tat org.apache.derby.client.am.Statement.completeExecute(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.net.NetStatementReply.parseEXCSQLSTTreply(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.net.NetStatementReply.readExecute(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.net.StatementReply.readExecute(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.net.NetPreparedStatement.readExecute_(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.am.PreparedStatement.readExecute(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.am.PreparedStatement.flowExecute(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.am.PreparedStatement.executeUpdateX(Unknown Source) [:]\n" +
            "\t... 156 more\n" +
            "\n" +
            "2011-06-15 14:11:12,641 WARN  [org.hibernate.util.JDBCExceptionReporter] (http-127.0.0.1-8181-5) SQL Error: -1, SQLState: 23505\n" +
            "2011-06-15 14:11:12,641 ERROR [org.hibernate.util.JDBCExceptionReporter] (http-127.0.0.1-8181-5) The statement was aborted because it would have caused a duplicate key value in a unique or primary key constraint or unique index identified by 'SQL110615140909511' defined on 'EDIS_PROJECT'.\n" +
            "2011-06-15 14:11:12,687 ERROR [com.hp.im.server.edm.rpc.ProjectServiceImpl] (http-127.0.0.1-8181-5) javax.persistence.PersistenceException: org.hibernate.exception.ConstraintViolationException: could not insert: [com.hp.im.server.edm.dto.ProjectDTO]: javax.ejb.EJBException: javax.persistence.PersistenceException: org.hibernate.exception.ConstraintViolationException: could not insert: [com.hp.im.server.edm.dto.ProjectDTO]\n" +
            "\tat org.jboss.ejb3.tx2.impl.CMTTxInterceptor.handleExceptionInOurTx(CMTTxInterceptor.java:183) [:0.0.1]\n" +
            "\tat org.jboss.ejb3.tx2.impl.CMTTxInterceptor.invokeInOurTx(CMTTxInterceptor.java:251) [:0.0.1]\n" +
            "\tat org.jboss.ejb3.tx2.impl.CMTTxInterceptor.required(CMTTxInterceptor.java:349) [:0.0.1]\n" +
            "\tat org.jboss.ejb3.tx2.impl.CMTTxInterceptor.invoke(CMTTxInterceptor.java:209) [:0.0.1]\n" +
            "\tat org.jboss.ejb3.tx2.aop.CMTTxInterceptorWrapper.invoke(CMTTxInterceptorWrapper.java:52) [:0.0.1]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.aspects.tx.TxPropagationInterceptor.invoke(TxPropagationInterceptor.java:76) [:1.0.0.GA]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.tx.NullInterceptor.invoke(NullInterceptor.java:42) [:1.0.3]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.security.Ejb3AuthenticationInterceptorv2.invoke(Ejb3AuthenticationInterceptorv2.java:182) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.ENCPropagationInterceptor.invoke(ENCPropagationInterceptor.java:41) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.BlockContainerShutdownInterceptor.invoke(BlockContainerShutdownInterceptor.java:67) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.core.context.CurrentInvocationContextInterceptor.invoke(CurrentInvocationContextInterceptor.java:47) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.aspects.currentinvocation.CurrentInvocationInterceptor.invoke(CurrentInvocationInterceptor.java:67) [:1.0.1]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.interceptor.EJB3TCCLInterceptor.invoke(EJB3TCCLInterceptor.java:86) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.stateless.StatelessContainer.dynamicInvoke(StatelessContainer.java:392) [:1.7.17]\n" +
            "\tat org.jboss.ejb3.remoting.IsLocalInterceptor.invokeLocal(IsLocalInterceptor.java:88) [:1.7.17]\n" +
            "\tat org.jboss.ejb3.remoting.IsLocalInterceptor.invoke(IsLocalInterceptor.java:75) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.async.impl.interceptor.AsynchronousClientInterceptor.invoke(AsynchronousClientInterceptor.java:143) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.aspects.remoting.PojiProxy.invoke(PojiProxy.java:62) [:1.0.1.GA]\n" +
            "\tat $Proxy195.invoke(Unknown Source)\tat org.jboss.ejb3.proxy.impl.handler.session.SessionProxyInvocationHandlerBase.invoke(SessionProxyInvocationHandlerBase.java:185) [:1.0.11]\n" +
            "\tat $Proxy242.createProject(Unknown Source)\tat com.hp.im.server.edm.rpc.ProjectServiceImpl.createProject(ProjectServiceImpl.java:79) [:]\n" +
            "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) [:1.6.0_23]\n" +
            "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39) [:1.6.0_23]\n" +
            "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25) [:1.6.0_23]\n" +
            "\tat java.lang.reflect.Method.invoke(Method.java:597) [:1.6.0_23]\n" +
            "\tat com.google.gwt.user.server.rpc.RPC.invokeAndEncodeResponse(RPC.java:562) [:]\n" +
            "\tat com.google.gwt.user.server.rpc.RemoteServiceServlet.processCall(RemoteServiceServlet.java:188) [:]\n" +
            "\tat com.google.gwt.user.server.rpc.RemoteServiceServlet.processPost(RemoteServiceServlet.java:224) [:]\n" +
            "\tat com.google.gwt.user.server.rpc.AbstractRemoteServiceServlet.doPost(AbstractRemoteServiceServlet.java:62) [:]\n" +
            "\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:754) [:1.0.0.Final]\n" +
            "\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:847) [:1.0.0.Final]\n" +
            "\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:324) [:6.0.0.Final]\n" +
            "\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:242) [:6.0.0.Final]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:368) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.access.intercept.FilterSecurityInterceptor.invoke(FilterSecurityInterceptor.java:109) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.access.intercept.FilterSecurityInterceptor.doFilter(FilterSecurityInterceptor.java:83) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.access.ExceptionTranslationFilter.doFilter(ExceptionTranslationFilter.java:97) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.session.SessionManagementFilter.doFilter(SessionManagementFilter.java:100) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.authentication.AnonymousAuthenticationFilter.doFilter(AnonymousAuthenticationFilter.java:78) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter.doFilter(SecurityContextHolderAwareRequestFilter.java:54) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.savedrequest.RequestCacheAwareFilter.doFilter(RequestCacheAwareFilter.java:35) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat com.hp.im.server.edm.servlet.filter.GWTCacheFilter.doFilter(GWTCacheFilter.java:39) [:]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter.doFilter(AbstractAuthenticationProcessingFilter.java:187) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.authentication.logout.LogoutFilter.doFilter(LogoutFilter.java:105) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.context.SecurityContextPersistenceFilter.doFilter(SecurityContextPersistenceFilter.java:79) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.session.ConcurrentSessionFilter.doFilter(ConcurrentSessionFilter.java:109) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.access.channel.ChannelProcessingFilter.doFilter(ChannelProcessingFilter.java:109) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:380) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.security.web.FilterChainProxy.doFilter(FilterChainProxy.java:169) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.web.filter.DelegatingFilterProxy.invokeDelegate(DelegatingFilterProxy.java:237) [:3.0.5.RELEASE]\n" +
            "\tat org.springframework.web.filter.DelegatingFilterProxy.doFilter(DelegatingFilterProxy.java:167) [:3.0.5.RELEASE]\n" +
            "\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:274) [:6.0.0.Final]\n" +
            "\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:242) [:6.0.0.Final]\n" +
            "\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:275) [:6.0.0.Final]\n" +
            "\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:191) [:6.0.0.Final]\n" +
            "\tat org.jboss.web.tomcat.security.SecurityAssociationValve.invoke(SecurityAssociationValve.java:181) [:6.0.0.Final]\n" +
            "\tat org.jboss.modcluster.catalina.CatalinaContext$RequestListenerValve.event(CatalinaContext.java:285) [:1.1.0.Final]\n" +
            "\tat org.jboss.modcluster.catalina.CatalinaContext$RequestListenerValve.invoke(CatalinaContext.java:261) [:1.1.0.Final]\n" +
            "\tat org.jboss.web.tomcat.security.JaccContextValve.invoke(JaccContextValve.java:88) [:6.0.0.Final]\n" +
            "\tat org.jboss.web.tomcat.security.SecurityContextEstablishmentValve.invoke(SecurityContextEstablishmentValve.java:100) [:6.0.0.Final]\n" +
            "\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:127) [:6.0.0.Final]\n" +
            "\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:102) [:6.0.0.Final]\n" +
            "\tat org.jboss.web.tomcat.service.jca.CachedConnectionValve.invoke(CachedConnectionValve.java:158) [:6.0.0.Final]\n" +
            "\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:109) [:6.0.0.Final]\n" +
            "\tat org.jboss.web.tomcat.service.request.ActiveRequestResponseCacheValve.invoke(ActiveRequestResponseCacheValve.java:53) [:6.0.0.Final]\n" +
            "\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:362) [:6.0.0.Final]\n" +
            "\tat org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:877) [:6.0.0.Final]\n" +
            "\tat org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler.process(Http11Protocol.java:654) [:6.0.0.Final]\n" +
            "\tat org.apache.tomcat.util.net.JIoEndpoint$Worker.run(JIoEndpoint.java:951) [:6.0.0.Final]\n" +
            "\tat java.lang.Thread.run(Thread.java:662) [:1.6.0_23]\n" +
            "Caused by: javax.persistence.PersistenceException: org.hibernate.exception.ConstraintViolationException: could not insert: [com.hp.im.server.edm.dto.ProjectDTO]\n" +
            "\tat org.hibernate.ejb.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1214) [:3.6.0.Final]\n" +
            "\tat org.hibernate.ejb.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1147) [:3.6.0.Final]\n" +
            "\tat org.hibernate.ejb.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1153) [:3.6.0.Final]\n" +
            "\tat org.hibernate.ejb.AbstractEntityManagerImpl.persist(AbstractEntityManagerImpl.java:678) [:3.6.0.Final]\n" +
            "\tat org.jboss.jpa.impl.tx.TransactionScopedEntityManager.persist(TransactionScopedEntityManager.java:206) [:2.0.0]\n" +
            "\tat com.hp.im.server.edm.buslogic.ProjectBL.createProject(ProjectBL.java:52) [:]\n" +
            "\tat com.hp.im.server.edm.ejb.ProjectBean.createProject(ProjectBean.java:90) [:]\n" +
            "\tat com.hp.im.server.edm.ejb.ProjectBean.createProject(ProjectBean.java:31) [:]\n" +
            "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) [:1.6.0_23]\n" +
            "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39) [:1.6.0_23]\n" +
            "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25) [:1.6.0_23]\n" +
            "\tat java.lang.reflect.Method.invoke(Method.java:597) [:1.6.0_23]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeTarget(MethodInvocation.java:122) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:111) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.interceptors.container.ContainerMethodInvocationWrapper.invokeNext(ContainerMethodInvocationWrapper.java:72) [:1.1.3]\n" +
            "\tat org.jboss.ejb3.interceptors.aop.InvocationContextInterceptor$InvocationContext.proceed(InvocationContextInterceptor.java:127) [:1.1.3]\n" +
            "\tat com.hp.im.server.edm.ejb.UserNameInterceptor.findUserName(UserNameInterceptor.java:58) [:]\n" +
            "\tat sun.reflect.GeneratedMethodAccessor382.invoke(Unknown Source) [:1.6.0_23]\n" +
            "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25) [:1.6.0_23]\n" +
            "\tat java.lang.reflect.Method.invoke(Method.java:597) [:1.6.0_23]\n" +
            "\tat org.jboss.ejb3.interceptors.aop.EJB3InterceptorInterceptor.invoke(EJB3InterceptorInterceptor.java:80) [:1.1.3]\n" +
            "\tat org.jboss.ejb3.interceptors.aop.EJB3InterceptorInterceptor.invoke(EJB3InterceptorInterceptor.java:71) [:1.1.3]\n" +
            "\tat org.jboss.ejb3.interceptors.container.ContainerMethodInvocationWrapper.invokeNext(ContainerMethodInvocationWrapper.java:62) [:1.1.3]\n" +
            "\tat org.jboss.ejb3.interceptors.aop.InterceptorSequencer.invoke(InterceptorSequencer.java:76) [:1.1.3]\n" +
            "\tat org.jboss.ejb3.interceptors.aop.InterceptorSequencer.aroundInvoke(InterceptorSequencer.java:62) [:1.1.3]\n" +
            "\tat sun.reflect.GeneratedMethodAccessor377.invoke(Unknown Source) [:1.6.0_23]\n" +
            "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25) [:1.6.0_23]\n" +
            "\tat java.lang.reflect.Method.invoke(Method.java:597) [:1.6.0_23]\n" +
            "\tat org.jboss.aop.advice.PerJoinpointAdvice.invoke(PerJoinpointAdvice.java:174) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.interceptors.aop.InvocationContextInterceptor.fillMethod(InvocationContextInterceptor.java:74) [:1.1.3]\n" +
            "\tat org.jboss.aop.advice.org.jboss.ejb3.interceptors.aop.InvocationContextInterceptor_z_fillMethod_1355334584.invoke(InvocationContextInterceptor_z_fillMethod_1355334584.java)\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.interceptors.aop.InvocationContextInterceptor.setup(InvocationContextInterceptor.java:90) [:1.1.3]\n" +
            "\tat org.jboss.aop.advice.org.jboss.ejb3.interceptors.aop.InvocationContextInterceptor_z_setup_1355334584.invoke(InvocationContextInterceptor_z_setup_1355334584.java)\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.async.impl.interceptor.AsynchronousServerInterceptor.invoke(AsynchronousServerInterceptor.java:128) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.connectionmanager.CachedConnectionInterceptor.invoke(CachedConnectionInterceptor.java:62) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.entity.TransactionScopedEntityManagerInterceptor.invoke(TransactionScopedEntityManagerInterceptor.java:56) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.AllowedOperationsInterceptor.invoke(AllowedOperationsInterceptor.java:47) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.tx.NullInterceptor.invoke(NullInterceptor.java:42) [:1.0.3]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.stateless.StatelessInstanceInterceptor.invoke(StatelessInstanceInterceptor.java:68) [:1.7.17]\n" +
            "\tat org.jboss.aop.joinpoint.MethodInvocation.invokeNext(MethodInvocation.java:102) [jboss-aop.jar:2.2.1.GA]\n" +
            "\tat org.jboss.ejb3.core.context.SessionInvocationContextAdapter.proceed(SessionInvocationContextAdapter.java:95) [:1.7.17]\n" +
            "\tat org.jboss.ejb3.tx2.impl.CMTTxInterceptor.invokeInOurTx(CMTTxInterceptor.java:247) [:0.0.1]\n" +
            "\t... 91 more\n" +
            "Caused by: org.hibernate.exception.ConstraintViolationException: could not insert: [com.hp.im.server.edm.dto.ProjectDTO]\n" +
            "\tat org.hibernate.exception.SQLStateConverter.convert(SQLStateConverter.java:96) [:3.6.0.Final]\n" +
            "\tat org.hibernate.exception.JDBCExceptionHelper.convert(JDBCExceptionHelper.java:66) [:3.6.0.Final]\n" +
            "\tat org.hibernate.id.insert.AbstractSelectingDelegate.performInsert(AbstractSelectingDelegate.java:64) [:3.6.0.Final]\n" +
            "\tat org.hibernate.persister.entity.AbstractEntityPersister.insert(AbstractEntityPersister.java:2327) [:3.6.0.Final]\n" +
            "\tat org.hibernate.persister.entity.AbstractEntityPersister.insert(AbstractEntityPersister.java:2834) [:3.6.0.Final]\n" +
            "\tat org.hibernate.action.EntityIdentityInsertAction.execute(EntityIdentityInsertAction.java:71) [:3.6.0.Final]\n" +
            "\tat org.hibernate.engine.ActionQueue.execute(ActionQueue.java:273) [:3.6.0.Final]\n" +
            "\tat org.hibernate.event.def.AbstractSaveEventListener.performSaveOrReplicate(AbstractSaveEventListener.java:320) [:3.6.0.Final]\n" +
            "\tat org.hibernate.event.def.AbstractSaveEventListener.performSave(AbstractSaveEventListener.java:203) [:3.6.0.Final]\n" +
            "\tat org.hibernate.event.def.AbstractSaveEventListener.saveWithGeneratedId(AbstractSaveEventListener.java:129) [:3.6.0.Final]\n" +
            "\tat org.hibernate.ejb.event.EJB3PersistEventListener.saveWithGeneratedId(EJB3PersistEventListener.java:69) [:3.6.0.Final]\n" +
            "\tat org.hibernate.event.def.DefaultPersistEventListener.entityIsTransient(DefaultPersistEventListener.java:179) [:3.6.0.Final]\n" +
            "\tat org.hibernate.event.def.DefaultPersistEventListener.onPersist(DefaultPersistEventListener.java:135) [:3.6.0.Final]\n" +
            "\tat org.hibernate.event.def.DefaultPersistEventListener.onPersist(DefaultPersistEventListener.java:61) [:3.6.0.Final]\n" +
            "\tat org.hibernate.impl.SessionImpl.firePersist(SessionImpl.java:808) [:3.6.0.Final]\n" +
            "\tat org.hibernate.impl.SessionImpl.persist(SessionImpl.java:782) [:3.6.0.Final]\n" +
            "\tat org.hibernate.impl.SessionImpl.persist(SessionImpl.java:786) [:3.6.0.Final]\n" +
            "\tat org.hibernate.ejb.AbstractEntityManagerImpl.persist(AbstractEntityManagerImpl.java:672) [:3.6.0.Final]\n" +
            "\t... 137 more\n" +
            "Caused by: java.sql.SQLIntegrityConstraintViolationException: The statement was aborted because it would have caused a duplicate key value in a unique or primary key constraint or unique index identified by 'SQL110615140909511' defined on 'EDIS_PROJECT'.\n" +
            "\tat org.apache.derby.client.am.SQLExceptionFactory40.getSQLException(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.am.SqlException.getSQLException(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.am.PreparedStatement.executeUpdate(Unknown Source) [:]\n" +
            "\tat org.jboss.resource.adapter.jdbc.CachedPreparedStatement.executeUpdate(CachedPreparedStatement.java:96) [:6.0.0.Final]\n" +
            "\tat org.jboss.resource.adapter.jdbc.WrappedPreparedStatement.executeUpdate(WrappedPreparedStatement.java:365) [:6.0.0.Final]\n" +
            "\tat org.hibernate.id.insert.AbstractSelectingDelegate.performInsert(AbstractSelectingDelegate.java:57) [:3.6.0.Final]\n" +
            "\t... 152 more\n" +
            "Caused by: org.apache.derby.client.am.SqlException: The statement was aborted because it would have caused a duplicate key value in a unique or primary key constraint or unique index identified by 'SQL110615140909511' defined on 'EDIS_PROJECT'.\n" +
            "\tat org.apache.derby.client.am.Statement.completeExecute(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.net.NetStatementReply.parseEXCSQLSTTreply(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.net.NetStatementReply.readExecute(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.net.StatementReply.readExecute(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.net.NetPreparedStatement.readExecute_(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.am.PreparedStatement.readExecute(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.am.PreparedStatement.flowExecute(Unknown Source) [:]\n" +
            "\tat org.apache.derby.client.am.PreparedStatement.executeUpdateX(Unknown Source) [:]\n" +
            "\t... 156 more\n" +
            "\n" +
            "2011-06-15 14:19:49,480 INFO  [org.jboss.system.server.jmx.JMXKernel] (Thread-27) Server exit called, exiting the JVM now!\n" +
            "2011-06-15 14:19:49,481 INFO  [STDOUT] (Thread-1) Posting Shutdown Request to the server...\n" +
            "\n" +
            "2011-06-15 14:19:49,482 INFO  [org.jboss.bootstrap.impl.base.server.AbstractServer] (Thread-28) Stopping: JBossAS [6.0.0.Final \"Neo\"]\n" +
            "2011-06-15 14:19:49,483 INFO  [org.apache.coyote.http11.Http11Protocol] (Thread-28) Pausing Coyote HTTP/1.1 on http-127.0.0.1-8080\n" +
            "2011-06-15 14:19:49,484 INFO  [org.apache.coyote.http11.Http11Protocol] (Thread-28) Stopping Coyote HTTP/1.1 on http-127.0.0.1-8080\n" +
            "2011-06-15 14:19:49,486 INFO  [org.apache.coyote.ajp.AjpProtocol] (Thread-28) Pausing Coyote AJP/1.3 on ajp-127.0.0.1-8009\n" +
            "2011-06-15 14:19:49,487 INFO  [org.apache.coyote.ajp.AjpProtocol] (Thread-28) Stopping Coyote AJP/1.3 on ajp-127.0.0.1-8009\n" +
            "2011-06-15 14:19:49,489 INFO  [org.apache.coyote.http11.Http11Protocol] (Thread-28) Pausing Coyote HTTP/1.1 on http-127.0.0.1-8181\n" +
            "2011-06-15 14:19:49,49";

    @Test
    public void test() throws Exception {
        final Pattern pattern = Pattern.compile("(.*)\\W(\\wException)(\\W.*)");
        final String[] lines = TextToLines.toLines(TEXT);
        for (String line : lines) {
            final Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                System.out.println(matcher.group(2));
            }
        }
    }
}
