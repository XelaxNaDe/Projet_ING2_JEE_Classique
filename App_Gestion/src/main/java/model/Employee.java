package model;

import jakarta.persistence.*;
import model.utils.RoleEnum;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "fname", nullable = false)
    private String fname;

    @Column(name = "sname", nullable = false)
    private String sname;

    @Column(name = "gender")
    private String gender;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "position")
    private String position;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_departement")
    private Departement departement;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "Employee_Role",
            joinColumns = @JoinColumn(name = "id"),
            inverseJoinColumns = @JoinColumn(name = "id_role")
    )
    private Set<RoleEmp> roles = new HashSet<>();

    public Employee() {}

    public Employee(String fname, String sname, String gender, String email, String password, String position, Departement departement) {
        this.fname = fname;
        this.sname = sname;
        this.gender = gender;
        this.email = email;
        this.password = password;
        this.position = position;
        this.departement = departement;
    }

    public boolean hasRole(RoleEnum roleEnum) {
        for (RoleEmp r : this.roles) {
            if (r.getNomRole().equals(roleEnum.name())) {
                return true;
            }
        }
        return false;
    }

    public void addRole(RoleEmp roleEntity) { this.roles.add(roleEntity); }
    public Set<RoleEmp> getRoles() { return roles; }
    public void setRoles(Set<RoleEmp> roles) { this.roles = roles; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFname() { return fname; }
    public void setFname(String fname) { this.fname = fname; }
    public String getSname() { return sname; }
    public void setSname(String sname) { this.sname = sname; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public Departement getDepartement() { return departement; }
    public void setDepartement(Departement departement) { this.departement = departement; }
}