package com.oms.backend.service;

import com.oms.backend.entity.Invoice;

public interface InvoiceService {
    Invoice generateInvoice(Long orderId);
    Invoice getByOrderId(Long orderId);
    byte[] downloadInvoicePdf(Long orderId);
}
