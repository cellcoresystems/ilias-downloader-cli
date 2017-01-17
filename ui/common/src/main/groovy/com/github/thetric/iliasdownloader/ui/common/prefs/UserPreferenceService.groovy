package com.github.thetric.iliasdownloader.ui.common.prefs

import groovy.transform.CompileStatic

/**
 * Created by Dominik Broj on 31.01.2016.
 *
 * @author Dominik Broj
 * @since 31.01.2016
 */
@CompileStatic
interface UserPreferenceService {
    /**
     * Falls die Nutzereinstellungen vorhanden sind, werden diese geladen. Andernfalls wird eine {@link
     * java.nio.file.NoSuchFileException} geworfen.
     *
     * @return die Nutzereinstellungen (falls vorhanden)
     * @throws java.nio.file.NoSuchFileException
     *         falls die Nutzereinstellungen nicht gefunden werden konnten
     * @throws IOException
     *         falls ein I/O-Fehler auftritt
     */
    UserPreferences loadUserPreferences() throws IOException

    void saveUserPreferences(UserPreferences userPreferences) throws IOException
}