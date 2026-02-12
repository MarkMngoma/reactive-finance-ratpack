package za.co.ratpack.finance.reactive.domain.mybatis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Currency entity model representing a stored currency in the system with audit information")
public class CurrencyEntityModel extends AbstractAuditEntityModel<CurrencyEntityModel> {

  @Serial
  private static final long serialVersionUID = 7974958210026859560L;
  
  @Schema(
    description = "Unique numeric identifier for the currency. This is the ISO 4217 numeric code. " +
                  "For example: USD=840, EUR=978, GBP=826, ZAR=710.",
    example = "840",
    format = "int32"
  )
  private Integer currencyId;
  
  @Schema(
    description = "Three-letter ISO 4217 currency code in uppercase. " +
                  "Examples: USD (US Dollar), EUR (Euro), GBP (British Pound), ZAR (South African Rand).",
    example = "USD"
  )
  private String currencyCode;
  
  @Schema(
    description = "Full descriptive name of the currency in English. " +
                  "Examples: 'US Dollar', 'Euro', 'British Pound Sterling', 'South African Rand'.",
    example = "US Dollar"
  )
  private String currencyName;
  
  @Schema(
    description = "The symbol or sign used to represent the currency in transactions and displays. " +
                  "Examples: '$' (Dollar), '€' (Euro), '£' (Pound), 'R' (Rand).",
    example = "$"
  )
  private String currencySymbol;
  
  @Schema(
    description = "Visual representation of the currency's country, typically an emoji flag or country code. " +
                  "Examples: '🇺🇸' (US), '🇪🇺' (EU), '🇬🇧' (UK), '🇿🇦' (South Africa), or country codes like 'US', 'EU'.",
    example = "🇺🇸"
  )
  private String currencyFlag;
}
