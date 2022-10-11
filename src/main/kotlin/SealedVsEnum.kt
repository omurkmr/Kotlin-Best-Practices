//we can use data classes with own values
sealed class HttpError(code: Int) {
    data class Unauthorized(val reason: String) : HttpError(401)
    object NotFound : HttpError(404)

    fun doNothing() {}
}

//cannot pass variable with interface
sealed interface HttpErrorInterface {
    data class Unauthorized(val reason: String) : HttpErrorInterface
    object NotFound : HttpErrorInterface

    fun doNothing() {}
}

enum class HttpErrorEnum(code: Int) {
    Unauthorized(401),
    NotFound(404);

    fun doNothing() {}
}

fun enumUsage() {
    val sealedError: HttpError = HttpError.Unauthorized("token not valid")

    when (sealedError) {
        HttpError.NotFound -> Unit
        is HttpError.Unauthorized -> Unit
    }

    val sealedInterfaceError: HttpErrorInterface = HttpErrorInterface.Unauthorized("token not valid")

    when (sealedInterfaceError) {
        HttpErrorInterface.NotFound -> Unit
        is HttpErrorInterface.Unauthorized -> Unit
    }

    val enumError: HttpErrorEnum = HttpErrorEnum.Unauthorized

    //this is possible with enum only
    HttpErrorEnum.values().forEach { }

    when (enumError) {
        HttpErrorEnum.Unauthorized -> Unit
        HttpErrorEnum.NotFound -> Unit
    }
}