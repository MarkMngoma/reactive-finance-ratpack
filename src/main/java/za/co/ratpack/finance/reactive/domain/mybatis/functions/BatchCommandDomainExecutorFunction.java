package za.co.ratpack.finance.reactive.domain.mybatis.functions;

import com.google.inject.Singleton;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.ExecutorType;
import org.mybatis.guice.transactional.Isolation;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import za.co.ratpack.finance.reactive.domain.mybatis.BatchDao;

import java.sql.SQLDataException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author markmngoma
 * @created at 00:43 on 23/12/2024
 */
@Singleton
public class BatchCommandDomainExecutorFunction {

  private static final Logger LOG = LoggerFactory.getLogger(BatchCommandDomainExecutorFunction.class);

  @Transactional(executorType = ExecutorType.BATCH, isolation = Isolation.READ_UNCOMMITTED)
  public <T, R extends BatchDao> void batchInsertCommand(final Function<T, ?> mapperFunction, final R mapperInstance, final Collection<T> batchEntries) throws SQLDataException {
    LOG.info("BatchCommandDomainExecutorFunction@insertBatch executed for #{} entries", batchEntries.size());
    try {
        MDC.setContextMap(MDC.getCopyOfContextMap());

        if (mapperFunction == null) {
          throw new SQLDataException("PreparedStatement.batchFunctionalFlush failed to pull mapper function.");
        }
        if (mapperInstance == null) {
          throw new SQLDataException("PreparedStatement.batchFunctionalFlush failed to locate mapper instance.");
        }

        int counter = 0;
        StopWatch stopwatch = new StopWatch();

        if (LOG.isInfoEnabled()) {
          stopwatch.start();
        }

        for (T entry : batchEntries) {
          mapperFunction.apply(entry);

          if (LOG.isDebugEnabled() && ++counter % 100 == 0) {
            LOG.debug("{} records processed for {}", counter, mapperFunction.toString());
          }
        }

        LOG.debug("{} records processed for {}", counter, mapperFunction.toString());
        List<BatchResult> results = mapperInstance.flushBatchedStatements();

        if (LOG.isInfoEnabled()) {
          stopwatch.stop();
          LOG.info("[{}] ms and [{}] seconds for BATCH {}",
            stopwatch.getTime(TimeUnit.MILLISECONDS),
            stopwatch.getTime(TimeUnit.SECONDS),
            mapperFunction.toString());
        }

      } catch (Exception e) {
        LOG.error("Batch call {} failed {}", mapperFunction, e.getMessage());
        throw new SQLDataException(e.getMessage());
      }
  }

}