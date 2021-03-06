package com.icthh.xm.utils

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.apache.commons.lang3.time.StopWatch
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

val loggers = ConcurrentHashMap<Class<Any>, Logger>()
val loggerFactory: (Class<Any>) -> Logger = { Logger.getInstance(it) }
val Any.log: Logger get() = loggers.computeIfAbsent(this.javaClass, loggerFactory)
val Any.logger get() = java.util.logging.Logger.getLogger(this.javaClass.name)

fun InputStream.readTextAndClose(charset: Charset = Charsets.UTF_8): String {
    return this.bufferedReader(charset).use { it.readText() }
}

fun showDiffDialog(windowTitle: String, content: String, title1: String,
                   title2: String, project: Project, file: VirtualFile) {
    val content1 = DiffContentFactory.getInstance().create(content)
    val content2 = DiffContentFactory.getInstance().create(project, file)
    val request = SimpleDiffRequest(windowTitle, content1, content2, title1, title2)
    DiffManager.getInstance().showDiff(project, request)
}

fun String?.templateOrEmpty(template: (String) -> String): String {
    this ?: return ""
    return template.invoke(this)
}

fun <T> difference(left: Set<T>, right: Set<T>): Set<T> {
    val first = HashSet(left)
    val second = HashSet(right)
    val roles = first.filter { second.contains(it) }
    first.removeAll(roles)
    second.removeAll(roles)
    val difference = HashSet<T>()
    difference.addAll(first)
    difference.addAll(second)
    return difference
}

val times: MutableMap<String, AtomicLong> = ConcurrentHashMap()
val timers: MutableMap<String, StopWatch> = ConcurrentHashMap()
fun start(key: String) {
    timers.put(key, StopWatch.createStarted())
}

fun stop(key: String) {
    val nanoTime = timers.get(key)?.getNanoTime() ?: 0
    times.computeIfAbsent(key) { AtomicLong() }
    times.get(key)?.addAndGet(nanoTime)
}

fun Any.startDiagnostic() {
    Thread {
        while(!Thread.interrupted()) {
            Thread.sleep(5000)
            val line = StringBuilder()
            times.forEach {
                line.append(it.key, " ", it.value.get() / 1000_000, "\n")
            }
            logger.info(line.toString())
        }
    }.start()
}


