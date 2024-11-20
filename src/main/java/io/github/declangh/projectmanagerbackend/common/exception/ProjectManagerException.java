package io.github.declangh.projectmanagerbackend.common.exception;

public class ProjectManagerException extends RuntimeException {

    public ProjectManagerException() {
        super();
    }

    public ProjectManagerException(String message) {
        super(message);
    }

    public ProjectManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProjectManagerException(Throwable cause) {
        super(cause);
    }
}
