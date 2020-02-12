package com.venomvendor.sms.deduplicate.service

interface OnDeletedListener {
    fun onResponse(deletedMessages: Int, interrupted: Boolean)
}
