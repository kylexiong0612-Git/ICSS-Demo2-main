package com.icss.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Policy {
    private String id;
    private String customerId;
    private String name;
    /** Active | Lapsed | Pending */
    private String status;
    private BigDecimal premium;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long createdAt;
}
