package hm.binkley.junit;

import lombok.RequiredArgsConstructor;
import org.junit.rules.ExternalResource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@code SQLTransactionRule} manages a SQL transaction as a JUnit {@code
 * &#64;Rule}, beginning at each test method and rolling back when the method
 * completes.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation
 */
@RequiredArgsConstructor
public final class SQLTransactionRule
        extends ExternalResource {
    private final Connection conn;

    @Override
    protected void before()
            throws Throwable {
        conn.setAutoCommit(false);
    }

    @Override
    protected void after() {
        try {
            conn.rollback();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
