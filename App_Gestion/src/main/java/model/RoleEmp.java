package model;

import jakarta.persistence.*;
import model.utils.RoleEnum;

@Entity
@Table(name = "Role")
public class RoleEmp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private int id;

    @Column(name = "nom_role", unique = true, nullable = false)
    private String nomRole;

    public RoleEmp() {}
    public RoleEmp(String nomRole) {
        this.nomRole = nomRole;
    }

    public RoleEnum getRoleAsEnum() {
        try {
            return RoleEnum.valueOf(this.nomRole);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    public void setRoleFromEnum(RoleEnum roleEnum) {
        this.nomRole = roleEnum.name();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomRole() { return nomRole; }
    public void setNomRole(String nomRole) { this.nomRole = nomRole; }
}