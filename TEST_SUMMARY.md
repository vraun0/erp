# Test Summary

## Overview
This document summarizes the testing activities performed on the University ERP System. Testing included automated verification scripts and manual UI testing to ensure all functional and non-functional requirements were met.

## 1. Automated Verification
A dedicated verification script (`VerificationScript.java`) was executed to test core business logic and new features.

| Feature | Status | Notes |
| :--- | :--- | :--- |
| **Auth Security** | ✅ PASS | Account locks after 5 failed attempts. |
| **Prerequisites** | ✅ PASS | Registration blocked if prereq not passed (Level-based logic verified). |
| **CSV Import** | ✅ PASS | Gradebook data correctly parsed and loaded. |
| **Backup/Restore** | ✅ PASS | Database backup file created successfully. |

## 2. Manual UI Testing
Manual testing was conducted following the `manual_test_plan.md`.

| Module | Test Case | Status |
| :--- | :--- | :--- |
| **Authentication** | Login/Logout | ✅ PASS |
| | Account Lockout Message | ✅ PASS |
| **Student** | View Catalog | ✅ PASS |
| | Register (Success) | ✅ PASS |
| | Register (Prereq Fail) | ✅ PASS |
| | View Timetable | ✅ PASS |
| **Instructor** | View Gradebook | ✅ PASS |
| | Edit Grades | ✅ PASS |
| | Import CSV | ✅ PASS |
| **Admin** | Create User/Course | ✅ PASS |
| | Toggle Maintenance | ✅ PASS |
| | Backup Database | ✅ PASS |

## 3. Bug Fixes & Resolutions
-   **PDF Dependency**: Fixed `itextpdf` version mismatch in `pom.xml`.
-   **UI Rendering**: Fixed Course Card height in Catalog to show "Register" button.
-   **Missing UI**: Added "Import CSV" button to Gradebook panel.
-   **Prerequisite Logic**: Updated logic to enforce level-based prerequisites (e.g., CS201 requires CS101), not just sequence numbers.

## 4. Conclusion
The application has passed all critical test cases and is stable for deployment. All known bugs have been resolved.
