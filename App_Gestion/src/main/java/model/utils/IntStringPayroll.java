package model.utils;


public class IntStringPayroll {
    private int id_line;
    private int id_payroll;
    private int amount;
    private String label;
    private String type;

    public IntStringPayroll(int line, int payroll, int amount, String label, String type) {
        this.id_line = line;
        this.id_payroll = payroll;
        this.amount = amount;
        this.label = label;
        this.type = type;
    }

    public int getId_line() { return id_line; }
    public void setId_line(int id_line) {this.id_line = id_line;}
    public int getId_payroll() { return id_payroll; }
    public void setId_payroll(int id_payroll) {this.id_payroll = id_payroll;}
    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}