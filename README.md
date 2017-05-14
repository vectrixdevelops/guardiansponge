<h1 align="center">Guardian</h1>
<h5 align="center">An Extensible AntiCheat Plugin For Sponge.</h5>

[![Build Status](https://travis-ci.org/ichorpowered/guardian.svg?branch=master)](https://travis-ci.org/ichorpowered/guardian) [![Sponge API](https://img.shields.io/badge/sponge--api-6.0.0-orange.svg)](https://github.com/SpongePowered/SpongeAPI) [![Precogs](https://img.shields.io/badge/precogs-1.0--SNAPSHOT-blue.svg)](https://github.com/me4502/Precogs)

Guardian is an extensible AntiCheat for Sponge that gives you the flexibility to customize the checks to fit your servers needs.
Guardian also provides a service for Precogs to reduce plugin conflicts with those who integrate with the lightweight service.

The plugin is a work in progress and there are plans for more detections to be added. Make sure to come chat with us on the
[Guardian Discord](https://discord.gg/pvSFtMm) or the [IchorPowered Discord](https://discord.gg/NzfKazX).

Cheats Guardian can detect so far:

- Horizontal Speed
- Vertical Speed
- Flight
- JetPack
- Jesus (walking on water)
- Invalid Movement (auto sneak and many others)

And more coming soon.

Guardian has an optional mixin mod called [ElderGuardian](https://github.com/ichorpowered/elderguardian) that allows Guardian to access things beyond the Sponge API.
It is recommended that you use it with Guardian to improve accuracy. Some detections may also require it to work.

## Contributing

There many ways to contribute to the project. Some being...

- Creating issues for reporting bugs.
- Creating issues for reporting new cheats that are not detected or new features.
- Contributing to the plugin by making a pull request, which fixes bugs or adds new features.

Developers:

To compile the project. Simply type `gradlew`, this will licenseFormat and build the project
for you.

For code style we prefer to stick with the [Sponge Code Style](https://docs.spongepowered.org/master/en/contributing/implementation/codestyle.html).

If you're unsure about something, don't be afraid to make an issue or join the discord to chat.

## Versioning

The versioning follows:

`guardian-[sponge-major | sponge-minor | sponge-patch]-[guardian-major | guardian-minor | guardian-patch]-[guardian-sub-patch]`

e.g `guardian-7.0.0-0.1.0-01`

Ensure that the SpongeAPI version is the same for the one you are using on your server.

## Credits

 - [me4502](https://github.com/me4502) for making Precogs to spending some of his time and knowledge to improving this AntiCheat, your help is hugely appreciated.

 - [ModularFramework](https://github.com/me4502/ModularFramework) providing a modular class loading system.
 - [Precogs](https://github.com/me4502/Precogs) providing a service for plugins to integrate with the AntiCheat. 

 - Thanks to [AbilityAPI](https://github.com/AbilityAPI/abilityapi) for inspiring the sequence system.
 - Thanks to [CraftBook](https://github.com/sk89q/CraftBook) for inspiring the configuration system.


