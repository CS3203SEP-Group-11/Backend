package com.levelup.payment_service.dto.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private UUID id;
    private String title;
    private BigDecimal price;
    private UUID instructorId;
}