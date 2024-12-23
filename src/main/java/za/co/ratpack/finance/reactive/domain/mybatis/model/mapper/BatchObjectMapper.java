package za.co.ratpack.finance.reactive.domain.mybatis.model.mapper;

import com.google.inject.Inject;
import org.modelmapper.ModelMapper;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author markmngoma
 * @created at 01:12 on 23/12/2024
 */
public class BatchObjectMapper<D, T> implements BiFunction<Collection<T>, Class<D>, List<D>> {

  private final ModelMapper modelMapper;

  @Inject
  public BatchObjectMapper(ModelMapper modelMapper) {
    this.modelMapper = modelMapper;
  }

  @Override
  public List<D> apply(Collection<T> requestEntries, Class<D> dataEntries) {
    return requestEntries.stream().map(entity -> map(entity, dataEntries)).toList();
  }

  public D map(T entity, Class<D> outClass) {
    return modelMapper.map(entity, outClass);
  }
}
