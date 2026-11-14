package com.example.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.model.CallLogEntry
import com.example.model.CallType
import com.example.model.ContactEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DevicePhoneRepository(private val context: Context) {

    fun hasCallLogPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALL_LOG,
        ) == PackageManager.PERMISSION_GRANTED

    fun hasContactsPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS,
        ) == PackageManager.PERMISSION_GRANTED

    suspend fun loadRecentCallLogs(limit: Int = 120): List<CallLogEntry> =
        withContext(Dispatchers.IO) {
            if (!hasCallLogPermission()) return@withContext emptyList()

            val results = mutableListOf<CallLogEntry>()
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
            )

            try {
                context.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    projection,
                    null,
                    null,
                    "${CallLog.Calls.DATE} DESC",
                )?.use { cursor ->
                    val idIdx = cursor.getColumnIndexOrThrow(CallLog.Calls._ID)
                    val nameIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
                    val numberIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                    val typeIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)
                    val dateIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
                    val durationIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)

                    while (cursor.moveToNext() && results.size < limit) {
                        val number = cursor.getString(numberIdx)?.trim().orEmpty()
                        if (number.isEmpty() || number.equals("private", ignoreCase = true)) {
                            continue
                        }
                        val id = cursor.getString(idIdx) ?: "${results.size}"
                        val cachedName = cursor.getString(nameIdx)?.trim()?.takeIf { it.isNotEmpty() }
                        val rawType = cursor.getInt(typeIdx)
                        val callType = when (rawType) {
                            CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                            CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                            CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                            else -> CallType.OTHER
                        }
                        val dateMillis = cursor.getLong(dateIdx)
                        val durationSeconds = cursor.getLong(durationIdx)

                        results.add(
                            CallLogEntry(
                                id = id,
                                displayName = cachedName,
                                phoneNumber = number,
                                callType = callType,
                                timestampMillis = dateMillis,
                                durationSeconds = durationSeconds,
                            )
                        )
                    }
                }
            } catch (_: SecurityException) {
                return@withContext emptyList()
            } catch (_: Exception) {
                return@withContext emptyList()
            }

            results
        }

    suspend fun loadContacts(limit: Int = 500): List<ContactEntry> =
        withContext(Dispatchers.IO) {
            if (!hasContactsPermission()) return@withContext emptyList()

            val results = mutableListOf<ContactEntry>()
            val seenKeys = HashSet<String>()
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL,
            )

            try {
                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    null,
                    null,
                    "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC",
                )?.use { cursor ->
                    val idIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID)
                    val nameIdx =
                        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIdx =
                        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val typeIdx =
                        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)
                    val labelIdx =
                        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.LABEL)

                    while (cursor.moveToNext() && results.size < limit) {
                        val number = cursor.getString(numberIdx)?.trim().orEmpty()
                        if (number.isEmpty()) continue

                        val name = cursor.getString(nameIdx)?.trim().orEmpty()
                            .ifEmpty { number }
                        val digitsSuffix = number.filter { it.isDigit() }.takeLast(10)
                        val dedupKey = "${name.lowercase()}|$digitsSuffix"
                        if (!seenKeys.add(dedupKey)) continue

                        val typeInt = cursor.getInt(typeIdx)
                        val customLabel = cursor.getString(labelIdx)
                        val label = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                            context.resources,
                            typeInt,
                            customLabel,
                        ).toString()

                        results.add(
                            ContactEntry(
                                id = cursor.getString(idIdx) ?: "${results.size}",
                                displayName = name,
                                phoneNumber = number,
                                label = label,
                            )
                        )
                    }
                }
            } catch (_: SecurityException) {
                return@withContext emptyList()
            } catch (_: Exception) {
                return@withContext emptyList()
            }

            results
        }
}
