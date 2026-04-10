/*
 * Installr.io
 *
 * Copyright (c) 2025-present Dorian Vallant
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package io.installr.atrium;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import io.installr.lib.pkg.InvalidPackageIdException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class ExceptionMappers {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private int code;
        private String message;
    }

    @ServerExceptionMapper
    public Response mapThrowable( Throwable t ) {
        final ErrorResponse error = new ErrorResponse();
        if ( t instanceof IllegalArgumentException ) {
            error.setCode( Response.Status.BAD_REQUEST.getStatusCode() );
            error.setMessage( t.getMessage() );
        }
        else if ( t instanceof WebApplicationException ) {
            if ( t.getCause() instanceof InvalidPackageIdException ) {
                error.setCode( Response.Status.BAD_REQUEST.getStatusCode() );
                error.setMessage( t.getCause().getMessage() );
            }
            else {
                Response originalErrorResponse = ( (WebApplicationException)t ).getResponse();
                error.setCode( originalErrorResponse.getStatus() );
                error.setMessage( t.getMessage() );
            }
        }
        else {
            error.setCode( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() );
            error.setMessage( t.toString() );
        }
        log.errorf( "http-code: %d; message: %s", error.getCode(), error.getMessage() );
        log.tracef( t, "STACKTRACE" );
        return Response.status( error.getCode() ).entity( error ).build();
    }

}
