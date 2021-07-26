package mod.lucky.tools

import kotlinx.cli.*

fun main(args: Array<String>) {
    val parser = ArgParser("example")
    parser.parse(args)
    print("Hello")
}
