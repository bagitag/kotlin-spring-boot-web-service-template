package com.example.configuration

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.turbo.MDCFilter
import ch.qos.logback.core.spi.FilterReply
import com.example.TemplateApplication
import org.slf4j.Marker

class DebugLoggingTurboFilter : MDCFilter() {

    private var packages: String? = null

    fun setPackages(packages: String) {
        this.packages = packages
    }

    override fun decide(
        marker: Marker?, logger: Logger?, level: Level?, format: String?, params: Array<out Any>?, t: Throwable?
    ): FilterReply {
        if (!isStarted) {
            return FilterReply.NEUTRAL
        }

        var filterReply = super.decide(marker, logger, level, format, params, t)

        if (filterReply === FilterReply.ACCEPT) {
            filterReply = extendPackagesWithProjectBasePackage().split(",").stream()
                .filter { e -> logger?.name?.startsWith(e) ?: false }
                .map { FilterReply.ACCEPT }
                .findAny()
                .orElse(FilterReply.NEUTRAL)
        }
        return filterReply
    }

    private fun extendPackagesWithProjectBasePackage(): String {
        val basePackage = TemplateApplication::class.java.`package`.name
        return "$basePackage,$packages"
    }
}
