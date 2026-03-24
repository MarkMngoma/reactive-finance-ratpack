package za.co.ratpack.finance.reactive.domain.mybatis.functions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.ibatis.executor.BatchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import za.co.ratpack.finance.reactive.domain.mybatis.BatchDao;
import za.co.ratpack.finance.reactive.domain.mybatis.SqlSessionTemplate;

import java.sql.SQLDataException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * @author markmngoma
 * @created at 00:43 on 23/12/2024
 */
@Singleton
public class BatchCommandDomainExecutorFunction {

  private static final Logger LOG = LoggerFactory.getLogger(BatchCommandDomainExecutorFunction.class);

  private final SqlSessionTemplate sessionTemplate;

  @Inject
  public BatchCommandDomainExecutorFunction(SqlSessionTemplate sessionTemplate) {
    this.sessionTemplate = sessionTemplate;
  }

  /**
   * Executes a batch database command within a programmatic
   * {@link org.apache.ibatis.session.ExecutorType#BATCH} transaction.
   *
   * <p>A dedicated {@link org.apache.ibatis.session.SqlSession} is opened for the
   * entire call; all {@code batchEntries} are processed, statements are flushed and
   * committed (or rolled back on error) before the session is closed
   * automatically via try-with-resources — replacing the previous mybatis-guice
   * {@code @Transactional} AOP interceptor approach.
   *
   * @param mapperClass    the MyBatis mapper interface class,
   *                       e.g. {@code CommandCurrencyDao.class}
   * @param mapperFunction an <em>unbound</em> method reference that writes a
   *                       single entry, e.g. {@code CommandCurrencyDao::insert}
   * @param batchEntries   the collection of entries to persist
   * @param <T>            the entry type
   * @param <M>            the mapper type — must extend {@link BatchDao}
   * @throws SQLDataException if validation fails or an error occurs during execution
   */
  public <T, M extends BatchDao> void executeBatchCommand(
      final Class<M> mapperClass,
      final BiConsumer<M, T> mapperFunction,
      final Collection<T> batchEntries) throws SQLDataException {
    LOG.info("BatchCommandDomainExecutorFunction@executeBatchCommand executed for #{} entries", batchEntries.size());
    try {
      MDC.setContextMap(MDC.getCopyOfContextMap());

      if (mapperFunction == null) {
        throw new SQLDataException("BatchCommandDomainExecutorFunction@executeBatchCommand mapper function not supplied.");
      }
      if (batchEntries == null || batchEntries.isEmpty()) {
        throw new SQLDataException("BatchCommandDomainExecutorFunction@executeBatchCommand no batch entries supplied.");
      }

      StopWatch stopwatch = new StopWatch();

      if (LOG.isInfoEnabled()) {
        stopwatch.start();
      }

      List<BatchResult> results = sessionTemplate.executeBatch(mapperClass, batchEntries, mapperFunction);

      LOG.debug("BatchCommandDomainExecutorFunction@executeBatchCommand batch result :: {}", results);

      if (LOG.isInfoEnabled()) {
        stopwatch.stop();
        LOG.info("[{}] ms and [{}] seconds for BATCH {}", stopwatch.getTime(TimeUnit.MILLISECONDS), stopwatch.getTime(TimeUnit.SECONDS), mapperClass.getSimpleName());
      }

    } catch (SQLDataException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("BatchCommandDomainExecutorFunction@executeBatchCommand call {} failed {}", mapperClass.getSimpleName(), e.getMessage());
      throw new SQLDataException(e.getMessage());
    }
  }
}
