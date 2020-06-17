package com.icthh.xm.extensions.entityspec

import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.yaml.YAMLElementGenerator
import org.jetbrains.yaml.psi.YAMLKeyValue
import java.awt.Color

class XmEntitySpecElementColorProvider: ElementColorProvider {
    override fun setColorTo(psiElement: PsiElement, color: Color) {
        if (psiElement !is LeafPsiElement || !psiElement.isEntitySpecification()) {
            return
        }

        val element = psiElement.parent?.parent ?: return
        if (element is YAMLKeyValue && element.keyText.equals("color")) {
            val elementGenerator = YAMLElementGenerator.getInstance(element.project)
            val hex = "#" + Integer.toHexString(color.getRGB()).substring(2)
            val colorKeyValue = elementGenerator.createYamlKeyValue("color", "\"${hex}\"")
            val value = colorKeyValue.value ?: return
            element.setValue(value)
            element.project.save()
        }

    }

    override fun getColorFrom(psiElement: PsiElement): Color? {
        if (psiElement !is LeafPsiElement || !psiElement.isEntitySpecification()) {
            return null
        }

        val element = psiElement.parent?.parent ?: return null

        if (element is YAMLKeyValue && element.keyText.equals("color")) {
            try {
                return Color.decode(element.valueText.toUpperCase())
            } catch (e: Exception) {
                return null
            }
        }
        return null
    }

}