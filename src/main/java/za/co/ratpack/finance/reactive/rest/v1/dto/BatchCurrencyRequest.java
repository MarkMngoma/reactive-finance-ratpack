package za.co.ratpack.finance.reactive.rest.v1.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author markmngoma
 * @created at 01:06 on 23/12/2024
 */
@Data
public class BatchCurrencyRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -5350072431597834004L;

  @NotNull.List(@NotNull(groups=CurrencyRequest.class, message="Currencies are required"))
  private List<CurrencyRequest> batchCurrencies;
}
