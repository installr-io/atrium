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

import java.nio.file.Files;
import java.nio.file.Path;

import io.installr.atrium.config.RegistryConfiguration;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

@QuarkusMain
@JBossLog
public class Main {
    public static void main( String ... args ) {
        Quarkus.run( RegistryApp.class, args );
    }

    public static class RegistryApp implements QuarkusApplication {
        @Inject
        RegistryConfiguration config;

        @Override
        public int run( String ... args ) throws Exception {
            if ( !Files.exists( config.location() ) ) {
                log.errorf( "Directory does not exist: %s", config.location() );
                return 1;
            }
            final Path configFile = config.location().resolve( "config.yaml" );
            if ( !Files.exists( configFile ) ) {
                log.errorf( "Config not found: %s", configFile );
                return 1;
            }
            Files.createDirectories( config.repoDir() );
            log.infof( "Atrium - The Installr(d) Registry" );
            log.infof( "    location: %s", config.location() );
            log.infof( "    repo-dir: %s", config.repoDir() );
            log.infof( "    number of repositories: %d", config.repositories().size() );
            Quarkus.waitForExit();
            return 0;
        }
    }

}
