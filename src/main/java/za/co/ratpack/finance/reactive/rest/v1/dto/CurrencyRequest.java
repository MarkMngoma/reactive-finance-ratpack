package za.co.ratpack.finance.reactive.rest.v1.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author markmngoma
 * @created at 01:03 on 23/12/2024
 */
@Data
public class CurrencyRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = 454483740137181001L;

  @NotNull(message = "Currency id is a required field")
  private Integer currencyId;

  @NotNull(message = "Currency code is a required field")
  private String currencyCode;

  @NotNull(message = "Currency name is a required field")
  private String currencyName;

  @NotNull(message = "Currency symbol is a required field")
  private String currencySymbol;

  @NotNull(message = "Currency flag is a required field")
  private String currencyFlag;
}
