package za.co.ratpack.finance.reactive.domain.mybatis;

import org.apache.ibatis.annotations.Flush;
import org.apache.ibatis.executor.BatchResult;

import java.util.List;

/**
 * @author markmngoma
 * @created at 22:12 on 22/12/2024
 */
public interface BatchDao {
  @Flush
  List<BatchResult> flushBatchedStatements();
}
