package org.nem.nac.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import org.nem.nac.R;
import org.nem.nac.models.NacPublicKey;

import java.util.List;

import timber.log.Timber;

public final class SelectSenderActivity extends NacBaseActivity {

	public static final String EXTRA_STR_ARR_ACCOUNTS_HEX = SelectSenderActivity.class.getCanonicalName() + "e-accounts";
	public static final String EXTRA_STR_SELECTED_ACCOUNT = SelectSenderActivity.class.getCanonicalName() + "e-selected-acc";

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_select_sender;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_select_sender;
	}

	@Override
	protected void onBeforeSetContent(final Bundle savedInstanceState) {
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		final String[] accountsHex = getIntent().getStringArrayExtra(EXTRA_STR_ARR_ACCOUNTS_HEX);
		final List<NacPublicKey> accPublicKeys = Stream.of(accountsHex)
			.map(NacPublicKey::new)
			.collect(Collectors.toList());

		final ArrayAdapter<NacPublicKey> adapter = new ArrayAdapter<>(this, R.layout.list_item_select_sender, accPublicKeys);
		final ListView accountsList = (ListView)findViewById(R.id.listview_accounts);
		accountsList.setAdapter(adapter);
		accountsList.setOnItemClickListener(this::onAccountClick);
	}

	private void onAccountClick(final AdapterView<?> adapterView, final View view, final int pos, final long id) {
		final NacPublicKey account = (NacPublicKey)adapterView.getItemAtPosition(pos);
		Timber.d("Account selected: %s", account);
		final Intent data = new Intent().putExtra(EXTRA_STR_SELECTED_ACCOUNT, account.toHexStr());
		setResult(Activity.RESULT_OK, data);
		finish();
	}
}
