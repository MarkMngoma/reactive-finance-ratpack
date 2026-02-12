package za.co.ratpack.finance.reactive.domain.mybatis.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author markmngoma
 * @created at 00:34 on 23/12/2024
 */
@Data
@Schema(description = "Abstract base class for entity models with audit tracking fields")
public abstract class AbstractAuditEntityModel<T extends AbstractAuditEntityModel<T>>  implements Serializable {

  @Serial
  private static final long serialVersionUID = -5306570991559159383L;

  @Schema(
    description = "Unique identifier for the entity. Auto-generated primary key.",
    example = "1",
    format = "int64"
  )
  private Long id;

  @Schema(
    description = "User ID who created this record. Defaults to 1 for system.",
    example = "1",
    format = "int64"
  )
  private Long createdBy = 1L;

  @Schema(
    description = "Timestamp when this record was created. Auto-populated on creation.",
    example = "2024-12-23T00:34:00",
    format = "date-time"
  )
  private LocalDateTime createdAt = LocalDateTime.now();

  @Schema(
    description = "User ID who last updated this record.",
    example = "1",
    format = "int64"
  )
  private Long updatedBy;

  @Schema(
    description = "Timestamp when this record was last updated.",
    example = "2024-12-24T10:15:30",
    format = "date-time"
  )
  private LocalDateTime updatedAt;

  @Schema(
    description = "Soft delete flag. When true, the record is considered deleted but not removed from database.",
    example = "false"
  )
  private boolean archived;

}
