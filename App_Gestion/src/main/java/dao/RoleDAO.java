package dao;

import model.RoleEmp;
import org.hibernate.Session;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoleDAO {

    public Map<Integer, String> getAllRoles() {
        Map<Integer, String> rolesMap = new HashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<RoleEmp> roles = session.createQuery("FROM RoleEmp", RoleEmp.class).list();

            for (RoleEmp r : roles) {
                rolesMap.put(r.getId(), r.getNomRole());
            }
        }
        return rolesMap;
    }

    public RoleEmp findByName(String roleName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM RoleEmp WHERE nomRole = :nom", RoleEmp.class)
                    .setParameter("nom", roleName)
                    .uniqueResult();
        }
    }
}