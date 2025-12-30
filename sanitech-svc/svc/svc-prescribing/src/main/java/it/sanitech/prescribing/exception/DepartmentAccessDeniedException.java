package it.sanitech.prescribing.exception;

import it.sanitech.prescribing.utilities.AppConstants;

/**
 * Eccezione applicativa per indicare un accesso negato in base al reparto (ABAC).
 */
public class DepartmentAccessDeniedException extends RuntimeException {

    public DepartmentAccessDeniedException(String message) {
        super(message);
    }

    public static DepartmentAccessDeniedException forDepartment(String departmentCode) {
        String dept = (departmentCode == null) ? "<null>" : departmentCode;
        return new DepartmentAccessDeniedException(AppConstants.ErrorMessage.MSG_DEPARTMENT_FORBIDDEN + " (dept=" + dept + ")");
    }
}
