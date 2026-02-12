package za.co.ratpack.finance.reactive.rest.v1.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Mock DTO for testing future-proof OpenAPI schema introspection.
 * This class was NOT hardcoded in the OpenApiSpecWriter but should be 
 * automatically discovered and matched by the ClasspathSchemaRegistry.
 * 
 * @author markmngoma
 */
@Data
public class TransactionRequest implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private String transactionId;
    private String accountNumber;
    private Double amount;
    private String currency;
    private String description;
    private String transactionType;
}
