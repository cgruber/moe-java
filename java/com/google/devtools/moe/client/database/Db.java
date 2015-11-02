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

import com.google.devtools.moe.client.project.InvalidProject;
import com.google.devtools.moe.client.repositories.Revision;

import java.util.Set;

/**
 * An abstraction of MOE's database.
 */
public interface Db {

  /** Returns the location from which this database was created. */
  String location();

  /** Adds an Equivalence to this Db. */
  void noteEquivalence(RepositoryEquivalence equivalence);

  /**
   * Returns the Revisions in Repository {@code otherRepository} that are equivalent to the given
   * Revision.
   *
   * @param revision  the Revision to find equivalent revisions for
   * @param otherRepository  the Repository to find equivalent revisions in
   */
  Set<Revision> findEquivalences(Revision revision, String otherRepository);

  /**
   * Stores a SubmittedMigration in this Db. Migrations are stored along with Equivalences to give
   * full historical information for runs of MOE, as not all migrations result in an Equivalence.
   *
   * @param migration  the SubmittedMigration to add to the database
   * @return true if the SubmittedMigration was newly added, false if it was already in this Db
   */
  boolean noteMigration(SubmittedMigration migration);

  /**
   * A means by which implementations can supply their internal storage value object.
   *
   * <p>This is a way of permitting what would normally be a violation of encapsulation for
   * related storage types.  For instance, a file-based db would have an appropriate writer
   * which would write itself to disk. But as long as the underlying system supported
   * {@link DbStorage} then it could write to a file a db that originated elsewhere, from a
   * url, or a data store.
   */
  public interface HasDbStorage {

    /** Supplies the underlying storage value holder */
    DbStorage getStorage();
  }

  /**
   * An object that writes a database to some location.
   */
  public interface Writer {
    /**
     * Writes the Db contents to the location from which it originated.
     */
    void write(Db db);

    /**
     * Writes the Db contents to the given location.
     */
    void writeToLocation(String dbLocation, Db db);
  }

  /**
   * A factory to produce {@link Db} instances.
   */
  public interface Factory {
    /** Parse db contents from a JSON string */
    Db parseJson(String jsonDb) throws InvalidProject;

    /** Load a database from the given path */
    Db load(String path);
  }
}
