package com.javasm.demo.spring;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class TestApplicationContext {
    public Class configClass;
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> singletonMap = new ConcurrentHashMap<>();

    public TestApplicationContext(Class configClass) {
        //第一步 扫描配置类

        this.configClass = configClass;
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            //获取扫描注解的相关信息
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            //获取的注解值为扫描路径
            String path = componentScanAnnotation.value(); //路径 com.javasm.demo
            path = path.replace(".", "/"); //com/javasm/demo
            ClassLoader classLoader = TestApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);
            File file = new File(resource.getFile()); //既能表示一个对象也能表示一个目录 ，故封装成为一个File对象
            //System.out.println(file);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    String fileName = f.getAbsolutePath();
                    //System.out.println(fileName);
                    if (fileName.endsWith(".class")) {
                        String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                        // className com.javasm.demo.service.UserService
                        className = className.replace("\\", ".");
                        System.out.println(className);
                        try {
                            Class<?> loadClass = classLoader.loadClass(className);
                            if (loadClass.isAnnotationPresent(Component.class)) {
                                Component component = loadClass.getAnnotation(Component.class);
                                String beanName = component.value();
                                //证明是一个bean;同时说明程序员生成了一个bean
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(loadClass);
                                if (loadClass.isAnnotationPresent(Scope.class)) {
                                    Scope scopeAnnotation = loadClass.getAnnotation(Scope.class);
                                    beanDefinition.setScope(scopeAnnotation.value());
                                } else {
                                    beanDefinition.setScope("singleton");
                                }
                                beanDefinitionMap.put(beanName, beanDefinition);

                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singletonMap.put(beanName, bean);
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        return null;
    }


    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new NullPointerException();
        } else {
            String scope = beanDefinition.getScope();
            if (scope.equals("singleton")) {
                Object bean = singletonMap.get(beanName);
                if (bean == null) {
                    Object bean1 = createBean(beanName, beanDefinition);
                    singletonMap.put(beanName, bean1);
                }
                return bean;
            } else {
                //多例
                return createBean(beanName, beanDefinition);
            }
        }
    }
}
