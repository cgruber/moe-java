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

package com.google.devtools.moe.client.svn;

import static org.easymock.EasyMock.expect;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.moe.client.CommandRunner;
import com.google.devtools.moe.client.FileSystem;
import com.google.devtools.moe.client.Injector;
import com.google.devtools.moe.client.codebase.Codebase;
import com.google.devtools.moe.client.codebase.CodebaseCreator;
import com.google.devtools.moe.client.project.RepositoryConfig;
import com.google.devtools.moe.client.repositories.Revision;
import com.google.devtools.moe.client.testing.TestingModule;

import dagger.Provides;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import java.io.File;

import javax.inject.Singleton;

public class SvnCodebaseCreatorTest extends TestCase {
  private final IMocksControl control = EasyMock.createControl();
  private final FileSystem fileSystem = control.createMock(FileSystem.class);
  private final CommandRunner cmd = control.createMock(CommandRunner.class);
  private final SvnUtil util = new SvnUtil(cmd);

  // TODO(cgruber): Rework these when statics aren't inherent in the design.
  @dagger.Component(modules = {TestingModule.class, Module.class})
  @Singleton
  interface Component {
    Injector context(); // TODO (b/19676630) Remove when bug is fixed.
  }

  @dagger.Module
  class Module {
    @Provides
    public CommandRunner commandRunner() {
      return cmd;
    }

    @Provides
    public FileSystem fileSystem() {
      return fileSystem;
    }
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Injector.INSTANCE =
        DaggerSvnCodebaseCreatorTest_Component.builder().module(new Module()).build().context();
  }

  public void testExportExplicitRevision() throws Exception {
    Revision result = Revision.create(45, "");
    SvnRevisionHistory revisionHistory = control.createMock(SvnRevisionHistory.class);

    RepositoryConfig mockConfig = control.createMock(RepositoryConfig.class);
    expect(mockConfig.getUrl()).andReturn("http://foo/svn/trunk/").anyTimes();
    expect(mockConfig.getProjectSpace()).andReturn("internal").anyTimes();
    expect(mockConfig.getIgnoreFilePatterns()).andReturn(ImmutableList.<String>of()).anyTimes();

    expect(revisionHistory.findHighestRevision("46")).andReturn(result);
    expect(fileSystem.getTemporaryDirectory("svn_export_testing_45_"))
        .andReturn(new File("/dummy/path/45"));
    expect(
            cmd.runCommand(
                "svn",
                ImmutableList.of(
                    "--no-auth-cache",
                    "export",
                    "http://foo/svn/trunk/",
                    "-r",
                    "45",
                    "/dummy/path/45"),
                ""))
        .andReturn("");
    // Short-circuit Utils.filterFiles for ignore_files_re.
    expect(Injector.INSTANCE.fileSystem().findFiles(new File("/dummy/path/45")))
        .andReturn(ImmutableSet.<File>of());

    control.replay();
    CodebaseCreator cc =
        new SvnCodebaseCreator(fileSystem, "testing", mockConfig, revisionHistory, util);
    Codebase r = cc.create(ImmutableMap.of("revision", "46"));
    assertEquals("/dummy/path/45", r.getPath().getAbsolutePath());
    assertEquals("internal", r.getProjectSpace());
    control.verify();
  }
}
