package com.github.jaryur.sequence;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Jaryur
 *
 * Comment:
 */
public class SequenceDatasource {

    private DataSource dataSource;

    private List<String> sequenceNames;

    public SequenceDatasource(DataSource dataSource, List<String> sequenceNames) {
        this.dataSource = dataSource;
        this.sequenceNames = sequenceNames;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public List<String> getSequenceNames() {
        return sequenceNames;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
