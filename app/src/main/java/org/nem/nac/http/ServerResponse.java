package org.nem.nac.http;

import android.support.annotation.NonNull;

import org.nem.nac.common.TimeSpan;
import org.nem.nac.models.network.Server;

public class ServerResponse<TModel> {

	public final Server server;
	public final TModel   model;
	public final TimeSpan responseTime;

	ServerResponse(@NonNull final Server server, @NonNull final TModel model, @NonNull final TimeSpan responseTime) {
		this.server = server;
		this.model = model;
		this.responseTime = responseTime;
	}
}
