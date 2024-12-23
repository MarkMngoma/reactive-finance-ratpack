package za.co.ratpack.finance.reactive.domain.mybatis.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author markmngoma
 * @created at 00:34 on 23/12/2024
 */
@Data
public abstract class AbstractAuditEntityModel<T extends AbstractAuditEntityModel<T>>  implements Serializable {

  @Serial
  private static final long serialVersionUID = -5306570991559159383L;

  private Long id;

  private Long createdBy = 1L;

  private LocalDateTime createdAt = LocalDateTime.now();

  private Long updatedBy;

  private LocalDateTime updatedAt;

  private boolean archived;

}
