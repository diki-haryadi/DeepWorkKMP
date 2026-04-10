package co.id.relay.digitals

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform