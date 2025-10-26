package com.example.vaulto.service

import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Intent
import android.os.CancellationSignal
import android.service.autofill.*
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.example.vaulto.MainActivity
import com.example.vaulto.data.local.dao.VaultItemDao
import com.example.vaulto.data.local.entities.VaultItemEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class VaultoAutofillService : AutofillService() {

    @Inject
    lateinit var vaultItemDao: VaultItemDao

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val context = request.fillContexts
        val structure = context.last().structure

        val packageName = structure.activityComponent.packageName
        val appName = getAppName(packageName)
        val autofillFields = parseAutofillFields(structure)

        if (autofillFields.isEmpty()) {
            callback.onSuccess(null)
            return
        }

        runBlocking {
            try {
                val matchingItems = findMatchingCredentials(packageName, appName)
                
                if (matchingItems.isEmpty()) {
                    val response = createOpenVaultoResponse(packageName, autofillFields)
                    callback.onSuccess(response)
                    return@runBlocking
                }

                val response = createFillResponse(matchingItems, autofillFields, packageName)
                callback.onSuccess(response)
            } catch (e: Exception) {
                callback.onSuccess(null)
            }
        }
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        val context = request.fillContexts
        val structure = context.last().structure
        val packageName = structure.activityComponent.packageName
        val appName = getAppName(packageName)
        val autofillFields = parseAutofillFields(structure)

        var username = ""
        var password = ""
        var url = ""

        autofillFields.forEach { field ->
            val value = field.autofillValue
            if (value?.isText == true) {
                val text = value.textValue.toString()
                when (field.autofillType) {
                    "username" -> username = text
                    "email" -> if (username.isEmpty()) username = text
                    "password" -> password = text
                    "url" -> url = text
                }
            }
        }

        if (password.isNotEmpty()) {
            runBlocking {
                try {
                    val existing = vaultItemDao.searchItems(packageName).first()
                    
                    if (existing.isEmpty()) {
                        val newItem = VaultItemEntity(
                            title = appName,
                            username = username,
                            password = password,
                            url = url.ifEmpty { packageName },
                            category = if (isWebBrowser(packageName)) "Web" else "Apps",
                            notes = "Auto-saved from $appName"
                        )
                        vaultItemDao.insertItem(newItem)
                        callback.onSuccess()
                    } else {
                        val item = existing.first()
                        vaultItemDao.updateItem(
                            item.copy(
                                username = username.ifEmpty { item.username },
                                password = password,
                                modifiedAt = System.currentTimeMillis()
                            )
                        )
                        callback.onSuccess()
                    }
                } catch (e: Exception) {
                    callback.onFailure("Failed to save: ${e.message}")
                }
            }
        } else {
            callback.onFailure("No password found")
        }
    }

    private fun parseAutofillFields(structure: AssistStructure): List<AutofillField> {
        val fields = mutableListOf<AutofillField>()

        for (i in 0 until structure.windowNodeCount) {
            val windowNode = structure.getWindowNodeAt(i)
            parseNode(windowNode.rootViewNode, fields)
        }

        return fields
    }

    private fun parseNode(node: AssistStructure.ViewNode, fields: MutableList<AutofillField>) {
        val autofillHints = node.autofillHints
        val autofillId = node.autofillId
        val htmlInfo = node.htmlInfo

        // Try HTML info first (for web views)
        if (htmlInfo != null && autofillId != null) {
            val htmlTag = htmlInfo.tag
            val htmlAttributes = htmlInfo.attributes
            
            if (htmlTag == "input" && htmlAttributes != null) {
                val type = htmlAttributes.find { it.first == "type" }?.second
                val name = htmlAttributes.find { it.first == "name" }?.second
                val id = htmlAttributes.find { it.first == "id" }?.second
                
                val fieldType = when {
                    type == "password" -> "password"
                    type == "email" -> "email"
                    name?.contains("user", ignoreCase = true) == true -> "username"
                    name?.contains("email", ignoreCase = true) == true -> "email"
                    name?.contains("pass", ignoreCase = true) == true -> "password"
                    id?.contains("user", ignoreCase = true) == true -> "username"
                    id?.contains("email", ignoreCase = true) == true -> "email"
                    id?.contains("pass", ignoreCase = true) == true -> "password"
                    else -> null
                }
                
                if (fieldType != null) {
                    fields.add(
                        AutofillField(
                            autofillId = autofillId,
                            autofillType = fieldType,
                            autofillValue = node.autofillValue,
                            hint = name ?: id ?: type ?: ""
                        )
                    )
                }
            }
        }

        // Try autofill hints (for native apps)
        if (autofillHints != null && autofillId != null) {
            val hint = autofillHints.firstOrNull()
            if (hint != null) {
                val type = when {
                    hint.contains("username", ignoreCase = true) -> "username"
                    hint.contains("email", ignoreCase = true) -> "email"
                    hint.contains("password", ignoreCase = true) -> "password"
                    else -> null
                }

                if (type != null) {
                    fields.add(
                        AutofillField(
                            autofillId = autofillId,
                            autofillType = type,
                            autofillValue = node.autofillValue,
                            hint = hint
                        )
                    )
                }
            }
        }

        // Try input type (fallback)
        if (fields.none { it.autofillId == autofillId } && autofillId != null) {
            val inputType = node.inputType
            val type = when {
                inputType and android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD != 0 -> "password"
                inputType and android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS != 0 -> "email"
                inputType and android.text.InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS != 0 -> "email"
                else -> null
            }
            
            if (type != null) {
                fields.add(
                    AutofillField(
                        autofillId = autofillId,
                        autofillType = type,
                        autofillValue = node.autofillValue,
                        hint = node.text?.toString() ?: ""
                    )
                )
            }
        }

        for (i in 0 until node.childCount) {
            parseNode(node.getChildAt(i), fields)
        }
    }

    private suspend fun findMatchingCredentials(
        packageName: String,
        appName: String
    ): List<VaultItemEntity> {
        return try {
            val byPackage = vaultItemDao.searchItems(packageName).first()
            val byAppName = vaultItemDao.searchItems(appName).first()
            val byUrl = vaultItemDao.searchItems(extractDomain(packageName)).first()
            
            (byPackage + byAppName + byUrl).distinctBy { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun createFillResponse(
        items: List<VaultItemEntity>,
        autofillFields: List<AutofillField>,
        packageName: String
    ): FillResponse {
        val responseBuilder = FillResponse.Builder()

        items.forEach { item ->
            val datasetBuilder = Dataset.Builder()
            
            val presentation = RemoteViews(this.packageName, android.R.layout.simple_list_item_1).apply {
                setTextViewText(
                    android.R.id.text1,
                    "🔐 ${item.title}${if (item.username.isNotEmpty()) " - ${item.username}" else ""}"
                )
            }

            var hasFields = false

            autofillFields.forEach { field ->
                val value = when (field.autofillType) {
                    "username", "email" -> if (item.username.isNotEmpty()) {
                        AutofillValue.forText(item.username)
                    } else null
                    "password" -> AutofillValue.forText(item.password)
                    else -> null
                }

                if (value != null) {
                    datasetBuilder.setValue(field.autofillId, value, presentation)
                    hasFields = true
                }
            }

            if (hasFields) {
                responseBuilder.addDataset(datasetBuilder.build())
            }
        }

        val openVaultoDataset = createOpenVaultoDataset(autofillFields)
        responseBuilder.addDataset(openVaultoDataset)

        val usernameId = autofillFields.find { 
            it.autofillType == "username" || it.autofillType == "email" 
        }?.autofillId
        val passwordId = autofillFields.find { it.autofillType == "password" }?.autofillId

        if (passwordId != null) {
            val requiredIds = if (usernameId != null) {
                arrayOf(usernameId, passwordId)
            } else {
                arrayOf(passwordId)
            }
            
            val saveInfo = SaveInfo.Builder(
                SaveInfo.SAVE_DATA_TYPE_USERNAME or SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                requiredIds
            ).build()

            responseBuilder.setSaveInfo(saveInfo)
        }

        return responseBuilder.build()
    }

    private fun createOpenVaultoDataset(autofillFields: List<AutofillField>): Dataset {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val presentation = RemoteViews(packageName, android.R.layout.simple_list_item_1).apply {
            setTextViewText(android.R.id.text1, "🔓 Open Vaulto to search")
        }

        val datasetBuilder = Dataset.Builder(presentation)
        
        val authPresentation = RemoteViews(packageName, android.R.layout.simple_list_item_1).apply {
            setTextViewText(android.R.id.text1, "🔓 Open Vaulto")
        }

        autofillFields.firstOrNull()?.let { field ->
            datasetBuilder.setValue(
                field.autofillId,
                null,
                authPresentation
            )
        }

        return datasetBuilder.build()
    }

    private fun createOpenVaultoResponse(
        packageName: String,
        autofillFields: List<AutofillField>
    ): FillResponse {
        val responseBuilder = FillResponse.Builder()
        responseBuilder.addDataset(createOpenVaultoDataset(autofillFields))
        return responseBuilder.build()
    }

    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private fun isWebBrowser(packageName: String): Boolean {
        return packageName.contains("chrome") ||
                packageName.contains("firefox") ||
                packageName.contains("browser") ||
                packageName.contains("opera") ||
                packageName.contains("samsung.internet") ||
                packageName.contains("brave") ||
                packageName.contains("edge")
    }

    private fun extractDomain(packageName: String): String {
        return packageName
            .removePrefix("com.")
            .removePrefix("org.")
            .removePrefix("net.")
            .split(".").firstOrNull() ?: packageName
    }

    data class AutofillField(
        val autofillId: AutofillId,
        val autofillType: String,
        val autofillValue: AutofillValue?,
        val hint: String
    )
}