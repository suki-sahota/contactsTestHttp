package com.example.contactstesthttp

data class ContactsResponse(
    val contacts: List<ContactsItem>
)

data class ContactsItem(
    val id: String,
    val name: String,
    val email: String,
    val address: String,
    val gender: String,
    val phone: Telephone
)

data class Telephone(
    val mobile: String,
    val home: String,
    val office: String
)
