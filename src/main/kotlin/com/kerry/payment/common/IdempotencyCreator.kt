package com.kerry.payment.common

import java.util.UUID

class IdempotencyCreator {
    companion object {
        fun create(any: Any): String = UUID.nameUUIDFromBytes(any.toString().toByteArray()).toString()
    }
}
