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

import com.google.auto.value.AutoValue;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.devtools.moe.client.AutoValueGsonAdapter;
import com.google.devtools.moe.client.MoeProblem;
import com.google.devtools.moe.client.parser.RepositoryExpression;
import com.google.devtools.moe.client.project.ProjectContext;
import com.google.gson.annotations.JsonAdapter;

import java.util.List;

/**
 * A Revision in a source control system.
 *
 * A dumb object with no mutable state.
 *
 * @author dbentley@google.com (Daniel Bentley)
 */
@AutoValue
@JsonAdapter(AutoValueGsonAdapter.class)
public abstract class Revision {
  /** The unique ID assigned to this revision by the underlying revision control system. */
  public abstract String revId();
  /** The label for the configured repository from which this revision originates. */
  public abstract String repositoryName();

  @Override
  public String toString() {
    return this.repositoryName() + "{" + revId() + "}";
  }

  /**
   * Creates a revision using a long ID for convenience, primarily in testing.
   *
   * @see #create(String, String)
   */
  public static Revision create(long revId, String repositoryName) {
    return create(String.valueOf(revId), repositoryName);
  }

  public static Revision create(String revId, String repositoryName) {
    return new AutoValue_Revision(revId, repositoryName);
  }

  /**
   * Return the list of Revisions given by a RepositoryExpression like "internal(revision=3,4,5)".
   */
  public static List<Revision> fromRepositoryExpression(
      RepositoryExpression repoEx, ProjectContext context) {
    RepositoryType repo = context.getRepository(repoEx.getRepositoryName());
    if (Strings.isNullOrEmpty(repoEx.getOption("revision"))) {
      throw new MoeProblem(
          "Repository expression must have a 'revision' option, e.g. internal(revision=3,4,5).");
    }

    RevisionHistory rh = repo.revisionHistory();
    ImmutableList.Builder<Revision> revBuilder = ImmutableList.builder();
    for (String revId : repoEx.getOption("revision").split(",")) {
      revBuilder.add(rh.findHighestRevision(revId));
    }
    return revBuilder.build();
  }
}
