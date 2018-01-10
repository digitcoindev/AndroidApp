package org.nem.nac.ui.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.annimon.stream.function.Consumer;

import org.nem.nac.R;
import org.nem.nac.application.AppHost;
import org.nem.nac.ui.utils.InputErrorUtils;

public final class EditFieldDialogFragment extends NacBaseDialogFragment {

	private static final String ARG_STR_VALUE        = "arg-value";
	private static final String ARG_BOOL_ALLOW_EMPTY = "arg-allow-empty";
	private static final String ARG_STR_HINT         = "arg-hint";

	public static EditFieldDialogFragment create(final @StringRes Integer titleRes, final String hint, final String initialValue,
			final boolean allowEmpty) {
		EditFieldDialogFragment fragment = new EditFieldDialogFragment();
		Bundle args = NacBaseDialogFragment.setArgs(true, titleRes, true, null);
		args.putString(ARG_STR_VALUE, initialValue);
		args.putBoolean(ARG_BOOL_ALLOW_EMPTY, allowEmpty);
		args.putString(ARG_STR_HINT, hint);
		fragment.setArguments(args);
		return fragment;
	}

	private EditText         _input;
	private String           _value;
	private String           _hint;
	private Consumer<String> _valueChangedListener;
	private boolean _valueChanged = false;
	private boolean _allowEmpty   = false;

	public String getValue() {
		return _value;
	}

	public EditFieldDialogFragment setOnConfirmChangedValueListener(final Consumer<String> listener) {
		_valueChangedListener = listener;
		return this;
	}

	@Override
	protected int getContentLayout() {
		return R.layout.fragment_edit_field_dialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			_value = getArguments().getString(ARG_STR_VALUE, "");
			_allowEmpty = getArguments().getBoolean(ARG_BOOL_ALLOW_EMPTY, false);
			_hint = getArguments().getString(ARG_STR_HINT, "");
		}
	}

	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View layout = super.onCreateView(inflater, container, savedInstanceState);
		_input = (EditText)layout.findViewById(R.id.input_value);
		_input.setHint(_hint);
		_input.setText(_value);
		_input.setSelection(_value.length());
		_input.addTextChangedListener(_changeWatcher);
		return layout;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		AppHost.SoftKeyboard.forceShowAsync();
	}

	@Override
	protected void onConfirmClick(final View clicked) {
		if (!_allowEmpty && _input.getText().length() == 0) {
			InputErrorUtils.setErrorState(_input, null);
			return;
		}
		super.onConfirmClick(clicked);
		if (_valueChangedListener != null && _valueChanged) {
			_valueChangedListener.accept(_input.getText().toString());
		}
	}

	private final TextWatcher _changeWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {}

		@Override
		public void afterTextChanged(final Editable s) {
			_valueChanged = true;
			_value = s.toString();
		}
	};
}
