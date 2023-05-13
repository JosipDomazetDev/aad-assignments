package com.example.homework3.model

enum class DataStatus {
    CACHED, INITIAL, SUCCESS, ERROR, LOADING
}

class StateWrapper<out T>(val status: DataStatus, val data: T?, val message: String?, val error: Throwable?) {
    companion object {

        fun <T> cached(data: T?): StateWrapper<T> {
            return StateWrapper(DataStatus.CACHED, data, null, null)
        }

        fun <T> init(): StateWrapper<T> {
            return StateWrapper(DataStatus.INITIAL, null, null, null)
        }

        fun <T> success(data: T?): StateWrapper<T> {
            return StateWrapper(DataStatus.SUCCESS, data, null, null)
        }

        fun <T> error(msg: String, error: Throwable?): StateWrapper<T> {
            return StateWrapper(DataStatus.ERROR, null, msg, error)
        }

        fun <T> loading(): StateWrapper<T> {
            return StateWrapper(DataStatus.LOADING, null, null, null)
        }
    }
}