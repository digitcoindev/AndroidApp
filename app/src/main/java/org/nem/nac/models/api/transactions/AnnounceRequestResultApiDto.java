package org.nem.nac.models.api.transactions;

import org.nem.nac.common.models.HashValue;
import org.nem.nac.models.api.RequestResultApiDto;

public final class AnnounceRequestResultApiDto extends RequestResultApiDto {

	public HashValue transactionHash;
	public HashValue innerTransactionHash;
}
