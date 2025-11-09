package model;

import model.utils.IntStringPayroll;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Payroll {

    private final Employee employee;
    private final LocalDate date;
    private final int salary;
    private List<IntStringPayroll> bonusesList = new ArrayList<>();
    private List<IntStringPayroll> deductionsList = new ArrayList<>();
    private double netPay;

    public Payroll(Employee employee, LocalDate date, int salary, List<IntStringPayroll> bonusesL, List<IntStringPayroll> deductionsL) {
        this.employee = employee;
        this.date = date;
        this.salary = salary;

        if (Objects.nonNull(bonusesL)) {
            this.bonusesList.addAll(bonusesL);
        }
        if (Objects.nonNull(deductionsL)) {
            this.deductionsList.addAll(deductionsL);
        }

        this.calculateNetPay();
    }

    private void calculateNetPay() {
        double sum = salary;

        for (IntStringPayroll bonus : bonusesList) {
            sum = sum + bonus.getAmount();
        }
        for (IntStringPayroll deduction : deductionsList) {
            sum = sum - deduction.getAmount();
        }
        this.netPay = sum;
    }

    public Employee getEmployee() {
        return employee;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getSalary() {
        return salary;
    }

    public List<IntStringPayroll> getBonusesList() {
        return bonusesList;
    }

    public List<IntStringPayroll> getDeductionsList() {
        return deductionsList;
    }

    public double getNetPay() {
        return netPay;
    }
}