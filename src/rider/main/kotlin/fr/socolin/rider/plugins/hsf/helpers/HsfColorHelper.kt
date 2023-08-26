package fr.socolin.rider.plugins.hsf.helpers

import java.awt.Color

class HsfColorHelper {
    companion object {
        fun colorFromHex(hex: String?): Color? {
            if (hex == null)
                return null
            return try {
                Color.decode(hex)
            } catch (e: Exception) {
                null
            }
        }

    }
}