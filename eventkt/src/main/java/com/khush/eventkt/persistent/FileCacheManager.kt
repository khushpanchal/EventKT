package com.khush.eventkt.persistent

import android.content.Context
import com.khush.eventkt.event.Event
import com.khush.eventkt.event.EventStatus
import com.khush.eventkt.utils.Const.FILE_EXCEPTION
import com.khush.eventkt.utils.Utils
import com.khush.eventkt.utils.Utils.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Collections
import java.util.UUID

/**
 * File cache manager: Class that stores events to the file system in disk
 *
 * Purpose of the class: Keep synced with memory
 *
 * [eventToFileIdMap] - Each event with unique event id is mapped to file id (unique name of the file)
 *
 * [uniqueFileId] - Unique Id of the file (or name of the file) in which events are pushed
 *
 * All the file operations are running in the background thread
 *
 * Any operation failure invoke [moveInMemoryDataToFile] as a fallback which cleans all the disk and re sync
 *
 * @property context Application context
 * @property filePath Name of the directory inside which events are stored in different files
 * @property perFileSizeThreshold Maximum size per file in bytes, directory can have multiple files
 */
class FileCacheManager(
    private val context: Context,
    private val filePath: String,
    private val perFileSizeThreshold: Int = 15000 //Around 15 events if 1.5kb each event
) : InMemoryCacheManager() {

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val sharedFlow = MutableSharedFlow<Job>(extraBufferCapacity = Int.MAX_VALUE)

    private val eventToFileIdMap: MutableMap<String, String> =
        Collections.synchronizedMap(HashMap())
    private var uniqueFileId = System.nanoTime().toString().plus(UUID.randomUUID().toString())

    init {
        sharedFlow.onEach {
            it.join()
        }.launchIn(scope)
    }

    /**
     * Add Single event to the memory and then add to the file
     *
     * @param event Single [Event]
     * @return Boolean for successful addition
     */
    override fun add(event: Event): Boolean {
        if (super.add(event)) {
            eventHashMap[event.id]?.let {
                syncEventToFile(it)
            }
            return true
        }
        return false
    }

    /**
     * Add List of events to the memory and then add to file
     *
     * @param eventList List of [Event]
     * @return Boolean for successful addition
     */
    override fun addAll(eventList: List<Event>): Boolean {
        if (super.addAll(eventList)) {
            val list = mutableListOf<Event>()
            eventList.forEach { event ->
                eventHashMap[event.id]?.let {
                    list.add(it)
                }
            }
            syncEventsToFile(list)
            return true
        }
        return false
    }

    /**
     * If [Event] is not mapped with any file, map it to [uniqueFileId]
     *
     * @param event Single [Event]
     */
    private fun assignFileIdToEvent(event: Event) {
        if (!eventToFileIdMap.containsKey(event.id)) {
            eventToFileIdMap[event.id] = uniqueFileId
            checkAndAssignFileId()
        }
    }

    /**
     * If current [uniqueFileId] size exceed [perFileSizeThreshold], generates new [uniqueFileId]
     *
     */
    private fun checkAndAssignFileId() {
        if (getFile(uniqueFileId).length() > perFileSizeThreshold) {
            generateUniqueFileId()
        }
    }

    /**
     * Generate [uniqueFileId]
     *
     */
    private fun generateUniqueFileId() {
        uniqueFileId = System.nanoTime().toString().plus(UUID.randomUUID().toString())
    }

    /**
     * Remove single event from the memory and then remove from the file
     *
     * @param event Single [Event]
     * @return Boolean for successful removal
     */
    override fun remove(event: Event): Boolean {
        val eventToRemove = eventHashMap[event.id]
        if (super.remove(event)) {
            if (eventToRemove != null) {
                syncEventRemovalToFile(eventToRemove)
            }
            return true
        }
        return false
    }

    /**
     * Remove list of events from the memory based on the status and then remove from the file
     *
     * @param status List of [EventStatus] for which events get removed
     * @return Boolean for successful removal
     */
    override fun removeAll(status: List<EventStatus>): Boolean {
        val eventToRemoveList = mutableListOf<Event>()
        synchronized(eventHashMap) {
            eventHashMap.values.forEach { event ->
                status.forEach { eventStatus ->
                    if (event.status == eventStatus.name) {
                        eventToRemoveList.add(event)
                    }
                }
            }
        }
        if (super.removeAll(status)) {
            syncEventsRemovalToFile(eventToRemoveList)
            return true
        }
        return false
    }

    /**
     * Remove event from [eventToFileIdMap]
     *
     * @param event Single [Event]
     */
    private fun removeEventFromMap(event: Event) {
        eventToFileIdMap.remove(event.id)
    }

    /**
     * Update status for the event to the memory and then to the file
     *
     * @param event Single [Event]
     * @param status [EventStatus] to update in event
     * @return Boolean for successful update
     */
    override fun updateEventStatus(event: Event, status: EventStatus): Boolean {
        if (super.updateEventStatus(event, status)) {
            eventHashMap[event.id]?.let {
                syncEventToFile(it)
            }
            return true
        }
        return false
    }

    /**
     * Update status for the list of events to the memory and then to the file
     *
     * @param eventList List of [Event]
     * @param status [EventStatus] to update in the list of event
     * @return Boolean for successful update
     */
    override fun updateEventStatusAll(eventList: List<Event>, status: EventStatus): Boolean {
        if (super.updateEventStatusAll(eventList, status)) {
            val list = mutableListOf<Event>()
            eventList.forEach { event ->
                eventHashMap[event.id]?.let {
                    list.add(it)
                }
            }
            syncEventsToFile(list)
            return true
        }
        return false
    }

    /**
     * Fetch in memory events and push all the events to disk
     *
     * @param success Lambda(Boolean) to invoke after successful write to disk
     */
    override fun syncDataToCache(success: (Boolean) -> Unit) {
        sharedFlow.tryEmit(
            scope.launch(start = CoroutineStart.LAZY) {
                kotlin.runCatching {
                    moveInMemoryDataToFile()
                }.onSuccess {
                    success.invoke(true)
                }.onFailure {
                    success.invoke(false)
                }
            }
        )
    }

    /**
     * Fetch events from the disk
     *
     * @param success Lambda(Boolean, List of [Event]) to invoke after successful fetch from disk
     */
    override fun syncDataFromCache(success: (Boolean, List<Event>) -> Unit) {
        sharedFlow.tryEmit(
            scope.launch(start = CoroutineStart.LAZY) {
                kotlin.runCatching {
                    moveFileDataToMemory {
                        success.invoke(true, it)
                    }
                }.onFailure {
                    success.invoke(false, emptyList())
                }
            }
        )
    }

    /**
     * [assignFileIdToEvent] and [addUpdateEventToFile]
     *
     * Any failure invokes [moveInMemoryDataToFile]
     *
     * @param event Single [Event]
     */
    private fun syncEventToFile(event: Event) {
        sharedFlow.tryEmit(
            scope.launch(start = CoroutineStart.LAZY) {
                kotlin.runCatching {
                    assignFileIdToEvent(event)
                    addUpdateEventToFile(event)
                }.onFailure {
                    kotlin.runCatching {
                        moveInMemoryDataToFile()
                    }
                }
            }
        )
    }

    /**
     * For each event [assignFileIdToEvent] and [addUpdateEventToFile]
     *
     * Any failure invokes [moveInMemoryDataToFile]
     *
     * @param eventList List of [Event]
     */
    private fun syncEventsToFile(eventList: List<Event>) {
        sharedFlow.tryEmit(
            scope.launch(start = CoroutineStart.LAZY) {
                kotlin.runCatching {
                    eventList.forEach { event ->
                        assignFileIdToEvent(event)
                        addUpdateEventToFile(event)
                    }
                }.onFailure {
                    kotlin.runCatching {
                        moveInMemoryDataToFile()
                    }
                }
            }
        )
    }

    /**
     * [removeEventFromFile] and [removeEventFromMap]
     *
     * Any failure invokes [moveInMemoryDataToFile]
     *
     * @param event Single [Event]
     */
    private fun syncEventRemovalToFile(event: Event) {
        sharedFlow.tryEmit(
            scope.launch(start = CoroutineStart.LAZY) {
                kotlin.runCatching {
                    removeEventFromFile(event)
                    removeEventFromMap(event)
                }.onFailure {
                    kotlin.runCatching {
                        moveInMemoryDataToFile()
                    }
                }
            }
        )
    }

    /**
     * For each event [removeEventFromFile] and [removeEventFromMap]
     *
     * Any failure invokes [moveInMemoryDataToFile]
     *
     * @param eventList List of [Event]
     */
    private fun syncEventsRemovalToFile(eventList: List<Event>) {
        sharedFlow.tryEmit(
            scope.launch(start = CoroutineStart.LAZY) {
                kotlin.runCatching {
                    eventList.forEach { event ->
                        removeEventFromFile(event)
                        removeEventFromMap(event)
                    }
                }.onFailure {
                    kotlin.runCatching {
                        moveInMemoryDataToFile()
                    }
                }
            }
        )
    }

    /**
     * Suspend function that delete all the files in disk and move all the in memory events to disk again
     *
     */
    @Throws(Exception::class)
    private suspend fun moveInMemoryDataToFile() =
        withContext(Dispatchers.IO) {
            val directory = File(context.cacheDir, filePath)
            if (directory.exists()) {
                val contents = directory.listFiles()
                contents?.forEach {
                    it.delete()
                }
            }
            val eventList = synchronized(eventHashMap) {
                eventHashMap.values.toList()
            }
            synchronized(eventToFileIdMap) {
                eventToFileIdMap.clear()
            }
            eventList.forEach { event ->
                assignFileIdToEvent(event)
                addUpdateEventToFile(event)
            }
        }

    /**
     * Suspend function that read all the events from disk and load it to the memory
     *
     * Also maps event id to file id [eventToFileIdMap]
     *
     * @param eventsFromCache Lambda(List of [Event]) that invokes on successful fetch from disk
     */
    @Throws(Exception::class)
    private suspend fun moveFileDataToMemory(eventsFromCache: (List<Event>) -> Unit) =
        withContext(Dispatchers.IO) {
            val directory = File(context.cacheDir, filePath)
            val eventList = mutableListOf<Event>()
            if (directory.exists() && directory.isDirectory) {
                val files = directory.listFiles()
                if (files != null) {
                    for (file in files) {
                        val fileId = file.nameWithoutExtension
                        readEventsFromFile(fileId).forEach { event ->
                            eventList.add(event)
                            eventHashMap[event.id] = event
                            eventToFileIdMap[event.id] = fileId
                        }
                    }
                }
            }
            eventsFromCache.invoke(eventList)
        }

    /**
     * Add or update event to file
     *
     * @param event Single [Event]
     */
    @Throws(Exception::class)
    private suspend fun addUpdateEventToFile(event: Event) =
        withContext(Dispatchers.IO) {
            val fileId = eventToFileIdMap[event.id] ?: throw Exception(FILE_EXCEPTION)
            val events = readEventsFromFile(fileId).filterNot { it.id == event.id }
                .toMutableList()
            events.add(event)
            writeEventsToFile(fileId, events)
        }

    /**
     * Remove event from file
     *
     * @param event Single [Event]
     */
    @Throws(Exception::class)
    private suspend fun removeEventFromFile(event: Event) =
        withContext(Dispatchers.IO) {
            val fileId = eventToFileIdMap[event.id] ?: throw Exception(FILE_EXCEPTION)
            val events = readEventsFromFile(fileId).filterNot { it.id == event.id }
                .toMutableList()

            if (events.size > 0) {
                writeEventsToFile(fileId, events)
            } else {
                val file = getFile(fileId)
                if (file.exists()) {
                    file.delete()
                }
            }
        }

    /**
     * Read events from file
     *
     * @param fileId Name of the file
     * @return List of [Event]
     */
    @Throws(Exception::class)
    private suspend fun readEventsFromFile(fileId: String): List<Event> =
        withContext(Dispatchers.IO) {
            val file = getFile(fileId)
            if (file.exists()) {
                Utils.fromJsonStringToEventList(file.readText())
            } else {
                listOf()
            }
        }

    /**
     * Write events to file
     *
     * @param fileId Name of the file
     * @param events List of [Event]
     */
    @Throws(Exception::class)
    private suspend fun writeEventsToFile(fileId: String, events: List<Event>) =
        withContext(Dispatchers.IO) {
            val file = getFile(fileId)
            file.writeText(events.toJson())
        }

    /**
     * Get file from the disk
     *
     * @param fileId Name of the file
     * @return [File]
     */
    private fun getFile(fileId: String): File {
        val directory = File(context.cacheDir, filePath)
        if (!directory.exists()) {
            directory.mkdir()
        }
        return File(directory, fileId)
    }
}