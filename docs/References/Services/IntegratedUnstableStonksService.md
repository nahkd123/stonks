# `IntegratedUnstableStonksService`
=== "Fabric"
    ```naharaconfig
    useService stonks.fabric.service.IntegratedUnstableStonksService
        // The configurations is the same as IntegratedStonksService, except for 2
        // things:

        // Maximum lag time in MS
        maxLag 5000

        // Rate to throw an exception, simulating a faulty service
        failRate 0.2
    ```

!!! warning
    This service was created for testing purpose! It was created to test Stonks implementation on different platforms, ensuring those implementations behaves correctly, such as handling unexpected error or unstable network condition.

    You do not want to use this service in your production environment.
