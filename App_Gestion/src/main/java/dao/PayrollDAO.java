package dao;

import model.Payroll;
import model.utils.IntStringPayroll;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;

public class PayrollDAO {

    public List<Payroll> getAllPayrolls() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT p FROM Payroll p " +
                            "LEFT JOIN FETCH p.employee " +
                            "LEFT JOIN FETCH p.allDetails", Payroll.class).list();
        }
    }

    public List<Payroll> findPayrollByEmployee(int employeeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT p FROM Payroll p " +
                                    "LEFT JOIN FETCH p.employee " +
                                    "LEFT JOIN FETCH p.allDetails " +
                                    "WHERE p.employee.id = :eid", Payroll.class)
                    .setParameter("eid", employeeId)
                    .list();
        }
    }

    public Payroll findPayrollById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT p FROM Payroll p " +
                                    "LEFT JOIN FETCH p.employee " +
                                    "LEFT JOIN FETCH p.allDetails " +
                                    "WHERE p.id = :pid", Payroll.class)
                    .setParameter("pid", id)
                    .uniqueResult();
        }
    }

    public List<Payroll> searchPayrolls(String employeeIdStr, String dateDebut, String dateFin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT DISTINCT p FROM Payroll p LEFT JOIN FETCH p.employee LEFT JOIN FETCH p.allDetails WHERE 1=1 ");

            if (employeeIdStr != null && !employeeIdStr.isEmpty()) {
                hql.append("AND p.employee.id = :eid ");
            }
            if (dateDebut != null && !dateDebut.isEmpty()) {
                hql.append("AND p.date >= :dStart ");
            }
            if (dateFin != null && !dateFin.isEmpty()) {
                hql.append("AND p.date <= :dEnd ");
            }

            Query<Payroll> query = session.createQuery(hql.toString(), Payroll.class);

            if (employeeIdStr != null && !employeeIdStr.isEmpty()) {
                query.setParameter("eid", Integer.parseInt(employeeIdStr));
            }
            if (dateDebut != null && !dateDebut.isEmpty()) {
                query.setParameter("dStart", LocalDate.parse(dateDebut));
            }
            if (dateFin != null && !dateFin.isEmpty()) {
                query.setParameter("dEnd", LocalDate.parse(dateFin));
            }

            return query.list();
        }
    }

    public int createPayroll(Payroll payroll) {
        Transaction tx = null;
        int id = 0;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(payroll);
            tx.commit();
            id = payroll.getId();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return id;
    }

    public List<Payroll> searchPayrolls(String employeeIdStr, String monthStr) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT DISTINCT p FROM Payroll p LEFT JOIN FETCH p.employee LEFT JOIN FETCH p.allDetails WHERE 1=1 ");

            if (employeeIdStr != null && !employeeIdStr.isEmpty()) {
                hql.append("AND p.employee.id = :eid ");
            }

            if (monthStr != null && !monthStr.isEmpty()) {
                hql.append("AND YEAR(p.date) = :year AND MONTH(p.date) = :month ");
            }

            hql.append("ORDER BY p.date DESC");

            Query<Payroll> query = session.createQuery(hql.toString(), Payroll.class);

            if (employeeIdStr != null && !employeeIdStr.isEmpty()) {
                query.setParameter("eid", Integer.parseInt(employeeIdStr));
            }

            if (monthStr != null && !monthStr.isEmpty()) {
                String[] parts = monthStr.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                query.setParameter("year", year);
                query.setParameter("month", month);
            }

            return query.list();
        }
    }

    public void updatePayroll(Payroll payrollData, List<IntStringPayroll> newLines) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Payroll existingPayroll = session.get(Payroll.class, payrollData.getId());

            if (existingPayroll != null) {
                existingPayroll.setDate(payrollData.getDate());
                existingPayroll.setSalary(payrollData.getSalary());
                existingPayroll.setNetPay(payrollData.getNetPay());
                existingPayroll.clearDetails();

                for (IntStringPayroll line : newLines) {
                    existingPayroll.addDetail(line);
                }

                session.merge(existingPayroll);
            }
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
                session.remove(p);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
}