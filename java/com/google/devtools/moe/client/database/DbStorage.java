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

package com.google.devtools.moe.client.database;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * MOE's database, storing all Equivalences and SubmittedMigrations in order from those between
 * lower revisions to those between higher revisions.
 *
 * <p>This class is used for serialization of a database file.
 *
 */
public class DbStorage {

  private final List<RepositoryEquivalence> equivalences;
  private final List<SubmittedMigration> migrations;

  public DbStorage() {
    equivalences = Lists.newArrayList();
    migrations = Lists.newArrayList();
  } // Constructed by gson.

  public List<RepositoryEquivalence> getEquivalences() {
    return ImmutableList.copyOf(equivalences);
  }

  public List<SubmittedMigration> getMigrations() {
    return ImmutableList.copyOf(migrations);
  }

  public void addEquivalence(RepositoryEquivalence e) {
    if (!equivalences.contains(e)) {
      equivalences.add(e);
    }
  }

  /**
   * Adds a SubmittedMigration.
   *
   * @return true if the SubmittedMigration was newly added, false if it was already in this Db
   */
  public boolean addMigration(SubmittedMigration m) {
    return !migrations.contains(m) && migrations.add(m);
  }
}
