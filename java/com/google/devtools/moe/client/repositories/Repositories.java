/*
 * Copyright (c) 2011 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.moe.client.repositories;

import static dagger.Provides.Type.SET;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.devtools.moe.client.dvcs.git.GitRepositoryFactory;
import com.google.devtools.moe.client.dvcs.hg.HgRepositoryFactory;
import com.google.devtools.moe.client.project.InvalidProject;
import com.google.devtools.moe.client.project.RepositoryConfig;
import com.google.devtools.moe.client.repositories.noop.NoopRepositoryFactory;
import com.google.devtools.moe.client.svn.SvnRepositoryFactory;

import dagger.Provides;

import java.util.Set;

import javax.inject.Inject;

/**
 * Creates a {@link RepositoryType} of the given kind, acting as a routing object between
 * various kinds of repository types and their factories.
 */
public class Repositories implements RepositoryType.Factory {
  // TODO(cgruber): Make this less of a holder, and more of a thing (Law of Demeter, folks...)

  private final ImmutableMap<String, RepositoryType.Factory> serviceFactories;

  @Inject
  public Repositories(Set<RepositoryType.Factory> services) {
    // A Set of services is expected, and indexed by this class, so that a more dynamic set
    // of Repositories can be dynamically detected, as opposed to using a static map binder
    this.serviceFactories =
        FluentIterable.from(services)
            .uniqueIndex(
                new Function<RepositoryType.Factory, String>() {
                  @Override
                  public String apply(RepositoryType.Factory input) {
                    return input.type();
                  }
                });
  }

  @Override
  public String type() {
    return "aggregate";
  }

  @Override
  public RepositoryType create(String name, RepositoryConfig config) throws InvalidProject {
    if (name.equals("file")) {
      throw new InvalidProject("Invalid repository name (reserved keyword): \"" + name + "\"");
    }
    RepositoryType.Factory factoryForConfig = serviceFactories.get(config.getType());
    if (factoryForConfig == null) {
      throw new InvalidProject("Invalid repository type: \"" + config.getType() + "\"");
    }
    return factoryForConfig.create(name, config);
  }

  /**
   * A dagger module which provides the {@link RepositoryType.Factory} implementations for
   * the repository types which are supported by default.
   */
  @dagger.Module
  public static class Defaults {
    @Provides(type = SET)
    static RepositoryType.Factory svn(SvnRepositoryFactory concrete) {
      return concrete;
    }

    @Provides(type = SET)
    static RepositoryType.Factory hg(HgRepositoryFactory concrete) {
      return concrete;
    }

    @Provides(type = SET)
    static RepositoryType.Factory git(GitRepositoryFactory concrete) {
      return concrete;
    }

    @Provides(type = SET)
    static RepositoryType.Factory noop(NoopRepositoryFactory concrete) {
      return concrete;
    }
  }
}
