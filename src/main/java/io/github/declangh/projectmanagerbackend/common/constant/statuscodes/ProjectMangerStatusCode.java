package io.github.declangh.projectmanagerbackend.common.constant.statuscodes;

import io.github.declangh.projectmanagerbackend.common.exception.ProjectManagerException;
import io.github.declangh.projectmanagerbackend.common.exception.ProjectManagerExceptionResolver;
import org.springframework.http.HttpStatus;

import java.net.http.HttpResponse;

/**
 * This class provides a standard way to communicate errors with the client using Http status codes.
 * <br/>
 * <br/>
 * Since GraphQL requests are always 200 (or 500 in a very bad case), it is difficult to tell the client what
 * exactly went wrong. Passing fields in this class as an error message in a {@link ProjectManagerException} will
 * ensure it is captured by the {@link ProjectManagerExceptionResolver} and sent as a GraphQL error message.
 */
public class ProjectMangerStatusCode {

    /**
     * A client makes a request that has been acknowledged but not acted upon.
     */
    public static final String ACCEPTED = "202";

    /**
     * A client sends a request that is malformed or suspicious. Generally, it just doesn't look right.
     */
    public static final String BAD_REQUEST = "400";

    /**
     * An unauthenticated client is requesting a resource.
     */
    public static final String UNAUTHORIZED = "401";

    /**
     * A client is requesting a resource they are not authorized to access.
     */
    public static final String FORBIDDEN = "403";

    /**
     * A client is requesting a resource that cannot be found.
     * <br/>
     * <br/>
     * You can send this rather than {@link #FORBIDDEN} to hide the existence of a resource from an unauthorized
     * client.
     */
    public static final String NOT_FOUND = "404";

    public static final String INTERNAL_SERVER_ERROR = "500";
}
