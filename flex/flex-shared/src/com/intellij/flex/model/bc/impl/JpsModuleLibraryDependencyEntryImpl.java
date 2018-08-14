package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.JpsLibraryDependencyEntry;
import com.intellij.flex.model.bc.LinkageType;
import com.intellij.flex.model.lib.JpsFlexLibraryProperties;
import com.intellij.flex.model.lib.JpsFlexLibraryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.library.JpsLibrary;
import org.jetbrains.jps.model.library.JpsTypedLibrary;
import org.jetbrains.jps.model.module.JpsModule;

class JpsModuleLibraryDependencyEntryImpl extends JpsFlexDependencyEntryBase<JpsModuleLibraryDependencyEntryImpl>
  implements JpsLibraryDependencyEntry {

  private String myLibraryId;

  JpsModuleLibraryDependencyEntryImpl(final String libraryId, final LinkageType linkageType) {
    super(linkageType);
    myLibraryId = libraryId;
  }

  private JpsModuleLibraryDependencyEntryImpl(final JpsModuleLibraryDependencyEntryImpl original) {
    super(original);
    myLibraryId = original.myLibraryId;
  }

  @Override
  @NotNull
  public JpsModuleLibraryDependencyEntryImpl createCopy() {
    return new JpsModuleLibraryDependencyEntryImpl(this);
  }

  @Override
  public void applyChanges(@NotNull final JpsModuleLibraryDependencyEntryImpl modified) {
    super.applyChanges(modified);
    myLibraryId = modified.myLibraryId;
  }

// ------------------------------------

  @Override
  @Nullable
  public JpsLibrary getLibrary() {
    final JpsModule module = ((JpsFlexBuildConfiguration)myParent.getParent().getParent()).getModule();

    for (JpsTypedLibrary<JpsSimpleElement<JpsFlexLibraryProperties>> library : module.getLibraryCollection()
      .getLibraries(JpsFlexLibraryType.INSTANCE)) {

      if (myLibraryId.equals(library.getProperties().getData().getLibraryId())) {
        return library;
      }
    }
    return null;
  }
}
