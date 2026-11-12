package com.example.util

import com.example.data.MriModule
import com.example.data.MriNode

object ResumeNodeResolver {
    fun findNode(modules: List<MriModule>, code: String?): MriNode? {
        if (code.isNullOrBlank()) return null
        for (module in modules) {
            for (node in module.nodes) {
                if (node.code == code) {
                    return node
                }
            }
        }
        return null
    }

    fun moduleIdOf(modules: List<MriModule>, code: String?): String? {
        if (code.isNullOrBlank()) return null
        for (module in modules) {
            if (module.nodes.any { it.code == code }) {
                return module.id
            }
        }
        if (code.contains(".")) {
            val prefix = code.substringBefore(".")
            if (modules.any { it.id == prefix }) {
                return prefix
            }
        }
        return null
    }

    fun codeToRememberOnOpen(module: MriModule, storedCode: String?): String? {
        if (storedCode != null && module.nodes.any { it.code == storedCode }) {
            return storedCode
        }
        return module.nodes.firstOrNull { it.level >= 1 }?.code
            ?: module.nodes.firstOrNull()?.code
    }

    fun isKnownNode(modules: List<MriModule>, code: String): Boolean {
        if (code.isBlank()) return false
        for (module in modules) {
            if (module.nodes.any { it.code == code }) {
                return true
            }
        }
        return false
    }
}
