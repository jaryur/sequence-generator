package com.github.jaryur.sequence.configuration;

import com.github.jaryur.sequence.DefaultProperties;
import com.github.jaryur.sequence.MysqlSequenceGenerator;
import com.github.jaryur.sequence.exception.SequenceConfigException;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Jaryur
 *
 * Comment:Auto Configuration
 */
@Configuration
@EnableConfigurationProperties(SequenceProperties.class)
public class SequenceAutoConfiguration {

    @Autowired
    private SequenceProperties sequenceProperties;

    @Bean(name = "DianwodaSequenceDatasource")
    public DataSource initDatasource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(sequenceProperties.getDatasource().getUrl());
        dataSource.setUsername(sequenceProperties.getDatasource().getUsername());
        dataSource.setPassword(sequenceProperties.getDatasource().getPassword());
        dataSource.setDriverClassName(sequenceProperties.getDatasource().getDriverClass());
        dataSource.setMinimumIdle(1);
        dataSource.setMaximumPoolSize(8);
        return dataSource;
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    @ConditionalOnMissingBean({DefaultProperties.class})
    public DefaultProperties initDefaultProperties() {
        return new DefaultProperties();
    }

    @Bean
    public MysqlSequenceGenerator initMysqlSequenceGenerator(@Autowired DefaultProperties defaultProperties) {
        if (sequenceProperties.getSpec() == null) {
            sequenceProperties.setSpec(new HashMap<>());
        }
        if (sequenceProperties.getApplication() == null || sequenceProperties.getApplication().length() == 0) {
            sequenceProperties.setApplication(defaultProperties.getApplication());
        }
        if (sequenceProperties.getStep() <= 0) {
            sequenceProperties.setStep(defaultProperties.getDefaultStep());
        }
        if (sequenceProperties.getCacheNSteps() < 0) {
            sequenceProperties.setCacheNSteps(defaultProperties.getDefaultCacheNum());
        }
        if (sequenceProperties.getLazyMode() == null) {
            sequenceProperties.setLazyMode(defaultProperties.isLazyMode());
        }
        Optional<String> optional = sequenceProperties.getSpec().keySet().stream().filter(s -> !sequenceProperties
                .getNames().contains(s)).findAny();
        if (optional.isPresent()) {
            throw new SequenceConfigException("unrecognized sequence spec of name:" + optional.get());
        }
        Map<String, SequenceSpecConfig> specConfigMap = new HashMap<>();
        sequenceProperties.getSpec().entrySet().forEach(entry -> {
            String[] values = entry.getValue().split(",");
            if (values.length != 2) {
                throw new SequenceConfigException("only two arguments is allowed for spec,but found:" + values
                        .length + " ,sequence name:" + entry.getKey());
            }
            specConfigMap.put(entry.getKey(), new SequenceSpecConfig(Integer.valueOf(values[0]), Integer.valueOf(values[1])));
        });
        MysqlSequenceGenerator mysqlSequenceGenerator =
                new MysqlSequenceGenerator(initDatasource(),
                        sequenceProperties.getApplication(),
                        sequenceProperties.getNames(),
                        sequenceProperties.getStep(),
                        sequenceProperties.getCacheNSteps(),
                        sequenceProperties.getSkip(),
                        sequenceProperties.getLazyMode(),
                        specConfigMap);
        return mysqlSequenceGenerator;

    }

}
