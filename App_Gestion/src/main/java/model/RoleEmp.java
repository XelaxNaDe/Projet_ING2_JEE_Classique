package model;

import jakarta.persistence.*;
import model.utils.RoleEnum; // Import de ton enum

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

    // --- L'astuce est ici ---
    // Méthode utilitaire pour obtenir la version Enum
    public RoleEnum getRoleAsEnum() {
        try {
            return RoleEnum.valueOf(this.nomRole);
        } catch (IllegalArgumentException e) {
            return null; // Ou une valeur par défaut
        }
    }

    // Méthode pour définir via l'Enum
    public void setRoleFromEnum(RoleEnum roleEnum) {
        this.nomRole = roleEnum.name();
    }

    // Getters et Setters classiques
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomRole() { return nomRole; }
    public void setNomRole(String nomRole) { this.nomRole = nomRole; }
}