package hm.binkley.junit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * {@code SQLTransactionRuleTest} tests {@link SQLTransactionRule}.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class SQLTransactionRuleTest {
    @Mock
    private Connection conn;

    private SQLTransactionRule rule;

    @Before
    public void setUp() {
        rule = new SQLTransactionRule(conn);
    }

    @Test
    public void shouldTransact()
            throws Throwable {
        rule.before();

        verify(conn).setAutoCommit(eq(false));
    }

    @Test
    public void shouldRollback()
            throws SQLException {
        rule.after();

        verify(conn).rollback();
    }
}
