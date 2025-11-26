package dao;

import model.Departement;
import model.Employee;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class DepartementDAO {

    public Departement findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // AVANT : return session.get(Departement.class, id);

            // APRES : On force le chargement du chef avec "LEFT JOIN FETCH"
            return session.createQuery(
                            "SELECT d FROM Departement d LEFT JOIN FETCH d.chefDepartement WHERE d.id = :id",
                            Departement.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }

    public List<Departement> getAllDepartments() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Le LEFT JOIN FETCH permet de charger le chef en même temps pour éviter les problèmes de lazy loading
            return session.createQuery("SELECT d FROM Departement d LEFT JOIN FETCH d.chefDepartement", Departement.class).list();
        }
    }

    /**
     * Vérifie si un employé est chef d'un département.
     * @return Le nom du département dirigé, ou null s'il n'est pas chef.
     */
    public String getDepartmentNameIfChef(int employeeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // On cherche s'il existe un département dont le chef a cet ID
            String nomDept = session.createQuery(
                            "SELECT d.nomDepartement FROM Departement d WHERE d.chefDepartement.id = :id", String.class)
                    .setParameter("id", employeeId)
                    .uniqueResult(); // Renvoie null si rien trouvé
            return nomDept;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int createDepartment(String nom, int idChef) {
        Transaction tx = null;
        int newId = 0;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Departement dept = new Departement();
            dept.setNomDepartement(nom);

            if (idChef > 0) {
                Employee chef = session.get(Employee.class, idChef);
                dept.setChefDepartement(chef);
            }

            session.persist(dept);
            tx.commit();
            newId = dept.getId();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return newId;
    }

    public void updateDepartment(int id, String nom, int idChef) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Departement dept = session.get(Departement.class, id);
            if (dept != null) {
                dept.setNomDepartement(nom);

                if (idChef > 0) {
                    Employee chef = session.get(Employee.class, idChef);
                    dept.setChefDepartement(chef);
                } else {
                    dept.setChefDepartement(null);
                }
                session.merge(dept);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void deleteDepartment(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Departement dept = session.get(Departement.class, id);
            if (dept != null) {
                session.remove(dept);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void removeAsChiefFromAnyDepartment(int idChef) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            // HQL Update
            session.createQuery("UPDATE Departement d SET d.chefDepartement = null WHERE d.chefDepartement.id = :idChef")
                    .setParameter("idChef", idChef)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
}