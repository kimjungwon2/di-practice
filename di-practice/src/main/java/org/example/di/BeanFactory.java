package org.example.di;

import org.example.annotation.Inject;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class BeanFactory {
    private final Set<Class<?>> preInstantiatedClazz;
    private Map<Class<?>, Object> beans = new HashMap<>();

    public BeanFactory(Set<Class<?>> preInstantiatedClazz) {
        this.preInstantiatedClazz = preInstantiatedClazz;
        initialize();
    }

    private void initialize() {
        for(Class<?> clazz : preInstantiatedClazz) {
            Object instance = createInstance(clazz);
            beans.put(clazz, instance);
        }
    }

    private Object createInstance(Class<?> clazz) {
        Constructor<?> constructor = findConstructor(clazz);
        List<Object> parameters = new ArrayList<>();

        for(Class<?> typeClass : constructor.getParameterTypes()) {
            parameters.add(getParameterByClass(typeClass));
        }

        try {
            return constructor.newInstance(parameters.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Constructor<?> findConstructor(Class<?> clazz) {
        Constructor<?> constructor = BeanFactoryUtils.getInjectedConstructor(clazz);

        if(Objects.nonNull(constructor)) {
            return constructor;
        }

        return clazz.getConstructors()[0];
    }

    public <T> T getBean(Class<T> requiredType) {
        return (T) beans.get(requiredType);
    }

    private Object getParameterByClass(Class<?> typeClass) {
        Object instanceBean = getBean(typeClass);
        if(Objects.nonNull(instanceBean)) {
            return instanceBean;
        }

        return createInstance(typeClass);
    }
}
