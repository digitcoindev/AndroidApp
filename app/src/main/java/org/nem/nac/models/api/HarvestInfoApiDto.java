package org.nem.nac.models.api;

import org.nem.nac.common.models.TimeValue;
import org.nem.nac.models.Xems;

public final class HarvestInfoApiDto {

	/**
	 * The number of seconds elapsed since the creation of the nemesis block.
	 */
	public TimeValue timeStamp;
	/**
	 * The database id for the harvested block.
	 */
	public int       id;
	/**
	 * The block difficulty. The initial difficulty was set to 100 000 000 000 000.
	 * The block difficulty is always between one tenth and ten times the initial difficulty.
	 */
	public long      difficulty;
	/**
	 * The total fee collected by harvesting the block.
	 */
	public Xems      totalFee;
	/**
	 * The height of the harvested block.
	 */
	public int       height;
}
