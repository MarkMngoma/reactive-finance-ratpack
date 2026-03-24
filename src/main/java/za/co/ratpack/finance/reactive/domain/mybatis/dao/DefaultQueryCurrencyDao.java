package za.co.ratpack.finance.reactive.domain.mybatis.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import za.co.ratpack.finance.reactive.domain.mybatis.SqlSessionTemplate;
import za.co.ratpack.finance.reactive.domain.mybatis.model.CurrencyEntityModel;
import za.co.ratpack.finance.reactive.domain.mybatis.model.typed.query.CurrencyModelTypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Session-per-operation implementation of {@link QueryCurrencyDao}.
 *
 * <p>Each read opens an auto-commit {@link org.apache.ibatis.session.SqlSession}
 * via {@link SqlSessionTemplate}, fetches the result and closes the session
 * automatically via try-with-resources.
 *
 * @author markmngoma
 */
@Singleton
public class DefaultQueryCurrencyDao implements QueryCurrencyDao {

  private final SqlSessionTemplate sessionTemplate;

  @Inject
  public DefaultQueryCurrencyDao(SqlSessionTemplate sessionTemplate) {
    this.sessionTemplate = sessionTemplate;
  }

  @Override
  public Optional<CurrencyEntityModel> selectOne(CurrencyModelTypedQuery query) {
    return sessionTemplate.query(session ->
      session.getMapper(QueryCurrencyDao.class).selectOne(query));
  }

  @Override
  public List<CurrencyEntityModel> selectQueryable(CurrencyModelTypedQuery query) {
    return sessionTemplate.query(session ->
      session.getMapper(QueryCurrencyDao.class).selectQueryable(query));
  }

  @Override
  public List<CurrencyEntityModel> select() {
    return sessionTemplate.query(session ->
      session.getMapper(QueryCurrencyDao.class).select());
  }
}
