package za.co.ratpack.finance.reactive.domain.mybatis.dao;

import org.apache.ibatis.annotations.Mapper;
import za.co.ratpack.finance.reactive.domain.mybatis.BatchDao;
import za.co.ratpack.finance.reactive.domain.mybatis.model.CurrencyEntityModel;

/**
 * @author markmngoma
 * @created at 00:30 on 23/12/2024
 */
@Mapper
public interface CommandCurrencyDao extends BatchDao {

  Long insert(final CurrencyEntityModel currencyEntityModel);

  Long update(final CurrencyEntityModel currencyEntityModel);
}
