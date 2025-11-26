package controller;

import dao.PayrollDAO;
import model.Payroll;
import model.utils.IntStringPayroll;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.PageSize;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.text.Normalizer; // Pour enlever les accents du nom de fichier
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@WebServlet(name = "PrintPayrollServlet", urlPatterns = "/print-payroll")
public class PrintPayrollServlet extends HttpServlet {

    private PayrollDAO payrollDAO;

    @Override
    public void init() {
        payrollDAO = new PayrollDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID manquant.");
            return;
        }

        try {
            int payrollId = Integer.parseInt(idStr);
            Payroll payroll = payrollDAO.findPayrollById(payrollId);

            if (payroll == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Fiche non trouvée.");
                return;
            }

            // --- CONSTRUCTION DU NOM DE FICHIER ---
            // 1. Récupération Nom/Prénom et nettoyage (accents/espaces)
            String nom = cleanString(payroll.getEmployee().getSname()).toUpperCase();
            String prenom = cleanString(payroll.getEmployee().getFname());

            // 2. Formatage Date (Mois-Annee ex: Mars-2024)
            DateTimeFormatter fileDateFmt = DateTimeFormatter.ofPattern("MMMM-yyyy", Locale.FRANCE);
            String dateStr = cleanString(payroll.getDate().format(fileDateFmt));
            // On s'assure que la première lettre est majuscule (mars -> Mars)
            dateStr = dateStr.substring(0, 1).toUpperCase() + dateStr.substring(1);

            String filename = "FicheDePaie_" + nom + "_" + prenom + "_" + dateStr + ".pdf";

            // 3. Configuration réponse
            resp.setContentType("application/pdf");
            // "attachment" force le téléchargement, "inline" l'ouvre dans le navigateur
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            OutputStream out = resp.getOutputStream();
            generatePdf(out, payroll);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur PDF : " + e.getMessage());
        }
    }

    // Petite fonction utilitaire pour nettoyer les chaines (enlever accents et espaces)
    private String cleanString(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "") // Enleve accents
                .replaceAll("\\s+", "-"); // Remplace espaces par tirets
    }

    /**
     * Logique de génération du contenu PDF avec OpenPDF
     */
    private void generatePdf(OutputStream out, Payroll payroll) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        document.open();

        // Formats
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRANCE);

        // --- Styles de Police ---
        Font fontTitle = new Font(Font.HELVETICA, 16, Font.BOLD);
        Font fontHeader = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font fontNormal = new Font(Font.HELVETICA, 11, Font.NORMAL);
        Font fontBonus = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.GREEN);
        Font fontDeduction = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.RED);
        Font fontPay = new Font(Font.HELVETICA, 14, Font.BOLD);

        // --- Titre ---
        Paragraph title = new Paragraph("FICHE DE PAIE N° " + payroll.getId(), fontTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        // --- Section Employé ---

        document.add(new Paragraph("Informations Employé :", fontHeader));

        PdfPTable tableInfo = new PdfPTable(2);
        tableInfo.setWidthPercentage(100);
        tableInfo.setSpacingBefore(10f);
        tableInfo.setSpacingAfter(10f);

        // On accède à l'employé via l'objet Payroll chargé par Hibernate
        String nomComplet = "Inconnu";
        String poste = "Non défini";

        if (payroll.getEmployee() != null) {
            nomComplet = payroll.getEmployee().getFname() + " " + payroll.getEmployee().getSname();
            poste = payroll.getEmployee().getPosition();
        }

        tableInfo.addCell(createCell("Nom Complet:", fontHeader));
        tableInfo.addCell(createCell(nomComplet, fontNormal));

        tableInfo.addCell(createCell("Poste:", fontHeader));
        tableInfo.addCell(createCell(poste, fontNormal));

        tableInfo.addCell(createCell("Date:", fontHeader));
        tableInfo.addCell(createCell(payroll.getDate().format(dateFormatter), fontNormal));

        document.add(tableInfo);

        // --- Salaire de base ---
        Paragraph salaire = new Paragraph(
                "Salaire de base : " + currencyFormat.format(payroll.getSalary()),
                fontPay
        );
        salaire.setAlignment(Element.ALIGN_RIGHT);
        document.add(salaire);

        document.add(new Paragraph("Détails de la Rémunération :", fontHeader));

        PdfPTable tableDetails = new PdfPTable(3);
        tableDetails.setWidthPercentage(100);
        tableDetails.setSpacingBefore(10f);

        tableDetails.addCell(createCell("Rubrique", fontHeader));
        tableDetails.addCell(createCell("Type", fontHeader));
        tableDetails.addCell(createCell("Montant", fontHeader));

        for (IntStringPayroll bonus : payroll.getBonusesList()) {
            tableDetails.addCell(createCell(bonus.getLabel(), fontNormal));
            tableDetails.addCell(createCell("PRIME (+)", fontBonus));
            tableDetails.addCell(createCell(currencyFormat.format(bonus.getAmount()), fontNormal));
        }

        for (IntStringPayroll deduction : payroll.getDeductionsList()) {
            tableDetails.addCell(createCell(deduction.getLabel(), fontNormal));
            tableDetails.addCell(createCell("DÉDUCTION (-)", fontDeduction));
            tableDetails.addCell(createCell(currencyFormat.format(deduction.getAmount()), fontNormal));
        }

        document.add(tableDetails);
        document.add(new Paragraph(" "));

        Paragraph netPay = new Paragraph(
                "Net à payer : " + currencyFormat.format(payroll.getNetPay()),
                fontPay
        );
        netPay.setAlignment(Element.ALIGN_RIGHT);
        document.add(netPay);

        document.close();
    }

    private PdfPCell createCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }
}