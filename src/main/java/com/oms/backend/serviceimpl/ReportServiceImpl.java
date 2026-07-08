package com.oms.backend.serviceimpl;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.oms.backend.entity.OrderItem;
import com.oms.backend.entity.Orders;
import com.oms.backend.repository.OrderItemRepository;
import com.oms.backend.repository.OrdersRepository;
import com.oms.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Filters orders by date range (mandatory) and optionally by customer, product or category.
     * This single method backs the "Reports (Date, Month, Customer, Product, Category wise)"
     * requirement: pass a wider/narrower date range for date/month-wise views, and set the
     * customerId/productId/categoryId filters for the other breakdowns.
     */
    private List<Orders> filterOrders(LocalDate from, LocalDate to, Long customerId, Long productId, Long categoryId) {
        LocalDateTime start = from != null ? from.atStartOfDay() : LocalDateTime.now().minusYears(5);
        LocalDateTime end = to != null ? to.atTime(23, 59, 59) : LocalDateTime.now();

        List<Orders> orders = ordersRepository.findByOrderDateBetween(start, end);

        if (customerId != null) {
            orders = orders.stream().filter(o -> o.getCustomer().getId().equals(customerId)).collect(Collectors.toList());
        }
        if (productId != null || categoryId != null) {
            orders = orders.stream().filter(o -> {
                List<OrderItem> items = orderItemRepository.findByOrderId(o.getId());
                return items.stream().anyMatch(i ->
                        (productId == null || i.getProduct().getId().equals(productId)) &&
                        (categoryId == null || i.getProduct().getCategory().getId().equals(categoryId)));
            }).collect(Collectors.toList());
        }
        return orders;
    }

    @Override
    public byte[] exportOrdersExcel(LocalDate from, LocalDate to, Long customerId, Long productId, Long categoryId) {
        List<Orders> orders = filterOrders(from, to, customerId, productId, categoryId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Orders Report");
            Row header = sheet.createRow(0);
            String[] cols = {"Order Number", "Customer", "Status", "Total", "GST", "Grand Total", "Order Date"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

            int rowIdx = 1;
            for (Orders o : orders) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(o.getOrderNumber());
                row.createCell(1).setCellValue(o.getCustomer().getFullName());
                row.createCell(2).setCellValue(o.getStatus().name());
                row.createCell(3).setCellValue(o.getTotalAmount().doubleValue());
                row.createCell(4).setCellValue(o.getGstAmount().doubleValue());
                row.createCell(5).setCellValue(o.getGrandTotal().doubleValue());
                row.createCell(6).setCellValue(o.getOrderDate().toString());
            }
            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    @Override
    public byte[] exportOrdersPdf(LocalDate from, LocalDate to, Long customerId, Long productId, Long categoryId) {
        List<Orders> orders = filterOrders(from, to, customerId, productId, categoryId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document doc = new Document(pdfDoc)) {

            doc.add(new Paragraph("Orders Report").setBold().setFontSize(16));
            doc.add(new Paragraph("Range: " + (from != null ? from : "N/A") + " to " + (to != null ? to : "N/A")));
            doc.add(new Paragraph(" "));

            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 3, 2, 2, 2}))
                    .useAllAvailableWidth();
            table.addHeaderCell("Order Number").addHeaderCell("Customer").addHeaderCell("Status")
                    .addHeaderCell("Grand Total").addHeaderCell("Date");

            for (Orders o : orders) {
                table.addCell(o.getOrderNumber());
                table.addCell(o.getCustomer().getFullName());
                table.addCell(o.getStatus().name());
                table.addCell(o.getGrandTotal().toString());
                table.addCell(o.getOrderDate().toLocalDate().toString());
            }
            doc.add(table);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
        return baos.toByteArray();
    }
}
