package com.bloodbridge.service;

import com.bloodbridge.exception.ResourceNotFoundException;
import com.bloodbridge.model.Donation;
import com.bloodbridge.repository.DonationRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final DonationRepository donationRepository;

    public byte[] generateCertificate(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found: " + donationId));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, new BaseColor(180, 0, 0));
            Paragraph title = new Paragraph("Blood Donation Certificate", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Subtitle
            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.ITALIC, BaseColor.DARK_GRAY);
            Paragraph subtitle = new Paragraph("BloodBridge Platform", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            document.add(new Paragraph(" "));
            document.add(new LineSeparator());
            document.add(new Paragraph(" "));

            // Certificate body
            Font bodyFont = new Font(Font.FontFamily.HELVETICA, 13, Font.NORMAL);
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            document.add(new Paragraph("This is to certify that", bodyFont));
            document.add(new Paragraph(" "));

            Font nameFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(180, 0, 0));
            Paragraph donorName = new Paragraph(donation.getDonor().getUser().getName(), nameFont);
            donorName.setAlignment(Element.ALIGN_CENTER);
            document.add(donorName);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("has successfully donated blood with the following details:", bodyFont));
            document.add(new Paragraph(" "));

            // Details table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(80);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);

            addTableRow(table, "Certificate ID:", donation.getCertificateId(), boldFont, bodyFont);
            addTableRow(table, "Blood Group:", donation.getBloodGroup().getDisplay(), boldFont, bodyFont);
            addTableRow(table, "Units Donated:", donation.getUnitsDonated() + " Unit(s)", boldFont, bodyFont);
            addTableRow(table, "Donation Date:", donation.getDonationDate().format(fmt), boldFont, bodyFont);
            addTableRow(table, "Blood Bank:", donation.getBloodBank().getName(), boldFont, bodyFont);
            addTableRow(table, "Next Eligible Date:", donation.getNextEligibleDate().format(fmt), boldFont, bodyFont);
            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new LineSeparator());

            Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
            Paragraph footer = new Paragraph(
                "Thank you for your selfless act of saving lives. Your donation matters!", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate certificate", e);
        }
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
