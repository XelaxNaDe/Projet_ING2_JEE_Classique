package model;

import jakarta.persistence.*;
import model.utils.IntStringPayroll;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "Payroll")
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_payroll")
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id")
    private Employee employee;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "salary")
    private int salary;

    @Column(name = "netPay")
    private double netPay;

    @OneToMany(mappedBy = "payroll", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<IntStringPayroll> allDetails = new ArrayList<>();

    public Payroll() {}

    public Payroll(Employee employee, LocalDate date, int salary, double netPay) {
        this.employee = employee;
        this.date = date;
        this.salary = salary;
        this.netPay = netPay;
    }

    public List<IntStringPayroll> getBonusesList() {
        if (allDetails == null) return new ArrayList<>();
        return allDetails.stream()
                .filter(line -> "Prime".equalsIgnoreCase(line.getType()))
                .collect(Collectors.toList());
    }

    public List<IntStringPayroll> getDeductionsList() {
        if (allDetails == null) return new ArrayList<>();
        return allDetails.stream()
                .filter(line -> "Déduction".equalsIgnoreCase(line.getType()))
                .collect(Collectors.toList());
    }

    public void addDetail(IntStringPayroll detail) {
        detail.setPayroll(this);
        this.allDetails.add(detail);
    }

    public void clearDetails() {
        this.allDetails.clear();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public int getEmployeeId() { return (employee != null) ? employee.getId() : 0; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getSalary() { return salary; }
    public void setSalary(int salary) { this.salary = salary; }

    public double getNetPay() { return netPay; }
    public void setNetPay(double netPay) { this.netPay = netPay; }

    public List<IntStringPayroll> getAllDetails() { return allDetails; }
    public void setAllDetails(List<IntStringPayroll> allDetails) { this.allDetails = allDetails; }
}