package dao;

import model.Departement;
import model.Employee;
import model.RoleEmp;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class EmployeeDAO {

    public Employee findByEmailAndPassword(String email, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Employee E WHERE E.email = :email AND E.password = :pwd", Employee.class)
                    .setParameter("email", email)
                    .setParameter("pwd", password)
                    .uniqueResult();
        }
    }

    public Employee findEmployeeById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Employee.class, id);
        }
    }

    public List<Employee> getAllEmployees() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Employee", Employee.class).list();
        }
    }

    public List<Employee> getAllEmployeesFull(String searchPrenom, String searchNom, String searchMatricule,
                                              int searchDepartementId, List<Integer> filterProjectEmployeeIds,
                                              String filterPoste, String filterRole) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.roles r LEFT JOIN FETCH e.departement d WHERE 1=1 ");

            if (searchPrenom != null && !searchPrenom.trim().isEmpty()) hql.append("AND e.fname LIKE :fname ");
            if (searchNom != null && !searchNom.trim().isEmpty()) hql.append("AND e.sname LIKE :sname ");
            if (searchMatricule != null && !searchMatricule.trim().isEmpty()) hql.append("AND e.id = :id ");

            if (searchDepartementId > 0) {
                hql.append("AND d.id = :deptId ");
            }

            if (filterProjectEmployeeIds != null) {
                if (filterProjectEmployeeIds.isEmpty()) {
                    return new java.util.ArrayList<>();
                }
                hql.append("AND e.id IN (:empIds) ");
            }

            if (filterPoste != null && !filterPoste.trim().isEmpty()) hql.append("AND e.position = :pos ");
            if (filterRole != null && !filterRole.trim().isEmpty()) hql.append("AND r.nomRole = :role ");

            org.hibernate.query.Query<Employee> query = session.createQuery(hql.toString(), Employee.class);

            if (searchPrenom != null && !searchPrenom.trim().isEmpty()) query.setParameter("fname", "%" + searchPrenom + "%");
            if (searchNom != null && !searchNom.trim().isEmpty()) query.setParameter("sname", "%" + searchNom + "%");
            if (searchMatricule != null && !searchMatricule.trim().isEmpty()) query.setParameter("id", Integer.parseInt(searchMatricule));

            if (searchDepartementId > 0) query.setParameter("deptId", searchDepartementId);

            if (filterProjectEmployeeIds != null && !filterProjectEmployeeIds.isEmpty()) {
                query.setParameterList("empIds", filterProjectEmployeeIds);
            }

            if (filterPoste != null && !filterPoste.trim().isEmpty()) query.setParameter("pos", filterPoste);
            if (filterRole != null && !filterRole.trim().isEmpty()) query.setParameter("role", filterRole);

            return query.list();
        }
    }

    public List<Employee> getEmployeesByDepartmentId(int idDepartement) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Employee e WHERE e.departement.id = :idDept", Employee.class)
                    .setParameter("idDept", idDepartement)
                    .list();
        }
    }

    public List<String> getDistinctPostes() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT DISTINCT e.position FROM Employee e WHERE e.position IS NOT NULL ORDER BY e.position", String.class).list();
        }
    }

    public List<String> getDistinctRoles() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT r.nomRole FROM RoleEmp r ORDER BY r.nomRole", String.class).list();
        }
    }

    public void createEmployee(Employee emp) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(emp);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void deleteEmployee(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Employee emp = session.get(Employee.class, id);

            if (emp != null) {
                session.remove(emp);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void updateEmployee(Employee emp) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(emp);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void setEmployeeDepartment(int idEmploye, int idDepartement) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Employee emp = session.get(Employee.class, idEmploye);
            if (emp != null) {
                if (idDepartement > 0) {
                    Departement d = session.getReference(Departement.class, idDepartement);
                    emp.setDepartement(d);
                } else {
                    emp.setDepartement(null);
                }
                session.merge(emp);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void clearEmployeeRoles(int employeeId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Employee emp = session.get(Employee.class, employeeId);
            if (emp != null) {
                emp.getRoles().clear();
                session.merge(emp);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void addEmployeeRole(int employeeId, int roleId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Employee emp = session.get(Employee.class, employeeId);
            RoleEmp role = session.get(RoleEmp.class, roleId);

            if (emp != null && role != null) {
                emp.getRoles().add(role);
                session.merge(emp);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void manageAdminRole(int employeeId, boolean shouldBeAdmin) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Employee emp = session.get(Employee.class, employeeId);
            RoleEmp adminRole = session.createQuery("FROM RoleEmp WHERE nomRole = :nom", RoleEmp.class)
                    .setParameter("nom", "ADMINISTRATOR")
                    .uniqueResult();

            if (emp != null && adminRole != null) {
                if (shouldBeAdmin) {
                    emp.getRoles().add(adminRole);
                } else {
                    emp.getRoles().removeIf(r -> r.getNomRole().equals("ADMINISTRATOR"));
                }
                session.merge(emp);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    private void assignSpecificRole(int employeeId, String roleName) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Employee emp = session.get(Employee.class, employeeId);
            RoleEmp role = session.createQuery("FROM RoleEmp WHERE nomRole = :nom", RoleEmp.class)
                    .setParameter("nom", roleName).uniqueResult();

            if (emp != null && role != null) {
                emp.getRoles().add(role);
                session.merge(emp);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void assignProjectManagerRole(int employeeId) {
        assignSpecificRole(employeeId, "PROJECTMANAGER");
    }

    public void assignHeadDepartementRole(int employeeId) {
        assignSpecificRole(employeeId, "HEADDEPARTEMENT");
    }

    public void checkAndRemoveProjectManagerRole(int employeeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("SELECT COUNT(p) FROM Project p WHERE p.chefProjet.id = :id", Long.class)
                    .setParameter("id", employeeId).uniqueResult();

            if (count == 0) {
                removeSpecificRole(employeeId, "PROJECTMANAGER");
            }
        }
    }

    public void checkAndRemoveHeadDepartementRole(int employeeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("SELECT COUNT(d) FROM Departement d WHERE d.chefDepartement.id = :id", Long.class)
                    .setParameter("id", employeeId).uniqueResult();

            if (count == 0) {
                removeSpecificRole(employeeId, "HEADDEPARTEMENT");
            }
        }
    }

    private void removeSpecificRole(int employeeId, String roleName) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Employee emp = session.get(Employee.class, employeeId);
            emp.getRoles().removeIf(r -> r.getNomRole().equals(roleName));
            session.merge(emp);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
        }
    }

    public void updateEmail(int id, String email) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Employee emp = session.get(Employee.class, id);
            if(emp != null) { emp.setEmail(email); session.merge(emp); }
            tx.commit();
        } catch (Exception e) { if(tx!=null) tx.rollback(); }
    }

    public void updatePassword(int id, String pwd) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Employee emp = session.get(Employee.class, id);
            if(emp != null) { emp.setPassword(pwd); session.merge(emp); }
            tx.commit();
        } catch (Exception e) { if(tx!=null) tx.rollback(); }
    }

    public boolean checkPassword(int id, String pwd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Employee emp = session.get(Employee.class, id);
            return emp != null && emp.getPassword().equals(pwd);
        }
    }
}