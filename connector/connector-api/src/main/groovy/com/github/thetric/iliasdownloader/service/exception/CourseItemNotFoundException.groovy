package com.github.thetric.iliasdownloader.service.exception

import groovy.transform.CompileStatic

/**
 * @author broj
 * @since 09.10.2016
 */
@CompileStatic
class CourseItemNotFoundException extends IliasException {
    private static final long serialVersionUID = 654654651649684L
    final String url

    CourseItemNotFoundException(String message, String url) {
        super(message + url)
        this.url = url
    }
}