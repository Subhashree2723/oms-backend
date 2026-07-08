package com.oms.backend.controller;

import com.oms.backend.entity.Invoice;
import com.oms.backend.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/api/admin/invoices/{orderId}")
    public ResponseEntity<Invoice> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(invoiceService.getByOrderId(orderId));
    }

    @PostMapping("/api/admin/invoices/{orderId}/generate")
    public ResponseEntity<Invoice> generate(@PathVariable Long orderId) {
        return ResponseEntity.ok(invoiceService.generateInvoice(orderId));
    }

    @GetMapping(value = {"/api/admin/invoices/{orderId}/download", "/api/customer/invoices/{orderId}/download"})
    public ResponseEntity<byte[]> download(@PathVariable Long orderId) {
        byte[] pdf = invoiceService.downloadInvoicePdf(orderId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + orderId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
