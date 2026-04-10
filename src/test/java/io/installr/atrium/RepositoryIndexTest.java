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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.installr.atrium.util.TestBase;
import io.installr.lib.pkg.Package;
import io.installr.lib.pkg.PackageId;
import io.installr.lib.registry.repository.RepositoryIndex;

public class RepositoryIndexTest extends TestBase {

    @Test
    void constructor_should_throw_exception_when_repopath_does_not_exist() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new RepositoryIndex( "does-not-exist", Path.of( "/parent" ), URI.create( "/" ) )
        );
        assertEquals( "'/parent' is not a directory.", ex.getMessage() );
    }

    @Test
    void constructor_should_throw_exception_when_repopath_is_not_a_directory() throws Exception {
        final Path file = Path.of( "/tmp/path-is-a-file" );
        try {
            Files.createFile( file );
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new RepositoryIndex( "path-is-a-file", file.getParent(), URI.create( "/" ) )
            );
            assertEquals( "'/tmp/path-is-a-file' is not a directory.", ex.getMessage() );
        }
        finally {
            Files.deleteIfExists( file );
        }
    }

    @Test
    void readIndex_should_return_empty_map_when_indexfile_does_not_exist() {
        final RepositoryIndex index = new RepositoryIndex( "empty", Path.of( "/tmp" ), URI.create( "/" ) );
        assertEquals( 0, index.packageCount() );
    }

    @Test
    void readIndex_should_read_contents_of_indexfile() throws Exception {
        final Path repoPath = Path.of( getClass().getResource( "/repositories/test-repo-a" ).toURI() );
        assertTrue( Files.exists( repoPath ) );

        final RepositoryIndex index = new RepositoryIndex( "test-repo-a", repoPath.getParent(), URI.create( "/" ) );
        assertEquals( 2, index.packageCount() );

        final PackageId pkgA = PackageId.valueOf( "test-package-a:0.1.0" );
        assertEquals( pkgA, index.get( pkgA ).getPackageInfo().getPackageId() );

        final PackageId pkgB = PackageId.valueOf( "test-package-b:2.9.7-SNAPSHOT" );
        assertEquals( pkgB, index.get( pkgB ).getPackageInfo().getPackageId() );
    }

    @Test
    void put_should_store_binary_and_update_index() throws Exception {
        final Path repoPath = Path.of( "/tmp/repos" );
        try {
            Files.createDirectories( repoPath );

            final Path packageFile = copyResource( "/packages/test-package-a.ipf" );
            assertTrue( Files.exists( repoPath ) );

            Package pkg = Package.loadFromFile( packageFile );
            assertNotNull( pkg );

            final RepositoryIndex index = new RepositoryIndex(
                "atrium-test-repo",
                repoPath,
                URI.create( "/" )
            );
            index.put( pkg );

            final PackageId testPackageId = PackageId.valueOf( "test-package-a:0.1.0" );
            // package should be part of the in-memory package list
            assertNotNull( index.get( testPackageId ) );
            // index file should countain the new package
            final Path indexFile = index.indexFile();
            assertTrue( Files.exists( indexFile ) );
            assertNotNull(
                new RepositoryIndex( "atrium-test-repo", repoPath, URI.create( "/" ) ).get( testPackageId )
            );
            // package (ipf) should be placed in the packages directory of the repository
            assertTrue( Files.exists( index.get( testPackageId ).getLocalPath() ) );
        }
        finally {
            deleteDirectory( repoPath );
        }
    }

    @Test
    void rm_should_remove_binary_and_update_index() throws Exception {
        final Path repoPath = Path.of( "/tmp/repos" );
        try {
            // prepare
            Files.createDirectories( repoPath );

            final Path packageFile = copyResource( "/packages/test-package-a.ipf" );
            assertTrue( Files.exists( repoPath ) );

            Package pkg = Package.loadFromFile( packageFile );
            assertNotNull( pkg );

            final RepositoryIndex index = new RepositoryIndex(
                "atrium-test-repo",
                repoPath,
                URI.create( "/" )
            );
            index.put( pkg );

            // test
            final PackageId testPackageId = PackageId.valueOf( "test-package-a:0.1.0" );
            final Package testPackage = index.get( testPackageId );
            index.rm( testPackageId );

            // package should be removed from the in-memory package list
            assertNull( index.get( testPackageId ) );
            // index file should not contain the package
            assertNull(
                new RepositoryIndex( "atrium-test-repo", repoPath, URI.create( "/" ) ).get( testPackageId )
            );
            // package (ipf) should be removed from packages directory of the repository
            assertFalse( Files.exists( testPackage.getLocalPath() ) );
        }
        finally {
            deleteDirectory( repoPath );
        }
    }

}
