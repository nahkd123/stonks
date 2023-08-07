package stonks.core.service;

/**
 * <p>
 * Local service allows you to save and load data for the service.
 * </p>
 */
public interface LocalStonksService extends StonksService {
	public void saveServiceData();

	public void loadServiceData();
}
