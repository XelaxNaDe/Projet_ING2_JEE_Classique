package dao;

import model.Project;
import model.Employee;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjetDAO {

    public List<Project> getAllProjects() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT p FROM Project p LEFT JOIN FETCH p.chefProjet", Project.class).list();
        }
    }

    public int createProject(Project project) {
        Transaction tx = null;
        int id = 0;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Project mergedProject = session.merge(project);

            tx.commit();

            id = mergedProject.getIdProjet();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return id;
    }

    public Project getProjectById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Project.class, id);
        }
    }

    public void updateProject(Project project) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(project);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void deleteProject(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Project p = session.get(Project.class, id);
            if (p != null) session.remove(p);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
        }
    }

    public Map<Employee, String> getAssignedEmployees(int idProjet) {
        Map<Employee, String> team = new HashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> results = session.createNativeQuery(
                            "SELECT e.*, ep.role_dans_projet FROM Employee e " +
                                    "JOIN Employe_Projet ep ON e.id = ep.id " +
                                    "WHERE ep.id_projet = :pid", Object[].class)
                    .setParameter("pid", idProjet)
                    .addEntity("e", Employee.class)
                    .addScalar("role_dans_projet", org.hibernate.type.StandardBasicTypes.STRING)
                    .list();

            for (Object[] row : results) {
                Employee emp = (Employee) row[0];
                String role = (String) row[1];
                team.put(emp, role);
            }
        }
        return team;
    }

    public void assignEmployeeToProject(int idProjet, int idEmploye, String role) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.createNativeQuery("INSERT INTO Employe_Projet (id, id_projet, role_dans_projet) VALUES (:eid, :pid, :role) " +
                            "ON DUPLICATE KEY UPDATE role_dans_projet = :role")
                    .setParameter("eid", idEmploye)
                    .setParameter("pid", idProjet)
                    .setParameter("role", role)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void removeEmployeeFromProject(int idProjet, int idEmploye) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.createNativeQuery("DELETE FROM Employe_Projet WHERE id = :eid AND id_projet = :pid")
                    .setParameter("eid", idEmploye)
                    .setParameter("pid", idProjet)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
        }
    }

    public List<Project> getProjectsByEmployeeId(int idEmploye) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createNativeQuery(
                            "SELECT p.* FROM Projet p JOIN Employe_Projet ep ON p.id_projet = ep.id_projet WHERE ep.id = :eid", Project.class)
                    .setParameter("eid", idEmploye)
                    .list();
        }
    }

    public List<Integer> getEmployeeIdsByProject(int idProjet) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createNativeQuery("SELECT id FROM Employe_Projet WHERE id_projet = :pid", Integer.class)
                    .setParameter("pid", idProjet)
                    .list();
        }
    }
}