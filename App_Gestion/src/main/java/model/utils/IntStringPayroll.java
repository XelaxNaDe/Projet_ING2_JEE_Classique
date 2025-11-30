package model.utils;

import jakarta.persistence.*;
import model.Payroll;

@Entity
@Table(name = "IntStringPayroll")
public class IntStringPayroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_line")
    private int idLine;

    @Column(name = "amount")
    private int amount;

    @Column(name = "label")
    private String label;

    @Column(name = "type_list")
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_payroll")
    private Payroll payroll;

    public IntStringPayroll() {}

    public IntStringPayroll(int amount, String label, String type) {
        this.amount = amount;
        this.label = label;
        this.type = type;
    }

    public IntStringPayroll(int amount, String label, String type, Payroll payroll) {
        this.amount = amount;
        this.label = label;
        this.type = type;
        this.payroll = payroll;
    }

    public int getIdLine() { return idLine; }
    public void setIdLine(int idLine) { this.idLine = idLine; }

    public int getId_line() { return idLine; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Payroll getPayroll() { return payroll; }
    public void setPayroll(Payroll payroll) { this.payroll = payroll; }

    public int getId_payroll() {
        return (payroll != null) ? payroll.getId() : 0;
    }
}