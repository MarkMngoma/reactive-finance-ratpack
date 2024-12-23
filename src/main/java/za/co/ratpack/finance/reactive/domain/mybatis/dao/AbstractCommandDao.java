package za.co.ratpack.finance.reactive.domain.mybatis.dao;

import org.apache.ibatis.annotations.Mapper;

/**
 * @author markmngoma
 * @created at 18:51 on 22/12/2024
 */
@Mapper
public interface AbstractCommandDao<T> {

  Long insert(T object);

  Long update(final T object);

  Long merge(final T object);
}
