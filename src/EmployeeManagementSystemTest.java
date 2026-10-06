import java.util.NoSuchElementException;

/**
 * Test harness for EmployeeManagementSystem.
 *
 * Covers:
 *   BB-01 to BB-12  →  Black-Box (Functional) tests
 *   WB-01 to WB-15  →  White-Box (Structural/Branch) tests
 *
 * No external framework needed — just compile and run.
 * Each test prints PASS or FAIL with a short reason.
 */
public class EmployeeManagementSystemTest {

    // ------------------------------------------------------------------ //
    //  Tiny test helper
    // ------------------------------------------------------------------ //
    static int passed = 0;
    static int failed = 0;

    /** Check that a condition is true. */
    static void assertTrue(String testId, String description, boolean condition) {
        if (condition) {
            System.out.printf("  PASS  [%s] %s%n", testId, description);
            passed++;
        } else {
            System.out.printf("  FAIL  [%s] %s%n", testId, description);
            failed++;
        }
    }

    /** Check that running a block throws the expected exception type. */
    static void assertThrows(String testId, String description,
                             Class<? extends Exception> expected, Runnable block) {
        try {
            block.run();
            System.out.printf("  FAIL  [%s] %s  (no exception thrown)%n", testId, description);
            failed++;
        } catch (Exception e) {
            if (expected.isInstance(e)) {
                System.out.printf("  PASS  [%s] %s%n", testId, description);
                passed++;
            } else {
                System.out.printf("  FAIL  [%s] %s  (got %s, expected %s)%n",
                        testId, description, e.getClass().getSimpleName(), expected.getSimpleName());
                failed++;
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Helper: fresh system for every test
    // ------------------------------------------------------------------ //
    static EmployeeManagementSystem fresh() {
        return new EmployeeManagementSystem();
    }

    // ==================================================================== //
    //  BLACK-BOX TEST CASES  (BB-01 … BB-12)
    //  Based on functional requirements FR1–FR6.
    //  Input/output only — internal code structure is NOT considered.
    // ==================================================================== //

    static void runBlackBoxTests() {
        System.out.println("\n========== BLACK-BOX (Functional) Tests ==========");

        // BB-01 — Add a valid employee
        // FR1: system shall allow adding an employee with ID, name, dept, salary
        {
            EmployeeManagementSystem s = fresh();
            boolean result = s.addEmployee("E001", "Alice Uwase", "Engineering", 3000);
            assertTrue("BB-01", "Add valid employee → accepted, count = 1",
                    result && s.getEmployeeCount() == 1);
        }

        // BB-02 — Duplicate ID rejected
        // FR2: duplicate IDs must be rejected
        {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice Uwase", "Engineering", 3000);
            boolean second = s.addEmployee("E001", "Bob Nkurunziza", "HR", 2500);
            assertTrue("BB-02", "Duplicate ID → rejected, count stays 1",
                    !second && s.getEmployeeCount() == 1);
        }

        // BB-03 — Empty ID rejected
        // FR2: empty/null ID must throw
        assertThrows("BB-03", "Empty ID → IllegalArgumentException",
                IllegalArgumentException.class,
                () -> fresh().addEmployee("", "Alice", "Engineering", 3000));

        // BB-04 — Negative salary rejected
        // FR2: negative base salary must throw
        assertThrows("BB-04", "Negative salary → IllegalArgumentException",
                IllegalArgumentException.class,
                () -> fresh().addEmployee("E001", "Alice", "Engineering", -100));

        // BB-05 — Remove existing employee
        // FR3: existing employee can be removed; count drops to 0
        {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice Uwase", "Engineering", 3000);
            boolean removed = s.removeEmployee("E001");
            assertTrue("BB-05", "Remove existing employee → removed, count = 0",
                    removed && s.getEmployeeCount() == 0);
        }

        // BB-06 — Remove non-existent ID
        // FR3: removing a missing ID returns false, no crash
        {
            EmployeeManagementSystem s = fresh();
            boolean result = s.removeEmployee("E999");
            assertTrue("BB-06", "Remove non-existent ID → returns false",
                    !result && s.getEmployeeCount() == 0);
        }

        // BB-07 — Payment for non-management employee below tax threshold
        // FR4: baseSalary=3000, 160h, 0 OT, Engineering → gross=3000, tax 15%
        //      net = 3000 * 0.85 = 2550.00
        {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice Uwase", "Engineering", 3000);
            double net = s.processPayment("E001", 160, 0);
            assertTrue("BB-07", "Engineering 3000/160h/0OT → net pay = 2550.00",
                    Double.compare(net, 2550.00) == 0);
        }

        // BB-08 — Payment for Management employee above tax threshold
        // FR4: baseSalary=6000, 160h, 0 OT, Management
        //      gross=6000, +10% bonus → 6600, tax 25% → net = 6600*0.75 = 4950.00
        {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Carol Mwangi", "Management", 6000);
            double net = s.processPayment("E001", 160, 0);
            assertTrue("BB-08", "Management 6000/160h/0OT → net pay = 4950.00",
                    Math.abs(net - 4950.00) < 0.001);
        }

        // BB-09 — Negative hours rejected
        // FR5: negative hoursWorked must throw
        {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice Uwase", "Engineering", 3000);
            assertThrows("BB-09", "Negative hours → IllegalArgumentException",
                    IllegalArgumentException.class,
                    () -> s.processPayment("E001", -10, 0));
        }

        // BB-10 — Payment for unregistered employee
        // FR5: unknown ID must throw NoSuchElementException
        assertThrows("BB-10", "Payment for unknown ID → NoSuchElementException",
                NoSuchElementException.class,
                () -> fresh().processPayment("E999", 160, 0));

        // BB-11 — Payroll report with employees
        // FR6: report must contain net pay, total, average, top earner
        {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice Uwase", "Engineering", 3000);
            s.processPayment("E001", 160, 0);
            String report = s.generatePayrollReport();
            assertTrue("BB-11", "Payroll report contains all expected fields",
                    report.contains("Alice Uwase")
                    && report.contains("Total")
                    && report.contains("Average")
                    && report.contains("Top Earner"));
        }

        // BB-12 — Payroll report with no employees
        // FR6: must return the exact "no employees" message
        {
            EmployeeManagementSystem s = fresh();
            String report = s.generatePayrollReport();
            assertTrue("BB-12", "Empty system → 'No employees to report.'",
                    report.equals("No employees to report."));
        }
    }

    // ==================================================================== //
    //  WHITE-BOX TEST CASES  (WB-01 … WB-15)
    //  Designed by looking at the internal code branches:
    //    addEmployee, removeEmployee, processPayment, generatePayrollReport.
    // ==================================================================== //

    static void runWhiteBoxTests() {
        System.out.println("\n========== WHITE-BOX (Structural/Branch) Tests ==========");

        // --- addEmployee branches ---

        // WB-01 — null ID → branch: id == null check
        assertThrows("WB-01", "null ID → IllegalArgumentException",
                IllegalArgumentException.class,
                () -> fresh().addEmployee(null, "Alice", "Engineering", 3000));

        // WB-02 — null name → branch: name == null check
        assertThrows("WB-02", "null name → IllegalArgumentException",
                IllegalArgumentException.class,
                () -> fresh().addEmployee("E001", null, "Engineering", 3000));

        // WB-03 — whitespace-only ID → branch: id.trim().isEmpty()
        assertThrows("WB-03", "Whitespace ID → IllegalArgumentException",
                IllegalArgumentException.class,
                () -> fresh().addEmployee("   ", "Alice", "Engineering", 3000));

        // WB-04 — salary exactly 0 → boundary: 0 is valid (not negative)
        {
            EmployeeManagementSystem s = fresh();
            boolean ok = s.addEmployee("E001", "Zero Pay", "Intern", 0);
            assertTrue("WB-04", "Salary = 0 is accepted (boundary)", ok);
        }

        // WB-05 — salary exactly -0.01 → branch: baseSalary < 0
        assertThrows("WB-05", "Salary = -0.01 → IllegalArgumentException",
                IllegalArgumentException.class,
                () -> fresh().addEmployee("E001", "Alice", "Engineering", -0.01));

        // --- removeEmployee branches ---

        // WB-06 — remove from a system that has multiple employees
        //          branch: key found, map shrinks by 1
        {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice", "Engineering", 3000);
            s.addEmployee("E002", "Bob",   "HR",          2500);
            boolean removed = s.removeEmployee("E001");
            assertTrue("WB-06", "Remove one of two employees → count = 1",
                    removed && s.getEmployeeCount() == 1);
        }

        // --- processPayment branches ---

        // WB-07 — overtime hours = 0 → branch: OT portion adds nothing
        {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice", "Engineering", 3200);
            double net = s.processPayment("E001", 160, 0);
            // gross = 3200, tax 15% → net = 2720.00
            assertTrue("WB-07", "0 OT hours → net = baseSalary * 0.85 = 2720.00",
                    Double.compare(net, 2720.00) == 0);
        }

        // WB-08 — overtime hours > 0 → branch: OT adds to gross
        {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice", "Engineering", 3200);
            // hourly = 3200/160 = 20, OT pay = 20*1.5*8 = 240, gross = 3200+240 = 3440
            // tax 15% → net = 3440 * 0.85 = 2924.00
            double net = s.processPayment("E001", 160, 8);
            assertTrue("WB-08", "8 OT hours → net = 2924.00",
                    Double.compare(net, 2924.00) == 0);
        }

        // WB-09 — gross pay exactly 5000 → boundary: tax switches to 25%
        {
            EmployeeManagementSystem s = fresh();
            // baseSalary=5000, 160h, 0 OT, non-management → gross=5000, tax 25%
            s.addEmployee("E001", "Alice", "Engineering", 5000);
            double net = s.processPayment("E001", 160, 0);
            // net = 5000 * 0.75 = 3750.00
            assertTrue("WB-09", "Gross = 5000 (boundary) → 25% tax, net = 3750.00",
                    Double.compare(net, 3750.00) == 0);
        }

        // WB-10 — gross pay just below 5000 → branch: tax stays at 15%
        {
            EmployeeManagementSystem s = fresh();
            // baseSalary=4999, 160h, 0 OT → gross=4999, tax 15%
            s.addEmployee("E001", "Alice", "Engineering", 4999);
            double net = s.processPayment("E001", 160, 0);
            // net = 4999 * 0.85 = 4249.15
            assertTrue("WB-10", "Gross = 4999 (just below 5000) → 15% tax",
                    Math.abs(net - 4249.15) < 0.01);
        }

        // WB-11 — negative overtime hours → branch: overtimeHours < 0 throws
        {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice", "Engineering", 3000);
            assertThrows("WB-11", "Negative OT hours → IllegalArgumentException",
                    IllegalArgumentException.class,
                    () -> s.processPayment("E001", 160, -5));
        }

        // WB-12 — Management department bonus branch
        {
            EmployeeManagementSystem s = fresh();
            // baseSalary=3000, 160h, 0 OT, Management
            // gross = 3000 * 1.10 = 3300, tax 15% → net = 3300 * 0.85 = 2805.00
            s.addEmployee("E001", "Dave", "Management", 3000);
            double net = s.processPayment("E001", 160, 0);
            assertTrue("WB-12", "Management 3000 below threshold → bonus+15% tax, net = 2805.00",
                    Math.abs(net - 2805.00) < 0.001);
        }

        // WB-13 — Non-management department — no bonus branch
        {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Eve", "Finance", 3000);
            double net = s.processPayment("E001", 160, 0);
            // gross = 3000 (no bonus), tax 15% → net = 2550.00
            assertTrue("WB-13", "Non-management dept → no bonus, net = 2550.00",
                    Double.compare(net, 2550.00) == 0);
        }

        // WB-14 — generatePayrollReport: multiple employees, correct top earner
        {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice", "Engineering", 3000);
            s.addEmployee("E002", "Bob",   "Engineering", 5000);
            s.processPayment("E001", 160, 0);  // net = 2550
            s.processPayment("E002", 160, 0);  // net = 3750 (above 5000 threshold)
            String report = s.generatePayrollReport();
            assertTrue("WB-14", "Report top earner = Bob (higher salary)",
                    report.contains("Bob"));
        }

        // WB-15 — getEmployeeCount: returns correct number after mixed operations
        {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice", "Engineering", 3000);
            s.addEmployee("E002", "Bob",   "HR",          2500);
            s.addEmployee("E003", "Carol", "Management",  6000);
            s.removeEmployee("E002");
            assertTrue("WB-15", "Count after 3 adds + 1 remove = 2",
                    s.getEmployeeCount() == 2);
        }
    }

    // ==================================================================== //
    //  CRUD TEST CASES
    // ==================================================================== //

    static void runCrudTests() {
        System.out.println("\n========== CRUD Tests ==========");

        // Create, read, update, then delete the same employee.
        {
            EmployeeManagementSystem s = fresh();
            boolean created = s.addEmployee("E100", "Mina Patel", "Support", 2800);
            EmployeeManagementSystem.Employee createdEmployee = s.getEmployee("E100");
            boolean initialReadMatches = createdEmployee.getId().equals("E100")
                    && createdEmployee.getName().equals("Mina Patel")
                    && createdEmployee.getDepartment().equals("Support")
                    && createdEmployee.getBaseSalary() == 2800;
            boolean updated = s.updateEmployee("E100", "Mina Patel", "Engineering", 3500);
            EmployeeManagementSystem.Employee updatedEmployee = s.getEmployee("E100");
            boolean deleted = s.removeEmployee("E100");

            assertTrue("CR-01", "Create → read → update → read updated values → delete",
                    created
                    && initialReadMatches
                    && updated
                    && updatedEmployee.getId().equals("E100")
                    && updatedEmployee.getDepartment().equals("Engineering")
                    && updatedEmployee.getBaseSalary() == 3500
                    && deleted
                    && s.getEmployeeCount() == 0);
        }

        // Reading or updating a missing employee follows the documented outcomes.
        {
            EmployeeManagementSystem s = fresh();
            assertThrows("CR-02", "Read missing employee → NoSuchElementException",
                    NoSuchElementException.class, () -> s.getEmployee("E999"));
            assertTrue("CR-03", "Update missing employee → false",
                    !s.updateEmployee("E999", "New Name", "HR", 2000));
        }

        // Updating does not permit invalid employee data.
        {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E100", "Mina Patel", "Support", 2800);
            assertThrows("CR-04", "Update with blank name → IllegalArgumentException",
                    IllegalArgumentException.class,
                    () -> s.updateEmployee("E100", " ", "Engineering", 3500));
            assertThrows("CR-05", "Update with negative salary → IllegalArgumentException",
                    IllegalArgumentException.class,
                    () -> s.updateEmployee("E100", "Mina Patel", "Engineering", -1));
            EmployeeManagementSystem.Employee employee = s.getEmployee("E100");
            assertTrue("CR-06", "Rejected updates leave the existing employee unchanged",
                    employee.getName().equals("Mina Patel")
                    && employee.getDepartment().equals("Support")
                    && employee.getBaseSalary() == 2800);
        }
    }

    // ------------------------------------------------------------------ //
    //  Main entry point
    // ------------------------------------------------------------------ //
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Employee Management System — Test Runner       ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        runBlackBoxTests();
        runWhiteBoxTests();
        runCrudTests();

        System.out.println("\n══════════════════════════════════════════");
        System.out.printf("  Results:  %d passed,  %d failed%n", passed, failed);
        System.out.println("══════════════════════════════════════════");

        // Exit with non-zero code if any test failed (useful for CI)
        if (failed > 0) System.exit(1);
    }
}
