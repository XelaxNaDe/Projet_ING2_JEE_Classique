package dao;

import model.Departement;
import model.Employee;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class DepartementDAO {

    public Departement findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT d FROM Departement d LEFT JOIN FETCH d.chefDepartement WHERE d.id = :id",
                            Departement.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }

    public List<Departement> getAllDepartments() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT d FROM Departement d LEFT JOIN FETCH d.chefDepartement", Departement.class).list();
        }
    }

    public String getDepartmentNameIfChef(int employeeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String nomDept = session.createQuery(
                            "SELECT d.nomDepartement FROM Departement d WHERE d.chefDepartement.id = :id", String.class)
                    .setParameter("id", employeeId)
                    .uniqueResult();
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