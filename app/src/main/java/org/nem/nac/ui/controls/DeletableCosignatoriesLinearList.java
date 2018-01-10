package org.nem.nac.ui.controls;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.annimon.stream.function.Consumer;

import org.nem.nac.R;
import org.nem.nac.models.NacPublicKey;

public final class DeletableCosignatoriesLinearList extends LinearList<NacPublicKey> {

	private boolean _allowDeleting = true;
	private DeleteItemListener _deleteItemListener;

	public DeletableCosignatoriesLinearList(final Context context) {
		super(context);
		init();
	}

	public DeletableCosignatoriesLinearList(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DeletableCosignatoriesLinearList(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public DeletableCosignatoriesLinearList setOnDeleteItemListener(final DeleteItemListener listener) {
		_deleteItemListener = listener;
		return this;
	}

	public void allowDeleting(final boolean allow) {
		_allowDeleting = allow;
	}

	@NonNull
	@Override
	protected View getItemView(@NonNull final LayoutInflater inflater, @Nullable final NacPublicKey item) {
		final View itemView = inflater.inflate(R.layout.list_item_deletable_cosignatory, this, false);
		if (item == null) {
			return itemView;
		}
		//noinspection RedundantCast
		itemView.setTag((NacPublicKey)item);
		final TextView cosignatoryInput = (TextView)itemView.findViewById(R.id.text_view_cosignatory);
		cosignatoryInput.setText(item.toHexStr());
		final ImageButton deleteInput = (ImageButton)itemView.findViewById(R.id.image_view_delete);
		if (_allowDeleting) {
			deleteInput.setOnClickListener(v -> {
				removeItem(item);
				if (_deleteItemListener != null) {
					_deleteItemListener.accept(item);
				}
			});
		}
		else {
			deleteInput.setVisibility(INVISIBLE);
		}
		return itemView;
	}

	private void init() {
		setOrientation(VERTICAL);
	}

	public interface DeleteItemListener extends Consumer<NacPublicKey> {

	}
}
