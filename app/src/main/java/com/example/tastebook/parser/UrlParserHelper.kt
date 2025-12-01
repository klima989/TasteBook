package com.example.tastebook.parser

import com.example.tastebook.data.Recipe
import com.example.tastebook.data.RecipeCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object UrlParserHelper {

    /**
     * Fetches HTML from the URL, then parses to extract recipe info.
     * Returns a Recipe object or null if unsuccessful.
     */
    suspend fun fetchAndParseRecipe(url: String): Recipe? = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Android)")  // optional, to pretend like a browser
                .timeout(15_000)
                .get()

            parseRecipeFromDocument(doc)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Parses a Jsoup Document to extract recipe info via JSON‑LD or fallback. */
    private fun parseRecipeFromDocument(doc: Document): Recipe? {
        // 1. Try JSON‑LD parsing
        val scripts = doc.select("script[type=application/ld+json]")
        for (script in scripts) {
            val jsonText = script.data()
            try {
                val root = if (jsonText.trim().startsWith("[")) {
                    JSONArray(jsonText)
                } else {
                    JSONObject(jsonText)
                }
                val recipeObj = findRecipeObject(root)
                if (recipeObj != null) {
                    return recipeFromJson(recipeObj)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        // 2. Fallback: try via CSS selectors (less reliable). Example:
        val title = doc.title()
        // image fallback (maybe og:image meta)
        val ogImage = doc.selectFirst("meta[property=og:image]")?.attr("content")
        // ingredients fallback (this depends on site structure)
        val ingredients = doc.select(".ingredients-selector")  // you’ll need the real CSS class
            .map { it.text() }
            .takeIf { it.isNotEmpty() }
        // steps fallback
        val steps = doc.select(".instructions-selector")
            .map { it.text() }
            .takeIf { it.isNotEmpty() }

        return Recipe(
            title = title,
            image = ogImage,
            ingredients = ingredients,
            steps = steps,
            url = "",
            category = RecipeCategory.Sweet,
            subCategory = ""
        )
    }

    /** Locate the JSONObject representing the Recipe in a JSON or JSONArray root. */
    private fun findRecipeObject(root: Any): JSONObject? {
        when (root) {
            is JSONObject -> {
                if (root.optString("@type") == "Recipe") {
                    return root
                }
                // Sometimes under @graph
                val graph = root.optJSONArray("@graph")
                if (graph != null) {
                    for (i in 0 until graph.length()) {
                        val obj = graph.getJSONObject(i)
                        if (obj.optString("@type") == "Recipe") {
                            return obj
                        }
                    }
                }
            }
            is JSONArray -> {
                for (i in 0 until root.length()) {
                    val item = root.get(i)
                    if (item is JSONObject && item.optString("@type") == "Recipe") {
                        return item
                    }
                }
            }
        }
        return null
    }

    /** Build our Recipe data class from a JSONObject that is the Recipe schema object. */
    private fun recipeFromJson(recipeObj: JSONObject): Recipe {
        val title = recipeObj.optString("name", null)

        // image might be String or Array
        val imageAny: String? = when (val img = recipeObj.opt("image")) {
            is String -> img
            is JSONArray -> {
                if (img.length() > 0) img.optString(0) else null
            }
            else -> null
        }

        val ingredients: List<String>? = recipeObj.optJSONArray("recipeIngredient")?.let { arr ->
            (0 until arr.length()).map { arr.optString(it) }
        }

        // Steps: recipeInstructions may be array of objects
        val steps: List<String>? = recipeObj.opt("recipeInstructions")?.let { instr ->
            when (instr) {
                is JSONArray -> {
                    // Convert JSONArray to Kotlin List<JSONObject>
                    val list = mutableListOf<JSONObject>()
                    for (i in 0 until instr.length()) {
                        val item = instr.opt(i)
                        if (item is JSONObject) {
                            list.add(item)
                        }
                    }
                    // Now you can use mapNotNull on the Kotlin list
                    list.mapNotNull { obj ->
                        obj.optString("text", null)
                    }
                }
                is JSONObject -> listOf(instr.optString("text", null))
                is String -> listOf(instr)
                else -> null
            }
        }

        return Recipe(
            title = title,
            image = imageAny,
            ingredients = ingredients,
            steps = steps,
            url = "",
            category = RecipeCategory.Sweet,
            subCategory = ""
        )
    }

}