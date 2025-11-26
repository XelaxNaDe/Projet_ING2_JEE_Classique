package dao;

import model.Employee;
import model.Payroll;
import model.utils.IntStringPayroll;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.List;

public class PayrollDAO {

    // --- LECTURE ---

    public List<Payroll> getAllPayrolls() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Hibernate charge Employee et les Lignes (allLines) automatiquement
            return session.createQuery("SELECT p FROM Payroll p", Payroll.class).list();
        }
    }

    public List<Payroll> findPayrollByEmployee(Employee e) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Payroll p WHERE p.employee.id = :empId", Payroll.class)
                    .setParameter("empId", e.getId())
                    .list();
        }
    }

    public List<Payroll> findPayrollByPeriod(LocalDate debut, LocalDate fin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Payroll p WHERE p.date BETWEEN :debut AND :fin", Payroll.class)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .list();
        }
    }

    public Payroll findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Payroll.class, id);
        }
    }

    // --- ÉCRITURE (Cascade gère tout) ---

    public void createPayroll(Payroll payroll) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            // Grâce au CascadeType.ALL dans Payroll, ceci sauvegarde aussi les lignes !
            session.persist(payroll);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void updatePayroll(Payroll payroll) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(payroll);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void deletePayroll(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Payroll p = session.get(Payroll.class, id);
            if (p != null) {
                session.remove(p); // Supprime aussi les lignes grâce à orphanRemoval=true
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    // --- GESTION DES LIGNES INDIVIDUELLES (Optionnel avec Cascade) ---
    // Normalement, on manipule l'objet Payroll, on ajoute une ligne, et on save Payroll.
    // Mais voici comment supprimer une ligne spécifique si besoin.

    public void deletePayrollLine(int idLine) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            IntStringPayroll line = session.get(IntStringPayroll.class, idLine);
            if (line != null) {
                // Il faut aussi la retirer de la liste du parent pour que la synchro se fasse bien
                line.getPayroll().getAllLines().remove(line);
                session.remove(line);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
}