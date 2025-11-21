package controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import dao.PayrollDAO;
import model.Payroll;
import model.utils.IntStringPayroll;
import model.utils.Role;
import model.Employee;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
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
        // Sécurité de base
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/Connexion.jsp");
            return;
        }

        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID manquant");
            return;
        }

        try {
            int idPayroll = Integer.parseInt(idStr);
            Payroll payroll = payrollDAO.findPayrollById(idPayroll);

            if (payroll == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Fiche de paie introuvable");
                return;
            }

            // --- Configuration PDF ---
            resp.setContentType("application/pdf");
            // "attachment" force le téléchargement, "inline" l'ouvre dans le navigateur
            resp.setHeader("Content-Disposition", "attachment; filename=Fiche_Paie_" + payroll.getEmployee().getSname() + "_" + payroll.getDate() + ".pdf");

            // --- Génération du Document ---
            Document document = new Document();
            PdfWriter.getInstance(document, resp.getOutputStream());
            document.open();

            // Polices
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            // Formatters
            NumberFormat curFmt = NumberFormat.getCurrencyInstance(Locale.FRANCE);
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // 1. Titre
            Paragraph title = new Paragraph("BULLETIN DE PAIE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 2. Infos Employé & Société (Tableau 2 colonnes sans bordures)
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);

            // Cellule Gauche (Société)
            PdfPCell cellCompany = new PdfPCell();
            cellCompany.setBorder(Rectangle.NO_BORDER);
            cellCompany.addElement(new Paragraph("ENTREPRISE JAVA CORP", headerFont));
            cellCompany.addElement(new Paragraph("123 Rue du Code", normalFont));
            cellCompany.addElement(new Paragraph("75000 PARIS", normalFont));
            infoTable.addCell(cellCompany);

            // Cellule Droite (Employé)
            PdfPCell cellEmp = new PdfPCell();
            cellEmp.setBorder(Rectangle.NO_BORDER);
            cellEmp.addElement(new Paragraph("Employé : " + payroll.getEmployee().getFname() + " " + payroll.getEmployee().getSname(), headerFont));
            cellEmp.addElement(new Paragraph("Matricule : " + payroll.getEmployeeId(), normalFont));
            cellEmp.addElement(new Paragraph("Poste : " + payroll.getEmployee().getPosition(), normalFont));
            cellEmp.addElement(new Paragraph("Période : " + payroll.getDate().format(dateFmt), normalFont));
            infoTable.addCell(cellEmp);

            document.add(infoTable);
            document.add(new Paragraph("\n")); // Espace

            // 3. Tableau des détails (Lignes de paie)
            PdfPTable table = new PdfPTable(3); // 3 colonnes: Libellé, Type, Montant
            table.setWidthPercentage(100);
            table.setWidths(new float[]{50f, 25f, 25f});

            // En-têtes du tableau
            addTableHeader(table, "Libellé", headerFont);
            addTableHeader(table, "Type", headerFont);
            addTableHeader(table, "Montant", headerFont);

            // Ligne Salaire de base
            table.addCell(new Phrase("Salaire de Base", normalFont));
            table.addCell(new Phrase("Salaire", normalFont));
            table.addCell(new Phrase(curFmt.format(payroll.getSalary()), normalFont));

            // Boucle Primes
            for (IntStringPayroll line : payroll.getBonusesList()) {
                table.addCell(new Phrase(line.getLabel(), normalFont));
                table.addCell(new Phrase("Prime (+)", normalFont));
                table.addCell(new Phrase(curFmt.format(line.getAmount()), normalFont));
            }

            // Boucle Déductions
            for (IntStringPayroll line : payroll.getDeductionsList()) {
                table.addCell(new Phrase(line.getLabel(), normalFont));
                table.addCell(new Phrase("Déduction (-)", normalFont));
                table.addCell(new Phrase("-" + curFmt.format(line.getAmount()), normalFont));
            }

            document.add(table);

            // 4. Totaux
            Paragraph total = new Paragraph("\nNET À PAYER : " + curFmt.format(payroll.getNetPay()), titleFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            Paragraph footer = new Paragraph("\n\nCe bulletin doit être conservé sans limitation de durée.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Erreur lors de la génération du PDF", e);
        }
    }

    private void addTableHeader(PdfPTable table, String title, Font font) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
        header.setBorderWidth(1);
        header.setPhrase(new Phrase(title, font));
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setPadding(5);
        table.addCell(header);
    }
}