package za.co.ratpack.finance.reactive.rest.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Request object for creating or modifying a currency resource.
 * Contains all required information to represent a currency in the system.
 * 
 * @author markmngoma
 * @created at 01:03 on 23/12/2024
 */
@Data
@Schema(description = "Currency request object containing all required currency information")
public class CurrencyRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = 454483740137181001L;

  @NotNull(message = "Currency id is a required field")
  @Schema(
    description = "Unique numeric identifier for the currency. This should be the ISO 4217 numeric code. " +
                  "For example: USD=840, EUR=978, GBP=826, ZAR=710. " +
                  "This is a required field and must be a positive integer.",
    example = "840",
    format = "int32"
  )
  private Integer currencyId;

  @NotNull(message = "Currency code is a required field")
  @Schema(
    description = "Three-letter ISO 4217 currency code in uppercase. " +
                  "This is a globally recognized standard for currency identification. " +
                  "Examples: USD (US Dollar), EUR (Euro), GBP (British Pound), ZAR (South African Rand). " +
                  "This is a required field and must be exactly 3 characters.",
    example = "USD"
  )
  private String currencyCode;

  @NotNull(message = "Currency name is a required field")
  @Schema(
    description = "Full descriptive name of the currency in English. " +
                  "This is how the currency will be displayed to users. " +
                  "Examples: 'US Dollar', 'Euro', 'British Pound Sterling', 'South African Rand'. " +
                  "This is a required field and should be clear and unambiguous.",
    example = "US Dollar"
  )
  private String currencyName;

  @NotNull(message = "Currency symbol is a required field")
  @Schema(
    description = "The symbol or sign used to represent the currency. " +
                  "This is typically shown before or after amounts in transactions and displays. " +
                  "Examples: '$' (Dollar), '€' (Euro), '£' (Pound), 'R' (Rand). " +
                  "This is a required field and can be 1-3 characters.",
    example = "$"
  )
  private String currencySymbol;

  @NotNull(message = "Currency flag is a required field")
  @Schema(
    description = "Visual representation of the currency's country, typically an emoji flag or country code. " +
                  "This helps users quickly identify the currency's origin. " +
                  "Examples: '🇺🇸' (US), '🇪🇺' (EU), '🇬🇧' (UK), '🇿🇦' (South Africa), or use country codes like 'US', 'EU', 'GB', 'ZA'. " +
                  "This is a required field and aids in visual currency recognition.",
    example = "🇺🇸"
  )
  private String currencyFlag;
}
