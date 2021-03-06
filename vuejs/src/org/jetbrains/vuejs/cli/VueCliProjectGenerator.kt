// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.cli

import com.intellij.ide.util.projectWizard.*
import com.intellij.lang.javascript.boilerplate.NpmPackageGeneratorPeerExtensible
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.impl.welcomeScreen.AbstractActionWithPanel
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.platform.HideableProjectGenerator
import com.intellij.platform.WebProjectGenerator
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.ui.UIUtil
import icons.VuejsIcons
import org.jetbrains.vuejs.VueBundle
import java.awt.BorderLayout
import java.awt.event.InputMethodEvent
import java.awt.event.InputMethodListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.*
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel

class VueCliProjectGenerator : WebProjectTemplate<NpmPackageProjectGenerator.Settings>(),
                               CustomStepProjectGenerator<NpmPackageProjectGenerator.Settings>,
                               HideableProjectGenerator {

  override fun isHidden(): Boolean = false

  override fun createStep(projectGenerator: DirectoryProjectGenerator<NpmPackageProjectGenerator.Settings>?,
                          callback: AbstractNewProjectStep.AbstractCallback<NpmPackageProjectGenerator.Settings>?): AbstractActionWithPanel {
    return VueCliProjectSettingsStep(projectGenerator, callback)
  }

  override fun createModuleBuilder(): ModuleBuilder {
    return VueCreateProjectModuleBuilder(this)
  }

  @Suppress("DEPRECATION")
  override fun createPeer(): GeneratorPeer<NpmPackageProjectGenerator.Settings> {
    return VueCliGeneratorSettingsPeer()
  }

  override fun generateProject(project: Project, baseDir: VirtualFile, settings: NpmPackageProjectGenerator.Settings, module: Module) {
  }

  override fun getName(): String = "Vue.js"
  override fun getDescription(): String = VueBundle.message("vue.project.generator.description")
  override fun getIcon(): Icon = VuejsIcons.Vue!!
}

class VueCliGeneratorSettingsPeer : NpmPackageGeneratorPeerExtensible(Arrays.asList("vue-cli", "@vue/cli"),
                                                                      "vue-cli or @vue/cli", { null }) {
  companion object {
    internal val TEMPLATE_KEY = Key.create<String>("create.vue.app.project.template")
    private val TEMPLATES = mapOf(
      Pair("browserify", "A full-featured Browserify + vueify setup with hot-reload, linting & unit testing"),
      Pair("browserify-simple", "A simple Browserify + vueify setup for quick prototyping"),
      Pair("pwa", "PWA template for vue-cli based on the webpack template"),
      Pair("simple", "The simplest possible Vue setup in a single HTML file"),
      Pair("webpack", "A full-featured Webpack + vue-loader setup with hot reload, linting, testing & css extraction"),
      Pair("webpack-simple", "A simple Webpack + vue-loader setup for quick prototyping")
    )
  }

  var template: ComboBox<*>? = null

  override fun createPanel(): JPanel {
    val panel = super.createPanel()
    template = ComboBox(TEMPLATES.keys.toTypedArray())
    template!!.selectedItem = "webpack"
    template!!.isEditable = true
    template!!.renderer = object : ColoredListCellRenderer<Any?>() {
      override fun customizeCellRenderer(list: JList<out Any?>, value: Any?, index: Int, selected: Boolean, hasFocus: Boolean) {
        if (value is String) {
          append(value)
          val comment = TEMPLATES[value] ?: return
          append(" ")
          append(comment, SimpleTextAttributes.GRAYED_ATTRIBUTES)
        }
      }
    }
    val component = LabeledComponent.create(template!!, "Project template")
    component.labelLocation = BorderLayout.WEST
    component.anchor = panel.getComponent(0) as JComponent
    panel.add(component)

    myPackageField.addSelectionListener {
      UIUtil.setEnabled(component, isOldPackage(), true)
    }
    UIUtil.setEnabled(component, isOldPackage(), true)
    return panel
  }

  private fun isOldPackage() = !myPackageField.selected.name.contains("@vue")

  @Suppress("OverridingDeprecatedMember", "DEPRECATION")
  override fun addSettingsStateListener(listener: WebProjectGenerator.SettingsStateListener) {
    super.addSettingsStateListener(listener)
    template!!.editor.editorComponent.addKeyListener(object : KeyAdapter() {
      override fun keyReleased(e: KeyEvent?) {
        listener.stateChanged(validate() == null)
      }
    })
    template!!.editor.editorComponent.addInputMethodListener(object : InputMethodListener {
      override fun caretPositionChanged(event: InputMethodEvent?) {
      }

      override fun inputMethodTextChanged(event: InputMethodEvent?) {
        listener.stateChanged(validate() == null)
      }
    })
    template!!.addItemListener { listener.stateChanged(validate() == null) }
  }

  override fun validate(): ValidationInfo? {
    val validate = super.validate()
    if (validate == null && isOldPackage()) {
      if (template!!.editor.item.toString().isBlank()) {
        return ValidationInfo("Please enter project template", template!!)
      }
    }
    return validate
  }

  override fun buildUI(settingsStep: SettingsStep) {
    super.buildUI(settingsStep)
    settingsStep.addSettingsField("Project template", template!!)
  }

  override fun getSettings(): NpmPackageProjectGenerator.Settings {
    val settings = super.getSettings()
    val text = template!!.selectedItem as? String
    if (text != null) {
      settings.putUserData<String>(TEMPLATE_KEY, text)
    }
    return settings
  }
}
