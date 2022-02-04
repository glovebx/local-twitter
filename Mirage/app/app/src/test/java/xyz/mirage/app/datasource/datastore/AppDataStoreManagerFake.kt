package xyz.mirage.app.datasource.datastore

import xyz.mirage.app.business.datasources.datastore.AppDataStore

class AppDataStoreManagerFake : AppDataStore {

    private val datastore: MutableMap<String, String> = mutableMapOf()

    override suspend fun setValue(key: String, value: String) {
        datastore[key] = value
    }

    override suspend fun readValue(key: String): String? {
        return datastore[key]
    }
}