package za.co.ratpack.finance.reactive.domain.mybatis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * @author markmngoma
 * @created at 00:32 on 23/12/2024
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({"id", "currencyId", "currencyCode", "currencyName", "currencySymbol", "currencyFlag"})
@JsonIgnoreProperties(value = { "archived", "createdBy", "updatedAt", "updatedBy" }, ignoreUnknown = true)
public class CurrencyEntityModel extends AbstractAuditEntityModel<CurrencyEntityModel> {

  @Serial
  private static final long serialVersionUID = 7974958210026859560L;
  private Integer currencyId;
  private String currencyCode;
  private String currencyName;
  private String currencySymbol;
  private String currencyFlag;
}
