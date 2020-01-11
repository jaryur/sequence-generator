package com.github.jaryur.sequence.configuration;

import com.github.jaryur.sequence.CacheMode;
import com.github.jaryur.sequence.DefaultProperties;
import com.github.jaryur.sequence.annotation.EnableSequenceGenerator;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.MultiValueMap;

/**
 * Created by Jaryur
 * Comment:
 */
public class SequenceDefinitionRegistar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        MultiValueMap<String, Object> attributes = importingClassMetadata.getAllAnnotationAttributes(
                EnableSequenceGenerator.class.getName(), false);
        DefaultProperties defaultProperties = new DefaultProperties();
        if (attributes != null) {
            String applicationPlaceHolder = (String) attributes.getFirst("application");
            CacheMode cacheMode = (CacheMode) attributes.getFirst("cacheMode");
            int defaultCacheNum = (int) attributes.getFirst("defaultCacheNum");
            int defaultStep = (int) attributes.getFirst("defaultStep");
//            SpelExpressionParser parser = new SpelExpressionParser();
            String application = environment.resolvePlaceholders(applicationPlaceHolder);
            defaultProperties.setLazyMode(CacheMode.LAZY.equals(cacheMode));
            defaultProperties.setDefaultCacheNum(defaultCacheNum);
            defaultProperties.setDefaultStep(defaultStep);
            defaultProperties.setApplication(application);
        }
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(DefaultProperties.class)
                .addPropertyValue("lazyMode", defaultProperties.isLazyMode())
                .addPropertyValue("defaultCacheNum", defaultProperties.getDefaultCacheNum())
                .addPropertyValue("defaultStep", defaultProperties.getDefaultStep())
                .addPropertyValue("application", defaultProperties.getApplication());
        registry.registerBeanDefinition(DefaultProperties.class.getName(), beanDefinitionBuilder.getBeanDefinition());
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}

