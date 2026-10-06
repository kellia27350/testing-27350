import java.util.*;

/**
 * Employee Management System
 * Handles: adding/removing employees, processing payments, and payroll reports.
 *
 * Business rules:
 *  - Overtime = 1.5x hourly rate  (hourly rate = baseSalary / 160)
 *  - Management department gets a 10% bonus on gross pay
 *  - Tax: 25% if gross pay >= 5000, otherwise 15%
 */
public class EmployeeManagementSystem {

    // --- Inner class: one employee record ---
    public static class Employee {
        private final String id;
        private String name;
        private String department;
        private double baseSalary;
        private double lastNetPay;

        private Employee(String id, String name, String department, double baseSalary) {
            this.id         = id;
            this.name       = name;
            this.department = department;
            this.baseSalary = baseSalary;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDepartment() {
            return department;
        }

        public double getBaseSalary() {
            return baseSalary;
        }

        public double getLastNetPay() {
            return lastNetPay;
        }
    }

    // Storage: employee ID → Employee
    private final Map<String, Employee> employees = new HashMap<>();

    // ------------------------------------------------------------------ //
    //  1. Add employee
    // ------------------------------------------------------------------ //
    /**
     * Registers a new employee.
     * Returns true on success, false if the ID already exists.
     * Throws IllegalArgumentException for bad input.
     */
    public boolean addEmployee(String id, String name, String department, double baseSalary) {
        validateEmployeeDetails(id, name, baseSalary);
        if (employees.containsKey(id))
            return false; // duplicate ID

        employees.put(id, new Employee(id, name, department, baseSalary));
        return true;
    }

    private static void validateEmployeeDetails(String id, String name, double baseSalary) {
        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("Employee ID cannot be empty.");
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Employee name cannot be empty.");
        if (baseSalary < 0)
            throw new IllegalArgumentException("Base salary cannot be negative.");
    }

    // ------------------------------------------------------------------ //
    //  2. Read employee
    // ------------------------------------------------------------------ //
    /**
     * Returns an employee by ID.
     * Throws NoSuchElementException if the employee does not exist.
     */
    public Employee getEmployee(String id) {
        Employee employee = employees.get(id);
        if (employee == null)
            throw new NoSuchElementException("Employee not found: " + id);
        return employee;
    }

    // ------------------------------------------------------------------ //
    //  3. Update employee
    // ------------------------------------------------------------------ //
    /**
     * Updates an employee's name, department, and base salary.
     * The employee ID is unchanged. Returns false if the ID is not registered.
     * Throws IllegalArgumentException for bad input.
     */
    public boolean updateEmployee(String id, String name, String department, double baseSalary) {
        validateEmployeeDetails(id, name, baseSalary);
        Employee employee = employees.get(id);
        if (employee == null)
            return false;

        employee.name = name;
        employee.department = department;
        employee.baseSalary = baseSalary;
        return true;
    }

    // ------------------------------------------------------------------ //
    //  4. Remove employee
    // ------------------------------------------------------------------ //
    /**
     * Removes an employee by ID.
     * Returns true if removed, false if not found.
     */
    public boolean removeEmployee(String id) {
        return employees.remove(id) != null;
    }

    // ------------------------------------------------------------------ //
    //  5. Process payment
    // ------------------------------------------------------------------ //
    /**
     * Calculates and stores net pay for an employee.
     * Returns the net pay amount.
     *
     * Throws IllegalArgumentException for negative hours.
     * Throws NoSuchElementException if the employee does not exist.
     */
    public double processPayment(String id, double hoursWorked, double overtimeHours) {
        if (hoursWorked < 0 || overtimeHours < 0)
            throw new IllegalArgumentException("Hours cannot be negative.");

        Employee emp = employees.get(id);
        if (emp == null)
            throw new NoSuchElementException("Employee not found: " + id);

        double hourlyRate = emp.baseSalary / 160.0;
        double grossPay   = (hourlyRate * hoursWorked) + (hourlyRate * 1.5 * overtimeHours);

        // Management bonus
        if ("Management".equalsIgnoreCase(emp.department))
            grossPay *= 1.10;

        // Tax bracket
        double tax    = (grossPay >= 5000) ? 0.25 : 0.15;
        double netPay = grossPay * (1 - tax);

        emp.lastNetPay = netPay;
        return netPay;
    }

    // ------------------------------------------------------------------ //
    //  6. Payroll report
    // ------------------------------------------------------------------ //
    /**
     * Builds and returns a payroll summary string.
     */
    public String generatePayrollReport() {
        if (employees.isEmpty())
            return "No employees to report.";

        double total   = 0;
        String topName = "";
        double topPay  = -1;

        StringBuilder sb = new StringBuilder("=== Payroll Report ===\n");
        for (Employee emp : employees.values()) {
            sb.append(String.format("  %-20s  Net Pay: %.2f%n", emp.getName(), emp.getLastNetPay()));
            total += emp.lastNetPay;
            if (emp.lastNetPay > topPay) {
                topPay  = emp.lastNetPay;
                topName = emp.getName();
            }
        }

        double average = total / employees.size();
        sb.append(String.format("Total Payroll : %.2f%n", total));
        sb.append(String.format("Average Pay   : %.2f%n", average));
        sb.append(String.format("Top Earner    : %s (%.2f)%n", topName, topPay));
        return sb.toString();
    }

    // ------------------------------------------------------------------ //
    //  7. Employee count
    // ------------------------------------------------------------------ //
    /** Returns how many employees are currently registered. */
    public int getEmployeeCount() {
        return employees.size();
    }

    // ------------------------------------------------------------------ //
    //  Simple console menu (interactive mode)
    // ------------------------------------------------------------------ //
    public static void main(String[] args) {
        EmployeeManagementSystem system = new EmployeeManagementSystem();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Employee Management System ---");
            System.out.println("1. Add Employee");
            System.out.println("2. Remove Employee");
            System.out.println("3. Process Payment");
            System.out.println("4. Payroll Report");
            System.out.println("5. Employee Count");
            System.out.println("6. Find Employee");
            System.out.println("7. Update Employee");
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        System.out.print("ID: ");          String eid  = sc.nextLine();
                        System.out.print("Name: ");         String name = sc.nextLine();
                        System.out.print("Department: ");   String dept = sc.nextLine();
                        System.out.print("Base Salary: ");  double sal  = Double.parseDouble(sc.nextLine());
                        boolean added = system.addEmployee(eid, name, dept, sal);
                        System.out.println(added ? "Employee added." : "ID already exists.");
                        break;

                    case "2":
                        System.out.print("Employee ID to remove: ");
                        boolean removed = system.removeEmployee(sc.nextLine());
                        System.out.println(removed ? "Employee removed." : "ID not found.");
                        break;

                    case "3":
                        System.out.print("Employee ID: ");      String pid = sc.nextLine();
                        System.out.print("Hours worked: ");     double hrs = Double.parseDouble(sc.nextLine());
                        System.out.print("Overtime hours: ");   double ot  = Double.parseDouble(sc.nextLine());
                        double net = system.processPayment(pid, hrs, ot);
                        System.out.printf("Net pay: %.2f%n", net);
                        break;

                    case "4":
                        System.out.println(system.generatePayrollReport());
                        break;

                    case "5":
                        System.out.println("Registered employees: " + system.getEmployeeCount());
                        break;

                    case "6":
                        System.out.print("Employee ID to find: ");
                        Employee employee = system.getEmployee(sc.nextLine());
                        System.out.printf("ID: %s, Name: %s, Department: %s, Base Salary: %.2f, Last Net Pay: %.2f%n",
                                employee.getId(), employee.getName(), employee.getDepartment(),
                                employee.getBaseSalary(), employee.getLastNetPay());
                        break;

                    case "7":
                        System.out.print("Employee ID to update: "); String uid = sc.nextLine();
                        System.out.print("New name: ");              String newName = sc.nextLine();
                        System.out.print("New department: ");        String newDepartment = sc.nextLine();
                        System.out.print("New base salary: ");       double newSalary = Double.parseDouble(sc.nextLine());
                        boolean updated = system.updateEmployee(uid, newName, newDepartment, newSalary);
                        System.out.println(updated ? "Employee updated." : "ID not found.");
                        break;

                    case "0":
                        System.out.println("Goodbye!");
                        return;

                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
