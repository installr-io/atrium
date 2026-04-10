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
package io.installr.atrium.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class TestBase {

    public static final Path TEST_RESOURCES_PATH = Path.of( "/tmp/installr.io/test-resources" );
    public static final String TEST_REPOSITORY_ID = "test-repository-a";

    @BeforeAll
    static final void baseSetup() throws Exception {
        Files.createDirectories( TEST_RESOURCES_PATH );
    }

    @AfterAll
    static final void baseTeardown() throws Exception {
        deleteDirectory( TEST_RESOURCES_PATH );
    }

    protected static final void deleteDirectory( final Path directory ) throws IOException {
        Files.walk( directory )
            .sorted( Comparator.reverseOrder() )
            .map( Path::toFile )
            .forEach( File::delete );
    }

    protected final Path copyResource( final String resource ) throws Exception {
        final Path orig = Path.of( getClass().getResource( resource ).toURI() );
        final Path copy = TEST_RESOURCES_PATH.resolve( orig.getFileName().toString() );
        Files.copy( orig, copy );
        return copy;
    }

}
