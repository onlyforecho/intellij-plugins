/*
 * Copyright 2000-2006 JetBrains s.r.o.
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
package jetbrains.communicator.core.impl;

import jetbrains.communicator.core.commands.CommandManager;
import jetbrains.communicator.core.commands.UserCommand;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Kir
 */
public class CommandManagerImpl implements CommandManager {

  @Override
  public <T extends UserCommand> T getCommand(Class<T> commandClass, MutablePicoContainer registerIn) {
    T result = (T) registerIn.getComponentInstanceOfType(commandClass);
    if (result == null) {
      registerIn.registerComponentImplementation(commandClass);
      result = (T) registerIn.getComponentInstanceOfType(commandClass);
    }
    return result;
  }
}
