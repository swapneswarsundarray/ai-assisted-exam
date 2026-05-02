package com.aentic.exam.controller;

import com.aentic.exam.entity.Report;
import com.aentic.exam.service.ExportService;
import com.aentic.exam.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@Controller
@RequestMapping("/reports")
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    @Autowired
    private ExportService exportService;
    
    @GetMapping("/{examId}")
    public String viewReport(@PathVariable Long examId, Model model) {
        Optional<Report> report = reportService.getReportByExamId(examId);
        if (report.isPresent()) {
            model.addAttribute("report", report.get());
            return "report/view";
        }
        return "redirect:/exams";
    }
    
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Report> getReport(@PathVariable Long id) {
        Optional<Report> report = reportService.getReportById(id);
        return report.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/code/{reportCode}")
    @ResponseBody
    public ResponseEntity<Report> getReportByCode(@PathVariable String reportCode) {
        Optional<Report> report = reportService.getReportByCode(reportCode);
        return report.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/exam/{examId}")
    @ResponseBody
    public ResponseEntity<Report> getReportByExamId(@PathVariable Long examId) {
        Optional<Report> report = reportService.getReportByExamId(examId);
        return report.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/export/pdf/{reportCode}")
    public ResponseEntity<ByteArrayResource> exportToPDF(@PathVariable String reportCode) throws IOException {
        byte[] pdfData = exportService.exportReportToPDF(reportCode);
        
        ByteArrayResource resource = new ByteArrayResource(pdfData);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report_" + reportCode + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfData.length)
                .body(resource);
    }
    
    @GetMapping("/export/json/{reportCode}")
    public ResponseEntity<ByteArrayResource> exportToJSON(@PathVariable String reportCode) throws IOException {
        byte[] jsonData = exportService.exportReportToJSON(reportCode);
        
        ByteArrayResource resource = new ByteArrayResource(jsonData);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report_" + reportCode + ".json")
                .contentType(MediaType.APPLICATION_JSON)
                .contentLength(jsonData.length)
                .body(resource);
    }
    
    @PostMapping("/generate/{examId}")
    @ResponseBody
    public ResponseEntity<Report> generateReport(@PathVariable Long examId) {
        try {
            Report report = reportService.generateReport(
                    reportService.getReportByExamId(examId)
                            .map(r -> r.getExam())
                            .orElseThrow(() -> new RuntimeException("Exam not found"))
            );
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
