package tintor.frpr.model;

/** @author Marko Tintor (tintor@gmail.com) */
public enum Variant {
	Fund_Raising(Company.statusFR, Company.Field.orderFR), Public_Relations(Company.statusPR, Company.Field.orderPR);

	/** Da li je aplikacija za Fund Raising ili Public Relations? */
	public final static Variant current = Fund_Raising;

	final String[] status;
	public final Company.Field[] order;

	/** @param status Moguce vrednosti statusa kompanije.
	 *  @param order  Redosled kolona kompanije. */
	Variant(final String[] status, final Company.Field[] order) {
		this.status = status;
		this.order = order;
	}
}