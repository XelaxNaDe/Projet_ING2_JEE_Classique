package dao;

import org.hibernate.Session;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsDAO {

    // Helper pour les compteurs simples (Label -> Nombre)
    public Map<String, Number> getCountMap(String sql) {
        Map<String, Number> results = new HashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createNativeQuery(sql, Object[].class).list();
            for (Object[] row : rows) {
                String key = (row[0] != null) ? row[0].toString() : "Non défini";
                Number value = (Number) row[1];
                results.put(key, value);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return results;
    }

    // NOUVEAU : Helper pour récupérer des listes détaillées (plusieurs colonnes)
    public List<Object[]> getListData(String sql) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createNativeQuery(sql, Object[].class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- STATISTIQUES RH ---

    public Map<String, Number> getEmployeesPerDepartment() {
        return getCountMap("SELECT d.nom_departement, COUNT(e.id) FROM Departement d LEFT JOIN Employee e ON d.id_departement = e.id_departement GROUP BY d.id_departement, d.nom_departement");
    }

    public Map<String, Number> getEmployeesByPosition() {
        return getCountMap("SELECT position, COUNT(id) FROM Employee WHERE position IS NOT NULL GROUP BY position");
    }

    // NOUVEAU : Répartition Homme/Femme
    public Map<String, Number> getGenderDistribution() {
        return getCountMap("SELECT gender, COUNT(id) FROM Employee GROUP BY gender");
    }

    // --- STATISTIQUES PROJETS ---

    // NOUVEAU : Détails complets des projets (Nom, Chef, Dates, Etat)
    public List<Object[]> getProjectDetails() {
        String sql = "SELECT p.nom_projet, e.fname, e.sname, p.date_debut, p.date_fin, p.etat " +
                "FROM Projet p " +
                "LEFT JOIN Employee e ON p.id_chef_projet = e.id " +
                "ORDER BY p.etat, p.date_fin";
        return getListData(sql);
    }
}