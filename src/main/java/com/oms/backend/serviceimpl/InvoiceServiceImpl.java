package com.oms.backend.serviceimpl;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.oms.backend.entity.Invoice;
import com.oms.backend.entity.OrderItem;
import com.oms.backend.entity.Orders;
import com.oms.backend.exception.ResourceNotFoundException;
import com.oms.backend.repository.InvoiceRepository;
import com.oms.backend.repository.OrderItemRepository;
import com.oms.backend.repository.OrdersRepository;
import com.oms.backend.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;

    @Value("${app.upload.invoice-dir}")
    private String invoiceDir;

    @Override
    public Invoice generateInvoice(Long orderId) {
        return invoiceRepository.findByOrderId(orderId).orElseGet(() -> createInvoice(orderId));
    }

    private Invoice createInvoice(Long orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        String invoiceNumber = "INV" + order.getOrderNumber().replace("ORD", "");

        byte[] pdfBytes = buildPdf(order, items, invoiceNumber);

        String filePath = null;
        try {
            Files.createDirectories(Paths.get(invoiceDir));
            filePath = invoiceDir + File.separator + invoiceNumber + ".pdf";
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(pdfBytes);
            }
        } catch (IOException e) {
            // if disk write fails, invoice record is still created; PDF can be regenerated on demand
        }

        Invoice invoice = Invoice.builder()
                .order(order)
                .invoiceNumber(invoiceNumber)
                .filePath(filePath)
                .totalAmount(order.getGrandTotal())
                .build();
        return invoiceRepository.save(invoice);
    }

    private byte[] buildPdf(Orders order, List<OrderItem> items, String invoiceNumber) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document doc = new Document(pdfDoc)) {

            doc.add(new Paragraph("Online Product Order Management System").setBold().setFontSize(16));
            doc.add(new Paragraph("Invoice: " + invoiceNumber));
            doc.add(new Paragraph("Order Number: " + order.getOrderNumber()));
            doc.add(new Paragraph("Customer: " + order.getCustomer().getFullName()));
            doc.add(new Paragraph("Date: " + order.getOrderDate()));
            doc.add(new Paragraph(" "));

            Table table = new Table(UnitValue.createPercentArray(new float[]{4, 1, 2, 2, 2}))
                    .useAllAvailableWidth();
            table.addHeaderCell("Product").addHeaderCell("Qty").addHeaderCell("Price")
                    .addHeaderCell("GST").addHeaderCell("Subtotal");

            for (OrderItem item : items) {
                table.addCell(item.getProduct().getName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell(item.getPrice().toString());
                table.addCell(item.getGstAmount().toString());
                table.addCell(item.getSubtotal().toString());
            }
            doc.add(table);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Total (excl. GST): " + order.getTotalAmount()));
            doc.add(new Paragraph("GST Amount: " + order.getGstAmount()));
            doc.add(new Paragraph("Grand Total: " + order.getGrandTotal()).setBold());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
        return baos.toByteArray();
    }

    @Override
    public Invoice getByOrderId(Long orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for order: " + orderId));
    }

    @Override
    public byte[] downloadInvoicePdf(Long orderId) {
        Invoice invoice = getByOrderId(orderId);
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        if (invoice.getFilePath() != null && Files.exists(Paths.get(invoice.getFilePath()))) {
            try {
                return Files.readAllBytes(Paths.get(invoice.getFilePath()));
            } catch (IOException e) {
                // fall through to regenerate
            }
        }
        return buildPdf(order, items, invoice.getInvoiceNumber());
    }
}
