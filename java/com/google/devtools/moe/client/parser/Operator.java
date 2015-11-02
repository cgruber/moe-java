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

/**
 * Operators in the MOE Codebase Expression Language.
 */
public enum Operator {
  EDIT('|'),
  TRANSLATE('>');

  private final char op;

  Operator(char op) {
    this.op = op;
  }

  @Override
  public String toString() {
    return String.valueOf(op);
  }

  public static Operator getOperator(char c) throws IllegalArgumentException {
    if (c == '|') {
      return EDIT;
    }
    if (c == '>') {
      return TRANSLATE;
    }
    throw new IllegalArgumentException("Invalid operator: " + c);
  }
}
