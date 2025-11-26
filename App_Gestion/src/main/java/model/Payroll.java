package model;

import jakarta.persistence.*;
import model.utils.IntStringPayroll;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "Payroll")
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_payroll")
    private int id;

    // Relation vers l'Employé
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id") // La colonne FK dans la table Payroll
    private Employee employee;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "salary")
    private int salary;

    @Column(name = "netPay")
    private double netPay;

    // RELATION One-to-Many : Une fiche de paie a plusieurs lignes
    // cascade = ALL signifie que si on sauvegarde/supprime la fiche, on sauvegarde/supprime ses lignes
    @OneToMany(mappedBy = "payroll", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<IntStringPayroll> allLines = new ArrayList<>();

    // Constructeur vide (Obligatoire Hibernate)
    public Payroll() {}

    // Constructeur complet
    public Payroll(Employee employee, LocalDate date, int salary) {
        this.employee = employee;
        this.date = date;
        this.salary = salary;
        this.netPay = salary; // Sera recalculé
    }

    // --- MÉTHODES MÉTIER ---

    // Filtre la liste principale pour ne renvoyer que les primes
    public List<IntStringPayroll> getBonusesList() {
        if (allLines == null) return new ArrayList<>();
        return allLines.stream()
                .filter(line -> "Prime".equalsIgnoreCase(line.getType()))
                .collect(Collectors.toList());
    }

    // Filtre la liste principale pour ne renvoyer que les déductions
    public List<IntStringPayroll> getDeductionsList() {
        if (allLines == null) return new ArrayList<>();
        return allLines.stream()
                .filter(line -> "Déduction".equalsIgnoreCase(line.getType())) // ou "DEDUCTION" selon ta BDD
                .collect(Collectors.toList());
    }

    public void addLine(IntStringPayroll line) {
        line.setPayroll(this); // Important : lier l'objet enfant au parent
        this.allLines.add(line);
        calculateNetPay();
    }

    public void calculateNetPay() {
        double sum = salary;
        for (IntStringPayroll line : allLines) {
            if ("Prime".equalsIgnoreCase(line.getType())) {
                sum += line.getAmount();
            } else if ("Déduction".equalsIgnoreCase(line.getType())) {
                sum -= line.getAmount();
            }
        }
        this.netPay = sum;
    }

    // Getters et Setters simples
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getSalary() { return salary; }
    public void setSalary(int salary) { this.salary = salary; }

    public double getNetPay() { return netPay; }
    public void setNetPay(double netPay) { this.netPay = netPay; }

    public List<IntStringPayroll> getAllLines() { return allLines; }
    public void setAllLines(List<IntStringPayroll> allLines) { this.allLines = allLines; }
}