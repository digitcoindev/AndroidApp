package org.nem.nac.share;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public final class ShareHelper {

	public static void shareQr(final Context context, final String subject, final String body, String filePath, final String chooserName) {
		final Intent emailIntent = new Intent(Intent.ACTION_SEND).setType("image/png")
				.putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
				.putExtra(android.content.Intent.EXTRA_TEXT, body)
				.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
		//
		// get available share intents
		List<Intent> targets = new ArrayList<>();
		Intent template = new Intent(Intent.ACTION_SEND);
		template.setType(emailIntent.getType());
		List<ResolveInfo> candidates = context.getPackageManager().
				queryIntentActivities(template, 0);

		// remove facebook which has a broken share intent
		for (ResolveInfo candidate : candidates) {
			String packageName = candidate.activityInfo.packageName;
			if (!packageName.contains("facebook")) {
				Intent target = new Intent(android.content.Intent.ACTION_SEND);
				target.setType(emailIntent.getType());
				target.putExtra(Intent.EXTRA_TEXT, body);
				target.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
				target.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
				target.setPackage(packageName);
				targets.add(target);
			}
		}
		Intent chooser = Intent.createChooser(targets.remove(0), chooserName);
		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targets.toArray(new Parcelable[targets.size()]));
		context.startActivity(chooser);
	}
}
