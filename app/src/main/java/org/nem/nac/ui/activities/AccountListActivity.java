package org.nem.nac.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.terlici.dragndroplist.DragNDropListView;

import org.nem.nac.R;
import org.nem.nac.application.AppSettings;
import org.nem.nac.common.utils.StringUtils;
import org.nem.nac.datamodel.NacPersistenceRuntimeException;
import org.nem.nac.datamodel.NemSQLiteHelper;
import org.nem.nac.datamodel.repositories.AccountRepository;
import org.nem.nac.models.account.Account;
import org.nem.nac.models.primitives.AddressValue;
import org.nem.nac.providers.AddressInfoProvider;
import org.nem.nac.ui.adapters.AccountListAdapter;
import org.nem.nac.ui.dialogs.EditFieldDialogFragment;
import org.nem.nac.ui.utils.Toaster;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public final class AccountListActivity extends NacBaseActivity {

	public static void start(Context context) {
		final Intent intent = new Intent(context, AccountListActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		AppSettings.instance().clearLastUsedAccAddress();
		context.startActivity(intent);
	}

	private DragNDropListView  _accountsListview;
	private AccountListAdapter _accountsAdapter;
	private Snackbar           _undoSnackbar;
	private View               _toolbarRightPanel;
	private TextView           _toolbarRightLabel;
	private final List<Long> _toDelete = new ArrayList<>();
	private String _undoAccountName;

	@Override
	protected int getLayoutId() {
		return R.layout.activity_account_list;
	}

	@Override
	protected int getActivityTitle() {
		return R.string.title_activity_account_list;
	}

	@Override
	public void onBackPressed() {
		if (_accountsListview != null && _accountsAdapter != null && _accountsAdapter.getIsEditMode()) {
			toggleEditMode();
		}
		else {
			finish();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		final Button addAccountBtn = ((Button)findViewById(R.id.btn_add_account));
		addAccountBtn.setOnClickListener(this::onAddAccountBtnClick);
		_accountsListview = (DragNDropListView)findViewById(R.id.listview_accounts);
		_accountsListview.setOnItemClickListener(this::onItemClick);
		setAccountsListDragNDropListener();
		_toolbarRightLabel = (TextView)findViewById(R.id.toolbar_right_text);

		try {
			setAccountsListViewAdapter();
		} catch (NacPersistenceRuntimeException e) {
			Timber.e(e, "Failed to set account list adapter");
			Toast.makeText(this, R.string.errormessage_error_occured, Toast.LENGTH_LONG).show();
		}
		if (_accountsAdapter.getCount() < 1) {
			_toolbarRightPanel.setClickable(false);
			_toolbarRightPanel.setVisibility(View.INVISIBLE);
		}
		else {
			_toolbarRightPanel.setClickable(true);
			_toolbarRightPanel.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (_toDelete.isEmpty() && _undoSnackbar != null) {
			_undoSnackbar.dismiss();
			_undoSnackbar = null;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		deletePendingAccounts();
	}

	@Override
	protected void setToolbarItems(@NonNull final Toolbar toolbar) {
		final ImageView leftIcon = (ImageView)toolbar.findViewById(R.id.toolbar_left_icon);
		leftIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_hd_configuration));
		leftIcon.setOnClickListener(this::onConfigurationClick);
		_toolbarRightPanel = findViewById(R.id.toolbar_right_panel);
		_toolbarRightPanel.setClickable(true);
		_toolbarRightPanel.setOnClickListener(this::onEditBtnClick);
		_toolbarRightLabel = (TextView)findViewById(R.id.toolbar_right_text);
		_toolbarRightLabel.setText(R.string.toolbar_btn_edit);
		_toolbarRightLabel.setVisibility(View.VISIBLE);
	}

	private void onConfigurationClick(final View clicked) {
		startActivity(new Intent(this, ConfigurationActivity.class));
	}

	private void onAddAccountBtnClick(final View clicked) {
		deletePendingAccounts();
		startActivity(new Intent(this, AddAccountActivity.class));
	}

	private void onEditBtnClick(final View clicked) {
		toggleEditMode();
		if (_accountsAdapter != null && _accountsListview != null) {
			_accountsListview.setDraggingEnabled(_accountsAdapter.getIsEditMode());
		}
	}

	private void toggleEditMode() {
		if (_accountsAdapter != null) {
			_accountsAdapter.toggleEditMode();
			_toolbarRightLabel.setText(_accountsAdapter.getIsEditMode()
					? R.string.toolbar_btn_done
					: R.string.toolbar_btn_edit);
			if (!_accountsAdapter.getIsEditMode()) {
				deletePendingAccounts();
				if (_undoSnackbar != null) { _undoSnackbar.dismiss(); }
			}
		}
		if (_accountsListview != null) {
			_accountsListview.post(_accountsListview::invalidateViews);
		}
	}

	private void onItemClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
		if (_accountsAdapter.getIsEditMode()) { return; }
		final AccountListAdapter.AccountItem account = (AccountListAdapter.AccountItem)adapterView.getItemAtPosition(position);
		startActivity(new Intent(this, DashboardActivity.class).putExtra(DashboardActivity.EXTRA_PARC_ACCOUNT_ADDRESS, new AddressValue(account.rawAddress)));
	}

	private void onItemEditClick(final AccountListAdapter.AccountItem account) {
		EditFieldDialogFragment.create(R.string.dialog_title_edit_account, getString(R.string.input_hint_account_name_edit), account.name, false)
				.setOnConfirmChangedValueListener(value -> {
					if (StringUtils.isNullOrEmpty(value)) {
						return;
					}

					final long accountId = account.id;
					try {
						final AccountRepository repository = new AccountRepository();
						final Account acc = repository.get(accountId);
						acc.name = value;
						repository.save(acc);
						AddressInfoProvider.instance().invalidateLocal();
						if (_accountsAdapter != null && isNotDestroyed()) {
							Timber.d("Notifying adapter about data change");
							_accountsAdapter.notifyDataSetChanged();
						}
						Toaster.instance().show(R.string.message_saved);
					} catch (NacPersistenceRuntimeException e) {
						Timber.e(e, "Failed to save account with name %s", value);
						Toaster.instance().showGeneralError();
					}
				})
				.show(getFragmentManager(), null);
	}

	private void onItemDeleteClick(final AccountListAdapter.AccountItem account) {
		_undoAccountName = account.name;
		_toDelete.add(account.id);
		_accountsAdapter.hideAccount(account.id);
		if (_undoSnackbar != null) {
			_undoSnackbar.setText(StringUtils.format(R.string.message_account_deleted, account.name));
		}
		else {
			_undoSnackbar = createUndoSnackbar();
		}

		_undoSnackbar.show();
	}

	private Snackbar createUndoSnackbar() {
		final Snackbar snackbar = Snackbar.make(findViewById(R.id.layout_coordinator),
				StringUtils.format(R.string.message_account_deleted, StringUtils.isNotNullOrEmpty(_undoAccountName)
						? _undoAccountName
						: ""), Snackbar.LENGTH_INDEFINITE)
							.setAction(R.string.action_undo, this::onUndoDeleteClick)
							.setActionTextColor(getResources().getColor(R.color.official_green));
		snackbar.getView().setBackgroundColor(getResources().getColor(R.color.background_material_dark));
		snackbar.setCallback(new Snackbar.Callback() {
				@Override
				public void onDismissed(final Snackbar snackbar, final int event) {
					super.onDismissed(snackbar, event);
					if (_toDelete.isEmpty()) { return; }
					getHandler().post(() -> {
						_undoSnackbar = createUndoSnackbar();
		_undoSnackbar.show();
					});
				}
		});
		return snackbar;
	}

	private void onUndoDeleteClick(final View clicked) {
		_toDelete.clear();
		_accountsAdapter.showAllAccounts();
		_undoSnackbar.dismiss();
		_undoSnackbar = null;
	}

	private void setAccountsListViewAdapter()
			throws NacPersistenceRuntimeException {
		final List<Account> accounts = new AccountRepository().getAllSorted();
		final boolean wasEditMode = _accountsAdapter != null && _accountsAdapter.getIsEditMode();
		_accountsAdapter = new AccountListAdapter(this, accounts);
		_accountsAdapter.setOnEditClickListener(this::onItemEditClick);
		_accountsAdapter.setOnDeleteClickListener(this::onItemDeleteClick);
		_accountsListview.setDraggingEnabled(true);
		_accountsListview.setDragNDropAdapter(_accountsAdapter);
		if (_toDelete != null) {
			for (Long id : _toDelete) {
				_accountsAdapter.hideAccount(id);
			}
		}
		if (wasEditMode) { _accountsAdapter.toggleEditMode(); }
	}

	private void setAccountsListDragNDropListener() {
		_accountsListview.setOnItemDragNDropListener(new DragNDropListView.OnItemDragNDropListener() {
			@Override
			public void onItemDrag(final DragNDropListView parent, final View view, final int position, final long id) {}

			@Override
			public void onItemDrop(final DragNDropListView parent, final View view, final int startPosition, final int endPosition, final long id) {
				final AccountRepository accountRepository = new AccountRepository();
				final List<Account> allAccounts = accountRepository.getAllSorted();
				final List<Account> aliveAndSorted = Stream.of(allAccounts)
						.filter(acc -> !_toDelete.contains(acc.id))
						.sorted((lhs, rhs) -> lhs.sortIndex - rhs.sortIndex)
						.collect(Collectors.toList());

				try {
					final Account dropped = aliveAndSorted.get(startPosition);
					final Account moved = aliveAndSorted.get(endPosition);
					allAccounts.remove(dropped);
					int moveIndex = allAccounts.indexOf(moved);
					if (startPosition < endPosition) {
						moveIndex++;
					}
					allAccounts.add(moveIndex, dropped);
					//
					NemSQLiteHelper sqLiteHelper = null;
					try {
						sqLiteHelper = NemSQLiteHelper.getInstance();
						sqLiteHelper.beginTransaction();
						//
						for (int i = 0; i < allAccounts.size(); i++) {
							final Account account = allAccounts.get(i);
							account.sortIndex = i;
							accountRepository.save(account);
						}
						//
						sqLiteHelper.commitTransaction();
					} finally {
						if (sqLiteHelper != null) { sqLiteHelper.endTransaction(); }
					}
					setAccountsListViewAdapter();
					//
				} catch (Throwable e) {
					Timber.e("Sorting failed!");
					Toaster.instance().showGeneralError();
				}
			}
		});
	}

	private void deletePendingAccounts() {
		if (_toDelete.isEmpty()) { return; }
		final AccountRepository accountRepository = new AccountRepository();
		while (!_toDelete.isEmpty()) {
			final Long deadId = _toDelete.remove(0);
			try {
				accountRepository.delete(deadId);
			} catch (NacPersistenceRuntimeException e) {
				Timber.e(e, "Failed to delete account!");
			}
		}
		AddressInfoProvider.instance().invalidateLocal();
	}
}
