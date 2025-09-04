package openTelemetry.products.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductResponse {
    
    private Integer id;
    
    private String title;
    
    private String description;
    
    private BigDecimal price;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime createdAt;
}
