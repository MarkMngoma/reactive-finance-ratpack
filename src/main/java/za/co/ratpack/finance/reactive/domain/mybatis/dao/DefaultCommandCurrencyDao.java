package za.co.ratpack.finance.reactive.domain.mybatis.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.ibatis.executor.BatchResult;
import za.co.ratpack.finance.reactive.domain.mybatis.SqlSessionTemplate;
import za.co.ratpack.finance.reactive.domain.mybatis.model.CurrencyEntityModel;

import java.util.List;

/**
 * Session-per-operation implementation of {@link CommandCurrencyDao}.
 *
 * <p>Each method opens its own {@link org.apache.ibatis.session.SqlSession} via
 * {@link SqlSessionTemplate}, commits (or rolls back) and closes it automatically
 * via try-with-resources. This removes the need for mybatis-guice's
 * {@code @Transactional} AOP interceptor.
 *
 * <p><strong>Batch inserts/updates</strong> must <em>not</em> go through this class.
 * Use
 * {@link za.co.ratpack.finance.reactive.domain.mybatis.functions.BatchCommandDomainExecutorFunction}
 * which manages a {@link org.apache.ibatis.session.ExecutorType#BATCH} session
 * across an entire collection of records.
 *
 * @author markmngoma
 */
@Singleton
public class DefaultCommandCurrencyDao implements CommandCurrencyDao {

  private final SqlSessionTemplate sessionTemplate;

  @Inject
  public DefaultCommandCurrencyDao(SqlSessionTemplate sessionTemplate) {
    this.sessionTemplate = sessionTemplate;
  }

  @Override
  public Long insert(CurrencyEntityModel model) {
    return sessionTemplate.execute(session ->
      session.getMapper(CommandCurrencyDao.class).insert(model));
  }

  @Override
  public Long update(CurrencyEntityModel model) {
    return sessionTemplate.execute(session ->
      session.getMapper(CommandCurrencyDao.class).update(model));
  }

  /**
   * Not supported on the per-operation singleton. Batch flushing is handled
   * internally by {@link SqlSessionTemplate#executeBatch} inside
   * {@code BatchCommandDomainExecutorFunction}.
   *
   * @throws UnsupportedOperationException always
   */
  @Override
  public List<BatchResult> flushBatchedStatements() {
    throw new UnsupportedOperationException(
      "flushBatchedStatements() is only valid inside a BATCH session. " +
      "Use BatchCommandDomainExecutorFunction for batch operations.");
  }
}
