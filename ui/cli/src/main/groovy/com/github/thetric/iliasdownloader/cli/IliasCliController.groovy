package com.github.thetric.iliasdownloader.cli

import com.github.thetric.iliasdownloader.cli.configloader.ExistingConfigCliController
import com.github.thetric.iliasdownloader.cli.configloader.SetupController
import com.github.thetric.iliasdownloader.cli.console.ConsoleService
import com.github.thetric.iliasdownloader.cli.sync.SyncHandler
import com.github.thetric.iliasdownloader.cli.sync.SyncHandlerImpl
import com.github.thetric.iliasdownloader.service.IliasService
import com.github.thetric.iliasdownloader.service.model.Course
import com.github.thetric.iliasdownloader.service.model.IliasItem
import com.github.thetric.iliasdownloader.ui.common.prefs.UserPreferenceService
import com.github.thetric.iliasdownloader.ui.common.prefs.UserPreferences
import groovy.transform.TupleConstructor
import groovy.util.logging.Log4j2

import java.nio.file.NoSuchFileException
import java.util.function.Function

import static com.github.thetric.iliasdownloader.service.IliasService.VisitResult.CONTINUE
import static org.apache.logging.log4j.Level.DEBUG

@Log4j2
@TupleConstructor
final class IliasCliController {
    CliOptions cliOptions

    Function<String, IliasService> iliasProvider
    ResourceBundle resourceBundle
    UserPreferenceService preferenceService
    ConsoleService consoleService

    void start() {
        IliasService iliasService = createIliasService()
        UserPreferences prefs = preferenceService.loadUserPreferences()

        updateFileSizeLimitFromCliOpts(prefs)

        Collection<Course> coursesFromIlias = iliasService.joinedCourses
        Collection<Course> coursesToSync

        if (cliOptions.showCourseSelection) {
            try {
                coursesToSync = showAndSaveCourseSelection(coursesFromIlias)
                prefs.activeCourses = coursesToSync*.id.unique()
            } catch (CourseSelectionOutOfRange e) {
                log.catching(DEBUG, e)
                String errMsg = resourceBundle.getString('sync.courses.prompt.errors.out-of-range')
                System.err.println errMsg.replace('{0}', coursesFromIlias.size() as String)
                return
            }
        } else {
            println ''
            coursesToSync = coursesFromIlias.findAll { prefs.activeCourses.contains(it.id) }
        }

        println ''
        println ">>> Syncing ${coursesToSync.size()} courses:"
        print coursesToSync.collect { "  > ${it.name}" }.join('\n')

        executeSync(iliasService, coursesToSync, prefs)
    }

    private void updateFileSizeLimitFromCliOpts(UserPreferences prefs) {
        if (cliOptions.fileSizeLimitinMiB != null) {
            // limit = 0 -> unlimited
            if (cliOptions.fileSizeLimitinMiB >= 0) {
                prefs.maxFileSizeInMiB = cliOptions.fileSizeLimitinMiB
                preferenceService.saveUserPreferences(prefs)
            } else {
                def errMsg = "${resourceBundle.getString('args.sync.max-size.negative')} $cliOptions.fileSizeLimitinMiB"
                throw new IllegalArgumentException(errMsg)
            }
        }
    }

    private IliasService createIliasService() {
        try {
            log.debug('check for existing config in {}', cliOptions.syncDir.toAbsolutePath())
            return new ExistingConfigCliController(
                iliasProvider,
                resourceBundle,
                preferenceService,
                consoleService).start()
        } catch (NoSuchFileException settingsNotFoundEx) {
            log.warn('no config found in {}', cliOptions.syncDir.toAbsolutePath())
            log.catching(DEBUG, settingsNotFoundEx)
            return new SetupController(
                iliasProvider,
                resourceBundle,
                preferenceService,
                consoleService).startSetup()
        }
    }

    private void executeSync(IliasService iliasService, Collection<Course> coursesToSync, UserPreferences prefs) {
        println ''
        println ">>> ${resourceBundle.getString('sync.started')}"
        SyncHandler syncHandler = new SyncHandlerImpl(cliOptions.syncDir, iliasService, prefs)
        for (Course course : coursesToSync) {
            iliasService.visit(course, { IliasItem iliasItem ->
                syncHandler.handle(iliasItem)
                return CONTINUE
            })
        }
        println ''
        println ">>> ${resourceBundle.getString('sync.finished')}"
    }

    private Collection<Course> showAndSaveCourseSelection(Collection<Course> allCourses) {
        println ''
        println ">>> ${resourceBundle.getString('sync.courses.available')}"
        allCourses.eachWithIndex { Course course, int i ->
            println "\t${i + 1} ${course.name} (ID: ${course.id})"
        }
        List<Integer> courseIndices = consoleService.
            readLine('sync.courses', resourceBundle.getString('sync.courses.prompt'))
                                                    .split(/\s+/)
                                                    .collect { Integer.parseInt it }
                                                    .collect { it - 1 }
        if (courseIndices.any { it < 0 || it > allCourses.size() }) {
            throw new CourseSelectionOutOfRange()
        }
        return courseIndices.collect { allCourses[it] }
    }

    private static final class CourseSelectionOutOfRange extends RuntimeException {

    }
}
