package org.nem.nac.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.annimon.stream.Optional;

import org.nem.nac.R;
import org.nem.nac.common.async.AsyncResult;
import org.nem.nac.common.enums.AccountType;
import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.models.account.Account;
import org.nem.nac.models.api.account.AccountInfoApiDto;
import org.nem.nac.models.api.account.AccountMetaDataPairApiDto;
import org.nem.nac.tasks.GetAccountInfoAsyncTask;
import org.nem.nac.ui.adapters.ViewPagerAdapter;
import org.nem.nac.ui.dialogs.ConfirmDialogFragment;
import org.nem.nac.ui.fragments.MsigAddCosigsFragment;
import org.nem.nac.ui.fragments.MsigRemoveCosigsFragment;

import timber.log.Timber;

public final class MultisigActivity extends NacBaseActivity {

	private TabLayout                _tabLayout;
	private ViewPager                _viewPager;
	private ViewPagerAdapter         _adapter;
	private MsigAddCosigsFragment    _addCosigsFragment;
	private MsigRemoveCosigsFragment _removeCosigsFragment;
	private Account                  _account;

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_multisig;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_multisig;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (lastUsedAddress == null) {
			Timber.e("Last used address was null");
			startActivity(new Intent(this, DashboardActivity.class));
			finish();
			return;
		}
		final Optional<Account> account = new AccountRepository().find(lastUsedAddress);
		if (!account.isPresent()) {
			Timber.e("Last used address is not valid account");
			startActivity(new Intent(this, DashboardActivity.class));
			finish();
			return;
		}
		_account = account.get();

		_addCosigsFragment = MsigAddCosigsFragment.create();
		_removeCosigsFragment = MsigRemoveCosigsFragment.create();
		_viewPager = (ViewPager)findViewById(R.id.viewpager);
		initViewPager(_viewPager);
		_tabLayout = (TabLayout)findViewById(R.id.tabs);
		_tabLayout.setupWithViewPager(_viewPager);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (_account == null) {
			Timber.e("Account was null");
			Toast.makeText(this, R.string.errormessage_error_occured, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		new GetAccountInfoAsyncTask(this, _account.publicData.address)
				.withCompleteCallback(this::onMyInfo)
				.execute();
	}

	private void onMyInfo(final GetAccountInfoAsyncTask task, final AsyncResult<AccountMetaDataPairApiDto> result) {
		Timber.d("Account info received");
		if (!result.getResult().isPresent()) {
			Timber.d("Account info not present");
			Toast.makeText(this, R.string.errormessage_failed_to_get_account_info, Toast.LENGTH_SHORT).show();
			return;
		}
		//
		if (!isMeResumed()) {
			return;
		}

		final AccountMetaDataPairApiDto accountInfo = result.getResult().get();
		if (accountInfo.account.publicKey == null) {
			final AccountRepository accountRepository = new AccountRepository();
			final Optional<Account> localAccount = accountRepository.find(accountInfo.account.address);
			if (!localAccount.isPresent()) {
				ConfirmDialogFragment.create(false, null, R.string.dialog_message_aborting_no_public_key, null)
						.setOnDismissListener(dialog -> finish())
						.show(getFragmentManager(), null);
				return;
			}
			accountInfo.account.publicKey = localAccount.get().publicData.publicKey;
		}
		final AccountType accountType = accountInfo.meta.getType();
		if (accountType == AccountType.MULTISIG) {
			ConfirmDialogFragment.create(false, null, R.string.dialog_message_multisig_cannot_manage_cosigs, null)
					.setOnDismissListener(dialog -> finish())
					.show(getFragmentManager(), null);
			return;
		}
		_addCosigsFragment.setMyInfo(accountInfo);
		if (accountType == AccountType.COSIGNATORY) {
			final ArrayAdapter<AccountInfoApiDto> multisigAdapter =
					new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
							accountInfo.meta.cosignatoryOf);
			_addCosigsFragment.setAccountsSpinnerAdapter(multisigAdapter);
			_removeCosigsFragment.setAccountsSpinnerAdapter(multisigAdapter);
			return;
		}
		if (accountType == AccountType.SIMPLE) {
			_addCosigsFragment.setAccountsSpinnerVisibility(View.GONE);
			_removeCosigsFragment.setAccountsSpinnerVisibility(View.GONE);
		}
	}

	private void initViewPager(ViewPager viewPager) {
		_adapter = new ViewPagerAdapter(getSupportFragmentManager());
		_adapter.addFragment(_addCosigsFragment, getString(R.string.title_fragment_add_cosig));
		_adapter.addFragment(_removeCosigsFragment, getString(R.string.title_fragment_remove_cosig));
		viewPager.setAdapter(_adapter);
	}
}
