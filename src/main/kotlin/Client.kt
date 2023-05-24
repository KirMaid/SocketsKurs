import java.net.Socket
import java.util.*

fun main() {
    val scanner = Scanner(System.`in`)

    while (true) {
        print("Enter command (create|read|update|delete|exit): ")
        val command = scanner.nextLine()

        if (command == "exit") {
            break
        }

        print("Enter data: ")
        val data = scanner.nextLine()

        var response = ""

        Socket("localhost", 1234).use { socket ->
            val input = socket.getInputStream().bufferedReader()
            val output = socket.getOutputStream().bufferedWriter()

            output.write("$command|$data")
            output.newLine()
            output.flush()

            response = input.readLine()
        }

        println("Response: $response")
    }
}