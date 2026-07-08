package com.oms.backend.service;

import java.time.LocalDate;

public interface ReportService {
    byte[] exportOrdersExcel(LocalDate from, LocalDate to, Long customerId, Long productId, Long categoryId);
    byte[] exportOrdersPdf(LocalDate from, LocalDate to, Long customerId, Long productId, Long categoryId);
}
