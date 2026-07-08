package com.oms.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType; // ADD, REMOVE, ADJUST

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "previous_qty", nullable = false)
    private Integer previousQty;

    @Column(name = "new_qty", nullable = false)
    private Integer newQty;

    @Column(length = 255)
    private String remarks;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
