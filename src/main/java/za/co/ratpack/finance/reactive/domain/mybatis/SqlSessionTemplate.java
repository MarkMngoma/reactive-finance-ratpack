package za.co.ratpack.finance.reactive.domain.mybatis;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A functional, try-with-resources wrapper around the MyBatis {@link SqlSession}.
 *
 * <p>All session lifecycle concerns (open → commit/rollback → close) are handled
 * internally so that callers can focus purely on the mapper operation to execute.
 * Three execution modes are provided:
 * <ul>
 *   <li>{@link #query} — read-only, auto-commit session</li>
 *   <li>{@link #execute} — write session with explicit commit / rollback</li>
 *   <li>{@link #executeBatch} — {@link ExecutorType#BATCH} session that flushes
 *       statements and commits (or rolls back) after processing all entries</li>
 * </ul>
 *
 * @author markmngoma
 */
@Singleton
public class SqlSessionTemplate {

  private static final Logger LOG = LoggerFactory.getLogger(SqlSessionTemplate.class);

  private final SqlSessionFactory sessionFactory;

  @Inject
  public SqlSessionTemplate(SqlSessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  /**
   * Executes a read-only operation inside an auto-commit session that is closed
   * automatically after {@code operation} returns.
   *
   * @param operation a lambda/method-reference that receives the open session and
   *                  returns a result
   * @param <R>       the result type
   * @return the value returned by {@code operation}
   */
  public <R> R query(Function<SqlSession, R> operation) {
    try (SqlSession session = sessionFactory.openSession(true)) {
      return operation.apply(session);
    }
  }

  /**
   * Executes a write operation within an explicit transaction. Commits on success
   * and rolls back on any unchecked exception before re-throwing it.
   *
   * @param operation a lambda/method-reference that receives the open session and
   *                  returns a result
   * @param <R>       the result type
   * @return the value returned by {@code operation}
   */
  public <R> R execute(Function<SqlSession, R> operation) {
    try (SqlSession session = sessionFactory.openSession(false)) {
      try {
        R result = operation.apply(session);
        session.commit();
        return result;
      } catch (RuntimeException ex) {
        session.rollback();
        LOG.error("SqlSessionTemplate#execute rolling back transaction: {}", ex.getMessage());
        throw ex;
      }
    }
  }

  /**
   * Executes a batch write operation within a {@link ExecutorType#BATCH} session.
   *
   * <p>A mapper proxy is obtained from the batch session and {@code mapperOperation}
   * is called once per entry. On success, statements are flushed and the session
   * is committed; on any exception the session is rolled back and the exception
   * re-thrown.
   *
   * @param mapperClass     the MyBatis mapper interface to retrieve from the batch
   *                        session, e.g. {@code CommandCurrencyDao.class}
   * @param batchEntries    the collection of items to persist
   * @param mapperOperation an <em>unbound</em> method reference,
   *                        e.g. {@code CommandCurrencyDao::insert}
   * @param <M>             the mapper type — must extend {@link BatchDao}
   * @param <T>             the entry type
   * @return the list of {@link BatchResult} produced by
   *         {@link SqlSession#flushStatements()}
   */
  public <M extends BatchDao, T> List<BatchResult> executeBatch(
      Class<M> mapperClass,
      Collection<T> batchEntries,
      BiConsumer<M, T> mapperOperation) {
    try (SqlSession session = sessionFactory.openSession(ExecutorType.BATCH, false)) {
      try {
        M mapper = session.getMapper(mapperClass);
        batchEntries.forEach(entry -> mapperOperation.accept(mapper, entry));
        List<BatchResult> results = session.flushStatements();
        session.commit();
        return results;
      } catch (RuntimeException ex) {
        session.rollback();
        LOG.error("SqlSessionTemplate#executeBatch rolling back batch transaction: {}", ex.getMessage());
        throw ex;
      }
    }
  }
}
