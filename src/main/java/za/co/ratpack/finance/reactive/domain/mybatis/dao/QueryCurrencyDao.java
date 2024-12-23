package za.co.ratpack.finance.reactive.domain.mybatis.dao;

import org.apache.ibatis.annotations.Mapper;
import za.co.ratpack.finance.reactive.domain.mybatis.model.CurrencyEntityModel;
import za.co.ratpack.finance.reactive.domain.mybatis.model.typed.query.CurrencyModelTypedQuery;

import java.util.List;
import java.util.Optional;

@Mapper
public interface QueryCurrencyDao {
  Optional<CurrencyEntityModel> selectOne(final CurrencyModelTypedQuery currencyModelTypedQuery);
  List<CurrencyEntityModel> selectQueryable(final CurrencyModelTypedQuery currencyModelTypedQuery);
  List<CurrencyEntityModel> select();
}
