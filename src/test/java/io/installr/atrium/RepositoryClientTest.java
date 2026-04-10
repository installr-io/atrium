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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.installr.atrium.util.TestBase;
import io.installr.lib.pkg.PackageId;
import io.installr.lib.registry.PackageUpload;
import io.installr.lib.registry.client.RepositoryClient;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestMethodOrder( OrderAnnotation.class )
class RepositoryClientTest extends TestBase {

    @BeforeAll
    static void setup() throws IOException {
        Path repoDir = ConfigProvider
            .getConfig().getValue( "atrium.repo-dir", Path.class );
        Files.createDirectories( repoDir );
    }

    @AfterAll
    static void teardown() throws IOException {
        Path repoDir = ConfigProvider
            .getConfig().getValue( "atrium.repo-dir", Path.class );
        deleteDirectory( repoDir );
    }

    @Test
    @Order( 1 )
    void registry_should_have_one_empty_repository() {
        // assertEquals( 1, client.listRepositories().size() );
    }

    @Test
    @Order( 2 )
    void putPackage_should_throw_exception_when_repository_does_not_exit() throws Exception {
        final Path packageFile = copyResource( "/packages/test-package-a.ipf" );
        final PackageUpload upload = new PackageUpload();
        upload.setPackageData( Files.readAllBytes( packageFile ) );
        upload.setRepositoryId( "this-repo-does-not-exist" );

        final RepositoryClient client = new RepositoryClient( createClientUrl( "this-repo-does-not-exist" ) );
        final ClientWebApplicationException ex = assertThrows(
            ClientWebApplicationException.class,
            () -> client.uploadPackage( upload )
        );

        assertEquals( 400, ex.getResponse().getStatus() );
        Files.delete( TEST_RESOURCES_PATH.resolve( "test-package-a.ipf" ) );
    }

    private URL createClientUrl( String repositoryId ) throws MalformedURLException {
        return URI.create( "http://localhost:9371/repositories/" + repositoryId ).toURL();
    }

    @Test
    @Order( 3 )
    void putPackage_should_put_the_package_to_the_repository() throws Exception {
        final Path packageFile = copyResource( "/packages/test-package-a.ipf" );
        final PackageUpload upload = new PackageUpload();
        upload.setPackageData( Files.readAllBytes( packageFile ) );
        upload.setRepositoryId( TEST_REPOSITORY_ID );

        final RepositoryClient client = new RepositoryClient( createClientUrl( TEST_REPOSITORY_ID ) );
        client.uploadPackage( upload );
        final PackageId pkgA = PackageId.valueOf( "test-package-a:0.1.0" );
        assertEquals( pkgA, client.getPackages().get( 0 ).getPackageInfo().getPackageId() );
        Files.delete( TEST_RESOURCES_PATH.resolve( "test-package-a.ipf" ) );
    }

    @Test
    @Order( 4 )
    void downloadIndex_should_download_the_index_file() throws Exception {
        final Path packageFile = copyResource( "/packages/test-package-a.ipf" );
        final PackageUpload upload = new PackageUpload();
        upload.setPackageData( Files.readAllBytes( packageFile ) );
        upload.setRepositoryId( TEST_REPOSITORY_ID );

        final RepositoryClient client = new RepositoryClient( createClientUrl( TEST_REPOSITORY_ID ) );
        client.uploadPackage( upload );

        final Path indexFile = client.downloadIndex( Path.of( "/tmp" ) );
        assertTrue( Files.exists( indexFile ) );
        Files.delete( indexFile );
        Files.delete( TEST_RESOURCES_PATH.resolve( "test-package-a.ipf" ) );
    }

    @Test
    @Order( 5 )
    void downloadPackage_should_download_the_package() throws Exception {
        Path packageFile = copyResource( "/packages/test-package-a.ipf" );
        final PackageUpload upload = new PackageUpload();
        upload.setPackageData( Files.readAllBytes( packageFile ) );
        upload.setRepositoryId( TEST_REPOSITORY_ID );

        final RepositoryClient client = new RepositoryClient( createClientUrl( TEST_REPOSITORY_ID ) );
        client.uploadPackage( upload );

        packageFile = client.downloadPackage( Path.of( "/tmp" ), PackageId.valueOf( "test-package-a:0.1.0" ) );

        assertTrue( Files.exists( packageFile ) );
        Files.delete( packageFile );
        Files.delete( TEST_RESOURCES_PATH.resolve( "test-package-a.ipf" ) );
    }

}
