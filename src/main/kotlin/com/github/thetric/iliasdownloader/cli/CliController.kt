package com.github.thetric.iliasdownloader.cli

import com.github.thetric.iliasdownloader.cli.console.ConsoleService
import com.github.thetric.iliasdownloader.cli.sync.ItemDownloadingItemVisitor
import com.github.thetric.iliasdownloader.service.IliasService
import com.github.thetric.iliasdownloader.service.exception.IliasAuthenticationException
import com.github.thetric.iliasdownloader.service.webparser.JsoupCookieService
import com.github.thetric.iliasdownloader.service.webparser.WebParserIliasServiceProvider
import com.github.thetric.iliasdownloader.ui.common.prefs.PreferenceService
import com.github.thetric.iliasdownloader.ui.common.prefs.UserPreferences
import mu.KotlinLogging
import java.util.*

private val log = KotlinLogging.logger {}

/**
 * Connects to the Ilias and starts the sync.
 */
internal class CliController(
    private val resourceBundle: ResourceBundle,
    private val cliOptions: CliOptions,
    private val consoleService: ConsoleService,
    private val preferenceService: PreferenceService<UserPreferences>
) {

    fun startCliController() {
        try {
            val iliasService = createIliasService()
            val preferencesUpdateService = UserPreferencesUpdateServiceImpl(
                iliasService,
                resourceBundle,
                preferenceService,
                consoleService
            )
            val syncSettings =
                preferencesUpdateService.updatePreferences(cliOptions)
            val itemVisitor = ItemDownloadingItemVisitor(
                iliasService,
                resourceBundle,
                syncSettings.maxFileSizePerFileInMiB
            )
            val syncController = SyncController(
                iliasService,
                itemVisitor,
                resourceBundle,
                cliOptions.syncDir
            )
            syncController.startSync(syncSettings.courses)
        } catch (authEx: IliasAuthenticationException) {
            log.error(authEx) { resourceBundle.getString("login.error") }
        }
    }

    private fun createIliasService(): IliasService {
        val cookieService = JsoupCookieService()
        val iliasProvider = { url: String ->
            WebParserIliasServiceProvider(cookieService, url).newInstance()
        }

        val loginService = LoginServiceImpl(
            iliasProvider,
            resourceBundle,
            preferenceService,
            consoleService
        )

        return loginService.connect()
    }
}
