package io.github.nahkd123.stonks.service;

/**
 * <p>
 * Configure the service with some administrative settings.
 * </p>
 * <ul>
 * <li><b>Lockdown</b>: Operator can lock the service down, preventing all users
 * from interacting with the service. Any interaction results in an
 * exception.</li>
 * <li><b>Overview samples</b>: The maximum number of top offers to be present
 * in {@link ProductOffersOverview}.</li>
 * </ul>
 */
public record ServiceConfig(boolean lockdown, int overviewSamples) {
}
