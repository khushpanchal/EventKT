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


    override fun add(event: Event): Boolean {
        if (super.add(event)) {
            eventHashMap[event.id]?.let {
                syncEventToFile(it)
            }
            return true
        }
        return false
    }


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


    private fun assignFileIdToEvent(event: Event) {
        if (!eventToFileIdMap.containsKey(event.id)) {
            eventToFileIdMap[event.id] = uniqueFileId
            checkAndAssignFileId()
        }
    }


    private fun checkAndAssignFileId() {
        if (getFile(uniqueFileId).length() > perFileSizeThreshold) {
            generateUniqueFileId()
        }
    }


    private fun generateUniqueFileId() {
        uniqueFileId = System.nanoTime().toString().plus(UUID.randomUUID().toString())
    }


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


    private fun removeEventFromMap(event: Event) {
        eventToFileIdMap.remove(event.id)
    }


    override fun updateEventStatus(event: Event, status: EventStatus): Boolean {
        if (super.updateEventStatus(event, status)) {
            eventHashMap[event.id]?.let {
                syncEventToFile(it)
            }
            return true
        }
        return false
    }


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


    @Throws(Exception::class)
    private suspend fun addUpdateEventToFile(event: Event) =
        withContext(Dispatchers.IO) {
            val fileId = eventToFileIdMap[event.id] ?: throw Exception(FILE_EXCEPTION)
            val events = readEventsFromFile(fileId).filterNot { it.id == event.id }
                .toMutableList()
            events.add(event)
            writeEventsToFile(fileId, events)
        }


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


    @Throws(Exception::class)
    private suspend fun writeEventsToFile(fileId: String, events: List<Event>) =
        withContext(Dispatchers.IO) {
            val file = getFile(fileId)
            file.writeText(events.toJson())
        }


    private fun getFile(fileId: String): File {
        val directory = File(context.cacheDir, filePath)
        if (!directory.exists()) {
            directory.mkdir()
        }
        return File(directory, fileId)
    }
}