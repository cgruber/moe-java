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

package com.google.devtools.moe.client.parser;

import static org.easymock.EasyMock.expect;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.devtools.moe.client.FileSystem;
import com.google.devtools.moe.client.FileSystem.Lifetime;
import com.google.devtools.moe.client.Injector;
import com.google.devtools.moe.client.MoeProblem;
import com.google.devtools.moe.client.NullFileSystemModule;
import com.google.devtools.moe.client.SystemCommandRunner;
import com.google.devtools.moe.client.codebase.Codebase;
import com.google.devtools.moe.client.codebase.CodebaseCreationError;
import com.google.devtools.moe.client.codebase.CodebaseCreator;
import com.google.devtools.moe.client.editors.Editor;
import com.google.devtools.moe.client.editors.ForwardTranslator;
import com.google.devtools.moe.client.editors.Translator;
import com.google.devtools.moe.client.editors.TranslatorPath;
import com.google.devtools.moe.client.editors.TranslatorStep;
import com.google.devtools.moe.client.project.FakeProjectContext;
import com.google.devtools.moe.client.project.ProjectContext;
import com.google.devtools.moe.client.repositories.RepositoryType;
import com.google.devtools.moe.client.repositories.RevisionHistory;
import com.google.devtools.moe.client.testing.TestingModule;
import com.google.devtools.moe.client.writer.WriterCreator;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import java.io.File;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Singleton;

public class ExpressionTest extends TestCase {
  private static final Map<String, String> EMPTY_MAP = ImmutableMap.of();

  // TODO(cgruber): Rework these when statics aren't inherent in the design.
  @dagger.Component(
      modules = {TestingModule.class, SystemCommandRunner.Module.class, NullFileSystemModule.class})
  @Singleton
  interface Component {
    Injector context(); // TODO (b/19676630) Remove when bug is fixed.
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Injector.INSTANCE = DaggerExpressionTest_Component.create().context();
  }

  public void testNoSuchCreator() throws Exception {
    try {
      new RepositoryExpression("foo").createCodebase(new FakeProjectContext());
      fail();
    } catch (MoeProblem expected) {
      assertEquals("No such repository 'foo' in the config. Found: []", expected.getMessage());
    }
  }

  public void testFileCodebaseCreator() throws Exception {
    IMocksControl control = EasyMock.createControl();
    final FileSystem mockFs = control.createMock(FileSystem.class);
    Injector.INSTANCE =
        DaggerExpressionTest_Component.builder()
            .nullFileSystemModule(
                new NullFileSystemModule() {
                  @Override
                  @Nullable
                  public FileSystem filesystem() {
                    return mockFs;
                  }
                })
            .build()
            .context();
    expect(mockFs.exists(new File("/foo"))).andReturn(true);
    expect(mockFs.isDirectory(new File("/foo"))).andReturn(true);
    File copyLocation = new File("/tmp/copy");
    expect(mockFs.getTemporaryDirectory("file_codebase_copy_")).andReturn(copyLocation);
    // Short-circuit Utils.copyDirectory().
    mockFs.makeDirsForFile(copyLocation);
    expect(mockFs.isFile(new File("/foo"))).andReturn(true);
    mockFs.copyFile(new File("/foo"), copyLocation);
    mockFs.setLifetime(EasyMock.eq(copyLocation), EasyMock.<Lifetime>anyObject());
    mockFs.cleanUpTempDirs();

    RepositoryExpression repoEx = new RepositoryExpression("file").withOption("path", "/foo");

    control.replay();
    Codebase c = repoEx.createCodebase(new FakeProjectContext());
    control.verify();

    assertEquals(copyLocation, c.getPath());
    assertEquals("public", c.getProjectSpace());
    assertEquals(repoEx, c.getExpression());
  }

  public void testNoSuchEditor() throws Exception {
    try {
      ProjectContext context = new FakeProjectContext();

      IMocksControl control = EasyMock.createControl();
      RepositoryExpression mockRepoEx = control.createMock(RepositoryExpression.class);
      expect(mockRepoEx.createCodebase(context)).andReturn(null); // Codebase unneeded

      Expression ex =
          new EditExpression(
              mockRepoEx, new Operation(Operator.EDIT, new Term("noSuchEditor", EMPTY_MAP)));

      control.replay();
      ex.createCodebase(context);
      fail();
    } catch (CodebaseCreationError expected) {
      assertEquals("no editor noSuchEditor", expected.getMessage());
    }
  }

  public void testNoSuchTranslator() throws Exception {
    try {
      final TranslatorPath tPath = new TranslatorPath("foo", "bar");
      final Translator t =
          new ForwardTranslator(ImmutableList.<TranslatorStep>of(new TranslatorStep("quux", null)));
      ProjectContext context =
          new FakeProjectContext() {
            @Override
            public ImmutableMap<TranslatorPath, Translator> translators() {
              return ImmutableMap.of(tPath, t);
            }
          };

      IMocksControl control = EasyMock.createControl();
      RepositoryExpression mockRepoEx = control.createMock(RepositoryExpression.class);
      Codebase mockRepoCodebase = control.createMock(Codebase.class);
      expect(mockRepoCodebase.getProjectSpace()).andReturn("internal").times(2);
      expect(mockRepoEx.createCodebase(context)).andReturn(mockRepoCodebase);

      Expression ex =
          new TranslateExpression(
              mockRepoEx, new Operation(Operator.TRANSLATE, new Term("public", EMPTY_MAP)));

      control.replay();
      ex.createCodebase(context);
      fail();
    } catch (CodebaseCreationError expected) {
      assertEquals(
          "Could not find translator from project space \"internal\" to \"public\".\n"
              + "Translators only available for [foo>bar]",
          expected.getMessage());
    }
  }

  public void testParseAndEvaluate() throws Exception {
    IMocksControl control = EasyMock.createControl();
    final RevisionHistory rh = control.createMock(RevisionHistory.class);
    final CodebaseCreator cc = control.createMock(CodebaseCreator.class);
    final WriterCreator wc = control.createMock(WriterCreator.class);
    final Editor e = control.createMock(Editor.class);
    Editor translatorEditor = control.createMock(Editor.class);

    File firstDir = new File("/first");
    File secondDir = new File("/second");
    File finalDir = new File("/final");

    final TranslatorPath tPath = new TranslatorPath("foo", "public");
    final Translator t =
        new ForwardTranslator(
            ImmutableList.<TranslatorStep>of(new TranslatorStep("quux", translatorEditor)));

    ProjectContext context =
        new FakeProjectContext() {
          @Override
          public ImmutableMap<String, RepositoryType> repositories() {
            return ImmutableMap.of("foo", RepositoryType.create("foo", rh, cc, wc));
          }

          @Override
          public ImmutableMap<TranslatorPath, Translator> translators() {
            return ImmutableMap.of(tPath, t);
          }

          @Override
          public ImmutableMap<String, Editor> editors() {
            return ImmutableMap.of("bar", e);
          }
        };

    Codebase firstCb =
        new Codebase(null, firstDir, "foo", new RepositoryExpression(new Term("foo", EMPTY_MAP)));

    Codebase secondCb =
        new Codebase(
            null, secondDir, "public", new RepositoryExpression(new Term("foo2", EMPTY_MAP)));

    Codebase finalCb =
        new Codebase(
            null, finalDir, "public", new RepositoryExpression(new Term("foo3", EMPTY_MAP)));

    expect(cc.create(EMPTY_MAP)).andReturn(firstCb);
    expect(translatorEditor.edit(firstCb, context, EMPTY_MAP)).andReturn(secondCb);
    expect(e.getDescription()).andReturn("");
    expect(e.edit(secondCb, context, EMPTY_MAP)).andReturn(finalCb);

    control.replay();
    Codebase c = Parser.parseExpression("foo>public|bar").createCodebase(context);

    control.verify();
    assertEquals(finalDir, c.getPath());
    assertEquals("public", c.getProjectSpace());
    assertEquals("foo>public|bar", c.getExpression().toString());
  }
}
