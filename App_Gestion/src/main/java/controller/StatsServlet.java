package controller;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import dao.StatsDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Employee;
import model.utils.RoleEnum;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@WebServlet(name = "StatsServlet", urlPatterns = "/stats")
public class StatsServlet extends HttpServlet {

    private StatsDAO statsDAO;

    @Override
    public void init() {
        statsDAO = new StatsDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Employee user = (session != null) ? (Employee) session.getAttribute("currentUser") : null;

        if (user == null || !user.hasRole(RoleEnum.ADMINISTRATOR)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "attachment; filename=\"Rapport_Activite_RH.pdf\"");

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, resp.getOutputStream());
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(0, 51, 102));
            Paragraph title = new Paragraph("RAPPORT D'ACTIVITÉ RH & PROJETS", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY);
            Paragraph subTitle = new Paragraph("Généré le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), subTitleFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);
            document.add(new Paragraph(" "));
            addProjectDetailsTable(document, statsDAO.getProjectDetails());
            document.add(new Paragraph(" "));
            PdfPTable layoutTable = new PdfPTable(2);
            layoutTable.setWidthPercentage(100);
            layoutTable.setWidths(new float[]{1, 1});

            PdfPCell cellDept = new PdfPCell();
            cellDept.setBorder(Rectangle.NO_BORDER);
            addSimpleSection(cellDept, "Effectifs par Département", statsDAO.getEmployeesPerDepartment());
            layoutTable.addCell(cellDept);

            PdfPCell cellPoste = new PdfPCell();
            cellPoste.setBorder(Rectangle.NO_BORDER);
            cellPoste.setPaddingLeft(10);
            addSimpleSection(cellPoste, "Répartition par Poste", statsDAO.getEmployeesByPosition());
            layoutTable.addCell(cellPoste);

            document.add(layoutTable);

            document.add(new Paragraph(" "));

            PdfPTable genderTable = new PdfPTable(1);
            genderTable.setWidthPercentage(50);
            genderTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            PdfPCell cellGender = new PdfPCell();
            cellGender.setBorder(Rectangle.NO_BORDER);
            addSimpleSection(cellGender, "Parité (Genre)", statsDAO.getGenderDistribution());
            genderTable.addCell(cellGender);
            document.add(genderTable);

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addProjectDetailsTable(Document doc, List<Object[]> projects) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph pTitle = new Paragraph("État d'avancement des Projets", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(0, 51, 102)));
        pTitle.setSpacingAfter(10);
        doc.add(pTitle);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 2, 1.5f, 1.5f, 1.5f});

        String[] headers = {"Nom du Projet", "Chef de Projet", "Début", "Fin Prévue", "Statut"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(new Color(0, 51, 102));
            cell.setPadding(5);
            table.addCell(cell);
        }

        if (projects != null) {
            for (Object[] row : projects) {
                String nomProjet = (String) row[0];
                String chefNom = (row[1] != null) ? row[1] + " " + row[2] : "Non assigné";
                String dateDebut = (row[3] != null) ? row[3].toString() : "-";
                String dateFin = (row[4] != null) ? row[4].toString() : "-";
                String etat = (String) row[5];

                table.addCell(new Phrase(nomProjet, contentFont));
                table.addCell(new Phrase(chefNom, contentFont));
                table.addCell(new Phrase(dateDebut, contentFont));
                table.addCell(new Phrase(dateFin, contentFont));

                Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
                PdfPCell statusCell = new PdfPCell(new Phrase(etat, statusFont));
                statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                if ("En cours".equalsIgnoreCase(etat)) {
                    statusCell.setBackgroundColor(new Color(23, 162, 184));
                } else if ("Terminé".equalsIgnoreCase(etat)) {
                    statusCell.setBackgroundColor(new Color(40, 167, 69));
                } else if ("Annulé".equalsIgnoreCase(etat)) {
                    statusCell.setBackgroundColor(new Color(220, 53, 69));
                } else {
                    statusCell.setBackgroundColor(Color.GRAY);
                }
                table.addCell(statusCell);
            }
        }
        doc.add(table);
    }

    private void addSimpleSection(PdfPCell container, String title, Map<String, Number> data) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

        Paragraph pTitle = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
        pTitle.setSpacingAfter(5);
        container.addElement(pTitle);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(95);
        table.setWidths(new float[]{3, 1});

        PdfPCell c1 = new PdfPCell(new Phrase("Catégorie", headerFont));
        c1.setBackgroundColor(Color.LIGHT_GRAY);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase("Nombre", headerFont));
        c2.setBackgroundColor(Color.LIGHT_GRAY);
        table.addCell(c2);

        for (Map.Entry<String, Number> entry : data.entrySet()) {
            table.addCell(new Phrase(entry.getKey()));
            table.addCell(new Phrase(entry.getValue().toString()));
        }
        container.addElement(table);
    }
}