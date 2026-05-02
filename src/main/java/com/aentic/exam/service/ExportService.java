package com.aentic.exam.service;

import com.aentic.exam.entity.Report;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {
    
    @Autowired
    private ReportService reportService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public byte[] exportReportToPDF(String reportCode) throws IOException {
        Report report = reportService.getReportByCode(reportCode)
                .orElseThrow(() -> new RuntimeException("Report not found with code: " + reportCode));
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        
        document.add(new Paragraph("Exam Report")
                .setFont(font)
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
        
        document.add(new Paragraph("\n"));
        
        Table infoTable = new Table(2);
        infoTable.addCell("Report Code:");
        infoTable.addCell(report.getReportCode());
        infoTable.addCell("Generated At:");
        infoTable.addCell(report.getGeneratedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        infoTable.addCell("Student:");
        infoTable.addCell(report.getExam().getStudent().getFullName());
        infoTable.addCell("Topic:");
        infoTable.addCell(report.getExam().getTopic().getName());
        infoTable.addCell("Exam Code:");
        infoTable.addCell(report.getExam().getExamCode());
        
        document.add(infoTable);
        document.add(new Paragraph("\n"));
        
        Table resultsTable = new Table(2);
        resultsTable.addCell("Total Questions:");
        resultsTable.addCell(report.getTotalQuestions().toString());
        resultsTable.addCell("Correct Answers:");
        resultsTable.addCell(report.getCorrectAnswers().toString());
        resultsTable.addCell("Wrong Answers:");
        resultsTable.addCell(report.getWrongAnswers().toString());
        resultsTable.addCell("Score:");
        resultsTable.addCell(report.getExam().getTotalScore() + "/" + report.getExam().getMaxScore());
        resultsTable.addCell("Percentage:");
        resultsTable.addCell(String.format("%.1f%%", report.getExam().getPercentage()));
        resultsTable.addCell("Grade:");
        resultsTable.addCell(report.getExam().getGrade() != null ? report.getExam().getGrade() : "N/A");
        
        document.add(resultsTable);
        document.add(new Paragraph("\n"));
        
        if (report.getTotalTimeMinutes() != null) {
            Table timeTable = new Table(2);
            timeTable.addCell("Total Time:");
            timeTable.addCell(report.getTotalTimeMinutes() + " minutes");
            if (report.getAverageTimePerQuestion() != null) {
                timeTable.addCell("Avg Time per Question:");
                timeTable.addCell(String.format("%.1f seconds", report.getAverageTimePerQuestion() * 60));
            }
            document.add(timeTable);
            document.add(new Paragraph("\n"));
        }
        
        document.add(new Paragraph("Strength Areas:")
                .setFont(font)
                .setBold());
        document.add(new Paragraph(report.getStrengthAreas()));
        
        document.add(new Paragraph("\nImprovement Areas:")
                .setFont(font)
                .setBold());
        document.add(new Paragraph(report.getImprovementAreas()));
        
        document.add(new Paragraph("\nDetailed Analysis:")
                .setFont(font)
                .setBold());
        document.add(new Paragraph(report.getDetailedAnalysis()));
        
        document.close();
        
        return outputStream.toByteArray();
    }
    
    public byte[] exportReportToJSON(String reportCode) throws IOException {
        Report report = reportService.getReportByCode(reportCode)
                .orElseThrow(() -> new RuntimeException("Report not found with code: " + reportCode));
        
        return objectMapper.writeValueAsBytes(report);
    }
    
    public byte[] exportStudentResultsToJSON(Long studentId) throws IOException {
        List<Report> reports = getReportsByStudentId(studentId);
        return objectMapper.writeValueAsBytes(reports);
    }
    
    private List<Report> getReportsByStudentId(Long studentId) {
        return java.util.Collections.emptyList();
    }
}
