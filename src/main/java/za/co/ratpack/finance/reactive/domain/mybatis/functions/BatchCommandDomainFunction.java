package za.co.ratpack.finance.reactive.domain.mybatis.functions;

import za.co.ratpack.finance.reactive.domain.mybatis.BatchDao;

import java.util.List;

/**
 * @author markmngoma
 * @created at 18:51 on 22/12/2024
 */
public interface BatchCommandDomainFunction<T> extends BatchDao {

  List<Long> batchInsert(final List<T> object);

}
