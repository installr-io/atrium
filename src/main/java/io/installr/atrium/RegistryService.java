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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.installr.atrium.config.RegistryConfiguration;
import io.installr.atrium.config.RepositoryConfiguration;
import io.installr.lib.registry.repository.Repository;
import io.installr.lib.registry.repository.RepositoryIndex;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import lombok.NonNull;
import lombok.extern.jbosslog.JBossLog;

@ApplicationScoped
@JBossLog
public class RegistryService {

    private Map<String, Repository> repositories;

    @Inject
    RegistryConfiguration config;

    @Inject
    UriInfo uriInfo;

    @PostConstruct
    void init() {
        repositories = config.repositories()
            .stream()
            .map( this::convertToRepository )
            .collect( Collectors.toMap( Repository::getId, v -> v ) );
        log.debugf( "RegistryService constructed" );
        log.debugf( "  repo-dir: %s", config.repoDir() );
        log.debugf( "  number of repositories: %d", repositories.size() );
    }

    private Repository convertToRepository( RepositoryConfiguration repoConfig ) {
        final String id = repoConfig.id();
        final URI repoUri = ( config.baseUri().isPresent()
            ? UriBuilder.fromUri( config.baseUri().get() )
            : uriInfo.getBaseUriBuilder() )
                .path( "/repositories/{name}" )
                .build( id );
        final Repository repo = new Repository();
        repo.setId( id );
        repo.setUri( repoUri );
        repo.setIndex( new RepositoryIndex( id, config.repoDir(), repoUri ) );
        return repo;
    }

    public List<Repository> getRepositories() {
        return new ArrayList<>( repositories.values() );
    }

    public Repository getRepository( @NonNull final String repositoryId ) {
        if ( !repositories.containsKey( repositoryId ) ) {
            throw new IllegalArgumentException( "repository not found: " + repositoryId );
        }
        return repositories.get( repositoryId );
    }

}
