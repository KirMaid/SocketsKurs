import java.net.ServerSocket
import java.net.Socket

fun main() {
    val server = ServerSocket(1234)
    println("Server started")

    while (true) {
        val client = server.accept()
        println("Client connected: ${client.inetAddress.hostAddress}")

        val connection = Connection(client)
        Thread(connection).start()
    }
}

class Connection(val client: Socket) : Runnable {
    override fun run() {
        val input = client.getInputStream().bufferedReader()
        val output = client.getOutputStream().bufferedWriter()

        while (true) {
            val request = input.readLine()
            if (request == null || request == "exit") {
                client.close()
                println("Client disconnected")
                break
            }

            val response = processRequest(request)
            output.write(response)
            output.newLine()
            output.flush()
        }
    }

    private fun processRequest(request: String): String {
        val parts = request.split("|")
        val command = parts[0]
        val data = parts.getOrNull(1)

        return when (command) {
            "create" -> createUser(data)
            "read" -> readUser(data)
            "update" -> updateUser(data)
            "delete" -> deleteUser(data)
            else -> "Unknown command"
        }
    }

    private fun createUser(data: String?): String {
        val fields = data?.split(",") ?: return "Invalid data"

        val name = fields.getOrNull(0) ?: return "Invalid data"
        val age = fields.getOrNull(1)?.toIntOrNull() ?: return "Invalid data"

        if (getUserByName(name) != null) {
            return "User already exists"
        }

        val id = getNextUserId()
        val user = "$id|$name|$age"

        addUser(user)

        return "User created: $user"
    }

    private fun readUser(data: String?): String {
        val name = data ?: return "Invalid data"

        val user = getUserByName(name) ?: return "User not found"

        return "User found: $user"
    }

    private fun updateUser(data: String?): String {
        val fields = data?.split(",") ?: return "Invalid data"

        val name = fields.getOrNull(0) ?: return "Invalid data"
        val age = fields.getOrNull(1)?.toIntOrNull() ?: return "Invalid data"

        val user = getUserByName(name) ?: return "User not found"

        val id = user.split("|")[0]

        val newUser = "$id|$name|$age"

        updateUser(user, newUser)

        return "User updated: $newUser"
    }

    private fun deleteUser(data: String?): String {
        val name = data ?: return "Invalid data"

        val user = getUserByName(name) ?: return "User not found"

        deleteUser(user)

        return "User deleted: $user"
    }

    private fun addUser(user: String) {
        val file = getUsersFile()
        file.appendText("$user\n")
    }

    private fun getNextUserId(): Int {
        val users = getUsers()

        return users.lastOrNull()?.split("|")?.getOrNull(0)?.toIntOrNull()?.plus(1) ?: 1
    }

    private fun getUsers(): List<String> {
        val file = getUsersFile()
        return file.readLines().filter { it.isNotBlank() }
    }

    private fun getUserByName(name: String): String? {
        val users = getUsers()

        return users.find { it.split("|")[1] == name }
    }

    private fun updateUser(oldUser: String, newUser: String) {
        val file = getUsersFile()

        val oldLine = "${oldUser}\n"
        val newLine = "${newUser}\n"

        file.writeText(file.readText().replace(oldLine, newLine))
    }

    private fun deleteUser(user: String) {
        val file = getUsersFile()

        val line = "${user}\n"

        file.writeText(file.readText().replace(line, ""))
    }

    private fun getUsersFile() = java.io.File("users.txt")
}