package org.nem.nac.ui.activities;

import android.os.Bundle;
import android.widget.TextView;

import org.nem.nac.R;
import org.nem.nac.application.LogFile;

import java.io.IOException;

public final class LogActivity extends NacBaseActivity {

	private TextView _logField;

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_log;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_log;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_logField = (TextView)findViewById(R.id.field_log);
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			final String log = LogFile.instance().read();
			_logField.setText(log.isEmpty() ? "No logs yet" : log);
		} catch (IOException e) {
			_logField.setText("Failed to read log");
		}
	}
}
