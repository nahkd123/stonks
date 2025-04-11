package io.github.nahkd123.stonks.service.provider.database;

public interface DatabaseServiceOption {
	enum Standard implements DatabaseServiceOption {
		/**
		 * <p>
		 * Backup table when performing table migration. The backed up table can be
		 * restored by deleting migrated table and rename the backup to table with
		 * current version.
		 * </p>
		 */
		BACKUP_ON_MIGRATE;
	}
}
