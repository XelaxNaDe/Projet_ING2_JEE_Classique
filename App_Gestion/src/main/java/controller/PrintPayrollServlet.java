package controller;

import dao.PayrollDAO;
import dao.EmployeeDAO;
import model.Payroll;
import model.utils.IntStringPayroll;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Importations OpenPDF (similaires à iText)
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
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@WebServlet(name = "PrintPayrollServlet", urlPatterns = "/print-payroll")
public class PrintPayrollServlet extends HttpServlet {

    private PayrollDAO payrollDAO;
    private EmployeeDAO employeeDAO;

    @Override
    public void init() {
        payrollDAO = new PayrollDAO();
        employeeDAO = new EmployeeDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de fiche de paie manquant.");
            return;
        }

        try {
            int payrollId = Integer.parseInt(idStr);
            // 1. Récupération des données complètes
            Payroll payroll = payrollDAO.findPayrollById(payrollId);

            if (payroll == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Fiche de paie non trouvée.");
                return;
            }

            // Assurez-vous que les détails sont chargés (primes/déductions)
            payroll.setBonusesList(payrollDAO.getAllPayrollsDetails(payrollId, "Prime"));
            payroll.setDeductionsList(payrollDAO.getAllPayrollsDetails(payrollId, "Déduction"));

            // L'employé doit être chargé par le DAO
            payroll.setEmployee(employeeDAO.findEmployeeById(payroll.getEmployeeId()));

            // 2. Configuration de la réponse HTTP pour le PDF
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "attachment; filename=\"FicheDePaie_" + payroll.getId() + ".pdf\"");
            OutputStream out = resp.getOutputStream();

            // 3. Génération du PDF avec OpenPDF
            generatePdf(out, payroll);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID de fiche de paie invalide.");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur de base de données : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors de la génération du PDF : " + e.getMessage());
        }
    }

    /**
     * Logique de génération du contenu PDF en utilisant OpenPDF
     */
    private void generatePdf(OutputStream out, Payroll payroll) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        document.open();

        // Formattage des nombres et dates
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRANCE);

        // --- Styles ---
        Font fontTitle = new Font(Font.HELVETICA, 16, Font.BOLD);
        Font fontHeader = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font fontNormal = new Font(Font.HELVETICA, 11, Font.NORMAL);
        Font fontBonus = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.GREEN);
        Font fontDeduction = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.RED);
        Font Pay = new Font(Font.HELVETICA, 14, Font.BOLD);


        // --- Titre ---
        Paragraph title = new Paragraph("FICHE DE PAIE N° " + payroll.getId(), fontTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" ")); // Saut de ligne


        // --- Section Employé ---
        document.add(new Paragraph("Informations Employé :", fontHeader));

        PdfPTable tableInfo = new PdfPTable(2);
        tableInfo.setWidthPercentage(100);
        tableInfo.setSpacingBefore(10f);
        tableInfo.setSpacingAfter(10f);

        // Entête
        tableInfo.addCell(createCell("Nom Complet:", fontHeader));
        tableInfo.addCell(createCell(payroll.getEmployee().getFname() + " " + payroll.getEmployee().getSname(), fontNormal));

        tableInfo.addCell(createCell("Poste:", fontHeader));
        tableInfo.addCell(createCell(payroll.getEmployee().getPosition(), fontNormal));

        tableInfo.addCell(createCell("Date du Paiement:", fontHeader));
        tableInfo.addCell(createCell(payroll.getDate().format(dateFormatter), fontNormal));

        document.add(tableInfo);

        // --- Net à Payer Final ---
        Paragraph salaire = new Paragraph(
                "Salaire de base : " + currencyFormat.format(payroll.getSalary()),
                Pay
        );
        salaire.setAlignment(Element.ALIGN_RIGHT);
        document.add(salaire);

        // --- Détails Paie (Primes et Déductions) ---
        document.add(new Paragraph("Détails de la Rémunération :", fontHeader));

        PdfPTable tableDetails = new PdfPTable(3);
        tableDetails.setWidthPercentage(100);
        tableDetails.setSpacingBefore(10f);

        tableDetails.addCell(createCell("Rubrique", fontHeader));
        tableDetails.addCell(createCell("Type", fontHeader));
        tableDetails.addCell(createCell("Montant", fontHeader));


        // Primes
        for (IntStringPayroll bonus : payroll.getBonusesList()) {
            tableDetails.addCell(createCell(bonus.getLabel(), fontNormal));
            tableDetails.addCell(createCell("PRIME (+)", fontBonus));
            tableDetails.addCell(createCell(currencyFormat.format(bonus.getAmount()), fontNormal));
        }

        // Déductions
        for (IntStringPayroll deduction : payroll.getDeductionsList()) {
            tableDetails.addCell(createCell(deduction.getLabel(), fontNormal));
            tableDetails.addCell(createCell("DÉDUCTION (-)", fontDeduction));
            tableDetails.addCell(createCell(currencyFormat.format(deduction.getAmount()), fontNormal));
        }

        document.add(tableDetails);
        document.add(new Paragraph(" "));

        // --- Net à Payer Final ---
        Paragraph netPay = new Paragraph(
                "Net à payer : " + currencyFormat.format(payroll.getNetPay()),
                Pay
        );
        netPay.setAlignment(Element.ALIGN_RIGHT);
        document.add(netPay);

        document.close();
    }

    /**
     * Helper pour créer une cellule de tableau OpenPDF
     */
    private PdfPCell createCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }
}