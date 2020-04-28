/*
 *   Copyright (C) 2020 VenomVendor <info@VenomVendor.com>
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.venomvendor.sms.deduplicate.search.manager

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.venomvendor.sms.deduplicate.core.factory.Logger
import com.venomvendor.sms.deduplicate.search.data.Message
import com.venomvendor.sms.deduplicate.search.factory.DuplicateFinder
import org.koin.core.KoinComponent
import org.koin.core.get

class DuplicateFinderImpl : DuplicateFinder, KoinComponent {

    private val spaceRegex = "\\s|\\n|\\t|\\r".toRegex()

    override suspend fun getDuplicates(
        messages: List<Message>,
        country: String,
        ignoreTimestamp: Boolean,
        ignoreSpace: Boolean
    ): Set<Message> {
        val logger = get<Logger>()

        val hashCodeCache = mutableSetOf<Int>()
        val phoneUtil = PhoneNumberUtil.getInstance()

        return messages
            .map { message ->
                val body = getBody(message, ignoreSpace)

                val formattedNumber = getFormattedNumber(message, country, phoneUtil, logger)

                val timeStamp = if (ignoreTimestamp) "" else message.date

                val uniqueMessage = "$formattedNumber$timeStamp$body"

                val hash = uniqueMessage.hashCode()

                val contains = hashCodeCache.contains(hash)
                if (contains.not()) {
                    hashCodeCache.add(hash)
                }
                return@map message.takeIf { contains }
            }
            .filterNotNull()
            .toSet()
    }

    private fun getBody(message: Message, ignoreSpace: Boolean): String {
        return if (ignoreSpace) {
            message.body.trim().replace(spaceRegex, "")
        } else {
            message.body
        }
    }

    private fun getFormattedNumber(
        message: Message,
        country: String,
        phoneUtil: PhoneNumberUtil,
        logger: Logger
    ): String {
        var formattedNumber = getFormattedNumber(message.phone, country, phoneUtil, logger)
        if (formattedNumber.isNotEmpty() && formattedNumber.startsWith("+").not()) {
            formattedNumber = reFormatPhone(formattedNumber, country, phoneUtil, logger)
        }
        formattedNumber = formattedNumber.replace(spaceRegex, "")

        return PhoneNumberUtil.normalizeDiallableCharsOnly(formattedNumber)
    }

    private fun getFormattedNumber(
        phone: String?,
        country: String,
        phoneUtil: PhoneNumberUtil,
        logger: Logger
    ): String {
        if (phone.isNullOrBlank()) {
            return ""
        }
        try {
            val number = phoneUtil.parseAndKeepRawInput(phone, country)
            val isNumberValid = phoneUtil.isValidNumber(number)
            if (isNumberValid) {
                return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
            }
        } catch (ex: NumberParseException) {
            logger.log(ex)
        }
        return phone
    }

    private fun reFormatPhone(
        formattedNumber: String,
        country: String,
        phoneUtil: PhoneNumberUtil,
        logger: Logger
    ): String {
        val filteredNum = formattedNumber.replace("^0+".toRegex(), "")
        return getFormattedNumber(filteredNum, country, phoneUtil, logger)
    }
}
