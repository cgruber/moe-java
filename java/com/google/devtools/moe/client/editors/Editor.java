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

package com.google.devtools.moe.client.editors;

import com.google.devtools.moe.client.codebase.Codebase;
import com.google.devtools.moe.client.project.ProjectContext;

import java.util.Map;

/**
 * An Editor takes one Codebase and returns an edited Codebase.
 */
public interface Editor {

  /**
   * Returns a description of what this editor will do.
   */
  public String getDescription();

  /**
   * Takes in a Codebase and returns a new, edited version of that Codebase.
   *
   * @param input  the Codebase to edit
   * @param context  the ProjectContext for this Editor's project
   * @param options  command-line parameters
   * @return a new Codebase that is an edited version of the input
   */
  public Codebase edit(Codebase input, ProjectContext context, Map<String, String> options);
}
