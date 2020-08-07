package com.example.contactstesthttp

import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MyAsync().execute()
    }

    // Has to be a class (can be inner class as below)
    inner class MyAsync() : AsyncTask<Void, Int, ContactsResponse>() { // Do work on worker/background thread
        override fun doInBackground(vararg p0: Void?):
                ContactsResponse {
            return getContactsApi()
        }

        override fun onPostExecute(result: ContactsResponse?) { // Communicate with main thread
            super.onPostExecute(result)
            textView.text = result!!.toString()
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
        }
    }

    fun getContactsApi(): ContactsResponse {
        val urlContacts
                = "https://api.androidhive.info/contacts/"
        val url: URL = URL(urlContacts)
        val httpUrlConnection: HttpURLConnection
                = url.openConnection() as HttpURLConnection

        httpUrlConnection.connectTimeout = 10000 // milliseconds
        httpUrlConnection.readTimeout = 15000    // milliseconds

        httpUrlConnection.requestMethod = "GET"
        httpUrlConnection.doInput = true

        httpUrlConnection.connect()

        val streamData = httpUrlConnection.inputStream
        val stringData = deSerializeStream(streamData)
        return convertToDataClass(stringData) // Type: ContactsResponse
    }

    private fun deSerializeStream(stream: InputStream): String {
        val builder = StringBuilder()
        val reader = BufferedReader(InputStreamReader(stream))
        var line: String = ""
        while (reader.readLine()
                .also({ line = it ?: "" }) != null) {
            builder.append(
                """$line
                    """.trimIndent()
            )
        }
        if (builder.length == 0) return ""
        else return builder.toString()
    }

    private fun convertToDataClass(stringData: String): ContactsResponse {
        val jsonResponse: JSONObject = JSONObject(stringData)          // Raw data
        val dataSet: ContactsResponse                                  // Cleaned data
        val listOfContacts: MutableList<ContactsItem> = mutableListOf()
        var contactsItem: ContactsItem
        var contactsPhone: Telephone

        val jsonArrayContacts = jsonResponse
            .getJSONArray("contacts")                           // Array of json contacts

        for(index in 0 until jsonArrayContacts.length()) {
            val jsonContactsItem: JSONObject
                    = jsonArrayContacts[index] as JSONObject

            val jsonTelephoneItem = jsonContactsItem
                .getJSONObject("phone")

            contactsPhone = Telephone(
                jsonTelephoneItem.getString("mobile"),
                jsonTelephoneItem.getString("home"),
                jsonTelephoneItem.getString("office")
            )
            contactsItem = ContactsItem(
                jsonContactsItem.getString("id"),
                jsonContactsItem.getString("name"),
                jsonContactsItem.getString("email"),
                jsonContactsItem.getString("address"),
                jsonContactsItem.getString("gender"),
                contactsPhone
            )
            listOfContacts.add(contactsItem)
        }
        dataSet = ContactsResponse(listOfContacts) // Just return constructor in real life
        return dataSet
    }
}