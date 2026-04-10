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
package io.installr.atrium.config;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping( prefix = "atrium" )
public interface RegistryConfiguration {

    @WithName( "config.location" )
    Path location();

    @WithName( "repo-dir" )
    @WithDefault( "${atrium.config.location}/repositories" )
    Path repoDir();

    @WithName( "base-uri" )
    Optional<URI> baseUri();

    List<RepositoryConfiguration> repositories();

}
