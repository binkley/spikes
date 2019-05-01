package com.example.demo;

import org.apache.commons.logging.Log;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.cloud.aws.messaging.listener.QueueMessageHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.HandlerMethod;
import org.springframework.messaging.handler.MessagingAdviceBean;
import org.springframework.messaging.handler.invocation.AbstractExceptionHandlerMethodResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DemoQueueMessageHandler extends QueueMessageHandler {
    public DemoQueueMessageHandler(List<MessageConverter> messageConverters) {
        super(messageConverters);
    }

    @Override
    protected List<? extends HandlerMethodArgumentResolver> initArgumentResolvers() {
        return super.initArgumentResolvers();
    }

    @Override
    protected List<? extends HandlerMethodReturnValueHandler> initReturnValueHandlers() {
        return super.initReturnValueHandlers();
    }

    @Override
    protected Log getHandlerMethodLogger() {
        final var facade = new DeferredLog();
        facade.switchTo(getClass());
        return facade;
    }

    @Override
    protected boolean isHandler(final Class<?> beanType) {
        return super.isHandler(beanType);
    }

    @Override
    protected MappingInformation getMappingForMethod(final Method method,
            final Class<?> handlerType) {
        return super.getMappingForMethod(method, handlerType);
    }

    @Override
    protected Set<String> getDirectLookupDestinations(final MappingInformation mapping) {
        return super.getDirectLookupDestinations(mapping);
    }

    @Override
    protected String getDestination(final Message<?> message) {
        return super.getDestination(message);
    }

    @Override
    protected MappingInformation getMatchingMapping(final MappingInformation mapping,
            final Message<?> message) {
        return super.getMatchingMapping(mapping, message);
    }

    @Override
    protected Comparator<MappingInformation> getMappingComparator(final Message<?> message) {
        return super.getMappingComparator(message);
    }

    @Override
    protected AbstractExceptionHandlerMethodResolver createExceptionHandlerMethodResolverFor(
            final Class<?> beanType) {
        return super.createExceptionHandlerMethodResolverFor(beanType);
    }

    @Override
    protected void handleNoMatch(final Set<MappingInformation> ts, final String lookupDestination,
            final Message<?> message) {
        super.handleNoMatch(ts, lookupDestination, message);
    }

    @Override
    protected void processHandlerMethodException(final HandlerMethod handlerMethod,
            final Exception ex,
            final Message<?> message) {
        super.processHandlerMethodException(handlerMethod, ex, message);
    }



    @Override
    public void setDestinationPrefixes(final Collection<String> prefixes) {
        super.setDestinationPrefixes(prefixes);
    }

    @Override
    public Collection<String> getDestinationPrefixes() {
        return super.getDestinationPrefixes();
    }

    @Override
    public void setCustomArgumentResolvers(
            final List<HandlerMethodArgumentResolver> customArgumentResolvers) {
        super.setCustomArgumentResolvers(customArgumentResolvers);
    }

    @Override
    public List<HandlerMethodArgumentResolver> getCustomArgumentResolvers() {
        return super.getCustomArgumentResolvers();
    }

    @Override
    public void setCustomReturnValueHandlers(
            final List<HandlerMethodReturnValueHandler> customReturnValueHandlers) {
        super.setCustomReturnValueHandlers(customReturnValueHandlers);
    }

    @Override
    public List<HandlerMethodReturnValueHandler> getCustomReturnValueHandlers() {
        return super.getCustomReturnValueHandlers();
    }

    @Override
    public void setArgumentResolvers(final List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.setArgumentResolvers(argumentResolvers);
    }

    @Override
    public List<HandlerMethodArgumentResolver> getArgumentResolvers() {
        return super.getArgumentResolvers();
    }

    @Override
    public void setReturnValueHandlers(
            final List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        super.setReturnValueHandlers(returnValueHandlers);
    }

    @Override
    public List<HandlerMethodReturnValueHandler> getReturnValueHandlers() {
        return super.getReturnValueHandlers();
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        super.setApplicationContext(applicationContext);
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return super.getApplicationContext();
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
    }

    @Override
    protected void registerHandlerMethod(final Object handler, final Method method,
            final MappingInformation mapping) {
        super.registerHandlerMethod(handler, method, mapping);
    }

    @Override
    protected HandlerMethod createHandlerMethod(final Object handler, final Method method) {
        return super.createHandlerMethod(handler, method);
    }

    @Override
    protected Log getReturnValueHandlerLogger() {
        return super.getReturnValueHandlerLogger();
    }

    @Override
    protected void registerExceptionHandlerAdvice(final MessagingAdviceBean bean,
            final AbstractExceptionHandlerMethodResolver resolver) {
        super.registerExceptionHandlerAdvice(bean, resolver);
    }

    @Override
    public Map<MappingInformation, HandlerMethod> getHandlerMethods() {
        return super.getHandlerMethods();
    }

    @Override
    public void handleMessage(final Message<?> message) throws MessagingException {
        super.handleMessage(message);
    }

    @Override
    protected String getLookupDestination(final String destination) {
        return super.getLookupDestination(destination);
    }

    @Override
    protected void handleMessageInternal(final Message<?> message, final String lookupDestination) {
        super.handleMessageInternal(message, lookupDestination);
    }

    @Override
    protected void handleMatch(final MappingInformation mapping, final HandlerMethod handlerMethod,
            final String lookupDestination, final Message<?> message) {
        super.handleMatch(mapping, handlerMethod, lookupDestination, message);
    }

    @Override
    protected InvocableHandlerMethod getExceptionHandlerMethod(final HandlerMethod handlerMethod,
            final Exception exception) {
        return super.getExceptionHandlerMethod(handlerMethod, exception);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
