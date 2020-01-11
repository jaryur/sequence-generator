package com.github.jaryur.sequence.configuration;

import com.github.jaryur.sequence.support.SequenceConstants;
import com.mysql.jdbc.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Created by Jaryur
 *
 * Comment:
 */
@Configuration
@ConfigurationProperties(prefix = "sequence")
public class SequenceProperties {

    private String application;

    private DatasourceConfig datasource;

    /**
     * sequence names
     */
    private List<String> names;

    private Boolean lazyMode;

    /**
     * global step
     */
    private int step = -1;


    /**
     * global cacheNSteps
     */
    private int cacheNSteps = -1;

    /**
     * skip N every time
     */
    private int skip = 0;

    /**
     * specific step and cacheNSteps config
     */
    private Map<String, String> spec;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public DatasourceConfig getDatasource() {
        return datasource;
    }

    public void setDatasource(DatasourceConfig datasource) {
        this.datasource = datasource;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getCacheNSteps() {
        return cacheNSteps;
    }

    public void setCacheNSteps(int cacheNSteps) {
        this.cacheNSteps = cacheNSteps;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getSkip() {
        return skip;
    }

    public Map<String, String> getSpec() {
        return spec;
    }

    public void setSpec(Map<String, String> spec) {
        this.spec = spec;
    }

    public Boolean getLazyMode() {
        return lazyMode;
    }

    public void setLazyMode(Boolean lazyMode) {
        this.lazyMode = lazyMode;
    }

    public static class DatasourceConfig {
        private String url;

        private String username;

        private String password;

        private String driverClass = "com.mysql.jdbc.Driver";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriverClass() {
            return driverClass;
        }

        public void setDriverClass(String driverClass) {
            this.driverClass = driverClass;
        }
    }
}
