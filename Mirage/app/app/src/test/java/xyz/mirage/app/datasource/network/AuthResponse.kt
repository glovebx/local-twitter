package xyz.mirage.app.datasource.network

const val accountResponse: String = "{\n" +
        "        \"id\":\"1410270110072967168\",\n" +
        "        \"username\":\"sentrionic\",\n" +
        "        \"email\":\"sen@example.com\",\n" +
        "        \"displayName\":\"Sense\",\n" +
        "        \"image\":\"https://gravatar.com/avatar/b8abd16496a669abb69ad275a0b0103c?d=identicon\",\n" +
        "        \"banner\":null,\n" +
        "        \"bio\":null\n" +
        "}"

const val invalidCredentialsResponse: String = "{\n" +
        "    \"error\": {\n" +
        "        \"type\": \"AUTHORIZATION\",\n" +
        "        \"message\": \"Invalid email and password combination\"\n" +
        "    }\n" +
        "}"

const val emailInUseResponse: String = "{\n" +
        "    \"error\": {\n" +
        "        \"type\": \"CONFLICT\",\n" +
        "        \"message\": \"A user with that email already exists\"\n" +
        "    }\n" +
        "}"

const val usernameInUseResponse: String = "{\n" +
        "    \"error\": {\n" +
        "        \"type\": \"CONFLICT\",\n" +
        "        \"message\": \"A user with that username already exists\"\n" +
        "    }\n" +
        "}"

const val badRequestResponse: String = "{\n" +
        "    \"errors\": [\n" +
        "        {\n" +
        "            \"field\": \"Username\",\n" +
        "            \"message\": \"the length must be between 4 and 15.\"\n" +
        "        }\n" +
        "    ]\n" +
        "}"

fun missingFieldResponse(field: String): String = "{\n" +
        "    \"errors\": [\n" +
        "        {\n" +
        "            \"field\": \"$field\",\n" +
        "            \"message\": \"cannot be blank.\"\n" +
        "        }\n" +
        "    ]\n" +
        "}"