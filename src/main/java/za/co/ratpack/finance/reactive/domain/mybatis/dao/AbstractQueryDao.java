package za.co.ratpack.finance.reactive.domain.mybatis.dao;

import java.util.List;
import java.util.Optional;

public interface AbstractQueryDao<T, Q> {

  Optional<T> selectOne(final Q typeObjectQuery);
  List<T> selectQueryable(final Q typeObjectQuery);
  List<T> selectCollective();
}
