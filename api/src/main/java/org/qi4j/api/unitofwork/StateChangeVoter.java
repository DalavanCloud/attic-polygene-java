/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.api.unitofwork;

/**
 * Implement this interface to vote on state changes
 * in Entities in a UnitOfWork. Register your voter
 * by calling {@link UnitOfWork#addStateChangeVoter(StateChangeVoter)}.
 */
public interface StateChangeVoter
{
    /**
     * Vote on a state change. Do nothing to accept it,
     * and throw ChangeVetoException, with a possible explanation,
     * to reject it.
     *
     * @param change the state change event
     * @throws ChangeVetoException thrown if this voter rejects the change
     */
    void acceptChange(StateChange change)
        throws ChangeVetoException;
}