# Guardian

> An experimental Sponge anticheat.

Guardian is an extensible anticheat client for Sponge servers.

Guardian is a work in progress and currently has no built in violation modules.
As well as that, the API is under construction.

## Usage

To compile the project. Simply type `gradlew`, this will licenseFormat and build the project
for you.

## Versioning

The versioning follows:

`guardian-spongeapiversion-guardianapiversion-guardianpatchversion`

Ensure that the SpongeAPI version is the same for the one you are using on your server.

The Guardian API Version follows semver and any addon violations from external plugins should depend on it as so.

The Guardian Patch Version is for changes to the individual violation modules.

Many changes to violation modules may push for incrementing the Guardian API Version patch number.
Although most patches are not pushed to the API patch until tested, thus being experimental.

## Credits

Thanks to [AbilityAPI](https://github.com/AbilityAPI/abilityapi) for inspiring the sequence system.
Thanks to [CraftBook](https://github.com/sk89q/CraftBook) for inspiring the configuration system.


