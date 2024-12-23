package za.co.ratpack.finance.reactive.domain.mybatis.model.typed.query;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author markmngoma
 * @created at 00:38 on 23/12/2024
 */
@Data
@NoArgsConstructor
public class CurrencyModelTypedQuery implements Serializable {
  @Serial
  private static final long serialVersionUID = 3272455595895391360L;

  private String currencyCode;
  private int currencyId;

  public CurrencyModelTypedQuery(String currencyCode) {
    this.currencyCode = currencyCode;
  }
}
