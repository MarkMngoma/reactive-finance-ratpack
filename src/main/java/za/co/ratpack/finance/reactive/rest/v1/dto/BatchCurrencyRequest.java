package za.co.ratpack.finance.reactive.rest.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Request object for creating multiple currencies in a single batch operation.
 * This allows efficient creation of multiple currency resources with a single API call.
 * 
 * @author markmngoma
 * @created at 01:06 on 23/12/2024
 */
@Data
@Schema(description = "Batch request for creating multiple currencies in a single operation")
public class BatchCurrencyRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -5350072431597834004L;

  @NotNull.List(@NotNull(groups=CurrencyRequest.class, message="Currencies are required"))
  @Schema(
    description = "List of currency objects to be created in batch. " +
                  "Each currency must contain all required fields (currencyId, currencyCode, currencyName, currencySymbol, currencyFlag). " +
                  "The batch operation will create all currencies atomically - if one fails, all fail. " +
                  "Minimum of 1 currency is required, and there is typically a maximum limit (e.g., 100) to prevent excessive load. " +
                  "Use this endpoint when you need to create multiple currencies efficiently in a single request.",
    example = "[{\"currencyId\": 840, \"currencyCode\": \"USD\", \"currencyName\": \"US Dollar\", \"currencySymbol\": \"$\", \"currencyFlag\": \"🇺🇸\"}, " +
              "{\"currencyId\": 978, \"currencyCode\": \"EUR\", \"currencyName\": \"Euro\", \"currencySymbol\": \"€\", \"currencyFlag\": \"🇪🇺\"}]"
  )
  private List<CurrencyRequest> batchCurrencies;
}
