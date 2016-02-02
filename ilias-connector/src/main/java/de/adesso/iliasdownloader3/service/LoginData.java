package de.adesso.iliasdownloader3.service;

import lombok.Value;

/**
 * Created by Dominik Broj on 01.02.2016.
 *
 * @author Dominik Broj
 * @since 01.02.2016
 */
@Value
public final class LoginData {
	private final String userName, password;
	private final LoginType loginType;
	private final DownloadMethod downloadMethod;
}
