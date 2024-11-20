package io.github.declangh.projectmanagerbackend.common.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.NonNull;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

@Component
public class ProjectManagerExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(@NonNull Throwable exception, @NonNull DataFetchingEnvironment env) {
        if (exception instanceof ProjectManagerException) {
            return GraphqlErrorBuilder.newError(env)
                    .message(exception.getMessage())
                    .build();
        }
        return super.resolveToSingleError(exception, env);
    }
}
