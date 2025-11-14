package model.utils;


public class IntStringPayroll {

    private int amount;
    private String label;

    public IntStringPayroll() {}

    public IntStringPayroll(int amount, String label) {
        this.amount = amount;
        this.label = label;
    }

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
}