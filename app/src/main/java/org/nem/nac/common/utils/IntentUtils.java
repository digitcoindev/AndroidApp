package org.nem.nac.common.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class IntentUtils {

	public static void startSharingChooser(
			@NonNull Context context,
			@NonNull final String type,
			@NonNull String chooserTitle, @Nullable final String subject, @Nullable final String body, @Nullable Parcelable stream) {
		final Intent intent = new Intent(Intent.ACTION_SEND)
				.setType(type);
		if (subject != null) {
			intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		}
		if (body != null) {
			intent.putExtra(android.content.Intent.EXTRA_TEXT, body);
		}
		if (stream != null) {
			intent.putExtra(Intent.EXTRA_STREAM, stream);
		}
		context.startActivity(Intent.createChooser(intent, chooserTitle));
	}
}
