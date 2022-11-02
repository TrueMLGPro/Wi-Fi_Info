# How To Contribute

Prerequisites:

- Familiarity with [pull requests](https://help.github.com/articles/using-pull-requests) and [issues](https://guides.github.com/features/issues/).
- Ability to use [Git](https://git-scm.com/downloads) or [GitHub Desktop](https://desktop.github.com/) (or any other Git GUI client you prefer).

## Getting Started

1. Fork this repository.
2. Clone your fork.
3. Create a branch specific to the issue you are working on.

> Make sure you have [Git](https://git-scm.com/downloads) installed. Open your cloned repo in the command line of your choice and create a branch (change the branch name used in the example below to your needs).
>
> ```console
> git checkout -b fix-memory-leak-mainactivity-java
> ```
>
> or use a GUI client (for example, [GitHub Desktop](https://desktop.github.com/)) to do it.

> For clarity, name your branch `update-xxx`, `fix-xxx` or `patch-xxx`. The `xxx` is a short description of the changes you're making. Examples include `update-readme` or `fix-typo-in-main-xml`.

4. Open up the project in Android Studio.
5. First thing to do is to create a `gradle.properties` file in the project's root directory. Its contents have to match the lines below:

```properties
# AndroidX
android.useAndroidX=true
android.enableJetifier=true
# Release build variables
KEYSTORE_LOCATION=null
KEYSTORE_PASSWORD=null
KEY_ALIAS=null
KEY_PASSWORD=null
# Optionally, you might want to add this line to reduce the build time
org.gradle.parallel=true
```

6. Sync Gradle to get the dependencies. It might take a while to complete.
7. You're ready to begin the development!
8. Once you're done: test, commit and push your changes.
9. Open a pull request and describe the changes you've made.

In particular, this community seeks the following types of contributions:

- **Ideas**: participate in an issue thread or start your own to have your voice heard.
- **Features & bug fixes**: contribute by implemeting a new feature, QoL addition/change, bug fix.
- **Writing**: contribute your expertise in an area by helping us expand the included content.
- **Copy editing**: fix typos, clarify language, and generally improve the quality of the content.
- **Formatting**: help keep content easy to read with consistent formatting.

## Conduct

We are committed to providing a friendly, safe and welcoming environment for all.

Please be kind and courteous. There's no need to be mean or rude.
Respect that people have differences of opinion and that every design or implementation choice carries a trade-off and numerous costs. There is seldom a right answer, merely an optimal answer given a set of values and circumstances.

Please keep unstructured critique to a minimum. If you have solid ideas you want to experiment with, make a fork and see how it works.

Any spamming, trolling, flaming, baiting or other attention-stealing behaviour is not welcome.

## Communication

If you want to discuss certain addition or change, ask questions, report bugs and such - feel free to join our [Discord server](https://discord.com/invite/qxE2DFr).

Likewise, [GitHub Issues](https://github.com/TrueMLGPro/Wi-Fi_Info/issues/) and [GitHub Discussions](https://github.com/TrueMLGPro/Wi-Fi_Info/discussions) are the ways for communicating about specific proposed changes to this project.
