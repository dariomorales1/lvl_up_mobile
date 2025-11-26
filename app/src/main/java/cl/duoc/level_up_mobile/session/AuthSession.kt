package cl.duoc.level_up_mobile.session

object AuthSession {

    var accessToken: String? = null
        private set

    var refreshToken: String? = null
        private set

    fun setTokens(access: String, refresh: String) {
        accessToken = access
        refreshToken = refresh
    }

    fun clear() {
        accessToken = null
        refreshToken = null
    }
}
