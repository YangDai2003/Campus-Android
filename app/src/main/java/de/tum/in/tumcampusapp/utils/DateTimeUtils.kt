package de.tum.`in`.tumcampusapp.utils

import android.content.Context
import android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE
import android.text.format.DateUtils.HOUR_IN_MILLIS
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.text.format.DateUtils.getRelativeTimeSpanString
import de.tum.`in`.tumcampusapp.R
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.text.ParseException
import java.util.Locale

object DateTimeUtils {

    @JvmStatic
    fun dateWithStartOfDay(): DateTime {
        return DateTime.now().withTimeAtStartOfDay()
    }

    @JvmStatic
    fun dateWithEndOfDay(): DateTime {
        return dateWithStartOfDay()
            .withHourOfDay(23)
            .withMinuteOfHour(59)
            .withSecondOfMinute(59)
    }

    /**
     * Format an upcoming string nicely by being more precise as time comes closer
     * E.g.:
     *      in 30 minutes
     *      in 2 h 4 min
     *      in 6 hours
     *      tomorrow
     * @see getRelativeTimeSpanString()
     */
    fun formatFutureTime(time: DateTime, context: Context): String {
        val timeInMillis = time.millis
        val now = DateTime.now().millis

        // Catch future dates: current clock might be running behind
        if (timeInMillis < now || timeInMillis <= 0) return DateTimeUtils.formatTimeOrDay(time, context)

        val diff = timeInMillis - now
        return when {
            diff < 60 * MINUTE_IN_MILLIS -> {
                val formatter = DateTimeFormat.forPattern("m")
                    .withLocale(Locale.ENGLISH)
                "${context.getString(R.string.IN)} ${formatter.print(DateTime(diff, DateTimeZone.UTC))} " + context.getString(R.string.MINUTES)
            }
            diff < 5 * HOUR_IN_MILLIS -> {
                val formatter = DateTimeFormat.forPattern("h 'h' m 'min'")
                    .withLocale(Locale.ENGLISH)
                "${context.getString(R.string.IN)} ${formatter.print(DateTime(diff, DateTimeZone.UTC))}"
            }
            else -> getRelativeTimeSpanString(timeInMillis, now, MINUTE_IN_MILLIS, FORMAT_ABBREV_RELATIVE).toString()
        }
    }

    /**
     * @Deprecated use formatTimeOrDay(DateTime, Context)
     */
    @Deprecated("Use the version with a proper DateTime object, there's really no reason to pass datetimes as strings")
    fun formatTimeOrDayFromISO(datetime: String, context: Context): String {
        val d = DateTimeUtils.parseIsoDate(datetime) ?: return ""
        return DateTimeUtils.formatTimeOrDay(d, context)
    }

    /**
     * Format a *past* ISO string timestamp with degrading granularity as time goes by
     * E.g.:
     *      Just now
     *      18:20
     *      Yesterday
     *      12.03.2016
     *
     * Please note, that this does *not* use getRelativeTimeSpanString(), because lectures scheduled
     * at 12:00 and starting at 12:15 get a bit annoying nagging you with "12 minutes ago", when
     * they actually only start in a couple of minutes
     * This is similar to formatFutureTime(), but not specialized on future dates
     * When in doubt, use formatFutureTime()
     * @see formatFutureTime()
     */
    fun formatTimeOrDay(time: DateTime, context: Context): String {
        val timeInMillis = time.millis
        val now = DateTime.now().millis

        // Catch future dates: current clock might be running behind
        if (timeInMillis > now || timeInMillis <= 0) {
            return context.getString(R.string.just_now)
        }

        val diff = now - timeInMillis
        return when {
            diff < MINUTE_IN_MILLIS ->
                context.getString(R.string.just_now)
            diff < 24 * HOUR_IN_MILLIS ->
                DateTimeFormat.forPattern("HH:mm")
                    .withLocale(Locale.ENGLISH)
                    .print(time)
            diff < 48 * HOUR_IN_MILLIS ->
                context.getString(R.string.yesterday)
            else ->
                DateTimeFormat.forPattern("dd.MM.yyyy")
                    .withLocale(Locale.ENGLISH)
                    .print(time)
        }
    }

    /**
     * 2014-06-30T16:31:57Z
     */
    private val isoDateFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    fun parseIsoDate(datetime: String) = try {
        isoDateFormatter.parseDateTime(datetime)
    } catch (e: ParseException) {
        Utils.log(e)
        null
    }

    /**
     * 2014-06-30T16:31:57.878Z
     */
    private val isoDateWithMillisFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    fun parseIsoDateWithMillis(datetime: String) = try {
        isoDateWithMillisFormatter.parseDateTime(datetime)
    } catch (e: ParseException) {
        Utils.log(e)
        null
    }

    /**
     * Checks whether two DateTime contain the same day
     *
     * @return true if both dates are on the same day
     */
    fun isSameDay(first: DateTime, second: DateTime) =
        first.year() == second.year() && first.dayOfYear() == second.dayOfYear()

    private val dateFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    /**
     * Converts a date-string to DateTime
     *
     * @param str String with ISO-Date (yyyy-mm-dd)
     * @return DateTime
     */
    fun getDate(str: String): DateTime = try {
        DateTime.parse(str, dateFormatter)
    } catch (e: RuntimeException) {
        Utils.log(e)
        DateTime()
    }

    /**
     * Converts DateTime to an ISO date-string
     *
     * @param d DateTime
     * @return String (yyyy-mm-dd)
     */
    fun getDateString(d: DateTime): String = dateFormatter.print(d)

    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * Converts DateTime to an ISO datetime-string
     *
     * @return String (yyyy-mm-dd hh:mm:ss)
     */
    @JvmStatic
    fun getDateTimeString(d: DateTime): String = dateTimeFormatter.print(d)

    /**
     * Converts a datetime-string to DateTime
     *
     * @param str String with ISO-DateTime (yyyy-mm-dd hh:mm:ss)
     */
    fun getDateTime(str: String): DateTime = try {
        DateTime.parse(str, dateTimeFormatter)
    } catch (e: RuntimeException) {
        Utils.log(e)
        DateTime()
    }

    fun getTimeString(d: DateTime): String = timeFormatter.print(d)

    private val timeFormatter = DateTimeFormat.forPattern("HH:mm")
}
