<h1 align="center">Guardian</h1>
<h5 align="center">An Extensible AntiCheat Plugin For Sponge.</h5>

[![Build Status](https://travis-ci.org/ichorpowered/guardian.svg?branch=master)](https://travis-ci.org/ichorpowered/guardian) [![Sponge API](https://img.shields.io/badge/sponge--api-6.0.0-orange.svg)](https://github.com/SpongePowered/SpongeAPI) [![Precogs](https://img.shields.io/badge/precogs-1.0--SNAPSHOT-blue.svg)](https://github.com/me4502/Precogs)

Guardian is an extensible AntiCheat for Sponge that gives you the flexibility to customize the checks to fit your servers needs.
Guardian also provides a service for Precogs to reduce plugin conflicts with those who integrate with the lightweight service.

The plugin is a work in progress and there are plans for more detections to be added.

Cheats Guardian can detect so far:

- Horizontal Speed
- Vertical Speed
- Flight
- JetPack

And more coming soon.

Guardian has an optional mixin mod called [ElderGuardian](https://github.com/ichorpowered/elderguardian) that allows Guardian to access things beyond the Sponge API.
It is recommended that you use it with Guardian to improve accuracy. Some detections may also require it to work.

## Contributing

There many ways to contribute to the project. Some being...

- Creating issues for reporting bugs.

- Creating issues for reporting new cheats that are not detected or new features.

- Contributing to the plugin by making a pull request.

Developers:

To compile the project. Simply type `gradlew`, this will licenseFormat and build the project
for you.

## Versioning

The versioning follows:

`guardian-spongeapiversion-guardianapiversion-guardianpatchversion`

Ensure that the SpongeAPI version is the same for the one you are using on your server.

## Credits

 - [ModularFramework](https://github.com/me4502/ModularFramework) providing a modular class loading system.
 - [Precogs](https://github.com/me4502/Precogs) providing a service for plugins to integrate with the AntiCheat. 

 - Thanks to [AbilityAPI](https://github.com/AbilityAPI/abilityapi) for inspiring the sequence system.
 - Thanks to [CraftBook](https://github.com/sk89q/CraftBook) for inspiring the configuration system.


