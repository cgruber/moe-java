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
 * An Operation in the MOE Expression Language is an operator followed by a term.
 *
 * <P>E.g., |patch(file="/path/to/path.txt") or >public
 */
// TODO(cgruber): Convert this to an autovalue or at least fix the hashcode/equals issue.
public class Operation {

  public final Operator operator;
  public final Term term;

  public Operation(Operator operator, Term term) {
    this.operator = operator;
    this.term = term;
  }

  @Override
  public String toString() {
    return operator.toString() + term.toString();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Operation && toString().equals(o.toString());
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }
}
