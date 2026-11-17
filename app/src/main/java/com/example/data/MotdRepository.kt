package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.engine.MotdCatalog
import com.example.model.MotdPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.motdDataStore: DataStore<Preferences> by preferencesDataStore(name = "fast_to_swap_prefs")

data class PersistedMotdState(
    val serverName: String = "EmeraldCraft Network",
    val playerCount: String = "148/500",
    val line1: String = MotdCatalog.defaultPresets.first().line1,
    val line2: String = MotdCatalog.defaultPresets.first().line2,
    val customPresets: List<MotdPreset> = emptyList(),
    val recentEmojis: List<String> = listOf("✦", "⚡", "⚔", "💎", "🔥", "✔", "👑", "⛏"),
    val isDarkStudio: Boolean = true
)

class MotdRepository(private val context: Context) {

    private object Keys {
        val SERVER_NAME = stringPreferencesKey("server_name")
        val PLAYER_COUNT = stringPreferencesKey("player_count")
        val LINE_1 = stringPreferencesKey("motd_line_1")
        val LINE_2 = stringPreferencesKey("motd_line_2")
        val CUSTOM_PRESETS = stringPreferencesKey("custom_presets_json")
        val RECENT_EMOJIS = stringPreferencesKey("recent_emojis_json")
        val DARK_STUDIO = booleanPreferencesKey("dark_studio")
    }

    val stateFlow: Flow<PersistedMotdState> = context.motdDataStore.data.map { prefs ->
        val defaultPreset = MotdCatalog.defaultPresets.first()
        PersistedMotdState(
            serverName = prefs[Keys.SERVER_NAME] ?: defaultPreset.serverName,
            playerCount = prefs[Keys.PLAYER_COUNT] ?: "148/500",
            line1 = prefs[Keys.LINE_1] ?: defaultPreset.line1,
            line2 = prefs[Keys.LINE_2] ?: defaultPreset.line2,
            customPresets = decodePresets(prefs[Keys.CUSTOM_PRESETS]),
            recentEmojis = decodeStringList(prefs[Keys.RECENT_EMOJIS])
                ?: listOf("✦", "⚡", "⚔", "💎", "🔥", "✔", "👑", "⛏"),
            isDarkStudio = prefs[Keys.DARK_STUDIO] ?: true
        )
    }

    suspend fun saveDraft(serverName: String, playerCount: String, line1: String, line2: String) {
        context.motdDataStore.edit { prefs ->
            prefs[Keys.SERVER_NAME] = serverName
            prefs[Keys.PLAYER_COUNT] = playerCount
            prefs[Keys.LINE_1] = line1
            prefs[Keys.LINE_2] = line2
        }
    }

    suspend fun saveCustomPresets(presets: List<MotdPreset>) {
        context.motdDataStore.edit { prefs ->
            prefs[Keys.CUSTOM_PRESETS] = encodePresets(presets)
        }
    }

    suspend fun saveRecentEmojis(emojis: List<String>) {
        context.motdDataStore.edit { prefs ->
            prefs[Keys.RECENT_EMOJIS] = JSONArray(emojis).toString()
        }
    }

    suspend fun setDarkStudio(dark: Boolean) {
        context.motdDataStore.edit { prefs ->
            prefs[Keys.DARK_STUDIO] = dark
        }
    }

    private fun encodePresets(presets: List<MotdPreset>): String {
        val arr = JSONArray()
        presets.forEach { p ->
            arr.put(
                JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("category", p.category)
                    put("serverName", p.serverName)
                    put("line1", p.line1)
                    put("line2", p.line2)
                }
            )
        }
        return arr.toString()
    }

    private fun decodePresets(raw: String?): List<MotdPreset> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { idx ->
                val obj = arr.getJSONObject(idx)
                MotdPreset(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    category = obj.optString("category", "Saved Slot"),
                    serverName = obj.optString("serverName", "Minecraft Server"),
                    line1 = obj.optString("line1", ""),
                    line2 = obj.optString("line2", ""),
                    isCustom = true
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun decodeStringList(raw: String?): List<String>? {
        if (raw.isNullOrBlank()) return null
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { idx -> arr.getString(idx) }
        } catch (_: Exception) {
            null
        }
    }
}
