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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.installr.lib.pkg.Package;
import io.installr.lib.pkg.PackageId;
import io.installr.lib.registry.PackageUpload;
import io.installr.lib.registry.RepositoryResource;
import io.installr.lib.registry.repository.Repository;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class RepositoryResourceImpl implements RepositoryResource {

    @Inject
    RegistryService registry;

    @Override
    public List<Repository> listRepositories() {
        return registry.getRepositories();
    }

    @Override
    public Repository getRepository( @NonNull @PathParam( "repositoryId" ) final String repositoryId ) {
        return registry.getRepository( repositoryId );
    }

    @Override
    public Uni<Response> index( @NonNull @PathParam( "repositoryId" ) final String repositoryId ) {
        final Path indexFile = getRepository( repositoryId ).getIndex().indexFile();
        if ( !Files.exists( indexFile ) ) {
            return Uni.createFrom()
                .item( Response.status( 500, "index.gz not found" ).build() );
        }
        return Uni.createFrom().item(
            Response.ok( indexFile )
                .header( "Content-Disposition", "attachment; filename=index.gz" )
                .build()
        );
    }

    @Override
    public List<Package> getPackages( @NonNull @PathParam( "repositoryId" ) final String repositoryId ) {
        return new ArrayList<>( registry.getRepository( repositoryId ).getIndex().packages().values() );
    }

    /*
     * curl -fsSL -JO http://localhost:8080/repositories/repo-a/packages/test-package-a:0.1.0
     */
    @Override
    public Uni<Response> getPackage(
        @NonNull @PathParam( "repositoryId" ) final String repositoryId,
        @NonNull @PathParam( "packageId" ) final PackageId packageId
    ) {
        final Package pkg = registry.getRepository( repositoryId ).getIndex().get( packageId );
        if ( pkg == null ) {
            throw new NotFoundException();
        }
        return Uni.createFrom().item(
            Response.ok( pkg.getLocalPath() )
                .header( "Content-Disposition", "attachment; filename=" + packageId.toPackageName() )
                .build()
        );
    }

    /**
     * curl -fsSL -i -X PUT -H "Content-Type: multipart/form-data" -F
     * "packageData=@./registry/src/test/resources/packages/test-package-a.ipf"
     * http://localhost:8080/repositories/repo-a/packages
     */
    @Override
    @SneakyThrows
    public Response putPackage( @Valid final PackageUpload packageUpload ) {
        final Path packagePath = Path.of( "/tmp", UUID.randomUUID().toString() + ".ipf" );
        try {
            Files.write( packagePath, packageUpload.getPackageData() );
            final Package pkg = Package.loadFromFile( packagePath );
            registry.getRepository( packageUpload.getRepositoryId() ).getIndex().put( pkg );
            return Response.created( pkg.getUri() ).build();
        }
        catch ( IOException ex ) {
            log.errorf( ex, "Error uploading package: %s", ex.getMessage() );
            return Response.serverError().build();
        }
        finally {
            Files.deleteIfExists( packagePath );
        }
    }

    /**
     * curl -fsSL -i -X DELETE http://localhost:8080/repositories/repo-a/packages/test-package-a:0.1.0
     */
    @Override
    public Response deletePackage(
        @NonNull @PathParam( "repositoryId" ) final String repositoryId,
        @NonNull @PathParam( "packageId" ) final PackageId packageId
    ) {
        registry.getRepository( repositoryId ).getIndex().rm( packageId );
        return Response.noContent().build();
    }

}
