package hm.binkley.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * A very simple DAO wrapper for using Spring transactions and JDBC template, designed for lambda
 * use.  Example:
 *
 * <pre>
 * SimpleDAO&lt;String&gt; someColumn = new SimpleDAO(transactionManager);
 * String columnValue = someColumn.dao(
 *        (jdbcTemplate, status) -&gt; jdbcTemplate.queryForObject("sql here", String.class);</pre>
 *
 * @author <a href="binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 */
public class SimpleDAO {
    private final DataSourceTransactionManager transactionManager;

    /**
     * Constructs a new {@code SimpleDAO} with the given data source <var>transactionManager</var>.
     *
     * @param transactionManager the transaction manager, never missing
     */
    @Inject
    public SimpleDAO(@Nonnull final DataSourceTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Runs the given <var>dao</var> within Spring transaction.
     *
     * @param dao the dao callback, never missing
     * @param <T> the return type of the callback
     *
     * @return the callback result
     */
    public final <T> T dao(@Nonnull final Dao<T> dao) {
        return dao.using(transactionManager);
    }

    /**
     * The functional interface for {@link #dao(Dao)}.
     *
     * @param <T> the callback return type
     */
    public interface Dao<T> {
        /**
         * Manages the JDBC callback, wrapping it in a Spring transaction.  The JDBC template passed
         * to the callback shares the data source of the transaction manager, and executes within a
         * transaction template.
         *
         * @param transactionManager the transaction manager, never missing
         *
         * @return the callback result
         */
        default T using(@Nonnull final DataSourceTransactionManager transactionManager) {
            return new TransactionTemplate(transactionManager).execute(
                    status -> with(new JdbcTemplate(transactionManager.getDataSource()), status));
        }

        /**
         * Executes the callback, passing in the given <var>jdbcTemplate</var> and transaction
         * <var>status</var>.  This is typically implemented as a lambda.
         *
         * @param jdbcTemplate the JDBC template, never missing
         * @param status the transaction status, never missing
         *
         * @return the callback result
         */
        T with(@Nonnull final JdbcTemplate jdbcTemplate, @Nonnull final TransactionStatus status);
    }
}
