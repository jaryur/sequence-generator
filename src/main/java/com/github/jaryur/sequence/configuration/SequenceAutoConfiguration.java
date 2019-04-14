package com.github.jaryur.sequence.configuration;

import com.github.jaryur.sequence.MysqlSequenceGenerator;
import com.github.jaryur.sequence.exception.SequenceConfigException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

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

    @Bean(name = "sequenceDatasource")
    public DataSource initDatasource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(sequenceProperties.getDatasource().getUrl());
        dataSource.setUsername(sequenceProperties.getDatasource().getUsername());
        dataSource.setPassword(sequenceProperties.getDatasource().getPassword());
        dataSource.setDriverClassName(sequenceProperties.getDatasource().getDriverClass());
        return dataSource;
    }

    @Bean
    public MysqlSequenceGenerator initMysqlSequenceGenerator() {
        if (sequenceProperties.getSpec() == null) {
            sequenceProperties.setSpec(new HashMap<>());
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
                        specConfigMap);
        return mysqlSequenceGenerator;

    }

}
