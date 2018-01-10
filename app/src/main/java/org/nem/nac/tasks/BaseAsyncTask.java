package org.nem.nac.tasks;

import android.os.AsyncTask;
import android.support.annotation.CallSuper;
import android.support.annotation.StringRes;

import com.annimon.stream.Optional;

import org.nem.nac.common.async.AsyncResult;
import org.nem.nac.common.async.AsyncResultImpl;
import org.nem.nac.common.utils.AssertUtils;
import org.nem.nac.models.network.Server;
import org.nem.nac.servers.ServerFinder;
import org.nem.nac.ui.activities.NacBaseActivity;

import java.lang.ref.SoftReference;

import timber.log.Timber;

public abstract class BaseAsyncTask<TTask extends BaseAsyncTask, TParams, TProgress, TResult>
		extends AsyncTask<TParams, TProgress, TResult> {

	//protected       ProgressDialog                    progressDialog;
	protected final Integer                           progressMessageRes;
	protected       TaskAsyncCallback<TTask, TResult> callback;
	protected Server server;
	private final   SoftReference<NacBaseActivity>    _activity;

	/**
	 * Starts task without showing any progress message
	 */
	public BaseAsyncTask() {
		_activity = null;
		progressMessageRes = null;
	}

	/**
	 * @param activity Pass an activity which displays progress.
	 * @param progressMessageRes Progress message to show
	 */
	public BaseAsyncTask(final NacBaseActivity activity, @StringRes final int progressMessageRes) {
		AssertUtils.notNull(activity, "activity");
		this.progressMessageRes = progressMessageRes;
		_activity = new SoftReference<>(activity);
	}

	/**
	 * Callback that will be called if task has been completed (not canceled).
	 */
	public BaseAsyncTask<TTask, TParams, TProgress, TResult> withCompleteCallback(final TaskAsyncCallback<TTask, TResult> callback) {
		this.callback = callback;
		return this;
	}

	/**
	 * Populates server field. Returns true if successful.
	 * Does all the logging internally.
	 */
	protected boolean populateServer() {
		final Optional<Server> server = ServerFinder.instance().getBest();
		if (server.isPresent()) {
			this.server = server.get();
		}
		else {
			this.server = null;
			Timber.e("No server!");
		}
		return server.isPresent();
	}

	@CallSuper
	@Override
	protected void onPreExecute() {
		if (progressMessageRes == null) {
			return;
		}

		final NacBaseActivity activity = _activity.get();
		if (activity != null) {
			activity.showProgressDialog(progressMessageRes);
		}
	}

	/**
	 * Dismisses progress dialog and calls complete callback.
	 */
	@CallSuper
	@Override
	protected void onPostExecute(final TResult result) {
		safeDismissProgress();
		final NacBaseActivity activity = _activity != null ? _activity.get() : null;
		if (callback != null && (_activity == null || activity != null && activity.isNotDestroyed())) {
			callback.apply((TTask)this, new AsyncResultImpl<>(result));
		}
	}

	@CallSuper
	@Override
	protected void onCancelled(final TResult tResult) {
		safeDismissProgress();
	}

	private void safeDismissProgress() {
		if (progressMessageRes == null) {
			return;
		}
		final NacBaseActivity activity = _activity.get();
		Timber.d("Trying to dismiss progress dialog for activity %s", activity == null ? null : activity.getClass().getSimpleName());
		if (activity != null) {
			activity.dismissProgressDialog();
		}
	}

	public interface TaskAsyncCallback<TTask extends BaseAsyncTask, TResult> {
		void apply(final TTask task, final AsyncResult<TResult> result);
	}
}
