package com.github.jaryur.sequence.annotation;

import com.github.jaryur.sequence.CacheMode;
import com.github.jaryur.sequence.configuration.SequenceDefinitionRegistar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by Jaryur
 * Comment:
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SequenceDefinitionRegistar.class)
public @interface EnableSequenceGenerator {

    String application();

    CacheMode cacheMode() default CacheMode.EAGER;

    int defaultCacheNum() default 1;

    int defaultStep() default 10;

}

