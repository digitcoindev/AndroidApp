package org.nem.nac.datamodel.mappers;

import android.support.annotation.Nullable;

import org.nem.nac.datamodel.entities.AppPasswordEntity;
import org.nem.nac.models.AppPassword;
import org.nem.nac.models.BinaryData;

public final class AppPasswordMapper {
	@Nullable
	public static AppPassword toModel(AppPasswordEntity src) {
		if (null == src) {
			return null;
		}
		return new AppPassword(src.passwordHash, new BinaryData(src.salt));
	}

	@Nullable
	public static AppPasswordEntity toEntity(AppPassword src) {
		if (null == src) {
			return null;
		}
		return new AppPasswordEntity(src.passwordHash, src.salt);
	}
}
