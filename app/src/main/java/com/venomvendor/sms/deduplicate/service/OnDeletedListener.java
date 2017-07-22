package com.venomvendor.sms.deduplicate.service;

public interface OnDeletedListener {

    void onResponse(int deletedMsgs, boolean interrupted);
}
