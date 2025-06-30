/*
 * Copyright (c) 2025, Salesforce, Inc.
 * SPDX-License-Identifier: Apache-2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.force.i18n.commons.util.collection;

import com.google.common.annotations.Beta;

/**
 * An iterator over a collection of ints.
 *
 * Beta class. Classes under com.force.i18n.commons package will be moved into a dedicated project.
 *
 * @author  Based on Sun's java.util.Iterator (modified by koliver)
 * @see java.util.Iterator
 */
@Beta
public interface IntIterator {
    /**
     * Returns {@code true} if the iteration has more elements. (In other
     * words, returns {@code true} if {@code next} would return an element
     * rather than throwing an exception.)
     *
     * @return {@code true} if the iterator has more elements.
     */
    boolean hasNext();

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     */
    int next();

    /*
    * IMPORTANT: this intentionally doesn't have the remove() operation because
    * it makes implementing an unmodifiable IntMap horrifically difficult (see
    * Collections.unmodifiableMap) and no one was using it. Don't add it.
    */
}
