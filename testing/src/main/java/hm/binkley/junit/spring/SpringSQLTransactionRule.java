package hm.binkley.junit.spring;

import hm.binkley.junit.SQLTransactionRule;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static java.lang.String.format;

/**
 * {@code SpringSQLTransactionRule} provides Spring-JDBC convenience for
 * {@link SQLTransactionRule}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 */
public final class SpringSQLTransactionRule
        extends SQLTransactionRule {
    public SpringSQLTransactionRule(final Connection conn) {
        super(conn);
    }

    public SpringSQLTransactionRule(final DataSource dataSource) {
        this(getConnection(dataSource));
    }

    public JdbcTemplate newJdbcTemplate() {
        return new JdbcTemplate(new SingleConnectionDataSource(conn, true));
    }

    private static Connection getConnection(final DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (final SQLException e) {
            throw new CannotGetJdbcConnectionException(
                    format("Cannot get connection from %s: %s", dataSource,
                            e.getMessage()), e);
        }
    }
}
